package scripting;

import clientside.MapleCharacter;
import clientside.MapleClient;
import clientside.PlayerStats;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.channel.ChannelServer;
import java.awt.Point;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import server.MapleCarnivalFactory;
import server.MapleCarnivalFactory.MCSkill;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleReactor;
import server.maps.ReactorDropEntry;
import tools.packet.CField;

public class ReactorActionManager extends AbstractPlayerInteraction {

    private MapleReactor reactor;

    public ReactorActionManager(MapleClient c, MapleReactor reactor) {
        super(c, reactor.getReactorId(), c.getPlayer().getMapId());
        this.reactor = reactor;
    }

    public void dropItems() {
        dropItems(false, 0, 0, 0, 0);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso) {
        dropItems(meso, mesoChance, minMeso, maxMeso, 0);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso, int minItems) {
        List<ReactorDropEntry> chances = ReactorScriptManager.getInstance().getDrops(this.reactor.getReactorId());
        List<ReactorDropEntry> items = new LinkedList();

        if ((meso)
                && (Math.random() < 1.0D / mesoChance)) {
            items.add(new ReactorDropEntry(0, mesoChance, -1));
        }

        int numItems = 0;

        Iterator iter = chances.iterator();

        while (iter.hasNext()) {
            ReactorDropEntry d = (ReactorDropEntry) iter.next();
            if ((Math.random() < 1.0D / d.chance) && ((d.questid <= 0) || (getPlayer().getQuestStatus(d.questid) == 1))) {
                numItems++;
                items.add(d);
            }

        }

        while (items.size() < minItems) {
            items.add(new ReactorDropEntry(0, mesoChance, -1));
            numItems++;
        }
        Point dropPos = this.reactor.getPosition();

        dropPos.x -= 12 * numItems;

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (ReactorDropEntry d : items) {
            if (d.itemId == 0) {
                int range = maxMeso - minMeso;
                int mesoDrop = Randomizer.nextInt(range) + minMeso * ChannelServer.getInstance(getClient().getChannel()).getMesoRate();
                this.reactor.getMap().spawnMesoDrop(mesoDrop, dropPos, this.reactor, getPlayer(), false, (byte) 0);
            } else {
                Item drop;
                if (GameConstants.getInventoryType(d.itemId) != MapleInventoryType.EQUIP) {
                    drop = new Item(d.itemId, (short) 0, (short) 1, (short) 0);
                } else {
                    drop = ii.randomizeStats((Equip) ii.getEquipById(d.itemId));
                }
                drop.setGMLog("Dropped from reactor " + this.reactor.getReactorId() + " on map " + getPlayer().getMapId());
                this.reactor.getMap().spawnItemDrop(this.reactor, getPlayer(), drop, dropPos, false, false);
            }
            dropPos.x += 25;
        }
    }

    public void dropSingleItem(int itemId) {
        Item drop;
        if (GameConstants.getInventoryType(itemId) != MapleInventoryType.EQUIP) {
            drop = new Item(itemId, (short) 0, (short) 1, (short) 0);
        } else {
            drop = MapleItemInformationProvider.getInstance().randomizeStats((Equip) MapleItemInformationProvider.getInstance().getEquipById(itemId));
        }
        drop.setGMLog("Dropped from reactor " + this.reactor.getReactorId() + " on map " + getPlayer().getMapId());
        this.reactor.getMap().spawnItemDrop(this.reactor, getPlayer(), drop, this.reactor.getPosition(), false, false);
    }

    public void spawnNpc(int npcId) {
        spawnNpc(npcId, getPosition());
    }

    public Point getPosition() {
        Point pos = this.reactor.getPosition();
        pos.y -= 10;
        return pos;
    }

    public MapleReactor getReactor() {
        return this.reactor;
    }

    public void spawnZakum() {
        this.reactor.getMap().spawnZakum(getPosition().x, getPosition().y);
    }

    public void spawnFakeMonster(int id) {
        spawnFakeMonster(id, 1, getPosition());
    }

