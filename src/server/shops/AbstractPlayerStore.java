package server.shops;

import clientside.MapleCharacter;
import clientside.MapleClient;
import client.inventory.Item;
import client.inventory.ItemLoader;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.World;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.Pair;
import tools.packet.PlayerShopPacket;

public abstract class AbstractPlayerStore extends MapleMapObject
        implements IMaplePlayerShop {

    protected boolean open = false;
    protected boolean available = false;
    protected String ownerName;
    protected String des;
    protected String pass;
    protected int ownerId;
    protected int owneraccount;
    protected int itemId;
    protected int channel;
    protected int map;
    protected AtomicLong meso = new AtomicLong(0L);
    protected WeakReference<MapleCharacter>[] chrs;
    protected List<String> visitors = new LinkedList();
    protected List<BoughtItem> bought = new LinkedList();
    protected List<MaplePlayerShopItem> items = new LinkedList();

    public AbstractPlayerStore(MapleCharacter owner, int itemId, String desc, String pass, int slots) {
        setPosition(owner.getTruePosition());
        this.ownerName = owner.getName();
        this.ownerId = owner.getId();
        this.owneraccount = owner.getAccountID();
        this.itemId = itemId;
        this.des = desc;
        this.pass = pass;
        this.map = owner.getMapId();
        this.channel = owner.getClient().getChannel();
        this.chrs = new WeakReference[slots];
        for (int i = 0; i < this.chrs.length; i++) {
            this.chrs[i] = new WeakReference(null);
        }
    }

    public int getMaxSize() {
        return this.chrs.length + 1;
    }

    public int getSize() {
        return getFreeSlot() == -1 ? getMaxSize() : getFreeSlot();
    }

    public void broadcastToVisitors(byte[] packet) {
        broadcastToVisitors(packet, true);
    }

    public void broadcastToVisitors(byte[] packet, boolean owner) {
        for (WeakReference chr : this.chrs) {
            if ((chr != null) && (chr.get() != null)) {
                ((MapleCharacter) chr.get()).getClient().getSession().write(packet);
            }
        }
        if ((getShopType() != 1) && (owner) && (getMCOwner() != null)) {
            getMCOwner().getClient().getSession().write(packet);
        }
    }

    public void broadcastToVisitors(byte[] packet, int exception) {
        for (WeakReference chr : this.chrs) {
            if ((chr != null) && (chr.get() != null) && (getVisitorSlot((MapleCharacter) chr.get()) != exception)) {
                ((MapleCharacter) chr.get()).getClient().getSession().write(packet);
            }
        }
        if ((getShopType() != 1) && (getMCOwner() != null) && (exception != this.ownerId)) {
            getMCOwner().getClient().getSession().write(packet);
        }
    }

    public long getMeso() {
        return this.meso.get();
    }

    public void setMeso(long meso) {
        this.meso.set(meso);
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return this.open;
    }

    public boolean saveItems() {
        if (getShopType() != 1) {
            return false;
        }
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM hiredmerch WHERE accountid = ? OR characterid = ?");
            ps.setInt(1, this.owneraccount);
            ps.setInt(2, this.ownerId);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO hiredmerch (characterid, accountid, Mesos, time) VALUES (?, ?, ?, ?)", 1);
            ps.setInt(1, this.ownerId);
            ps.setInt(2, this.owneraccount);
            ps.setLong(3, this.meso.get());
            ps.setLong(4, System.currentTimeMillis());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (!rs.next()) {
                rs.close();
                ps.close();
                throw new RuntimeException("Error, adding merchant to DB");
            }
            int packageid = rs.getInt(1);
            rs.close();
            ps.close();
            List iters = new ArrayList();

            for (MaplePlayerShopItem pItems : this.items) {
                if ((pItems.item != null) && (pItems.bundles > 0) && ((pItems.item.getQuantity() > 0) || (GameConstants.isRechargable(pItems.item.getItemId())))) {
                    Item item = pItems.item.copy();
                    item.setQuantity((short) (item.getQuantity() * pItems.bundles));
                    iters.add(new Pair(item, GameConstants.getInventoryType(item.getItemId())));
                }
            }
            ItemLoader.HIRED_MERCHANT.saveItems(iters, packageid);
            return true;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return false;
    }

    public MapleCharacter getVisitor(int num) {
        return (MapleCharacter) this.chrs[num].get();
    }

    public void update() {
        if (isAvailable()) {
            if (getShopType() == 1) {
                getMap().broadcastMessage(PlayerShopPacket.updateHiredMerchant((HiredMerchant) this));
            } else if (getMCOwner() != null) {
                getMap().broadcastMessage(PlayerShopPacket.sendPlayerShopBox(getMCOwner()));
            }
        }
    }

    public void addVisitor(MapleCharacter visitor) {
        int i = getFreeSlot();
        if (i > 0) {
            if (getShopType() >= 3) {
                broadcastToVisitors(PlayerShopPacket.getMiniGameNewVisitor(visitor, i, (MapleMiniGame) this));
            } else {
                broadcastToVisitors(PlayerShopPacket.shopVisitorAdd(visitor, i));
            }
            this.chrs[(i - 1)] = new WeakReference(visitor);
            if ((!isOwner(visitor)) && (!this.visitors.contains(visitor.getName()))) {
                this.visitors.add(visitor.getName());
            }
            if (i == 3) {
                update();
            }
        }
    }

    public void removeVisitor(MapleCharacter visitor) {
        byte slot = getVisitorSlot(visitor);
        boolean shouldUpdate = getFreeSlot() == -1;
        if (slot > 0) {
            broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(slot), slot);
            this.chrs[(slot - 1)] = new WeakReference(null);
            if (shouldUpdate) {
                update();
            }
        }
    }

    public byte getVisitorSlot(MapleCharacter visitor) {
        for (byte i = 0; i < this.chrs.length; i = (byte) (i + 1)) {
            if ((this.chrs[i] != null) && (this.chrs[i].get() != null) && (((MapleCharacter) this.chrs[i].get()).getId() == visitor.getId())) {
                return (byte) (i + 1);
            }
        }
        if (visitor.getId() == this.ownerId) {
            return 0;
        }
        return -1;
    }

    public void removeAllVisitors(int error, int type) {
        for (int i = 0; i < this.chrs.length; i++) {
            MapleCharacter visitor = getVisitor(i);
            if (visitor != null) {
                if (type != -1) {
                    visitor.getClient().getSession().write(PlayerShopPacket.shopErrorMessage(error, type));
                }
                broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(getVisitorSlot(visitor)), getVisitorSlot(visitor));
                visitor.setPlayerShop(null);
                this.chrs[i] = new WeakReference(null);
            }
        }
        update();
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public int getOwnerId() {
        return this.ownerId;
    }

    public int getOwnerAccId() {
        return this.owneraccount;
    }

    public String getDescription() {
        if (this.des == null) {
            return "";
        }
        return this.des;
    }

    public List<Pair<Byte, MapleCharacter>> getVisitors() {
        List chrz = new LinkedList();
        for (byte i = 0; i < this.chrs.length; i = (byte) (i + 1)) {
            if ((this.chrs[i] != null) && (this.chrs[i].get() != null)) {
                chrz.add(new Pair(Byte.valueOf((byte) (i + 1)), this.chrs[i].get()));
            }
        }
        return chrz;
    }

    public List<MaplePlayerShopItem> getItems() {
        return this.items;
    }

    public void addItem(MaplePlayerShopItem item) {
        this.items.add(item);
    }

    public boolean removeItem(int item) {
        return false;
    }

    public void removeFromSlot(int slot) {
        this.items.remove(slot);
    }

    public byte getFreeSlot() {
        for (byte i = 0; i < this.chrs.length; i = (byte) (i + 1)) {
            if ((this.chrs[i] == null) || (this.chrs[i].get() == null)) {
                return (byte) (i + 1);
            }
        }
        return -1;
    }

    public int getItemId() {
        return this.itemId;
    }

    public boolean isOwner(MapleCharacter chr) {
        return (chr.getId() == this.ownerId) && (chr.getName().equals(this.ownerName));
    }

    public String getPassword() {
        if (this.pass == null) {
            return "";
        }
        return this.pass;
    }

    public void sendDestroyData(MapleClient client) {
    }

    public void sendSpawnData(MapleClient client) {
    }

    public MapleMapObjectType getType() {
        return MapleMapObjectType.SHOP;
    }

    public MapleCharacter getMCOwnerWorld() {
        int ourChannel = World.Find.findChannel(this.ownerId);
        if (ourChannel <= 0) {
            return null;
        }
        return ChannelServer.getInstance(ourChannel).getPlayerStorage().getCharacterById(this.ownerId);
    }

    public MapleCharacter getMCOwnerChannel() {
        return ChannelServer.getInstance(this.channel).getPlayerStorage().getCharacterById(this.ownerId);
    }

    public MapleCharacter getMCOwner() {
        return getMap().getCharacterById(this.ownerId);
    }

    public MapleMap getMap() {
        return ChannelServer.getInstance(this.channel).getMapFactory().getMap(this.map);
    }

    public int getGameType() {
        if (getShopType() == 1) {
            return 5;
        }
        if (getShopType() == 2) {
            return 4;
        }
        if (getShopType() == 3) {
            return 1;
        }
        if (getShopType() == 4) {
            return 2;
        }
        return 0;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public void setAvailable(boolean b) {
        this.available = b;
    }

    public List<BoughtItem> getBoughtItems() {
        return this.bought;
    }

    public static final class BoughtItem {

        public int id;
        public int quantity;
        public int totalPrice;
        public String buyer;

        public BoughtItem(int id, int quantity, int totalPrice, String buyer) {
            this.id = id;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.buyer = buyer;
        }
    }
}