package server.maps;

import clientside.MapleBuffStat;
import clientside.MapleCharacter;
import clientside.MapleClient;
import clientside.MonsterFamiliar;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.TutorialConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.PartyOperation;
import handling.world.World;
import handling.world.exped.ExpeditionType;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import scripting.EventManager;
import server.MapleCarnivalFactory;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePVP;
import server.MaplePortal;
import server.MapleSquad;
import server.MapleSquad.MapleSquadType;
import server.MapleStatEffect;
import server.Randomizer;
import server.SpeedRunner;
import server.Timer;
import server.Timer.MapTimer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.life.MonsterDropEntry;
import server.life.MonsterGlobalDropEntry;
import server.life.SpawnPoint;
import server.life.SpawnPointAreaBoss;
import server.life.Spawns;
import static server.quest.MapleQuestRequirementType.mob;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;
import tools.packet.CField;
import tools.packet.CField.SummonPacket;
import tools.packet.CWvsContext;
import tools.packet.MobPacket;
import tools.packet.PetPacket;

public final class MapleMap {

    private final Map<MapleMapObjectType, LinkedHashMap<Integer, MapleMapObject>> mapobjects;
    private final Map<MapleMapObjectType, ReentrantReadWriteLock> mapobjectlocks;
    private final List<MapleCharacter> characters = new ArrayList();
    private final ReentrantReadWriteLock charactersLock = new ReentrantReadWriteLock();
    private int runningOid = 500000;
    private final Lock runningOidLock = new ReentrantLock();
    private final List<Spawns> monsterSpawn = new ArrayList();
    private final AtomicInteger spawnedMonstersOnMap = new AtomicInteger(0);
    private final Map<Integer, MaplePortal> portals = new HashMap();
    private MapleFootholdTree footholds = null;
    private float monsterRate;
    private float recoveryRate;
    private MapleMapEffect mapEffect;
    private WeakReference<MapleCharacter> changeMobOrigin = null;
    private byte channel;
    protected MapleClient c;
    private short decHP = 0;
    private short createMobInterval = 9000;
    private short top = 0;
    private short bottom = 0;
    private short left = 0;
    private short right = 0;
    private int consumeItemCoolTime = 0;
    private int protectItem = 0;
    private int decHPInterval = 10000;
    private int mapid;
    private int returnMapId;
    private int timeLimit;
    private boolean muted;
    private int fieldLimit;
    private int maxRegularSpawn = 0;
    private int fixedMob;
    private int forcedReturnMap = 999999999;
    private int instanceid = -1;
    private int lvForceMove = 0;
    private int lvLimit = 0;
    private int permanentWeather = 0;
    private int partyBonusRate = 0;
    private boolean town;
    private boolean clock;
    private boolean personalShop;
    private boolean everlast = false;
    private boolean dropsDisabled = false;
    private boolean gDropsDisabled = false;
    private boolean soaring = false;
    private boolean squadTimer = false;
    private boolean isSpawns = true;
    private boolean checkStates = true;
    private String mapName;
    private String streetName;
    private String onUserEnter;
    private String onFirstUserEnter;
    private String speedRunLeader = "";
    private List<Integer> dced = new ArrayList();
    private ScheduledFuture<?> squadSchedule;
    private long speedRunStart = 0L;
    private long lastSpawnTime = 0L;
    private long lastHurtTime = 0L;
    private MapleNodes nodes;
    private MapleSquad.MapleSquadType squad;
    private Map<String, Integer> environment = new LinkedHashMap();

    public MapleMap(int mapid, int channel, int returnMapId, float monsterRate) {
        this.mapid = mapid;
        this.channel = ((byte) channel);
        this.returnMapId = returnMapId;
        if (this.returnMapId == 999999999) {
            this.returnMapId = mapid;
        }
        if (GameConstants.getPartyPlay(mapid) > 0) {
            this.monsterRate = ((monsterRate - 1.0F) * 2.5F + 1.0F);
        } else {
            this.monsterRate = monsterRate;
        }
        EnumMap objsMap = new EnumMap(MapleMapObjectType.class);
        EnumMap objlockmap = new EnumMap(MapleMapObjectType.class);
        for (MapleMapObjectType type : MapleMapObjectType.values()) {
            objsMap.put(type, new LinkedHashMap());
            objlockmap.put(type, new ReentrantReadWriteLock());
        }
        this.mapobjects = Collections.unmodifiableMap(objsMap);
        this.mapobjectlocks = Collections.unmodifiableMap(objlockmap);
    }

    public Collection<MapleCharacter> getNearestPvpChar(Point attacker, double maxRange, double maxHeight, Collection<MapleCharacter> chr) {
        Collection<MapleCharacter> character = new LinkedList<MapleCharacter>();
        for (MapleCharacter a : characters) {
            if (chr.contains(a.getClient().getPlayer())) {
                Point attackedPlayer = a.getPosition();
                MaplePortal Port = a.getMap().findClosestSpawnpoint(a.getPosition());
                Point nearestPort = Port.getPosition();
                double safeDis = attackedPlayer.distance(nearestPort);
                double distanceX = attacker.distance(attackedPlayer.getX(), attackedPlayer.getY());
                if (MaplePVP.isLeft) {
                    if (attacker.x > attackedPlayer.x && distanceX < maxRange && distanceX > 2
                            && attackedPlayer.y >= attacker.y - maxHeight && attackedPlayer.y <= attacker.y + maxHeight && safeDis > 2) {
                        character.add(a);
                    }
                }
                if (MaplePVP.isRight) {
                    if (attacker.x < attackedPlayer.x && distanceX < maxRange && distanceX > 2
                            && attackedPlayer.y >= attacker.y - maxHeight && attackedPlayer.y <= attacker.y + maxHeight && safeDis > 2) {
                        character.add(a);
                    }
                }
            }
        }
        return character;
    }

    public final void setSpawns(boolean fm) {
        this.isSpawns = fm;
    }

    public final boolean getSpawns() {
        return this.isSpawns;
    }

    public final void setFixedMob(int fm) {
        this.fixedMob = fm;
    }

    public final void setForceMove(int fm) {
        this.lvForceMove = fm;
    }

    public final int getForceMove() {
        return this.lvForceMove;
    }

    public final void setLevelLimit(int fm) {
        this.lvLimit = fm;
    }

    public final int getLevelLimit() {
        return this.lvLimit;
    }

    public final void setReturnMapId(int rmi) {
        this.returnMapId = rmi;
    }

    public final void setSoaring(boolean b) {
        this.soaring = b;
    }

    public final boolean canSoar() {
        return this.soaring;
    }

    public final void toggleDrops() {
        this.dropsDisabled = (!this.dropsDisabled);
    }

    public final void setDrops(boolean b) {
        this.dropsDisabled = b;
    }

    public final void toggleGDrops() {
        this.gDropsDisabled = (!this.gDropsDisabled);
    }

    public final int getId() {
        return this.mapid;
    }

    public final MapleMap getReturnMap() {
        return ChannelServer.getInstance(this.channel).getMapFactory().getMap(this.returnMapId);
    }

    public final int getReturnMapId() {
        return this.returnMapId;
    }

    public final int getForcedReturnId() {
        return this.forcedReturnMap;
    }

    public final MapleMap getForcedReturnMap() {
        return ChannelServer.getInstance(this.channel).getMapFactory().getMap(this.forcedReturnMap);
    }

    public final void setForcedReturnMap(int map) {
        this.forcedReturnMap = map;
    }

    public final float getRecoveryRate() {
        return this.recoveryRate;
    }

    public final void setRecoveryRate(float recoveryRate) {
        this.recoveryRate = recoveryRate;
    }

    public final int getFieldLimit() {
        return this.fieldLimit;
    }

    public final void setFieldLimit(int fieldLimit) {
        this.fieldLimit = fieldLimit;
    }

    public final void setCreateMobInterval(short createMobInterval) {
        this.createMobInterval = createMobInterval;
    }

    public final void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public final void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public final String getMapName() {
        return this.mapName;
    }

    public final String getStreetName() {
        return this.streetName;
    }

    public final void setFirstUserEnter(String onFirstUserEnter) {
        this.onFirstUserEnter = onFirstUserEnter;
    }

    public final void setUserEnter(String onUserEnter) {
        this.onUserEnter = onUserEnter;
    }

    public final String getFirstUserEnter() {
        return this.onFirstUserEnter;
    }

    public final String getUserEnter() {
        return this.onUserEnter;
    }

    public final boolean hasClock() {
        return this.clock;
    }

    public final void setClock(boolean hasClock) {
        this.clock = hasClock;
    }

    public final boolean isTown() {
        return this.town;
    }

    public final void setTown(boolean town) {
        this.town = town;
    }

    public final boolean allowPersonalShop() {
        return this.personalShop;
    }

    public final void setPersonalShop(boolean personalShop) {
        this.personalShop = personalShop;
    }

    public final void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public final void setEverlast(boolean everlast) {
        this.everlast = everlast;
    }

    public final boolean getEverlast() {
        return this.everlast;
    }

    public final int getHPDec() {
        return this.decHP;
    }

    public final void setHPDec(int delta) {
        if ((delta > 0) || (this.mapid == 749040100)) {
            this.lastHurtTime = System.currentTimeMillis();
        }
        this.decHP = ((short) delta);
    }

    public final int getHPDecInterval() {
        return this.decHPInterval;
    }

    public final void setHPDecInterval(int delta) {
        this.decHPInterval = delta;
    }

    public final int getHPDecProtect() {
        return this.protectItem;
    }

    public final void setHPDecProtect(int delta) {
        this.protectItem = delta;
    }

