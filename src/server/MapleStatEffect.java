package server;

import clientside.MapleBuffStat;
import clientside.MapleCharacter;
import clientside.MapleCoolDownValueHolder;
import clientside.MapleDisease;
import clientside.MapleStat;
import clientside.MapleTrait;
import clientside.PlayerStats;
import clientside.Skill;
import clientside.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import handling.channel.ChannelServer;
import handling.channel.handler.PlayerHandler;
import handling.world.MaplePartyCharacter;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import provider.MapleData;
import provider.MapleDataTool;
import provider.MapleDataType;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleDoor;
import server.maps.MapleExtractor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSummon;
import server.maps.MechDoor;
import server.maps.SummonMovementType;
import tools.CaltechEval;
import tools.FileoutputUtil;
import tools.Pair;
import tools.Triple;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.BuffPacket;
import tools.packet.JobPacket;

public class MapleStatEffect
        implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private Map<MapleStatInfo, Integer> info;
    private Map<MapleTrait.MapleTraitType, Integer> traits;
    private boolean overTime;
    private boolean skill;
    private boolean partyBuff;
    private EnumMap<MapleBuffStat, Integer> statups;
    private ArrayList<Pair<Integer, Integer>> availableMap;
    private EnumMap<MonsterStatus, Integer> monsterStatus;
    private Point lt;
    private Point rb;
    private byte level;
    private List<MapleDisease> cureDebuffs;
    private List<Integer> petsCanConsume;
    private List<Integer> familiars;
    private List<Integer> randomPickup;
    private List<Triple<Integer, Integer, Integer>> rewardItem;
    private byte expR;
    private byte familiarTarget;
    private byte recipeUseCount;
    private byte recipeValidDay;
    private byte reqSkillLevel;
    private byte slotCount;
    private byte effectedOnAlly;
    private byte effectedOnEnemy;
    private byte type;
    private byte preventslip;
    private byte immortal;
    private byte bs;
    private short ignoreMob;
    private short mesoR;
    private short thaw;
    private short fatigueChange;
    private short lifeId;
    private short imhp;
    private short immp;
    private short inflation;
    private short useLevel;
    private short indiePdd;
    private short indieMdd;
    private short incPVPdamage;
    private short mobSkill;
    private short mobSkillLevel;
    private short powerCon;
    private double hpR;
    private double mpR;
    private int sourceid;
    private int recipe;
    private int moveTo;
    private int moneyCon;
    private int morphId;
    private int expinc;
    private int exp;
    private int consumeOnPickup;
    private int charColor;
    private int interval;
    private int rewardMeso;
    private int totalprob;
    private int cosmetic;
    private int expBuff;
    private int itemup;
    private int mesoup;
    private int cashup;
    private int berserk;
    private int illusion;
    private int booster;
    private int berserk2;
    private int cp;
    private int nuffSkill;

    public MapleStatEffect() {
        this.partyBuff = true;

        this.morphId = 0;
    }

    public final boolean isAddedByte() {
        switch (sourceid) {
            case 8000:
            case 10008000:
            case 20008000:
            case 20018000:
            case 30008000:
            case 20028000:
            case 30018000:
            case 30028000:
            case 20038000:
            case 20048000:
            case 50008000:
            case 60008000:
            case 60018000:
            case 4001005:
            case 4101004:
            case 4201003:
            case 4001006:
            case 4301003:
            case 4311001:
            case 13101006:
            case 4330001:
            case 14001003:
            case 14001007:
            case 14101003:
            case 5301003:
            case 5320008:
            case 9001001: // GM haste
            case 1101013: // combo
            case 11111001: // combo

            case 4001003:
            case 1002:
            case 10001002:
            case 20001002:
            case 20011002:
            case 50001002:
            case 33111007:
            case 13111005:
            case 30001001:
            case 30011001:
            case 61121014:
            case 65121009:
            case 35121005:
            case 35111004:
            case 35121013:

            case 1211006: // wk charges
            case 1211003:
            case 1211004:
            case 1211005:
            case 1211007:
            case 1211008:
            case 1210016:
            case 1221003:
            case 1221004:
            case 15101006:
            case 21101006: // �ƶ� - ����� ����

            case 8004:
            case 10008004:
            case 20008004:
            case 20018004:
            case 20028004:
            case 20038004:
            case 30008004:
            case 30018004:
            case 1211011:
            case 27100003:
                //case 60011216:
                //case 5211009:
                return skill;
        }
        return false;
    }

    public static MapleStatEffect loadSkillEffectFromData(MapleData source, int skillid, boolean overtime, int level, String variables) {
        return loadFromData(source, skillid, true, overtime, level, variables);
    }

    public static MapleStatEffect loadItemEffectFromData(MapleData source, int itemid) {
        return loadFromData(source, itemid, false, false, 1, null);
    }

    private static void addBuffStatPairToListIfNotZero(EnumMap<MapleBuffStat, Integer> list, MapleBuffStat buffstat, Integer val) {
        if (val.intValue() != 0) {
            list.put(buffstat, val);
        }
    }

    private static int parseEval(String path, MapleData source, int def, String variables, int level) {
        if (variables == null) {
            return MapleDataTool.getIntConvert(path, source, def);
        }
        MapleData dd = source.getChildByPath(path);
        if (dd == null) {
            return def;
        }
        if (dd.getType() != MapleDataType.STRING) {
            return MapleDataTool.getIntConvert(path, source, def);
        }
        String dddd = MapleDataTool.getString(dd).replace(variables, String.valueOf(level));
        dddd = dddd.replace("y", String.valueOf(level));
        if (dddd.substring(0, 1).equals("-")) {
            if ((dddd.substring(1, 2).equals("u")) || (dddd.substring(1, 2).equals("d"))) {
                dddd = "n(" + dddd.substring(1, dddd.length()) + ")";
            } else {
                dddd = "n" + dddd.substring(1, dddd.length());
            }
        } else if (dddd.substring(0, 1).equals("=")) {
            dddd = dddd.substring(1, dddd.length());
        }
        return (int) new CaltechEval(dddd).evaluate();
    }
    
       public static int parseEval(String data, int level) {
        String variables = "x";
        String dddd = data.replace(variables, String.valueOf(level));
        if (dddd.substring(0, 1).equals("-")) { //-30+3*x
            if (dddd.substring(1, 2).equals("u") || dddd.substring(1, 2).equals("d")) { //-u(x/2)
                dddd = "n(" + dddd.substring(1, dddd.length()) + ")"; //n(u(x/2))
            } else {
                dddd = "n" + dddd.substring(1, dddd.length()); //n30+3*x
            }
        } else if (dddd.substring(0, 1).equals("=")) { //lol nexon and their mistakes
            dddd = dddd.substring(1, dddd.length());
        }
        return (int) (new CaltechEval(dddd).evaluate());
    }
       

    private static MapleStatEffect loadFromData(MapleData source, int sourceid, boolean skill, boolean overTime, int level, String variables) {
        MapleStatEffect ret = new MapleStatEffect();
        ret.sourceid = sourceid;
        ret.skill = skill;
        ret.level = ((byte) level);
        if (source == null) {
            return ret;
        }
        ret.info = new EnumMap(MapleStatInfo.class);
        for (MapleStatInfo i : MapleStatInfo.values()) {
            if (i.isSpecial()) {
                ret.info.put(i, Integer.valueOf(parseEval(i.name().substring(0, i.name().length() - 1), source, i.getDefault(), variables, level)));
            } else {
                ret.info.put(i, Integer.valueOf(parseEval(i.name(), source, i.getDefault(), variables, level)));
            }
        }
        ret.hpR = (parseEval("hpR", source, 0, variables, level) / 100.0D);
        ret.mpR = (parseEval("mpR", source, 0, variables, level) / 100.0D);
        ret.ignoreMob = ((short) parseEval("ignoreMobpdpR", source, 0, variables, level));
        ret.thaw = ((short) parseEval("thaw", source, 0, variables, level));
        ret.interval = parseEval("interval", source, 0, variables, level);
        ret.expinc = parseEval("expinc", source, 0, variables, level);
        ret.powerCon = ((short) parseEval("powerCon", source, 0, variables, level));
        ret.exp = parseEval("exp", source, 0, variables, level);
        ret.morphId = parseEval("morph", source, 0, variables, level);
        ret.cp = parseEval("cp", source, 0, variables, level);
        ret.cosmetic = parseEval("cosmetic", source, 0, variables, level);
        ret.slotCount = ((byte) parseEval("slotCount", source, 0, variables, level));
        ret.preventslip = ((byte) parseEval("preventslip", source, 0, variables, level));
        ret.useLevel = ((short) parseEval("useLevel", source, 0, variables, level));
        ret.nuffSkill = parseEval("nuffSkill", source, 0, variables, level);
        ret.familiarTarget = ((byte) (parseEval("familiarPassiveSkillTarget", source, 0, variables, level) + 1));
        ret.immortal = ((byte) parseEval("immortal", source, 0, variables, level));
        ret.type = ((byte) parseEval("type", source, 0, variables, level));
        ret.bs = ((byte) parseEval("bs", source, 0, variables, level));
        ret.indiePdd = ((short) parseEval("indiePdd", source, 0, variables, level));
        ret.indieMdd = ((short) parseEval("indieMdd", source, 0, variables, level));
        ret.expBuff = parseEval("expBuff", source, 0, variables, level);
        ret.cashup = parseEval("cashBuff", source, 0, variables, level);
        ret.itemup = parseEval("itemupbyitem", source, 0, variables, level);
        ret.mesoup = parseEval("mesoupbyitem", source, 0, variables, level);
        ret.berserk = parseEval("berserk", source, 0, variables, level);
        ret.berserk2 = parseEval("berserk2", source, 0, variables, level);
        ret.booster = parseEval("booster", source, 0, variables, level);
        ret.lifeId = ((short) parseEval("lifeId", source, 0, variables, level));
        ret.inflation = ((short) parseEval("inflation", source, 0, variables, level));
        ret.imhp = ((short) parseEval("imhp", source, 0, variables, level));
        ret.immp = ((short) parseEval("immp", source, 0, variables, level));
        ret.illusion = parseEval("illusion", source, 0, variables, level);
        ret.consumeOnPickup = parseEval("consumeOnPickup", source, 0, variables, level);
        if ((ret.consumeOnPickup == 1)
                && (parseEval("party", source, 0, variables, level) > 0)) {
            ret.consumeOnPickup = 2;
        }

        ret.recipe = parseEval("recipe", source, 0, variables, level);
        ret.recipeUseCount = ((byte) parseEval("recipeUseCount", source, 0, variables, level));
        ret.recipeValidDay = ((byte) parseEval("recipeValidDay", source, 0, variables, level));
        ret.reqSkillLevel = ((byte) parseEval("reqSkillLevel", source, 0, variables, level));
        ret.effectedOnAlly = ((byte) parseEval("effectedOnAlly", source, 0, variables, level));
        ret.effectedOnEnemy = ((byte) parseEval("effectedOnEnemy", source, 0, variables, level));
        ret.incPVPdamage = ((short) parseEval("incPVPDamage", source, 0, variables, level));
        ret.moneyCon = parseEval("moneyCon", source, 0, variables, level);
        ret.moveTo = parseEval("moveTo", source, -1, variables, level);
        ret.charColor = 0;
        String cColor = MapleDataTool.getString("charColor", source, null);
        if (cColor != null) {
            try {
                ret.charColor |= Integer.parseInt("0x" + cColor.substring(0, 2));
                ret.charColor |= Integer.parseInt("0x" + cColor.substring(2, 4) + "00");
                ret.charColor |= Integer.parseInt("0x" + cColor.substring(4, 6) + "0000");
                ret.charColor |= Integer.parseInt("0x" + cColor.substring(6, 8) + "000000");
            } catch (NumberFormatException ddd) {
            }
        }
        ret.traits = new EnumMap(MapleTrait.MapleTraitType.class);
        for (MapleTrait.MapleTraitType t : MapleTrait.MapleTraitType.values()) {
            int expz = parseEval(t.name() + "EXP", source, 0, variables, level);
            if (expz != 0) {
                ret.traits.put(t, Integer.valueOf(expz));
            }
        }
        List cure = new ArrayList(5);
        if (parseEval("poison", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.POISON);
        }
        if (parseEval("seal", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.SEAL);
        }
        if (parseEval("darkness", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.DARKNESS);
        }
        if (parseEval("weakness", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.WEAKEN);
        }
        if (parseEval("curse", source, 0, variables, level) > 0) {
            cure.add(MapleDisease.CURSE);
        }
        ret.cureDebuffs = cure;
        ret.petsCanConsume = new ArrayList();
        for (int i = 0;; i++) {
            int dd = parseEval(String.valueOf(i), source, 0, variables, level);
            if (dd <= 0) {
                break;
            }
            ret.petsCanConsume.add(Integer.valueOf(dd));
        }

        MapleData mdd = source.getChildByPath("0");
        if ((mdd != null) && (mdd.getChildren().size() > 0)) {
            ret.mobSkill = ((short) parseEval("mobSkill", mdd, 0, variables, level));
            ret.mobSkillLevel = ((short) parseEval("level", mdd, 0, variables, level));
        } else {
            ret.mobSkill = 0;
            ret.mobSkillLevel = 0;
        }
        MapleData pd = source.getChildByPath("randomPickup");
        if (pd != null) {
            ret.randomPickup = new ArrayList();
            for (MapleData p : pd) {
                ret.randomPickup.add(Integer.valueOf(MapleDataTool.getInt(p)));
            }
        }
        MapleData ltd = source.getChildByPath("lt");
        if (ltd != null) {
            ret.lt = ((Point) ltd.getData());
            ret.rb = ((Point) source.getChildByPath("rb").getData());
        }
        MapleData ltc = source.getChildByPath("con");
        if (ltc != null) {
            ret.availableMap = new ArrayList();
            for (MapleData ltb : ltc) {
                ret.availableMap.add(new Pair(Integer.valueOf(MapleDataTool.getInt("sMap", ltb, 0)), Integer.valueOf(MapleDataTool.getInt("eMap", ltb, 999999999))));
            }
        }
        MapleData ltb = source.getChildByPath("familiar");
        if (ltb != null) {
            ret.fatigueChange = ((short) (parseEval("incFatigue", ltb, 0, variables, level) - parseEval("decFatigue", ltb, 0, variables, level)));
            ret.familiarTarget = ((byte) parseEval("target", ltb, 0, variables, level));
            MapleData lta = ltb.getChildByPath("targetList");
            if (lta != null) {
                ret.familiars = new ArrayList();
                for (MapleData ltz : lta) {
                    ret.familiars.add(Integer.valueOf(MapleDataTool.getInt(ltz, 0)));
                }
            }
        } else {
            ret.fatigueChange = 0;
        }
        int totalprob = 0;
        MapleData lta = source.getChildByPath("reward");
        if (lta != null) {
            ret.rewardMeso = parseEval("meso", lta, 0, variables, level);
            MapleData ltz = lta.getChildByPath("case");
            if (ltz != null) {
                ret.rewardItem = new ArrayList();
                for (MapleData lty : ltz) {
                    ret.rewardItem.add(new Triple(Integer.valueOf(MapleDataTool.getInt("id", lty, 0)), Integer.valueOf(MapleDataTool.getInt("count", lty, 0)), Integer.valueOf(MapleDataTool.getInt("prop", lty, 0))));
                    totalprob += MapleDataTool.getInt("prob", lty, 0);
                }
            }
        } else {
            ret.rewardMeso = 0;
        }
        ret.totalprob = totalprob;

        if (ret.skill) {
            int priceUnit = ((Integer) ret.info.get(MapleStatInfo.priceUnit)).intValue();
            if (priceUnit > 0) {
                int price = ((Integer) ret.info.get(MapleStatInfo.price)).intValue();
                int extendPrice = ((Integer) ret.info.get(MapleStatInfo.extendPrice)).intValue();
                ret.info.put(MapleStatInfo.price, Integer.valueOf(price * priceUnit));
                ret.info.put(MapleStatInfo.extendPrice, Integer.valueOf(extendPrice * priceUnit));
            }
            switch (sourceid) {
                case 1100002:
                case 1120013:
                case 1200002:
                case 1300002:
                case 2111007:
                case 2211007:
                case 2311007:
                case 3100001:
                case 3120008:
                case 3200001:
                case 11101002:
                case 12111007:
                case 13101002:
                case 22150004:
                case 22161005:
                case 22181004:
                case 23100006:
                case 23120012:
                case 32111010:
                case 33100009:
                case 51121006:
                    ret.info.put(MapleStatInfo.mobCount, Integer.valueOf(6));
                    break;
                case 35111004:
                case 35121005:
                case 35121013:
                    ret.info.put(MapleStatInfo.attackCount, Integer.valueOf(6));
                    ret.info.put(MapleStatInfo.bulletCount, Integer.valueOf(6));
                    break;
                case 24100003:
                case 24120002:
                    ret.info.put(MapleStatInfo.attackCount, Integer.valueOf(15));
            }

            if (GameConstants.isNoDelaySkill(sourceid)) {
                ret.info.put(MapleStatInfo.mobCount, Integer.valueOf(6));
            }
        }
        if ((!ret.skill) && (((Integer) ret.info.get(MapleStatInfo.time)).intValue() > -1)) {
            ret.overTime = true;
        } else {
            ret.info.put(MapleStatInfo.time, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.time)).intValue() * 1000));
            ret.info.put(MapleStatInfo.subTime, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.subTime)).intValue() * 1000));
            ret.overTime = ((overTime) || (ret.isMorph()) || (ret.isPirateMorph()) || (ret.isFinalAttack()) || (ret.isAngel()) || (ret.getSummonMovementType() != null));
        }
        ret.monsterStatus = new EnumMap(MonsterStatus.class);
        ret.statups = new EnumMap(MapleBuffStat.class);
        if ((ret.overTime) && (ret.getSummonMovementType() == null) && (!ret.isEnergyCharge()) && (ret.sourceid != 42101002)) {
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.WATK, (Integer) ret.info.get(MapleStatInfo.pad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.WDEF, (Integer) ret.info.get(MapleStatInfo.pdd));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MATK, (Integer) ret.info.get(MapleStatInfo.mad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MDEF, (Integer) ret.info.get(MapleStatInfo.mdd));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ACC, (Integer) ret.info.get(MapleStatInfo.acc));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.AVOID, (Integer) ret.info.get(MapleStatInfo.eva));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.SPEED, (sourceid == 32120001) || (sourceid == 32101003) ? (Integer) ret.info.get(MapleStatInfo.x) : (Integer) ret.info.get(MapleStatInfo.speed));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.JUMP, (Integer) ret.info.get(MapleStatInfo.jump));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MAXHP, (Integer) ret.info.get(MapleStatInfo.mhpR));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MAXMP, (Integer) ret.info.get(MapleStatInfo.mmpR));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.BOOSTER, Integer.valueOf(ret.booster));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP_LOSS_GUARD, Integer.valueOf(ret.thaw));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.EXPRATE, Integer.valueOf(ret.expBuff));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ACASH_RATE, Integer.valueOf(ret.cashup));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.DROP_RATE, Integer.valueOf(GameConstants.getModifier(ret.sourceid, ret.itemup)));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MESO_RATE, Integer.valueOf(GameConstants.getModifier(ret.sourceid, ret.mesoup)));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.BERSERK_FURY, Integer.valueOf(ret.berserk2));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ILLUSION, Integer.valueOf(ret.illusion));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.PYRAMID_PQ, Integer.valueOf(ret.berserk));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_MAXHP, (Integer) ret.info.get(MapleStatInfo.emhp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_MAXMP, (Integer) ret.info.get(MapleStatInfo.emmp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_WATK, (Integer) ret.info.get(MapleStatInfo.epad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_MATK, (Integer) ret.info.get(MapleStatInfo.emad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_WDEF, (Integer) ret.info.get(MapleStatInfo.epdd));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ENHANCED_MDEF, (Integer) ret.info.get(MapleStatInfo.emdd));
            //addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.GIANT_POTION, Integer.valueOf(ret.inflation));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.STR, (Integer) ret.info.get(MapleStatInfo.str));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.DEX, (Integer) ret.info.get(MapleStatInfo.dex));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.INT, (Integer) ret.info.get(MapleStatInfo.int_));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.LUK, (Integer) ret.info.get(MapleStatInfo.luk));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_ATK, (Integer) ret.info.get(MapleStatInfo.indiePad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_MATK, (Integer) ret.info.get(MapleStatInfo.indieMad));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP_BOOST, Integer.valueOf(ret.imhp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MP_BOOST, Integer.valueOf(ret.immp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.PVP_DAMAGE, Integer.valueOf(ret.incPVPdamage));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP_BOOST_PERCENT, (Integer) ret.info.get(MapleStatInfo.indieMhpR));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MP_BOOST_PERCENT, (Integer) ret.info.get(MapleStatInfo.indieMmpR));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.HP_BOOST, (Integer) ret.info.get(MapleStatInfo.indieMhp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.MP_BOOST, (Integer) ret.info.get(MapleStatInfo.indieMmp));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_JUMP, (Integer) ret.info.get(MapleStatInfo.indieJump));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_SPEED, (Integer) ret.info.get(MapleStatInfo.indieSpeed));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_ACC, (Integer) ret.info.get(MapleStatInfo.indieAcc));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_AVOID, (Integer) ret.info.get(MapleStatInfo.indieEva));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.ANGEL_STAT, (Integer) ret.info.get(MapleStatInfo.indieAllStat));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.PVP_ATTACK, (Integer) ret.info.get(MapleStatInfo.PVPdamage));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.INVINCIBILITY, Integer.valueOf(ret.immortal));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.NO_SLIP, Integer.valueOf(ret.preventslip));
            addBuffStatPairToListIfNotZero(ret.statups, MapleBuffStat.FAMILIAR_SHADOW, Integer.valueOf(ret.charColor > 0 ? 1 : 0));
            if ((sourceid == 5221006) || (ret.isPirateMorph())) {
                ret.statups.put(MapleBuffStat.STANCE, Integer.valueOf(100));
            }
        }
        if (ret.skill) {
            switch (sourceid) {
                case 2001002:
                case 12001001:
                case 22111001:
                case 27000003:
                    ret.statups.put(MapleBuffStat.MAGIC_GUARD, ret.info.get(MapleStatInfo.x));
                    break;
                case 2301003:
                    ret.statups.put(MapleBuffStat.INVINCIBLE, ret.info.get(MapleStatInfo.x));
                    break;
                case 35001002:
                case 35120000:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    break;
                case 9101002:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.DARKSIGHT, ret.info.get(MapleStatInfo.x));
                    break;
                case 13101006:
                    ret.statups.put(MapleBuffStat.WIND_WALK, ret.info.get(MapleStatInfo.x));
                    break;
                case 4330001:
                    ret.statups.put(MapleBuffStat.DARKSIGHT, Integer.valueOf(ret.level));
                    break;
                case 4001003:
                case 14001003:
                case 20031211:
                    ret.statups.put(MapleBuffStat.DARKSIGHT, ret.info.get(MapleStatInfo.x));
                    break;
               case 9101004:
               case 9001004: // hide
                    ret.info.put(MapleStatInfo.time, Integer.MAX_VALUE);
                    ret.statups.put(MapleBuffStat.DARKSIGHT, ret.info.get(MapleStatInfo.x));
                    break;
                case 4211003:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.PICKPOCKET, ret.info.get(MapleStatInfo.x));
                    break;
                case 4201011:
                case 4211005:
                    ret.statups.put(MapleBuffStat.MESOGUARD, ret.info.get(MapleStatInfo.x));
                    break;
                case 4111001:
                    ret.statups.put(MapleBuffStat.MESOUP, ret.info.get(MapleStatInfo.x));
                    break;
                case 11001022: //������Ʈ : �ҿ�
                    ret.statups.put(MapleBuffStat.SUMMON, 1);

                    break;

                case 11001021: //�ҵ� ���� ����Ʈ     



                    break;
                case 11101022: //falling
                    ret.statups.put(MapleBuffStat.rising1, 1);
                    ret.statups.put(MapleBuffStat.rising2, ret.info.get(MapleStatInfo.attackCount));
                    ret.statups.put(MapleBuffStat.rising3, ret.info.get(MapleStatInfo.x));
                    break;
                case 11111022: //����¡ ��
                    ret.statups.put(MapleBuffStat.DMG_DEC, 2);

                    break;
                case 11101024:
                    ret.statups.put(MapleBuffStat.BOOSTER, ret.info.get(MapleStatInfo.x));
                    break;
                case 11111024: //�ҿ� �����

                    break;
                case 11121005:
                    ret.statups.put(MapleBuffStat.moon1, 11121005);
                    break;
                case 11121011: //�ַ糪 Ÿ�� : �� ��
                    ret.statups.put(MapleBuffStat.moon1, 11121012);
                    ret.statups.put(MapleBuffStat.moon2, 1);
                    ret.statups.put(MapleBuffStat.moon3, ret.info.get(MapleStatInfo.x));

                    break;
                case 11121012: //�ַ糪 Ÿ�� : ����¡ ��
                    ret.statups.put(MapleBuffStat.moon1, 11121011);
                    ret.statups.put(MapleBuffStat.moon4, 1);
                    ret.statups.put(MapleBuffStat.moon5, ret.info.get(MapleStatInfo.x));

                    break;
                case 11121006: //�ҿ� �÷���

                    break;
                case 11121054: //�ҿ� ����

                    break;
                case 4111002:
                case 4211008:
                case 4331002:
                case 14111000:
                case 15121004:
                    ret.statups.put(MapleBuffStat.SHADOWPARTNER, ret.info.get(MapleStatInfo.x));
                    break;
                case 2121054:
                    ret.statups.put(MapleBuffStat.FIRE_AURA, 1);
                    break;
                case 2321054:
                    break;
            //    case 4211008:
                case 36111006:
                    ret.statups.put(MapleBuffStat.SHADOWPARTNER, Integer.valueOf(ret.level));
                    break;
                case 11101002:
                case 13101002:
                    ret.statups.put(MapleBuffStat.FINALATTACK, ret.info.get(MapleStatInfo.x));
                    break;
                case 22161004:
                    ret.statups.put(MapleBuffStat.ONYX_SHROUD, ret.info.get(MapleStatInfo.x));
                    break;
                case 13111023:
                ret.statups.put(MapleBuffStat.ALBATROSS, ret.info.get(MapleStatInfo.x));
                ret.statups.put(MapleBuffStat.INDIE_PAD, ret.info.get(MapleStatInfo.indiePad));
                ret.statups.put(MapleBuffStat.HP_BOOST, ret.info.get(MapleStatInfo.indieMhp));
                ret.statups.put(MapleBuffStat.ATTACK_SPEED, ret.info.get(MapleStatInfo.indieBooster));//true?
                ret.statups.put(MapleBuffStat.CRITICAL_PERCENT_UP, ret.info.get(MapleStatInfo.indieCr));
                    break;
                case 13120008:
                ret.statups.put(MapleBuffStat.ALBATROSS, ret.info.get(MapleStatInfo.x));
                ret.statups.put(MapleBuffStat.INDIE_PAD, ret.info.get(MapleStatInfo.indiePad));
                ret.statups.put(MapleBuffStat.HP_BOOST, ret.info.get(MapleStatInfo.indieMhp));
                ret.statups.put(MapleBuffStat.ATTACK_SPEED, ret.info.get(MapleStatInfo.indieBooster));//true?
                ret.statups.put(MapleBuffStat.CRITICAL_PERCENT_UP, ret.info.get(MapleStatInfo.indieCr));
                    break;
                case 13001022:
                    ret.statups.put(MapleBuffStat.LIGHTNING, 1);
                    ret.statups.put(MapleBuffStat.PERCENT_DAMAGE_BUFF, ret.info.get(MapleStatInfo.indieDamR));
                    break;
                case 2311002:
                case 3101004:
                case 3201004:
                case 13101003:
                case 13101024:
                case 33101003:
                case 35101005:
                    ret.statups.put(MapleBuffStat.SOULARROW, ret.info.get(MapleStatInfo.x));
                    break;
                case 2121009:
                case 2221009:
                case 2321010:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.BUFF_MASTERY, ret.info.get(MapleStatInfo.x));
                    break;
                case 2120010:
                case 2220010:
                case 2320011:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.ARCANE_AIM, ret.info.get(MapleStatInfo.x));
                    break;
                case 1211004:
                case 1211006:
                case 1211008:
                case 1221004:
                case 11111007:
                case 15101006:
                case 21101006:
                case 21111005:
                    ret.statups.put(MapleBuffStat.WK_CHARGE, ret.info.get(MapleStatInfo.x));
                    break;
                case 2111008:
                case 2211008:
                case 12101005:
                case 22121001:
                    ret.statups.put(MapleBuffStat.ELEMENT_RESET, ret.info.get(MapleStatInfo.x));
                    break;
                case 3111000:
                case 3121008:
                case 13111001:
                    ret.statups.put(MapleBuffStat.CONCENTRATE, ret.info.get(MapleStatInfo.x));
                    break;
                case 5110001:
                case 15100004:
                    ret.statups.put(MapleBuffStat.ENERGY_CHARGE, Integer.valueOf(0));
                    break;
                case 1101004:
                case 1201004:
                case 1301004:
                case 2111005:
                case 2211005:
                case 2311006:
                case 3101002:
                case 3201002:
                case 4101003:
                case 4201002:
                case 4311009:
                case 5101006:
                case 5201003:
                case 5301002:
                case 5701005:
                case 11101001:
                case 12101004:
                case 13101001:
                case 13101023:
                case 14101002:
                case 15101002:
                case 15101022:
                case 22141002:
                case 23101002:
                case 24101005:
                case 27101004:
                case 31201002:
                case 31001001:
                case 32101005:
                case 33001003:
                case 35101006:
                case 36101004:
                case 41101005:
                case 42101003:
                case 51101003:
                    ret.statups.put(MapleBuffStat.BOOSTER, ret.info.get(MapleStatInfo.x));
                    break;
                case 15001022:
                    ret.statups.put(MapleBuffStat.LIGHTNING, 1);
                    break;
                case 36101002:
                    ret.info.put(MapleStatInfo.powerCon, Integer.valueOf(6));
                    ret.statups.put(MapleBuffStat.SPIRIT_damage, ret.info.get(MapleStatInfo.x));
                    break;
                case 36111003:
                    ret.statups.put(MapleBuffStat.WATER_SHIELD, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.Dark_Crescendo, ret.info.get(MapleStatInfo.y));
                    break;
                case 36121003:
                    ret.statups.put(MapleBuffStat.PERCENT_DAMAGE_BUFF, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.BOSS_DAMAGE, ret.info.get(MapleStatInfo.x));
                    break;
                case 36121004:
                    ret.statups.put(MapleBuffStat.STANCE, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.Ignore_resistances, ret.info.get(MapleStatInfo.y));
                    break;
                case 41121003:
                    ret.statups.put(MapleBuffStat.ABNORMAL_STATUS_R, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.ELEMENTAL_STATUS_R, ret.info.get(MapleStatInfo.y));
                    break;
                case 31211003:
                    ret.statups.put(MapleBuffStat.ABNORMAL_STATUS_R, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.ELEMENTAL_STATUS_R, ret.info.get(MapleStatInfo.y));

                    break;
                case 21001003:
                    ret.statups.put(MapleBuffStat.BOOSTER, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.y)).intValue()));
                    break;
                case 27001004:
                    ret.statups.put(MapleBuffStat.MANA_WELL, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.indieMmpR)).intValue()));
                    break;
                case 36121054:
                    ret.statups.put(MapleBuffStat.SURPLUS, Integer.valueOf(16));
                    break;
                case 36111008: // Emergency Resupply
                ret.statups.put(MapleBuffStat.SUPPLY_SURPLUS, ret.info.get(MapleStatInfo.x));
                break;
                case 27101202:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.PRESSURE_VOID, Integer.valueOf(ret.info.get(MapleStatInfo.x)));
                    break;
                case 27111004:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.PRESSURE_VOID, Integer.valueOf(3));
                    break;
                case 27111005:
                    ret.statups.put(MapleBuffStat.Dusk_Guard, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.mdd)).intValue()));
                    ret.statups.put(MapleBuffStat.SPIRIT_LINK, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.pad)).intValue()));
                    break;
                case 27111006:
                    ret.statups.put(MapleBuffStat.ENHANCED_MAXMP, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.emad)).intValue()));
                    break;
    //            case 27110007:
    ///               ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
   //                 ret.statups.put(MapleBuffStat.Lunar_Tide, Integer.valueOf(2));
    //                break;
                case 30010242:
                    ret.statups.put(MapleBuffStat.LUNAR_TIDE, Integer.valueOf(1));
                    break;
                case 27121052:
                     ret.monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                case 27121054:
                    ret.statups.put(MapleBuffStat.LUMINOUS_GAUGE, Integer.valueOf(20040218));
                    break;
                case 27121005:
                    ret.statups.put(MapleBuffStat.Dark_Crescendo, ret.info.get(MapleStatInfo.x));
                    break;
            case 27121006: // Arcane Pitch
                ret.statups.put(MapleBuffStat.IGNORE_DEF, ret.info.get(MapleStatInfo.x));
                ret.statups.put(MapleBuffStat.FINALATTACK, ret.info.get(MapleStatInfo.y));
                break;
            case 24121053:
            case 21121053:
            case 22171053:
            case 23121053:
            case 27121053: //Heroic Memories
                ret.statups.put(MapleBuffStat.DAMAGE_CAP_INCREASE, ret.info.get(MapleStatInfo.indieMaxDamageOver));
                ret.statups.put(MapleBuffStat.DAMAGE_PERCENT, ret.info.get(MapleStatInfo.indieDamR));
            break;
            case 30020234:
                ret.statups.put(MapleBuffStat.STANCE, ret.info.get(MapleStatInfo.w));
                ret.statups.put(MapleBuffStat.DAMAGE_PERCENT, ret.info.get(MapleStatInfo.z));
                ret.statups.put(MapleBuffStat.AVOID, ret.info.get(MapleStatInfo.y));
                break;
            case 36000004:
                ret.statups.put(MapleBuffStat.STANCE, ret.info.get(MapleStatInfo.w));
                ret.statups.put(MapleBuffStat.DAMAGE_PERCENT, ret.info.get(MapleStatInfo.z));
                ret.statups.put(MapleBuffStat.AVOID, ret.info.get(MapleStatInfo.y));
                break;
           case 36100007:
                ret.statups.put(MapleBuffStat.STANCE, ret.info.get(MapleStatInfo.w));
                ret.statups.put(MapleBuffStat.DAMAGE_PERCENT, ret.info.get(MapleStatInfo.z));
                ret.statups.put(MapleBuffStat.AVOID, ret.info.get(MapleStatInfo.y));
                break;
           case 36110004:
                ret.statups.put(MapleBuffStat.STANCE, ret.info.get(MapleStatInfo.w));
                ret.statups.put(MapleBuffStat.DAMAGE_PERCENT, ret.info.get(MapleStatInfo.z));
                ret.statups.put(MapleBuffStat.AVOID, ret.info.get(MapleStatInfo.y));
                break;
           case 36120010:
                ret.statups.put(MapleBuffStat.STANCE, ret.info.get(MapleStatInfo.w));
                ret.statups.put(MapleBuffStat.DAMAGE_PERCENT, ret.info.get(MapleStatInfo.z));
                ret.statups.put(MapleBuffStat.AVOID, ret.info.get(MapleStatInfo.y));
                break;
          case 36120016:
                ret.statups.put(MapleBuffStat.STANCE, ret.info.get(MapleStatInfo.w));
                ret.statups.put(MapleBuffStat.DAMAGE_PERCENT, ret.info.get(MapleStatInfo.z));
                ret.statups.put(MapleBuffStat.AVOID, ret.info.get(MapleStatInfo.y));
                break;
            case 5721053:
            case 5321053:
            case 5221053:
            case 5121053:
            case 4341053:
            case 4221053:
            case 4121053:
            case 3221053:
            case 3121053:
            case 2321053:
            case 2221053:
            case 2121053:
            case 1321053:
            case 1221053:
            case 1121053: //Epic Adventure
                ret.statups.put(MapleBuffStat.DAMAGE_CAP_INCREASE, ret.info.get(MapleStatInfo.indieMaxDamageOver));
                ret.statups.put(MapleBuffStat.DAMAGE_PERCENT, ret.info.get(MapleStatInfo.indieDamR));
            break;
            case 31221053:
            case 31121053:
            case 32121053:
            case 33121053:
            case 35121053: //For Liberty
                ret.statups.put(MapleBuffStat.DAMAGE_CAP_INCREASE, ret.info.get(MapleStatInfo.indieMaxDamageOver));
                ret.statups.put(MapleBuffStat.DAMAGE_PERCENT, ret.info.get(MapleStatInfo.indieDamR));
            break;
                case 41001001:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.SPIRIT_damage, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.y)).intValue()));
                    ret.statups.put(MapleBuffStat.SPEED_LEVEL, Integer.valueOf(-1));

                    ret.statups.put(MapleBuffStat.Battoujutsu_Stance, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.x)).intValue()));
                    break;
                case 42101002:
                    ret.statups.put(MapleBuffStat.Haku_Reborn, Integer.valueOf(1));
                    break;
                case 42101004:
                case 42111006:
                    ret.statups.put(MapleBuffStat.Frozen_Shikigami_Haunting, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.x)).intValue()));
                    break;
                case 42121008:
                    ret.statups.put(MapleBuffStat.Frozen_Shikigami_Haunting, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.x)).intValue()));
                    ret.statups.put(MapleBuffStat.Battoujutsu_Stance, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.x)).intValue()));
                    break;
                case 61101004:
                    ret.statups.put(MapleBuffStat.BOOSTER, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.x)).intValue()));
                    break;
                case 61101002:
                case 61120007:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.Tempest_Blades, Integer.valueOf(0));
                    break;
                case 61111003:
                    ret.statups.put(MapleBuffStat.ABNORMAL_STATUS_R, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.asrR)).intValue()));
                    ret.statups.put(MapleBuffStat.ELEMENTAL_STATUS_R, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.terR)).intValue()));
                    break;
                case 61111004:
                    ret.statups.put(MapleBuffStat.ATTACKUP_indieDamR, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.indieDamR)).intValue()));
                    break;
                case 61121009:
                    ret.statups.put(MapleBuffStat.Ignores_monster_DEF, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.ignoreMobpdpR)).intValue()));
                    break;
                case 60001216:
                case 60001217:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.KAISER_MODE_CHANGE, Integer.valueOf(0));
                    break;
                case 61111008:
                case 61120008:
                    ret.statups.put(MapleBuffStat.SPEED, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.speed)).intValue()));
                    ret.statups.put(MapleBuffStat.MORPH, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.morph)).intValue()));
                    ret.statups.put(MapleBuffStat.SPIRIT_damage, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.cr)).intValue()));
                    ret.statups.put(MapleBuffStat.ATTACKUP_indieDamR, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.indieDamR)).intValue()));
                    ret.statups.put(MapleBuffStat.STANCE, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.prop)).intValue()));
                    ret.statups.put(MapleBuffStat.INDIEBOOSTER, Integer.valueOf(-2));
                    break;
                case 30021237:
                    ret.statups.put(MapleBuffStat.fly, Integer.valueOf(1));
                    break;
                case 4341052:
                    ret.statups.put(MapleBuffStat.ASURA, ret.info.get(MapleStatInfo.x));
                    break;
                case 4341054:
                    ret.statups.put(MapleBuffStat.ARIANT_COSS_IMU2, Integer.valueOf(1));
                    ret.overTime = true;
                    break;
                case 36001002:
                    ret.info.put(MapleStatInfo.powerCon, Integer.valueOf(3));
                    ret.statups.put(MapleBuffStat.ANGEL_ATK, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.indiePad)).intValue()));
                    break;
                case 5111007:
                case 5120012:
                case 5211007:
                case 5220014:
                case 5311005:
                case 5320007:
                case 5711011:
                case 5720005:
                case 5811007:
                case 5911007:
                case 15111011:
                case 35111013:
                case 35120014:
                    ret.statups.put(MapleBuffStat.DICE_ROLL, Integer.valueOf(0));
                    break;
                case 80001264:
                     ret.info.put(MapleStatInfo.cooltime, Integer.valueOf(180000));
                case 5120011:
                case 5220012:
                    ret.info.put(MapleStatInfo.cooltime, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.PIRATES_REVENGE, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.damR)).intValue()));
                    break;
                case 4121054:
                    ret.statups.put(MapleBuffStat.SPEED_LEVEL, Integer.valueOf(1));
                    break;
                case 15111023:
                    //  ret.statups.put(MapleBuffStat.STATUS_RESIST, ret.info.get(MapleStatInfo.asrR));
                    //  ret.statups.put(MapleBuffStat.ELEMENT_RESIST, ret.info.get(MapleStatInfo.asrR));

                    break;
                case 15111024:
                    //     ret.statups.put(MapleBuffStat.ARCANE_AIM, ret.info.get(MapleStatInfo.x));
                    //    ret.statups.put(MapleBuffStat.Damage_Absorbed, ret.info.get(MapleStatInfo.y));

                    break;
                case 5121009:
                case 15111005:
                    ret.statups.put(MapleBuffStat.SPEED_INFUSION, ret.info.get(MapleStatInfo.x));
                    break;
                case 4321000:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(1000));
                    ret.statups.put(MapleBuffStat.DASH_SPEED, Integer.valueOf(100 + ((Integer) ret.info.get(MapleStatInfo.x)).intValue()));
                    ret.statups.put(MapleBuffStat.DASH_JUMP, ret.info.get(MapleStatInfo.y));
                    break;
                case 1101007:
                case 1201007:
                    ret.statups.put(MapleBuffStat.POWERGUARD, ret.info.get(MapleStatInfo.x));
                    break;
                case 32111004:
                    ret.statups.put(MapleBuffStat.CONVERSION, ret.info.get(MapleStatInfo.x));
                    break;
                case 1301007:
                case 9001008:
                case 9101008:
                    ret.statups.put(MapleBuffStat.MAXHP, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.MAXMP, ret.info.get(MapleStatInfo.x));
                    break;
                case 1111002:
                case 11111001:
                    ret.statups.put(MapleBuffStat.COMBO, Integer.valueOf(1));
                    break;
                case 21120007:
                    ret.statups.put(MapleBuffStat.COMBO_BARRIER, ret.info.get(MapleStatInfo.x));
                    break;
                case 31011001: // Overload Release
                ret.info.put(MapleStatInfo.time, Integer.valueOf(60000));
                ret.statups.put(MapleBuffStat.OVERLOAD_RELEASE, ret.info.get(MapleStatInfo.x));
                ret.statups.put(MapleBuffStat.HP_R, ret.info.get(MapleStatInfo.indieMhpR));
                break;
                case 31211004: // Diabolic Recovery
                ret.info.put(MapleStatInfo.time, Integer.valueOf(180000));    
                ret.statups.put(MapleBuffStat.HP_R, ret.info.get(MapleStatInfo.indieMhpR));
                ret.statups.put(MapleBuffStat.DIABOLIC_RECOVERY, ret.info.get(MapleStatInfo.x));
                ret.statups.put(MapleBuffStat.Idk, ret.info.get(MapleStatInfo.w));
                ret.statups.put(MapleBuffStat.RANDOM, ret.info.get(MapleStatInfo.y));
            //    ret.statups.put(MapleBuffStat.RECOVERY, ret.info.get(MapleStatInfo.x));
                break;
                 case 31221054:
                ret.statups.put(MapleBuffStat.DIABOLIC_RECOVERY, ret.info.get(MapleStatInfo.x));
                ret.statups.put(MapleBuffStat.HP_R, ret.info.get(MapleStatInfo.indieMhpR));
       //         ret.info.put(MapleStatInfo.time, Integer.valueOf(180000));
                     break;
                 case 36101003:
                     ret.statups.put(MapleBuffStat.MP_R, ret.info.get(MapleStatInfo.indieMmpR));
                     ret.statups.put(MapleBuffStat.HP_R, ret.info.get(MapleStatInfo.indieMhpR));
                     ret.info.put(MapleStatInfo.time, Integer.valueOf(180000));
                     break;      
                case 5211006:
                case 5220011:
                case 22151002:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.HOMING_BEACON, ret.info.get(MapleStatInfo.x));
                    break;
                case 4341007:
                    ret.statups.put(MapleBuffStat.STANCE, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.prop)).intValue()));
                    break;
                case 1311005:
                case 1311006:
                case 21111009:
                    ret.hpR = (-((Integer) ret.info.get(MapleStatInfo.x)).intValue() / 100.0D);
                    break;
                case 1211010:
                    ret.hpR = (((Integer) ret.info.get(MapleStatInfo.x)).intValue() / 100.0D);
                    break;
                case 60011219:
                    ret.statups.put(MapleBuffStat.TERMS, ret.info.get(MapleStatInfo.indieDamR));
                case 4341002:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(60000));
                    ret.hpR = (-((Integer) ret.info.get(MapleStatInfo.x)).intValue() / 100.0D);
                    ret.statups.put(MapleBuffStat.FINAL_CUT, ret.info.get(MapleStatInfo.y));
                    break;
                case 2111007:
                case 2211007:
                case 2311007:
                case 12111007:
                case 22161005:
                case 32111010:
                    ret.info.put(MapleStatInfo.mpCon, ret.info.get(MapleStatInfo.y));
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.TELEPORT_MASTERY, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case 4331003:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.OWL_SPIRIT, ret.info.get(MapleStatInfo.y));
                    break;
                case 1311008:
                    if (!GameConstants.GMS) {
                        ret.statups.put(MapleBuffStat.DRAGONBLOOD, ret.info.get(MapleStatInfo.x));
                    }
                    break;
                case 1121000:
                case 1221000:
                case 1321000:
                case 2121000:
                case 2221000:
                case 2321000:
                case 3121000:
                case 3221000:
                case 4121000:
                case 4221000:
                case 4341000:
                case 5121000:
                case 5221000:
                case 5321005:
                case 5721000:
                case 21121000:
                case 22171000:
                case 23121005:
                case 13121000:
                case 11121000:
                case 15121000:
                case 24121008:
                case 31221008:
                case 27121009:
                case 31121004:
                case 32121007:
                case 33121007:
                case 35121007:
                case 36121008:
                case 41121005:
                case 42121006:
                case 51121005:
                case 61121014:
                case 65121009:
                    ret.statups.put(MapleBuffStat.MAPLE_WARRIOR, ret.info.get(MapleStatInfo.x));
                    break;
                case 31221004:
                    ret.statups.put(MapleBuffStat.ATTACKUP_indieDamR, ret.info.get(MapleStatInfo.indieDamR));
                    ret.statups.put(MapleBuffStat.INDIEBOOSTER, 2);
                    ret.statups.put(MapleBuffStat.BOOSTER, 2);
                    break;
             //   case 60011219:
               //     ret.statups.put(MapleBuffStat.ATTACKUP_indieDamR, ret.info.get(MapleStatInfo.indieDamR));
                 //   break;
                case 65101002:
                    ret.statups.put(MapleBuffStat.Damage_Absorbed, ret.info.get(MapleStatInfo.x));
                    break;
                case 65121004:
                    ret.statups.put(MapleBuffStat.Crit_Damage, ret.info.get(MapleStatInfo.x));
                    break;
                case 15111006:
                    ret.statups.put(MapleBuffStat.SPARK, ret.info.get(MapleStatInfo.x));
                    break;
                case 3121002:
                case 3221002:
                case 13121005:
                case 33121004:
                    ret.statups.put(MapleBuffStat.SHARP_EYES, Integer.valueOf((((Integer) ret.info.get(MapleStatInfo.x)).intValue() << 8) + ((Integer) ret.info.get(MapleStatInfo.criticaldamageMax)).intValue()));
                    break;
                case 22151003:
                    ret.statups.put(MapleBuffStat.MAGIC_RESISTANCE, ret.info.get(MapleStatInfo.x));
                    break;
                case 2000007:
                case 12000006:
                case 22000002:
                case 32000012:
                    ret.statups.put(MapleBuffStat.ELEMENT_WEAKEN, ret.info.get(MapleStatInfo.x));
                    break;
                case 21101003:
                    ret.statups.put(MapleBuffStat.BODY_PRESSURE, ret.info.get(MapleStatInfo.x));
                    break;
                case 21000000:
                    ret.statups.put(MapleBuffStat.ARAN_COMBO, Integer.valueOf(100));
                    break;
                case 23101003:
                    ret.statups.put(MapleBuffStat.SPIRIT_SURGE, ret.info.get(MapleStatInfo.x));
                    break;
                case 21100005:
                case 31121002:
                case 32101004:
                    ret.statups.put(MapleBuffStat.COMBO_DRAIN, ret.info.get(MapleStatInfo.x));
                    break;
                case 21111001:
                    ret.statups.put(MapleBuffStat.SMART_KNOCKBACK, ret.info.get(MapleStatInfo.x));
                    break;
                case 23121004:
                    ret.statups.put(MapleBuffStat.PIRATES_REVENGE, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.damR)).intValue()));
                    break;
                case 1111007:
                case 1211009:
                case 1311007:
                    ret.monsterStatus.put(MonsterStatus.MAGIC_CRASH, Integer.valueOf(1));
                    break;
                case 1220013:
                    ret.statups.put(MapleBuffStat.DIVINE_SHIELD, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.x)).intValue() + 1));
                    break;
                case 1211011:
                    ret.statups.put(MapleBuffStat.COMBAT_ORDERS, ret.info.get(MapleStatInfo.x));
                    break;
                case 5321054:

                    ret.statups.put(MapleBuffStat.ATTACK_COUNT, ret.info.get(MapleStatInfo.x));
                    break;
                case 23111005:
                    ret.statups.put(MapleBuffStat.ABNORMAL_STATUS_R, ret.info.get(MapleStatInfo.terR));
                    ret.statups.put(MapleBuffStat.ELEMENTAL_STATUS_R, ret.info.get(MapleStatInfo.terR));
                    ret.statups.put(MapleBuffStat.WATER_SHIELD, ret.info.get(MapleStatInfo.x));
                    break;
                case 22131001:
                    ret.statups.put(MapleBuffStat.MAGIC_SHIELD, ret.info.get(MapleStatInfo.x));
                    break;
                case 22181003:
                    ret.statups.put(MapleBuffStat.SOUL_STONE, Integer.valueOf(1));
                    break;
                case 24111002:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.SOUL_STONE, Integer.valueOf(1));
                    break;
                case 32121003:
                    ret.statups.put(MapleBuffStat.TORNADO, ret.info.get(MapleStatInfo.x));
                    break;
                case 5211009:
                    //   ret.statups.put(MapleBuffStat.SKILL_COUNT, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.STACK_WATK, ret.info.get(MapleStatInfo.y));
                    break;
                case 2311009:
                    ret.statups.put(MapleBuffStat.HOLY_MAGIC_SHELL, ret.info.get(MapleStatInfo.x));
                    ret.info.put(MapleStatInfo.cooltime, ret.info.get(MapleStatInfo.y));
                    ret.hpR = (((Integer) ret.info.get(MapleStatInfo.z)).intValue() / 100.0D);
                    break;
                case 32111005:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(60000));
                    ret.statups.put(MapleBuffStat.BODY_BOOST, Integer.valueOf(ret.level));
                    break;
                case 22131002:
                case 22141003:
                    ret.statups.put(MapleBuffStat.SLOW, ret.info.get(MapleStatInfo.x));
                    break;
                case 4001002:
                case 14001002:
                    ret.monsterStatus.put(MonsterStatus.WATK, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.info.get(MapleStatInfo.y));
                    break;
                case 5221009:
                    ret.monsterStatus.put(MonsterStatus.HYPNOTIZE, Integer.valueOf(1));
                    break;
                case 4341003:
                    ret.monsterStatus.put(MonsterStatus.MONSTER_BOMB, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.damage)).intValue()));
                    break;
                case 1201006:
                    ret.monsterStatus.put(MonsterStatus.WATK, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.DARKNESS, ret.info.get(MapleStatInfo.z));
                    break;


                case 1111005:
                case 1111008:
                case 1121001:
                case 1211002:
                case 1221001:
                case 1321001:
                case 2211003:
                case 2221006:
                case 2311004:
                case 3101005:
                case 3120010:
                case 4121008:
                case 4201004:
                case 4211002:
                case 4221007:
                case 4331005:
                case 5101002:
                case 5101003:
                case 5111002:
                case 5121004:
                case 5121005:
                case 5121007:
                case 5201004:
                case 5301001:
                case 5310008:
                case 5311001:
                case 5311002:
                case 9001020:
                case 9101020:
                case 15101005:
                case 21110006:
                case 22131000:
                case 22141001:
                case 22151001:
                case 22181001:
                case 31101002:
                case 31111001:
                case 32101001:
                case 32111011:
                case 32121004:
                case 33101001:
                case 33101002:
                case 33111002:
                case 33121002:
                case 35101003:
                case 35111015:
                case 51111007:
                    ret.monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case 1111003:
                case 4321002:
                case 11111002:
                case 90001004:
                    ret.monsterStatus.put(MonsterStatus.DARKNESS, ret.info.get(MapleStatInfo.x));
                    break;
                case 4121003:
                case 4221003:
                case 33121005:
                    ret.monsterStatus.put(MonsterStatus.SHOWDOWN, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.MDEF, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.info.get(MapleStatInfo.x));
                    break;
                case 31121003:
                    ret.monsterStatus.put(MonsterStatus.SHOWDOWN, ret.info.get(MapleStatInfo.w));
                    ret.monsterStatus.put(MonsterStatus.MDEF, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.MATK, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.WATK, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.ACC, ret.info.get(MapleStatInfo.x));
                    break;
                case 23121002:
                    ret.monsterStatus.put(MonsterStatus.WDEF, Integer.valueOf(-((Integer) ret.info.get(MapleStatInfo.x)).intValue()));
                    break;
                case 2121006:
                case 2201004:
                case 2211002:
                case 2211006:
                case 2221001:
                case 2221003:
                case 2221007:
                case 3211003:
                case 5211005:
                case 21120006:
                case 22121000:
                case 90001006:
                    ret.monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.time)).intValue() * 2));
                    break;
                case 2101003:
                case 2201003:
                case 12101001:
                case 90001002:
                case 4121015:
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.info.get(MapleStatInfo.x));
                    break;
                case 5011002:
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.info.get(MapleStatInfo.z));
                    break;
                case 1121010:
                    ret.statups.put(MapleBuffStat.ENRAGE, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.x)).intValue() * 100 + ((Integer) ret.info.get(MapleStatInfo.mobCount)).intValue()));
                    break;
                case 22161002:
                case 23111002:
                    ret.monsterStatus.put(MonsterStatus.IMPRINT, ret.info.get(MapleStatInfo.x));
                    break;
                case 90001003:
                    ret.monsterStatus.put(MonsterStatus.POISON, Integer.valueOf(1));
                    break;
                case 4121004:
                case 4221004:
                    ret.monsterStatus.put(MonsterStatus.NINJA_AMBUSH, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.damage)).intValue()));
                    break;
                case 2311005:
                    ret.monsterStatus.put(MonsterStatus.DOOM, Integer.valueOf(1));
                    break;
                case 32111006:
                    ret.statups.put(MapleBuffStat.REAPER, Integer.valueOf(1));
                    break;
                case 35121003:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.SUMMON, Integer.valueOf(1));
                    break;
                case 35111001:
                case 35111009:
                case 13111024:
                case 35111010:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.PUPPET, Integer.valueOf(1));
                    break;
                case 3111002:
                case 3120012:
                case 3211002:
                case 3220012:
                case 4341006:
                case 5211001:
                case 5211014:
                case 5220002:
                case 5321003:
                case 33111003:
                    ret.statups.put(MapleBuffStat.PUPPET, Integer.valueOf(1));
                    break;
                case 3120006:
                case 3220005:
                    ret.statups.put(MapleBuffStat.ELEMENTAL_STATUS_R, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.terR)).intValue()));
                    ret.statups.put(MapleBuffStat.SPIRIT_LINK, Integer.valueOf(1));
                    break;
                case 5220019:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(120000));
                    break;
                case 2121005:
                case 3101007:
                case 3111005:
                case 3121006:
                case 3201007:
                case 3211005:
                case 5211011:
                case 5211015:
                case 5211016:
                case 5711001:
                case 23111008:
                case 23111009:
                case 23111010:
                case 33111005:
                case 35111002:
                case 61111002:
                    ret.statups.put(MapleBuffStat.SUMMON, Integer.valueOf(1));
                    ret.monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                    break;
                case 2221005:
                case 3221005:
                    ret.statups.put(MapleBuffStat.SUMMON, Integer.valueOf(1));
                    ret.monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                    break;
                case 35111005:
                    ret.statups.put(MapleBuffStat.SUMMON, Integer.valueOf(1));
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.WDEF, ret.info.get(MapleStatInfo.y));
                    break;
                case 1321007:
                    ret.statups.put(MapleBuffStat.BEHOLDER, Integer.valueOf(ret.level));
                    break;
                case 2321003:
                case 4111007:
                case 36121002: // TEST HYPO
                case 36121013: // TEST HYPO
                case 36121014: // TEST HYPO
                case 4211007:
                case 5211002:
                case 5321004:
                case 11001004:

                case 12001004:
                case 12111004:
                case 13001004:
                case 14001005:
                case 15001004:
                case 33101008:
                case 35111011:
                case 35121009:
                case 35121011:
                case 42100010:
                case 22171052:
                case 42101021:
                case 42121021:
                case 42101001:
                    ret.statups.put(MapleBuffStat.SUMMON, Integer.valueOf(1));
                    break;
                case 42111003:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(60000));
                    ret.statups.put(MapleBuffStat.SUMMON, Integer.valueOf(1));
                    break;
                case 35121010:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(60000));
                    ret.statups.put(MapleBuffStat.DAMAGE_BUFF, ret.info.get(MapleStatInfo.x));
                    break;
                case 31121005:
                    ret.statups.put(MapleBuffStat.PIRATES_REVENGE, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.damR)).intValue()));
                    ret.statups.put(MapleBuffStat.DARK_METAMORPHOSIS, Integer.valueOf(6));
                    break;
                case 2311003:
                case 9001002:
                    ret.statups.put(MapleBuffStat.HOLY_SYMBOL, ret.info.get(MapleStatInfo.x));
                    break;
                case 80001034:
                case 80001035:
                case 80001036:
                    ret.statups.put(MapleBuffStat.VIRTUE_EFFECT, Integer.valueOf(1));
                    break;
                case 2111004:
                case 2211004:
                case 12111002:
                case 90001005:
                    ret.monsterStatus.put(MonsterStatus.SEAL, Integer.valueOf(1));
                    break;
                case 24121003:
                    ret.info.put(MapleStatInfo.damage, ret.info.get(MapleStatInfo.v));
                    ret.info.put(MapleStatInfo.attackCount, ret.info.get(MapleStatInfo.w));
                    ret.info.put(MapleStatInfo.mobCount, ret.info.get(MapleStatInfo.x));
                    break;
                case 4111003:
                case 14111001:
                    ret.monsterStatus.put(MonsterStatus.SHADOW_WEB, Integer.valueOf(1));
                    break;
                case 4111009:
                case 5201008:
                case 14111007:
                    ret.statups.put(MapleBuffStat.SPIRIT_CLAW, Integer.valueOf(0));
                    break;
                case 2121004:
                case 2221004:
                case 2321004:
                    ret.hpR = (((Integer) ret.info.get(MapleStatInfo.y)).intValue() / 100.0D);
                    ret.mpR = (((Integer) ret.info.get(MapleStatInfo.y)).intValue() / 100.0D);
                    ret.statups.put(MapleBuffStat.INFINITY, ret.info.get(MapleStatInfo.x));
                    if (GameConstants.GMS) {
                        ret.statups.put(MapleBuffStat.STANCE, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.prop)).intValue()));
                    }
                    break;
                case 22181004:
                    ret.statups.put(MapleBuffStat.ONYX_WILL, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.damage)).intValue()));
                    ret.statups.put(MapleBuffStat.STANCE, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.prop)).intValue()));
                    break;
                case 1121002:
                case 1221002:
                case 1321002:
                case 5321010:
                case 21121003:
                case 32111014:
                case 32121005:
                case 50001214:
                case 51121004:
                case 65111004:
                    ret.statups.put(MapleBuffStat.STANCE, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.prop)).intValue()));
                    break;
                case 2121002:
                case 2221002:
                case 2321002:
                    ret.statups.put(MapleBuffStat.MANA_REFLECTION, Integer.valueOf(1));
                    break;
                case 2321005:
                    ret.statups.put(MapleBuffStat.HOLY_SHIELD, Integer.valueOf(GameConstants.GMS ? ret.level : ((Integer) ret.info.get(MapleStatInfo.x)).intValue()));
                    break;
                case 3121007:
                    ret.statups.put(MapleBuffStat.HAMSTRING, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.info.get(MapleStatInfo.x));
                    break;
                case 3221006:
                case 33111004:
                    ret.statups.put(MapleBuffStat.BLIND, ret.info.get(MapleStatInfo.x));
                    ret.monsterStatus.put(MonsterStatus.ACC, ret.info.get(MapleStatInfo.x));
                    break;
                case 33111007:
                    ret.statups.put(MapleBuffStat.SPEED, ret.info.get(MapleStatInfo.z));
                    ret.statups.put(MapleBuffStat.ATTACK_BUFF, ret.info.get(MapleStatInfo.y));
                    ret.statups.put(MapleBuffStat.FELINE_BERSERK, ret.info.get(MapleStatInfo.x));
                    break;
                case 2301004:
                case 9001003:
                case 9101003:
                    ret.statups.put(MapleBuffStat.BLESS, Integer.valueOf(ret.level));
                    break;
                case 32120000:
                    ret.info.put(MapleStatInfo.dot, ret.info.get(MapleStatInfo.damage));
                    ret.info.put(MapleStatInfo.dotTime, Integer.valueOf(3));
                case 32001003:
                case 32110007:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(sourceid == 32110007 ? 60000 : 2100000000));
                    ret.statups.put(MapleBuffStat.AURA, Integer.valueOf(ret.level));
                    ret.statups.put(MapleBuffStat.DARK_AURA, ret.info.get(MapleStatInfo.x));
                    break;
                case 32110000:
                case 32110008:
                case 32111012:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(sourceid == 32110008 ? 60000 : 2100000000));

                    ret.statups.put(MapleBuffStat.BLUE_AURA, Integer.valueOf(ret.level));
                    break;
                case 32120001:
                    ret.monsterStatus.put(MonsterStatus.SPEED, ret.info.get(MapleStatInfo.speed));
                case 32101003:
                case 32110009:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(sourceid == 32110009 ? 60000 : 2100000000));

                    ret.statups.put(MapleBuffStat.YELLOW_AURA, Integer.valueOf(ret.level));
                    break;
                case 33101004:
                    ret.statups.put(MapleBuffStat.RAINING_MINES, ret.info.get(MapleStatInfo.x));
                    break;
                       case 13121004:// Touch of the Wind
                //ret.statups.put(MapleBuffStat.AVOID, ret.info.get(MapleStatInfo.x));
                //ret.statups.put(MapleBuffStat.ACC, ret.info.get(MapleStatInfo.y));
                //ret.statups.put(MapleBuffStat.HP_BOOST_PERCENT, ret.info.get(MapleStatInfo.indieMhpR));
                //ret.statups.put(MapleBuffStat.ALBATROSS, ret.info.get(MapleStatInfo.x));
                //statups.add(new Triple<MapleBuffStats, Integer, Boolean>(MapleBuffStats.WN_BISS, ret.effects.getStats("prop"), false));
                ret.statups.put(MapleBuffStat.TOUCH_OF_THE_WIND2, ret.info.get(MapleStatInfo.x));
                ret.statups.put(MapleBuffStat.HAMSTRING, ret.info.get(MapleStatInfo.y));
                ret.statups.put(MapleBuffStat.TOUCH_OF_THE_WIND1, ret.info.get(MapleStatInfo.prop));
                ret.statups.put(MapleBuffStat.HP_R, ret.info.get(MapleStatInfo.indieMhpR));
                         break;
                case 35101007:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.PERFECT_ARMOR, ret.info.get(MapleStatInfo.x));
                    break;
                case 31101003:
                    ret.statups.put(MapleBuffStat.PERFECT_ARMOR, ret.info.get(MapleStatInfo.y));
                    break;
                case 35121006:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.SATELLITESAFE_PROC, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.SATELLITESAFE_ABSORB, ret.info.get(MapleStatInfo.y));
                    break;
                case 51111004:
                    ret.statups.put(MapleBuffStat.DEFENCE_R, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.STATUS_RESIST, ret.info.get(MapleStatInfo.y));
                    ret.statups.put(MapleBuffStat.ELEMENT_RESIST, ret.info.get(MapleStatInfo.z));
                    break;
                case 51121006:
                    ret.statups.put(MapleBuffStat.DAMAGE_BUFF, ret.info.get(MapleStatInfo.x));
                    ret.statups.put(MapleBuffStat.CRITICAL_RATE_BUFF, ret.info.get(MapleStatInfo.y));
                    ret.statups.put(MapleBuffStat.CRITICAL_RATE_BUFF, ret.info.get(MapleStatInfo.z));
                    break;
                case 51111003:
                    ret.statups.put(MapleBuffStat.DAMAGE_BUFF, ret.info.get(MapleStatInfo.x));
                    break;
                case 20021110:
                case 20031203:
                case 80001040:
                    ret.moveTo = ((Integer) ret.info.get(MapleStatInfo.x)).intValue();
                    break;
                case 5311004:
                    ret.statups.put(MapleBuffStat.BARREL_ROLL, Integer.valueOf(0));
                    break;
                case 5121015:
                    ret.statups.put(MapleBuffStat.DAMAGE_BUFF, ret.info.get(MapleStatInfo.x));
                    break;
                case 80001089:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.SOARING, Integer.valueOf(1));
                    break;
                case 20031205:
                    ret.statups.put(MapleBuffStat.PHANTOM_MOVE, ret.info.get(MapleStatInfo.x));
                    break;
                case 35001001:
                case 35101009:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(1000));
                    ret.statups.put(MapleBuffStat.MECH_CHANGE, Integer.valueOf(level));
                    break;
                case 35111004:
                case 35121013:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(5000));
                    ret.statups.put(MapleBuffStat.MECH_CHANGE, Integer.valueOf(level));
                    break;
                case 35121005:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.MECH_CHANGE, Integer.valueOf(level));
                    break;
                case 10001075:
                    ret.statups.put(MapleBuffStat.ECHO_OF_HERO, ret.info.get(MapleStatInfo.x));
                    break;
                case 31121007:
                    ret.statups.put(MapleBuffStat.BOUNDLESS_RAGE, Integer.valueOf(1));
                    break;
                case 31111004:
                    ret.statups.put(MapleBuffStat.ABNORMAL_STATUS_R, ret.info.get(MapleStatInfo.y));
                    ret.statups.put(MapleBuffStat.ELEMENTAL_STATUS_R, ret.info.get(MapleStatInfo.z));
                    ret.statups.put(MapleBuffStat.DEFENCE_BOOST_R, ret.info.get(MapleStatInfo.x));
                    break;
                case 80001262:
                case 80000086:
                    ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                    ret.statups.put(MapleBuffStat.ANGEL_ATK, Integer.valueOf(12));
                    ret.statups.put(MapleBuffStat.ANGEL_MATK, Integer.valueOf(12));
                    ret.statups.put(MapleBuffStat.SPEED, Integer.valueOf(1));
                    break;
            }
   
                if (GameConstants.isBeginnerJob(sourceid / 10000)) {
                    switch (sourceid % 10000) {
                        case 1087:
                            ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                            ret.statups.put(MapleBuffStat.ANGEL_ATK, Integer.valueOf(10));
                            ret.statups.put(MapleBuffStat.ANGEL_MATK, Integer.valueOf(10));
                            ret.statups.put(MapleBuffStat.SPEED, Integer.valueOf(1));
                            break;
                        case 1085:
                        case 1090:
                            ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                            ret.statups.put(MapleBuffStat.ANGEL_ATK, Integer.valueOf(5));
                            ret.statups.put(MapleBuffStat.ANGEL_MATK, Integer.valueOf(5));
                            ret.statups.put(MapleBuffStat.SPEED, Integer.valueOf(1));
                            break;
                        case 1179:
                            ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                            ret.statups.put(MapleBuffStat.ANGEL_ATK, Integer.valueOf(12));
                            ret.statups.put(MapleBuffStat.ANGEL_MATK, Integer.valueOf(12));
                            ret.statups.put(MapleBuffStat.SPEED, Integer.valueOf(1));
                            break;

                        case 1105:
                            ret.statups.put(MapleBuffStat.ICE_SKILL, Integer.valueOf(1));
                            ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                            break;
                        case 93:
                            ret.statups.put(MapleBuffStat.HIDDEN_POTENTIAL, Integer.valueOf(1));
                            break;
                        case 8001:
                            ret.statups.put(MapleBuffStat.SOULARROW, ret.info.get(MapleStatInfo.x));
                            break;
                        case 1005:
                            ret.statups.put(MapleBuffStat.ECHO_OF_HERO, ret.info.get(MapleStatInfo.x));
                            break;
                        case 1011:
                            ret.statups.put(MapleBuffStat.BERSERK_FURY, ret.info.get(MapleStatInfo.x));
                            break;
                        case 1010:
                            ret.statups.put(MapleBuffStat.DIVINE_BODY, Integer.valueOf(1));
                            break;
                        case 1001:
                            if ((sourceid / 10000 == 3001) || (sourceid / 10000 == 3000)) {
                                ret.statups.put(MapleBuffStat.INFILTRATE, ret.info.get(MapleStatInfo.x));
                            } else {
                                ret.statups.put(MapleBuffStat.RECOVERY, ret.info.get(MapleStatInfo.x));
                            }
                            break;
                        case 8003:
                            ret.statups.put(MapleBuffStat.MAXHP, ret.info.get(MapleStatInfo.x));
                            ret.statups.put(MapleBuffStat.MAXMP, ret.info.get(MapleStatInfo.x));
                            break;
                        case 8004:
                            ret.statups.put(MapleBuffStat.COMBAT_ORDERS, ret.info.get(MapleStatInfo.x));
                            break;
                        case 8005:
                            ret.statups.put(MapleBuffStat.HOLY_SHIELD, Integer.valueOf(1));
                            break;
                        case 8006:
                            ret.statups.put(MapleBuffStat.SPEED_INFUSION, ret.info.get(MapleStatInfo.x));
                            break;
                        case 103:
                            ret.monsterStatus.put(MonsterStatus.STUN, Integer.valueOf(1));
                            break;
                        case 99:
                        case 104:
                            ret.monsterStatus.put(MonsterStatus.FREEZE, Integer.valueOf(1));
                            ret.info.put(MapleStatInfo.time, Integer.valueOf(((Integer) ret.info.get(MapleStatInfo.time)).intValue() * 2));
                            break;
                        case 8002:
                            ret.statups.put(MapleBuffStat.SHARP_EYES, Integer.valueOf((((Integer) ret.info.get(MapleStatInfo.x)).intValue() << 8) + ((Integer) ret.info.get(MapleStatInfo.y)).intValue() + ((Integer) ret.info.get(MapleStatInfo.criticaldamageMax)).intValue()));
                            break;
                        case 1026:
                        case 1142:
                            ret.info.put(MapleStatInfo.time, Integer.valueOf(2100000000));
                            ret.statups.put(MapleBuffStat.SOARING, Integer.valueOf(1));
                    }
                }
            
