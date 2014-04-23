package handling.channel.handler;

import clientside.MapleCharacter;
import clientside.MapleClient;
import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import server.MapleDueyActions;
import tools.Pair;
import tools.data.LittleEndianAccessor;

public class DueyHandler {

    public static final void DueyOperation(LittleEndianAccessor slea, MapleClient c) {
    }

    private static final boolean addMesoToDB(int mesos, String sName, int recipientID, boolean isOn) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO dueypackages (RecieverId, SenderName, Mesos, TimeStamp, Checked, Type) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, recipientID);
            ps.setString(2, sName);
            ps.setInt(3, mesos);
            ps.setLong(4, System.currentTimeMillis());
            ps.setInt(5, isOn ? 0 : 1);
            ps.setInt(6, 3);

            ps.executeUpdate();
            ps.close();

            return true;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return false;
    }

    private static final boolean addItemToDB(Item item, int quantity, int mesos, String sName, int recipientID, boolean isOn) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO dueypackages (RecieverId, SenderName, Mesos, TimeStamp, Checked, Type) VALUES (?, ?, ?, ?, ?, ?)", 1);
            ps.setInt(1, recipientID);
            ps.setString(2, sName);
            ps.setInt(3, mesos);
            ps.setLong(4, System.currentTimeMillis());
            ps.setInt(5, isOn ? 0 : 1);

            ps.setInt(6, item.getType());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                ItemLoader.DUEY.saveItems(Collections.singletonList(new Pair<Item, MapleInventoryType>(item, GameConstants.getInventoryType(item.getItemId()))), rs.getInt(1));
            }
            rs.close();
            ps.close();

            return true;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return false;
    }

    public static final List<MapleDueyActions> loadItems(MapleCharacter chr) {
        List packages = new LinkedList();
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM dueypackages WHERE RecieverId = ?");
            ps.setInt(1, chr.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MapleDueyActions dueypack = getItemByPID(rs.getInt("packageid"));
                dueypack.setSender(rs.getString("SenderName"));
                dueypack.setMesos(rs.getInt("Mesos"));
                dueypack.setSentTime(rs.getLong("TimeStamp"));
                packages.add(dueypack);
            }
            rs.close();
            ps.close();
            return packages;
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return null;
    }

    public static final MapleDueyActions loadSingleItem(int packageid, int charid) {
        List packages = new LinkedList();
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM dueypackages WHERE PackageId = ? and RecieverId = ?");
            ps.setInt(1, packageid);
            ps.setInt(2, charid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                MapleDueyActions dueypack = getItemByPID(packageid);
                dueypack.setSender(rs.getString("SenderName"));
                dueypack.setMesos(rs.getInt("Mesos"));
                dueypack.setSentTime(rs.getLong("TimeStamp"));
                packages.add(dueypack);
                rs.close();
                ps.close();
                return dueypack;
            }
            rs.close();
            ps.close();
            return null;
        } catch (SQLException se) {
        }
        return null;
    }

    public static final void reciveMsg(MapleClient c, int recipientId) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE dueypackages SET Checked = 0 where RecieverId = ?");
            ps.setInt(1, recipientId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private static final void removeItemFromDB(int packageid, int charid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM dueypackages WHERE PackageId = ? and RecieverId = ?");
            ps.setInt(1, packageid);
            ps.setInt(2, charid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private static final MapleDueyActions getItemByPID(int packageid) {
        try {
            Map iter = ItemLoader.DUEY.loadItems(false, packageid);
            if ((iter != null) && (iter.size() > 0)) {
                Iterator i$ = iter.values().iterator();
                if (i$.hasNext()) {
                    Pair i = (Pair) i$.next();
                    return new MapleDueyActions(packageid, (Item) i.getLeft());
                }
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return new MapleDueyActions(packageid);
    }
}