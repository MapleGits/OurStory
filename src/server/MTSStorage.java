package server;

import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import tools.Pair;
import tools.packet.MTSCSPacket;

public class MTSStorage {

    private static final long serialVersionUID = 231541893513228L;
    private long lastUpdate = System.currentTimeMillis();
    private final Map<Integer, MTSCart> idToCart;
    private final AtomicInteger packageId;
    private final Map<Integer, MTSItemInfo> buyNow;
    private static MTSStorage instance;
    private boolean end = false;
    private ReentrantReadWriteLock mutex;
    private ReentrantReadWriteLock cart_mutex;

    public MTSStorage() {
        this.idToCart = new LinkedHashMap();
        this.buyNow = new LinkedHashMap();
        this.packageId = new AtomicInteger(1);
        this.mutex = new ReentrantReadWriteLock();
        this.cart_mutex = new ReentrantReadWriteLock();
    }

    public static final MTSStorage getInstance() {
        return instance;
    }

    public static final void load() {
        if (instance == null) {
            instance = new MTSStorage();
            instance.loadBuyNow();
        }
    }

    public final boolean check(int packageid) {
        return getSingleItem(packageid) != null;
    }

    public final boolean checkCart(int packageid, int charID) {
        MTSItemInfo item = getSingleItem(packageid);
        return (item != null) && (item.getCharacterId() != charID);
    }

    public final MTSItemInfo getSingleItem(int packageid) {
        this.mutex.readLock().lock();
        try {
            return (MTSItemInfo) this.buyNow.get(Integer.valueOf(packageid));
        } finally {
            this.mutex.readLock().unlock();
        }
    }

    public final void addToBuyNow(MTSCart cart, Item item, int price, int cid, String seller, long expiration) {
        this.mutex.writeLock().lock();
        int id;
        try {
            id = this.packageId.incrementAndGet();
            this.buyNow.put(Integer.valueOf(id), new MTSItemInfo(price, item, seller, id, cid, expiration));
        } finally {
            this.mutex.writeLock().unlock();
        }
        cart.addToNotYetSold(id);
    }

    public final boolean removeFromBuyNow(int id, int cidBought, boolean check) {
        Item item = null;
        this.mutex.writeLock().lock();
        try {
            if (this.buyNow.containsKey(Integer.valueOf(id))) {
                MTSItemInfo r = (MTSItemInfo) this.buyNow.get(Integer.valueOf(id));
                if ((!check) || (r.getCharacterId() == cidBought)) {
                    item = r.getItem();
                    this.buyNow.remove(Integer.valueOf(id));
                }
            }
        } finally {
            this.mutex.writeLock().unlock();
        }
        if (item != null) {
            this.cart_mutex.readLock().lock();
            try {
                for (Map.Entry c : this.idToCart.entrySet()) {
                    ((MTSCart) c.getValue()).removeFromCart(id);
                    ((MTSCart) c.getValue()).removeFromNotYetSold(id);
                    if (((Integer) c.getKey()).intValue() == cidBought) {
                        ((MTSCart) c.getValue()).addToInventory(item);
                    }
                }
            } finally {
                this.cart_mutex.readLock().unlock();
            }
        }
        return item != null;
    }

