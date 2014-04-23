package handling.channel.handler;

import clientside.InnerAbillity;
import clientside.InnerSkillValueHolder;
import clientside.MapleCharacter;
import clientside.MapleCharacterUtil;
import clientside.MapleClient;
import clientside.MapleDisease;
import clientside.MapleQuestStatus;
import clientside.MapleStat;
import clientside.MapleTrait;
import clientside.MonsterFamiliar;
import clientside.PlayerStats;
import clientside.Skill;
import clientside.SkillEntry;
import clientside.SkillFactory;
import client.inventory.Equip;
import client.inventory.Equip.ScrollResult;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import clientside.MapleTrait.MapleTraitType;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.MaplePartyCharacter;
import handling.world.World;
import java.awt.Point;
import java.awt.Rectangle;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import scripting.NPCScriptManager;
import server.MapleInventoryManipulator;
import static server.MapleInventoryManipulator.equip;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.MapleStatEffect;
import server.RandomRewards;
import server.Randomizer;
import server.StructFamiliar;
import server.StructItemOption;
import server.StructRewardItem;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.SavedLocationType;
import server.quest.MapleQuest;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import tools.FileoutputUtil;
import tools.Pair;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.InventoryPacket;
import tools.packet.MTSCSPacket;
import tools.packet.MobPacket;
import tools.packet.PetPacket;
import tools.packet.PlayerShopPacket;

public class InventoryHandler {

    public static final int OWL_ID = 2;

    public static final void ItemMove(LittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().hasBlockedInventory()) {
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        slea.readInt();
        MapleInventoryType type = MapleInventoryType.getByType(slea.readByte());
        short src = slea.readShort();
        short dst = slea.readShort();
        short quantity = slea.readShort();

        if ((src < 0) && (dst > 0)) {
            MapleInventoryManipulator.unequip(c, src, dst);
        } else if (dst < 0) {
            MapleInventoryManipulator.equip(c, src, dst);
        } else if (dst == 0) {
            if (c.getPlayer().getGMLevel() == 6 && c.getPlayer().getDGM() == 0) {
                c.getPlayer().dropMessage(1, "You may not do this as a GM.");
                c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                return;
            }
            MapleInventoryManipulator.drop(c, type, src, quantity);
        } else {
            MapleInventoryManipulator.move(c, type, src, dst);
        }
    }

    public static final void SwitchBag(LittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().hasBlockedInventory()) {
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        slea.readInt();
        short src = (short) slea.readInt();
        short dst = (short) slea.readInt();
        if ((src < 100) || (dst < 100)) {
            return;
        }
        MapleInventoryManipulator.move(c, MapleInventoryType.ETC, src, dst);
    }

