package handling.cashshop.handler;

import clientside.MapleCharacter;
import clientside.MapleCharacterUtil;
import clientside.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.CharacterTransfer;
import handling.world.World;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import server.CashItemFactory;
import server.CashItemInfo;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.FileoutputUtil;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.MTSCSPacket;

public class CashShopOperation {

    private static List<cashinformation> dataCache = new LinkedList();
    private static List<cashinformation> dataCache1 = new LinkedList();

    public static final void BuyCashItem(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        int action = slea.readByte();
        //  System.out.println("Actioon: " + action);
        if (action == 0) {
            slea.skip(2);
            CouponCode(slea.readMapleAsciiString(), c);
        } else if (action == 2) {
            slea.skip(1);
            int toCharge = GameConstants.GMS ? slea.readInt() : 1;
            CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());

            if ((item != null) && (chr.getCSPoints(toCharge) >= item.getPrice())) {
                if (!item.genderEquals(c.getPlayer().getGender())) {
                    c.getSession().write(MTSCSPacket.sendCSFail(167));
                    doCSPackets(c);
                    return;
                }
                if (item.getId() == 5211046) {
                    c.getSession().write(CWvsContext.serverNotice(1, "You cannot purchase this item through cash shop."));
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                if (item.getId() == 5211047) {
                    c.getSession().write(CWvsContext.serverNotice(1, "You cannot purchase this item through cash shop."));
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                if (item.getId() == 5211048) {
                    c.getSession().write(CWvsContext.serverNotice(1, "You cannot purchase this item through cash shop."));
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                if (item.getId() == 5050100) {
                    c.getSession().write(CWvsContext.serverNotice(1, "You cannot purchase this item through cash shop."));
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                if (item.getId() == 5051001) {
                    c.getSession().write(CWvsContext.serverNotice(1, "You cannot purchase this item through cash shop."));
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                if (c.getPlayer().getCashInventory().getItemsSize() >= 100) {
                    c.getSession().write(MTSCSPacket.sendCSFail(178));
                    doCSPackets(c);
                    return;
                }

                chr.modifyCSPoints(toCharge, -item.getPrice(), false);
                Item itemz = chr.getCashInventory().toItem(item);
                if ((itemz != null) && (itemz.getUniqueId() > 0) && (itemz.getItemId() == item.getId()) && (itemz.getQuantity() == item.getCount())) {
                    MapleInventoryManipulator.addbyItem(c, itemz);
                    c.getSession().write(MTSCSPacket.showBoughtCSItem(itemz, item.getSN(), c.getAccID()));
                } else {
                    c.getSession().write(MTSCSPacket.sendCSFail(0));
                }
            } else {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
            }
        } else if ((action == 4)) {

            slea.readMapleAsciiString();
            System.out.println(slea.toString());
            CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            int amount = slea.readInt();

            String partnerName = slea.readMapleAsciiString();
            String msg = slea.readMapleAsciiString();
            if ((item == null) || (c.getPlayer().getCSPoints(4) < item.getPrice()) || (msg.length() > 73) || (msg.length() < 1)) {
                //     c.getSession().write(MTSCSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            Triple info = MapleCharacterUtil.getInfoByName(partnerName, c.getPlayer().getWorld());
            if ((info == null) || (((Integer) info.getLeft()).intValue() <= 0) || (((Integer) info.getLeft()).intValue() == c.getPlayer().getId()) || (((Integer) info.getMid()).intValue() == c.getAccID())) {
                //    c.getSession().write(MTSCSPacket.sendCSFail(162));
                doCSPackets(c);
                return;
            }


            c.getPlayer().getCashInventory().gift(((Integer) info.getLeft()).intValue(), c.getPlayer().getName(), msg, item.getSN(), MapleInventoryIdentifier.getInstance());
            c.getPlayer().modifyCSPoints(4, -item.getPrice(), false);
            c.getSession().write(MTSCSPacket.sendGift(item.getPrice(), item.getId(), item.getCount(), partnerName, action == 36));
        } else if (action == 5) {
            chr.clearWishlist();
            if (slea.available() < 40L) {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            int[] wishlist = new int[10];
            for (int i = 0; i < 10; i++) {
                wishlist[i] = slea.readInt();
            }
            chr.setWishlist(wishlist);
        } else if (action == 6) {
            slea.skip(1);
            int toCharge = GameConstants.GMS ? slea.readInt() : 1;
            boolean coupon = slea.readByte() > 0;
            if (coupon) {
                MapleInventoryType type = getInventoryType(slea.readInt());
                if (chr.getCSPoints(toCharge) >= (GameConstants.GMS ? 6000 : 12000)) {
                    if (chr.getInventory(type).getSlotLimit() < 89) {
                        chr.modifyCSPoints(toCharge, GameConstants.GMS ? -6000 : -12000, false);
                        chr.getInventory(type).addSlot((byte) 8);
                        chr.dropMessage(1, "Slots has been increased to " + chr.getInventory(type).getSlotLimit());
                    }
                }
                c.getSession().write(MTSCSPacket.sendCSFail(164));
            } else {
                MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
                if (chr.getCSPoints(toCharge) >= (GameConstants.GMS ? 4000 : 8000)) {
                    if (chr.getInventory(type).getSlotLimit() < 93) {
                        chr.modifyCSPoints(toCharge, GameConstants.GMS ? -4000 : -8000, false);
                        chr.getInventory(type).addSlot((byte) 4);
                        chr.dropMessage(1, "Slots has been increased to " + chr.getInventory(type).getSlotLimit());
                    }
                }
                c.getSession().write(MTSCSPacket.sendCSFail(164));
            }
        } else if (action == 8) {
            slea.skip(1);
            int toCharge = GameConstants.GMS ? slea.readInt() : 1;
            int coupon = slea.readByte() > 0 ? 2 : 1;
            if ((chr.getCSPoints(toCharge) >= (GameConstants.GMS ? 4000 : 8000) * coupon) && (chr.getStorage().getSlots() < 49 - 4 * coupon)) {
                chr.modifyCSPoints(toCharge, (GameConstants.GMS ? -4000 : -8000) * coupon, false);
                chr.getStorage().increaseSlots((byte) (4 * coupon));
                chr.getStorage().saveToDB();
                chr.dropMessage(1, "Storage slots increased to: " + chr.getStorage().getSlots());
            } else {
                c.getSession().write(MTSCSPacket.sendCSFail(164));
            }
        } else if (action == 9) {
            slea.skip(1);
            int toCharge = GameConstants.GMS ? slea.readInt() : 1;
            CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            int slots = c.getCharacterSlots();
            if ((item == null) || (c.getPlayer().getCSPoints(toCharge) < item.getPrice()) || (slots > 15) || (item.getId() != 5430000)) {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            if (c.gainCharacterSlot()) {
                c.getPlayer().modifyCSPoints(toCharge, -item.getPrice(), false);
                chr.dropMessage(1, "Character slots increased to: " + (slots + 1));
            } else {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
            }
        } else if (action == 14) {
//            Item item = c.getPlayer().getCashInventory().findByCashId((int) slea.readLong());
//            if ((item != null) && (item.getQuantity() > 0) && (MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner()))) {
//                Item item_ = item.copy();
//                short pos = MapleInventoryManipulator.addbyItem(c, item_, true);
//                if (pos >= 0) {
//                    if (item_.getPet() != null) {
//                        item_.getPet().setInventoryPosition(pos);
//                        c.getPlayer().addPet(item_.getPet());
//                    }
//                    c.getPlayer().getCashInventory().removeFromInventory(item);
//                    c.getSession().write(MTSCSPacket.confirmFromCSInventory(item_, pos));
//                } else {
//                    c.getSession().write(MTSCSPacket.sendCSFail(177));
//                }
//            } else {
//                c.getSession().write(MTSCSPacket.sendCSFail(177));
//            }
        } else if (action == 15) {
//            int uniqueid = (int) slea.readLong();
//            MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
//            Item item = c.getPlayer().getInventory(type).findByUniqueId(uniqueid);
//            if ((item != null) && (item.getQuantity() > 0) && (item.getUniqueId() > 0) && (c.getPlayer().getCashInventory().getItemsSize() < 100)) {
//                Item item_ = item.copy();
//                MapleInventoryManipulator.removeFromSlot(c, type, item.getPosition(), item.getQuantity(), false);
//                if (item_.getPet() != null) {
//                    c.getPlayer().removePetCS(item_.getPet());
//                }
//                item_.setPosition((short) 0);
//                c.getPlayer().getCashInventory().addToInventory(item_);
//            } else {
//                c.getSession().write(MTSCSPacket.sendCSFail(177));
//            }
        } else if ((action == 35) || (action == 41)) {
            slea.readMapleAsciiString();
            int toCharge = slea.readInt();
            CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            int amount = slea.readInt();
            String partnerName = slea.readMapleAsciiString();
            String msg = slea.readMapleAsciiString();
            if ((item == null) || (!GameConstants.isEffectRing(item.getId())) || (c.getPlayer().getCSPoints(toCharge) < item.getPrice()) || (msg.length() > 73) || (msg.length() < 1)) {
                // c.getSession().write(MTSCSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            if (!item.genderEquals(c.getPlayer().getGender())) {
                // c.getSession().write(MTSCSPacket.sendCSFail(166));
                doCSPackets(c);
                return;
            }
            if (c.getPlayer().getCashInventory().getItemsSize() >= 100) {
                // c.getSession().write(MTSCSPacket.sendCSFail(177));
                doCSPackets(c);
                return;
            }

            Triple info = MapleCharacterUtil.getInfoByName(partnerName, c.getPlayer().getWorld());
            if ((info == null) || (((Integer) info.getLeft()).intValue() <= 0) || (((Integer) info.getLeft()).intValue() == c.getPlayer().getId())) {
                //   c.getSession().write(MTSCSPacket.sendCSFail(180));
                doCSPackets(c);
                return;
            }
            if (((Integer) info.getMid()).intValue() == c.getAccID()) {
                //    c.getSession().write(MTSCSPacket.sendCSFail(163));
                doCSPackets(c);
                return;
            }
            if ((((Integer) info.getRight()).intValue() == c.getPlayer().getGender()) && (action == 30)) {
                //  c.getSession().write(MTSCSPacket.sendCSFail(161));
                doCSPackets(c);
                return;
            }

            int err = MapleRing.createRing(item.getId(), c.getPlayer(), partnerName, msg, ((Integer) info.getLeft()).intValue(), item.getSN());

            if (err != 1) {
                //  c.getSession().write(MTSCSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            c.getPlayer().modifyCSPoints(toCharge, -amount, false);
            c.getPlayer().dropMessage(1, "Purchase successful.");
            /*   Item itemz = chr.getCashInventory().toItem(item);
             if ((itemz != null) && (itemz.getUniqueId() > 0) && (itemz.getItemId() == item.getId()) && (itemz.getQuantity() == item.getCount())) {
             chr.getCashInventory().addToInventory(itemz);
             c.getSession().write(MTSCSPacket.showBoughtCSItem(itemz, item.getSN(), c.getAccID()));
             } else {
             c.getSession().write(MTSCSPacket.sendCSFail(0));
             }*/
        } else if (action == 36) {
            System.out.println(slea.toString());
            slea.skip(1);
            int toCharge = slea.readInt();
            int blo = slea.readInt();
            int amount = slea.readInt();


            List<Integer> ccc = null;

            ccc = CashItemFactory.getInstance().getPackageItems2(blo);
//            if ((ccc == null) || (c.getPlayer().getCSPoints(toCharge) < amount)) {
//                c.getSession().write(MTSCSPacket.sendCSFail(0));
//                doCSPackets(c);
//                return;
//            }
//            if (c.getPlayer().getCashInventory().getItemsSize() >= 100 - ccc.size()) {
//                c.getSession().write(MTSCSPacket.sendCSFail(177));
//                doCSPackets(c);
//                return;
//            }

            Map<Integer, Item> ccz = new HashMap<>();
            for (int i : ccc) {

                // final CashItemInfo cii = CashItemFactory.getInstance().getItem(i);

                //  Item itemz = c.getPlayer().getCashInventory().toItem(cii);

                // ccz.put(i, itemz);
                if (GameConstants.isPet(i)) {

                    int uniqueid = MapleInventoryIdentifier.getInstance();



                    Item item = new Item(i, (byte) 0, (short) 1, (byte) 0, uniqueid);

                    item.setExpiration(2475606994921L);




                    final MaplePet pet = MaplePet.createPet(i, uniqueid);
                    item.setPet(pet);


                    MapleInventoryManipulator.addbyItem(c, item);
                } else {
                    MapleInventoryManipulator.addById(c, i, (short) 1, "CS");
                }
            }
            chr.modifyCSPoints(toCharge, -amount, false);
            c.getSession().write(MTSCSPacket.showBoughtCSPackage(ccz, c.getAccID()));
        } else if (action == 0xFF) {
            CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
            if ((item == null) || (!MapleItemInformationProvider.getInstance().isQuestItem(item.getId()))) {
                c.getSession().write(MTSCSPacket.sendCSFail(0));
                doCSPackets(c);
                return;
            }
            if (c.getPlayer().getMeso() < item.getPrice()) {
                c.getSession().write(MTSCSPacket.sendCSFail(184));
                doCSPackets(c);
                return;
            }
            if (c.getPlayer().getInventory(GameConstants.getInventoryType(item.getId())).getNextFreeSlot() < 0) {
                c.getSession().write(MTSCSPacket.sendCSFail(177));
                doCSPackets(c);
                return;
            }

            byte pos = MapleInventoryManipulator.addId(c, item.getId(), (short) item.getCount(), null, "Cash shop: quest item on " + FileoutputUtil.CurrentReadable_Date());
            if (pos < 0) {
                c.getSession().write(MTSCSPacket.sendCSFail(177));
                doCSPackets(c);
                return;
            }
            chr.gainMeso(-item.getPrice(), false);
            c.getSession().write(MTSCSPacket.showBoughtCSQuestItem(item.getPrice(), (short) item.getCount(), pos, item.getId()));
        } else if (action == 101) {
            int cashtype = slea.readInt() % 1000000 / 100;
            if (dataCache1 != null) {
                dataCache1.clear();
            }
            for (cashinformation entry : dataCache) {
                if (entry.type == cashtype) {
                    dataCache1.add(entry);
                }
            }
            c.getSession().write(MTSCSPacket.cash_send_item(cashtype, dataCache1));
        } else if (action == 48) {
            c.getSession().write(MTSCSPacket.updatePurchaseRecord());
        } else if ((action == 105) || (action == 106)) {
            int sn = slea.readInt();
            likeItems(action == 105 ? 1 : -1, sn);

        } else if (action == 109) { // 'favorite' tab
        } else if (action == 113) {

            slea.skip(1);
            int toCharge = 1;
            List<CashItemInfo> items = new ArrayList();
            byte amount = slea.readByte();
            for (int i = 0; i < amount; i++) {
                CashItemInfo item = CashItemFactory.getInstance().getItem(slea.readInt());
                items.add(item);
            }

            /*     if ((item != null) && (chr.getCSPoints(toCharge) >= item.getPrice())) {
             if (!item.genderEquals(c.getPlayer().getGender())) {
             c.getSession().write(MTSCSPacket.sendCSFail(167));
             doCSPackets(c);
             return;
             }
             if (item.getId() == 5211046) {
             c.getSession().write(CWvsContext.serverNotice(1, "You cannot purchase this item through cash shop."));
             c.getSession().write(CWvsContext.enableActions());
             return;
             }
             if (item.getId() == 5211047) {
             c.getSession().write(CWvsContext.serverNotice(1, "You cannot purchase this item through cash shop."));
             c.getSession().write(CWvsContext.enableActions());
             return;
             }
             if (item.getId() == 5211048) {
             c.getSession().write(CWvsContext.serverNotice(1, "You cannot purchase this item through cash shop."));
             c.getSession().write(CWvsContext.enableActions());
             return;
             }
             if (item.getId() == 5050100) {
             c.getSession().write(CWvsContext.serverNotice(1, "You cannot purchase this item through cash shop."));
             c.getSession().write(CWvsContext.enableActions());
             return;
             }
             if (item.getId() == 5051001) {
             c.getSession().write(CWvsContext.serverNotice(1, "You cannot purchase this item through cash shop."));
             c.getSession().write(CWvsContext.enableActions());
             return;
             }
             if (c.getPlayer().getCashInventory().getItemsSize() >= 100) {
             c.getSession().write(MTSCSPacket.sendCSFail(178));
             doCSPackets(c);
             return;
             }

             chr.modifyCSPoints(toCharge, -item.getPrice(), false);
             Item itemz = chr.getCashInventory().toItem(item);
             if ((itemz != null) && (itemz.getUniqueId() > 0) && (itemz.getItemId() == item.getId()) && (itemz.getQuantity() == item.getCount())) {
             MapleInventoryManipulator.addbyItem(c, itemz);
             c.getSession().write(MTSCSPacket.showBoughtCSItem(itemz, item.getSN(), c.getAccID()));
             } else {
             c.getSession().write(MTSCSPacket.sendCSFail(0));
             }
             } else {
             c.getSession().write(MTSCSPacket.sendCSFail(0));
             }
        
             * */
        } else if (action == 102) {
        } else if (action == 103) {
            slea.skip(1);
            slea.readInt();
        } else if (action == 107) {
        } else if (action == 112) {
        } else {
            int uniqueid;
            if (action == 91) {
                uniqueid = (int) slea.readLong();
            } else {
                System.out.println("New Action: " + action + " Remaining: " + slea.toString());
                c.getSession().write(MTSCSPacket.sendCSFail(0));
            }
        }
        doCSPackets(c);
    }

    public static void LeaveCS(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        CashShopServer.getPlayerStorageMTS().deregisterPlayer(chr);
        CashShopServer.getPlayerStorage().deregisterPlayer(chr);
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, c.getSessionIPAddress());

        try {

            World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), c.getChannel());
            c.getSession().write(CField.getChannelChange(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1])));
        } finally {
            final String s = c.getSessionIPAddress();
            LoginServer.addIPAuth(s.substring(s.indexOf('/') + 1, s.length()));
            chr.saveToDB(false, true);
            c.setPlayer(null);
            c.setReceiving(false);
            c.getSession().close(true);
        }
    }

    public static void EnterCS(final int playerid, final MapleClient c) {
        CharacterTransfer transfer = CashShopServer.getPlayerStorage().getPendingCharacter(playerid);
        boolean mts = false;
        if (transfer == null) {
            transfer = CashShopServer.getPlayerStorageMTS().getPendingCharacter(playerid);
            mts = true;
            if (transfer == null) {
                c.getSession().close(true);
                System.out.println("Something fk with transfer");
                return;
            }
        }
        MapleCharacter chr = MapleCharacter.ReconstructChr(transfer, c, false);

        c.setPlayer(chr);
        c.setAccID(chr.getAccountID());

        if (!c.CheckIPAddress()) { // Remote hack
            System.out.println("Something fk with ip hack");
            c.getSession().close(true);
            return;
        }

        final int state = c.getLoginState();
        boolean allowLogin = false;
        if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL || state == MapleClient.LOGIN_NOTLOGGEDIN) {
            allowLogin = !World.isCharacterListConnected(c.loadCharacterNames(c.getWorld()));
        }
        if (!allowLogin) {
            c.setPlayer(null);
            c.getSession().close(true);
            return;
        }
        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());
        CashShopServer.getPlayerStorage().registerPlayer(chr);
        c.getSession().write(MTSCSPacket.warpCS(c));
        c.getSession().write(MTSCSPacket.warpCS1(c, 1));
        c.getSession().write(MTSCSPacket.warpCS1(c, 2));
        c.getSession().write(MTSCSPacket.warpCS1(c, 3));
        c.getSession().write(MTSCSPacket.warpCS1(c, 4));
        c.getSession().write(MTSCSPacket.warpCS1(c, 5));
        c.getSession().write(MTSCSPacket.warpCS1(c, 6));
        CSUpdate(c);
    }

    public static void CSUpdate(MapleClient c) {
        c.getSession().write(MTSCSPacket.getCSGifts(c));
        doCSPackets(c);
    }

    public static void CouponCode(String code, MapleClient c) {
        if (code.length() <= 0) {
            return;
        }
        Triple info = null;
        try {
            info = MapleCharacterUtil.getNXCodeInfo(code);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if ((info != null) && (((Boolean) info.left).booleanValue())) {
            int type = ((Integer) info.mid).intValue();
            int item = ((Integer) info.right).intValue();
            try {
                MapleCharacterUtil.setNXCodeUsed(c.getPlayer().getName(), code);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Map itemz = new HashMap();
            int maplePoints = 0;
            int mesos = 0;
            switch (type) {
                case 1:
                case 2:
                    c.getPlayer().modifyCSPoints(type, item, false);
                    maplePoints = item;
                    break;
                case 3:
                    CashItemInfo itez = CashItemFactory.getInstance().getItem(item);
                    if (itez == null) {
                        c.getSession().write(MTSCSPacket.sendCSFail(0));
                        return;
                    }
                    byte slot = MapleInventoryManipulator.addId(c, itez.getId(), (short) 1, "", "Cash shop: coupon code on " + FileoutputUtil.CurrentReadable_Date());
                    if (slot <= -1) {
                        c.getSession().write(MTSCSPacket.sendCSFail(0));
                        return;
                    }
                    itemz.put(Integer.valueOf(item), c.getPlayer().getInventory(GameConstants.getInventoryType(item)).getItem((short) slot));

                    break;
                case 4:
                    c.getPlayer().gainMeso(item, false);
                    mesos = item;
            }

            c.getSession().write(MTSCSPacket.showCouponRedeemedItem(itemz, mesos, maplePoints, c));
        } else {
            c.getSession().write(MTSCSPacket.sendCSFail(info == null ? 167 : 165));
        }
    }

    private static final MapleInventoryType getInventoryType(int id) {
        switch (id) {
            case 50200093:
                return MapleInventoryType.EQUIP;
            case 50200094:
                return MapleInventoryType.USE;
            case 50200197:
                return MapleInventoryType.SETUP;
            case 50200095:
                return MapleInventoryType.ETC;
        }
        return MapleInventoryType.UNDEFINED;
    }

    public static void likeItems(int like, int sn) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("update cashshop_item set like=like+" + like + " where sn=?");
            ps.setInt(1, sn);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
        }
    }

    public static void runCashItems() {
        try {
            dataCache.clear();
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM cashshop_item");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                initItemInformation(rs);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
    }

    public static void initItemInformation(ResultSet sqlItemData) throws SQLException {
        cashinformation ret = new cashinformation();
        int type = sqlItemData.getInt("type");
        ret.type = type;
        ret.sn = sqlItemData.getInt("sn");
        ret.itemid = sqlItemData.getInt("itemid");
        ret.price_old = sqlItemData.getInt("price_old");
        ret.price_new = sqlItemData.getInt("price_new");
        ret.quantity = sqlItemData.getInt("quantity");
        ret.gender = sqlItemData.getInt("gender");
        ret.day = sqlItemData.getInt("day");
        ret.like = sqlItemData.getInt("like");
        ret.pack = sqlItemData.getInt("pack");
        ret.packhead = sqlItemData.getString("packhead").split(",");
        dataCache.add(ret);
    }

    public static void discsitem(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if (dataCache.size() == 0) {
            runCashItems();
        }
        Byte action = Byte.valueOf(slea.readByte());


    }

    public static final void doCSPackets(MapleClient c) {
        c.getSession().write(MTSCSPacket.getCSInventory(c));
        c.getSession().write(MTSCSPacket.showNXMapleTokens(c.getPlayer()));
        c.getSession().write(MTSCSPacket.enableCSUse());
        c.getSession().write(MTSCSPacket.enableCSUse1());
        c.getPlayer().getCashInventory().checkExpire(c);
    }
}