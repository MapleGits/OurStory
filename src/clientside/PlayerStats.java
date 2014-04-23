package clientside;

import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MapleWeaponType;
import clientside.MapleTrait.MapleTraitType;
import constants.GameConstants;
import handling.world.World;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildSkill;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import static server.MapleStatInfo.evaR;
import server.StructItemOption;
import server.StructSetItem;
import server.StructSetItem.SetItem;
import server.life.Element;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.JobPacket;

public class PlayerStats
        implements Serializable {

    private static final long serialVersionUID = -679541993413738569L;
    private Map<Integer, Integer> setHandling = new HashMap();
    private Map<Integer, Integer> skillsIncrement = new HashMap();
    private Map<Integer, Integer> damageIncrease = new HashMap();
    private EnumMap<Element, Integer> elemBoosts = new EnumMap(Element.class);
    private List<Equip> durabilityHandling = new ArrayList();
    private List<Equip> equipLevelHandling = new ArrayList();
    private transient float shouldHealHP;
    private transient float shouldHealMP;
    private transient short passive_sharpeye_min_percent;
    private transient short passive_sharpeye_percent;
    private transient short passive_sharpeye_rate;
    private transient byte passive_mastery;
    private transient int localstr;
    private transient int localdex;
    private transient int localluk;
    private transient int localint_;
    private transient int localmaxhp;
    private transient int localmaxmp;
    private transient int magic;
    private transient int watk;
    private transient int hands;
    private transient int accuracy;
    private transient float localmaxbasedamage;
    private transient float localmaxbasepvpdamage;
    private transient float localmaxbasepvpdamageL;
    public transient boolean equippedWelcomeBackRing;
    public transient boolean hasClone;
    public transient boolean Berserk;
    public transient double expBuff;
    public transient double dropBuff;
    public transient double mesoBuff;
    public transient double cashBuff;
    public transient double mesoGuard;
    public transient double mesoGuardMeso;
    public transient double expMod;
    public transient double pickupRange;
    public transient double dam_r;
    public transient double bossdam_r;
    public transient int recoverHP;
    public transient int recoverMP;
    public transient int mpconReduce;
    public transient int mpconPercent;
    public transient int incMesoProp;
    public transient int reduceCooltime;
    public transient int coolTimeR;
    public transient int suddenDeathR;
    public transient int expLossReduceR;
    public transient int DAMreflect;
    public transient int DAMreflect_rate;
    public transient int ignoreDAMr;
    public transient int ignoreDAMr_rate;
    public transient int ignoreDAM;
    public transient int ignoreDAM_rate;
    public transient int hpRecover;
    public transient int hpRecoverProp;
    public transient int hpRecoverPercent;
    public transient int mpRecover;
    public transient int mpRecoverProp;
    public transient int RecoveryUP;
    public transient int BuffUP;
    public transient int RecoveryUP_Skill;
    public transient int BuffUP_Skill;
    public transient int incAllskill;
    public transient int combatOrders;
    public transient int ignoreTargetDEF;
    public transient int defRange;
    public transient int BuffUP_Summon;
    public transient int dodgeChance;
    public transient int speed;
    public transient int jump;
    public transient int harvestingTool;
    public transient int equipmentBonusExp;
    public transient int dropMod;
    public transient int cashMod;
    public transient int levelBonus;
    public transient int ASR;
    public transient int TER;
    public transient int evaR;
    public transient int pickRate;
    public transient int decreaseDebuff;
    public transient int equippedFairy;
    public transient int equippedSummon;
    public transient int pvpDamage;
    public transient int hpRecoverTime;
    public transient int mpRecoverTime;
    public transient int dot;
    public transient int dotTime;
    public transient int questBonus;
    public transient int pvpRank;
    public transient int pvpExp;
    public transient int wdef;
    public transient int mdef;
    public transient int trueMastery;
    public transient int damX;
    public transient int def;
    public transient int element_ice;
    public transient int element_fire;
    public transient int element_light;
    public transient int element_psn;
    public int hp;
    public int maxhp;
    public int mp;
    public int maxmp;
    public int str;
    public int dex;
    public int luk;
    public int int_;
    private transient int percent_hp;
    private transient int percent_mp;
    private transient int percent_str;
    private transient int percent_dex;
    private transient int percent_int;
    private transient int percent_luk;
    private transient int percent_acc;
    private transient int percent_atk;
    private transient int percent_matk;
    private transient int percent_wdef;
    private transient int percent_mdef;
    private transient int add_hp;
    private transient int add_mp;
    private transient int add_str;
    private transient int add_dex;
    private transient int add_int;
    private transient int add_luk;
    private transient int add_acc;
    private transient int add_atk;
    private transient int add_matk;
    private transient int add_wdef;
    private transient int add_mdef;
    private transient int ms_maxhp;
    private transient int ms_maxmp;
    private static final int[] allJobs = {0, 10000, 10000000, 20000000, 20010000, 20020000, 20030000, 30000000, 30010000};
    public static final int[] pvpSkills = {1000007, 2000007, 3000006, 4000010, 5000006, 5010004, 11000006, 12000006, 13000005, 14000006, 15000005, 21000005, 22000002, 23000004, 31000005, 32000012, 33000004, 35000005};
    public static final int[] hpskills = {110, 1000006, 11000005, 11000023, 11120036, 1120036, 1220036, 13120036, 1320036, 14110009, 15000008, 15111011, 15120036, 15120007, 20010194, 21120004, 21120036, 2120036, 22131001, 22170036, 2220036, 23120036, 2320036, 24120036, 27120036, 31000003, 31120036, 3120006, 3120036, 31220036, 32121010, 32120036, 3220005, 3220036, 33120036, 35111013, 35120014, 35120036, 36120036, 40020000, 4110008, 41120036, 4120036, 4210013, 42120036, 4220036, 4330008, 4340036, 5100009, 51000000, 5111007, 51120036, 5120012, 5120036, 5211007, 5220014, 5220036, 5311005, 5310007, 5320007, 5320036, 5711011, 5720005, 5800009, 5811007, 5911007, 60001216, 60000222, 61100007, 61100005, 61110007, 61110005, 61120010, 61120036, 65120036, 70000031, 71000013, 72000075, 80000000, 80000006};

 




    public void recalcLocalStats(MapleCharacter chra) {
        recalcLocalStats(false, chra);
    }

    private void resetLocalStats(int job) {
        this.accuracy = 0;
        this.wdef = 0;
        this.mdef = 0;
        this.damX = 0;
        this.localdex = getDex();
        this.localint_ = getInt();
        this.localstr = getStr();
        this.localluk = getLuk();
        this.hp = getHp();
        this.speed = 100;
        this.jump = 100;
        this.pickupRange = 0.0D;
        this.decreaseDebuff = 0;
        this.ASR = 0;
        this.TER = 0;
        this.dot = 0;
        this.questBonus = 1;
        this.dotTime = 0;
        this.trueMastery = 0;
        this.percent_wdef = 0;
        this.percent_mdef = 0;
        this.percent_hp = 0;
        this.percent_mp = 0;
        this.percent_str = 0;
        this.percent_dex = 0;
        this.percent_int = 0;
        this.percent_luk = 0;
        this.percent_acc = 0;
        this.percent_atk = 0;
        this.percent_matk = 0;
        this.add_wdef = 0;
        this.add_mdef = 0;
        this.add_hp = 0;
        this.add_mp = 0;
        this.add_str = 0;
        this.add_dex = 0;
        this.add_int = 0;
        this.add_luk = 0;
        this.add_acc = 0;
        this.add_atk = 0;
        this.add_matk = 0;
        this.passive_sharpeye_rate = 5;
        this.passive_sharpeye_min_percent = 20;
        this.passive_sharpeye_percent = 50;
        this.magic = 0;
        this.watk = 0;
        this.dodgeChance = 0;
        this.pvpDamage = 0;
        this.mesoGuard = 50.0D;
        this.mesoGuardMeso = 0.0D;
        this.dam_r = 100.0D;
        this.bossdam_r = 100.0D;
        this.expBuff = 100.0D;
        this.cashBuff = 100.0D;
        this.dropBuff = 100.0D;
        this.mesoBuff = 100.0D;
        this.recoverHP = 0;
        this.recoverMP = 0;
        this.mpconReduce = 0;
        this.mpconPercent = 100;
        this.incMesoProp = 0;
        this.reduceCooltime = 0;
        this.coolTimeR = 0;
        this.suddenDeathR = 0;
        this.expLossReduceR = 0;
        this.DAMreflect = 0;
        this.DAMreflect_rate = 0;
        this.ignoreDAMr = 0;
        this.ignoreDAMr_rate = 0;
        this.ignoreDAM = 0;
        this.ignoreDAM_rate = 0;
        this.ignoreTargetDEF = 0;
        this.hpRecover = 0;
        this.hpRecoverProp = 0;
        this.hpRecoverPercent = 0;
        this.mpRecover = 0;
        this.mpRecoverProp = 0;
        this.pickRate = 0;
        this.equippedWelcomeBackRing = false;
        this.equippedFairy = 0;
        this.equippedSummon = 0;
        this.hasClone = false;
        this.Berserk = false;
        this.equipmentBonusExp = 0;
        this.RecoveryUP = 0;
        this.BuffUP = 0;
        this.RecoveryUP_Skill = 0;
        this.BuffUP_Skill = 0;
        this.BuffUP_Summon = 0;
        this.dropMod = 1;
        this.expMod = 1.0D;
        this.cashMod = 1;
        this.levelBonus = 0;
        this.incAllskill = 0;
        this.combatOrders = 0;
        this.defRange = (isRangedJob(job) ? 200 : 0);
        this.durabilityHandling.clear();
        this.equipLevelHandling.clear();
        this.skillsIncrement.clear();
        this.damageIncrease.clear();
        this.setHandling.clear();
        this.harvestingTool = 0;
        this.element_fire = 100;
        this.element_ice = 100;
        this.element_light = 100;
        this.element_psn = 100;
        this.def = 100;
        accuracy = 0;
        wdef = 0;
        mdef = 0;
        damX = 0;
        localdex = getDex();
        localint_ = getInt();
        localstr = getStr();
        localluk = getLuk();
        speed = 100;
        jump = 100;
        pickupRange = 0.0;
        decreaseDebuff = 0;
        ASR = 0;
        TER = 0;
        dot = 0;
        questBonus = 1;
        dotTime = 0;
        trueMastery = 0;
        percent_wdef = 0;
        percent_mdef = 0;
        percent_hp = 0;
        percent_mp = 0;
        percent_str = 0;
        percent_dex = 0;
        percent_int = 0;
        percent_luk = 0;
        percent_acc = 0;
        percent_atk = 0;
        percent_matk = 0;
        add_wdef = 0;
        add_mdef = 0;
        add_hp = 0;
        add_mp = 0;
        add_str = 0;
        add_dex = 0;
        add_int = 0;
        add_luk = 0;
        add_acc = 0;
        add_atk = 0;
        add_matk = 0;
        passive_sharpeye_rate = 5;
        passive_sharpeye_min_percent = 20;
        passive_sharpeye_percent = 50;
        magic = 0;
        watk = 0;
        evaR = 0;
        pvpDamage = 0;
        mesoGuard = 50.0;
        mesoGuardMeso = 0.0;
        dam_r = 100.0;
        bossdam_r = 100.0;
        expBuff = 100.0;
        cashBuff = 100.0;
        dropBuff = 100.0;
        mesoBuff = 100.0;
        recoverHP = 0;
        recoverMP = 0;
        mpconReduce = 0;
        mpconPercent = 100;
        incMesoProp = 0;
        reduceCooltime = 0;
        coolTimeR = 0;
        suddenDeathR = 0;
        expLossReduceR = 0;
        DAMreflect = 0;
        DAMreflect_rate = 0;
        ignoreDAMr = 0;
        ignoreDAMr_rate = 0;
        ignoreDAM = 0;
        ignoreDAM_rate = 0;
        ignoreTargetDEF = 0;
        hpRecover = 0;
        hpRecoverProp = 0;
        hpRecoverPercent = 0;
        mpRecover = 0;
        mpRecoverProp = 0;
        pickRate = 0;
        equippedWelcomeBackRing = false;
        equippedFairy = 0;
        equippedSummon = 0;
        hasClone = false;
        Berserk = false;
        equipmentBonusExp = 0;
        RecoveryUP = 0;
        BuffUP = 0;
        RecoveryUP_Skill = 0;
        BuffUP_Skill = 0;
        BuffUP_Summon = 0;
        dropMod = 1;
        expMod = 1.0;
        cashMod = 1;
        levelBonus = 0;
        incAllskill = 0;
        combatOrders = 0;
        defRange = isRangedJob(job) ? 200 : 0;
        durabilityHandling.clear();
        equipLevelHandling.clear();
        skillsIncrement.clear();
        damageIncrease.clear();
        setHandling.clear();
        harvestingTool = 0;
        element_fire = 100;
        element_ice = 100;
        element_light = 100;
        element_psn = 100;
        def = 100;
        accuracy = 0;
        wdef = 0;
        mdef = 0;
        damX = 0;
        localdex = getDex();
        localint_ = getInt();
        localstr = getStr();
        localluk = getLuk();
        speed = 100;
        jump = 100;
        pickupRange = 0.0;
        decreaseDebuff = 0;
        ASR = 0;
        TER = 0;
        dot = 0;
        questBonus = 1;
        dotTime = 0;
        trueMastery = 0;
        percent_wdef = 0;
        percent_mdef = 0;
        percent_hp = 0;
        percent_mp = 0;
        percent_str = 0;
        percent_dex = 0;
        percent_int = 0;
        percent_luk = 0;
        percent_acc = 0;
        percent_atk = 0;
        percent_matk = 0;
        add_wdef = 0;
        add_mdef = 0;
        add_hp = 0;
        add_mp = 0;
        add_str = 0;
        add_dex = 0;
        add_int = 0;
        add_luk = 0;
        add_acc = 0;
        add_atk = 0;
        add_matk = 0;
        passive_sharpeye_rate = 5;
        passive_sharpeye_min_percent = 20;
        passive_sharpeye_percent = 50;
        magic = 0;
        watk = 0;
        evaR = 0;
        pvpDamage = 0;
        mesoGuard = 50.0;
        mesoGuardMeso = 0.0;
        dam_r = 100.0;
        bossdam_r = 100.0;
        expBuff = 100.0;
        cashBuff = 100.0;
        dropBuff = 100.0;
        mesoBuff = 100.0;
        recoverHP = 0;
        recoverMP = 0;
        mpconReduce = 0;
        mpconPercent = 100;
        incMesoProp = 0;
        reduceCooltime = 0;
        coolTimeR = 0;
        suddenDeathR = 0;
        expLossReduceR = 0;
        DAMreflect = 0;
        DAMreflect_rate = 0;
        ignoreDAMr = 0;
        ignoreDAMr_rate = 0;
        ignoreDAM = 0;
        ignoreDAM_rate = 0;
        ignoreTargetDEF = 0;
        hpRecover = 0;
        hpRecoverProp = 0;
        hpRecoverPercent = 0;
        mpRecover = 0;
        mpRecoverProp = 0;
        pickRate = 0;
        equippedWelcomeBackRing = false;
        equippedFairy = 0;
        equippedSummon = 0;
        hasClone = false;
        Berserk = false;
        equipmentBonusExp = 0;
        RecoveryUP = 0;
        BuffUP = 0;
        RecoveryUP_Skill = 0;
        BuffUP_Skill = 0;
        BuffUP_Summon = 0;
        dropMod = 1;
        expMod = 1.0;
        cashMod = 1;
        levelBonus = 0;
        incAllskill = 0;
        combatOrders = 0;
        defRange = isRangedJob(job) ? 200 : 0;
        durabilityHandling.clear();
        equipLevelHandling.clear();
        skillsIncrement.clear();
        damageIncrease.clear();
        setHandling.clear();
        harvestingTool = 0;
        element_fire = 100;
        element_ice = 100;
        element_light = 100;
        element_psn = 100;
        def = 100;
        wdef = 0;
        mdef = 0;
        damX = 0;
        localdex = getDex();
        localint_ = getInt();
        localstr = getStr();
        localluk = getLuk();
        speed = 100;
        jump = 100;
        pickupRange = 0.0;
        decreaseDebuff = 0;
        ASR = 0;
        TER = 0;
        dot = 0;
        questBonus = 1;
        dotTime = 0;
        trueMastery = 0;
        percent_wdef = 0;
        percent_mdef = 0;
        percent_hp = 0;
        percent_mp = 0;
        percent_str = 0;
        percent_dex = 0;
        percent_int = 0;
        percent_luk = 0;
        percent_acc = 0;
        percent_atk = 0;
        percent_matk = 0;
        add_wdef = 0;
        add_mdef = 0;
        add_hp = 0;
        add_mp = 0;
        add_str = 0;
        add_dex = 0;
        add_int = 0;
        add_luk = 0;
        add_acc = 0;
        add_atk = 0;
        add_matk = 0;
  //      crit_rate = 5;
        passive_sharpeye_min_percent = 20;
        passive_sharpeye_percent = 50;
        magic = 0;
        watk = 0;
        dodgeChance = 0;
        pvpDamage = 0;
        mesoGuard = 50.0;
        mesoGuardMeso = 0.0;
        dam_r = 100.0;
        bossdam_r = 100.0;
        expBuff = 100.0;
        cashBuff = 100.0;
        dropBuff = 100.0;
        mesoBuff = 100.0;
        recoverHP = 0;
        recoverMP = 0;
        mpconReduce = 0;
        mpconPercent = 100;
        incMesoProp = 0;
        reduceCooltime = 0;
        coolTimeR = 0;
        suddenDeathR = 0;
        expLossReduceR = 0;
        DAMreflect = 0;
        DAMreflect_rate = 0;
        ignoreDAMr = 0;
        ignoreDAMr_rate = 0;
        ignoreDAM = 0;
        ignoreDAM_rate = 0;
        ignoreTargetDEF = 0;
        hpRecover = 0;
        hpRecoverProp = 0;
        hpRecoverPercent = 0;
        mpRecover = 0;
        mpRecoverProp = 0;
        pickRate = 0;
        equippedWelcomeBackRing = false;
        equippedFairy = 0;
        equippedSummon = 0;
        hasClone = false;
        Berserk = false;
        equipmentBonusExp = 0;
        RecoveryUP = 0;
        BuffUP = 0;
        RecoveryUP_Skill = 0;
        BuffUP_Skill = 0;
        BuffUP_Summon = 0;
        dropMod = (int) 1.0;
        expMod = 1.0;
        cashMod = 1;
        levelBonus = 0;
        incAllskill = 0;
        combatOrders = 0;
        defRange = isRangedJob(job) ? 200 : 0;
        durabilityHandling.clear();
        equipLevelHandling.clear();
        skillsIncrement.clear();
        damageIncrease.clear();
        setHandling.clear();
        harvestingTool = 0;
        element_fire = 100;
        element_ice = 100;
        element_light = 100;
        element_psn = 100;
        def = 100;
    }
    
    public void recalcLocalStats(boolean first_login, MapleCharacter chra) {
        if (chra.isClone()) {
            return; //clones share PlayerStats objects and do not need to be recalculated
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int oldmaxhp = localmaxhp;
        int localmaxhp_ = getMaxHp();
        int localmaxmp_ = getMaxMp();
        resetLocalStats(chra.getJob());
        for (MapleTraitType t : MapleTraitType.values()) {
            chra.getTrait(t).clearLocalExp();
        }
        StructItemOption soc;
        final Map<Skill, SkillEntry> sData = new HashMap<>();
        final Iterator<Item> itera = chra.getInventory(MapleInventoryType.EQUIPPED).newList().iterator();
        while (itera.hasNext()) {
            final Equip equip = (Equip) itera.next();
            if (equip.getPosition() == -11) {
                if (GameConstants.isMagicWeapon(equip.getItemId())) {
                    final Map<String, Integer> eqstat = MapleItemInformationProvider.getInstance().getEquipStats(equip.getItemId());
                    if (eqstat != null) { //slow, poison, darkness, seal, freeze
                        if (eqstat.containsKey("incRMAF")) {
                            element_fire = eqstat.get("incRMAF");
                        }
                        if (eqstat.containsKey("incRMAI")) {
                            element_ice = eqstat.get("incRMAI");
                        }
                        if (eqstat.containsKey("incRMAL")) {
                            element_light = eqstat.get("incRMAL");
                        }
                        if (eqstat.containsKey("incRMAS")) {
                            element_psn = eqstat.get("incRMAS");
                        }
                        if (eqstat.containsKey("elemDefault")) {
                            def = eqstat.get("elemDefault");
                        }
                    }
                }
            }
            if ((equip.getItemId() / 10000 == 166 && equip.getAndroid() != null
                    || equip.getItemId() / 10000 == 167) && chra.getAndroid() == null) {
                final Equip android = (Equip) chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -27);
                final Equip heart = (Equip) chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -28);
                if (android != null && heart != null) {
                    chra.setAndroid(equip.getAndroid());
                }
            }
            //if (equip.getItemId() / 1000 == 1099) {
            //    equippedForce += equip.getMp();
            //}
            chra.getTrait(MapleTraitType.craft).addLocalExp(equip.getHands());
            accuracy += equip.getAcc();
            localmaxhp_ += equip.getHp();
            localmaxmp_ += equip.getMp();
            localdex += equip.getDex();
            localint_ += equip.getInt();
            localstr += equip.getStr();
            localluk += equip.getLuk();
            watk += equip.getWatk();
            magic += equip.getMatk();
            wdef += equip.getWdef();
            mdef += equip.getMdef();
            speed += equip.getSpeed();
            jump += equip.getJump();
            pvpDamage += equip.getPVPDamage();
            bossdam_r += equip.getBossDamage();
            ignoreTargetDEF += equip.getIgnorePDR();
            dam_r *= (Double.valueOf(equip.getTotalDamage()) + 100.0) / 100.0;
            percent_str += equip.getAllStat();
            percent_dex += equip.getAllStat();
            percent_int += equip.getAllStat();
            percent_luk += equip.getAllStat();
            switch (equip.getItemId()) {
                case 1112127:
                    equippedWelcomeBackRing = true;
                    break;
                case 1122017:
                    equippedFairy = 10;
                    break;
                case 1122158:
                    equippedFairy = 5;
                    break;
                case 1112594:
                    equippedSummon = 1085;
                    break;
                case 1112585:
                    equippedSummon = 1085;
                    break;
                case 1112586:
                    equippedSummon = 1087;
                    break;
                case 1112663:
                    equippedSummon = 1179;
                    break;
                default:
                    for (int eb_bonus : GameConstants.Equipments_Bonus) {
                        if (equip.getItemId() == eb_bonus) {
                            //equipmentBonusExp += GameConstants.Equipment_Bonus_EXP(eb_bonus);
                            break;
                        }
                    }
                    break;
            }
            final Integer set = ii.getSetItemID(equip.getItemId());
            if (set != null && set > 0) {
                int value = 1;
                if (setHandling.containsKey(set)) {
                    value += setHandling.get(set).intValue();
                }
                setHandling.put(set, value); //id of Set, number of items to go with the set
            }
            if (equip.getIncSkill() > 0 && ii.getEquipSkills(equip.getItemId()) != null) {
                for (final int zzz : ii.getEquipSkills(equip.getItemId())) {
                    final Skill skil = SkillFactory.getSkill(zzz);
                    if (skil != null && skil.canBeLearnedBy(chra.getJob())) { //dont go over masterlevel :D
                        int value = 1;
                        if (skillsIncrement.get(skil.getId()) != null) {
                            value += skillsIncrement.get(skil.getId());
                        }
                        skillsIncrement.put(skil.getId(), value);
                    }
                }
            }
            final Pair<Integer, Integer> ix = handleEquipAdditions(ii, chra, first_login, sData, equip.getItemId());
            if (ix != null) {
                localmaxhp_ += ix.getLeft();
                localmaxmp_ += ix.getRight();
            }
            if (equip.getState() >= 17) {
                final int[] potentials = {equip.getPotential1(), equip.getPotential2(), equip.getPotential3()};
                for (final int i : potentials) {
                    if (i > 0) {
                        soc = ii.getPotentialInfo(i).get(ii.getReqLevel(equip.getItemId()) / 10);
                        if (soc != null) {
                            localmaxhp_ += soc.get("incMHP");
                            localmaxmp_ += soc.get("incMMP");
                            handleItemOption(soc, chra, first_login, sData);
                        }
                    }
                }
            }
            if (equip.getSocketState() > 15) {
                final int[] sockets = {equip.getSocket1(), equip.getSocket2(), equip.getSocket3()};
                for (final int i : sockets) {
                    if (i > 0) {
                        soc = ii.getSocketInfo(i);
                        if (soc != null) {
                            localmaxhp_ += soc.get("incMHP");
                            localmaxmp_ += soc.get("incMMP");
                            handleItemOption(soc, chra, first_login, sData);
                        }
                    }
                }
            }
            if (equip.getDurability() > 0) {
                durabilityHandling.add((Equip) equip);
            }
            if (GameConstants.getMaxLevel(equip.getItemId()) > 0 && (GameConstants.getStatFromWeapon(equip.getItemId()) == null ? (equip.getEquipLevel() <= GameConstants.getMaxLevel(equip.getItemId())) : (equip.getEquipLevel() < GameConstants.getMaxLevel(equip.getItemId())))) {
                equipLevelHandling.add((Equip) equip);
            }
        }
        final Iterator<Entry<Integer, Integer>> iter = setHandling.entrySet().iterator();
        while (iter.hasNext()) {
            final Entry<Integer, Integer> entry = iter.next();
            final StructSetItem set = ii.getSetItem(entry.getKey());
            if (set != null) {
                final Map<Integer, SetItem> itemz = set.getItems();
                for (Entry<Integer, SetItem> ent : itemz.entrySet()) {
                    if (ent.getKey() <= entry.getValue()) {
                        SetItem se = ent.getValue();
                        localstr += se.incSTR + se.incAllStat;
                        localdex += se.incDEX + se.incAllStat;
                        localint_ += se.incINT + se.incAllStat;
                        localluk += se.incLUK + se.incAllStat;
                        watk += se.incPAD;
                        magic += se.incMAD;
                        speed += se.incSpeed;
                        accuracy += se.incACC;
                        localmaxhp_ += se.incMHP;
                        localmaxmp_ += se.incMMP;
                        percent_hp += se.incMHPr;
                        percent_mp += se.incMMPr;
                        wdef += se.incPDD;
                        mdef += se.incMDD;
                        if (se.option1 > 0 && se.option1Level > 0) {
                            soc = ii.getPotentialInfo(se.option1).get(se.option1Level);
                            if (soc != null) {
                                localmaxhp_ += soc.get("incMHP");
                                localmaxmp_ += soc.get("incMMP");
                                handleItemOption(soc, chra, first_login, sData);
                            }
                        }
                        if (se.option2 > 0 && se.option2Level > 0) {
                            soc = ii.getPotentialInfo(se.option2).get(se.option2Level);
                            if (soc != null) {
                                localmaxhp_ += soc.get("incMHP");
                                localmaxmp_ += soc.get("incMMP");
                                handleItemOption(soc, chra, first_login, sData);
                            }
                        }
                    }
                }
            }
        }
        handleProfessionTool(chra);
        for (Item item : chra.getInventory(MapleInventoryType.CASH).newList()) {
            if (item.getItemId() / 100000 == 52) {
                if (expMod < 3 && (item.getItemId() == 5211060 || item.getItemId() == 5211050 || item.getItemId() == 5211051 || item.getItemId() == 5211052 || item.getItemId() == 5211053 || item.getItemId() == 5211054)) {
                    expMod = 3.0; //overwrite
                } else if (expMod < 2 && (item.getItemId() == 5210000 || item.getItemId() == 5210001 || item.getItemId() == 5210002 || item.getItemId() == 5210003 || item.getItemId() == 5210004 || item.getItemId() == 5210005 || item.getItemId() == 5211061 || item.getItemId() == 5211000 || item.getItemId() == 5211001 || item.getItemId() == 5211002 || item.getItemId() == 5211003 || item.getItemId() == 5211046 || item.getItemId() == 5211047 || item.getItemId() == 5211048 || item.getItemId() == 5211049)) {
                    expMod = 2.0;
                } else if (expMod < 1.5 && (item.getItemId() == 5211068)) {
                    expMod = 1.5;
                }
            } else if (dropMod == 1.0 && item.getItemId() / 10000 == 536) {
                if (item.getItemId() >= 5360000 && item.getItemId() < 5360100) {
                    dropMod = (int) 2.0;
                }
            } else if (item.getItemId() == 5710000) {
                questBonus = 2;
            } else if (item.getItemId() == 5590000) {
                levelBonus += 5;
            }
        }
        for (Item item : chra.getInventory(MapleInventoryType.ETC).list()) {
            switch (item.getItemId()) {
                case 4030003:
                    pickupRange = Double.POSITIVE_INFINITY;
                    break;
                case 4030004:
                    hasClone = true;
                    break;
                case 4031864:
                    cashMod = 2;
                    break;
            }
        }
        if (first_login && chra.getLevel() >= 30) {
            if (chra.isGM()) { //!job lol
                for (int i = 0; i < allJobs.length; i++) {
                    sData.put(SkillFactory.getSkill(1085 + allJobs[i]), new SkillEntry((byte) 1, (byte) 0, -1));
                    sData.put(SkillFactory.getSkill(1087 + allJobs[i]), new SkillEntry((byte) 1, (byte) 0, -1));
                }
            } else {
                sData.put(SkillFactory.getSkill(getSkillByJob(1085, chra.getJob())), new SkillEntry((byte) 1, (byte) 0, -1));
                sData.put(SkillFactory.getSkill(getSkillByJob(1087, chra.getJob())), new SkillEntry((byte) 1, (byte) 0, -1));
            }
        }
        // add to localmaxhp_ if percentage plays a role in it, else add_hp
        handleBuffStats(chra);
        Integer buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_MAXHP);
        if (buff != null) {
            localmaxhp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_MAXMP);
        if (buff != null) {
            localmaxmp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.HP_BOOST);
        if (buff != null) {
            localmaxhp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MP_BOOST);
        if (buff != null) {
            localmaxmp_ += buff.intValue();
        }
        handlePassiveSkills(chra);
     //   handleHyperPassiveSkills(chra);
        if (chra.getGuildId() > 0) {
            final MapleGuild g = World.Guild.getGuild(chra.getGuildId());
            if (g != null && g.getSkills().size() > 0) {
                final long now = System.currentTimeMillis();
                for (MapleGuildSkill gs : g.getSkills()) {
                    if (gs.timestamp > now && gs.activator.length() > 0) {
                        final MapleStatEffect e = SkillFactory.getSkill(gs.skillID).getEffect(gs.level);
                        passive_sharpeye_rate += e.getCr();
                        watk += e.getAttackX();
                        magic += e.getMagicX();
                        expBuff *= (e.getEXPRate() + 100.0) / 100.0;
                        dodgeChance += e.getER();
                        percent_wdef += e.getWDEFRate();
                        percent_mdef += e.getMDEFRate();
                    }
                }
            }
        }
        for (Pair<Integer, Integer> ix : chra.getCharacterCard().getCardEffects()) {
            final MapleStatEffect e = SkillFactory.getSkill(ix.getLeft()).getEffect(ix.getRight());
            percent_wdef += e.getWDEFRate();
            watk += (e.getLevelToWatk() * chra.getLevel());
            percent_hp += e.getPercentHP();
            percent_mp += e.getPercentMP();
            magic += (e.getLevelToMatk() * chra.getLevel());
            RecoveryUP += e.getMPConsumeEff();
            percent_acc += e.getPercentAcc();
            passive_sharpeye_rate += e.getCr();
            jump += e.getPassiveJump();
            speed += e.getPassiveSpeed();
            dodgeChance += e.getPercentAvoid();
            damX += (e.getLevelToDamage() * chra.getLevel());
            BuffUP_Summon += e.getSummonTimeInc();
            expLossReduceR += e.getEXPLossRate();
            ASR += e.getASRRate();
            //ignoreMobDamR
            suddenDeathR += e.getSuddenDeathR();
            BuffUP_Skill += e.getBuffTimeRate();
            //onHitHpRecoveryR
            //onHitMpRecoveryR
            coolTimeR += e.getCooltimeReduceR();
            incMesoProp += e.getMesoAcquisition();
            damX += Math.floor((e.getHpToDamage() * oldmaxhp) / 100.0f);
            damX += Math.floor((e.getMpToDamage() * oldmaxhp) / 100.0f);
            //finalAttackDamR
            passive_sharpeye_percent += e.getCriticalMax();
            ignoreTargetDEF += e.getIgnoreMob();
            localstr += e.getStrX();
            localdex += e.getDexX();
            localint_ += e.getIntX();
            localluk += e.getLukX();
            localmaxhp_ += e.getMaxHpX();
            localmaxmp_ += e.getMaxMpX();
            watk += e.getAttackX();
            magic += e.getMagicX();
            bossdam_r += e.getBossDamage();
        }

        localstr += Math.floor((localstr * percent_str) / 100.0f);
        localdex += Math.floor((localdex * percent_dex) / 100.0f);
        localint_ += Math.floor((localint_ * percent_int) / 100.0f);
        localluk += Math.floor((localluk * percent_luk) / 100.0f);
        if (localint_ > localdex) {
            accuracy += localint_ + Math.floor(localluk * 1.2);
        } else {
            accuracy += localluk + Math.floor(localdex * 1.2);
        }
        watk += Math.floor((watk * percent_atk) / 100.0f);
        magic += Math.floor((magic * percent_matk) / 100.0f);
        localint_ += Math.floor((localint_ * percent_matk) / 100.0f);

        wdef += Math.floor((localstr * 1.2) + ((localdex + localluk) * 0.5) + (localint_ * 0.4));
        mdef += Math.floor((localstr * 0.4) + ((localdex + localluk) * 0.5) + (localint_ * 1.2));
        wdef += Math.min(30000, Math.floor((wdef * percent_wdef) / 100.0f));
        mdef += Math.min(30000, Math.floor((wdef * percent_mdef) / 100.0f));

        hands = localdex + localint_ + localluk;
        calculateFame(chra);
        ignoreTargetDEF += chra.getTrait(MapleTraitType.charisma).getLevel() / 10;
        pvpDamage += chra.getTrait(MapleTraitType.charisma).getLevel() / 10;
        ASR += chra.getTrait(MapleTraitType.will).getLevel() / 5;

        accuracy += Math.floor((accuracy * percent_acc) / 100.0f);
        accuracy += chra.getTrait(MapleTraitType.insight).getLevel() * 15 / 10;

        localmaxhp_ += Math.floor((percent_hp * localmaxhp_) / 100.0f);
        localmaxhp_ += chra.getTrait(MapleTraitType.will).getLevel() * 20;
        localmaxhp = Math.min(500000, Math.abs(Math.max(-500000, localmaxhp_)));

        localmaxmp_ += Math.floor((percent_mp * localmaxmp_) / 100.0f);
        localmaxmp_ += chra.getTrait(MapleTraitType.sense).getLevel() * 20;
        localmaxmp = Math.min(500000, Math.abs(Math.max(-500000, localmaxmp_)));

        if (chra.getEventInstance() != null && chra.getEventInstance().getName().startsWith("PVP")) { //hack
            MapleStatEffect eff;
            localmaxhp = Math.min(40000, localmaxhp * 3); //approximate.
            localmaxmp = Math.min(20000, localmaxmp * 2);
            //not sure on 20000 cap
            for (int i : pvpSkills) {
                Skill skil = SkillFactory.getSkill(i);
                if (skil != null && skil.canBeLearnedBy(chra.getJob())) {
                    sData.put(skil, new SkillEntry((byte) 1, (byte) 0, -1));
                    eff = skil.getEffect(1);
                    switch ((i / 1000000) % 10) {
                        case 1:
                            if (eff.getX() > 0) {
                                pvpDamage += (wdef / eff.getX());
                            }
                            break;
                        case 3:
                            hpRecoverProp += eff.getProb();
                            hpRecover += eff.getX();
                            mpRecoverProp += eff.getProb();
                            mpRecover += eff.getX();
                            break;
                        case 5:
                            passive_sharpeye_rate += eff.getProb();
                            passive_sharpeye_percent = 100;
                            break;
                    }
                    break;
                }
            }
            eff = chra.getStatForBuff(MapleBuffStat.MORPH);
            if (eff != null && eff.getSourceId() % 10000 == 1105) { //ice knight
                localmaxhp = 500000;
                localmaxmp = 500000;
            }
        }
        chra.changeSkillLevel_Skip(sData, false);
        if (GameConstants.isDemon(chra.getJob())) {
            localmaxmp = GameConstants.getMPByJob(chra.getJob());
        if (GameConstants.demonAvenger(chra.getJob())) {
            chra.getClient().getSession().write(JobPacket.AvengerPacket.giveAvengerHpBuff(hp));
        }
        CalcPassive_SharpEye(chra);
        CalcPassive_Mastery(chra);
        recalcPVPRank(chra);
        if (first_login) {
            chra.silentEnforceMaxHpMp();
            relocHeal(chra);
        } else {
            chra.enforceMaxHpMp();
        }
        calculateMaxBaseDamage(Math.max(magic, watk), pvpDamage, chra);
        trueMastery = Math.min(100, trueMastery);
        passive_sharpeye_min_percent = (short) Math.min(passive_sharpeye_min_percent, passive_sharpeye_percent);
        if (oldmaxhp != 0 && oldmaxhp != localmaxhp) {
            chra.updatePartyMemberHP();
        }
    }
    }

    
 /*   public void recalcLocalStats(boolean first_login, MapleCharacter chra) {
        if (chra.isClone()) {
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int oldmaxhp = this.localmaxhp;
        int localmaxhp_ = getMaxHp();
        int localmaxmp_ = getMaxMp();
        resetLocalStats(chra.getJob());
        for (MapleTrait.MapleTraitType t : MapleTrait.MapleTraitType.values()) {
            chra.getTrait(t).clearLocalExp();
        }

        Map sData = new HashMap();
        Iterator itera = chra.getInventory(MapleInventoryType.EQUIPPED).newList().iterator();
        while (itera.hasNext()) {
            Equip equip = (Equip) itera.next();
            if ((equip.getPosition() == -11)
                    && (GameConstants.isMagicWeapon(equip.getItemId()))) {
                Map eqstat = MapleItemInformationProvider.getInstance().getEquipStats(equip.getItemId());
                if (eqstat != null) {
                    if (eqstat.containsKey("incRMAF")) {
                        this.element_fire = ((Integer) eqstat.get("incRMAF")).intValue();
                    }
                    if (eqstat.containsKey("incRMAI")) {
                        this.element_ice = ((Integer) eqstat.get("incRMAI")).intValue();
                    }
                    if (eqstat.containsKey("incRMAL")) {
                        this.element_light = ((Integer) eqstat.get("incRMAL")).intValue();
                    }
                    if (eqstat.containsKey("incRMAS")) {
                        this.element_psn = ((Integer) eqstat.get("incRMAS")).intValue();
                    }
                    if (eqstat.containsKey("elemDefault")) {
                        this.def = ((Integer) eqstat.get("elemDefault")).intValue();
                    }
                }
            }

            if ((equip.getItemId() / 10000 == 166) && (equip.getAndroid() != null) && (chra.getAndroid() == null)) {
                chra.setAndroid(equip.getAndroid());
            }

            chra.getTrait(MapleTrait.MapleTraitType.craft).addLocalExp(equip.getHands());
            this.accuracy += equip.getAcc();
            localmaxhp_ += equip.getHp();
            localmaxmp_ += equip.getMp();
            this.localdex += equip.getDex();
            this.localint_ += equip.getInt();
            this.localstr += equip.getStr();
            this.localluk += equip.getLuk();
            this.watk += equip.getWatk();
            this.magic += equip.getMatk();
            this.wdef += equip.getWdef();
            this.mdef += equip.getMdef();
            this.speed += equip.getSpeed();
            this.jump += equip.getJump();
            this.pvpDamage += equip.getPVPDamage();
            switch (equip.getItemId()) {
                case 1112127:
                    this.equippedWelcomeBackRing = true;
                    break;
                case 1122017:
                    this.equippedFairy = 10;
                    break;
                case 1122158:
                    this.equippedFairy = 5;
                    break;
                case 1112594:
                    equippedSummon = 1085;
                    break;
                case 1112585:
                    equippedSummon = 1085;
                    break;
//                case 1113020:
//                    equippedSummon = 1162;
//                    break;
                case 1112586:
                    equippedSummon = 1087;
                    break;
                case 1112735:
                case 1112663:
                    equippedSummon = 1179;
                    break;
                default:
                    for (int eb_bonus : GameConstants.Equipments_Bonus) {
                        if (equip.getItemId() == eb_bonus) {
                            break;
                        }
                    }
            }
            if (equippedSummon > 0) {
                equippedSummon = getSkillByJob(equippedSummon, chra.getJob());

            }
            Integer set = ii.getSetItemID(equip.getItemId());
            if ((set != null) && (set.intValue() > 0)) {
                int value = 1;
                if (this.setHandling.containsKey(set)) {
                    value += ((Integer) this.setHandling.get(set)).intValue();
                }
                this.setHandling.put(set, Integer.valueOf(value));
            }
            Iterator i$;
            if ((equip.getIncSkill() > 0) && (ii.getEquipSkills(equip.getItemId()) != null)) {
                for (i$ = ii.getEquipSkills(equip.getItemId()).iterator(); i$.hasNext();) {
                    int zzz = ((Integer) i$.next()).intValue();
                    Skill skil = SkillFactory.getSkill(zzz);
                    if ((skil != null) && (skil.canBeLearnedBy(chra.getJob()))) {
                        int value = 1;
                        if (this.skillsIncrement.get(Integer.valueOf(skil.getId())) != null) {
                            value += ((Integer) this.skillsIncrement.get(Integer.valueOf(skil.getId()))).intValue();
                        }
                        this.skillsIncrement.put(Integer.valueOf(skil.getId()), Integer.valueOf(value));
                    }
                }
            }
            Pair ix = handleEquipAdditions(ii, chra, first_login, sData, equip.getItemId());
            if (ix != null) {
                localmaxhp_ += ((Integer) ix.getLeft()).intValue();
                localmaxmp_ += ((Integer) ix.getRight()).intValue();
            }
            if (equip.getState() >= 17) {
                int[] potentials = {equip.getPotential1(), equip.getPotential2(), equip.getPotential3(), equip.getPotential4(), equip.getPotential5()};
                for (int i : potentials) {
                    if (i > 0) {
                        StructItemOption soc = (StructItemOption) ii.getPotentialInfo(i).get(ii.getReqLevel(equip.getItemId()) / 10);
                        if (soc != null) {
                            localmaxhp_ += soc.get("incMHP");
                            localmaxmp_ += soc.get("incMMP");
                            handleItemOption(soc, chra, first_login, sData);
                        }
                    }
                }
            }
            if (equip.getSocketState() > 15) {
                int[] sockets = {equip.getSocket1(), equip.getSocket2(), equip.getSocket3()};
                for (int i : sockets) {
                    if (i > 0) {
                        StructItemOption soc = ii.getSocketInfo(i);
                        if (soc != null) {
                            localmaxhp_ += soc.get("incMHP");
                            localmaxmp_ += soc.get("incMMP");
                            handleItemOption(soc, chra, first_login, sData);
                        }
                    }
                }
            }
            if (equip.getDurability() > 0) {
                this.durabilityHandling.add(equip);
            }
            if ((GameConstants.getMaxLevel(equip.getItemId()) > 0) && (GameConstants.getStatFromWeapon(equip.getItemId()) == null ? equip.getEquipLevel() <= GameConstants.getMaxLevel(equip.getItemId()) : equip.getEquipLevel() < GameConstants.getMaxLevel(equip.getItemId()))) {
                this.equipLevelHandling.add(equip);
            }
        }
        Iterator iter = this.setHandling.entrySet().iterator();
        Map.Entry entry;
        while (iter.hasNext()) {
            entry = (Map.Entry) iter.next();
            StructSetItem set = ii.getSetItem(((Integer) entry.getKey()).intValue());
            if (set != null) {
                Map<Integer, SetItem> itemz = set.getItems();
                for (Entry ent : itemz.entrySet()) {
                    if (((Integer) ent.getKey()).intValue() <= ((Integer) entry.getValue()).intValue()) {
                        StructSetItem.SetItem se = (StructSetItem.SetItem) ent.getValue();
                        this.localstr += se.incSTR + se.incAllStat;
                        this.localdex += se.incDEX + se.incAllStat;
                        this.localint_ += se.incINT + se.incAllStat;
                        this.localluk += se.incLUK + se.incAllStat;
                        this.watk += se.incPAD;
                        this.magic += se.incMAD;
                        this.speed += se.incSpeed;
                        this.accuracy += se.incACC;
                        localmaxhp_ += se.incMHP;
                        localmaxmp_ += se.incMMP;
                        this.percent_hp += se.incMHPr;
                        this.percent_mp += se.incMMPr;
                        this.wdef += se.incPDD;
                        this.mdef += se.incMDD;
                        if ((se.option1 > 0) && (se.option1Level > 0)) {
                            StructItemOption soc = (StructItemOption) ii.getPotentialInfo(se.option1).get(se.option1Level);
                            if (soc != null) {
                                localmaxhp_ += soc.get("incMHP");
                                localmaxmp_ += soc.get("incMMP");
                                handleItemOption(soc, chra, first_login, sData);
                            }
                        }
                        if ((se.option2 > 0) && (se.option2Level > 0)) {
                            StructItemOption soc = (StructItemOption) ii.getPotentialInfo(se.option2).get(se.option2Level);
                            if (soc != null) {
                                localmaxhp_ += soc.get("incMHP");
                                localmaxmp_ += soc.get("incMMP");
                                handleItemOption(soc, chra, first_login, sData);
                            }
                        }
                    }
                }
            }
        }
        handleProfessionTool(chra);
        for (Item item : chra.getInventory(MapleInventoryType.CASH).newList()) {
            if (item.getItemId() / 100000 == 52) {
                if ((this.expMod < 3.0D) && ((item.getItemId() == 5211060) || (item.getItemId() == 5211050) || (item.getItemId() == 5211051) || (item.getItemId() == 5211052) || (item.getItemId() == 5211053) || (item.getItemId() == 5211054))) {
                    this.expMod = 3.0D;
                } else if ((this.expMod < 2.0D) && ((item.getItemId() == 5210000) || (item.getItemId() == 5210001) || (item.getItemId() == 5210002) || (item.getItemId() == 5210003) || (item.getItemId() == 5210004) || (item.getItemId() == 5210005) || (item.getItemId() == 5211061) || (item.getItemId() == 5211000) || (item.getItemId() == 5211001) || (item.getItemId() == 5211002) || (item.getItemId() == 5211003) || (item.getItemId() == 5211046) || (item.getItemId() == 5211047) || (item.getItemId() == 5211048) || (item.getItemId() == 5211049))) {
                    this.expMod = 2.0D;
                } else if ((this.expMod < 1.5D) && (item.getItemId() == 5211068)) {
                    this.expMod = 1.5D;
                }
            } else if ((this.dropMod == 1) && (item.getItemId() / 10000 == 536)) {
                if ((item.getItemId() == 5360000) || (item.getItemId() == 5360009) || (item.getItemId() == 5360010) || (item.getItemId() == 5360011) || (item.getItemId() == 5360012) || (item.getItemId() == 5360013) || (item.getItemId() == 5360014) || (item.getItemId() == 5360017) || (item.getItemId() == 5360050) || (item.getItemId() == 5360053) || (item.getItemId() == 5360042) || (item.getItemId() == 5360052)) {
                    this.dropMod = 2;
                }
            } else if ((this.levelBonus == 0) && (item.getItemId() == 5590000)) {
                this.levelBonus = 5;
            } else if (item.getItemId() == 5710000) {
                this.questBonus = 2;
            }
        }
        for (Item item : chra.getInventory(MapleInventoryType.ETC).list()) {
            switch (item.getItemId()) {
                case 4030003:
                    this.pickupRange = (1.0D / 0.0D);
                    break;
                case 4030004:
                    this.hasClone = true;
                    break;
                case 4030005:
                    this.cashMod = 2;
            }
        }

        if ((first_login) && (chra.getLevel() >= 30)) {
            if (chra.isGM()) {
                for (int i = 0; i < allJobs.length; i++) {
                    sData.put(SkillFactory.getSkill(1085 + allJobs[i]), new SkillEntry(1, (byte) 0, -1L));
                    sData.put(SkillFactory.getSkill(1087 + allJobs[i]), new SkillEntry(1, (byte) 0, -1L));
                    sData.put(SkillFactory.getSkill(1179 + allJobs[i]), new SkillEntry(1, (byte) 0, -1L));
                }
            } else {
                sData.put(SkillFactory.getSkill(getSkillByJob(1085, chra.getJob())), new SkillEntry(1, (byte) 0, -1L));
                sData.put(SkillFactory.getSkill(getSkillByJob(1087, chra.getJob())), new SkillEntry(1, (byte) 0, -1L));
                sData.put(SkillFactory.getSkill(getSkillByJob(1179, chra.getJob())), new SkillEntry(1, (byte) 0, -1L));
            }
        }

        handleBuffStats(chra);
        Integer buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_MAXHP);
        if (buff != null) {
            localmaxhp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_MAXMP);
        if (buff != null) {
            localmaxmp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.HP_BOOST);
        if (buff != null) {
            localmaxhp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MP_BOOST);
        if (buff != null) {
            localmaxmp_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MANA_WELL);
        if (buff != null) {
            localmaxmp_ += buff.intValue();
        }
        handlePassiveSkills(chra);
        long now;
        if (chra.getGuildId() > 0) {
            MapleGuild g = World.Guild.getGuild(chra.getGuildId());
            if ((g != null) && (g.getSkills().size() > 0)) {
                now = System.currentTimeMillis();
                for (MapleGuildSkill gs : g.getSkills()) {
                    if ((gs.timestamp > now) && (gs.activator.length() > 0)) {
                        MapleStatEffect e = SkillFactory.getSkill(gs.skillID).getEffect(gs.level);
                        this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + e.getCr()));
                        this.watk += e.getAttackX();
                        this.magic += e.getMagicX();
                        this.expBuff *= (e.getEXPRate() + 100.0D) / 100.0D;
                        this.dodgeChance += e.getER();
                        this.percent_wdef += e.getWDEFRate();
                        this.percent_mdef += e.getMDEFRate();
                    }
                }
            }
        }

        for (Pair ix : chra.getCharacterCard().getCardEffects()) {
            MapleStatEffect e = SkillFactory.getSkill(((Integer) ix.getLeft()).intValue()).getEffect(((Integer) ix.getRight()).intValue());
            this.percent_wdef += e.getWDEFRate();
            this.watk += e.getLevelToWatk() * chra.getLevel();
            this.percent_hp += e.getPercentHP();
            this.percent_mp += e.getPercentMP();
            this.magic += e.getLevelToMatk() * chra.getLevel();
            this.RecoveryUP += e.getMPConsumeEff();
            this.percent_acc += e.getPercentAcc();
            this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + e.getCr()));
            this.jump += e.getPassiveJump();
            this.speed += e.getPassiveSpeed();
            this.dodgeChance += e.getPercentAvoid();
            this.damX += e.getLevelToDamage() * chra.getLevel();
            this.BuffUP_Summon += e.getSummonTimeInc();
            this.expLossReduceR += e.getEXPLossRate();
            this.ASR += e.getASRRate();

            this.suddenDeathR += e.getSuddenDeathR();
            this.BuffUP_Skill += e.getBuffTimeRate();

            this.coolTimeR += e.getCooltimeReduceR();
            this.incMesoProp += e.getMesoAcquisition();
            this.damX = ((int) (this.damX + Math.floor(e.getHpToDamage() * oldmaxhp / 100.0F)));
            this.damX = ((int) (this.damX + Math.floor(e.getMpToDamage() * oldmaxhp / 100.0F)));

            this.passive_sharpeye_percent = ((short) (this.passive_sharpeye_percent + e.getCriticalMax()));
            this.ignoreTargetDEF += e.getIgnoreMob();
            this.localstr += e.getStrX();
            this.localdex += e.getDexX();
            this.localint_ += e.getIntX();
            this.localluk += e.getLukX();
            localmaxhp_ += e.getMaxHpX();
            localmaxmp_ += e.getMaxMpX();
            this.watk += e.getAttackX();
            this.magic += e.getMagicX();
            this.bossdam_r += e.getBossDamage();
        }

        this.localstr = ((int) (this.localstr + Math.floor(this.localstr * this.percent_str / 100.0F)));
        this.localdex = ((int) (this.localdex + Math.floor(this.localdex * this.percent_dex / 100.0F)));
        this.localint_ = ((int) (this.localint_ + Math.floor(this.localint_ * this.percent_int / 100.0F)));
        this.localluk = ((int) (this.localluk + Math.floor(this.localluk * this.percent_luk / 100.0F)));
        if (this.localint_ > this.localdex) {
            this.accuracy = ((int) (this.accuracy + (this.localint_ + Math.floor(this.localluk * 1.2D))));
        } else {
            this.accuracy = ((int) (this.accuracy + (this.localluk + Math.floor(this.localdex * 1.2D))));
        }
        this.watk = ((int) (this.watk + Math.floor(this.watk * this.percent_atk / 100.0F)));
        this.magic = ((int) (this.magic + Math.floor(this.magic * this.percent_matk / 100.0F)));
        this.localint_ = ((int) (this.localint_ + Math.floor(this.localint_ * this.percent_matk / 100.0F)));

        this.wdef = ((int) (this.wdef + Math.floor(this.localstr * 1.2D + (this.localdex + this.localluk) * 0.5D + this.localint_ * 0.4D)));
        this.mdef = ((int) (this.mdef + Math.floor(this.localstr * 0.4D + (this.localdex + this.localluk) * 0.5D + this.localint_ * 1.2D)));
        this.wdef = ((int) (this.wdef + Math.min(30000.0D, Math.floor(this.wdef * this.percent_wdef / 100.0F))));
        this.mdef = ((int) (this.mdef + Math.min(30000.0D, Math.floor(this.wdef * this.percent_mdef / 100.0F))));

        this.hands = (this.localdex + this.localint_ + this.localluk);
        calculateFame(chra);
        this.ignoreTargetDEF += chra.getTrait(MapleTrait.MapleTraitType.charisma).getLevel() / 10;
        this.pvpDamage += chra.getTrait(MapleTrait.MapleTraitType.charisma).getLevel() / 10;
        this.ASR += chra.getTrait(MapleTrait.MapleTraitType.will).getLevel() / 5;

        this.accuracy = ((int) (this.accuracy + Math.floor(this.accuracy * this.percent_acc / 100.0F)));
        this.accuracy += chra.getTrait(MapleTrait.MapleTraitType.insight).getLevel() * 15 / 10;

        localmaxhp_ = (int) (localmaxhp_ + Math.floor(this.percent_hp * localmaxhp_ / 100.0F));
        localmaxhp_ += chra.getTrait(MapleTrait.MapleTraitType.will).getLevel() * 20;
        this.localmaxhp = Math.min(500000, Math.abs(Math.max(-500000, localmaxhp_)));

        localmaxmp_ = (int) (localmaxmp_ + Math.floor(this.percent_mp * localmaxmp_ / 100.0F));
        localmaxmp_ += chra.getTrait(MapleTrait.MapleTraitType.sense).getLevel() * 20;
        this.localmaxmp = Math.min(500000, Math.abs(Math.max(-500000, localmaxmp_)));
     //   if (GameConstants.demonAvenger(chra.getJob())) {
    //        chra.getClient().getSession().write(JobPacket.AvengerPacket.giveAvengerHpBuff(hp));
    //    }
        if ((chra.getEventInstance() != null) && (chra.getEventInstance().getName().startsWith("PVP"))) {
            this.localmaxhp = Math.min(40000, this.localmaxhp * 3);
            this.localmaxmp = Math.min(20000, this.localmaxmp * 2);

            for (int i : pvpSkills) {
                Skill skil = SkillFactory.getSkill(i);
                if ((skil != null) && (skil.canBeLearnedBy(chra.getJob()))) {
                    sData.put(skil, new SkillEntry(1, (byte) 0, -1L));
                    MapleStatEffect eff = skil.getEffect(1);
                    switch (i / 1000000 % 10) {
                        case 1:
                            if (eff.getX() > 0) {
                                this.pvpDamage += this.wdef / eff.getX();
                            }
                            break;
                        case 3:
                            this.hpRecoverProp += eff.getProb();
                            this.hpRecover += eff.getX();
                            this.mpRecoverProp += eff.getProb();
                            this.mpRecover += eff.getX();
                            break;
                        case 5:
                            this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + eff.getProb()));
                            this.passive_sharpeye_percent = 100;
                        case 2:
                        case 4:
                    }
                    break;
                }
            }
            MapleStatEffect eff = chra.getStatForBuff(MapleBuffStat.MORPH);
            if ((eff != null) && (eff.getSourceId() % 10000 == 1105)) {
                this.localmaxhp = 500000;
                this.localmaxmp = 500000;
            }
        }
        chra.changeSkillLevel_Skip(sData, false);
        if (GameConstants.isDemon(chra.getJob())) {
            this.localmaxmp = GameConstants.getMPByJob(chra.getJob());
        }
             if (GameConstants.demonAvenger(chra.getJob())) {
            chra.getClient().getSession().write(JobPacket.AvengerPacket.giveAvengerHpBuff(hp));
        }
        CalcPassive_SharpEye(chra);
        CalcPassive_Mastery(chra);
        recalcPVPRank(chra);
        if (first_login) {
            chra.silentEnforceMaxHpMp();
            relocHeal(chra);
        } else {
            chra.enforceMaxHpMp();
        }
        calculateMaxBaseDamage(Math.max(this.magic, this.watk), this.pvpDamage, chra);
        this.trueMastery = Math.min(100, this.trueMastery);
        this.passive_sharpeye_min_percent = ((short) Math.min(this.passive_sharpeye_min_percent, this.passive_sharpeye_percent));
        if ((oldmaxhp != 0) && (oldmaxhp != this.localmaxhp)) {
            chra.updatePartyMemberHP();
        }
  
    }*/
    
    private void handlePassiveSkills(MapleCharacter chra) {
        Skill bx;
        int bof;
        MapleStatEffect eff;
        if (GameConstants.isKOC(chra.getJob())) {
            bx = SkillFactory.getSkill(2000006);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
                eff = bx.getEffect(bof);
                percent_hp += eff.getX();
                percent_mp += eff.getX();
            }
        }
        switch (chra.getJob()) {
            case 200:
            case 210:
            case 211:
            case 212:
            case 220:
            case 221:
            case 222:
            case 230:
            case 231:
            case 232: {
                bx = SkillFactory.getSkill(2000006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_mp += bx.getEffect(bof).getPercentMP();
                }
                break;
            }
            case 1200:
            case 1210:
            case 1211:
            case 1212: {
                bx = SkillFactory.getSkill(12000005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_mp += bx.getEffect(bof).getPercentMP();
                }
                bx = SkillFactory.getSkill(12110000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    TER += bx.getEffect(bof).getX();
                }
                break;
            }
            case 1100:
            case 1110:
            case 1111:
            case 1112: {
                bx = SkillFactory.getSkill(11000005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getPercentHP();
                }
                break;
            }
            case 2003: // Phantom noob
                // Phantom Instinct - 20030204
                // Dexterous Training - 20030206
                bx = SkillFactory.getSkill(20030204); // +10% crit rate
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    passive_sharpeye_rate += eff.getCr();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                bx = SkillFactory.getSkill(20030206); // +40 DEX, Base Avoidability: +20%
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    localdex += eff.getDexX();
                    dodgeChance += eff.getER();
                }
                break;
            case 2400:
            case 2410:
            case 2411:
            case 2412: { // Phantom
                // Blanc Carte - 24100003
                // Cane Mastery - 24100004
                // Luck of Phantom Thief - 24111002
                // 24111003- uses monlight effect, but is Misfortune Protection
                // 24110004 - Flash and Flee -> active
                // 24111005 - Moonlight
                // 24111006 - Phantom Charge
                // 24111008- Breeze Carte, (hidden), linked to phantom charge
                // 24121000 - Ultimate Drive
                // 24120002 - Noir Carte
                // 24121003 - Twilight
                // 24121004 - Pray of Aria
                // 24121005 - Tempest of Card
                // 24120006 - Cane Expert
                // 24121007 - Soul Steal
                // 24121008 - Maple Warrior
                // 24121009 - Hero's will
                // 24121010 - Some linked skill (Twilight)
                bx = SkillFactory.getSkill(24001002); // Swift Phantom
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    speed += eff.getPassiveSpeed();
                    jump += eff.getPassiveJump();
                }
                bx = SkillFactory.getSkill(24000003); // Quick Evasion
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dodgeChance += eff.getX();
                }
                bx = SkillFactory.getSkill(24100006); //Luck Monopoly
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    localluk += eff.getLukX();
                }
                bx = SkillFactory.getSkill(24110007); // Acute Sense
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    passive_sharpeye_rate += eff.getCr();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                bx = SkillFactory.getSkill(24111002); //Luck of Phantom Thief
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    localluk += eff.getLukX();
                }
                break;
            }
            case 501:
            case 530:
            case 531:
            case 532:
                bx = SkillFactory.getSkill(5010003);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    watk += bx.getEffect(bof).getAttackX();
                }
                bx = SkillFactory.getSkill(5300008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(5311001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    damageIncrease.put(5301001, (int) bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(5310007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getHpR();
                    ASR += eff.getASRRate();
                    percent_wdef += eff.getWDEFRate();
                }
                bx = SkillFactory.getSkill(5310006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    watk += bx.getEffect(bof).getAttackX();
                }
                bx = SkillFactory.getSkill(5321009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                break;
            case 3001:
            case 3100:
            case 3110:
            case 3111:
            case 3112:
                mpRecoverProp = 100;
                bx = SkillFactory.getSkill(31000003);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getHpR();
                }
                bx = SkillFactory.getSkill(31100007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(31000004, (int) eff.getDAMRate());
                    damageIncrease.put(31001006, (int) eff.getDAMRate());
                    damageIncrease.put(31001007, (int) eff.getDAMRate());
                    damageIncrease.put(31001008, (int) eff.getDAMRate());
                }
                bx = SkillFactory.getSkill(31100005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(31100010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(31000004, (int) eff.getX());
                    damageIncrease.put(31001006, (int) eff.getX());
                    damageIncrease.put(31001007, (int) eff.getX());
                    damageIncrease.put(31001008, (int) eff.getX());
                }
                bx = SkillFactory.getSkill(31111007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                }
                bx = SkillFactory.getSkill(31110008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dodgeChance += eff.getX();
                    // HACK: shouldn't be here
                    hpRecoverPercent += eff.getY();
                    hpRecoverProp += eff.getX();
                    //mpRecover += eff.getY(); // handle in takeDamage
                    //mpRecoverProp += eff.getX();
                }
                bx = SkillFactory.getSkill(31110009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    mpRecover += 1;
                    mpRecoverProp += eff.getProb();
                }
                bx = SkillFactory.getSkill(31111006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getX() + 100.0) / 100.0;
                    bossdam_r *= (eff.getX() + 100.0) / 100.0;
                    passive_sharpeye_rate += eff.getY();
                }
                bx = SkillFactory.getSkill(31121006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ignoreTargetDEF += bx.getEffect(bof).getIgnoreMob();
                }
                bx = SkillFactory.getSkill(31120011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(31000004, (int) eff.getX());
                    damageIncrease.put(31001006, (int) eff.getX());
                    damageIncrease.put(31001007, (int) eff.getX());
                    damageIncrease.put(31001008, (int) eff.getX());
                }
                bx = SkillFactory.getSkill(31120008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    watk += eff.getAttackX();
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                bx = SkillFactory.getSkill(31120010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_wdef += bx.getEffect(bof).getT();
                }
                bx = SkillFactory.getSkill(30010112);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    bossdam_r += eff.getBossDamage();
                    mpRecover += eff.getX();
                    mpRecoverProp += eff.getBossDamage(); //yes
                }
                bx = SkillFactory.getSkill(30010185);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    chra.getTrait(MapleTraitType.will).addLocalExp(GameConstants.getTraitExpNeededForLevel(eff.getY()));
                    chra.getTrait(MapleTraitType.charisma).addLocalExp(GameConstants.getTraitExpNeededForLevel(eff.getZ()));
                }
                bx = SkillFactory.getSkill(30010111);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    hpRecoverPercent += eff.getX();
                    hpRecoverProp += eff.getProb(); //yes
                }
                break;
            case 510:
            case 511:
            case 512: {
                bx = SkillFactory.getSkill(5100009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getPercentHP();
                }
                break;
            }
            case 1510:
            case 1511:
            case 1512: {
                bx = SkillFactory.getSkill(15100007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getPercentHP();
                }
                break;
            }
            case 508:
            case 570:
            case 571:
            case 572: { // Jett
                bx = SkillFactory.getSkill(5080000); // Comet Booster
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    accuracy += eff.getAccX();
                    jump += eff.getPassiveJump();
                    speed += eff.getSpeedMax(); // TODO: split speed max and speed. (speed have a limit, while speedMax will add to the max)
                } // TODO: research more on percentage hp/mp and stats, which doesn't take effect to note.
                bx = SkillFactory.getSkill(5080004); // Shadow Heart
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    passive_sharpeye_rate += eff.getCr();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                // 5081010, 5081011: Hidden
                if (chra.getJob() >= 570) {
                    bx = SkillFactory.getSkill(5700003); // Physical Training
                    bof = chra.getTotalSkillLevel(bx);
                    if (bof > 0) {
                        eff = bx.getEffect(bof);
                        localstr += eff.getStrX();
                        localdex += eff.getDexX();
                    }
                }
                if (chra.getJob() >= 571) {
                    bx = SkillFactory.getSkill(5710004); // High Life
                    bof = chra.getTotalSkillLevel(bx);
                    if (bof > 0) {
                        eff = bx.getEffect(bof);
                        percent_wdef += eff.getWDEFRate();
                        percent_mdef += eff.getMDEFRate();
                        add_hp += eff.getMaxHpX();
                        add_mp += eff.getMaxMpX();
                    }
                    bx = SkillFactory.getSkill(5710005); // Cutting Edge
                    bof = chra.getTotalSkillLevel(bx);
                    if (bof > 0) {
                        eff = bx.getEffect(bof);
                        passive_sharpeye_rate += eff.getCr();
                        ignoreTargetDEF += eff.getIgnoreMob();
                    }
                }
                if (chra.getJob() == 572) {
                    bx = SkillFactory.getSkill(5720008); // Collateral Damage
                    bof = chra.getTotalSkillLevel(bx);
                    if (bof > 0) {
                        eff = bx.getEffect(bof);
                        passive_sharpeye_rate += eff.getCr();
                        passive_sharpeye_min_percent += eff.getCriticalMin();
                        passive_sharpeye_percent += eff.getCriticalMax();
                        bossdam_r += eff.getBossDamage();
                    }
                    // TODO: 5721009, 5720012(Counterattack)
                }
                break;
            }
            case 400: // Thief
            case 410: // Assassin
            case 411: // Hermit
            case 412: // Night Lord
            case 420: // Bandit
            case 421: // Chief Bandit
            case 422: { // Shadower
                bx = SkillFactory.getSkill(4001005); // Haste
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    speed += eff.getSpeedMax();
                }
                // 4000010: Magic Theft, invisible.
                if (chra.getJob() >= 410 && chra.getJob() <= 412) {
                    bx = SkillFactory.getSkill(4100007); // Physical Training
                    bof = chra.getTotalSkillLevel(bx);
                    if (bof > 0) {
                        eff = bx.getEffect(bof);
                        localluk += eff.getLukX();
                        localdex += eff.getDexX();
                    }
                }
                if (chra.getJob() == 411 || chra.getJob() == 412) {
                    bx = SkillFactory.getSkill(4110008); // Enveloping Darkness
                    bof = chra.getTotalSkillLevel(bx);
                    if (bof > 0) {
                        eff = bx.getEffect(bof);
                        percent_hp += eff.getPercentHP();
                        ASR += eff.getASRRate();
                        TER += eff.getTERRate();
                    }
                    bx = SkillFactory.getSkill(4110012); // Expert Throwing Star Handling
                    bof = chra.getTotalSkillLevel(bx);
                    if (bof > 0) {
                        eff = bx.getEffect(bof);
                        damageIncrease.put(4001344, eff.getDAMRate());
                        damageIncrease.put(4101008, eff.getDAMRate());
                        damageIncrease.put(4101009, eff.getDAMRate());
                        damageIncrease.put(4101010, eff.getDAMRate());
                    }
                    bx = SkillFactory.getSkill(4110014);
                    bof = chra.getTotalSkillLevel(bx);
                    if (bof > 0) {
                        eff = bx.getEffect(bof);
                        RecoveryUP += eff.getX() - 100;
                    }
                }
                if (chra.getJob() == 412) {
                    bx = SkillFactory.getSkill(4121014); // Dark Harmony
                    bof = chra.getTotalSkillLevel(bx);
                    if (bof > 0) {
                        eff = bx.getEffect(bof);
                        ignoreTargetDEF += eff.getIgnoreMob();
                    }
                }

                bx = SkillFactory.getSkill(4200006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getPercentHP();
                    ASR += eff.getASRRate();
                    TER += eff.getTERRate();
                }
                bx = SkillFactory.getSkill(4210000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_wdef += eff.getX();
                    percent_mdef += eff.getX();
                }
                break;
            }
            case 431: // Blade Acolyte
            case 432: // Blade Specialist
            case 433: // Blade Lord
            case 434: { // Blade Master
                bx = SkillFactory.getSkill(4001006); // Haste
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    speed += eff.getSpeedMax();
                }

                bx = SkillFactory.getSkill(4310004);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getPercentHP();
                    ASR += eff.getASRRate();
                    TER += eff.getTERRate();
                }
                bx = SkillFactory.getSkill(4341006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_wdef += eff.getWDEFRate();
                    percent_mdef += eff.getMDEFRate();
                }
                break;
            }
            case 100:
            case 110:
            case 111:
            case 112:
            case 120:
            case 121:
            case 122:
            case 130:
            case 131:
            case 132: {
                bx = SkillFactory.getSkill(1000006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getPercentHP();
                }
                bx = SkillFactory.getSkill(1210001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_wdef += eff.getX();
                    percent_mdef += eff.getX();
                }
                bx = SkillFactory.getSkill(1220005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_wdef += bx.getEffect(bof).getT();
                }
                bx = SkillFactory.getSkill(1220010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    trueMastery += bx.getEffect(bof).getMastery();
                }
                bx = SkillFactory.getSkill(1310000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    TER += bx.getEffect(bof).getX();
                }
                break;
            }
            case 322: { // Crossbowman
                bx = SkillFactory.getSkill(3220004);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    watk += eff.getX();
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                bx = SkillFactory.getSkill(3220009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getPercentHP();
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                bx = SkillFactory.getSkill(3220005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0 && chra.getBuffedValue(MapleBuffStat.SPIRIT_LINK) != null) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getX();
                    dam_r *= (eff.getDamage() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDamage() + 100.0) / 100.0;
                }
                break;
            }
            case 312: { // Bowmaster
                bx = SkillFactory.getSkill(3120005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    watk += bx.getEffect(bof).getX();
                }
                bx = SkillFactory.getSkill(3120011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getPercentHP();
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                bx = SkillFactory.getSkill(3120006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0 && chra.getBuffedValue(MapleBuffStat.SPIRIT_LINK) != null) {
                    eff = bx.getEffect(bof);
                    percent_hp += eff.getX();
                    dam_r *= (eff.getDamage() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDamage() + 100.0) / 100.0;
                }
                break;
            }
            case 3510:
            case 3511:
            case 3512: {
                bx = SkillFactory.getSkill(35100000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    watk += bx.getEffect(bof).getAttackX();
                }
                bx = SkillFactory.getSkill(35120000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    trueMastery += bx.getEffect(bof).getMastery();
                }
                break;
            }
            case 3211:
            case 3212: {
                bx = SkillFactory.getSkill(32110000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ASR += bx.getEffect(bof).getASRRate();
                    TER += bx.getEffect(bof).getTERRate();
                }
                bx = SkillFactory.getSkill(32110001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                bx = SkillFactory.getSkill(32120000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    magic += bx.getEffect(bof).getMagicX();
                }
                bx = SkillFactory.getSkill(32120001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(32120009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getPercentHP();
                }
                break;
            }
            case 3300:
            case 3310:
            case 3311:
            case 3312: {
                bx = SkillFactory.getSkill(33120000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    watk += eff.getX();
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                bx = SkillFactory.getSkill(33110000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDamage() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDamage() + 100.0) / 100.0;
                }
                bx = SkillFactory.getSkill(33120010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    ignoreTargetDEF += eff.getIgnoreMob();
                    dodgeChance += eff.getER();
                }
                bx = SkillFactory.getSkill(32110001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                }
                break;
            }
            case 2200:
            case 2210:
            case 2211:
            case 2212:
            case 2213:
            case 2214:
            case 2215:
            case 2216:
            case 2217:
            case 2218: {
                magic += chra.getTotalSkillLevel(SkillFactory.getSkill(22000000));
                bx = SkillFactory.getSkill(22150000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    mpconPercent += eff.getX() - 100;
                    dam_r *= eff.getY() / 100.0;
                    bossdam_r *= eff.getY() / 100.0;
                }
                bx = SkillFactory.getSkill(22160000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDamage() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDamage() + 100.0) / 100.0;
                }
                bx = SkillFactory.getSkill(22170001); // magic mastery, this is an invisible skill
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    magic += eff.getX();
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                break;
            }
            case 2112: {
                bx = SkillFactory.getSkill(21120001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    watk += eff.getX();
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                break;
            }
        }
        bx = SkillFactory.getSkill(80000000);
        bof = chra.getSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            localstr += eff.getStrX();
            localdex += eff.getDexX();
            localint_ += eff.getIntX();
            localluk += eff.getLukX();
            percent_hp += eff.getHpR();
            percent_mp += eff.getMpR();
        }
        bx = SkillFactory.getSkill(80000001);
        bof = chra.getSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            bossdam_r += eff.getBossDamage();
        }
        bx = SkillFactory.getSkill(80001040);
        bof = chra.getSkillLevel(bx);
        if (bof > 0) {
            expBuff *= (bx.getEffect(bof).getEXPRate() + 100.0) / 100.0;
        }
        if (GameConstants.isAdventurer(chra.getJob())) {
            bx = SkillFactory.getSkill(74);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                levelBonus += bx.getEffect(bof).getX();
            }

            bx = SkillFactory.getSkill(80);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                levelBonus += bx.getEffect(bof).getX();
            }

            bx = SkillFactory.getSkill(10074);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                levelBonus += bx.getEffect(bof).getX();
            }

            bx = SkillFactory.getSkill(10080);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                levelBonus += bx.getEffect(bof).getX();
            }

            bx = SkillFactory.getSkill(110);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                eff = bx.getEffect(bof);
                localstr += eff.getStrX();
                localdex += eff.getDexX();
                localint_ += eff.getIntX();
                localluk += eff.getLukX();
                percent_hp += eff.getHpR();
                percent_mp += eff.getMpR();
            }

            bx = SkillFactory.getSkill(10110);
            bof = chra.getSkillLevel(bx);
            if (bof > 0) {
                eff = bx.getEffect(bof);
                localstr += eff.getStrX();
                localdex += eff.getDexX();
                localint_ += eff.getIntX();
                localluk += eff.getLukX();
                percent_hp += eff.getHpR();
                percent_mp += eff.getMpR();
            }
        }
        bx = SkillFactory.getSkill(GameConstants.getBOF_ForJob(chra.getJob()));
        bof = chra.getSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            watk += eff.getX();
            magic += eff.getY();
            accuracy += eff.getX();
        }

        bx = SkillFactory.getSkill(GameConstants.getEmpress_ForJob(chra.getJob()));
        bof = chra.getSkillLevel(bx);
        if (bof > 0) {
            eff = bx.getEffect(bof);
            watk += eff.getX();
            magic += eff.getY();
            accuracy += eff.getZ();
        }
        switch (chra.getJob()) {
            case 210:
            case 211:
            case 212: { // IL
                bx = SkillFactory.getSkill(2100007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    localint_ += bx.getEffect(bof).getIntX();
                }
                bx = SkillFactory.getSkill(2110000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dotTime += eff.getX();
                    dot += eff.getZ();
                }
                bx = SkillFactory.getSkill(2110001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    mpconPercent += eff.getX() - 100;
                    dam_r *= eff.getY() / 100.0;
                    bossdam_r *= eff.getY() / 100.0;
                }
                bx = SkillFactory.getSkill(2121003);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(2111003, (int) eff.getX());
                }
                bx = SkillFactory.getSkill(2120009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    magic += eff.getMagicX();
                    BuffUP_Skill += eff.getX();
                }
                bx = SkillFactory.getSkill(2121005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    TER += bx.getEffect(bof).getTERRate();
                }
                bx = SkillFactory.getSkill(2121009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    magic += bx.getEffect(bof).getMagicX();
                }
                bx = SkillFactory.getSkill(2120010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getX() * eff.getY() + 100.0) / 100.0;
                    bossdam_r *= (eff.getX() * eff.getY() + 100.0) / 100.0;
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                break;
            }
            case 220:
            case 221:
            case 222: { // IL
                bx = SkillFactory.getSkill(2200007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    localint_ += bx.getEffect(bof).getIntX();
                }
                bx = SkillFactory.getSkill(2210000);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dot += bx.getEffect(bof).getZ();
                }
                bx = SkillFactory.getSkill(2210001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    mpconPercent += eff.getX() - 100;
                    dam_r *= eff.getY() / 100.0;
                    bossdam_r *= eff.getY() / 100.0;
                }
                bx = SkillFactory.getSkill(2220009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    magic += eff.getMagicX();
                    BuffUP_Skill += eff.getX();
                }
                bx = SkillFactory.getSkill(2221005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    TER += bx.getEffect(bof).getTERRate();
                }
                bx = SkillFactory.getSkill(2221009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    magic += bx.getEffect(bof).getMagicX();
                }
                bx = SkillFactory.getSkill(2220010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getX() * eff.getY() + 100.0) / 100.0;
                    bossdam_r *= (eff.getX() * eff.getY() + 100.0) / 100.0;
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                break;
            }
            case 1211:
            case 1212: { // flame
                bx = SkillFactory.getSkill(12110001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    mpconPercent += eff.getX() - 100;
                    dam_r *= eff.getY() / 100.0;
                    bossdam_r *= eff.getY() / 100.0;
                }

                bx = SkillFactory.getSkill(12111004);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    TER += bx.getEffect(bof).getTERRate();
                }
                break;
            }
            case 230:
            case 231:
            case 232: { // Bishop
                bx = SkillFactory.getSkill(2300007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    localint_ += bx.getEffect(bof).getIntX();
                }
                bx = SkillFactory.getSkill(2310008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    passive_sharpeye_rate += bx.getEffect(bof).getCr();
                }
                bx = SkillFactory.getSkill(2320010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    magic += eff.getMagicX();
                    BuffUP_Skill += eff.getX();
                }
                bx = SkillFactory.getSkill(2321010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    magic += bx.getEffect(bof).getMagicX();
                }
                bx = SkillFactory.getSkill(2320005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ASR += bx.getEffect(bof).getASRRate();
                }
                bx = SkillFactory.getSkill(2320011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getX() * eff.getY() + 100.0) / 100.0;
                    bossdam_r *= (eff.getX() * eff.getY() + 100.0) / 100.0;
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                break;
            }
            case 2002:
            case 2300:
            case 2310:
            case 2311:
            case 2312: {
                bx = SkillFactory.getSkill(20021110);
                bof = chra.getSkillLevel(bx);
                if (bof > 0) {
                    expBuff *= (bx.getEffect(bof).getEXPRate() + 100.0) / 100.0;
                }
                bx = SkillFactory.getSkill(20020112);
                bof = chra.getSkillLevel(bx);
                if (bof > 0) {
                    chra.getTrait(MapleTraitType.charm).addLocalExp(GameConstants.getTraitExpNeededForLevel(30));
                }
                bx = SkillFactory.getSkill(23000001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(23100008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(23110004);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getProb();
                }
                bx = SkillFactory.getSkill(23110004);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    damageIncrease.put(23101001, (int) bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(23121004);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getProb();
                }
                bx = SkillFactory.getSkill(23120009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    watk += bx.getEffect(bof).getX();
                }
                bx = SkillFactory.getSkill(23120010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ignoreTargetDEF += bx.getEffect(bof).getX(); //or should we do 100?
                }
                bx = SkillFactory.getSkill(23120011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    damageIncrease.put(23101001, (int) bx.getEffect(bof).getDAMRate());
                }
                bx = SkillFactory.getSkill(23120012);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    watk += bx.getEffect(bof).getAttackX();
                }
                break;
            }
            case 1300:
            case 1310:
            case 1311:
            case 1312: {
                bx = SkillFactory.getSkill(13000001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    defRange += bx.getEffect(bof).getRange();
                }
                bx = SkillFactory.getSkill(13110008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(13110003);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                break;
            }
            case 300:
            case 310:
            case 311:
            case 312: {
                bx = SkillFactory.getSkill(3000002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    defRange += bx.getEffect(bof).getRange();
                }
                bx = SkillFactory.getSkill(3100006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(3001004, eff.getX());
                    damageIncrease.put(3001005, eff.getY());
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(3110007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getER();
                }
                bx = SkillFactory.getSkill(3120005);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                break;
            }
            case 320:
            case 321:
            case 322: {
                bx = SkillFactory.getSkill(3000002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    defRange += bx.getEffect(bof).getRange();
                }
                bx = SkillFactory.getSkill(3200006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(3001004, eff.getX());
                    damageIncrease.put(3001005, eff.getY());
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(3220010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    damageIncrease.put(3211006, bx.getEffect(bof).getDamage() - 150);
                }
                bx = SkillFactory.getSkill(3210007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getER();
                }
                break;
            }
            case 422: {
                bx = SkillFactory.getSkill(4221007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Savage Blow, Steal, and Assaulter
                    eff = bx.getEffect(bof);
                    damageIncrease.put(4201005, (int) eff.getDAMRate());
                    damageIncrease.put(4201004, (int) eff.getDAMRate());
                    damageIncrease.put(4211002, (int) eff.getDAMRate());
                }
                bx = SkillFactory.getSkill(4210012);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    mesoBuff *= (eff.getMesoRate() + 100.0) / 100.0;
                    pickRate += eff.getU();
                    mesoGuard -= eff.getV();
                    mesoGuardMeso -= eff.getW();
                    damageIncrease.put(4211006, eff.getX());
                }
                break;
            }
            case 433:
            case 434: {
                bx = SkillFactory.getSkill(4330007);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    hpRecoverProp += eff.getProb();
                    hpRecoverPercent += eff.getX();
                }
                bx = SkillFactory.getSkill(4341002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Fatal Blow, Slash Storm, Tornado Spin, Bloody Storm, Upper Stab, and Flying Assaulter
                    eff = bx.getEffect(bof);
                    damageIncrease.put(4311002, (int) eff.getDAMRate());
                    damageIncrease.put(4311003, (int) eff.getDAMRate());
                    damageIncrease.put(4321000, (int) eff.getDAMRate());
                    damageIncrease.put(4321001, (int) eff.getDAMRate());
                    damageIncrease.put(4331000, (int) eff.getDAMRate());
                    damageIncrease.put(4331004, (int) eff.getDAMRate());
                    damageIncrease.put(4331005, (int) eff.getDAMRate());
                }
                bx = SkillFactory.getSkill(4341006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    dodgeChance += bx.getEffect(bof).getER();
                }
                break;
            }
            case 2110:
            case 2111:
            case 2112: { // Aran
                bx = SkillFactory.getSkill(21101006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDAMRate() + 100.0) / 100.0;
                }
                bx = SkillFactory.getSkill(21110002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    damageIncrease.put(21000004, bx.getEffect(bof).getW());
                }
                bx = SkillFactory.getSkill(21111010);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ignoreTargetDEF += bx.getEffect(bof).getIgnoreMob();
                }
                bx = SkillFactory.getSkill(21120002);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    damageIncrease.put(21100007, bx.getEffect(bof).getZ());
                }
                bx = SkillFactory.getSkill(21120011);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(21100002, (int) eff.getDAMRate());
                    damageIncrease.put(21110003, (int) eff.getDAMRate());
                }
                break;
            }
            case 3511:
            case 3512: {
                bx = SkillFactory.getSkill(35110014);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //ME-07 Drillhands, Atomic Hammer
                    eff = bx.getEffect(bof);
                    damageIncrease.put(35001003, (int) eff.getDAMRate());
                    damageIncrease.put(35101003, (int) eff.getDAMRate());
                }
                bx = SkillFactory.getSkill(35121006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Satellite
                    eff = bx.getEffect(bof);
                    damageIncrease.put(35111001, (int) eff.getDAMRate());
                    damageIncrease.put(35111009, (int) eff.getDAMRate());
                    damageIncrease.put(35111010, (int) eff.getDAMRate());
                }
                bx = SkillFactory.getSkill(35120001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Satellite
                    eff = bx.getEffect(bof);
                    damageIncrease.put(35111005, eff.getX());
                    damageIncrease.put(35111011, eff.getX());
                    damageIncrease.put(35121009, eff.getX());
                    damageIncrease.put(35121010, eff.getX());
                    damageIncrease.put(35121011, eff.getX());
                    BuffUP_Summon += eff.getY();
                }
                break;
            }
            case 110:
            case 111:
            case 112: {
                bx = SkillFactory.getSkill(1100009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(1001004, eff.getX());
                    damageIncrease.put(1001005, eff.getY());
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(1110009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= eff.getDamage() / 100.0;
                    bossdam_r *= eff.getDamage() / 100.0;
                }
                bx = SkillFactory.getSkill(1120012);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ignoreTargetDEF += bx.getEffect(bof).getIgnoreMob();
                }
                bx = SkillFactory.getSkill(1120013);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    watk += eff.getAttackX();
                    damageIncrease.put(1100002, (int) eff.getDamage());
                }
                break;
            }
            case 120:
            case 121:
            case 122: {
                bx = SkillFactory.getSkill(1200009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(1001004, eff.getX());
                    damageIncrease.put(1001005, eff.getY());
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(1220006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    ASR += bx.getEffect(bof).getASRRate();
                    TER += bx.getEffect(bof).getTERRate();
                }
                break;
            }
            case 511:
            case 512: {
                bx = SkillFactory.getSkill(5110008);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Backspin Blow, Double Uppercut, and Corkscrew Blow
                    eff = bx.getEffect(bof);
                    damageIncrease.put(5101002, eff.getX());
                    damageIncrease.put(5101003, eff.getY());
                    damageIncrease.put(5101004, eff.getZ());
                }
                break;
            }
            case 520:
            case 521:
            case 522: {
                bx = SkillFactory.getSkill(5220001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) { //Flamethrower and Ice Splitter
                    eff = bx.getEffect(bof);
                    damageIncrease.put(5211004, (int) eff.getDamage());
                    damageIncrease.put(5211005, (int) eff.getDamage());
                }
                break;
            }
            case 130:
            case 131:
            case 132: {
                bx = SkillFactory.getSkill(1300009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(1001004, eff.getX());
                    damageIncrease.put(1001005, eff.getY());
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(1310009);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    passive_sharpeye_rate += eff.getCr();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                    hpRecoverProp += eff.getProb();
                    hpRecoverPercent += eff.getX();
                }
                bx = SkillFactory.getSkill(1320006);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    dam_r *= (eff.getDamage() + 100.0) / 100.0;
                    bossdam_r *= (eff.getDamage() + 100.0) / 100.0;
                }
                break;
            }
            case 1400:
            case 1410:
            case 1411:
            case 1412: {
                bx = SkillFactory.getSkill(14110003);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    RecoveryUP += eff.getX() - 100;
                    BuffUP += eff.getY() - 100;
                }
                bx = SkillFactory.getSkill(14000001);
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    defRange += bx.getEffect(bof).getRange();
                }
                break;
            }
            case 5000: // Mihile 0
            case 5100: // Mihile 1
            case 5110: // Mihile 2
            case 5111: // Mihile 3
            case 5112: { // Mihile 4
                // Mihile 1st Job Passive Skills
                bx = SkillFactory.getSkill(51000000); // Mihile || HP Boost
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_hp += bx.getEffect(bof).getPercentHP();
                }
                bx = SkillFactory.getSkill(51000001); // Mihile || Soul Shield
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    percent_wdef += eff.getX();
                    percent_mdef += eff.getX();
                }
                bx = SkillFactory.getSkill(51000002); // Mihile || Soul Devotion
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    accuracy += eff.getAccX();
                    speed += eff.getPassiveSpeed();
                    jump += eff.getPassiveJump();
                }

                // Mihile 2nd Job Passive Skills
                bx = SkillFactory.getSkill(51100000); // Mihile || Physical Training
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    damageIncrease.put(5001002, eff.getX());
                    damageIncrease.put(5001003, eff.getY());
                    localstr += eff.getStrX();
                    localdex += eff.getDexX();
                }
                bx = SkillFactory.getSkill(51120002); // Mihile || Final Attack && Advanced Final Attack
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    watk += eff.getAttackX();
                    damageIncrease.put(51100002, (int) eff.getDamage());
                }

                // Mihile 3rd Job Passive Skills
                bx = SkillFactory.getSkill(51110000); // Mihile || Self Recovery
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    hpRecoverProp += eff.getProb();
                    hpRecover += eff.getX();
                    mpRecoverProp += eff.getProb();
                    mpRecover += eff.getX();
                }
                bx = SkillFactory.getSkill(51110001); // Mihile || Intense Focus
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    localstr += eff.getStrX();
                    // Add Attack Speed here
                }
                bx = SkillFactory.getSkill(51110002); // Mihile || Righteous Indignation
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    ASR += eff.getX();
                    percent_atk += eff.getX();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }

                // Mihile 4th Job Passive Skills
                bx = SkillFactory.getSkill(51120000); // Mihile || Combat Mastery
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    ignoreTargetDEF += eff.getIgnoreMob();
                }
                bx = SkillFactory.getSkill(51120001); // Mihile || Expert Sword Mastery
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    eff = bx.getEffect(bof);
                    watk += bx.getEffect(bof).getX();
                    trueMastery += eff.getMastery();
                    passive_sharpeye_min_percent += eff.getCriticalMin();
                }
                bx = SkillFactory.getSkill(51120003); // Mihile || Soul Asylum
                bof = chra.getTotalSkillLevel(bx);
                if (bof > 0) {
                    percent_wdef += bx.getEffect(bof).getT();
                }
                break;
            }
        }
        if (GameConstants.isResist(chra.getJob())) {
            bx = SkillFactory.getSkill(30000002);
            bof = chra.getTotalSkillLevel(bx);
            if (bof > 0) {
                RecoveryUP += bx.getEffect(bof).getX() - 100;
            }
        }
    }

    private void handleBuffStats(MapleCharacter chra) {
        MapleStatEffect eff = chra.getStatForBuff(MapleBuffStat.MONSTER_RIDING);
        if ((eff != null) && (eff.getSourceId() == 33001001)) {
            this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + eff.getW()));
            this.percent_hp += eff.getZ();
        }
        Integer buff = chra.getBuffedValue(MapleBuffStat.DICE_ROLL);
        if (buff != null) {
            this.percent_wdef += GameConstants.getDiceStat(buff.intValue(), 2);
            this.percent_mdef += GameConstants.getDiceStat(buff.intValue(), 2);
            this.percent_hp += GameConstants.getDiceStat(buff.intValue(), 3);
            this.percent_mp += GameConstants.getDiceStat(buff.intValue(), 3);
            this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + GameConstants.getDiceStat(buff.intValue(), 4)));
            this.dam_r *= (GameConstants.getDiceStat(buff.intValue(), 5) + 100.0D) / 100.0D;
            this.bossdam_r *= (GameConstants.getDiceStat(buff.intValue(), 5) + 100.0D) / 100.0D;
            this.expBuff *= (GameConstants.getDiceStat(buff.intValue(), 6) + 100.0D) / 100.0D;
        }
        buff = chra.getBuffedValue(MapleBuffStat.HP_BOOST_PERCENT);
        if (buff != null) {
            this.percent_hp += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MP_BOOST_PERCENT);
        if (buff != null) {
            this.percent_mp += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DEFENCE_BOOST_R);
        if (buff != null) {
            this.percent_wdef += buff.intValue();
            this.percent_mdef += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ABNORMAL_STATUS_R);
        if (buff != null) {
            this.ASR += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ELEMENTAL_STATUS_R);
        if (buff != null) {
            this.TER += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.INFINITY);
        if (buff != null) {
            this.percent_matk += buff.intValue() - 1;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ONYX_SHROUD);
        if (buff != null) {
            this.dodgeChance += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.PVP_DAMAGE);
        if (buff != null) {
            this.pvpDamage += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.PVP_ATTACK);
        if (buff != null) {
            this.pvpDamage += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.FELINE_BERSERK);
        if (buff != null) {
            this.percent_hp += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.BLUE_AURA);
        if (eff != null) {
            this.percent_wdef += eff.getZ() + eff.getY();
            this.percent_mdef += eff.getZ() + eff.getY();
        }
        buff = chra.getBuffedValue(MapleBuffStat.CONVERSION);
        if (buff != null) {
            this.percent_hp += buff.intValue();
        } else {
            buff = chra.getBuffedValue(MapleBuffStat.MAXHP);
            if (buff != null) {
                this.percent_hp += buff.intValue();
            }
        }
        buff = chra.getBuffedValue(MapleBuffStat.MAXMP);
        if (buff != null) {
            this.percent_mp += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MP_BUFF);
        if (buff != null) {
            this.percent_mp += buff.intValue();
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.BUFF_MASTERY);
        if (buff != null) {
            this.BuffUP_Skill += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.STR);
        if (buff != null) {
            this.localstr += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DEX);
        if (buff != null) {
            this.localdex += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.INT);
        if (buff != null) {
            this.localint_ += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.LUK);
        if (buff != null) {
            this.localluk += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ANGEL_STAT);
        if (buff != null) {
            this.localstr += buff.intValue();
            this.localdex += buff.intValue();
            this.localint_ += buff.intValue();
            this.localluk += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_WDEF);
        if (buff != null) {
            this.wdef += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_MDEF);
        if (buff != null) {
            this.mdef += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.WDEF);
        if (buff != null) {
            this.wdef += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.WDEF);
        if (buff != null) {
            this.mdef += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MAPLE_WARRIOR);
        if (buff != null) {
            double d = buff.doubleValue() / 100.0D;
            this.localstr = ((int) (this.localstr + d * this.str));
            this.localdex = ((int) (this.localdex + d * this.dex));
            this.localluk = ((int) (this.localluk + d * this.luk));
            this.localint_ = ((int) (this.localint_ + d * this.int_));
        }
        buff = chra.getBuffedValue(MapleBuffStat.ECHO_OF_HERO);
        if (buff != null) {
            double d = buff.doubleValue() / 100.0D;
            this.watk += (int) (this.watk * d);
            this.magic += (int) (this.magic * d);
        }
        buff = chra.getBuffedValue(MapleBuffStat.ARAN_COMBO);
        if (buff != null) {
            this.watk += buff.intValue() / 10;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MESOGUARD);
        if (buff != null) {
            this.mesoGuardMeso += buff.doubleValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.EXPRATE);
        if (buff != null) {
            this.expBuff *= buff.doubleValue() / 100.0D;
        }
        buff = chra.getBuffedValue(MapleBuffStat.DROP_RATE);
        if (buff != null) {
            this.dropBuff *= buff.doubleValue() / 100.0D;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ACASH_RATE);
        if (buff != null) {
            this.cashBuff *= buff.doubleValue() / 100.0D;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MESO_RATE);
        if (buff != null) {
            this.mesoBuff *= buff.doubleValue() / 100.0D;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MESOUP);
        if (buff != null) {
            this.mesoBuff *= buff.doubleValue() / 100.0D;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ACC);
        if (buff != null) {
            this.accuracy += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ANGEL_ACC);
        if (buff != null) {
            this.accuracy += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ANGEL_ATK);
        if (buff != null) {
            this.watk += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.ANGEL_MATK);
        if (buff != null) {
            this.magic += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.WATK);
        if (buff != null) {
            this.watk += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.SPIRIT_SURGE);
        if (buff != null) {
            this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + buff.intValue()));
            this.dam_r *= (buff.intValue() + 100.0D) / 100.0D;
            this.bossdam_r *= (buff.intValue() + 100.0D) / 100.0D;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ENHANCED_WATK);
        if (buff != null) {
            this.watk += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.ENERGY_CHARGE);
        if (eff != null) {
            this.watk += eff.getWatk();
            this.accuracy += eff.getAcc();
        }
        buff = chra.getBuffedValue(MapleBuffStat.MATK);
        if (buff != null) {
            this.magic += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.SPEED);
        if (buff != null) {
            this.speed += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.JUMP);
        if (buff != null) {
            this.jump += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DASH_SPEED);
        if (buff != null) {
            this.speed += buff.intValue();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DASH_JUMP);
        if (buff != null) {
            this.jump += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.HIDDEN_POTENTIAL);
        if (eff != null) {
            this.passive_sharpeye_rate = 100;
            this.ASR = 100;

            this.wdef += eff.getX();
            this.mdef += eff.getX();
            this.watk += eff.getX();
            this.magic += eff.getX();
        }
        buff = chra.getBuffedValue(MapleBuffStat.DAMAGE_BUFF);
        if (buff != null) {
            this.dam_r *= (buff.doubleValue() + 100.0D) / 100.0D;
            this.bossdam_r *= (buff.doubleValue() + 100.0D) / 100.0D;
        }
        buff = chra.getBuffedSkill_Y(MapleBuffStat.FINAL_CUT);
        if (buff != null) {
            this.dam_r *= buff.doubleValue() / 100.0D;
            this.bossdam_r *= buff.doubleValue() / 100.0D;
        }
        buff = chra.getBuffedSkill_Y(MapleBuffStat.OWL_SPIRIT);
        if (buff != null) {
            this.dam_r *= buff.doubleValue() / 100.0D;
            this.bossdam_r *= buff.doubleValue() / 100.0D;
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.BERSERK_FURY);
        if (buff != null) {
            this.dam_r *= buff.doubleValue() / 100.0D;
            this.bossdam_r *= buff.doubleValue() / 100.0D;
        }
        eff = chra.getStatForBuff(MapleBuffStat.BLESS);
        if (eff != null) {
            this.watk += eff.getX();
            this.magic += eff.getY();
            this.accuracy += eff.getV();
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.CONCENTRATE);
        if (buff != null) {
            this.mpconReduce += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.HOLY_SHIELD);
        if (eff != null) {
            this.watk += eff.getX();
            this.magic += eff.getY();
            this.accuracy += eff.getV();
            this.mpconReduce += eff.getMPConReduce();
        }
        eff = chra.getStatForBuff(MapleBuffStat.MAGIC_RESISTANCE);
        if (eff != null) {
            this.ASR += eff.getX();
        }

        eff = chra.getStatForBuff(MapleBuffStat.COMBO);
        buff = chra.getBuffedValue(MapleBuffStat.COMBO);
        if ((eff != null) && (buff != null)) {
            this.dam_r *= (100.0D + (eff.getV() + eff.getDAMRate()) * (buff.intValue() - 1)) / 100.0D;
            this.bossdam_r *= (100.0D + (eff.getV() + eff.getDAMRate()) * (buff.intValue() - 1)) / 100.0D;
        }
        eff = chra.getStatForBuff(MapleBuffStat.SUMMON);
        if ((eff != null)
                && (eff.getSourceId() == 35121010)) {
            this.dam_r *= (eff.getX() + 100.0D) / 100.0D;
            this.bossdam_r *= (eff.getX() + 100.0D) / 100.0D;
        }

        eff = chra.getStatForBuff(MapleBuffStat.DARK_AURA);
        if (eff != null) {
            this.dam_r *= (eff.getX() + 100.0D) / 100.0D;
            this.bossdam_r *= (eff.getX() + 100.0D) / 100.0D;
        }
        eff = chra.getStatForBuff(MapleBuffStat.BODY_BOOST);
        if (eff != null) {
            this.dam_r *= (eff.getV() + 100.0D) / 100.0D;
            this.bossdam_r *= (eff.getV() + 100.0D) / 100.0D;
        }
        eff = chra.getStatForBuff(MapleBuffStat.BEHOLDER);
        if (eff != null) {
            this.trueMastery += eff.getMastery();
        }
        eff = chra.getStatForBuff(MapleBuffStat.MECH_CHANGE);
        if (eff != null) {
            this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + eff.getCr()));
        }
        eff = chra.getStatForBuff(MapleBuffStat.PYRAMID_PQ);
        if ((eff != null) && (eff.getBerserk() > 0)) {
            this.dam_r *= eff.getBerserk() / 100.0D;
            this.bossdam_r *= eff.getBerserk() / 100.0D;
        }
        eff = chra.getStatForBuff(MapleBuffStat.WK_CHARGE);
        if (eff != null) {
            this.dam_r *= eff.getDamage() / 100.0D;
            this.bossdam_r *= eff.getDamage() / 100.0D;
        }
        eff = chra.getStatForBuff(MapleBuffStat.PICKPOCKET);
        if (eff != null) {
            this.pickRate = eff.getProb();
        }
        eff = chra.getStatForBuff(MapleBuffStat.PIRATES_REVENGE);
        if (eff != null) {
            this.dam_r *= (eff.getDAMRate() + 100.0D) / 100.0D;
            this.bossdam_r *= (eff.getDAMRate() + 100.0D) / 100.0D;
        }
        eff = chra.getStatForBuff(MapleBuffStat.LIGHTNING_CHARGE);
        if (eff != null) {
            this.dam_r *= eff.getDamage() / 100.0D;
            this.bossdam_r *= eff.getDamage() / 100.0D;
        }
        eff = chra.getStatForBuff(MapleBuffStat.WIND_WALK);
        if (eff != null) {
            this.dam_r *= eff.getDamage() / 100.0D;
            this.bossdam_r *= eff.getDamage() / 100.0D;
        }
        eff = chra.getStatForBuff(MapleBuffStat.DIVINE_SHIELD);
        if (eff != null) {
            this.watk += eff.getEnhancedWatk();
        }
        buff = chra.getBuffedSkill_Y(MapleBuffStat.DARKSIGHT);
        if (buff != null) {
            this.dam_r *= (buff.intValue() + 100.0D) / 100.0D;
            this.bossdam_r *= (buff.intValue() + 100.0D) / 100.0D;
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.ENRAGE);
        if (buff != null) {
            this.dam_r *= (buff.intValue() + 100.0D) / 100.0D;
            this.bossdam_r *= (buff.intValue() + 100.0D) / 100.0D;
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.COMBAT_ORDERS);
        if (buff != null) {
            this.combatOrders += buff.intValue();
        }
        eff = chra.getStatForBuff(MapleBuffStat.SHARP_EYES);
        if (eff != null) {
            this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + eff.getX()));
            this.passive_sharpeye_percent = ((short) (this.passive_sharpeye_percent + eff.getCriticalMax()));
        }
        buff = chra.getBuffedValue(MapleBuffStat.CRITICAL_RATE_BUFF);
        if (buff != null) {
            this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + buff.intValue()));
        }
        if (this.speed > 140) {
            this.speed = 140;
        }
        if (this.jump > 123) {
            this.jump = 123;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MONSTER_RIDING);
        if (buff != null) {
            this.jump = 120;
            switch (buff.intValue()) {
                case 1:
                    this.speed = 150;
                    break;
                case 2:
                    this.speed = 170;
                    break;
                case 3:
                    this.speed = 180;
                    break;
                default:
                    this.speed = 200;
            }
        }
    }

    public boolean checkEquipLevels(MapleCharacter chr, int gain) {
        if (chr.isClone()) {
            return false;
        }
        boolean changed = false;
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        List<Equip> all = new ArrayList(this.equipLevelHandling);
        for (Equip eq : all) {
            int lvlz = eq.getEquipLevel();
            eq.setItemEXP(eq.getItemEXP() + gain);

            if (eq.getEquipLevel() > lvlz) {
                Iterator i$;
                for (int i = eq.getEquipLevel() - lvlz; i > 0; i--) {
                    Map inc = ii.getEquipIncrements(eq.getItemId());
                    if ((inc != null) && (inc.containsKey(Integer.valueOf(lvlz + i)))) {
                        eq = ii.levelUpEquip(eq, (Map) inc.get(Integer.valueOf(lvlz + i)));
                    }

                    if ((GameConstants.getStatFromWeapon(eq.getItemId()) == null) && (GameConstants.getMaxLevel(eq.getItemId()) < lvlz + i) && (Math.random() < 0.1D) && (eq.getIncSkill() <= 0) && (ii.getEquipSkills(eq.getItemId()) != null)) {
                        for (i$ = ii.getEquipSkills(eq.getItemId()).iterator(); i$.hasNext();) {
                            int zzz = ((Integer) i$.next()).intValue();
                            Skill skil = SkillFactory.getSkill(zzz);
                            if ((skil != null) && (skil.canBeLearnedBy(chr.getJob()))) {
                                eq.setIncSkill(skil.getId());
                                chr.dropMessage(5, "Your skill has gained a levelup: " + skil.getName() + " +1");
                            }
                        }
                    }
                }
                changed = true;
            }
            chr.forceReAddItem(eq.copy(), MapleInventoryType.EQUIPPED);
        }
        if (changed) {
            chr.equipChanged();
            chr.getClient().getSession().write(CField.EffectPacket.showItemLevelupEffect());
            chr.getMap().broadcastMessage(chr, CField.EffectPacket.showForeignItemLevelupEffect(chr.getId()), false);
        }
        return changed;
    }

    public boolean checkEquipDurabilitys(MapleCharacter chr, int gain) {
        return checkEquipDurabilitys(chr, gain, false);
    }

    public boolean checkEquipDurabilitys(MapleCharacter chr, int gain, boolean aboveZero) {
        if ((chr.isClone()) || (chr.inPVP())) {
            return true;
        }
        List<Equip> all = new ArrayList(this.durabilityHandling);
        for (Equip item : all) {
            if (item != null) {
                if (item.getPosition() >= 0 == aboveZero) {
                    item.setDurability(item.getDurability() + gain);
                    if (item.getDurability() < 0) {
                        item.setDurability(0);
                    }
                }
            }
        }
        for (Equip eqq : all) {
            if ((eqq != null) && (eqq.getDurability() == 0) && (eqq.getPosition() < 0)) {
                if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                    chr.getClient().getSession().write(CWvsContext.InventoryPacket.getInventoryFull());
                    chr.getClient().getSession().write(CWvsContext.InventoryPacket.getShowInventoryFull());
                    return false;
                }
                this.durabilityHandling.remove(eqq);
                short pos = chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
                MapleInventoryManipulator.unequip(chr.getClient(), eqq.getPosition(), pos);
            } else if (eqq != null) {
                chr.forceReAddItem(eqq.copy(), MapleInventoryType.EQUIPPED);
            }
        }
        return true;
    }

    private void CalcPassive_SharpEye(MapleCharacter player) {
        if (GameConstants.isDemon(player.getJob())) {
            Skill critSkill = SkillFactory.getSkill(30010022);
            int critlevel = player.getTotalSkillLevel(critSkill);
            if (critlevel > 0) {
                this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + critSkill.getEffect(critlevel).getProb()));
                this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
            }
        } else if (GameConstants.isMercedes(player.getJob())) {
            Skill critSkill = SkillFactory.getSkill(20020022);
            int critlevel = player.getTotalSkillLevel(critSkill);
            if (critlevel > 0) {
                this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + critSkill.getEffect(critlevel).getProb()));
                this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
            }
        } else if (GameConstants.isResist(player.getJob())) {
            Skill critSkill = SkillFactory.getSkill(30000022);
            int critlevel = player.getTotalSkillLevel(critSkill);
            if (critlevel > 0) {
                this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + critSkill.getEffect(critlevel).getProb()));
                this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
            }
        }
        Skill critSkill;
        int critlevel;
        switch (player.getJob()) {
            case 410:
            case 411:
            case 412:
                critSkill = SkillFactory.getSkill(4100001);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getProb()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                }
                break;
            case 2412:
                critSkill = SkillFactory.getSkill(24120006);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                    this.watk += critSkill.getEffect(critlevel).getAttackX();
                    return;
                }
                break;
            case 1410:
            case 1411:
            case 1412:
                critSkill = SkillFactory.getSkill(14100001);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getProb()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                    return;
                }
                break;
            case 3100:
            case 3110:
            case 3111:
            case 3112:
                critSkill = SkillFactory.getSkill(31100006);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCr()));
                    this.watk += critSkill.getEffect(critlevel).getAttackX();
                    return;
                }
                break;
            case 2300:
            case 2310:
            case 2311:
            case 2312:
                critSkill = SkillFactory.getSkill(23000003);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCr()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                    return;
                }
                break;
            case 3210:
            case 3211:
            case 3212:
                critSkill = SkillFactory.getSkill(32100006);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCr()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                    return;
                }
                break;
            case 434:
                critSkill = SkillFactory.getSkill(4340010);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getProb()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                    return;
                }
                break;
            case 520:
            case 521:
            case 522:
                critSkill = SkillFactory.getSkill(5200007);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCr()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                    return;
                }
                break;
            case 1211:
            case 1212:
                critSkill = SkillFactory.getSkill(12110000);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCr()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                    return;
                }
                break;
            case 530:
            case 531:
            case 532:
                critSkill = SkillFactory.getSkill(5300004);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getCr()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                    return;
                }
                break;
            case 510:
            case 511:
            case 512:
                critSkill = SkillFactory.getSkill(5110000);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getProb()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                }

                return;
            case 1511:
            case 1512:
                critSkill = SkillFactory.getSkill(15110000);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getProb()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                    return;
                }
                break;
            case 2111:
            case 2112:
                critSkill = SkillFactory.getSkill(21110000);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) (critSkill.getEffect(critlevel).getX() * critSkill.getEffect(critlevel).getY() + critSkill.getEffect(critlevel).getCr())));
                    return;
                }
                break;
            case 300:
            case 310:
            case 311:
            case 312:
            case 320:
            case 321:
            case 322:
                critSkill = SkillFactory.getSkill(3000001);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getProb()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                    return;
                }
                break;
            case 1300:
            case 1310:
            case 1311:
            case 1312:
                critSkill = SkillFactory.getSkill(13000000);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getProb()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                    return;
                }
                break;
            case 2214:
            case 2215:
            case 2216:
            case 2217:
            case 2218:
                critSkill = SkillFactory.getSkill(22140000);
                critlevel = player.getTotalSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + (short) critSkill.getEffect(critlevel).getProb()));
                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + critSkill.getEffect(critlevel).getCriticalMin()));
                    return;
                }
                break;
        }
    }

    private void CalcPassive_Mastery(MapleCharacter player) {
        if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11) == null) {
            this.passive_mastery = 0;
            return;
        }

        MapleWeaponType weaponType = GameConstants.getWeaponType(player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11).getItemId());
        boolean acc = true;
        int skil;
        switch (weaponType) {
            case BOW:
                skil = GameConstants.isKOC(player.getJob()) ? 13100000 : 3100000;
                break;
            case CLAW:
                skil = 4100000;
                break;
            case CANE:
                skil = player.getTotalSkillLevel(24120006) > 0 ? 24120006 : 24100004;
                break;
            case CANNON:
                skil = 5300005;
                break;
            case KATARA:
            case DAGGER:
                skil = player.getJob() >= 430 && player.getJob() <= 434 ? 4300000 : 4200000;
                break;
            case CROSSBOW:
                skil = GameConstants.isResist(player.getJob()) ? 33100000 : 3200000;
                break;
            case AXE1H:
            case BLUNT1H:
                skil = GameConstants.isMihile(player.getJob()) ? 51100001 : (GameConstants.isResist(player.getJob()) ? 31100004 : (GameConstants.isKOC(player.getJob()) ? 11100000 : (player.getJob() > 112 ? 1200000 : 1100000))); // Mihile , DemonSlayer Dawn Warrior, Hero, Pally || Weapon Mastery
                break;
            case AXE2H:
            case SWORD1H:
            case SWORD2H:
            case BLUNT2H:
                skil = GameConstants.isMihile(player.getJob()) ? 51100001 : (GameConstants.isKOC(player.getJob()) ? 11100000 : (player.getJob() > 112 ? 1200000 : 1100000)); // Mihile , Dawn Warrior, Hero, Pally || Weapon Mastery
                break;
            case POLE_ARM:
                skil = GameConstants.isAran(player.getJob()) ? 21100000 : 1300000;
                break;
            case SPEAR:
                skil = 1300000;
                break;
            case KNUCKLE:
                skil = GameConstants.isKOC(player.getJob()) ? 15100001 : 5100001;
                break;
            case GUN:
                skil = GameConstants.isResist(player.getJob()) ? 35100000 : (GameConstants.isJett(player.getJob()) ? 5700000 : 5200000);
                break;
            case DUAL_BOW:
                skil = 23100005;
                break;
            case WAND:
            case STAFF:
                acc = false;
                skil = GameConstants.isResist(player.getJob()) ? 32100006 : (player.getJob() <= 212 ? 2100006 : (player.getJob() <= 222 ? 2200006 : (player.getJob() <= 232 ? 2300006 : (player.getJob() <= 2000 ? 12100007 : 22120002))));
                break;
            default:
                passive_mastery = 0;
                return;

        }

        if (player.getSkillLevel(skil) <= 0) {
            this.passive_mastery = 0;
            return;
        }
        MapleStatEffect eff = SkillFactory.getSkill(skil).getEffect(player.getTotalSkillLevel(skil));
        if (acc) {
            this.accuracy += eff.getX();
            if (skil == 35100000) {
                this.watk += eff.getX();
            }
        } else {
            this.magic += eff.getX();
        }
        this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + eff.getCr()));
        this.passive_mastery = ((byte) eff.getMastery());
        this.trueMastery += eff.getMastery() + weaponType.getBaseMastery();
        if (player.getJob() == 412) {
            Skill bx = SkillFactory.getSkill(4120012);
            int bof = player.getTotalSkillLevel(bx);
            if (bof > 0) {
                MapleStatEffect eff2 = bx.getEffect(bof);
                this.passive_mastery = ((byte) eff2.getMastery());
                this.accuracy += eff2.getPercentAcc();
                this.dodgeChance += eff2.getPercentAvoid();
                this.watk += eff2.getX();
                this.trueMastery -= eff.getMastery();
                this.trueMastery += eff2.getMastery();
            }
        }
    }

    private void calculateFame(MapleCharacter player) {
        player.getTrait(MapleTrait.MapleTraitType.charm).addLocalExp(player.getFame());
        for (MapleTrait.MapleTraitType t : MapleTrait.MapleTraitType.values()) {
            player.getTrait(t).recalcLevel();
        }
    }

    public final short passive_sharpeye_min_percent() {
        return this.passive_sharpeye_min_percent;
    }

    public final short passive_sharpeye_percent() {
        return this.passive_sharpeye_percent;
    }

    public final short passive_sharpeye_rate() {
        return this.passive_sharpeye_rate;
    }

    public final byte passive_mastery() {
        return this.passive_mastery;
    }

    public final void calculateMaxBaseDamage(int watk, int pvpDamage, MapleCharacter chra) {
        if (watk <= 0) {
            this.localmaxbasedamage = 1.0F;
            this.localmaxbasepvpdamage = 1.0F;
        } else {
            Item weapon_item = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11);
            Item weapon_item2 = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -10);
            int job = chra.getJob();
            MapleWeaponType weapon = weapon_item == null ? MapleWeaponType.NOT_A_WEAPON : GameConstants.getWeaponType(weapon_item.getItemId());
            MapleWeaponType weapon2 = weapon_item2 == null ? MapleWeaponType.NOT_A_WEAPON : GameConstants.getWeaponType(weapon_item2.getItemId());

            boolean mage = ((job >= 200) && (job <= 232)) || ((job >= 1200) && (job <= 1212)) || ((job >= 2200) && (job <= 2218)) || ((job >= 3200) && (job <= 3212));
            int mainstat;
            int secondarystat;
            int mainstatpvp;
            int secondarystatpvp;
            switch (weapon) {
                case BOW:
                case CROSSBOW:
                case GUN:
                    mainstat = localdex;
                    secondarystat = localstr;
                    mainstatpvp = dex;
                    secondarystatpvp = str;
                    break;
                case DAGGER:
                case KATARA:
                case CLAW:
                case CANE:
                    mainstat = localluk;
                    secondarystat = localdex + localstr;
                    mainstatpvp = luk;
                    secondarystatpvp = dex + str;
                    break;
                default:
                    if (mage) {
                        mainstat = localint_;
                        secondarystat = localluk;
                        mainstatpvp = int_;
                        secondarystatpvp = luk;
                    } else {
                        mainstat = localstr;
                        secondarystat = localdex;
                        mainstatpvp = str;
                        secondarystatpvp = dex;
                    }
                    break;
            }
            this.localmaxbasepvpdamage = (weapon.getMaxDamageMultiplier() * (4 * mainstatpvp + secondarystatpvp) * (100.0F + pvpDamage / 100.0F));
            this.localmaxbasepvpdamageL = (weapon.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * (100.0F + pvpDamage / 100.0F));
            if ((weapon2 != MapleWeaponType.NOT_A_WEAPON) && (weapon_item != null) && (weapon_item2 != null)) {
                Equip we1 = (Equip) weapon_item;
                Equip we2 = (Equip) weapon_item2;
                this.localmaxbasedamage = (weapon.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * ((watk - (mage ? we2.getMatk() : we2.getWatk())) / 100.0F));
                this.localmaxbasedamage += weapon2.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * ((watk - (mage ? we1.getMatk() : we1.getWatk())) / 100.0F);
            } else {
                this.localmaxbasedamage = (weapon.getMaxDamageMultiplier() * (4 * mainstat + secondarystat) * (watk / 100.0F));
            }
        }
    }

    public final float getHealHP() {
        return this.shouldHealHP;
    }

    public final float getHealMP() {
        return this.shouldHealMP;
    }

    public final void relocHeal(MapleCharacter chra) {
        if (chra.isClone()) {
            return;
        }
        int playerjob = chra.getJob();

        this.shouldHealHP = (10 + this.recoverHP);
        this.shouldHealMP = (GameConstants.isDemon(chra.getJob()) ? 0.0F : 3 + this.recoverMP + this.localint_ / 10);
        this.mpRecoverTime = 0;
        this.hpRecoverTime = 0;
        if ((playerjob == 111) || (playerjob == 112)) {
            Skill effect = SkillFactory.getSkill(1110000);
            int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                MapleStatEffect eff = effect.getEffect(lvl);
                if (eff.getHp() > 0) {
                    this.shouldHealHP += eff.getHp();
                    this.hpRecoverTime = 4000;
                }
                this.shouldHealMP += eff.getMp();
                this.mpRecoverTime = 4000;
            }
        } else if ((playerjob == 1111) || (playerjob == 1112)) {
            Skill effect = SkillFactory.getSkill(11110000);
            int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                this.shouldHealMP += effect.getEffect(lvl).getMp();
                this.mpRecoverTime = 4000;
            }
        } else if (GameConstants.isMercedes(playerjob)) {
            Skill effect = SkillFactory.getSkill(20020109);
            int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                this.shouldHealHP += effect.getEffect(lvl).getX() * this.localmaxhp / 100;
                this.hpRecoverTime = 4000;
                this.shouldHealMP += effect.getEffect(lvl).getX() * this.localmaxmp / 100;
                this.mpRecoverTime = 4000;
            }
        } else if ((GameConstants.isJett(playerjob)) && (playerjob != 508)) {
            Skill effect = SkillFactory.getSkill(5700005);
            int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                MapleStatEffect eff = effect.getEffect(lvl);
                this.shouldHealHP += eff.getX();
                this.shouldHealMP += eff.getX();
                this.hpRecoverTime = eff.getY();
                this.mpRecoverTime = eff.getY();
            }
        } else if ((playerjob == 3111) || (playerjob == 3112)) {
            Skill effect = SkillFactory.getSkill(31110009);
            int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                this.shouldHealMP += effect.getEffect(lvl).getY();
                this.mpRecoverTime = 4000;
            }
        }
        if (chra.getChair() != 0) {
            this.shouldHealHP += 99.0F;
            this.shouldHealMP += 99.0F;
        } else if (chra.getMap() != null) {
            float recvRate = chra.getMap().getRecoveryRate();
            if (recvRate > 0.0F) {
                this.shouldHealHP *= recvRate;
                this.shouldHealMP *= recvRate;
            }
        }
    }

    public final void connectData(MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort(this.str);
        mplew.writeShort(this.dex);
        mplew.writeShort(this.int_);
        mplew.writeShort(this.luk);
        mplew.writeInt(this.hp);
        mplew.writeInt(this.maxhp);
        mplew.writeInt(this.mp);
        mplew.writeInt(this.maxmp);
    }

    public static int getSkillByJob(int skillID, int job) {
        if (GameConstants.isKOC(job)) {
            return skillID + 10000000;
        }
        if (GameConstants.isAran(job)) {
            return skillID + 20000000;
        }
        if (GameConstants.isEvan(job)) {
            return skillID + 20010000;
        }
        if (GameConstants.isMercedes(job)) {
            return skillID + 20020000;
        }
        if (GameConstants.isPhantom(job)) {
            return skillID + 20030000;
        }
        if (GameConstants.isluminous(job)) {
            return skillID + 20040000;
        }
        if (GameConstants.hayato(job)) {
            return skillID + 40010000;
        }
        if (GameConstants.kanna(job)) {
            return skillID + 40020000;
        }
        if (GameConstants.kaiser(job)) {
            return skillID + 60000000;
        }
        if (GameConstants.angelic(job)) {
            return skillID + 60010000;
        }
        if (GameConstants.xenon(job)) {
            return skillID + 30020000;
        }
        if ((GameConstants.isDemon(job)) || (GameConstants.demonAvenger(job))) {
            return skillID + 30010000;
        }
        if (GameConstants.isResist(job)) {
            return skillID + 30000000;
        }

        return skillID;
    }

    public final int getSkillIncrement(int skillID) {
        if (this.skillsIncrement.containsKey(Integer.valueOf(skillID))) {
            return ((Integer) this.skillsIncrement.get(Integer.valueOf(skillID))).intValue();
        }
        return 0;
    }

    public final int getElementBoost(Element key) {
        if (this.elemBoosts.containsKey(key)) {
            return ((Integer) this.elemBoosts.get(key)).intValue();
        }
        return 0;
    }

    public final int getDamageIncrease(int key) {
        if (this.damageIncrease.containsKey(Integer.valueOf(key))) {
            return ((Integer) this.damageIncrease.get(Integer.valueOf(key))).intValue() + this.damX;
        }
        return this.damX;
    }

    public final int getAccuracy() {
        return this.accuracy;
    }

    public void heal_noUpdate(MapleCharacter chra) {
        setHp(getCurrentMaxHp(), chra);
        setMp(getCurrentMaxMp(chra.getJob()), chra);
    }

    public void heal(MapleCharacter chra) {
        heal_noUpdate(chra);
        chra.updateSingleStat(MapleStat.HP, getCurrentMaxHp());
        chra.updateSingleStat(MapleStat.MP, getCurrentMaxMp(chra.getJob()));
    }

    public Pair<Integer, Integer> handleEquipAdditions(MapleItemInformationProvider ii, MapleCharacter chra, boolean first_login, Map<Skill, SkillEntry> sData, int itemId) {


        List<Triple<String, String, String>> additions = ii.getEquipAdditions(itemId);
        if (additions == null) {
            return null;
        }
        int localmaxhp_x = 0;
        int localmaxmp_x = 0;
        int skillid = 0;
        int skilllevel = 0;

        for (Triple add : additions) {
            if (!((String) add.getMid()).contains("con")) {

                int right = 0;
                try {
                    right = Integer.parseInt((String) add.getRight());
                } catch (NumberFormatException fff) {
                }
                String craft;
                String job;
                String level;
                switch ((String) add.getLeft()) {
                    case "elemboost":
                        craft = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "craft");
                        if ((((String) add.getMid()).equals("elemVol")) && ((craft == null) || ((craft != null) && (chra.getTrait(MapleTrait.MapleTraitType.craft).getLocalTotalExp() >= Integer.parseInt(craft))))) {
                            int value = Integer.parseInt(((String) add.getRight()).substring(1, ((String) add.getRight()).length()));
                            Element key = Element.getFromChar(((String) add.getRight()).charAt(0));
                            if (this.elemBoosts.get(key) != null) {
                                value += ((Integer) this.elemBoosts.get(key)).intValue();
                            }
                            this.elemBoosts.put(key, Integer.valueOf(value));
                        }
                        break;
                    case "mobcategory":
                        if (((String) add.getMid()).equals("damage")) {
                            this.dam_r *= (right + 100.0D) / 100.0D;
                            this.bossdam_r += (right + 100.0D) / 100.0D;
                        }
                        break;
                    case "critical":
                        boolean canJob = false;
                        boolean canLevel = false;
                        job = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "job");
                        if (job != null) {
                            if (job.contains(",")) {
                                String[] strx = job.split(","); //???
                                for (String x : strx) {
                                    if (chra.getJob() == Integer.parseInt(x)) {
                                        canJob = true;
                                    }
                                }
                            } else if (chra.getJob() == Integer.parseInt(job)) {
                                canJob = true;
                            }
                        }

                        level = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "level");
                        if ((level != null)
                                && (chra.getLevel() >= Integer.parseInt(level))) {
                            canLevel = true;
                        }

                        if (((job != null) && (canJob)) || ((job == null) && (((level != null) && (canLevel)) || (level == null)))) {
                            switch ((String) add.getMid()) {
                                case "prob":
                                    this.passive_sharpeye_rate = ((short) (this.passive_sharpeye_rate + right));
                                    break;
                                case "damage":
                                    this.passive_sharpeye_min_percent = ((short) (this.passive_sharpeye_min_percent + right));
                                    this.passive_sharpeye_percent = ((short) (this.passive_sharpeye_percent + right));
                            }
                        }
                        break;
                    case "boss":
                        craft = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "craft");
                        if ((((String) add.getMid()).equals("damage")) && ((craft == null) || ((craft != null) && (chra.getTrait(MapleTrait.MapleTraitType.craft).getLocalTotalExp() >= Integer.parseInt(craft))))) {
                            this.bossdam_r *= (right + 100.0D) / 100.0D;
                        }
                        break;
                    case "mobdie":
                        craft = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "craft");
                        if ((craft == null) || ((craft != null) && (chra.getTrait(MapleTrait.MapleTraitType.craft).getLocalTotalExp() >= Integer.parseInt(craft)))) {
                            switch ((String) add.getMid()) {
                                case "hpIncOnMobDie":
                                    this.hpRecover += right;
                                    this.hpRecoverProp += 5;
                                    break;
                                case "mpIncOnMobDie":
                                    this.mpRecover += right;
                                    this.mpRecoverProp += 5;
                            }
                        }
                        break;
                    case "skill":
                        if (first_login) {
                            craft = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "craft");
                            if ((craft == null) || ((craft != null) && (chra.getTrait(MapleTrait.MapleTraitType.craft).getLocalTotalExp() >= Integer.parseInt(craft)))) {
                                switch ((String) add.getMid()) {
                                    case "id":
                                        skillid = right;
                                        break;
                                    case "level":
                                        skilllevel = right;
                                }
                            }
                        }
                        break;
                    case "hpmpchange":
                        switch ((String) add.getMid()) {
                            case "hpChangerPerTime":
                                this.recoverHP += right;
                                break;
                            case "mpChangerPerTime":
                                this.recoverMP += right;
                        }

                        break;
                    case "statinc":
                        boolean canJobx = false;
                        boolean canLevelx = false;
                        job = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "job");
                        if (job != null) {
                            if (job.contains(",")) {
                                String[] jobs = job.split(",");
                                for (String x : jobs) {
                                    if (chra.getJob() == Integer.parseInt(x)) {
                                        canJobx = true;
                                    }
                                }
                            } else if (chra.getJob() == Integer.parseInt(job)) {
                                canJobx = true;
                            }
                        }
                        level = ii.getEquipAddReqs(itemId, (String) add.getLeft(), "level");
                        if ((level != null) && (chra.getLevel() >= Integer.parseInt(level))) {
                            canLevelx = true;
                        }
                        if (((canJobx) || (job == null)) && ((canLevelx) || (level == null))) {
                            if (itemId == 1142367) {
                                int wut = Calendar.getInstance().get(7);
                                if ((wut != 1) && (wut != 7)) {
                                    break;
                                }
                            } else {
                                switch ((String) add.getMid()) {
                                    case "incPAD":
                                        this.watk += right;
                                        break;
                                    case "incMAD":
                                        this.magic += right;
                                        break;
                                    case "incSTR":
                                        this.localstr += right;
                                        break;
                                    case "incDEX":
                                        this.localdex += right;
                                        break;
                                    case "incINT":
                                        this.localint_ += right;
                                        break;
                                    case "incLUK":
                                        this.localluk += right;
                                        break;
                                    case "incJump":
                                        this.jump += right;
                                        break;
                                    case "incMHP":
                                        localmaxhp_x += right;
                                        break;
                                    case "incMMP":
                                        localmaxmp_x += right;
                                        break;
                                    case "incPDD":
                                        this.wdef += right;
                                        break;
                                    case "incMDD":
                                        this.mdef += right;
                                        break;
                                    case "incACC":
                                        this.accuracy += right;
                                        break;
                                    case "incEVA":
                                        break;
                                    case "incSpeed":
                                        this.speed += right;
                                        break;
                                    case "incMMPr":
                                        this.percent_mp += right;
                                }
                            }
                        }
                        break;
                }
            }
        }

        if ((skillid != 0) && (skilllevel != 0)) {
            sData.put(SkillFactory.getSkill(skillid), new SkillEntry((byte) skilllevel, (byte) 0, -1L));
        }

        return new Pair(Integer.valueOf(localmaxhp_x), Integer.valueOf(localmaxmp_x));
    }
        public final int d(int variable){
        return (int) Math.floor(variable);
    }

    public void handleItemOption(StructItemOption soc, MapleCharacter chra, boolean first_login, Map<Skill, SkillEntry> hmm) {
        localstr += soc.get("incSTR");
        localdex += soc.get("incDEX");
        localint_ += soc.get("incINT");
        localluk += soc.get("incLUK");
        accuracy += soc.get("incACC");
        // incEVA -> increase dodge
        speed += soc.get("incSpeed");
        jump += soc.get("incJump");
        watk += soc.get("incPAD");
        magic += soc.get("incMAD");
        wdef += soc.get("incPDD");
        mdef += soc.get("incMDD");
        percent_str += soc.get("incSTRr");
        percent_dex += soc.get("incDEXr");
        percent_int += soc.get("incINTr");
        percent_luk += soc.get("incLUKr");
        percent_hp += soc.get("incMHPr");
        percent_mp += soc.get("incMMPr");
        percent_acc += soc.get("incACCr");
        evaR += soc.get("incEVAr");
        percent_atk += soc.get("incPADr");
        percent_matk += soc.get("incMADr");
        percent_wdef += soc.get("incPDDr");
        percent_mdef += soc.get("incMDDr");
        passive_sharpeye_rate += soc.get("incCr");
        bossdam_r *= (soc.get("incDAMr") + 100.0) / 100.0;
        if (soc.get("boss") <= 0) {
            dam_r *= (soc.get("incDAMr") + 100.0) / 100.0;
        }
        recoverHP += soc.get("RecoveryHP"); // This shouldn't be here, set 4 seconds.
        recoverMP += soc.get("RecoveryMP"); // This shouldn't be here, set 4 seconds.
        if (soc.get("HP") > 0) { // Should be heal upon attacking
            hpRecover += soc.get("HP");
            hpRecoverProp += soc.get("prop");
        }
        if (soc.get("MP") > 0 && !GameConstants.isDemon(chra.getJob())) {
            mpRecover += soc.get("MP");
            mpRecoverProp += soc.get("prop");
        }
        ignoreTargetDEF += ((100 - ignoreTargetDEF) * ((soc.get("ignoreTargetDEF")) / (double)100));
        if (soc.get("ignoreDAM") > 0) {
            ignoreDAM += soc.get("ignoreDAM");
            ignoreDAM_rate += soc.get("prop");
        }
        incAllskill += soc.get("incAllskill");
        if (soc.get("ignoreDAMr") > 0) {
            ignoreDAMr += ((100 - ignoreDAMr) * (soc.get("ignoreDAMr") / (double)100));
            ignoreDAMr_rate += ((100 - ignoreDAMr_rate) * (soc.get("prop") / (double)100));
        }
        RecoveryUP += soc.get("RecoveryUP"); // only for hp items and skills
        passive_sharpeye_min_percent += soc.get("incCriticaldamageMin");
        passive_sharpeye_percent += soc.get("incCriticaldamageMax");
        TER += soc.get("incTerR"); // elemental resistance = avoid element damage from monster
        ASR += soc.get("incAsrR"); // abnormal status = disease
        if (soc.get("DAMreflect") > 0) {
            DAMreflect += soc.get("DAMreflect");
            DAMreflect_rate += soc.get("prop");
        }
        mpconReduce += soc.get("mpconReduce");
        reduceCooltime += soc.get("reduceCooltime"); // in seconds
        incMesoProp += soc.get("incMesoProp"); // mesos + %
        dropBuff *= (100 + soc.get("incRewardProp")) / 100.0; // extra drop rate for item
        if (first_login && soc.get("skillID") > 0) {
            hmm.put(SkillFactory.getSkill(getSkillByJob(soc.get("skillID"), chra.getJob())), new SkillEntry((byte) 1, (byte) 0, -1));
        }
        // TODO: Auto Steal potentials (modify handleSteal), abnormal status duration decrease,
        // poison, stun, etc (uses level field -> cast disease to mob/player), face?
    }

    public final void handleProfessionTool(MapleCharacter chra) {
        if ((chra.getProfessionLevel(92000000) > 0) || (chra.getProfessionLevel(92010000) > 0)) {
            Iterator itera = chra.getInventory(MapleInventoryType.EQUIP).newList().iterator();
            while (itera.hasNext()) {
                Equip equip = (Equip) itera.next();
                if (((equip.getDurability() != 0) && (equip.getItemId() / 10000 == 150) && (chra.getProfessionLevel(92000000) > 0)) || ((equip.getItemId() / 10000 == 151) && (chra.getProfessionLevel(92010000) > 0))) {
                    if (equip.getDurability() > 0) {
                        this.durabilityHandling.add(equip);
                    }
                    this.harvestingTool = equip.getPosition();
                    break;
                }
            }
        }
    }

    public void recalcPVPRank(MapleCharacter chra) {
        this.pvpRank = 10;
        this.pvpExp = chra.getTotalBattleExp();
        for (int i = 0; i < 10; i++) {
            if (this.pvpExp > GameConstants.getPVPExpNeededForLevel(i + 1)) {
                this.pvpRank -= 1;
                this.pvpExp -= GameConstants.getPVPExpNeededForLevel(i + 1);
            }
        }
    }

    public int getHPPercent() {
        return (int) Math.ceil(this.hp * 100.0D / this.localmaxhp);
    }

    public final void init(MapleCharacter chra) {
        recalcLocalStats(chra);
    }

    public final int getStr() {
        return this.str;
    }

    public final int getDex() {
        return this.dex;
    }

    public final int getInt() {
        return this.int_;
    }

    public final int getLuk() {
        return this.luk;
    }

    public final int getHp() {
        return this.hp;
    }

    public final int getMp() {
        return this.mp;
    }

    public final int getMaxHp() {
        return this.maxhp;
    }

    public final int getMaxMp() {
        return this.maxmp;
    }

    public final void setStr(short str, MapleCharacter chra) {
        this.str = str;
        recalcLocalStats(chra);
    }
    
    public final void setHP(short HP, MapleCharacter chra) {
        this.str = HP;
        recalcLocalStats(chra);
    }
    
        public final void setMP(short MP, MapleCharacter chra) {
        this.str = MP;
        recalcLocalStats(chra);
    }

    public final void setDex(short dex, MapleCharacter chra) {
        this.dex = dex;
        recalcLocalStats(chra);
    }

    public final void setInt(short int_, MapleCharacter chra) {
        this.int_ = int_;
        recalcLocalStats(chra);
    }

    public final void setLuk(short luk, MapleCharacter chra) {
        this.luk = luk;
        recalcLocalStats(chra);
    }

    public final boolean setHp(final int newhp, MapleCharacter chra) {
        if (newhp > 500000) {
            return setHp(500000, false, chra);
        } else {
            return setHp(newhp, false, chra);
        }
    }

    public final boolean setHp(int newhp, boolean silent, MapleCharacter chra) {
        final int oldHp = hp;
        int thp = newhp;
        if (thp < 0) {
            thp = 0;
        }
        if (thp > localmaxhp) {
            thp = localmaxhp;
        }
        this.hp = thp;

        if (chra != null) {
            if (!silent) {
                chra.checkBerserk();
                chra.updatePartyMemberHP();
            }
               if (chra.getJob() == 3122) {
                chra.getClient().getSession().write(CWvsContext.giveDemonWatk(chra));
            }
            if (oldHp > hp && !chra.isAlive()) {
                chra.playerDead();
            }
        }
        if (GameConstants.demonAvenger(chra.getJob())) {
            chra.getClient().getSession().write(JobPacket.AvengerPacket.giveAvengerHpBuff(hp));
        }
        return hp != oldHp;
    }

    public final boolean setMp(int newmp, MapleCharacter chra) {
        int oldMp = this.mp;
        int tmp = newmp;
        if (tmp < 0) {
            tmp = 0;
        }
        if (tmp > this.localmaxmp) {
            tmp = this.localmaxmp;
        }
        this.mp = tmp;
        return this.mp != oldMp;
    }

    public final void setMaxHp(int hp, MapleCharacter chra) {
        if (hp < 0) {
            chra.getClient().getSession().write(CWvsContext.enableActions());
            return;
        }
        this.maxhp = hp;
        recalcLocalStats(chra);

    }

    public final void setMaxMp(int mp, MapleCharacter chra) {
        this.maxmp = mp;
        recalcLocalStats(chra);
    }

    public final void setInfo(int maxhp, int maxmp, int hp, int mp) {
        this.maxhp = maxhp;
        this.maxmp = maxmp;
        this.hp = hp;
        this.mp = mp;
    }

    public final int getTotalStr() {
        return this.localstr;
    }

    public final int getTotalDex() {
        return this.localdex;
    }

    public final int getTotalInt() {
        return this.localint_;
    }

    public final int getTotalLuk() {
        return this.localluk;
    }

    public final int getTotalWatk() {
        return this.watk;
    }

    public final int getTotalMagic() {
        return this.magic;
    }

    public final int getCurrentMaxHp() {
        return this.localmaxhp;
    }

    public final int getCurrentMaxMp(int job) {
        if (GameConstants.isDemon(job)) {
            return GameConstants.getMPByJob(job);
        }
        return this.localmaxmp;
    }

    public final int getHands() {
        return this.hands;
    }

    public final float getCurrentMaxBaseDamage() {
        return this.localmaxbasedamage;
    }

    public final float getCurrentMaxBasePVPDamage() {
        return this.localmaxbasepvpdamage;
    }

    public final float getCurrentMaxBasePVPDamageL() {
        return this.localmaxbasepvpdamageL;
    }

    public final boolean isRangedJob(int job) {
        if ((GameConstants.isJett(job)) || (GameConstants.isMercedes(job)) || (GameConstants.isCannon(job)) || (job == 400) || (job / 10 == 52) || (job / 100 == 3) || (job / 100 == 13) || (job / 100 == 14) || (job / 100 == 33) || (job / 100 == 35) || (job / 10 == 41)) {
            return true;
        }
        return false;
    }
}