    public final int getCurrentPartyId() {
        this.charactersLock.readLock().lock();
        try {
            Iterator ltr = this.characters.iterator();

            while (ltr.hasNext()) {
                MapleCharacter chr = (MapleCharacter) ltr.next();
                if (chr.getParty() != null) {
                    return chr.getParty().getId();
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return -1;
    }

    public final void addMapObject(MapleMapObject mapobject) {
        this.runningOidLock.lock();
        int newOid;
        try {
            newOid = ++this.runningOid;
        } finally {
            this.runningOidLock.unlock();
        }

        mapobject.setObjectId(newOid);

        ((ReentrantReadWriteLock) this.mapobjectlocks.get(mapobject.getType())).writeLock().lock();
        try {
            ((LinkedHashMap) this.mapobjects.get(mapobject.getType())).put(Integer.valueOf(newOid), mapobject);
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(mapobject.getType())).writeLock().unlock();
        }
    }

    private void spawnAndAddRangedMapObject(MapleMapObject mapobject, DelayedPacketCreation packetbakery) {
        addMapObject(mapobject);

        this.charactersLock.readLock().lock();
        try {
            Iterator itr = this.characters.iterator();

            while (itr.hasNext()) {
                MapleCharacter chr = (MapleCharacter) itr.next();
                if ((!chr.isClone()) && ((mapobject.getType() == MapleMapObjectType.MIST) || (chr.getTruePosition().distanceSq(mapobject.getTruePosition()) <= GameConstants.maxViewRangeSq()))) {
                    packetbakery.sendPackets(chr.getClient());
                    chr.addVisibleMapObject(mapobject);
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
    }

    public final void removeMapObject(MapleMapObject obj) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(obj.getType())).writeLock().lock();
        try {
            ((LinkedHashMap) this.mapobjects.get(obj.getType())).remove(Integer.valueOf(obj.getObjectId()));
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(obj.getType())).writeLock().unlock();
        }
    }

    public final Point calcPointBelow(Point initial) {
        MapleFoothold fh = this.footholds.findBelow(initial);
        if (fh == null) {
            return null;
        }
        int dropY = fh.getY1();
        if ((!fh.isWall()) && (fh.getY1() != fh.getY2())) {
            double s1 = Math.abs(fh.getY2() - fh.getY1());
            double s2 = Math.abs(fh.getX2() - fh.getX1());
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int) (Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2))));
            } else {
                dropY = fh.getY1() + (int) (Math.cos(Math.atan(s2 / s1)) * (Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2))));
            }
        }
        return new Point(initial.x, dropY);
    }

    public final Point calcDropPos(Point initial, Point fallback) {
        Point ret = calcPointBelow(new Point(initial.x, initial.y - 50));
        if (ret == null) {
            return fallback;
        }
        return ret;
    }

    private void dropFromMonster(MapleCharacter chr, MapleMonster mob, boolean instanced) {
        if (mob == null || chr == null || ChannelServer.getInstance(channel) == null || dropsDisabled || mob.dropsDisabled() || chr.getPyramidSubway() != null) { //no drops in pyramid ok? no cash either
            return;
        }

        //We choose not to readLock for this.
        //This will not affect the internal state, and we don't want to
        //introduce unneccessary locking, especially since this function
        //is probably used quite often.
        if (!instanced && mapobjects.get(MapleMapObjectType.ITEM).size() >= 250) {
            removeDrops();
        }

        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final byte droptype = (byte) (mob.getStats().isExplosiveReward() ? 3 : mob.getStats().isFfaLoot() ? 2 : chr.getParty() != null ? 1 : 0);
        final int mobpos = mob.getTruePosition().x, cmServerrate = ChannelServer.getInstance(channel).getMesoRate(), chServerrate = ChannelServer.getInstance(channel).getDropRate(), caServerrate = ChannelServer.getInstance(channel).getCashRate();
        Item idrop;
        byte d = 1;
        Point pos = new Point(0, mob.getTruePosition().y);
        double showdown = 100.0;
        final MonsterStatusEffect mse = mob.getBuff(MonsterStatus.SHOWDOWN);
        if (mse != null) {
            showdown += mse.getX();
        }

        final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
        final List<MonsterDropEntry> derp = mi.retrieveDrop(mob.getId());
        if (derp == null) { //if no drops, no global drops either <3
            return;
        }
        final List<MonsterDropEntry> dropEntry = new ArrayList<>(derp);
        Collections.shuffle(dropEntry);

        boolean mesoDropped = false;
        for (final MonsterDropEntry de : dropEntry) {
            if (de.itemId == mob.getStolen()) {
                continue;
            }
            if (Randomizer.nextInt(999999) < (int) (de.chance * chServerrate * chr.getDropMod() * (chr.getStat().dropBuff / 100.0) * (showdown / 100.0))) {
                if (mesoDropped && droptype != 3 && de.itemId == 0) { //not more than 1 sack of meso
                    continue;
                }
                if (de.questid > 0 && chr.getQuestStatus(de.questid) != 1) {
                    continue;
                }
                if (de.itemId / 10000 == 238 && !mob.getStats().isBoss() && chr.getMonsterBook().getLevelByCard(ii.getCardMobId(de.itemId)) >= 2) {
                    continue;
                }
                if (droptype == 3) {
                    pos.x = (mobpos + (d % 2 == 0 ? (40 * (d + 1) / 2) : -(40 * (d / 2))));
                } else {
                    pos.x = (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                }
                if (de.itemId == 0) { // meso
                    int mesos = Randomizer.nextInt(1 + Math.abs(de.Maximum - de.Minimum)) + de.Minimum;

                    if (mesos > 0) {
                        spawnMobMesoDrop((int) (mesos * (chr.getStat().mesoBuff / 100.0) * chr.getDropMod() * cmServerrate), calcDropPos(pos, mob.getTruePosition()), mob, chr, false, droptype);
                        mesoDropped = true;
                    }
                } else {
                    if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                    } else {
                        final int range = Math.abs(de.Maximum - de.Minimum);
                        idrop = new Item(de.itemId, (byte) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(range <= 0 ? 1 : range) + de.Minimum : 1), (byte) 0);
                    }
                    idrop.setGMLog("Dropped from monster " + mob.getId() + " on " + mapid);
                    spawnMobDrop(idrop, calcDropPos(pos, mob.getTruePosition()), mob, chr, droptype, de.questid);
                }
                d++;
            }
        }
        final List<MonsterGlobalDropEntry> globalEntry = new ArrayList<>(mi.getGlobalDrop());
        Collections.shuffle(globalEntry);
        //   final int cashz = (int) ((mob.getStats().isBoss() && mob.getStats().getHPDisplayType() == 0 ? 20 : 1) * caServerrate);
        // final int cashModifier = (int) ((mob.getStats().isBoss() ? (mob.getStats().isPartyBonus() ? (mob.getMobExp() / 1000) : 0) : (mob.getMobExp() / 1000 + mob.getMobMaxHp() / 20000))); //no rate
        // Global Drops
        for (final MonsterGlobalDropEntry de : globalEntry) {
            if (Randomizer.nextInt(999999) < de.chance && (de.continent < 0 || (de.continent < 10 && mapid / 100000000 == de.continent) || (de.continent < 100 && mapid / 10000000 == de.continent) || (de.continent < 1000 && mapid / 1000000 == de.continent))) {
                if (de.questid > 0 && chr.getQuestStatus(de.questid) != 1) {
                    continue;
                }
                if (de.itemId == 0) {
                    chr.modifyCSPoints(1, 10, true);
                } else if (!gDropsDisabled) {
                    if (droptype == 3) {
                        pos.x = (mobpos + (d % 2 == 0 ? (40 * (d + 1) / 2) : -(40 * (d / 2))));
                    } else {
                        pos.x = (mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : -(25 * (d / 2))));
                    }
                    if (GameConstants.getInventoryType(de.itemId) == MapleInventoryType.EQUIP) {
                        idrop = ii.randomizeStats((Equip) ii.getEquipById(de.itemId));
                    } else {
                        idrop = new Item(de.itemId, (byte) 0, (short) (de.Maximum != 1 ? Randomizer.nextInt(de.Maximum - de.Minimum) + de.Minimum : 1), (byte) 0);
                    }
                    idrop.setGMLog("Dropped from monster " + mob.getId() + " on " + mapid + " (Global)");
                    spawnMobDrop(idrop, calcDropPos(pos, mob.getTruePosition()), mob, chr, de.onlySelf ? 0 : droptype, de.questid);
                    d++;
                }
            }
        }
    }

    public void removeMonster(MapleMonster monster) {
        if (monster == null) {
            return;
        }
        this.spawnedMonstersOnMap.decrementAndGet();
        broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 0));
        removeMapObject(monster);
        monster.killed();
    }

    public void killMonster(final MapleMonster monster) { // For mobs with removeAfter
        if (monster == null) {
            return;
        }
        spawnedMonstersOnMap.decrementAndGet();
        monster.setHp(0);
        if (monster.getLinkCID() <= 0) {
            monster.spawnRevives(this);
        }
        broadcastMessage(MobPacket.killMonster(monster.getObjectId(), monster.getStats().getSelfD() < 0 ? 1 : monster.getStats().getSelfD()));
        removeMapObject(monster);
        monster.killed();
    }

    public final void killMonster(MapleMonster monster, MapleCharacter chr, boolean withDrops, boolean second, byte animation) {
        killMonster(monster, chr, withDrops, second, animation, 0);
    }

    public final void killMonster(final MapleMonster monster, final MapleCharacter chr, boolean withDrops, boolean second, byte animation, int lastSkill) {
        if (((monster.getId() == 8810122) || (monster.getId() == 8810018)) && (!second)) {
            Timer.MapTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    MapleMap.this.killMonster(monster, chr, true, true, (byte) 1);
                    MapleMap.this.killAllMonsters(true);
                }
            }, 3000L);

            return;
        }
        if (monster.getId() == 8820014) {
            killMonster(8820000);
        } else if (monster.getId() == 9300166) {
            animation = 4;
        }
        this.spawnedMonstersOnMap.decrementAndGet();
        removeMapObject(monster);
        monster.killed();
        MapleSquad sqd = getSquadByMap();
        boolean instanced = (sqd != null) || (monster.getEventInstance() != null) || (getEMByMap() != null);
        int dropOwner = monster.killBy(chr, lastSkill);
        if (animation >= 0) {
            broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animation));
        }

        if (monster.getBuffToGive() > -1) {
            int buffid = monster.getBuffToGive();
            MapleStatEffect buff = MapleItemInformationProvider.getInstance().getItemEffect(buffid);

            this.charactersLock.readLock().lock();
            try {
                for (MapleCharacter mc : this.characters) {
                    if (mc.isAlive()) {
                        buff.applyTo(mc);

                        switch (monster.getId()) {
                            case 8810018:
                            case 8810122:
                            case 8820001:
                                mc.getClient().getSession().write(CField.EffectPacket.showOwnBuffEffect(buffid, 13, mc.getLevel(), 1));
                                broadcastMessage(mc, CField.EffectPacket.showBuffeffect(mc.getId(), buffid, 13, mc.getLevel(), 1), false);
                        }
                    }
                }
            } finally {
                this.charactersLock.readLock().unlock();
            }
        }
        int mobid = monster.getId();
        ExpeditionType type = null;
        if ((mobid == 8810018) && (this.mapid == 240060200)) {
            charactersLock.readLock().lock();
            try {
                for (MapleCharacter c : characters) {
                    //c.finishAchievement(16);
                }
            } finally {
                charactersLock.readLock().unlock();
            }
            //FileoutputUtil.log(FileoutputUtil.Horntail_Log, MapDebug_Log());
            if (speedRunStart > 0) {
                type = ExpeditionType.Horntail;
            }
            doShrine(true);
        } else if ((mobid == 8810122) && (this.mapid == 240060201)) {
            World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, "To the crew that have finally conquered Chaos Horned Tail after numerous attempts, I salute thee! You are the true heroes of Leafre!!"));
            charactersLock.readLock().lock();
            try {
                for (MapleCharacter c : characters) {
                    /// c.finishAchievement(24);
                }
            } finally {
                charactersLock.readLock().unlock();
            }
