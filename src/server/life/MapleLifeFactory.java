package server.life;

import constants.GameConstants;
import java.awt.Point;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import provider.MapleDataType;
import server.Randomizer;
import tools.Pair;
import tools.StringUtil;

public class MapleLifeFactory {

    private static final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Mob.wz"));
    private static final MapleDataProvider npcData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Npc.wz"));
    private static final MapleDataProvider stringDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/String.wz"));
    private static final MapleDataProvider etcDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Etc.wz"));
    private static final MapleData mobStringData = stringDataWZ.getData("Mob.img");
    private static final MapleData npcStringData = stringDataWZ.getData("Npc.img");
    private static final MapleData npclocData = etcDataWZ.getData("NpcLocation.img");
    private static Map<Integer, String> npcNames = new HashMap();
    private static Map<Integer, MapleMonsterStats> monsterStats = new HashMap();
    private static Map<Integer, Integer> NPCLoc = new HashMap();
    private static Map<Integer, List<Integer>> questCount = new HashMap();

    public static AbstractLoadedMapleLife getLife(int id, String type) {
        if (type.equalsIgnoreCase("n")) {
            return getNPC(id);
        }
        if (type.equalsIgnoreCase("m")) {
            return getMonster(id);
        }
        System.err.println("Unknown Life type: " + type + "");
        return null;
    }

    public static int getNPCLocation(int npcid) {
        if (NPCLoc.containsKey(Integer.valueOf(npcid))) {
            return ((Integer) NPCLoc.get(Integer.valueOf(npcid))).intValue();
        }
        int map = MapleDataTool.getIntConvert(Integer.toString(npcid) + "/0", npclocData, -1);
        NPCLoc.put(Integer.valueOf(npcid), Integer.valueOf(map));
        return map;
    }

    public static final void loadQuestCounts() {
        if (questCount.size() > 0) {
            return;
        }
        for (MapleDataDirectoryEntry mapz : data.getRoot().getSubdirectories()) {
            if (mapz.getName().equals("QuestCountGroup")) {
                for (MapleDataFileEntry entry : mapz.getFiles()) {
                    int id = Integer.parseInt(entry.getName().substring(0, entry.getName().length() - 4));
                    MapleData dat = data.getData("QuestCountGroup/" + entry.getName());
                    if ((dat != null) && (dat.getChildByPath("info") != null)) {
                        List z = new ArrayList();
                        for (MapleData da : dat.getChildByPath("info")) {
                            z.add(Integer.valueOf(MapleDataTool.getInt(da, 0)));
                        }
                        questCount.put(Integer.valueOf(id), z);
                    } else {
                        System.out.println("null questcountgroup");
                    }
                }
            }
        }
        for (MapleData c : npcStringData) {
            int nid = Integer.parseInt(c.getName());
            String n = StringUtil.getLeftPaddedStr(nid + ".img", '0', 11);
            try {
                if (npcData.getData(n) != null) {
                    String name = MapleDataTool.getString("name", c, "MISSINGNO");
                    if ((!name.contains("Maple TV")) && (name.contains("Baby Moon Bunny"))) {
                        continue;
                    }
                    npcNames.put(Integer.valueOf(nid), name);
                }
            } catch (NullPointerException e) {
            } catch (RuntimeException e) {
            }
        }
    }

    public static final List<Integer> getQuestCount(int id) {
        return (List) questCount.get(Integer.valueOf(id));
    }

    public static MapleMonster getMonster(int mid) {
        MapleMonsterStats stats = getMonsterStats(mid);
        if (stats == null) {
            return null;
        }
        return new MapleMonster(mid, stats);
    }

    public static MapleMonsterStats getMonsterStats(int mid) {
        MapleMonsterStats stats = (MapleMonsterStats) monsterStats.get(Integer.valueOf(mid));

        if (stats == null) {
            MapleData monsterData = null;
            try {
                monsterData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(mid) + ".img", '0', 11));
            } catch (RuntimeException e) {
                return null;
            }
            if (monsterData == null) {
                return null;
            }
            MapleData monsterInfoData = monsterData.getChildByPath("info");
            stats = new MapleMonsterStats(mid);