    public void spawnFakeMonster(int id, int x, int y) {
        spawnFakeMonster(id, 1, new Point(x, y));
    }

    public void spawnFakeMonster(int id, int qty) {
        spawnFakeMonster(id, qty, getPosition());
    }

    public void spawnFakeMonster(int id, int qty, int x, int y) {
        spawnFakeMonster(id, qty, new Point(x, y));
    }

    private void spawnFakeMonster(int id, int qty, Point pos) {
        for (int i = 0; i < qty; i++) {
            this.reactor.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
        }
    }

    public void killAll() {
        this.reactor.getMap().killAllMonsters(true);
    }

    public void killMonster(int monsId) {
        this.reactor.getMap().killMonster(monsId);
    }

    public void spawnMonster(int id) {
        spawnMonster(id, 1, getPosition());
    }

    public void spawnMonster(int id, int qty) {
        spawnMonster(id, qty, getPosition());
    }

    public void dispelAllMonsters(int num) {
        MapleCarnivalFactory.MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
        if (skil != null) {
            for (MapleMonster mons : getMap().getAllMonstersThreadsafe()) {
                mons.dispelSkill(skil.getSkill());
            }
        }
    }

    public void cancelHarvest(boolean succ) {
        getPlayer().setFatigue((byte) (getPlayer().getFatigue() + 1));
        getPlayer().getMap().broadcastMessage(getPlayer(), CField.showHarvesting(getPlayer().getId(), 0), false);
        getPlayer().getMap().broadcastMessage(CField.harvestResult(getPlayer().getId(), succ));
    }

    public void doHarvest() {
        if (getPlayer().getFatigue() < (GameConstants.GMS ? 200 : 100)) {
            if ((getPlayer().getStat().harvestingTool > 0) && (getReactor().getTruePosition().distanceSq(getPlayer().getTruePosition()) <= 10000.0D));
        } else {
            return;
        }

        int pID = getReactor().getReactorId() < 200000 ? 92000000 : 92010000;
        String pName = getReactor().getReactorId() < 200000 ? "Herbalism" : "Mining";
        int he = getPlayer().getProfessionLevel(pID);
        if (he <= 0) {
            return;
        }
        Item item = getInventory(1).getItem((short) getPlayer().getStat().harvestingTool);
        if (item != null) {
            if ((item.getItemId() / 10000 | 0x0) == (getReactor().getReactorId() < 200000 ? 150 : 151));
        } else {
            return;
        }

        int hm = getReactor().getReactorId() % 100;
        int successChance = 90 + (he - hm) * 10;
        if (getReactor().getReactorId() % 100 == 10) {
            hm = 1;
            successChance = 100;
        } else if (getReactor().getReactorId() % 100 == 11) {
            hm = 10;
            successChance -= 40;
        }
        getPlayer().getStat().checkEquipDurabilitys(getPlayer(), -1, true);
        int masteryIncrease = (hm - he) * 2 + 20;
        boolean succ = randInt(100) < successChance;
        if (!succ) {
            masteryIncrease /= 10;
            dropSingleItem(getReactor().getReactorId() < 200000 ? 4022023 : 4010010);
        } else {
            dropItems();
            if (getReactor().getReactorId() < 200000) {
                addTrait("sense", 5);
                if (Randomizer.nextInt(10) == 0) {
                    dropSingleItem(2440000);
                }
                if (Randomizer.nextInt(100) == 0) {
                    dropSingleItem(4032933);
                }
            } else {
                addTrait("insight", 5);
                if (Randomizer.nextInt(10) == 0) {
                    dropSingleItem(2440001);
                }
            }
        }
        cancelHarvest(succ);
        playerMessage(-5, pName + "'s Mastery increased. (+" + masteryIncrease + ")");
        if (getPlayer().addProfessionExp(pID, masteryIncrease)) {
            playerMessage(-5, pName + " has gained a level.");
        }
    }
}