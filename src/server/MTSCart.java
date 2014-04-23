package server;

import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import tools.Pair;

public class MTSCart
        implements Serializable {

    private static final long serialVersionUID = 231541893513373578L;
    private int characterId;
    private int tab = 1;
    private int type = 0;
    private int page = 0;
    private List<Item> transfer = new ArrayList();
    private List<Integer> cart = new ArrayList();
    private List<Integer> notYetSold = new ArrayList(10);
    private List<Integer> currentViewingItems = new ArrayList();
    private int owedNX = 0;

    public MTSCart(int characterId) throws SQLException {
        this.characterId = characterId;
        for (Pair<Item, MapleInventoryType> item : ItemLoader.MTS_TRANSFER.loadItems(false, characterId).values()) {
            this.transfer.add(item.getLeft());
        }
        loadCart();
        loadNotYetSold();
    }

    public List<Item> getInventory() {
        return this.transfer;
    }

    public void addToInventory(Item item) {
        this.transfer.add(item);
    }

    public void removeFromInventory(Item item) {
        this.transfer.remove(item);
    }

    public List<Integer> getCart() {
        return this.cart;
    }

    public boolean addToCart(int car) {
        if (!this.cart.contains(Integer.valueOf(car))) {
            this.cart.add(Integer.valueOf(car));
            return true;
        }
        return false;
    }

    public void removeFromCart(int car) {
        for (int i = 0; i < this.cart.size(); i++) {
            if (((Integer) this.cart.get(i)).intValue() == car) {
                this.cart.remove(i);
            }
        }
    }

    public List<Integer> getNotYetSold() {
        return this.notYetSold;
    }

    public void addToNotYetSold(int car) {
        this.notYetSold.add(Integer.valueOf(car));
    }

    public void removeFromNotYetSold(int car) {
        for (int i = 0; i < this.notYetSold.size(); i++) {
            if (((Integer) this.notYetSold.get(i)).intValue() == car) {
                this.notYetSold.remove(i);
            }
        }
    }

    public final int getSetOwedNX() {
        int on = this.owedNX;
        this.owedNX = 0;
        return on;
    }

    public void increaseOwedNX(int newNX) {
        this.owedNX += newNX;
    }

    public void save() throws SQLException {
        List itemsWithType = new ArrayList();

        for (Item item : getInventory()) {
            itemsWithType.add(new Pair(item, GameConstants.getInventoryType(item.getItemId())));
        }

        ItemLoader.MTS_TRANSFER.saveItems(itemsWithType, this.characterId);
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("DELETE FROM mts_cart WHERE characterid = ?");
        ps.setInt(1, this.characterId);
        ps.execute();
        ps.close();
        ps = con.prepareStatement("INSERT INTO mts_cart VALUES(DEFAULT, ?, ?)");
        ps.setInt(1, this.characterId);
        for (Iterator i$ = this.cart.iterator(); i$.hasNext();) {
            int i = ((Integer) i$.next()).intValue();
            ps.setInt(2, i);
            ps.executeUpdate();
        }
        if (this.owedNX > 0) {
            ps.setInt(2, -this.owedNX);
            ps.executeUpdate();
        }
        ps.close();
    }

    public void loadCart() throws SQLException {
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM mts_cart WHERE characterid = ?");
        ps.setInt(1, this.characterId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int iId = rs.getInt("itemid");
            if (iId < 0) {
                this.owedNX -= iId;
            } else if (MTSStorage.getInstance().check(iId)) {
                this.cart.add(Integer.valueOf(iId));
            }
        }
        rs.close();
        ps.close();
    }

    public void loadNotYetSold() throws SQLException {
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM mts_items WHERE characterid = ?");
        ps.setInt(1, this.characterId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int pId = rs.getInt("id");
            if (MTSStorage.getInstance().check(pId)) {
                this.notYetSold.add(Integer.valueOf(pId));
            }
        }
        rs.close();
        ps.close();
    }

    public void changeInfo(int tab, int type, int page) {
        if ((tab != this.tab) || (type != this.type)) {
            refreshCurrentView(tab, type);
        }
        this.tab = tab;
        this.type = type;
        this.page = page;
    }

    public int getTab() {
        return this.tab;
    }

    public int getType() {
        return this.type;
    }

    public int getPage() {
        return this.page;
    }

    public List<Integer> getCurrentViewPage() {
        List ret = new ArrayList();
        int size = this.currentViewingItems.size() / 16 + (this.currentViewingItems.size() % 16 > 0 ? 1 : 0);
        if (this.page > size) {
            this.page = 0;
        }
        for (int i = this.page * 16; (i < this.page * 16 + 16)
                && (this.currentViewingItems.size() > i); i++) {
            ret.add(this.currentViewingItems.get(i));
        }

        return ret;
    }

    public List<Integer> getCurrentView() {
        return this.currentViewingItems;
    }

    public void refreshCurrentView() {
        refreshCurrentView(this.tab, this.type);
    }

    public void refreshCurrentView(int newTab, int newType) {
        this.currentViewingItems.clear();
        Iterator i$;
        if (newTab == 1) {
            this.currentViewingItems = MTSStorage.getInstance().getBuyNow(newType);
        } else if (newTab == 4) {
            for (i$ = this.cart.iterator(); i$.hasNext();) {
                int i = ((Integer) i$.next()).intValue();
                if ((newType == 0) || (GameConstants.getInventoryType(i).getType() == newType)) {
                    this.currentViewingItems.add(Integer.valueOf(i));
                }
            }
        }
    }

    public void changeCurrentView(List<Integer> items) {
        this.currentViewingItems.clear();
        this.currentViewingItems = items;
    }
}