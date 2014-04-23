package server;

import clientside.MapleClient;
import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import database.DatabaseException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import tools.Pair;
import tools.packet.CField;
import tools.packet.CField.NPCPacket;

public class MapleStorage
        implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private int id;
    private int accountId;
    private List<Item> items;
    private long meso;
    private int lastNPC = 0;
    private byte slots;
    private boolean changed = false;
    private Map<MapleInventoryType, List<Item>> typeItems = new EnumMap(MapleInventoryType.class);

    private MapleStorage(int id, byte slots, int meso, int accountId) {
        this.id = id;
        this.slots = slots;
        this.items = new LinkedList();
        this.meso = meso;
        this.accountId = accountId;
    }

    public static int create(int id) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("INSERT INTO storages (accountid, slots, meso) VALUES (?, ?, ?)", 1);
        ps.setInt(1, id);
        ps.setInt(2, 48);
        ps.setInt(3, 0);
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            int storageid = rs.getInt(1);
            ps.close();
            rs.close();
            return storageid;
        }
        ps.close();
        rs.close();
        throw new DatabaseException("Inserting char failed.");
    }

    public static MapleStorage loadStorage(int id) {
        MapleStorage ret = null;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM storages WHERE accountid = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int storeId = rs.getInt("storageid");
                ret = new MapleStorage(storeId, rs.getByte("slots"), rs.getInt("meso"), id);
                rs.close();
                ps.close();

                for (Pair<Item, MapleInventoryType> mit : ItemLoader.STORAGE.loadItems(false, id).values()) {
                    ret.items.add(mit.getLeft());
                }
            } else {
                int storeId = create(id);
                ret = new MapleStorage(storeId, (byte) 48, 0, id);
                rs.close();
                ps.close();
            }
        } catch (SQLException ex) {
            System.err.println("Error loading storage" + ex);
        }
        return ret;
    }

    public void saveToDB() {
        if (!this.changed) {
            return;
        }
        try {
            Connection con = DatabaseConnection.getConnection();

            PreparedStatement ps = con.prepareStatement("UPDATE storages SET slots = ?, meso = ? WHERE storageid = ?");
            ps.setInt(1, this.slots);
            ps.setLong(2, this.meso);
            ps.setInt(3, this.id);
            ps.executeUpdate();
            ps.close();

            List listing = new ArrayList();
            for (Item item : this.items) {
                listing.add(new Pair(item, GameConstants.getInventoryType(item.getItemId())));
            }
            ItemLoader.STORAGE.saveItems(listing, this.accountId);
        } catch (SQLException ex) {
            System.err.println("Error saving storage" + ex);
        }
    }

    public Item takeOut(byte slot) {
        if ((slot >= this.items.size()) || (slot < 0)) {
            return null;
        }
        this.changed = true;
        Item ret = (Item) this.items.remove(slot);
        MapleInventoryType type = GameConstants.getInventoryType(ret.getItemId());
        this.typeItems.put(type, filterItems(type));
        return ret;
    }

    public void store(Item item) {
        this.changed = true;
        this.items.add(item);
        MapleInventoryType type = GameConstants.getInventoryType(item.getItemId());
        this.typeItems.put(type, filterItems(type));
    }

    public void arrange() { //i believe gms does by itemID
        Collections.sort(items, new Comparator<Item>() {
            public int compare(Item o1, Item o2) {
                if (o1.getItemId() < o2.getItemId()) {
                    return -1;
                } else if (o1.getItemId() == o2.getItemId()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (MapleInventoryType type : MapleInventoryType.values()) {
            typeItems.put(type, items);
        }
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    private List<Item> filterItems(MapleInventoryType type) {
        List ret = new ArrayList();

        for (Item item : this.items) {
            if (GameConstants.getInventoryType(item.getItemId()) == type) {
                ret.add(item);
            }
        }
        return ret;
    }

    public byte getSlot(MapleInventoryType type, byte slot) {
        byte ret = 0;
        List it = (List) this.typeItems.get(type);
        if ((it == null) || (slot >= it.size()) || (slot < 0)) {
            return -1;
        }
        for (Item item : this.items) {
            if (item == it.get(slot)) {
                return ret;
            }
            ret = (byte) (ret + 1);
        }
        return -1;
    }

    public void sendStorage(MapleClient c, int npcId) {
        // sort by inventorytype to avoid confusion
        lastNPC = npcId;
        Collections.sort(items, new Comparator<Item>() {
            public int compare(Item o1, Item o2) {
                if (GameConstants.getInventoryType(o1.getItemId()).getType() < GameConstants.getInventoryType(o2.getItemId()).getType()) {
                    return -1;
                } else if (GameConstants.getInventoryType(o1.getItemId()) == GameConstants.getInventoryType(o2.getItemId())) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        for (MapleInventoryType type : MapleInventoryType.values()) {
            typeItems.put(type, items);
        }
        c.getSession().write(NPCPacket.getStorage(npcId, slots, items, meso));
    }

    public void update(MapleClient c) {
        c.getSession().write(CField.NPCPacket.arrangeStorage(this.slots, this.items, true));
    }

    public void sendStored(MapleClient c, MapleInventoryType type) {
        c.getSession().write(CField.NPCPacket.storeStorage(this.slots, type, (Collection) this.typeItems.get(type)));
    }

    public void sendTakenOut(MapleClient c, MapleInventoryType type) {
        c.getSession().write(CField.NPCPacket.takeOutStorage(this.slots, type, (Collection) this.typeItems.get(type)));
    }

    public long getMeso() {
        return this.meso;
    }

    public Item findById(int itemId) {
        for (Item item : this.items) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public void setMeso(long meso) {
        if (meso < 0L) {
            return;
        }
        this.changed = true;
        this.meso = meso;
    }

    public void sendMeso(MapleClient c) {
        c.getSession().write(CField.NPCPacket.mesoStorage(this.slots, this.meso));
    }

    public boolean isFull() {
        return this.items.size() >= this.slots;
    }

    public int getSlots() {
        return this.slots;
    }

    public void increaseSlots(byte gain) {
        this.changed = true;
        this.slots = ((byte) (this.slots + gain));
    }

    public void setSlots(byte set) {
        this.changed = true;
        this.slots = set;
    }

    public void close() {
        this.typeItems.clear();
    }
}