    private final void loadBuyNow() {
        int lastPackage = 0;

        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM mts_items WHERE tab = 1");
            ResultSet rs = ps.executeQuery();
            int cId;
            while (rs.next()) {
                lastPackage = rs.getInt("id");
                cId = rs.getInt("characterid");
                if (!this.idToCart.containsKey(Integer.valueOf(cId))) {
                    this.idToCart.put(Integer.valueOf(cId), new MTSCart(cId));
                }
                Map<Long, Pair<Item, MapleInventoryType>> items = ItemLoader.MTS.loadItems(false, lastPackage);
                if ((items != null) && (items.size() > 0)) {
                    for (Pair i : items.values()) {
                        this.buyNow.put(Integer.valueOf(lastPackage), new MTSItemInfo(rs.getInt("price"), (Item) i.getLeft(), rs.getString("seller"), lastPackage, cId, rs.getLong("expiration")));
                    }
                }
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.packageId.set(lastPackage);
    }

    public final void saveBuyNow(boolean isShutDown) {
        if (this.end) {
            return;
        }
        this.end = isShutDown;
        if (isShutDown) {
            System.out.println("Saving MTS...");
        }
        final Map<Integer, ArrayList<Item>> expire = new HashMap<Integer, ArrayList<Item>>();
        final List<Integer> toRemove = new ArrayList<Integer>();
        final long now = System.currentTimeMillis();
        final Map<Integer, ArrayList<Pair<Item, MapleInventoryType>>> items = new HashMap<Integer, ArrayList<Pair<Item, MapleInventoryType>>>();
        final Connection con = DatabaseConnection.getConnection();
        mutex.writeLock().lock(); //lock wL so rL will also be locked
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM mts_items WHERE tab = 1");
            ps.execute();
            ps.close();
            ps = con.prepareStatement("INSERT INTO mts_items VALUES (?, ?, ?, ?, ?, ?)");
            for (MTSItemInfo m : buyNow.values()) {
                if (now > m.getEndingDate()) {
                    if (!expire.containsKey(m.getCharacterId())) {
                        expire.put(m.getCharacterId(), new ArrayList<Item>());
                    }
                    expire.get(m.getCharacterId()).add(m.getItem());
                    toRemove.add(m.getId());
                    items.put(m.getId(), null); //destroy from the mtsitems.
                } else {
                    ps.setInt(1, m.getId());
                    ps.setByte(2, (byte) 1);
                    ps.setInt(3, m.getPrice());
                    ps.setInt(4, m.getCharacterId());
                    ps.setString(5, m.getSeller());
                    ps.setLong(6, m.getEndingDate());
                    ps.executeUpdate();
                    if (!items.containsKey(m.getId())) {
                        items.put(m.getId(), new ArrayList<Pair<Item, MapleInventoryType>>());
                    }
                    items.get(m.getId()).add(new Pair<Item, MapleInventoryType>(m.getItem(), GameConstants.getInventoryType(m.getItem().getItemId())));
                }
            }
            for (int i : toRemove) {
                buyNow.remove(i);
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mutex.writeLock().unlock();
        }
        if (isShutDown) {
            System.out.println("Saving MTS items...");
        }
        try {
            for (Entry<Integer, ArrayList<Pair<Item, MapleInventoryType>>> ite : items.entrySet()) {
                ItemLoader.MTS.saveItems(ite.getValue(), ite.getKey());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (isShutDown) {
            System.out.println("Saving MTS carts...");
        }
        cart_mutex.writeLock().lock();
        try {
            for (Entry<Integer, MTSCart> c : idToCart.entrySet()) {
                for (int i : toRemove) {
                    c.getValue().removeFromCart(i);
                    c.getValue().removeFromNotYetSold(i);
                }
                if (expire.containsKey(c.getKey())) {
                    for (Item item : expire.get(c.getKey())) {
                        c.getValue().addToInventory(item);
                    }
                }
                c.getValue().save();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cart_mutex.writeLock().unlock();
        }
        lastUpdate = System.currentTimeMillis();
    }

    public final void checkExpirations() {
        if (System.currentTimeMillis() - this.lastUpdate > 3600000L) {
            saveBuyNow(false);
        }
    }

    public final MTSCart getCart(int characterId) {
        this.cart_mutex.readLock().lock();
        MTSCart ret;
        try {
            ret = (MTSCart) this.idToCart.get(Integer.valueOf(characterId));
        } finally {
            this.cart_mutex.readLock().unlock();
        }
        if (ret == null) {
            this.cart_mutex.writeLock().lock();
            try {
                ret = new MTSCart(characterId);
                this.idToCart.put(Integer.valueOf(characterId), ret);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                this.cart_mutex.writeLock().unlock();
            }
        }
        return ret;
    }

    public final byte[] getCurrentMTS(MTSCart cart) {
        this.mutex.readLock().lock();
        try {
            return MTSCSPacket.sendMTS(getMultiItems(cart.getCurrentView(), cart.getPage()), cart.getTab(), cart.getType(), cart.getPage(), cart.getCurrentView().size());
        } finally {
            this.mutex.readLock().unlock();
        }
    }

    public final byte[] getCurrentNotYetSold(MTSCart cart) {
        this.mutex.readLock().lock();
        try {
            List nys = new ArrayList();

            List nyss = new ArrayList(cart.getNotYetSold());
            for (Iterator i$ = nyss.iterator(); i$.hasNext();) {
                int i = ((Integer) i$.next()).intValue();
                MTSItemInfo r = (MTSItemInfo) this.buyNow.get(Integer.valueOf(i));
                if (r == null) {
                    cart.removeFromNotYetSold(i);
                } else {
                    nys.add(r);
                }
            }
            return MTSCSPacket.getNotYetSoldInv(nys);
        } finally {
            this.mutex.readLock().unlock();
        }
    }

    public final byte[] getCurrentTransfer(MTSCart cart, boolean changed) {
        return MTSCSPacket.getTransferInventory(cart.getInventory(), changed);
    }

    public final List<MTSItemInfo> getMultiItems(List<Integer> items, int pageNumber) {
        List ret = new ArrayList();

        List cartt = new ArrayList(items);
        if (pageNumber > cartt.size() / 16 + (cartt.size() % 16 > 0 ? 1 : 0)) {
            pageNumber = 0;
        }
        int maxSize = Math.min(cartt.size(), pageNumber * 16 + 16);
        int minSize = Math.min(cartt.size(), pageNumber * 16);
        for (int i = minSize; (i < maxSize)
                && (cartt.size() > i); i++) {
            MTSItemInfo r = (MTSItemInfo) this.buyNow.get(cartt.get(i));
            if (r == null) {
                items.remove(i);
                cartt.remove(i);
            } else {
                ret.add(r);
            }

        }

        return ret;
    }

    public final List<Integer> getBuyNow(int type) {
        this.mutex.readLock().lock();
        try {
            if (type == 0) {
                return new ArrayList(this.buyNow.keySet());
            }

            Object ret = new ArrayList(this.buyNow.values());
            List rett = new ArrayList();

            for (int i = 0; i < ((List) ret).size(); i++) {
                MTSItemInfo r = (MTSItemInfo) ((List) ret).get(i);
                if ((r != null) && (GameConstants.getInventoryType(r.getItem().getItemId()).getType() == type)) {
                    rett.add(Integer.valueOf(r.getId()));
                }
            }
            return rett;
        } finally {
            this.mutex.readLock().unlock();
        }
    }

    public final List<Integer> getSearch(boolean item, String name, int type, int tab) {
        this.mutex.readLock().lock();
        try {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if ((tab != 1) || (name.length() <= 0)) {
                return new ArrayList();
            }
            name = name.toLowerCase();
            Object ret = new ArrayList(this.buyNow.values());
            List rett = new ArrayList();

            for (int i = 0; i < ((List) ret).size(); i++) {
                MTSItemInfo r = (MTSItemInfo) ((List) ret).get(i);
                if ((r != null) && ((type == 0) || (GameConstants.getInventoryType(r.getItem().getItemId()).getType() == type))) {
                    String thename = item ? ii.getName(r.getItem().getItemId()) : r.getSeller();
                    if ((thename != null) && (thename.toLowerCase().contains(name))) {
                        rett.add(Integer.valueOf(r.getId()));
                    }
                }
            }
            return rett;
        } finally {
            this.mutex.readLock().unlock();
        }
    }

    private final List<MTSItemInfo> getCartItems(MTSCart cart) {
        return getMultiItems(cart.getCart(), cart.getPage());
    }

    public static class MTSItemInfo {

        private int price;
        private Item item;
        private String seller;
        private int id;
        private int cid;
        private long date;

        public MTSItemInfo(int price, Item item, String seller, int id, int cid, long date) {
            this.item = item;
            this.price = price;
            this.seller = seller;
            this.id = id;
            this.cid = cid;
            this.date = date;
        }

        public Item getItem() {
            return this.item;
        }

        public int getPrice() {
            return this.price;
        }

        public int getRealPrice() {
            return this.price + getTaxes();
        }

        public int getTaxes() {
            return 0 + this.price * 5 / 100;
        }

        public int getId() {
            return this.id;
        }

        public int getCharacterId() {
            return this.cid;
        }

        public long getEndingDate() {
            return this.date;
        }

        public String getSeller() {
            return this.seller;
        }
    }
}