//        } else {
//            switch (sourceid) {
//                case 2022746:
//                case 2022747:
//                case 2022823:
//                    ret.statups.clear();
//                    ret.statups.put(MapleBuffStat.PYRAMID_PQ, Integer.valueOf(1));
//            }
        }

        if (ret.isPoison()) {
            ret.monsterStatus.put(MonsterStatus.POISON, Integer.valueOf(1));
        }
        if ((ret.isMorph()) || (ret.isPirateMorph())) {
            ret.statups.put(MapleBuffStat.MORPH, Integer.valueOf(ret.getMorph()));
        }

        return ret;
    }

    public final void applyPassive(MapleCharacter applyto, MapleMapObject obj) {
        if ((makeChanceResult()) && (!GameConstants.isDemon(applyto.getJob()))) {
            switch (this.sourceid) {
                case 2100000:
                case 2200000:
                case 2300000:
                    if ((obj == null) || (obj.getType() != MapleMapObjectType.MONSTER)) {
                        return;
                    }
                    MapleMonster mob = (MapleMonster) obj;
                    if (!mob.getStats().isBoss()) {
                        int absorbMp = Math.min((int) (mob.getMobMaxMp() * (getX() / 100.0D)), mob.getMp());
                        if (absorbMp > 0) {
                            mob.setMp(mob.getMp() - absorbMp);
                            applyto.getStat().setMp(applyto.getStat().getMp() + absorbMp, applyto);
                            applyto.getClient().getSession().write(CField.EffectPacket.showOwnBuffEffect(this.sourceid, 1, applyto.getLevel(), this.level));
                            applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showBuffeffect(applyto.getId(), this.sourceid, 1, applyto.getLevel(), this.level), false);
                        }
                    }
                    break;
            }
        }
    }

    public final boolean applyTo(MapleCharacter chr) {
        return applyTo(chr, chr, true, null, ((Integer) this.info.get(MapleStatInfo.time)).intValue());
    }

    public final boolean applyTo(MapleCharacter chr, Point pos) {
        return applyTo(chr, chr, true, pos, ((Integer) this.info.get(MapleStatInfo.time)).intValue());
    }

    public final boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, Point pos, int newDuration) {
        if ((isHeal()) && ((applyfrom.getMapId() == 749040100) || (applyto.getMapId() == 749040100))) {
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        } else if (sourceid == 9101002 && !applyto.isGM() && (applyto.getId() != applyfrom.getId())) {
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        }
        if (((isSoaring_Mount()) && (applyfrom.getBuffedValue(MapleBuffStat.MONSTER_RIDING) == null)) || ((isSoaring_Normal()) && (!applyfrom.getMap().canSoar()))) {
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        }
        if ((this.sourceid == 4341006) && (applyfrom.getBuffedValue(MapleBuffStat.SHADOWPARTNER) == null)) {
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        }
        if ((this.sourceid == 33101008) && ((applyfrom.getBuffedValue(MapleBuffStat.RAINING_MINES) == null) || (applyfrom.getBuffedValue(MapleBuffStat.SUMMON) != null) || (!applyfrom.canSummon()))) {
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        }
        if (isShadow() && applyfrom.getJob() != 412 && applyfrom.getJob() != 422 && applyfrom.getJob() != 1412) { //pirate/shadow = dc
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        } else if (isMI() && applyfrom.getJob() != 434) { //pirate/shadow = dc
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        }
        if ((this.sourceid == 33101004) && (applyfrom.getMap().isTown())) {
            applyfrom.dropMessage(5, "You may not use this skill in towns.");
            applyfrom.getClient().getSession().write(CWvsContext.enableActions());
            return false;
        }
        int hpchange = calcHPChange(applyfrom, primary);
        int mpchange = calcMPChange(applyfrom, primary);

        PlayerStats stat = applyto.getStat();
        if (primary) {
            if ((((Integer) this.info.get(MapleStatInfo.itemConNo)).intValue() != 0) && (!applyto.isClone()) && (!applyto.inPVP())) {
                if (!applyto.haveItem(((Integer) this.info.get(MapleStatInfo.itemCon)).intValue(), ((Integer) this.info.get(MapleStatInfo.itemConNo)).intValue(), false, true)) {
                    applyto.getClient().getSession().write(CWvsContext.enableActions());
                    return false;
                }
                MapleInventoryManipulator.removeById(applyto.getClient(), GameConstants.getInventoryType(((Integer) this.info.get(MapleStatInfo.itemCon)).intValue()), ((Integer) this.info.get(MapleStatInfo.itemCon)).intValue(), ((Integer) this.info.get(MapleStatInfo.itemConNo)).intValue(), false, true);
            }
        } else if ((!primary) && (isResurrection())) {
            hpchange = stat.getMaxHp();
            applyto.setStance(0);
        }
        if ((isDispel()) && (makeChanceResult())) {
            applyto.dispelDebuffs();
        } else if (isHeroWill()) {
            applyto.dispelDebuffs();
        } else if (this.cureDebuffs.size() > 0) {
            for (MapleDisease debuff : this.cureDebuffs) {
                applyfrom.dispelDebuff(debuff);
            }
        } else if (isMPRecovery()) {
            int toDecreaseHP = stat.getMaxHp() / 100 * 10;
            if (stat.getHp() > toDecreaseHP) {
                hpchange += -toDecreaseHP;
                mpchange += toDecreaseHP / 100 * getY();
            } else {
                hpchange = stat.getHp() == 1 ? 0 : stat.getHp() - 1;
            }
        }
        Map hpmpupdate = new EnumMap(MapleStat.class);
        if (hpchange != 0) {
            if ((hpchange < 0) && (-hpchange > stat.getHp()) && (!applyto.hasDisease(MapleDisease.ZOMBIFY))) {
                applyto.getClient().getSession().write(CWvsContext.enableActions());
                return false;
            }
            stat.setHp(stat.getHp() + hpchange, applyto);
        }
        if (mpchange != 0) {
            if ((mpchange < 0) && (-mpchange > stat.getMp())) {
                applyto.getClient().getSession().write(CWvsContext.enableActions());
                return false;
            }

            if (((mpchange < 0) && (GameConstants.isDemon(applyto.getJob()))) || (!GameConstants.isDemon(applyto.getJob()))) {
                stat.setMp(stat.getMp() + mpchange, applyto);
            }
            hpmpupdate.put(MapleStat.MP, Long.valueOf(stat.getMp()));
        }
        hpmpupdate.put(MapleStat.HP, Long.valueOf(stat.getHp()));

        applyto.getClient().getSession().write(CWvsContext.updatePlayerStats(hpmpupdate, true, applyto));
        if (this.expinc != 0) {
            applyto.gainExp(this.expinc, true, true, false);
            applyto.getClient().getSession().write(CField.EffectPacket.showForeignEffect(20));
        } else if (this.sourceid / 10000 == 238) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            int mobid = ii.getCardMobId(this.sourceid);
            if (mobid > 0) {
                boolean done = applyto.getMonsterBook().monsterCaught(applyto.getClient(), mobid, MapleLifeFactory.getMonsterStats(mobid).getName());
                applyto.getClient().getSession().write(CWvsContext.getCard(done ? this.sourceid : 0, 1));
            }
        } else if (isReturnScroll()) {
            applyReturnScroll(applyto);
        } else if ((this.useLevel > 0) && (!this.skill)) {
            applyto.setExtractor(new MapleExtractor(applyto, this.sourceid, this.useLevel * 50, 1440));
            applyto.getMap().spawnExtractor(applyto.getExtractor());
        } else {
            if (isMistEruption()) {
                int i = ((Integer) this.info.get(MapleStatInfo.y)).intValue();
                for (MapleMist m : applyto.getMap().getAllMistsThreadsafe()) {
                    if ((m.getOwnerId() == applyto.getId()) && (m.getSourceSkill().getId() == 2111003)) {
                        if (m.getSchedule() != null) {
                            m.getSchedule().cancel(false);
                            m.setSchedule(null);
                        }
                        if (m.getPoisonSchedule() != null) {
                            m.getPoisonSchedule().cancel(false);
                            m.setPoisonSchedule(null);
                        }
                        applyto.getMap().broadcastMessage(CField.removeMist(m.getObjectId(), true));
                        applyto.getMap().removeMapObject(m);

                        i--;
                        if (i <= 0) {
                            break;
                        }
                    }
                }
            } else if (this.cosmetic > 0) {
                if (this.cosmetic >= 30000) {
                    applyto.setHair(this.cosmetic);
                    applyto.updateSingleStat(MapleStat.HAIR, this.cosmetic);
                } else if (this.cosmetic >= 20000) {
                    applyto.setFace(this.cosmetic);
                    applyto.updateSingleStat(MapleStat.FACE, this.cosmetic);
                } else if (this.cosmetic < 100) {
                    applyto.setSkinColor((byte) this.cosmetic);
                    applyto.updateSingleStat(MapleStat.SKIN, this.cosmetic);
                }
                applyto.equipChanged();
            } else if (this.bs > 0) {
                if (!applyto.inPVP()) {
                    return false;
                }
                int x = Integer.parseInt(applyto.getEventInstance().getProperty(String.valueOf(applyto.getId())));
                applyto.getEventInstance().setProperty(String.valueOf(applyto.getId()), String.valueOf(x + this.bs));
                applyto.getClient().getSession().write(CField.getPVPScore(x + this.bs, false));
            } else if (((Integer) this.info.get(MapleStatInfo.iceGageCon)).intValue() > 0) {
                if (!applyto.inPVP()) {
                    return false;
                }
                int x = Integer.parseInt(applyto.getEventInstance().getProperty("icegage"));
                if (x < ((Integer) this.info.get(MapleStatInfo.iceGageCon)).intValue()) {
                    return false;
                }
                applyto.getEventInstance().setProperty("icegage", String.valueOf(x - ((Integer) this.info.get(MapleStatInfo.iceGageCon)).intValue()));
                applyto.getClient().getSession().write(CField.getPVPIceGage(x - ((Integer) this.info.get(MapleStatInfo.iceGageCon)).intValue()));
                applyto.applyIceGage(x - ((Integer) this.info.get(MapleStatInfo.iceGageCon)).intValue());
            } else if (this.recipe > 0) {
                if ((applyto.getSkillLevel(this.recipe) > 0) || (applyto.getProfessionLevel(this.recipe / 10000 * 10000) < this.reqSkillLevel)) {
                    return false;
                }
                applyto.changeSingleSkillLevel(SkillFactory.getCraft(this.recipe), 2147483647, this.recipeUseCount, this.recipeValidDay > 0 ? System.currentTimeMillis() + this.recipeValidDay * 24L * 60L * 60L * 1000L : -1L);
            } else if (isComboRecharge()) {
                applyto.setCombo((short) Math.min(30000, applyto.getCombo() + ((Integer) this.info.get(MapleStatInfo.y)).intValue()));
                applyto.setLastCombo(System.currentTimeMillis());
                applyto.getClient().getSession().write(CField.rechargeCombo(applyto.getCombo()));
                SkillFactory.getSkill(21000000).getEffect(10).applyComboBuff(applyto, applyto.getCombo());
            } else if (isDragonBlink()) {
                MaplePortal portal = applyto.getMap().getPortal(Randomizer.nextInt(applyto.getMap().getPortals().size()));
                if (portal != null) {
                    applyto.getClient().getSession().write(CField.dragonBlink(portal.getId()));
                    applyto.getMap().movePlayer(applyto, portal.getPosition());
                    applyto.checkFollow();
                }
            } else if ((isSpiritClaw()) && (!applyto.isClone())) {
                MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
                boolean itemz = false;
                for (int i = 0; i < use.getSlotLimit(); i++) {
                    Item item = use.getItem((short) (byte) i);
                    if ((item != null)
                            && (GameConstants.isRechargable(item.getItemId())) && (item.getQuantity() >= 100)) {
                        MapleInventoryManipulator.removeFromSlot(applyto.getClient(), MapleInventoryType.USE, (short) i, (short) 100, false, true);
                        itemz = true;
                        break;
                    }
                }

                if (!itemz) {
                    return false;
                }
            } else if ((isSpiritBlast()) && (!applyto.isClone())) {
                MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
                boolean itemz = false;
                for (int i = 0; i < use.getSlotLimit(); i++) {
                    Item item = use.getItem((short) (byte) i);
                    if ((item != null)
                            && (GameConstants.isBullet(item.getItemId())) && (item.getQuantity() >= 100)) {
                        MapleInventoryManipulator.removeFromSlot(applyto.getClient(), MapleInventoryType.USE, (short) i, (short) 100, false, true);
                        itemz = true;
                        break;
                    }
                }

                if (!itemz) {
                    return false;
                }
            } else if ((this.cp != 0) && (applyto.getCarnivalParty() != null)) {
                applyto.getCarnivalParty().addCP(applyto, this.cp);
                applyto.CPUpdate(false, applyto.getAvailableCP(), applyto.getTotalCP(), 0);
                for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                    chr.CPUpdate(true, applyto.getCarnivalParty().getAvailableCP(), applyto.getCarnivalParty().getTotalCP(), applyto.getCarnivalParty().getTeam());
                }
            } else {
                MapleCarnivalFactory.MCSkill skil;
                MapleDisease dis;
                if ((this.nuffSkill != 0) && (applyto.getParty() != null)) {
                    skil = MapleCarnivalFactory.getInstance().getSkill(this.nuffSkill);
                    if (skil != null) {
                        dis = skil.getDisease();
                        for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                            if (((applyto.getParty() == null) || (chr.getParty() == null) || (chr.getParty().getId() != applyto.getParty().getId())) && ((skil.targetsAll) || (Randomizer.nextBoolean()))) {
                                if (dis == null) {
                                    chr.dispel();
                                } else if (skil.getSkill() == null) {
                                    chr.giveDebuff(dis, 1, 30000L, dis.getDisease(), 1);
                                } else {
                                    chr.giveDebuff(dis, skil.getSkill());
                                }
                                if (!skil.targetsAll) {
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    if (((this.effectedOnEnemy > 0) || (this.effectedOnAlly > 0)) && (primary) && (applyto.inPVP())) {
                        int eventType = Integer.parseInt(applyto.getEventInstance().getProperty("type"));
                        if ((eventType > 0) || (this.effectedOnEnemy > 0)) {
                            for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                                if ((chr.getId() != applyto.getId()) && (this.effectedOnAlly > 0 ? chr.getTeam() != applyto.getTeam() : (chr.getTeam() != applyto.getTeam()) || (eventType == 0))) {
                                    applyTo(applyto, chr, false, pos, newDuration);
                                }
                            }
                        }
                    } else if ((this.mobSkill > 0) && (this.mobSkillLevel > 0) && (primary) && (applyto.inPVP())) {
                        int eventType;
                        if (this.effectedOnEnemy > 0) {
                            eventType = Integer.parseInt(applyto.getEventInstance().getProperty("type"));
                            for (MapleCharacter chr : applyto.getMap().getCharactersThreadsafe()) {
                                if ((chr.getId() != applyto.getId()) && ((chr.getTeam() != applyto.getTeam()) || (eventType == 0))) {
                                    chr.disease(this.mobSkill, this.mobSkillLevel);
                                }
                            }
                        } else if ((this.sourceid == 2910000) || (this.sourceid == 2910001)) {
                            applyto.getClient().getSession().write(CField.EffectPacket.showOwnBuffEffect(this.sourceid, 13, applyto.getLevel(), this.level));
                            applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showBuffeffect(applyto.getId(), this.sourceid, 13, applyto.getLevel(), this.level), false);

                            applyto.getClient().getSession().write(CField.EffectPacket.showOwnCraftingEffect("UI/UIWindow2.img/CTF/Effect", 0, 0));
                            applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showCraftingEffect(applyto.getId(), "UI/UIWindow2.img/CTF/Effect", 0, 0), false);
                            if (applyto.getTeam() == this.sourceid - 2910000) {
                                if (this.sourceid == 2910000) {
                                    applyto.getEventInstance().broadcastPlayerMsg(-7, "The Red Team's flag has been restored.");
                                } else {
                                    applyto.getEventInstance().broadcastPlayerMsg(-7, "The Blue Team's flag has been restored.");
                                }
                                applyto.getMap().spawnAutoDrop(this.sourceid, (Point) ((Pair) applyto.getMap().getGuardians().get(this.sourceid - 2910000)).left);
                            } else {
                                applyto.disease(this.mobSkill, this.mobSkillLevel);
                                if (this.sourceid == 2910000) {
                                    applyto.getEventInstance().setProperty("redflag", String.valueOf(applyto.getId()));
                                    applyto.getEventInstance().broadcastPlayerMsg(-7, "The Red Team's flag has been captured!");
                                    applyto.getClient().getSession().write(CField.EffectPacket.showOwnCraftingEffect("UI/UIWindow2.img/CTF/Tail/Red", 600000, 0));
                                    applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showCraftingEffect(applyto.getId(), "UI/UIWindow2.img/CTF/Tail/Red", 600000, 0), false);
                                } else {
                                    applyto.getEventInstance().setProperty("blueflag", String.valueOf(applyto.getId()));
                                    applyto.getEventInstance().broadcastPlayerMsg(-7, "The Blue Team's flag has been captured!");
                                    applyto.getClient().getSession().write(CField.EffectPacket.showOwnCraftingEffect("UI/UIWindow2.img/CTF/Tail/Blue", 600000, 0));
                                    applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showCraftingEffect(applyto.getId(), "UI/UIWindow2.img/CTF/Tail/Blue", 600000, 0), false);
                                }
                            }
                        } else {
                            applyto.disease(this.mobSkill, this.mobSkillLevel);
                        }
                    } else if ((this.randomPickup != null) && (this.randomPickup.size() > 0)) {
                        MapleItemInformationProvider.getInstance().getItemEffect(((Integer) this.randomPickup.get(Randomizer.nextInt(this.randomPickup.size()))).intValue()).applyTo(applyto);
                    }
                }
            }
        }
        for (Map.Entry t : this.traits.entrySet()) {
            applyto.getTrait((MapleTrait.MapleTraitType) t.getKey()).addExp(((Integer) t.getValue()).intValue(), applyto);
        }
        SummonMovementType summonMovementType = getSummonMovementType();
        if ((summonMovementType != null) && ((this.sourceid != 32111006) || ((applyfrom.getBuffedValue(MapleBuffStat.REAPER) != null) && (!primary))) && (!applyto.isClone())) {
            int summId = this.sourceid;
            if (this.sourceid == 3111002) {
                Skill elite = SkillFactory.getSkill(3120012);
                if (applyfrom.getTotalSkillLevel(elite) > 0) {
                    return elite.getEffect(applyfrom.getTotalSkillLevel(elite)).applyTo(applyfrom, applyto, primary, pos, newDuration);
                }
            } else if (this.sourceid == 3211002) {
                Skill elite = SkillFactory.getSkill(3220012);
                if (applyfrom.getTotalSkillLevel(elite) > 0) {
                    return elite.getEffect(applyfrom.getTotalSkillLevel(elite)).applyTo(applyfrom, applyto, primary, pos, newDuration);
                }
            }
            MapleSummon tosummon = new MapleSummon(applyfrom, summId, getLevel(), new Point(pos == null ? applyfrom.getTruePosition() : pos), summonMovementType);
            applyfrom.cancelEffect(this, true, -1L, this.statups);
            applyfrom.getMap().spawnSummon(tosummon);
            applyfrom.addSummon(tosummon);
            tosummon.addHP(((Integer) this.info.get(MapleStatInfo.x)).shortValue());

            if (isBeholder()) {
                tosummon.addHP((short) 1);
            } else if (this.sourceid == 4341006) {
                applyfrom.cancelEffectFromBuffStat(MapleBuffStat.SHADOWPARTNER);
            } else {
                if (this.sourceid == 32111006) {
                    return true;
                }
                if (this.sourceid == 35111002) {
                    List count = new ArrayList();
                    List<MapleSummon> ss = applyfrom.getSummonsReadLock();
                    try {
                        for (MapleSummon s : ss) {
                            if (s.getSkill() == this.sourceid) {
                                count.add(Integer.valueOf(s.getObjectId()));
                            }
                        }
                    } finally {
                        applyfrom.unlockSummonsReadLock();
                    }
                    if (count.size() != 3) {
                        return true;
                    }
                    applyfrom.getClient().getSession().write(CField.skillCooldown(this.sourceid, getCooldown(applyfrom)));
                    applyfrom.addCooldown(this.sourceid, System.currentTimeMillis(), getCooldown(applyfrom) * 1000);
                    applyfrom.getMap().broadcastMessage(CField.teslaTriangle(applyfrom.getId(), ((Integer) count.get(0)).intValue(), ((Integer) count.get(1)).intValue(), ((Integer) count.get(2)).intValue()));
                } else if (this.sourceid == 35121003) {
                    applyfrom.getClient().getSession().write(CWvsContext.enableActions());
                }
            }
        } else if (isMechDoor()) {
            int newId = 0;
            boolean applyBuff = false;
            if (applyto.getMechDoors().size() >= 2) {
                MechDoor remove = (MechDoor) applyto.getMechDoors().remove(0);
                newId = remove.getId();
                applyto.getMap().broadcastMessage(CField.removeMechDoor(remove, true));
                applyto.getMap().removeMapObject(remove);
            } else {
                for (MechDoor d : applyto.getMechDoors()) {
                    if (d.getId() == newId) {
                        applyBuff = true;
                        newId = 1;
                        break;
                    }
                }
            }
            MechDoor door = new MechDoor(applyto, new Point(pos == null ? applyto.getTruePosition() : pos), newId);
            applyto.getMap().spawnMechDoor(door);
            applyto.addMechDoor(door);
            applyto.getClient().getSession().write(CWvsContext.mechPortal(door.getTruePosition()));
            if (!applyBuff) {
                return true;
            }
        }
        if ((primary) && (this.availableMap != null)) {
            for (Pair e : this.availableMap) {
                if ((applyto.getMapId() < ((Integer) e.left).intValue()) || (applyto.getMapId() > ((Integer) e.right).intValue())) {
                    applyto.getClient().getSession().write(CWvsContext.enableActions());
                    return true;
                }
            }
        }
        if ((this.overTime) && (!isEnergyCharge())) {
            applyBuffEffect(applyfrom, applyto, primary, newDuration);
        }
        if (this.skill) {
            removeMonsterBuff(applyfrom);
        }
        if (primary) {
            if (((this.overTime) || (isHeal())) && (!isEnergyCharge())) {
                applyBuff(applyfrom, newDuration);
            }
            if (isMonsterBuff()) {
                applyMonsterBuff(applyfrom);
            }
        }
        if (isMagicDoor()) {
            MapleDoor door = new MapleDoor(applyto, new Point(pos == null ? applyto.getTruePosition() : pos), this.sourceid);
            if (door.getTownPortal() != null) {
                applyto.getMap().spawnDoor(door);
                applyto.addDoor(door);

                MapleDoor townDoor = new MapleDoor(door);
                applyto.addDoor(townDoor);
                door.getTown().spawnDoor(townDoor);

                if (applyto.getParty() != null) {
                    applyto.silentPartyUpdate();
                }
            } else {
                applyto.dropMessage(5, "You may not spawn a door because all doors in the town are taken.");
            }
        } else if (isMist()) {
            Rectangle bounds = calculateBoundingBox(pos != null ? pos : applyfrom.getPosition(), applyfrom.isFacingLeft());
            MapleMist mist = new MapleMist(bounds, applyfrom, this);
            applyfrom.getMap().spawnMist(mist, getDuration(), false);
        } else if (isTimeLeap()) {
            for (MapleCoolDownValueHolder i : applyto.getCooldowns()) {
                if (i.skillId != 5121010) {
                    applyto.removeCooldown(i.skillId);
                    applyto.getClient().getSession().write(CField.skillCooldown(i.skillId, 0));
                }
            }
        } else {
            for (WeakReference chrz : applyto.getClones()) {
                if (chrz.get() != null) {
                    applyTo((MapleCharacter) chrz.get(), (MapleCharacter) chrz.get(), primary, pos, newDuration);
                }
            }
        }
        if ((this.fatigueChange != 0) && (applyto.getSummonedFamiliar() != null) && ((this.familiars == null) || (this.familiars.contains(Integer.valueOf(applyto.getSummonedFamiliar().getFamiliar()))))) {
            applyto.getSummonedFamiliar().addFatigue(applyto, this.fatigueChange);
        }
        if (this.rewardMeso != 0) {
            applyto.gainMeso(this.rewardMeso, false);
        }
        if ((this.rewardItem != null) && (this.totalprob > 0)) {
            for (Triple reward : this.rewardItem) {
                if ((MapleInventoryManipulator.checkSpace(applyto.getClient(), ((Integer) reward.left).intValue(), ((Integer) reward.mid).intValue(), "")) && (((Integer) reward.right).intValue() > 0) && (Randomizer.nextInt(this.totalprob) < ((Integer) reward.right).intValue())) {
                    if (GameConstants.getInventoryType(((Integer) reward.left).intValue()) == MapleInventoryType.EQUIP) {
                        Item item = MapleItemInformationProvider.getInstance().getEquipById(((Integer) reward.left).intValue());
                        item.setGMLog("Reward item (effect): " + this.sourceid + " on " + FileoutputUtil.CurrentReadable_Date());
                        MapleInventoryManipulator.addbyItem(applyto.getClient(), item);
                    } else {
                        MapleInventoryManipulator.addById(applyto.getClient(), ((Integer) reward.left).intValue(), ((Integer) reward.mid).shortValue(), "Reward item (effect): " + this.sourceid + " on " + FileoutputUtil.CurrentReadable_Date());
                    }
                }
            }
        }
        if ((this.familiarTarget == 2) && (applyfrom.getParty() != null) && (primary)) {
            for (MaplePartyCharacter mpc : applyfrom.getParty().getMembers()) {
                if ((mpc.getId() != applyfrom.getId()) && (mpc.getChannel() == applyfrom.getClient().getChannel()) && (mpc.getMapid() == applyfrom.getMapId()) && (mpc.isOnline())) {
                    MapleCharacter mc = applyfrom.getMap().getCharacterById(mpc.getId());
                    if (mc != null) {
                        applyTo(applyfrom, mc, false, null, newDuration);
                    }
                }
            }
        } else if ((this.familiarTarget == 3) && (primary)) {
            for (MapleCharacter mc : applyfrom.getMap().getCharactersThreadsafe()) {
                if (mc.getId() != applyfrom.getId()) {
                    applyTo(applyfrom, mc, false, null, newDuration);
                }
            }
        }
        return true;
    }

    public boolean isPhantomSkill() {
        return sourceid / 100 == 24;
    }

    public final boolean applyReturnScroll(MapleCharacter applyto) {
        if ((this.moveTo != -1) && ((applyto.getMap().getReturnMapId() != applyto.getMapId()) || (this.sourceid == 2031010) || (this.sourceid == 2030021))) {
            MapleMap target;
            if (this.moveTo == 999999999) {
                target = applyto.getMap().getReturnMap();
            } else {
                target = ChannelServer.getInstance(applyto.getClient().getChannel()).getMapFactory().getMap(this.moveTo);
                if ((target.getId() / 10000000 != 60) && (applyto.getMapId() / 10000000 != 61)
                        && (target.getId() / 10000000 != 21) && (applyto.getMapId() / 10000000 != 20)
                        && (target.getId() / 10000000 != applyto.getMapId() / 10000000)) {
                    return false;
                }

            }

            applyto.changeMap(target, target.getPortal(0));
            return true;
        }

        return false;
    }

    private boolean isSoulStone() {
        return ((this.skill) && (this.sourceid == 22181003)) || (this.sourceid == 24111002);
    }

    private void applyBuff(MapleCharacter applyfrom, int newDuration) {
        if ((isSoulStone()) && (this.sourceid != 24111002)) {
            if (applyfrom.getParty() != null) {
                int membrs = 0;
                for (MapleCharacter chr : applyfrom.getMap().getCharactersThreadsafe()) {
                    if ((!chr.isClone()) && (chr.getParty() != null) && (chr.getParty().getId() == applyfrom.getParty().getId()) && (chr.isAlive())) {
                        membrs++;
                    }
                }
                List<MapleCharacter> awarded = new ArrayList();
                while (awarded.size() < Math.min(membrs, ((Integer) this.info.get(MapleStatInfo.y)).intValue())) {
                    for (MapleCharacter chr : applyfrom.getMap().getCharactersThreadsafe()) {
                        if ((chr != null) && (!chr.isClone()) && (chr.isAlive()) && (chr.getParty() != null) && (chr.getParty().getId() == applyfrom.getParty().getId()) && (!awarded.contains(chr)) && (Randomizer.nextInt(((Integer) this.info.get(MapleStatInfo.y)).intValue()) == 0)) {
                            awarded.add(chr);
                        }
                    }
                }
                for (MapleCharacter chr : awarded) {
                    if (sourceid == 9101002) {
                        applyfrom.getClient().getSession().write(CWvsContext.enableActions());
                        break;
                    }
                    applyTo(applyfrom, chr, false, null, newDuration);
                    chr.getClient().getSession().write(CField.EffectPacket.showOwnBuffEffect(this.sourceid, 2, applyfrom.getLevel(), this.level));
                    chr.getMap().broadcastMessage(chr, CField.EffectPacket.showBuffeffect(chr.getId(), this.sourceid, 2, applyfrom.getLevel(), this.level), false);
                }
            }
        } else if ((isPartyBuff()) && ((applyfrom.getParty() != null) || (isGmBuff()) || (applyfrom.inPVP()))) {
            Rectangle bounds = calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
            List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(new MapleMapObjectType[]{MapleMapObjectType.PLAYER}));

            for (MapleMapObject affectedmo : affecteds) {
                final MapleCharacter affected = (MapleCharacter) affectedmo;
                if (sourceid == 9101002) {
                    applyfrom.getClient().getSession().write(CWvsContext.enableActions());
                    break;
                }
                if ((affected.getId() != applyfrom.getId()) && ((isGmBuff()) || ((applyfrom.inPVP()) && (affected.getTeam() == applyfrom.getTeam()) && (Integer.parseInt(applyfrom.getEventInstance().getProperty("type")) != 0)) || ((applyfrom.getParty() != null) && (affected.getParty() != null) && (applyfrom.getParty().getId() == affected.getParty().getId())))) {
                    if (((isResurrection()) && (!affected.isAlive())) || ((!isResurrection()) && (affected.isAlive()))) {
                        applyTo(applyfrom, affected, false, null, newDuration);
                        affected.getClient().getSession().write(CField.EffectPacket.showOwnBuffEffect(this.sourceid, 2, applyfrom.getLevel(), this.level));
                        affected.getMap().broadcastMessage(affected, CField.EffectPacket.showBuffeffect(affected.getId(), this.sourceid, 2, applyfrom.getLevel(), this.level), false);
                    }
                    if (isTimeLeap()) {
                        for (MapleCoolDownValueHolder i : affected.getCooldowns()) {
                            if (i.skillId != 5121010) {
                                affected.removeCooldown(i.skillId);
                                affected.getClient().getSession().write(CField.skillCooldown(i.skillId, 0));
                            }
                        }
                    }
                }
            }
        }
        MapleCharacter affected;
    }

    private void removeMonsterBuff(MapleCharacter applyfrom) {
        List<MonsterStatus> cancel = new ArrayList();
        switch (this.sourceid) {
            case 1111007:
            case 1211009:
            case 1311007:
                cancel.add(MonsterStatus.WEAPON_DEFENSE_UP);
                cancel.add(MonsterStatus.MAGIC_DEFENSE_UP);
                cancel.add(MonsterStatus.WEAPON_ATTACK_UP);
                cancel.add(MonsterStatus.MAGIC_ATTACK_UP);
                break;
            default:
                return;
        }
        Rectangle bounds = calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
        List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(new MapleMapObjectType[]{MapleMapObjectType.MONSTER}));
        int i = 0;

        for (MapleMapObject mo : affected) {
            if (makeChanceResult()) {
                for (MonsterStatus stat : cancel) {
                    ((MapleMonster) mo).cancelStatus(stat);
                }
            }
            i++;
            if (i >= ((Integer) this.info.get(MapleStatInfo.mobCount)).intValue()) {
                break;
            }
        }
    }

    public final void applyMonsterBuff(MapleCharacter applyfrom) {
        Rectangle bounds = calculateBoundingBox(applyfrom.getTruePosition(), applyfrom.isFacingLeft());
        boolean pvp = applyfrom.inPVP();
        MapleMapObjectType objType = pvp ? MapleMapObjectType.PLAYER : MapleMapObjectType.MONSTER;
        List<MapleMapObject> affected = this.sourceid == 35111005 ? applyfrom.getMap().getMapObjectsInRange(applyfrom.getTruePosition(), (1.0D / 0.0D), Arrays.asList(new MapleMapObjectType[]{objType})) : applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(new MapleMapObjectType[]{objType}));
        int i = 0;

        for (MapleMapObject mo : affected) {
            if (makeChanceResult()) {
                for (Map.Entry stat : getMonsterStati().entrySet()) {
                    if (pvp) {
                        MapleCharacter chr = (MapleCharacter) mo;
                        MapleDisease d = MonsterStatus.getLinkedDisease((MonsterStatus) stat.getKey());
                        if (d != null) {
                            chr.giveDebuff(d, ((Integer) stat.getValue()).intValue(), getDuration(), d.getDisease(), 1);
                        }
                    } else {
                        MapleMonster mons = (MapleMonster) mo;
                        if ((this.sourceid == 35111005) && (mons.getStats().isBoss())) {
                            break;
                        }
                        mons.applyStatus(applyfrom, new MonsterStatusEffect((MonsterStatus) stat.getKey(), (Integer) stat.getValue(), this.sourceid, null, false), isPoison(), isSubTime(this.sourceid) ? getSubTime() : getDuration(), true, this);
                    }
                }
                if ((pvp) && (this.skill)) {
                    MapleCharacter chr = (MapleCharacter) mo;
                    handleExtraPVP(applyfrom, chr);
                }
            }
            i++;
            if ((i >= ((Integer) this.info.get(MapleStatInfo.mobCount)).intValue()) && (this.sourceid != 35111005)) {
                break;
            }
        }
    }

    public final boolean isSubTime(int source) {
        switch (source) {
            case 1201006:
            case 23111008:
            case 23111009:
            case 23111010:
            case 31101003:
            case 31121003:
            case 31121005:
                return true;
        }
        return false;
    }

    public final void handleExtraPVP(MapleCharacter applyfrom, MapleCharacter chr) {
        if ((this.sourceid == 2311005) || (this.sourceid == 5121005) || (this.sourceid == 1201006) || ((GameConstants.isBeginnerJob(this.sourceid / 10000)) && (this.sourceid % 10000 == 104))) {
            long starttime = System.currentTimeMillis();

            int localsourceid = this.sourceid == 5121005 ? 90002000 : this.sourceid;
            Map localstatups = new EnumMap(MapleBuffStat.class);
            if (this.sourceid == 2311005) {
                localstatups.put(MapleBuffStat.MORPH, Integer.valueOf(7));
            } else if (this.sourceid == 1201006) {
                localstatups.put(MapleBuffStat.THREATEN_PVP, Integer.valueOf(this.level));
            } else if (this.sourceid == 5121005) {
                localstatups.put(MapleBuffStat.SNATCH, Integer.valueOf(1));
            } else {
                localstatups.put(MapleBuffStat.MORPH, this.info.get(MapleStatInfo.x));
            }
            chr.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(localsourceid, getDuration(), localstatups, this));
            chr.registerEffect(this, starttime, Timer.BuffTimer.getInstance().schedule(new CancelEffectAction(chr, this, starttime, localstatups), isSubTime(this.sourceid) ? getSubTime() : getDuration()), localstatups, false, getDuration(), applyfrom.getId());
        }
    }

    public final Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        return calculateBoundingBox(posFrom, facingLeft, this.lt, this.rb, ((Integer) this.info.get(MapleStatInfo.range)).intValue());
    }

    public final Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft, int addedRange) {
        return calculateBoundingBox(posFrom, facingLeft, this.lt, this.rb, ((Integer) this.info.get(MapleStatInfo.range)).intValue() + addedRange);
    }

    public static Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft, Point lt, Point rb, int range) {
        if ((lt == null) || (rb == null)) {
            return new Rectangle((facingLeft ? -200 - range : 0) + posFrom.x, -100 - range + posFrom.y, 200 + range, 100 + range);
        }
        Point myrb;
        Point mylt;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x - range, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(lt.x * -1 + posFrom.x + range, rb.y + posFrom.y);
            mylt = new Point(rb.x * -1 + posFrom.x, lt.y + posFrom.y);
        }
        return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
    }

    public final double getMaxDistanceSq() {
        int maxX = Math.max(Math.abs(this.lt == null ? 0 : this.lt.x), Math.abs(this.rb == null ? 0 : this.rb.x));
        int maxY = Math.max(Math.abs(this.lt == null ? 0 : this.lt.y), Math.abs(this.rb == null ? 0 : this.rb.y));
        return maxX * maxX + maxY * maxY;
    }

    public final void setDuration(int d) {
        this.info.put(MapleStatInfo.time, Integer.valueOf(d));
    }

    public final void silentApplyBuff(MapleCharacter chr, long starttime, int localDuration, Map<MapleBuffStat, Integer> statup, int cid) {
        chr.registerEffect(this, starttime, Timer.BuffTimer.getInstance().schedule(new CancelEffectAction(chr, this, starttime, statup), starttime + localDuration - System.currentTimeMillis()), statup, true, localDuration, cid);

        SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null) {
            MapleSummon tosummon = new MapleSummon(chr, this, chr.getTruePosition(), summonMovementType);
            if (!tosummon.isPuppet()) {
                chr.getMap().spawnSummon(tosummon);
                chr.addSummon(tosummon);
                tosummon.addHP(((Integer) this.info.get(MapleStatInfo.x)).shortValue());
                if (isBeholder()) {
                    tosummon.addHP((short) 1);
                }
            }
        }
    }

    public final void applyKAISER_Combo(MapleCharacter applyto, short combo) {
        EnumMap stat = new EnumMap(MapleBuffStat.class);
        stat.put(MapleBuffStat.KAISER_COMBO, Integer.valueOf(combo));
        applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(0, 99999, stat, this));
    }

    public final void applyXenon_Combo(MapleCharacter applyto, int combo) {
        EnumMap stat = new EnumMap(MapleBuffStat.class);
        stat.put(MapleBuffStat.Xenon_supply_surplus, Integer.valueOf(combo));
        applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(0, 99999, stat, this));
    }

    public final void applyComboBuff(MapleCharacter applyto, short combo) {
        EnumMap stat = new EnumMap(MapleBuffStat.class);
        stat.put(MapleBuffStat.ARAN_COMBO, Integer.valueOf(combo));
        applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, 99999, stat, this));

        long starttime = System.currentTimeMillis();

        applyto.registerEffect(this, starttime, null, applyto.getId());
    }

    public final void applyBlackBlessingBuff(MapleCharacter applyto, int combo) {
        EnumMap stat = new EnumMap(MapleBuffStat.class);
        stat.put(MapleBuffStat.Black_Blessing, Integer.valueOf(combo));
        applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, 99999, stat, this));
    }
