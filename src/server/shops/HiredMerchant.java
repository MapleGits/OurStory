package server.shops;

import clientside.MapleCharacter;
import clientside.MapleClient;
import client.inventory.Item;
import client.inventory.ItemFlag;
import constants.GameConstants;
import handling.channel.ChannelServer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Timer;
import server.maps.MapleMapObjectType;
import tools.packet.CWvsContext;
import tools.packet.PlayerShopPacket;

public class HiredMerchant extends AbstractPlayerStore {

    public ScheduledFuture<?> schedule;
    private List<String> blacklist;
    private int storeid;
    private long start;

    public HiredMerchant(MapleCharacter owner, int itemId, String desc) {
        super(owner, itemId, desc, "", 6);
        this.start = System.currentTimeMillis();
        this.blacklist = new LinkedList();
        this.schedule = Timer.EtcTimer.getInstance().schedule(new Runnable() {
            public void run() {
                if ((HiredMerchant.this.getMCOwner() != null) && (HiredMerchant.this.getMCOwner().getPlayerShop() == HiredMerchant.this)) {
                    HiredMerchant.this.getMCOwner().setPlayerShop(null);
                }
                HiredMerchant.this.removeAllVisitors(-1, -1);
                HiredMerchant.this.closeShop(true, true);
            }
        }, 86400000L);
    }

    public byte getShopType() {
        return 1;
    }

    public final void setStoreid(int storeid) {
        this.storeid = storeid;
    }

    public List<MaplePlayerShopItem> searchItem(int itemSearch) {
        List itemz = new LinkedList();
        for (MaplePlayerShopItem item : this.items) {
            if ((item.item.getItemId() == itemSearch) && (item.bundles > 0)) {
                itemz.add(item);
            }
        }
        return itemz;
    }

    public void buy(MapleClient c, int item, short quantity) {
        MaplePlayerShopItem pItem = (MaplePlayerShopItem) this.items.get(item);
        Item shopItem = pItem.item;
        Item newItem = shopItem.copy();
        short perbundle = newItem.getQuantity();
        int theQuantity = pItem.price * quantity;
        newItem.setQuantity((short) (quantity * perbundle));

        short flag = newItem.getFlag();

        if (ItemFlag.KARMA_EQ.check(flag)) {
            newItem.setFlag((short) (flag - ItemFlag.KARMA_EQ.getValue()));
        } else if (ItemFlag.KARMA_USE.check(flag)) {
            newItem.setFlag((short) (flag - ItemFlag.KARMA_USE.getValue()));
        }

        if (MapleInventoryManipulator.checkSpace(c, newItem.getItemId(), newItem.getQuantity(), newItem.getOwner())) {
            long gainmeso = getMeso() + theQuantity - GameConstants.EntrustedStoreTax(theQuantity);
            if (gainmeso > 0L) {
                setMeso(gainmeso);
                MaplePlayerShopItem tmp171_169 = pItem;
                tmp171_169.bundles = ((short) (tmp171_169.bundles - quantity));
                MapleInventoryManipulator.addFromDrop(c, newItem, false);
                this.bought.add(new AbstractPlayerStore.BoughtItem(newItem.getItemId(), quantity, theQuantity, c.getPlayer().getName()));
                c.getPlayer().gainMeso(-theQuantity, false);
                saveItems();
                MapleCharacter chr = getMCOwnerWorld();
                if (chr != null) {
                    chr.dropMessage(-5, "Item " + MapleItemInformationProvider.getInstance().getName(newItem.getItemId()) + " (" + perbundle + ") x " + quantity + " has sold in the Hired Merchant. Quantity left: " + pItem.bundles);
                }
            } else {
                c.getPlayer().dropMessage(1, "The seller has too many mesos.");
                c.getSession().write(CWvsContext.enableActions());
            }
        } else {
            c.getPlayer().dropMessage(1, "Your inventory is full.");
            c.getSession().write(CWvsContext.enableActions());
        }
    }

    public void closeShop(boolean saveItems, boolean remove) {
        if (this.schedule != null) {
            this.schedule.cancel(false);
        }
        if (saveItems) {
            saveItems();
            this.items.clear();
        }
        if (remove) {
            ChannelServer.getInstance(this.channel).removeMerchant(this);
            getMap().broadcastMessage(PlayerShopPacket.destroyHiredMerchant(getOwnerId()));
        }
        getMap().removeMapObject(this);
        this.schedule = null;
    }

    public int getTimeLeft() {
        return (int) ((System.currentTimeMillis() - this.start) / 1000L);
    }

    public final int getStoreId() {
        return this.storeid;
    }

    public MapleMapObjectType getType() {
        return MapleMapObjectType.HIRED_MERCHANT;
    }

    public void sendDestroyData(MapleClient client) {
        if (isAvailable()) {
            client.getSession().write(PlayerShopPacket.destroyHiredMerchant(getOwnerId()));
        }
    }

    public void sendSpawnData(MapleClient client) {
        if (isAvailable()) {
            client.getSession().write(PlayerShopPacket.spawnHiredMerchant(this));
        }
    }

    public final boolean isInBlackList(String bl) {
        return this.blacklist.contains(bl);
    }

    public final void addBlackList(String bl) {
        this.blacklist.add(bl);
    }

    public final void removeBlackList(String bl) {
        this.blacklist.remove(bl);
    }

    public final void sendBlackList(MapleClient c) {
        c.getSession().write(PlayerShopPacket.MerchantBlackListView(this.blacklist));
    }

    public final void sendVisitor(MapleClient c) {
        c.getSession().write(PlayerShopPacket.MerchantVisitorView(this.visitors));
    }
}