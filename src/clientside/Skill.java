package clientside;

import constants.GameConstants;
import java.util.ArrayList;
import java.util.List;
import provider.MapleData;
import provider.MapleDataTool;
import server.MapleStatEffect;
import server.Randomizer;
import server.life.Element;
import tools.Pair;

public class Skill {

    private String name = "";
    private final List<MapleStatEffect> effects = new ArrayList();
    private List<MapleStatEffect> pvpEffects = null;
    private List<Integer> animation = null;
    private final List<Pair<Integer, Byte>> requiredSkill = new ArrayList();
    private Element element = Element.NEUTRAL;
    private int id;
    private int animationTime = 0;
    private int masterLevel = 0;
    private int maxLevel = 0;
    private int delay = 0;
    private int trueMax = 0;
    private int eventTamingMob = 0;
    private int skillType = 0;
    private boolean invisible = false;
    private boolean hyper = false;
    private boolean chargeskill = false;
    private boolean timeLimited = false;
    private boolean combatOrders = false;
    private boolean pvpDisabled = false;
    private boolean magic = false;
    private boolean casterMove = false;
    private boolean pushTarget = false;
    private boolean pullTarget = false;

    public Skill(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static final Skill loadFromData(int id, MapleData data, MapleData delayData) {
        Skill ret = new Skill(id);

        boolean isBuff = false;
        int skillType = MapleDataTool.getInt("skillType", data, -1);
        String elem = MapleDataTool.getString("elemAttr", data, null);
        if (elem != null) {
            ret.element = Element.getFromChar(elem.charAt(0));
        }
        ret.skillType = skillType;
        ret.invisible = (MapleDataTool.getInt("invisible", data, 0) > 0);
        ret.timeLimited = (MapleDataTool.getInt("timeLimited", data, 0) > 0);
        ret.combatOrders = (MapleDataTool.getInt("combatOrders", data, 0) > 0);
        ret.masterLevel = MapleDataTool.getInt("masterLevel", data, 0);
        if ((id == 22111001) || (id == 22140000) || (id == 22141002)) {
            ret.masterLevel = 5;
        }
        ret.eventTamingMob = MapleDataTool.getInt("eventTamingMob", data, 0);
        MapleData inf = data.getChildByPath("info");
        if (inf != null) {
            ret.pvpDisabled = (MapleDataTool.getInt("pvp", inf, 1) <= 0);
            ret.magic = (MapleDataTool.getInt("magicDamage", inf, 0) > 0);
            ret.casterMove = (MapleDataTool.getInt("casterMove", inf, 0) > 0);
            ret.pushTarget = (MapleDataTool.getInt("pushTarget", inf, 0) > 0);
            ret.pullTarget = (MapleDataTool.getInt("pullTarget", inf, 0) > 0);
        }
        MapleData effect = data.getChildByPath("effect");
        if (skillType == 2) {
            isBuff = true;
        } else if (skillType == 3) {
            ret.animation = new ArrayList();
            ret.animation.add(Integer.valueOf(0));
            isBuff = effect != null;
        } else {
            MapleData action_ = data.getChildByPath("action");
            MapleData hit = data.getChildByPath("hit");
            MapleData ball = data.getChildByPath("ball");

            boolean action = false;
            if ((action_ == null)
                    && (data.getChildByPath("prepare/action") != null)) {
                action_ = data.getChildByPath("prepare/action");
                action = true;
            }

            isBuff = (effect != null) && (hit == null) && (ball == null);
            String d;
            if (action_ != null) {
                d = null;
                if (action) {
                    d = MapleDataTool.getString(action_, null);
                } else {
                    d = MapleDataTool.getString("0", action_, null);
                }
                if (d != null) {
                    isBuff |= d.equals("alert2");
                    MapleData dd = delayData.getChildByPath(d);
                    if (dd != null) {
                        for (MapleData del : dd) {
                            ret.delay += Math.abs(MapleDataTool.getInt("delay", del, 0));
                        }
                        if (ret.delay > 30) {
                            ret.delay = ((int) Math.round(ret.delay * 11.0D / 16.0D));
                            ret.delay -= ret.delay % 30;
                        }
                    }
                    if (SkillFactory.getDelay(d) != null) {
                        ret.animation = new ArrayList();
                        ret.animation.add(SkillFactory.getDelay(d));
                        if (!action) {
                            for (MapleData ddc : action_) {
                                if (!MapleDataTool.getString(ddc, d).equals(d)) {
                                    String c = MapleDataTool.getString(ddc);
                                    if (SkillFactory.getDelay(c) != null) {
                                        ret.animation.add(SkillFactory.getDelay(c));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            switch (id) {
                case 1076:
                case 11076:
                case 2111002:
                case 2111003:
                case 2121001:
                case 2221001:
                case 2301002:
                case 4121015:
                case 2321001:
                case 4211001:
                case 12111005:
                case 22161003:
                case 32121006:
                    isBuff = false;
                    break;
                case 93:
                case 1004:
                case 4121054:
                case 1026:
                case 1111002:
                case 65111100:
                case 1111007:
                case 1211009:
                case 4341054:
                case 1220013:
                case 5321054:
                case 1311007:
                case 1320009:
                case 2120010:
                case 2121009:
                case 4341052:
                case 31201002:
                case 31001001:
                case 2220010:
                case 2221009:
                case 2311006:
                case 2320011:
                case 2321010:
                case 3120006:
                case 3121002:
                case 3220005:
                case 3221002:
                case 4111001:
                case 4111009:
                case 4211003:
                case 4221013:
                case 4321000:
                case 4331003:
                case 4341002:
                case 5110001:
                case 5111005:
                case 5111007:
                case 5120011:
                case 5120012:
                case 5121003:
                case 5121009:
                case 5121015:
                case 5211001:
                case 5211002:
                case 5211006:
                case 5211007:
                case 5211009:
                case 5220002:
                case 5220011:
                case 5220012:
                case 5311004:
                case 5311005:
                case 5320007:
                case 5321003:
                case 5321004:
                case 5701005:
                case 5711001:
                case 5711011:
                case 5720005:
                case 5721002:
                case 9001004:
                case 9101004:
                case 10000093:
                case 10001004:
                case 10001026:
                case 13111005:
                case 14111007:
                case 15100004:
                case 15101006:
                case 15111002:
                case 15111005:
                case 15111006:
                case 15111011:
                case 20000093:
                case 20001004:
                case 20001026:
                case 20010093:
                case 20011004:
                case 20011026:
                case 20020093:
                case 20021026:
                case 20031209:
                case 20031210:
                case 21000000:
                case 21101003:
                case 22121001:
                case 22131001:
                case 22131002:
                case 22141002:
                case 22151002:
                case 22151003:
                case 22161002:
                case 22161004:
                case 22171000:
                case 22171004:
                case 22181000:
                case 22181003:
                case 22181004:
                case 24101005:
                case 24111002:
                case 24121008:
                case 24121009:
                case 27101202:
                case 27110007:
                case 30000093:
                case 30001026:
                case 30010093:
                case 30011026:
                case 31121005:
                case 32001003:
                case 32101003:
                case 32110000:
                case 32110007:
                case 32110008:
                case 32110009:
                case 32111005:
                case 32111006:
                case 32111012:
                case 32120000:
                case 32120001:
                case 32121003:
                case 33101006:
                case 33111003:
                case 35001001:
                case 35001002:
                case 35101005:
                case 35101007:
                case 35101009:
                case 35111001:
                case 35111002:
                case 35111004:
                case 35111005:
                case 35111009:
                case 35111010:
                case 35111011:
                case 35111013:
                case 35120000:
                case 35120014:
                case 35121003:
                case 35121005:
                case 35121006:
                case 35121009:
                case 35121010:
                case 35121013:
                case 36111006:
                case 41001001:
                case 41121003:
                case 42100010:
                case 42101002:
                case 42101004:
                case 42111006:
                case 42121008:
                case 50001214:
                case 51101003:
                case 51111003:
                case 51111004:
                case 51121004:
                case 51121005:
                case 60001216:
                case 60001217:
                case 61101002:
                case 61111008:
                case 61120007:
                case 61120008:
                case 61120011:
                case 80001000:
                case 80001089:
                case 31211003:
                case 31221004:
                case 32121054:
                case 2121054:
                case 11101021:
                case 13111024:
                case 2321054:
                case 11101022: //polling moon
                case 11111022:
                case 36121054:
                case 31011001:
                case 31211004:
                    isBuff = true;
            }
        }

        ret.chargeskill = (data.getChildByPath("keydown") != null);
        ret.hyper = (data.getChildByPath("hyper") != null);

        MapleData level = data.getChildByPath("common");
        if (level != null) {
            ret.maxLevel = MapleDataTool.getInt("maxLevel", level, 1);
            ret.trueMax = (ret.maxLevel + (ret.combatOrders ? 2 : 0));
            for (int i = 1; i <= ret.trueMax; i++) {
                ret.effects.add(MapleStatEffect.loadSkillEffectFromData(level, id, isBuff, i, "x"));
            }
        } else {
            for (MapleData leve : data.getChildByPath("level")) {
                ret.effects.add(MapleStatEffect.loadSkillEffectFromData(leve, id, isBuff, Byte.parseByte(leve.getName()), null));
            }
            ret.maxLevel = ret.effects.size();
            ret.trueMax = ret.effects.size();
        }
        MapleData level2 = data.getChildByPath("PVPcommon");
        if (level2 != null) {
            ret.pvpEffects = new ArrayList();
            for (int i = 1; i <= ret.trueMax; i++) {
                ret.pvpEffects.add(MapleStatEffect.loadSkillEffectFromData(level2, id, isBuff, i, "x"));
            }
        }
        MapleData reqDataRoot = data.getChildByPath("req");
        if (reqDataRoot != null) {
            for (MapleData reqData : reqDataRoot.getChildren()) {
                ret.requiredSkill.add(new Pair(Integer.valueOf(Integer.parseInt(reqData.getName())), Byte.valueOf((byte) MapleDataTool.getInt(reqData, 1))));
            }
        }
        ret.animationTime = 0;
        if (effect != null) {
            for (MapleData effectEntry : effect) {
                ret.animationTime += MapleDataTool.getIntConvert("delay", effectEntry, 0);
            }
        }
        return ret;
    }

    public MapleStatEffect getEffect(int level) {
        if (this.effects.size() < level) {
            if (this.effects.size() > 0) {
                return (MapleStatEffect) this.effects.get(this.effects.size() - 1);
            }
            return null;
        }
        if (level <= 0) {
            return (MapleStatEffect) this.effects.get(0);
        }
        return (MapleStatEffect) this.effects.get(level - 1);
    }

    public MapleStatEffect getPVPEffect(int level) {
        if (this.pvpEffects == null) {
            return getEffect(level);
        }
        if (this.pvpEffects.size() < level) {
            if (this.pvpEffects.size() > 0) {
                return (MapleStatEffect) this.pvpEffects.get(this.pvpEffects.size() - 1);
            }
            return null;
        }
        if (level <= 0) {
            return (MapleStatEffect) this.pvpEffects.get(0);
        }
        return (MapleStatEffect) this.pvpEffects.get(level - 1);
    }

    public int getSkillType() {
        return this.skillType;
    }

    public List<Integer> getAllAnimation() {
        return this.animation;
    }

    public int getAnimation() {
        if (this.animation == null) {
            return -1;
        }
        return ((Integer) this.animation.get(Randomizer.nextInt(this.animation.size()))).intValue();
    }

    public boolean isPVPDisabled() {
        return this.pvpDisabled;
    }

    public boolean isChargeSkill() {
        return this.chargeskill;
    }

    public boolean isInvisible() {
        return this.invisible;
    }

    public boolean hasRequiredSkill() {
        return this.requiredSkill.size() > 0;
    }

    public List<Pair<Integer, Byte>> getRequiredSkills() {
        return this.requiredSkill;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getTrueMax() {
        return this.trueMax;
    }

    public boolean combatOrders() {
        return this.combatOrders;
    }

    public boolean canBeLearnedBy(int job) {
        int jid = job;
        int skillForJob = this.id / 10000;
        if (skillForJob == 2001) {
            return GameConstants.isEvan(job);
        }
        if (skillForJob == 0) {
            return GameConstants.isAdventurer(job);
        }
        if (skillForJob == 1000) {
            return GameConstants.isKOC(job);
        }
        if (skillForJob == 2000) {
            return GameConstants.isAran(job);
        }
        if (skillForJob == 3000) {
            return GameConstants.isResist(job);
        }
        if (skillForJob == 1) {
            return GameConstants.isCannon(job);
        }
        if (skillForJob == 3001) {
            return (GameConstants.isDemon(job)) || (GameConstants.demonAvenger(job));
        }
        if (skillForJob == 2002) {
            return GameConstants.isMercedes(job);
        }
        if (skillForJob == 508) {
            return GameConstants.isJett(job);
        }
        if (skillForJob == 2003) {
            return GameConstants.isPhantom(job);
        }
        if (skillForJob == 2004) {
            return GameConstants.isluminous(job);
        }
        if (skillForJob == 4001) {
            return GameConstants.hayato(job);
        }
        if (skillForJob == 4002) {
            return GameConstants.kanna(job);
        }
        if (skillForJob == 6000) {
            return GameConstants.kaiser(job);
        }
        if (skillForJob == 6001) {
            return GameConstants.angelic(job);
        }
        if (skillForJob == 3002) {
            return GameConstants.xenon(job);
        }
        if (jid / 100 != skillForJob / 100) {
            return false;
        }
        if (jid / 1000 != skillForJob / 1000) {
            return false;
        }
        if ((GameConstants.isluminous(skillForJob)) && (!GameConstants.isluminous(job))) {
            return false;
        }
        if ((GameConstants.hayato(skillForJob)) && (!GameConstants.hayato(job))) {
            return false;
        }
        if ((GameConstants.kanna(skillForJob)) && (!GameConstants.kanna(job))) {
            return false;
        }
        if ((GameConstants.kaiser(skillForJob)) && (!GameConstants.kaiser(job))) {
            return false;
        }
        if ((GameConstants.angelic(skillForJob)) && (!GameConstants.angelic(job))) {
            return false;
        }
        if ((GameConstants.xenon(skillForJob)) && (!GameConstants.xenon(job))) {
            return false;
        }
        if ((GameConstants.demonAvenger(skillForJob)) && (!GameConstants.demonAvenger(job))) {
            return false;
        }
        if ((GameConstants.isPhantom(skillForJob)) && (!GameConstants.isPhantom(job))) {
            return false;
        }
        if ((GameConstants.isJett(skillForJob)) && (!GameConstants.isJett(job))) {
            return false;
        }
        if ((GameConstants.isCannon(skillForJob)) && (!GameConstants.isCannon(job))) {
            return false;
        }
        if ((GameConstants.isDemon(skillForJob)) && (!GameConstants.isDemon(job))) {
            return false;
        }
        if ((GameConstants.isAdventurer(skillForJob)) && (!GameConstants.isAdventurer(job))) {
            return false;
        }
        if ((GameConstants.isKOC(skillForJob)) && (!GameConstants.isKOC(job))) {
            return false;
        }
        if ((GameConstants.isAran(skillForJob)) && (!GameConstants.isAran(job))) {
            return false;
        }
        if ((GameConstants.isEvan(skillForJob)) && (!GameConstants.isEvan(job))) {
            return false;
        }
        if ((GameConstants.isMercedes(skillForJob)) && (!GameConstants.isMercedes(job))) {
            return false;
        }
        if ((GameConstants.isResist(skillForJob)) && (!GameConstants.isResist(job))) {
            return false;
        }
        if ((jid / 10 % 10 == 0) && (skillForJob / 10 % 10 > jid / 10 % 10)) {
            return false;
        }
        if ((skillForJob / 10 % 10 != 0) && (skillForJob / 10 % 10 != jid / 10 % 10)) {
            return false;
        }
        if (skillForJob % 10 > jid % 10) {
            return false;
        }
        return true;
    }

    public boolean isTimeLimited() {
        return this.timeLimited;
    }

    public boolean isFourthJobSkill(int skillid) {
        switch (skillid / 10000) {
            case 112:
            case 122:
            case 132:
            case 212:
            case 222:
            case 232:
            case 312:
            case 322:
            case 412:
            case 422:
            case 512:
            case 522:
                return true;
        }
        return false;
    }

    public boolean isThirdJobSkill(int skillid) {
        switch (skillid / 10000) {
            case 111:
            case 121:
            case 131:
            case 211:
            case 221:
            case 231:
            case 311:
            case 321:
            case 411:
            case 421:
            case 511:
            case 521:
                return true;
        }
        return false;
    }

    public boolean isSecondJobSkill(int skillid) {
        switch (skillid / 10000) {
            case 110:
            case 120:
            case 130:
            case 210:
            case 220:
            case 230:
            case 310:
            case 320:
            case 410:
            case 420:
            case 510:
            case 520:
                return true;
        }
        return false;
    }

    public boolean isFourthJob() {
        switch (this.id) {
            case 3120011:
            case 3220010:
            case 4320005:
            case 4340010:
            case 5120012:
            case 5211009:
            case 5220014:
            case 5320007:
            case 5321006:
            case 5720008:
            case 21120011:
            case 22181004:
            case 23120011:
            case 23121008:
            case 33120010:
            case 33121005:
                return false;
        }
        if (ishyper()) {
            return true;
        }

        if ((this.id / 10000 == 2312) || (this.id / 10000 == 2712) || (this.id / 10000 == 6112) || (this.id / 10000 == 6512)) {
            return true;
        }
        if ((this.id == 24121009) || (this.id == 24121010)) {
            return true;
        }
        // if ((this.id / 10000 == 3612) && (getMasterLevel() >= 10)) {
        // return true;
        // }
        if ((getMaxLevel() <= 15) && (!this.invisible) && (getMasterLevel() <= 0)) {
            return false;
        }
        if ((this.id / 10000 >= 2210) && (this.id / 10000 < 3000)) {
            return (this.id / 10000 % 10 >= 7) || (getMasterLevel() > 0);
        }
        if ((this.id / 10000 >= 430) && (this.id / 10000 <= 434)) {
            return (this.id / 10000 % 10 == 4) || (getMasterLevel() > 0);
        }
        return (this.id / 10000 % 10 == 2) && (this.id < 90000000) && (!isBeginnerSkill());
    }

    public Element getElement() {
        return this.element;
    }

    public int getAnimationTime() {
        return this.animationTime;
    }

    public int getMasterLevel() {
        return this.masterLevel;
    }

    public int getDelay() {
        return this.delay;
    }

    public int getTamingMob() {
        return this.eventTamingMob;
    }

    public boolean isBeginnerSkill() {
        int jobId = this.id / 10000;
        return GameConstants.isBeginnerJob(jobId);
    }

    public boolean isMagic() {
        return this.magic;
    }

    public boolean ishyper() {
        return this.hyper;
    }

    public boolean isMovement() {
        return this.casterMove;
    }

    public boolean isPush() {
        return this.pushTarget;
    }

    public boolean isPull() {
        return this.pullTarget;
    }

    public boolean isSpecialSkill() {
        int jobId = this.id / 10000;
        return (jobId == 900) || (jobId == 800) || (jobId == 9000) || (jobId == 9200) || (jobId == 9201) || (jobId == 9202) || (jobId == 9203) || (jobId == 9204);
    }
}