//    public final void applyDaBuff(MapleCharacter applyto) {
//        EnumMap stat = new EnumMap(MapleBuffStat.class);
//
//        stat.put(MapleBuffStat.LIFE_TIDAL, applyto.getStat().getMaxHp());
//        applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, 99999999, stat, this));
//    }

  /*  public final void applyLunarTideBuff(MapleCharacter applyto) {
        EnumMap stat = new EnumMap(MapleBuffStat.class);
        double hpx = applyto.getStat().getMaxHp() / applyto.getStat().getHp();
        double mpx = applyto.getStat().getMaxMp() / applyto.getStat().getMp();
        stat.put(MapleBuffStat.Lunar_Tide, Integer.valueOf(hpx >= mpx ? 2 : 1));
        applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, 99999999, stat, this));
    } */

    public final void applyEnergyBuff(MapleCharacter applyto, boolean infinity, int targets) {
        long starttime = System.currentTimeMillis();
        if (infinity) {
            applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveEnergyChargeTest(0, ((Integer) this.info.get(MapleStatInfo.time)).intValue() / 1000, targets));
            applyto.registerEffect(this, starttime, null, applyto.getId());
        } else {
            EnumMap stat = new EnumMap(MapleBuffStat.class);
            stat.put(MapleBuffStat.ENERGY_CHARGE, Integer.valueOf(10000));
            applyto.cancelEffect(this, true, -1L, stat);
            applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveEnergyChargeTest(applyto.getId(), 10000, ((Integer) this.info.get(MapleStatInfo.time)).intValue() / 1000), false);
            CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime, stat);
            ScheduledFuture schedule = Timer.BuffTimer.getInstance().schedule(cancelAction, starttime + ((Integer) this.info.get(MapleStatInfo.time)).intValue() - System.currentTimeMillis());
            applyto.registerEffect(this, starttime, schedule, stat, false, ((Integer) this.info.get(MapleStatInfo.time)).intValue(), applyto.getId());
        }
    }

    private void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, int newDuration) {
        int localDuration = newDuration;
        int zz;
        EnumMap stat;
        if (primary) {
            localDuration = Math.max(newDuration, alchemistModifyVal(applyfrom, localDuration, false));
        }
        Map localstatups = this.statups;
        Map maskedStatups = null;
        boolean normal = true;
        boolean showEffect = primary;
        int maskedDuration = 0;
        switch (this.sourceid) {
            case 42101002:
                if (applyto.getHaku() != null) {
                    applyto.getHaku().sendstats();
                    applyto.getMap().broadcastMessage(applyto, CField.spawnHaku_change0(applyto.getId()), true);
                    applyto.getMap().broadcastMessage(applyto, CField.spawnHaku_change1(applyto.getHaku()), true);
                    applyto.getMap().broadcastMessage(applyto, CField.spawnHaku_bianshen(applyto.getId(), applyto.getHaku().getObjectId(), applyto.getHaku().getstats()), true);
                }
                break;
            case 61101002:
            case 61120007:
                if (applyfrom.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11) == null) {
                    normal = false;
                } else {
                    this.statups.put(MapleBuffStat.Tempest_Blades, Integer.valueOf(applyfrom.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11).getItemId()));
                    maskedStatups = new EnumMap(localstatups);
                    maskedStatups.clear();
                    maskedStatups.put(MapleBuffStat.Tempest_Blades, Integer.valueOf(applyfrom.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -11).getItemId()));
                }
                break;
            case 60001216:
            case 60001217:
                if (applyfrom.getStatForBuff(MapleBuffStat.KAISER_MODE_CHANGE) == null) {
                    break;
                }
                applyfrom.cancelEffectFromBuffStat(MapleBuffStat.KAISER_MODE_CHANGE);
                break;
 //           case 27110007:
 //               localstatups = new EnumMap(MapleBuffStat.class);
 //               double hpx = applyfrom.getStat().getMaxHp() / applyfrom.getStat().getHp();
 //               double mpx = applyfrom.getStat().getMaxMp() / applyfrom.getStat().getMp();
 //               localstatups.put(MapleBuffStat.Lunar_Tide, Integer.valueOf(hpx >= mpx ? 2 : 1));
 //               break;
