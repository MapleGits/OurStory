package clientside;

import clientside.MapleTrait.MapleTraitType;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.ItemLoader;
import client.inventory.MapleAndroid;
import client.inventory.MapleImp;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MapleInventoryType;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.MapConstants;
import constants.OccupationConstants;
import constants.ServerConstants;
import constants.ServerConstants.PlayerGMRank;
import database.DatabaseConnection;
import database.DatabaseException;
import handling.channel.ChannelServer;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.world.CharacterTransfer;
import handling.world.MapleCharacterLook;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.PlayerBuffStorage;
import handling.world.PlayerBuffValueHolder;
import handling.world.World;
import handling.world.family.MapleFamily;
import handling.world.family.MapleFamilyBuff;
import handling.world.family.MapleFamilyCharacter;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildCharacter;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import static org.omg.CORBA.AnySeqHelper.type;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import scripting.EventInstanceManager;
import scripting.EventManager;
import scripting.NPCScriptManager;
import server.CashShop;
import server.MapleCarnivalChallenge;
import server.MapleCarnivalParty;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleShop;
import server.MapleStatEffect;
import server.MapleStorage;
import server.MapleTrade;
import server.RandomRewards;
import server.Randomizer;
import server.Timer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterStats;
import server.life.MobSkill;
import server.life.MobSkillFactory;
import server.life.PlayerNPC;
import server.maps.AnimatedMapleMapObject;
import server.maps.Event_PyramidSubway;
import server.maps.FieldLimitType;
import server.maps.MapleDoor;
import server.maps.MapleDragon;
import server.maps.MapleExtractor;
import server.maps.MapleFoothold;
import server.maps.MapleHaku;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleSummon;
import server.maps.MechDoor;
import server.maps.SavedLocationType;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.shops.IMaplePlayerShop;
import tools.ConcurrentEnumMap;
import tools.FileoutputUtil;
import tools.MockIOSession;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.InfoPacket;
import tools.packet.JobPacket.AvengerPacket;
import tools.packet.MTSCSPacket;
import tools.packet.MobPacket;
import tools.packet.MonsterCarnivalPacket;
import tools.packet.PetPacket;
import tools.packet.PlayerShopPacket;
import tools.packet.JobPacket;