//            FileoutputUtil.log(FileoutputUtil.Horntail_Log, MapDebug_Log());
            if (speedRunStart > 0) {
                type = ExpeditionType.ChaosHT;
            }
            doShrine(true);
        } else if ((mobid == 9400266) && (this.mapid == 802000111)) {
            doShrine(true);
        } else if ((mobid == 9400265) && (this.mapid == 802000211)) {
            doShrine(true);
        } else if ((mobid == 9400270) && (this.mapid == 802000411)) {
            doShrine(true);
        } else if ((mobid == 9400273) && (this.mapid == 802000611)) {
            doShrine(true);
        } else if ((mobid == 9400294) && (this.mapid == 802000711)) {
            doShrine(true);
        } else if ((mobid == 9400296) && (this.mapid == 802000803)) {
            doShrine(true);
        } else if ((mobid == 9400289) && (this.mapid == 802000821)) {
            doShrine(true);
        } else if ((getAllMonstersThreadsafe().isEmpty()) && (this.mapid == 955000100)) {
            if (getPlayer() != null) {
                getPlayer().broadcastPacket(CField.showEffect("aswan/clear"));
                getPlayer().dropMessage(6, "[Hilla's Gang Liberation] Portal to continue has been unblocked!");
            }
        } else if ((getAllMonstersThreadsafe().isEmpty()) && (this.mapid == 955000200)) {
            if (getPlayer() != null) {
                getPlayer().broadcastPacket(CField.showEffect("aswan/clear"));
                getPlayer().dropMessage(6, "[Hilla's Gang Liberation] Portal to continue has been unblocked!");
            }
        } else if ((getAllMonstersThreadsafe().isEmpty()) && (this.mapid == 955000300)) {
            if (getPlayer() != null) {
                getPlayer().broadcastPacket(CField.showEffect("aswan/clear"));
                getPlayer().dropMessage(6, "[Hilla's Gang Liberation] Enter the portal to claim your reward!");
            }
        } else if ((mobid == 8830000) && (this.mapid == 105100300)) {
            if (this.speedRunStart > 0L) {
                type = ExpeditionType.Normal_Balrog;
            }
        } else if (((mobid == 9420544) || (mobid == 9420549)) && (this.mapid == 551030200) && (monster.getEventInstance() != null) && (monster.getEventInstance().getName().contains(getEMByMap().getName()))) {
            doShrine(getAllReactor().isEmpty());
        } else if ((mobid == 8820001) && (this.mapid == 270050100)) {
            World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, "Oh, the exploration team who has defeated Pink Bean with undying fervor! You are the true victors of time!"));
            charactersLock.readLock().lock();
            try {
                for (MapleCharacter c : characters) {
                    // c.finishAchievement(17);
                }
            } finally {
                charactersLock.readLock().unlock();
            }
            if (speedRunStart > 0) {
                type = ExpeditionType.Pink_Bean;
            }
            doShrine(true);
        } else if ((mobid == 8850011) && (this.mapid == 274040200)) {
            World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, "To you whom have defeated Empress Cygnus in the future, you are the heroes of time!"));
            charactersLock.readLock().lock();
            try {
                for (MapleCharacter c : characters) {
                    // c.finishAchievement(39);
                }
            } finally {
                charactersLock.readLock().unlock();
            }
            if (speedRunStart > 0) {
                type = ExpeditionType.Cygnus;
            }
            doShrine(true);
        } else if ((mobid == 8870000) && (this.mapid == 262031300)) {

            this.charactersLock.readLock().lock();
            try {
                for (MapleCharacter c : characters) {
                    // c.finishAchievement(39);
                }
            } finally {
                MapleCharacter c;
                this.charactersLock.readLock().unlock();
            }
            if (this.speedRunStart > 0L) {
                type = ExpeditionType.Hilla;
            }
            doShrine(true);
        } else if ((mobid == 8840000) && (this.mapid == 211070100)) {
            this.charactersLock.readLock().lock();
            try {
                for (MapleCharacter c : characters) {
                    // c.finishAchievement(39);
                }
            } finally {
                MapleCharacter c;
                this.charactersLock.readLock().unlock();
            }
            if (this.speedRunStart > 0L) {
                type = ExpeditionType.Von_Leon;
            }
            doShrine(true);
        } else if ((mobid == 8800002) && (this.mapid == 280030000)) {
            this.charactersLock.readLock().lock();
            try {
                for (MapleCharacter c : characters) {
                    // c.finishAchievement(39);
                }
            } finally {
                MapleCharacter c;
                this.charactersLock.readLock().unlock();
            }

            if (this.speedRunStart > 0L) {
                type = ExpeditionType.Zakum;
            }
            doShrine(true);
        } else if ((mobid == 8800102) && (this.mapid == 280030001)) {
            this.charactersLock.readLock().lock();
            try {
                for (MapleCharacter c : characters) {
                    // c.finishAchievement(39);
                }
            } finally {
                MapleCharacter c;
                this.charactersLock.readLock().unlock();
            }

            if (this.speedRunStart > 0L) {
                type = ExpeditionType.Chaos_Zakum;
            }

            doShrine(true);
        } else if ((mobid >= 8800003) && (mobid <= 8800010)) {
            boolean makeZakReal = true;
            List<MapleMonster> monsters = getAllMonstersThreadsafe();

            for (MapleMonster mons : monsters) {
                if ((mons.getId() >= 8800003) && (mons.getId() <= 8800010)) {
                    makeZakReal = false;
                    break;
                }
            }
            if (makeZakReal) {
                for (MapleMapObject object : monsters) {
                    MapleMonster mons = (MapleMonster) object;
                    if (mons.getId() == 8800000) {
                        Point pos = mons.getTruePosition();
                        killAllMonsters(true);
                        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), pos);
                        break;
                    }
                }
            }
        } else if ((mobid >= 8800103) && (mobid <= 8800110)) {
            boolean makeZakReal = true;
            List<MapleMonster> monsters = getAllMonstersThreadsafe();

            for (MapleMonster mons : monsters) {
                if ((mons.getId() >= 8800103) && (mons.getId() <= 8800110)) {
                    makeZakReal = false;
                    break;
                }
            }
            if (makeZakReal) {
                for (MapleMonster mons : monsters) {
                    if (mons.getId() == 8800100) {
                        Point pos = mons.getTruePosition();
                        killAllMonsters(true);
                        spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800100), pos);
                        break;
                    }
                }
            }
        } else if (mobid == 8820008) {
            for (MapleMapObject mmo : getAllMonstersThreadsafe()) {
                MapleMonster mons = (MapleMonster) mmo;
                if (mons.getLinkOid() != monster.getObjectId()) {
                    killMonster(mons, chr, false, false, animation);
                }
            }
        } else if ((mobid >= 8820010) && (mobid <= 8820014)) {
            for (MapleMapObject mmo : getAllMonstersThreadsafe()) {
                MapleMonster mons = (MapleMonster) mmo;
                if ((mons.getId() != 8820000) && (mons.getId() != 8820001) && (mons.getObjectId() != monster.getObjectId()) && (mons.isAlive()) && (mons.getLinkOid() == monster.getObjectId())) {
                    killMonster(mons, chr, false, false, animation);
                }
            }
        } else if ((mobid / 100000 == 98) && (chr.getMapId() / 10000000 == 95) && (getAllMonstersThreadsafe().size() == 0)) {
            switch (chr.getMapId() % 1000 / 100) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                    chr.getClient().getSession().write(CField.MapEff("monsterPark/clear"));
                    break;
                case 5:
                    if (chr.getMapId() / 1000000 == 952) {
                        chr.getClient().getSession().write(CField.MapEff("monsterPark/clearF"));
                    } else {
                        chr.getClient().getSession().write(CField.MapEff("monsterPark/clear"));
                    }
                    break;
                case 6:
                    chr.getClient().getSession().write(CField.MapEff("monsterPark/clearF"));
            }
        }

        if ((type != null)
                && (this.speedRunStart > 0L) && (this.speedRunLeader.length() > 0)) {
            long endTime = System.currentTimeMillis();
            String time = StringUtil.getReadableMillis(this.speedRunStart, endTime);
            broadcastMessage(CWvsContext.serverNotice(5, new StringBuilder().append(this.speedRunLeader).append("'s squad has taken ").append(time).append(" to defeat ").append(type.name()).append("!").toString()));
            getRankAndAdd(this.speedRunLeader, time, type, endTime - this.speedRunStart, sqd == null ? null : sqd.getMembers());
            endSpeedRun();
        }

        if (withDrops) {
            MapleCharacter drop = null;
            if (dropOwner <= 0) {
                drop = chr;
            } else {
                drop = getCharacterById(dropOwner);
                if (drop == null) {
                    drop = chr;
                }
            }
            dropFromMonster(drop, monster, instanced);
        }
    }

    public List<MapleReactor> getAllReactor() {
        return getAllReactorsThreadsafe();
    }

    public List<MapleReactor> getAllReactorsThreadsafe() {
        ArrayList<MapleReactor> ret = new ArrayList<MapleReactor>();
        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                ret.add((MapleReactor) mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
        return ret;
    }

    public List<MapleSummon> getAllSummonsThreadsafe() {
        ArrayList<MapleSummon> ret = new ArrayList<MapleSummon>();
        mapobjectlocks.get(MapleMapObjectType.SUMMON).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.SUMMON).values()) {
                if (mmo instanceof MapleSummon) {
                    ret.add((MapleSummon) mmo);
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.SUMMON).readLock().unlock();
        }
        return ret;
    }

    public List<MapleMapObject> getAllDoor() {
        return getAllDoorsThreadsafe();
    }

    public List<MapleMapObject> getAllDoorsThreadsafe() {
        ArrayList<MapleMapObject> ret = new ArrayList<MapleMapObject>();
        mapobjectlocks.get(MapleMapObjectType.DOOR).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.DOOR).values()) {
                if (mmo instanceof MapleDoor) {
                    ret.add(mmo);
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.DOOR).readLock().unlock();
        }
        return ret;
    }

    public List<MapleMapObject> getAllMechDoorsThreadsafe() {
        ArrayList<MapleMapObject> ret = new ArrayList<MapleMapObject>();
        mapobjectlocks.get(MapleMapObjectType.DOOR).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.DOOR).values()) {
                if (mmo instanceof MechDoor) {
                    ret.add(mmo);
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.DOOR).readLock().unlock();
        }
        return ret;
    }

    public List<MapleMapObject> getAllMerchant() {
        return getAllHiredMerchantsThreadsafe();
    }

    public List<MapleMapObject> getAllHiredMerchantsThreadsafe() {
        ArrayList<MapleMapObject> ret = new ArrayList<MapleMapObject>();
        mapobjectlocks.get(MapleMapObjectType.HIRED_MERCHANT).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.HIRED_MERCHANT).values()) {
                ret.add(mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.HIRED_MERCHANT).readLock().unlock();
        }
        return ret;
    }

    public List<MapleMonster> getAllMonster() {
        return getAllMonstersThreadsafe();
    }

    public List<MapleMonster> getAllMonstersThreadsafe() {
        ArrayList<MapleMonster> ret = new ArrayList<MapleMonster>();
        mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.MONSTER).values()) {
                ret.add((MapleMonster) mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().unlock();
        }
        return ret;
    }

    public List<Integer> getAllUniqueMonsters() {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.MONSTER).values()) {
                final int theId = ((MapleMonster) mmo).getId();
                if (!ret.contains(theId)) {
                    ret.add(theId);
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.MONSTER).readLock().unlock();
        }
        return ret;
    }

    public final void killAllMonsters(boolean animate) {
        for (MapleMapObject monstermo : getAllMonstersThreadsafe()) {
            MapleMonster monster = (MapleMonster) monstermo;
            this.spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0L);
            broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animate ? 1 : 0));
            removeMapObject(monster);
            monster.killed();
        }
    }

    public final void killMonster(int monsId) {
        for (MapleMapObject mmo : getAllMonstersThreadsafe()) {
            if (((MapleMonster) mmo).getId() == monsId) {
                this.spawnedMonstersOnMap.decrementAndGet();
                removeMapObject(mmo);
                broadcastMessage(MobPacket.killMonster(mmo.getObjectId(), 1));
                ((MapleMonster) mmo).killed();
                break;
            }
        }
    }

    private String MapDebug_Log() {
        StringBuilder sb = new StringBuilder("Defeat time : ");
        sb.append(FileoutputUtil.CurrentReadable_Time());

        sb.append(" | Mapid : ").append(this.mapid);

        this.charactersLock.readLock().lock();
        try {
            sb.append(" Users [").append(this.characters.size()).append("] | ");
            for (MapleCharacter mc : this.characters) {
                sb.append(mc.getName()).append(", ");
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return sb.toString();
    }

    public final void limitReactor(int rid, int num) {
        List<MapleReactor> toDestroy = new ArrayList();
        Map<Integer, Integer> contained = new LinkedHashMap();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if (contained.containsKey(Integer.valueOf(mr.getReactorId()))) {
                    if (((Integer) contained.get(Integer.valueOf(mr.getReactorId()))).intValue() >= num) {
                        toDestroy.add(mr);
                    } else {
                        contained.put(Integer.valueOf(mr.getReactorId()), Integer.valueOf(((Integer) contained.get(Integer.valueOf(mr.getReactorId()))).intValue() + 1));
                    }
                } else {
                    contained.put(Integer.valueOf(mr.getReactorId()), Integer.valueOf(1));
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        for (MapleReactor mr : toDestroy) {
            destroyReactor(mr.getObjectId());
        }
    }

    public final void destroyReactors(int first, int last) {
        List<MapleReactor> toDestroy = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if ((mr.getReactorId() >= first) && (mr.getReactorId() <= last)) {
                    toDestroy.add(mr);
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        for (MapleReactor mr : toDestroy) {
            destroyReactor(mr.getObjectId());
        }
    }

    public final void destroyReactor(int oid) {
        final MapleReactor reactor = getReactorByOid(oid);
        if (reactor == null) {
            return;
        }
        broadcastMessage(CField.destroyReactor(reactor));
        reactor.setAlive(false);
        removeMapObject(reactor);
        reactor.setTimerActive(false);

        if (reactor.getDelay() > 0) {
            Timer.MapTimer.getInstance().schedule(new Runnable() {
                public final void run() {
                    MapleMap.this.respawnReactor(reactor);
                }
            }, reactor.getDelay());
        }
    }

    public final void reloadReactors() {
        List<MapleReactor> toSpawn = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor reactor = (MapleReactor) obj;
                broadcastMessage(CField.destroyReactor(reactor));
                reactor.setAlive(false);
                reactor.setTimerActive(false);
                toSpawn.add(reactor);
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        for (MapleReactor r : toSpawn) {
            removeMapObject(r);
            if (!r.isCustom()) {
                respawnReactor(r);
            }
        }
    }

    public final void resetReactors() {
        setReactorState((byte) 0);
    }

    public final void setReactorState() {
        setReactorState((byte) 1);
    }

    public final void setReactorState(final byte state) {
        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                ((MapleReactor) obj).forceHitReactor((byte) state);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
    }

    public final void setReactorDelay(final int state) {
        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                ((MapleReactor) obj).setDelay(state);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
    }

    public final void shuffleReactors() {
        shuffleReactors(0, 9999999);
    }

    public final void shuffleReactors(int first, int last) {
        List points = new ArrayList();
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if ((mr.getReactorId() >= first) && (mr.getReactorId() <= last)) {
                    points.add(mr.getPosition());
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        Collections.shuffle(points);
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if ((mr.getReactorId() >= first) && (mr.getReactorId() <= last)) {
                    mr.setPosition((Point) points.remove(points.size() - 1));
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }

    public final void updateMonsterController(MapleMonster monster) {
        if ((!monster.isAlive()) || (monster.getLinkCID() > 0) || (monster.getStats().isEscort())) {
            return;
        }
        if (monster.getController() != null) {
            if ((monster.getController().getMap() != this) || (monster.getController().getTruePosition().distanceSq(monster.getTruePosition()) > monster.getRange())) {
                monster.getController().stopControllingMonster(monster);
            } else {
                return;
            }
        }
        int mincontrolled = -1;
        MapleCharacter newController = null;

        this.charactersLock.readLock().lock();
        try {
            Iterator ltr = this.characters.iterator();

            while (ltr.hasNext()) {
                MapleCharacter chr = (MapleCharacter) ltr.next();
                if ((!chr.isHidden()) && (!chr.isClone()) && ((chr.getControlledSize() < mincontrolled) || (mincontrolled == -1)) && (chr.getTruePosition().distanceSq(monster.getTruePosition()) <= monster.getRange())) {
                    mincontrolled = chr.getControlledSize();
                    newController = chr;
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        if (newController != null) {
            if (monster.isFirstAttack()) {
                newController.controlMonster(monster, true);
                monster.setControllerHasAggro(true);
            } else {
                newController.controlMonster(monster, false);
            }
        }
    }

    public final MapleMapObject getMapObject(int oid, MapleMapObjectType type) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(type)).readLock().lock();
        try {
            return (MapleMapObject) ((LinkedHashMap) this.mapobjects.get(type)).get(Integer.valueOf(oid));
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(type)).readLock().unlock();
        }
    }

    public final boolean containsNPC(int npcid) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.NPC)).readLock().lock();
        try {
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();
            MapleNPC n;
            while (itr.hasNext()) {
                n = (MapleNPC) itr.next();
                if (n.getId() == npcid) {
                    return true;
                }
            }
            return false;
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.NPC)).readLock().unlock();
        }
    }

    public MapleNPC getNPCById(int id) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.NPC)).readLock().lock();
        try {
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();
            MapleNPC n;
            while (itr.hasNext()) {
                n = (MapleNPC) itr.next();
                if (n.getId() == id) {
                    return n;
                }
            }
            return null;
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.NPC)).readLock().unlock();
        }
    }

    public MapleMonster getMonsterById(int id) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().lock();
        try {
            MapleMonster ret = null;
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.MONSTER)).values().iterator();
            MapleMonster n;
            while (itr.hasNext()) {
                n = (MapleMonster) itr.next();
                if (n.getId() == id) {
                    ret = n;
                    break;
                }
            }
            return ret;
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().unlock();
        }
    }

    public int countMonsterById(int id) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().lock();
        try {
            int ret = 0;
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.MONSTER)).values().iterator();
            MapleMonster n;
            while (itr.hasNext()) {
                n = (MapleMonster) itr.next();
                if (n.getId() == id) {
                    ret++;
                }
            }
            return ret;
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().unlock();
        }
    }

    public MapleReactor getReactorById(int id) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            MapleReactor ret = null;
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.REACTOR)).values().iterator();
            MapleReactor n;
            while (itr.hasNext()) {
                n = (MapleReactor) itr.next();
                if (n.getReactorId() == id) {
                    ret = n;
                    break;
                }
            }
            return ret;
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }

    public final MapleMonster getMonsterByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.MONSTER);
        if (mmo == null) {
            return null;
        }
        return (MapleMonster) mmo;
    }

    public final MapleNPC getNPCByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.NPC);
        if (mmo == null) {
            return null;
        }
        return (MapleNPC) mmo;
    }

    public final MapleReactor getReactorByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.REACTOR);
        if (mmo == null) {
            return null;
        }
        return (MapleReactor) mmo;
    }

    public final MonsterFamiliar getFamiliarByOid(int oid) {
        MapleMapObject mmo = getMapObject(oid, MapleMapObjectType.FAMILIAR);
        if (mmo == null) {
            return null;
        }
        return (MonsterFamiliar) mmo;
    }

    public final MapleReactor getReactorByName(String name) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for (MapleMapObject obj : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                MapleReactor mr = (MapleReactor) obj;
                if (mr.getName().equalsIgnoreCase(name)) {
                    return mr;
                }
            }
            return null;
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }

    public final void spawnNpc(int id, Point pos) {
        MapleNPC npc = MapleLifeFactory.getNPC(id);
        npc.setPosition(pos);
        npc.setCy(pos.y);
        npc.setRx0(pos.x + 50);
        npc.setRx1(pos.x - 50);
        npc.setFh(getFootholds().findBelow(pos).getId());
        npc.setCustom(true);
        addMapObject(npc);
        broadcastMessage(CField.NPCPacket.spawnNPC(npc, true));
    }

    public final void removeNpc(int npcid) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.NPC)).writeLock().lock();
        try {
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();
            while (itr.hasNext()) {
                MapleNPC npc = (MapleNPC) itr.next();
                if ((npc.isCustom()) && ((npcid == -1) || (npc.getId() == npcid))) {
                    broadcastMessage(CField.NPCPacket.removeNPCController(npc.getObjectId()));
                    broadcastMessage(CField.NPCPacket.removeNPC(npc.getObjectId()));
                    itr.remove();
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.NPC)).writeLock().unlock();
        }
    }

    public final void hideNpc(int npcid) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.NPC)).readLock().lock();
        try {
            Iterator itr = ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.NPC)).values().iterator();
            while (itr.hasNext()) {
                MapleNPC npc = (MapleNPC) itr.next();
                if ((npcid == -1) || (npc.getId() == npcid)) {
                    broadcastMessage(CField.NPCPacket.removeNPCController(npc.getObjectId()));
                    broadcastMessage(CField.NPCPacket.removeNPC(npc.getObjectId()));
                }
            }
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.NPC)).readLock().unlock();
        }
    }

    public final void spawnReactorOnGroundBelow(MapleReactor mob, Point pos) {
        mob.setPosition(pos);
        mob.setCustom(true);
        spawnReactor(mob);
    }

    public final void spawnMonster_sSack(MapleMonster mob, Point pos, int spawnType) {
        mob.setPosition(calcPointBelow(new Point(pos.x, pos.y - 1)));
        spawnMonster(mob, spawnType);
    }

    public final void spawnMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        spawnMonster_sSack(mob, pos, -2);
    }

    public final int spawnMonsterWithEffectBelow(MapleMonster mob, Point pos, int effect) {
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        return spawnMonsterWithEffect(mob, effect, spos);
    }

    public final void spawnZakum(int x, int y) {
        Point pos = new Point(x, y);
        MapleMonster mainb = MapleLifeFactory.getMonster(8800000);
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        mainb.setPosition(spos);
        mainb.setFake(true);

        spawnFakeMonster(mainb);

        int[] zakpart = {8800003, 8800004, 8800005, 8800006, 8800007, 8800008, 8800009, 8800010};

        for (int i : zakpart) {
            MapleMonster part = MapleLifeFactory.getMonster(i);
            part.setPosition(spos);

            spawnMonster(part, -2);
        }
        if (this.squadSchedule != null) {
            cancelSquadSchedule(false);
        }
    }

    public final void spawnChaosZakum(int x, int y) {
        Point pos = new Point(x, y);
        MapleMonster mainb = MapleLifeFactory.getMonster(8800100);
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        mainb.setPosition(spos);
        mainb.setFake(true);

        spawnFakeMonster(mainb);

        int[] zakpart = {8800103, 8800104, 8800105, 8800106, 8800107, 8800108, 8800109, 8800110};

        for (int i : zakpart) {
            MapleMonster part = MapleLifeFactory.getMonster(i);
            part.setPosition(spos);

            spawnMonster(part, -2);
        }
        if (this.squadSchedule != null) {
            cancelSquadSchedule(false);
        }
    }

    public final void spawnFakeMonsterOnGroundBelow(MapleMonster mob, Point pos) {
        Point spos = calcPointBelow(new Point(pos.x, pos.y - 1));
        spos.y -= 1;
        mob.setPosition(spos);
        spawnFakeMonster(mob);
    }

    private void checkRemoveAfter(MapleMonster monster) {
        int ra = monster.getStats().getRemoveAfter();

        if ((ra > 0) && (monster.getLinkCID() <= 0)) {
            monster.registerKill(ra * 1000);
        }
    }

    public final void spawnRevives(final MapleMonster monster, final int oid) {
        monster.setMap(this);
        checkRemoveAfter(monster);
        monster.setLinkOid(oid);
        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public final void sendPackets(MapleClient c) {
                c.getSession().write(MobPacket.spawnMonster(monster, monster.getStats().getSummonType() <= 1 ? -3 : monster.getStats().getSummonType(), oid));
            }
        });
        updateMonsterController(monster);

        this.spawnedMonstersOnMap.incrementAndGet();
    }

    public final void spawnMonster(MapleMonster monster, int spawnType) {
        spawnMonster(monster, spawnType, false);
    }

    public final void spawnMonster(final MapleMonster monster, final int spawnType, final boolean overwrite) {
        //Channel check.
        if (channel == 21) {
            //List of bosses to transform.  Bosses only get x36 HP, rather than x200.
            for (int m : GameConstants.mobBuff) {
                if (monster.getId() == m) {
                    //In order: (newLevel, hpMulti, bossHpMulti, expMulti)
                    monster.hellChangeLevel(monster.getStats().getLevel() * 1.5, 320, 36, 16);
                    break;
                } else {
                    monster.hellChangeLevel(monster.getStats().getLevel() * 1.5, 320, 200, 16);
                }
            }
        }
        monster.setMap(this);
        checkRemoveAfter(monster);

        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public final void sendPackets(MapleClient c) {
                c.getSession().write(MobPacket.spawnMonster(monster, (monster.getStats().getSummonType() <= 1) || (monster.getStats().getSummonType() == 27) || (overwrite) ? spawnType : monster.getStats().getSummonType(), 0));
            }
        });
        updateMonsterController(monster);

        this.spawnedMonstersOnMap.incrementAndGet();
    }

    public final int spawnMonsterWithEffect(final MapleMonster monster, final int effect, Point pos) {
        try {
            monster.setMap(this);
            monster.setPosition(pos);

            spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
                @Override
                public final void sendPackets(MapleClient c) {
                    c.getSession().write(MobPacket.spawnMonster(monster, effect, 0));
                }
            });
            updateMonsterController(monster);

            this.spawnedMonstersOnMap.incrementAndGet();
            return monster.getObjectId();
        } catch (Exception e) {
        }
        return -1;
    }

    public final void spawnFakeMonster(final MapleMonster monster) {
        monster.setMap(this);
        monster.setFake(true);

        spawnAndAddRangedMapObject(monster, new DelayedPacketCreation() {
            @Override
            public final void sendPackets(MapleClient c) {
                c.getSession().write(MobPacket.spawnMonster(monster, -4, 0));
            }
        });
        updateMonsterController(monster);

        this.spawnedMonstersOnMap.incrementAndGet();
    }

    public final void spawnReactor(final MapleReactor reactor) {
        reactor.setMap(this);

        spawnAndAddRangedMapObject(reactor, new DelayedPacketCreation() {
            @Override
            public final void sendPackets(MapleClient c) {
                c.getSession().write(CField.spawnReactor(reactor));
            }
        });
    }

    private void respawnReactor(MapleReactor reactor) {
        reactor.setState((byte) 0);
        reactor.setAlive(true);
        spawnReactor(reactor);
    }

    public final void spawnDoor(final MapleDoor door) {
        spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {
            @Override
            public final void sendPackets(MapleClient c) {
                door.sendSpawnData(c);
                c.getSession().write(CWvsContext.enableActions());
            }
        });
    }

    public final void spawnMechDoor(final MechDoor door) {
        spawnAndAddRangedMapObject(door, new DelayedPacketCreation() {
            @Override
            public final void sendPackets(MapleClient c) {
                c.getSession().write(CField.spawnMechDoor(door, true));
                c.getSession().write(CWvsContext.enableActions());
            }
        });
    }

    public final void spawnSummon(final MapleSummon summon) {
        summon.updateMap(this);
        spawnAndAddRangedMapObject(summon, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                if ((summon != null) && (c.getPlayer() != null) && ((!summon.isChangedMap()) || (summon.getOwnerId() == c.getPlayer().getId()))) {
                    c.getSession().write(CField.SummonPacket.spawnSummon(summon, true));
                }
            }
        });
    }

    public final void spawnFamiliar(final MonsterFamiliar familiar, final boolean respawn) {
        spawnAndAddRangedMapObject(familiar, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                if ((familiar != null) && (c.getPlayer() != null)) {
                    c.getSession().write(CField.spawnFamiliar(familiar, true, respawn));
                }
            }
        });
    }

    public final void spawnExtractor(final MapleExtractor ex) {
        spawnAndAddRangedMapObject(ex, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                ex.sendSpawnData(c);
            }
        });
    }

    public final void spawnMist(final MapleMist mist, final int duration, boolean fake) {
        spawnAndAddRangedMapObject(mist, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                mist.sendSpawnData(c);
            }
        });
        Timer.MapTimer tMan = Timer.MapTimer.getInstance();
        final ScheduledFuture poisonSchedule;
        switch (mist.isPoisonMist()) {
            case 1:
                final MapleCharacter owner = getCharacterById(mist.getOwnerId());
                final boolean pvp = owner.inPVP();
                poisonSchedule = tMan.register(new Runnable() {
                    public void run() {
                        for (MapleMapObject mo : MapleMap.this.getMapObjectsInRect(mist.getBox(), Collections.singletonList(pvp ? MapleMapObjectType.PLAYER : MapleMapObjectType.MONSTER))) {
                            if ((pvp) && (mist.makeChanceResult()) && (!((MapleCharacter) mo).hasDOT()) && (((MapleCharacter) mo).getId() != mist.getOwnerId())) {
                                ((MapleCharacter) mo).setDOT(mist.getSource().getDOT(), mist.getSourceSkill().getId(), mist.getSkillLevel());
                            } else if ((!pvp) && (mist.makeChanceResult()) && (!((MapleMonster) mo).isBuffed(MonsterStatus.POISON))) {
                                ((MapleMonster) mo).applyStatus(owner, new MonsterStatusEffect(MonsterStatus.POISON, Integer.valueOf(1), mist.getSourceSkill().getId(), null, false), true, duration, true, mist.getSource());
                            }
                        }
                    }
                }, 2000L, 2500L);

                break;
            case 4:
                poisonSchedule = tMan.register(new Runnable() {
                    public void run() {
                        for (MapleMapObject mo : MapleMap.this.getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER))) {
                            if (mist.makeChanceResult()) {
                                MapleCharacter chr = (MapleCharacter) mo;
                                chr.addMP((int) (mist.getSource().getX() * (chr.getStat().getMaxMp() / 100.0D)));
                            }
                        }
                    }
                }, 2000L, 2500L);

                break;
            default:
                poisonSchedule = null;
        }

        mist.setPoisonSchedule(poisonSchedule);
        mist.setSchedule(tMan.schedule(new Runnable() {
            public void run() {
                MapleMap.this.broadcastMessage(CField.removeMist(mist.getObjectId(), false));
                MapleMap.this.removeMapObject(mist);
                if (poisonSchedule != null) {
                    poisonSchedule.cancel(false);
                }
            }
        }, duration));
    }

    public final void disappearingItemDrop(MapleMapObject dropper, MapleCharacter owner, Item item, Point pos) {
        Point droppos = calcDropPos(pos, pos);
        MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 1, false);
        broadcastMessage(CField.dropItemFromMapObject(drop, dropper.getTruePosition(), droppos, (byte) 3), drop.getTruePosition());
    }

    public final void spawnMesoDrop(int meso, Point position, final MapleMapObject dropper, MapleCharacter owner, boolean playerDrop, byte droptype) {
        final Point droppos = calcDropPos(position, position);
        final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, droptype, playerDrop);

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            public void sendPackets(MapleClient c) {
                c.getSession().write(CField.dropItemFromMapObject(mdrop, dropper.getTruePosition(), droppos, (byte) 1));
            }
        });
        if (!this.everlast) {
            mdrop.registerExpire(120000L);
            if ((droptype == 0) || (droptype == 1)) {
                mdrop.registerFFA(30000L);
            }
        }
    }

    public final void spawnMobMesoDrop(int meso, final Point position, final MapleMapObject dropper, MapleCharacter owner, boolean playerDrop, byte droptype) {
        final MapleMapItem mdrop = new MapleMapItem(meso, position, dropper, owner, droptype, playerDrop);

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            public void sendPackets(MapleClient c) {
                c.getSession().write(CField.dropItemFromMapObject(mdrop, dropper.getTruePosition(), position, (byte) 1));
            }
        });
        mdrop.registerExpire(120000L);
        if ((droptype == 0) || (droptype == 1)) {
            mdrop.registerFFA(30000L);
        }
    }

    public final void spawnMobDrop(final Item idrop, final Point dropPos, final MapleMonster mob, MapleCharacter chr, byte droptype, final int questid) {
        final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, mob, chr, droptype, false, questid);

        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            public void sendPackets(MapleClient c) {
                if ((c != null) && (c.getPlayer() != null) && ((questid <= 0) || (c.getPlayer().getQuestStatus(questid) == 1)) && ((idrop.getItemId() / 10000 != 238) || (c.getPlayer().getMonsterBook().getLevelByCard(idrop.getItemId()) >= 2)) && (mob != null) && (dropPos != null)) {
                    c.getSession().write(CField.dropItemFromMapObject(mdrop, mob.getTruePosition(), dropPos, (byte) 1));
                }
            }
        });
        mdrop.registerExpire(120000L);
        if ((droptype == 0) || (droptype == 1)) {
            mdrop.registerFFA(30000L);
        }
        activateItemReactors(mdrop, chr.getClient());
    }

    public final void spawnRandDrop() {
        if (mapid != 910000000 || channel != 1) {
            return; //fm, ch1
        }

        mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().lock();
        try {
            for (MapleMapObject o : mapobjects.get(MapleMapObjectType.ITEM).values()) {
                if (((MapleMapItem) o).isRandDrop()) {
                    return;
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().unlock();
        }
        MapTimer.getInstance().schedule(new Runnable() {
            public void run() {
                final Point pos = new Point(Randomizer.nextInt(800) + 531, -806);
                final int theItem = Randomizer.nextInt(1000);
                int itemid = 0;
                if (theItem < 950) { //0-949 = normal, 950-989 = rare, 990-999 = super
                    itemid = GameConstants.normalDrops[Randomizer.nextInt(GameConstants.normalDrops.length)];
                } else if (theItem < 990) {
                    itemid = GameConstants.rareDrops[Randomizer.nextInt(GameConstants.rareDrops.length)];
                } else {
                    itemid = GameConstants.superDrops[Randomizer.nextInt(GameConstants.superDrops.length)];
                }
                spawnAutoDrop(itemid, pos);
            }
        }, 20000);
    }

    public final void spawnAutoDrop(final int itemid, final Point pos) {
        Item idrop = null;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (GameConstants.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
            idrop = ii.randomizeStats((Equip) ii.getEquipById(itemid));
        } else {
            idrop = new Item(itemid, (byte) 0, (short) 1, (byte) 0);
        }
        idrop.setGMLog("Dropped from auto " + " on " + mapid);
        final MapleMapItem mdrop = new MapleMapItem(pos, idrop);
        spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            @Override
            public void sendPackets(MapleClient c) {
                c.getSession().write(CField.dropItemFromMapObject(mdrop, pos, pos, (byte) 1));
            }
        });
        broadcastMessage(CField.dropItemFromMapObject(mdrop, pos, pos, (byte) 0));
        if (itemid / 10000 != 291) {
            mdrop.registerExpire(120000);
        }
    }

    public final void spawnItemDrop(final MapleMapObject dropper, MapleCharacter owner, Item item, Point pos, boolean ffaDrop, boolean playerDrop) {
        final Point droppos = calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte) 2, playerDrop);

        spawnAndAddRangedMapObject(drop, new DelayedPacketCreation() {
            public void sendPackets(MapleClient c) {
                c.getSession().write(CField.dropItemFromMapObject(drop, dropper.getTruePosition(), droppos, (byte) 1));
            }
        });
        broadcastMessage(CField.dropItemFromMapObject(drop, dropper.getTruePosition(), droppos, (byte) 0));

        if (!this.everlast) {
            drop.registerExpire(120000L);
            activateItemReactors(drop, owner.getClient());
        }
    }

    private void activateItemReactors(final MapleMapItem drop, final MapleClient c) {
        final Item item = drop.getItem();

        mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().lock();
        try {
            for (final MapleMapObject o : mapobjects.get(MapleMapObjectType.REACTOR).values()) {
                final MapleReactor react = (MapleReactor) o;

                if (react.getReactorType() == 100) {
                    if (item.getItemId() == GameConstants.getCustomReactItem(react.getReactorId(), react.getReactItem().getLeft()) && react.getReactItem().getRight() == item.getQuantity()) {
                        if (react.getArea().contains(drop.getTruePosition())) {
                            if (!react.isTimerActive()) {
                                MapTimer.getInstance().schedule(new ActivateItemReactor(drop, react, c), 5000);
                                react.setTimerActive(true);
                                break;
                            }
                        }
                    }
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.REACTOR).readLock().unlock();
        }
    }

    public int getItemsSize() {
        return ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.ITEM)).size();
    }

    public int getExtractorSize() {
        return ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.EXTRACTOR)).size();
    }

    public int getMobsSize() {
        return ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.MONSTER)).size();
    }

    public List<MapleMapItem> getAllItems() {
        return getAllItemsThreadsafe();
    }

    public List<MapleMapItem> getAllItemsThreadsafe() {
        ArrayList<MapleMapItem> ret = new ArrayList<MapleMapItem>();
        mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.ITEM).values()) {
                ret.add((MapleMapItem) mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().unlock();
        }
        return ret;
    }

    public Point getPointOfItem(int itemid) {
        mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.ITEM).values()) {
                MapleMapItem mm = ((MapleMapItem) mmo);
                if (mm.getItem() != null && mm.getItem().getItemId() == itemid) {
                    return mm.getPosition();
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().unlock();
        }
        return null;
    }

    public List<MapleMist> getAllMistsThreadsafe() {
        ArrayList<MapleMist> ret = new ArrayList<MapleMist>();
        mapobjectlocks.get(MapleMapObjectType.MIST).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.MIST).values()) {
                ret.add((MapleMist) mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.MIST).readLock().unlock();
        }
        return ret;
    }

    public final void returnEverLastItem(final MapleCharacter chr) {
        for (final MapleMapObject o : getAllItemsThreadsafe()) {
            final MapleMapItem item = ((MapleMapItem) o);
            if (item.getOwner() == chr.getId()) {
                item.setPickedUp(true);
                broadcastMessage(CField.removeItemFromMap(item.getObjectId(), 2, chr.getId()), item.getTruePosition());
                if (item.getMeso() > 0) {
                    chr.gainMeso(item.getMeso(), false);
                } else {
                    MapleInventoryManipulator.addFromDrop(chr.getClient(), item.getItem(), false);
                }
                removeMapObject(item);
            }
        }
        spawnRandDrop();
    }

    public final void talkMonster(String msg, int itemId, int objectid) {
        if (itemId > 0) {
            startMapEffect(msg, itemId, false);
        }
        broadcastMessage(MobPacket.talkMonster(objectid, itemId, msg));
        broadcastMessage(MobPacket.removeTalkMonster(objectid));
    }

    public final void startMapEffect(String msg, int itemId) {
        startMapEffect(msg, itemId, false);
    }

    public final void startMapEffect(String msg, int itemId, boolean jukebox) {
        if (this.mapEffect != null) {
            return;
        }
        this.mapEffect = new MapleMapEffect(msg, itemId);
        this.mapEffect.setJukebox(jukebox);
        broadcastMessage(this.mapEffect.makeStartData());
        Timer.MapTimer.getInstance().schedule(new Runnable() {
            public void run() {
                if (MapleMap.this.mapEffect != null) {
                    MapleMap.this.broadcastMessage(MapleMap.this.mapEffect.makeDestroyData());
                    MapleMap.this.mapEffect = null;
                }
            }
        }, jukebox ? 300000L : 30000L);
    }

    public final void startExtendedMapEffect(final String msg, final int itemId) {
        broadcastMessage(CField.startMapEffect(msg, itemId, true));
        Timer.MapTimer.getInstance().schedule(new Runnable() {
            public void run() {
                MapleMap.this.broadcastMessage(CField.removeMapEffect());
                MapleMap.this.broadcastMessage(CField.startMapEffect(msg, itemId, false));
            }
        }, 60000L);
    }

    public final void startSimpleMapEffect(String msg, int itemId) {
        broadcastMessage(CField.startMapEffect(msg, itemId, true));
    }

    public final void startJukebox(String msg, int itemId) {
        startMapEffect(msg, itemId, true);
    }

    public final void addPlayer(MapleCharacter chr) {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.PLAYER)).writeLock().lock();
        try {
            ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.PLAYER)).put(Integer.valueOf(chr.getObjectId()), chr);
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.PLAYER)).writeLock().unlock();
        }

        this.charactersLock.writeLock().lock();
        try {
            this.characters.add(chr);
        } finally {
            this.charactersLock.writeLock().unlock();
        }
        chr.setChangeTime();
        if ((GameConstants.isTeamMap(this.mapid)) && (!chr.inPVP())) {
            chr.setTeam(getAndSwitchTeam() ? 0 : 1);
        }
        byte[] packet = CField.spawnPlayerMapobject(chr);
        if (!chr.isHidden()) {
            broadcastMessage(chr, packet, false);
            if ((chr.isIntern()) && (this.speedRunStart > 0L)) {
                endSpeedRun();
                broadcastMessage(CWvsContext.serverNotice(5, "The speed run has ended."));
            }
        } else {
            broadcastGMMessage(chr, packet, false);
        }

        if (!chr.isClone()) {
            if ((!this.onFirstUserEnter.equals(""))
                    && (getCharactersSize() == 1)) {
                MapScriptMethods.startScript_FirstUser(chr.getClient(), this.onFirstUserEnter);
            }

            sendObjectPlacement(chr);

            chr.getClient().getSession().write(packet);

            if (!this.onUserEnter.equals("")) {
                MapScriptMethods.startScript_User(chr.getClient(), this.onUserEnter);
            }
            GameConstants.achievementRatio(chr.getClient());
            chr.getClient().getSession().write(CField.spawnFlags(this.nodes.getFlags()));
            if ((GameConstants.isTeamMap(this.mapid)) && (!chr.inPVP())) {
                chr.getClient().getSession().write(CField.showEquipEffect(chr.getTeam()));
            }
            switch (this.mapid) {
                case 809000101:
                case 809000201:
                    chr.getClient().getSession().write(CField.showEquipEffect());
                    break;
                case 689000000:
                case 689000010:
                    chr.getClient().getSession().write(CField.getCaptureFlags(this));
            }
        }

        for (MaplePet pet : chr.getPets()) {
            if (pet.getSummoned()) {
                broadcastMessage(chr, PetPacket.showPet(chr, pet, false, false), false);
            }
        }
        if (chr.getSummonedFamiliar() != null) {
            chr.spawnFamiliar(chr.getSummonedFamiliar(), true);
        }
        if (chr.getAndroid() != null) {
            chr.getAndroid().setPos(chr.getPosition());
            broadcastMessage(CField.spawnAndroid(chr, chr.getAndroid()));
        }
        if ((chr.getParty() != null) && (!chr.isClone())) {
            chr.silentPartyUpdate();
            chr.getClient().getSession().write(CWvsContext.PartyPacket.updateParty(chr.getClient().getChannel(), chr.getParty(), PartyOperation.SILENT_UPDATE, null));
            chr.updatePartyMemberHP();
            chr.receivePartyMemberHP();
        }
        if (chr.getDamage() == 696969) {
            chr.startAutoLooter();
        }
        if ((!chr.isInBlockedMap()) && (chr.getLevel() > 10)) {
            chr.getClient().getSession().write(CField.getPublicNPCInfo());
        }
        if (GameConstants.isPhantom(chr.getJob())) {
            chr.getClient().getSession().write(CField.updateCardStack(chr.getCardStack()));
        }
        if (!chr.isClone()) {
            List<MapleSummon> ss = chr.getSummonsReadLock();
            try {
                for (MapleSummon summon : ss) {
                    summon.setPosition(chr.getTruePosition());
                    chr.addVisibleMapObject(summon);
                    spawnSummon(summon);
                }
            } finally {
                chr.unlockSummonsReadLock();
            }
        }
        if (this.mapEffect != null) {
            this.mapEffect.sendStartData(chr.getClient());
        }
        if (timeLimit > 0 && getForcedReturnMap() != null) {
            chr.startMapTimeLimitTask(timeLimit, getForcedReturnMap());
        }
        if ((chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) && (!GameConstants.isResist(chr.getJob()))
                && (FieldLimitType.Mount.check(this.fieldLimit))) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        }

        if (!chr.isClone()) {
            if ((chr.getEventInstance() != null) && (chr.getEventInstance().isTimerStarted()) && (!chr.isClone())) {
                if (chr.inPVP()) {
                    chr.getClient().getSession().write(CField.getPVPClock(Integer.parseInt(chr.getEventInstance().getProperty("type")), (int) (chr.getEventInstance().getTimeLeft() / 1000L)));
                } else {
                    chr.getClient().getSession().write(CField.getClock((int) (chr.getEventInstance().getTimeLeft() / 1000L)));
                }
            }
            if (hasClock()) {
                Calendar cal = Calendar.getInstance();
                chr.getClient().getSession().write(CField.getClockTime(cal.get(11), cal.get(12), cal.get(13)));
            }
            if ((chr.getCarnivalParty() != null) && (chr.getEventInstance() != null)) {
                chr.getEventInstance().onMapLoad(chr);
            }
            if ((getSquadBegin() != null) && (getSquadBegin().getTimeLeft() > 0L) && (getSquadBegin().getStatus() == 1)) {
                chr.getClient().getSession().write(CField.getClock((int) (getSquadBegin().getTimeLeft() / 1000L)));
            }
            if ((this.mapid / 1000 != 105100) && (this.mapid / 100 != 8020003) && (this.mapid / 100 != 8020008) && (this.mapid != 271040100)) {
                MapleSquad sqd = getSquadByMap();
                EventManager em = getEMByMap();
                if ((!this.squadTimer) && (sqd != null) && (chr.getName().equals(sqd.getLeaderName())) && (em != null) && (em.getProperty("leader") != null) && (em.getProperty("leader").equals("true")) && (this.checkStates)) {
                    doShrine(false);
                    this.squadTimer = true;
                }
            }
            if ((getNumMonsters() > 0) && ((this.mapid == 280030001) || (this.mapid == 240060201) || (this.mapid == 280030000) || (this.mapid == 240060200) || (this.mapid == 220080001) || (this.mapid == 541020800) || (this.mapid == 541010100))) {
                String music = "Bgm09/TimeAttack";
                switch (this.mapid) {
                    case 240060200:
                    case 240060201:
                        music = "Bgm14/HonTale";
                        break;
                    case 280030000:
                    case 280030001:
                        music = "Bgm06/FinalFight";
                }

                chr.getClient().getSession().write(CField.musicChange(music));
            }

            for (WeakReference chrz : chr.getClones()) {
                if (chrz.get() != null) {
                    ((MapleCharacter) chrz.get()).setPosition(chr.getTruePosition());
                    ((MapleCharacter) chrz.get()).setMap(this);
                    addPlayer((MapleCharacter) chrz.get());
                }
            }
            if ((this.mapid == 914000000) || (this.mapid == 927000000)) {
                chr.getClient().getSession().write(CWvsContext.temporaryStats_Aran());
            } else if ((this.mapid == 105100300) && (chr.getLevel() >= 91)) {
                chr.getClient().getSession().write(CWvsContext.temporaryStats_Balrog(chr));
            } else if ((this.mapid == 140090000) || (this.mapid == 105100301) || (this.mapid == 105100401) || (this.mapid == 105100100)) {
                chr.getClient().getSession().write(CWvsContext.temporaryStats_Reset());
            }
        }
        if ((GameConstants.isEvan(chr.getJob())) && (chr.getJob() >= 2200)) {
            if (chr.getDragon() == null) {
                chr.makeDragon();
            } else {
                chr.getDragon().setPosition(chr.getPosition());
            }
            if (chr.getDragon() != null) {
                broadcastMessage(CField.spawnDragon(chr.getDragon()));
            }
        }
        if (GameConstants.kanna(chr.getJob())) {
            if (chr.getHaku() == null && chr.getBuffedValue(MapleBuffStat.Haku_Reborn) == null) {
                chr.makeHaku();
            } else {
                chr.getHaku().setPosition(chr.getPosition());
            }
            if (chr.getHaku() != null) {
                broadcastMessage(CField.spawnHaku(chr.getHaku()));
                if (chr.getHaku() != null && chr.getBuffedValue(MapleBuffStat.Haku_Reborn) != null) {
                    chr.getHaku().sendstats();
                    chr.getMap().broadcastMessage(chr, CField.spawnHaku_change0(chr.getId()), true);
                    chr.getMap().broadcastMessage(chr, CField.spawnHaku_change1(chr.getHaku()), true);
                    chr.getMap().broadcastMessage(chr, CField.spawnHaku_bianshen(chr.getId(), chr.getHaku().getObjectId(), chr.getHaku().getstats()), true);
                }
            }
        }
        if (this.mapid == 103050900) {
            try {
                chr.getClient().getSession().write(CField.UIPacket.IntroEnableUI(1));
                chr.getClient().getSession().write(CField.UIPacket.getDirectionInfo(1, 8000));
                chr.getClient().getSession().write(CField.UIPacket.getDirectionInfo(1, 8000));
                chr.getClient().getSession().write(CField.UIPacket.getDirectionInfo(3, 2));
                chr.dropMessage(-1, "On A Rainy Day");
                chr.dropMessage(-1, "The Secret Garden Depths");
                chr.getClient().removeClickedNPC();
                Thread.sleep(11000L);
            } catch (InterruptedException e) {
            }
            chr.getClient().getSession().write(CField.UIPacket.getDirectionStatus(false));
            chr.getClient().getSession().write(CField.UIPacket.IntroEnableUI(0));
            chr.getClient().removeClickedNPC();
            chr.dropMessage(-1, "Click on ryden to get your first quest");
        }
        if (this.permanentWeather > 0) {
            chr.getClient().getSession().write(CField.startMapEffect("", this.permanentWeather, false));
        }
        if (getPlatforms().size() > 0) {
            chr.getClient().getSession().write(CField.getMovingPlatforms(this));
        }
        if (this.environment.size() > 0) {
            chr.getClient().getSession().write(CField.getUpdateEnvironment(this));
        }

        if (isTown()) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.RAINING_MINES);
        }
        if (!canSoar()) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.SOARING);
        }
        if ((chr.getJob() < 3200) || (chr.getJob() > 3212)) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.AURA);
        }
        chr.getClient().getSession().write(CField.NPCPacket.setNPCScriptable(GameConstants.SCRIPTABLE_NPCS));
    }

    public int getNumItems() {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.ITEM)).readLock().lock();
        try {
            return ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.ITEM)).size();
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.ITEM)).readLock().unlock();
        }
    }

    public int getNumMonsters() {
        ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().lock();
        try {
            return ((LinkedHashMap) this.mapobjects.get(MapleMapObjectType.MONSTER)).size();
        } finally {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(MapleMapObjectType.MONSTER)).readLock().unlock();
        }
    }

    public void doShrine(final boolean spawned) {
        if (this.squadSchedule != null) {
            cancelSquadSchedule(true);
        }
        MapleSquad sqd = getSquadByMap();
        if (sqd == null) {
            return;
        }
        final int mode = (this.mapid == 240060200) || (this.mapid == 240060201) ? 3 : this.mapid == 280030001 ? 2 : this.mapid == 280030000 ? 1 : 0;

        EventManager em = getEMByMap();
        if ((sqd != null) && (em != null) && (getCharactersSize() > 0)) {
            final String leaderName = sqd.getLeaderName();
            final String state = em.getProperty("state");

            MapleMap returnMapa = getForcedReturnMap();
            if ((returnMapa == null) || (returnMapa.getId() == this.mapid)) {
                returnMapa = getReturnMap();
            }
            if ((mode == 1) || (mode == 2)) {
                broadcastMessage(CField.showChaosZakumShrine(spawned, 5));
            } else if (mode == 3) {
                broadcastMessage(CField.showChaosHorntailShrine(spawned, 5));
            } else {
                broadcastMessage(CField.showHorntailShrine(spawned, 5));
            }
            if (spawned) {
                broadcastMessage(CField.getClock(60));
            }
            final MapleMap returnMapz = returnMapa;
            Runnable run;
            if (!spawned) {
                final List<MapleMonster> monsterz = getAllMonstersThreadsafe();
                final List monsteridz = new ArrayList();
                for (MapleMapObject m : monsterz) {
                    monsteridz.add(Integer.valueOf(m.getObjectId()));
                }
                run = new Runnable() {
                    public void run() {
                        MapleSquad sqnow = MapleMap.this.getSquadByMap();
                        if ((MapleMap.this.getCharactersSize() > 0) && (MapleMap.this.getNumMonsters() == monsterz.size()) && (sqnow != null) && (sqnow.getStatus() == 2) && (sqnow.getLeaderName().equals(leaderName)) && (MapleMap.this.getEMByMap().getProperty("state").equals(state))) {
                            boolean passed = monsterz.isEmpty();
                            for (MapleMapObject m : MapleMap.this.getAllMonstersThreadsafe()) {
                                for (Iterator i$ = monsteridz.iterator(); i$.hasNext();) {
                                    int i = ((Integer) i$.next()).intValue();
                                    if (m.getObjectId() == i) {
                                        passed = true;
                                        break;
                                    }
                                }
                                if (passed) {
                                    break;
                                }
                            }
                            if (passed) {
                                byte[] packet;
                                if ((mode == 1) || (mode == 2)) {
                                    packet = CField.showChaosZakumShrine(spawned, 0);
                                } else {
                                    packet = CField.showHorntailShrine(spawned, 0);
                                }
                                for (MapleCharacter chr : MapleMap.this.getCharactersThreadsafe()) {
                                    chr.getClient().getSession().write(packet);
                                    chr.changeMap(returnMapz, returnMapz.getPortal(0));
                                }
                                MapleMap.this.checkStates("");
                                MapleMap.this.resetFully();
                            }
                        }
                    }
                };
            } else {
                run = new Runnable() {
                    public void run() {
                        MapleSquad sqnow = MapleMap.this.getSquadByMap();

                        if ((MapleMap.this.getCharactersSize() > 0) && (sqnow != null) && (sqnow.getStatus() == 2) && (sqnow.getLeaderName().equals(leaderName)) && (MapleMap.this.getEMByMap().getProperty("state").equals(state))) {
                            byte[] packet;
                            if ((mode == 1) || (mode == 2)) {
                                packet = CField.showChaosZakumShrine(spawned, 0);
                            } else {
                                packet = CField.showHorntailShrine(spawned, 0);
                            }
                            for (MapleCharacter chr : MapleMap.this.getCharactersThreadsafe()) {
                                chr.getClient().getSession().write(packet);
                                chr.changeMap(returnMapz, returnMapz.getPortal(0));
                            }
                            MapleMap.this.checkStates("");
                            MapleMap.this.resetFully();
                        }
                    }
                };
            }
            this.squadSchedule = Timer.MapTimer.getInstance().schedule(run, 300000L);
        }
    }

    public final MapleSquad getSquadByMap() {
        MapleSquadType zz = null;
        switch (mapid) {
            case 105100400:
            case 105100300:
                zz = MapleSquadType.bossbalrog;
                break;
            case 280030000:
                zz = MapleSquadType.zak;
                break;
            case 280030001:
                zz = MapleSquadType.chaoszak;
                break;
            case 240060200:
                zz = MapleSquadType.horntail;
                break;
            case 240060201:
                zz = MapleSquadType.chaosht;
                break;
            case 270050100:
                zz = MapleSquadType.pinkbean;
                break;
            case 802000111:
                zz = MapleSquadType.nmm_squad;
                break;
            case 802000211:
                zz = MapleSquadType.vergamot;
                break;
            case 802000311:
                zz = MapleSquadType.tokyo_2095;
                break;
            case 802000411:
                zz = MapleSquadType.dunas;
                break;
            case 802000611:
                zz = MapleSquadType.nibergen_squad;
                break;
            case 802000711:
                zz = MapleSquadType.dunas2;
                break;
            case 802000801:
            case 802000802:
            case 802000803:
                zz = MapleSquadType.core_blaze;
                break;
            case 802000821:
            case 802000823:
                zz = MapleSquadType.aufheben;
                break;
            case 211070100:
            case 211070101:
            case 211070110:
                zz = MapleSquadType.vonleon;
                break;
            case 551030200:
                zz = MapleSquadType.scartar;
                break;
            case 271040100:
                zz = MapleSquadType.cygnus;
                break;
            default:
                return null;
        }
        return ChannelServer.getInstance(channel).getMapleSquad(zz);
    }

    public final MapleSquad getSquadBegin() {
        if (this.squad != null) {
            return ChannelServer.getInstance(this.channel).getMapleSquad(this.squad);
        }
        return null;
    }

    public final EventManager getEMByMap() {
        String em = null;
        switch (this.mapid) {
            case 105100400:
                em = "BossBalrog_EASY";
                break;
            case 105100300:
                em = "BossBalrog_NORMAL";
                break;
            case 280030000:
                em = "ZakumBattle";
                break;
            case 240060200:
                em = "HorntailBattle";
                break;
            case 280030001:
                em = "ChaosZakum";
                break;
            case 240060201:
                em = "ChaosHorntail";
                break;
            case 270050100:
                em = "PinkBeanBattle";
                break;
            case 802000111:
                em = "NamelessMagicMonster";
                break;
            case 802000211:
                em = "Vergamot";
                break;
            case 802000311:
                em = "2095_tokyo";
                break;
            case 802000411:
                em = "Dunas";
                break;
            case 802000611:
                em = "Nibergen";
                break;
            case 802000711:
                em = "Dunas2";
                break;
            case 802000801:
            case 802000802:
            case 802000803:
                em = "CoreBlaze";
                break;
            case 802000821:
            case 802000823:
                em = "Aufhaven";
                break;
            case 211070100:
            case 211070101:
            case 211070110:
                em = "VonLeonBattle";
                break;
            case 551030200:
                em = "ScarTarBattle";
                break;
            case 271040100:
                em = "CygnusBattle";
                break;
            case 262031300:
                em = "HillaBattle";
                break;
            case 272020110:
            case 272030400:
                em = "ArkariumBattle";
                break;
            default:
                return null;
        }
        return ChannelServer.getInstance(this.channel).getEventSM().getEventManager(em);
    }

    public final void removePlayer(final MapleCharacter chr) {
        //log.warn("[dc] [level2] Player {} leaves map {}", new Object[] { chr.getName(), mapid });

        if (everlast) {
            returnEverLastItem(chr);
        }

        charactersLock.writeLock().lock();
        try {
            characters.remove(chr);
        } finally {
            charactersLock.writeLock().unlock();
        }
        removeMapObject(chr);
        chr.checkFollow();
        chr.removeExtractor();
        broadcastMessage(CField.removePlayerFromMap(chr.getId()));

        if (chr.getSummonedFamiliar() != null) {
            chr.removeVisibleFamiliar();
        }
        if (chr.getDamage() == 696969) {
            chr.stopAutoLooter();
        }
        List<MapleSummon> toCancel = new ArrayList<MapleSummon>();
        final List<MapleSummon> ss = chr.getSummonsReadLock();
        try {
            for (final MapleSummon summon : ss) {
                broadcastMessage(SummonPacket.removeSummon(summon, true));
                removeMapObject(summon);
                if (summon.getMovementType() == SummonMovementType.STATIONARY || summon.getMovementType() == SummonMovementType.CIRCLE_STATIONARY || summon.getMovementType() == SummonMovementType.WALK_STATIONARY) {
                    toCancel.add(summon);
                } else {
                    summon.setChangedMap(true);
                }
            }
        } finally {
            chr.unlockSummonsReadLock();
        }
        for (MapleSummon summon : toCancel) {
            chr.removeSummon(summon);
            chr.dispelSkill(summon.getSkill()); //remove the buff
        }
        if (!chr.isClone()) {
            checkStates(chr.getName());
            if (mapid == 109020001) {
                chr.canTalk(true);
            }
            for (final WeakReference<MapleCharacter> chrz : chr.getClones()) {
                if (chrz.get() != null) {
                    removePlayer(chrz.get());
                }
            }
            chr.leaveMap(this);
        }
    }

    public final void broadcastMessage(byte[] packet) {
        broadcastMessage(null, packet, (1.0D / 0.0D), null);
    }

    public final void broadcastMessage(MapleCharacter source, byte[] packet, boolean repeatToSource) {
        broadcastMessage(repeatToSource ? null : source, packet, (1.0D / 0.0D), source.getTruePosition());
    }

    public final void broadcastMessage(byte[] packet, Point rangedFrom) {
        broadcastMessage(null, packet, GameConstants.maxViewRangeSq(), rangedFrom);
    }

    public final void broadcastMessage(MapleCharacter source, byte[] packet, Point rangedFrom) {
        broadcastMessage(source, packet, GameConstants.maxViewRangeSq(), rangedFrom);
    }

    public void broadcastMessage(MapleCharacter source, byte[] packet, double rangeSq, Point rangedFrom) {
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter chr : this.characters) {
                if (chr != source) {
                    if (rangeSq < (1.0D / 0.0D)) {
                        if (rangedFrom.distanceSq(chr.getTruePosition()) <= rangeSq) {
                            chr.getClient().getSession().write(packet);
                        }
                    } else {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
    }

    private void sendObjectPlacement(MapleCharacter c) {
        if ((c == null) || (c.isClone())) {
            return;
        }
        for (MapleMapObject o : getMapObjectsInRange(c.getTruePosition(), c.getRange(), GameConstants.rangedMapobjectTypes)) {
            if ((o.getType() != MapleMapObjectType.REACTOR)
                    || (((MapleReactor) o).isAlive())) {
                o.sendSpawnData(c.getClient());
                c.addVisibleMapObject(o);
            }
        }
    }

    public final List<MaplePortal> getPortalsInRange(Point from, double rangeSq) {
        List ret = new ArrayList();
        for (MaplePortal type : this.portals.values()) {
            if ((from.distanceSq(type.getPosition()) <= rangeSq) && (type.getTargetMapId() != this.mapid) && (type.getTargetMapId() != 999999999)) {
                ret.add(type);
            }
        }
        return ret;
    }

    public final List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq) {
        List ret = new ArrayList();
        for (MapleMapObjectType type : MapleMapObjectType.values()) {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(type)).readLock().lock();
            try {
                Iterator itr = ((LinkedHashMap) this.mapobjects.get(type)).values().iterator();
                while (itr.hasNext()) {
                    MapleMapObject mmo = (MapleMapObject) itr.next();
                    if (from.distanceSq(mmo.getTruePosition()) <= rangeSq) {
                        ret.add(mmo);
                    }
                }
            } finally {
                ((ReentrantReadWriteLock) this.mapobjectlocks.get(type)).readLock().unlock();
            }
        }
        return ret;
    }

    public List<MapleMapObject> getItemsInRange(Point from, double rangeSq) {
        return getMapObjectsInRange(from, rangeSq, Arrays.asList(new MapleMapObjectType[]{MapleMapObjectType.ITEM}));
    }

    public final List<MapleMapObject> getMapObjectsInRange(Point from, double rangeSq, List<MapleMapObjectType> MapObject_types) {
        List ret = new ArrayList();
        for (MapleMapObjectType type : MapObject_types) {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(type)).readLock().lock();
            try {
                Iterator itr = ((LinkedHashMap) this.mapobjects.get(type)).values().iterator();
                while (itr.hasNext()) {
                    MapleMapObject mmo = (MapleMapObject) itr.next();
                    if (from.distanceSq(mmo.getTruePosition()) <= rangeSq) {
                        ret.add(mmo);
                    }
                }
            } finally {
                ((ReentrantReadWriteLock) this.mapobjectlocks.get(type)).readLock().unlock();
            }
        }
        return ret;
    }

    public final List<MapleMapObject> getMapObjectsInRect(Rectangle box, List<MapleMapObjectType> MapObject_types) {
        List ret = new ArrayList();
        for (MapleMapObjectType type : MapObject_types) {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(type)).readLock().lock();
            try {
                Iterator itr = ((LinkedHashMap) this.mapobjects.get(type)).values().iterator();
                while (itr.hasNext()) {
                    MapleMapObject mmo = (MapleMapObject) itr.next();
                    if (box.contains(mmo.getTruePosition())) {
                        ret.add(mmo);
                    }
                }
            } finally {
                ((ReentrantReadWriteLock) this.mapobjectlocks.get(type)).readLock().unlock();
            }
        }
        return ret;
    }

    public final List<MapleCharacter> getCharactersIntersect(Rectangle box) {
        List ret = new ArrayList();
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter chr : this.characters) {
                if (chr.getBounds().intersects(box)) {
                    ret.add(chr);
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return ret;
    }

    public final List<MapleCharacter> getPlayersInRectAndInList(Rectangle box, List<MapleCharacter> chrList) {
        List character = new LinkedList();

        this.charactersLock.readLock().lock();
        try {
            Iterator ltr = this.characters.iterator();

            while (ltr.hasNext()) {
                MapleCharacter a = (MapleCharacter) ltr.next();
                if ((chrList.contains(a)) && (box.contains(a.getTruePosition()))) {
                    character.add(a);
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return character;
    }

    public final void addPortal(MaplePortal myPortal) {
        this.portals.put(Integer.valueOf(myPortal.getId()), myPortal);
    }

    public final MaplePortal getPortal(String portalname) {
        for (MaplePortal port : this.portals.values()) {
            if (port.getName().equals(portalname)) {
                return port;
            }
        }
        return null;
    }

    public final MaplePortal getPortal(int portalid) {
        return (MaplePortal) this.portals.get(Integer.valueOf(portalid));
    }

    public final void resetPortals() {
        for (MaplePortal port : this.portals.values()) {
            port.setPortalState(true);
        }
    }

    public final void setFootholds(MapleFootholdTree footholds) {
        this.footholds = footholds;
    }

    public final MapleFootholdTree getFootholds() {
        return this.footholds;
    }

    public final int getNumSpawnPoints() {
        return this.monsterSpawn.size();
    }

    public final void loadMonsterRate(final boolean first) {
        final int spawnSize = monsterSpawn.size();
        if (spawnSize >= 20 || partyBonusRate > 0) {
            maxRegularSpawn = Math.round(spawnSize / monsterRate);
        } else {
            maxRegularSpawn = (int) Math.ceil(spawnSize * monsterRate);
        }
        if (fixedMob > 0) {
            maxRegularSpawn = fixedMob;
        } else if (maxRegularSpawn <= 2) {
            maxRegularSpawn = 2;
        } else if (maxRegularSpawn > spawnSize) {
            maxRegularSpawn = Math.max(10, spawnSize);
        }

        Collection<Spawns> newSpawn = new LinkedList<>();
        Collection<Spawns> newBossSpawn = new LinkedList<>();
        for (final Spawns s : monsterSpawn) {
            if (s.getCarnivalTeam() >= 2) {
                continue; // Remove carnival spawned mobs
            }
            if (s.getMonster().isBoss()) {
                newBossSpawn.add(s);
            } else {
                newSpawn.add(s);
            }
        }
        monsterSpawn.clear();
        monsterSpawn.addAll(newBossSpawn);
        monsterSpawn.addAll(newSpawn);

        if (first && spawnSize > 0) {
            lastSpawnTime = System.currentTimeMillis();
            if (GameConstants.isForceRespawn(mapid)) {
                createMobInterval = 15000;
            }
            respawn(false); // this should do the trick, we don't need to wait upon entering map
        }
    }

    public final SpawnPoint addMonsterSpawn(MapleMonster monster, int mobTime, byte carnivalTeam, String msg) {
        Point newpos = calcPointBelow(monster.getPosition());
        newpos.y -= 1;
        SpawnPoint sp = new SpawnPoint(monster, newpos, mobTime, carnivalTeam, msg);
        if (carnivalTeam > -1) {
            this.monsterSpawn.add(0, sp);
        } else {
            this.monsterSpawn.add(sp);
        }
        return sp;
    }

    public final void addAreaMonsterSpawn(MapleMonster monster, Point pos1, Point pos2, Point pos3, int mobTime, String msg, boolean shouldSpawn) {
        pos1 = calcPointBelow(pos1);
        pos2 = calcPointBelow(pos2);
        pos3 = calcPointBelow(pos3);
        if (pos1 != null) {
            pos1.y -= 1;
        }
        if (pos2 != null) {
            pos2.y -= 1;
        }
        if (pos3 != null) {
            pos3.y -= 1;
        }
        if ((pos1 == null) && (pos2 == null) && (pos3 == null)) {
            System.out.println(new StringBuilder().append("WARNING: mapid ").append(this.mapid).append(", monster ").append(monster.getId()).append(" could not be spawned.").toString());

            return;
        }
        if (pos1 != null) {
            if (pos2 == null) {
                pos2 = new Point(pos1);
            }
            if (pos3 == null) {
                pos3 = new Point(pos1);
            }
        } else if (pos2 != null) {
            if (pos1 == null) {
                pos1 = new Point(pos2);
            }
            if (pos3 == null) {
                pos3 = new Point(pos2);
            }
        } else if (pos3 != null) {
            if (pos1 == null) {
                pos1 = new Point(pos3);
            }
            if (pos2 == null) {
                pos2 = new Point(pos3);
            }
        }
        this.monsterSpawn.add(new SpawnPointAreaBoss(monster, pos1, pos2, pos3, mobTime, msg, shouldSpawn));
    }

    public final List<MapleCharacter> getCharacters() {
        return getCharactersThreadsafe();
    }

    public final List<MapleCharacter> getCharactersThreadsafe() {
        List chars = new ArrayList();

        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : this.characters) {
                chars.add(mc);
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return chars;
    }

    public final MapleCharacter getCharacterByName(String id) {
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : this.characters) {
                if (mc.getName().equalsIgnoreCase(id)) {
                    return mc;
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return null;
    }

    public final MapleCharacter getCharacterById_InMap(int id) {
        return getCharacterById(id);
    }

    public final MapleCharacter getCharacterById(int id) {
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : this.characters) {
                if (mc.getId() == id) {
                    return mc;
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return null;
    }

    public final void updateMapObjectVisibility(MapleCharacter chr, MapleMapObject mo) {
        if ((chr == null) || (chr.isClone())) {
            return;
        }
        if (!chr.isMapObjectVisible(mo)) {
            if ((mo.getType() == MapleMapObjectType.MIST) || (mo.getType() == MapleMapObjectType.EXTRACTOR) || (mo.getType() == MapleMapObjectType.SUMMON) || (mo.getType() == MapleMapObjectType.FAMILIAR) || ((mo instanceof MechDoor)) || (mo.getTruePosition().distanceSq(chr.getTruePosition()) <= mo.getRange())) {
                chr.addVisibleMapObject(mo);
                mo.sendSpawnData(chr.getClient());
            }
        } else if ((!(mo instanceof MechDoor)) && (mo.getType() != MapleMapObjectType.MIST) && (mo.getType() != MapleMapObjectType.EXTRACTOR) && (mo.getType() != MapleMapObjectType.SUMMON) && (mo.getType() != MapleMapObjectType.FAMILIAR) && (mo.getTruePosition().distanceSq(chr.getTruePosition()) > mo.getRange())) {
            chr.removeVisibleMapObject(mo);
            mo.sendDestroyData(chr.getClient());
        } else if ((mo.getType() == MapleMapObjectType.MONSTER)
                && (chr.getTruePosition().distanceSq(mo.getTruePosition()) <= GameConstants.maxViewRangeSq_Half())) {
            updateMonsterController((MapleMonster) mo);
        }
    }

    public void moveMonster(MapleMonster monster, Point reportedPos) {
        monster.setPosition(reportedPos);

        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter mc : this.characters) {
                updateMapObjectVisibility(mc, monster);
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
    }

    public void movePlayer(MapleCharacter player, Point newPosition) {
        player.setPosition(newPosition);
        if (!player.isClone()) {
            try {
                Collection<MapleMapObject> visibleObjects = player.getAndWriteLockVisibleMapObjects();
                ArrayList copy = new ArrayList(visibleObjects);
                Iterator itr = copy.iterator();
                while (itr.hasNext()) {
                    MapleMapObject mo = (MapleMapObject) itr.next();
                    if ((mo != null) && (getMapObject(mo.getObjectId(), mo.getType()) == mo)) {
                        updateMapObjectVisibility(player, mo);
                    } else if (mo != null) {
                        visibleObjects.remove(mo);
                    }
                }
                for (MapleMapObject mo : getMapObjectsInRange(player.getTruePosition(), player.getRange())) {
                    if ((mo != null) && (!visibleObjects.contains(mo))) {
                        mo.sendSpawnData(player.getClient());
                        visibleObjects.add(mo);
                    }
                }
            } finally {
                Collection visibleObjects;
                player.unlockWriteVisibleMapObjects();
            }
        }
    }

    public MaplePortal findClosestSpawnpoint(Point from) {
        MaplePortal closest = getPortal(0);
        double shortestDistance = (1.0D / 0.0D);
        for (MaplePortal portal : this.portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if ((portal.getType() >= 0) && (portal.getType() <= 2) && (distance < shortestDistance) && (portal.getTargetMapId() == 999999999)) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public MaplePortal findClosestPortal(Point from) {
        MaplePortal closest = getPortal(0);
        double shortestDistance = (1.0D / 0.0D);
        for (MaplePortal portal : this.portals.values()) {
            double distance = portal.getPosition().distanceSq(from);
            if (distance < shortestDistance) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }

    public String spawnDebug() {
        StringBuilder sb = new StringBuilder("Mobs in map : ");
        sb.append(getMobsSize());
        sb.append(" spawnedMonstersOnMap: ");
        sb.append(this.spawnedMonstersOnMap);
        sb.append(" spawnpoints: ");
        sb.append(this.monsterSpawn.size());
        sb.append(" maxRegularSpawn: ");
        sb.append(this.maxRegularSpawn);
        sb.append(" actual monsters: ");
        sb.append(getNumMonsters());
        sb.append(" monster rate: ");
        sb.append(this.monsterRate);
        sb.append(" fixed: ");
        sb.append(this.fixedMob);

        return sb.toString();
    }

    public int characterSize() {
        return this.characters.size();
    }

    public final int getMapObjectSize() {
        return this.mapobjects.size() + getCharactersSize() - this.characters.size();
    }

    public final int getCharactersSize() {
        int ret = 0;
        this.charactersLock.readLock().lock();
        try {
            Iterator ltr = this.characters.iterator();

            while (ltr.hasNext()) {
                MapleCharacter chr = (MapleCharacter) ltr.next();
                if (!chr.isClone()) {
                    ret++;
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return ret;
    }

    public Collection<MaplePortal> getPortals() {
        return Collections.unmodifiableCollection(this.portals.values());
    }

    public int getSpawnedMonstersOnMap() {
        return this.spawnedMonstersOnMap.get();
    }

    public void spawnMonsterOnGroudBelow(MapleMonster mob, Point pos) {
        spawnMonsterOnGroundBelow(mob, pos);
    }

    public void respawn(boolean force) {
        respawn(force, System.currentTimeMillis());
    }

    public void respawn(boolean force, long now) {
        this.lastSpawnTime = now;
        int numShouldSpawn;
        int spawned;
        if (force) {
            numShouldSpawn = this.monsterSpawn.size() - this.spawnedMonstersOnMap.get();

            if (numShouldSpawn > 0) {
                spawned = 0;

                for (Spawns spawnPoint : this.monsterSpawn) {
                    spawnPoint.spawnMonster(this);
                    spawned++;
                    if (spawned >= numShouldSpawn) {
                        break;
                    }
                }
            }
        } else {
            numShouldSpawn = (GameConstants.isForceRespawn(this.mapid) ? this.monsterSpawn.size() : this.maxRegularSpawn) - this.spawnedMonstersOnMap.get();
            if (numShouldSpawn > 0) {
                spawned = 0;

                List<Spawns> randomSpawn = new ArrayList(this.monsterSpawn);
                Collections.shuffle(randomSpawn);

                for (Spawns spawnPoint : randomSpawn) {
                    if ((this.isSpawns) || (spawnPoint.getMobTime() <= 0)) {
                        if ((spawnPoint.shouldSpawn(this.lastSpawnTime)) || (GameConstants.isForceRespawn(this.mapid)) || ((this.monsterSpawn.size() < 10) && (this.maxRegularSpawn > this.monsterSpawn.size()) && (this.partyBonusRate > 0))) {
                            spawnPoint.spawnMonster(this);
                            spawned++;
                        }
                        if (spawned >= numShouldSpawn) {
                            break;
                        }
                    }
                }
            }
        }
    }

    public String getSnowballPortal() {
        int[] teamss = new int[2];
        this.charactersLock.readLock().lock();
        try {
            for (MapleCharacter chr : this.characters) {
                if (chr.getTruePosition().y > -80) {
                    teamss[0] += 1;
                } else {
                    teamss[1] += 1;
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        if (teamss[0] > teamss[1]) {
            return "st01";
        }
        return "st00";
    }

    public boolean isDisconnected(int id) {
        return this.dced.contains(Integer.valueOf(id));
    }

    public void addDisconnected(int id) {
        this.dced.add(Integer.valueOf(id));
    }

    public void resetDisconnected() {
        this.dced.clear();
    }

    public void startSpeedRun() {
        final MapleSquad squad = getSquadByMap();
        if (squad != null) {
            charactersLock.readLock().lock();
            try {
                for (MapleCharacter chr : characters) {
                    if (chr.getName().equals(squad.getLeaderName()) && !chr.isIntern()) {
                        startSpeedRun(chr.getName());
                        return;
                    }
                }
            } finally {
                charactersLock.readLock().unlock();
            }
        }
    }

    public void startSpeedRun(String leader) {
        this.speedRunStart = System.currentTimeMillis();
        this.speedRunLeader = leader;
    }

    public void endSpeedRun() {
        this.speedRunStart = 0L;
        this.speedRunLeader = "";
    }
    
        public void clearDrops(MapleCharacter player, boolean drops) {
        List<MapleMapObject> items = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM));
        for (MapleMapObject i : items) {
            player.getMap().removeMapObject(i);
            player.getMap().broadcastMessage(CField.removeItemFromMap(i.getObjectId(), 0, player.getId()));
        }
        if (drops) {//if drops = true
            player.dropMessage(6, "Cleared " + items.size() + " Drops");
        }
    }

    public void getRankAndAdd(String leader, String time, ExpeditionType type, long timz, Collection<String> squad) {
        try {
            long lastTime = SpeedRunner.getSpeedRunData(type) == null ? 0L : ((Long) SpeedRunner.getSpeedRunData(type).right).longValue();

            StringBuilder rett = new StringBuilder();
            if (squad != null) {
                for (String chr : squad) {
                    rett.append(chr);
                    rett.append(",");
                }
            }
            String z = rett.toString();
            if (squad != null) {
                z = z.substring(0, z.length() - 1);
            }
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO speedruns(`type`, `leader`, `timestring`, `time`, `members`) VALUES (?,?,?,?,?)");
            ps.setString(1, type.name());
            ps.setString(2, leader);
            ps.setString(3, time);
            ps.setLong(4, timz);
            ps.setString(5, z);
            ps.executeUpdate();
            ps.close();

            if (lastTime == 0L) {
                SpeedRunner.addSpeedRunData(type, SpeedRunner.addSpeedRunData(new StringBuilder(SpeedRunner.getPreamble(type)), new HashMap(), z, leader, 1, time), timz);
            } else {
                SpeedRunner.removeSpeedRunData(type);
                SpeedRunner.loadSpeedRunData(type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getSpeedRunStart() {
        return this.speedRunStart;
    }

    public final void disconnectAll() {
        for (MapleCharacter chr : getCharactersThreadsafe()) {
            if (!chr.isGM()) {
                chr.getClient().disconnect(true, false);
                chr.getClient().getSession().close(true);
            }
        }
    }

    public List<MapleNPC> getAllNPCs() {
        return getAllNPCsThreadsafe();
    }

    public List<MapleNPC> getAllNPCsThreadsafe() {
        ArrayList<MapleNPC> ret = new ArrayList<MapleNPC>();
        mapobjectlocks.get(MapleMapObjectType.NPC).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.NPC).values()) {
                ret.add((MapleNPC) mmo);
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.NPC).readLock().unlock();
        }
        return ret;
    }

    public final void resetNPCs() {
        removeNpc(-1);
    }

    public final void resetPQ(int level) {
        resetFully();
        for (MapleMonster mons : getAllMonstersThreadsafe()) {
            mons.changeLevel(level, true);
        }
        resetSpawnLevel(level);
    }

    public final void resetSpawnLevel(int level) {
        for (Spawns spawn : this.monsterSpawn) {
            if ((spawn instanceof SpawnPoint)) {
                ((SpawnPoint) spawn).setLevel(level);
            }
        }
    }

    public final void resetFully() {
        resetFully(true);
    }

    public final void resetFully(boolean respawn) {
        killAllMonsters(false);
        reloadReactors();
        removeDrops();
        resetNPCs();
        resetSpawns();
        resetDisconnected();
        endSpeedRun();
        cancelSquadSchedule(true);
        resetPortals();
        this.environment.clear();
        if (respawn) {
            respawn(true);
        }
    }

    public final void cancelSquadSchedule(boolean interrupt) {
        this.squadTimer = false;
        this.checkStates = true;
        if (this.squadSchedule != null) {
            this.squadSchedule.cancel(interrupt);
            this.squadSchedule = null;
        }
    }

    public final void removeDrops() {
        final List<MapleMapItem> items = getAllItemsThreadsafe();
        for (MapleMapItem i : items) {
            i.expire(this);
        }
    }

    public final void resetAllSpawnPoint(int mobid, int mobTime) {
        Collection<Spawns> sss = new LinkedList(this.monsterSpawn);
        resetFully();
        this.monsterSpawn.clear();
        for (Spawns s : sss) {
            MapleMonster newMons = MapleLifeFactory.getMonster(mobid);
            newMons.setF(s.getF());
            newMons.setFh(s.getFh());
            newMons.setPosition(s.getPosition());
            addMonsterSpawn(newMons, mobTime, (byte) -1, null);
        }
        loadMonsterRate(true);
    }

    public final void resetSpawns() {
        boolean changed = false;
        Iterator sss = this.monsterSpawn.iterator();
        while (sss.hasNext()) {
            if (((Spawns) sss.next()).getCarnivalId() > -1) {
                sss.remove();
                changed = true;
            }
        }
        setSpawns(true);
        if (changed) {
            loadMonsterRate(true);
        }
    }

    public final boolean makeCarnivalSpawn(int team, MapleMonster newMons, int num) {
        MapleNodes.MonsterPoint ret = null;
        for (MapleNodes.MonsterPoint mp : this.nodes.getMonsterPoints()) {
            if ((mp.team == team) || (mp.team == -1)) {
                Point newpos = calcPointBelow(new Point(mp.x, mp.y));
                newpos.y -= 1;
                boolean found = false;
                for (Spawns s : this.monsterSpawn) {
                    if ((s.getCarnivalId() > -1) && ((mp.team == -1) || (s.getCarnivalTeam() == mp.team)) && (s.getPosition().x == newpos.x) && (s.getPosition().y == newpos.y)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ret = mp;
                    break;
                }
            }
        }
        if (ret != null) {
            newMons.setCy(ret.cy);
            newMons.setF(0);
            newMons.setFh(ret.fh);
            newMons.setRx0(ret.x + 50);
            newMons.setRx1(ret.x - 50);
            newMons.setPosition(new Point(ret.x, ret.y));
            newMons.setHide(false);
            SpawnPoint sp = addMonsterSpawn(newMons, 1, (byte) team, null);
            sp.setCarnival(num);
        }
        return ret != null;
    }

    public final boolean makeCarnivalReactor(int team, int num) {
        MapleReactor old = getReactorByName(new StringBuilder().append(team).append("").append(num).toString());
        if ((old != null) && (old.getState() < 5)) {
            return false;
        }
        Point guardz = null;
        List<MapleReactor> react = getAllReactorsThreadsafe();
        for (Pair guard : this.nodes.getGuardians()) {
            if ((((Integer) guard.right).intValue() == team) || (((Integer) guard.right).intValue() == -1)) {
                boolean found = false;
                for (MapleReactor r : react) {
                    if ((r.getTruePosition().x == ((Point) guard.left).x) && (r.getTruePosition().y == ((Point) guard.left).y) && (r.getState() < 5)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    guardz = (Point) guard.left;
                    break;
                }
            }
        }
        MapleCarnivalFactory.MCSkill skil;
        if (guardz != null) {
            MapleReactor my = new MapleReactor(MapleReactorFactory.getReactor(9980000 + team), 9980000 + team);
            my.setState((byte) 1);
            my.setName(new StringBuilder().append(team).append("").append(num).toString());

            spawnReactorOnGroundBelow(my, guardz);
            skil = MapleCarnivalFactory.getInstance().getGuardian(num);
            for (MapleMonster mons : getAllMonstersThreadsafe()) {
                if (mons.getCarnivalTeam() == team) {
                    skil.getSkill().applyEffect(null, mons, false);
                }
            }
        }
        return guardz != null;
    }

    public final void blockAllPortal() {
        for (MaplePortal p : this.portals.values()) {
            p.setPortalState(false);
        }
    }

    public boolean getAndSwitchTeam() {
        return getCharactersSize() % 2 != 0;
    }

    public void setSquad(MapleSquad.MapleSquadType s) {
        this.squad = s;
    }

    public int getChannel() {
        return this.channel;
    }

    public int getConsumeItemCoolTime() {
        return this.consumeItemCoolTime;
    }

    public void setConsumeItemCoolTime(int ciit) {
        this.consumeItemCoolTime = ciit;
    }

    public void setPermanentWeather(int pw) {
        this.permanentWeather = pw;
    }

    public int getPermanentWeather() {
        return this.permanentWeather;
    }

    public void checkStates(String chr) {
        if (!this.checkStates) {
            return;
        }
        MapleSquad sqd = getSquadByMap();
        EventManager em = getEMByMap();
        int size = getCharactersSize();
        if ((sqd != null) && (sqd.getStatus() == 2)) {
            sqd.removeMember(chr);
            if (em != null) {
                if (sqd.getLeaderName().equalsIgnoreCase(chr)) {
                    em.setProperty("leader", "false");
                }
                if ((chr.equals("")) || (size == 0)) {
                    em.setProperty("state", "0");
                    em.setProperty("leader", "true");
                    cancelSquadSchedule(!chr.equals(""));
                    sqd.clear();
                    sqd.copy();
                }
            }
        }
        if ((em != null) && (em.getProperty("state") != null) && ((sqd == null) || (sqd.getStatus() == 2)) && (size == 0)) {
            em.setProperty("state", "0");
            if (em.getProperty("leader") != null) {
                em.setProperty("leader", "true");
            }
        }
        if ((this.speedRunStart > 0L) && (size == 0)) {
            endSpeedRun();
        }
    }

    public void setCheckStates(boolean b) {
        this.checkStates = b;
    }

    public void setNodes(MapleNodes mn) {
        this.nodes = mn;
    }

    public final List<MapleNodes.MaplePlatform> getPlatforms() {
        return this.nodes.getPlatforms();
    }

    public Collection<MapleNodes.MapleNodeInfo> getNodes() {
        return this.nodes.getNodes();
    }

    public MapleNodes.MapleNodeInfo getNode(int index) {
        return this.nodes.getNode(index);
    }

    public boolean isLastNode(int index) {
        return this.nodes.isLastNode(index);
    }

    public final List<Rectangle> getAreas() {
        return this.nodes.getAreas();
    }

    public final Rectangle getArea(int index) {
        return this.nodes.getArea(index);
    }

    public final void changeEnvironment(String ms, int type) {
        broadcastMessage(CField.environmentChange(ms, type));
    }

    public final void toggleEnvironment(String ms) {
        if (this.environment.containsKey(ms)) {
            moveEnvironment(ms, ((Integer) this.environment.get(ms)).intValue() == 1 ? 2 : 1);
        } else {
            moveEnvironment(ms, 1);
        }
    }

    public final void moveEnvironment(String ms, int type) {
        broadcastMessage(CField.environmentMove(ms, type));
        this.environment.put(ms, Integer.valueOf(type));
    }

    public final Map<String, Integer> getEnvironment() {
        return this.environment;
    }

    public final int getNumPlayersInArea(int index) {
        return getNumPlayersInRect(getArea(index));
    }

    public final int getNumPlayersInRect(Rectangle rect) {
        int ret = 0;

        this.charactersLock.readLock().lock();
        try {
            Iterator ltr = this.characters.iterator();

            while (ltr.hasNext()) {
                if (rect.contains(((MapleCharacter) ltr.next()).getTruePosition())) {
                    ret++;
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
        return ret;
    }

    public final int getNumPlayersItemsInArea(int index) {
        return getNumPlayersItemsInRect(getArea(index));
    }

    public final MapleCharacter getPlayer() {
        return this.c.getPlayer();
    }

    public final int getNumPlayersItemsInRect(final Rectangle rect) {
        int ret = getNumPlayersInRect(rect);

        mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().lock();
        try {
            for (MapleMapObject mmo : mapobjects.get(MapleMapObjectType.ITEM).values()) {
                if (rect.contains(mmo.getTruePosition())) {
                    ret++;
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().unlock();
        }
        return ret;
    }

    public void broadcastGMMessage(MapleCharacter source, byte[] packet, boolean repeatToSource) {
        broadcastGMMessage(repeatToSource ? null : source, packet);
    }

    private void broadcastGMMessage(MapleCharacter source, byte[] packet) {
        this.charactersLock.readLock().lock();
        try {
            if (source == null) {
                for (MapleCharacter chr : this.characters) {
                    if (chr.isStaff()) {
                        chr.getClient().getSession().write(packet);
                    }
                }
            } else {
                for (MapleCharacter chr : this.characters) {
                    if ((chr != source) && (chr.getGMLevel() >= source.getGMLevel())) {
                        chr.getClient().getSession().write(packet);
                    }
                }
            }
        } finally {
            this.charactersLock.readLock().unlock();
        }
    }

    public final List<Pair<Integer, Integer>> getMobsToSpawn() {
        return this.nodes.getMobsToSpawn();
    }

    public final List<Integer> getSkillIds() {
        return this.nodes.getSkillIds();
    }

    public final boolean canSpawn(long now) {
        return (this.lastSpawnTime > 0L) && (this.lastSpawnTime + this.createMobInterval < now);
    }

    public final boolean canHurt(long now) {
        if ((this.lastHurtTime > 0L) && (this.lastHurtTime + this.decHPInterval < now)) {
            this.lastHurtTime = now;
            return true;
        }
        return false;
    }

    public final void resetShammos(final MapleClient c) {
        killAllMonsters(true);
        broadcastMessage(CWvsContext.serverNotice(5, "A player has moved too far from Shammos. Shammos is going back to the start."));
        Timer.EtcTimer.getInstance().schedule(new Runnable() {
            public void run() {
                if (c.getPlayer() != null) {
                    c.getPlayer().changeMap(MapleMap.this, MapleMap.this.getPortal(0));
                    if (MapleMap.this.getCharactersThreadsafe().size() > 1) {
                        MapScriptMethods.startScript_FirstUser(c, "shammos_Fenter");
                    }
                }
            }
        }, 500L);
    }

    public int getInstanceId() {
        return this.instanceid;
    }

    public void setInstanceId(int ii) {
        this.instanceid = ii;
    }

    public int getPartyBonusRate() {
        return this.partyBonusRate;
    }

    public void setPartyBonusRate(int ii) {
        this.partyBonusRate = ii;
    }

    public short getTop() {
        return this.top;
    }

    public short getBottom() {
        return this.bottom;
    }

    public short getLeft() {
        return this.left;
    }

    public short getRight() {
        return this.right;
    }

    public void setTop(int ii) {
        this.top = ((short) ii);
    }

    public void setBottom(int ii) {
        this.bottom = ((short) ii);
    }

    public void setLeft(int ii) {
        this.left = ((short) ii);
    }

    public void setRight(int ii) {
        this.right = ((short) ii);
    }

    public final void setChangeableMobOrigin(MapleCharacter d) {
        this.changeMobOrigin = new WeakReference(d);
    }

    public final MapleCharacter getChangeableMobOrigin() {
        if (this.changeMobOrigin == null) {
            return null;
        }
        return (MapleCharacter) this.changeMobOrigin.get();
    }

    public List<Pair<Point, Integer>> getGuardians() {
        return this.nodes.getGuardians();
    }

    public MapleNodes.DirectionInfo getDirectionInfo(int i) {
        return this.nodes.getDirection(i);
    }

    public final void spawnTutorialDrop() {
        if (mapobjects.get(MapleMapObjectType.ITEM).size() >= 3) {
            return;
        }
        if (mapid != TutorialConstants.tutorialDropsMap) {
            return;
        }
        mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().lock();
        try {
            for (MapleMapObject o : mapobjects.get(MapleMapObjectType.ITEM).values()) {
                if (((MapleMapItem) o).isRandDrop()) { // this is to show that 1 item has already been dropped before
                    return;
                }
            }
        } finally {
            mapobjectlocks.get(MapleMapObjectType.ITEM).readLock().unlock();
        }

        final Point pos = new Point(TutorialConstants.dropPosX, TutorialConstants.dropPosY); // change to the position you want to		
        final int[] itemid = null;

        if (mapid == TutorialConstants.tutorialDropsMap) {
            if (itemid != null) {
                spawnAutoDrop(TutorialConstants.tutorialDrops[Randomizer.nextInt(TutorialConstants.tutorialDrops.length)], pos);
            }
            spawnAutoDrop(TutorialConstants.tutorialDrops[Randomizer.nextInt(TutorialConstants.tutorialDrops.length)], pos);
        }
    }

    public boolean getMuted() {
        return muted;
    }

    public void setMuted(boolean isMuted) {
        this.muted = isMuted;
    }

    public final MapleMapObject getClosestMapObjectInRange(Point from, double rangeSq, List<MapleMapObjectType> MapObject_types) {
        MapleMapObject ret = null;
        for (MapleMapObjectType type : MapObject_types) {
            ((ReentrantReadWriteLock) this.mapobjectlocks.get(type)).readLock().lock();
            try {
                Iterator itr = ((LinkedHashMap) this.mapobjects.get(type)).values().iterator();
                while (itr.hasNext()) {
                    MapleMapObject mmo = (MapleMapObject) itr.next();
                    if ((from.distanceSq(mmo.getTruePosition()) <= rangeSq) && ((ret == null) || (from.distanceSq(ret.getTruePosition()) > from.distanceSq(mmo.getTruePosition())))) {
                        ret = mmo;
                    }
                }
            } finally {
                ((ReentrantReadWriteLock) this.mapobjectlocks.get(type)).readLock().unlock();
            }
        }
        return ret;
    }

    private static abstract interface DelayedPacketCreation {

        public abstract void sendPackets(MapleClient paramMapleClient);
    }

    private class ActivateItemReactor
            implements Runnable {

        private MapleMapItem mapitem;
        private MapleReactor reactor;
        private MapleClient c;

        public ActivateItemReactor(MapleMapItem mapitem, MapleReactor reactor, MapleClient c) {
            this.mapitem = mapitem;
            this.reactor = reactor;
            this.c = c;
        }

        public void run() {
            if ((this.mapitem != null) && (this.mapitem == MapleMap.this.getMapObject(this.mapitem.getObjectId(), this.mapitem.getType())) && (!this.mapitem.isPickedUp())) {
                this.mapitem.expire(MapleMap.this);
                this.reactor.hitReactor(this.c);
                this.reactor.setTimerActive(false);

                if (this.reactor.getDelay() > 0) {
                    Timer.MapTimer.getInstance().schedule(new Runnable() {
                        public void run() {
                            MapleMap.ActivateItemReactor.this.reactor.forceHitReactor((byte) 0);
                        }
                    }, this.reactor.getDelay());
                }

            } else {
                this.reactor.setTimerActive(false);
            }
        }
    }
}