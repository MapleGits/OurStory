package handling.channel.handler;

import clientside.MapleCharacter;
import clientside.MapleClient;
import clientside.MapleQuestStatus;
import clientside.RockPaperScissors;
import clientside.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import handling.SendPacketOpcode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import scripting.NPCConversationManager;
import scripting.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShop;
import server.MapleStorage;
import server.ServerProperties;
import server.life.MapleNPC;
import server.maps.MapScriptMethods;
import server.quest.MapleQuest;
import tools.Pair;
import tools.data.LittleEndianAccessor;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class NPCHandler {

    public static void NPCAnimation(LittleEndianAccessor slea, MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_ACTION.getValue());
        int length = (int) slea.available();
        if (length == 10) {
            mplew.writeInt(slea.readInt());
            mplew.writeShort(slea.readShort());
            mplew.writeInt(slea.readInt());
        } else if (length > 10) {
            mplew.write(slea.read(length - 9));
        } else {
            return;
        }
        c.getSession().write(mplew.getPacket());
    }

    public static final void NPCShop(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        byte bmode = slea.readByte();
        if (chr == null) {
            return;
        }

        switch (bmode) {
            case 0: {
                MapleShop shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                slea.skip(2);
                int itemId = slea.readInt();
                short quantity = slea.readShort();
                shop.buy(c, itemId, quantity);
                break;
            }
            case 1: {
                MapleShop shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                byte slot = (byte) slea.readShort();
                int itemId = slea.readInt();
                short quantity = slea.readShort();
                shop.sell(c, GameConstants.getInventoryType(itemId), slot, quantity);
                break;
            }
            case 2: {
                MapleShop shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                byte slot = (byte) slea.readShort();
                shop.recharge(c, slot);
                break;
            }
            default:
                chr.setConversation(0);
        }
    }

    public static final void NPCTalk(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MapleNPC npc = chr.getMap().getNPCByOid(slea.readInt());
        if (npc == null) {
            return;
        }
        if (chr.hasBlockedInventory()) {
            return;
        }
        if (npc.hasShop()) {
            chr.setConversation(1);
            npc.sendShop(c);
        } else {
            NPCScriptManager.getInstance().start(c, npc.getId());
        }
    }

    public static final void QuestAction(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        byte action = slea.readByte();
        int quest = slea.readUShort();
        if (chr == null) {
            return;
        }
        MapleQuest q = MapleQuest.getInstance(quest);
        switch (action) {
            case 0: {
                slea.readInt();
                int itemid = slea.readInt();
                q.RestoreLostItem(chr, itemid);
                break;
            }
            case 1: {
                int npc = slea.readInt();
                String[] questid = ServerProperties.getProperty("world.completequestid", "0").split(",");
                for (int i = 0; i < questid.length; i++) {
                    if (quest == Integer.parseInt(questid[i])) {
                        q.socomplete(chr, npc);
                        return;
                    }
                }
                if (!q.hasStartScript()) {
                    q.start(chr, npc);
                }
                break;
            }
            case 2: {
                int npc = slea.readInt();

                slea.readInt();
                if (q.hasEndScript()) {
                    return;
                }
                if (slea.available() >= 4L) {
                    q.complete(chr, npc, Integer.valueOf(slea.readInt()));
                } else {
                    q.complete(chr, npc);
                }

                break;
            }
            case 3: {
                if (GameConstants.canForfeit(q.getId())) {
                    q.forfeit(chr);
                } else {
                    chr.dropMessage(1, "You may not forfeit this quest.");
                }
                break;
            }
            case 4: {
                int npc = slea.readInt();
                if (chr.hasBlockedInventory()) {
                    return;
                }
                if ((npc == 9010000)
                        && (GameConstants.xenon(chr.getJob()))) {
                    if (chr.getxenoncombo() < 20) {
                        chr.setxenoncombo((short) (chr.getxenoncombo() + 1));
                    }
                    SkillFactory.getSkill(30020232).getEffect(1).applyXenon_Combo(chr, chr.getxenoncombo());
                }

                NPCScriptManager.getInstance().startQuest(c, npc, quest);
                break;
            }
            case 5: {
                int npc = slea.readInt();
                if (chr.hasBlockedInventory()) {
                    return;
                }

                NPCScriptManager.getInstance().endQuest(c, npc, quest, false);
                c.getSession().write(CField.EffectPacket.showForeignEffect(12));
                chr.getMap().broadcastMessage(chr, CField.EffectPacket.showForeignEffect(chr.getId(), 12), false);
                break;
            }
        }
    }

    public static final void Storage(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        byte mode = slea.readByte();
        if (chr == null) {
            return;
        }
        MapleStorage storage = chr.getStorage();

        switch (mode) {
            case 4: {
                byte type = slea.readByte();
                byte slot = storage.getSlot(MapleInventoryType.getByType(type), slea.readByte());
                Item item = storage.takeOut(slot);

                if (item != null) {
                    if (!MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {
                        storage.store(item);
                        chr.dropMessage(1, "Your inventory is full");
                    } else {
                        MapleInventoryManipulator.addFromDrop(c, item, false);
                        storage.sendTakenOut(c, GameConstants.getInventoryType(item.getItemId()));
                    }
                } else {
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                break;
            }
            case 5: {
                byte slot = (byte) slea.readShort();
                int itemId = slea.readInt();
                MapleInventoryType type = GameConstants.getInventoryType(itemId);
                short quantity = slea.readShort();
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                if (quantity < 1) {
                    return;
                }
                if (storage.isFull()) {
                    c.getSession().write(CField.NPCPacket.getStorageFull());
                    return;
                }
                if (chr.getInventory(type).getItem((short) slot) == null) {
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }

                if (chr.getMeso() < 100L) {
                    chr.dropMessage(1, "You don't have enough mesos to store the item");
                } else {
                    Item item = chr.getInventory(type).getItem((short) slot).copy();

                    if (GameConstants.isPet(item.getItemId())) {
                        c.getSession().write(CWvsContext.enableActions());
                        return;
                    }
                    short flag = item.getFlag();
//                    if ((ii.isPickupRestricted(item.getItemId())) && (storage.findById(item.getItemId()) != null)) {
//                        c.getSession().write(CWvsContext.enableActions());
//                        return;
//                    }
                    if ((item.getItemId() == itemId) && ((item.getQuantity() >= quantity) || (GameConstants.isThrowingStar(itemId)) || (GameConstants.isBullet(itemId)))) {
                        if ((GameConstants.isThrowingStar(itemId)) || (GameConstants.isBullet(itemId))) {
                            quantity = item.getQuantity();
                        }
                        chr.gainMeso(-100L, false, false);
                        MapleInventoryManipulator.removeFromSlot(c, type, (short) slot, quantity, false);
                        item.setQuantity(quantity);
                        storage.store(item);
                    } else {
                        return;
                    }
                }
                storage.sendStored(c, GameConstants.getInventoryType(itemId));
                break;
            }
            case 6:
                storage.arrange();
                storage.update(c);
                break;
            case 7: {
                long meso = slea.readInt();
                long storageMesos = storage.getMeso();
                long playerMesos = chr.getMeso();

                if (((meso > 0L) && (storageMesos >= meso)) || ((meso < 0L) && (playerMesos >= -meso))) {
                    if ((meso < 0L) && (storageMesos - meso < 0L)) {
                        meso = -(2147483647L - storageMesos);
                        if (-meso <= playerMesos);
                    } else if ((meso > 0L) && (playerMesos + meso < 0L)) {
                        meso = 2147483647L - playerMesos;
                        if (meso > storageMesos) {
                            return;
                        }
                    }
                    storage.setMeso(storageMesos - meso);
                    chr.gainMeso(meso, false, false);
                } else {
                    return;
                }
                storage.sendMeso(c);
                break;
            }
            case 8:
                storage.close();
                chr.setConversation(0);
                break;
            default:
                System.out.println("Unhandled Storage mode : " + mode);
        }
    }

    public static final void NPCMoreTalk(LittleEndianAccessor slea, MapleClient c) {
        byte lastMsg = slea.readByte();
        byte action = slea.readByte();

        if (((lastMsg == 18) && (c.getPlayer().getDirection() >= 0)) || ((lastMsg == 19) && (c.getPlayer().getDirection() == -1) && (action == 1) && (GameConstants.GMS))) {
            MapScriptMethods.startDirectionInfo(c.getPlayer(), lastMsg == 19);
            return;
        }

        NPCConversationManager cm = NPCScriptManager.getInstance().getCM(c);

        if ((cm == null) || (c.getPlayer().getConversation() == 0) || (cm.getLastMsg() != lastMsg)) {
            return;
        }
        cm.setLastMsg((byte) -1);
        if (lastMsg == 3) {
            if (action != 0) {
                cm.setGetText(slea.readMapleAsciiString());
                if (cm.getType() == 0) {
                    NPCScriptManager.getInstance().startQuest(c, action, lastMsg, -1);
                } else if (cm.getType() == 1) {
                    NPCScriptManager.getInstance().endQuest(c, action, lastMsg, -1);
                } else {
                    NPCScriptManager.getInstance().action(c, action, lastMsg, -1);
                }
            } else {
                cm.dispose();
            }
        } else {
            int selection = -1;
            if (slea.available() >= 4L) {
                selection = slea.readInt();
            } else if (slea.available() > 0L) {
                selection = slea.readByte();
            }
            if ((lastMsg == 4) && (selection == -1)) {
                cm.dispose();
                return;
            }
            if ((selection >= -1) && (action != -1)) {
                if (cm.getType() == 0) {
                    NPCScriptManager.getInstance().startQuest(c, action, lastMsg, selection);
                } else if (cm.getType() == 1) {
                    NPCScriptManager.getInstance().endQuest(c, action, lastMsg, selection);
                } else {
                    NPCScriptManager.getInstance().action(c, action, lastMsg, selection);
                }
            } else {
                cm.dispose();
            }
        }
    }

    public static final void repairAll(MapleClient c) {
        if (c.getPlayer().getMapId() != 240000000) {
            return;
        }

        int price = 0;

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Map<Equip, Integer> eqs = new HashMap();
        MapleInventoryType[] types = {MapleInventoryType.EQUIP, MapleInventoryType.EQUIPPED};
        for (MapleInventoryType type : types) {
            for (Item item : c.getPlayer().getInventory(type).newList()) {
                if ((item instanceof Equip)) {
                    Equip eq = (Equip) item;
                    if (eq.getDurability() >= 0) {
                        Map<String, Integer> eqStats = ii.getEquipStats(eq.getItemId());
                        if ((eqStats.containsKey("durability")) && (((Integer) eqStats.get("durability")).intValue() > 0) && (eq.getDurability() < ((Integer) eqStats.get("durability")).intValue())) {
                            double rPercentage = 100.0D - Math.ceil(eq.getDurability() * 1000.0D / (((Integer) eqStats.get("durability")).intValue() * 10.0D));
                            eqs.put(eq, eqStats.get("durability"));
                            price += (int) Math.ceil(rPercentage * ii.getPrice(eq.getItemId()) / (ii.getReqLevel(eq.getItemId()) < 70 ? 100.0D : 1.0D));
                        }
                    }
                }
            }
        }
        if ((eqs.size() <= 0) || (c.getPlayer().getMeso() < price)) {
            return;
        }
        c.getPlayer().gainMeso(-price, true);

        for (Map.Entry eqqz : eqs.entrySet()) {
            Equip ez = (Equip) eqqz.getKey();
            ez.setDurability(((Integer) eqqz.getValue()).intValue());
            c.getPlayer().forceReAddItem(ez.copy(), ez.getPosition() < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP);
        }
    }

    public static final void repair(LittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer().getMapId() != 240000000) || (slea.available() < 4L)) {
            return;
        }
        int position = slea.readInt();
        MapleInventoryType type = position < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP;
        Item item = c.getPlayer().getInventory(type).getItem((short) (byte) position);
        if (item == null) {
            return;
        }
        Equip eq = (Equip) item;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Map eqStats = ii.getEquipStats(item.getItemId());
        if ((eq.getDurability() < 0) || (!eqStats.containsKey("durability")) || (((Integer) eqStats.get("durability")).intValue() <= 0) || (eq.getDurability() >= ((Integer) eqStats.get("durability")).intValue())) {
            return;
        }
        double rPercentage = 100.0D - Math.ceil(eq.getDurability() * 1000.0D / (((Integer) eqStats.get("durability")).intValue() * 10.0D));

        int price = (int) Math.ceil(rPercentage * ii.getPrice(eq.getItemId()) / (ii.getReqLevel(eq.getItemId()) < 70 ? 100.0D : 1.0D));

        if (c.getPlayer().getMeso() < price) {
            return;
        }
        c.getPlayer().gainMeso(-price, false);
        eq.setDurability(((Integer) eqStats.get("durability")).intValue());
        c.getPlayer().forceReAddItem(eq.copy(), type);
    }

    public static final void UpdateQuest(LittleEndianAccessor slea, MapleClient c) {
        MapleQuest quest = MapleQuest.getInstance(slea.readShort());
        if (quest != null) {
            c.getPlayer().updateQuest(c.getPlayer().getQuest(quest), true);
        }
    }

    public static final void UseItemQuest(LittleEndianAccessor slea, MapleClient c) {
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item item = c.getPlayer().getInventory(MapleInventoryType.ETC).getItem(slot);
        int qid = slea.readInt();
        MapleQuest quest = MapleQuest.getInstance(qid);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Pair questItemInfo = null;
        boolean found = false;
        for (Item i : c.getPlayer().getInventory(MapleInventoryType.ETC)) {
            if (i.getItemId() / 10000 == 422) {
                questItemInfo = ii.questItemInfo(i.getItemId());
                if ((questItemInfo != null) && (((Integer) questItemInfo.getLeft()).intValue() == qid) && (questItemInfo.getRight() != null) && (((List) questItemInfo.getRight()).contains(Integer.valueOf(itemId)))) {
                    found = true;
                    break;
                }
            }
        }
        if ((quest != null) && (found) && (item != null) && (item.getQuantity() > 0) && (item.getItemId() == itemId)) {
            int newData = slea.readInt();
            MapleQuestStatus stats = c.getPlayer().getQuestNoAdd(quest);
            if ((stats != null) && (stats.getStatus() == 1)) {
                stats.setCustomData(String.valueOf(newData));
                c.getPlayer().updateQuest(stats, true);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, slot, (short) 1, false);
            }
        }
    }

    public static final void RPSGame(LittleEndianAccessor slea, MapleClient c) {
        if ((slea.available() == 0L) || (c.getPlayer() == null) || (c.getPlayer().getMap() == null) || (!c.getPlayer().getMap().containsNPC(9000019))) {
            if ((c.getPlayer() != null) && (c.getPlayer().getRPS() != null)) {
                c.getPlayer().getRPS().dispose(c);
            }
            return;
        }
        byte mode = slea.readByte();
        switch (mode) {
            case 0:
            case 5:
                if (c.getPlayer().getRPS() != null) {
                    c.getPlayer().getRPS().reward(c);
                }
                if (c.getPlayer().getMeso() >= 1000L) {
                    c.getPlayer().setRPS(new RockPaperScissors(c, mode));
                } else {
                    c.getSession().write(CField.getRPSMode((byte) 8, -1, -1, -1));
                }
                break;
            case 1:
                if ((c.getPlayer().getRPS() == null) || (!c.getPlayer().getRPS().answer(c, slea.readByte()))) {
                    c.getSession().write(CField.getRPSMode((byte) 13, -1, -1, -1));
                }
                break;
            case 2:
                if ((c.getPlayer().getRPS() == null) || (!c.getPlayer().getRPS().timeOut(c))) {
                    c.getSession().write(CField.getRPSMode((byte) 13, -1, -1, -1));
                }
                break;
            case 3:
                if ((c.getPlayer().getRPS() == null) || (!c.getPlayer().getRPS().nextRound(c))) {
                    c.getSession().write(CField.getRPSMode((byte) 13, -1, -1, -1));
                }
                break;
            case 4:
                if (c.getPlayer().getRPS() != null) {
                    c.getPlayer().getRPS().dispose(c);
                } else {
                    c.getSession().write(CField.getRPSMode((byte) 13, -1, -1, -1));
                }
                break;
        }
    }

    public static final void OpenPublicNpc(LittleEndianAccessor slea, MapleClient c) {
        int npcid = slea.readInt();
        if ((c.getPlayer().hasBlockedInventory()) || (c.getPlayer().isInBlockedMap()) || (c.getPlayer().getLevel() < 10)) {
            return;
        }
        for (int i = 0; i < GameConstants.publicNpcIds.length; i++) {
            if (GameConstants.publicNpcIds[i] == npcid) {
                NPCScriptManager.getInstance().start(c, npcid);
                return;
            }
        }
    }
}