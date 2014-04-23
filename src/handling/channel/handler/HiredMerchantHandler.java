package handling.channel.handler;

import clientside.MapleCharacter;
import clientside.MapleClient;
import client.inventory.Item;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.world.World;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MerchItemPackage;
import tools.Pair;
import tools.StringUtil;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;
import tools.packet.PlayerShopPacket;

public class HiredMerchantHandler {

    public static final boolean UseHiredMerchant(MapleClient c, boolean packet) {
        if ((c.getPlayer().getMap() != null) && (c.getPlayer().getMap().allowPersonalShop())) {
            byte state = checkExistance(c.getPlayer().getAccountID(), c.getPlayer().getId());

            switch (state) {
                case 1:
                    c.getPlayer().dropMessage(1, "Please claim your items from Fredrick first.");
                    break;
                case 0:
                    boolean merch = World.hasMerchant(c.getPlayer().getAccountID(), c.getPlayer().getId());
                    if (!merch) {
                        if (c.getChannelServer().isShutdown()) {
                            c.getPlayer().dropMessage(1, "The server is about to shut down.");
                            return false;
                        }
                        if (packet) {
                            c.getSession().write(PlayerShopPacket.sendTitleBox());
                        }
                        return true;
                    }
                    c.getPlayer().dropMessage(1, "Please close the existing store and try again.");

                    break;
                default:
                    c.getPlayer().dropMessage(1, "An unknown error occured.");
            }
        } else {
            c.getSession().close(true);
        }
        return false;
    }

