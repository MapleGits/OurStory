package scripting;

import clientside.MapleCharacter;
import clientside.MapleClient;
import clientside.MapleQuestStatus;
import clientside.MapleTrait;
import clientside.Skill;
import clientside.SkillFactory;
import client.inventory.Equip;
import client.inventory.ItemFlag;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.World;
import handling.world.guild.MapleGuild;
import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleSquad;
import server.Randomizer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.Event_DojoAgent;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import server.maps.SavedLocationType;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.PetPacket;

public abstract class AbstractPlayerInteraction {

    protected MapleClient c;
    protected int id;
    protected int id2;

    public AbstractPlayerInteraction(MapleClient c, int id, int id2) {
        this.c = c;
        this.id = id;
        this.id2 = id2;
    }

    public final MapleClient getClient() {
        return this.c;
    }

    public final MapleClient getC() {
        return this.c;
    }

    public MapleCharacter getChar() {
        return this.c.getPlayer();
    }

    public final ChannelServer getChannelServer() {
        return this.c.getChannelServer();
    }

    public final MapleCharacter getPlayer() {
        return this.c.getPlayer();
    }

    public final EventManager getEventManager(String event) {
        return this.c.getChannelServer().getEventSM().getEventManager(event);
    }

    public final EventInstanceManager getEventInstance() {
        return this.c.getPlayer().getEventInstance();
    }