            if (MapleDataTool.getLongConvert("finalmaxHP", monsterInfoData) > 0L) {
                stats.setHp(MapleDataTool.getLongConvert("finalmaxHP", monsterInfoData));
            } else {
                stats.setHp(GameConstants.getPartyPlayHP(mid) > 0 ? GameConstants.getPartyPlayHP(mid) : MapleDataTool.getIntConvert("maxHP", monsterInfoData));
            }
            stats.setMp(MapleDataTool.getIntConvert("maxMP", monsterInfoData, 0));
            stats.setExp(GameConstants.getPartyPlayEXP(mid) > 0 ? GameConstants.getPartyPlayEXP(mid) : mid == 9300027 ? 0 : MapleDataTool.getIntConvert("exp", monsterInfoData, 0));
            stats.setLevel((short) MapleDataTool.getIntConvert("level", monsterInfoData, 1));
            stats.setCharismaEXP((short) MapleDataTool.getIntConvert("charismaEXP", monsterInfoData, 0));
            stats.setRemoveAfter(MapleDataTool.getIntConvert("removeAfter", monsterInfoData, 0));
            stats.setrareItemDropLevel((byte) MapleDataTool.getIntConvert("rareItemDropLevel", monsterInfoData, 0));
            stats.setFixedDamage(MapleDataTool.getIntConvert("fixedDamage", monsterInfoData, -1));
            stats.setOnlyNormalAttack(MapleDataTool.getIntConvert("onlyNormalAttack", monsterInfoData, 0) > 0);
            stats.setBoss((GameConstants.getPartyPlayHP(mid) > 0) || (MapleDataTool.getIntConvert("boss", monsterInfoData, 0) > 0) || (mid == 8810018) || (mid == 9410066) || ((mid >= 8810118) && (mid <= 8810122)));
            stats.setExplosiveReward(MapleDataTool.getIntConvert("explosiveReward", monsterInfoData, 0) > 0);
            stats.setUndead(MapleDataTool.getIntConvert("undead", monsterInfoData, 0) > 0);
            stats.setEscort(MapleDataTool.getIntConvert("escort", monsterInfoData, 0) > 0);
            stats.setPartyBonus((GameConstants.getPartyPlayHP(mid) > 0) || (MapleDataTool.getIntConvert("partyBonusMob", monsterInfoData, 0) > 0));
            stats.setPartyBonusRate(MapleDataTool.getIntConvert("partyBonusR", monsterInfoData, 0));
            if (mobStringData.getChildByPath(String.valueOf(mid)) != null) {
                stats.setName(MapleDataTool.getString("name", mobStringData.getChildByPath(String.valueOf(mid)), "MISSINGNO"));
            }
            stats.setBuffToGive(MapleDataTool.getIntConvert("buff", monsterInfoData, -1));
            stats.setChange(MapleDataTool.getIntConvert("changeableMob", monsterInfoData, 0) > 0);
            stats.setFriendly(MapleDataTool.getIntConvert("damagedByMob", monsterInfoData, 0) > 0);
            stats.setNoDoom(MapleDataTool.getIntConvert("noDoom", monsterInfoData, 0) > 0);
            stats.setFfaLoot(MapleDataTool.getIntConvert("publicReward", monsterInfoData, 0) > 0);
            stats.setCP((byte) MapleDataTool.getIntConvert("getCP", monsterInfoData, 0));
            stats.setPoint(MapleDataTool.getIntConvert("point", monsterInfoData, 0));
            stats.setDropItemPeriod(MapleDataTool.getIntConvert("dropItemPeriod", monsterInfoData, 0));
            stats.setPhysicalAttack(MapleDataTool.getIntConvert("PADamage", monsterInfoData, 0));
            stats.setMagicAttack(MapleDataTool.getIntConvert("MADamage", monsterInfoData, 0));
            stats.setPDRate((byte) MapleDataTool.getIntConvert("PDRate", monsterInfoData, 0));
            stats.setMDRate((byte) MapleDataTool.getIntConvert("MDRate", monsterInfoData, 0));
            stats.setAcc(MapleDataTool.getIntConvert("acc", monsterInfoData, 0));
            stats.setEva(MapleDataTool.getIntConvert("eva", monsterInfoData, 0));
            stats.setSummonType((byte) MapleDataTool.getIntConvert("summonType", monsterInfoData, 0));
            stats.setCategory((byte) MapleDataTool.getIntConvert("category", monsterInfoData, 0));
            stats.setSpeed(MapleDataTool.getIntConvert("speed", monsterInfoData, 0));
            stats.setPushed(MapleDataTool.getIntConvert("pushed", monsterInfoData, 0));

