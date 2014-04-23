package handling.cashshop.handler;

import clientside.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import server.MTSCart;
import server.MTSStorage;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.data.LittleEndianAccessor;
import tools.packet.MTSCSPacket;

public class MTSOperation {

    public static void MTSOperation(LittleEndianAccessor slea, MapleClient c) {
        MTSCart cart = MTSStorage.getInstance().getCart(c.getPlayer().getId());

        if (slea.available() <= 0L) {
            doMTSPackets(cart, c);
            return;
        }
        byte op = slea.readByte();
        if (op == 2) {
            byte invType = slea.readByte();
            if ((invType != 1) && (invType != 2)) {
                c.getSession().write(MTSCSPacket.getMTSFailSell());
                doMTSPackets(cart, c);
                return;
            }
            int itemid = slea.readInt();
            if (slea.readByte() != 0) {
                c.getSession().write(MTSCSPacket.getMTSFailSell());
                doMTSPackets(cart, c);
                return;
            }
            slea.skip(12);
            short stars = 1;
            short quantity = 1;
            byte slot = 0;
            if (invType == 1) {
                slea.skip(32);
            } else {
                stars = slea.readShort();
            }
            slea.readMapleAsciiString();

            if (invType == 1) {
                slea.skip(50);
                slot = (byte) slea.readInt();
                slea.skip(4);
            } else {
                slea.readShort();
                if ((GameConstants.isThrowingStar(itemid)) || (GameConstants.isBullet(itemid))) {
                    slea.skip(8);
                }
                slot = (byte) slea.readInt();
                if ((GameConstants.isThrowingStar(itemid)) || (GameConstants.isBullet(itemid))) {
                    quantity = stars;
                    slea.skip(4);
                } else {
                    quantity = (short) slea.readInt();
                }
            }
            int price = slea.readInt();
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            MapleInventoryType type = GameConstants.getInventoryType(itemid);
            Item item = c.getPlayer().getInventory(type).getItem((short) slot).copy();
            if ((ii.isCash(itemid)) || (quantity <= 0) || (item == null) || (item.getQuantity() <= 0) || (item.getItemId() != itemid) || (item.getUniqueId() > 0) || (item.getQuantity() < quantity) || (price < 100) || (c.getPlayer().getMeso() < 10000L) || (cart.getNotYetSold().size() >= 10) || (item.getExpiration() > -1L) || (item.getFlag() > 0)) {
                c.getSession().write(MTSCSPacket.getMTSFailSell());
                doMTSPackets(cart, c);
                return;
            }
            if (type == MapleInventoryType.EQUIP) {
                Equip eq = (Equip) item;
                if ((eq.getState() > 0) || (eq.getEnhance() > 0) || (eq.getDurability() > -1)) {
                    c.getSession().write(MTSCSPacket.getMTSFailSell());
                    doMTSPackets(cart, c);
                    return;
                }
            }
            if ((quantity >= 50) && (item.getItemId() == 2340000)) {
                c.setMonitored(true);
            }
            long expiration = System.currentTimeMillis() + 604800000L;
            item.setQuantity(quantity);
            MTSStorage.getInstance().addToBuyNow(cart, item, price, c.getPlayer().getId(), c.getPlayer().getName(), expiration);
            MapleInventoryManipulator.removeFromSlot(c, type, (short) slot, quantity, false);
            c.getPlayer().gainMeso(-10000L, false);
            c.getSession().write(MTSCSPacket.getMTSConfirmSell());
        } else if (op == 5) {
            cart.changeInfo(slea.readInt(), slea.readInt(), slea.readInt());
        } else if (op == 6) {
            cart.changeInfo(slea.readInt(), slea.readInt(), 0);
            slea.readInt();
            cart.changeCurrentView(MTSStorage.getInstance().getSearch(slea.readInt() > 0, slea.readMapleAsciiString(), cart.getType(), cart.getTab()));
        } else if (op == 7) {
            if (!MTSStorage.getInstance().removeFromBuyNow(slea.readInt(), c.getPlayer().getId(), true)) {
                c.getSession().write(MTSCSPacket.getMTSFailCancel());
            } else {
                c.getSession().write(MTSCSPacket.getMTSConfirmCancel());
                sendMTSPackets(cart, c, true);
            }
        } else if (op == 8) {
            int id = 2147483647 - slea.readInt();
            if (id >= cart.getInventory().size()) {
                c.getPlayer().dropMessage(1, "Please try it again later.");
                sendMTSPackets(cart, c, true);
                return;
            }
            Item item = (Item) cart.getInventory().get(id);

            if ((item != null) && (item.getQuantity() > 0) && (MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner()))) {
                Item item_ = item.copy();
                short pos = MapleInventoryManipulator.addbyItem(c, item_, true);
                if (pos >= 0) {
                    if (item_.getPet() != null) {
                        item_.getPet().setInventoryPosition(pos);
                        c.getPlayer().addPet(item_.getPet());
                    }
                    cart.removeFromInventory(item);
                    c.getSession().write(MTSCSPacket.getMTSConfirmTransfer(GameConstants.getInventoryType(item_.getItemId()).getType(), pos));
                    sendMTSPackets(cart, c, true);
                    return;
                }

                c.getSession().write(MTSCSPacket.getMTSFailBuy());
            } else {
                c.getSession().write(MTSCSPacket.getMTSFailBuy());
            }
        } else if (op == 9) {
            int id = slea.readInt();
            if ((MTSStorage.getInstance().checkCart(id, c.getPlayer().getId())) && (cart.addToCart(id))) {
                c.getSession().write(MTSCSPacket.addToCartMessage(false, false));
            } else {
                c.getSession().write(MTSCSPacket.addToCartMessage(true, false));
            }
        } else if (op == 10) {
            int id = slea.readInt();
            if (cart.getCart().contains(Integer.valueOf(id))) {
                cart.removeFromCart(id);
                c.getSession().write(MTSCSPacket.addToCartMessage(false, true));
            } else {
                c.getSession().write(MTSCSPacket.addToCartMessage(true, true));
            }
        } else if ((op == 16) || (op == 17)) {
            MTSStorage.MTSItemInfo mts = MTSStorage.getInstance().getSingleItem(slea.readInt());
            if ((mts != null) && (mts.getCharacterId() != c.getPlayer().getId())) {
                if (c.getPlayer().getCSPoints(1) > mts.getRealPrice()) {
                    if (MTSStorage.getInstance().removeFromBuyNow(mts.getId(), c.getPlayer().getId(), false)) {
                        c.getPlayer().modifyCSPoints(1, -mts.getRealPrice(), false);
                        MTSStorage.getInstance().getCart(mts.getCharacterId()).increaseOwedNX(mts.getPrice());
                        c.getSession().write(MTSCSPacket.getMTSConfirmBuy());
                        sendMTSPackets(cart, c, true);
                        return;
                    }
                    c.getSession().write(MTSCSPacket.getMTSFailBuy());
                } else {
                    c.getSession().write(MTSCSPacket.getMTSFailBuy());
                }
            } else {
                c.getSession().write(MTSCSPacket.getMTSFailBuy());
            }
        } else if (!c.getPlayer().isAdmin());
        doMTSPackets(cart, c);
    }

    public static void MTSUpdate(MTSCart cart, MapleClient c) {
        int a = MTSStorage.getInstance().getCart(c.getPlayer().getId()).getSetOwedNX();
        c.getPlayer().modifyCSPoints(1, GameConstants.GMS ? a * 2 : a, false);
        c.getSession().write(MTSCSPacket.getMTSWantedListingOver(0, 0));
        doMTSPackets(cart, c);
    }

    private static void doMTSPackets(MTSCart cart, MapleClient c) {
        sendMTSPackets(cart, c, false);
    }

    private static void sendMTSPackets(MTSCart cart, MapleClient c, boolean changed) {
        c.getSession().write(MTSStorage.getInstance().getCurrentMTS(cart));
        c.getSession().write(MTSStorage.getInstance().getCurrentNotYetSold(cart));
        c.getSession().write(MTSStorage.getInstance().getCurrentTransfer(cart, changed));
        c.getSession().write(MTSCSPacket.showMTSCash(c.getPlayer()));
        c.getSession().write(MTSCSPacket.enableCSUse());
        MTSStorage.getInstance().checkExpirations();
    }
}