public class MapleCharacter extends AnimatedMapleMapObject
        implements Serializable, MapleCharacterLook {

    private static final long serialVersionUID = 845748950829L;
    private String name;
    private String chalktext;
    private String BlessOfFairy_Origin;
    private String BlessOfEmpress_Origin;
    private String teleportname;
    private long lastCombo, lastfametime, keydown_skill, nextConsume, pqStartTime, lastDragonBloodTime,
            lastBerserkTime, lastRecoveryTime, lastSummonTime, mapChangeTime, lastFishingTime, lastFairyTime,
            lastHPTime, lastMPTime, lastFamiliarEffectTime, lastExceedTime, lastDOTTime, meso;
    private AtomicLong exp = new AtomicLong();
    private byte gmLevel;
    private byte gender;
    private byte initialSpawnPoint;
    private byte skinColor;
    private byte guildrank = 5;
    private byte allianceRank = 5;
    private byte world;
    private byte fairyExp;
    private byte numClones;
    private byte subcategory;
    private byte cardStack;
    private byte runningStack;
    private byte runningbless = 0;
    private short level;
    private short job;
    private int msipoints;
    private short mulung_energy;
    private short combo;
    private short force;
    private short availableCP;
    private short fatigue;
    private short totalCP;
    private int hpApUsed;
    private short scrolledPosition;
    private short kaisercombo;
    private short xenoncombo = 0;
    private short xenonSurplus;
    private short exceed;
    private short exceedAttack = 0;
    private boolean muted;
    Calendar unmuteTime = null;
        private int accountid, id, hair, face, secondHair, secondFace, faceMarking, elf, mapid, fame, pvpExp, pvpPoints, totalWins, totalLosses,
            guildid = 0, fallcounter, maplepoints, acash, nxcredit, chair, itemEffect, points, vpoints, dpoints, epoints,
            rank = 1, rankMove = 0, jobRank = 1, jobRankMove = 0, marriageId, marriageItemId, dotHP,
            currentrep, totalrep, coconutteam, followid, battleshipHP, gachexp, challenge, guildContribution = 0,
            remainingAp, honourExp, honorLevel, runningLight, runningLightSlot, runningDark, runningDarkSlot, luminousState;
    private int demonMarking;
    private int totalvote;
    private int honourLevel;
    private int runninglight;
    private int runninglightslot;
    private int runningdark;
    private int runningdarkslot;
    private int timenow;
    private AutoLoot autolooter;
    private Point old;
    private MonsterFamiliar summonedFamiliar;
    private int[] wishlist;
    private int[] rocks;
    private int dgm, gml;
    private int[] savedLocations;
    private int[] regrocks;
    private int[] hyperrocks;
    private int[] remainingSp = new int[10];
    private int[] school = new int[4];
    private transient AtomicInteger inst;
    private transient AtomicInteger insd;
    private transient List<LifeMovementFragment> lastres;
    private List<Integer> lastmonthfameids;
    private List<Integer> lastmonthbattleids;
    private List<Integer> extendedSlots;
    private List<MapleDoor> doors;
    private List<MechDoor> mechDoors;
    private List<MaplePet> pets;
    private List<Item> rebuy;
    private MapleImp[] imps;
    private List<Pair<Integer, Boolean>> stolenSkills = new ArrayList();
    private transient WeakReference<MapleCharacter>[] clones;
    private transient Set<MapleMonster> controlled;
    private transient Set<MapleMapObject> visibleMapObjects;
    private transient ReentrantReadWriteLock visibleMapObjectsLock;
    private transient ReentrantReadWriteLock summonsLock;
    private transient ReentrantReadWriteLock controlledLock;
    private transient MapleAndroid android;
    private Map<MapleQuest, MapleQuestStatus> quests;
    private Map<Integer, String> questinfo;
    private Map<Skill, SkillEntry> skills;
    private transient Map<MapleBuffStat, MapleBuffStatValueHolder> effects;
    private Map<String, String> CustomValues = new HashMap();
    private transient List<MapleSummon> summons;
    private transient Map<Integer, MapleCoolDownValueHolder> coolDowns;
    private transient Map<MapleDisease, MapleDiseaseValueHolder> diseases;
    private CashShop cs;
    private transient Deque<MapleCarnivalChallenge> pendingCarnivalRequests;
    private transient MapleCarnivalParty carnivalParty;
    private BuddyList buddylist;
    private MonsterBook monsterbook;
    private MapleClient client;
    private transient MapleParty party;
    private PlayerStats stats;
    private MapleCharacterCards characterCard;
    private transient MapleMap map;
    private transient MapleShop shop;
    private transient MapleDragon dragon;
    private transient MapleHaku Haku;
    private transient MapleExtractor extractor;
    private transient RockPaperScissors rps;
    private Map<Integer, MonsterFamiliar> familiars;
    private MapleStorage storage;
    private transient MapleTrade trade;
    private MapleMount mount;
    private int sp;
    private MapleMessenger messenger;
    private byte[] petStore;
    private transient IMaplePlayerShop playerShop;
    private boolean invincible;
    private boolean canTalk;
    private boolean clone;
    private boolean followinitiator;
    private boolean followon;
    private boolean smega;
    private boolean hasSummon;
    private MapleGuildCharacter mgc;
    private MapleFamilyCharacter mfc;
    private transient EventInstanceManager eventInstance;
    private List<MapleCharacter> chars = new LinkedList();
    private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
    private final Lock rL = this.mutex.readLock();
    private final Lock wL = this.mutex.writeLock();
    private transient EventManager eventInstanceAzwan;
    private MapleInventory[] inventory;
    private SkillMacro[] skillMacros = new SkillMacro[5];
    private EnumMap<MapleTrait.MapleTraitType, MapleTrait> traits;
    private MapleKeyLayout keylayout;
    private transient ScheduledFuture<?> mapTimeLimitTask;
    private transient ScheduledFuture<?> mapTimeLimitTaskADD;
    private transient Event_PyramidSubway pyramidSubway = null;
    private transient List<Integer> pendingExpiration = null;
    private transient Map<Skill, SkillEntry> pendingSkills = null;
    private transient Map<Integer, Integer> linkMobs;
    private List<InnerSkillValueHolder> innerSkills;
    private boolean changed_wishlist;
    private boolean changed_trocklocations;
    private boolean changed_regrocklocations;
    private boolean changed_hyperrocklocations;
    private boolean changed_skillmacros;
    private boolean changed_savedlocations;
    private boolean changed_questinfo;
    private boolean changed_skills;
    private boolean changed_extendedSlots;
    public boolean keyvalue_changed = false;
    public boolean innerskill_changed = true;
    private int reborns;
    private int apstorage;
    private short occupationId;
    private short occupationEXP;
    private long loginTime;
    private int showdamage;
    private long damage;
    private int jobid;
    private int str;
    private int luk;
    private int int_;
    private int dex;
    private int DonatorPoints;
    private transient PlayerRandomStream CRand;

    private MapleCharacter(boolean ChannelServer) {
        setStance(0);
        setPosition(new Point(0, 0));

        this.inventory = new MapleInventory[MapleInventoryType.values().length];
        for (MapleInventoryType type : MapleInventoryType.values()) {
            this.inventory[type.ordinal()] = new MapleInventory(type);
        }
        this.quests = new LinkedHashMap();
        this.skills = new LinkedHashMap();
        this.stats = new PlayerStats();
        this.innerSkills = new LinkedList();
        this.characterCard = new MapleCharacterCards();
        for (int i = 0; i < this.remainingSp.length; i++) {
            this.remainingSp[i] = 0;
        }
        for (int i = 0; i < this.school.length; i++) {
            this.school[i] = 0;
        }
        this.traits = new EnumMap(MapleTrait.MapleTraitType.class);
        for (MapleTrait.MapleTraitType t : MapleTrait.MapleTraitType.values()) {
            this.traits.put(t, new MapleTrait(t));
        }
        if (ChannelServer) {
            this.changed_skills = false;
            this.changed_wishlist = false;
            this.changed_trocklocations = false;
            this.changed_regrocklocations = false;
            this.changed_hyperrocklocations = false;
            this.changed_skillmacros = false;
            this.changed_savedlocations = false;
            this.changed_extendedSlots = false;
            this.changed_questinfo = false;
            this.scrolledPosition = 0;
            this.lastCombo = 0L;
            this.mulung_energy = 0;

            this.loginTime = 0L;

            this.combo = 0;
            this.force = 0;
            this.keydown_skill = 0L;
            this.nextConsume = 0L;
            this.pqStartTime = 0L;
            this.fairyExp = 0;
            this.cardStack = 0;
            this.runningStack = 1;
            this.mapChangeTime = 0L;
            this.lastRecoveryTime = 0L;
            this.lastDragonBloodTime = 0L;
            this.lastBerserkTime = 0L;
            this.lastFishingTime = 0L;
            this.lastFairyTime = 0L;
            this.lastHPTime = 0L;
            this.lastMPTime = 0L;
            this.lastFamiliarEffectTime = 0L;
            this.old = new Point(0, 0);
            this.coconutteam = 0;
            this.followid = 0;
            this.battleshipHP = 0;
            this.marriageItemId = 0;
            this.fallcounter = 0;
            this.challenge = 0;
            this.dotHP = 0;
            this.lastSummonTime = 0L;
            this.hasSummon = false;
            this.invincible = false;
            this.canTalk = true;
            this.clone = false;
            this.followinitiator = false;
            this.followon = false;
            this.rebuy = new ArrayList();
            this.linkMobs = new HashMap();
            this.teleportname = "";
            this.smega = true;
            this.petStore = new byte[3];
            for (int i = 0; i < this.petStore.length; i++) {
                this.petStore[i] = -1;
            }
            this.wishlist = new int[10];
            this.rocks = new int[10];
            this.regrocks = new int[5];
            this.hyperrocks = new int[13];
            this.imps = new MapleImp[3];
            this.clones = new WeakReference[10];
            for (int i = 0; i < this.clones.length; i++) {
                this.clones[i] = new WeakReference(null);
            }
            this.familiars = new LinkedHashMap();
            this.extendedSlots = new ArrayList();
            this.effects = new ConcurrentEnumMap(MapleBuffStat.class);
            this.coolDowns = new LinkedHashMap();
            this.diseases = new ConcurrentEnumMap(MapleDisease.class);
            this.inst = new AtomicInteger(0);
            this.insd = new AtomicInteger(-1);
            this.keylayout = new MapleKeyLayout();
            this.doors = new ArrayList();
            this.mechDoors = new ArrayList();
            this.controlled = new LinkedHashSet();
            this.controlledLock = new ReentrantReadWriteLock();
            this.summons = new LinkedList();
            this.summonsLock = new ReentrantReadWriteLock();
            this.visibleMapObjects = new LinkedHashSet();
            this.visibleMapObjectsLock = new ReentrantReadWriteLock();
            this.pendingCarnivalRequests = new LinkedList();

            this.savedLocations = new int[SavedLocationType.values().length];
            for (int i = 0; i < SavedLocationType.values().length; i++) {
                this.savedLocations[i] = -1;
            }
            this.questinfo = new LinkedHashMap();
            this.pets = new ArrayList();
        }
    }
    
    public static MapleCharacter getDefault(final MapleClient client) {
        MapleCharacter ret = new MapleCharacter(false);
        ret.client = client;
        ret.map = null;
        ret.exp.set(0);
        ret.gmLevel = 0;
        ret.job = 0;
        ret.meso = 0L;
        ret.level = 1;
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.accountid = client.getAccID();
        ret.buddylist = new BuddyList(232);

        ret.stats.str = 12;
        ret.stats.dex = 5;
        ret.stats.int_ = 4;
        ret.stats.luk = 4;
        ret.stats.maxhp = 50;
        ret.stats.hp = 50;
        ret.stats.maxmp = 50;
        ret.stats.mp = 50;
        ret.gachexp = 0;
        try {
            Connection con = DatabaseConnection.getConnection();

            PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ret.client.setAccountName(rs.getString("name"));
                ret.nxcredit = rs.getInt("nxCredit");
                ret.acash = rs.getInt("ACash");
                ret.maplepoints = rs.getInt("mPoints");
                ret.points = rs.getInt("points");
                ret.vpoints = rs.getInt("vpoints");
                //ret.totalvote = rs.getInt("totalvote");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println(new StringBuilder().append("Error getting character default").append(e).toString());
        }
        return ret;
    }
    

    public final Map<Byte, Integer> getEquips() {
        Map eq = new HashMap();
        for (Item item : this.inventory[MapleInventoryType.EQUIPPED.ordinal()].newList()) {
            eq.put(Byte.valueOf((byte) item.getPosition()), Integer.valueOf(item.getItemId()));
        }
        return eq;
    }
    
       public Map<Byte, Integer> getSecondEquips() {
        final Map<Byte, Integer> eq = new HashMap<>();
        for (final Item item : inventory[MapleInventoryType.EQUIPPED.ordinal()].newList()) {
            int itemId = item.getItemId();
            if (item instanceof Equip) {
                if (GameConstants.angelic(getJob()) && GameConstants.isOverall(itemId)) {
                    itemId = 1051291; //ab def overall
                }
            }
            if (GameConstants.angelic(getJob())) {
                if (!GameConstants.isOverall(itemId) && !GameConstants.isSecondaryWeapon(itemId)
                        && !GameConstants.isWeapon(itemId) && !GameConstants.isMedal(itemId)) {
                    continue;
                }
            }
            eq.put((byte) item.getPosition(), itemId);
        }
        return eq;
    }

    public static final MapleCharacter ReconstructChr(CharacterTransfer ct, MapleClient client, boolean isChannel) {
        MapleCharacter ret = new MapleCharacter(true);
        ret.client = client;
        if (!isChannel) {
            ret.client.setChannel(ct.channel);
        }
        ret.id = ct.characterid;
        ret.name = ct.name;
        ret.level = ct.level;
        ret.fame = ct.fame;

        ret.CRand = new PlayerRandomStream();

        ret.stats.str = ct.str;
        ret.stats.dex = ct.dex;
        ret.stats.int_ = ct.int_;
        ret.stats.luk = ct.luk;
        ret.stats.maxhp = ct.maxhp;
        ret.stats.maxmp = ct.maxmp;
        ret.stats.hp = ct.hp;
        ret.stats.mp = ct.mp;

        ret.characterCard.setCards(ct.cardsInfo);

        ret.chalktext = ct.chalkboard;
        ret.gmLevel = ct.gmLevel;
        ret.exp = ct.exp;
        ret.hpApUsed = ct.hpApUsed;
        ret.remainingSp = ct.remainingSp;
        ret.remainingAp = ct.remainingAp;
        ret.school = ct.school;
        ret.meso = ct.meso;
        ret.stolenSkills = ct.stolenSkills;
        ret.skinColor = ct.skinColor;
        ret.gender = ct.gender;
        ret.job = ct.job;
        ret.hair = ct.hair;
        ret.face = ct.face;
        ret.demonMarking = ct.demonMarking;
        ret.accountid = ct.accountid;
        ret.totalWins = ct.totalWins;
        ret.totalLosses = ct.totalLosses;
        client.setAccID(ct.accountid);
        ret.mapid = ct.mapid;
        ret.initialSpawnPoint = ct.initialSpawnPoint;
        ret.world = ct.world;
        ret.guildid = ct.guildid;
        ret.guildrank = ct.guildrank;
        ret.guildContribution = ct.guildContribution;
        ret.allianceRank = ct.alliancerank;
        ret.points = ct.points;
        ret.vpoints = ct.vpoints;
        ret.fairyExp = ct.fairyExp;
        ret.cardStack = ct.cardStack;
        ret.marriageId = ct.marriageId;
        ret.dgm = ct.dgm;
        ret.gml = ct.gml;
        ret.msipoints = ct.msipoints;
        ret.muted = ct.muted;
        ret.unmuteTime = ct.unmuteTime;
        ret.currentrep = ct.currentrep;
        ret.totalrep = ct.totalrep;
        ret.gachexp = ct.gachexp;
        ret.pvpExp = ct.pvpExp;
        ret.pvpPoints = ct.pvpPoints;
        ret.honourExp = ct.honourexp;
        ret.honourLevel = ct.honourlevel;
        ret.innerSkills = ((LinkedList) ct.innerSkills);
        ret.damage = ct.damage;
        ret.showdamage = ct.showdamage;
        ret.reborns = ct.reborns;
        ret.apstorage = ct.apstorage;
        ret.makeMFC(ct.familyid, ct.seniorid, ct.junior1, ct.junior2);
        if (ret.guildid > 0) {
            ret.mgc = new MapleGuildCharacter(ret);
        }
        ret.fatigue = ct.fatigue;
        ret.buddylist = new BuddyList(ct.buddysize);
        ret.subcategory = ct.subcategory;

        if (isChannel) {
            MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
            ret.map = mapFactory.getMap(ret.mapid);
            if (ret.map == null) {
                ret.map = mapFactory.getMap(100000000);
            } else if ((ret.map.getForcedReturnId() != 999999999) && (ret.map.getForcedReturnMap() != null)) {
                ret.map = ret.map.getForcedReturnMap();
            }

            MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
            if (portal == null) {
                portal = ret.map.getPortal(0);
                ret.initialSpawnPoint = 0;
            }
            ret.setPosition(portal.getPosition());

            int messengerid = ct.messengerid;
            if (messengerid > 0) {
                ret.messenger = World.Messenger.getMessenger(messengerid);
            }
        } else {
            ret.messenger = null;
        }
        int partyid = ct.partyid;
        if (partyid >= 0) {
            MapleParty party = World.Party.getParty(partyid);
            if ((party != null) && (party.getMemberById(ret.id) != null)) {
                ret.party = party;
            }

        }

        for (Map.Entry<Integer, Object> qs : ct.Quest.entrySet()) {
            MapleQuestStatus queststatus_from = (MapleQuestStatus) qs.getValue();
            queststatus_from.setQuest(((Integer) qs.getKey()).intValue());
            ret.quests.put(queststatus_from.getQuest(), queststatus_from);
        }
        for (Map.Entry<Integer, SkillEntry> qs : ct.Skills.entrySet()) {
            ret.skills.put(SkillFactory.getSkill(((Integer) qs.getKey()).intValue()), qs.getValue());
        }
        for (Map.Entry t : ct.traits.entrySet()) {
            ((MapleTrait) ret.traits.get(t.getKey())).setExp(((Integer) t.getValue()).intValue());
        }
        ret.monsterbook = new MonsterBook(ct.mbook, ret);
        ret.inventory = ((MapleInventory[]) ct.inventorys);
        ret.BlessOfFairy_Origin = ct.BlessOfFairy;
        ret.BlessOfEmpress_Origin = ct.BlessOfEmpress;
        ret.skillMacros = ((SkillMacro[]) ct.skillmacro);
        ret.petStore = ct.petStore;
        ret.keylayout = new MapleKeyLayout(ct.keymap);
        ret.questinfo = ct.InfoQuest;
        ret.familiars = ct.familiars;
        ret.savedLocations = ct.savedlocation;
        ret.wishlist = ct.wishlist;
        ret.rocks = ct.rocks;
        ret.regrocks = ct.regrocks;
        ret.hyperrocks = ct.hyperrocks;
        ret.buddylist.loadFromTransfer(ct.buddies);
        ret.keydown_skill = 0L;
        ret.lastfametime = ct.lastfametime;
        ret.lastmonthfameids = ct.famedcharacters;
        ret.lastmonthbattleids = ct.battledaccs;
        ret.extendedSlots = ct.extendedSlots;
        ret.storage = ((MapleStorage) ct.storage);
        ret.cs = ((CashShop) ct.cs);
        client.setAccountName(ct.accountname);
        ret.nxcredit = ct.nxCredit;
        ret.acash = ct.ACash;
        ret.maplepoints = ct.MaplePoints;
        ret.numClones = ct.clonez;
        ret.imps = ct.imps;
        ret.rebuy = ct.rebuy;
        ret.mount = new MapleMount(ret, ct.mount_itemid, PlayerStats.getSkillByJob(80001000, ret.job), ct.mount_Fatigue, ct.mount_level, ct.mount_exp);
        ret.expirationTask(false, false);
        ret.stats.recalcLocalStats(true, ret);
        client.setTempIP(ct.tempIP);

        return ret;
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver) {
        return loadCharFromDB(charid, client, channelserver, null);
    }

    public static MapleCharacter loadCharFromDB(int charid, MapleClient client, boolean channelserver, Map<Integer, CardData> cads) {
        MapleCharacter ret = new MapleCharacter(channelserver);
        ret.client = client;
        ret.id = charid;

        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                throw new RuntimeException("Loading the Char Failed (char not found)");
            }
            ret.name = rs.getString("name");
            ret.level = rs.getShort("level");
            ret.fame = rs.getInt("fame");

            ret.stats.str = rs.getShort("str");
            ret.stats.dex = rs.getShort("dex");
            ret.stats.int_ = rs.getShort("int");
            ret.stats.luk = rs.getShort("luk");
            ret.stats.maxhp = rs.getInt("maxhp");
            ret.stats.maxmp = rs.getInt("maxmp");
            ret.stats.hp = rs.getInt("hp");
            ret.stats.mp = rs.getInt("mp");
            ret.job = rs.getShort("job");
            ret.gmLevel = rs.getByte("gm");
            ret.exp.set(rs.getLong("exp"));
            ret.hpApUsed = rs.getInt("hpApUsed");
            String[] sp = rs.getString("sp").split(",");
            for (int i = 0; i < ret.remainingSp.length; i++) {
                ret.remainingSp[i] = Integer.parseInt(sp[i]);
            }
            /*String[] sls = rs.getString("school").split(",");
             for (int i = 0; i < ret.school.length; i++) {
             ret.school[i] = Integer.parseInt(sls[i]);
             }*/
            ret.remainingAp = rs.getInt("ap");
            ret.dgm = rs.getInt("dgm");
            ret.msipoints = rs.getInt("msipoints");
            ret.meso = rs.getLong("meso");
            ret.skinColor = rs.getByte("skincolor");
            ret.gender = rs.getByte("gender");
            ret.muted = rs.getInt("muted") == 1 ? true : false;
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(rs.getLong("unmutetime")));
            ret.unmuteTime = c;
            ret.hair = rs.getInt("hair");
            ret.face = rs.getInt("face");
            ret.demonMarking = rs.getInt("demonMarking");
            ret.accountid = rs.getInt("accountid");
            client.setAccID(ret.accountid);
            ret.mapid = rs.getInt("map");
            ret.initialSpawnPoint = rs.getByte("spawnpoint");
            ret.world = rs.getByte("world");
            ret.guildid = rs.getInt("guildid");
            ret.guildrank = rs.getByte("guildrank");
            ret.allianceRank = rs.getByte("allianceRank");
            ret.guildContribution = rs.getInt("guildContribution");
            ret.totalWins = rs.getInt("totalWins");
            ret.totalLosses = rs.getInt("totalLosses");
            ret.currentrep = rs.getInt("currentrep");
            ret.totalrep = rs.getInt("totalrep");
            ret.makeMFC(rs.getInt("familyid"), rs.getInt("seniorid"), rs.getInt("junior1"), rs.getInt("junior2"));
            if (ret.guildid > 0) {
                ret.mgc = new MapleGuildCharacter(ret);
            }
            ret.gachexp = rs.getInt("gachexp");
            ret.buddylist = new BuddyList(rs.getInt("buddyCapacity"));
            ret.honourExp = rs.getInt("honourExp");
            ret.honourLevel = rs.getInt("honourLevel");
            ret.damage = rs.getLong("damage");
            ret.showdamage = rs.getInt("showdamage");
            //ret.subcategory = rs.getByte("subcategory");
            ret.mount = new MapleMount(ret, 0, PlayerStats.getSkillByJob(80001000, ret.job), (byte) 0, (byte) 1, 0);
            ret.rank = rs.getInt("rank");
            ret.rankMove = rs.getInt("rankMove");
            ret.jobRank = rs.getInt("jobRank");
            ret.jobRankMove = rs.getInt("jobRankMove");
            ret.marriageId = rs.getInt("marriageId");
            ret.fatigue = rs.getShort("fatigue");
            ret.pvpExp = rs.getInt("pvpExp");
            ret.pvpPoints = rs.getInt("pvpPoints");
            ret.gml = rs.getInt("gml");
            ret.reborns = rs.getInt("reborns");
            ret.apstorage = rs.getInt("apstorage");

            for (MapleTrait t : ret.traits.values()) {
                t.setExp(rs.getInt(t.getType().name()));
            }
            if (channelserver) {
                ret.CRand = new PlayerRandomStream();
                MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
                ret.map = mapFactory.getMap(ret.mapid);
                if (ret.map == null) {
                    ret.map = mapFactory.getMap(100000000);
                }
                MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
                if (portal == null) {
                    portal = ret.map.getPortal(0);
                    ret.initialSpawnPoint = 0;
                }
                ret.setPosition(portal.getPosition());

                int partyid = rs.getInt("party");
                if (partyid >= 0) {
                    MapleParty party = World.Party.getParty(partyid);
                    if ((party != null) && (party.getMemberById(ret.id) != null)) {
                        ret.party = party;
                    }
                }
                String[] pets = rs.getString("pets").split(",");
                for (int i = 0; i < ret.petStore.length; i++) {
                    ret.petStore[i] = Byte.parseByte(pets[i]);
                }
                rs.close();
                ps.close();


            }

            rs.close();
            ps.close();

            if (cads != null) {
                ret.characterCard.setCards(cads);
            } else {
                ret.characterCard.loadCards(client, channelserver);
            }

            ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
            ps.setInt(1, charid);
            rs = ps.executeQuery();
            pse = con.prepareStatement("SELECT * FROM queststatusmobs WHERE queststatusid = ?");

            while (rs.next()) {
                int id = rs.getInt("quest");
                MapleQuest q = MapleQuest.getInstance(id);
                byte stat = rs.getByte("status");
                if (((stat != 1) && (stat != 2)) || (((!channelserver) || ((q != null) && (!q.isBlocked()))) && ((stat != 1) || (!channelserver) || (q.canStart(ret, null))))) {
                    MapleQuestStatus status = new MapleQuestStatus(q, stat);
                    long cTime = rs.getLong("time");
                    if (cTime > -1L) {
                        status.setCompletionTime(cTime * 1000L);
                    }
                    status.setForfeited(rs.getInt("forfeited"));
                    status.setCustomData(rs.getString("customData"));
                    ret.quests.put(q, status);
                    pse.setInt(1, rs.getInt("queststatusid"));
                    ResultSet rsMobs = pse.executeQuery();

                    while (rsMobs.next()) {
                        status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
                    }
                    rsMobs.close();
                }
            }
            rs.close();
            ps.close();
            pse.close();

            if (channelserver) {
                ret.monsterbook = MonsterBook.loadCards(ret.accountid, ret);

                ps = con.prepareStatement("SELECT * FROM inventoryslot where characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();

                if (!rs.next()) {
                    rs.close();
                    ps.close();
                    throw new RuntimeException("No Inventory slot column found in SQL. [inventoryslot]");
                }
                ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equip"));
                ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("use"));
                ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setup"));
                ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etc"));
                ret.getInventory(MapleInventoryType.CASH).setSlotLimit(rs.getByte("cash"));

                ps.close();
                rs.close();

                for (Pair mit : ItemLoader.INVENTORY.loadItems(false, charid).values()) {
                    ret.getInventory((MapleInventoryType) mit.getRight()).addFromDB((Item) mit.getLeft());
                    if (((Item) mit.getLeft()).getPet() != null) {
                        ret.pets.add(((Item) mit.getLeft()).getPet());
                    }
                }

                ps = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                ps.setInt(1, ret.accountid);
                rs = ps.executeQuery();
                if (rs.next()) {
                    ret.getClient().setAccountName(rs.getString("name"));
                    ret.nxcredit = rs.getInt("nxCredit");
                    ret.acash = rs.getInt("ACash");
                    ret.maplepoints = rs.getInt("mPoints");
                    ret.points = rs.getInt("points");
                    ret.vpoints = rs.getInt("vpoints");
                    //ret.totalvote = rs.getInt("totalvote");

                    if (rs.getTimestamp("lastlogon") != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(rs.getTimestamp("lastlogon").getTime());
                    }
                    if (rs.getInt("banned") > 0) {
                        rs.close();
                        ps.close();
                        ret.getClient().getSession().close(true);
                        throw new RuntimeException("Loading a banned character");
                    }
                    rs.close();
                    ps.close();

                    ps = con.prepareStatement("UPDATE accounts SET lastlogon = CURRENT_TIMESTAMP() WHERE id = ?");
                    ps.setInt(1, ret.accountid);
                    ps.executeUpdate();
                } else {
                    rs.close();
                }
                ps.close();

                ps = con.prepareStatement("SELECT * FROM questinfo WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();

                while (rs.next()) {
                    ret.questinfo.put(Integer.valueOf(rs.getInt("quest")), rs.getString("customData"));
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT skillid, skilllevel, masterlevel, expiration FROM skills WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();

                while (rs.next()) {
                    int skid = rs.getInt("skillid");
                    Skill skil = SkillFactory.getSkill(skid);
                    int skl = rs.getInt("skilllevel");
                    byte msl = rs.getByte("masterlevel");
                    if ((skil != null) && (GameConstants.isApplicableSkill(skid))) {
                        if ((skl > skil.getMaxLevel()) && (skid < 92000000)) {
                            if ((!skil.isBeginnerSkill()) && (skil.canBeLearnedBy(ret.job)) && (!skil.isSpecialSkill())) {
                                ret.remainingSp[GameConstants.getSkillBookForSkill(skid)] += skl - skil.getMaxLevel();
                            }
                            skl = (byte) skil.getMaxLevel();
                        }
                        if (msl > skil.getMaxLevel()) {
                            msl = (byte) skil.getMaxLevel();
                        }
                        ret.skills.put(skil, new SkillEntry(skl, msl, rs.getLong("expiration")));
                    } else if ((skil == null)
                            && (!GameConstants.isBeginnerJob(skid / 10000)) && (skid / 10000 != 900) && (skid / 10000 != 800) && (skid / 10000 != 9000)) {
                        ret.remainingSp[GameConstants.getSkillBookForSkill(skid)] += skl;
                    }
                }

                rs.close();
                ps.close();

                ret.expirationTask(false, true);

                ps = con.prepareStatement("SELECT * FROM characters WHERE accountid = ? ORDER BY level DESC");
                ps.setInt(1, ret.accountid);
                rs = ps.executeQuery();
                int maxlevel_ = 0;
                int maxlevel_2 = 0;
                while (rs.next()) {
                    if (rs.getInt("id") != charid) {
                        if (GameConstants.isKOC(rs.getShort("job"))) {
                            int maxlevel = rs.getShort("level") / 5;

                            if (maxlevel > 24) {
                                maxlevel = 24;
                            }
                            if ((maxlevel > maxlevel_2) || (maxlevel_2 == 0)) {
                                maxlevel_2 = maxlevel;
                                ret.BlessOfEmpress_Origin = rs.getString("name");
                            }
                        }
                        int maxlevel = rs.getShort("level") / 10;

                        if (maxlevel > 20) {
                            maxlevel = 20;
                        }
                        if ((maxlevel > maxlevel_) || (maxlevel_ == 0)) {
                            maxlevel_ = maxlevel;
                            ret.BlessOfFairy_Origin = rs.getString("name");
                        }

                    }

                }

                if (ret.BlessOfFairy_Origin == null) {
                    ret.BlessOfFairy_Origin = ret.name;
                }
                ret.skills.put(SkillFactory.getSkill(GameConstants.getBOF_ForJob(ret.job)), new SkillEntry(maxlevel_, (byte) 0, -1L));
                if (SkillFactory.getSkill(GameConstants.getEmpress_ForJob(ret.job)) != null) {
                    if (ret.BlessOfEmpress_Origin == null) {
                        ret.BlessOfEmpress_Origin = ret.BlessOfFairy_Origin;
                    }
                    ret.skills.put(SkillFactory.getSkill(GameConstants.getEmpress_ForJob(ret.job)), new SkillEntry(maxlevel_2, (byte) 0, -1L));
                }
                ps.close();
                rs.close();
                ps = con.prepareStatement("SELECT skill_id, skill_level, max_level, rank FROM inner_ability_skills WHERE player_id = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.innerSkills.add(new InnerSkillValueHolder(rs.getInt("skill_id"), rs.getByte("skill_level"), rs.getByte("max_level"), rs.getByte("rank")));
                }
                ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();

                while (rs.next()) {
                    int position = rs.getInt("position");
                    SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
                    ret.skillMacros[position] = macro;
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT * FROM familiars WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getLong("expiry") > System.currentTimeMillis()) {
                        ret.familiars.put(Integer.valueOf(rs.getInt("familiar")), new MonsterFamiliar(charid, rs.getInt("id"), rs.getInt("familiar"), rs.getLong("expiry"), rs.getString("name"), rs.getInt("fatigue"), rs.getByte("vitality")));
                    }
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();

                Map keyb = ret.keylayout.Layout();
                while (rs.next()) {
                    keyb.put(Integer.valueOf(rs.getInt("key")), new Pair(Byte.valueOf(rs.getByte("type")), Integer.valueOf(rs.getInt("action"))));
                }
                rs.close();
                ps.close();
                ret.keylayout.unchanged();

                ps = con.prepareStatement("SELECT `locationtype`,`map` FROM savedlocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.savedLocations[rs.getInt("locationtype")] = rs.getInt("map");
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                ret.lastfametime = 0L;
                ret.lastmonthfameids = new ArrayList(31);
                while (rs.next()) {
                    ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                    ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterid_to")));
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT `accid_to`,`when` FROM battlelog WHERE accid = ? AND DATEDIFF(NOW(),`when`) < 30");
                ps.setInt(1, ret.accountid);
                rs = ps.executeQuery();
                ret.lastmonthbattleids = new ArrayList();
                while (rs.next()) {
                    ret.lastmonthbattleids.add(Integer.valueOf(rs.getInt("accid_to")));
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT `itemId` FROM extendedslots WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.extendedSlots.add(Integer.valueOf(rs.getInt("itemId")));
                }
                rs.close();
                ps.close();

                ret.buddylist.loadFromDb(charid);
                ret.storage = MapleStorage.loadStorage(ret.accountid);
                ret.cs = new CashShop(ret.accountid, charid, ret.getJob());

                ps = con.prepareStatement("SELECT sn FROM wishlist WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                int i = 0;
                while (rs.next()) {
                    ret.wishlist[i] = rs.getInt("sn");
                    i++;
                }
                while (i < 10) {
                    ret.wishlist[i] = 0;
                    i++;
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT mapid FROM trocklocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                int r = 0;
                while (rs.next()) {
                    ret.rocks[r] = rs.getInt("mapid");
                    r++;
                }
                while (r < 10) {
                    ret.rocks[r] = 999999999;
                    r++;
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT mapid FROM regrocklocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                r = 0;
                while (rs.next()) {
                    ret.regrocks[r] = rs.getInt("mapid");
                    r++;
                }
                while (r < 5) {
                    ret.regrocks[r] = 999999999;
                    r++;
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT mapid FROM hyperrocklocations WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                r = 0;
                while (rs.next()) {
                    ret.hyperrocks[r] = rs.getInt("mapid");
                    r++;
                }
                while (r < 13) {
                    ret.hyperrocks[r] = 999999999;
                    r++;
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT * from stolen WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret.stolenSkills.add(new Pair(Integer.valueOf(rs.getInt("skillid")), Boolean.valueOf(rs.getInt("chosen") > 0)));
                }
                rs.close();
                ps.close();
                ps = con.prepareStatement("SELECT * FROM imps WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                r = 0;
                while (rs.next()) {
                    ret.imps[r] = new MapleImp(rs.getInt("itemid"));
                    ret.imps[r].setLevel(rs.getByte("level"));
                    ret.imps[r].setState(rs.getByte("state"));
                    ret.imps[r].setCloseness(rs.getShort("closeness"));
                    ret.imps[r].setFullness(rs.getShort("fullness"));
                    r++;
                }
                rs.close();
                ps.close();

                ps = con.prepareStatement("SELECT * FROM mountdata WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new RuntimeException("No mount data found on SQL column");
                }
                Item mount = ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (byte) (GameConstants.GMS ? -18 : -23));
                ret.mount = new MapleMount(ret, mount != null ? mount.getItemId() : 0, GameConstants.GMS ? 80001000 : PlayerStats.getSkillByJob(80001000, ret.job), rs.getByte("Fatigue"), rs.getByte("Level"), rs.getInt("Exp"));
                ps.close();
                rs.close();

                ret.stats.recalcLocalStats(true, ret);
            } else {
                for (Pair mit : ItemLoader.INVENTORY.loadItems(true, charid).values()) {
                    ret.getInventory((MapleInventoryType) mit.getRight()).addFromDB((Item) mit.getLeft());
                }
                ret.stats.recalcPVPRank(ret);
            }
        } catch (SQLException ess) {
            ess.printStackTrace();
            System.out.println("Failed to load character..");
            FileoutputUtil.outputFileError("Log_Packet_Except.txt", ess);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignore) {
            }
        }
        return ret;
    }

    public static void saveNewCharToDB(MapleCharacter chr, short db) {
        Connection con = DatabaseConnection.getConnection();

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con.setTransactionIsolation(1);
            con.setAutoCommit(false);

            ps = con.prepareStatement("INSERT INTO characters (level, str, dex, luk, `int`, hp, mp, maxhp, maxmp, sp, ap, skincolor, gender, job, hair, face, demonMarking, map, meso, party, buddyCapacity, pets, subcategory, accountid, name, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);
            ps.setInt(1, chr.level);
            PlayerStats stat = chr.stats;
            ps.setInt(2, stat.getStr());
            ps.setInt(3, stat.getDex());
            ps.setInt(4, stat.getInt());
            ps.setInt(5, stat.getLuk());
            ps.setInt(6, stat.getHp());
            ps.setInt(7, stat.getMp());
            ps.setInt(8, stat.getMaxHp());
            ps.setInt(9, stat.getMaxMp());
            StringBuilder sps = new StringBuilder();
            for (int i = 0; i < chr.remainingSp.length; i++) {
                sps.append(chr.remainingSp[i]);
                sps.append(",");
            }
            String sp = sps.toString();
            ps.setString(10, sp.substring(0, sp.length() - 1));
            ps.setInt(11, chr.remainingAp);
            ps.setByte(12, chr.skinColor);
            ps.setByte(13, chr.gender);
            ps.setInt(14, chr.job);
            ps.setInt(15, chr.hair);
            ps.setInt(16, chr.face);
            ps.setInt(17, chr.demonMarking);
            if ((db < 0) || (db > 10)) {
                db = 0;
            }
            ps.setInt(18, 10000);
        //    ps.setInt(18, db == 2 ? 3000600 : type.map);
            ps.setLong(19, chr.meso);
            ps.setInt(20, -1);
            ps.setInt(21, chr.buddylist.getCapacity());
            ps.setString(22, "-1,-1,-1");
            ps.setInt(23, db);
            ps.setInt(24, chr.getAccountID());
            ps.setString(25, chr.name);
            ps.setByte(26, chr.world);
            StringBuilder spss = new StringBuilder();
            for (int i = 0; i < chr.school.length; i++) {
                spss.append(chr.school[i]);
                spss.append(",");
            }
            String sps3 = spss.toString();
            //ps.setString(27, sps3.substring(0, sps3.length() - 1));
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                chr.id = rs.getInt(1);
            } else {
                ps.close();
                rs.close();
                throw new DatabaseException("Inserting char failed.");
            }
            ps.close();
            rs.close();
            ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", 1);
            pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
            ps.setInt(1, chr.id);
            for (MapleQuestStatus q : chr.quests.values()) {
                ps.setInt(2, q.getQuest().getId());
                ps.setInt(3, q.getStatus());
                ps.setInt(4, (int) (q.getCompletionTime() / 1000L));
                ps.setInt(5, q.getForfeited());
                ps.setString(6, q.getCustomData());
                ps.execute();
                rs = ps.getGeneratedKeys();
                Iterator i$;
                if (q.hasMobKills()) {
                    rs.next();
                    for (i$ = q.getMobKills().keySet().iterator(); i$.hasNext();) {
                        int mob = ((Integer) i$.next()).intValue();
                        pse.setInt(1, rs.getInt(1));
                        pse.setInt(2, mob);
                        pse.setInt(3, q.getMobKills(mob));
                        pse.execute();
                    }
                }
                rs.close();
            }
            ps.close();
            pse.close();

            ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration) VALUES (?, ?, ?, ?, ?)");
            ps.setInt(1, chr.id);

            for (Map.Entry skill : chr.skills.entrySet()) {
                if (GameConstants.isApplicableSkill(((Skill) skill.getKey()).getId())) {
                    ps.setInt(2, ((Skill) skill.getKey()).getId());
                    ps.setInt(3, ((SkillEntry) skill.getValue()).skillevel);
                    ps.setByte(4, ((SkillEntry) skill.getValue()).masterlevel);
                    ps.setLong(5, ((SkillEntry) skill.getValue()).expiration);
                    ps.execute();
                }
            }
            ps.close();

            ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            ps.setByte(2, (byte) 96);
            ps.setByte(3, (byte) 96);
            ps.setByte(4, (byte) 96);
            ps.setByte(5, (byte) 96);
            ps.setByte(6, (byte) 96);
            ps.execute();
            ps.close();

            ps = con.prepareStatement("INSERT INTO mountdata (characterid, `Level`, `Exp`, `Fatigue`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            ps.setByte(2, (byte) 1);
            ps.setInt(3, 0);
            ps.setByte(4, (byte) 0);
            ps.execute();
            ps.close();

            int[] array1 = {2, 3, 4, 5, 6, 7, 8, 13, 16, 17, 18, 19, 20, 21, 23, 24, 25, 26, 27, 29, 31, 33, 34, 35, 37, 38, 39, 40, 41, 43, 44, 45, 46, 47, 48, 49, 50, 56, 57, 59, 60, 61, 62, 63, 64, 65};
            int[] array2 = {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 6, 6, 6};
            int[] array3 = {10, 12, 13, 18, 24, 21, 29, 33, 8, 5, 0, 4, 28, 31, 1, 25, 19, 14, 15, 52, 2, 26, 17, 11, 3, 20, 27, 16, 23, 9, 50, 51, 6, 32, 30, 22, 7, 53, 54, 100, 101, 102, 103, 104, 105, 106};

            ps = con.prepareStatement("INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            for (int i = 0; i < array1.length; i++) {
                ps.setInt(2, array1[i]);
                ps.setInt(3, array2[i]);
                ps.setInt(4, array3[i]);
                ps.execute();
            }
            ps.close();

            List listing = new ArrayList();
            for (MapleInventory iv : chr.inventory) {
                for (Item item : iv.list()) {
                    listing.add(new Pair(item, iv.getType()));
                }
            }
            ItemLoader.INVENTORY.saveItems(listing, con, chr.id);

            con.commit();
        } catch (Exception e) {
            FileoutputUtil.outputFileError("Log_Packet_Except.txt", e);
            e.printStackTrace();
            System.err.println("[charsave] Error saving character data");
            try {
                con.rollback();
            } catch (SQLException ex) {
                FileoutputUtil.outputFileError("Log_Packet_Except.txt", ex);
                ex.printStackTrace();
                System.err.println("[charsave] Error Rolling Back");
            }
        } finally {
            try {
                if (pse != null) {
                    pse.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
                con.setAutoCommit(true);
                con.setTransactionIsolation(4);
            } catch (SQLException e) {
                FileoutputUtil.outputFileError("Log_Packet_Except.txt", e);
                e.printStackTrace();
                System.err.println("[charsave] Error going back to autocommit mode");
            }
        }
    }

    public void saveToDB(boolean dc, boolean fromcs) {
        if (isClone()) {
            return;
        }
        Connection con = DatabaseConnection.getConnection();

        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con.setTransactionIsolation(1);
            con.setAutoCommit(false);

            ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, demonMarking = ?, map = ?, meso = ?, hpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, pets = ?, subcategory = ?, marriageId = ?, currentrep = ?, totalrep = ?, gachexp = ?, fatigue = ?, charm = ?, charisma = ?, craft = ?, insight = ?, sense = ?, will = ?, honourExp = ?, honourLevel = ?, damage = ?, showdamage = ?, totalwins = ?, totallosses = ?, pvpExp = ?, pvpPoints = ?, reborns = ?, apstorage = ?, name = ?, school = ?, dgm = ?, msipoints = ?, gml = ?, muted = ?, unmutetime = ? WHERE id = ?", 1);

            ps.setInt(1, this.level);
            ps.setInt(2, this.fame);
            ps.setInt(3, this.stats.getStr());
            ps.setInt(4, this.stats.getDex());
            ps.setInt(5, this.stats.getLuk());
            ps.setInt(6, this.stats.getInt());
            ps.setLong(7, Math.abs(exp.get()));
            ps.setInt(8, this.stats.getHp() < 1 ? 50 : this.stats.getHp());
            ps.setInt(9, this.stats.getMp());
            ps.setInt(10, this.stats.getMaxHp());
            ps.setInt(11, this.stats.getMaxMp());
            StringBuilder sps = new StringBuilder();
            for (int i = 0; i < this.remainingSp.length; i++) {
                sps.append(this.remainingSp[i]);
                sps.append(",");
            }
            String sp = sps.toString();
            ps.setString(12, sp.substring(0, sp.length() - 1));
            ps.setInt(13, this.remainingAp);
            ps.setByte(14, this.gmLevel);
            ps.setByte(15, this.skinColor);
            ps.setByte(16, this.gender);
            ps.setInt(17, this.job);
            ps.setInt(18, this.hair);
            ps.setInt(19, this.face);
            ps.setInt(20, this.demonMarking);
            if ((!fromcs) && (this.map != null)) {
                if ((this.map.getForcedReturnId() != 999999999) && (this.map.getForcedReturnMap() != null)) {
                    ps.setInt(21, this.map.getForcedReturnId());
                } else {
                    ps.setInt(21, this.stats.getHp() < 1 ? this.map.getReturnMapId() : this.map.getId());
                }
            } else {
                ps.setInt(21, this.mapid);
            }
            ps.setLong(22, this.meso);
            ps.setInt(23, this.hpApUsed);
            if (this.map == null) {
                ps.setByte(24, (byte) 0);
            } else {
                MaplePortal closest = this.map.findClosestSpawnpoint(getTruePosition());
                ps.setByte(24, (byte) (closest != null ? closest.getId() : 0));
            }
            ps.setInt(25, this.party == null ? -1 : this.party.getId());
            ps.setShort(26, (short) this.buddylist.getCapacity());
            StringBuilder petz = new StringBuilder();
            int petLength = 0;
            for (MaplePet pet : this.pets) {
                if (pet.getSummoned()) {
                    pet.saveToDb();
                    petz.append(pet.getInventoryPosition());
                    petz.append(",");
                    petLength++;
                }
            }
            while (petLength < 3) {
                petz.append("-1,");
                petLength++;
            }
            String petstring = petz.toString();
            ps.setString(27, petstring.substring(0, petstring.length() - 1));
            ps.setByte(28, this.subcategory);
            ps.setInt(29, this.marriageId);
            ps.setInt(30, this.currentrep);
            ps.setInt(31, this.totalrep);
            ps.setInt(32, this.gachexp);
            ps.setShort(33, this.fatigue);
            ps.setInt(34, ((MapleTrait) this.traits.get(MapleTrait.MapleTraitType.charm)).getTotalExp());
            ps.setInt(35, ((MapleTrait) this.traits.get(MapleTrait.MapleTraitType.charisma)).getTotalExp());
            ps.setInt(36, ((MapleTrait) this.traits.get(MapleTrait.MapleTraitType.craft)).getTotalExp());
            ps.setInt(37, ((MapleTrait) this.traits.get(MapleTrait.MapleTraitType.insight)).getTotalExp());
            ps.setInt(38, ((MapleTrait) this.traits.get(MapleTrait.MapleTraitType.sense)).getTotalExp());
            ps.setInt(39, ((MapleTrait) this.traits.get(MapleTrait.MapleTraitType.will)).getTotalExp());
            ps.setInt(40, getHonourExp());
            ps.setInt(41, getHonourLevel());
            ps.setLong(42, this.damage);
            ps.setInt(43, this.showdamage);
            ps.setInt(44, this.totalWins);
            ps.setInt(45, this.totalLosses);
            ps.setInt(46, this.pvpExp);
            ps.setInt(47, this.pvpPoints);
            ps.setInt(48, this.reborns);
            ps.setInt(49, this.apstorage);
            ps.setString(50, this.name);
            StringBuilder spss = new StringBuilder();
            for (int i = 0; i < this.school.length; i++) {
                spss.append(this.school[i]);
                spss.append(",");
            }
            String spss00 = spss.toString();
            ps.setString(51, spss00.substring(0, spss00.length() - 1));
            ps.setInt(52, dgm);
            ps.setInt(53, msipoints);
            ps.setInt(54, gml);
            ps.setInt(55, muted ? 1 : 0);
            ps.setLong(56, unmuteTime == null ? 0 : unmuteTime.getTimeInMillis());
            ps.setInt(57, this.id);
            if (ps.executeUpdate() < 1) {
                ps.close();
                throw new DatabaseException(new StringBuilder().append("Character not in database (").append(this.id).append(")").toString());
            }
            ps.close();
            deleteWhereCharacterId(con, "DELETE FROM stolen WHERE characterid = ?");
            for (Pair st : this.stolenSkills) {
                ps = con.prepareStatement("INSERT INTO stolen (characterid, skillid, chosen) VALUES (?, ?, ?)");
                ps.setInt(1, this.id);
                ps.setInt(2, ((Integer) st.left).intValue());
                ps.setInt(3, ((Boolean) st.right).booleanValue() ? 1 : 0);
                ps.execute();
                ps.close();
            }

            if (this.changed_skillmacros) {
                deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");
                for (int i = 0; i < 5; i++) {
                    SkillMacro macro = this.skillMacros[i];
                    if (macro != null) {
                        ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
                        ps.setInt(1, this.id);
                        ps.setInt(2, macro.getSkill1());
                        ps.setInt(3, macro.getSkill2());
                        ps.setInt(4, macro.getSkill3());
                        ps.setString(5, macro.getName());
                        ps.setInt(6, macro.getShout());
                        ps.setInt(7, i);
                        ps.execute();
                        ps.close();
                    }
                }
            }

            deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, this.id);
            ps.setByte(2, getInventory(MapleInventoryType.EQUIP).getSlotLimit());
            ps.setByte(3, getInventory(MapleInventoryType.USE).getSlotLimit());
            ps.setByte(4, getInventory(MapleInventoryType.SETUP).getSlotLimit());
            ps.setByte(5, getInventory(MapleInventoryType.ETC).getSlotLimit());
            ps.setByte(6, getInventory(MapleInventoryType.CASH).getSlotLimit());
            ps.execute();
            ps.close();

            saveInventory(con);

            if (this.changed_questinfo) {
                deleteWhereCharacterId(con, "DELETE FROM questinfo WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO questinfo (`characterid`, `quest`, `customData`) VALUES (?, ?, ?)");
                ps.setInt(1, this.id);
                for (Map.Entry q : this.questinfo.entrySet()) {
                    ps.setInt(2, ((Integer) q.getKey()).intValue());
                    ps.setString(3, (String) q.getValue());
                    ps.execute();
                }
                ps.close();
            }

            deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", 1);
            pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
            ps.setInt(1, this.id);
            for (MapleQuestStatus q : this.quests.values()) {
                ps.setInt(2, q.getQuest().getId());
                ps.setInt(3, q.getStatus());
                ps.setInt(4, (int) (q.getCompletionTime() / 1000L));
                ps.setInt(5, q.getForfeited());
                ps.setString(6, q.getCustomData());
                ps.execute();
                rs = ps.getGeneratedKeys();
                Iterator i$;
                if (q.hasMobKills()) {
                    rs.next();
                    for (i$ = q.getMobKills().keySet().iterator(); i$.hasNext();) {
                        int mob = ((Integer) i$.next()).intValue();
                        pse.setInt(1, rs.getInt(1));
                        pse.setInt(2, mob);
                        pse.setInt(3, q.getMobKills(mob));
                        pse.execute();
                    }
                }
                rs.close();
            }
            ps.close();
            pse.close();

            if (this.changed_skills) {
                deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, this.id);

                for (Map.Entry skill : this.skills.entrySet()) {
                    if (GameConstants.isApplicableSkill(((Skill) skill.getKey()).getId())) {
                        ps.setInt(2, ((Skill) skill.getKey()).getId());
                        ps.setInt(3, ((SkillEntry) skill.getValue()).skillevel);
                        ps.setByte(4, ((SkillEntry) skill.getValue()).masterlevel);
                        ps.setLong(5, ((SkillEntry) skill.getValue()).expiration);
                        ps.execute();
                    }
                }
                ps.close();
            }

            if ((this.innerskill_changed)
                    && (this.innerSkills != null)) {
                deleteWhereCharacterId(con, "DELETE FROM inner_ability_skills WHERE player_id = ?");
                ps = con.prepareStatement("INSERT INTO inner_ability_skills (player_id, skill_id, skill_level, max_level, rank) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, this.id);

                for (int i = 0; i < this.innerSkills.size(); i++) {
                    ps.setInt(2, ((InnerSkillValueHolder) this.innerSkills.get(i)).getSkillId());
                    ps.setInt(3, ((InnerSkillValueHolder) this.innerSkills.get(i)).getSkillLevel());
                    ps.setInt(4, ((InnerSkillValueHolder) this.innerSkills.get(i)).getMaxLevel());
                    ps.setInt(5, ((InnerSkillValueHolder) this.innerSkills.get(i)).getRank());
                    ps.executeUpdate();
                }
                ps.close();
            }

            List<MapleCoolDownValueHolder> cd = getCooldowns();
            if ((dc) && (cd.size() > 0)) {
                ps = con.prepareStatement("INSERT INTO skills_cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)");
                ps.setInt(1, getId());
                for (MapleCoolDownValueHolder cooling : cd) {
                    ps.setInt(2, cooling.skillId);
                    ps.setLong(3, cooling.startTime);
                    ps.setLong(4, cooling.length);
                    ps.execute();
                }
                ps.close();
            }

            if (this.changed_savedlocations) {
                deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`) VALUES (?, ?, ?)");
                ps.setInt(1, this.id);
                for (SavedLocationType savedLocationType : SavedLocationType.values()) {
                    if (this.savedLocations[savedLocationType.getValue()] != -1) {
                        ps.setInt(2, savedLocationType.getValue());
                        ps.setInt(3, this.savedLocations[savedLocationType.getValue()]);
                        ps.execute();
                    }
                }
                ps.close();
            }





            if (buddylist.changed()) {
                deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, ?)");
                ps.setInt(1, id);
                for (BuddylistEntry entry : buddylist.getBuddies()) {
                    ps.setInt(2, entry.getCharacterId());
                    ps.setInt(3, entry.isVisible() ? 0 : 1);
                    ps.execute();
                }
                ps.close();
                buddylist.setChanged(false);
            }

            ps = con.prepareStatement("UPDATE accounts SET `nxCredit` = ?, `ACash` = ?, `mPoints` = ?, `points` = ?, `vpoints` = ? WHERE id = ?");
            ps.setInt(1, this.nxcredit);
            ps.setInt(2, this.acash);
            ps.setInt(3, this.maplepoints);
            ps.setInt(4, this.points);
            ps.setInt(5, this.vpoints);
            //ps.setInt(6, this.totalvote);
            ps.setInt(6, this.accountid);
            ps.execute();
            ps.close();

            if (this.storage != null) {
                this.storage.saveToDB();
            }
            if (this.cs != null) {
                this.cs.save();
            }
            PlayerNPC.updateByCharId(this);
            this.keylayout.saveKeys(this.id);
            this.mount.saveMount(this.id);
            this.monsterbook.saveCards(this.accountid);

            deleteWhereCharacterId(con, "DELETE FROM familiars WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO familiars (characterid, expiry, name, fatigue, vitality, familiar) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, this.id);
            for (MonsterFamiliar f : this.familiars.values()) {
                ps.setLong(2, f.getExpiry());
                ps.setString(3, f.getName());
                ps.setInt(4, f.getFatigue());
                ps.setByte(5, f.getVitality());
                ps.setInt(6, f.getFamiliar());
                ps.executeUpdate();
            }
            ps.close();

            deleteWhereCharacterId(con, "DELETE FROM imps WHERE characterid = ?");
            ps = con.prepareStatement("INSERT INTO imps (characterid, itemid, closeness, fullness, state, level) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, this.id);
            for (int i = 0; i < this.imps.length; i++) {
                if (this.imps[i] != null) {
                    ps.setInt(2, this.imps[i].getItemId());
                    ps.setShort(3, this.imps[i].getCloseness());
                    ps.setShort(4, this.imps[i].getFullness());
                    ps.setByte(5, this.imps[i].getState());
                    ps.setByte(6, this.imps[i].getLevel());
                    ps.executeUpdate();
                }
            }
            ps.close();
            if (this.changed_wishlist) {
                deleteWhereCharacterId(con, "DELETE FROM wishlist WHERE characterid = ?");
                for (int i = 0; i < getWishlistSize(); i++) {
                    ps = con.prepareStatement("INSERT INTO wishlist(characterid, sn) VALUES(?, ?) ");
                    ps.setInt(1, getId());
                    ps.setInt(2, this.wishlist[i]);
                    ps.execute();
                    ps.close();
                }
            }
            if (this.changed_trocklocations) {
                deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?");
                for (int i = 0; i < this.rocks.length; i++) {
                    if (this.rocks[i] != 999999999) {
                        ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid) VALUES(?, ?) ");
                        ps.setInt(1, getId());
                        ps.setInt(2, this.rocks[i]);
                        ps.execute();
                        ps.close();
                    }
                }
            }

            if (this.changed_regrocklocations) {
                deleteWhereCharacterId(con, "DELETE FROM regrocklocations WHERE characterid = ?");
                for (int i = 0; i < this.regrocks.length; i++) {
                    if (this.regrocks[i] != 999999999) {
                        ps = con.prepareStatement("INSERT INTO regrocklocations(characterid, mapid) VALUES(?, ?) ");
                        ps.setInt(1, getId());
                        ps.setInt(2, this.regrocks[i]);
                        ps.execute();
                        ps.close();
                    }
                }
            }
            if (this.changed_hyperrocklocations) {
                deleteWhereCharacterId(con, "DELETE FROM hyperrocklocations WHERE characterid = ?");
                for (int i = 0; i < this.hyperrocks.length; i++) {
                    if (this.hyperrocks[i] != 999999999) {
                        ps = con.prepareStatement("INSERT INTO hyperrocklocations(characterid, mapid) VALUES(?, ?) ");
                        ps.setInt(1, getId());
                        ps.setInt(2, this.hyperrocks[i]);
                        ps.execute();
                        ps.close();
                    }
                }
            }
            Iterator i$;
            if (this.changed_extendedSlots) {
                deleteWhereCharacterId(con, "DELETE FROM extendedslots WHERE characterid = ?");
                for (i$ = this.extendedSlots.iterator(); i$.hasNext();) {
                    int i = ((Integer) i$.next()).intValue();
                    if (getInventory(MapleInventoryType.ETC).findById(i) != null) {
                        ps = con.prepareStatement("INSERT INTO extendedslots(characterid, itemId) VALUES(?, ?) ");
                        ps.setInt(1, getId());
                        ps.setInt(2, i);
                        ps.execute();
                        ps.close();
                    }
                }
            }
            this.changed_wishlist = false;
            this.changed_trocklocations = false;
            this.changed_regrocklocations = false;
            this.changed_hyperrocklocations = false;
            this.changed_skillmacros = false;
            this.changed_savedlocations = false;
            this.changed_questinfo = false;
            this.changed_extendedSlots = false;
            this.changed_skills = false;
            con.commit();
        } catch (Exception e) {
            FileoutputUtil.outputFileError("Log_Packet_Except.txt", e);
            e.printStackTrace();
            System.err.println(new StringBuilder().append(MapleClient.getLogMessage(this, "[charsave] Error saving character data")).append(e).toString());
            try {
                con.rollback();
            } catch (SQLException ex) {
                FileoutputUtil.outputFileError("Log_Packet_Except.txt", ex);
                System.err.println(new StringBuilder().append(MapleClient.getLogMessage(this, "[charsave] Error Rolling Back")).append(e).toString());
            }
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (pse != null) {
                    pse.close();
                }
                if (rs != null) {
                    rs.close();
                }
                con.setAutoCommit(true);
                con.setTransactionIsolation(4);
            } catch (SQLException e) {
                FileoutputUtil.outputFileError("Log_Packet_Except.txt", e);
                System.err.println(new StringBuilder().append(MapleClient.getLogMessage(this, "[charsave] Error going back to autocommit mode")).append(e).toString());
            }
        }
    }

    private void deleteWhereCharacterId(Connection con, String sql) throws SQLException {
        deleteWhereCharacterId(con, sql, this.id);
    }

    public static void deleteWhereCharacterId(Connection con, String sql, int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public static void deleteWhereCharacterId_NoLock(Connection con, String sql, int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.execute();
        ps.close();
    }

    public void saveInventory(Connection con) throws SQLException {
        List listing = new ArrayList();
        for (MapleInventory iv : this.inventory) {
            for (Item item : iv.list()) {
                listing.add(new Pair(item, iv.getType()));
            }
        }
        if (con != null) {
            ItemLoader.INVENTORY.saveItems(listing, con, this.id);
        } else {
            ItemLoader.INVENTORY.saveItems(listing, this.id);
        }
    }

    public final PlayerStats getStat() {
        return this.stats;
    }



    public final void updateInfoQuest(int questid, String data) {
        this.questinfo.put(Integer.valueOf(questid), data);
        this.changed_questinfo = true;
        this.client.getSession().write(CWvsContext.InfoPacket.updateInfoQuest(questid, data));
    }

    public final String getInfoQuest(int questid) {
        if (this.questinfo.containsKey(Integer.valueOf(questid))) {
            return (String) this.questinfo.get(Integer.valueOf(questid));
        }
        return "";
    }

    public final int getNumQuest() {
        int i = 0;
        for (MapleQuestStatus q : this.quests.values()) {
            if ((q.getStatus() == 2) && (!q.isCustom())) {
                i++;
            }
        }
        return i;
    }

    public final byte getQuestStatus(int quest) {
        MapleQuest qq = MapleQuest.getInstance(quest);
        if (getQuestNoAdd(qq) == null) {
            return 0;
        }
        return getQuestNoAdd(qq).getStatus();
    }

    public final MapleQuestStatus getQuest(MapleQuest quest) {
        if (!this.quests.containsKey(quest)) {
            return new MapleQuestStatus(quest, 0);
        }
        return (MapleQuestStatus) this.quests.get(quest);
    }

    public final void setQuestAdd(MapleQuest quest, byte status, String customData) {
        if (!this.quests.containsKey(quest)) {
            MapleQuestStatus stat = new MapleQuestStatus(quest, status);
            stat.setCustomData(customData);
            this.quests.put(quest, stat);
        }
    }

    public final MapleQuestStatus getQuestNAdd(MapleQuest quest) {
        if (!this.quests.containsKey(quest)) {
            MapleQuestStatus status = new MapleQuestStatus(quest, 0);
            this.quests.put(quest, status);
            return status;
        }
        return (MapleQuestStatus) this.quests.get(quest);
    }

    public final MapleQuestStatus getQuestNoAdd(MapleQuest quest) {
        return (MapleQuestStatus) this.quests.get(quest);
    }

    public final MapleQuestStatus getQuestRemove(MapleQuest quest) {
        return (MapleQuestStatus) this.quests.remove(quest);
    }

    public final void updateQuest(MapleQuestStatus quest) {
        updateQuest(quest, false);
    }

    public final void updateQuest(MapleQuestStatus quest, boolean update) {
        this.quests.put(quest.getQuest(), quest);
        if (!quest.isCustom()) {
            this.client.getSession().write(CWvsContext.InfoPacket.updateQuest(quest));
            if ((quest.getStatus() == 1) && (!update)) {
                this.client.getSession().write(CField.updateQuestInfo(this, quest.getQuest().getId(), quest.getNpc(), (byte) 10));
            }
        }
    }

    public final Map<Integer, String> getInfoQuest_Map() {
        return this.questinfo;
    }

    public final Map<MapleQuest, MapleQuestStatus> getQuest_Map() {
        return this.quests;
    }

    public Integer getBuffedValue(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) this.effects.get(effect);
        return mbsvh == null ? null : Integer.valueOf(mbsvh.value);
    }

    public final Integer getBuffedSkill_X(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) this.effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return Integer.valueOf(mbsvh.effect.getX());
    }

    public final Integer getBuffedSkill_Y(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) this.effects.get(effect);
        if (mbsvh == null) {
            return null;
        }
        return Integer.valueOf(mbsvh.effect.getY());
    }

    public boolean isBuffFrom(MapleBuffStat stat, Skill skill) {
        MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) this.effects.get(stat);
        if ((mbsvh == null) || (mbsvh.effect == null)) {
            return false;
        }
        return (mbsvh.effect.isSkill()) && (mbsvh.effect.getSourceId() == skill.getId());
    }

    public int getBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) this.effects.get(stat);
        return mbsvh == null ? -1 : mbsvh.effect.getSourceId();
    }

    public int getTrueBuffSource(MapleBuffStat stat) {
        MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) this.effects.get(stat);
        return mbsvh.effect.isSkill() ? mbsvh.effect.getSourceId() : mbsvh == null ? -1 : -mbsvh.effect.getSourceId();
    }

    public int getItemQuantity(int itemid, boolean checkEquipped) {
        int possesed = this.inventory[GameConstants.getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            possesed += this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return possesed;
    }

    public void setBuffedValue(MapleBuffStat effect, int value) {
        MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) this.effects.get(effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.value = value;
    }

    public void setSchedule(MapleBuffStat effect, ScheduledFuture<?> sched) {
        MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) this.effects.get(effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.schedule.cancel(false);
        mbsvh.schedule = sched;
    }

    public Long getBuffedStarttime(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) this.effects.get(effect);
        return mbsvh == null ? null : Long.valueOf(mbsvh.startTime);
    }

    public MapleStatEffect getStatForBuff(MapleBuffStat effect) {
        MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) this.effects.get(effect);
        return mbsvh == null ? null : mbsvh.effect;
    }

    public void doDragonBlood() {
        MapleStatEffect bloodEffect = getStatForBuff(MapleBuffStat.DRAGONBLOOD);
        if (bloodEffect == null) {
            this.lastDragonBloodTime = 0L;
            return;
        }
        prepareDragonBlood();
        if (this.stats.getHp() - bloodEffect.getX() <= 1) {
            cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.DRAGONBLOOD});
        } else {
            addHP(-bloodEffect.getX());
            this.client.getSession().write(CField.EffectPacket.showOwnBuffEffect(bloodEffect.getSourceId(), 7, getLevel(), bloodEffect.getLevel()));
            this.map.broadcastMessage(this, CField.EffectPacket.showBuffeffect(getId(), bloodEffect.getSourceId(), 7, getLevel(), bloodEffect.getLevel()), false);
        }
    }

    public final boolean canBlood(long now) {
        return (this.lastDragonBloodTime > 0L) && (this.lastDragonBloodTime + 4000L < now);
    }

    private void prepareDragonBlood() {
        this.lastDragonBloodTime = System.currentTimeMillis();
    }

    public void doRecovery() {
        MapleStatEffect bloodEffect = getStatForBuff(MapleBuffStat.RECOVERY);
        if (bloodEffect == null) {
            bloodEffect = getStatForBuff(MapleBuffStat.MECH_CHANGE);
            if (bloodEffect == null) {
                this.lastRecoveryTime = 0L;
                return;
            }
            if (bloodEffect.getSourceId() == 35121005) {
                prepareRecovery();
                if (this.stats.getMp() < bloodEffect.getU()) {
                    cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
                    cancelEffectFromBuffStat(MapleBuffStat.MECH_CHANGE);
                } else {
                    addMP(-bloodEffect.getU());
                }
            }
        } else {
            prepareRecovery();
            if (this.stats.getHp() >= this.stats.getCurrentMaxHp()) {
                cancelEffectFromBuffStat(MapleBuffStat.RECOVERY);
            } else {
                healHP(bloodEffect.getX());
            }
        }
    }

    public final boolean canRecover(long now) {
        return (this.lastRecoveryTime > 0L) && (this.lastRecoveryTime + 5000L < now);
    }

    private void prepareRecovery() {
        this.lastRecoveryTime = System.currentTimeMillis();
    }

    public void startMapTimeLimitTask(int time, final MapleMap to) {
        if (time <= 0) { //jail
            time = 1;
        }
        client.getSession().write(CField.getClock(time));
        final MapleMap ourMap = getMap();
        time *= 1000;
        mapTimeLimitTask = Timer.MapTimer.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if (ourMap.getId() == GameConstants.JAIL) {
                    getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_TIME)).setCustomData(String.valueOf(System.currentTimeMillis()));
                    getQuestNAdd(MapleQuest.getInstance(GameConstants.JAIL_QUEST)).setCustomData("0"); //release them!
                    changeMap(getMap(), to.getPortal(0));
                } else {
                    changeMap(to, to.getPortal(0));
                }

            }
        }, time, time);
    }

    public boolean canDOT(long now) {
        return (this.lastDOTTime > 0L) && (this.lastDOTTime + 8000L < now);
    }

    public boolean hasDOT() {
        return this.dotHP > 0;
    }

    public void doDOT() {
        addHP(-(this.dotHP * 4));
        this.dotHP = 0;
        this.lastDOTTime = 0L;
    }

    public void setDOT(int d, int source, int sourceLevel) {
        this.dotHP = d;
        addHP(-(this.dotHP * 4));
        this.map.broadcastMessage(CField.getPVPMist(this.id, source, sourceLevel, d));
        this.lastDOTTime = System.currentTimeMillis();
    }

    public void startFishingTask() {
        cancelFishingTask();
        this.lastFishingTime = System.currentTimeMillis();
    }

    public boolean canFish(long now) {
        return (this.lastFishingTime > 0L) && (this.lastFishingTime + GameConstants.getFishingTime(false, isGM()) < now);
    }

    public void doFish(long now) {
        this.lastFishingTime = now;
        if ((this.client == null) || (this.client.getPlayer() == null) || (!this.client.isReceiving()) || (!haveItem(2270008, 1, false, true)) || (!GameConstants.isFishingMap(getMapId())) || (this.chair <= 0)) {
            cancelFishingTask();
            return;
        }
        MapleInventoryManipulator.removeById(this.client, MapleInventoryType.USE, 2270008, 1, false, false);
        boolean passed = false;
        while (!passed) {
            int randval = RandomRewards.getFishingReward();
            switch (randval) {
                case 0:
                    int money = Randomizer.rand(10, 50000);
                    gainMeso(money, true);
                    passed = true;
                    break;
                case 1:
                    int experi = Math.min(Randomizer.nextInt(Math.abs((int) getNeededExp() / 250) + 1), 500000);
                    gainExp(experi, true, false, true);
                    passed = true;
                    break;
                default:
                    if (MapleItemInformationProvider.getInstance().itemExists(randval)) {
                        MapleInventoryManipulator.addById(this.client, randval, (short) 1, new StringBuilder().append("Fishing on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                        passed = true;
                    }
                    break;
            }
        }
    }

    public void cancelMapTimeLimitTask() {
        if (this.mapTimeLimitTask != null) {
            this.mapTimeLimitTask.cancel(false);
            this.mapTimeLimitTask = null;
        }
    }
    
        public static void addLinkSkill(int cid, int skill) {
        Connection con = DatabaseConnection.getConnection();
        try {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration) VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, cid);
                if (GameConstants.isApplicableSkill(skill)) { //do not save additional skills
                    ps.setInt(2, skill);
                    ps.setInt(3, 1);
                    ps.setByte(4, (byte) 1);
                    ps.setLong(5, -1);
                    ps.execute();
                }
            }
        } catch (SQLException ex) {
            System.out.println("Failed adding link skill: " + ex);
        }
    }

    public long getNeededExp() {
        return GameConstants.getExpNeededForLevel(this.level).longValue();
    }

    public void cancelFishingTask() {
        this.lastFishingTime = 0L;
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule, int from) {
        registerEffect(effect, starttime, schedule, effect.getStatups(), false, effect.getDuration(), from);
    }

    public void registerEffect(MapleStatEffect effect, long starttime, ScheduledFuture<?> schedule, Map<MapleBuffStat, Integer> statups, boolean silent, int localDuration, int cid) {
        if (effect.isHide()) {
            this.map.broadcastMessage(this, CField.removePlayerFromMap(getId()), false);
        } else if (effect.isDragonBlood()) {
            prepareDragonBlood();
        } else if (effect.isRecovery()) {
            prepareRecovery();
        } else if (effect.isBerserk()) {
            checkBerserk();
        } else if (effect.isMonsterRiding_()) {
            getMount().startSchedule();
        }
        int clonez = 0;
        for (Entry<MapleBuffStat, Integer> statup : statups.entrySet()) {
            if (statup.getKey() == MapleBuffStat.ILLUSION) {
                clonez = ((Integer) statup.getValue()).intValue();
            }
            int value = ((Integer) statup.getValue()).intValue();
            if (statup.getKey() == MapleBuffStat.MONSTER_RIDING) {
                if ((effect.getSourceId() == 5221006) && (this.battleshipHP <= 0)) {
                    this.battleshipHP = maxBattleshipHP(effect.getSourceId());
                }
                removeFamiliar();
            }
            this.effects.put(statup.getKey(), new MapleBuffStatValueHolder(effect, starttime, schedule, value, localDuration, cid));
        }
        if (clonez > 0) {
            int cloneSize = Math.max(getNumClones(), getCloneSize());
            if (clonez > cloneSize) {
                for (int i = 0; i < clonez - cloneSize; i++) {
                    cloneLook();
                }
            }
        }
        if (!silent) {
            this.stats.recalcLocalStats(this);
        }
    }

    public List<MapleBuffStat> getBuffStats(MapleStatEffect effect, long startTime) {
        List bstats = new ArrayList();
        Map<MapleBuffStat, MapleBuffStatValueHolder> allBuffs = new EnumMap(this.effects);
        for (Map.Entry stateffect : allBuffs.entrySet()) {
            MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) stateffect.getValue();
            if ((mbsvh.effect.sameSource(effect)) && ((startTime == -1L) || (startTime == mbsvh.startTime) || (((MapleBuffStat) stateffect.getKey()).canStack()))) {
                bstats.add(stateffect.getKey());
            }
        }
        return bstats;
    }

    private boolean deregisterBuffStats(List<MapleBuffStat> stats) {
        boolean clonez = false;
        List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList(stats.size());
        for (MapleBuffStat stat : stats) {
            MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder) this.effects.remove(stat);
            if (mbsvh != null) {
                boolean addMbsvh = true;
                for (MapleBuffStatValueHolder contained : effectsToCancel) {
                    if ((mbsvh.startTime == contained.startTime) && (contained.effect == mbsvh.effect)) {
                        addMbsvh = false;
                    }
                }
                if (addMbsvh) {
                    effectsToCancel.add(mbsvh);
                }
                if ((stat == MapleBuffStat.SUMMON) || (stat == MapleBuffStat.PUPPET) || (stat == MapleBuffStat.REAPER) || (stat == MapleBuffStat.BEHOLDER) || (stat == MapleBuffStat.DAMAGE_BUFF) || (stat == MapleBuffStat.RAINING_MINES) || (stat == MapleBuffStat.ANGEL_ATK)) {
                    int summonId = mbsvh.effect.getSourceId();
                    List<MapleSummon> toRemove = new ArrayList();
                    this.visibleMapObjectsLock.writeLock().lock();
                    this.summonsLock.writeLock().lock();
                    try {
                        for (MapleSummon summon : this.summons) {
                            if ((summon.getSkill() == summonId) || ((stat == MapleBuffStat.RAINING_MINES) && (summonId == 33101008)) || ((summonId == 35121009) && (summon.getSkill() == 35121011)) || (((summonId != 86) && (summonId != 88) && (summonId != 91)) || ((summon.getSkill() == summonId + 999) || (((summonId == 1085) || (summonId == 80001262) || (summonId == 1262) || (summonId == 1087) || (summonId == 1090) || (summonId == 1179)) && (summon.getSkill() == summonId - 999))))) {
                                this.map.broadcastMessage(CField.SummonPacket.removeSummon(summon, true));
                                this.map.removeMapObject(summon);
                                this.visibleMapObjects.remove(summon);
                                toRemove.add(summon);
                            }
                        }
                        for (MapleSummon s : toRemove) {
                            this.summons.remove(s);
                        }
                    } finally {
                        this.summonsLock.writeLock().unlock();
                        this.visibleMapObjectsLock.writeLock().unlock();
                    }
                    if ((summonId == 3111005) || (summonId == 3211005)) {
                        cancelEffectFromBuffStat(MapleBuffStat.SPIRIT_LINK);
                    }
                } else if (stat == MapleBuffStat.DRAGONBLOOD) {
                    this.lastDragonBloodTime = 0L;
                } else if ((stat == MapleBuffStat.RECOVERY) || (mbsvh.effect.getSourceId() == 35121005)) {
                    this.lastRecoveryTime = 0L;
                } else if ((stat == MapleBuffStat.HOMING_BEACON) || (stat == MapleBuffStat.ARCANE_AIM)) {
                    this.linkMobs.clear();
                } else if (stat == MapleBuffStat.ILLUSION) {
                    disposeClones();
                    clonez = true;
                }
            }
        }
        for (MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
            if ((getBuffStats(cancelEffectCancelTasks.effect, cancelEffectCancelTasks.startTime).isEmpty()) && (cancelEffectCancelTasks.schedule != null)) {
                cancelEffectCancelTasks.schedule.cancel(false);
            }
        }

        return clonez;
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime) {
        if (effect == null) {
            return;
        }
        cancelEffect(effect, overwrite, startTime, effect.getStatups());
    }

    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime, Map<MapleBuffStat, Integer> statups) {
        if (effect == null) {
            return;
        }
        List buffstats;
        if (!overwrite) {
            buffstats = getBuffStats(effect, startTime);
        } else {
            buffstats = new ArrayList(statups.keySet());
        }

        if (buffstats.size() <= 0) {
            return;
        }
        if ((effect.isInfinity()) && (getBuffedValue(MapleBuffStat.INFINITY) != null)) {
            int duration = Math.max(effect.getDuration(), effect.alchemistModifyVal(this, effect.getDuration(), false));
            long start = getBuffedStarttime(MapleBuffStat.INFINITY).longValue();
            duration += (int) (start - System.currentTimeMillis());
            if (duration > 0) {
                int neworbcount = getBuffedValue(MapleBuffStat.INFINITY).intValue() + effect.getDamage();
                Map stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.INFINITY, Integer.valueOf(neworbcount));
                setBuffedValue(MapleBuffStat.INFINITY, neworbcount);
                this.client.getSession().write(CWvsContext.BuffPacket.giveBuff(effect.getSourceId(), duration, stat, effect));
                addHP((int) (effect.getHpR() * this.stats.getCurrentMaxHp()));
                addMP((int) (effect.getMpR() * this.stats.getCurrentMaxMp(getJob())));
                setSchedule(MapleBuffStat.INFINITY, Timer.BuffTimer.getInstance().schedule(new MapleStatEffect.CancelEffectAction(this, effect, start, stat), effect.alchemistModifyVal(this, 4000, false)));
                return;
            }
        }
        boolean clonez = deregisterBuffStats(buffstats);
        if (effect.isMagicDoor()) {
            if (!getDoors().isEmpty()) {
                removeDoor();
                silentPartyUpdate();
            }
        } else if (effect.isMechDoor()) {
            if (!getMechDoors().isEmpty()) {
                removeMechDoor();
            }
        } else if (effect.isMonsterRiding_()) {
            getMount().cancelSchedule();
        } else if (effect.isMonsterRiding()) {
            cancelEffectFromBuffStat(MapleBuffStat.MECH_CHANGE);
        } else if (effect.isAranCombo()) {
            this.combo = 0;
        }

        cancelPlayerBuffs(buffstats, overwrite);
        if ((!overwrite)
                && (effect.isHide()) && (this.client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null)) {
            this.map.broadcastMessage(this, CField.spawnPlayerMapobject(this), false);

            for (MaplePet pet : this.pets) {
                if (pet.getSummoned()) {
                    this.map.broadcastMessage(this, PetPacket.showPet(this, pet, false, false), false);
                }
            }
            for (WeakReference chr : this.clones) {
                if (chr.get() != null) {
                    this.map.broadcastMessage((MapleCharacter) chr.get(), CField.spawnPlayerMapobject((MapleCharacter) chr.get()), false);
                }
            }
        }

        if ((effect.getSourceId() == 35121013) && (!overwrite)) {
            SkillFactory.getSkill(35121005).getEffect(getTotalSkillLevel(35121005)).applyTo(this);
        }
        if (!clonez) {
            for (WeakReference chr : this.clones) {
                if (chr.get() != null) {
                    ((MapleCharacter) chr.get()).cancelEffect(effect, overwrite, startTime);
                }
            }
        }
    }

    public void cancelBuffStats(MapleBuffStat[] stat) {
        List buffStatList = Arrays.asList(stat);
        deregisterBuffStats(buffStatList);
        cancelPlayerBuffs(buffStatList, false);
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat) {
        if (this.effects.get(stat) != null) {
            cancelEffect(((MapleBuffStatValueHolder) this.effects.get(stat)).effect, false, -1L);
        }
    }

    public void cancelEffectFromBuffStat(MapleBuffStat stat, int from) {
        if ((this.effects.get(stat) != null) && (((MapleBuffStatValueHolder) this.effects.get(stat)).cid == from)) {
            cancelEffect(((MapleBuffStatValueHolder) this.effects.get(stat)).effect, false, -1L);
        }
    }

    private void cancelPlayerBuffs(List<MapleBuffStat> buffstats, boolean overwrite) {
        boolean write = (this.client != null) && (this.client.getChannelServer() != null) && (this.client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null);
        if (buffstats.contains(MapleBuffStat.HOMING_BEACON)) {
            this.client.getSession().write(CWvsContext.BuffPacket.cancelHoming());
        } else {
            if (overwrite) {
                List z = new ArrayList();
                for (MapleBuffStat s : buffstats) {
                    if (s.canStack()) {
                        z.add(s);
                    }
                }
                if (z.size() > 0) {
                    buffstats = z;
                } else {
                    return;
                }
            } else if (write) {
                this.stats.recalcLocalStats(this);
            }
            if ((buffstats.contains(MapleBuffStat.MORPH)) && (GameConstants.kaiser(getJob()))) {
                resetKaiserCombo();
            }
            this.client.getSession().write(CWvsContext.BuffPacket.cancelBuff(buffstats));
            map.broadcastMessage(this, CWvsContext.BuffPacket.cancelForeignBuff(getId(), buffstats), false);

            //   this.map.broadcastMessage(this, CWvsContext.BuffPacket.cancelForeignBuff(getId(), buffstats), false);
        }
    }

    public void dispel() {
        if (!isHidden()) {
            LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList(this.effects.values());
            for (MapleBuffStatValueHolder mbsvh : allBuffs) {
                if ((mbsvh.effect.isSkill()) && (mbsvh.schedule != null) && (!mbsvh.effect.isMorph()) && (!mbsvh.effect.isGmBuff()) && (!mbsvh.effect.isMonsterRiding()) && (!mbsvh.effect.isMechChange()) && (!mbsvh.effect.isEnergyCharge()) && (!mbsvh.effect.isAranCombo())) {
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            }
        }
    }
public final void QuestInfoPacket(final MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort(questinfo.size());
        for (final Entry<Integer, String> q : questinfo.entrySet()) {
            mplew.writeShort(q.getKey());
            mplew.writeMapleAsciiString(q.getValue() == null ? "" : q.getValue());
        }
}
        
    public void dispelSkill(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList(this.effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if ((mbsvh.effect.isSkill()) && (mbsvh.effect.getSourceId() == skillid)) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }

    public void dispelSummons() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList(this.effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.getSummonMovementType() != null) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
            }
        }
    }

    public void dispelBuff(int skillid) {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList(this.effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.getSourceId() == skillid) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }

    public void cancelAllBuffs_() {
        this.effects.clear();
    }

    public void cancelAllBuffs() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList(this.effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            cancelEffect(mbsvh.effect, false, mbsvh.startTime);
        }
    }

    public void cancelMorphs() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList(this.effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            switch (mbsvh.effect.getSourceId()) {
                case 5111005:
                case 5121003:
                case 13111005:
                case 15111002:
                case 61111008:
                case 61120008:
                    return;
            }
            if (mbsvh.effect.isMorph()) {
                if (MapConstants.isStorylineMap(getMapId())) {
                    return;
                }
                if (mbsvh.effect.isMorph()) {
                    disposeClones();
                    cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            }
        }
    }

    public int getMorphState() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList(this.effects.values());
        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMorph()) {
                return mbsvh.effect.getSourceId();
            }
        }
        return -1;
    }

    public void silentGiveBuffs(List<PlayerBuffValueHolder> buffs) {
        if (buffs == null) {
            return;
        }
        for (PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime, mbsvh.localDuration, mbsvh.statup, mbsvh.cid);
        }
    }

    public List<PlayerBuffValueHolder> getAllBuffs() {
        final List<PlayerBuffValueHolder> ret = new ArrayList<PlayerBuffValueHolder>();
        final Map<Pair<Integer, Byte>, Integer> alreadyDone = new HashMap<Pair<Integer, Byte>, Integer>();
        final LinkedList<Entry<MapleBuffStat, MapleBuffStatValueHolder>> allBuffs = new LinkedList<Entry<MapleBuffStat, MapleBuffStatValueHolder>>(effects.entrySet());
        for (Entry<MapleBuffStat, MapleBuffStatValueHolder> mbsvh : allBuffs) {
            final Pair<Integer, Byte> key = new Pair<Integer, Byte>(mbsvh.getValue().effect.getSourceId(), mbsvh.getValue().effect.getLevel());
            if (alreadyDone.containsKey(key)) {
                ret.get(alreadyDone.get(key)).statup.put(mbsvh.getKey(), mbsvh.getValue().value);
            } else {
                alreadyDone.put(key, ret.size());
                final EnumMap<MapleBuffStat, Integer> list = new EnumMap<MapleBuffStat, Integer>(MapleBuffStat.class);
                list.put(mbsvh.getKey(), mbsvh.getValue().value);
                ret.add(new PlayerBuffValueHolder(mbsvh.getValue().startTime, mbsvh.getValue().effect, list, mbsvh.getValue().localDuration, mbsvh.getValue().cid));
            }
        }
        return ret;
    }

    public void cancelMagicDoor() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList(this.effects.values());

        for (MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMagicDoor()) {
                cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }

    public int getSkillLevel(int skillid) {
        return getSkillLevel(SkillFactory.getSkill(skillid));
    }

    public int getTotalSkillLevel(int skillid) {
        if (GameConstants.iskaiser_Transfiguration_Skill(skillid)) {
            return SkillFactory.getSkill(skillid).getMaxLevel();
        }
        return getTotalSkillLevel(SkillFactory.getSkill(skillid));
    }

    public final void handleEnergyCharge(int skillid, int targets) {
        Skill echskill = SkillFactory.getSkill(skillid);
        int skilllevel = getTotalSkillLevel(echskill);
        if (skilllevel > 0) {
            MapleStatEffect echeff = echskill.getEffect(skilllevel);
            if (targets > 0) {
                if (getBuffedValue(MapleBuffStat.ENERGY_CHARGE) == null) {
                    echeff.applyEnergyBuff(this, true, targets);
                } else {
                    Integer energyLevel = getBuffedValue(MapleBuffStat.ENERGY_CHARGE);

                    if (energyLevel.intValue() < 10000) {
                        energyLevel = Integer.valueOf(energyLevel.intValue() + echeff.getX() * targets);

                        this.client.getSession().write(CField.EffectPacket.showOwnBuffEffect(skillid, 2, getLevel(), skilllevel));
                        this.map.broadcastMessage(this, CField.EffectPacket.showBuffeffect(this.id, skillid, 2, getLevel(), skilllevel), false);

                        if (energyLevel.intValue() >= 10000) {
                            energyLevel = Integer.valueOf(10000);
                        }
                        this.client.getSession().write(CWvsContext.BuffPacket.giveEnergyChargeTest(energyLevel.intValue(), echeff.getDuration() / 1000));
                        setBuffedValue(MapleBuffStat.ENERGY_CHARGE, Integer.valueOf(energyLevel.intValue()).intValue());
                    } else if (energyLevel.intValue() == 10000) {
                        echeff.applyEnergyBuff(this, false, targets);
                        setBuffedValue(MapleBuffStat.ENERGY_CHARGE, Integer.valueOf(10001).intValue());
                    }
                }
            }
        }
    }

    public final void handleBattleshipHP(int damage) {
        if (damage < 0) {
            MapleStatEffect effect = getStatForBuff(MapleBuffStat.MONSTER_RIDING);
            if ((effect != null) && (effect.getSourceId() == 5221006)) {
                this.battleshipHP += damage;
                this.client.getSession().write(CField.skillCooldown(5221999, this.battleshipHP / 10));
                if (this.battleshipHP <= 0) {
                    this.battleshipHP = 0;
                    this.client.getSession().write(CField.skillCooldown(5221006, effect.getCooldown(this)));
                    addCooldown(5221006, System.currentTimeMillis(), effect.getCooldown(this) * 1000);
                    cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
                }
            }
        }
    }

    public final void handleOrbgain() {
        int orbcount = getBuffedValue(MapleBuffStat.COMBO).intValue();
        Skill combo;
        Skill advcombo;
        switch (getJob()) {
            case 1110:
            case 1111:
            case 1112:
                combo = SkillFactory.getSkill(11111001);
                advcombo = SkillFactory.getSkill(11110005);
                break;
            default:
                combo = SkillFactory.getSkill(1111002);
                advcombo = SkillFactory.getSkill(1120003);
        }

        MapleStatEffect ceffect = null;
        int advComboSkillLevel = getTotalSkillLevel(advcombo);
        if (advComboSkillLevel > 0) {
            ceffect = advcombo.getEffect(advComboSkillLevel);
        } else if (getSkillLevel(combo) > 0) {
            ceffect = combo.getEffect(getTotalSkillLevel(combo));
        } else {
            return;
        }

        if (orbcount < ceffect.getX() + 1) {
            int neworbcount = orbcount + 1;
            if ((advComboSkillLevel > 0) && (ceffect.makeChanceResult())
                    && (neworbcount < ceffect.getX() + 1)) {
                neworbcount++;
            }

            EnumMap stat = new EnumMap(MapleBuffStat.class);
            stat.put(MapleBuffStat.COMBO, Integer.valueOf(neworbcount));
            setBuffedValue(MapleBuffStat.COMBO, neworbcount);
            int duration = ceffect.getDuration();
            duration += (int) (getBuffedStarttime(MapleBuffStat.COMBO).longValue() - System.currentTimeMillis());

            this.client.getSession().write(CWvsContext.BuffPacket.giveBuff(combo.getId(), duration, stat, ceffect));
            this.map.broadcastMessage(this, CWvsContext.BuffPacket.giveForeignBuff(getId(), stat, ceffect), false);
        }
    }

    public void handleOrbconsume(int howmany) {
        Skill combo;
        switch (getJob()) {
            case 1110:
            case 1111:
            case 1112:
                combo = SkillFactory.getSkill(11111001);
                break;
            default:
                combo = SkillFactory.getSkill(1111002);
        }

        if (getSkillLevel(combo) <= 0) {
            return;
        }
        MapleStatEffect ceffect = getStatForBuff(MapleBuffStat.COMBO);
        if (ceffect == null) {
            return;
        }
        EnumMap stat = new EnumMap(MapleBuffStat.class);
        stat.put(MapleBuffStat.COMBO, Integer.valueOf(Math.max(1, getBuffedValue(MapleBuffStat.COMBO).intValue() - howmany)));
        setBuffedValue(MapleBuffStat.COMBO, Math.max(1, getBuffedValue(MapleBuffStat.COMBO).intValue() - howmany));
        int duration = ceffect.getDuration();
        duration += (int) (getBuffedStarttime(MapleBuffStat.COMBO).longValue() - System.currentTimeMillis());

        this.client.getSession().write(CWvsContext.BuffPacket.giveBuff(combo.getId(), duration, stat, ceffect));
        this.map.broadcastMessage(this, CWvsContext.BuffPacket.giveForeignBuff(getId(), stat, ceffect), false);
    }

    public void silentEnforceMaxHpMp() {
        this.stats.setMp(this.stats.getMp(), this);
        this.stats.setHp(this.stats.getHp(), true, this);
    }

    public void enforceMaxHpMp() {
        Map statups = new EnumMap(MapleStat.class);
        if (this.stats.getMp() > this.stats.getCurrentMaxMp(getJob())) {
            this.stats.setMp(this.stats.getMp(), this);
            statups.put(MapleStat.MP, Long.valueOf(this.stats.getMp()));
        }
        if (this.stats.getHp() > this.stats.getCurrentMaxHp()) {
            this.stats.setHp(this.stats.getHp(), this);
            statups.put(MapleStat.HP, Long.valueOf(this.stats.getHp()));
        }
        if (statups.size() > 0) {
            client.getSession().write(CWvsContext.updatePlayerStats(statups, this));
        }
    }

    public MapleMap getMap() {
        return this.map;
    }

    public MonsterBook getMonsterBook() {
        return this.monsterbook;
    }

    public void setMap(MapleMap newmap) {
        this.map = newmap;
    }

    public void setMap(int PmapId) {
        this.mapid = PmapId;
    }

    public int getMapId() {
        if (this.map != null) {
            return this.map.getId();
        }
        return this.mapid;
    }

    public byte getInitialSpawnpoint() {
        return this.initialSpawnPoint;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public final String getBlessOfFairyOrigin() {
        return this.BlessOfFairy_Origin;
    }

    public final String getBlessOfEmpressOrigin() {
        return this.BlessOfEmpress_Origin;
    }

    public final short getLevel() {
        return this.level;
    }

    public final int getFame() {
        return this.fame;
    }

    public final int getFallCounter() {
        return this.fallcounter;
    }

    public final MapleClient getClient() {
        return this.client;
    }

    public final void setClient(MapleClient client) {
        this.client = client;
    }

    public long getExp() {
        return exp.get();
    }

    public int getRemainingAp() {
        return this.remainingAp;
    }

    public int getRemainingSp() {
        return this.remainingSp[GameConstants.getSkillBook(this.job)];
    }

    public int getRemainingSp(int skillbook) {
        return this.remainingSp[skillbook];
    }

    public int[] getRemainingSps() {
        return this.remainingSp;
    }

    public int getRemainingSpSize() {
        int ret = 0;
        for (int i = 0; i < this.remainingSp.length; i++) {
            if (this.remainingSp[i] > 0) {
                ret++;
            }
        }
        return ret;
    }

    public int getHpApUsed() {
        return this.hpApUsed;
    }

    public boolean isHidden() {
        return getBuffSource(MapleBuffStat.DARKSIGHT) / 1000000 == 9;
    }

    public void setHpApUsed(int hpApUsed) {
        this.hpApUsed = hpApUsed;
    }

    public byte getSkinColor() {
        return this.skinColor;
    }

    public void setSkinColor(byte skinColor) {
        this.skinColor = skinColor;
    }

    public short getJob() {
        return this.job;
    }

    public byte getGender() {
        return this.gender;
    }

    public int getHair() {
        return this.hair;
    }

    public int getFace() {
        return this.face;
    }

    public int getDemonMarking() {
        return this.demonMarking;
    }

    public void setDemonMarking(int mark) {
        this.demonMarking = mark;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExp(long amount) {
        this.exp.set(amount);
    }

    public void setHair(int hair) {
        this.hair = hair;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public void setFame(int fame) {
        this.fame = fame;
    }

    public void setFallCounter(int fallcounter) {
        this.fallcounter = fallcounter;
    }

    public Point getOldPosition() {
        return this.old;
    }

    public void setOldPosition(Point x) {
        this.old = x;
    }

    public void setRemainingAp(int remainingAp) {
        this.remainingAp = remainingAp;
    }

    public void setRemainingSp(int remainingSp) {
        this.remainingSp[GameConstants.getSkillBook(this.job)] = remainingSp;
    }

    public void setRemainingSp(int remainingSp, int skillbook) {
        this.remainingSp[skillbook] = remainingSp;
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public void setInvincible(boolean invinc) {
        this.invincible = invinc;
    }

    public boolean isInvincible() {
        return this.invincible;
    }

    public BuddyList getBuddylist() {
        return this.buddylist;
    }

    public void addFame(int famechange) {
        this.fame += famechange;
        getTrait(MapleTrait.MapleTraitType.charm).addLocalExp(famechange);
    }

    public void updateFame() {
        updateSingleStat(MapleStat.FAME, this.fame);
    }

    public void changeMapBanish(int mapid, String portal, String msg) {
        dropMessage(5, msg);
        MapleMap map = this.client.getChannelServer().getMapFactory().getMap(mapid);
        changeMap(map, map.getPortal(portal));
    }

    public void changeMap(MapleMap to, Point pos) {
        changeMapInternal(to, pos, CField.getWarpToMap(to, 128, this), null);
    }

    public void changeMap(MapleMap to) {
        changeMapInternal(to, to.getPortal(0).getPosition(), CField.getWarpToMap(to, 0, this), to.getPortal(0));
    }

    public void changeMap(MapleMap to, MaplePortal pto) {
        changeMapInternal(to, pto.getPosition(), CField.getWarpToMap(to, pto.getId(), this), null);
    }

    public void changeMapPortal(MapleMap to, MaplePortal pto) {
        changeMapInternal(to, pto.getPosition(), CField.getWarpToMap(to, pto.getId(), this), pto);
    }

    private void changeMapInternal(MapleMap to, Point pos, byte[] warpPacket, MaplePortal pto) {
        if (to == null) {
            return;
        }
        int nowmapid = this.map.getId();
        if (this.eventInstance != null) {
            this.eventInstance.changedMap(this, to.getId());
        }
        boolean pyramid = this.pyramidSubway != null;
        if (this.map.getId() == nowmapid) {
            this.client.getSession().write(warpPacket);
            boolean shouldChange = (!isClone()) && (this.client.getChannelServer().getPlayerStorage().getCharacterById(getId()) != null);
            boolean shouldState = this.map.getId() == to.getId();
            if ((shouldChange) && (shouldState)) {
                to.setCheckStates(false);
            }
            this.map.removePlayer(this);
            if (shouldChange) {
                this.map = to;
                setPosition(pos);
                to.addPlayer(this);
                this.stats.relocHeal(this);
                if (shouldState) {
                    to.setCheckStates(true);
                }
            }
        }
        if ((pyramid) && (this.pyramidSubway != null)) {
            this.pyramidSubway.onChangeMap(this, to.getId());
        }
    }

    public void cancelChallenge() {
        if ((this.challenge != 0) && (this.client.getChannelServer() != null)) {
            MapleCharacter chr = this.client.getChannelServer().getPlayerStorage().getCharacterById(this.challenge);
            if (chr != null) {
                chr.dropMessage(6, new StringBuilder().append(getName()).append(" has denied your request.").toString());
                chr.setChallenge(0);
            }
            dropMessage(6, "Denied the challenge.");
            this.challenge = 0;
        }
    }

    public void leaveMap(MapleMap map) {
        this.controlledLock.writeLock().lock();
        this.visibleMapObjectsLock.writeLock().lock();
        try {
            for (MapleMonster mons : this.controlled) {
                if (mons != null) {
                    mons.setController(null);
                    mons.setControllerHasAggro(false);
                    map.updateMonsterController(mons);
                }
            }
            this.controlled.clear();
            this.visibleMapObjects.clear();
        } finally {
            this.controlledLock.writeLock().unlock();
            this.visibleMapObjectsLock.writeLock().unlock();
        }
        if (this.chair != 0) {
            this.chair = 0;
        }
        clearLinkMid();
        cancelFishingTask();
        cancelChallenge();
        if (!getMechDoors().isEmpty()) {
            removeMechDoor();
        }
        cancelMapTimeLimitTask();
        if (getTrade() != null) {
            MapleTrade.cancelTrade(getTrade(), this.client, this);
        }
    }

    public void changeJob(short newJob) {
        try {
            cancelEffectFromBuffStat(MapleBuffStat.SHADOWPARTNER);
            this.job = newJob;
            updateSingleStat(MapleStat.JOB, newJob);
            maxSkillsByJob();
            maxRiding();
            maxLead();
            maxDA();
            if (GameConstants.isPhantom(this.job)) {
                this.client.getSession().write(CField.updateCardStack(0));
                resetRunningStack();
            }



            int maxhp = this.stats.getMaxHp();
            int maxmp = this.stats.getMaxMp();

            switch (this.job) {
                case 100:
                case 1100:
                case 2100:
                case 3200:
                case 5000:
                    maxhp += Randomizer.rand(200, 250);
                    break;
                case 3100:
                    maxhp += Randomizer.rand(200, 250);
                    break;
                case 3110:
                    maxhp += Randomizer.rand(300, 350);
                    break;
                case 200:
                case 2200:
                case 2210:
                    maxmp += Randomizer.rand(100, 150);
                    break;
                case 300:
                case 400:
                case 500:
                case 2300:
                case 3300:
                case 3500:
                    maxhp += Randomizer.rand(100, 150);
                    maxmp += Randomizer.rand(25, 50);
                    break;
                case 110:
                case 120:
                case 130:
                case 1110:
                case 2110:
                case 3210:
                    maxhp += Randomizer.rand(300, 350);
                    break;
                case 210:
                case 220:
                case 230:
                    maxmp += Randomizer.rand(400, 450);
                    break;
                case 310:
                case 320:
                case 410:
                case 420:
                case 430:
                case 510:
                case 520:
                case 530:
                case 1310:
                case 1410:
                case 2310:
                case 3310:
                case 3510:
                    maxhp += Randomizer.rand(200, 250);
                    maxhp += Randomizer.rand(150, 200);
                    break;
                case 800:
                case 910:
                    maxhp += 500000;
                    maxmp += 500000;
            }

            if (maxhp >= 500000) {
                maxhp = 500000;
            }
            if (maxmp >= 500000) {
                maxmp = 500000;
            }
            if (GameConstants.isDemon(this.job)) {
                maxmp = GameConstants.getMPByJob(this.job);
            }
            this.stats.setInfo(maxhp, maxmp, maxhp, maxmp);
            Map statup = new EnumMap(MapleStat.class);
            statup.put(MapleStat.MAXHP, Long.valueOf(maxhp));
            statup.put(MapleStat.MAXMP, Long.valueOf(maxmp));
            statup.put(MapleStat.HP, Long.valueOf(maxhp));
            statup.put(MapleStat.MP, Long.valueOf(maxmp));
            this.characterCard.recalcLocalStats(this);
            this.stats.recalcLocalStats(this);
            client.getSession().write(CWvsContext.updatePlayerStats(statup, this));
            this.map.broadcastMessage(this, CField.EffectPacket.showForeignEffect(getId(), 11), false);
            silentPartyUpdate();
            guildUpdate();
            familyUpdate();
            if (this.dragon != null) {
                this.map.broadcastMessage(CField.removeDragon(this.id));
                this.dragon = null;
            }
            if (this.Haku != null) {
                this.Haku = null;
            }
            if ((newJob >= 2200) && (newJob <= 2218)) {
                if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
                    cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.MONSTER_RIDING});
                }
                makeDragon();
            }
            if (((newJob >= 4200) && (newJob <= 4212)) || (newJob == 4002)) {
                if (getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null) {
                    cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.MONSTER_RIDING});
                }
                makeHaku();
            }
        } catch (Exception e) {
            FileoutputUtil.outputFileError("Log_Script_Except.txt", e);
        }
    }

    public void equipitem(int itemId) {
        Item used = getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
        if (used != null) {
            MapleInventoryManipulator.unequip(this.client, (short) -10, getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
        }
        short slot = (short) MapleInventoryManipulator.addId(this.client, itemId);
        MapleInventoryManipulator.equip(this.client, slot, (short) -10);
        reloadC();
    }

    public void reloadC() {
        getClient().getSession().write(CField.getCharInfo(this));
        getMap().removePlayer(this);
        getMap().addPlayer(this);
    }

    public void makeDragon() {
        this.dragon = new MapleDragon(this);
        this.map.broadcastMessage(CField.spawnDragon(this.dragon));
    }

    public MapleDragon getDragon() {
        return this.dragon;
    }

    public void makeHaku() {
        this.Haku = new MapleHaku(this);
        this.map.broadcastMessage(CField.spawnHaku(this.Haku));
        if (getHaku() != null && this.getBuffedValue(MapleBuffStat.Haku_Reborn) != null) {
            getHaku().sendstats();
            getMap().broadcastMessage(this, CField.spawnHaku_change0(this.getId()), true);
            getMap().broadcastMessage(this, CField.spawnHaku_change1(this.getHaku()), true);
            getMap().broadcastMessage(this, CField.spawnHaku_bianshen(this.getId(), this.getHaku().getObjectId(), this.getHaku().getstats()), true);
        }
    }

    public MapleHaku getHaku() {
        return this.Haku;
    }

    public void gainAp(int ap) {
        this.remainingAp += ap;
    }

    public void gainSP(int sp) {
        this.remainingSp[GameConstants.getSkillBook(this.job)] += sp;
        updateSingleStat(MapleStat.AVAILABLESP, 0L);
        this.client.getSession().write(CWvsContext.InfoPacket.getSPMsg((byte) sp, this.job));
    }

    public void gainSP(int sp, int skillbook) {
        this.remainingSp[skillbook] += sp;
        updateSingleStat(MapleStat.AVAILABLESP, 0L);
        this.client.getSession().write(CWvsContext.InfoPacket.getSPMsg((byte) sp, (short) 0));
    }

    public void resetSP(int sp) {
        for (int i = 0; i < this.remainingSp.length; i++) {
            this.remainingSp[i] = sp;
        }
        updateSingleStat(MapleStat.AVAILABLESP, 0L);
    }

    public void resetAPSP() {
        resetSP(0);
        gainAp(-this.remainingAp);
    }

    public void setHonourExp(int exp) {
        this.honourExp = exp;
    }

    public int getHonourExp() {
        return this.honourExp;
    }

    public void setHonourLevel(int level) {
        this.honourLevel = level;
    }

    public int getHonourLevel() {
        if (this.honourLevel == 0) {
            this.honourLevel = 1;
        }
        return this.honourLevel;
    }

    public List<InnerSkillValueHolder> getInnerSkills() {
        return this.innerSkills;
    }

    public List<Integer> getProfessions() {
        List prof = new ArrayList();
        for (int i = 9200; i <= 9204; i++) {
            if (getProfessionLevel(this.id * 10000) > 0) {
                prof.add(Integer.valueOf(i));
            }
        }
        return prof;
    }

    public byte getProfessionLevel(int id) {
        int ret = getSkillLevel(id);
        if (ret <= 0) {
            return 0;
        }
        return (byte) (ret >>> 24 & 0xFF);
    }

    public short getProfessionExp(int id) {
        int ret = getSkillLevel(id);
        if (ret <= 0) {
            return 0;
        }
        return (short) (ret & 0xFFFF);
    }

    public boolean addProfessionExp(int id, int expGain) {
        int ret = getProfessionLevel(id);
        if ((ret <= 0) || (ret >= 10)) {
            return false;
        }
        int newExp = getProfessionExp(id) + expGain;
        if (newExp >= GameConstants.getProfessionEXP(ret)) {
            changeProfessionLevelExp(id, ret + 1, newExp - GameConstants.getProfessionEXP(ret));
            int traitGain = (int) Math.pow(2.0D, ret + 1);
            switch (id) {
                case 92000000:
                    ((MapleTrait) this.traits.get(MapleTrait.MapleTraitType.sense)).addExp(traitGain, this);
                    break;
                case 92010000:
                    ((MapleTrait) this.traits.get(MapleTrait.MapleTraitType.will)).addExp(traitGain, this);
                    break;
                case 92020000:
                case 92030000:
                case 92040000:
                    ((MapleTrait) this.traits.get(MapleTrait.MapleTraitType.craft)).addExp(traitGain, this);
            }

            return true;
        }
        changeProfessionLevelExp(id, ret, newExp);
        return false;
    }

    public void changeProfessionLevelExp(int id, int level, int exp) {
        changeSingleSkillLevel(SkillFactory.getSkill(id), ((level & 0xFF) << 24) + (exp & 0xFFFF), (byte) 10);
    }

    public void changeSingleSkillLevel(Skill skill, int newLevel, byte newMasterlevel) {
        if (skill == null) {
            return;
        }
        changeSingleSkillLevel(skill, newLevel, newMasterlevel, SkillFactory.getDefaultSExpiry(skill));
    }

    public void changeSingleSkillLevel(Skill skill, int newLevel, byte newMasterlevel, long expiration) {
        Map list = new HashMap();
        boolean hasRecovery = false;
        boolean recalculate = false;
        if (changeSkillData(skill, newLevel, newMasterlevel, expiration)) {
            list.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
            if (GameConstants.isRecoveryIncSkill(skill.getId())) {
                hasRecovery = true;
            }
            if (skill.getId() < 80000000) {
                recalculate = true;
            }
        }
        if (list.isEmpty()) {
            return;
        }
        this.client.getSession().write(CWvsContext.updateSkills(list));
        reUpdateStat(hasRecovery, recalculate);
    }

    public void changeSkillsLevel(Map<Skill, SkillEntry> ss) {
        if (ss.isEmpty()) {
            return;
        }

        Map list = new HashMap();
        boolean hasRecovery = false;
        boolean recalculate = false;
        for (Map.Entry data : ss.entrySet()) {
            if (changeSkillData((Skill) data.getKey(), ((SkillEntry) data.getValue()).skillevel, ((SkillEntry) data.getValue()).masterlevel, ((SkillEntry) data.getValue()).expiration)) {
                list.put(data.getKey(), data.getValue());
                if (GameConstants.isRecoveryIncSkill(((Skill) data.getKey()).getId())) {
                    hasRecovery = true;
                }
                if (((Skill) data.getKey()).getId() < 80000000) {
                    recalculate = true;
                }
            }
        }
        if (list.isEmpty()) {
            return;
        }
        this.client.getSession().write(CWvsContext.updateSkills(list));
        reUpdateStat(hasRecovery, recalculate);
    }

    public void reUpdateStat(boolean hasRecovery, boolean recalculate) {
        this.changed_skills = true;
        if (hasRecovery) {
            this.stats.relocHeal(this);
        }
        if (recalculate) {
            this.stats.recalcLocalStats(this);
        }
    }

    public boolean changeSkillData(Skill skill, int newLevel, byte newMasterlevel, long expiration) {
        if ((skill == null) || ((!GameConstants.isApplicableSkill(skill.getId())) && (!GameConstants.isApplicableSkill_(skill.getId())))) {
            return false;
        }
        if ((newLevel == 0) && (newMasterlevel == 0)) {
            if (this.skills.containsKey(skill)) {
                this.skills.remove(skill);
            } else {
                return false;
            }
        } else {
            this.skills.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
        }
        return true;
    }

    public void changeSkillLevel_Skip(Map<Skill, SkillEntry> skill, boolean write) {
        if (skill.isEmpty()) {
            return;
        }
        Map newL = new HashMap();
        for (Entry<Skill, SkillEntry> z : skill.entrySet()) {
            if (z.getKey() != null) {
                newL.put(z.getKey(), z.getValue());
                if ((((SkillEntry) z.getValue()).skillevel == 0) && (((SkillEntry) z.getValue()).masterlevel == 0)) {
                    if (this.skills.containsKey(z.getKey())) {
                        this.skills.remove(z.getKey());
                    }
                } else {
                    this.skills.put(z.getKey(), z.getValue());
                }
            }
        }
        if ((write) && (!newL.isEmpty())) {
            this.client.getSession().write(CWvsContext.updateSkills(newL));
        }
    }

    public void playerDead() {
        MapleStatEffect statss = getStatForBuff(MapleBuffStat.SOUL_STONE);
        if (statss != null) {
            dropMessage(5, "You have been revived by Soul Stone.");
            getStat().setHp(getStat().getMaxHp() / 100 * statss.getX(), this);
            setStance(0);
            changeMap(getMap(), getMap().getPortal(0));
            return;
        }
        if (getEventInstance() != null) {
            getEventInstance().playerKilled(this);
        }
        cancelEffectFromBuffStat(MapleBuffStat.SHADOWPARTNER);
        cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        cancelEffectFromBuffStat(MapleBuffStat.SOARING);
        cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        cancelEffectFromBuffStat(MapleBuffStat.MECH_CHANGE);
        cancelEffectFromBuffStat(MapleBuffStat.RECOVERY);
        cancelEffectFromBuffStat(MapleBuffStat.HP_BOOST_PERCENT);
        cancelEffectFromBuffStat(MapleBuffStat.MP_BOOST_PERCENT);
        cancelEffectFromBuffStat(MapleBuffStat.HP_BOOST);
        cancelEffectFromBuffStat(MapleBuffStat.MP_BOOST);
        cancelEffectFromBuffStat(MapleBuffStat.ENHANCED_MAXHP);
        cancelEffectFromBuffStat(MapleBuffStat.ENHANCED_MAXMP);
        cancelEffectFromBuffStat(MapleBuffStat.MAXHP);
        cancelEffectFromBuffStat(MapleBuffStat.MAXMP);
        dispelSummons();
        checkFollow();
        this.dotHP = 0;
        this.lastDOTTime = 0L;
        if ((!GameConstants.isBeginnerJob(this.job)) && (!inPVP())) {
            int charms = getItemQuantity(5130000, false);
            if (charms > 0) {
                MapleInventoryManipulator.removeById(this.client, MapleInventoryType.CASH, 5130000, 1, true, false);

                charms--;
                if (charms > 255) {
                    charms = 255;
                }
                this.client.getSession().write(CField.EffectPacket.useCharm((byte) charms, (byte) 0, true));
            } else {
                float diepercentage;
                long expforlevel = getNeededExp();
                if (map.isTown() || FieldLimitType.RegularExpLoss.check(map.getFieldLimit())) {
                    diepercentage = 0.01f;
                } else {
                    diepercentage = (float) (0.1f - ((traits.get(MapleTraitType.charisma).getLevel() / 20) / 100f));
                }
                long v10 = (int) (exp.get() - (long) ((double) expforlevel * diepercentage));
                if (v10 < 0) {
                    v10 = 0;
                }
                this.exp.set(v10);
            }
            this.updateSingleStat(MapleStat.EXP, this.exp.get());
        }
        if (!this.stats.checkEquipDurabilitys(this, -100)) {
            dropMessage(5, "An item has run out of durability but has no inventory room to go to.");
        }
        if (this.pyramidSubway != null) {
            this.stats.setHp(50, this);
            this.pyramidSubway.fail(this);
        }
    }

    public void updatePartyMemberHP() {
        int channel;
        if ((this.party != null) && (this.client.getChannelServer() != null)) {
            channel = this.client.getChannel();
            for (MaplePartyCharacter partychar : this.party.getMembers()) {
                if ((partychar != null) && (partychar.getMapid() == getMapId()) && (partychar.getChannel() == channel)) {
                    MapleCharacter other = this.client.getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other != null) {
                        other.getClient().getSession().write(CField.updatePartyMemberHP(getId(), this.stats.getHp(), this.stats.getCurrentMaxHp()));
                    }
                }
            }
        }
    }

    public void receivePartyMemberHP() {
        if (this.party == null) {
            return;
        }
        int channel = this.client.getChannel();
        for (MaplePartyCharacter partychar : this.party.getMembers()) {
            if ((partychar != null) && (partychar.getMapid() == getMapId()) && (partychar.getChannel() == channel)) {
                MapleCharacter other = this.client.getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
                if (other != null) {
                    this.client.getSession().write(CField.updatePartyMemberHP(other.getId(), other.getStat().getHp(), other.getStat().getCurrentMaxHp()));
                }
            }
        }
    }

    public void healHP(int delta) {
        addHP(delta);
        this.client.getSession().write(CField.EffectPacket.showOwnHpHealed(delta));
        getMap().broadcastMessage(this, CField.EffectPacket.showHpHealed(getId(), delta), false);
    }

    public void healMP(int delta) {
        addMP(delta);
        this.client.getSession().write(CField.EffectPacket.showOwnHpHealed(delta));
        getMap().broadcastMessage(this, CField.EffectPacket.showHpHealed(getId(), delta), false);
    }

    public void addHP(int delta) {

        int alpha = stats.getHp() + delta;
        if (alpha < 0 && getClient().getChannel() != 9) {
            dropMessage(5, "[Alpha] You have taken damage:" + delta + " If you died, then report this to Chaos");
        }
        if (delta < 0 && getClient().getChannel() != 9) {
            dropMessage(5, "[Delta] You have taken damage:" + delta + " If you died, then report this to Chaos");
        }
        alpha = Math.min(getStat().getCurrentMaxHp(), alpha);
        if (alpha > 500000) {
            alpha = 500000;
        }

        if (stats.setHp(alpha, this)) {
            updateSingleStat(MapleStat.HP, stats.getHp());
        }

    }

    public void addMP(int delta) {
        addMP(delta, false);
    }

    public void addMP(int delta, boolean ignore) {
        if (((delta < 0) && (GameConstants.isDemon(getJob()))) || (((!GameConstants.isDemon(getJob())) || (ignore)) && (this.stats.setMp(this.stats.getMp() + delta, this)))) {
            updateSingleStat(MapleStat.MP, this.stats.getMp());
        }
    }

    public void addMPHP(int hpDiff, int mpDiff) {
        Map statups = new EnumMap(MapleStat.class);

        if (this.stats.setHp(this.stats.getHp() + hpDiff, this)) {
            statups.put(MapleStat.HP, Long.valueOf(this.stats.getHp()));
        }
        if (((mpDiff < 0) && (GameConstants.isDemon(getJob()))) || ((!GameConstants.isDemon(getJob()))
                && (this.stats.setMp(this.stats.getMp() + mpDiff, this)))) {
            statups.put(MapleStat.MP, Long.valueOf(this.stats.getMp()));
        }

        if (statups.size() > 0) {
            client.getSession().write(CWvsContext.updatePlayerStats(statups, this));
        }
    }

    public void updateSingleStat(MapleStat stat, long newval) {
        updateSingleStat(stat, newval, false);
    }

    public void updateSingleStat(MapleStat stat, long newval, boolean itemReaction) {
        Map statup = new EnumMap(MapleStat.class);
        statup.put(stat, Long.valueOf(newval));
        client.getSession().write(CWvsContext.updatePlayerStats(statup, itemReaction, this));
    }

    public void gainExp(int total, boolean show, boolean inChat, boolean white) {
        try {
            long prevexp = getExp();
            long needed = getNeededExp();
            if (total > 0) {
                this.stats.checkEquipLevels(this, total);
            }
            if (this.level >= 250) {
                setExp(0L);
            } else {
                boolean leveled = false;
                long tot = this.exp.get() + total;
                if (tot >= needed) {
                    this.exp.addAndGet(total);
                    levelUp();
                    leveled = true;
                    if (this.level >= 250) {
                        setExp(0L);
                    } else {
                        needed = GameConstants.getExpNeededForLevel(this.level).longValue();
                        if (this.exp.get() >= needed) {
                            if (this.gmLevel >= 0) {
                                while (this.exp.get() >= GameConstants.getExpNeededForLevel(this.level).longValue()) {
                                    levelUp();
                                    setExp(getExp() - needed);
                                }
                            }
                            setExp(needed - 1L);
                        }
                    }
                } else {
                    this.exp.addAndGet(total);
                }

                if (total > 0) {
                    familyRep(prevexp, needed, leveled);
                }
            }
            if (total != 0) {
                if (this.exp.get() < 0L) {
                    if (total > 0) {
                        setExp(needed);
                    } else if (total < 0) {
                        setExp(0L);
                    }
                }
                updateSingleStat(MapleStat.EXP, getExp());
                if (show) {
                    this.client.getSession().write(CWvsContext.InfoPacket.GainEXP_Others(total, inChat, white));
                }
            }
        } catch (Exception e) {
            FileoutputUtil.outputFileError("Log_Script_Except.txt", e);
        }
    }

    public void setGmLevel(byte level) {
        this.gmLevel = level;
    }

    public void familyRep(long prevexp, long needed, boolean leveled) {
        if (this.mfc != null) {
            long onepercent = needed / 100L;
            if (onepercent <= 0L) {
                return;
            }
            long percentrep = getExp() / onepercent - prevexp / onepercent;
            if (leveled) {
                percentrep = 100L - percentrep + this.level / 2;
            }
            if (percentrep > 0L) {
                int sensen = World.Family.setRep(this.mfc.getFamilyId(), this.mfc.getSeniorId(), percentrep * 10L, this.level, this.name);
                if (sensen > 0) {
                    World.Family.setRep(this.mfc.getFamilyId(), sensen, percentrep * 5L, this.level, this.name);
                }
            }
        }
    }

    public void gainExpMonster(int gain, boolean show, boolean white, byte pty, int Class_Bonus_EXP, int Equipment_Bonus_EXP, int Premium_Bonus_EXP, boolean partyBonusMob, int partyBonusRate) {
        int total = gain + Class_Bonus_EXP + Equipment_Bonus_EXP + Premium_Bonus_EXP;
        int partyinc = 0;
        long prevexp = getExp();
        if (pty > 1) {
            double rate = (this.map == null) || (!partyBonusMob) || (this.map.getPartyBonusRate() <= 0) ? 0.05D : partyBonusRate > 0 ? partyBonusRate / 100.0D : this.map.getPartyBonusRate() / 100.0D;
            partyinc = (int) ((float) (gain * rate) * (pty + (rate > 0.05D ? -1 : 1)));
            total += partyinc;
        }

        if ((gain > 0) && (total < gain)) {
            total = 2147483647;
        }
        if (total > 0) {
            this.stats.checkEquipLevels(this, total);
        }
        long needed = getNeededExp();
        if ((this.level >= 250) && (!isIntern())) {
            setExp(0L);
        } else {
            boolean leveled = false;
            long tot = this.exp.get() + total;
            if (tot >= needed) {
                this.exp.addAndGet(total);
                levelUp();
                leveled = true;
                if (this.level >= 250) {
                    setExp(0L);
                } else {
                    needed = GameConstants.getExpNeededForLevel(this.level).longValue();
                    if (this.exp.get() >= needed) {
                        if (this.gmLevel >= 0) {
                            while (this.exp.get() >= GameConstants.getExpNeededForLevel(this.level).longValue()) {
                                levelUp();
                                setExp(getExp() - needed);
                            }
                        }
                        setExp(needed - 1L);
                    }
                }
            } else {
                this.exp.addAndGet(total);
            }

            if (total > 0) {
                familyRep(prevexp, needed, leveled);
            }
        }
        if (gain != 0) {
            if (this.exp.get() < 0L) {
                if (gain > 0) {
                    setExp(getNeededExp());
                } else if (gain < 0) {
                    setExp(0L);
                }
            }
            updateSingleStat(MapleStat.EXP, getExp());
            if (show) {
                this.client.getSession().write(CWvsContext.InfoPacket.GainEXP_Monster(gain, white, partyinc, Class_Bonus_EXP, Equipment_Bonus_EXP, Premium_Bonus_EXP));
            }
        }
    }

    public int getGML() {
        return gml;
    }

    public void setGML(int amt) {
        this.gml = amt;
    }

    public void forceReAddItem_NoUpdate(Item item, MapleInventoryType type) {
        getInventory(type).removeSlot(item.getPosition());
        getInventory(type).addFromDB(item);
    }

    public void forceReAddItem(Item item, MapleInventoryType type) {
        forceReAddItem_NoUpdate(item, type);
        if (type != MapleInventoryType.UNDEFINED) {
            this.client.getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse(item, type == MapleInventoryType.EQUIPPED ? 1 : type.getType(), this));
        }
    }

    public void forceReAddItem_Flag(Item item, MapleInventoryType type) {
        forceReAddItem_NoUpdate(item, type);
        if (type != MapleInventoryType.UNDEFINED) {
            this.client.getSession().write(CWvsContext.InventoryPacket.updateSpecialItemUse_(item, type == MapleInventoryType.EQUIPPED ? 1 : type.getType(), this));
        }
    }

    public void forceReAddItem_Book(Item item, MapleInventoryType type) {
        forceReAddItem_NoUpdate(item, type);
        if (type != MapleInventoryType.UNDEFINED) {
            this.client.getSession().write(CWvsContext.upgradeBook(item, this));
        }
    }

    public void silentPartyUpdate() {
        if (this.party != null) {
            World.Party.updateParty(this.party.getId(), PartyOperation.SILENT_UPDATE, new MaplePartyCharacter(this));
        }
    }

    public boolean isSuperGM() {
        return gmLevel >= PlayerGMRank.SUPERGM.getLevel();
    }

    public boolean isIntern() {
        return gmLevel >= PlayerGMRank.INTERN.getLevel();
    }

    public boolean isGM() {
        return gmLevel >= PlayerGMRank.GM.getLevel();
    }

    public boolean isAdmin() {
        return gmLevel >= PlayerGMRank.ADMIN.getLevel();
    }

    public int getGMLevel() {
        return this.gmLevel;
    }

    public boolean hasGmLevel(int level) {
        return this.gmLevel >= level;
    }

    public final MapleInventory getInventory(MapleInventoryType type) {
        return this.inventory[type.ordinal()];
    }

    public final MapleInventory[] getInventorys() {
        return this.inventory;
    }

    public final void expirationTask(boolean pending, boolean firstLoad) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (pending) {
            if (pendingExpiration != null) {
                for (Integer z : pendingExpiration) {
                    client.getSession().write(InfoPacket.itemExpired(z.intValue()));
                    if (!firstLoad) {
                        final Pair<Integer, String> replace = ii.replaceItemInfo(z.intValue());
                        if (replace != null && replace.left > 0 && replace.right.length() > 0) {
                            dropMessage(5, replace.right);
                        }
                    }
                }
            }
            pendingExpiration = null;
            if (pendingSkills != null) {
                client.getSession().write(CWvsContext.updateSkills(pendingSkills));
                for (Skill z : pendingSkills.keySet()) {
                    client.getSession().write(CWvsContext.serverNotice(5, "[" + SkillFactory.getSkillName(z.getId()) + "] skill has expired and will not be available for use."));
                }
            } //not real msg
            pendingSkills = null;
            return;
        }
        final MapleQuestStatus stat = getQuestNoAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT));
        long expiration;
        final List<Integer> ret = new ArrayList<Integer>();
        final long currenttime = System.currentTimeMillis();
        final List<Triple<MapleInventoryType, Item, Boolean>> toberemove = new ArrayList<Triple<MapleInventoryType, Item, Boolean>>(); // This is here to prevent deadlock.
        final List<Item> tobeunlock = new ArrayList<Item>(); // This is here to prevent deadlock.

        for (final MapleInventoryType inv : MapleInventoryType.values()) {
            for (final Item item : getInventory(inv)) {
                expiration = item.getExpiration();

                if ((expiration != -1 && !GameConstants.isPet(item.getItemId()) && currenttime > expiration)) {
                    if (ItemFlag.LOCK.check(item.getFlag())) {
                        tobeunlock.add(item);
                    } else if (currenttime > expiration) {
                        toberemove.add(new Triple<MapleInventoryType, Item, Boolean>(inv, item, false));
                    }
                } else if (item.getItemId() == 5000054 && item.getPet() != null && item.getPet().getSecondsLeft() <= 0) {
                    toberemove.add(new Triple<MapleInventoryType, Item, Boolean>(inv, item, false));
                } else if (item.getPosition() == -59) {
                    if (stat == null || stat.getCustomData() == null || Long.parseLong(stat.getCustomData()) < currenttime) {
                        toberemove.add(new Triple<MapleInventoryType, Item, Boolean>(inv, item, true));
                    }
                }
            }
        }
        Item item;
        for (final Triple<MapleInventoryType, Item, Boolean> itemz : toberemove) {
            item = itemz.getMid();
            getInventory(itemz.getLeft()).removeItem(item.getPosition(), item.getQuantity(), false);
            if (itemz.getRight() && getInventory(GameConstants.getInventoryType(item.getItemId())).getNextFreeSlot() > -1) {
                item.setPosition(getInventory(GameConstants.getInventoryType(item.getItemId())).getNextFreeSlot());
                getInventory(GameConstants.getInventoryType(item.getItemId())).addFromDB(item);
            } else {
                ret.add(item.getItemId());
            }
            if (!firstLoad) {
                final Pair<Integer, String> replace = ii.replaceItemInfo(item.getItemId());
                if (replace != null && replace.left > 0) {
                    Item theNewItem = null;
                    if (GameConstants.getInventoryType(replace.left) == MapleInventoryType.EQUIP) {
                        theNewItem = ii.getEquipById(replace.left);
                        theNewItem.setPosition(item.getPosition());
                    } else {
                        theNewItem = new Item(replace.left, item.getPosition(), (short) 1, (byte) 0);
                    }
                    getInventory(itemz.getLeft()).addFromDB(theNewItem);
                }
            }
        }
        for (final Item itemz : tobeunlock) {
            itemz.setExpiration(-1);
            itemz.setFlag((byte) (itemz.getFlag() - ItemFlag.LOCK.getValue()));
        }
        this.pendingExpiration = ret;

        final Map<Skill, SkillEntry> skilz = new HashMap<>();
        final List<Skill> toberem = new ArrayList<Skill>();
        for (Entry<Skill, SkillEntry> skil : skills.entrySet()) {
            if (skil.getValue().expiration != -1 && currenttime > skil.getValue().expiration) {
                toberem.add(skil.getKey());
            }
        }
        for (Skill skil : toberem) {
            skilz.put(skil, new SkillEntry(0, (byte) 0, -1));
            this.skills.remove(skil);
            changed_skills = true;
        }
        this.pendingSkills = skilz;
        if (stat != null && stat.getCustomData() != null && Long.parseLong(stat.getCustomData()) < currenttime) { //expired bro
            quests.remove(MapleQuest.getInstance(7830));
            quests.remove(MapleQuest.getInstance(GameConstants.PENDANT_SLOT));
        }
    }

    public void refreshBattleshipHP() {
        if (getJob() == 422) {
            this.client.getSession().write(CWvsContext.giveKilling(currentBattleshipHP()));
        }
    }

    public MapleShop getShop() {
        return this.shop;
    }

    public void setShop(MapleShop shop) {
        this.shop = shop;
    }

    public long getMeso() {
        return this.meso;
    }

    public final int[] getSavedLocations() {
        return this.savedLocations;
    }

    public int getSavedLocation(SavedLocationType type) {
        return this.savedLocations[type.getValue()];
    }

    public void saveLocation(SavedLocationType type) {
        this.savedLocations[type.getValue()] = getMapId();
        this.changed_savedlocations = true;
    }

    public void saveLocation(SavedLocationType type, int mapz) {
        this.savedLocations[type.getValue()] = mapz;
        this.changed_savedlocations = true;
    }

    public void clearSavedLocation(SavedLocationType type) {
        this.savedLocations[type.getValue()] = -1;
        this.changed_savedlocations = true;
    }

    public void gainMeso(long gain, boolean show) {
        gainMeso(gain, show, false);
    }

    public void gainMeso(long gain, boolean show, boolean inChat) {
        if (this.meso + gain < 0L) {
            this.client.getSession().write(CWvsContext.enableActions());
            return;
        }
        this.meso += gain;
        updateSingleStat(MapleStat.MESO, this.meso, false);
        this.client.getSession().write(CWvsContext.enableActions());
        if (gain > Long.MAX_VALUE) {
            gain = Long.MAX_VALUE;
        }
        if (show) {
            this.client.getSession().write(CWvsContext.InfoPacket.showMesoGain((int) gain, inChat));
        }
        if (getGML() == 1 && (meso > 1000000000 && meso <= Long.MAX_VALUE)) {
            if (getInventory(MapleInventoryType.ETC).isFull()) {
                dropMessage(1, "ETC inventory is full, can't use AutoBacon!");
            } else {
                gainMeso(-1000000000, true);
                MapleInventoryManipulator.addById(client, 4031588, (short) 1, getName());
            }
        }
    }

    public void controlMonster(MapleMonster monster, boolean aggro) {
        if ((this.clone) || (monster == null)) {
            return;
        }
        monster.setController(this);
        this.controlledLock.writeLock().lock();
        try {
            this.controlled.add(monster);
        } finally {
            this.controlledLock.writeLock().unlock();
        }
        this.client.getSession().write(MobPacket.controlMonster(monster, false, aggro));
        monster.sendStatus(this.client);
    }

    public void stopControllingMonster(MapleMonster monster) {
        if ((this.clone) || (monster == null)) {
            return;
        }
        this.controlledLock.writeLock().lock();
        try {
            if (this.controlled.contains(monster)) {
                this.controlled.remove(monster);
            }
        } finally {
            this.controlledLock.writeLock().unlock();
        }
    }

    public void checkMonsterAggro(MapleMonster monster) {
        if ((this.clone) || (monster == null)) {
            return;
        }
        if (monster.getController() == this) {
            monster.setControllerHasAggro(true);
        } else {
            monster.switchController(this, true);
        }
    }

    public int getControlledSize() {
        return this.controlled.size();
    }

    public int getAccountID() {
        return this.accountid;
    }

    public void mobKilled(int id, int skillID) {
        for (MapleQuestStatus q : this.quests.values()) {
            if ((q.getStatus() == 1) && (q.hasMobKills())) {
                if (q.mobKilled(id, skillID)) {
                    this.client.getSession().write(CWvsContext.InfoPacket.updateQuestMobKills(q));
                    if (q.getQuest().canComplete(this, null)) {
                        this.client.getSession().write(CWvsContext.getShowQuestCompletion(q.getQuest().getId()));
                    }
                }
            }
        }
    }

    public final List<MapleQuestStatus> getStartedQuests() {
        List ret = new LinkedList();
        for (MapleQuestStatus q : this.quests.values()) {
            if ((q.getStatus() == 1) && (!q.isCustom()) && (!q.getQuest().isBlocked())) {
                ret.add(q);
            }
        }
        return ret;
    }

    public final List<MapleQuestStatus> getCompletedQuests() {
        List ret = new LinkedList();
        for (MapleQuestStatus q : this.quests.values()) {
            if ((q.getStatus() == 2) && (!q.isCustom()) && (!q.getQuest().isBlocked())) {
                ret.add(q);
            }
        }
        return ret;
    }

    public final List<Pair<Integer, Long>> getCompletedMedals() {
        List ret = new ArrayList();
        for (MapleQuestStatus q : this.quests.values()) {
            if ((q.getStatus() == 2) && (!q.isCustom()) && (!q.getQuest().isBlocked()) && (q.getQuest().getMedalItem() > 0) && (GameConstants.getInventoryType(q.getQuest().getMedalItem()) == MapleInventoryType.EQUIP)) {
                ret.add(new Pair(Integer.valueOf(q.getQuest().getId()), Long.valueOf(q.getCompletionTime())));
            }
        }
        return ret;
    }

    public Map<Skill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap(this.skills);
    }

    public int getTotalSkillLevel(Skill skill) {
        if (skill == null) {
            return 0;
        }
        if (GameConstants.iskaiser_Transfiguration_Skill(skill.getId())) {
            return skill.getMaxLevel();
        }
        SkillEntry ret = (SkillEntry) this.skills.get(skill);
        if ((ret == null) || (ret.skillevel <= 0)) {
            return 0;
        }
        return Math.min(skill.getTrueMax(), ret.skillevel + (skill.isBeginnerSkill() ? 0 : this.stats.combatOrders + (skill.getMaxLevel() > 10 ? this.stats.incAllskill : 0) + this.stats.getSkillIncrement(skill.getId())));
    }

    public int getAllSkillLevels() {
        int rett = 0;
        for (Map.Entry ret : this.skills.entrySet()) {
            if ((!((Skill) ret.getKey()).isBeginnerSkill()) && (!((Skill) ret.getKey()).isSpecialSkill()) && (((SkillEntry) ret.getValue()).skillevel > 0)) {
                rett += ((SkillEntry) ret.getValue()).skillevel;
            }
        }
        return rett;
    }

    public long getSkillExpiry(Skill skill) {
        if (skill == null) {
            return 0L;
        }
        SkillEntry ret = (SkillEntry) this.skills.get(skill);
        if ((ret == null) || (ret.skillevel <= 0)) {
            return 0L;
        }
        return ret.expiration;
    }

    public int getSkillLevel(Skill skill) {
        if (skill == null) {
            return 0;
        }
        SkillEntry ret = (SkillEntry) this.skills.get(skill);
        if ((ret == null) || (ret.skillevel <= 0)) {
            return 0;
        }
        return ret.skillevel;
    }

    public byte getMasterLevel(int skill) {
        return getMasterLevel(SkillFactory.getSkill(skill));
    }

    public byte getMasterLevel(Skill skill) {
        SkillEntry ret = (SkillEntry) this.skills.get(skill);
        if (ret == null) {
            return 0;
        }
        return ret.masterlevel;
    }

    public void levelUp() {
        this.remainingAp += 5;
        int maxhp = this.stats.getMaxHp();
        int maxmp = this.stats.getMaxMp();

        if ((GameConstants.isBeginnerJob(this.job)) && (GameConstants.isMihile(this.job))) {
            maxhp += Randomizer.rand(12, 16);
            maxmp += Randomizer.rand(10, 12);
        } else if (((this.job == 3100) && (this.job >= 3110) && (this.job <= 3112))) {
            maxhp += Randomizer.rand(48, 52);
        } else if (((this.job == 3101) && (this.job >= 3120) && (this.job <= 3112))) {
            maxhp += Randomizer.rand(56, 62);
        } else if (((this.job >= 100) && (this.job <= 132)) || ((this.job >= 1100) && (this.job <= 1111)) || ((this.job >= 6100) && (this.job <= 6112))) {
            maxhp += Randomizer.rand(48, 52);
            maxmp += Randomizer.rand(4, 6);
        } else if (((this.job >= 200) && (this.job <= 232)) || ((this.job >= 1200) && (this.job <= 1211))) {
            maxhp += Randomizer.rand(10, 14);
            maxmp += Randomizer.rand(48, 52);
       } else if ((this.job >= 2700) && (this.job <= 2712)) {
            maxhp += Randomizer.rand(16, 21);
            maxmp += Randomizer.rand(230, 300);
        } else if ((this.job >= 3200) && (this.job <= 3212)) {
            maxhp += Randomizer.rand(20, 24);
            maxmp += Randomizer.rand(42, 44);
        } else if (((this.job >= 300) && (this.job <= 322)) || ((this.job >= 400) && (this.job <= 434)) || ((this.job >= 1300) && (this.job <= 1311)) || ((this.job >= 1400) && (this.job <= 1411)) || ((this.job >= 3300) && (this.job <= 3312)) || ((this.job >= 2300) && (this.job <= 2312))) {
            maxhp += Randomizer.rand(20, 24);
            maxmp += Randomizer.rand(14, 16);
        } else if (((this.job >= 510) && (this.job <= 512)) || ((this.job >= 1510) && (this.job <= 1512)) || (GameConstants.xenon(this.job))) {
            maxhp += Randomizer.rand(37, 41);
            maxmp += Randomizer.rand(18, 22);
        } else if (((this.job >= 500) && (this.job <= 532)) || ((this.job >= 3500) && (this.job <= 3512)) || (this.job == 1500)) {
            maxhp += Randomizer.rand(20, 24);
            maxmp += Randomizer.rand(18, 22);
        } else if ((this.job >= 2100) && (this.job <= 2112)) {
            maxhp += Randomizer.rand(50, 52);
            maxmp += Randomizer.rand(4, 6);
        } else if ((this.job >= 2200) && (this.job <= 2218)) {
            maxhp += Randomizer.rand(12, 16);
            maxmp += Randomizer.rand(50, 52);
        } else {
            maxhp += Randomizer.rand(50, 100);
            maxmp += Randomizer.rand(50, 100);
        }
        maxmp += this.stats.getTotalInt() / 10;

        exp.addAndGet(-GameConstants.getExpNeededForLevel(level));
        if (exp.get() < 0) {
            exp.set(0);
        }

        this.level = ((short) (this.level + 1));
        if ((GameConstants.isKOC(this.job)) && (this.level < 250) && (this.level > 10)) {
            this.exp.set(getNeededExp() / 10L);
        }
        if ((this.level == 10) && (MapConstants.isStorylineMap(this.mapid))) {
            MapleQuest.getInstance(50000).forceComplete(this, 0);
        }
        maxhp = Math.min(500000, Math.abs(maxhp));
        maxmp = Math.min(500000, Math.abs(maxmp));
        if (GameConstants.isDemon(this.job)) {
            maxmp = GameConstants.getMPByJob(this.job);
        }
        Map statup = new EnumMap(MapleStat.class);
        MapleInventory equip = this.client.getPlayer().getInventory(MapleInventoryType.EQUIPPED);

        statup.put(MapleStat.MAXHP, Long.valueOf(maxhp));
        statup.put(MapleStat.MAXMP, Long.valueOf(maxmp));
        statup.put(MapleStat.HP, Long.valueOf(maxhp));
        statup.put(MapleStat.MP, Long.valueOf(maxmp));
        statup.put(MapleStat.EXP, Long.valueOf(this.exp.get()));
        statup.put(MapleStat.LEVEL, Long.valueOf(this.level));
    
          if (GameConstants.isSeparatedSp(this.job)) {
            if (this.level >= 10) {
                this.remainingSp[GameConstants.getSkillBook(this.job, this.level)] += 3;
            }
        } else if (this.level >= 10) {
            this.remainingSp[GameConstants.getSkillBook(this.job)] += 3;
        }

        statup.put(MapleStat.AVAILABLEAP, Long.valueOf(this.remainingAp));


        this.stats.setInfo(maxhp, maxmp, maxhp, maxmp);
        client.getSession().write(CWvsContext.updatePlayerStats(statup, this));
        this.map.broadcastMessage(this, CField.EffectPacket.showForeignEffect(getId(), 0), false);
        this.characterCard.recalcLocalStats(this);
        this.stats.recalcLocalStats(this);
        silentPartyUpdate();
        guildUpdate();
        familyUpdate();
        updateSingleStat(MapleStat.AVAILABLESP, 0); // we don't care the value here
  
        if (getJob() == 3122) {
            getClient().getSession().write(CWvsContext.giveDemonWatk(this));
        }

    }

    public void eqitem(int itemid) {
        MapleInventory equip = this.client.getPlayer().getInventory(MapleInventoryType.EQUIPPED);
        Item eq_weapon = MapleItemInformationProvider.getInstance().getEquipById(itemid);
        eq_weapon.setPosition((short) -10);
        equip.addFromDB(eq_weapon);
        equipChanged();
        reloadC();
    }

    public void changeKeybinding(int key, byte type, int action) {
        if (type != 0) {
            this.keylayout.Layout().put(Integer.valueOf(key), new Pair(Byte.valueOf(type), Integer.valueOf(action)));
        } else {
            this.keylayout.Layout().remove(Integer.valueOf(key));
        }
    }

    public void sendMacros() {
        for (int i = 0; i < 5; i++) {
            if (this.skillMacros[i] != null) {
                this.client.getSession().write(CField.getMacros(this.skillMacros));
                break;
            }
        }
    }

    public void updateMacros(int position, SkillMacro updateMacro) {
        this.skillMacros[position] = updateMacro;
        this.changed_skillmacros = true;
    }

    public final SkillMacro[] getMacros() {
        return this.skillMacros;
    }

    public void tempban(String reason, Calendar duration, int greason, boolean IPMac) {
        if (IPMac) {
            this.client.banMacs();
        }
        this.client.getSession().write(CWvsContext.GMPoliceMessage(true));
        try {
            Connection con = DatabaseConnection.getConnection();

            if (IPMac) {
                PreparedStatement ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, this.client.getSession().getRemoteAddress().toString().split(":")[0]);
                ps.execute();
                ps.close();
            }

            this.client.getSession().close(true);

            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET tempban = ?, banreason = ?, greason = ? WHERE id = ?");
            Timestamp TS = new Timestamp(duration.getTimeInMillis());
            ps.setTimestamp(1, TS);
            ps.setString(2, reason);
            ps.setInt(3, greason);
            ps.setInt(4, this.accountid);
            ps.execute();
            ps.close();
        } catch (SQLException ex) {
            System.err.println(new StringBuilder().append("Error while tempbanning").append(ex).toString());
        }
    }

    public int getMaxHp() {
        return getStat().getMaxHp();
    }

    public int getMaxMp() {
        return getStat().getMaxMp();
    }

    public void setHp(int amount) {
        getStat().setHp(amount, this);
    }

    public void setMp(int amount) {
        getStat().setMp(amount, this);
    }

    public final boolean ban(String reason, boolean IPMac, boolean autoban, boolean hellban) {
        if (this.lastmonthfameids == null) {
            throw new RuntimeException("Trying to ban a non-loaded character (testhack)");
        }
        this.client.getSession().write(CWvsContext.GMPoliceMessage(true));
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?");
            ps.setInt(1, autoban ? 2 : 1);
            ps.setString(2, reason);
            ps.setInt(3, this.accountid);
            ps.execute();
            ps.close();

            if (IPMac) {
                this.client.banMacs();
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, this.client.getSessionIPAddress());
                ps.execute();
                ps.close();

                if (hellban) {
                    PreparedStatement psa = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                    psa.setInt(1, this.accountid);
                    ResultSet rsa = psa.executeQuery();
                    if (rsa.next()) {
                        PreparedStatement pss = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE email = ? OR SessionIP = ?");
                        pss.setInt(1, autoban ? 2 : 1);
                        pss.setString(2, reason);
                        pss.setString(3, rsa.getString("email"));
                        pss.setString(4, this.client.getSessionIPAddress());
                        pss.execute();
                        pss.close();
                    }
                    rsa.close();
                    psa.close();
                }
            }
        } catch (SQLException ex) {
            System.err.println(new StringBuilder().append("Error while banning").append(ex).toString());
            return false;
        }
        this.client.getSession().close(true);
        return true;
    }

    public static boolean ban(String id, String reason, boolean accountId, int gmlevel, boolean hellban) {
        try {
            Connection con = DatabaseConnection.getConnection();

            if (id.matches("/[0-9]{1,3}\\..*")) {
                PreparedStatement ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.execute();
                ps.close();
                return true;
            }
            PreparedStatement ps;
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }
            boolean ret = false;
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int z = rs.getInt(1);
                PreparedStatement psb = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ? AND gm < ?");
                psb.setString(1, reason);
                psb.setInt(2, z);
                psb.setInt(3, gmlevel);
                psb.execute();
                psb.close();

                if (gmlevel > 100) {
                    PreparedStatement psa = con.prepareStatement("SELECT * FROM accounts WHERE id = ?");
                    psa.setInt(1, z);
                    ResultSet rsa = psa.executeQuery();
                    if (rsa.next()) {
                        String sessionIP = rsa.getString("sessionIP");
                        if ((sessionIP != null) && (sessionIP.matches("/[0-9]{1,3}\\..*"))) {
                            PreparedStatement psz = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                            psz.setString(1, sessionIP);
                            psz.execute();
                            psz.close();
                        }
                        if (rsa.getString("macs") != null) {
                            String[] macData = rsa.getString("macs").split(", ");
                            if (macData.length > 0) {
                                MapleClient.banMacs(macData);
                            }
                        }
                        if (hellban) {
                            PreparedStatement pss = con.prepareStatement(new StringBuilder().append("UPDATE accounts SET banned = 1, banreason = ? WHERE email = ?").append(sessionIP == null ? "" : " OR SessionIP = ?").toString());
                            pss.setString(1, reason);
                            pss.setString(2, rsa.getString("email"));
                            if (sessionIP != null) {
                                pss.setString(3, sessionIP);
                            }
                            pss.execute();
                            pss.close();
                        }
                    }
                    rsa.close();
                    psa.close();
                }
                ret = true;
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException ex) {
            System.err.println(new StringBuilder().append("Error while banning").append(ex).toString());
        }
        return false;
    }

    public int getObjectId() {
        return getId();
    }

    public void setObjectId(int id) {
        throw new UnsupportedOperationException();
    }

    public MapleStorage getStorage() {
        return this.storage;
    }

    public void addVisibleMapObject(MapleMapObject mo) {
        if (this.clone) {
            return;
        }
        this.visibleMapObjectsLock.writeLock().lock();
        try {
            this.visibleMapObjects.add(mo);
        } finally {
            this.visibleMapObjectsLock.writeLock().unlock();
        }
    }

    public void removeVisibleMapObject(MapleMapObject mo) {
        if (this.clone) {
            return;
        }
        this.visibleMapObjectsLock.writeLock().lock();
        try {
            this.visibleMapObjects.remove(mo);
        } finally {
            this.visibleMapObjectsLock.writeLock().unlock();
        }
    }

    public boolean isMapObjectVisible(MapleMapObject mo) {
        this.visibleMapObjectsLock.readLock().lock();
        try {
            return (!this.clone) && (this.visibleMapObjects.contains(mo));
        } finally {
            this.visibleMapObjectsLock.readLock().unlock();
        }
    }

    public Collection<MapleMapObject> getAndWriteLockVisibleMapObjects() {
        this.visibleMapObjectsLock.writeLock().lock();
        return this.visibleMapObjects;
    }

    public void unlockWriteVisibleMapObjects() {
        this.visibleMapObjectsLock.writeLock().unlock();
    }

    public boolean isAlive() {
        return this.stats.getHp() > 0;
    }

    public int gettimenow() {
        return this.timenow;
    }

    public void settimenow(int slot) {
        this.timenow = slot;
    }

    public void startMapTimeLimitTaskadd() {
        this.mapTimeLimitTaskADD = Timer.MapTimer.getInstance().register(new Runnable() {
            int now = 0;

            public void run() {
                if ((MapleCharacter.this.getMapId() >= 925060100) && (MapleCharacter.this.getMapId() <= 925063900)) {
                    this.now += 1;
                    if (MapleCharacter.this.timenow == -1) {
                        MapleCharacter.this.mapTimeLimitTaskADD.cancel(false);
                        MapleCharacter.this.mapTimeLimitTaskADD = null;
                    } else {
                        MapleCharacter.this.timenow = this.now;
                    }
                } else {
                    MapleCharacter.this.mapTimeLimitTaskADD.cancel(false);
                    MapleCharacter.this.mapTimeLimitTaskADD = null;
                }
            }
        }, 1000L, 1000L);
    }

    public void sendDestroyData(MapleClient client) {
        client.getSession().write(CField.removePlayerFromMap(getObjectId()));
        for (WeakReference chr : this.clones) {
            if (chr.get() != null) {
                ((MapleCharacter) chr.get()).sendDestroyData(client);
            }
        }
    }

    public void sendSpawnData(MapleClient client) {
        if (client.getPlayer().allowedToTarget(this)) {
            client.getSession().write(CField.spawnPlayerMapobject(this));

            for (MaplePet pet : this.pets) {
                if (pet.getSummoned()) {
                    client.getSession().write(PetPacket.showPet(this, pet, false, false));
                }
            }
            for (WeakReference chr : this.clones) {
                if (chr.get() != null) {
                    ((MapleCharacter) chr.get()).sendSpawnData(client);
                }
            }
            if (this.dragon != null) {
                client.getSession().write(CField.spawnDragon(this.dragon));
            }
            if (this.Haku != null) {
                client.getSession().write(CField.spawnHaku(this.Haku));
                if (getHaku() != null && this.getBuffedValue(MapleBuffStat.Haku_Reborn) != null) {
                    getHaku().sendstats();
                    getMap().broadcastMessage(this, CField.spawnHaku_change0(this.getId()), true);
                    getMap().broadcastMessage(this, CField.spawnHaku_change1(this.getHaku()), true);
                    getMap().broadcastMessage(this, CField.spawnHaku_bianshen(this.getId(), this.getHaku().getObjectId(), this.getHaku().getstats()), true);
                }
            }
            if (this.android != null) {
                client.getSession().write(CField.spawnAndroid(this, this.android));
            }
            if (this.summonedFamiliar != null) {
                client.getSession().write(CField.spawnFamiliar(this.summonedFamiliar, true, true));
            }
            if ((this.summons != null) && (this.summons.size() > 0)) {
                this.summonsLock.readLock().lock();
                try {
                    for (MapleSummon summon : this.summons) {
                        client.getSession().write(CField.SummonPacket.spawnSummon(summon, false));
                    }
                } finally {
                    this.summonsLock.readLock().unlock();
                }
            }
            if ((this.followid > 0) && (this.followon)) {
                client.getSession().write(CField.followEffect(this.followinitiator ? this.followid : this.id, this.followinitiator ? this.id : this.followid, null));
            }
        }
    }

    public void setMSIPoints(int amt) {
        this.msipoints += amt;
    }

    public void setMSIPoints2(int amt) {
        this.msipoints = amt;
    }

    public int getMSIPoints() {
        return msipoints;
    }

    public final void equipChanged() {
        if (this.map == null) {
            return;
        }
        this.map.broadcastMessage(this, CField.updateCharLook(this), false);
        this.stats.recalcLocalStats(this);
        if (getMessenger() != null) {
            World.Messenger.updateMessenger(getMessenger().getId(), getName(), this.client.getChannel());
        }
    }

    public final MaplePet getPet(int index) {
        byte count = 0;
        for (MaplePet pet : this.pets) {
            if (pet.getSummoned()) {
                if (count == index) {
                    return pet;
                }
                count = (byte) (count + 1);
            }
        }
        return null;
    }

    public void removePetCS(MaplePet pet) {
        this.pets.remove(pet);
    }

    public void addPet(MaplePet pet) {
        if (this.pets.contains(pet)) {
            this.pets.remove(pet);
        }
        this.pets.add(pet);
    }

    public void removePet(MaplePet pet, boolean shiftLeft) {
        pet.setSummoned(0);
    }

    public final byte getPetIndex(MaplePet petz) {
        byte count = 0;
        for (MaplePet pet : this.pets) {
            if (pet.getSummoned()) {
                if (pet.getUniqueId() == petz.getUniqueId()) {
                    return count;
                }
                count = (byte) (count + 1);
            }
        }
        return -1;
    }

    public final byte getPetIndex(int petId) {
        byte count = 0;
        for (MaplePet pet : this.pets) {
            if (pet.getSummoned()) {
                if (pet.getUniqueId() == petId) {
                    return count;
                }
                count = (byte) (count + 1);
            }
        }
        return -1;
    }

    public final List<MaplePet> getSummonedPets() {
        List ret = new ArrayList();
        for (MaplePet pet : this.pets) {
            if (pet.getSummoned()) {
                ret.add(pet);
            }
        }
        return ret;
    }

    public final byte getPetById(int petId) {
        byte count = 0;
        for (MaplePet pet : this.pets) {
            if (pet.getSummoned()) {
                if (pet.getPetItemId() == petId) {
                    return count;
                }
                count = (byte) (count + 1);
            }
        }
        return -1;
    }

    public final List<MaplePet> getPets() {
        return this.pets;
    }

    public final void unequipAllPets() {
        for (MaplePet pet : this.pets) {
            if (pet != null) {
                unequipPet(pet, true, false);
            }
        }
    }

    public void unequipPet(MaplePet pet, boolean shiftLeft, boolean hunger) {
        if (pet.getSummoned()) {
            pet.saveToDb();

            this.client.getSession().write(PetPacket.updatePet(pet, getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), false));
            if (this.map != null) {
                this.map.broadcastMessage(this, PetPacket.showPet(this, pet, true, hunger), true);
            }
            removePet(pet, shiftLeft);

            if (GameConstants.GMS) {
                this.client.getSession().write(PetPacket.petStatUpdate(this));
            }
            this.client.getSession().write(CWvsContext.enableActions());
        }
    }

    public final long getLastFameTime() {
        return this.lastfametime;
    }

    public final List<Integer> getFamedCharacters() {
        return this.lastmonthfameids;
    }

    public final List<Integer> getBattledCharacters() {
        return this.lastmonthbattleids;
    }

    public FameStatus canGiveFame(MapleCharacter from) {
        if (this.lastfametime >= System.currentTimeMillis() - 86400000L) {
            return FameStatus.NOT_TODAY;
        }
        if ((from == null) || (this.lastmonthfameids == null) || (this.lastmonthfameids.contains(Integer.valueOf(from.getId())))) {
            return FameStatus.NOT_THIS_MONTH;
        }
        return FameStatus.OK;
    }

    public void hasGivenFame(MapleCharacter to) {
        this.lastfametime = System.currentTimeMillis();
        this.lastmonthfameids.add(Integer.valueOf(to.getId()));
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, to.getId());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println(new StringBuilder().append("ERROR writing famelog for char ").append(getName()).append(" to ").append(to.getName()).append(e).toString());
        }
    }

    public boolean canBattle(MapleCharacter to) {
        if ((to == null) || (this.lastmonthbattleids == null) || (this.lastmonthbattleids.contains(Integer.valueOf(to.getAccountID())))) {
            return false;
        }
        return true;
    }

    public void hasBattled(MapleCharacter to) {
        this.lastmonthbattleids.add(Integer.valueOf(to.getAccountID()));
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO battlelog (accid, accid_to) VALUES (?, ?)");
            ps.setInt(1, getAccountID());
            ps.setInt(2, to.getAccountID());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println(new StringBuilder().append("ERROR writing battlelog for char ").append(getName()).append(" to ").append(to.getName()).append(e).toString());
        }
    }

    public final MapleKeyLayout getKeyLayout() {
        return this.keylayout;
    }

    public MapleParty getParty() {
        if (this.party == null) {
            return null;
        }
        if (this.party.isDisbanded()) {
            this.party = null;
        }
        return this.party;
    }

    public byte getWorld() {
        return this.world;
    }

    public void setWorld(byte world) {
        this.world = world;
    }

    public void setParty(MapleParty party) {
        this.party = party;
    }

    public MapleTrade getTrade() {
        return this.trade;
    }

    public void setTrade(MapleTrade trade) {
        this.trade = trade;
    }

    public EventInstanceManager getEventInstance() {
        return this.eventInstance;
    }

    public void setEventInstance(EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }

    public void setEventInstanceAzwan(EventManager eventInstance) {
        this.eventInstanceAzwan = eventInstance;
    }

    public void addDoor(MapleDoor door) {
        this.doors.add(door);
    }

    public void clearDoors() {
        this.doors.clear();
    }

    public List<MapleDoor> getDoors() {
        return new ArrayList(this.doors);
    }

    public void addMechDoor(MechDoor door) {
        this.mechDoors.add(door);
    }

    public void clearMechDoors() {
        this.mechDoors.clear();
    }

    public List<MechDoor> getMechDoors() {
        return new ArrayList(this.mechDoors);
    }

    public void setSmega() {
        if (this.smega) {
            this.smega = false;
            dropMessage(5, "You have set megaphone to disabled mode");
        } else {
            this.smega = true;
            dropMessage(5, "You have set megaphone to enabled mode");
        }
    }

    public boolean getSmega() {
        return this.smega;
    }

    public List<MapleSummon> getSummonsReadLock() {
        this.summonsLock.readLock().lock();
        return this.summons;
    }

    public int getSummonsSize() {
        return this.summons.size();
    }

    public void unlockSummonsReadLock() {
        this.summonsLock.readLock().unlock();
    }

    public void addSummon(MapleSummon s) {
        this.summonsLock.writeLock().lock();
        try {
            this.summons.add(s);
        } finally {
            this.summonsLock.writeLock().unlock();
        }
    }

    public void removeSummon(MapleSummon s) {
        this.summonsLock.writeLock().lock();
        try {
            this.summons.remove(s);
        } finally {
            this.summonsLock.writeLock().unlock();
        }
    }

    public int getChair() {
        return this.chair;
    }

    public int getItemEffect() {
        return this.itemEffect;
    }

    public void setChair(int chair) {
        this.chair = chair;
        this.stats.relocHeal(this);
    }

    public void setItemEffect(int itemEffect) {
        this.itemEffect = itemEffect;
    }

    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }

    public int getFamilyId() {
        if (this.mfc == null) {
            return 0;
        }
        return this.mfc.getFamilyId();
    }

    public int getSeniorId() {
        if (this.mfc == null) {
            return 0;
        }
        return this.mfc.getSeniorId();
    }

    public int getJunior1() {
        if (this.mfc == null) {
            return 0;
        }
        return this.mfc.getJunior1();
    }

    public int getJunior2() {
        if (this.mfc == null) {
            return 0;
        }
        return this.mfc.getJunior2();
    }

    public int getCurrentRep() {
        return this.currentrep;
    }

    public int getTotalRep() {
        return this.totalrep;
    }

    public void setCurrentRep(int _rank) {
        this.currentrep = _rank;
        if (this.mfc != null) {
            this.mfc.setCurrentRep(_rank);
        }
    }

    public void setTotalRep(int _rank) {
        this.totalrep = _rank;
        if (this.mfc != null) {
            this.mfc.setTotalRep(_rank);
        }
    }

    public int getTotalWins() {
        return this.totalWins;
    }

    public int getTotalLosses() {
        return this.totalLosses;
    }

    public void increaseTotalWins() {
        this.totalWins += 1;
    }

    public void increaseTotalLosses() {
        this.totalLosses += 1;
    }

    public int getGuildId() {
        return this.guildid;
    }

    public byte getGuildRank() {
        return this.guildrank;
    }

    public int getGuildContribution() {
        return this.guildContribution;
    }

    public void setGuildId(int _id) {
        this.guildid = _id;
        if (this.guildid > 0) {
            if (this.mgc == null) {
                this.mgc = new MapleGuildCharacter(this);
            } else {
                this.mgc.setGuildId(this.guildid);
            }
        } else {
            this.mgc = null;
            this.guildContribution = 0;
        }
    }

    public void setGuildRank(byte _rank) {
        this.guildrank = _rank;
        if (this.mgc != null) {
            this.mgc.setGuildRank(_rank);
        }
    }

    public void setGuildContribution(int _c) {
        this.guildContribution = _c;
        if (this.mgc != null) {
            this.mgc.setGuildContribution(_c);
        }
    }

    public MapleGuildCharacter getMGC() {
        return this.mgc;
    }

    public void setAllianceRank(byte rank) {
        this.allianceRank = rank;
        if (this.mgc != null) {
            this.mgc.setAllianceRank(rank);
        }
    }

    public byte getAllianceRank() {
        return this.allianceRank;
    }

    public MapleGuild getGuild() {
        if (getGuildId() <= 0) {
            return null;
        }
        return World.Guild.getGuild(getGuildId());
    }

    public void setJob(int j) {
        this.job = ((short) j);
    }

    public void guildUpdate() {
        if (this.guildid <= 0) {
            return;
        }
        this.mgc.setLevel(this.level);
        this.mgc.setJobId(this.job);
        World.Guild.memberLevelJobUpdate(this.mgc);
    }

    public void setJobId(int job) {
        this.jobid = job;
    }

    public int getJobId() {
        return this.jobid;
    }

    public void saveGuildStatus() {
        MapleGuild.setOfflineGuildStatus(this.guildid, this.guildrank, this.guildContribution, this.allianceRank, this.id);
    }

    public void familyUpdate() {
        if (this.mfc == null) {
            return;
        }
        World.Family.memberFamilyUpdate(this.mfc, this);
    }

    public void saveFamilyStatus() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE characters SET familyid = ?, seniorid = ?, junior1 = ?, junior2 = ? WHERE id = ?");
            if (this.mfc == null) {
                ps.setInt(1, 0);
                ps.setInt(2, 0);
                ps.setInt(3, 0);
                ps.setInt(4, 0);
            } else {
                ps.setInt(1, this.mfc.getFamilyId());
                ps.setInt(2, this.mfc.getSeniorId());
                ps.setInt(3, this.mfc.getJunior1());
                ps.setInt(4, this.mfc.getJunior2());
            }
            ps.setInt(5, this.id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            System.out.println(new StringBuilder().append("SQLException: ").append(se.getLocalizedMessage()).toString());
            se.printStackTrace();
        }
    }

    public void modifyCSPoints(int type, int quantity) {
        modifyCSPoints(type, quantity, false);
    }

    public void modifyCSPoints(int type, int quantity, boolean show) {
        switch (type) {
            case 1:
                if (this.nxcredit + quantity < 0) {
                    if (show) {
                        dropMessage(-1, "You have gained the max cash. No cash will be awarded.");
                    }
                    return;
                }
                this.nxcredit += quantity;
                break;
            case 4:
                if (this.acash + quantity < 0) {
                    if (show) {
                        dropMessage(-1, "You have gained the max cash. No cash will be awarded.");
                    }
                    return;
                }
                this.acash += quantity;
                break;
            case 2:
                if (this.maplepoints + quantity < 0) {
                    if (show) {
                        dropMessage(-1, "You have gained the max maple points. No cash will be awarded.");
                    }
                    return;
                }
                this.maplepoints += quantity;
                break;
            case 3:
        }

        if ((show) && (quantity != 0)) {
            dropMessage(-1, new StringBuilder().append("You have ").append(quantity > 0 ? "gained " : "lost ").append(quantity).append(type == 1 ? " cash." : " maple points.").toString());
            this.client.getSession().write(CField.EffectPacket.showForeignEffect(20));
        }
    }

    public int getCSPoints(int type) {
        switch (type) {
            case 1:
                return this.nxcredit;
            case 4:
                return this.acash;
            case 2:
                return this.maplepoints;
            case 3:
        }
        return 0;
    }

    public final boolean hasEquipped(int itemid) {
        return this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid) >= 1;
    }

    public final boolean haveItem(int itemid, int quantity, boolean checkEquipped, boolean greaterOrEquals) {
        MapleInventoryType type = GameConstants.getInventoryType(itemid);
        int possesed = this.inventory[type.ordinal()].countById(itemid);
        if ((checkEquipped) && (type == MapleInventoryType.EQUIP)) {
            possesed += this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        if (greaterOrEquals) {
            return possesed >= quantity;
        }
        return possesed == quantity;
    }

    public final boolean haveItem(int itemid, int quantity) {
        return haveItem(itemid, quantity, true, true);
    }

    public final boolean haveItem(int itemid) {
        return haveItem(itemid, 1, true, true);
    }

    public static boolean tempban(String reason, Calendar duration, int greason, int accountid) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET tempban = ?, banreason = ?, greason = ? WHERE id = ?");
            Timestamp TS = new Timestamp(duration.getTimeInMillis());
            ps.setTimestamp(1, TS);
            ps.setString(2, reason);
            ps.setInt(3, greason);
            ps.setInt(4, accountid);
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public void setGM(byte level) {
        this.gmLevel = level;
    }

    public final void broadcastPacket(byte[] p) {
        for (MapleCharacter chr : getPlayers()) {
            chr.getClient().getSession().write(p);
        }
    }

    public List<MapleCharacter> getPlayers() {
        this.rL.lock();
        try {
            return new LinkedList(this.chars);
        } finally {
            this.rL.unlock();
        }
    }

    public int getBuddyCapacity() {
        return this.buddylist.getCapacity();
    }

    public void setBuddyCapacity(int capacity) {
        this.buddylist.setCapacity(capacity);
        this.client.getSession().write(CWvsContext.BuddylistPacket.updateBuddyCapacity(capacity));
    }

    public MapleMessenger getMessenger() {
        return this.messenger;
    }

    public void setMessenger(MapleMessenger messenger) {
        this.messenger = messenger;
    }

    public void addCooldown(int skillId, long startTime, long length) {
        this.coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length));
    }

    public void removeCooldown(int skillId) {
        if (this.coolDowns.containsKey(Integer.valueOf(skillId))) {
            this.coolDowns.remove(Integer.valueOf(skillId));
        }
    }

    public boolean skillisCooling(int skillId) {
        return this.coolDowns.containsKey(Integer.valueOf(skillId));
    }

    public void giveCoolDowns(int skillid, long starttime, long length) {
        addCooldown(skillid, starttime, length);
    }

    public void giveCoolDowns(List<MapleCoolDownValueHolder> cooldowns) {
        if (cooldowns != null) {
            for (MapleCoolDownValueHolder cooldown : cooldowns) {
                this.coolDowns.put(Integer.valueOf(cooldown.skillId), cooldown);
            }
        } else {
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM skills_cooldowns WHERE charid = ?");
                ps.setInt(1, getId());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getLong("length") + rs.getLong("StartTime") - System.currentTimeMillis() > 0L) {
                        giveCoolDowns(rs.getInt("SkillID"), rs.getLong("StartTime"), rs.getLong("length"));
                    }
                }
                ps.close();
                rs.close();
                deleteWhereCharacterId(con, "DELETE FROM skills_cooldowns WHERE charid = ?");
            } catch (SQLException e) {
                System.err.println("Error while retriving cooldown from SQL storage");
            }
        }
    }

    public int getCooldownSize() {
        return this.coolDowns.size();
    }

    public int getDiseaseSize() {
        return this.diseases.size();
    }

    public List<MapleCoolDownValueHolder> getCooldowns() {
        List ret = new ArrayList();
        for (MapleCoolDownValueHolder mc : this.coolDowns.values()) {
            if (mc != null) {
                ret.add(mc);
            }
        }
        return ret;
    }

    public final List<MapleDiseaseValueHolder> getAllDiseases() {
        return new ArrayList(this.diseases.values());
    }

    public final boolean hasDisease(MapleDisease dis) {
        return this.diseases.containsKey(dis);
    }

    public void giveDebuff(MapleDisease disease, MobSkill skill) {
        giveDebuff(disease, skill.getX(), skill.getDuration(), skill.getSkillId(), skill.getSkillLevel());
    }

    public void giveDebuff(MapleDisease disease, int x, long duration, int skillid, int level) {
        if ((this.map != null) && (!hasDisease(disease))) {
            if ((disease != MapleDisease.SEDUCE) && (disease != MapleDisease.STUN) && (disease != MapleDisease.FLAG) && (getBuffedValue(MapleBuffStat.HOLY_SHIELD) != null)) {
                return;
            }

            int mC = getBuffSource(MapleBuffStat.MECH_CHANGE);
            if ((mC > 0) && (mC != 35121005)) {
                return;
            }
            if ((this.stats.ASR > 0) && (Randomizer.nextInt(100) < this.stats.ASR)) {
                return;
            }

            this.diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration - this.stats.decreaseDebuff));
            this.client.getSession().write(CWvsContext.BuffPacket.giveDebuff(disease, x, skillid, level, (int) duration));
            this.map.broadcastMessage(this, CWvsContext.BuffPacket.giveForeignDebuff(this.id, disease, skillid, level, x), false);

            if ((x > 0) && (disease == MapleDisease.POISON)) {
                addHP((int) -(x * ((duration - this.stats.decreaseDebuff) / 1000L)));
            }
        }
    }

    public final void giveSilentDebuff(List<MapleDiseaseValueHolder> ld) {
        if (ld != null) {
            for (MapleDiseaseValueHolder disease : ld) {
                this.diseases.put(disease.disease, disease);
            }
        }
    }

    public void dispelDebuff(MapleDisease debuff) {
        if (hasDisease(debuff)) {
            this.client.getSession().write(CWvsContext.BuffPacket.cancelDebuff(debuff));
            this.map.broadcastMessage(this, CWvsContext.BuffPacket.cancelForeignDebuff(this.id, debuff), false);

            this.diseases.remove(debuff);
        }
    }

    public void dispelDebuffs() {
        List<MapleDisease> diseasess = new ArrayList(this.diseases.keySet());
        for (MapleDisease d : diseasess) {
            dispelDebuff(d);
        }
    }

    public void cancelAllDebuffs() {
        this.diseases.clear();
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public void sendNote(String to, String msg) {
        sendNote(to, msg, 0);
    }

    public void sendNote(String to, String msg, int fame) {
        MapleCharacterUtil.sendNote(to, getName(), msg, fame);
    }

    public void showNote() {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM notes WHERE `to`=?", 1005, 1008);
            ps.setString(1, getName());
            ResultSet rs = ps.executeQuery();
            rs.last();
            int count = rs.getRow();
            rs.first();
            this.client.getSession().write(MTSCSPacket.showNotes(rs, count));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.err.println(new StringBuilder().append("Unable to show note").append(e).toString());
        }
    }

    public void deleteNote(int id, int fame) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT gift FROM notes WHERE `id`=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if ((rs.next())
                    && (rs.getInt("gift") == fame) && (fame > 0)) {
                addFame(fame);
                updateSingleStat(MapleStat.FAME, getFame());
                this.client.getSession().write(CWvsContext.InfoPacket.getShowFameGain(fame));
            }

            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM notes WHERE `id`=?");
            ps.setInt(1, id);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println(new StringBuilder().append("Unable to delete note").append(e).toString());
        }
    }

    public int getMulungEnergy() {
        return this.mulung_energy;
    }

    public void mulung_EnergyModify(boolean inc) {
        if (inc) {
            if (this.mulung_energy + 100 > 10000) {
                this.mulung_energy = 10000;
            } else {
                this.mulung_energy = ((short) (this.mulung_energy + 100));
            }
        } else {
            this.mulung_energy = 0;
        }
        this.client.getSession().write(CWvsContext.MulungEnergy(this.mulung_energy));
    }

    public void writeMulungEnergy() {
        this.client.getSession().write(CWvsContext.MulungEnergy(this.mulung_energy));
    }

    public void writeEnergy(String type, String inc) {
        this.client.getSession().write(CWvsContext.sendPyramidEnergy(type, inc));
    }

    public void writeStatus(String type, String inc) {
        this.client.getSession().write(CWvsContext.sendGhostStatus(type, inc));
    }

    public void writePoint(String type, String inc) {
        this.client.getSession().write(CWvsContext.sendGhostPoint(type, inc));
    }

    public final short getCombo() {
        return this.combo;
    }

    public void setCombo(short combo) {
        this.combo = combo;
    }

    public final long getLastCombo() {
        return this.lastCombo;
    }

    public void setLastCombo(long combo) {
        this.lastCombo = combo;
    }

    public final long getKeyDownSkill_Time() {
        return this.keydown_skill;
    }

    public void setKeyDownSkill_Time(long keydown_skill) {
        this.keydown_skill = keydown_skill;
    }

    public void checkBerserk() {
        if ((this.job != 132) || (this.lastBerserkTime < 0L) || (this.lastBerserkTime + 10000L > System.currentTimeMillis())) {
            return;
        }
        Skill BerserkX = SkillFactory.getSkill(1320006);
        int skilllevel = getTotalSkillLevel(BerserkX);
        if ((skilllevel >= 1) && (this.map != null)) {
            this.lastBerserkTime = System.currentTimeMillis();
            MapleStatEffect ampStat = BerserkX.getEffect(skilllevel);
            this.stats.Berserk = (this.stats.getHp() * 100 / this.stats.getCurrentMaxHp() >= ampStat.getX());
            this.client.getSession().write(CField.EffectPacket.showOwnBuffEffect(1320006, 1, getLevel(), skilllevel, (byte) (this.stats.Berserk ? 1 : 0)));
            this.map.broadcastMessage(this, CField.EffectPacket.showBuffeffect(getId(), 1320006, 1, getLevel(), skilllevel, (byte) (this.stats.Berserk ? 1 : 0)), false);
        } else {
            this.lastBerserkTime = -1L;
        }
    }
//    public void checkDA() {
//        Skill LunarTide = SkillFactory.getSkill(30010242);
//        int skilllevel = getTotalSkillLevel(LunarTide);
//        if ((skilllevel >= 1) && (this.map != null)) {
//            MapleStatEffect ampStat = LunarTide.getEffect(skilllevel);
//            ampStat.applyDaBuff(this);
//        }
//    }

 /*   public void checkLunarTide() {
        Skill LunarTide = SkillFactory.getSkill(27110007);
        int skilllevel = getTotalSkillLevel(LunarTide);
        if ((skilllevel >= 1) && (this.map != null)) {
            MapleStatEffect ampStat = LunarTide.getEffect(skilllevel);
            ampStat.applyLunarTideBuff(this);
        }
    }*/

    public void setChalkboard(String text) {
        this.chalktext = text;
        if (this.map != null) {
            this.map.broadcastMessage(MTSCSPacket.useChalkboard(getId(), text));
        }
    }

    public String getChalkboard() {
        return this.chalktext;
    }

    public MapleMount getMount() {
        return this.mount;
    }

    public int[] getWishlist() {
        return this.wishlist;
    }

    public void clearWishlist() {
        for (int i = 0; i < 10; i++) {
            this.wishlist[i] = 0;
        }
        this.changed_wishlist = true;
    }

    public int getWishlistSize() {
        int ret = 0;
        for (int i = 0; i < 10; i++) {
            if (this.wishlist[i] > 0) {
                ret++;
            }
        }
        return ret;
    }

    public void setWishlist(int[] wl) {
        this.wishlist = wl;
        this.changed_wishlist = true;
    }

    public int[] getRocks() {
        return this.rocks;
    }

    public int getRockSize() {
        int ret = 0;
        for (int i = 0; i < 10; i++) {
            if (this.rocks[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromRocks(int map) {
        for (int i = 0; i < 10; i++) {
            if (this.rocks[i] == map) {
                this.rocks[i] = 999999999;
                this.changed_trocklocations = true;
                break;
            }
        }
    }

    public void addRockMap() {
        if (getRockSize() >= 10) {
            return;
        }
        this.rocks[getRockSize()] = getMapId();
        this.changed_trocklocations = true;
    }

    public boolean isRockMap(int id) {
        for (int i = 0; i < 10; i++) {
            if (this.rocks[i] == id) {
                return true;
            }
        }
        return false;
    }

    public int[] getRegRocks() {
        return this.regrocks;
    }

    public int getRegRockSize() {
        int ret = 0;
        for (int i = 0; i < 5; i++) {
            if (this.regrocks[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromRegRocks(int map) {
        for (int i = 0; i < 5; i++) {
            if (this.regrocks[i] == map) {
                this.regrocks[i] = 999999999;
                this.changed_regrocklocations = true;
                break;
            }
        }
    }

    public void addRegRockMap() {
        if (getRegRockSize() >= 5) {
            return;
        }
        this.regrocks[getRegRockSize()] = getMapId();
        this.changed_regrocklocations = true;
    }

    public boolean isRegRockMap(int id) {
        for (int i = 0; i < 5; i++) {
            if (this.regrocks[i] == id) {
                return true;
            }
        }
        return false;
    }
        public short getExceed() {
        return exceed;
    }

    public void setExceed(short amount) {
        this.exceed = amount;
    }

    public void gainExceed(short amount) {
        this.exceed += amount;
        updateExceed(exceed);
    }

    public void updateExceed(short amount) {
        client.getSession().write(AvengerPacket.giveExceed(amount));
    }

    public void handleExceedAttack(int skill) {
        long now = System.currentTimeMillis();
        if (lastExceedTime + 15000 < now) {
            exceedAttack = 0;
            lastExceedTime = now;
        }
        client.getSession().write(AvengerPacket.giveExceedAttack(skill, ++exceedAttack));
    }

    public int[] getHyperRocks() {
        return this.hyperrocks;
    }

    public int getHyperRockSize() {
        int ret = 0;
        for (int i = 0; i < 13; i++) {
            if (this.hyperrocks[i] != 999999999) {
                ret++;
            }
        }
        return ret;
    }

    public void deleteFromHyperRocks(int map) {
        for (int i = 0; i < 13; i++) {
            if (this.hyperrocks[i] == map) {
                this.hyperrocks[i] = 999999999;
                this.changed_hyperrocklocations = true;
                break;
            }
        }
    }

    public void addHyperRockMap() {
        if (getRegRockSize() >= 13) {
            return;
        }
        this.hyperrocks[getHyperRockSize()] = getMapId();
        this.changed_hyperrocklocations = true;
    }

    public boolean isHyperRockMap(int id) {
        for (int i = 0; i < 13; i++) {
            if (this.hyperrocks[i] == id) {
                return true;
            }
        }
        return false;
    }

    public List<LifeMovementFragment> getLastRes() {
        return this.lastres;
    }

    public void setLastRes(List<LifeMovementFragment> lastres) {
        this.lastres = lastres;
    }

    public void dropMessage(int type, String message) {
        if (type == -1) {
            this.client.getSession().write(CWvsContext.getTopMsg(message));
        } else if (type == -2) {
            this.client.getSession().write(PlayerShopPacket.shopChat(message, 0));
        } else if (type == -3) {
            this.client.getSession().write(CField.getChatText(getId(), message, isSuperGM(), 0));
        } else if (type == -4) {
            this.client.getSession().write(CField.getChatText(getId(), message, isSuperGM(), 1));
        } else if (type == -5) {
            this.client.getSession().write(CField.getGameMessage(message, (short) 6));
        } else if (type == -6) {
            this.client.getSession().write(CField.getGameMessage(message, (short) 11));
        } else if (type == -7) {
            this.client.getSession().write(CWvsContext.getMidMsg(message, false, 0));
        } else if (type == -8) {
            this.client.getSession().write(CWvsContext.getMidMsg(message, true, 0));
        } else {
            this.client.getSession().write(CWvsContext.serverNotice(type, message));
        }
    }

    public IMaplePlayerShop getPlayerShop() {
        return this.playerShop;
    }

    public void setPlayerShop(IMaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }

    public int getConversation() {
        return this.inst.get();
    }

    public void setConversation(int inst) {
        this.inst.set(inst);
    }

    public int getDirection() {
        return this.insd.get();
    }

    public void setDirection(int inst) {
        this.insd.set(inst);
    }

    public MapleCarnivalParty getCarnivalParty() {
        return this.carnivalParty;
    }

    public void setCarnivalParty(MapleCarnivalParty party) {
        this.carnivalParty = party;
    }

    public void addCP(int ammount) {
        this.totalCP = ((short) (this.totalCP + ammount));
        this.availableCP = ((short) (this.availableCP + ammount));
    }

    public void useCP(int ammount) {
        this.availableCP = ((short) (this.availableCP - ammount));
    }

    public int getAvailableCP() {
        return this.availableCP;
    }

    public int getTotalCP() {
        return this.totalCP;
    }

    public void resetCP() {
        this.totalCP = 0;
        this.availableCP = 0;
    }

    public void addCarnivalRequest(MapleCarnivalChallenge request) {
        this.pendingCarnivalRequests.add(request);
    }

    public final MapleCarnivalChallenge getNextCarnivalRequest() {
        return (MapleCarnivalChallenge) this.pendingCarnivalRequests.pollLast();
    }

    public void clearCarnivalRequests() {
        this.pendingCarnivalRequests = new LinkedList();
    }

    public void startMonsterCarnival(int enemyavailable, int enemytotal) {
        this.client.getSession().write(MonsterCarnivalPacket.startMonsterCarnival(this, enemyavailable, enemytotal));
    }

    public void CPUpdate(boolean party, int available, int total, int team) {
        this.client.getSession().write(MonsterCarnivalPacket.CPUpdate(party, available, total, team));
    }

    public void playerDiedCPQ(String name, int lostCP, int team) {
        this.client.getSession().write(MonsterCarnivalPacket.playerDiedMessage(name, lostCP, team));
    }

    public boolean getCanTalk() {
        return this.canTalk;
    }

    public void canTalk(boolean talk) {
        this.canTalk = talk;
    }

    public double getEXPMod() {
        return this.stats.expMod;
    }

    public int getDropMod() {
        return this.stats.dropMod;
    }

    public int getCashMod() {
        return this.stats.cashMod;
    }

    public void setPoints2(int p) {
        this.points += p;

    }

    public void setPoints(int p) {
        this.points = p;
    }

    public int getPoints() {
        return this.points;
    }

    public void setVPoints(int p) {
        this.vpoints = p;
    }

    public int getVPoints() {
        return this.vpoints;
    }

    /*  public void setTotalVote(int px) {
     this.totalvote = px;
     }*/

    /*  public int getTotalVote() {
     return this.totalvote;
     }*/
    public int getDonatorPoints() {
        return this.DonatorPoints;
    }

    public int getGMlevel() {
        return this.gmLevel;
    }

    public CashShop getCashInventory() {
        return this.cs;
    }

    public void removeItem(int id, int quantity) {
        MapleInventoryManipulator.removeById(this.client, GameConstants.getInventoryType(id), id, -quantity, true, false);
        this.client.getSession().write(CWvsContext.InfoPacket.getShowItemGain(id, (short) quantity, true));
    }

    public void removeAll(int id) {
        removeAll(id, true);
    }

    public void removeAll(int id, boolean show) {
        MapleInventoryType type = GameConstants.getInventoryType(id);
        int possessed = getInventory(type).countById(id);

        if (possessed > 0) {
            MapleInventoryManipulator.removeById(getClient(), type, id, possessed, true, false);
            if (show) {
                getClient().getSession().write(CWvsContext.InfoPacket.getShowItemGain(id, (short) -possessed, true));
            }
        }
    }

    public Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> getRings(boolean equip) {
        MapleInventory iv = getInventory(MapleInventoryType.EQUIPPED);
        List<Item> equipped = iv.newList();
        Collections.sort(equipped);
        List<MapleRing> crings = new ArrayList<>(), frings = new ArrayList<>(), mrings = new ArrayList<>();
        MapleRing ring;
        for (Item ite : equipped) {
            Equip item = (Equip) ite;
            if (item.getRing() != null) {
                ring = item.getRing();
                ring.setEquipped(true);
                if (GameConstants.isEffectRing(item.getItemId())) {
                    if (equip) {
                        if (GameConstants.isCrushRing(item.getItemId())) {
                            crings.add(ring);
                        } else if (GameConstants.isFriendshipRing(item.getItemId())) {
                            frings.add(ring);
                        } else if (GameConstants.isMarriageRing(item.getItemId())) {
                            mrings.add(ring);
                        }
                    } else {
                        if (crings.isEmpty() && GameConstants.isCrushRing(item.getItemId())) {
                            crings.add(ring);
                        } else if (frings.isEmpty() && GameConstants.isFriendshipRing(item.getItemId())) {
                            frings.add(ring);
                        } else if (mrings.isEmpty() && GameConstants.isMarriageRing(item.getItemId())) {
                            mrings.add(ring);
                        } //for 3rd person the actual slot doesnt matter, so we'll use this to have both shirt/ring same?
                        //however there seems to be something else behind this, will have to sniff someone with shirt and ring, or more conveniently 3-4 of those
                    }
                }
            }
        }
        if (equip) {
            iv = getInventory(MapleInventoryType.EQUIP);
            for (Item ite : iv.list()) {
                Equip item = (Equip) ite;
                if (item.getRing() != null && GameConstants.isCrushRing(item.getItemId())) {
                    ring = item.getRing();
                    ring.setEquipped(false);
                    if (GameConstants.isFriendshipRing(item.getItemId())) {
                        frings.add(ring);
                    } else if (GameConstants.isCrushRing(item.getItemId())) {
                        crings.add(ring);
                    } else if (GameConstants.isMarriageRing(item.getItemId())) {
                        mrings.add(ring);
                    }
                }
            }
        }
        Collections.sort(frings, new MapleRing.RingComparator());
        Collections.sort(crings, new MapleRing.RingComparator());
        Collections.sort(mrings, new MapleRing.RingComparator());
        return new Triple<>(crings, frings, mrings);
    }

    public int getFH() {
        MapleFoothold fh = getMap().getFootholds().findBelow(getTruePosition());
        if (fh != null) {
            return fh.getId();
        }
        return 0;
    }

    public void startFairySchedule(boolean exp) {
        startFairySchedule(exp, false);
    }

    public void startFairySchedule(boolean exp, boolean equipped) {
        cancelFairySchedule((exp) || (this.stats.equippedFairy == 0));
        if (this.fairyExp <= 0) {
            this.fairyExp = ((byte) this.stats.equippedFairy);
        }
        if ((equipped) && (this.fairyExp < this.stats.equippedFairy * 3) && (this.stats.equippedFairy > 0)) {
            dropMessage(5, new StringBuilder().append("The Fairy Pendant's experience points will increase to ").append(this.fairyExp + this.stats.equippedFairy).append("% after one hour.").toString());
        }
        this.lastFairyTime = System.currentTimeMillis();
    }

    public final boolean canFairy(long now) {
        return (this.lastFairyTime > 0L) && (this.lastFairyTime + 3600000L < now);
    }

    public final boolean canHP(long now) {
        if (this.lastHPTime + 5000L < now) {
            this.lastHPTime = now;
            return true;
        }
        return false;
    }

    public final boolean canMP(long now) {
        if (this.lastMPTime + 5000L < now) {
            this.lastMPTime = now;
            return true;
        }
        return false;
    }

    public final boolean canHPRecover(long now) {
        if ((this.stats.hpRecoverTime > 0) && (this.lastHPTime + this.stats.hpRecoverTime < now)) {
            this.lastHPTime = now;
            return true;
        }
        return false;
    }

    public final boolean canMPRecover(long now) {
        if ((this.stats.mpRecoverTime > 0) && (this.lastMPTime + this.stats.mpRecoverTime < now)) {
            this.lastMPTime = now;
            return true;
        }
        return false;
    }

    public void cancelFairySchedule(boolean exp) {
        this.lastFairyTime = 0L;
        if (exp) {
            this.fairyExp = 0;
        }
    }

    public void doFairy() {
        if ((this.fairyExp < this.stats.equippedFairy * 3) && (this.stats.equippedFairy > 0)) {
            this.fairyExp = ((byte) (this.fairyExp + this.stats.equippedFairy));
            dropMessage(5, new StringBuilder().append("The Fairy Pendant's EXP was boosted to ").append(this.fairyExp).append("%.").toString());
        }
        if (getGuildId() > 0) {
            World.Guild.gainGP(getGuildId(), 20, this.id);
            this.client.getSession().write(CWvsContext.InfoPacket.getGPContribution(20));
        }
        ((MapleTrait) this.traits.get(MapleTrait.MapleTraitType.will)).addExp(5, this);
        startFairySchedule(false, true);
    }

    public byte getFairyExp() {
        return this.fairyExp;
    }

    public int getTeam() {
        return this.coconutteam;
    }

    public void setTeam(int v) {
        this.coconutteam = v;
    }

    public void spawnPet(byte slot) {
        spawnPet(slot, false, true);
    }

    public void spawnPet(byte slot, boolean lead) {
        spawnPet(slot, lead, true);
    }

    public void spawnPet(byte slot, boolean lead, boolean broadcast) {
        Item item = getInventory(MapleInventoryType.CASH).getItem((short) slot);
        if ((item == null) || (item.getItemId() > 5000400) || (item.getItemId() < 5000000)) {
            return;
        }
        switch (item.getItemId()) {
            case 5000028:
            case 5000047: {
                MaplePet pet = MaplePet.createPet(item.getItemId() + 1, MapleInventoryIdentifier.getInstance());
                if (pet != null) {
                    MapleInventoryManipulator.addById(this.client, item.getItemId() + 1, (short) 1, item.getOwner(), pet, 45L, new StringBuilder().append("Evolved from pet ").append(item.getItemId()).append(" on ").append(FileoutputUtil.CurrentReadable_Date()).toString());
                    MapleInventoryManipulator.removeFromSlot(this.client, MapleInventoryType.CASH, (short) slot, (short) 1, false);
                }
                break;
            }
            default:
                MaplePet pet = item.getPet();
                if ((pet != null) && ((item.getItemId() != 5000054) || (pet.getSecondsLeft() > 0)) && ((item.getExpiration() == -1L) || (item.getExpiration() > System.currentTimeMillis()))) {
                    if (pet.getSummoned()) {
                        unequipPet(pet, true, false);
                    } else {
                        int leadid = 8;
                        if (GameConstants.isKOC(getJob())) {
                            leadid = 10000018;
                        } else if (GameConstants.isAran(getJob())) {
                            leadid = 20000024;
                        } else if (GameConstants.isEvan(getJob())) {
                            leadid = 20011024;
                        } else if (GameConstants.isMercedes(getJob())) {
                            leadid = 20021024;
                        } else if (GameConstants.isPhantom(getJob())) {
                            leadid = 20031024;
                        } else if (GameConstants.isDemon(getJob())) {
                            leadid = 30011024;
                        } else if (GameConstants.isResist(getJob())) {
                            leadid = 30001024;
                        } else if (GameConstants.demonAvenger(getJob())) {
                            leadid = 30021024;
                        } else if (GameConstants.hayato(getJob())) {
                            leadid = 40011024;
                        } else if (GameConstants.kanna(getJob())) {
                            leadid = 40021024;
                        } else if (GameConstants.angelic(getJob())) {
                            leadid = 60011024;
                        } else if (GameConstants.kaiser(getJob())) {
                            leadid = 60001024;
                        } else if (GameConstants.isMihile(getJob())) {
                            leadid = 50001018;
                        } else if (GameConstants.luminous(getJob())) {
                            leadid = 20041024;
                        }

                        if ((getSkillLevel(SkillFactory.getSkill(leadid)) == 0) && (getPet(0) != null)) {
                            unequipPet(getPet(0), false, false);
                        } else if ((!lead) || (getSkillLevel(SkillFactory.getSkill(leadid)) <= 0));
                        Point pos = getPosition();
                        pet.setPos(pos);
                        try {
                            pet.setFh(getMap().getFootholds().findBelow(pos).getId());
                        } catch (NullPointerException e) {
                            pet.setFh(0);
                        }
                        pet.setStance(0);
                        pet.setSummoned(1);
                        addPet(pet);
                        pet.setSummoned(getPetIndex(pet) + 1);
                        if ((broadcast) && (getMap() != null)) {
                            getMap().broadcastMessage(this, PetPacket.showPet(this, pet, false, false), true);
                            this.client.getSession().write(PetPacket.showPetUpdate(this, pet.getUniqueId(), (byte) (pet.getSummonedValue() - 1)));
                            if (!GameConstants.GMS);
                        }
                    }
                }
                break;
        }

        this.client.getSession().write(CWvsContext.enableActions());
    }

    public void clearLinkMid() {
        this.linkMobs.clear();
        cancelEffectFromBuffStat(MapleBuffStat.HOMING_BEACON);
        cancelEffectFromBuffStat(MapleBuffStat.ARCANE_AIM);
    }

    public int getFirstLinkMid() {
        Iterator i$ = this.linkMobs.keySet().iterator();
        if (i$.hasNext()) {
            Integer lm = (Integer) i$.next();
            return lm.intValue();
        }
        return 0;
    }

    public Map<Integer, Integer> getAllLinkMid() {
        return this.linkMobs;
    }

    public void setLinkMid(int lm, int x) {
        this.linkMobs.put(Integer.valueOf(lm), Integer.valueOf(x));
    }

    public int getDamageIncrease(int lm) {
        if (this.linkMobs.containsKey(Integer.valueOf(lm))) {
            return ((Integer) this.linkMobs.get(Integer.valueOf(lm))).intValue();
        }
        return 0;
    }

    public boolean isClone() {
        return this.clone;
    }

    public void setClone(boolean c) {
        this.clone = c;
    }

    public WeakReference<MapleCharacter>[] getClones() {
        return this.clones;
    }

    public MapleCharacter cloneLooks() {
        MapleClient cs = new MapleClient(null, null, new MockIOSession());

        int minus = getId() + Randomizer.nextInt(2147483647 - getId());

        MapleCharacter ret = new MapleCharacter(true);
        ret.id = minus;
        ret.client = cs;
        ret.exp.set(0);
        ret.meso = 0L;
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.accountid = this.client.getAccID();
        ret.name = this.name;
        ret.level = this.level;
        ret.fame = this.fame;
        ret.job = this.job;
        ret.hair = this.hair;
        ret.face = this.face;
        ret.demonMarking = this.demonMarking;
        ret.skinColor = this.skinColor;
        ret.monsterbook = this.monsterbook;
        ret.mount = this.mount;
        ret.CRand = new PlayerRandomStream();
        ret.gmLevel = this.gmLevel;
        ret.gender = this.gender;
        ret.mapid = this.map.getId();
        ret.map = this.map;
        ret.setStance(getStance());
        ret.chair = this.chair;
        ret.itemEffect = this.itemEffect;
        ret.guildid = this.guildid;
        ret.currentrep = this.currentrep;
        ret.totalrep = this.totalrep;
        ret.stats = this.stats;
        ret.effects.putAll(this.effects);
        ret.dispelSummons();
        ret.guildrank = this.guildrank;
        ret.guildContribution = this.guildContribution;
        ret.allianceRank = this.allianceRank;
        ret.setPosition(getTruePosition());
        for (Item equip : getInventory(MapleInventoryType.EQUIPPED).newList()) {
            ret.getInventory(MapleInventoryType.EQUIPPED).addFromDB(equip.copy());
        }
        ret.skillMacros = this.skillMacros;
        ret.keylayout = this.keylayout;
        ret.questinfo = this.questinfo;
        ret.savedLocations = this.savedLocations;
        ret.wishlist = this.wishlist;
        ret.buddylist = this.buddylist;
        ret.keydown_skill = 0L;
        ret.lastmonthfameids = this.lastmonthfameids;
        ret.lastfametime = this.lastfametime;

        ret.loginTime = this.loginTime;

        ret.storage = this.storage;
        ret.cs = this.cs;
        ret.client.setAccountName(this.client.getAccountName());
        ret.nxcredit = this.nxcredit;
        ret.acash = this.acash;
        ret.maplepoints = this.maplepoints;
        ret.clone = true;
        ret.client.setChannel(this.client.getChannel());
        while ((this.map.getCharacterById(ret.id) != null) || (this.client.getChannelServer().getPlayerStorage().getCharacterById(ret.id) != null)) {
            ret.id += 1;
        }
        ret.client.setPlayer(ret);
        return ret;
    }

    public final void cloneLook() {
        if (this.clone) {
            return;
        }
        for (int i = 0; i < this.clones.length; i++) {
            if (this.clones[i].get() == null) {
                MapleCharacter newp = cloneLooks();
                this.map.addPlayer(newp);
                this.map.broadcastMessage(CField.updateCharLook(newp));
                this.map.movePlayer(newp, getTruePosition());
                this.clones[i] = new WeakReference(newp);
                return;
            }
        }
    }

    public final void disposeClones() {
        this.numClones = 0;
        for (int i = 0; i < this.clones.length; i++) {
            if (this.clones[i].get() != null) {
                this.map.removePlayer((MapleCharacter) this.clones[i].get());
                if (((MapleCharacter) this.clones[i].get()).getClient() != null) {
                    ((MapleCharacter) this.clones[i].get()).getClient().setPlayer(null);
                    ((MapleCharacter) this.clones[i].get()).client = null;
                }
                this.clones[i] = new WeakReference(null);
                this.numClones = ((byte) (this.numClones + 1));
            }
        }
    }

    public final int getCloneSize() {
        int z = 0;
        for (int i = 0; i < this.clones.length; i++) {
            if (this.clones[i].get() != null) {
                z++;
            }
        }
        return z;
    }

    public void spawnClones() {
        if (!isGM()) {
            this.numClones = ((byte) (this.stats.hasClone ? 1 : 0));
        }
        for (int i = 0; i < this.numClones; i++) {
            cloneLook();
        }
        this.numClones = 0;
    }

    public byte getNumClones() {
        return this.numClones;
    }

    public void setDragon(MapleDragon d) {
        this.dragon = d;
    }

    public void setHaku(MapleHaku d) {
        this.Haku = d;
    }

    public MapleExtractor getExtractor() {
        return this.extractor;
    }

    public void setExtractor(MapleExtractor me) {
        removeExtractor();
        this.extractor = me;
    }

    public void removeExtractor() {
        if (this.extractor != null) {
            this.map.broadcastMessage(CField.removeExtractor(this.id));
            this.map.removeMapObject(this.extractor);
            this.extractor = null;
        }
    }

    public final void spawnSavedPets() {
        for (int i = 0; i < this.petStore.length; i++) {
            if (this.petStore[i] > -1) {
                spawnPet(this.petStore[i], false, false);
            }
        }
        if (GameConstants.GMS) {
            this.client.getSession().write(PetPacket.petStatUpdate(this));
        }
        this.petStore = new byte[]{-1, -1, -1};
    }

    public final byte[] getPetStores() {
        return this.petStore;
    }

    public void resetStats(int str, int dex, int int_, int luk) {
        Map stat = new EnumMap(MapleStat.class);
        int total = this.stats.getStr() + this.stats.getDex() + this.stats.getLuk() + this.stats.getInt() + getRemainingAp();

        total -= str;
        this.stats.str = str;

        total -= dex;
        this.stats.dex = dex;

        total -= int_;
        this.stats.int_ = int_;

        total -= luk;
        this.stats.luk = luk;

        setRemainingAp(total);
        this.stats.recalcLocalStats(this);
        stat.put(MapleStat.STR, Long.valueOf(str));
        stat.put(MapleStat.DEX, Long.valueOf(dex));
        stat.put(MapleStat.INT, Long.valueOf(int_));
        stat.put(MapleStat.LUK, Long.valueOf(luk));
        client.getSession().write(CWvsContext.updatePlayerStats(stat, false, this));
    }

    public Event_PyramidSubway getPyramidSubway() {
        return this.pyramidSubway;
    }

    public void setPyramidSubway(Event_PyramidSubway ps) {
        this.pyramidSubway = ps;
    }

    public final int getDamage2() {
        return (int) this.damage;
    }

    public final long getDamage() {
        return this.damage;
    }

    public final void setDamage2(int damage4) {
        this.damage += damage4;
    }

    public final void setDamage(long damage3) {
        this.damage = damage3;
    }

    public int getshowdamage() {
        return this.showdamage;
    }

    public void setshowdamage(int ax) {
        this.showdamage = ax;
    }

    public void gainDamage(int min, int max) {
        if (getDamage() > 1000000000000000000L) {
            dropMessage(6, "Cannot gain more Damage as you're at Max.");
            return;
        }
        int randomNum = Randomizer.nextInt(max - min + 1) + min;
        setDamage(getDamage() + randomNum);
        dropMessage(5, new StringBuilder().append("You have gained ").append(randomNum).append("Damage.").toString());
    }

    public void doERB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(2218);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 2218);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void doDBRB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(434);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 434);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void doARB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(2112);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 2112);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public boolean isNotGM() {
        return gmLevel <= 1;
    }

    public void doEXPRB2() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(0);
        setRemainingAp(getRemainingAp() + 100);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 0);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void doEXPRB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(0);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 0);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void doMRB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(3512);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 3512);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void doWHRB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(3312);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 3312);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void doBAMRB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(3212);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 3212);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void doPHANTOMRB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(2412);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 2412);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void doMIRB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(5112);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 5112);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void doJETTRB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(572);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 572);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void doMERCRB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(2312);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 2312);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void doCANNONRB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(532);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 532);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void doDSRB() {
        setReborns(getReborns() + 1);
        setLevel((short) 3);
        setExp(0);
        setJob(3112);
        updateSingleStat(MapleStat.LEVEL, 2);
        updateSingleStat(MapleStat.JOB, 3112);
        updateSingleStat(MapleStat.EXP, 0);
    }

    public void resetSRB() {
        this.stats.dex = 4;
        this.stats.str = 4;
        this.stats.luk = 4;
        this.stats.int_ = 4;
        updateSingleStat(MapleStat.STR, 4);
        updateSingleStat(MapleStat.DEX, 4);
        updateSingleStat(MapleStat.INT, 4);
        updateSingleStat(MapleStat.LUK, 4);
    }

    public byte getSubcategory() {
        if (GameConstants.isJett(this.job)) {
            return 10;
        }
        if ((this.job >= 430) && (this.job <= 434)) {
            return 1;
        }
        if ((GameConstants.isCannon(this.job)) || (this.job == 1)) {
            return 2;
        }
        if ((this.job != 0) && (this.job != 400)) {
            return 0;
        }
        return this.subcategory;
    }

    public void setSubcategory(int z) {
        this.subcategory = ((byte) z);
    }

    public int itemQuantity(int itemid) {
        return getInventory(GameConstants.getInventoryType(itemid)).countById(itemid);
    }

    public void setRPS(RockPaperScissors rps) {
        this.rps = rps;
    }

    public RockPaperScissors getRPS() {
        return this.rps;
    }

    public long getNextConsume() {
        return this.nextConsume;
    }

    public void setNextConsume(long nc) {
        this.nextConsume = nc;
    }

    public int getRank() {
        return this.rank;
    }

    public int getRankMove() {
        return this.rankMove;
    }

    public int getJobRank() {
        return this.jobRank;
    }

        public void showInfo(String caption, boolean pink, String msg) {
        short type = (short) (pink ? 6 : 7);
        if (caption == null || caption.isEmpty()) {
            client.getSession().write(CField.getGameMessage(msg, type));
        } else {
            client.getSession().write(CField.getGameMessage("[" + caption + "] " + msg, type));
        }
    }
    
    public int getJobRankMove() {
        return this.jobRankMove;
    }

    public void changeChannel(int channel) {
        ChannelServer toch = ChannelServer.getInstance(channel);

        if (channel == client.getChannel() || toch == null || toch.isShutdown()) {
            client.getSession().write(CField.serverBlocked(1));
            return;
        }
        changeRemoval();

        ChannelServer ch = ChannelServer.getInstance(this.client.getChannel());
        if (getMessenger() != null) {
            World.Messenger.silentLeaveMessenger(getMessenger().getId(), new MapleMessengerCharacter(this));
        }
        PlayerBuffStorage.addBuffsToStorage(getId(), getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(getId(), getCooldowns());
        PlayerBuffStorage.addDiseaseToStorage(getId(), getAllDiseases());
        World.ChannelChange_Data(new CharacterTransfer(this), getId(), channel);
        ch.removePlayer(this);
        client.updateLoginState(MapleClient.CHANGE_CHANNEL, client.getSessionIPAddress());
        final String s = client.getSessionIPAddress();
        LoginServer.addIPAuth(s.substring(s.indexOf('/') + 1, s.length()));
        client.getSession().write(CField.getChannelChange(client, Integer.parseInt(toch.getIP().split(":")[1])));
        saveToDB(false, false);
        getMap().removePlayer(this);

        this.client.setPlayer(this);
        this.client.setReceiving(false);
    }

    public void expandInventory(byte type, int amount) {
        MapleInventory inv = getInventory(MapleInventoryType.getByType(type));
        inv.addSlot((byte) amount);
        this.client.getSession().write(CWvsContext.InventoryPacket.getSlotUpdate(type, inv.getSlotLimit()));
    }

    public boolean allowedToTarget(MapleCharacter other) {
        return other != null && !other.isHidden() || client.getPlayer().getGMLevel() >= 4;
    }

    public int getFollowId() {
        return this.followid;
    }

    public void setFollowId(int fi) {
        this.followid = fi;
        if (fi == 0) {
            this.followinitiator = false;
            this.followon = false;
        }
    }

    public void setFollowInitiator(boolean fi) {
        this.followinitiator = fi;
    }

    public void setFollowOn(boolean fi) {
        this.followon = fi;
    }

    public boolean isFollowOn() {
        return this.followon;
    }

    public boolean isFollowInitiator() {
        return this.followinitiator;
    }

    public void checkFollow() {
        if (this.followid <= 0) {
            return;
        }
        if (this.followon) {
            this.map.broadcastMessage(CField.followEffect(this.id, 0, null));
            this.map.broadcastMessage(CField.followEffect(this.followid, 0, null));
        }
        MapleCharacter tt = this.map.getCharacterById(this.followid);
        this.client.getSession().write(CField.getFollowMessage("Follow canceled."));
        if (tt != null) {
            tt.setFollowId(0);
            tt.getClient().getSession().write(CField.getFollowMessage("Follow canceled."));
        }
        setFollowId(0);
    }

    public int getMarriageId() {
        return this.marriageId;
    }

    public void setMarriageId(int mi) {
        this.marriageId = mi;
    }

    public int getMarriageItemId() {
        return this.marriageItemId;
    }

    public void setMarriageItemId(int mi) {
        this.marriageItemId = mi;
    }

    public boolean isStaff() {
        return this.gmLevel >= ServerConstants.PlayerGMRank.INTERN.getLevel();
    }

    public boolean isDonator() {
        return this.gmLevel >= ServerConstants.PlayerGMRank.DONATOR.getLevel();
    }

    public boolean startPartyQuest(int questid) {
        boolean ret = false;
        MapleQuest q = MapleQuest.getInstance(questid);
        if ((q == null) || (!q.isPartyQuest())) {
            return false;
        }
        if ((!this.quests.containsKey(q)) || (!this.questinfo.containsKey(Integer.valueOf(questid)))) {
            MapleQuestStatus status = getQuestNAdd(q);
            status.setStatus((byte) 1);
            updateQuest(status);
            switch (questid) {
                case 1300:
                case 1301:
                case 1302:
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;rank=F;try=0;cmp=0;CR=0;VR=0;gvup=0;vic=0;lose=0;draw=0");
                    break;
                case 1303:
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;have1=0;rank=F;try=0;cmp=0;CR=0;VR=0;vic=0;lose=0");
                    break;
                case 1204:
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have0=0;have1=0;have2=0;have3=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
                case 1206:
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have0=0;have1=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
                default:
                    updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;rank=F;try=0;cmp=0;CR=0;VR=0");
            }

            ret = true;
        }
        return ret;
    }

    public String getOneInfo(int questid, String key) {
        if ((!this.questinfo.containsKey(Integer.valueOf(questid))) || (key == null) || (MapleQuest.getInstance(questid) == null) || (!MapleQuest.getInstance(questid).isPartyQuest())) {
            return null;
        }
        String[] split = ((String) this.questinfo.get(Integer.valueOf(questid))).split(";");
        for (String x : split) {
            String[] split2 = x.split("=");
            if ((split2.length == 2) && (split2[0].equals(key))) {
                return split2[1];
            }
        }
        return null;
    }

    public void updateOneInfo(int questid, String key, String value) {
        if ((!this.questinfo.containsKey(Integer.valueOf(questid))) || (key == null) || (value == null) || (MapleQuest.getInstance(questid) == null) || (!MapleQuest.getInstance(questid).isPartyQuest())) {
            return;
        }
        String[] split = ((String) this.questinfo.get(Integer.valueOf(questid))).split(";");
        boolean changed = false;
        StringBuilder newQuest = new StringBuilder();
        for (String x : split) {
            String[] split2 = x.split("=");
            if (split2.length == 2) {
                if (split2[0].equals(key)) {
                    newQuest.append(key).append("=").append(value);
                } else {
                    newQuest.append(x);
                }
                newQuest.append(";");
                changed = true;
            }
        }
        updateInfoQuest(questid, changed ? newQuest.toString().substring(0, newQuest.toString().length() - 1) : newQuest.toString());
    }

    public int getDGM() {
        return dgm;
    }

    public void recalcPartyQuestRank(int questid) {
        if ((MapleQuest.getInstance(questid) == null) || (!MapleQuest.getInstance(questid).isPartyQuest())) {
            return;
        }
        if (!startPartyQuest(questid)) {
            String oldRank = getOneInfo(questid, "rank");
            if ((oldRank == null) || (oldRank.equals("S"))) {
                return;
            }
            String newRank = null;
            if (oldRank.equals("A")) {
                newRank = "S";
            } else if (oldRank.equals("B")) {
                newRank = "A";
            } else if (oldRank.equals("C")) {
                newRank = "B";
            } else if (oldRank.equals("D")) {
                newRank = "C";
            } else if (oldRank.equals("F")) {
                newRank = "D";
            } else {
                return;
            }
            List<Pair<String, Pair<String, Integer>>> questInfo = MapleQuest.getInstance(questid).getInfoByRank(newRank);
            if (questInfo == null) {
                return;
            }
            for (Pair q : questInfo) {
                boolean found = false;
                String val = getOneInfo(questid, (String) ((Pair) q.right).left);
                if (val == null) {
                    return;
                }
                int vall = 0;
                try {
                    vall = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    return;
                }
                if (((String) q.left).equals("less")) {
                    found = vall < ((Integer) ((Pair) q.right).right).intValue();
                } else if (((String) q.left).equals("more")) {
                    found = vall > ((Integer) ((Pair) q.right).right).intValue();
                } else if (((String) q.left).equals("equal")) {
                    found = vall == ((Integer) ((Pair) q.right).right).intValue();
                }
                if (!found) {
                    return;
                }
            }

            updateOneInfo(questid, "rank", newRank);
        }
    }

    public void tryPartyQuest(int questid) {
        if ((MapleQuest.getInstance(questid) == null) || (!MapleQuest.getInstance(questid).isPartyQuest())) {
            return;
        }
        try {
            startPartyQuest(questid);
            this.pqStartTime = System.currentTimeMillis();
            updateOneInfo(questid, "try", String.valueOf(Integer.parseInt(getOneInfo(questid, "try")) + 1));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("tryPartyQuest error");
        }
    }

    public void endPartyQuest(int questid) {
        if ((MapleQuest.getInstance(questid) == null) || (!MapleQuest.getInstance(questid).isPartyQuest())) {
            return;
        }
        try {
            startPartyQuest(questid);
            if (this.pqStartTime > 0L) {
                long changeTime = System.currentTimeMillis() - this.pqStartTime;
                int mins = (int) (changeTime / 1000L / 60L);
                int secs = (int) (changeTime / 1000L % 60L);
                int mins2 = Integer.parseInt(getOneInfo(questid, "min"));
                if ((mins2 <= 0) || (mins < mins2)) {
                    updateOneInfo(questid, "min", String.valueOf(mins));
                    updateOneInfo(questid, "sec", String.valueOf(secs));
                    updateOneInfo(questid, "date", FileoutputUtil.CurrentReadable_Date());
                }
                int newCmp = Integer.parseInt(getOneInfo(questid, "cmp")) + 1;
                updateOneInfo(questid, "cmp", String.valueOf(newCmp));
                updateOneInfo(questid, "CR", String.valueOf((int) Math.ceil(newCmp * 100.0D / Integer.parseInt(getOneInfo(questid, "try")))));
                recalcPartyQuestRank(questid);
                this.pqStartTime = 0L;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("endPartyQuest error");
        }
    }

    public void havePartyQuest(int itemId) {
        int questid = 0;
        int index = -1;
        switch (itemId) {
            case 1002798:
                questid = 1200;
                break;
            case 1072369:
                questid = 1201;
                break;
            case 1022073:
                questid = 1202;
                break;
            case 1082232:
                questid = 1203;
                break;
            case 1002571:
            case 1002572:
            case 1002573:
            case 1002574:
                questid = 1204;
                index = itemId - 1002571;
                break;
            case 1102226:
                questid = 1303;
                break;
            case 1102227:
                questid = 1303;
                index = 0;
                break;
            case 1122010:
                questid = 1205;
                break;
            case 1032060:
            case 1032061:
                questid = 1206;
                index = itemId - 1032060;
                break;
            case 3010018:
                questid = 1300;
                break;
            case 1122007:
                questid = 1301;
                break;
            case 1122058:
                questid = 1302;
                break;
            default:
                return;
        }
        if ((MapleQuest.getInstance(questid) == null) || (!MapleQuest.getInstance(questid).isPartyQuest())) {
            return;
        }
        startPartyQuest(questid);
        updateOneInfo(questid, new StringBuilder().append("have").append(index == -1 ? "" : Integer.valueOf(index)).toString(), "1");
    }

    public void resetStatsByJob(boolean beginnerJob) {
        int baseJob = beginnerJob ? this.job % 1000 : this.job % 1000 / 100 * 100;
        boolean UA = getQuestNoAdd(MapleQuest.getInstance(111111)) != null;
        if (baseJob == 100) {
            resetStats(UA ? 4 : 35, 4, 4, 4);
        } else if (baseJob == 200) {
            resetStats(4, 4, UA ? 4 : 20, 4);
        } else if ((baseJob == 300) || (baseJob == 400)) {
            resetStats(4, UA ? 4 : 25, 4, 4);
        } else if (baseJob == 500) {
            resetStats(4, UA ? 4 : 20, 4, 4);
        } else if (baseJob == 0) {
            resetStats(4, 4, 4, 4);
        }
    }

    public boolean hasSummon() {
        return this.hasSummon;
    }

    public void setHasSummon(boolean summ) {
        this.hasSummon = summ;
    }

    public void removeDoor() {
        MapleDoor door = (MapleDoor) getDoors().iterator().next();
        for (MapleCharacter chr : door.getTarget().getCharactersThreadsafe()) {
            door.sendDestroyData(chr.getClient());
        }
        for (MapleCharacter chr : door.getTown().getCharactersThreadsafe()) {
            door.sendDestroyData(chr.getClient());
        }
        for (MapleDoor destroyDoor : getDoors()) {
            door.getTarget().removeMapObject(destroyDoor);
            door.getTown().removeMapObject(destroyDoor);
        }
        clearDoors();
    }

    public void removeMechDoor() {
        for (MechDoor destroyDoor : getMechDoors()) {
            for (MapleCharacter chr : getMap().getCharactersThreadsafe()) {
                destroyDoor.sendDestroyData(chr.getClient());
            }
            getMap().removeMapObject(destroyDoor);
        }
        clearMechDoors();
    }

    public void changeRemoval() {
        changeRemoval(false);
    }

    public void changeRemoval(boolean dc) {
        removeFamiliar();
        dispelSummons();
        if (!dc) {
            cancelEffectFromBuffStat(MapleBuffStat.SOARING);
            cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
            cancelEffectFromBuffStat(MapleBuffStat.MECH_CHANGE);
            cancelEffectFromBuffStat(MapleBuffStat.RECOVERY);
        }
        if (getPyramidSubway() != null) {
            getPyramidSubway().dispose(this);
        }
        if ((this.playerShop != null) && (!dc)) {
            this.playerShop.removeVisitor(this);
            if (this.playerShop.isOwner(this)) {
                this.playerShop.setOpen(true);
            }
        }
        if (!getDoors().isEmpty()) {
            removeDoor();
        }
        if (!getMechDoors().isEmpty()) {
            removeMechDoor();
        }
        disposeClones();
        NPCScriptManager.getInstance().dispose(this.client);
        cancelFairySchedule(false);
    }

    public boolean canUseFamilyBuff(MapleFamilyBuff buff) {
        MapleQuestStatus stat = getQuestNoAdd(MapleQuest.getInstance(buff.questID));
        if (stat == null) {
            return true;
        }
        if (stat.getCustomData() == null) {
            stat.setCustomData("0");
        }
        return Long.parseLong(stat.getCustomData()) + 86400000L < System.currentTimeMillis();
    }

    public void useFamilyBuff(MapleFamilyBuff buff) {
        MapleQuestStatus stat = getQuestNAdd(MapleQuest.getInstance(buff.questID));
        stat.setCustomData(String.valueOf(System.currentTimeMillis()));
    }

    public List<Integer> usedBuffs() {
        List used = new ArrayList();
        MapleFamilyBuff[] z = MapleFamilyBuff.values();
        for (int i = 0; i < z.length; i++) {
            if (!canUseFamilyBuff(z[i])) {
                used.add(Integer.valueOf(i));
            }
        }
        return used;
    }

    public String getTeleportName() {
        return this.teleportname;
    }

    public void setTeleportName(String tname) {
        this.teleportname = tname;
    }

    public int getNoJuniors() {
        if (this.mfc == null) {
            return 0;
        }
        return this.mfc.getNoJuniors();
    }

    public MapleFamilyCharacter getMFC() {
        return this.mfc;
    }

    public void makeMFC(int familyid, int seniorid, int junior1, int junior2) {
        if (familyid > 0) {
            MapleFamily f = World.Family.getFamily(familyid);
            if (f == null) {
                this.mfc = null;
            } else {
                this.mfc = f.getMFC(this.id);
                if (this.mfc == null) {
                    this.mfc = f.addFamilyMemberInfo(this, seniorid, junior1, junior2);
                }
                if (this.mfc.getSeniorId() != seniorid) {
                    this.mfc.setSeniorId(seniorid);
                }
                if (this.mfc.getJunior1() != junior1) {
                    this.mfc.setJunior1(junior1);
                }
                if (this.mfc.getJunior2() != junior2) {
                    this.mfc.setJunior2(junior2);
                }
            }
        } else {
            this.mfc = null;
        }
    }

    public void setFamily(int newf, int news, int newj1, int newj2) {
        if ((this.mfc == null) || (newf != this.mfc.getFamilyId()) || (news != this.mfc.getSeniorId()) || (newj1 != this.mfc.getJunior1()) || (newj2 != this.mfc.getJunior2())) {
            makeMFC(newf, news, newj1, newj2);
        }
    }

    public int maxBattleshipHP(int skillid) {
        return getTotalSkillLevel(skillid) * 5000 + (getLevel() - 120) * 3000;
    }

    public int currentBattleshipHP() {
        return this.battleshipHP;
    }

    public void setBattleshipHP(int v) {
        this.battleshipHP = v;
    }

    public void decreaseBattleshipHP() {
        this.battleshipHP -= 1;
    }

    public int getGachExp() {
        return this.gachexp;
    }

    public void setGachExp(int ge) {
        this.gachexp = ge;
    }

    public boolean isInBlockedMap() {
        if ((!isAlive()) || (getPyramidSubway() != null) || (getMap().getSquadByMap() != null) || (getEventInstance() != null) || (getMap().getEMByMap() != null)) {
            return true;
        }
        if (((getMapId() >= 680000210) && (getMapId() <= 680000502)) || ((getMapId() / 10000 == 92502) && (getMapId() >= 925020100)) || (getMapId() / 10000 == 92503) || (getMapId() == 910310300)) {
            return true;
        }
        for (int i : GameConstants.blockedMaps) {
            if (getMapId() == i) {
                return true;
            }
        }
        return false;
    }

    public boolean isInTownMap() {
        if ((hasBlockedInventory()) || (!getMap().isTown()) || (FieldLimitType.VipRock.check(getMap().getFieldLimit())) || (getEventInstance() != null)) {
            return false;
        }
        for (int i : GameConstants.blockedMaps) {
            if (getMapId() == i) {
                return false;
            }
        }
        return true;
    }

    public boolean hasBlockedInventory() {
        return (!isAlive()) || (getTrade() != null) || (getConversation() > 0) || (getDirection() >= 0) || (getPlayerShop() != null) || (this.map == null);
    }

    public void startPartySearch(List<Integer> jobs, int maxLevel, int minLevel, int membersNeeded) {
        for (MapleCharacter chr : this.map.getCharacters()) {
            if ((chr.getId() != this.id) && (chr.getParty() == null) && (chr.getLevel() >= minLevel) && (chr.getLevel() <= maxLevel) && ((jobs.isEmpty()) || (jobs.contains(Integer.valueOf(chr.getJob())))) && ((isGM()) || (!chr.isGM()))) {
                if ((this.party == null) || (this.party.getMembers().size() >= 6) || (this.party.getMembers().size() >= membersNeeded)) {
                    break;
                }
                chr.setParty(this.party);
                World.Party.updateParty(this.party.getId(), PartyOperation.JOIN, new MaplePartyCharacter(chr));
                chr.receivePartyMemberHP();
                chr.updatePartyMemberHP();
            }
        }
    }

    public int getChallenge() {
        return this.challenge;
    }

    public void setChallenge(int c) {
        this.challenge = c;
    }

    public short getFatigue() {
        return this.fatigue;
    }

    public void setFatigue(int j) {
        this.fatigue = ((short) Math.max(0, j));
        updateSingleStat(MapleStat.FATIGUE, this.fatigue);
    }

    public void fakeRelog() {
        this.client.getSession().write(CField.getCharInfo(this));
        MapleMap mapp = getMap();
        mapp.setCheckStates(false);
        mapp.removePlayer(this);
        mapp.addPlayer(this);
        mapp.setCheckStates(true);

        this.client.getSession().write(CWvsContext.getFamiliarInfo(this));
    }

    public boolean canSummon() {
        return canSummon(5000);
    }

    public boolean canSummon(int g) {
        if (this.lastSummonTime + g < System.currentTimeMillis()) {
            this.lastSummonTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public int getIntNoRecord(int questID) {
        MapleQuestStatus stat = getQuestNoAdd(MapleQuest.getInstance(questID));
        if ((stat == null) || (stat.getCustomData() == null)) {
            return 0;
        }
        return Integer.parseInt(stat.getCustomData());
    }

    public int getIntRecord(int questID) {
        MapleQuestStatus stat = getQuestNAdd(MapleQuest.getInstance(questID));
        if (stat.getCustomData() == null) {
            stat.setCustomData("0");
        }
        return Integer.parseInt(stat.getCustomData());
    }

    public void updatePetAuto() {
        if (getIntNoRecord(122221) > 0) {
            this.client.getSession().write(CField.petAutoHP(getIntRecord(122221)));
        }
        if (getIntNoRecord(122223) > 0) {
            this.client.getSession().write(CField.petAutoMP(getIntRecord(122223)));
        }
    }

    public void sendEnglishQuiz(String msg) {
    }

    public void setChangeTime() {
        this.mapChangeTime = System.currentTimeMillis();
    }

    public long getChangeTime() {
        return this.mapChangeTime;
    }

    public short getScrolledPosition() {
        return this.scrolledPosition;
    }

    public void setScrolledPosition(short s) {
        this.scrolledPosition = s;
    }

    public MapleTrait getTrait(MapleTrait.MapleTraitType t) {
        return (MapleTrait) this.traits.get(t);
    }

    public void forceCompleteQuest(int id) {
        MapleQuest.getInstance(id).forceComplete(this, 9270035);
    }

    public List<Integer> getExtendedSlots() {
        return this.extendedSlots;
    }

    public int getExtendedSlot(int index) {
        if ((this.extendedSlots.size() <= index) || (index < 0)) {
            return -1;
        }
        return ((Integer) this.extendedSlots.get(index)).intValue();
    }

    public void changedExtended() {
        this.changed_extendedSlots = true;
    }

    public MapleAndroid getAndroid() {
        return this.android;
    }

    public void removeAndroid() {
        if (this.map != null) {
            this.map.broadcastMessage(CField.deactivateAndroid(this.id));
        }
        this.android = null;
    }

    public void setAndroid(MapleAndroid a) {
        this.android = a;
        if ((this.map != null) && (a != null)) {
            this.map.broadcastMessage(CField.spawnAndroid(this, a));
            this.map.broadcastMessage(CField.showAndroidEmotion(getId(), Randomizer.nextInt(17) + 1));
        }
    }

    public List<Item> getRebuy() {
        return this.rebuy;
    }

    public Map<Integer, MonsterFamiliar> getFamiliars() {
        return this.familiars;
    }

    public MonsterFamiliar getSummonedFamiliar() {
        return this.summonedFamiliar;
    }

    public void removeFamiliar() {
        if ((this.summonedFamiliar != null) && (this.map != null)) {
            removeVisibleFamiliar();
        }
        this.summonedFamiliar = null;
    }

    public void removeVisibleFamiliar() {
        getMap().removeMapObject(this.summonedFamiliar);
        removeVisibleMapObject(this.summonedFamiliar);
        getMap().broadcastMessage(CField.removeFamiliar(getId()));
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        cancelEffect(ii.getItemEffect(ii.getFamiliar(this.summonedFamiliar.getFamiliar()).passive), false, System.currentTimeMillis());
    }

    public void spawnFamiliar(MonsterFamiliar mf, boolean respawn) {
        this.summonedFamiliar = mf;

        mf.setStance(0);
        mf.setPosition(getPosition());
        mf.setFh(getFH());
        addVisibleMapObject(mf);
        getMap().spawnFamiliar(mf, respawn);

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleStatEffect eff = ii.getItemEffect(ii.getFamiliar(this.summonedFamiliar.getFamiliar()).passive);
        if ((eff != null) && (eff.getInterval() <= 0) && (eff.makeChanceResult())) {
            eff.applyTo(this);
        }
        this.lastFamiliarEffectTime = System.currentTimeMillis();
    }

    public final boolean canFamiliarEffect(long now, MapleStatEffect eff) {
        return (this.lastFamiliarEffectTime > 0L) && (this.lastFamiliarEffectTime + eff.getInterval() < now);
    }

    public void doFamiliarSchedule(long now) {
        if (this.familiars == null) {
            return;
        }
        for (MonsterFamiliar mf : this.familiars.values()) {
            if ((this.summonedFamiliar != null) && (this.summonedFamiliar.getId() == mf.getId())) {
                mf.addFatigue(this, 5);
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                MapleStatEffect eff = ii.getItemEffect(ii.getFamiliar(this.summonedFamiliar.getFamiliar()).passive);
                if ((eff != null) && (eff.getInterval() > 0) && (canFamiliarEffect(now, eff)) && (eff.makeChanceResult())) {
                    eff.applyTo(this);
                }
            } else if (mf.getFatigue() > 0) {
                mf.setFatigue(Math.max(0, mf.getFatigue() - 5));
            }
        }
    }

    public MapleImp[] getImps() {
        return this.imps;
    }

    public void sendImp() {
        for (int i = 0; i < this.imps.length; i++) {
            if (this.imps[i] != null) {
                this.client.getSession().write(CWvsContext.updateImp(this.imps[i], MapleImp.ImpFlag.SUMMONED.getValue(), i, true));
            }
        }
    }

    public int getBattlePoints() {
        return this.pvpPoints;
    }

    public int getTotalBattleExp() {
        return this.pvpExp;
    }

    public void setBattlePoints(int p) {
        if (p != this.pvpPoints) {
            this.client.getSession().write(CWvsContext.InfoPacket.getBPMsg(p - this.pvpPoints));
            updateSingleStat(MapleStat.BATTLE_POINTS, p);
        }
        this.pvpPoints = p;
    }

    public void setTotalBattleExp(int p) {
        int previous = this.pvpExp;
        this.pvpExp = p;
        if (p != previous) {
            this.stats.recalcPVPRank(this);

            updateSingleStat(MapleStat.BATTLE_EXP, this.stats.pvpExp);
            updateSingleStat(MapleStat.BATTLE_RANK, this.stats.pvpRank);
        }
    }

    public void changeTeam(int newTeam) {
        this.coconutteam = newTeam;

        if (inPVP()) {
            this.client.getSession().write(CField.getPVPTransform(newTeam + 1));
            this.map.broadcastMessage(CField.changeTeam(this.id, newTeam + 1));
        } else {
            this.client.getSession().write(CField.showEquipEffect(newTeam));
        }
    }

    public void disease(int type, int level) {
        if (MapleDisease.getBySkill(type) == null) {
            return;
        }
        this.chair = 0;
        this.client.getSession().write(CField.cancelChair(-1));
        this.map.broadcastMessage(this, CField.showChair(this.id, 0), false);
        giveDebuff(MapleDisease.getBySkill(type), MobSkillFactory.getMobSkill(type, level));
    }

    public boolean inPVP() {
        return (this.eventInstance != null) && (this.eventInstance.getName().startsWith("PVP"));
    }

    public void clearAllCooldowns() {
        for (MapleCoolDownValueHolder m : getCooldowns()) {
            int skil = m.skillId;
            removeCooldown(skil);
            this.client.getSession().write(CField.skillCooldown(skil, 0));
        }
    }

    public Pair<Double, Boolean> modifyDamageTaken(double damage, MapleMapObject attacke) {
        Pair ret = new Pair(Double.valueOf(damage), Boolean.valueOf(false));
        if (damage <= 0.0D) {
            return ret;
        }
        if ((this.stats.ignoreDAMr > 0) && (Randomizer.nextInt(100) < this.stats.ignoreDAMr_rate)) {
            damage -= Math.floor(this.stats.ignoreDAMr * damage / 100.0D);
        }
        if ((this.stats.ignoreDAM > 0) && (Randomizer.nextInt(100) < this.stats.ignoreDAM_rate)) {
            damage -= this.stats.ignoreDAM;
        }
        Integer div = getBuffedValue(MapleBuffStat.DIVINE_SHIELD);
        Integer div2 = getBuffedValue(MapleBuffStat.HOLY_MAGIC_SHELL);
        if (div2 != null) {
            if (div2.intValue() <= 0) {
                cancelEffectFromBuffStat(MapleBuffStat.HOLY_MAGIC_SHELL);
            } else {
                setBuffedValue(MapleBuffStat.HOLY_MAGIC_SHELL, div2.intValue() - 1);
                damage = 0.0D;
            }
        } else if (div != null) {
            if (div.intValue() <= 0) {
                cancelEffectFromBuffStat(MapleBuffStat.DIVINE_SHIELD);
            } else {
                setBuffedValue(MapleBuffStat.DIVINE_SHIELD, div.intValue() - 1);
                damage = 0.0D;
            }
        }
        MapleStatEffect barrier = getStatForBuff(MapleBuffStat.COMBO_BARRIER);
        if (barrier != null) {
            damage = barrier.getX() / 1000.0D * damage;
        }
        barrier = getStatForBuff(MapleBuffStat.MAGIC_SHIELD);
        if (barrier != null) {
            damage = barrier.getX() / 1000.0D * damage;
        }
        barrier = getStatForBuff(MapleBuffStat.WATER_SHIELD);
        if (barrier != null) {
            damage = barrier.getX() / 1000.0D * damage;
        }
        List attack = ((attacke instanceof MapleMonster)) || (attacke == null) ? null : new ArrayList();
        if (damage > 0.0D) {
            if ((getJob() == 122) && (!skillisCooling(1220013))) {
                Skill divine = SkillFactory.getSkill(1220013);
                if (getTotalSkillLevel(divine) > 0) {
                    MapleStatEffect divineShield = divine.getEffect(getTotalSkillLevel(divine));
                    if (divineShield.makeChanceResult()) {
                        divineShield.applyTo(this);
                        this.client.getSession().write(CField.skillCooldown(1220013, divineShield.getCooldown(this)));
                        addCooldown(1220013, System.currentTimeMillis(), divineShield.getCooldown(this) * 1000);
                    }
                }
            } else if ((getBuffedValue(MapleBuffStat.SATELLITESAFE_PROC) != null) && (getBuffedValue(MapleBuffStat.SATELLITESAFE_ABSORB) != null) && (getBuffedValue(MapleBuffStat.PUPPET) != null)) {
                double buff = getBuffedValue(MapleBuffStat.SATELLITESAFE_PROC).doubleValue();
                double buffz = getBuffedValue(MapleBuffStat.SATELLITESAFE_ABSORB).doubleValue();
                if ((int) (buff / 100.0D * getStat().getMaxHp()) <= damage) {
                    damage -= buffz / 100.0D * damage;
                    cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
                }
            } else if ((getJob() == 433) || (getJob() == 434)) {
                Skill divine = SkillFactory.getSkill(4330001);
                if ((getTotalSkillLevel(divine) > 0) && (getBuffedValue(MapleBuffStat.DARKSIGHT) == null) && (!skillisCooling(divine.getId()))) {
                    MapleStatEffect divineShield = divine.getEffect(getTotalSkillLevel(divine));
                    if (Randomizer.nextInt(100) < divineShield.getX()) {
                        divineShield.applyTo(this);
                    }
                }
            } else if (((getJob() == 512) || (getJob() == 522)) && (getBuffedValue(MapleBuffStat.PIRATES_REVENGE) == null)) {
                Skill divine = SkillFactory.getSkill(getJob() == 512 ? 5120011 : 5220012);
                if ((getTotalSkillLevel(divine) > 0) && (!skillisCooling(divine.getId()))) {
                    MapleStatEffect divineShield = divine.getEffect(getTotalSkillLevel(divine));
                    if (divineShield.makeChanceResult()) {
                        divineShield.applyTo(this);
                        this.client.getSession().write(CField.skillCooldown(divine.getId(), divineShield.getCooldown(this)));
                        addCooldown(divine.getId(), System.currentTimeMillis(), divineShield.getCooldown(this) * 1000);
                    }
                }
            } else if ((getJob() == 312) && (attacke != null)) {
                Skill divine = SkillFactory.getSkill(3120010);
                if (getTotalSkillLevel(divine) > 0) {
                    MapleStatEffect divineShield = divine.getEffect(getTotalSkillLevel(divine));
                    if (divineShield.makeChanceResult()) {
                        int i;
                        if ((attacke instanceof MapleMonster)) {
                            Rectangle bounds = divineShield.calculateBoundingBox(getTruePosition(), isFacingLeft());
                            List<MapleMapObject> affected = getMap().getMapObjectsInRect(bounds, Arrays.asList(new MapleMapObjectType[]{attacke.getType()}));
                            i = 0;

                            for (MapleMapObject mo : affected) {
                                MapleMonster mons = (MapleMonster) mo;
                                if ((!mons.getStats().isFriendly()) && (!mons.isFake())) {
                                    mons.applyStatus(this, new MonsterStatusEffect(MonsterStatus.STUN, Integer.valueOf(1), divineShield.getSourceId(), null, false), false, divineShield.getDuration(), true, divineShield);
                                    int theDmg = (int) (divineShield.getDamage() * getStat().getCurrentMaxBaseDamage() / 100.0D);
                                    mons.damage(this, theDmg, true);
                                    getMap().broadcastMessage(MobPacket.damageMonster(mons.getObjectId(), theDmg));
                                    i++;
                                    if (i >= divineShield.getMobCount()) {
                                        break;
                                    }
                                }
                            }
                        } else {
                            MapleCharacter chr = (MapleCharacter) attacke;
                            chr.addHP(-divineShield.getDamage());
                            attack.add(Integer.valueOf(divineShield.getDamage()));
                        }
                    }
                }
            } else if (((getJob() == 531) || (getJob() == 532)) && (attacke != null)) {
                Skill divine = SkillFactory.getSkill(5310009);
                if (getTotalSkillLevel(divine) > 0) {
                    MapleStatEffect divineShield = divine.getEffect(getTotalSkillLevel(divine));
                    if (divineShield.makeChanceResult()) {
                        if ((attacke instanceof MapleMonster)) {
                            MapleMonster attacker = (MapleMonster) attacke;
                            int theDmg = (int) (divineShield.getDamage() * getStat().getCurrentMaxBaseDamage() / 100.0D);
                            attacker.damage(this, theDmg, true);
                            getMap().broadcastMessage(MobPacket.damageMonster(attacker.getObjectId(), theDmg));
                        } else {
                            MapleCharacter attacker = (MapleCharacter) attacke;
                            attacker.addHP(-divineShield.getDamage());
                            attack.add(Integer.valueOf(divineShield.getDamage()));
                        }
                    }
                }
            } else if ((getJob() == 132) && (attacke != null)) {
                Skill divine = SkillFactory.getSkill(1320011);
                if ((getTotalSkillLevel(divine) > 0) && (!skillisCooling(divine.getId())) && (getBuffSource(MapleBuffStat.BEHOLDER) == 1321007)) {
                    MapleStatEffect divineShield = divine.getEffect(getTotalSkillLevel(divine));
                    if (divineShield.makeChanceResult()) {
                        this.client.getSession().write(CField.skillCooldown(divine.getId(), divineShield.getCooldown(this)));
                        addCooldown(divine.getId(), System.currentTimeMillis(), divineShield.getCooldown(this) * 1000);
                        if ((attacke instanceof MapleMonster)) {
                            MapleMonster attacker = (MapleMonster) attacke;
                            int theDmg = (int) (divineShield.getDamage() * getStat().getCurrentMaxBaseDamage() / 100.0D);
                            attacker.damage(this, theDmg, true);
                            getMap().broadcastMessage(MobPacket.damageMonster(attacker.getObjectId(), theDmg));
                        } else {
                            MapleCharacter attacker = (MapleCharacter) attacke;
                            attacker.addHP(-divineShield.getDamage());
                            attack.add(Integer.valueOf(divineShield.getDamage()));
                        }
                    }
                }
            }
            if (attacke != null) {
                int damr = (Randomizer.nextInt(100) < getStat().DAMreflect_rate ? getStat().DAMreflect : 0) + (getBuffedValue(MapleBuffStat.POWERGUARD) != null ? getBuffedValue(MapleBuffStat.POWERGUARD).intValue() : 0);
                int bouncedam_ = damr + (getBuffedValue(MapleBuffStat.PERFECT_ARMOR) != null ? getBuffedValue(MapleBuffStat.PERFECT_ARMOR).intValue() : 0);
                if (bouncedam_ > 0) {
                    long bouncedamage = (long) (damage * bouncedam_ / 100.0D);
                    long bouncer = (long) (damage * damr / 100.0D);
                    damage -= bouncer;
                    if ((attacke instanceof MapleMonster)) {
                        MapleMonster attacker = (MapleMonster) attacke;
                        bouncedamage = Math.min(bouncedamage, attacker.getMobMaxHp() / 10L);
                        attacker.damage(this, bouncedamage, true);
                        getMap().broadcastMessage(this, MobPacket.damageMonster(attacker.getObjectId(), bouncedamage), getTruePosition());
                        if (getBuffSource(MapleBuffStat.PERFECT_ARMOR) == 31101003) {
                            MapleStatEffect eff = getStatForBuff(MapleBuffStat.PERFECT_ARMOR);
                            if (eff.makeChanceResult()) {
                                attacker.applyStatus(this, new MonsterStatusEffect(MonsterStatus.STUN, Integer.valueOf(1), eff.getSourceId(), null, false), false, eff.getSubTime(), true, eff);
                            }
                        }
                    } else {
                        MapleCharacter attacker = (MapleCharacter) attacke;
                        bouncedamage = Math.min(bouncedamage, attacker.getStat().getCurrentMaxHp() / 10);
                        attacker.addHP(-(int) bouncedamage);
                        attack.add(Integer.valueOf((int) bouncedamage));
                        if (getBuffSource(MapleBuffStat.PERFECT_ARMOR) == 31101003) {
                            MapleStatEffect eff = getStatForBuff(MapleBuffStat.PERFECT_ARMOR);
                            if (eff.makeChanceResult()) {
                                attacker.disease(MapleDisease.STUN.getDisease(), 1);
                            }
                        }
                    }
                    ret.right = Boolean.valueOf(true);
                }
                if (((getJob() == 411) || (getJob() == 412) || (getJob() == 421) || (getJob() == 422)) && (getBuffedValue(MapleBuffStat.SUMMON) != null) && (attacke != null)) {
                    List<MapleSummon> ss = getSummonsReadLock();
                    try {
                        for (MapleSummon sum : ss) {

                            if ((sum.getTruePosition().distanceSq(getTruePosition()) < 400000.0D) && ((sum.getSkill() == 4111007) || (sum.getSkill() == 4211007))) {
                                List allDamage = new ArrayList();
                                if ((attacke instanceof MapleMonster)) {
                                    MapleMonster attacker = (MapleMonster) attacke;
                                    int theDmg = (int) (SkillFactory.getSkill(sum.getSkill()).getEffect(sum.getSkillLevel()).getX() * damage / 100.0D);
                                    allDamage.add(new Pair(Integer.valueOf(attacker.getObjectId()), Integer.valueOf(theDmg)));
                                    getMap().broadcastMessage(CField.SummonPacket.summonAttack(sum.getOwnerId(), sum.getObjectId(), (byte) -124, allDamage, getLevel(), true));
                                    attacker.damage(this, theDmg, true);
                                    checkMonsterAggro(attacker);
                                    if (!attacker.isAlive()) {
                                        getClient().getSession().write(MobPacket.killMonster(attacker.getObjectId(), 1));
                                    }
                                } else {
                                    MapleCharacter chr = (MapleCharacter) attacke;
                                    int dmg = SkillFactory.getSkill(sum.getSkill()).getEffect(sum.getSkillLevel()).getX();
                                    chr.addHP(-dmg);
                                    attack.add(Integer.valueOf(dmg));
                                }
                            }
                        }
                    } finally {
                        unlockSummonsReadLock();
                    }
                }
            }
        }
        if ((attack != null) && (attack.size() > 0) && (attacke != null)) {
            getMap().broadcastMessage(CField.pvpCool(attacke.getObjectId(), attack));
        }
        ret.left = Double.valueOf(damage);
        return ret;
    }

    public void onAttack(long maxhp, int maxmp, int skillid, int oid, int totDamage, int critCount) {
        if ((this.stats.hpRecoverProp > 0)
                && (Randomizer.nextInt(100) <= this.stats.hpRecoverProp)) {
            if (this.stats.hpRecover > 0) {
                healHP(this.stats.hpRecover);
            }
            if (this.stats.hpRecoverPercent > 0) {
                //   addHP((int) Math.min(maxhp, Math.min((int) (totDamage * this.stats.hpRecoverPercent / 100.0D), this.stats.getMaxHp() / 2)));
            }
        }

        if ((this.stats.mpRecoverProp > 0) && (!GameConstants.isDemon(getJob()))
                && (Randomizer.nextInt(100) <= this.stats.mpRecoverProp)) {
            healMP(this.stats.mpRecover);
        }

        if (getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null) {
            //   addHP((int) Math.min(maxhp, Math.min((int) (totDamage * getStatForBuff(MapleBuffStat.COMBO_DRAIN).getX() / 100.0D), this.stats.getMaxHp() / 2)));
        }
        if (getBuffSource(MapleBuffStat.COMBO_DRAIN) == 23101003) {
            addMP(Math.min(maxmp, Math.min((int) (totDamage * getStatForBuff(MapleBuffStat.COMBO_DRAIN).getX() / 100.0D), this.stats.getMaxMp() / 2)));
        }
        if ((getBuffedValue(MapleBuffStat.REAPER) != null) && (getBuffedValue(MapleBuffStat.SUMMON) == null) && (getSummonsSize() < 4) && (canSummon())) {
            MapleStatEffect eff = getStatForBuff(MapleBuffStat.REAPER);
            if (eff.makeChanceResult()) {
                eff.applyTo(this, this, false, null, eff.getDuration());
            }
        }
        if ((getJob() == 212) || (getJob() == 222) || (getJob() == 232)) {
            int[] skills = {2120010, 2220010, 2320011};
            for (int i : skills) {
                Skill skill = SkillFactory.getSkill(i);
                if (getTotalSkillLevel(skill) > 0) {
                    MapleStatEffect venomEffect = skill.getEffect(getTotalSkillLevel(skill));
                    if ((!venomEffect.makeChanceResult()) || (getAllLinkMid().size() >= venomEffect.getY())) {
                        break;
                    }
                    setLinkMid(oid, venomEffect.getX());
                    venomEffect.applyTo(this);
                    break;
                }
            }

        }

        int[] skills = {4110011, 4120005, 4210010, 4220005, 4320005, 4340001, 14110004};
        for (int i : skills) {
            if (i == 4110011) {
                if (getTotalSkillLevel(4120011) > 0) {
                    i = 4120011;
                }
            } else if ((i == 4210010)
                    && (getTotalSkillLevel(4220011) > 0)) {
                i = 4220011;
            }

            Skill skill = SkillFactory.getSkill(i);
            if (getTotalSkillLevel(skill) > 0) {
                MapleStatEffect venomEffect = skill.getEffect(getTotalSkillLevel(skill));
                MapleMonster monster = this.map.getMonsterByOid(oid);
                if ((!venomEffect.makeChanceResult()) || (monster == null)) {
                    break;
                }
                monster.applyStatus(this, new MonsterStatusEffect(MonsterStatus.POISON, Integer.valueOf(1), i, null, false), true, venomEffect.getDuration(), true, venomEffect);
                break;
            }

        }



        if (skillid > 0) {
            Skill skil = SkillFactory.getSkill(skillid);
            MapleStatEffect effect = skil.getEffect(getTotalSkillLevel(skil));
            switch (skillid) {
                case 1078:
                case 11078:
                case 3111008:
                case 4101005:
                case 5111004:
                case 14101006:
                case 15111001:
                case 31111003:
                case 33111006:
                    //  addHP((int) Math.min(maxhp, Math.min((int) (totDamage * effect.getX() / 100.0D), this.stats.getMaxHp() / 2)));
                    break;
                case 5211006:
                case 5220011:
                case 22151002:
                    setLinkMid(oid, effect.getX());
                    break;
                case 33101007:
                    clearLinkMid();
            }
        }
    }

    public void handleForceGain(int oid, int skillid) {
        handleForceGain(oid, skillid, 0);
    }

    public void handleForceGain(int oid, int skillid, int extraForce) {
        if ((!GameConstants.isForceIncrease(skillid)) && (extraForce <= 0)) {
            return;
        }
        int forceGain = 1;
        if ((getLevel() >= 30) && (getLevel() < 70)) {
            forceGain = 2;
        } else if ((getLevel() >= 70) && (getLevel() < 120)) {
            forceGain = 3;
        } else if (getLevel() >= 120) {
            forceGain = 4;
        }
        this.force = ((short) (this.force + 1));
        if (GameConstants.isDemon(getJob())) {
            addMP(extraForce > 0 ? extraForce : forceGain, true);
        }
        getClient().getSession().write(CField.gainForce(oid, this.force, forceGain));
        if ((GameConstants.isDemon(getJob()))
                && (this.stats.mpRecoverProp > 0) && (extraForce <= 0)
                && (Randomizer.nextInt(100) <= this.stats.mpRecoverProp)) {
            this.force = ((short) (this.force + 1));
            addMP(this.stats.mpRecover, true);
            getClient().getSession().write(CField.gainForce(oid, this.force, this.stats.mpRecover));
        }
    }

    public void afterAttack(int mobCount, int attackCount, int skillid) {
        switch (getJob()) {
            case 511:
            case 512:
                handleEnergyCharge(5110001, mobCount * attackCount);
                break;
            case 1510:
            case 1511:
            case 1512:
                handleEnergyCharge(15100004, mobCount * attackCount);
                break;
            case 422:
                if (skillid == 4221001) {
                    setBattleshipHP(0);
                } else {
                    setBattleshipHP(Math.min(5, currentBattleshipHP() + 1));
                }
                refreshBattleshipHP();
                break;
            case 111:
            case 112:
            case 1111:
            case 1112:
            case 2411:
            case 2412:
                if (((skillid != 1111008 ? 1 : 0) & (getBuffedValue(MapleBuffStat.COMBO) != null ? 1 : 0)) != 0) {
                    handleOrbgain();
                }
                break;
        }
        if (getBuffedValue(MapleBuffStat.OWL_SPIRIT) != null) {
            if (currentBattleshipHP() > 0) {
                decreaseBattleshipHP();
            }
            if (currentBattleshipHP() <= 0) {
                cancelEffectFromBuffStat(MapleBuffStat.OWL_SPIRIT);
            }
        }
        if (!isIntern()) {
            cancelEffectFromBuffStat(MapleBuffStat.WIND_WALK);
            cancelEffectFromBuffStat(MapleBuffStat.INFILTRATE);
            MapleStatEffect ds = getStatForBuff(MapleBuffStat.DARKSIGHT);
            if ((ds != null) && ((ds.getSourceId() != 4330001) || (!ds.makeChanceResult()))) {
                cancelEffectFromBuffStat(MapleBuffStat.DARKSIGHT);
            }
        }
    }

    public void applyIceGage(int x) {
        updateSingleStat(MapleStat.ICE_GAGE, x);
    }

    public Rectangle getBounds() {
        return new Rectangle(getTruePosition().x - 25, getTruePosition().y - 75, 50, 75);
    }

  /* public final Map<Byte, Integer> getEquips() {
        Map eq = new HashMap();
        for (Item item : this.inventory[MapleInventoryType.EQUIPPED.ordinal()].newList()) {
            eq.put(Byte.valueOf((byte) item.getPosition()), Integer.valueOf(item.getItemId()));
        }
        return eq;
    } */

    public void changeLuminousMode(final int skillid) {
        final boolean equilibrium = skillid == 20040220;
        final boolean eclipse = skillid == 20040217;
        final boolean sunfire = skillid == 20040216;
        final MapleCharacter chr = this;
        if (equilibrium || (!eclipse && !sunfire)) {
            return; //impossible
        }
        dispelBuff(skillid);
        luminousState = skillid;
        if (sunfire) {
            runningLight--;
        } else if (eclipse) {
            runningDark--;
        }
        client.getSession().write(JobPacket.LuminousPacket.updateLuminousGauge(runningDarkSlot, runningLightSlot, runningDark, runningLight));
        luminousState = 20040200;
        client.getSession().write(JobPacket.LuminousPacket.giveLuminousState(20040220, chr.getLightGauge(), chr.getDarkGauge(), 0));
        SkillFactory.getSkill(20040220).getEffect(1).applyTo(this);
        equipChanged();
        Timer.WorldTimer.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
                dispelBuff(20040220);
                client.getSession().write(JobPacket.LuminousPacket.giveLuminousState(skillid, chr.getLightGauge(), chr.getDarkGauge(), 0));
                SkillFactory.getSkill(skillid).getEffect(1).applyTo(chr);
            }
        }, 10000);
    }

    public void handleLuminous(int skillid) {
        int[] lightSkills = new int[]{27001100, 27101100, 27101101, 27111100, 27111101, 27121100};
        int[] darkSkills = new int[]{27001201, 27101202, 27111202, 27121201, 27121202, 27120211};
        boolean found = false;
        for (int light : lightSkills) {
            if (skillid == light) {
                runningLightSlot += Randomizer.nextInt(200) + 100;
                if (runningLightSlot > 10000) {
                    runningLightSlot = 0;
                    runningLight += 1;
                    if (runningLight > 5) {
                        runningLight = 5;
                    }
                }
                found = true;
            }
        }
        for (int dark : darkSkills) {
            if (skillid == dark) {
                runningDarkSlot += Randomizer.nextInt(200) + 100;
                if (runningDarkSlot > 10000) {
                    runningDarkSlot = 0;
                    runningDark += 1;
                    if (runningDark > 5) {
                        runningDark = 5;
                    }
                }
                found = true;
            }
        }
        if (!found) {
            return;
        }

        client.getSession().write(JobPacket.LuminousPacket.updateLuminousGauge(runningDarkSlot, runningLightSlot, runningDark, runningLight));
    }

    public final PlayerRandomStream CRand() {
        return this.CRand;
    }

    public void handleKaiserCombo() {
        if (this.kaisercombo < 1000) {
            this.kaisercombo = ((short) (this.kaisercombo + 100));
        }
        SkillFactory.getSkill(61111008).getEffect(1).applyKAISER_Combo(this, this.kaisercombo);
    }

    public void resetKaiserCombo() {
        this.kaisercombo = 0;
        SkillFactory.getSkill(61111008).getEffect(1).applyKAISER_Combo(this, this.kaisercombo);
    }

    public void handleCardStack() {
        Skill noir = SkillFactory.getSkill(24120002);
        Skill blanc = SkillFactory.getSkill(24100003);
        MapleStatEffect ceffect = null;
        int advSkillLevel = getTotalSkillLevel(noir);
        boolean isAdv = false;
        if (advSkillLevel > 0) {
            ceffect = noir.getEffect(advSkillLevel);
            isAdv = true;
        } else if (getSkillLevel(blanc) > 0) {
            ceffect = blanc.getEffect(getTotalSkillLevel(blanc));
        } else {
            return;
        }
        if ((getJob() == 2412) && (getCardStack() == 40)) {
            return;
        }
        if (((getJob() == 2400) && (getCardStack() == 20)) || ((getJob() == 2410) && (getCardStack() == 20)) || ((getJob() == 2411) && (getCardStack() == 20))) {
            return;
        }
        if (ceffect.makeChanceResult()) {
            if (this.cardStack < (getJob() == 2412 ? 40 : 20)) {
                this.cardStack = ((byte) (this.cardStack + 1));
            }
            this.runningStack = ((byte) (this.runningStack + 1));
            this.client.getSession().write(CField.gainCardStack(getId(), this.runningStack, isAdv ? 2 : 1, ceffect.getSourceId(), Randomizer.rand(100000, 500000), 1));
            this.client.getSession().write(CField.updateCardStack(this.cardStack));
        }
    }

    public void resetRunningStack() {
        this.runningStack = 0;
        this.runningdark = 0;
        this.runningdarkslot = 0;
        this.runninglight = 0;
        this.runninglightslot = 0;
    }

    public int getRunningStack() {
        return this.runningStack;
    }

    public void setReborns(int reborns) {
        this.reborns = reborns;
    }

    public void addRunningStack(int s) {
        this.runningStack = ((byte) (this.runningStack + s));
    }

    public void setCardStack(byte amount) {
        this.cardStack = amount;
    }
    
        public int getLightGauge() {
        return runningLightSlot;
    }

    public int getDarkGauge() {
        return runningDarkSlot;
    }

    public int getLuminousState() {
        return luminousState;
    }

    public void setLuminousState(int luminousState) {
        this.luminousState = luminousState;
    }

    public byte getCardStack() {
        return this.cardStack;
    }

    public final MapleCharacterCards getCharacterCard() {
        return this.characterCard;
    }

    public int getReborns() {
        return this.reborns;
    }

    public int getAPS() {
        return this.apstorage;
    }

    public void gainAPS(int aps) {
        this.apstorage += aps;
    }

    public final boolean canHold(int itemid) {
        return getInventory(GameConstants.getInventoryType(itemid)).getNextFreeSlot() > -1;
    }

    public void gainCurrency(short amount, boolean sound) {
        if (sound) {
            this.client.getSession().write(CField.playSound("Coconut/Victory"));
        }
        MapleInventoryManipulator.addById(this.client, 4001055, amount, "gainCurrency function");
        dropMessage(-1, new StringBuilder().append("You have gained ").append(amount).append(" ").append(amount > 1 ? new StringBuilder().append("").append(MapleItemInformationProvider.getInstance().getName(4001055)).append("s ").toString() : new StringBuilder().append("").append(MapleItemInformationProvider.getInstance().getName(4001055)).append(" ").toString()).append("").toString());
    }

    public long getLoginTime() {
        return this.loginTime;
    }

    public void setLoginTime(long login) {
        this.loginTime = login;
    }

    public short getOccupation() {
        return this.occupationId;
    }

    public void changeOccupation(short occu) {
        this.occupationId = occu;
    }

    public short getOccupationEXP() {
        return this.occupationEXP;
    }

    public void gainOccupationEXP(int amount) {
        if ((getOccupation() <= 0) || (getOccupation() % 10 >= 10) || (amount >= 30000) || (this.occupationEXP >= 30000)) {
            return;
        }
        dropMessage(-1, new StringBuilder().append("You have ").append(amount > 1 ? "lost " : "gained ").append("").append(amount).append(" occupation experience.").toString());
        short occLevel = (short) (this.occupationId % 10);
        if (this.occupationEXP + amount >= OccupationConstants.getOccExpForLevel(occLevel)) {
            this.occupationEXP = ((short) (this.occupationEXP + amount));
            levelOccupation();

            short needed = (short) OccupationConstants.getOccExpForLevel(occLevel);
            if (this.occupationEXP > needed) {
                this.occupationEXP = needed;
            }
        } else {
            this.occupationEXP = ((short) (this.occupationEXP + amount));
        }
    }

    public void levelOccupation() {
        if (this.occupationId % 10 < 10) {
            this.occupationId = ((short) (this.occupationId + 1));
        }
        if (this.occupationId % 10 == 0) {
            StringBuilder sb = new StringBuilder("[Ranch Owner] ");
            sb.append(getName());
            sb.append(" has leveled up to a Level 10 ").append(OccupationConstants.toString(this.occupationId)).append(". Congrats!!");
            World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, sb.toString()));
        }
    }

    public int getStr() {
        return this.str;
    }

    public void setStr(int str) {
        this.str = str;
        this.stats.recalcLocalStats(this);
    }

    public int getInt() {
        return this.int_;
    }

    public void setInt(int int_) {
        this.int_ = int_;
        this.stats.recalcLocalStats(this);
    }

    public int getLuk() {
        return this.luk;
    }

    public int getDex() {
        return this.dex;
    }

    public void setLuk(int luk) {
        this.luk = luk;
        this.stats.recalcLocalStats(this);
    }

    public void setDex(int dex) {
        this.dex = dex;
        this.stats.recalcLocalStats(this);
    }

    public void gainvpoints(int hi) {
        this.vpoints += hi;
    }

    public static String makeMapleReadable(String in) {
        String i = in.replace('I', 'i');
        i = i.replace('l', 'L');
        i = i.replace("rn", "Rn");
        i = i.replace("vv", "Vv");
        i = i.replace("VV", "Vv");
        return i;
    }

    public static boolean ban(String id, String reason, boolean accountId) {
        PreparedStatement ps = null;
        try {
            Connection con = DatabaseConnection.getConnection();
            if (id.matches("/[0-9]{1,3}\\..*")) {
                ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
                return true;
            }
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            } else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }

            boolean ret = false;
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement psb = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?")) {
                        psb.setString(1, reason);
                        psb.setInt(2, rs.getInt(1));
                        psb.executeUpdate();
                    }
                    ret = true;
                }
            }
            ps.close();
            return ret;
        } catch (SQLException ex) {
        } finally {
            try {
                if (ps != null && !ps.isClosed()) {
                    ps.close();
                }
            } catch (SQLException e) {
            }
        }
        return false;
    }

    public final boolean ban(String reason, boolean IPMac, boolean autoban) {
        if (lastmonthfameids == null) {
            throw new RuntimeException("Trying to ban a non-loaded character (testhack)");
        }
        client.getSession().write(CWvsContext.GMPoliceMessage(true));
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?");
            ps.setInt(1, autoban ? 2 : 1);
            ps.setString(2, reason);
            ps.setInt(3, accountid);
            ps.execute();
            ps.close();


            client.banMacs();
            ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
            ps.setString(1, client.getSessionIPAddress());
            ps.execute();
            ps.close();

        } catch (SQLException ex) {
            System.err.println("Error while banning" + ex);
            return false;
        }
        client.getSession().close(true);
        return true;
    }

    public void ban(String reason, boolean permBan) {
        if (this.lastmonthfameids == null) {
            throw new RuntimeException("Trying to ban a non-loaded character (testhack)");
        }
        try {
            getClient().banMacs();
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?");
            ps.setInt(1, 1);
            ps.setString(2, reason);
            ps.setInt(3, this.accountid);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("INSERT INTO ipbans VALUES (DEFAULT, ?)");
            String[] ipSplit = this.client.getSession().getRemoteAddress().toString().split(":");
            ps.setString(1, ipSplit[0]);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
        }
        this.client.getSession().close(true);
    }

    public static boolean unban(String name) {
        try {
            int accountid = -1;
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                accountid = rs.getInt("accountid");
            }
            ps.close();
            rs.close();
            if (accountid == -1) {
                return false;
            }
            ps = con.prepareStatement("UPDATE accounts SET banned = -1 WHERE id = ?");
            ps.setInt(1, accountid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            return false;
        }
        return true;
    }

    public void changeMap(int map, int portal) {
        MapleMap warpMap = this.client.getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void addHonourExp(int amount) {
        if (getHonourLevel() == 0) {
            setHonourLevel(1);
        }
        if (getHonourExp() + amount >= getHonourLevel() * 500) {
            honourLevelUp();
            int leftamount = getHonourExp() + amount - (getHonourLevel() - 1) * 500;
            leftamount = Math.min(leftamount, getHonourLevel() * 500 - 1);
            setHonourExp(leftamount);
            return;
        }
        setHonourExp(getHonourExp() + amount);
        this.client.getSession().write(CWvsContext.updateAzwanFame(getHonourLevel(), getHonourExp(), true));
        this.client.getSession().write(CWvsContext.professionInfo("honorLeveling", 0, getHonourLevel(), getHonourNextExp()));
    }

    public int getHonourNextExp() {
        if (getHonourLevel() == 0) {
            return 0;
        }
        return (getHonourLevel() + 1) * 500;
    }

    public void honourLevelUp() {
        setHonourLevel(getHonourLevel() + 1);
        this.client.getSession().write(CWvsContext.updateAzwanFame(getHonourLevel(), getHonourExp(), true));
        if (getHonourLevel() == 2) {
            InnerSkillValueHolder diella = InnerAbillity.getInstance().renewSkill(0, -1);
            this.innerSkills.add(diella);
            changeSkillLevel(SkillFactory.getSkill(diella.getSkillId()), diella.getSkillLevel(), diella.getSkillLevel());
            this.client.getSession().write(CField.getCharInfo(this));
        } else if (getHonourLevel() == 30) {
            InnerSkillValueHolder is = InnerAbillity.getInstance().renewSkill(Randomizer.rand(0, 2), -1);
            this.innerSkills.add(is);
            changeSkillLevel(SkillFactory.getSkill(is.getSkillId()), is.getSkillLevel(), is.getSkillLevel());
            this.client.getSession().write(CField.getCharInfo(this));
        } else if (getHonourLevel() == 70) {
            InnerSkillValueHolder beautiful = InnerAbillity.getInstance().renewSkill(Randomizer.rand(1, 3), -1);
            this.innerSkills.add(beautiful);
            changeSkillLevel(SkillFactory.getSkill(beautiful.getSkillId()), beautiful.getSkillLevel(), beautiful.getSkillLevel());
            this.client.getSession().write(CField.getCharInfo(this));
        }
    }

    public void unchooseStolenSkill(int skillID) {
        if ((skillisCooling(20031208)) || (this.stolenSkills == null)) {
            dropMessage(-6, "[Loadout] The skill is under cooldown. Please wait.");
            return;
        }
        int job = GameConstants.getJobNumber(skillID / 10000);
        boolean changed = false;
        for (Pair sk : this.stolenSkills) {
            if ((((Boolean) sk.right).booleanValue()) && (GameConstants.getJobNumber(((Integer) sk.left).intValue() / 10000) == job)) {
                cancelStolenSkill(((Integer) sk.left).intValue());
                sk.right = Boolean.valueOf(false);
                changed = true;
            }
        }
        if (changed) {
            Skill skil = SkillFactory.getSkill(skillID);
            changeSkillLevel_Skip(skil, getSkillLevel(skil), (byte) 0);
            this.client.getSession().write(CField.replaceStolenSkill(GameConstants.getStealSkill(job), 0));
        }
    }

    public void cancelStolenSkill(int skillID) {
        Skill skk = SkillFactory.getSkill(skillID);
        MapleStatEffect eff = skk.getEffect(getTotalSkillLevel(skk));
        Iterator i$;
        MapleMonster mons;
        if ((eff.isMonsterBuff()) || ((eff.getStatups().isEmpty()) && (!eff.getMonsterStati().isEmpty()))) {
            for (i$ = this.map.getAllMonstersThreadsafe().iterator(); i$.hasNext();) {
                mons = (MapleMonster) i$.next();
                for (MonsterStatus b : eff.getMonsterStati().keySet()) {
                    if ((mons.isBuffed(b)) && (mons.getBuff(b).getFromID() == this.id)) {
                        mons.cancelStatus(b);
                    }
                }
            }
        } else if ((eff.getDuration() > 0) && (!eff.getStatups().isEmpty())) {
            for (MapleCharacter chr : this.map.getCharactersThreadsafe()) {
                chr.cancelEffect(eff, false, -1L, eff.getStatups());
            }
        }
    }

    public void chooseStolenSkill(int skillID) {
        if ((skillisCooling(20031208)) || (this.stolenSkills == null)) {
            dropMessage(-6, "[Loadout] The skill is under cooldown. Please wait.");
            return;
        }
        Pair dummy = new Pair(Integer.valueOf(skillID), Boolean.valueOf(false));
        if (this.stolenSkills.contains(dummy)) {
            unchooseStolenSkill(skillID);
            ((Pair) this.stolenSkills.get(this.stolenSkills.indexOf(dummy))).right = Boolean.valueOf(true);

            this.client.getSession().write(CField.replaceStolenSkill(GameConstants.getStealSkill(GameConstants.getJobNumber(skillID / 10000)), skillID));
        }
    }

    public void addStolenSkill(int skillID, int skillLevel) {
        if ((skillisCooling(20031208)) || (this.stolenSkills == null)) {
            dropMessage(-6, "[Loadout] The skill is under cooldown. Please wait.");
            return;
        }
        Pair dummy = new Pair(Integer.valueOf(skillID), Boolean.valueOf(true));
        Skill skil = SkillFactory.getSkill(skillID);
        if ((!this.stolenSkills.contains(dummy)) && (GameConstants.canSteal(skil))) {
            dummy.right = Boolean.valueOf(false);
            skillLevel = Math.min(skil.getMaxLevel(), skillLevel);
            int job = GameConstants.getJobNumber(skillID / 10000);
            if ((!this.stolenSkills.contains(dummy)) && (getSkillLevel(GameConstants.getStealSkill(job)) > 0)) {
                int count = 0;
                skillLevel = Math.min(getSkillLevel(GameConstants.getStealSkill(job)), skillLevel);
                for (Pair sk : this.stolenSkills) {
                    if (GameConstants.getJobNumber(((Integer) sk.left).intValue() / 10000) == job) {
                        count++;
                    }
                }
                if (count < GameConstants.getNumSteal(job)) {
                    this.stolenSkills.add(dummy);
                    this.changed_skills = true;
                    changeSkillLevel_Skip(skil, skillLevel, (byte) skillLevel);
                    this.client.getSession().write(CField.addStolenSkill(job, count, skillID, skillLevel));
                }
            }
        }
    }

    public void removeStolenSkill(int skillID) {
        if ((skillisCooling(20031208)) || (this.stolenSkills == null)) {
            dropMessage(-6, "[Loadout] The skill is under cooldown. Please wait.");
            return;
        }
        int job = GameConstants.getJobNumber(skillID / 10000);
        Pair dummy = new Pair(Integer.valueOf(skillID), Boolean.valueOf(false));
        int count = -1;
        int cc = 0;
        for (int i = 0; i < this.stolenSkills.size(); i++) {
            if (((Integer) ((Pair) this.stolenSkills.get(i)).left).intValue() == skillID) {
                if (((Boolean) ((Pair) this.stolenSkills.get(i)).right).booleanValue()) {
                    unchooseStolenSkill(skillID);
                }
                count = cc;
                break;
            }
            if (GameConstants.getJobNumber(((Integer) ((Pair) this.stolenSkills.get(i)).left).intValue() / 10000) == job) {
                cc++;
            }
        }
        if (count >= 0) {
            cancelStolenSkill(skillID);
            this.stolenSkills.remove(dummy);
            dummy.right = Boolean.valueOf(true);
            this.stolenSkills.remove(dummy);
            this.changed_skills = true;
            changeSkillLevel_Skip(SkillFactory.getSkill(skillID), 0, (byte) 0);

            this.client.getSession().write(CField.replaceStolenSkill(GameConstants.getStealSkill(job), 0));
            for (int i = 0; i < GameConstants.getNumSteal(job); i++) {
                this.client.getSession().write(CField.removeStolenSkill(job, i));
            }
            count = 0;
            for (Pair sk : this.stolenSkills) {
                if (GameConstants.getJobNumber(((Integer) sk.left).intValue() / 10000) == job) {
                    this.client.getSession().write(CField.addStolenSkill(job, count, ((Integer) sk.left).intValue(), getSkillLevel(((Integer) sk.left).intValue())));
                    if (((Boolean) sk.right).booleanValue()) {
                        this.client.getSession().write(CField.replaceStolenSkill(GameConstants.getStealSkill(job), ((Integer) sk.left).intValue()));
                    }
                    count++;
                }
            }
            this.client.getSession().write(CField.removeStolenSkill(job, count));
        }
    }

    public List<Pair<Integer, Boolean>> getStolenSkills() {
        return this.stolenSkills;
    }

    public void changeSkillLevel_Skip(Skill skil, int skilLevel, byte masterLevel) {
        Map enry = new HashMap(1);
        enry.put(skil, new SkillEntry(skilLevel, masterLevel, -1L));
        changeSkillLevel_Skip(enry, true);
    }

    public String getKeyValue(String key) {
        if (this.CustomValues.containsKey(key)) {
            return (String) this.CustomValues.get(key);
        }
        return null;
    }

    public void changeSkillLevel(Skill skill, byte newLevel, byte newMasterlevel) {
        changeSkillLevel(skill, newLevel, newMasterlevel);
    }

    public void setKeyValue(String key, String values) {
        if (this.CustomValues.containsKey(key)) {
            this.CustomValues.remove(key);
        }
        this.CustomValues.put(key, values);
        this.keyvalue_changed = true;
    }

    public void maxSkill(int skillid) {
        Skill skill_ = SkillFactory.getSkill(skillid);
        byte maxlevel = (byte) skill_.getMaxLevel();
        changeSkillLevel(skill_, maxlevel, maxlevel);
    }

    public void maxAllSkills() {
        int skillid = 0;
        HashMap sa = new HashMap();

        MapleDataProvider dataProvider = MapleDataProviderFactory.getDataProvider(new File(new StringBuilder().append(System.getProperty("net.sf.odinms.wzpath")).append("/String.wz").toString()));
        MapleData skilldData = dataProvider.getData("Skill.img");
        for (MapleData skill_ : skilldData.getChildren()) {
            try {
                skillid = Integer.parseInt(skill_.getName());
                Skill skil = SkillFactory.getSkill(skillid);
                skillid = skil.getId();
                if ((skillid == 1003) || (skillid == 10001003) || (skillid == 20001003) || (skillid == 20021003) || (skillid == 20031003) || (skillid == 30001003) || (skillid == 50001003) || (skillid == 20041003)) {
                    maxSkill(skillid);
                } else if (((skil.getName().toUpperCase().contains("S WILL")) || (skillid < 1000000) || (skil.ishyper()) || (skillid == 65121052) || (skillid == 65121010) || (skillid == 42121007) || (skillid == 42101020) || (skillid == 42101021) || (skillid == 42101022) || (skillid == 42101023) || (skillid == 42121020) || (skillid == 42121021) || (skillid == 42121022) || (skillid == 42121023) || (skillid != 42120024))
                        && ((skillid < 8000000) || (skillid > 8100000))
                        && ((skillid < 9000000) || (skillid > 9100000))
                        && ((skillid < 10000000) || (skillid > 11000000))
                        && ((skillid < 20000000) || (skillid > 20050000))
                        && ((skillid < 30000000) || (skillid > 30020000))
                        && ((skillid < 40000000) || (skillid > 40030000))
                        && ((skillid < 50000000) || (skillid >= 51000000))
                        && ((skillid < 60000000) || (skillid > 61000000))
                        && ((skillid < 70000000) || (skillid > 72000000))
                        && (skillid < 80000000)) {
                    if ((getJob() >= 6100) && (getJob() <= 6112) && (skillid / 10000 >= 6100) && (skillid / 10000 <= 6112)) {
                        if ((skil.getName().contains("(Transfiguration)")) || (skillid == 61111114) || (skillid == 61121015) || (skillid == 61121116) || (skillid == 61120018) || (skillid == 61001004) || (skillid == 61001005) || (skillid == 61110009) || (skillid == 61111113) || (skillid != 61120008)) {
                            sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                        }
                    } else if ((getJob() >= 4100) && (getJob() <= 4112) && (skillid / 10000 >= 4100) && (skillid / 10000 <= 4112)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 4200) && (getJob() <= 4212) && (skillid / 10000 >= 4200) && (skillid / 10000 <= 4212)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 3600) && (getJob() <= 3612) && (skillid / 10000 >= 3600) && (skillid / 10000 <= 3612)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 6500) && (getJob() <= 6512) && (skillid / 10000 >= 6500) && (skillid / 10000 <= 6512)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 100) && (getJob() <= 132) && (skillid / 10000 == 100)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 110) && (getJob() <= 112) && (skillid / 10000 >= 110) && (skillid / 10000 <= 112)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 120) && (getJob() <= 122) && (skillid / 10000 >= 120) && (skillid / 10000 <= 122)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 130) && (getJob() <= 132) && (skillid / 10000 >= 130) && (skillid / 10000 <= 132)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 200) && (getJob() <= 232) && (skillid / 10000 == 200)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 210) && (getJob() <= 212) && (skillid / 10000 >= 210) && (skillid / 10000 <= 212)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 220) && (getJob() <= 222) && (skillid / 10000 >= 220) && (skillid / 10000 <= 222)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 230) && (getJob() <= 232) && (skillid / 10000 >= 230) && (skillid / 10000 <= 232)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 300) && (getJob() <= 322) && (skillid / 10000 == 300)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 310) && (getJob() <= 312) && (skillid / 10000 >= 310) && (skillid / 10000 <= 312)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 320) && (getJob() <= 322) && (skillid / 10000 >= 320) && (skillid / 10000 <= 322)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 400) && (getJob() <= 434) && (skillid / 10000 == 400)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 410) && (getJob() <= 412) && (skillid / 10000 >= 410) && (skillid / 10000 <= 412)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 420) && (getJob() <= 422) && (skillid / 10000 >= 420) && (skillid / 10000 <= 422)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 430) && (getJob() <= 434) && (skillid / 10000 >= 430) && (skillid / 10000 <= 434) && (skillid != 4340012) && (skillid != 4341008)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 500) && (getJob() <= 532) && (skillid / 10000 == 500)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 510) && (getJob() <= 512) && (skillid / 10000 >= 510) && (skillid / 10000 <= 512)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 520) && (getJob() <= 522) && (skillid / 10000 >= 520) && (skillid / 10000 <= 522)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 530) && (getJob() <= 532) && (skillid / 10000 >= 530) && (skillid / 10000 <= 532)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 570) && (getJob() <= 572) && (skillid / 10000 == 508)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 570) && (getJob() <= 572) && (skillid / 10000 >= 570) && (skillid / 10000 <= 572)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 580) && (getJob() <= 582) && (skillid / 10000 >= 580) && (skillid / 10000 <= 582)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 590) && (getJob() <= 592) && (skillid / 10000 >= 590) && (skillid / 10000 <= 592)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 1100) && (getJob() <= 1112) && (skillid / 10000 >= 1100) && (skillid / 10000 <= 1112)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 1200) && (getJob() <= 1212) && (skillid / 10000 >= 1200) && (skillid / 10000 <= 1212)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 1300) && (getJob() <= 1312) && (skillid / 10000 >= 1300) && (skillid / 10000 <= 1312)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 1400) && (getJob() <= 1412) && (skillid / 10000 >= 1400) && (skillid / 10000 <= 1412)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 1500) && (getJob() <= 1512) && (skillid / 10000 >= 1500) && (skillid / 10000 <= 1512)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 2100) && (getJob() <= 2112) && (skillid / 10000 >= 2100) && (skillid / 10000 <= 2112)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 2200) && (getJob() <= 2218) && (skillid / 10000 >= 2200) && (skillid / 10000 <= 2218)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 2400) && (getJob() <= 2412) && (skillid / 10000 >= 2400) && (skillid / 10000 <= 2412)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 2700) && (getJob() <= 2712) && (skillid / 10000 >= 2700) && (skillid / 10000 <= 2712)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 3100) && (getJob() <= 3112) && (skillid / 10000 >= 3100) && (skillid / 10000 <= 3112)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 3200) && (getJob() <= 3212) && (skillid / 10000 >= 3200) && (skillid / 10000 <= 3212)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 3300) && (getJob() <= 3312) && (skillid / 10000 >= 3300) && (skillid / 10000 <= 3312)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 3500) && (getJob() <= 3512) && (skillid / 10000 >= 3500) && (skillid / 10000 <= 3512)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 5100) && (getJob() <= 5112) && (skillid / 10000 >= 5100) && (skillid / 10000 <= 5112)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else if ((getJob() >= 2300) && (getJob() <= 2312) && (skillid / 10000 >= 2300) && (skillid / 10000 <= 2312)) {
                        sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
                    } else;
                }
            } finally {
            }
        }
        changeSkillsLevel(sa);
    }

    public void applyBlackBlessingBuff(int combos) {
        if ((combos == -1) && (this.runningbless == 0)) {
            combos = 0;
            return;
        }
        Skill skill = SkillFactory.getSkill(27100003);
        int lvl = getTotalSkillLevel(27100003);
        if (lvl > 0) {
            this.runningbless = ((byte) (this.runningbless + combos));
            if (this.runningbless > 3) {
                this.runningbless = 3;
            }
            if (this.runningbless == 0) {
                if (getBuffedValue(MapleBuffStat.Black_Blessing) != null) {
                    cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.Black_Blessing});
                }
            } else {
                skill.getEffect(lvl).applyBlackBlessingBuff(this, this.runningbless);
            }
        }
    }

    public int getBossLog(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            int count = 0;

            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bosslog WHERE characterid = ? AND bossid = ? AND lastattempt >= subtime(CURRENT_TIMESTAMP, '1 0:0:0.0')");
            ps.setInt(1, this.id);
            ps.setString(2, boss);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                count = -1;
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception Ex) {
        }
        return -1;
    }

    public int getBossLog(String boss, String mintime, String maxtime) {
        Connection con = DatabaseConnection.getConnection();
        try {
            int count = 0;

            PreparedStatement ps = con.prepareStatement(new StringBuilder().append("SELECT COUNT(*) FROM bosslog WHERE accountid = ? AND bossid = ? AND lastattempt >='").append(mintime).append("' and lastattempt <='").append(maxtime).append("'").toString());
            ps.setInt(1, getAccountID());
            ps.setString(2, boss);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                count = -1;
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception Ex) {
        }
        return -1;
    }

    public int getBossLog(String boss, int days) {
        Connection con = DatabaseConnection.getConnection();
        try {
            int count = 0;

            PreparedStatement ps = con.prepareStatement(new StringBuilder().append("SELECT COUNT(*) FROM bosslog WHERE characterid = ? AND bossid = ? AND lastattempt >= subtime(CURRENT_TIMESTAMP, '").append(days).append(" 0:0:0.0')").toString());
            ps.setInt(1, this.id);
            ps.setString(2, boss);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                count = -1;
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception Ex) {
        }
        return -1;
    }

    public void setBossLog(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("insert into bosslog (characterid, bossid,accountid) values (?,?,?)");
            ps.setInt(1, this.id);
            ps.setString(2, boss);
            ps.setInt(3, this.accountid);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
        }
    }

    public void setBossLogwithid(String boss, int ids) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("insert into bosslog (characterid, bossid,accountid) values (?,?,?)");
            ps.setInt(1, ids);
            ps.setString(2, boss);
            ps.setInt(3, this.accountid);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
        }
    }

    public int getfsbLog(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            int count = 0;
            getId();

            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bosslog WHERE characterid = ? AND bossid = ?");
            ps.setInt(1, this.id);
            ps.setString(2, boss);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                count = -1;
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception Ex) {
        }
        return -1;
    }

    public int getfsbLogs(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            int count = 0;

            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bosslog WHERE bossid = ?");
            ps.setString(1, boss);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                count = -1;
            }
            rs.close();
            ps.close();
            return count;
        } catch (Exception Ex) {
        }
        return -1;
    }

    public void delfsbLogs(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("delete from bosslog where bossid=?");
            ps.setString(1, boss);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
        }
    }

    public void delBossLog(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("delete from bosslog where characterid=? and bossid=?");
            ps.setInt(1, this.id);
            ps.setString(2, boss);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
        }
    }

    public void delBossLog1(String boss) {
        int count = 0;
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM bosslog WHERE characterid = ? AND bossid = ?");
            ps.setInt(1, this.id);
            ps.setString(2, boss);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("bosslogid");
            }
            if (count > 0) {
                ps = con.prepareStatement("delete from bosslog where bosslogid=?");
                ps.setInt(1, count);
                ps.executeUpdate();
                ps.close();
            }
            rs.close();
            ps.close();
        } catch (Exception Ex) {
        }
    }

    public void delBossLog2(String boss) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("delete from bosslog where accountid=? and bossid=?");
            ps.setInt(1, getAccountID());
            ps.setString(2, boss);
            ps.executeUpdate();
            ps.close();
        } catch (Exception Ex) {
        }
    }

    public int getdojo() {
        try {
            int rets = 0;
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT dojo FROM characters WHERE id = ?");
            ps.setInt(1, this.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rets = rs.getInt("dojo");
            }
            rs.close();
            ps.close();
            return rets;
        } catch (SQLException ex) {
        }
        return 0;
    }

    public void setdojo(int slot) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("update characters set dojo=dojo+? WHERE id = ?");
            ps.setInt(1, slot);
            ps.setInt(2, this.id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
        }
    }

    public int getdojopm() {
        int check = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT id,chrid FROM dojoup");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt("chrid") == this.id) {
                    check = rs.getInt("id");
                }
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
        }
        return check;
    }

    public int getmoney() {
        int money = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT money FROM accounts WHERE id = ?");
            ps.setInt(1, getAccountID());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                money = rs.getInt("money");
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
        }
        return money;
    }

    public void setmoney(int slot) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(new StringBuilder().append("UPDATE accounts SET money = money+").append(slot).append(" WHERE id = ?").toString());
            ps.setInt(1, getAccountID());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
        }
    }

    public void setmoneylog(int slot, String logs) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO fsbotherlog (`accountid`, `characterid`, `account`, `charactername`,`quantity`,`log`) VALUES (?, ?, ?,?,?,?)");
            ps.setInt(1, getAccountID());
            ps.setInt(2, this.id);
            ps.setString(3, getClient().getAccountName());
            ps.setString(4, this.name);
            ps.setInt(5, slot);
            ps.setString(6, logs);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
        }
    }

    public void setplaymoney(String chrname, int slot) {
        try {
            int accid = 0;
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            ps.setString(1, chrname);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                accid = rs.getInt("accountid");
            }
            rs.close();
            ps.close();
            if (accid == 0) {
                dropMessage(5, new StringBuilder().append(chrname).append(" is not in database!").toString());
            } else {
                ps = con.prepareStatement(new StringBuilder().append("UPDATE accounts SET money = money+").append(slot).append(" WHERE id = ?").toString());
                ps.setInt(1, accid);
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("INSERT INTO fsblog (`accountid`, `characterid`, `account`, `charactername`,`fsbtype`) VALUES (?, ?, ?,?,?)");
                ps.setInt(1, accid);
                ps.setInt(2, 0);
                ps.setString(3, "0");
                ps.setString(4, chrname);
                ps.setInt(5, slot);
                ps.executeUpdate();
                ps.close();
                dropMessage(5, new StringBuilder().append(chrname).append(" donata add:").append(slot).toString());
            }
        } catch (SQLException se) {
        }
    }

    public int[] getschool() {
        return this.school;
    }

    public final void maxLead() {
        for (Skill skil : SkillFactory.getAllSkills()) {
            if (GameConstants.isRidingSKill(skil.getId())) { //no db/additionals/resistance skills
                if (skil.getId() == 80000023) {
                    changeSingleSkillLevel(SkillFactory.getSkill(skil.getId()), (byte) 1, (byte) 1, -1);
                }

            }
        }
    }
    
        public void changeWarriorStance(final int skillid) {
        if (skillid == 11101022) {
            dispelBuff(11111022);
            /*List<MapleBuffStat> statups = new LinkedList();
             statups.add(MapleBuffStat.INDIE_BOOSTER);
             statups.add(MapleBuffStat.DAMAGE_PERCENT);
             statups.add(MapleBuffStat.WARRIOR_STANCE);
             //client.getSession().write(BuffPacket.cancelBuff(statups));*/
            client.getSession().write(JobPacket.DawnWarriorPacket.giveMoonfallStance(getSkillLevel(skillid)));
            SkillFactory.getSkill(skillid).getEffect(1).applyTo(this);
        } else if (skillid == 11111022) {
            dispelBuff(11101022);
            /*List<MapleBuffStat> statups = new LinkedList();
             statups.add(MapleBuffStat.MOON_STANCE1);
             statups.add(MapleBuffStat.MOON_STANCE2);
             statups.add(MapleBuffStat.WARRIOR_STANCE);
             //client.getSession().write(BuffPacket.cancelBuff(statups));*/
            client.getSession().write(JobPacket.DawnWarriorPacket.giveSunriseStance(getSkillLevel(skillid)));
            SkillFactory.getSkill(skillid).getEffect(1).applyTo(this);
            /*} else if (skillid == 11121005) {
             //equinox
             } else if (skillid == 11121011) {
             dispelBuff(11101022);
             client.getSession().write(DawnWarriorPacket.giveEquinox_Moon(getSkillLevel(skillid), Integer.MAX_VALUE));
             SkillFactory.getSkill(skillid).getEffect(1).applyTo(this);
             } else if (skillid == 11121012) {
             dispelBuff(11101022);
             client.getSession().write(DawnWarriorPacket.giveEquinox_Sun(getSkillLevel(skillid), Integer.MAX_VALUE));
             SkillFactory.getSkill(skillid).getEffect(1).applyTo(this);*/
        }
    }

    public final void maxRiding() {
        for (Skill skil : SkillFactory.getAllSkills()) {
            if (GameConstants.isRidingSKill(skil.getId())) { //no db/additionals/resistance skills
                if (skil.getId() == 80001228 || skil.getId() == 80001186 || skil.getId() == 80001237 || skil.getId() == 80001240 || skil.getId() == 80001241 || skil.getId() == 80001243 || skil.getId() == 80001244 || skil.getId() == 80001245 || skil.getId() == 80001246 || skil.getId() == 80001199 || skil.getId() == 80001223 || skil.getId() == 80001224) {
                } else {
                    changeSingleSkillLevel(SkillFactory.getSkill(skil.getId()), (byte) 1, (byte) 1, -1);
                }
            }
        }
    }

    public final void maxDA() {
        if (getJob() == 3122) {
            changeSingleSkillLevel(SkillFactory.getSkill(31011000), (byte) 20, (byte) 20, -1);
            changeSingleSkillLevel(SkillFactory.getSkill(31010002), (byte) 10, (byte) 10, -1);
            changeSingleSkillLevel(SkillFactory.getSkill(31010003), (byte) 15, (byte) 15, -1);
            changeSingleSkillLevel(SkillFactory.getSkill(31011001), (byte) 20, (byte) 20, -1);
        }
    }

    public final void gmskillz() {
        for (Skill skil : SkillFactory.getAllSkills()) {
            // if (GameConstants.isApplicableSkill(skil.getId()) && skil.canBeLearnedBy(getJob()) && GameConstants.canuseskill(skil.getId())) { //no db/additionals/resistance skills
            // if (GameConstants.isApplicableSkill(skil.getId()) && skil.canBeLearnedBy(getJob())) { 
            changeSingleSkillLevel(SkillFactory.getSkill(skil.getId()), (byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), -1);
            //  }
        }
    }

    public final void maxSkillsByJob() {
        for (Skill skil : SkillFactory.getAllSkills()) {
            if (GameConstants.isApplicableSkill(skil.getId()) && skil.canBeLearnedBy(getJob()) && GameConstants.canuseskill(skil.getId())) { //no db/additionals/resistance skills
                // if (GameConstants.isApplicableSkill(skil.getId()) && skil.canBeLearnedBy(getJob())) { 
                changeSingleSkillLevel(SkillFactory.getSkill(skil.getId()), (byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), -1);
            }
        }
    }

    public int getgetschool(int skillbook) {
        return this.school[skillbook];
    }

    public void setgetschool(int skillbook, int slot) {
        this.school[skillbook] = slot;
    }

    public short getxenoncombo() {
        return this.xenoncombo;
    }

    public void setxenoncombo(short slot) {
        this.xenoncombo = slot;
    }

    public final void maxAAllSkills() {
        for (Skill skil : SkillFactory.getAllSkills()) {
            //   if (GameConstants.isApplicableSkill(skil.getId()) && skil.getId() < 90000000) { //no db/additionals/resistance skills
            changeSingleSkillLevel(SkillFactory.getSkill(skil.getId()), (byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), -1);
        }
        //    }
    }

    public void unequipEverything() {
        MapleInventory equipped = this.getInventory(MapleInventoryType.EQUIPPED);
        List<Short> position = new ArrayList<>();
        for (Item item : equipped.list()) {
            position.add(item.getPosition());
        }
        for (short pos : position) {
            MapleInventoryManipulator.unequip(client, pos, getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
        }
    }

    public void changeMap2(int map, int portal) {
        MapleMap warpMap = getClient().getChannelServer().getMapFactory().getMap(map);
        changeMap(warpMap, warpMap.getPortal(portal));
    }

    public void setDGM(int ig) {
        this.dgm = ig;
    }

    public void clearInvGM() {
        java.util.Map<Pair<Short, Short>, MapleInventoryType> eqs = new HashMap<>();
        for (MapleInventoryType type : MapleInventoryType.values()) {
            for (Item item : getInventory(type)) {
                eqs.put(new Pair<>(item.getPosition(), item.getQuantity()), type);
            }
        }
        for (Map.Entry<Pair<Short, Short>, MapleInventoryType> eq : eqs.entrySet()) {
            MapleInventoryManipulator.removeFromSlot(this.getClient(), eq.getValue(), eq.getKey().left, eq.getKey().right, false, false);
        }
    }

    public boolean isMuted() {
        if (Calendar.getInstance().after(unmuteTime)) {
            muted = false;
        }
        return muted;
    }

    public void setMuted(boolean mute) {
        this.muted = mute;
    }

    public Calendar getUnmuteTime() {
        return this.unmuteTime;
    }

    public void setUnmuteTime(Calendar time) {
        unmuteTime = time;
    }

    public void send(Object ob) {
        getClient().getSession().write(ob);
    }

    public void ea() {
        send(CWvsContext.enableActions());
    }

    public void startAutoLooter() {
        this.autolooter = new AutoLoot(this);
        this.autolooter.start();
    }

    public void stopAutoLooter() {
        try {
            this.autolooter.interrupt();
        } catch (NullPointerException npuy) {
        }
    }

    public static enum FameStatus {

        OK, NOT_TODAY, NOT_THIS_MONTH;
    }
}