            MapleData selfd = monsterInfoData.getChildByPath("selfDestruction");
            if (selfd != null) {
                stats.setSelfDHP(MapleDataTool.getIntConvert("hp", selfd, 0));
                stats.setRemoveAfter(MapleDataTool.getIntConvert("removeAfter", selfd, stats.getRemoveAfter()));
                stats.setSelfD((byte) MapleDataTool.getIntConvert("action", selfd, -1));
            } else {
                stats.setSelfD((byte) -1);
            }
            MapleData firstAttackData = monsterInfoData.getChildByPath("firstAttack");
            if (firstAttackData != null) {
                if (firstAttackData.getType() == MapleDataType.FLOAT) {
                    stats.setFirstAttack(Math.round(MapleDataTool.getFloat(firstAttackData)) > 0);
                } else {
                    stats.setFirstAttack(MapleDataTool.getInt(firstAttackData) > 0);
                }
            }
            if ((stats.isBoss()) || (isDmgSponge(mid))) {
                if ((monsterInfoData.getChildByPath("hpTagColor") == null) || (monsterInfoData.getChildByPath("hpTagBgcolor") == null)) {
                    stats.setTagColor(0);
                    stats.setTagBgColor(0);
                } else {
                    stats.setTagColor(MapleDataTool.getIntConvert("hpTagColor", monsterInfoData));
                    stats.setTagBgColor(MapleDataTool.getIntConvert("hpTagBgcolor", monsterInfoData));
                }
            }

            MapleData banishData = monsterInfoData.getChildByPath("ban");
            if (banishData != null) {
                stats.setBanishInfo(new BanishInfo(MapleDataTool.getString("banMsg", banishData), MapleDataTool.getInt("banMap/0/field", banishData, -1), MapleDataTool.getString("banMap/0/portal", banishData, "sp")));
            }

            final MapleData reviveInfo = monsterInfoData.getChildByPath("revive");
            if (reviveInfo != null) {
                List<Integer> revives = new LinkedList<>();
                for (MapleData bdata : reviveInfo) {
                    revives.add(MapleDataTool.getInt(bdata));
                }
                stats.setRevives(revives);
            }

            MapleData monsterSkillData = monsterInfoData.getChildByPath("skill");
            if (monsterSkillData != null) {
                int i = 0;
                List skills = new ArrayList();
                while (monsterSkillData.getChildByPath(Integer.toString(i)) != null) {
                    skills.add(new Pair(Integer.valueOf(MapleDataTool.getInt(i + "/skill", monsterSkillData, 0)), Integer.valueOf(MapleDataTool.getInt(i + "/level", monsterSkillData, 0))));
                    i++;
                }
                stats.setSkills(skills);
            }

            decodeElementalString(stats, MapleDataTool.getString("elemAttr", monsterInfoData, ""));

            int link = MapleDataTool.getIntConvert("link", monsterInfoData, 0);
            if (link != 0) {
                monsterData = data.getData(StringUtil.getLeftPaddedStr(link + ".img", '0', 11));
            }

            for (MapleData idata : monsterData) {
                if (idata.getName().equals("fly")) {
                    stats.setFly(true);
                    stats.setMobile(true);
                    break;
                }
                if (idata.getName().equals("move")) {
                    stats.setMobile(true);
                }
            }

