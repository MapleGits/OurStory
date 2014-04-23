package handling.world;

import clientside.BuddylistEntry;
import clientside.CardData;
import clientside.CharacterNameAndId;
import clientside.MapleCharacter;
import clientside.MapleTrait;
import clientside.MonsterFamiliar;
import clientside.Skill;
import clientside.SkillEntry;
import client.inventory.Item;
import client.inventory.MapleImp;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import server.quest.MapleQuest;
import tools.Pair;

public class CharacterTransfer
        implements Externalizable {

    public int characterid;
    public int accountid;
    public int fame;
    public int pvpExp;
    public int pvpPoints;
    public int hair;
    public int face;
    public int demonMarking;
    public int mapid;
    public int honourexp;
    public int honourlevel;
    public int guildid;
    public int partyid;
    public int messengerid;
    public int ACash;
    public int nxCredit;
    public int MaplePoints;
    public int mount_itemid;
    public int mount_exp;
    public int points;
    public int vpoints;
    public int marriageId;
    public int maxhp;
    public int dgm;
    public int maxmp;
    public int gml;
    public int hp;
    public int mp;
    public int familyid;
    public int seniorid;
    public int junior1;
    public int junior2;
    public int currentrep;
    public int totalrep;
    public int battleshipHP;
    public int gachexp;
    public int msipoints;
    public int guildContribution;
    public int totalWins;
    public int totalLosses;
    public boolean muted;
    public Calendar unmuteTime = null;
    public byte channel;
    public byte gender;
    public byte gmLevel;
    public byte guildrank;
    public byte alliancerank;
    public byte clonez;
    public byte fairyExp;
    public byte cardStack;
    public int buddysize;
    public byte world;
    public byte initialSpawnPoint;
    public byte skinColor;
    public byte mount_level;
    public byte mount_Fatigue;
    public byte subcategory;
    public long lastfametime;
    public long TranferTime;
    public AtomicLong exp = new AtomicLong();
    public long meso;
    public String name;
    public String accountname;
    public String BlessOfFairy;
    public String BlessOfEmpress;
    public String chalkboard;
    public String tempIP;
    public short level;
    public int hpApUsed;
    public short job;
    public short fatigue;
    public Object inventorys;
    public Object skillmacro;
    public Object storage;
    public Object cs;
    public Object anticheat;
    public Object innerSkills;
    public int[] savedlocation;
    public int[] wishlist;
    public int[] rocks;
    public int[] remainingSp;
    public int[] regrocks;
    public int[] hyperrocks;
    public int[] school;
    public byte[] petStore;
    public MapleImp[] imps;
    public Map<Integer, Integer> mbook;
    public List<Pair<Integer, Boolean>> stolenSkills;
    public Map<Integer, Pair<Byte, Integer>> keymap;
    public Map<Integer, MonsterFamiliar> familiars;
    public List<Integer> famedcharacters = null;
    public List<Integer> battledaccs = null;
    public List<Integer> extendedSlots = null;
    public List<Item> rebuy = null;
    public final Map<MapleTrait.MapleTraitType, Integer> traits = new EnumMap(MapleTrait.MapleTraitType.class);
    public final Map<CharacterNameAndId, Boolean> buddies = new LinkedHashMap();
    public final Map<Integer, Object> Quest = new LinkedHashMap();
    public Map<Integer, String> InfoQuest;
    public final Map<Integer, SkillEntry> Skills = new LinkedHashMap();
    public final Map<Integer, CardData> cardsInfo = new LinkedHashMap();
    public int reborns;
    public int apstorage;
    public int str;
    public int dex;
    public int int_;
    public int luk;
    public int remainingAp;
    public short occupationId;
    public short occupationEXP;
    public long damage;
    public int showdamage;

    public CharacterTransfer() {
        this.famedcharacters = new ArrayList();
        this.battledaccs = new ArrayList();
        this.extendedSlots = new ArrayList();
        this.rebuy = new ArrayList();
        this.InfoQuest = new LinkedHashMap();
        this.keymap = new LinkedHashMap();
        this.familiars = new LinkedHashMap();
        this.mbook = new LinkedHashMap();
    }

    public CharacterTransfer(MapleCharacter chr) {
        this.characterid = chr.getId();
        this.accountid = chr.getAccountID();
        this.accountname = chr.getClient().getAccountName();
        this.channel = ((byte) chr.getClient().getChannel());
        this.nxCredit = chr.getCSPoints(1);
        this.ACash = chr.getCSPoints(4);
        this.MaplePoints = chr.getCSPoints(2);
        this.dgm = chr.getDGM();
        this.gml = chr.getGML();
        this.msipoints = chr.getMSIPoints();
        this.muted = chr.isMuted();
        this.unmuteTime = chr.getUnmuteTime();
        this.stolenSkills = chr.getStolenSkills();
        this.vpoints = chr.getVPoints();
        this.name = chr.getName();
        this.fame = chr.getFame();
        this.gender = chr.getGender();
        this.level = chr.getLevel();
        this.str = chr.getStat().getStr();
        this.dex = chr.getStat().getDex();
        this.int_ = chr.getStat().getInt();
        this.luk = chr.getStat().getLuk();
        this.hp = chr.getStat().getHp();
        this.mp = chr.getStat().getMp();
        this.maxhp = chr.getStat().getMaxHp();
        this.maxmp = chr.getStat().getMaxMp();
        this.exp.set(chr.getExp());
        this.hpApUsed = chr.getHpApUsed();
        this.remainingAp = chr.getRemainingAp();
        this.remainingSp = chr.getRemainingSps();
        this.school = chr.getschool();
        this.meso = chr.getMeso();
        this.pvpExp = chr.getTotalBattleExp();
        this.pvpPoints = chr.getBattlePoints();

        this.reborns = chr.getReborns();
        this.apstorage = chr.getAPS();
        this.occupationId = chr.getOccupation();
        this.occupationEXP = chr.getOccupationEXP();

        this.skinColor = chr.getSkinColor();
        this.job = chr.getJob();
        this.hair = chr.getHair();
        this.face = chr.getFace();
        this.demonMarking = chr.getDemonMarking();
        this.mapid = chr.getMapId();
        this.initialSpawnPoint = chr.getInitialSpawnpoint();
        this.marriageId = chr.getMarriageId();
        this.world = chr.getWorld();
        this.guildid = chr.getGuildId();
        this.guildrank = chr.getGuildRank();
        this.guildContribution = chr.getGuildContribution();
        this.alliancerank = chr.getAllianceRank();
        this.gmLevel = ((byte) chr.getGMLevel());
        this.points = chr.getPoints();
        this.fairyExp = chr.getFairyExp();
        this.cardStack = chr.getCardStack();
        this.clonez = chr.getNumClones();
        this.petStore = chr.getPetStores();
        this.subcategory = chr.getSubcategory();
        this.imps = chr.getImps();
        this.fatigue = chr.getFatigue();
        this.currentrep = chr.getCurrentRep();
        this.totalrep = chr.getTotalRep();
        this.familyid = chr.getFamilyId();
        this.totalWins = chr.getTotalWins();
        this.totalLosses = chr.getTotalLosses();
        this.seniorid = chr.getSeniorId();
        this.junior1 = chr.getJunior1();
        this.junior2 = chr.getJunior2();
        this.battleshipHP = chr.currentBattleshipHP();
        this.gachexp = chr.getGachExp();
        this.familiars = chr.getFamiliars();
        this.tempIP = chr.getClient().getTempIP();
        this.rebuy = chr.getRebuy();
        boolean uneq = false;
        for (int i = 0; i < this.petStore.length; i++) {
            MaplePet pet = chr.getPet(i);
            if (this.petStore[i] == 0) {
                this.petStore[i] = -1;
            }
            if (pet != null) {
                uneq = true;
                this.petStore[i] = ((byte) Math.max(this.petStore[i], pet.getInventoryPosition()));
            }
        }

        if (uneq) {
            chr.unequipAllPets();
        }

        for (MapleTrait.MapleTraitType t : MapleTrait.MapleTraitType.values()) {
            this.traits.put(t, Integer.valueOf(chr.getTrait(t).getTotalExp()));
        }
        for (BuddylistEntry qs : chr.getBuddylist().getBuddies()) {
            this.buddies.put(new CharacterNameAndId(qs.getCharacterId(), qs.getName(), qs.getGroup()), Boolean.valueOf(qs.isVisible()));
        }
        this.buddysize = chr.getBuddyCapacity();

        this.partyid = (chr.getParty() == null ? -1 : chr.getParty().getId());

        if (chr.getMessenger() != null) {
            this.messengerid = chr.getMessenger().getId();
        } else {
            this.messengerid = 0;
        }


        this.InfoQuest = chr.getInfoQuest_Map();

        for (Map.Entry qs : chr.getQuest_Map().entrySet()) {
            this.Quest.put(Integer.valueOf(((MapleQuest) qs.getKey()).getId()), qs.getValue());
        }

        this.mbook = chr.getMonsterBook().getCards();
        this.inventorys = chr.getInventorys();

        for (final Entry<Skill, SkillEntry> qs : chr.getSkills().entrySet()) {
            this.Skills.put(Integer.valueOf(((Skill) qs.getKey()).getId()), qs.getValue());
        }
        for (final Entry<Integer, CardData> ii : chr.getCharacterCard().getCards().entrySet()) {
            this.cardsInfo.put(ii.getKey(), ii.getValue());
        }

        this.BlessOfFairy = chr.getBlessOfFairyOrigin();
        this.BlessOfEmpress = chr.getBlessOfEmpressOrigin();
        this.chalkboard = chr.getChalkboard();
        this.skillmacro = chr.getMacros();
        this.keymap = chr.getKeyLayout().Layout();
        this.savedlocation = chr.getSavedLocations();
        this.wishlist = chr.getWishlist();
        this.rocks = chr.getRocks();
        this.regrocks = chr.getRegRocks();
        this.hyperrocks = chr.getHyperRocks();
        this.famedcharacters = chr.getFamedCharacters();
        this.battledaccs = chr.getBattledCharacters();
        this.lastfametime = chr.getLastFameTime();
        this.storage = chr.getStorage();
        this.cs = chr.getCashInventory();
        this.extendedSlots = chr.getExtendedSlots();

        MapleMount mount = chr.getMount();
        this.mount_itemid = mount.getItemId();
        this.mount_Fatigue = mount.getFatigue();
        this.mount_level = mount.getLevel();
        this.mount_exp = mount.getExp();
        this.honourexp = chr.getHonourExp();
        this.honourlevel = chr.getHonourLevel();
        this.innerSkills = chr.getInnerSkills();
        this.damage = chr.getDamage();
        this.showdamage = chr.getshowdamage();
        this.TranferTime = System.currentTimeMillis();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.characterid = in.readInt();
        this.accountid = in.readInt();
        this.accountname = in.readUTF();
        this.channel = in.readByte();
        this.nxCredit = in.readInt();
        this.ACash = in.readInt();
        this.MaplePoints = in.readInt();
        this.name = in.readUTF();
        this.fame = in.readInt();
        this.gender = in.readByte();
        this.level = in.readShort();
        this.str = in.readShort();
        this.dex = in.readShort();
        this.int_ = in.readShort();
        this.luk = in.readShort();
        this.hp = in.readInt();
        this.mp = in.readInt();
        this.dgm = in.readInt();
        this.gml = in.readInt();
        this.msipoints = in.readInt();
        this.muted = in.readBoolean();
        this.unmuteTime = (Calendar) in.readObject();
        this.maxhp = in.readInt();
        this.maxmp = in.readInt();
        this.exp.set(in.readLong());
        this.hpApUsed = in.readInt();
        this.remainingAp = in.readInt();
        this.remainingSp = new int[in.readByte()];
        for (int i = 0; i < this.remainingSp.length; i++) {
            this.remainingSp[i] = in.readInt();
        }
        this.school = new int[in.readByte()];
        for (int i = 0; i < this.school.length; i++) {
            this.school[i] = in.readInt();
        }
        this.meso = in.readLong();
        this.skinColor = in.readByte();
        this.job = in.readShort();
        this.hair = in.readInt();
        this.face = in.readInt();
        this.demonMarking = in.readInt();
        this.mapid = in.readInt();
        this.initialSpawnPoint = in.readByte();
        this.world = in.readByte();
        this.guildid = in.readInt();
        this.guildrank = in.readByte();
        this.guildContribution = in.readInt();
        this.alliancerank = in.readByte();
        this.gmLevel = in.readByte();
        this.points = in.readInt();
        this.vpoints = in.readInt();
        if (in.readByte() == 1) {
            this.BlessOfFairy = in.readUTF();
        } else {
            this.BlessOfFairy = null;
        }
        if (in.readByte() == 1) {
            this.BlessOfEmpress = in.readUTF();
        } else {
            this.BlessOfEmpress = null;
        }
        if (in.readByte() == 1) {
            this.chalkboard = in.readUTF();
        } else {
            this.chalkboard = null;
        }
        this.clonez = in.readByte();
        this.skillmacro = in.readObject();
        this.lastfametime = in.readLong();
        this.storage = in.readObject();
        this.cs = in.readObject();
        this.mount_itemid = in.readInt();
        this.mount_Fatigue = in.readByte();
        this.mount_level = in.readByte();
        this.mount_exp = in.readInt();
        this.partyid = in.readInt();
        this.messengerid = in.readInt();
        this.inventorys = in.readObject();
        this.fairyExp = in.readByte();
        this.cardStack = in.readByte();
        this.subcategory = in.readByte();
        this.fatigue = in.readShort();
        this.marriageId = in.readInt();
        this.familyid = in.readInt();
        this.seniorid = in.readInt();
        this.junior1 = in.readInt();
        this.junior2 = in.readInt();
        this.currentrep = in.readInt();
        this.totalrep = in.readInt();
        this.battleshipHP = in.readInt();
        this.gachexp = in.readInt();
        this.totalWins = in.readInt();
        this.totalLosses = in.readInt();
        this.anticheat = in.readObject();
        this.tempIP = in.readUTF();
        this.honourexp = in.readInt();
        this.honourlevel = in.readInt();
        this.innerSkills = in.readObject();
        this.damage = in.readLong();
        this.showdamage = in.readInt();
        this.pvpExp = in.readInt();
        this.pvpPoints = in.readInt();

        this.reborns = in.readInt();
        this.apstorage = in.readInt();
        this.occupationId = in.readShort();
        this.occupationEXP = in.readShort();

        int mbooksize = in.readShort();
        for (int i = 0; i < mbooksize; i++) {
            this.mbook.put(Integer.valueOf(in.readInt()), Integer.valueOf(in.readInt()));
        }

        int skillsize = in.readShort();
        for (int i = 0; i < skillsize; i++) {
            this.Skills.put(Integer.valueOf(in.readInt()), new SkillEntry(in.readInt(), in.readByte(), in.readLong()));
        }

        int cardsize = in.readByte();
        for (int i = 0; i < cardsize; i++) {
            this.cardsInfo.put(Integer.valueOf(in.readInt()), new CardData(in.readInt(), in.readShort(), in.readShort()));
        }

        this.buddysize = in.readInt();
        short addedbuddysize = in.readShort();
        for (int i = 0; i < addedbuddysize; i++) {
            this.buddies.put(new CharacterNameAndId(in.readInt(), in.readUTF(), in.readUTF()), Boolean.valueOf(in.readBoolean()));
        }

        int questsize = in.readShort();
        for (int i = 0; i < questsize; i++) {
            this.Quest.put(Integer.valueOf(in.readInt()), in.readObject());
        }


        int famesize = in.readByte();
        for (int i = 0; i < famesize; i++) {
            this.famedcharacters.add(Integer.valueOf(in.readInt()));
        }

        int battlesize = in.readInt();
        for (int i = 0; i < battlesize; i++) {
            this.battledaccs.add(Integer.valueOf(in.readInt()));
        }

        int esize = in.readByte();
        for (int i = 0; i < esize; i++) {
            this.extendedSlots.add(Integer.valueOf(in.readInt()));
        }

        int savesize = in.readByte();
        this.savedlocation = new int[savesize];
        for (int i = 0; i < savesize; i++) {
            this.savedlocation[i] = in.readInt();
        }

        int wsize = in.readByte();
        this.wishlist = new int[wsize];
        for (int i = 0; i < wsize; i++) {
            this.wishlist[i] = in.readInt();
        }

        int rsize = in.readByte();
        this.rocks = new int[rsize];
        for (int i = 0; i < rsize; i++) {
            this.rocks[i] = in.readInt();
        }

        int resize = in.readByte();
        this.regrocks = new int[resize];
        for (int i = 0; i < resize; i++) {
            this.regrocks[i] = in.readInt();
        }

        int hesize = in.readByte();
        this.hyperrocks = new int[resize];
        for (int i = 0; i < hesize; i++) {
            this.hyperrocks[i] = in.readInt();
        }

        int infosize = in.readShort();
        for (int i = 0; i < infosize; i++) {
            this.InfoQuest.put(Integer.valueOf(in.readInt()), in.readUTF());
        }

        int keysize = in.readInt();
        for (int i = 0; i < keysize; i++) {
            this.keymap.put(Integer.valueOf(in.readInt()), new Pair(Byte.valueOf(in.readByte()), Integer.valueOf(in.readInt())));
        }

        int fsize = in.readShort();
        for (int i = 0; i < fsize; i++) {
            this.familiars.put(Integer.valueOf(in.readInt()), new MonsterFamiliar(this.characterid, in.readInt(), in.readInt(), in.readLong(), in.readUTF(), in.readInt(), in.readByte()));
        }

        this.petStore = new byte[in.readByte()];
        for (int i = 0; i < this.petStore.length; i++) {
            this.petStore[i] = in.readByte();
        }


        int rebsize = in.readShort();
        for (int i = 0; i < rebsize; i++) {
            this.rebuy.add((Item) in.readObject());
        }

        this.imps = new MapleImp[in.readByte()];
        for (int x = 0; x < this.imps.length; x++) {
            if (in.readByte() > 0) {
                MapleImp i = new MapleImp(in.readInt());
                i.setFullness(in.readShort());
                i.setCloseness(in.readShort());
                i.setState(in.readByte());
                i.setLevel(in.readByte());

                this.imps[x] = i;
            }
        }

        for (int i = 0; i < MapleTrait.MapleTraitType.values().length; i++) {
            this.traits.put(MapleTrait.MapleTraitType.values()[in.readByte()], Integer.valueOf(in.readInt()));
        }
        this.TranferTime = System.currentTimeMillis();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.characterid);
        out.writeInt(this.accountid);
        out.writeUTF(this.accountname);
        out.writeByte(this.channel);
        out.writeInt(this.nxCredit);
        out.writeInt(this.ACash);
        out.writeInt(this.MaplePoints);
        out.writeUTF(this.name);
        out.writeInt(this.fame);
        out.writeByte(this.gender);
        out.writeShort(this.level);
        out.writeShort(this.str);
        out.writeShort(this.dex);
        out.writeShort(this.int_);
        out.writeShort(this.luk);
        out.writeInt(this.hp);
        out.writeInt(this.mp);
        out.writeInt(this.maxhp);
        out.writeInt(this.maxmp);
        out.writeLong(this.exp.get());
        out.writeInt(this.dgm);
        out.writeInt(this.gml);
        out.writeInt(this.msipoints);
        out.writeBoolean(this.muted);
        out.writeObject(this.unmuteTime);
        out.writeInt(this.hpApUsed);
        out.writeInt(this.remainingAp);
        out.writeByte(this.remainingSp.length);
        for (int i = 0; i < this.remainingSp.length; i++) {
            out.writeInt(this.remainingSp[i]);
        }
        out.writeByte(this.school.length);
        for (int i = 0; i < this.school.length; i++) {
            out.writeInt(this.school[i]);
        }
        out.writeLong(this.meso);
        out.writeByte(this.skinColor);
        out.writeShort(this.job);
        out.writeInt(this.hair);
        out.writeInt(this.face);
        out.writeInt(this.demonMarking);
        out.writeInt(this.mapid);
        out.writeByte(this.initialSpawnPoint);
        out.writeByte(this.world);
        out.writeInt(this.guildid);
        out.writeByte(this.guildrank);
        out.writeInt(this.guildContribution);
        out.writeByte(this.alliancerank);
        out.writeByte(this.gmLevel);
        out.writeInt(this.points);
        out.writeInt(this.vpoints);
        out.writeByte(this.BlessOfFairy == null ? 0 : 1);
        if (this.BlessOfFairy != null) {
            out.writeUTF(this.BlessOfFairy);
        }
        out.writeByte(this.BlessOfEmpress == null ? 0 : 1);
        if (this.BlessOfEmpress != null) {
            out.writeUTF(this.BlessOfEmpress);
        }
        out.writeByte(this.chalkboard == null ? 0 : 1);
        if (this.chalkboard != null) {
            out.writeUTF(this.chalkboard);
        }
        out.writeByte(this.clonez);

        out.writeObject(this.skillmacro);
        out.writeLong(this.lastfametime);
        out.writeObject(this.storage);
        out.writeObject(this.cs);
        out.writeInt(this.mount_itemid);
        out.writeByte(this.mount_Fatigue);
        out.writeByte(this.mount_level);
        out.writeInt(this.mount_exp);
        out.writeInt(this.partyid);
        out.writeInt(this.messengerid);
        out.writeObject(this.inventorys);
        out.writeByte(this.fairyExp);
        out.writeByte(this.cardStack);
        out.writeByte(this.subcategory);
        out.writeShort(this.fatigue);
        out.writeInt(this.marriageId);
        out.writeInt(this.familyid);
        out.writeInt(this.seniorid);
        out.writeInt(this.junior1);
        out.writeInt(this.junior2);
        out.writeInt(this.currentrep);
        out.writeInt(this.totalrep);
        out.writeInt(this.battleshipHP);
        out.writeInt(this.gachexp);
        out.writeInt(this.totalWins);
        out.writeInt(this.totalLosses);
        out.writeObject(this.anticheat);
        out.writeUTF(this.tempIP);
        out.writeInt(this.pvpExp);
        out.writeInt(this.pvpPoints);
        out.writeInt(this.honourexp);
        out.writeInt(this.honourlevel);
        out.writeObject(this.innerSkills);
        out.writeLong(this.damage);
        out.writeInt(this.showdamage);
        out.writeInt(this.reborns);
        out.writeInt(this.apstorage);
        out.writeShort(this.occupationId);
        out.writeShort(this.occupationEXP);

        out.writeShort(this.mbook.size());
        for (Map.Entry ms : this.mbook.entrySet()) {
            out.writeInt(((Integer) ms.getKey()).intValue());
            out.writeInt(((Integer) ms.getValue()).intValue());
        }

        out.writeShort(this.Skills.size());
        for (Map.Entry qs : this.Skills.entrySet()) {
            out.writeInt(((Integer) qs.getKey()).intValue());
            out.writeInt(((SkillEntry) qs.getValue()).skillevel);
            out.writeByte(((SkillEntry) qs.getValue()).masterlevel);
            out.writeLong(((SkillEntry) qs.getValue()).expiration);
        }

        out.writeByte(this.cardsInfo.size());
        for (Map.Entry qs : this.cardsInfo.entrySet()) {
            out.writeInt(((Integer) qs.getKey()).intValue());
            out.writeInt(((CardData) qs.getValue()).cid);
            out.writeShort(((CardData) qs.getValue()).level);
            out.writeShort(((CardData) qs.getValue()).job);
        }

        out.writeInt(this.buddysize);
        out.writeShort(this.buddies.size());
        for (Map.Entry qs : this.buddies.entrySet()) {
            out.writeInt(((CharacterNameAndId) qs.getKey()).getId());
            out.writeUTF(((CharacterNameAndId) qs.getKey()).getName());
            out.writeUTF(((CharacterNameAndId) qs.getKey()).getGroup());
            out.writeBoolean(((Boolean) qs.getValue()).booleanValue());
        }

        out.writeShort(this.Quest.size());
        for (Map.Entry qs : this.Quest.entrySet()) {
            out.writeInt(((Integer) qs.getKey()).intValue());
            out.writeObject(qs.getValue());
        }

        out.writeByte(this.famedcharacters.size());
        for (Integer zz : this.famedcharacters) {
            out.writeInt(zz.intValue());
        }

        out.writeInt(this.battledaccs.size());
        for (Integer zz : this.battledaccs) {
            out.writeInt(zz.intValue());
        }

        out.writeByte(this.extendedSlots.size());
        for (Integer zz : this.extendedSlots) {
            out.writeInt(zz.intValue());
        }

        out.writeByte(this.savedlocation.length);
        for (int zz : this.savedlocation) {
            out.writeInt(zz);
        }

        out.writeByte(this.wishlist.length);
        for (int zz : this.wishlist) {
            out.writeInt(zz);
        }

        out.writeByte(this.rocks.length);
        for (int zz : this.rocks) {
            out.writeInt(zz);
        }

        out.writeByte(this.regrocks.length);
        for (int zz : this.regrocks) {
            out.writeInt(zz);
        }

        out.writeByte(this.hyperrocks.length);
        for (int zz : this.hyperrocks) {
            out.writeInt(zz);
        }

        out.writeShort(this.InfoQuest.size());
        for (Map.Entry qs : this.InfoQuest.entrySet()) {
            out.writeInt(((Integer) qs.getKey()).intValue());
            out.writeUTF((String) qs.getValue());
        }

        out.writeInt(this.keymap.size());
        for (Map.Entry qs : this.keymap.entrySet()) {
            out.writeInt(((Integer) qs.getKey()).intValue());
            out.writeByte(((Byte) ((Pair) qs.getValue()).left).byteValue());
            out.writeInt(((Integer) ((Pair) qs.getValue()).right).intValue());
        }

        out.writeShort(this.familiars.size());
        for (Map.Entry qs : this.familiars.entrySet()) {
            out.writeInt(((Integer) qs.getKey()).intValue());
            MonsterFamiliar f = (MonsterFamiliar) qs.getValue();
            out.writeInt(f.getId());
            out.writeInt(f.getFamiliar());
            out.writeLong(f.getExpiry());
            out.writeUTF(f.getName());
            out.writeInt(f.getFatigue());
            out.writeByte(f.getVitality());
        }

        out.writeByte(this.petStore.length);
        for (int i = 0; i < this.petStore.length; i++) {
            out.writeByte(this.petStore[i]);
        }


        out.writeShort(this.rebuy.size());
        for (int i = 0; i < this.rebuy.size(); i++) {
            out.writeObject(this.rebuy.get(i));
        }

        out.writeByte(this.imps.length);
        for (int i = 0; i < this.imps.length; i++) {
            if (this.imps[i] != null) {
                out.writeByte(1);
                out.writeInt(this.imps[i].getItemId());
                out.writeShort(this.imps[i].getFullness());
                out.writeShort(this.imps[i].getCloseness());
                out.writeByte(this.imps[i].getState());
                out.writeByte(this.imps[i].getLevel());
            } else {
                out.writeByte(0);
            }
        }

        for (Map.Entry ts : this.traits.entrySet()) {
            out.writeByte(((MapleTrait.MapleTraitType) ts.getKey()).ordinal());
            out.writeInt(((Integer) ts.getValue()).intValue());
        }
    }
}