//              case 4341052: 
//                         localstatups = new EnumMap(MapleBuffStat.class);
//                localstatups.put(MapleBuffStat.ASURA, 1);
//                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, localstatups, this));
//                normal = false;
//            break;
            case 4221013:
                localstatups = new EnumMap(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.ANGEL_ATK, this.info.get(MapleStatInfo.x));
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            case 31011001: {
                // set exceed to 0
             //   applyto.getClient().getSession().write(setExceed(short) 1);
            //    applyto.getClient().getSession().write(PlayerHandler.closeRangeAttack.setExceed());
                applyto.getClient().getSession().write(JobPacket.AvengerPacket.cancelExceed());
                applyto.addHP((int) ((applyto.getStat().getCurrentMaxHp() * (level / 100.0D)) * (getX() / 100.0D)));
                break;
            }
            case 5311004:
                zz = Randomizer.nextInt(4) + 1;
                applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto.getId(), this.sourceid, zz, -1, this.level), false);
                applyto.getClient().getSession().write(CField.EffectPacket.showOwnDiceEffect(this.sourceid, zz, -1, this.level));
                localstatups = new EnumMap(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.BARREL_ROLL, Integer.valueOf(zz));
                break;
            case 5211011:
            case 5211015:
            case 5211016:
                if (applyfrom.getTotalSkillLevel(5220019) <= 0) {
                    break;
                }
                SkillFactory.getSkill(5220019).getEffect(applyfrom.getTotalSkillLevel(5220019)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                break;
            case 42101001:
                SkillFactory.getSkill(42100010).getEffect(applyfrom.getTotalSkillLevel(42101001)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                normal = false;
                break;
            case 5111007:
            case 5211007:
            case 5311005:
            case 5711011:
            case 5811007:
            case 5911007:
            case 15111011:
            case 35111013:
                if (applyto.getStatForBuff(MapleBuffStat.DICE_ROLL) != null) {
                    applyto.cancelEffectFromBuffStat(MapleBuffStat.DICE_ROLL);
                }
                zz = Randomizer.nextInt(6) + 1;
                applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto.getId(), this.sourceid, zz, -1, this.level), false);
                applyto.getClient().getSession().write(CField.EffectPacket.showOwnDiceEffect(this.sourceid, zz, -1, this.level));
                if (zz <= 1) {
                    return;
                }
                localstatups = new EnumMap(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.DICE_ROLL, Integer.valueOf(zz));
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveDice(zz, this.sourceid, localDuration, localstatups));
                normal = false;
                showEffect = false;
                break;
            case 5120012:
            case 5220014:
            case 5320007:
            case 5720005:
            case 35120014:
                if (applyto.getStatForBuff(MapleBuffStat.DICE_ROLL) != null) {
                    applyto.cancelEffectFromBuffStat(MapleBuffStat.DICE_ROLL);
                }
                zz = Randomizer.nextInt(6) + 1;
                int zz2 = makeChanceResult() ? Randomizer.nextInt(6) + 1 : 0;
                applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto.getId(), this.sourceid, zz, zz2 > 0 ? -1 : 0, this.level), false);
                applyto.getClient().getSession().write(CField.EffectPacket.showOwnDiceEffect(this.sourceid, zz, zz2 > 0 ? -1 : 0, this.level));
                if ((zz <= 1) && (zz2 <= 1)) {
                    return;
                }
                int buffid = zz2 <= 1 ? zz : zz <= 1 ? zz2 : zz == zz2 ? zz * 100 : zz * 10 + zz2;
                if (buffid >= 100) {
                    applyto.dropMessage(-6, "[Double Lucky Dice] You have rolled a Double Down! (" + buffid / 100 + ")");
                } else if (buffid >= 10) {
                    applyto.dropMessage(-6, "[Double Lucky Dice] You have rolled two dice. (" + buffid / 10 + " and " + buffid % 10 + ")");
                }
                localstatups = new EnumMap(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.DICE_ROLL, Integer.valueOf(buffid));
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveDice(zz, this.sourceid, localDuration, localstatups));
                normal = false;
                showEffect = false;
                break;
            case 20031209:
            case 20031210:
                zz = Randomizer.nextInt(this.sourceid == 20031209 ? 2 : 5) + 1;
                int skillid = 24100003;
                if (applyto.getSkillLevel(24120002) > 0) {
                    skillid = 24120002;
                }
                applyto.setCardStack((byte) 0);
                applyto.resetRunningStack();
                applyto.addRunningStack(skillid == 24100003 ? 5 : 10);
                applyto.getMap().broadcastMessage(applyto, CField.gainCardStack(applyto.getId(), applyto.getRunningStack(), skillid == 24120002 ? 2 : 1, skillid, 0, skillid == 24100003 ? 5 : 10), true);
                applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showDiceEffect(applyto.getId(), this.sourceid, zz, -1, this.level), false);
                applyto.getClient().getSession().write(CField.EffectPacket.showOwnDiceEffect(this.sourceid, zz, -1, this.level));
                localstatups = new EnumMap(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.JUDGMENT_DRAW, Integer.valueOf(zz));
                if (zz == 5) {
                    localstatups.put(MapleBuffStat.ABSORB_DAMAGE_HP, this.info.get(MapleStatInfo.z));
                }
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, localstatups, this));
                normal = false;
                showEffect = false;
                break;
            case 33101006:
                applyto.clearLinkMid();
                MapleBuffStat theBuff = null;
                int theStat = ((Integer) this.info.get(MapleStatInfo.y)).intValue();
                switch (Randomizer.nextInt(6)) {
                    case 0:
                        theBuff = MapleBuffStat.CRITICAL_RATE_BUFF;
                        break;
                    case 1:
                        theBuff = MapleBuffStat.MP_BUFF;
                        break;
                    case 2:
                        theBuff = MapleBuffStat.DAMAGE_TAKEN_BUFF;
                        theStat = ((Integer) this.info.get(MapleStatInfo.x)).intValue();
                        break;
                    case 3:
                        theBuff = MapleBuffStat.DODGE_CHANGE_BUFF;
                        theStat = ((Integer) this.info.get(MapleStatInfo.x)).intValue();
                        break;
                    case 4:
                        theBuff = MapleBuffStat.DAMAGE_BUFF;
                        break;
                    case 5:
                        theBuff = MapleBuffStat.ATTACK_BUFF;
                }

                localstatups = new EnumMap(MapleBuffStat.class);
                localstatups.put(theBuff, Integer.valueOf(theStat));
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            case 8006:
            case 4321000:
            case 5121009:
            case 10008006:
            case 15111005:
            case 20008006:
            case 20018006:
            case 20028006:
            case 30008006:
            case 30018006:
                //     applyto.getMap().broadcastMessage(applyto, BuffPacket.giveForeignBuff(sourceid, statups, this), false);

                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(sourceid, localDuration, statups, this));
                applyto.getClient().getSession().write(CWvsContext.enableActions());
                normal = false;
                break;
            case 24121004:
                localstatups.put(MapleBuffStat.DAMAGE_BUFF, this.info.get(MapleStatInfo.damR));
                break;
            case 5211006:
            case 5220011:
            case 22151002:
                if (applyto.getFirstLinkMid() > 0) {
                    applyto.getClient().getSession().write(CWvsContext.BuffPacket.cancelHoming());
                    applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveHoming(this.sourceid, applyto.getFirstLinkMid(), 1));
                } else {
                    return;
                }
                normal = false;
                break;
            case 2120010:
            case 2220010:
            case 2320011:
                if (applyto.getFirstLinkMid() > 0) {
                    applyto.getClient().getSession().write(BuffPacket.giveBuff(sourceid, localDuration, statups, this));
                } else {
                    return;
                }
                normal = false;
                break;
            case 30001001:
            case 30011001:
                if (applyto.isHidden()) {
                    break;
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.INFILTRATE, Integer.valueOf(0));

                break;
            case 13101006:
                if (applyto.isHidden()) {
                    break;
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.DARKSIGHT, Integer.valueOf(1));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            case 4001003:
                if ((applyfrom.getTotalSkillLevel(4330001) > 0) && (((applyfrom.getJob() >= 430) && (applyfrom.getJob() <= 434)) || ((applyfrom.getJob() == 400) && (applyfrom.getSubcategory() == 1)))) {
                    SkillFactory.getSkill(4330001).getEffect(applyfrom.getTotalSkillLevel(4330001)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }

            case 4330001:
            case 14001003:
            case 20031211:
                if (applyto.isHidden()) {
                    return;
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.DARKSIGHT, Integer.valueOf(0));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            case 23111005:
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.WATER_SHIELD, this.info.get(MapleStatInfo.x));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
      //            case 36101003:
      //               stat = new EnumMap(MapleBuffStat.class);
      //               stat.put(MapleBuffStat.MP_R, this.info.get(MapleStatInfo.indieMmpR));
      //               stat.put(MapleBuffStat.HP_R, this.info.get(MapleStatInfo.indieMhpR));
      //               stat.put(MapleStatInfo.time, this.info.get(MapleStatInfo.time));
      //               break; 
            case 23101003:
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.SPIRIT_SURGE, this.info.get(MapleStatInfo.x));
                stat.put(MapleBuffStat.SPIRIT_damage, this.info.get(MapleStatInfo.damage));
      //     case 31211004: // Diabolic Recovery
      //          stat = new EnumMap(MapleBuffStat.class);
      //          stat.put(MapleBuffStat.DIABOLIC_RECOVERY, this.info.get(MapleStatInfo.x));
      //          stat.put(MapleBuffStat.HP_R, this.info.get(MapleStatInfo.indieMhpR));
               // break;
            case 32121003:
                if (applyto.isHidden()) {
                    break; //was break label5643?
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.TORNADO, this.info.get(MapleStatInfo.x));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            case 32111005:
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                Pair statt;
                int sourcez = 0;
                if (applyfrom.getStatForBuff(MapleBuffStat.DARK_AURA) != null) {
                    sourcez = 32001003;
                    statt = new Pair(MapleBuffStat.DARK_AURA, Integer.valueOf(this.level + 10 + applyto.getTotalSkillLevel(sourcez)));
                } else {
                    if (applyfrom.getStatForBuff(MapleBuffStat.YELLOW_AURA) != null) {
                        sourcez = 32101003;
                        statt = new Pair(MapleBuffStat.YELLOW_AURA, Integer.valueOf(applyto.getTotalSkillLevel(sourcez)));
                    } else {
                        if (applyfrom.getStatForBuff(MapleBuffStat.BLUE_AURA) != null) {
                            sourcez = 32111012;
                            localDuration = 10000;
                            statt = new Pair(MapleBuffStat.BLUE_AURA, Integer.valueOf(applyto.getTotalSkillLevel(sourcez)));
                        } else {
                            return;
                        }
                    }
                }
                localstatups = new EnumMap(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.BODY_BOOST, Integer.valueOf(this.level));
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, localstatups, this));
                localstatups.put(statt.left, statt.right);
                stat = new EnumMap(MapleBuffStat.class);
                stat.put((Enum) statt.left, statt.right);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA, applyfrom.getId());
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(sourcez, localDuration, stat, this));
                normal = false;
                break;
            case 32001003:
                if (applyfrom.getTotalSkillLevel(32120000) > 0) {
                    SkillFactory.getSkill(32120000).getEffect(applyfrom.getTotalSkillLevel(32120000)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }

            case 32110007:
            case 32120000:
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                EnumMap statx = new EnumMap(MapleBuffStat.class);
                statx.put(this.sourceid == 32110007 ? MapleBuffStat.BODY_BOOST : MapleBuffStat.AURA, Integer.valueOf(this.sourceid == 32120000 ? applyfrom.getTotalSkillLevel(32001003) : this.level));

                statx.clear();
                statx.put(MapleBuffStat.DARK_AURA, this.info.get(MapleStatInfo.x));
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, statx, this));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), statx, this), false);
                normal = false;
                break;
            case 32111012:
                if (applyfrom.getTotalSkillLevel(32110000) > 0) {
                    SkillFactory.getSkill(32110000).getEffect(applyfrom.getTotalSkillLevel(32110000)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }

            case 32110008:
                localDuration = 10000;
            case 32110000:
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                EnumMap statc = new EnumMap(MapleBuffStat.class);
                statc.put(this.sourceid == 32110008 ? MapleBuffStat.BODY_BOOST : MapleBuffStat.AURA, Integer.valueOf(this.sourceid == 32110000 ? applyfrom.getTotalSkillLevel(32111012) : this.level));

                statc.clear();
                statc.put(MapleBuffStat.BLUE_AURA, Integer.valueOf(this.level));
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, statc, this));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), statc, this), false);
                normal = false;
                break;
            case 32101003:
                if (applyfrom.getTotalSkillLevel(32120001) > 0) {
                    SkillFactory.getSkill(32120001).getEffect(applyfrom.getTotalSkillLevel(32120001)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }

            case 32110009:
            case 32120001:
                applyto.cancelEffectFromBuffStat(MapleBuffStat.YELLOW_AURA);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLUE_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.DARK_AURA, applyfrom.getId());
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BODY_BOOST);
                EnumMap statv = new EnumMap(MapleBuffStat.class);
                statv.put(this.sourceid == 32110009 ? MapleBuffStat.BODY_BOOST : MapleBuffStat.AURA, Integer.valueOf(this.sourceid == 32120001 ? applyfrom.getTotalSkillLevel(32101003) : this.level));

                statv.clear();
                statv.put(MapleBuffStat.YELLOW_AURA, Integer.valueOf(this.level));
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, statv, this));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), statv, this), false);
                normal = false;
                break;
            case 2121054:
                //  if ((applyto.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) && (applyto.getBuffSource(MapleBuffStat.WK_CHARGE) != this.sourceid)) {
                localstatups = new EnumMap(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.FIRE_AURA, Integer.valueOf(0));


                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveForeignBuff(this.sourceid, localstatups, this));
                normal = true;
                break;
            case 1211008:
                if ((applyto.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) && (applyto.getBuffSource(MapleBuffStat.WK_CHARGE) != this.sourceid)) {
                    localstatups = new EnumMap(MapleBuffStat.class);
                    localstatups.put(MapleBuffStat.LIGHTNING_CHARGE, Integer.valueOf(1));
                } else if (!applyto.isHidden()) {
                    EnumMap statb = new EnumMap(MapleBuffStat.class);
                    statb.put(MapleBuffStat.WK_CHARGE, Integer.valueOf(1));
                }

                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            case 35111004:
                if ((applyto.getBuffedValue(MapleBuffStat.MECH_CHANGE) != null) && (applyto.getBuffSource(MapleBuffStat.MECH_CHANGE) == 35121005)) {
                    SkillFactory.getSkill(35121013).getEffect(this.level).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }
                if (applyto.isHidden()) {
                    break; //was break label5643
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.MECH_CHANGE, Integer.valueOf(1));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            case 35001001:
            case 35101009:
            case 35121005:
            case 35121013:
                if (applyto.isHidden()) {
                    break; //gotta find out label 5643 some time
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.MECH_CHANGE, Integer.valueOf(1));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            case 1220013:
                if (applyto.isHidden()) {
                    break; //was break label 5643
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.DIVINE_SHIELD, Integer.valueOf(1));

                break;
            case 1111002:
            case 11111001:
                if (applyto.isHidden()) {
                    break; //was break label 5643
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.COMBO, Integer.valueOf(0));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            case 3101004:
            case 13101024:
            case 3201004:
            case 13101003:
                if (applyto.isHidden()) {
                    break; //was break label 5643
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.SOULARROW, Integer.valueOf(0));

                break;
            case 2321005:
                if (!GameConstants.GMS) {
                    break; //was break label 5643
                }
                applyto.cancelEffectFromBuffStat(MapleBuffStat.BLESS);
                break;
            case 4111002:
            case 4211008:
            case 4331002:
            case 14111000:
            case 36111006:
                if (applyto.isHidden()) {
                    break; //was break label 5643
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.SHADOWPARTNER, this.info.get(MapleStatInfo.x));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            case 15111006:
                localstatups = new EnumMap(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.SPARK, this.info.get(MapleStatInfo.x));
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            case 4341002:
                localstatups = new EnumMap(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.FINAL_CUT, this.info.get(MapleStatInfo.y));
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, localstatups, this));
                normal = false;
                break;
            case 3211005:
                if (applyfrom.getTotalSkillLevel(3220005) <= 0) {
                    break; //was break label 5643
                }
                SkillFactory.getSkill(3220005).getEffect(applyfrom.getTotalSkillLevel(3220005)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                break;
            case 3111005:
                if (applyfrom.getTotalSkillLevel(3120006) <= 0) {
                    break; //was break label 5643
                }
                SkillFactory.getSkill(3120006).getEffect(applyfrom.getTotalSkillLevel(3120006)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                break;
            case 1211004:

            case 1211006:
            case 1221004:
            case 11111007:
            case 15101006:
            case 21101006:
//          case 1211008:
            case 21111005:
            case 51111003:
                if (applyto.isHidden()) {
                    break; //was break label 5643
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.WK_CHARGE, Integer.valueOf(1));

                break;
            case 3120006:
            case 3220005:
                if (applyto.isHidden()) {
                    break; //was break label 5643
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.SPIRIT_LINK, Integer.valueOf(0));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            case 31121005:
                if (applyto.isHidden()) {
                    break; //was break label 5643
                }
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.DARK_METAMORPHOSIS, Integer.valueOf(6));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
                break;
            case 2121004:
            case 2221004:
            case 2321004:
                maskedDuration = alchemistModifyVal(applyfrom, 4000, false);
                break;
            case 4331003:
                localstatups = new EnumMap(MapleBuffStat.class);
                localstatups.put(MapleBuffStat.OWL_SPIRIT, this.info.get(MapleStatInfo.y));
                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, localstatups, this));
                applyto.setBattleshipHP(((Integer) this.info.get(MapleStatInfo.x)).intValue());
                normal = false;
                break;
            case 1121010:
                applyto.handleOrbconsume(10);
                break;
            case 2022746:
            case 2022747:
            case 2022823:
                if (applyto.isHidden()) {
                    break; //was break label 5643
                }
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), maskedStatups == null ? localstatups : maskedStatups, this), false);
                break;
            case 35001002:
                if (applyfrom.getTotalSkillLevel(35120000) > 0) {
                    SkillFactory.getSkill(35120000).getEffect(applyfrom.getTotalSkillLevel(35120000)).applyBuffEffect(applyfrom, applyto, primary, newDuration);
                    return;
                }
                break;
        }
        if (isPirateMorph()) {
            stat = new EnumMap(MapleBuffStat.class);
            stat.put(MapleBuffStat.MORPH, Integer.valueOf(getMorph(applyto)));
            applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
            applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.sourceid, localDuration, stat, this));
            maskedStatups = new EnumMap(localstatups);
            maskedStatups.remove(MapleBuffStat.MORPH);
            normal = false;
        } else if (isMorph()) {
//            if (!applyto.isHidden()) {
//                if (isIceKnight()) {
//                    stat = new EnumMap(MapleBuffStat.class);
//                    stat.put(MapleBuffStat.ICE_KNIGHT, Integer.valueOf(2));
//                    applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(0, localDuration, stat, this));
//                }
//                stat = new EnumMap(MapleBuffStat.class);
//                stat.put(MapleBuffStat.MORPH, Integer.valueOf(getMorph(applyto)));
//                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
//            }
        } else if (isInflation()) {
//            if (!applyto.isHidden()) {
//                stat = new EnumMap(MapleBuffStat.class);
//                stat.put(MapleBuffStat.GIANT_POTION, Integer.valueOf(this.inflation));
//                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
//            }
        } else if (this.charColor > 0) {
            if (!applyto.isHidden()) {
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.FAMILIAR_SHADOW, Integer.valueOf(1));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
            }
        } else if (isMonsterRiding()) {
            localDuration = 2100000000;
            localstatups = new EnumMap(this.statups);
            localstatups.put(MapleBuffStat.MONSTER_RIDING, Integer.valueOf(1));
            int mountid = parseMountInfo(applyto, this.sourceid);
            int mountid2 = parseMountInfo_Pure(applyto, this.sourceid);
            if ((mountid != 0) && (mountid2 != 0)) {
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.MONSTER_RIDING, Integer.valueOf(0));
                applyto.cancelEffectFromBuffStat(MapleBuffStat.POWERGUARD);
                applyto.cancelEffectFromBuffStat(MapleBuffStat.MANA_REFLECTION);

                applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveMount(mountid2, this.sourceid, stat));
                // applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.showMonsterRiding(applyto.getId(), mountid, sourceid), false);
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.showMonsterRiding(applyto.getId(), stat, mountid, this.sourceid), false);
            } else {
                return;
            }
            normal = false;
        } else if (isSoaring()) {
            if (!applyto.isHidden()) {
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.SOARING, Integer.valueOf(1));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
            }
        } else if (this.berserk > 0) {
            if (!applyto.isHidden()) {
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.PYRAMID_PQ, Integer.valueOf(0));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
            }
        } else if ((isBerserkFury()) || (this.berserk2 > 0)) {
            if (!applyto.isHidden()) {
                stat = new EnumMap(MapleBuffStat.class);
                stat.put(MapleBuffStat.BERSERK_FURY, Integer.valueOf(1));
                applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
            }
        } else if ((isDivineBody())
                && (!applyto.isHidden())) {
            stat = new EnumMap(MapleBuffStat.class);
            stat.put(MapleBuffStat.DIVINE_BODY, Integer.valueOf(1));
            applyto.getMap().broadcastMessage(applyto, CWvsContext.BuffPacket.giveForeignBuff(applyto.getId(), stat, this), false);
        }


        if ((showEffect) && (!applyto.isHidden())) {
            applyto.getMap().broadcastMessage(applyto, CField.EffectPacket.showBuffeffect(applyto.getId(), this.sourceid, 1, applyto.getLevel(), this.level), false);
        }
        if (isMechPassive()) {
            applyto.getClient().getSession().write(CField.EffectPacket.showOwnBuffEffect(this.sourceid - 1000, 1, applyto.getLevel(), this.level, (byte) 1));
        }
        if ((!isMonsterRiding()) && (!isMechDoor()) && (getSummonMovementType() == null)) {
            applyto.cancelEffect(this, true, -1L, localstatups);
        }

        if ((normal) && (localstatups.size() > 0)) {
            applyto.getClient().getSession().write(CWvsContext.BuffPacket.giveBuff(this.skill ? this.sourceid : -this.sourceid, localDuration, maskedStatups == null ? localstatups : maskedStatups, this));
        }
        long starttime = System.currentTimeMillis();
        CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime, localstatups);
        ScheduledFuture schedule = Timer.BuffTimer.getInstance().schedule(cancelAction, maskedDuration > 0 ? maskedDuration : localDuration);
        applyto.registerEffect(this, starttime, schedule, localstatups, false, localDuration, applyfrom.getId());
        if ((getpowerCon() > 0) && (applyto.getxenoncombo() >= getpowerCon())) {
            if (applyto.getBuffedValue(MapleBuffStat.SURPLUS) == null) {
                applyto.setxenoncombo((short) (applyto.getxenoncombo() - getpowerCon()));
                SkillFactory.getSkill(30020232).getEffect(1).applyXenon_Combo(applyto, applyto.getxenoncombo());
            }
        }
    }

    public static int parseMountInfo(final MapleCharacter player, final int skillid) {
        switch (skillid) {
            case 80001000:
                if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -118) != null && player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -119) != null) {
                    return player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -118).getItemId();
                }
                return parseMountInfo_Pure(player, skillid);
            default:
                return GameConstants.getMountItem(skillid, player);
        }
    }

    public static int parseMountInfo_Pure(final MapleCharacter player, final int skillid) {
        switch (skillid) {
            case 80001000:
                if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null && player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -19) != null) {
                    return player.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18).getItemId();
                }
                return 0;
            default:
                return GameConstants.getMountItem(skillid, player);
        }
    }

    private int calcHPChange(MapleCharacter applyfrom, boolean primary) {
        int hpchange = 0;
        if (((Integer) this.info.get(MapleStatInfo.hp)).intValue() != 0) {
            if (!this.skill) {
                if (primary) {
                    hpchange += alchemistModifyVal(applyfrom, ((Integer) this.info.get(MapleStatInfo.hp)).intValue(), true);
                } else {
                    hpchange += ((Integer) this.info.get(MapleStatInfo.hp)).intValue();
                }
                if (applyfrom.hasDisease(MapleDisease.ZOMBIFY)) {
                    hpchange /= 2;
                }
            } else {
                hpchange += makeHealHP(((Integer) this.info.get(MapleStatInfo.hp)).intValue() / 100.0D, applyfrom.getStat().getTotalMagic(), 3.0D, 5.0D);
                if (applyfrom.hasDisease(MapleDisease.ZOMBIFY)) {
                    hpchange = -hpchange;
                }
            }
        }
        if (this.hpR != 0.0D) {
            hpchange += (int) (applyfrom.getStat().getCurrentMaxHp() * this.hpR) / (applyfrom.hasDisease(MapleDisease.ZOMBIFY) ? 2 : 1);
        }

        if ((primary)
                && (((Integer) this.info.get(MapleStatInfo.hpCon)).intValue() != 0)) {
            hpchange -= ((Integer) this.info.get(MapleStatInfo.hpCon)).intValue();
        }

        switch (this.sourceid) {
            case 4211001:
                PlayerStats stat = applyfrom.getStat();
                int v42 = getY() + 100;
                int v38 = Randomizer.rand(1, 100) + 100;
                hpchange = (int) ((v38 * stat.getLuk() * 0.033D + stat.getDex()) * v42 * 0.002D);
                hpchange += makeHealHP(getY() / 100.0D, applyfrom.getStat().getTotalLuk(), 2.3D, 3.5D);
        }

        return hpchange;
    }

    private static int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
        return (int) (Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1) + (int) (stat * lowerfactor * rate));
    }

    private int calcMPChange(MapleCharacter applyfrom, boolean primary) {
        int mpchange = 0;
        if (((Integer) this.info.get(MapleStatInfo.mp)).intValue() != 0) {
            if (primary) {
                mpchange += alchemistModifyVal(applyfrom, ((Integer) this.info.get(MapleStatInfo.mp)).intValue(), false);
            } else {
                mpchange += ((Integer) this.info.get(MapleStatInfo.mp)).intValue();
            }
        }
        if (this.mpR != 0.0D) {
            mpchange += (int) (applyfrom.getStat().getCurrentMaxMp(applyfrom.getJob()) * this.mpR);
        }
        if (GameConstants.isDemon(applyfrom.getJob())) {
            mpchange = 0;
        }
        if (primary) {
            if ((((Integer) this.info.get(MapleStatInfo.mpCon)).intValue() != 0) && (!GameConstants.isDemon(applyfrom.getJob()))) {
                boolean free = false;
                if ((applyfrom.getJob() == 411) || (applyfrom.getJob() == 412)) {
                    Skill expert = SkillFactory.getSkill(4110012);
                    if (applyfrom.getTotalSkillLevel(expert) > 0) {
                        MapleStatEffect ret = expert.getEffect(applyfrom.getTotalSkillLevel(expert));
                        if (ret.makeChanceResult()) {
                            free = true;
                        }
                    }
                }
                if (applyfrom.getBuffedValue(MapleBuffStat.INFINITY) != null) {
                    mpchange = 0;
                } else if (!free) {
                    mpchange = (int) (mpchange - (((Integer) this.info.get(MapleStatInfo.mpCon)).intValue() - ((Integer) this.info.get(MapleStatInfo.mpCon)).intValue() * applyfrom.getStat().mpconReduce / 100) * (applyfrom.getStat().mpconPercent / 100.0D));
                }
            } else if ((((Integer) this.info.get(MapleStatInfo.forceCon)).intValue() != 0) && (GameConstants.isDemon(applyfrom.getJob()))) {
                if (applyfrom.getBuffedValue(MapleBuffStat.BOUNDLESS_RAGE) != null) {
                    mpchange = 0;
                } else {
                    mpchange -= ((Integer) this.info.get(MapleStatInfo.forceCon)).intValue();
                }
            }
        }

        return mpchange;
    }

    public final int alchemistModifyVal(MapleCharacter chr, int val, boolean withX) {
        if (!this.skill) {
            return val * (100 + (withX ? chr.getStat().RecoveryUP : chr.getStat().BuffUP)) / 100;
        }
        return val * (100 + (withX ? chr.getStat().RecoveryUP : chr.getStat().BuffUP_Skill + (getSummonMovementType() == null ? 0 : chr.getStat().BuffUP_Summon))) / 100;
    }

    public final void setSourceId(int newid) {
        this.sourceid = newid;
    }

    public final boolean isGmBuff() {
        switch (this.sourceid) {
            case 9001000:
            case 9001001:
            case 9001002:
            case 9001003:
            case 9001005:
            case 9001008:
            case 9101000:
            case 9101001:
            case 9101002:
            case 9101003:
            case 9101005:
            case 9101008:
            case 10001075:
                return true;
        }
        return (GameConstants.isBeginnerJob(this.sourceid / 10000)) && (this.sourceid % 10000 == 1005);
    }

    public final boolean isInflation() {
        return this.inflation > 0;
    }

    public final int getInflation() {
        return this.inflation;
    }

    public final boolean isEnergyCharge() {
        return (this.skill) && ((this.sourceid == 5110001) || (this.sourceid == 15100004));
    }

    public boolean isMonsterBuff() {
        switch (this.sourceid) {
            case 1111007:
            case 1201006:
            case 1211009:
            case 1311007:
            case 2101003:
            case 2111004:
            case 2201003:
            case 4121015: //���� �����
            case 2211004:
            case 2311005:
            case 4111003:
            case 4121004:
            case 4221004:
            case 4321002:
            case 4341003:
            case 5011002:
            case 12101001:
            case 12111002:
            case 14111001:
            case 22121000:
            case 22151001:
            case 22161002:
            case 32120000:
            case 32120001:
            case 35111005:
            case 90001002:
            case 90001003:
            case 90001004:
            case 90001005:
            case 90001006:
                return this.skill;
        }
        return false;
    }

    public final void setPartyBuff(boolean pb) {
        this.partyBuff = pb;
    }

    private boolean isPartyBuff() {
        if ((this.lt == null) || (this.rb == null) || (!this.partyBuff)) {
            return (isSoulStone()) && (this.sourceid != 24111002);
        }
        switch (this.sourceid) {
            case 1211003:
            case 1211004:
            case 1211005:
            case 1211006:
            case 1211007:
            case 1211008:
            case 1221003:
            case 1221004:
            case 4311001:
            case 4331003:
            case 4341002:
            case 11111007:
            case 12101005:
            case 35121005:
            case 51111003:
                return false;
        }
        if (GameConstants.isNoDelaySkill(this.sourceid)) {
            return false;
        }
        return true;
    }

    public final boolean isArcane() {
        return (this.skill) && ((this.sourceid == 2320011) || (this.sourceid == 2220010) || (this.sourceid == 2120010));
    }

    public final boolean isHeal() {
        return (this.skill) && ((this.sourceid == 2301002) || (this.sourceid == 9101000) || (this.sourceid == 9001000));
    }

    public final boolean isResurrection() {
        return (this.skill) && ((this.sourceid == 9001005) || (this.sourceid == 9101005) || (this.sourceid == 2321006));
    }

    public final boolean isTimeLeap() {
        return (this.skill) && (this.sourceid == 5121010);
    }

    public final int getHp() {
        return ((Integer) this.info.get(MapleStatInfo.hp)).intValue();
    }

    public final int getMp() {
        return ((Integer) this.info.get(MapleStatInfo.mp)).intValue();
    }

    public final int getDOTStack() {
        return ((Integer) this.info.get(MapleStatInfo.dotSuperpos)).intValue();
    }

    public final double getHpR() {
        return this.hpR;
    }

    public final double getMpR() {
        return this.mpR;
    }

    public final int getMastery() {
        return ((Integer) this.info.get(MapleStatInfo.mastery)).intValue();
    }

    public final int getWatk() {
        return ((Integer) this.info.get(MapleStatInfo.pad)).intValue();
    }

    public final int getMatk() {
        return ((Integer) this.info.get(MapleStatInfo.mad)).intValue();
    }

    public final int getWdef() {
        return ((Integer) this.info.get(MapleStatInfo.pdd)).intValue();
    }

    public final int getMdef() {
        return ((Integer) this.info.get(MapleStatInfo.mdd)).intValue();
    }

    public final int getAcc() {
        return ((Integer) this.info.get(MapleStatInfo.acc)).intValue();
    }

    public final int getAvoid() {
        return ((Integer) this.info.get(MapleStatInfo.eva)).intValue();
    }

    public final int getSpeed() {
        return ((Integer) this.info.get(MapleStatInfo.speed)).intValue();
    }

    public final int getJump() {
        return ((Integer) this.info.get(MapleStatInfo.jump)).intValue();
    }

    public final int getSpeedMax() {
        return ((Integer) this.info.get(MapleStatInfo.speedMax)).intValue();
    }

    public final int getPassiveSpeed() {
        return ((Integer) this.info.get(MapleStatInfo.psdSpeed)).intValue();
    }

    public final int getPassiveJump() {
        return ((Integer) this.info.get(MapleStatInfo.psdJump)).intValue();
    }

    public final int getDuration() {
        return ((Integer) this.info.get(MapleStatInfo.time)).intValue();
    }

    public final int getSubTime() {
        return ((Integer) this.info.get(MapleStatInfo.subTime)).intValue();
    }

    public final boolean isOverTime() {
        return this.overTime;
    }

    public final Map<MapleBuffStat, Integer> getStatups() {
        return this.statups;
    }

    public final boolean sameSource(MapleStatEffect effect) {
        boolean sameSrc = this.sourceid == effect.sourceid;
        switch (this.sourceid) {
            case 32120000:
                sameSrc = effect.sourceid == 32001003;
                break;
            case 32110000:
                sameSrc = effect.sourceid == 32111012;
                break;
            case 32120001:
                sameSrc = effect.sourceid == 32101003;
                break;
            case 35120000:
                sameSrc = effect.sourceid == 35001002;
                break;
            case 35121013:
                sameSrc = effect.sourceid == 35111004;
        }

        return (effect != null) && (sameSrc) && (this.skill == effect.skill);
    }

    public final int getCr() {
        return ((Integer) this.info.get(MapleStatInfo.cr)).intValue();
    }

    public final int getT() {
        return ((Integer) this.info.get(MapleStatInfo.t)).intValue();
    }

    public final int getU() {
        return ((Integer) this.info.get(MapleStatInfo.u)).intValue();
    }

    public final int getV() {
        return ((Integer) this.info.get(MapleStatInfo.v)).intValue();
    }

    public final int getW() {
        return ((Integer) this.info.get(MapleStatInfo.w)).intValue();
    }

    public final int getX() {
        return ((Integer) this.info.get(MapleStatInfo.x)).intValue();
    }

    public final int getY() {
        return ((Integer) this.info.get(MapleStatInfo.y)).intValue();
    }

    public final int getZ() {
        return ((Integer) this.info.get(MapleStatInfo.z)).intValue();
    }

    public final int getDamage() {
        return ((Integer) this.info.get(MapleStatInfo.damage)).intValue();
    }

    public final int getPVPDamage() {
        return ((Integer) this.info.get(MapleStatInfo.PVPdamage)).intValue();
    }

    public final int getAttackCount() {
        return ((Integer) this.info.get(MapleStatInfo.attackCount)).intValue();
    }

    public final int getBulletCount() {
        return ((Integer) this.info.get(MapleStatInfo.bulletCount)).intValue();
    }

    public final int getBulletConsume() {
        return ((Integer) this.info.get(MapleStatInfo.bulletConsume)).intValue();
    }

    public final int getMobCount() {
        return ((Integer) this.info.get(MapleStatInfo.mobCount)).intValue();
    }


    public final int getMoneyCon() {
        return this.moneyCon;
    }

    public final int getCooltimeReduceR() {
        return ((Integer) this.info.get(MapleStatInfo.coolTimeR)).intValue();
    }

    public final int getMesoAcquisition() {
        return ((Integer) this.info.get(MapleStatInfo.mesoR)).intValue();
    }

    public final int getCooldown(MapleCharacter chra) {
        return 0;
    }

    public final Map<MonsterStatus, Integer> getMonsterStati() {
        return this.monsterStatus;
    }

    public final int getBerserk() {
        return this.berserk;
    }

    public final boolean isHide() {
        return skill && (sourceid == 9001004 || sourceid == 9101004);
    }

    public final boolean isDragonBlood() {
        return (this.skill) && (this.sourceid == 1311008);
    }

    public final boolean isRecovery() {
        return (this.skill) && ((this.sourceid == 1001) || (this.sourceid == 10001001) || (this.sourceid == 20001001) || (this.sourceid == 20011001) || (this.sourceid == 20021001) || (this.sourceid == 11001) || (this.sourceid == 35121005));
    }

    public final boolean isBerserk() {
        return (this.skill) && (this.sourceid == 1320006);
    }

    public final boolean isBeholder() {
        return (this.skill) && (this.sourceid == 1321007);
    }

    public final boolean isMPRecovery() {
        return (this.skill) && (this.sourceid == 5101005);
    }

    public final boolean isInfinity() {
        return (this.skill) && ((this.sourceid == 2121004) || (this.sourceid == 2221004) || (this.sourceid == 2321004));
    }

    public final boolean isMonsterRiding_() {
        return skill && (sourceid == 80001000);
    }

    public final boolean isMonsterRiding() {
        return skill && (isMonsterRiding_() || GameConstants.getMountItem(sourceid, null) != 0);
    }

    public final boolean isMagicDoor() {
        return (this.skill) && ((this.sourceid == 2311002) || (this.sourceid % 10000 == 8001));
    }

    public final boolean isMesoGuard() {
        return (this.skill) && (this.sourceid == 4211005);
    }

    public final boolean isMechDoor() {
        return (this.skill) && (this.sourceid == 35101005);
    }

    public final boolean isComboRecharge() {
        return (this.skill) && (this.sourceid == 21111009);
    }

    public final boolean isDragonBlink() {
        return (this.skill) && (this.sourceid == 22141004);
    }
    
        public final int getOnActive() {
        return info.get(MapleStatInfo.onActive);
    }

    public final boolean isCharge() {
        switch (this.sourceid) {
            case 1211003:
            case 1211006:
            case 1211008:
            case 11111007:
            case 1221004:
            case 12101005:
            case 15101006:
            case 21111005:
            case 51111003:
                return this.skill;
        }
        return false;
    }

    public final boolean isPoison() {
        return (((Integer) this.info.get(MapleStatInfo.dot)).intValue() > 0) && (((Integer) this.info.get(MapleStatInfo.dotTime)).intValue() > 0);
    }

    public boolean isMist() {
        return (this.skill) && ((this.sourceid == 42111004) || sourceid == 4121015 || (this.sourceid == 42121005) || (this.sourceid == 2111003) || (this.sourceid == 4221006) || (this.sourceid == 12111005) || (this.sourceid == 14111006) || (this.sourceid == 22161003) || (this.sourceid == 32121006) || (this.sourceid == 1076) || (this.sourceid == 11076));
    }

    private boolean isSpiritClaw() {
        return ((this.skill) && (this.sourceid == 4111009)) || (this.sourceid == 14111007) || (this.sourceid == 5201008);
    }

    private boolean isSpiritBlast() {
        return (this.skill) && (this.sourceid == 5201008);
    }

    private boolean isDispel() {
        return (this.skill) && ((this.sourceid == 2311001) || (this.sourceid == 9001000) || (this.sourceid == 9101000));
    }

    private boolean isHeroWill() {
        switch (this.sourceid) {
            case 1121011:
            case 1221012:
            case 1321010:
            case 2121008:
            case 2221008:
            case 2321009:
            case 3121009:
            case 3221008:
            case 4121009:
            case 4221008:
            case 4341008:
            case 5121008:
            case 5221010:
            case 5321008:
            case 5721002:
            case 21121008:
            case 22171004:
            case 23121008:
            case 24121009:
            case 32121008:
            case 33121008:
            case 35121008:
            case 36121009:
            case 65121010:
            case 61121220:
            case 61121015:
                return this.skill;
        }
        return false;
    }

    public final boolean isAranCombo() {
        return this.sourceid == 21000000;
    }

    public final boolean isCombo() {
        switch (this.sourceid) {
            case 1111002:
            case 11111001:
                return this.skill;
        }
        return false;
    }

    public final boolean isPirateMorph() {
        switch (this.sourceid) {
            case 5111005:
            case 5121003:
            case 13111005:
            case 15111002:
                return this.skill;
        }
        return false;
    }

    public final boolean isMorph() {
        return this.morphId > 0;
    }

    public final int getMorph() {
        switch (this.sourceid) {
            case 5111005:
            case 15111002:
                return 1000;
            case 5121003:
                return 1001;
            case 5101007:
                return 1002;
            case 13111005:
                return 1003;
        }
        return this.morphId;
    }

    public final boolean isDivineBody() {
        return (this.skill) && (GameConstants.isBeginnerJob(this.sourceid / 10000)) && (this.sourceid % 10000 == 1010);
    }

    public final boolean isDivineShield() {
        switch (this.sourceid) {
            case 1220013:
                return this.skill;
        }
        return false;
    }

    public final boolean isBerserkFury() {
        return (this.skill) && (GameConstants.isBeginnerJob(this.sourceid / 10000)) && (this.sourceid % 10000 == 1011);
    }

    public final int getMorph(MapleCharacter chr) {
        int morph = getMorph();
        switch (morph) {
            case 1000:
            case 1001:
            case 1003:
                return morph + (chr.getGender() == 1 ? 100 : 0);
            case 1002:
        }
        return morph;
    }

    public final byte getLevel() {
        return this.level;
    }

    public final SummonMovementType getSummonMovementType() {
        if (!this.skill) {
            return null;
        }
        switch (this.sourceid) {
            case 3111002:
            case 3120012:
            case 36121002:
            case 36121013:
            case 36121014:
            case 13111024:
            case 3211002:
            case 3220012:
            case 4111007:
            case 4211007:
            case 4341006:
            case 5211001:
            case 5211014:
            case 5220002:
            case 5320011:
            case 5321003:
            case 5321004:
            case 5711001:
            case 33101008:
            case 33111003:
            case 35111002:
            case 35111005:
            case 35111011:
            case 35121003:
            case 35121009:
            case 35121010:
            case 35121011:
            case 42100010:
            case 61111002:
                return SummonMovementType.STATIONARY;
            case 3101007:
            case 3111005:
            case 3121006:
            case 3201007:
            case 3211005:
            case 3221005:
            case 23111008:
            case 23111009:
            case 23111010:
            case 33111005:
                return SummonMovementType.CIRCLE_FOLLOW;
            case 5211002:
                return SummonMovementType.CIRCLE_STATIONARY;
            case 5211011:
            case 5211015:
            case 5211016:
            case 32111006:
                return SummonMovementType.WALK_STATIONARY;
            case 1321007:
            case 2121005:
            case 2221005:
            case 2321003:
            case 11001004:
            case 12001004:
            case 12111004:
            case 13001004:
            case 14001005:
            case 22171052:
            case 42111003:
            case 42101021:
            case 42121021:
            case 15001004:
            case 35111001:
            case 35111009:
            case 35111010:
                return SummonMovementType.FOLLOW;
        }
        if (isAngel()) {
            return SummonMovementType.FOLLOW;
        }
        return null;
    }

    public final boolean isAngel() {
        return GameConstants.isAngel(this.sourceid);
    }

    public final boolean isSkill() {
        return this.skill;
    }

    public final int getSourceId() {
        return this.sourceid;
    }

    public final boolean isIceKnight() {
        return (this.skill) && (GameConstants.isBeginnerJob(this.sourceid / 10000)) && (this.sourceid % 10000 == 1105);
    }

    public final boolean isSoaring() {
        return (isSoaring_Normal()) || (isSoaring_Mount());
    }

    public final boolean isSoaring_Normal() {
        return (this.skill) && (GameConstants.isBeginnerJob(this.sourceid / 10000)) && (this.sourceid % 10000 == 1026);
    }

    public final boolean isSoaring_Mount() {
        return (this.skill) && (((GameConstants.isBeginnerJob(this.sourceid / 10000)) && (this.sourceid % 10000 == 1142)) || (this.sourceid == 80001089));
    }

    public final boolean isFinalAttack() {
        switch (this.sourceid) {
            case 11101002:
            case 13101002:
                return this.skill;
        }
        return false;
    }

    public final boolean isMistEruption() {
        switch (this.sourceid) {
            case 2121003:
                return this.skill;
        }
        return false;
    }

    public final boolean isShadow() {
        switch (this.sourceid) {
            case 4111002:
            case 4211008:
         //   case 4331002:
            case 14111000:
                // case 36111006:
                return this.skill;
        }
        return false;
    }

    public final boolean isMI() {
        switch (sourceid) {
            case 4331002: // shadowpartner
                return skill;
        }
        return false;
    }

    public final boolean isMechPassive() {
        switch (this.sourceid) {
            case 35121013:
                return true;
        }
        return false;
    }

    public final boolean makeChanceResult() {
        return (((Integer) this.info.get(MapleStatInfo.prop)).intValue() >= 100) || (Randomizer.nextInt(100) < ((Integer) this.info.get(MapleStatInfo.prop)).intValue());
    }

    public final int getProb() {
        return ((Integer) this.info.get(MapleStatInfo.prop)).intValue();
    }

    public final short getIgnoreMob() {
        return this.ignoreMob;
    }

    public final int getEnhancedHP() {
        return ((Integer) this.info.get(MapleStatInfo.emhp)).intValue();
    }

    public final int getEnhancedMP() {
        return ((Integer) this.info.get(MapleStatInfo.emmp)).intValue();
    }

    public final int getEnhancedWatk() {
        return ((Integer) this.info.get(MapleStatInfo.epad)).intValue();
    }

    public final int getEnhancedWdef() {
        return ((Integer) this.info.get(MapleStatInfo.pdd)).intValue();
    }

    public final int getEnhancedMatk() {
        return ((Integer) this.info.get(MapleStatInfo.emad)).intValue();
    }

    public final int getEnhancedMdef() {
        return ((Integer) this.info.get(MapleStatInfo.emdd)).intValue();
    }

    public final int getDOT() {
        return ((Integer) this.info.get(MapleStatInfo.dot)).intValue();
    }

    public final int getDOTTime() {
        return ((Integer) this.info.get(MapleStatInfo.dotTime)).intValue();
    }

    public final int getCriticalMax() {
        return ((Integer) this.info.get(MapleStatInfo.criticaldamageMax)).intValue();
    }

    public final int getCriticalMin() {
        return ((Integer) this.info.get(MapleStatInfo.criticaldamageMin)).intValue();
    }

    public final int getASRRate() {
        return ((Integer) this.info.get(MapleStatInfo.asrR)).intValue();
    }

    public final int getTERRate() {
        return ((Integer) this.info.get(MapleStatInfo.terR)).intValue();
    }

    public final int getDAMRate() {
        return ((Integer) this.info.get(MapleStatInfo.damR)).intValue();
    }

    public final int getHpToDamage() {
        return ((Integer) this.info.get(MapleStatInfo.mhp2damX)).intValue();
    }

    public final int getMpToDamage() {
        return ((Integer) this.info.get(MapleStatInfo.mmp2damX)).intValue();
    }

    public final int getLevelToDamage() {
        return ((Integer) this.info.get(MapleStatInfo.lv2damX)).intValue();
    }

    public final int getLevelToWatk() {
        return ((Integer) this.info.get(MapleStatInfo.lv2pdX)).intValue();
    }

    public final int getLevelToMatk() {
        return ((Integer) this.info.get(MapleStatInfo.lv2mdX)).intValue();
    }

    public final int getEXPLossRate() {
        return ((Integer) this.info.get(MapleStatInfo.expLossReduceR)).intValue();
    }

    public final int getBuffTimeRate() {
        return ((Integer) this.info.get(MapleStatInfo.bufftimeR)).intValue();
    }

    public final int getSuddenDeathR() {
        return ((Integer) this.info.get(MapleStatInfo.suddenDeathR)).intValue();
    }

    public final int getPercentAcc() {
        return ((Integer) this.info.get(MapleStatInfo.accR)).intValue();
    }

    public final int getPercentAvoid() {
        return ((Integer) this.info.get(MapleStatInfo.evaR)).intValue();
    }

    public final int getSummonTimeInc() {
        return ((Integer) this.info.get(MapleStatInfo.summonTimeR)).intValue();
    }

    public final int getMPConsumeEff() {
        return ((Integer) this.info.get(MapleStatInfo.mpConEff)).intValue();
    }

    public final short getMesoRate() {
        return this.mesoR;
    }

    public final int getEXP() {
        return this.exp;
    }

    public final int getAttackX() {
        return ((Integer) this.info.get(MapleStatInfo.padX)).intValue();
    }

    public final int getMagicX() {
        return ((Integer) this.info.get(MapleStatInfo.madX)).intValue();
    }

    public final int getPercentHP() {
        return ((Integer) this.info.get(MapleStatInfo.mhpR)).intValue();
    }

    public final int getPercentMP() {
        return ((Integer) this.info.get(MapleStatInfo.mmpR)).intValue();
    }

    public final int getConsume() {
        return this.consumeOnPickup;
    }

    public final int getSelfDestruction() {
        return ((Integer) this.info.get(MapleStatInfo.selfDestruction)).intValue();
    }

    public final int getCharColor() {
        return this.charColor;
    }

    public final List<Integer> getPetsCanConsume() {
        return this.petsCanConsume;
    }

    public final boolean isReturnScroll() {
        return (this.skill) && ((this.sourceid == 80001040) || (this.sourceid == 20021110) || (this.sourceid == 20031203));
    }

    public final boolean isMechChange() {
        switch (this.sourceid) {
            case 35001001:
            case 35101009:
            case 35111004:
            case 35121005:
            case 35121013:
                return this.skill;
        }
        return false;
    }

    public final int getRange() {
        return ((Integer) this.info.get(MapleStatInfo.range)).intValue();
    }

    public final int getER() {
        return ((Integer) this.info.get(MapleStatInfo.er)).intValue();
    }

    public final int getPrice() {
        return ((Integer) this.info.get(MapleStatInfo.price)).intValue();
    }

    public final int getExtendPrice() {
        return ((Integer) this.info.get(MapleStatInfo.extendPrice)).intValue();
    }

    public final int getPeriod() {
        return ((Integer) this.info.get(MapleStatInfo.period)).intValue();
    }

    public final int getReqGuildLevel() {
        return ((Integer) this.info.get(MapleStatInfo.reqGuildLevel)).intValue();
    }

    public final byte getEXPRate() {
        return this.expR;
    }

    public final short getLifeID() {
        return this.lifeId;
    }

    public final short getUseLevel() {
        return this.useLevel;
    }

    public final byte getSlotCount() {
        return this.slotCount;
    }

    public final int getStr() {
        return ((Integer) this.info.get(MapleStatInfo.str)).intValue();
    }

    public final int getStrX() {
        return ((Integer) this.info.get(MapleStatInfo.strX)).intValue();
    }

    public final int getDex() {
        return ((Integer) this.info.get(MapleStatInfo.dex)).intValue();
    }

    public final int getDexX() {
        return ((Integer) this.info.get(MapleStatInfo.dexX)).intValue();
    }

    public final int getInt() {
        return ((Integer) this.info.get(MapleStatInfo.int_)).intValue();
    }

    public final int getIntX() {
        return ((Integer) this.info.get(MapleStatInfo.intX)).intValue();
    }

    public final int getLuk() {
        return ((Integer) this.info.get(MapleStatInfo.luk)).intValue();
    }

    public final int getLukX() {
        return ((Integer) this.info.get(MapleStatInfo.lukX)).intValue();
    }

    public final int getMaxHpX() {
        return ((Integer) this.info.get(MapleStatInfo.mhpX)).intValue();
    }

    public final int getMaxMpX() {
        return ((Integer) this.info.get(MapleStatInfo.mmpX)).intValue();
    }

    public final short getpowerCon() {
        return this.powerCon;
    }

    public final int getAccX() {
        return ((Integer) this.info.get(MapleStatInfo.accX)).intValue();
    }

    public final int getMPConReduce() {
        return ((Integer) this.info.get(MapleStatInfo.mpConReduce)).intValue();
    }

    public final int getIndieMHp() {
        return ((Integer) this.info.get(MapleStatInfo.indieMhp)).intValue();
    }

    public final int getIndieMMp() {
        return ((Integer) this.info.get(MapleStatInfo.indieMmp)).intValue();
    }

    public final int getIndieAllStat() {
        return ((Integer) this.info.get(MapleStatInfo.indieAllStat)).intValue();
    }

    public final byte getType() {
        return this.type;
    }

    public int getBossDamage() {
        return ((Integer) this.info.get(MapleStatInfo.bdR)).intValue();
    }

    public int getInterval() {
        return this.interval;
    }

    public ArrayList<Pair<Integer, Integer>> getAvailableMaps() {
        return this.availableMap;
    }

    public int getWDEFRate() {
        return ((Integer) this.info.get(MapleStatInfo.pddR)).intValue();
    }

    public int getMDEFRate() {
        return ((Integer) this.info.get(MapleStatInfo.mddR)).intValue();
    }

    public final boolean isUnstealable() {
        for (MapleBuffStat b : this.statups.keySet()) {
            if (b == MapleBuffStat.MAPLE_WARRIOR) {
                return true;
            }
        }
        return this.sourceid == 4221013;
    }

    public static class CancelEffectAction
            implements Runnable {

        private final MapleStatEffect effect;
        private final WeakReference<MapleCharacter> target;
        private final long startTime;
        private final Map<MapleBuffStat, Integer> statup;

        public CancelEffectAction(MapleCharacter target, MapleStatEffect effect, long startTime, Map<MapleBuffStat, Integer> statup) {
            this.effect = effect;
            this.target = new WeakReference(target);
            this.startTime = startTime;
            this.statup = statup;
        }

        public void run() {
            MapleCharacter realTarget = (MapleCharacter) this.target.get();
            if ((realTarget != null) && (!realTarget.isClone())) {
                realTarget.cancelEffect(this.effect, false, this.startTime, this.statup);
            }
        }
    }

    public final boolean isEpicAdventure() {
        switch (sourceid) {
            case 1121053:
            case 1221053:
            case 1321053:
            case 2121053:
            case 2221053:
            case 2321053:
            case 3121053:
            case 3221053:
            case 4121053:
            case 4221053:
            case 4341053:
            case 5121053:
            case 5221053:
            case 5321053: //���� ��庥��
            case 27121053: //������� ����[Hyper]
            case 21121053: //[�ƶ�-Hyper]
            case 22171053: //[����-Hyper]
            case 23121053: //[�޸�������-Hyper]
            case 24121053: //[����-Hyper]
                return true;
        }
        return false;
    }
}