    private static final byte checkExistance(int accid, int cid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where accountid = ? OR characterid = ?");
            ps.setInt(1, accid);
            ps.setInt(2, cid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ps.close();
                rs.close();
                return 1;
            }
            rs.close();
            ps.close();
            return 0;
        } catch (SQLException se) {
        }
        return -1;
    }

    public static void displayMerch(MapleClient c) {
        int conv = c.getPlayer().getConversation();
        boolean merch = World.hasMerchant(c.getPlayer().getAccountID(), c.getPlayer().getId());
        MerchItemPackage pack;
        if (merch) {
            c.getPlayer().dropMessage(1, "Please close the existing store and try again.");
            c.getPlayer().setConversation(0);
        } else if (c.getChannelServer().isShutdown()) {
            c.getPlayer().dropMessage(1, "The world is going to shut down.");
            c.getPlayer().setConversation(0);
        } else if (conv == 3) {
            pack = loadItemFrom_Database(c.getPlayer().getAccountID());

            if (pack == null) {
                c.getPlayer().dropMessage(1, "You do not have any item(s) with Fredrick.");
                c.getPlayer().setConversation(0);
            } else if (pack.getItems().size() <= 0) {
                if (!check(c.getPlayer(), pack)) {
                    c.getSession().write(PlayerShopPacket.merchItem_Message(33));
                    return;
                }
                if (deletePackage(c.getPlayer().getAccountID(), pack.getPackageid(), c.getPlayer().getId())) {
                    c.getPlayer().fakeRelog();
                    c.getPlayer().gainMeso(pack.getMesos(), false);
                    c.getSession().write(PlayerShopPacket.merchItem_Message(29));
                    c.getPlayer().dropMessage(1, "You have retrieved your mesos.");
                } else {
                    c.getPlayer().dropMessage(1, "An unknown error occured.");
                }
                c.getPlayer().setConversation(0);
            } else {
                c.getSession().write(PlayerShopPacket.merchItemStore_ItemData(pack));
                MapleInventoryManipulator.checkSpace(c, conv, conv, null);
                for (Item item : pack.getItems()) {
                    if (c.getPlayer().getInventory(GameConstants.getInventoryType(item.getItemId())).isFull()) {
                        c.removeClickedNPC();
                        c.getPlayer().dropMessage(1, "Your inventory is full (Please relog)");
                        break;
                    }
                    MapleInventoryManipulator.addFromDrop(c, item, true);
                    deletePackage(c.getPlayer().getAccountID(), pack.getPackageid(), c.getPlayer().getId());

                    c.removeClickedNPC();
 
                }
              c.getPlayer().dropMessage(1, "Items have been returned to you, please relog.");
            }
        }

        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void MerchantItemStore(LittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer() == null) {
            return;
        }
        byte operation = slea.readByte();
        if ((operation == 27) || (operation == 28)) {
            requestItems(c, operation == 27);
        } else if (operation == 30) {
            c.getPlayer().setConversation(0);
        }
    }

    private static void requestItems(MapleClient c, boolean request) {
        if (c.getPlayer().getConversation() != 3) {
            return;
        }
        boolean merch = World.hasMerchant(c.getPlayer().getAccountID(), c.getPlayer().getId());
        if (merch) {
            c.getPlayer().dropMessage(1, "Please close the existing store and try again.");
            c.getPlayer().setConversation(0);
            return;
        }
        MerchItemPackage pack = loadItemFrom_Database(c.getPlayer().getAccountID());
        if (pack == null) {
            c.getPlayer().dropMessage(1, "An unknown error occured.");
            return;
        }
        if (c.getChannelServer().isShutdown()) {
            c.getPlayer().dropMessage(1, "The world is going to shut down.");
            c.getPlayer().setConversation(0);
            return;
        }
        int days = StringUtil.getDaysAmount(pack.getSavedTime(), System.currentTimeMillis());
        double percentage = days / 100.0D;
        int fee = (int) Math.ceil(percentage * pack.getMesos());
        if ((request) && (days > 0) && (percentage > 0.0D) && (pack.getMesos() > 0) && (fee > 0)) {
            c.getSession().write(PlayerShopPacket.merchItemStore((byte) 38, days, fee));
            return;
        }
        if (fee < 0) {
            c.getSession().write(PlayerShopPacket.merchItem_Message(33));
            return;
        }
        if (c.getPlayer().getMeso() < fee) {
            c.getSession().write(PlayerShopPacket.merchItem_Message(35));
            return;
        }
        if (!check(c.getPlayer(), pack)) {
            c.getSession().write(PlayerShopPacket.merchItem_Message(36));
            return;
        }
        if (deletePackage(c.getPlayer().getAccountID(), pack.getPackageid(), c.getPlayer().getId())) {
            if (fee > 0) {
                c.getPlayer().gainMeso(-fee, true);
            }
            c.getPlayer().gainMeso(pack.getMesos(), false);
            for (Item item : pack.getItems()) {
                MapleInventoryManipulator.addFromDrop(c, item, false);
            }
            c.getSession().write(PlayerShopPacket.merchItem_Message(32));
        } else {
            c.getPlayer().dropMessage(1, "An unknown error occured.");
        }
    }

    private static final boolean check(MapleCharacter chr, MerchItemPackage pack) {
        if (chr.getMeso() + pack.getMesos() < 0L) {
            return false;
        }
        byte eq = 0;
        byte use = 0;
        byte setup = 0;
        byte etc = 0;
        byte cash = 0;
        for (Item item : pack.getItems()) {
            MapleInventoryType invtype = GameConstants.getInventoryType(item.getItemId());
            if (invtype == MapleInventoryType.EQUIP) {
                eq = (byte) (eq + 1);
            } else if (invtype == MapleInventoryType.USE) {
                use = (byte) (use + 1);
            } else if (invtype == MapleInventoryType.SETUP) {
                setup = (byte) (setup + 1);
            } else if (invtype == MapleInventoryType.ETC) {
                etc = (byte) (etc + 1);
            } else if (invtype == MapleInventoryType.CASH) {
                cash = (byte) (cash + 1);
            }
        }
        if ((chr.getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < eq) || (chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() < use) || (chr.getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < setup) || (chr.getInventory(MapleInventoryType.ETC).getNumFreeSlot() < etc) || (chr.getInventory(MapleInventoryType.CASH).getNumFreeSlot() < cash)) {
            return false;
        }
        return true;
    }

    private static final boolean deletePackage(int accid, int packageid, int chrId) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE from hiredmerch where accountid = ? OR packageid = ? OR characterid = ?");
            ps.setInt(1, accid);
            ps.setInt(2, packageid);
            ps.setInt(3, chrId);
            ps.executeUpdate();
            ps.close();
            ItemLoader.HIRED_MERCHANT.saveItems(null, packageid);
            return true;
        } catch (SQLException e) {
        }
        return false;
    }

    private static final MerchItemPackage loadItemFrom_Database(int accountid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where accountid = ?");
            ps.setInt(1, accountid);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                ps.close();
                rs.close();
                return null;
            }
            int packageid = rs.getInt("PackageId");

            MerchItemPackage pack = new MerchItemPackage();
            pack.setPackageid(packageid);
            pack.setMesos(rs.getInt("Mesos"));
            pack.setSavedTime(rs.getLong("time"));

            ps.close();
            rs.close();

            Map<Long, Pair<Item, MapleInventoryType>> items = ItemLoader.HIRED_MERCHANT.loadItems(false, packageid);
            if (items != null) {
                List iters = new ArrayList();
                for (Pair z : items.values()) {
                    iters.add(z.left);
                }
                pack.setItems(iters);
            }

            return pack;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}