            for (int i = 0;; i++) {
                MapleData monsterAtt = monsterInfoData.getChildByPath("attack/" + i);
                MapleData attackData = monsterData.getChildByPath("attack" + (i + 1) + "/info");
                if ((attackData == null) || (monsterAtt == null)) {
                    break;
                }
                MobAttackInfo ret = new MobAttackInfo();

                boolean deadlyAttack = monsterAtt.getChildByPath("deadlyAttack") != null;
                if (!deadlyAttack) {
                    deadlyAttack = attackData.getChildByPath("deadlyAttack") != null;
                }
                ret.setDeadlyAttack(deadlyAttack);

                int mpBurn = MapleDataTool.getInt("mpBurn", monsterAtt, 0);
                if (mpBurn == 0) {
                    mpBurn = MapleDataTool.getInt("mpBurn", attackData, 0);
                }
                ret.setMpBurn(mpBurn);

                int disease = MapleDataTool.getInt("disease", monsterAtt, 0);
                if (disease == 0) {
                    disease = MapleDataTool.getInt("disease", attackData, 0);
                }
                ret.setDiseaseSkill(disease);

                int level = MapleDataTool.getInt("level", monsterAtt, 0);
                if (level == 0) {
                    level = MapleDataTool.getInt("level", attackData, 0);
                }
                ret.setDiseaseLevel(level);

                int conMP = MapleDataTool.getInt("conMP", monsterAtt, 0);
                if (conMP == 0) {
                    conMP = MapleDataTool.getInt("conMP", attackData, 0);
                }
                ret.setMpCon(conMP);

                int attackAfter = MapleDataTool.getInt("attackAfter", monsterAtt, 0);
                if (attackAfter == 0) {
                    attackAfter = MapleDataTool.getInt("attackAfter", attackData, 0);
                }
                ret.attackAfter = attackAfter;

                int PADamage = MapleDataTool.getInt("PADamage", monsterAtt, 0);
                if (PADamage == 0) {
                    PADamage = MapleDataTool.getInt("PADamage", attackData, 0);
                }
                ret.PADamage = PADamage;

                int MADamage = MapleDataTool.getInt("MADamage", monsterAtt, 0);
                if (MADamage == 0) {
                    MADamage = MapleDataTool.getInt("MADamage", attackData, 0);
                }
                ret.MADamage = MADamage;

                boolean magic = MapleDataTool.getInt("magic", monsterAtt, 0) > 0;
                if (!magic) {
                    magic = MapleDataTool.getInt("magic", attackData, 0) > 0;
                }
                ret.magic = magic;
                ret.isElement = (monsterAtt.getChildByPath("elemAttr") != null);

                if (attackData.getChildByPath("range") != null) {
                    ret.range = MapleDataTool.getInt("range/r", attackData, 0);
                    if ((attackData.getChildByPath("range/lt") != null) && (attackData.getChildByPath("range/rb") != null)) {
                        ret.lt = ((Point) attackData.getChildByPath("range/lt").getData());
                        ret.rb = ((Point) attackData.getChildByPath("range/rb").getData());
                    }
                }
                stats.addMobAttack(ret);
            }

            byte hpdisplaytype = -1;
            if (stats.getTagColor() > 0) {
                hpdisplaytype = 0;
            } else if (stats.isFriendly()) {
                hpdisplaytype = 1;
            } else if ((mid >= 9300184) && (mid <= 9300215)) {
                hpdisplaytype = 2;
            } else if ((!stats.isBoss()) || (mid == 9410066) || (stats.isPartyBonus())) {
                hpdisplaytype = 3;
            }
            stats.setHPDisplayType(hpdisplaytype);

            monsterStats.put(Integer.valueOf(mid), stats);
        }
        return stats;
    }

    public static final void decodeElementalString(MapleMonsterStats stats, String elemAttr) {
        for (int i = 0; i < elemAttr.length(); i += 2) {
            stats.setEffectiveness(Element.getFromChar(elemAttr.charAt(i)), ElementalEffectiveness.getByNumber(Integer.valueOf(String.valueOf(elemAttr.charAt(i + 1))).intValue()));
        }
    }

    private static final boolean isDmgSponge(int mid) {
        switch (mid) {
            case 8810018:
            case 8810118:
            case 8810119:
            case 8810120:
            case 8810121:
            case 8810122:
            case 8820009:
            case 8820010:
            case 8820011:
            case 8820012:
            case 8820013:
            case 8820014:
                return true;
        }
        return false;
    }

    public static MapleNPC getNPC(int nid) {
        String name = (String) npcNames.get(Integer.valueOf(nid));
        if (name == null) {
            return null;
        }
        return new MapleNPC(nid, name);
    }

    public static int getRandomNPC() {
        List vals = new ArrayList(npcNames.keySet());
        int ret = 0;
        while (ret <= 0) {
            ret = ((Integer) vals.get(Randomizer.nextInt(vals.size()))).intValue();
            if (((String) npcNames.get(Integer.valueOf(ret))).contains("MISSINGNO")) {
                ret = 0;
            }
        }
        return ret;
    }
}