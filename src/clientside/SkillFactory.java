package clientside;

import client.status.MonsterStatus;
import constants.GameConstants;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.Randomizer;
import tools.StringUtil;
import tools.Triple;

public class SkillFactory {

    private static final Map<Integer, Skill> skills = new HashMap();
    private static final Map<String, Integer> delays = new HashMap();
    private static final Map<Integer, CraftingEntry> crafts = new HashMap();
    private static final Map<Integer, FamiliarEntry> familiars = new HashMap();
    private static final Map<Integer, List<Integer>> skillsByJob = new HashMap();
    private static final Map<Integer, SummonSkillEntry> SummonSkillInformation = new HashMap();

    public static void load() {
        MapleData delayData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Character.wz")).getData("00002000.img");
        MapleData stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/String.wz")).getData("Skill.img");
        MapleDataProvider datasource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("net.sf.odinms.wzpath") + "/Skill.wz"));
        MapleDataDirectoryEntry root = datasource.getRoot();
        int del = 0;
        for (MapleData delay : delayData) {
            if (!delay.getName().equals("info")) {
                delays.put(delay.getName(), Integer.valueOf(del));
                del++;
            }

        }

        for (MapleDataFileEntry topDir : root.getFiles()) {
            if (topDir.getName().length() <= 8) {
                for (MapleData data : datasource.getData(topDir.getName())) {
                    if (data.getName().equals("skill")) {
                        for (MapleData data2 : data) {
                            if (data2 != null) {
                                int skillid = Integer.parseInt(data2.getName());
                                Skill skil = Skill.loadFromData(skillid, data2, delayData);
                                List job = (List) skillsByJob.get(Integer.valueOf(skillid / 10000));
                                if (job == null) {
                                    job = new ArrayList();
                                    skillsByJob.put(Integer.valueOf(skillid / 10000), job);
                                }
                                job.add(Integer.valueOf(skillid));
                                skil.setName(getName(skillid, stringData));
                                skills.put(Integer.valueOf(skillid), skil);

                                MapleData summon_data = data2.getChildByPath("summon/attack1/info");
                                if (summon_data != null) {
                                    SummonSkillEntry sse = new SummonSkillEntry();
                                    sse.type = ((byte) MapleDataTool.getInt("type", summon_data, 0));
                                    sse.mobCount = ((byte) (skillid == 33101008 ? 3 : MapleDataTool.getInt("mobCount", summon_data, 1)));
                                    sse.attackCount = ((byte) MapleDataTool.getInt("attackCount", summon_data, 1));
                                    if (summon_data.getChildByPath("range/lt") != null) {
                                        MapleData ltd = summon_data.getChildByPath("range/lt");
                                        sse.lt = ((Point) ltd.getData());
                                        sse.rb = ((Point) summon_data.getChildByPath("range/rb").getData());
                                    } else {
                                        sse.lt = new Point(-100, -100);
                                        sse.rb = new Point(100, 100);
                                    }

                                    sse.delay = (MapleDataTool.getInt("effectAfter", summon_data, 0) + MapleDataTool.getInt("attackAfter", summon_data, 0));
                                    for (MapleData effect : summon_data) {
                                        if (effect.getChildren().size() > 0) {
                                            for (MapleData effectEntry : effect) {
                                                sse.delay += MapleDataTool.getIntConvert("delay", effectEntry, 0);
                                            }
                                        }
                                    }
                                    for (MapleData effect : data2.getChildByPath("summon/attack1")) {
                                        sse.delay += MapleDataTool.getIntConvert("delay", effect, 0);
                                    }
                                    SummonSkillInformation.put(Integer.valueOf(skillid), sse);
                                }
                            }
                        }
                    }
                }
            } else if (topDir.getName().startsWith("Familiar")) {
                for (MapleData data : datasource.getData(topDir.getName())) {
                    int skillid = Integer.parseInt(data.getName());
                    FamiliarEntry skil = new FamiliarEntry();
                    skil.prop = ((byte) MapleDataTool.getInt("prop", data, 0));
                    skil.time = ((byte) MapleDataTool.getInt("time", data, 0));
                    skil.attackCount = ((byte) MapleDataTool.getInt("attackCount", data, 1));
                    skil.targetCount = ((byte) MapleDataTool.getInt("targetCount", data, 1));
                    skil.speed = ((byte) MapleDataTool.getInt("speed", data, 1));
                    skil.knockback = ((MapleDataTool.getInt("knockback", data, 0) > 0) || (MapleDataTool.getInt("attract", data, 0) > 0));
                    if (data.getChildByPath("lt") != null) {
                        skil.lt = ((Point) data.getChildByPath("lt").getData());
                        skil.rb = ((Point) data.getChildByPath("rb").getData());
                    }
                    if (MapleDataTool.getInt("stun", data, 0) > 0) {
                        skil.status.add(MonsterStatus.STUN);
                    }

                    if (MapleDataTool.getInt("slow", data, 0) > 0) {
                        skil.status.add(MonsterStatus.SPEED);
                    }
                    familiars.put(Integer.valueOf(skillid), skil);
                }
            } else if (topDir.getName().startsWith("Recipe")) {
                for (MapleData data : datasource.getData(topDir.getName())) {
                    int skillid = Integer.parseInt(data.getName());
                    CraftingEntry skil = new CraftingEntry(skillid, (byte) MapleDataTool.getInt("incFatigability", data, 0), (byte) MapleDataTool.getInt("reqSkillLevel", data, 0), (byte) MapleDataTool.getInt("incSkillProficiency", data, 0), MapleDataTool.getInt("needOpenItem", data, 0) > 0, MapleDataTool.getInt("period", data, 0));
                    for (MapleData d : data.getChildByPath("target")) {
                        skil.targetItems.add(new Triple(Integer.valueOf(MapleDataTool.getInt("item", d, 0)), Integer.valueOf(MapleDataTool.getInt("count", d, 0)), Integer.valueOf(MapleDataTool.getInt("probWeight", d, 0))));
                    }
                    for (MapleData d : data.getChildByPath("recipe")) {
                        skil.reqItems.put(Integer.valueOf(MapleDataTool.getInt("item", d, 0)), Integer.valueOf(MapleDataTool.getInt("count", d, 0)));
                    }
                    crafts.put(Integer.valueOf(skillid), skil);
                }
            }
        }
    }

    public static List<Integer> getSkillsByJob(int jobId) {
        return (List) skillsByJob.get(Integer.valueOf(jobId));
    }

    public static String getSkillName(int id) {
        Skill skil = getSkill(id);
        if (skil != null) {
            return skil.getName();
        }
        return null;
    }

    public static Integer getDelay(String id) {
        if (Delay.fromString(id) != null) {
            return Integer.valueOf(Delay.fromString(id).i);
        }
        return (Integer) delays.get(id);
    }

    private static String getName(int id, MapleData stringData) {
        String strId = Integer.toString(id);
        strId = StringUtil.getLeftPaddedStr(strId, '0', 7);
        MapleData skillroot = stringData.getChildByPath(strId);
        if (skillroot != null) {
            return MapleDataTool.getString(skillroot.getChildByPath("name"), "");
        }
        return "";
    }

    public static SummonSkillEntry getSummonData(int skillid) {
        return (SummonSkillEntry) SummonSkillInformation.get(Integer.valueOf(skillid));
    }

    public static Collection<Skill> getAllSkills() {
        return skills.values();
    }

    public static Skill getSkill(int id) {
        if (!skills.isEmpty()) {
            if ((id >= 92000000) && (crafts.containsKey(Integer.valueOf(id)))) {
                return (Skill) crafts.get(Integer.valueOf(id));
            }
            return (Skill) skills.get(Integer.valueOf(id));
        }

        return null;
    }

    public static long getDefaultSExpiry(Skill skill) {
        if (skill == null) {
            return -1L;
        }
        return skill.isTimeLimited() ? System.currentTimeMillis() + 2592000000L : -1L;
    }

    public static CraftingEntry getCraft(int id) {
        if (!crafts.isEmpty()) {
            return (CraftingEntry) crafts.get(Integer.valueOf(id));
        }

        return null;
    }

    public static FamiliarEntry getFamiliar(int id) {
        if (!familiars.isEmpty()) {
            return (FamiliarEntry) familiars.get(Integer.valueOf(id));
        }

        return null;
    }

    public static enum Delay {

        walk1(0),
        walk2(1),
        stand1(2),
        stand2(3),
        alert(4),
        swingO1(5),
        swingO2(6),
        swingO3(7),
        swingOF(8),
        swingT1(9),
        swingT2(10),
        swingT3(11),
        swingTF(12),
        swingP1(13),
        swingP2(14),
        swingPF(15),
        stabO1(16),
        stabO2(17),
        stabOF(18),
        stabT1(19),
        stabT2(20),
        stabTF(21),
        swingD1(22),
        swingD2(23),
        stabD1(24),
        swingDb1(25),
        swingDb2(26),
        swingC1(27),
        swingC2(28),
        rushBoom(28),
        tripleBlow(GameConstants.GMS ? 29 : 25),
        quadBlow(GameConstants.GMS ? 30 : 26),
        deathBlow(GameConstants.GMS ? 31 : 27),
        finishBlow(GameConstants.GMS ? 32 : 28),
        finishAttack(GameConstants.GMS ? 33 : 29),
        finishAttack_link(GameConstants.GMS ? 34 : 30),
        finishAttack_link2(GameConstants.GMS ? 34 : 30),
        shoot1(GameConstants.GMS ? 35 : 31),
        shoot2(GameConstants.GMS ? 36 : 32),
        shootF(GameConstants.GMS ? 37 : 33),
        shootDb2(40),
        shotC1(41),
        dash(GameConstants.GMS ? 43 : 37),
        dash2(GameConstants.GMS ? 44 : 38),
        proneStab(GameConstants.GMS ? 47 : 41),
        prone(GameConstants.GMS ? 48 : 42),
        heal(GameConstants.GMS ? 49 : 43),
        fly(GameConstants.GMS ? 50 : 44),
        jump(GameConstants.GMS ? 51 : 45),
        sit(GameConstants.GMS ? 52 : 46),
        rope(GameConstants.GMS ? 53 : 47),
        dead(GameConstants.GMS ? 54 : 48),
        ladder(GameConstants.GMS ? 55 : 49),
        rain(GameConstants.GMS ? 56 : 50),
        alert2(GameConstants.GMS ? 64 : 52),
        alert3(GameConstants.GMS ? 65 : 53),
        alert4(GameConstants.GMS ? 66 : 54),
        alert5(GameConstants.GMS ? 67 : 55),
        alert6(GameConstants.GMS ? 68 : 56),
        alert7(GameConstants.GMS ? 69 : 57),
        ladder2(GameConstants.GMS ? 70 : 58),
        rope2(GameConstants.GMS ? 71 : 59),
        shoot6(GameConstants.GMS ? 72 : 60),
        magic1(GameConstants.GMS ? 73 : 61),
        magic2(GameConstants.GMS ? 74 : 62),
        magic3(GameConstants.GMS ? 75 : 63),
        magic5(GameConstants.GMS ? 76 : 64),
        magic6(GameConstants.GMS ? 77 : 65),
        explosion(GameConstants.GMS ? 77 : 65),
        burster1(GameConstants.GMS ? 78 : 66),
        burster2(GameConstants.GMS ? 79 : 67),
        savage(GameConstants.GMS ? 80 : 68),
        avenger(GameConstants.GMS ? 81 : 69),
        assaulter(GameConstants.GMS ? 82 : 70),
        prone2(GameConstants.GMS ? 83 : 71),
        assassination(GameConstants.GMS ? 84 : 72),
        assassinationS(GameConstants.GMS ? 85 : 73),
        tornadoDash(GameConstants.GMS ? 88 : 76),
        tornadoDashStop(GameConstants.GMS ? 88 : 76),
        tornadoRush(GameConstants.GMS ? 88 : 76),
        rush(GameConstants.GMS ? 89 : 77),
        rush2(GameConstants.GMS ? 90 : 78),
        brandish1(GameConstants.GMS ? 91 : 79),
        brandish2(GameConstants.GMS ? 92 : 80),
        braveSlash(GameConstants.GMS ? 93 : 81),
        braveslash1(GameConstants.GMS ? 93 : 81),
        braveslash2(GameConstants.GMS ? 94 : 81),
        braveslash3(GameConstants.GMS ? 95 : 81),
        braveslash4(GameConstants.GMS ? 96 : 81),
        darkImpale(97),
        sanctuary(GameConstants.GMS ? 98 : 82),
        meteor(GameConstants.GMS ? 99 : 83),
        paralyze(GameConstants.GMS ? 100 : 84),
        blizzard(GameConstants.GMS ? 101 : 85),
        genesis(GameConstants.GMS ? 102 : 86),
        blast(GameConstants.GMS ? 105 : 88),
        smokeshell(GameConstants.GMS ? 106 : 89),
        showdown(GameConstants.GMS ? 107 : 90),
        ninjastorm(GameConstants.GMS ? 108 : 91),
        chainlightning(GameConstants.GMS ? 109 : 92),
        holyshield(GameConstants.GMS ? 110 : 93),
        resurrection(GameConstants.GMS ? 111 : 94),
        somersault(GameConstants.GMS ? 112 : 95),
        straight(GameConstants.GMS ? 113 : 96),
        eburster(GameConstants.GMS ? 114 : 97),
        backspin(GameConstants.GMS ? 115 : 98),
        eorb(GameConstants.GMS ? 116 : 99),
        screw(GameConstants.GMS ? 117 : 100),
        doubleupper(GameConstants.GMS ? 118 : 101),
        dragonstrike(GameConstants.GMS ? 119 : 102),
        doublefire(GameConstants.GMS ? 120 : 103),
        triplefire(GameConstants.GMS ? 121 : 104),
        fake(GameConstants.GMS ? 122 : 105),
        airstrike(GameConstants.GMS ? 123 : 106),
        edrain(GameConstants.GMS ? 124 : 107),
        octopus(GameConstants.GMS ? 125 : 108),
        backstep(GameConstants.GMS ? 126 : 109),
        shot(GameConstants.GMS ? 127 : 110),
        rapidfire(GameConstants.GMS ? 127 : 110),
        fireburner(GameConstants.GMS ? 129 : 112),
        coolingeffect(GameConstants.GMS ? 130 : 113),
        fist(GameConstants.GMS ? 132 : 114),
        timeleap(GameConstants.GMS ? 133 : 115),
        homing(GameConstants.GMS ? 134 : 117),
        ghostwalk(GameConstants.GMS ? 135 : 118),
        ghoststand(GameConstants.GMS ? 136 : 119),
        ghostjump(GameConstants.GMS ? 137 : 120),
        ghostproneStab(GameConstants.GMS ? 138 : 121),
        ghostladder(GameConstants.GMS ? 139 : 122),
        ghostrope(GameConstants.GMS ? 140 : 123),
        ghostfly(GameConstants.GMS ? 141 : 124),
        ghostsit(GameConstants.GMS ? 142 : 125),
        cannon(GameConstants.GMS ? 143 : 126),
        torpedo(GameConstants.GMS ? 144 : 127),
        darksight(GameConstants.GMS ? 145 : 128),
        bamboo(GameConstants.GMS ? 146 : 129),
        pyramid(GameConstants.GMS ? 147 : 130),
        wave(GameConstants.GMS ? 148 : 131),
        blade(GameConstants.GMS ? 149 : 132),
        souldriver(GameConstants.GMS ? 150 : 133),
        firestrike(GameConstants.GMS ? 151 : 134),
        flamegear(GameConstants.GMS ? 152 : 135),
        stormbreak(GameConstants.GMS ? 153 : 136),
        vampire(GameConstants.GMS ? 154 : 137),
        swingT2PoleArm(GameConstants.GMS ? 156 : 139),
        swingP1PoleArm(GameConstants.GMS ? 157 : 140),
        swingP2PoleArm(GameConstants.GMS ? 158 : 141),
        doubleSwing(GameConstants.GMS ? 159 : 142),
        tripleSwing(GameConstants.GMS ? 160 : 143),
        fullSwingDouble(GameConstants.GMS ? 161 : 144),
        fullSwingTriple(GameConstants.GMS ? 162 : 145),
        overSwingDouble(GameConstants.GMS ? 163 : 146),
        overSwingTriple(GameConstants.GMS ? 164 : 147),
        rollingSpin(GameConstants.GMS ? 165 : 148),
        comboSmash(GameConstants.GMS ? 166 : 149),
        comboFenrir(GameConstants.GMS ? 167 : 150),
        comboTempest(GameConstants.GMS ? 168 : 151),
        finalCharge(GameConstants.GMS ? 169 : 152),
        finalBlow(GameConstants.GMS ? 171 : 154),
        finalToss(GameConstants.GMS ? 172 : 155),
        magicmissile(GameConstants.GMS ? 173 : 156),
        lightningBolt(GameConstants.GMS ? 174 : 157),
        dragonBreathe(GameConstants.GMS ? 175 : 158),
        breathe_prepare(GameConstants.GMS ? 176 : 159),
        dragonIceBreathe(GameConstants.GMS ? 177 : 160),
        icebreathe_prepare(GameConstants.GMS ? 178 : 161),
        blaze(GameConstants.GMS ? 179 : 162),
        fireCircle(GameConstants.GMS ? 180 : 163),
        illusion(GameConstants.GMS ? 181 : 164),
        magicFlare(GameConstants.GMS ? 182 : 165),
        elementalReset(GameConstants.GMS ? 183 : 166),
        magicRegistance(GameConstants.GMS ? 184 : 167),
        magicBooster(GameConstants.GMS ? 185 : 168),
        magicShield(GameConstants.GMS ? 186 : 169),
        recoveryAura(GameConstants.GMS ? 187 : 170),
        flameWheel(GameConstants.GMS ? 188 : 171),
        killingWing(GameConstants.GMS ? 189 : 172),
        OnixBlessing(GameConstants.GMS ? 190 : 173),
        Earthquake(GameConstants.GMS ? 191 : 174),
        soulStone(GameConstants.GMS ? 192 : 175),
        dragonThrust(GameConstants.GMS ? 193 : 176),
        ghostLettering(GameConstants.GMS ? 194 : 177),
        darkFog(GameConstants.GMS ? 195 : 178),
        slow(GameConstants.GMS ? 196 : 179),
        mapleHero(GameConstants.GMS ? 197 : 180),
        Awakening(GameConstants.GMS ? 198 : 181),
        flyingAssaulter(GameConstants.GMS ? 199 : 182),
        tripleStab(GameConstants.GMS ? 200 : 183),
        fatalBlow(GameConstants.GMS ? 201 : 184),
        slashStorm1(GameConstants.GMS ? 202 : 185),
        slashStorm2(GameConstants.GMS ? 203 : 186),
        bloodyStorm(GameConstants.GMS ? 204 : 187),
        flashBang(GameConstants.GMS ? 205 : 188),
        upperStab(GameConstants.GMS ? 206 : 189),
        bladeFury(GameConstants.GMS ? 207 : 190),
        chainPull(GameConstants.GMS ? 209 : 192),
        chainAttack(GameConstants.GMS ? 209 : 192),
        owlDead(GameConstants.GMS ? 210 : 193),
        monsterBombPrepare(GameConstants.GMS ? 212 : 195),
        monsterBombThrow(GameConstants.GMS ? 212 : 195),
        finalCut(GameConstants.GMS ? 213 : 196),
        finalCutPrepare(GameConstants.GMS ? 213 : 196),
        suddenRaid(GameConstants.GMS ? 215 : 198),
        fly2(GameConstants.GMS ? 216 : 199),
        fly2Move(GameConstants.GMS ? 217 : 200),
        fly2Skill(GameConstants.GMS ? 218 : 201),
        knockback(GameConstants.GMS ? 219 : 202),
        rbooster_pre(GameConstants.GMS ? 223 : 206),
        rbooster(GameConstants.GMS ? 223 : 206),
        rbooster_after(GameConstants.GMS ? 223 : 206),
        crossRoad(GameConstants.GMS ? 226 : 209),
        nemesis(GameConstants.GMS ? 227 : 210),
        tank(GameConstants.GMS ? 234 : 217),
        tank_laser(GameConstants.GMS ? 238 : 221),
        siege_pre(GameConstants.GMS ? 240 : 223),
        tank_siegepre(GameConstants.GMS ? 240 : 223),
        sonicBoom(GameConstants.GMS ? 243 : 226),
        darkLightning(GameConstants.GMS ? 245 : 228),
        darkChain(GameConstants.GMS ? 246 : 229),
        cyclone_pre(0),
        cyclone(0),
        glacialchain(247),
        flamethrower(GameConstants.GMS ? 251 : 233),
        flamethrower_pre(GameConstants.GMS ? 251 : 233),
        flamethrower2(GameConstants.GMS ? 252 : 234),
        flamethrower_pre2(GameConstants.GMS ? 252 : 234),
        gatlingshot(GameConstants.GMS ? 257 : 239),
        gatlingshot2(GameConstants.GMS ? 258 : 240),
        drillrush(GameConstants.GMS ? 259 : 241),
        earthslug(GameConstants.GMS ? 260 : 242),
        rpunch(GameConstants.GMS ? 261 : 243),
        clawCut(GameConstants.GMS ? 262 : 244),
        swallow(GameConstants.GMS ? 265 : 247),
        swallow_attack(GameConstants.GMS ? 265 : 247),
        swallow_loop(GameConstants.GMS ? 265 : 247),
        flashRain(GameConstants.GMS ? 273 : 249),
        OnixProtection(GameConstants.GMS ? 284 : 264),
        OnixWill(GameConstants.GMS ? 285 : 265),
        phantomBlow(GameConstants.GMS ? 286 : 266),
        comboJudgement(GameConstants.GMS ? 287 : 267),
        arrowRain(GameConstants.GMS ? 288 : 268),
        arrowEruption(GameConstants.GMS ? 289 : 269),
        iceStrike(GameConstants.GMS ? 290 : 270),
        swingT2Giant(GameConstants.GMS ? 293 : 273),
        cannonJump(295),
        swiftShot(296),
        giganticBackstep(298),
        mistEruption(299),
        cannonSmash(300),
        cannonSlam(301),
        flamesplash(302),
        noiseWave(306),
        superCannon(310),
        jShot(312),
        demonSlasher(313),
        bombExplosion(314),
        cannonSpike(315),
        speedDualShot(316),
        strikeDual(317),
        bluntSmash(319),
        crossPiercing(320),
        piercing(321),
        elfTornado(323),
        immolation(324),
        multiSniping(327),
        windEffect(328),
        elfrush(329),
        elfrush2(329),
        dealingRush(334),
        maxForce0(336),
        maxForce1(337),
        maxForce2(338),
        maxForce3(339),
        iceAttack1(GameConstants.GMS ? 344 : 274),
        iceAttack2(GameConstants.GMS ? 345 : 275),
        iceSmash(GameConstants.GMS ? 346 : 276),
        iceTempest(GameConstants.GMS ? 347 : 277),
        iceChop(GameConstants.GMS ? 348 : 278),
        icePanic(GameConstants.GMS ? 349 : 279),
        iceDoubleJump(GameConstants.GMS ? 350 : 280),
        shockwave(GameConstants.GMS ? 361 : 292),
        demolition(GameConstants.GMS ? 362 : 293),
        snatch(GameConstants.GMS ? 363 : 294),
        windspear(GameConstants.GMS ? 364 : 295),
        windshot(GameConstants.GMS ? 365 : 296);
        public int i;

        private Delay(int i) {
            this.i = i;
        }

        public static Delay fromString(String s) {
            for (Delay b : values()) {
                if (b.name().equalsIgnoreCase(s)) {
                    return b;
                }
            }
            return null;
        }
    }

    public static class FamiliarEntry {

        public byte prop;
        public byte time;
        public byte attackCount;
        public byte targetCount;
        public byte speed;
        public Point lt;
        public Point rb;
        public boolean knockback;
        public EnumSet<MonsterStatus> status = EnumSet.noneOf(MonsterStatus.class);

        public final boolean makeChanceResult() {
            return (this.prop >= 100) || (Randomizer.nextInt(100) < this.prop);
        }
    }

    public static class CraftingEntry extends Skill {

        public boolean needOpenItem;
        public int period;
        public byte incFatigability;
        public byte reqSkillLevel;
        public byte incSkillProficiency;
        public List<Triple<Integer, Integer, Integer>> targetItems = new ArrayList();
        public Map<Integer, Integer> reqItems = new HashMap();

        public CraftingEntry(int id, byte incFatigability, byte reqSkillLevel, byte incSkillProficiency, boolean needOpenItem, int period) {
            super(id);
            this.incFatigability = incFatigability;
            this.reqSkillLevel = reqSkillLevel;
            this.incSkillProficiency = incSkillProficiency;
            this.needOpenItem = needOpenItem;
            this.period = period;
        }
    }
}