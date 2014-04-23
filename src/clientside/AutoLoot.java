/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package clientside;

/**
 *
 * @author WiseHero
 */
import handling.channel.handler.InventoryHandler;
import handling.world.MaplePartyCharacter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleMonster;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;

public class AutoLoot extends Thread {

    private ConcurrentLinkedQueue<MapleMapObject> autoloots;
    private MapleMapItem item;
    private MapleMapObject object;
    private MapleCharacter chr;
    private MapleClient c;

    public AutoLoot(MapleCharacter chr) {
        this.chr = chr;
        this.autoloots = new ConcurrentLinkedQueue<>();
        this.c = chr.getClient();
    }

    public synchronized void addObject(MapleMapObject ob) {
        final MapleMapItem mapitem = (MapleMapItem) ob;
        final Lock lock = mapitem.getLock();
        lock.lock();
        try {
            if (mapitem.isPickedUp() || mapitem.getQuest() > 0 && this.chr.getQuestStatus(mapitem.getQuest()) != 1 || mapitem.getOwner() != this.chr.getId() && ((!mapitem.isPlayerDrop() && mapitem.getDropType() == 0) || (mapitem.isPlayerDrop() && this.chr.getMap().getEverlast())) || !mapitem.isPlayerDrop() && mapitem.getDropType() == 1 && mapitem.getOwner() != this.chr.getId()) {
                return;
            }
            if (mapitem.getMeso() > 0) {
                this.autoloots.add(ob);
            } else {
                if (!(this.c.getPlayer().inPVP() && Integer.parseInt(this.c.getPlayer().getEventInstance().getProperty("ice")) == this.c.getPlayer().getId()) && !(InventoryHandler.useItem(c, mapitem.getItemId())) && mapitem.getItemId() / 10000 != 291) {
                    this.autoloots.add(ob);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public synchronized void run() {
        try {
            while (!interrupted()) {
                this.wait(5000);
                List<MapleMapItem> items = chr.getMap().getAllItemsThreadsafe();
                for (MapleMapItem i : items) {
                    this.addObject(i);
                }
                while (this.autoloots.peek() != null) {
                    this.object = this.autoloots.poll();
                    this.item = (MapleMapItem) this.object;
                    final Lock lock = this.item.getLock();
                    lock.lock();
                    try {
                        if (this.item.getMeso() > 0) {
                            if (this.chr.getParty() != null && this.item.getOwner() != this.chr.getId()) {
                                final List<MapleCharacter> toGive = new LinkedList<>();
                                final int splitMeso = this.item.getMeso() * 40 / 100;
                                for (MaplePartyCharacter z : this.chr.getParty().getMembers()) {
                                    MapleCharacter m = this.chr.getMap().getCharacterById(z.getId());
                                    if (m != null && m.getId() != this.chr.getId()) {
                                        toGive.add(m);
                                    }
                                }
                                for (final MapleCharacter m : toGive) {
                                    m.gainMeso(splitMeso / toGive.size() + (0), true);
                                }
                                this.chr.gainMeso(this.item.getMeso() - splitMeso, true);
                            } else {
                                this.chr.gainMeso(this.item.getMeso(), true);
                            }
                            if (!interrupted()) {
                                InventoryHandler.removeItem(this.chr, this.item, this.object);
                                this.wait(20);
                            }
                        } else {
                            if (MapleInventoryManipulator.checkSpace(c, this.item.getItemId(), this.item.getItem().getQuantity(), this.item.getItem().getOwner()) && !interrupted()) {
                                MapleInventoryManipulator.addFromDrop(this.chr.getClient(), this.item.getItem(), true, this.item.getDropper() instanceof MapleMonster);
                                InventoryHandler.removeItem(this.chr, this.item, this.object);
                                this.wait(20);
                            }
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
        }
    }
}