    public final void warp(int map) {
        MapleMap mapz = getWarpMap(map);
        try {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(Randomizer.nextInt(mapz.getPortals().size())));
        } catch (Exception e) {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(0));
        }
    }

    public final void warp_Instanced(int map) {
        MapleMap mapz = getMap_Instanced(map);
        try {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(Randomizer.nextInt(mapz.getPortals().size())));
        } catch (Exception e) {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(0));
        }
    }

    public final void warp(int map, int portal) {
        MapleMap mapz = getWarpMap(map);
        if ((portal != 0) && (map == this.c.getPlayer().getMapId())) {
            Point portalPos = new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition());
            if (portalPos.distanceSq(getPlayer().getTruePosition()) < 90000.0D) {
                this.c.getSession().write(CField.instantMapWarp((byte) portal));
                this.c.getPlayer().checkFollow();
                this.c.getPlayer().getMap().movePlayer(this.c.getPlayer(), portalPos);
            } else {
                this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
            }
        } else {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
        }
    }

    public final void warpS(int map, int portal) {
        MapleMap mapz = getWarpMap(map);
        this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
    }

    public final void warp(int map, String portal) {
        MapleMap mapz = getWarpMap(map);
        if ((map == 109060000) || (map == 109060002) || (map == 109060004)) {
            portal = mapz.getSnowballPortal();
        }
        if (map == this.c.getPlayer().getMapId()) {
            Point portalPos = new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition());
            if (portalPos.distanceSq(getPlayer().getTruePosition()) < 90000.0D) {
                this.c.getPlayer().checkFollow();
                this.c.getSession().write(CField.instantMapWarp((byte) this.c.getPlayer().getMap().getPortal(portal).getId()));
                this.c.getPlayer().getMap().movePlayer(this.c.getPlayer(), new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition()));
            } else {
                this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
            }
        } else {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
        }
    }

    public final void warpS(int map, String portal) {
        MapleMap mapz = getWarpMap(map);
        if ((map == 109060000) || (map == 109060002) || (map == 109060004)) {
            portal = mapz.getSnowballPortal();
        }
        this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
    }

    public final void warpMap(int mapid, int portal) {
        MapleMap map = getMap(mapid);
        for (MapleCharacter chr : this.c.getPlayer().getMap().getCharactersThreadsafe()) {
            chr.changeMap(map, map.getPortal(portal));
        }
    }

    public final void playPortalSE() {
        this.c.getSession().write(CField.EffectPacket.showOwnBuffEffect(0, 7, 1, 1));
    }

    private final MapleMap getWarpMap(int map) {
        return ChannelServer.getInstance(this.c.getChannel()).getMapFactory().getMap(map);
    }

    public final MapleMap getMap() {
        return this.c.getPlayer().getMap();
    }

    public final MapleMap getMap(int map) {
        return getWarpMap(map);
    }

    public final MapleMap getMap_Instanced(int map) {
        return this.c.getPlayer().getEventInstance() == null ? getMap(map) : this.c.getPlayer().getEventInstance().getMapInstance(map);
    }

    public void spawnMonster(int id, int qty) {
        spawnMob(id, qty, this.c.getPlayer().getTruePosition());
    }

    public final void spawnMobOnMap(int id, int qty, int x, int y, int map) {
        for (int i = 0; i < qty; i++) {
            getMap(map).spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), new Point(x, y));
        }
    }

    public final void spawnMob(int id, int qty, int x, int y) {
        spawnMob(id, qty, new Point(x, y));
    }

    public final void spawnMob(int id, int x, int y) {
        spawnMob(id, 1, new Point(x, y));
    }

    private final void spawnMob(int id, int qty, Point pos) {
        for (int i = 0; i < qty; i++) {
            this.c.getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
        }
    }

    public final void killMob(int ids) {
        this.c.getPlayer().getMap().killMonster(ids);
    }

    public final void killAllMob() {
        this.c.getPlayer().getMap().killAllMonsters(true);
    }

    public final void addHP(int delta) {
        this.c.getPlayer().addHP(delta);
    }

    public final int getPlayerStat(String type) {
        if (type.equals("LVL")) {
            return this.c.getPlayer().getLevel();
        }
        if (type.equals("STR")) {
            return this.c.getPlayer().getStat().getStr();
        }
        if (type.equals("DEX")) {
            return this.c.getPlayer().getStat().getDex();
        }
        if (type.equals("INT")) {
            return this.c.getPlayer().getStat().getInt();
        }
        if (type.equals("LUK")) {
            return this.c.getPlayer().getStat().getLuk();
        }
        if (type.equals("HP")) {
            return this.c.getPlayer().getStat().getHp();
        }
        if (type.equals("MP")) {
            return this.c.getPlayer().getStat().getMp();
        }
        if (type.equals("MAXHP")) {
            return this.c.getPlayer().getStat().getMaxHp();
        }
        if (type.equals("MAXMP")) {
            return this.c.getPlayer().getStat().getMaxMp();
        }
        if (type.equals("RAP")) {
            return this.c.getPlayer().getRemainingAp();
        }
        if (type.equals("RSP")) {
            return this.c.getPlayer().getRemainingSp();
        }
        if (type.equals("GID")) {
            return this.c.getPlayer().getGuildId();
        }
        if (type.equals("GRANK")) {
            return this.c.getPlayer().getGuildRank();
        }
        if (type.equals("ARANK")) {
            return this.c.getPlayer().getAllianceRank();
        }
        if (type.equals("GM")) {
            return this.c.getPlayer().isGM() ? 1 : 0;
        }
        if (type.equals("ADMIN")) {
            return this.c.getPlayer().isAdmin() ? 1 : 0;
        }
        if (type.equals("GENDER")) {
            return this.c.getPlayer().getGender();
        }
        if (type.equals("FACE")) {
            return this.c.getPlayer().getFace();
        }
        if (type.equals("HAIR")) {
            return this.c.getPlayer().getHair();
        }
        return -1;
    }

    public final String getName() {
        return this.c.getPlayer().getName();
    }

    public final boolean haveItem(int itemid) {
        return haveItem(itemid, 1);
    }

    public final boolean haveItem(int itemid, int quantity) {
        return haveItem(itemid, quantity, false, true);
    }

    public final boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
        return this.c.getPlayer().haveItem(itemid, quantity, checkEquipped, greaterOrEquals);
    }

    public final boolean canHold() {
        for (int i = 1; i <= 5; i++) {
            if (this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte) i)).getNextFreeSlot() <= -1) {
                return false;
            }
        }
        return true;
    }

    public final boolean canHoldSlots(int slot) {
        for (int i = 1; i <= 5; i++) {
            if (this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte) i)).isFull(slot)) {
                return false;
            }
        }
        return true;
    }

    public final boolean canHold(int itemid) {
        return this.c.getPlayer().getInventory(GameConstants.getInventoryType(itemid)).getNextFreeSlot() > -1;
    }

    public final boolean canHold(int itemid, int quantity) {
        return MapleInventoryManipulator.checkSpace(this.c, itemid, quantity, "");
    }

    public final MapleQuestStatus getQuestRecord(int id) {
        return this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(id));
    }

    public final MapleQuestStatus getQuestNoRecord(int id) {
        return this.c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(id));
    }

    public final byte getQuestStatus(int id) {
        return this.c.getPlayer().getQuestStatus(id);
    }

    public final boolean isQuestActive(int id) {
        return getQuestStatus(id) == 1;
    }

    public final boolean isQuestFinished(int id) {
        return getQuestStatus(id) == 2;
    }

    public final void showQuestMsg(String msg) {
        this.c.getSession().write(CWvsContext.showQuestMsg(msg));
    }

    public final void forceStartQuest(int id, String data) {
        MapleQuest.getInstance(id).forceStart(this.c.getPlayer(), 0, data);
    }

    public final void forceStartQuest(int id, int data, boolean filler) {
        MapleQuest.getInstance(id).forceStart(this.c.getPlayer(), 0, filler ? String.valueOf(data) : null);
    }

    public void forceStartQuest(int id) {
        MapleQuest.getInstance(id).forceStart(this.c.getPlayer(), 0, null);
    }

    public void forceCompleteQuest(int id) {
        MapleQuest.getInstance(id).forceComplete(getPlayer(), 0);
    }

    public void spawnNpc(int npcId) {
        this.c.getPlayer().getMap().spawnNpc(npcId, this.c.getPlayer().getPosition());
    }

    public final void spawnNpc(int npcId, int x, int y) {
        this.c.getPlayer().getMap().spawnNpc(npcId, new Point(x, y));
    }

    public final void spawnNpc(int npcId, Point pos) {
        this.c.getPlayer().getMap().spawnNpc(npcId, pos);
    }

    public final void removeNpc(int mapid, int npcId) {
        this.c.getChannelServer().getMapFactory().getMap(mapid).removeNpc(npcId);
    }

    public final void removeNpc(int npcId) {
        this.c.getPlayer().getMap().removeNpc(npcId);
    }

    public final void forceStartReactor(int mapid, int id) {
        MapleMap map = this.c.getChannelServer().getMapFactory().getMap(mapid);

        for (MapleMapObject remo : map.getAllReactorsThreadsafe()) {
            MapleReactor react = (MapleReactor) remo;
            if (react.getReactorId() == id) {
                react.forceStartReactor(this.c);
                break;
            }
        }
    }

    public final void destroyReactor(int mapid, int id) {
        MapleMap map = this.c.getChannelServer().getMapFactory().getMap(mapid);

        for (MapleMapObject remo : map.getAllReactorsThreadsafe()) {
            MapleReactor react = (MapleReactor) remo;
            if (react.getReactorId() == id) {
                react.hitReactor(this.c);
                break;
            }
        }
    }

    public final void hitReactor(int mapid, int id) {
        MapleMap map = this.c.getChannelServer().getMapFactory().getMap(mapid);

        for (MapleMapObject remo : map.getAllReactorsThreadsafe()) {
            MapleReactor react = (MapleReactor) remo;
            if (react.getReactorId() == id) {
                react.hitReactor(this.c);
                break;
            }
        }
    }

    public final int getJob() {
        return this.c.getPlayer().getJob();
    }

    public final void gainNX(int amount) {
        this.c.getPlayer().modifyCSPoints(4, amount, true);
    }

    public final void gainItemPeriod(int id, short quantity, int period) {
        gainItem(id, quantity, false, period, -1, "");
    }

    public final void gainItemPeriod(int id, short quantity, long period, String owner) {
        gainItem(id, quantity, false, period, -1, owner);
    }

    public final void makeitem(int id, short str, short dex, short ints, short luk, short watk, short matk) {
        makeitem(id, str, dex, ints, luk, watk, matk, (short) 1, 0L, 0, "");
    }

    public final void makeitem(int id, short str, short dex, short ints, short luk, short watk, short matk, short quantity, long period, String owner) {
        makeitem(id, str, dex, ints, luk, watk, matk, quantity, period, 0, owner);
    }

    public final void makeitem(int id, short str, short dex, short ints, short luk, short watk, short matk, short quantity, long period, int jianding, String owner) {
        makeitem(id, str, dex, ints, luk, watk, matk, quantity, period, 0, jianding, owner);
    }

    public final void makeitem(int id, short str, short dex, short ints, short luk, short watk, short matk, short quantity, long period, int block, int jianding, String owner) {
        if (quantity >= 0) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            MapleInventoryType type = GameConstants.getInventoryType(id);

            if (!MapleInventoryManipulator.checkSpace(this.c, id, quantity, "")) {
                return;
            }
            if ((type.equals(MapleInventoryType.EQUIP)) && (!GameConstants.isThrowingStar(id)) && (!GameConstants.isBullet(id))) {
                Equip item = (Equip) ii.getEquipById(id);
                if (period > 0L) {
                    item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                }
                if (str > 0) {
                    item.setStr(str);
                }
                if (dex > 0) {
                    item.setDex(dex);
                }
                if (ints > 0) {
                    item.setInt(ints);
                }
                if (luk > 0) {
                    item.setLuk(luk);
                }
                if (watk > 0) {
                    item.setWatk(watk);
                }
                if (matk > 0) {
                    item.setMatk(matk);
                }
                if (owner != null) {
                    item.setOwner(owner);
                }
                if (block > 0) {
                    item.setFlag((short) (byte) (item.getFlag() | ItemFlag.LOCK.getValue()));
                }
                if (jianding > 0) {
                    item.setPotential1((byte) -jianding);
                }
                item.setGMLog("\u811A\u672C\u83B7\u5F97 " + this.id + " (" + this.id2 + ") on " + FileoutputUtil.CurrentReadable_Time());
                String name = ii.getName(id);
                if ((id / 10000 == 114) && (name != null) && (name.length() > 0)) {
                    String msg = "\u606D\u559C\u60A8\u83B7\u5F97\u52CB\u7AE0 <" + name + ">";
                    this.c.getPlayer().dropMessage(-1, msg);
                    this.c.getPlayer().dropMessage(5, msg);
                }
                MapleInventoryManipulator.addbyItem(this.c, item.copy());
            } else {
                MapleInventoryManipulator.addById(this.c, id, quantity, owner == null ? "" : owner, null, period, "Script " + this.id + " (" + this.id2 + ") on " + FileoutputUtil.CurrentReadable_Date());
            }
        } else {
            MapleInventoryManipulator.removeById(this.c, GameConstants.getInventoryType(id), id, -quantity, true, false);
        }
        this.c.getSession().write(CWvsContext.InfoPacket.getShowItemGain(id, quantity, true));
    }

    public final void makeitem2(int id, short str, short dex, short ints, short luk, short watk, short matk, short quantity, long period, int block, int jianding, String owner) {
        if (quantity >= 0) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            MapleInventoryType type = GameConstants.getInventoryType(id);

            if (!MapleInventoryManipulator.checkSpace(this.c, id, quantity, "")) {
                return;
            }
            if ((type.equals(MapleInventoryType.EQUIP)) && (!GameConstants.isThrowingStar(id)) && (!GameConstants.isBullet(id))) {
                Equip item = (Equip) ii.getEquipById(id);
                if (period > 0L) {
                    item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                }
                if (str > 0) {
                    item.setStr((short) (item.getStr() + str));
                }
                if (dex > 0) {
                    item.setDex((short) (item.getDex() + dex));
                }
                if (ints > 0) {
                    item.setInt((short) (item.getInt() + ints));
                }
                if (luk > 0) {
                    item.setLuk((short) (item.getLuk() + luk));
                }
                if (watk > 0) {
                    item.setWatk((short) (item.getWatk() + watk));
                }
                if (matk > 0) {
                    item.setMatk((short) (item.getMatk() + matk));
                }
                if (owner != null) {
                    item.setOwner(owner);
                }
                if (block > 0) {
                    item.setFlag((short) (byte) (item.getFlag() | ItemFlag.LOCK.getValue()));
                }
                if (jianding > 0) {
                    item.setPotential1((byte) -jianding);
                }
                item.setGMLog("\u811A\u672C\u83B7\u5F97 " + this.id + " (" + this.id2 + ") on " + FileoutputUtil.CurrentReadable_Time());
                String name = ii.getName(id);
                if ((id / 10000 == 114) && (name != null) && (name.length() > 0)) {
                    String msg = "\u606D\u559C\u60A8\u83B7\u5F97\u52CB\u7AE0 <" + name + ">";
                    this.c.getPlayer().dropMessage(-1, msg);
                    this.c.getPlayer().dropMessage(5, msg);
                }
                MapleInventoryManipulator.addbyItem(this.c, item.copy());
            } else {
                MapleInventoryManipulator.addById(this.c, id, quantity, owner == null ? "" : owner, null, period, "Script " + this.id + " (" + this.id2 + ") on " + FileoutputUtil.CurrentReadable_Date());
            }
        } else {
            MapleInventoryManipulator.removeById(this.c, GameConstants.getInventoryType(id), id, -quantity, true, false);
        }
        this.c.getSession().write(CWvsContext.InfoPacket.getShowItemGain(id, quantity, true));
    }

    public final void makeitems(int id, short str, short dex, short ints, short luk, short watk, short matk, long period, int jianding, String owner) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = GameConstants.getInventoryType(id);

        if (!MapleInventoryManipulator.checkSpace(this.c, id, 1, "")) {
            return;
        }
        if ((type.equals(MapleInventoryType.EQUIP)) && (!GameConstants.isThrowingStar(id)) && (!GameConstants.isBullet(id))) {
            Equip item = (Equip) ii.getEquipById(id);
            if (period > 0L) {
                item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
            }
            item.setStr(str);
            item.setDex(dex);
            item.setInt(ints);
            item.setLuk(luk);
            item.setWatk(watk);
            item.setMatk(matk);
            if (owner != null) {
                item.setOwner(owner);
            }
            if (jianding > 0) {
                item.setPotential1((byte) -jianding);
            }
            item.setGMLog("\u811A\u672C\u83B7\u5F97 " + this.id + " (" + this.id2 + ") on " + FileoutputUtil.CurrentReadable_Time());
            String name = ii.getName(id);
            if ((id / 10000 == 114) && (name != null) && (name.length() > 0)) {
                String msg = "\u606D\u559C\u60A8\u83B7\u5F97\u52CB\u7AE0 <" + name + ">";
                this.c.getPlayer().dropMessage(-1, msg);
                this.c.getPlayer().dropMessage(5, msg);
            }
            MapleInventoryManipulator.addbyItem(this.c, item.copy());
        } else {
            MapleInventoryManipulator.addById(this.c, id, (short) 1, owner == null ? "" : owner, null, period, "Script " + this.id + " (" + this.id2 + ") on " + FileoutputUtil.CurrentReadable_Date());
        }
        this.c.getSession().write(CWvsContext.InfoPacket.getShowItemGain(id, (short) 1, true));
    }

    public final void gainItem(int id, short quantity) {
        gainItem(id, quantity, false, 0L, -1, "");
    }

    public final void gainItemSilent(int id, short quantity) {
        gainItem(id, quantity, false, 0L, -1, "", this.c, false);
    }

    public final void gainItem(int id, short quantity, boolean randomStats) {
        gainItem(id, quantity, randomStats, 0L, -1, "");
    }

    public final void gainItem(int id, short quantity, boolean randomStats, int slots) {
        gainItem(id, quantity, randomStats, 0L, slots, "");
    }

    public final void gainItem(int id, short quantity, long period) {
        gainItem(id, quantity, false, period, -1, "");
    }

    public final void gainItem(int id, short quantity, boolean randomStats, long period, int slots) {
        gainItem(id, quantity, randomStats, period, slots, "");
    }

    public final void gainItem(int id, short quantity, boolean randomStats, long period, int slots, String owner) {
        gainItem(id, quantity, randomStats, period, slots, owner, this.c);
    }

    public final void gainItem(int id, short quantity, boolean randomStats, long period, int slots, String owner, MapleClient cg) {
        gainItem(id, quantity, randomStats, period, slots, owner, cg, true);
    }

    public final void gainItem(int id, short quantity, boolean randomStats, long period, int slots, String owner, MapleClient cg, boolean show) {
        if (quantity >= 0) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            MapleInventoryType type = GameConstants.getInventoryType(id);

            if (!MapleInventoryManipulator.checkSpace(cg, id, quantity, "")) {
                return;
            }
            if ((type.equals(MapleInventoryType.EQUIP)) && (!GameConstants.isThrowingStar(id)) && (!GameConstants.isBullet(id))) {
                Equip item = (Equip) (randomStats ? ii.randomizeStats((Equip) ii.getEquipById(id)) : ii.getEquipById(id));
                if (period > 0L) {
                    item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                }
                if (slots > 0) {
                    item.setUpgradeSlots((byte) (item.getUpgradeSlots() + slots));
                }
                if (owner != null) {
                    item.setOwner(owner);
                }
                item.setGMLog("Received from interaction " + this.id + " (" + this.id2 + ") on " + FileoutputUtil.CurrentReadable_Time());
                String name = ii.getName(id);
                if ((id / 10000 == 114) && (name != null) && (name.length() > 0)) {
                    String msg = "You have attained title <" + name + ">";
                    cg.getPlayer().dropMessage(-1, msg);
                    cg.getPlayer().dropMessage(5, msg);
                }
                MapleInventoryManipulator.addbyItem(cg, item.copy());
            } else {
                MapleInventoryManipulator.addById(cg, id, quantity, owner == null ? "" : owner, null, period, "Received from interaction " + this.id + " (" + this.id2 + ") on " + FileoutputUtil.CurrentReadable_Date());
            }
        } else {
            MapleInventoryManipulator.removeById(cg, GameConstants.getInventoryType(id), id, -quantity, true, false);
        }
        if (show) {
            cg.getSession().write(CWvsContext.InfoPacket.getShowItemGain(id, quantity, true));
        }
    }

    public final boolean removeItem(int id) {
        if (MapleInventoryManipulator.removeById_Lock(this.c, GameConstants.getInventoryType(id), id)) {
            this.c.getSession().write(CWvsContext.InfoPacket.getShowItemGain(id, (short) -1, true));
            return true;
        }
        return false;
    }

    public final void changeMusic(String songName) {
        getPlayer().getMap().broadcastMessage(CField.musicChange(songName));
    }

    public final void worldMessage(int type, String message) {
        World.Broadcast.broadcastMessage(CWvsContext.serverNotice(type, message));
    }

    public final void playerMessage(String message) {
        playerMessage(5, message);
    }

    public final void mapMessage(String message) {
        mapMessage(5, message);
    }

    public final void guildMessage(String message) {
        guildMessage(5, message);
    }

    public final void playerMessage(int type, String message) {
        this.c.getPlayer().dropMessage(type, message);
    }

    public final void mapMessage(int type, String message) {
        this.c.getPlayer().getMap().broadcastMessage(CWvsContext.serverNotice(type, message));
    }

    public final void guildMessage(int type, String message) {
        if (getPlayer().getGuildId() > 0) {
            World.Guild.guildPacket(getPlayer().getGuildId(), CWvsContext.serverNotice(type, message));
        }
    }

    public final MapleGuild getGuild() {
        return getGuild(getPlayer().getGuildId());
    }

    public final MapleGuild getGuild(int guildid) {
        return World.Guild.getGuild(guildid);
    }

    public final MapleParty getParty() {
        return this.c.getPlayer().getParty();
    }

    public final int getCurrentPartyId(int mapid) {
        return getMap(mapid).getCurrentPartyId();
    }

    public final boolean isLeader() {
        if (getPlayer().getParty() == null) {
            return false;
        }
        return getParty().getLeader().getId() == this.c.getPlayer().getId();
    }

    public final boolean isAllPartyMembersAllowedJob(int job) {
        if (this.c.getPlayer().getParty() == null) {
            return false;
        }
        for (MaplePartyCharacter mem : this.c.getPlayer().getParty().getMembers()) {
            if (mem.getJobId() / 100 != job) {
                return false;
            }
        }
        return true;
    }

    public final boolean allMembersHere() {
        if (this.c.getPlayer().getParty() == null) {
            return false;
        }
        for (MaplePartyCharacter mem : this.c.getPlayer().getParty().getMembers()) {
            MapleCharacter chr = this.c.getPlayer().getMap().getCharacterById(mem.getId());
            if (chr == null) {
                return false;
            }
        }
        return true;
    }

    public final void warpParty(int mapId) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            warp(mapId, 0);
            return;
        }
        MapleMap target = getMap(mapId);
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                curChar.changeMap(target, target.getPortal(0));
            }
        }
    }

    public final void warpParty(int mapId, int portal) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            if (portal < 0) {
                warp(mapId);
            } else {
                warp(mapId, portal);
            }
            return;
        }
        boolean rand = portal < 0;
        MapleMap target = getMap(mapId);
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                if (rand) {
                    try {
                        curChar.changeMap(target, target.getPortal(Randomizer.nextInt(target.getPortals().size())));
                    } catch (Exception e) {
                        curChar.changeMap(target, target.getPortal(0));
                    }
                } else {
                    curChar.changeMap(target, target.getPortal(portal));
                }
            }
        }
    }

    public final void warpParty_Instanced(int mapId) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            warp_Instanced(mapId);
            return;
        }
        MapleMap target = getMap_Instanced(mapId);

        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                curChar.changeMap(target, target.getPortal(0));
            }
        }
    }

    public void gainMeso(int gain) {
        this.c.getPlayer().gainMeso(gain, true, true);
    }

    public void gainExp(int gain) {
        this.c.getPlayer().gainExp(gain, true, true, true);
    }

    public void gainExpR(int gain) {
        this.c.getPlayer().gainExp(gain * this.c.getChannelServer().getExpRate(), true, true, true);
    }

    public final void givePartyItems(int id, short quantity, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            if (quantity >= 0) {
                MapleInventoryManipulator.addById(chr.getClient(), id, quantity, "Received from party interaction " + id + " (" + this.id2 + ")");
            } else {
                MapleInventoryManipulator.removeById(chr.getClient(), GameConstants.getInventoryType(id), id, -quantity, true, false);
            }
            chr.getClient().getSession().write(CWvsContext.InfoPacket.getShowItemGain(id, quantity, true));
        }
    }

    public void addPartyTrait(String t, int e, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            chr.getTrait(MapleTrait.MapleTraitType.valueOf(t)).addExp(e, chr);
        }
    }

    public void addPartyTrait(String t, int e) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            addTrait(t, e);
            return;
        }
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                curChar.getTrait(MapleTrait.MapleTraitType.valueOf(t)).addExp(e, curChar);
            }
        }
    }

    public void addTrait(String t, int e) {
        getPlayer().getTrait(MapleTrait.MapleTraitType.valueOf(t)).addExp(e, getPlayer());
    }

    public final void givePartyItems(int id, short quantity) {
        givePartyItems(id, quantity, false);
    }

    public final void givePartyItems(int id, short quantity, boolean removeAll) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            gainItem(id, (short) (removeAll ? -getPlayer().itemQuantity(id) : quantity));
            return;
        }

        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                gainItem(id, (short) (removeAll ? -curChar.itemQuantity(id) : quantity), false, 0L, 0, "", curChar.getClient());
            }
        }
    }

    public final void givePartyExp_PQ(int maxLevel, double mod, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            int amount = (int) Math.round(GameConstants.getExpNeededForLevel(chr.getLevel() > maxLevel ? maxLevel + (maxLevel - chr.getLevel()) / 10 : chr.getLevel()).longValue() / (Math.min(chr.getLevel(), maxLevel) / 5.0D) / (mod * 2.0D));
            chr.gainExp(amount * this.c.getChannelServer().getExpRate(), true, true, true);
        }
    }

    public final void gainExp_PQ(int maxLevel, double mod) {
        int amount = (int) Math.round(GameConstants.getExpNeededForLevel(getPlayer().getLevel() > maxLevel ? maxLevel + getPlayer().getLevel() / 10 : getPlayer().getLevel()).longValue() / (Math.min(getPlayer().getLevel(), maxLevel) / 10.0D) / mod);
        gainExp(amount * this.c.getChannelServer().getExpRate());
    }

    public final void givePartyExp_PQ(int maxLevel, double mod) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            int amount = (int) Math.round(GameConstants.getExpNeededForLevel(getPlayer().getLevel() > maxLevel ? maxLevel + getPlayer().getLevel() / 10 : getPlayer().getLevel()).longValue() / (Math.min(getPlayer().getLevel(), maxLevel) / 10.0D) / mod);
            gainExp(amount * this.c.getChannelServer().getExpRate());
            return;
        }
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                int amount = (int) Math.round(GameConstants.getExpNeededForLevel(curChar.getLevel() > maxLevel ? maxLevel + curChar.getLevel() / 10 : curChar.getLevel()).longValue() / (Math.min(curChar.getLevel(), maxLevel) / 10.0D) / mod);
                curChar.gainExp(amount * this.c.getChannelServer().getExpRate(), true, true, true);
            }
        }
    }

    public final void givePartyExp(int amount, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            chr.gainExp(amount * this.c.getChannelServer().getExpRate(), true, true, true);
        }
    }

    public final void givePartyExp(int amount) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            gainExp(amount * this.c.getChannelServer().getExpRate());
            return;
        }
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                curChar.gainExp(amount * this.c.getChannelServer().getExpRate(), true, true, true);
            }
        }
    }

    public final void endPartyQuest(int amount, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            chr.endPartyQuest(amount);
        }
    }

    public final void endPartyQuest(int amount) {
        if ((getPlayer().getParty() == null) || (getPlayer().getParty().getMembers().size() == 1)) {
            getPlayer().endPartyQuest(amount);
            return;
        }
        int cMap = getPlayer().getMapId();
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if ((curChar != null) && ((curChar.getMapId() == cMap) || (curChar.getEventInstance() == getPlayer().getEventInstance()))) {
                curChar.endPartyQuest(amount);
            }
        }
    }

    public final void removeFromParty(int id, List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            int possesed = chr.getInventory(GameConstants.getInventoryType(id)).countById(id);
            if (possesed > 0) {
                MapleInventoryManipulator.removeById(this.c, GameConstants.getInventoryType(id), id, possesed, true, false);
                chr.getClient().getSession().write(CWvsContext.InfoPacket.getShowItemGain(id, (short) -possesed, true));
            }
        }
    }

    public final void removeFromParty(int id) {
        givePartyItems(id, (short) 0, true);
    }

    public final void useSkill(int skill, int level) {
        if (level <= 0) {
            return;
        }
        SkillFactory.getSkill(skill).getEffect(level).applyTo(this.c.getPlayer());
    }

    public final void useItem(int id) {
        MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(this.c.getPlayer());
        this.c.getSession().write(CWvsContext.InfoPacket.getStatusMsg(id));
    }

    public final void cancelItem(int id) {
        this.c.getPlayer().cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(id), false, -1L);
    }

    public final int getMorphState() {
        return this.c.getPlayer().getMorphState();
    }

    public final void removeAll(int id) {
        this.c.getPlayer().removeAll(id);
    }

    public final void gainCloseness(int closeness, int index) {
        MaplePet pet = getPlayer().getPet(index);
        if (pet != null) {
            pet.setCloseness(pet.getCloseness() + closeness * getChannelServer().getTraitRate());
            getClient().getSession().write(PetPacket.updatePet(pet, getPlayer().getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), true));
        }
    }

    public final void gainClosenessAll(int closeness) {
        for (MaplePet pet : getPlayer().getPets()) {
            if ((pet != null) && (pet.getSummoned())) {
                pet.setCloseness(pet.getCloseness() + closeness);
                getClient().getSession().write(PetPacket.updatePet(pet, getPlayer().getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), true));
            }
        }
    }

    public final void resetMap(int mapid) {
        getMap(mapid).resetFully();
    }

    public final void openNpc(int id) {
        getClient().removeClickedNPC();
        NPCScriptManager.getInstance().start(getClient(), id);
    }

    public final void openNpc(MapleClient cg, int id) {
        cg.removeClickedNPC();
        NPCScriptManager.getInstance().start(cg, id);
    }

    public void openNpc(int id, int npcMode) {
        getClient().removeClickedNPC();
        NPCScriptManager.getInstance().start(getClient(), id);
    }

    public final int getMapId() {
        return this.c.getPlayer().getMap().getId();
    }

    public final boolean haveMonster(int mobid) {
        for (MapleMapObject obj : this.c.getPlayer().getMap().getAllMonstersThreadsafe()) {
            MapleMonster mob = (MapleMonster) obj;
            if (mob.getId() == mobid) {
                return true;
            }
        }
        return false;
    }

    public final int getChannelNumber() {
        return this.c.getChannel();
    }

    public final int getMonsterCount(int mapid) {
        return this.c.getChannelServer().getMapFactory().getMap(mapid).getNumMonsters();
    }

    public final void teachSkill(int id, int level, byte masterlevel) {
        getPlayer().changeSingleSkillLevel(SkillFactory.getSkill(id), level, masterlevel);
    }

    public final void teachSkill(int id, int level) {
        Skill skil = SkillFactory.getSkill(id);
        if (getPlayer().getSkillLevel(skil) > level) {
            level = getPlayer().getSkillLevel(skil);
        }
        getPlayer().changeSingleSkillLevel(skil, level, (byte) skil.getMaxLevel());
    }

    public final int getPlayerCount(int mapid) {
        return this.c.getChannelServer().getMapFactory().getMap(mapid).getCharactersSize();
    }

    public final void dojo_getUp() {
        this.c.getSession().write(CWvsContext.InfoPacket.updateInfoQuest(1207, "pt=1;min=4;belt=1;tuto=1"));
        this.c.getSession().write(CField.EffectPacket.Mulung_DojoUp2());
        this.c.getSession().write(CField.instantMapWarp((byte) 6));
    }

    public final boolean dojoAgent_NextMap(boolean dojo, boolean fromresting) {
        if (dojo) {
            return Event_DojoAgent.warpNextMap(this.c.getPlayer(), fromresting, this.c.getPlayer().getMap());
        }
        return Event_DojoAgent.warpNextMap_Agent(this.c.getPlayer(), fromresting);
    }

    public final boolean dojoAgent_NextMap(boolean dojo, boolean fromresting, int mapid, int mode) {
        if (dojo) {
            return Event_DojoAgent.warpNextMap(this.c.getPlayer(), fromresting, getMap(mapid));
        }
        return Event_DojoAgent.warpNextMap_Agent(this.c.getPlayer(), fromresting);
    }

    public final int dojo_getPts() {
        return this.c.getPlayer().getIntNoRecord(150100);
    }


    public final int getSavedLocation(String loc) {
        Integer ret = Integer.valueOf(this.c.getPlayer().getSavedLocation(SavedLocationType.fromString(loc)));
        if ((ret == null) || (ret.intValue() == -1)) {
            return 100000000;
        }
        return ret.intValue();
    }

    public final void saveLocation(String loc) {
        this.c.getPlayer().saveLocation(SavedLocationType.fromString(loc));
    }

    public final void saveReturnLocation(String loc) {
        this.c.getPlayer().saveLocation(SavedLocationType.fromString(loc), this.c.getPlayer().getMap().getReturnMap().getId());
    }

    public final void clearSavedLocation(String loc) {
        this.c.getPlayer().clearSavedLocation(SavedLocationType.fromString(loc));
    }

    public final void summonMsg(String msg) {
        if (!this.c.getPlayer().hasSummon()) {
            playerSummonHint(true);
        }
        this.c.getSession().write(CField.UIPacket.summonMessage(msg));
    }

    public final void summonMsg(int type) {
        if (!this.c.getPlayer().hasSummon()) {
            playerSummonHint(true);
        }
        this.c.getSession().write(CField.UIPacket.summonMessage(type));
    }

    public final void showInstruction(String msg, int width, int height) {
        this.c.getSession().write(CField.sendHint(msg, width, height));
    }

    public final void playerSummonHint(boolean summon) {
        this.c.getPlayer().setHasSummon(summon);
        this.c.getSession().write(CField.UIPacket.summonHelper(summon));
    }

    public final String getInfoQuest(int id) {
        return this.c.getPlayer().getInfoQuest(id);
    }

    public final void updateInfoQuest(int id, String data) {
        this.c.getPlayer().updateInfoQuest(id, data);
    }

    public final boolean getEvanIntroState(String data) {
        return getInfoQuest(22013).equals(data);
    }

    public final void updateEvanIntroState(String data) {
        updateInfoQuest(22013, data);
    }

    public final void Aran_Start() {
        this.c.getSession().write(CField.Aran_Start());
    }

    public final void evanTutorial(String data, int v1) {
        this.c.getSession().write(CField.NPCPacket.getEvanTutorial(data));
    }

    public final void AranTutInstructionalBubble(String data) {
        this.c.getSession().write(CField.EffectPacket.AranTutInstructionalBalloon(data));
    }

    public final void ShowWZEffect(String data) {
        this.c.getSession().write(CField.EffectPacket.AranTutInstructionalBalloon(data));
    }

    public final void showWZEffect(String data) {
        this.c.getSession().write(CField.EffectPacket.ShowWZEffect(data));
    }

    public final void EarnTitleMsg(String data) {
        this.c.getSession().write(CWvsContext.getTopMsg(data));
    }

    public final void EnableUI(short i) {
        this.c.getSession().write(CField.UIPacket.IntroEnableUI(i));
    }

    public final void DisableUI(boolean enabled) {
        this.c.getSession().write(CField.UIPacket.IntroDisableUI(enabled));
    }

    public final void MovieClipIntroUI(boolean enabled) {
        this.c.getSession().write(CField.UIPacket.IntroDisableUI(enabled));
        this.c.getSession().write(CField.UIPacket.IntroLock(enabled));
    }

    public MapleInventoryType getInvType(int i) {
        return MapleInventoryType.getByType((byte) i);
    }

    public String getItemName(int id) {
        return MapleItemInformationProvider.getInstance().getName(id);
    }

    public void gainPet(int id, String name, int level, int closeness, int fullness, long period, short flags) {
        if ((id > 5000400) || (id < 5000000)) {
            id = 5000000;
        }
        if (level > 30) {
            level = 30;
        }
        if (closeness > 30000) {
            closeness = 30000;
        }
        if (fullness > 100) {
            fullness = 100;
        }
        try {
            MapleInventoryManipulator.addById(this.c, id, (short) 1, "", MaplePet.createPet(id, name, level, closeness, fullness, MapleInventoryIdentifier.getInstance(), id == 5000054 ? (int) period : 0, flags), 45L, "Pet from interaction " + id + " (" + this.id2 + ")" + " on " + FileoutputUtil.CurrentReadable_Date());
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void removeSlot(int invType, byte slot, short quantity) {
        MapleInventoryManipulator.removeFromSlot(this.c, getInvType(invType), (short) slot, quantity, true);
    }

    public void gainGP(int gp) {
        if (getPlayer().getGuildId() <= 0) {
            return;
        }
        World.Guild.gainGP(getPlayer().getGuildId(), gp);
    }

    public int getGP() {
        if (getPlayer().getGuildId() <= 0) {
            return 0;
        }
        return World.Guild.getGP(getPlayer().getGuildId());
    }

    public void showMapEffect(String path) {
        getClient().getSession().write(CField.MapEff(path));
    }

    public int itemQuantity(int itemid) {
        return getPlayer().itemQuantity(itemid);
    }

    public EventInstanceManager getDisconnected(String event) {
        EventManager em = getEventManager(event);
        if (em == null) {
            return null;
        }
        for (EventInstanceManager eim : em.getInstances()) {
            if ((eim.isDisconnected(this.c.getPlayer())) && (eim.getPlayerCount() > 0)) {
                return eim;
            }
        }
        return null;
    }

    public boolean isAllReactorState(int reactorId, int state) {
        boolean ret = false;
        for (MapleReactor r : getMap().getAllReactorsThreadsafe()) {
            if (r.getReactorId() == reactorId) {
                ret = r.getState() == state;
            }
        }
        return ret;
    }

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public void spawnMonster(int id) {
        spawnMonster(id, 1, getPlayer().getTruePosition());
    }

    public void spawnMonster(int id, int x, int y) {
        spawnMonster(id, 1, new Point(x, y));
    }

    public void spawnMonster(int id, int qty, int x, int y) {
        spawnMonster(id, qty, new Point(x, y));
    }

    public void spawnMonster(int id, int qty, Point pos) {
        for (int i = 0; i < qty; i++) {
            getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
        }
    }

    public void sendNPCText(String text, int npc) {
        getMap().broadcastMessage(CField.NPCPacket.getNPCTalk(npc, (byte) 0, text, "00 00", (byte) 0));
    }

    public boolean getTempFlag(int flag) {
        return (this.c.getChannelServer().getTempFlag() & flag) == flag;
    }

    public void logPQ(String text) {
    }

    public void outputFileError(Throwable t) {
        FileoutputUtil.outputFileError("Log_Script_Except.txt", t);
    }

    public void trembleEffect(int type, int delay) {
        this.c.getSession().write(CField.trembleEffect(type, delay));
    }

    public int nextInt(int arg0) {
        return Randomizer.nextInt(arg0);
    }

    public MapleQuest getQuest(int arg0) {
        return MapleQuest.getInstance(arg0);
    }

    public void achievement(int a) {
        this.c.getPlayer().getMap().broadcastMessage(CField.achievementRatio(a));
    }

    public final MapleInventory getInventory(int type) {
        return this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte) type));
    }

    public final void prepareAswanMob(int mapid, EventManager eim) {
        MapleMap map = eim.getMapFactory().getMap(mapid);
        if (this.c.getPlayer().getParty() != null) {
            map.setChangeableMobOrigin(ChannelServer.getInstance(this.c.getChannel()).getPlayerStorage().getCharacterById(this.c.getPlayer().getParty().getLeader().getId()));
        } else {
            map.setChangeableMobOrigin(this.c.getPlayer());
        }

        map.killAllMonsters(false);
        map.respawn(true);
    }

    public boolean isGMS() {
        return GameConstants.GMS;
    }

    public int randInt(int arg0) {
        return Randomizer.nextInt(arg0);
    }

    public void sendDirectionStatus(int key, int value) {
        this.c.getSession().write(CField.UIPacket.getDirectionInfo(key, value));
        this.c.getSession().write(CField.UIPacket.getDirectionStatus(true));
    }

    public void sendDirectionInfo(String data) {
        this.c.getSession().write(CField.UIPacket.getDirectionInfo(data, 2000, 0, -100, 0));
        this.c.getSession().write(CField.UIPacket.getDirectionInfo(1, 2000));
    }

    public String getbosslogstats(String bossid) {
        if (this.c.getPlayer().getBossLog(bossid) > 0) {
            return "#gok#b";
        }
        return "#rfail#b";
    }

    public int getbosslog(String bossid) {
        return this.c.getPlayer().getBossLog(bossid);
    }

    public int getbosslog(String bossid, int days) {
        return this.c.getPlayer().getBossLog(bossid, days);
    }

    public void setbosslog(String bossid) {
        this.c.getPlayer().setBossLog(bossid);
    }

    public void setpartybosslog(String bossid) {
        if (getPlayer().getParty() == null) {
            this.c.getPlayer().setBossLog(bossid);
            return;
        }
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = this.c.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (((chr.isOnline()) && (curChar.getEventInstance() == null) && (getPlayer().getEventInstance() == null)) || (curChar.getEventInstance() == getPlayer().getEventInstance())) {
                chr.setBossLog(bossid);
            }
        }
    }

    public int getpartybosslog(String bossid) {
        if (getPlayer().getParty() == null) {
            return this.c.getPlayer().getBossLog(bossid);
        }
        int rets = 0;
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            rets += chr.getBossLog(bossid);
        }
        return rets;
    }

    public int getpartybosslog(String bossid, int slot) {
        if (getPlayer().getParty() == null) {
            return this.c.getPlayer().getBossLog(bossid);
        }
        int rets = 0;
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            if (chr.getBossLog(bossid) < slot) {
                rets++;
            }
        }
        return rets;
    }

    public void setsquadbosslog(String squadtype, String bossid) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(squadtype);
        if (squad == null) {
            this.c.getPlayer().setBossLog(bossid);
            return;
        }
        for (String chrname : squad.getMembers()) {
            MapleCharacter curChar = this.c.getChannelServer().getPlayerStorage().getCharacterByName(chrname);
            if (((curChar.isAlive()) && (curChar.getEventInstance() == null) && (getPlayer().getEventInstance() == null)) || (curChar.getEventInstance() == getPlayer().getEventInstance())) {
                curChar.setBossLog(bossid);
            }
        }
    }

    public int getsquadbosslog(String squadtype, String bossid) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(squadtype);
        if (squad == null) {
            return this.c.getPlayer().getBossLog(bossid);
        }
        int rets = 0;
        for (String chrname : squad.getMembers()) {
            MapleCharacter curChar = this.c.getChannelServer().getPlayerStorage().getCharacterByName(chrname);
            if (((curChar.isAlive()) && (curChar.getEventInstance() == null) && (getPlayer().getEventInstance() == null)) || (curChar.getEventInstance() == getPlayer().getEventInstance())) {
                rets += curChar.getBossLog(bossid);
            }
        }
        return rets;
    }

    public int getfsblog(String boss) {
        return this.c.getPlayer().getfsbLog(boss);
    }

    public void delbosslog(String boss) {
        this.c.getPlayer().delBossLog(boss);
    }

    public void showDoJangRank() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT `name`, `dojo` FROM characters where dojo>0 ORDER BY `dojo` ASC LIMIT 50");
            ResultSet rs = ps.executeQuery();
            this.c.getSession().write(CField.showDoJangRank(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
        }
    }

    public int getmoney() {
        return getPlayer().getmoney();
    }

    public void setmoney(int slot, String log) {
        getPlayer().setmoney(slot);
        getPlayer().setmoneylog(slot, log);
    }

    public void laba(int type, String message) {
        dropmessage(type, message);
    }

    public void dropmessage(int type, String message) {
        if (type == -1) {
            World.Broadcast.broadcastMessage(CWvsContext.getTopMsg(message));
        } else if (type != -2) {
            if (type == -3) {
                World.Broadcast.broadcastSmega(CWvsContext.itemMegaphone("[notice] : " + message, false, this.c.getChannel(), null));
            } else if (type == -4) {
                World.Broadcast.broadcastSmega(CWvsContext.serverNotice(18, this.c.getChannel(), "[notice] : " + message, false));
            } else if (type == -5) {
                World.Broadcast.broadcastSmega(CWvsContext.serverNotice(18, this.c.getChannel(), "[notice] : " + message, false));
            } else if (type == -6) {
                World.Broadcast.broadcastMessage(CWvsContext.yellowChat(message));
            } else {
                World.Broadcast.broadcastMessage(CWvsContext.serverNotice(type, message));
            }
        }
    }
}