    public static final void MoveBag(LittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().hasBlockedInventory()) {
            return;
        }
        c.getPlayer().setScrolledPosition((short) 0);
        slea.readInt();
        boolean srcFirst = slea.readInt() > 0;
        short dst = (short) slea.readInt();
        if (slea.readByte() != 4) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        short src = slea.readShort();
        MapleInventoryManipulator.move(c, MapleInventoryType.ETC, srcFirst ? dst : src, srcFirst ? src : dst);
    }

    public static final void ItemSort(LittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        c.getPlayer().setScrolledPosition((short) 0);
        MapleInventoryType pInvType = MapleInventoryType.getByType(slea.readByte());
        if ((pInvType == MapleInventoryType.UNDEFINED) || (c.getPlayer().hasBlockedInventory())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        MapleInventory pInv = c.getPlayer().getInventory(pInvType);
        boolean sorted = false;

        while (!sorted) {
            byte freeSlot = (byte) pInv.getNextFreeSlot();
            if (freeSlot != -1) {
                byte itemSlot = -1;
                for (byte i = (byte) (freeSlot + 1); i <= pInv.getSlotLimit(); i = (byte) (i + 1)) {
                    if (pInv.getItem((short) i) != null) {
                        itemSlot = i;
                        break;
                    }
                }
                if (itemSlot > 0) {
                    MapleInventoryManipulator.move(c, pInvType, (short) itemSlot, (short) freeSlot);
                } else {
                    sorted = true;
                }
            } else {
                sorted = true;
            }
        }
        c.getSession().write(CWvsContext.finishedSort(pInvType.getType()));
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void ItemGather(LittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        c.getPlayer().setScrolledPosition((short) 0);
        if (c.getPlayer().hasBlockedInventory()) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        byte mode = slea.readByte();
        MapleInventoryType invType = MapleInventoryType.getByType(mode);
        MapleInventory Inv = c.getPlayer().getInventory(invType);

        List<Item> itemMap = new LinkedList();
        for (Item item : Inv.list()) {
            itemMap.add(item.copy());
        }
        for (Item itemStats : itemMap) {
            MapleInventoryManipulator.removeFromSlot(c, invType, itemStats.getPosition(), itemStats.getQuantity(), true, false);
        }

        List<Item> sortedItems = sortItems(itemMap);
        for (Item item : sortedItems) {
            MapleInventoryManipulator.addFromDrop(c, item, false);
        }
        c.getSession().write(CWvsContext.finishedGather(mode));
        c.getSession().write(CWvsContext.enableActions());
        itemMap.clear();
        sortedItems.clear();
    }

    private static final List<Item> sortItems(final List<Item> passedMap) {
        final List<Integer> itemIds = new ArrayList<Integer>(); // empty list.
        for (Item item : passedMap) {
            itemIds.add(item.getItemId()); // adds all item ids to the empty list to be sorted.
        }
        Collections.sort(itemIds); // sorts item ids

        final List<Item> sortedList = new LinkedList<Item>(); // ordered list pl0x <3.

        for (Integer val : itemIds) {
            for (Item item : passedMap) {
                if (val == item.getItemId()) { // Goes through every index and finds the first value that matches
                    sortedList.add(item);
                    passedMap.remove(item);
                    break;
                }
            }
        }
        return sortedList;
    }

    public static final boolean UseRewardItem(byte slot, int itemId, MapleClient c, MapleCharacter chr) {
        Item toUse = c.getPlayer().getInventory(GameConstants.getInventoryType(itemId)).getItem((short) slot);
        c.getSession().write(CWvsContext.enableActions());
        if ((toUse != null) && (toUse.getQuantity() >= 1) && (toUse.getItemId() == itemId) && (!chr.hasBlockedInventory())) {
            if ((chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() > -1) && (chr.getInventory(MapleInventoryType.USE).getNextFreeSlot() > -1) && (chr.getInventory(MapleInventoryType.SETUP).getNextFreeSlot() > -1) && (chr.getInventory(MapleInventoryType.ETC).getNextFreeSlot() > -1)) {
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                Pair<Integer, List<StructRewardItem>> rewards = ii.getRewardItem(itemId);

                if ((rewards != null) && (((Integer) rewards.getLeft()).intValue() > 0)) {
                    while (true) {
                        for (StructRewardItem reward : rewards.getRight()) {
                            if ((reward.prob > 0) && (Randomizer.nextInt(((Integer) rewards.getLeft()).intValue()) < reward.prob)) {
                                if (GameConstants.getInventoryType(reward.itemid) == MapleInventoryType.EQUIP) {
                                    Item item = ii.getEquipById(reward.itemid);
                                    if (reward.period > 0L) {
                                        item.setExpiration(System.currentTimeMillis() + reward.period * 60L * 60L * 10L);
                                    }
                                    item.setGMLog(new StringBuilder().append("Reward item: ").append(itemId).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                                    MapleInventoryManipulator.addbyItem(c, item);
                                } else {
                                    MapleInventoryManipulator.addById(c, reward.itemid, reward.quantity, new StringBuilder().append("Reward item: ").append(itemId).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                                }
                                MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(itemId), itemId, 1, false, false);

                                c.getSession().write(CField.EffectPacket.showRewardItemAnimation(reward.itemid, reward.effect));
                                chr.getMap().broadcastMessage(chr, CField.EffectPacket.showRewardItemAnimation(reward.itemid, reward.effect, chr.getId()), false);
                                return true;
                            }
                        }
                    }
                }
                chr.dropMessage(6, "Unknown error.");
            } else {
                chr.dropMessage(6, "Insufficient inventory slot.");
            }
        }
        return false;
    }

    public static void UseItem(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {

        if (chr == null || !chr.isAlive() || chr.getMapId() == 749040100 || chr.getMap() == null || chr.hasDisease(MapleDisease.POTION) || chr.hasBlockedInventory() || chr.inPVP()) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        long time = System.currentTimeMillis();
        if (chr.getNextConsume() > time) {
            chr.dropMessage(5, "You may not use this item yet.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        slea.readInt();
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);

        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) { //cwk quick hack

            if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                if (chr.getMap().getConsumeItemCoolTime() > 0) {
                    chr.setNextConsume(time + (chr.getMap().getConsumeItemCoolTime() * 1000));
                }
            }

        } else {
            c.getSession().write(CWvsContext.enableActions());
        }
    }

    public static final void UseCosmetic(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (!chr.isAlive()) || (chr.getMap() == null) || (chr.hasBlockedInventory()) || (chr.inPVP())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);

        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != itemId) || (itemId / 10000 != 254) || (itemId / 1000 % 10 != chr.getGender())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
        }
    }

    public static final void UseReturnScroll(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((!chr.isAlive()) || (chr.getMapId() == 749040100) || (chr.hasBlockedInventory()) || (chr.isInBlockedMap()) || (chr.inPVP())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        slea.readInt();
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);

        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != itemId)) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) {
            if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyReturnScroll(chr)) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
            } else {
                c.getSession().write(CWvsContext.enableActions());
            }
        } else {
            c.getSession().write(CWvsContext.enableActions());
        }
    }

    public static final void UseAlienSocket(LittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        c.getPlayer().setScrolledPosition((short) 0);
        Item alienSocket = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((short) (byte) slea.readShort());
        int alienSocketId = slea.readInt();
        Item toMount = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) (byte) slea.readShort());
        if ((alienSocket == null) || (alienSocketId != alienSocket.getItemId()) || (toMount == null) || (c.getPlayer().hasBlockedInventory())) {
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            return;
        }

        Equip eqq = (Equip) toMount;
        if (eqq.getSocketState() != 0) {
            c.getPlayer().dropMessage(1, "This item already has a socket.");
        } else {
            eqq.setSocket1(0);
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, alienSocket.getPosition(), (short) 1, false);
            c.getPlayer().forceReAddItem(toMount, MapleInventoryType.EQUIP);
        }
        c.getSession().write(MTSCSPacket.useAlienSocket(true));
        c.getPlayer().fakeRelog();
        c.getPlayer().dropMessage(5, new StringBuilder().append("Added 1 socket successfully to ").append(toMount).toString());
    }

    public static final void UseNebulite(LittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        c.getPlayer().setScrolledPosition((short) 0);
        Item nebulite = c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((short) (byte) slea.readShort());
        int nebuliteId = slea.readInt();
        Item toMount = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) (byte) slea.readShort());
        if ((nebulite == null) || (nebuliteId != nebulite.getItemId()) || (toMount == null) || (c.getPlayer().hasBlockedInventory())) {
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            return;
        }
        Equip eqq = (Equip) toMount;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        boolean success = false;
        if (eqq.getSocket1() == 0) {
            StructItemOption pot = ii.getSocketInfo(nebuliteId);
            if ((pot != null) && (GameConstants.optionTypeFits(pot.optionType, eqq.getItemId()))) {
                eqq.setSocket1(pot.opID);

                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.SETUP, nebulite.getPosition(), (short) 1, false);
                c.getPlayer().forceReAddItem(toMount, MapleInventoryType.EQUIP);
                success = true;
            }
        }
        c.getPlayer().getMap().broadcastMessage(CField.showNebuliteEffect(c.getPlayer().getId(), success));
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void UseGoldHammer(LittleEndianAccessor slea, MapleClient c) {
        slea.skip(4);
        int slot = slea.readInt();
        slea.skip(8);
        Equip item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) (byte) slea.readInt());

        if (item != null) {
            if ((GameConstants.canHammer(item.getItemId())) && (MapleItemInformationProvider.getInstance().getSlots(item.getItemId()) > 0) && (item.getViciousHammer() < 1)) {
                item.setViciousHammer((byte) (item.getViciousHammer() + 1));
                item.setUpgradeSlots((byte) (item.getUpgradeSlots() + 1));
                c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIP);
                c.getSession().write(MTSCSPacket.ViciousHammer(true, item.getViciousHammer()));
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false, true);
                c.getPlayer().fakeRelog();
                c.getPlayer().dropMessage(1, new StringBuilder().append("Added 1 slot successfully to ").append(item).toString());

            } else {
                c.getPlayer().fakeRelog();
                c.getPlayer().dropMessage(1, new StringBuilder().append("You may not use this on this item anymore!").toString());
            }
        }
    }

    public static final void UseNebuliteFusion(LittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        c.getPlayer().setScrolledPosition((short) 0);
        int nebuliteId1 = slea.readInt();
        Item nebulite1 = c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((short) (byte) slea.readShort());
        int nebuliteId2 = slea.readInt();
        Item nebulite2 = c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((short) (byte) slea.readShort());
        int mesos = slea.readInt();
        int premiumQuantity = slea.readInt();
        if ((nebulite1 == null) || (nebulite2 == null) || (nebuliteId1 != nebulite1.getItemId()) || (nebuliteId2 != nebulite2.getItemId()) || ((mesos == 0) && (premiumQuantity == 0)) || ((mesos != 0) && (premiumQuantity != 0)) || (mesos < 0) || (premiumQuantity < 0) || (c.getPlayer().hasBlockedInventory())) {
            c.getPlayer().dropMessage(1, "Failed to fuse Nebulite.");
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            return;
        }
        int grade1 = GameConstants.getNebuliteGrade(nebuliteId1);
        int grade2 = GameConstants.getNebuliteGrade(nebuliteId2);
        int highestRank = grade1 > grade2 ? grade1 : grade2;
        if ((grade1 == -1) || (grade2 == -1) || ((highestRank == 3) && (premiumQuantity != 2)) || ((highestRank == 2) && (premiumQuantity != 1)) || ((highestRank == 1) && (mesos != 5000)) || ((highestRank == 0) && (mesos != 3000)) || ((mesos > 0) && (c.getPlayer().getMeso() < mesos)) || ((premiumQuantity > 0) && (c.getPlayer().getItemQuantity(4420000, false) < premiumQuantity)) || (grade1 >= 4) || (grade2 >= 4) || (c.getPlayer().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < 1)) {
            c.getSession().write(CField.useNebuliteFusion(c.getPlayer().getId(), 0, false));
            return;
        }
        int avg = (grade1 + grade2) / 2;
        int rank = Randomizer.nextInt(100) < 4 ? 0 : avg != 0 ? avg - 1 : Randomizer.nextInt(100) < 70 ? avg : avg != 3 ? avg + 1 : avg;

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List pots = new LinkedList(ii.getAllSocketInfo(rank).values());
        int newId = 0;
        while (newId == 0) {
            StructItemOption pot = (StructItemOption) pots.get(Randomizer.nextInt(pots.size()));
            if (pot != null) {
                newId = pot.opID;
            }
        }
        if (mesos > 0) {
            c.getPlayer().gainMeso(-mesos, true);
        } else if (premiumQuantity > 0) {
            MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4420000, premiumQuantity, false, false);
        }
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.SETUP, nebulite1.getPosition(), (short) 1, false);
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.SETUP, nebulite2.getPosition(), (short) 1, false);
        MapleInventoryManipulator.addById(c, newId, (short) 1, new StringBuilder().append("Fused from ").append(nebuliteId1).append(" and ").append(nebuliteId2).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
        c.getSession().write(CField.useNebuliteFusion(c.getPlayer().getId(), newId, true));
    }

    public static final void UseMagnify(LittleEndianAccessor slea, MapleClient c) {
        slea.readInt();
        c.getPlayer().setScrolledPosition((short) 0);
        byte src = (byte) slea.readShort();
        boolean insight = src == 127;
        Item magnify = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((short) src);
        byte dst = (byte) slea.readShort();
        Item toReveal = null;
        if (dst > 0) {
            toReveal = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) dst);
        } else {
            toReveal = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) dst);
        }

        if (((magnify == null) && (!insight)) || (toReveal == null) || (c.getPlayer().hasBlockedInventory())) {
            c.getPlayer().dropMessage(5, new StringBuilder().append(magnify).append(" | ").append(insight).append(" | ").append(toReveal).append(" | ").append(c.getPlayer().hasBlockedInventory()).toString());
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            return;
        }
        Equip eqq = (Equip) toReveal;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int reqLevel = ii.getReqLevel(eqq.getItemId()) / 10;
        if ((eqq.getState() == 1) && ((insight) || (magnify.getItemId() == 2460003) || ((magnify.getItemId() == 2460002) && (reqLevel <= 12)) || ((magnify.getItemId() == 2460001) && (reqLevel <= 7)) || ((magnify.getItemId() == 2460000) && (reqLevel <= 3)))) {
            List pots = new LinkedList(ii.getAllPotentialInfo().values());
            int new_state = Math.abs(eqq.getPotential1());
            if ((new_state > 20) || (new_state < 17)) {
                new_state = 17;
            }
            int lines = 2;
            if (eqq.getPotential2() != 0) {
                lines++;
            }

            while (eqq.getState() != new_state) {
                for (int i = 0; i < lines; i++) {
                    boolean rewarded = false;
                    while (!rewarded) {
                        StructItemOption pot = (StructItemOption) ((List) pots.get(Randomizer.nextInt(pots.size()))).get(reqLevel);
                        if ((pot != null) && (pot.reqLevel / 10 <= reqLevel) && (GameConstants.optionTypeFits(pot.optionType, eqq.getItemId())) && (GameConstants.potentialIDFits(pot.opID, new_state, i))) {
                            if (i == 0) {
                                eqq.setPotential1(pot.opID);
                            } else if (i == 1) {
                                eqq.setPotential2(pot.opID);
                            } else if (i == 2) {
                                eqq.setPotential3(pot.opID);
                            }
                            rewarded = true;
                        }
                    }
                }
            }
            c.getPlayer().getTrait(MapleTrait.MapleTraitType.insight).addExp((insight ? 10 : magnify.getItemId() + 2 - 2460000) * 2, c.getPlayer());
            c.getPlayer().getMap().broadcastMessage(CField.showMagnifyingEffect(c.getPlayer().getId(), eqq.getPosition()));
            if (!insight) {
                c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(magnify, toReveal, false, true));
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, magnify.getPosition(), (short) 1, false);
            } else {
                c.getPlayer().forceReAddItem(toReveal, MapleInventoryType.EQUIP);
            }
            c.getSession().write(CWvsContext.enableActions());
        } else {
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
        }
    }
    
    public static final void addToScrollLog(int accountID, int charID, int scrollID, int itemID, byte oldSlots, byte newSlots, byte viciousHammer, String result, boolean ws, boolean ls, int vega) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO scroll_log VALUES(DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, accountID);
            ps.setInt(2, charID);
            ps.setInt(3, scrollID);
            ps.setInt(4, itemID);
            ps.setByte(5, oldSlots);
            ps.setByte(6, newSlots);
            ps.setByte(7, viciousHammer);
            ps.setString(8, result);
            ps.setByte(9, (byte) (ws ? 1 : 0));
            ps.setByte(10, (byte) (ls ? 1 : 0));
            ps.setInt(11, vega);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            FileoutputUtil.outputFileError("Log_Packet_Except.txt", e);
        }
    }
    
        public static boolean isAllowedPotentialStat(Equip eqq, int opID) { //For now
        //if (GameConstants.isWeapon(eqq.getItemId())) {
        //    return !(opID > 60000) || (opID >= 1 && opID <= 4) || (opID >= 9 && opID <= 12) || (opID >= 10001 && opID <= 10006) || (opID >= 10011 && opID <= 10012) || (opID >= 10041 && opID <= 10046) || (opID >= 10051 && opID <= 10052) || (opID >= 10055 && opID <= 10081) || (opID >= 10201 && opID <= 10291) || (opID >= 210001 && opID <= 20006) || (opID >= 20011 && opID <= 20012) || (opID >= 20041 && opID <= 20046) || (opID >= 20051 && opID <= 20052) || (opID >= 20055 && opID <= 20081) || (opID >= 20201 && opID <= 20291) || (opID >= 30001 && opID <= 30006) || (opID >= 30011 && opID <= 30012) || (opID >= 30041 && opID <= 30046) || (opID >= 30051 && opID <= 30052) || (opID >= 30055 && opID <= 30081) || (opID >= 30201 && opID <= 30291) || (opID >= 40001 && opID <= 40006) || (opID >= 40011 && opID <= 40012) || (opID >= 40041 && opID <= 40046) || (opID >= 40051 && opID <= 40052) || (opID >= 40055 && opID <= 40081) || (opID >= 40201 && opID <= 40291);
        //}
        return opID < 60000;
    }

    public static void UseSpecialScroll(LittleEndianAccessor rh, MapleCharacter chr) {
        byte slot = (byte) rh.readShort();
        byte dst = (byte) rh.readShort();
        rh.skip(1);
        boolean use = false;
        Equip toScroll;
        if (dst < 0) {
            toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
        } else {
            toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(dst);
        }
        Item scroll = chr.getInventory(MapleInventoryType.CASH).getItem(slot);

        if (scroll == null || !GameConstants.isSpecialScroll(scroll.getItemId())) {
            scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
            use = true;
        }

        if (!use) {
            if (scroll.getItemId() == 5064000 || scroll.getItemId() == 5064003 || scroll.getItemId() == 5064002 || scroll.getItemId() == 5064004) {
                short flag = toScroll.getFlag();
                flag |= ItemFlag.SHIELD_WARD.getValue();
                toScroll.setFlag(flag);
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse(toScroll, toScroll.getType(), chr));
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.scrolledItem(1));
            } else if (scroll.getItemId() == 5064100 || scroll.getItemId() == 5064101) {
                short flag = toScroll.getFlag();
                flag |= ItemFlag.SLOTS_PROTECT.getValue();
                toScroll.setFlag(flag);
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse(toScroll, toScroll.getType(), chr));
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.scrolledItem(1));
            } else if (scroll.getItemId() == 5064300) {
                short flag = toScroll.getFlag();
                flag |= ItemFlag.SLOTS_PROTECT.getValue();
                toScroll.setFlag(flag);
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse(toScroll, toScroll.getType(), chr));
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.scrolledItem(1));
            } else if (scroll.getItemId() == 5063000) {
                short flag = toScroll.getFlag();
                flag |= ItemFlag.LUCKS_KEY.getValue();
                toScroll.setFlag(flag);
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse(toScroll, toScroll.getType(), chr));
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.scrolledItem(1));
            } else if (scroll.getItemId() == 5063100) {
                short flag = toScroll.getFlag();
                if (!ItemFlag.LUCKS_KEY.check(flag) && !ItemFlag.SHIELD_WARD.check(flag)) {
                    flag |= ItemFlag.LUCKS_KEY.getValue();
                    flag |= ItemFlag.SHIELD_WARD.getValue();
                    toScroll.setFlag(flag);
                    chr.getClient().getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse(toScroll, toScroll.getType(), chr));
                    chr.getClient().getSession().write(CWvsContext.InventoryPacket.scrolledItem(1));
                } else {
                    chr.getClient().getSession().write(CWvsContext.enableActions());
                    return;
                }
            }
            chr.getInventory(MapleInventoryType.CASH).removeItem(scroll.getPosition(), (short) 1, false);
        } else {
            if (scroll.getItemId() == 2531000 || scroll.getItemId() == 2531001) {
                short flag = toScroll.getFlag();
                flag |= ItemFlag.SHIELD_WARD.getValue();
                toScroll.setFlag(flag);
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse(toScroll, toScroll.getType(), chr));
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.scrolledItem(1));
            } else if (scroll.getItemId() == 2532000) {
                short flag = toScroll.getFlag();
                flag |= ItemFlag.SLOTS_PROTECT.getValue();
                toScroll.setFlag(flag);
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse(toScroll, toScroll.getType(), chr));
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.scrolledItem(1));
            } else if (scroll.getItemId() == 2530000 || scroll.getItemId() == 2530001 || scroll.getItemId() == 2530002) {
                short flag = toScroll.getFlag();
                flag |= ItemFlag.LUCKS_KEY.getValue();
                toScroll.setFlag(flag);
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse(toScroll, toScroll.getType(), chr));
                chr.getClient().getSession().write(CWvsContext.InventoryPacket.scrolledItem(1));
            }
            chr.getInventory(MapleInventoryType.USE).removeItem(scroll.getPosition(), (short) 1, false);

        }

        chr.getClient().getSession().write(CWvsContext.InventoryPacket.scrolledItem(scroll, toScroll, false, false));

        chr.getMap().broadcastMessage(chr, CField.getScrollEffect(chr.getId(), scroll.getItemId(), toScroll.getItemId(), ScrollResult.SUCCESS, false, false), true);
        if (dst < 0) {
            chr.equipChanged();
        }


    }

    public static boolean UseUpgradeScroll(short slot, short dst, short ws, MapleClient c, MapleCharacter chr, boolean cashs, boolean skill) {
        return UseUpgradeScroll(slot, dst, ws, c, chr, 0, cashs, skill);
    }

    public static final boolean UseUpgradeScroll(short slot, short dst, short ws, MapleClient c, MapleCharacter chr, int vegas, boolean cashs, boolean legendarySpirit) {
        boolean whiteScroll = false;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        chr.setScrolledPosition((short) 0);
        if ((ws & 0x2) == 2) {
            whiteScroll = true;
        }
        Equip toScroll = null;
        if (dst < 0) {
            toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
        } else {
            toScroll = (Equip) chr.getInventory(MapleInventoryType.EQUIP).getItem(dst);
        }
        if ((toScroll == null) || (c.getPlayer().hasBlockedInventory())) {
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }
        byte oldLevel = toScroll.getLevel();
        byte oldEnhance = toScroll.getEnhance();
        byte oldState = toScroll.getState();
        short oldFlag = toScroll.getFlag();
        byte oldSlots = toScroll.getUpgradeSlots();

        Item scroll = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if ((scroll == null) || (cashs)) {
            scroll = chr.getInventory(MapleInventoryType.CASH).getItem(slot);
            if (scroll == null) {
                c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
                c.getSession().write(CWvsContext.enableActions());
                return false;
            }
        }
        if ((!GameConstants.isSpecialScroll(scroll.getItemId())) && !GameConstants.isInnocence(toScroll.getItemId()) && (!GameConstants.isCleanSlate(scroll.getItemId())) && (!GameConstants.isEquipScroll(scroll.getItemId())) && (!GameConstants.isPotentialScroll(scroll.getItemId()))) {
            if (toScroll.getUpgradeSlots() < 1) {
                c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
                c.getSession().write(CWvsContext.enableActions());
                return false;
            }
        } else if (GameConstants.isEquipScroll(scroll.getItemId())) {
            if ((toScroll.getUpgradeSlots() >= 1) || (toScroll.getEnhance() >= 100) || (vegas > 0) || (ii.isCash(toScroll.getItemId()))) {
                c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
                c.getSession().write(CWvsContext.enableActions());
                return false;
            }
        } else if (GameConstants.isPotentialScroll(scroll.getItemId())) {
            boolean isEpic = scroll.getItemId() / 100 == 20497;
            if (((!isEpic) && (toScroll.getState() >= 1)) || ((isEpic) && (toScroll.getState() >= 18)) || ((toScroll.getLevel() == 0) && (toScroll.getUpgradeSlots() == 0) && (toScroll.getItemId() / 10000 != 135) && (!isEpic)) || (vegas > 0) || (ii.isCash(toScroll.getItemId()))) {
                c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
                c.getSession().write(CWvsContext.enableActions());
                return false;
            }
        } else if ((GameConstants.isSpecialScroll(scroll.getItemId())) && ((ii.isCash(toScroll.getItemId())) || (toScroll.getEnhance() >= 12))) {
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }

        if ((!GameConstants.canScroll(toScroll.getItemId())) && (!GameConstants.isChaosScroll(toScroll.getItemId())) && !GameConstants.isInnocence(toScroll.getItemId())) {
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }
        if (((GameConstants.isCleanSlate(scroll.getItemId())) || (GameConstants.isTablet(scroll.getItemId())) || !GameConstants.isInnocence(toScroll.getItemId()) || (GameConstants.isGeneralScroll(scroll.getItemId())) || (GameConstants.isChaosScroll(scroll.getItemId()))) && ((vegas > 0) || (ii.isCash(toScroll.getItemId())))) {
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }
        if ((GameConstants.isTablet(scroll.getItemId())) && (toScroll.getDurability() < 0)) {
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }
        if ((!GameConstants.isTablet(scroll.getItemId())) && (!GameConstants.isPotentialScroll(scroll.getItemId())) && (!GameConstants.isEquipScroll(scroll.getItemId())) && (!GameConstants.isCleanSlate(scroll.getItemId())) && (!GameConstants.isSpecialScroll(scroll.getItemId())) && (!GameConstants.isChaosScroll(scroll.getItemId())) && !GameConstants.isInnocence(toScroll.getItemId()) && (toScroll.getDurability() >= 0)) {
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }
        Item wscroll = null;

        List scrollReqs = ii.getScrollReqs(scroll.getItemId());
        if ((scrollReqs != null) && (scrollReqs.size() > 0) && (!scrollReqs.contains(Integer.valueOf(toScroll.getItemId())))) {
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }

        if (whiteScroll) {
            wscroll = chr.getInventory(MapleInventoryType.USE).findById(2340000);
            if (wscroll == null) {
                whiteScroll = false;
            }
        }
        if ((GameConstants.isTablet(scroll.getItemId())) || (GameConstants.isGeneralScroll(scroll.getItemId()))) {
            switch (scroll.getItemId() % 1000 / 100) {

                case 0:
                    if ((GameConstants.isTwoHanded(toScroll.getItemId())) || (!GameConstants.isWeapon(toScroll.getItemId()))) {
                        c.getSession().write(CWvsContext.enableActions());
                        return false;
                    }
                    break;
                case 1:
                    if ((!GameConstants.isTwoHanded(toScroll.getItemId())) || (!GameConstants.isWeapon(toScroll.getItemId()))) {
                        c.getSession().write(CWvsContext.enableActions());
                        return false;
                    }
                    break;
                case 2:
                    if ((GameConstants.isAccessory(toScroll.getItemId())) || (GameConstants.isWeapon(toScroll.getItemId()))) {
                        c.getSession().write(CWvsContext.enableActions());
                        return false;
                    }
                    break;
                case 3:
                    if ((!GameConstants.isAccessory(toScroll.getItemId())) || (GameConstants.isWeapon(toScroll.getItemId()))) {
                        c.getSession().write(CWvsContext.enableActions());
                        return false;
                    }
                    break;
            }
        } else if ((!GameConstants.isAccessoryScroll(scroll.getItemId())) && (!GameConstants.isChaosScroll(scroll.getItemId())) && !GameConstants.isInnocence(toScroll.getItemId()) && (!GameConstants.isCleanSlate(scroll.getItemId())) && (!GameConstants.isEquipScroll(scroll.getItemId())) && (!GameConstants.isPotentialScroll(scroll.getItemId())) && (!GameConstants.isSpecialScroll(scroll.getItemId()))
                && (!ii.canScroll(scroll.getItemId(), toScroll.getItemId()))) {
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }

        if ((GameConstants.isAccessoryScroll(scroll.getItemId())) && (!GameConstants.isAccessory(toScroll.getItemId()))) {
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }
        if (scroll.getQuantity() <= 0) {
            c.getSession().write(CWvsContext.enableActions());
            return false;
        }

        if ((legendarySpirit) && (vegas == 0)) {
            chr.getStat();
            if (chr.getSkillLevel(SkillFactory.getSkill(PlayerStats.getSkillByJob(1003, chr.getJob()))) <= 0) {
                c.getSession().write(CWvsContext.enableActions());
                return false;
            }

        }
        // Scroll Success/ Failure/ Curse
        Equip scrolled = (Equip) ii.scrollEquipWithId(toScroll, scroll, whiteScroll, chr, vegas);
        ScrollResult scrollSuccess;
        if (ItemFlag.SLOTS_PROTECT.check(oldFlag) && scrolled != null) {
            scrolled.setFlag((short) (oldFlag - ItemFlag.SLOTS_PROTECT.getValue()));



        }
        if (scrolled == null) {
            if (ItemFlag.SHIELD_WARD.check(oldFlag)) {
                scrolled = toScroll;
                scrollSuccess = Equip.ScrollResult.FAIL;
                scrolled.setFlag((short) (oldFlag - ItemFlag.SHIELD_WARD.getValue()));
            } else {
                scrollSuccess = Equip.ScrollResult.CURSE;
            }

        } else if ((scroll.getItemId() / 100 == 20497 && scrolled.getState() == 1) || scrolled.getLevel() > oldLevel || scrolled.getEnhance() > oldEnhance || scrolled.getState() > oldState || scrolled.getFlag() > oldFlag) {
            scrollSuccess = Equip.ScrollResult.SUCCESS;
        } else if ((GameConstants.isCleanSlate(scroll.getItemId()) && scrolled.getUpgradeSlots() > oldSlots)) {
            scrollSuccess = Equip.ScrollResult.SUCCESS;
        } else {
            scrollSuccess = Equip.ScrollResult.FAIL;
        }
        chr.getInventory(GameConstants.getInventoryType(scroll.getItemId())).removeItem(scroll.getPosition(), (short) 1, false);
        if (whiteScroll) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, wscroll.getPosition(), (short) 1, false, false);
        } else if ((scrollSuccess == Equip.ScrollResult.FAIL) && (scrolled.getUpgradeSlots() < oldSlots) && (c.getPlayer().getInventory(MapleInventoryType.CASH).findById(5640000) != null)) {
            chr.setScrolledPosition(scrolled.getPosition());
            if (vegas == 0) {
                c.getSession().write(CWvsContext.pamSongUI());
            }
        }

        if (scrollSuccess == Equip.ScrollResult.CURSE) {
            c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(scroll, toScroll, true, false));
            if (dst < 0) {
                chr.getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
            } else {
                chr.getInventory(MapleInventoryType.EQUIP).removeItem(toScroll.getPosition());
            }
        } else if (vegas == 0) {
            c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(scroll, scrolled, false, false));
        }


        c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(scrollSuccess == Equip.ScrollResult.CURSE ? 2 : scrollSuccess == Equip.ScrollResult.SUCCESS ? 1 : 0));
        if (scrollSuccess == Equip.ScrollResult.SUCCESS && GameConstants.isInnocence(scroll.getItemId())) {
            c.getSession().write(InventoryPacket.scrolledItem(scroll, toScroll, true, false));
            chr.getInventory(MapleInventoryType.EQUIPPED).removeItem(toScroll.getPosition());
        }
        chr.getMap().broadcastMessage(chr, CField.getScrollEffect(c.getPlayer().getId(), scroll.getItemId(), toScroll.getItemId(), scrollSuccess, legendarySpirit, whiteScroll), vegas == 0);

        if ((dst < 0) && ((scrollSuccess == Equip.ScrollResult.SUCCESS) || (scrollSuccess == Equip.ScrollResult.CURSE)) && (vegas == 0)) {
            chr.equipChanged();
        }
        return true;
    }

    public static final boolean UseSkillBook(byte slot, int itemId, MapleClient c, MapleCharacter chr) {
        Item toUse = chr.getInventory(GameConstants.getInventoryType(itemId)).getItem((short) slot);

        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != itemId) || (chr.hasBlockedInventory())) {
            return false;
        }
        Map skilldata = MapleItemInformationProvider.getInstance().getEquipStats(toUse.getItemId());
        if (skilldata == null) {
            return false;
        }
        boolean canuse = false;
        boolean success = false;
        int skill = 0;
        int maxlevel = 0;

        Integer SuccessRate = (Integer) skilldata.get("success");
        Integer ReqSkillLevel = (Integer) skilldata.get("reqSkillLevel");
        Integer MasterLevel = (Integer) skilldata.get("masterLevel");

        byte i = 0;
        while (true) {
            Integer CurrentLoopedSkillId = (Integer) skilldata.get(new StringBuilder().append("skillid").append(i).toString());
            i = (byte) (i + 1);
            if ((CurrentLoopedSkillId == null) || (MasterLevel == null)) {
                break;
            }
            Skill CurrSkillData = SkillFactory.getSkill(CurrentLoopedSkillId.intValue());
            if ((CurrSkillData != null) && (CurrSkillData.canBeLearnedBy(chr.getJob())) && ((ReqSkillLevel == null) || (chr.getSkillLevel(CurrSkillData) >= ReqSkillLevel.intValue())) && (chr.getMasterLevel(CurrSkillData) < MasterLevel.intValue())) {
                canuse = true;
                if ((SuccessRate == null) || (Randomizer.nextInt(100) <= SuccessRate.intValue())) {
                    success = true;
                    chr.changeSingleSkillLevel(CurrSkillData, chr.getSkillLevel(CurrSkillData), (byte) MasterLevel.intValue());
                } else {
                    success = false;
                }
                MapleInventoryManipulator.removeFromSlot(c, GameConstants.getInventoryType(itemId), (short) slot, (short) 1, false);
                break;
            }
        }
        c.getPlayer().getMap().broadcastMessage(CWvsContext.useSkillBook(chr, skill, maxlevel, canuse, success));
        c.getSession().write(CWvsContext.enableActions());
        return canuse;
    }

    public static final void UseCatchItem(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        slea.readInt();
        c.getPlayer().setScrolledPosition((short) 0);
        byte slot = (byte) slea.readShort();
        int itemid = slea.readInt();
        MapleMonster mob = chr.getMap().getMonsterByOid(slea.readInt());
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        MapleMap map = chr.getMap();

        if ((toUse != null) && (toUse.getQuantity() > 0) && (toUse.getItemId() == itemid) && (mob != null) && (!chr.hasBlockedInventory()) && (itemid / 10000 == 227) && (MapleItemInformationProvider.getInstance().getCardMobId(itemid) == mob.getId())) {
            if ((!MapleItemInformationProvider.getInstance().isMobHP(itemid)) || (mob.getHp() <= mob.getMobMaxHp() / 2L)) {
                map.broadcastMessage(MobPacket.catchMonster(mob.getObjectId(), itemid, (byte) 1));
                map.killMonster(mob, chr, true, false, (byte) 1);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false, false);
                if (MapleItemInformationProvider.getInstance().getCreateId(itemid) > 0) {
                    MapleInventoryManipulator.addById(c, MapleItemInformationProvider.getInstance().getCreateId(itemid), (short) 1, new StringBuilder().append("Catch item ").append(itemid).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                }
            } else {
                map.broadcastMessage(MobPacket.catchMonster(mob.getObjectId(), itemid, (byte) 0));
                c.getSession().write(CWvsContext.catchMob(mob.getId(), itemid, (byte) 0));
            }
        }
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void UseMountFood(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        slea.readInt();
        byte slot = (byte) slea.readShort();
        int itemid = slea.readInt();
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        MapleMount mount = chr.getMount();

        if ((itemid / 10000 == 226) && (toUse != null) && (toUse.getQuantity() > 0) && (toUse.getItemId() == itemid) && (mount != null) && (!c.getPlayer().hasBlockedInventory())) {
            int fatigue = mount.getFatigue();

            boolean levelup = false;
            mount.setFatigue((byte) -30);

            if (fatigue > 0) {
                mount.increaseExp();
                int level = mount.getLevel();
                if ((level < 30) && (mount.getExp() >= GameConstants.getMountExpNeededForLevel(level + 1))) {
                    mount.setLevel((byte) (level + 1));
                    levelup = true;
                }
            }
            chr.getMap().broadcastMessage(CWvsContext.updateMount(chr, levelup));
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
        }
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void UseScriptedNPCItem(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        slea.readInt();
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        Item toUse = chr.getInventory(GameConstants.getInventoryType(itemId)).getItem((short) slot);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        long expiration_days = 0L;
        int mountid = 0;

        if ((toUse != null) && (toUse.getQuantity() >= 1) && (toUse.getItemId() == itemId) && (!chr.hasBlockedInventory()) && (!chr.inPVP())) {
            MapleQuestStatus marr;
            long lastTime;
            switch (toUse.getItemId()) {
                case 2430746: {
                    int[] randitem = {4310048, 1003552, 1322162, 1332193, 1362067, 1372139, 1382168, 1402151, 1412104, 1422107, 1432138, 1442182, 1052461, 1452170, 1462159, 1472179, 1482140, 1492152, 1522071, 1532074, 1082433, 1072666, 1102441, 1152089, 1132154, 1302227, 1312116, 1003529, 1322154, 1332186, 1362060, 1372131, 1382160, 1402145, 1412102, 1422105, 1432135, 1442173, 1052457, 1452165, 1462156, 1472177, 1482138, 1492138, 1522068, 1532073, 1082430, 1072660, 1102394, 1152088, 1132151, 1302212, 1312114, 2041511, 2046085, 2046086, 2046087, 2046088, 2046089, 2046094, 2046156, 2046162, 2046166, 2046552, 2046553, 2046554, 2046555, 2046556, 2046557, 2046558, 2046559, 2046560, 2046561, 2046562, 2046563, 2046564, 2046565, 2046566, 2046567, 2046568, 2046681, 2046682, 2046683, 2046684, 2046739, 2046740, 2046741, 2046742, 2046743, 2046744, 2046745, 2046746, 2046747, 2046748, 2046749, 2046750, 2046751, 2046752, 2046753, 2046754, 2046799, 2046943, 2046944, 2046945, 2046946, 2048047, 2048048, 2048049, 2048050};

                    int itemid = randitem[((int) (Math.random() * randitem.length))];
                    if (ii.itemExists(itemid)) {
                        if (GameConstants.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
                            Equip it = ii.randomizeStats((Equip) ii.getEquipById(itemid));
                            it.setPotential1(-17);
                            Item item = it.copy();
                            item.setGMLog(new StringBuilder().append("Scripted item: ").append(itemId).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            MapleInventoryManipulator.addbyItem(c, item);
                        } else {
                            MapleInventoryManipulator.addById(c, itemid, (short) 1, new StringBuilder().append("Scripted item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                        }
                        c.getSession().write(CWvsContext.InfoPacket.getShowItemGain(itemid, (short) 1, true));
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                    }
                    break;
                }
                case 2430737: {
                    int[] randitem = {4310048, 4001695, 2001530, 2001531, 2450039, 2046743, 2046744, 2046745, 2046746, 2046556, 2046557, 2046558, 2046559};
                    int itemid = randitem[((int) (Math.random() * randitem.length))];
                    int itemslot = (itemid == 4310048) || (itemid == 2450039) ? 1 : 10;
                    if (MapleItemInformationProvider.getInstance().itemExists(itemid)) {
                        c.getSession().write(CWvsContext.InfoPacket.getShowItemGain(itemid, (short) itemslot, true));
                        MapleInventoryManipulator.addById(c, itemid, (short) itemslot, new StringBuilder().append("Scripted item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                    }
                    break;
                }
                case 2430007: {
                    MapleInventory inventory = chr.getInventory(MapleInventoryType.SETUP);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);

                    if ((inventory.countById(3994102) >= 20) && (inventory.countById(3994103) >= 20) && (inventory.countById(3994104) >= 20) && (inventory.countById(3994105) >= 20)) {
                        MapleInventoryManipulator.addById(c, 2430008, (short) 1, new StringBuilder().append("Scripted item: ").append(itemId).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994102, 20, false, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994103, 20, false, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994104, 20, false, false);
                        MapleInventoryManipulator.removeById(c, MapleInventoryType.SETUP, 3994105, 20, false, false);
                    } else {
                        MapleInventoryManipulator.addById(c, 2430007, (short) 1, new StringBuilder().append("Scripted item: ").append(itemId).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                    }
                    NPCScriptManager.getInstance().start(c, 2084001);
                    break;
                }
                case 2430008: {
                    chr.saveLocation(SavedLocationType.RICHIE);

                    boolean warped = false;

                    for (int i = 390001000; i <= 390001004; i++) {
                        MapleMap map = c.getChannelServer().getMapFactory().getMap(i);

                        if (map.getCharactersSize() == 0) {
                            chr.changeMap(map, map.getPortal(0));
                            warped = true;
                            break;
                        }
                    }
                    if (warped) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                    } else {
                        c.getPlayer().dropMessage(5, "All maps are currently in use, please try again later.");
                    }
                    break;
                }
                case 2430112:
                    if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430112) >= 25) {
                            if ((MapleInventoryManipulator.checkSpace(c, 2049400, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 25, true, false))) {
                                MapleInventoryManipulator.addById(c, 2049400, (short) 1, new StringBuilder().append("Scripted item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430112) >= 10) {
                            if ((MapleInventoryManipulator.checkSpace(c, 2049400, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 10, true, false))) {
                                MapleInventoryManipulator.addById(c, 2049401, (short) 1, new StringBuilder().append("Scripted item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "There needs to be 10 Fragments for a Potential Scroll, 25 for Advanced Potential Scroll.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make some space.");
                    }
                    break;
                case 2430481:
                    if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430481) >= 30) {
                            if ((MapleInventoryManipulator.checkSpace(c, 2049701, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 30, true, false))) {
                                MapleInventoryManipulator.addById(c, 2049701, (short) 1, new StringBuilder().append("Scripted item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430481) >= 20) {
                            if ((MapleInventoryManipulator.checkSpace(c, 2049300, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 20, true, false))) {
                                MapleInventoryManipulator.addById(c, 2049300, (short) 1, new StringBuilder().append("Scripted item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "There needs to be 20 Fragments for a Advanced Equip Enhancement Scroll, 30 for Epic Potential Scroll 80%.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make some space.");
                    }
                    break;
                case 2430691:
                    if (c.getPlayer().getInventory(MapleInventoryType.CASH).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430691) >= 10) {
                            if ((MapleInventoryManipulator.checkSpace(c, 5750001, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 10, true, false))) {
                                MapleInventoryManipulator.addById(c, 5750001, (short) 1, new StringBuilder().append("Scripted item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "There needs to be 10 Fragments for a Nebulite Diffuser.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make some space.");
                    }
                    break;
                case 2430748:
                    if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430748) >= 20) {
                            if ((MapleInventoryManipulator.checkSpace(c, 4420000, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 20, true, false))) {
                                MapleInventoryManipulator.addById(c, 4420000, (short) 1, new StringBuilder().append("Scripted item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "There needs to be 20 Fragments for a Premium Fusion Ticket.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make some space.");
                    }
                    break;
                case 2430692:
                    if (c.getPlayer().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.USE).countById(2430692) >= 1) {
                            int rank = Randomizer.nextInt(100) < 30 ? 1 : Randomizer.nextInt(100) < 4 ? 2 : 0;
                            List pots = new LinkedList(ii.getAllSocketInfo(rank).values());
                            int newId = 0;
                            while (newId == 0) {
                                StructItemOption pot = (StructItemOption) pots.get(Randomizer.nextInt(pots.size()));
                                if (pot != null) {
                                    newId = pot.opID;
                                }
                            }
                            if ((MapleInventoryManipulator.checkSpace(c, newId, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false))) {
                                MapleInventoryManipulator.addById(c, newId, (short) 1, new StringBuilder().append("Scripted item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                                c.getSession().write(CWvsContext.InfoPacket.getShowItemGain(newId, (short) 1, true));
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "You do not have a Nebulite Box.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make some space.");
                    }

                    break;
                case 5680019: {
                    int hair = 32150 + c.getPlayer().getHair() % 10;
                    c.getPlayer().setHair(hair);
                    c.getPlayer().updateSingleStat(MapleStat.HAIR, hair);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, (short) slot, (short) 1, false);

                    break;
                }
                case 5680020: {
                    int hair = 32160 + c.getPlayer().getHair() % 10;
                    c.getPlayer().setHair(hair);
                    c.getPlayer().updateSingleStat(MapleStat.HAIR, hair);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, (short) slot, (short) 1, false);

                    break;
                }
                case 3994225:
                    c.getPlayer().dropMessage(5, "Please bring this item to the NPC.");
                    break;
                case 2430212:
                    marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(122500));
                    if (marr.getCustomData() == null) {
                        marr.setCustomData("0");
                    }
                    lastTime = Long.parseLong(marr.getCustomData());
                    if (lastTime + 600000L > System.currentTimeMillis()) {
                        c.getPlayer().dropMessage(5, "You can only use one energy drink per 10 minutes.");
                    } else if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 5);
                    }
                    break;
                case 2430213:
                    marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(122500));
                    if (marr.getCustomData() == null) {
                        marr.setCustomData("0");
                    }
                    lastTime = Long.parseLong(marr.getCustomData());
                    if (lastTime + 600000L > System.currentTimeMillis()) {
                        c.getPlayer().dropMessage(5, "You can only use one energy drink per 10 minutes.");
                    } else if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 10);
                    }
                    break;
                case 2430214:
                case 2430220:
                    if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 30);
                    }
                    break;
                case 2430227:
                    if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 50);
                    }
                    break;
                case 2430231:
                    marr = c.getPlayer().getQuestNAdd(MapleQuest.getInstance(122500));
                    if (marr.getCustomData() == null) {
                        marr.setCustomData("0");
                    }
                    lastTime = Long.parseLong(marr.getCustomData());
                    if (lastTime + 600000L > System.currentTimeMillis()) {
                        c.getPlayer().dropMessage(5, "You can only use one energy drink per 10 minutes.");
                    } else if (c.getPlayer().getFatigue() > 0) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                        c.getPlayer().setFatigue(c.getPlayer().getFatigue() - 40);
                    }
                    break;
                case 2430144:
                    int itemid = Randomizer.nextInt(373) + 2290000;
                    if ((MapleItemInformationProvider.getInstance().itemExists(itemid)) && (!MapleItemInformationProvider.getInstance().getName(itemid).contains("Special")) && (!MapleItemInformationProvider.getInstance().getName(itemid).contains("Event"))) {
                        MapleInventoryManipulator.addById(c, itemid, (short) 1, new StringBuilder().append("Reward item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                    }
                    break;
                case 2430370:
                    if (MapleInventoryManipulator.checkSpace(c, 2028062, 1, "")) {
                        MapleInventoryManipulator.addById(c, 2028062, (short) 1, new StringBuilder().append("Reward item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                    }
                    break;
                case 2430158:
                    if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= 1) {
                        if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000630) >= 100) {
                            if ((MapleInventoryManipulator.checkSpace(c, 4310010, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false))) {
                                MapleInventoryManipulator.addById(c, 4310010, (short) 1, new StringBuilder().append("Scripted item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else if (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000630) >= 50) {
                            if ((MapleInventoryManipulator.checkSpace(c, 4310009, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false))) {
                                MapleInventoryManipulator.addById(c, 4310009, (short) 1, new StringBuilder().append("Scripted item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "There needs to be 50 Purification Totems for a Noble Lion King Medal, 100 for Royal Lion King Medal.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make some space.");
                    }
                    break;
                case 2430159:
                    MapleQuest.getInstance(3182).forceComplete(c.getPlayer(), 2161004);
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                    break;
                case 2430200:
                    if (c.getPlayer().getQuestStatus(31152) != 2) {
                        c.getPlayer().dropMessage(5, "You have no idea how to use it.");
                    } else if (c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() >= 1) {
                        if ((c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000660) >= 1) && (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000661) >= 1) && (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000662) >= 1) && (c.getPlayer().getInventory(MapleInventoryType.ETC).countById(4000663) >= 1)) {
                            if ((MapleInventoryManipulator.checkSpace(c, 4032923, 1, "")) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, toUse.getItemId(), 1, true, false)) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000660, 1, true, false)) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000661, 1, true, false)) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000662, 1, true, false)) && (MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4000663, 1, true, false))) {
                                MapleInventoryManipulator.addById(c, 4032923, (short) 1, new StringBuilder().append("Scripted item: ").append(toUse.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            } else {
                                c.getPlayer().dropMessage(5, "Please make some space.");
                            }
                        } else {
                            c.getPlayer().dropMessage(5, "There needs to be 1 of each Stone for a Dream Key.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make some space.");
                    }

                    break;
                case 2430130:
                case 2430131:
                    if (GameConstants.isResist(c.getPlayer().getJob())) {
                        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                        c.getPlayer().gainExp(20000 + c.getPlayer().getLevel() * 50 * c.getChannelServer().getExpRate(), true, true, false);
                    } else {
                        c.getPlayer().dropMessage(5, "You may not use this item.");
                    }
                    break;
                case 2430132:
                case 2430133:
                case 2430134:
                case 2430142:
                    if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() >= 1) {
                        if ((c.getPlayer().getJob() == 3200) || (c.getPlayer().getJob() == 3210) || (c.getPlayer().getJob() == 3211) || (c.getPlayer().getJob() == 3212)) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                            MapleInventoryManipulator.addById(c, 1382101, (short) 1, new StringBuilder().append("Scripted item: ").append(itemId).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                        } else if ((c.getPlayer().getJob() == 3300) || (c.getPlayer().getJob() == 3310) || (c.getPlayer().getJob() == 3311) || (c.getPlayer().getJob() == 3312)) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                            MapleInventoryManipulator.addById(c, 1462093, (short) 1, new StringBuilder().append("Scripted item: ").append(itemId).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                        } else if ((c.getPlayer().getJob() == 3500) || (c.getPlayer().getJob() == 3510) || (c.getPlayer().getJob() == 3511) || (c.getPlayer().getJob() == 3512)) {
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
                            MapleInventoryManipulator.addById(c, 1492080, (short) 1, new StringBuilder().append("Scripted item: ").append(itemId).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                        } else {
                            c.getPlayer().dropMessage(5, "You may not use this item.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Make some space.");
                    }

                    break;
                case 2430036:
                    mountid = 1027;
                    expiration_days = 1L;
                    break;
                case 2430170:
                    mountid = 1027;
                    expiration_days = 7L;
                    break;
                case 2430037:
                    mountid = 1028;
                    expiration_days = 1L;
                    break;
                case 2430038:
                    mountid = 1029;
                    expiration_days = 1L;
                    break;
                case 2430039:
                    mountid = 1030;
                    expiration_days = 1L;
                    break;
                case 2430040:
                    mountid = 1031;
                    expiration_days = 1L;
                    break;
                case 2430223:
                    mountid = 1031;
                    expiration_days = 15L;
                    break;
                case 2430259:
                    mountid = 1031;
                    expiration_days = 3L;
                    break;
                case 2430242:
                    mountid = 80001018;
                    expiration_days = 10L;
                    break;
                case 2430243:
                    mountid = 80001019;
                    expiration_days = 10L;
                    break;
                case 2430261:
                    mountid = 80001019;
                    expiration_days = 3L;
                    break;
                case 2430249:
                    mountid = 80001027;
                    expiration_days = 3L;
                    break;
                case 2430225:
                    mountid = 1031;
                    expiration_days = 10L;
                    break;
                case 2430053:
                    mountid = 1027;
                    expiration_days = 1L;
                    break;
                case 2430054:
                    mountid = 1028;
                    expiration_days = 30L;
                    break;
                case 2430055:
                    mountid = 1029;
                    expiration_days = 30L;
                    break;
                case 2430257:
                    mountid = 1029;
                    expiration_days = 7L;
                    break;
                case 2430056:
                    mountid = 1035;
                    expiration_days = 30L;
                    break;
                case 2430057:
                    mountid = 1033;
                    expiration_days = 30L;
                    break;
                case 2430072:
                    mountid = 1034;
                    expiration_days = 7L;
                    break;
                case 2430073:
                    mountid = 1036;
                    expiration_days = 15L;
                    break;
                case 2430074:
                    mountid = 1037;
                    expiration_days = 15L;
                    break;
                case 2430272:
                    mountid = 1038;
                    expiration_days = 3L;
                    break;
                case 2430275:
                    mountid = 80001033;
                    expiration_days = 7L;
                    break;
                case 2430075:
                    mountid = 1038;
                    expiration_days = 15L;
                    break;
                case 2430076:
                    mountid = 1039;
                    expiration_days = 15L;
                    break;
                case 2430077:
                    mountid = 1040;
                    expiration_days = 15L;
                    break;
                case 2430080:
                    mountid = 1042;
                    expiration_days = 20L;
                    break;
                case 2430082:
                    mountid = 1044;
                    expiration_days = 7L;
                    break;
                case 2430260:
                    mountid = 1044;
                    expiration_days = 3L;
                    break;
                case 2430091:
                    mountid = 1049;
                    expiration_days = 10L;
                    break;
                case 2430092:
                    mountid = 1050;
                    expiration_days = 10L;
                    break;
                case 2430263:
                    mountid = 1050;
                    expiration_days = 3L;
                    break;
                case 2430093:
                    mountid = 1051;
                    expiration_days = 10L;
                    break;
                case 2430101:
                    mountid = 1052;
                    expiration_days = 10L;
                    break;
                case 2430102:
                    mountid = 1053;
                    expiration_days = 10L;
                    break;
                case 2430103:
                    mountid = 1054;
                    expiration_days = 30L;
                    break;
                case 2430266:
                    mountid = 1054;
                    expiration_days = 3L;
                    break;
                case 2430265:
                    mountid = 1151;
                    expiration_days = 3L;
                    break;
                case 2430258:
                    mountid = 1115;
                    expiration_days = 365L;
                    break;
                case 2430117:
                    mountid = 1036;
                    expiration_days = 365L;
                    break;
                case 2430118:
                    mountid = 1039;
                    expiration_days = 365L;
                    break;
                case 2430119:
                    mountid = 1040;
                    expiration_days = 365L;
                    break;
                case 2430120:
                    mountid = 1037;
                    expiration_days = 365L;
                    break;
                case 2430271:
                    mountid = 1069;
                    expiration_days = 3L;
                    break;
                case 2430136:
                    mountid = 1069;
                    expiration_days = 30L;
                    break;
                case 2430137:
                    mountid = 1069;
                    expiration_days = 365L;
                    break;
                case 2430145:
                    mountid = 1070;
                    expiration_days = 30L;
                    break;
                case 2430146:
                    mountid = 1070;
                    expiration_days = 365L;
                    break;
                case 2430147:
                    mountid = 1071;
                    expiration_days = 30L;
                    break;
                case 2430148:
                    mountid = 1071;
                    expiration_days = 365L;
                    break;
                case 2430135:
                    mountid = 1065;
                    expiration_days = 15L;
                    break;
                case 2430149:
                    mountid = 1072;
                    expiration_days = 30L;
                    break;
                case 2430262:
                    mountid = 1072;
                    expiration_days = 3L;
                    break;
                case 2430179:
                    mountid = 1081;
                    expiration_days = 15L;
                    break;
                case 2430264:
                    mountid = 1081;
                    expiration_days = 3L;
                    break;
                case 2430201:
                    mountid = 1096;
                    expiration_days = 60L;
                    break;
                case 2430228:
                    mountid = 1101;
                    expiration_days = 60L;
                    break;
                case 2430276:
                    mountid = 1101;
                    expiration_days = 15L;
                    break;
                case 2430277:
                    mountid = 1101;
                    expiration_days = 365L;
                    break;
                case 2430283:
                    mountid = 1025;
                    expiration_days = 10L;
                    break;
                case 2430291:
                    mountid = 1145;
                    expiration_days = -1L;
                    break;
                case 2430293:
                    mountid = 1146;
                    expiration_days = -1L;
                    break;
                case 2430295:
                    mountid = 1147;
                    expiration_days = -1L;
                    break;
                case 2430297:
                    mountid = 1148;
                    expiration_days = -1L;
                    break;
                case 2430299:
                    mountid = 1149;
                    expiration_days = -1L;
                    break;
                case 2430301:
                    mountid = 1150;
                    expiration_days = -1L;
                    break;
                case 2430303:
                    mountid = 1151;
                    expiration_days = -1L;
                    break;
                case 2430305:
                    mountid = 1152;
                    expiration_days = -1L;
                    break;
                case 2430307:
                    mountid = 1153;
                    expiration_days = -1L;
                    break;
                case 2430309:
                    mountid = 1154;
                    expiration_days = -1L;
                    break;
                case 2430311:
                    mountid = 1156;
                    expiration_days = -1L;
                    break;
                case 2430313:
                    mountid = 1156;
                    expiration_days = -1L;
                    break;
                case 2430315:
                    mountid = 1118;
                    expiration_days = -1L;
                    break;
                case 2430317:
                    mountid = 1121;
                    expiration_days = -1L;
                    break;
                case 2430319:
                    mountid = 1122;
                    expiration_days = -1L;
                    break;
                case 2430321:
                    mountid = 1123;
                    expiration_days = -1L;
                    break;
                case 2430323:
                    mountid = 1124;
                    expiration_days = -1L;
                    break;
                case 2430325:
                    mountid = 1129;
                    expiration_days = -1L;
                    break;
                case 2430327:
                    mountid = 1130;
                    expiration_days = -1L;
                    break;
                case 2430329:
                    mountid = 1063;
                    expiration_days = -1L;
                    break;
                case 2430331:
                    mountid = 1025;
                    expiration_days = -1L;
                    break;
                case 2430333:
                    mountid = 1034;
                    expiration_days = -1L;
                    break;
                case 2430335:
                    mountid = 1136;
                    expiration_days = -1L;
                    break;
                case 2430337:
                    mountid = 1051;
                    expiration_days = -1L;
                    break;
                case 2430339:
                    mountid = 1138;
                    expiration_days = -1L;
                    break;
                case 2430341:
                    mountid = 1139;
                    expiration_days = -1L;
                    break;
                case 2430343:
                    mountid = 1027;
                    expiration_days = -1L;
                    break;
                case 2430346:
                    mountid = 1029;
                    expiration_days = -1L;
                    break;
                case 2430348:
                    mountid = 1028;
                    expiration_days = -1L;
                    break;
                case 2430350:
                    mountid = 1033;
                    expiration_days = -1L;
                    break;
                case 2430352:
                    mountid = 1064;
                    expiration_days = -1L;
                    break;
                case 2430354:
                    mountid = 1096;
                    expiration_days = -1L;
                    break;
                case 2430356:
                    mountid = 1101;
                    expiration_days = -1L;
                    break;
                case 2430358:
                    mountid = 1102;
                    expiration_days = -1L;
                    break;
                case 2430360:
                    mountid = 1054;
                    expiration_days = -1L;
                    break;
                case 2430362:
                    mountid = 1053;
                    expiration_days = -1L;
                    break;
                case 2430292:
                    mountid = 1145;
                    expiration_days = 90L;
                    break;
                case 2430294:
                    mountid = 1146;
                    expiration_days = 90L;
                    break;
                case 2430296:
                    mountid = 1147;
                    expiration_days = 90L;
                    break;
                case 2430298:
                    mountid = 1148;
                    expiration_days = 90L;
                    break;
                case 2430300:
                    mountid = 1149;
                    expiration_days = 90L;
                    break;
                case 2430302:
                    mountid = 1150;
                    expiration_days = 90L;
                    break;
                case 2430304:
                    mountid = 1151;
                    expiration_days = 90L;
                    break;
                case 2430306:
                    mountid = 1152;
                    expiration_days = 90L;
                    break;
                case 2430308:
                    mountid = 1153;
                    expiration_days = 90L;
                    break;
                case 2430310:
                    mountid = 1154;
                    expiration_days = 90L;
                    break;
                case 2430312:
                    mountid = 1156;
                    expiration_days = 90L;
                    break;
                case 2430314:
                    mountid = 1156;
                    expiration_days = 90L;
                    break;
                case 2430316:
                    mountid = 1118;
                    expiration_days = 90L;
                    break;
                case 2430318:
                    mountid = 1121;
                    expiration_days = 90L;
                    break;
                case 2430320:
                    mountid = 1122;
                    expiration_days = 90L;
                    break;
                case 2430322:
                    mountid = 1123;
                    expiration_days = 90L;
                    break;
                case 2430326:
                    mountid = 1129;
                    expiration_days = 90L;
                    break;
                case 2430328:
                    mountid = 1130;
                    expiration_days = 90L;
                    break;
                case 2430330:
                    mountid = 1063;
                    expiration_days = 90L;
                    break;
                case 2430332:
                    mountid = 1025;
                    expiration_days = 90L;
                    break;
                case 2430334:
                    mountid = 1034;
                    expiration_days = 90L;
                    break;
                case 2430336:
                    mountid = 1136;
                    expiration_days = 90L;
                    break;
                case 2430338:
                    mountid = 1051;
                    expiration_days = 90L;
                    break;
                case 2430340:
                    mountid = 1138;
                    expiration_days = 90L;
                    break;
                case 2430342:
                    mountid = 1139;
                    expiration_days = 90L;
                    break;
                case 2430344:
                    mountid = 1027;
                    expiration_days = 90L;
                    break;
                case 2430347:
                    mountid = 1029;
                    expiration_days = 90L;
                    break;
                case 2430349:
                    mountid = 1028;
                    expiration_days = 90L;
                    break;
                case 2430351:
                    mountid = 1033;
                    expiration_days = 90L;
                    break;
                case 2430353:
                    mountid = 1064;
                    expiration_days = 90L;
                    break;
                case 2430355:
                    mountid = 1096;
                    expiration_days = 90L;
                    break;
                case 2430357:
                    mountid = 1101;
                    expiration_days = 90L;
                    break;
                case 2430359:
                    mountid = 1102;
                    expiration_days = 90L;
                    break;
                case 2430361:
                    mountid = 1054;
                    expiration_days = 90L;
                    break;
                case 2430363:
                    mountid = 1053;
                    expiration_days = 90L;
                    break;
                case 2430324:
                    mountid = 1158;
                    expiration_days = -1L;
                    break;
                case 2430345:
                    mountid = 1158;
                    expiration_days = 90L;
                    break;
                case 2430367:
                    mountid = 1115;
                    expiration_days = 3L;
                    break;
                case 2430365:
                    mountid = 1025;
                    expiration_days = 365L;
                    break;
                case 2430366:
                    mountid = 1025;
                    expiration_days = 15L;
                    break;
                case 2430369:
                    mountid = 1049;
                    expiration_days = 10L;
                    break;
                case 2430392:
                    mountid = 80001038;
                    expiration_days = 90L;
                    break;
                case 2430476:
                    mountid = 1039;
                    expiration_days = 15L;
                    break;
                case 2430477:
                    mountid = 1039;
                    expiration_days = 365L;
                    break;
                case 2430232:
                    mountid = 1106;
                    expiration_days = 10L;
                    break;
                case 2430511:
                    mountid = 80001033;
                    expiration_days = 15L;
                    break;
                case 2430512:
                    mountid = 80001033;
                    expiration_days = 365L;
                    break;
                case 2430536:
                    mountid = 80001114;
                    expiration_days = 365L;
                    break;
                case 2430537:
                    mountid = 80001114;
                    expiration_days = 15L;
                    break;
                case 2430229:
                    mountid = 1102;
                    expiration_days = 60L;
                    break;
                case 2430199:
                    mountid = 1102;
                    expiration_days = 60L;
                    break;
                case 2430206:
                    mountid = 1089;
                    expiration_days = 7L;
                    break;
                case 2430211:
                    mountid = 80001009;
                    expiration_days = 30L;
                    break;
                default:
                    System.out.println(new StringBuilder().append("New scripted item : ").append(toUse.getItemId()).toString());
            }
        }

        if (mountid > 0) {
            mountid = PlayerStats.getSkillByJob(mountid, c.getPlayer().getJob());
            int fk = GameConstants.getMountItem(mountid, c.getPlayer());
            if (GameConstants.GMS && fk > 0 && mountid < 80001000) { //TODO JUMP
                for (int i = 80001001; i < 80001999; i++) {
                    Skill skill = SkillFactory.getSkill(i);
                    if (skill != null && GameConstants.getMountItem(skill.getId(), c.getPlayer()) == fk) {
                        mountid = i;
                        break;
                    }
                }
            }
            if (c.getPlayer().getSkillLevel(mountid) > 0) {
                c.getPlayer().dropMessage(5, "You already have this skill.");
            } else if (SkillFactory.getSkill(mountid) == null || GameConstants.getMountItem(mountid, c.getPlayer()) == 0) {
                c.getPlayer().dropMessage(5, "The skill could not be gained.");
            } else if (expiration_days > 0) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (byte) 1, false);
                c.getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(mountid), (byte) 1, (byte) 1, System.currentTimeMillis() + (long) (expiration_days * 24 * 60 * 60 * 1000));
                c.getPlayer().dropMessage(5, "The skill has been attained.");
            }
        }
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void UseSummonBag(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
//        if ((!chr.isAlive()) || (chr.hasBlockedInventory()) || (chr.inPVP())) {
//            c.getSession().write(CWvsContext.enableActions());
//            return;
//        }
//        slea.readInt();
//        byte slot = (byte) slea.readShort();
//        int itemId = slea.readInt();
//        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
//
//        if ((toUse != null) && (toUse.getQuantity() >= 1) && (toUse.getItemId() == itemId) && ((c.getPlayer().getMapId() < 910000000) || (c.getPlayer().getMapId() > 910000022))) {
//            Map<String, Integer> toSpawn = MapleItemInformationProvider.getInstance().getEquipStats(itemId);
//
//            if (toSpawn == null) {
//                c.getSession().write(CWvsContext.enableActions());
//                return;
//            }
//            MapleMonster ht = null;
//            int type = 0;
//            for (Map.Entry i : toSpawn.entrySet()) {
//                if ((((String) i.getKey()).startsWith("mob")) && (Randomizer.nextInt(99) <= ((Integer) i.getValue()).intValue())) {
//                    ht = MapleLifeFactory.getMonster(Integer.parseInt(((String) i.getKey()).substring(3)));
//                    chr.getMap().spawnMonster_sSack(ht, chr.getPosition(), type);
//                }
//            }
//            if (ht == null) {
//                c.getSession().write(CWvsContext.enableActions());
//                return;
//            }
//
//            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
//        }
        c.getPlayer().dropMessage(1, "Disabled, probably because of you.");
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void UseTreasureChest(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        short slot = slea.readShort();
        int itemid = slea.readInt();

        Item toUse = chr.getInventory(MapleInventoryType.ETC).getItem((short) (byte) slot);
        if ((toUse == null) || (toUse.getQuantity() <= 0) || (toUse.getItemId() != itemid) || (chr.hasBlockedInventory())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }

        int keyIDforRemoval = 0;
        int reward;
        String box;
        switch (toUse.getItemId()) {
            case 4280000:
                reward = RandomRewards.getGoldBoxReward();
                keyIDforRemoval = 5490000;
                box = "Gold";
                break;
            case 4280001:
                reward = RandomRewards.getSilverBoxReward();
                keyIDforRemoval = 5490001;
                box = "Silver";
                break;
            default:
                return;
        }

        int amount = 1;
        switch (reward) {
            case 2000004:
                amount = 200;
                break;
            case 2000005:
                amount = 100;
        }

        if (chr.getInventory(MapleInventoryType.CASH).countById(keyIDforRemoval) > 0) {
            Item item = MapleInventoryManipulator.addbyId_Gachapon(c, reward, (short) amount);

            if (item == null) {
                chr.dropMessage(5, "Please check your item inventory and see if you have a Master Key, or if the inventory is full.");
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, (short) (byte) slot, (short) 1, true);
            MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, keyIDforRemoval, 1, true, false);
            c.getSession().write(CWvsContext.InfoPacket.getShowItemGain(reward, (short) amount, true));

            if (GameConstants.gachaponRareItem(item.getItemId()) > 0) {
                World.Broadcast.broadcastSmega(CWvsContext.getGachaponMega(c.getPlayer().getName(), " : got a(n)", item, (byte) 2, new StringBuilder().append("[").append(box).append(" Chest]").toString()));
            }
        } else {
            chr.dropMessage(5, "Please check your item inventory and see if you have a Master Key, or if the inventory is full.");
            c.getSession().write(CWvsContext.enableActions());
        }
    }

    public static final void UseCashItem(LittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer() == null) || (c.getPlayer().getMap() == null) || (c.getPlayer().inPVP())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (c.getPlayer().getMap().getId() == GameConstants.JAIL) {
            c.getPlayer().dropMessage(5, "You're in jail, herp derp.");
            c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
            return;
        }
        if (c.getPlayer().isMuted() || (c.getPlayer().getMap().getMuted() && !c.getPlayer().isGM())) {
            c.getPlayer().dropMessage(5, c.getPlayer().isMuted() ? "You are Muted, therefore you are unable to talk. " : "The map is Muted, therefore you are unable to talk.");
            c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
            return;
        }
        slea.readInt();
        c.getPlayer().setScrolledPosition((short) 0);
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();

        Item toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((short) slot);
        if ((toUse == null) || (toUse.getItemId() != itemId) || (toUse.getQuantity() < 1) || (c.getPlayer().hasBlockedInventory())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }

        boolean used = false;
        boolean cc = false;

        switch (itemId) {
            case 5043000:
            case 5043001:
                short questid = slea.readShort();
                int npcid = slea.readInt();
                MapleQuest quest = MapleQuest.getInstance(questid);

                if ((c.getPlayer().getQuest(quest).getStatus() == 1) && (quest.canComplete(c.getPlayer(), Integer.valueOf(npcid)))) {
                    int mapId = MapleLifeFactory.getNPCLocation(npcid);
                    if (mapId != -1) {
                        MapleMap map = c.getChannelServer().getMapFactory().getMap(mapId);
                        if ((map.containsNPC(npcid)) && (!FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())) && (!FieldLimitType.VipRock.check(map.getFieldLimit())) && (!c.getPlayer().isInBlockedMap())) {
                            c.getPlayer().changeMap(map, map.getPortal(0));
                        }
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, "Unknown error has occurred.");
                    }
                }
                break;
            case 2320000:
            case 5040000:
            case 5040001:
            case 5040002:
            case 5040003:
            case 5040004:
            case 5041000:
            case 5041001:
                used = UseTeleRock(slea, c, itemId);
                break;
            case 5450005:
                c.getPlayer().setConversation(4);
                c.getPlayer().getStorage().sendStorage(c, 1022005);
                break;
            case 5050000:
                c.getPlayer().dropMessage(1, "Disabled.");
                c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                break;

            case 5220083:
                used = true;
                for (Entry<Integer, StructFamiliar> f : MapleItemInformationProvider.getInstance().getFamiliars().entrySet()) {
                    if ((((StructFamiliar) f.getValue()).itemid == 2870055) || (((StructFamiliar) f.getValue()).itemid == 2871002) || (((StructFamiliar) f.getValue()).itemid == 2870235) || (((StructFamiliar) f.getValue()).itemid == 2870019)) {
                        MonsterFamiliar mf = (MonsterFamiliar) c.getPlayer().getFamiliars().get(f.getKey());
                        if (mf != null) {
                            if (mf.getVitality() >= 3) {
                                mf.setExpiry(Math.min(System.currentTimeMillis() + 7776000000L, mf.getExpiry() + 2592000000L));
                            } else {
                                mf.setVitality(mf.getVitality() + 1);
                                mf.setExpiry(mf.getExpiry() + 2592000000L);
                            }
                        } else {
                            mf = new MonsterFamiliar(c.getPlayer().getId(), ((Integer) f.getKey()).intValue(), System.currentTimeMillis() + 2592000000L);
                            c.getPlayer().getFamiliars().put(f.getKey(), mf);
                        }
                        c.getSession().write(CField.registerFamiliar(mf));
                    }
                }
                break;
            case 5220084:
                if (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() < 3) {
                    c.getPlayer().dropMessage(5, "Make 3 USE space.");
                } else {
                    used = true;
                    int[] familiars = new int[3];
                    while (true) {
                        for (int i = 0; i < familiars.length; i++) {
                            if (familiars[i] <= 0) {
                                for (Map.Entry f : MapleItemInformationProvider.getInstance().getFamiliars().entrySet()) {
                                    if ((Randomizer.nextInt(500) == 0) && (((i < 2) && (((StructFamiliar) f.getValue()).grade == 0)) || ((i == 2) && (((StructFamiliar) f.getValue()).grade != 0)))) {
                                        MapleInventoryManipulator.addById(c, ((StructFamiliar) f.getValue()).itemid, (short) 1, "Booster Pack");

                                        familiars[i] = ((StructFamiliar) f.getValue()).itemid;
                                        break;
                                    }
                                }
                            }
                        }
                        if ((familiars[0] > 0) && (familiars[1] > 0) && (familiars[2] > 0)) {
                            break;
                        }
                    }
                    c.getSession().write(MTSCSPacket.getBoosterPack(familiars[0], familiars[1], familiars[2]));
                    c.getSession().write(MTSCSPacket.getBoosterPackClick());
                    c.getSession().write(MTSCSPacket.getBoosterPackReveal());
                }
                break;
            case 5050001:
            case 5050002:
            case 5050003:
            case 5050004:
            case 5050005:
            case 5050006:
            case 5050007:
            case 5050008:
            case 5050009:
                if ((itemId >= 5050005) && (!GameConstants.isEvan(c.getPlayer().getJob()))) {
                    c.getPlayer().dropMessage(1, "This reset is only for Evans.");
                } else if ((itemId < 5050005) && (GameConstants.isEvan(c.getPlayer().getJob()))) {
                    c.getPlayer().dropMessage(1, "This reset is only for non-Evans.");
                } else {
                    int skill1 = slea.readInt();
                    int skill2 = slea.readInt();
                    for (int i : GameConstants.blockedSkills) {
                        if (skill1 == i) {
                            c.getPlayer().dropMessage(1, "You may not add this skill.");
                            return;
                        }
                    }

                    Skill skillSPTo = SkillFactory.getSkill(skill1);
                    Skill skillSPFrom = SkillFactory.getSkill(skill2);

                    if ((skillSPTo.isBeginnerSkill()) || (skillSPFrom.isBeginnerSkill())) {
                        c.getPlayer().dropMessage(1, "You may not add beginner skills.");
                    } else if (GameConstants.getSkillBookForSkill(skill1) != GameConstants.getSkillBookForSkill(skill2)) {
                        c.getPlayer().dropMessage(1, "You may not add different job skills.");
                    } else if ((c.getPlayer().getSkillLevel(skillSPTo) + 1 <= skillSPTo.getMaxLevel()) && (c.getPlayer().getSkillLevel(skillSPFrom) > 0) && (skillSPTo.canBeLearnedBy(c.getPlayer().getJob()))) {
                        if ((skillSPTo.isFourthJob()) && (c.getPlayer().getSkillLevel(skillSPTo) + 1 > c.getPlayer().getMasterLevel(skillSPTo))) {
                            c.getPlayer().dropMessage(1, "You will exceed the master level.");
                        } else {
                            if (itemId >= 5050005) {
                                if ((GameConstants.getSkillBookForSkill(skill1) != (itemId - 5050005) * 2) && (GameConstants.getSkillBookForSkill(skill1) != (itemId - 5050005) * 2 + 1)) {
                                    c.getPlayer().dropMessage(1, "You may not add this job SP using this reset.");
                                    break;
                                }
                            } else {
                                int theJob = GameConstants.getJobNumber(skill2 / 10000);
                                switch (skill2 / 10000) {
                                    case 430:
                                        theJob = 1;
                                        break;
                                    case 431:
                                    case 432:
                                        theJob = 2;
                                        break;
                                    case 433:
                                        theJob = 3;
                                        break;
                                    case 434:
                                        theJob = 4;
                                }

                                if (theJob != itemId - 5050000) {
                                    c.getPlayer().dropMessage(1, "You may not subtract from this skill. Use the appropriate SP reset.");
                                    break;
                                }
                            }
                            Map sa = new HashMap();
                            sa.put(skillSPFrom, new SkillEntry((byte) (c.getPlayer().getSkillLevel(skillSPFrom) - 1), c.getPlayer().getMasterLevel(skillSPFrom), SkillFactory.getDefaultSExpiry(skillSPFrom)));
                            sa.put(skillSPTo, new SkillEntry((byte) (c.getPlayer().getSkillLevel(skillSPTo) + 1), c.getPlayer().getMasterLevel(skillSPTo), SkillFactory.getDefaultSExpiry(skillSPTo)));
                            c.getPlayer().changeSkillsLevel(sa);
                            used = true;
                        }
                    }
                }
                break;
            case 5500000: {
                Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                int days = 1;
                if ((item != null) && (!GameConstants.isAccessory(item.getItemId())) && (item.getExpiration() > -1L) && (!ii.isCash(item.getItemId())) && (System.currentTimeMillis() + 8640000000L > item.getExpiration() + 86400000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if ((c.getPlayer().getName().indexOf(z) != -1) || (item.getOwner().indexOf(z) != -1)) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + 86400000L);
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5500001: {
                Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                int days = 7;
                if ((item != null) && (!GameConstants.isAccessory(item.getItemId())) && (item.getExpiration() > -1L) && (!ii.isCash(item.getItemId())) && (System.currentTimeMillis() + 8640000000L > item.getExpiration() + 604800000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if ((c.getPlayer().getName().indexOf(z) != -1) || (item.getOwner().indexOf(z) != -1)) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + 604800000L);
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5500002: {
                Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                int days = 20;
                if ((item != null) && (!GameConstants.isAccessory(item.getItemId())) && (item.getExpiration() > -1L) && (!ii.isCash(item.getItemId())) && (System.currentTimeMillis() + 8640000000L > item.getExpiration() + 1728000000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if ((c.getPlayer().getName().indexOf(z) != -1) || (item.getOwner().indexOf(z) != -1)) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + 1728000000L);
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5500005: {
                Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                int days = 50;
                if ((item != null) && (!GameConstants.isAccessory(item.getItemId())) && (item.getExpiration() > -1L) && (!ii.isCash(item.getItemId())) && (System.currentTimeMillis() + 8640000000L > item.getExpiration() + 4320000000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if ((c.getPlayer().getName().indexOf(z) != -1) || (item.getOwner().indexOf(z) != -1)) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + 25032704L);
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5500006: {
                Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                int days = 99;
                if ((item != null) && (!GameConstants.isAccessory(item.getItemId())) && (item.getExpiration() > -1L) && (!ii.isCash(item.getItemId())) && (System.currentTimeMillis() + 8640000000L > item.getExpiration() + 8553600000L)) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if ((c.getPlayer().getName().indexOf(z) != -1) || (item.getOwner().indexOf(z) != -1)) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setExpiration(item.getExpiration() + -36334592L);
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(1, "It may not be used on this item.");
                    }
                }
                break;
            }
            case 5060000: {
                Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slea.readShort());

                if ((item != null) && (item.getOwner().equals(""))) {
                    boolean change = true;
                    for (String z : GameConstants.RESERVED) {
                        if (c.getPlayer().getName().indexOf(z) != -1) {
                            change = false;
                        }
                    }
                    if (change) {
                        item.setOwner(c.getPlayer().getName());
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIPPED);
                        used = true;
                    }
                }
                break;
            }
            case 5680015:
                if (c.getPlayer().getFatigue() > 0) {
                    c.getPlayer().setFatigue(0);
                    used = true;
                }
                break;
            case 5534000: {
                Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) (byte) slea.readInt());
                if (item != null) {
                    Equip eq = (Equip) item;
                    if (eq.getState() == 0) {
                        eq.resetPotential();
                        c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, itemId));
                        c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(toUse, item, false, true));
                        c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(5, "This item's Potential cannot be reset.");
                    }
                } else {
                    c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), false, itemId));
                }
                c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                break;

            }
            case 5062000: {
                if (c.getPlayer().getLevel() < 50) {
                    c.getPlayer().dropMessage(1, "You may not use this until level 50.");
                } else {
                    Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) (byte) slea.readInt());
                    if ((item != null) && (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1)) {
                        Equip eq = (Equip) item;
                        if ((eq.getState() >= 17) && (eq.getState() != 20)) {
                            eq.renewPotential(0);
                            c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, itemId));
                            c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(toUse, item, false, true));
                            c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                            MapleInventoryManipulator.addById(c, 2430112, (short) 1, new StringBuilder().append("Cube on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(1));
                            used = true;
                        } else {
                            c.getPlayer().dropMessage(5, "This item's Potential cannot be reset.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make <USE> some space.");
                    }
                }
                c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                break;
            }
            case 5062001:
            case 5062100:
                if (c.getPlayer().getLevel() < 70) {
                    c.getPlayer().dropMessage(1, "You may not use this until level 70.");
                } else {
                    Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) (byte) slea.readInt());
                    if ((item != null) && (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1)) {
                        Equip eq = (Equip) item;
                        if ((eq.getState() >= 17) && (eq.getState() != 20)) {
                            eq.renewPotential(1);
                            c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, itemId));
                            c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(toUse, item, false, true));
                            c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                            MapleInventoryManipulator.addById(c, 2430112, (short) 1, new StringBuilder().append("Cube on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            used = true;
                        } else {
                            c.getPlayer().dropMessage(5, "This item's Potential cannot be reset.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make <USE> some space.");
                    }
                }
                c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                break;
            case 5062002:
            case 5062003:
                if (c.getPlayer().getLevel() < 100) {
                    c.getPlayer().dropMessage(1, "You may not use this until level 100.");
                } else {
                    Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) (byte) slea.readInt());
                    if ((item != null) && (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1)) {
                        Equip eq = (Equip) item;
                        if (eq.getState() >= 17) {
                            eq.renewPotential(3);
                            c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, itemId));
                            c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(toUse, item, false, true));
                            c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                            MapleInventoryManipulator.addById(c, 2430481, (short) 1, new StringBuilder().append("Cube on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(1));
                            used = true;
                        } else {
                            c.getPlayer().dropMessage(5, "This item's Potential cannot be reset.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make <USE> some space.");
                    }
                }
                c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());

                break;
            case 5062005:
            case 5062006:
                if (c.getPlayer().getLevel() < 100) {
                    c.getPlayer().dropMessage(1, "You may not use this until level 100.");
                } else {
                    Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) (byte) slea.readInt());
                    if ((item != null) && (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1)) {
                        Equip eq = (Equip) item;
                        if (eq.getState() >= 17) {
                            eq.renewPotential(3);
                            c.getPlayer().getMap().broadcastMessage(CField.showPotentialReset(false, c.getPlayer().getId(), true, itemId));
                            c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(toUse, item, false, true));
                            c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                            MapleInventoryManipulator.addById(c, 2430759, (short) 1, new StringBuilder().append("Cube on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(1));
                            used = true;
                        } else {
                            c.getPlayer().dropMessage(5, "This item's Potential cannot be reset.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "Please make <USE> some space.");
                    }
                }
                c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                break;
            case 5750000:
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(1, "You may not use this until level 10.");
                } else {
                    Item item = c.getPlayer().getInventory(MapleInventoryType.SETUP).getItem((short) (byte) slea.readInt());
                    if ((item != null) && (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) && (c.getPlayer().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() >= 1)) {
                        int grade = GameConstants.getNebuliteGrade(item.getItemId());
                        if ((grade != -1) && (grade < 4)) {
                            int rank = Randomizer.nextInt(100) < 7 ? grade : grade != 3 ? grade + 1 : Randomizer.nextInt(100) < 2 ? grade + 1 : grade;
                            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                            List pots = new LinkedList(ii.getAllSocketInfo(rank).values());
                            int newId = 0;
                            while (newId == 0) {
                                StructItemOption pot = (StructItemOption) pots.get(Randomizer.nextInt(pots.size()));
                                if (pot != null) {
                                    newId = pot.opID;
                                }
                            }
                            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.SETUP, item.getPosition(), (short) 1, false);
                            MapleInventoryManipulator.addById(c, newId, (short) 1, new StringBuilder().append("Upgraded from alien cube on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            MapleInventoryManipulator.addById(c, 2430691, (short) 1, new StringBuilder().append("Alien Cube on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                            used = true;
                        } else {
                            c.getPlayer().dropMessage(1, "Grade S Nebulite cannot be added.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "You do not have sufficient inventory slot.");
                    }
                }
                c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                break;
            case 5750001:
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(1, "You may not use this until level 10.");
                } else {
                    Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) (byte) slea.readInt());
                    if (item != null) {
                        Equip eq = (Equip) item;
                        if (eq.getSocket1() > 0) {
                            eq.setSocket1(0);
                            c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(toUse, item, false, true));
                            c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                            used = true;
                        } else {
                            c.getPlayer().dropMessage(5, "This item do not have a socket.");
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "This item's nebulite cannot be removed.");
                    }
                }
                c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                break;
            case 5521000: {
                MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                Item item = c.getPlayer().getInventory(type).getItem((short) (byte) slea.readInt());

//                if ((item != null) && (!ItemFlag.KARMA_ACC.check(item.getFlag())) && (!ItemFlag.KARMA_ACC_USE.check(item.getFlag()))
//                        && (MapleItemInformationProvider.getInstance().isShareTagEnabled(item.getItemId()))) {
//                    short flag = item.getFlag();
//                    if (ItemFlag.UNTRADEABLE.check(flag)) {
//                        flag = (short) (flag - ItemFlag.UNTRADEABLE.getValue());
//                    } else if (type == MapleInventoryType.EQUIP) {
//                        flag = (short) (flag | ItemFlag.KARMA_ACC.getValue());
//                    } else {
//                        flag = (short) (flag | ItemFlag.KARMA_ACC_USE.getValue());
//                    }
//                    item.setFlag(flag);
//                    c.getPlayer().forceReAddItem_NoUpdate(item, type);
//                    c.getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse(item, type.getType(), item.getPosition(), true, c.getPlayer()));
//                    used = true;
//                }
                c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                break;
            }
            case 5520000:
            case 5520001: {
                MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                Item item = c.getPlayer().getInventory(type).getItem((short) (byte) slea.readInt());

                if ((item != null) && (!ItemFlag.KARMA_EQ.check(item.getFlag())) && (!ItemFlag.KARMA_USE.check(item.getFlag())) && (((itemId == 5520000) && (MapleItemInformationProvider.getInstance().isKarmaEnabled(item.getItemId()))) || ((itemId == 5520001) && (MapleItemInformationProvider.getInstance().isPKarmaEnabled(item.getItemId()))))) {
                    short flag = item.getFlag();
                    if (ItemFlag.UNTRADEABLE.check(flag)) {
                        flag = (short) (flag - ItemFlag.UNTRADEABLE.getValue());
                    } else if (type == MapleInventoryType.EQUIP) {
                        flag = (short) (flag | ItemFlag.KARMA_EQ.getValue());
                    } else {
                        flag = (short) (flag | ItemFlag.KARMA_USE.getValue());
                    }
                    item.setFlag(flag);
                    c.getPlayer().forceReAddItem_NoUpdate(item, type);
                    c.getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse(item, type.getType(), item.getPosition(), true, c.getPlayer()));
                    used = true;
                }
                break;
            }
            case 5570000: {
                slea.readInt();
                Equip item = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) (byte) slea.readInt());

                if (item != null) {
                    if ((GameConstants.canHammer(item.getItemId())) && (MapleItemInformationProvider.getInstance().getSlots(item.getItemId()) > 0) && (item.getViciousHammer() < 2)) {
                        item.setViciousHammer((byte) (item.getViciousHammer() + 1));
                        item.setUpgradeSlots((byte) (item.getUpgradeSlots() + 1));
                        c.getPlayer().forceReAddItem(item, MapleInventoryType.EQUIP);
                        c.getSession().write(MTSCSPacket.ViciousHammer(true, item.getViciousHammer()));
                        c.getPlayer().fakeRelog();
                        c.getPlayer().dropMessage(1, new StringBuilder().append("Added 1 slot successfully to ").append(item).toString());
                        used = true;
                    } else {
                        c.getPlayer().dropMessage(5, "You may not use it on this item.");
                        c.getSession().write(MTSCSPacket.ViciousHammer(true, 0));
                    }
                }
                break;
            }
            case 5610000:
            case 5610001:
                slea.readInt();
                short dst = (short) slea.readInt();
                slea.readInt();
                short src = (short) slea.readInt();
                used = UseUpgradeScroll(src, dst, (short) 2, c, c.getPlayer(), itemId, false, false);
                cc = used;
                break;
            case 5060001: {
                MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                Item item = c.getPlayer().getInventory(type).getItem((short) (byte) slea.readInt());

                if ((item != null) && (item.getExpiration() == -1L)) {
                    short flag = item.getFlag();
                    flag = (short) (flag | ItemFlag.LOCK.getValue());
                    item.setFlag(flag);

                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5061000: {
                MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                Item item = c.getPlayer().getInventory(type).getItem((short) (byte) slea.readInt());

                if ((item != null) && (item.getExpiration() == -1L)) {
                    short flag = item.getFlag();
                    flag = (short) (flag | ItemFlag.LOCK.getValue());
                    item.setFlag(flag);
                    item.setExpiration(System.currentTimeMillis() + 604800000L);

                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5061001: {
                MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                Item item = c.getPlayer().getInventory(type).getItem((short) (byte) slea.readInt());

                if ((item != null) && (item.getExpiration() == -1L)) {
                    short flag = item.getFlag();
                    flag = (short) (flag | ItemFlag.LOCK.getValue());
                    item.setFlag(flag);

                    item.setExpiration(System.currentTimeMillis() + -1702967296L);

                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5064200:
            case 5062300: {//resets all stats except for potential
                c.getPlayer().dropMessage(6, "Please use the scrolls.. not this cash item.");
                break;
            }
            case 5064300:
            case 5064301: {
                Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) slea.readShort());
                if (item != null && item.getType() == 1) { //equip
                    short flag = item.getFlag();
                    flag |= ItemFlag.SCROLL_PROTECT.getValue();
                    item.setFlag(flag);

                    c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(toUse, item, false, false));
                    used = true;
                }
                break;
            }
            case 5061002: {
                MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                Item item = c.getPlayer().getInventory(type).getItem((short) (byte) slea.readInt());

                if ((item != null) && (item.getExpiration() == -1L)) {
                    short flag = item.getFlag();
                    flag = (short) (flag | ItemFlag.LOCK.getValue());
                    item.setFlag(flag);

                    item.setExpiration(System.currentTimeMillis() + -813934592L);

                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5061003: {
                MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                Item item = c.getPlayer().getInventory(type).getItem((short) (byte) slea.readInt());

                if ((item != null) && (item.getExpiration() == -1L)) {
                    short flag = item.getFlag();
                    flag = (short) (flag | ItemFlag.LOCK.getValue());
                    item.setFlag(flag);

                    item.setExpiration(System.currentTimeMillis() + 1471228928L);

                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5063000: {
                MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                Item item = c.getPlayer().getInventory(type).getItem((short) (byte) slea.readInt());

                if ((item != null) && (item.getType() == 1)) {
                    short flag = item.getFlag();
                    flag = (short) (flag | ItemFlag.LUCKS_KEY.getValue());
                    item.setFlag(flag);

                    c.getPlayer().forceReAddItem_Flag(item, type);
                    used = true;
                }
                break;
            }
            case 5064000:
            case 5064002:
            case 5064003:
            case 5064004: {
                Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) slea.readShort());
                if (item != null && item.getType() == 1) { //equip
                    if (((Equip) item).getEnhance() >= 12) {
                        break; //cannot be used
                    }
                    short flag = item.getFlag();
                    flag |= ItemFlag.SHIELD_WARD.getValue();
                    item.setFlag(flag);

                    c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(toUse, item, false, false));
                    used = true;
                }
                break;
            }
            case 5064100:
            case 5064101: {
                Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((byte) slea.readShort());
                if (item != null && item.getType() == 1) { //equip
                    if (((Equip) item).getEnhance() >= 12) {
                        break; //cannot be used
                    }
                    short flag = item.getFlag();
                    flag |= ItemFlag.SLOTS_PROTECT.getValue();
                    item.setFlag(flag);

                    c.getSession().write(CWvsContext.InventoryPacket.scrolledItem(toUse, item, false, false));
                    used = true;
                }
                break;
            }
            case 5060003:
            case 5060004: {
                Item item = c.getPlayer().getInventory(MapleInventoryType.ETC).findById(itemId == 5060003 ? 4170023 : 4170024);
                if ((item == null) || (item.getQuantity() <= 0)) {
                    return;
                }
                if (getIncubatedItems(c, itemId)) {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.ETC, item.getPosition(), (short) 1, false);
                    used = true;
                }
                break;
            }
            case 5070000:
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                } else if (c.getPlayer().getMapId() == 910310300) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                    String message = slea.readMapleAsciiString();

                    if (message.length() <= 65) {
                        StringBuilder sb = new StringBuilder();
                        addMedalString(c.getPlayer(), sb);
                        sb.append(c.getPlayer().getName());
                        sb.append(" : ");
                        sb.append(message);

                        c.getPlayer().getMap().broadcastMessage(CWvsContext.serverNotice(2, sb.toString()));
                        used = true;
                    }
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }

                break;
            case 5071000:
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                } else if (c.getPlayer().getMapId() == 910310300) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                    String message = slea.readMapleAsciiString();

                    if (message.length() <= 65) {
                        StringBuilder sb = new StringBuilder();
                        addMedalString(c.getPlayer(), sb);
                        sb.append(c.getPlayer().getName());
                        sb.append(" : ");
                        sb.append(message);

                        c.getChannelServer().broadcastSmegaPacket(CWvsContext.serverNotice(2, sb.toString()));
                        used = true;
                    }
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }

                break;
            case 5077000:
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                } else if (c.getPlayer().getMapId() == 910310300) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                    byte numLines = slea.readByte();
                    if (numLines > 3) {
                        return;
                    }
                    List messages = new LinkedList();

                    for (int i = 0; i < numLines; i++) {
                        String message = slea.readMapleAsciiString();
                        if (message.length() > 65) {
                            break;
                        }
                        messages.add(new StringBuilder().append(c.getPlayer().getName()).append(" : ").append(message).toString());
                    }
                    boolean ear = slea.readByte() > 0;

                    World.Broadcast.broadcastSmega(CWvsContext.tripleSmega(messages, ear, c.getChannel()));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            case 5079004:
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                } else if (c.getPlayer().getMapId() == 910310300) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                    String message = slea.readMapleAsciiString();

                    if (message.length() <= 65) {
                        World.Broadcast.broadcastSmega(CWvsContext.echoMegaphone(c.getPlayer().getName(), message));
                        used = true;
                    }
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }

                break;
            case 5073000:
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                } else if (c.getPlayer().getMapId() == 910310300) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                    String message = slea.readMapleAsciiString();

                    if (message.length() <= 65) {
                        StringBuilder sb = new StringBuilder();
                        addMedalString(c.getPlayer(), sb);
                        sb.append(c.getPlayer().getName());
                        sb.append(" : ");
                        sb.append(message);

                        boolean ear = slea.readByte() != 0;
                        World.Broadcast.broadcastSmega(CWvsContext.serverNotice(9, c.getChannel(), sb.toString(), ear));
                        used = true;
                    }
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }

                break;
            case 5074000:
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                } else if (c.getPlayer().getMapId() == 910310300) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                    String message = slea.readMapleAsciiString();

                    if (message.length() <= 65) {
                        StringBuilder sb = new StringBuilder();
                        addMedalString(c.getPlayer(), sb);
                        sb.append(c.getPlayer().getName());
                        sb.append(" : ");
                        sb.append(message);

                        boolean ear = slea.readByte() != 0;

                        World.Broadcast.broadcastSmega(CWvsContext.serverNotice(22, c.getChannel(), sb.toString(), ear));
                        used = true;
                    }
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }

                break;
            case 5072000:
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                } else if (c.getPlayer().getMapId() == 910310300) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                    String message = slea.readMapleAsciiString();

                    if (message.length() <= 65) {
                        StringBuilder sb = new StringBuilder();
                        addMedalString(c.getPlayer(), sb);
                        sb.append(c.getPlayer().getName());
                        sb.append(" : ");
                        sb.append(message);

                        boolean ear = slea.readByte() != 0;

                        World.Broadcast.broadcastSmega(CWvsContext.serverNotice(3, c.getChannel(), sb.toString(), ear));
                        used = true;
                    }
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }

                break;
            case 5076000:
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                } else if (c.getPlayer().getMapId() == 910310300) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                    String message = slea.readMapleAsciiString();

                    if (message.length() <= 65) {
                        StringBuilder sb = new StringBuilder();
                        addMedalString(c.getPlayer(), sb);
                        sb.append(c.getPlayer().getName());
                        sb.append(" : ");
                        sb.append(message);

                        boolean ear = slea.readByte() > 0;

                        Item item = null;
                        if (slea.readByte() == 1) {
                            byte invType = (byte) slea.readInt();
                            byte pos = (byte) slea.readInt();
                            if (pos <= 0) {
                                invType = -1;
                            }
                            item = c.getPlayer().getInventory(MapleInventoryType.getByType(invType)).getItem((short) pos);
                        }
                        World.Broadcast.broadcastSmega(CWvsContext.itemMegaphone(sb.toString(), ear, c.getChannel(), item));
                        used = true;
                    }
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }

                break;
            case 5075000:
            case 5075001:
            case 5075002:
                c.getPlayer().dropMessage(5, "There are no MapleTVs to broadcast the message to.");
                break;
            case 5075003:
            case 5075004:
            case 5075005:
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                } else if (c.getPlayer().getMapId() == 910310300) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                } else {
                    int tvType = itemId % 10;
                    if (tvType == 3) {
                        slea.readByte();
                    }
                    boolean ear = (tvType != 1) && (tvType != 2) && (slea.readByte() > 1);
                    MapleCharacter victim = (tvType == 1) || (tvType == 4) ? null : c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
                    if ((tvType == 0) || (tvType == 3)) {
                        victim = null;
                    } else if (victim == null) {
                        c.getPlayer().dropMessage(1, "That character is not in the channel.");
                        break;
                    }
                    String message = slea.readMapleAsciiString();
                    World.Broadcast.broadcastSmega(CWvsContext.serverNotice(3, c.getChannel(), new StringBuilder().append(c.getPlayer().getName()).append(" : ").append(message).toString(), ear));
                    used = true;
                }
                break;
            case 5090000:
            case 5090100: {
                String sendTo = slea.readMapleAsciiString();
                String msg = slea.readMapleAsciiString();
                c.getPlayer().sendNote(sendTo, msg);
                used = true;
                break;
            }
            case 5100000:
                c.getPlayer().getMap().broadcastMessage(CField.musicChange("Jukebox/Congratulation"));
                used = true;
                break;
            case 5190000:
            case 5190001:
            case 5190002:
            case 5190003:
            case 5190004:
            case 5190005:
            case 5190006:
            case 5190007:
            case 5190008: {
                int uniqueid = (int) slea.readLong();
                MaplePet pet = c.getPlayer().getPet(0);
                int slo = 0;

                if (pet != null) {
                    if (pet.getUniqueId() != uniqueid) {
                        pet = c.getPlayer().getPet(1);
                        slo = 1;
                        if (pet == null) {
                            break;
                        }
                        if (pet.getUniqueId() != uniqueid) {
                            pet = c.getPlayer().getPet(2);
                            slo = 2;
                            if ((pet == null)
                                    || (pet.getUniqueId() != uniqueid)) {
                                break;
                            }

                        }

                    }

                    MaplePet.PetFlag zz = MaplePet.PetFlag.getByAddId(itemId);
                    if ((zz != null) && (!zz.check(pet.getFlags()))) {
                        pet.setFlags(pet.getFlags() | zz.getValue());
                        c.getSession().write(PetPacket.updatePet(pet, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), true));
                        c.getSession().write(CWvsContext.enableActions());
                        c.getSession().write(MTSCSPacket.changePetFlag(uniqueid, true, zz.getValue()));
                        used = true;
                    }
                }
                break;
            }
            case 5191000:
            case 5191001:
            case 5191002:
            case 5191003:
            case 5191004: {
                int uniqueid = (int) slea.readLong();
                MaplePet pet = c.getPlayer().getPet(0);
                int slo = 0;

                if (pet != null) {
                    if (pet.getUniqueId() != uniqueid) {
                        pet = c.getPlayer().getPet(1);
                        slo = 1;
                        if (pet == null) {
                            break;
                        }
                        if (pet.getUniqueId() != uniqueid) {
                            pet = c.getPlayer().getPet(2);
                            slo = 2;
                            if ((pet == null)
                                    || (pet.getUniqueId() != uniqueid)) {
                                break;
                            }

                        }

                    }

                    MaplePet.PetFlag zz = MaplePet.PetFlag.getByDelId(itemId);
                    if ((zz != null) && (zz.check(pet.getFlags()))) {
                        pet.setFlags(pet.getFlags() - zz.getValue());
                        c.getSession().write(PetPacket.updatePet(pet, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), true));
                        c.getSession().write(CWvsContext.enableActions());
                        c.getSession().write(MTSCSPacket.changePetFlag(uniqueid, false, zz.getValue()));
                        used = true;
                    }
                }
                break;
            }
            case 5501001:
            case 5501002: {
                Skill skil = SkillFactory.getSkill(slea.readInt());
                if ((skil != null) && (skil.getId() / 10000 == 8000) && (c.getPlayer().getSkillLevel(skil) > 0) && (skil.isTimeLimited()) && (GameConstants.getMountItem(skil.getId(), c.getPlayer()) > 0)) {
                    long toAdd = (itemId == 5501001 ? 30 : 60) * 24 * 60 * 60 * 1000L;
                    long expire = c.getPlayer().getSkillExpiry(skil);
                    if ((expire >= System.currentTimeMillis()) && (expire + toAdd < System.currentTimeMillis() + 31536000000L)) {
                        c.getPlayer().changeSingleSkillLevel(skil, c.getPlayer().getSkillLevel(skil), c.getPlayer().getMasterLevel(skil), expire + toAdd);
                        used = true;
                    }
                }
                break;
            }
            case 5170000: {
                MaplePet pet = c.getPlayer().getPet(0);
                int slo = 0;

                if (pet != null) {
                    String nName = slea.readMapleAsciiString();

                    for (String z : GameConstants.RESERVED) {
                        if ((pet.getName().indexOf(z) != -1) || (nName.indexOf(z) != -1)) {
                            break;
                        }
                    }
                    if (MapleCharacterUtil.canChangePetName(nName)) {
                        pet.setName(nName);
                        c.getSession().write(PetPacket.updatePet(pet, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), true));
                        c.getSession().write(CWvsContext.enableActions());
                        c.getPlayer().getMap().broadcastMessage(MTSCSPacket.changePetName(c.getPlayer(), nName, slo));
                        used = true;
                    }
                }
                break;
            }
            case 5700000: {
                slea.skip(8);
                if (c.getPlayer().getAndroid() != null) {
                    String nName = slea.readMapleAsciiString();
                    for (String z : GameConstants.RESERVED) {
                        if ((c.getPlayer().getAndroid().getName().indexOf(z) != -1) || (nName.indexOf(z) != -1)) {
                            break;
                        }
                    }
                    if (MapleCharacterUtil.canChangePetName(nName)) {
                        c.getPlayer().getAndroid().setName(nName);
                        c.getPlayer().setAndroid(c.getPlayer().getAndroid());
                        used = true;
                    }
                }
                break;
            }

            case 5240000:
            case 5240001:
            case 5240002:
            case 5240003:
            case 5240004:
            case 5240005:
            case 5240006:
            case 5240007:
            case 5240008:
            case 5240009:
            case 5240010:
            case 5240011:
            case 5240012:
            case 5240013:
            case 5240014:
            case 5240015:
            case 5240016:
            case 5240017:
            case 5240018:
            case 5240019:
            case 5240020:
            case 5240021:
            case 5240022:
            case 5240023:
            case 5240024:
            case 5240025:
            case 5240026:
            case 5240027:
            case 5240028:
            case 5240029:
            case 5240030:
            case 5240031:
            case 5240032:
            case 5240033:
            case 5240034:
            case 5240035:
            case 5240036:
            case 5240037:
            case 5240038:
            case 5240039:
            case 5240040:
                MaplePet pet = c.getPlayer().getPet(0);

                if (pet != null) {
                    if (!pet.canConsume(itemId)) {
                        pet = c.getPlayer().getPet(1);
                        if (pet == null) {
                            break;
                        }
                        if (!pet.canConsume(itemId)) {
                            pet = c.getPlayer().getPet(2);
                            if ((pet == null)
                                    || (!pet.canConsume(itemId))) {
                                break;
                            }

                        }

                    }

                    byte petindex = c.getPlayer().getPetIndex(pet);
                    pet.setFullness(100);
                    if (pet.getCloseness() < 30000) {
                        if (pet.getCloseness() + 100 * c.getChannelServer().getTraitRate() > 30000) {
                            pet.setCloseness(30000);
                        } else {
                            pet.setCloseness(pet.getCloseness() + 100 * c.getChannelServer().getTraitRate());
                        }
                        if (pet.getCloseness() >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                            pet.setLevel(pet.getLevel() + 1);
                            c.getSession().write(CField.EffectPacket.showOwnPetLevelUp(c.getPlayer().getPetIndex(pet)));
                            c.getPlayer().getMap().broadcastMessage(PetPacket.showPetLevelUp(c.getPlayer(), petindex));
                        }
                    }
                    c.getSession().write(PetPacket.updatePet(pet, c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), true));
                    c.getPlayer().getMap().broadcastMessage(c.getPlayer(), PetPacket.commandResponse(c.getPlayer().getId(), (byte) 1, petindex, true, true), true);
                    used = true;
                }
                break;
            case 5230000:
            case 5230001:
                int itemSearch = slea.readInt();
                List hms = c.getChannelServer().searchMerchant(itemSearch);
                if (hms.size() > 0) {
                    c.getSession().write(CWvsContext.getOwlSearched(itemSearch, hms));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(1, "Unable to find the item.");
                }
                break;
            case 5280001:
            case 5281000:
            case 5281001:
                Rectangle bounds = new Rectangle((int) c.getPlayer().getPosition().getX(), (int) c.getPlayer().getPosition().getY(), 1, 1);
                MapleMist mist = new MapleMist(bounds, c.getPlayer());
                c.getPlayer().getMap().spawnMist(mist, 10000, true);
                c.getSession().write(CWvsContext.enableActions());
                used = true;
                break;
            case 5370000:
            case 5370001:
                break;
            case 5079000:
            case 5079001:
            case 5390000:
            case 5390001:
            case 5390002:
            case 5390003:
            case 5390004:
            case 5390005:
            case 5390006:
            case 5390007:
            case 5390008:
            case 5390009:
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "Must be level 10 or higher.");
                } else if (c.getPlayer().getMapId() == 910310300) {
                    c.getPlayer().dropMessage(5, "Cannot be used here.");
                } else if (!c.getChannelServer().getMegaphoneMuteState()) {
                    List lines = new LinkedList();
                    for (int i = 0; i < 4; i++) {
                        String text = slea.readMapleAsciiString();
                        if (text.length() <= 55) {
                            lines.add(text);
                        }
                    }
                    boolean ear = slea.readByte() != 0;
                    World.Broadcast.broadcastSmega(CWvsContext.getAvatarMega(c.getPlayer(), c.getChannel(), itemId, lines, ear));
                    used = true;
                } else {
                    c.getPlayer().dropMessage(5, "The usage of Megaphone is currently disabled.");
                }
                break;
            case 5450000:
            case 5450003:
            case 5452001:
                for (int i : GameConstants.blockedMaps) {
                    if (c.getPlayer().getMapId() == i) {
                        c.getPlayer().dropMessage(5, "You may not use this command here.");
                        c.getSession().write(CWvsContext.enableActions());
                        return;
                    }
                }
                if (c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(5, "You must be over level 10 to use this command.");
                } else if ((c.getPlayer().hasBlockedInventory()) || (c.getPlayer().getMap().getSquadByMap() != null) || (c.getPlayer().getEventInstance() != null) || (c.getPlayer().getMap().getEMByMap() != null) || (c.getPlayer().getMapId() >= 990000000)) {
                    c.getPlayer().dropMessage(5, "You may not use this command here.");
                } else if (((c.getPlayer().getMapId() >= 680000210) && (c.getPlayer().getMapId() <= 680000502)) || ((c.getPlayer().getMapId() / 1000 == 980000) && (c.getPlayer().getMapId() != 980000000)) || (c.getPlayer().getMapId() / 100 == 1030008) || (c.getPlayer().getMapId() / 100 == 922010) || (c.getPlayer().getMapId() / 10 == 13003000)) {
                    c.getPlayer().dropMessage(5, "You may not use this command here.");
                } else {
                    MapleShopFactory.getInstance().getShop(61).sendShop(c);
                }

                break;
            case 5300000:
            case 5300001:
            case 5300002: {
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                ii.getItemEffect(itemId).applyTo(c.getPlayer());
                used = true;
                break;
            }
            default:
                if (itemId / 10000 == 512) {
                    MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                    String msg = ii.getMsg(itemId);
                    String ourMsg = slea.readMapleAsciiString();
                    if (!msg.contains("%s")) {
                        msg = ourMsg;
                    } else {
                        msg = msg.replaceFirst("%s", c.getPlayer().getName());
                        if (!msg.contains("%s")) {
                            msg = ii.getMsg(itemId).replaceFirst("%s", ourMsg);
                        } else {
                            try {
                                msg = msg.replaceFirst("%s", ourMsg);
                            } catch (Exception e) {
                                msg = ii.getMsg(itemId).replaceFirst("%s", ourMsg);
                            }
                        }
                    }
                    c.getPlayer().getMap().startMapEffect(msg, itemId);

                    int buff = ii.getStateChangeItem(itemId);
                    if (buff != 0) {
                        for (MapleCharacter mChar : c.getPlayer().getMap().getCharactersThreadsafe()) {
                            ii.getItemEffect(buff).applyTo(mChar);
                        }
                    }
                    used = true;
                } else if (itemId / 10000 == 510) {
                    c.getPlayer().getMap().startJukebox(c.getPlayer().getName(), itemId);
                    used = true;
                } else if (itemId / 10000 == 520) {
                    int mesars = MapleItemInformationProvider.getInstance().getMeso(itemId);
                    if ((mesars > 0) && (c.getPlayer().getMeso() < 2147483647 - mesars)) {
                        used = true;
                        if (Math.random() > 0.1D) {
                            int gainmes = Randomizer.nextInt(mesars);
                            c.getPlayer().gainMeso(gainmes, false);
                            c.getSession().write(MTSCSPacket.sendMesobagSuccess(gainmes));
                        } else {
                            c.getSession().write(MTSCSPacket.sendMesobagFailed(false));
                        }
                    }
                } else if (itemId / 10000 == 562) {
                    if (UseSkillBook(slot, itemId, c, c.getPlayer())) {
                        c.getPlayer().gainSP(1);
                    }
                } else if (itemId / 10000 == 553) {
                    UseRewardItem(slot, itemId, c, c.getPlayer());
                } else if (itemId / 10000 != 519) {
                    System.out.println(new StringBuilder().append("Unhandled CS item : ").append(itemId).toString());
                    System.out.println(slea.toString(true));
                }
                break;
        }

        if (used) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, (short) slot, (short) 1, false, true);
        }
        c.getSession().write(CWvsContext.enableActions());
        if (cc) {
            if ((!c.getPlayer().isAlive()) || (c.getPlayer().getEventInstance() != null) || (FieldLimitType.ChannelSwitch.check(c.getPlayer().getMap().getFieldLimit()))) {
                c.getPlayer().dropMessage(1, "Auto relog failed.");
                return;
            }
            c.getPlayer().dropMessage(5, "Auto relogging. Please wait.");
            c.getPlayer().fakeRelog();
            if (c.getPlayer().getScrolledPosition() != 0) {
                c.getSession().write(CWvsContext.pamSongUI());
            }
        }
    }

    public static void ResetCoreAura(int slot, MapleClient c, MapleCharacter chr) {
        Item starDust = chr.getInventory(MapleInventoryType.USE).getItem((short) (byte) slot);
        if ((starDust == null) || (c.getPlayer().hasBlockedInventory())) {
            c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            return;
        }
    }

    public static final void useInnerCirculator(LittleEndianAccessor slea, MapleClient c) {
        int itemid = slea.readInt();
        short slot = (short) slea.readInt();
        Item item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem(slot);
        if (item.getItemId() == itemid) {
            List<InnerSkillValueHolder> newValues = new LinkedList();
            int i = 0;
            for (InnerSkillValueHolder isvh : c.getPlayer().getInnerSkills()) {
                if ((i == 0) && (c.getPlayer().getInnerSkills().size() > 1) && (itemid == 2701000)) {
                    newValues.add(InnerAbillity.getInstance().renewSkill(isvh.getRank(), itemid, true));
                } else {
                    newValues.add(InnerAbillity.getInstance().renewSkill(isvh.getRank(), itemid, false));
                }

                i++;
            }
            c.getPlayer().getInnerSkills().clear();
            for (InnerSkillValueHolder isvh : newValues) {
                c.getPlayer().getInnerSkills().add(isvh);
            }

            c.getPlayer().getInventory(MapleInventoryType.USE).removeItem(slot, (short) 1, false);

            c.getSession().write(CField.getCharInfo(c.getPlayer()));
            MapleMap currentMap = c.getPlayer().getMap();
            currentMap.removePlayer(c.getPlayer());
            currentMap.addPlayer(c.getPlayer());

            c.getPlayer().dropMessage(5, "Inner Potential has been reconfigured.");
        }
    }

    public static final void Pickup_Player(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if (c.getPlayer().hasBlockedInventory()) {
            return;
        }
        slea.readInt();
        c.getPlayer().setScrolledPosition((short) 0);
        slea.skip(1);
        Point Client_Reportedpos = slea.readPos();
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        MapleMapObject ob = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.ITEM);

        if (ob == null) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        MapleMapItem mapitem = (MapleMapItem) ob;
        Lock lock = mapitem.getLock();
        lock.lock();
        try {
            if (mapitem.isPickedUp()) {
                c.getSession().write(CWvsContext.enableActions());
            } else if ((mapitem.getQuest() > 0) && (chr.getQuestStatus(mapitem.getQuest()) != 1)) {
                c.getSession().write(CWvsContext.enableActions());
            } else if ((mapitem.getOwner() != chr.getId()) && (((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 0)) || ((mapitem.isPlayerDrop()) && (chr.getMap().getEverlast())))) {
                c.getSession().write(CWvsContext.enableActions());
            } else if ((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 1) && (mapitem.getOwner() != chr.getId()) && ((chr.getParty() == null) || (chr.getParty().getMemberById(mapitem.getOwner()) == null))) {
                c.getSession().write(CWvsContext.enableActions());
            } else {
                double Distance = Client_Reportedpos.distanceSq(mapitem.getPosition());
                if (mapitem.getMeso() > 0) {
                    if ((chr.getParty() != null) && (mapitem.getOwner() != chr.getId())) {
                        List<MapleCharacter> toGive = new LinkedList();
                        int splitMeso = mapitem.getMeso() * 40 / 100;
                        for (MaplePartyCharacter z : chr.getParty().getMembers()) {
                            MapleCharacter m = chr.getMap().getCharacterById(z.getId());
                            if ((m != null) && (m.getId() != chr.getId())) {
                                toGive.add(m);
                            }
                        }
                        for (MapleCharacter m : toGive) {
                            int mesos = splitMeso / toGive.size();
                            if (((mapitem.getDropper() instanceof MapleMonster)) && (m.getStat().incMesoProp > 0)) {
                                mesos = (int) (mesos + Math.floor(m.getStat().incMesoProp * mesos / 100.0F));
                            }
                            m.gainMeso(mesos, true);
                        }
                        int mesos = mapitem.getMeso() - splitMeso;
                        if (((mapitem.getDropper() instanceof MapleMonster)) && (chr.getStat().incMesoProp > 0)) {
                            mesos = (int) (mesos + Math.floor(chr.getStat().incMesoProp * mesos / 100.0F));
                        }
                        chr.gainMeso(mesos, true);
                    } else {
                        int mesos = mapitem.getMeso();
                        if (((mapitem.getDropper() instanceof MapleMonster)) && (chr.getStat().incMesoProp > 0)) {
                            mesos = (int) (mesos + Math.floor(chr.getStat().incMesoProp * mesos / 100.0F));
                        }
                        chr.gainMeso(mesos, true);
                    }
                    removeItem(chr, mapitem, ob);
//                } else if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItemId())) {
//                    c.getSession().write(CWvsContext.enableActions());
//                    c.getPlayer().dropMessage(5, "This item cannot be picked up.");
//                    if (c.getPlayer().haveItem(4001326, 1)) {
//                        c.getSession().write(CWvsContext.enableActions());
//                        c.getPlayer().dropMessage(5, "You may only have 1 of the color Red at a time.");
//                        return;
//                    }
//                    if (c.getPlayer().haveItem(4001327, 1)) {
//                        c.getSession().write(CWvsContext.enableActions());
//                        c.getPlayer().dropMessage(5, "You may only have 1 of the color Orange at a time.");
//                        return;
//                    }
//                    if (c.getPlayer().haveItem(4001328, 1)) {
//                        c.getSession().write(CWvsContext.enableActions());
//                        c.getPlayer().dropMessage(5, "You may only have 1 of the color Yellow at a time.");
//                        return;
//                    }
//                    if (c.getPlayer().haveItem(4001329, 1)) {
//                        c.getSession().write(CWvsContext.enableActions());
//                        c.getPlayer().dropMessage(5, "You may only have 1 of the color Green at a time.");
//                        return;
//                    }
//                    if (c.getPlayer().haveItem(4001330, 1)) {
//                        c.getSession().write(CWvsContext.enableActions());
//                        c.getPlayer().dropMessage(5, "You may only have 1 of the color Blue at a time.");
//                        return;
//                    }
//                    if (c.getPlayer().haveItem(4001331, 1)) {
//                        c.getSession().write(CWvsContext.enableActions());
//                        c.getPlayer().dropMessage(5, "You may only have 1 of the color Indigo at a time.");
//                        return;
//                    }
//                    if (c.getPlayer().haveItem(4001332, 1)) {
//                        c.getSession().write(CWvsContext.enableActions());
//                        c.getPlayer().dropMessage(5, "You may only have 1 of the color Violet at a time.");
//                    }
                } else if ((c.getPlayer().inPVP()) && (Integer.parseInt(c.getPlayer().getEventInstance().getProperty("ice")) == c.getPlayer().getId())) {
                    c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
                    c.getSession().write(CWvsContext.InventoryPacket.getShowInventoryFull());
                    c.getSession().write(CWvsContext.enableActions());
                } else if (useItem(c, mapitem.getItemId())) {
                    removeItem(c.getPlayer(), mapitem, ob);

                    if (mapitem.getItemId() / 10000 == 291) {
                        c.getPlayer().getMap().broadcastMessage(CField.getCapturePosition(c.getPlayer().getMap()));
                        c.getPlayer().getMap().broadcastMessage(CField.resetCapture());
                    }
                } else if ((mapitem.getItemId() / 10000 != 291) && (MapleInventoryManipulator.checkSpace(c, mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner()))) {
                    if ((mapitem.getItem().getQuantity() >= 50) && (mapitem.getItemId() == 2340000)) {
                        c.setMonitored(true);
                    }
                    MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true, mapitem.getDropper() instanceof MapleMonster);
                    removeItem(chr, mapitem, ob);
                } else {
                    c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
                    c.getSession().write(CWvsContext.InventoryPacket.getShowInventoryFull());
                    c.getSession().write(CWvsContext.enableActions());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public static final void Pickup_Pet(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if (chr == null) {
            return;
        }

        c.getPlayer().setScrolledPosition((short) 0);
        byte petz = (byte) (GameConstants.GMS ? c.getPlayer().getPetIndex((int) slea.readLong()) : slea.readInt());
        MaplePet pet = chr.getPet(petz);
        slea.skip(1);
        slea.readInt();
        Point Client_Reportedpos = slea.readPos();
        MapleMapObject ob = chr.getMap().getMapObject(slea.readInt(), MapleMapObjectType.ITEM);

        if ((ob == null) || (pet == null)) {
            return;
        }

        MapleMapItem mapitem = (MapleMapItem) ob;
        Lock lock = mapitem.getLock();
        lock.lock();
        try {
            if (mapitem.isPickedUp()) {
                c.getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
            } else {
                if ((mapitem.getOwner() != chr.getId()) && (mapitem.isPlayerDrop())) {
                    return;
                }
                if ((mapitem.getOwner() != chr.getId()) && (((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 0)) || ((mapitem.isPlayerDrop()) && (chr.getMap().getEverlast())))) {
                    c.getSession().write(CWvsContext.enableActions());
                } else if ((!mapitem.isPlayerDrop()) && (mapitem.getDropType() == 1) && (mapitem.getOwner() != chr.getId()) && ((chr.getParty() == null) || (chr.getParty().getMemberById(mapitem.getOwner()) == null))) {
                    c.getSession().write(CWvsContext.enableActions());
                } else {
                    double Distance = Client_Reportedpos.distanceSq(mapitem.getPosition());

                    if (mapitem.getMeso() > 0) {
                        if ((chr.getParty() != null) && (mapitem.getOwner() != chr.getId())) {
                            List<MapleCharacter> toGive = new LinkedList();
                            int splitMeso = mapitem.getMeso() * 40 / 100;
                            for (MaplePartyCharacter z : chr.getParty().getMembers()) {
                                MapleCharacter m = chr.getMap().getCharacterById(z.getId());
                                if ((m != null) && (m.getId() != chr.getId())) {
                                    toGive.add(m);
                                }
                            }
                            for (MapleCharacter m : toGive) {
                                m.gainMeso(splitMeso / toGive.size(), true);
                            }
                            chr.gainMeso(mapitem.getMeso() - splitMeso, true);
                        } else {
                            chr.gainMeso(mapitem.getMeso(), true);
                        }

                        removeItem_Pet(chr, mapitem, petz);
                    } else if (mapitem.getItemId() / 10000 == 291) {
                        c.getSession().write(CWvsContext.enableActions());
                    } else if (useItem(c, mapitem.getItemId())) {
                        removeItem_Pet(chr, mapitem, petz);
                    } else if (MapleInventoryManipulator.checkSpace(c, mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
                        if ((mapitem.getItem().getQuantity() >= 50) && (mapitem.getItemId() == 2340000)) {
                            c.setMonitored(true);
                        }

                        MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true, mapitem.getDropper() instanceof MapleMonster);
                        removeItem_Pet(chr, mapitem, petz);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public static final boolean useItem(MapleClient c, int id) {
        if (GameConstants.isUse(id)) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            MapleStatEffect eff = ii.getItemEffect(id);
            if (eff == null) {
                return false;
            }

            if (id / 10000 == 291) {
                boolean area = false;
                for (Rectangle rect : c.getPlayer().getMap().getAreas()) {
                    if (rect.contains(c.getPlayer().getTruePosition())) {
                        area = true;
                        break;
                    }
                }
                if ((!c.getPlayer().inPVP()) || ((c.getPlayer().getTeam() == id - 2910000) && (area))) {
                    return false;
                }
            }
            int consumeval = eff.getConsume();

            if (consumeval > 0) {
                consumeItem(c, eff);
                consumeItem(c, ii.getItemEffectEX(id));
                c.getSession().write(CWvsContext.InfoPacket.getShowItemGain(id, (short) 1));
                return true;
            }
        }
        return false;
    }

    public static final void consumeItem(MapleClient c, MapleStatEffect eff) {
        if (eff == null) {
            return;
        }
        if (eff.getConsume() == 2) {
            if ((c.getPlayer().getParty() != null) && (c.getPlayer().isAlive())) {
                for (MaplePartyCharacter pc : c.getPlayer().getParty().getMembers()) {
                    MapleCharacter chr = c.getPlayer().getMap().getCharacterById(pc.getId());
                    if ((chr != null) && (chr.isAlive())) {
                        eff.applyTo(chr);
                    }
                }
            } else {
                eff.applyTo(c.getPlayer());
            }
        } else if (c.getPlayer().isAlive()) {
            eff.applyTo(c.getPlayer());
        }
    }

    public static final void removeItem_Pet(MapleCharacter chr, MapleMapItem mapitem, int pet) {
        mapitem.setPickedUp(true);
        chr.getMap().broadcastMessage(CField.removeItemFromMap(mapitem.getObjectId(), 5, chr.getId(), pet));
        chr.getMap().removeMapObject(mapitem);
        if (mapitem.isRandDrop()) {
            chr.getMap().spawnRandDrop();
        }
    }

    public static final void removeItem(MapleCharacter chr, MapleMapItem mapitem, MapleMapObject ob) {
        mapitem.setPickedUp(true);
        chr.getMap().broadcastMessage(CField.removeItemFromMap(mapitem.getObjectId(), 2, chr.getId()), mapitem.getPosition());
        chr.getMap().removeMapObject(ob);
        if (mapitem.isRandDrop()) {
            chr.getMap().spawnRandDrop();
        }
    }

    private static final void addMedalString(MapleCharacter c, StringBuilder sb) {
        Item medal = c.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -46);
        if (medal != null) {
            sb.append("<");
            if ((medal.getItemId() == 1142257) && (GameConstants.isAdventurer(c.getJob()))) {
                MapleQuestStatus stat = c.getQuestNoAdd(MapleQuest.getInstance(111111));
                if ((stat != null) && (stat.getCustomData() != null)) {
                    sb.append(stat.getCustomData());
                    sb.append("'s Successor");
                } else {
                    sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
                }
            } else {
                sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
            }
            sb.append("> ");
        }
    }

    private static final boolean getIncubatedItems(MapleClient c, int itemId) {
        if ((c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < 2) || (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() < 2) || (c.getPlayer().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < 2)) {
            c.getPlayer().dropMessage(5, "Please make room in your inventory.");
            return false;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int id1 = RandomRewards.getPeanutReward();
        int id2 = RandomRewards.getPeanutReward();
        while (!ii.itemExists(id1)) {
            id1 = RandomRewards.getPeanutReward();
        }
        while (!ii.itemExists(id2)) {
            id2 = RandomRewards.getPeanutReward();
        }
        c.getSession().write(CWvsContext.getPeanutResult(id1, (short) 1, id2, (short) 1, itemId));
        MapleInventoryManipulator.addById(c, id1, (short) 1, new StringBuilder().append(ii.getName(itemId)).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
        MapleInventoryManipulator.addById(c, id2, (short) 1, new StringBuilder().append(ii.getName(itemId)).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
        c.getPlayer().dropMessage(1, new StringBuilder().append("You have gained ").append(id1).append(",").append(id2).toString());
        return true;
    }

    public static final void OwlMinerva(LittleEndianAccessor slea, MapleClient c) {
        byte slot = (byte) slea.readShort();
        int itemid = slea.readInt();
        Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((short) slot);
        if ((toUse != null) && (toUse.getQuantity() > 0) && (toUse.getItemId() == itemid) && (itemid == 2310000) && (!c.getPlayer().hasBlockedInventory())) {
            int itemSearch = slea.readInt();
            List hms = c.getChannelServer().searchMerchant(itemSearch);
            if (hms.size() > 0) {
                c.getSession().write(CWvsContext.getOwlSearched(itemSearch, hms));
                MapleInventoryManipulator.removeById(c, MapleInventoryType.USE, itemid, 1, true, false);
            } else {
                c.getPlayer().dropMessage(1, "Unable to find the item.");
            }
        }
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void Owl(LittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer().haveItem(5230000, 1, true, false)) || (c.getPlayer().haveItem(2310000, 1, true, false))) {
            if ((c.getPlayer().getMapId() >= 910000000) && (c.getPlayer().getMapId() <= 910000022)) {
                c.getSession().write(CWvsContext.getOwlOpen());
            } else {
                c.getPlayer().dropMessage(5, "This can only be used inside the Free Market.");
                c.getSession().write(CWvsContext.enableActions());
            }
        }
    }

    public static final void OwlWarp(LittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.getSession().write(CWvsContext.getOwlMessage(4));
            return;
        }
        if (c.getPlayer().getTrade() != null) {
            c.getSession().write(CWvsContext.getOwlMessage(7));
            return;
        }
        if ((c.getPlayer().getMapId() >= 910000000) && (c.getPlayer().getMapId() <= 910000022) && (!c.getPlayer().hasBlockedInventory())) {
            int id = slea.readInt();
            int map = slea.readInt();
            if ((map >= 910000001) && (map <= 910000022)) {
                c.getSession().write(CWvsContext.getOwlMessage(0));
                MapleMap mapp = c.getChannelServer().getMapFactory().getMap(map);
                c.getPlayer().changeMap(mapp, mapp.getPortal(0));
                HiredMerchant merchant = null;
                List<MapleMapObject> objects;
                switch (2) {
                    case 0:
                        objects = mapp.getAllHiredMerchantsThreadsafe();
                        for (MapleMapObject ob : objects) {
                            if ((ob instanceof IMaplePlayerShop)) {
                                IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                                if ((ips instanceof HiredMerchant)) {
                                    HiredMerchant merch = (HiredMerchant) ips;
                                    if (merch.getOwnerId() == id) {
                                        merchant = merch;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    case 1:
                        objects = mapp.getAllHiredMerchantsThreadsafe();
                        for (MapleMapObject ob : objects) {
                            if ((ob instanceof IMaplePlayerShop)) {
                                IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                                if ((ips instanceof HiredMerchant)) {
                                    HiredMerchant merch = (HiredMerchant) ips;
                                    if (merch.getStoreId() == id) {
                                        merchant = merch;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        MapleMapObject ob = mapp.getMapObject(id, MapleMapObjectType.HIRED_MERCHANT);
                        if ((ob instanceof IMaplePlayerShop)) {
                            IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                            if ((ips instanceof HiredMerchant)) {
                                merchant = (HiredMerchant) ips;
                            }
                        }
                        break;
                }
                if (merchant != null) {
                    if (merchant.isOwner(c.getPlayer())) {
                        merchant.setOpen(false);
                        merchant.removeAllVisitors(16, 0);
                        c.getPlayer().setPlayerShop(merchant);
                        c.getSession().write(PlayerShopPacket.getHiredMerch(c.getPlayer(), merchant, false));
                    } else if ((!merchant.isOpen()) || (!merchant.isAvailable())) {
                        c.getPlayer().dropMessage(1, "The owner of the store is currently undergoing store maintenance. Please try again in a bit.");
                    } else if (merchant.getFreeSlot() == -1) {
                        c.getPlayer().dropMessage(1, "You can't enter the room due to full capacity.");
                    } else if (merchant.isInBlackList(c.getPlayer().getName())) {
                        c.getPlayer().dropMessage(1, "You may not enter this store.");
                    } else {
                        c.getPlayer().setPlayerShop(merchant);
                        merchant.addVisitor(c.getPlayer());
                        c.getSession().write(PlayerShopPacket.getHiredMerch(c.getPlayer(), merchant, false));
                    }
                } else {
                    c.getPlayer().dropMessage(1, "The room is already closed.");
                }
            } else {
                c.getSession().write(CWvsContext.getOwlMessage(23));
            }
        } else {
            c.getSession().write(CWvsContext.getOwlMessage(23));
        }
    }

    public static final void PamSong(LittleEndianAccessor slea, MapleClient c) {
        Item pam = c.getPlayer().getInventory(MapleInventoryType.CASH).findById(5640000);
        if ((slea.readByte() > 0) && (c.getPlayer().getScrolledPosition() != 0) && (pam != null) && (pam.getQuantity() > 0)) {
            MapleInventoryType inv = c.getPlayer().getScrolledPosition() < 0 ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP;
            Item item = c.getPlayer().getInventory(inv).getItem(c.getPlayer().getScrolledPosition());
            c.getPlayer().setScrolledPosition((short) 0);
            if (item != null) {
                Equip eq = (Equip) item;
                eq.setUpgradeSlots((byte) (eq.getUpgradeSlots() + 1));
                c.getPlayer().forceReAddItem_Flag(eq, inv);
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, pam.getPosition(), (short) 1, true, false);
                c.getPlayer().getMap().broadcastMessage(CField.pamsSongEffect(c.getPlayer().getId()));
            }
        } else {
            c.getPlayer().setScrolledPosition((short) 0);
        }
    }

    public static final void TeleRock(LittleEndianAccessor slea, MapleClient c) {
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        Item toUse = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((short) slot);

        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != itemId) || (itemId / 10000 != 232) || (c.getPlayer().hasBlockedInventory())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        boolean used = UseTeleRock(slea, c, itemId);
        if (used) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false);
        }
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final boolean UseTeleRock(LittleEndianAccessor slea, MapleClient c, int itemId) {
        boolean used = false;
        if ((itemId == 5041001) || (itemId == 5040004)) {
            slea.readByte();
        }
        if (slea.readByte() == 0) {
            MapleMap target = c.getChannelServer().getMapFactory().getMap(slea.readInt());
            if (((itemId == 5041000) && (c.getPlayer().isRockMap(target.getId()))) || ((itemId != 5041000) && (c.getPlayer().isRegRockMap(target.getId()))) || (((itemId == 5040004) || (itemId == 5041001)) && ((c.getPlayer().isHyperRockMap(target.getId())) || (GameConstants.isHyperTeleMap(target.getId())))
                    && (!FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())) && (!FieldLimitType.VipRock.check(target.getFieldLimit())) && (!c.getPlayer().isInBlockedMap()))) {
                c.getPlayer().changeMap(target, target.getPortal(0));
                used = true;
            }
        } else {
            MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
            if ((victim != null) && (!victim.isIntern()) && (c.getPlayer().getEventInstance() == null) && (victim.getEventInstance() == null)
                    && (!FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())) && (!FieldLimitType.VipRock.check(c.getChannelServer().getMapFactory().getMap(victim.getMapId()).getFieldLimit())) && (!victim.isInBlockedMap()) && (!c.getPlayer().isInBlockedMap()) && ((itemId == 5041000) || (itemId == 5040004) || (itemId == 5041001) || (victim.getMapId() / 100000000 == c.getPlayer().getMapId() / 100000000))) {
                c.getPlayer().changeMap(victim.getMap(), victim.getMap().findClosestPortal(victim.getTruePosition()));
                used = true;
            }

        }

        return (used) && (itemId != 5041001) && (itemId != 5040004);
    }
}