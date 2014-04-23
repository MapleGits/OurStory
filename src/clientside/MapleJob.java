package clientside;

import java.io.PrintStream;

public enum MapleJob {

    BEGINNER(0),
    WARRIOR(100),
    FIGHTER(110),
    CRUSADER(111),
    HERO(112),
    PAGE(120),
    WHITEKNIGHT(121),
    PALADIN(122),
    SPEARMAN(130),
    DRAGONKNIGHT(131),
    DARKKNIGHT(132),
    MAGICIAN(200),
    FP_WIZARD(210),
    FP_MAGE(211),
    FP_ARCHMAGE(212),
    IL_WIZARD(220),
    IL_MAGE(221),
    IL_ARCHMAGE(222),
    CLERIC(230),
    PRIEST(231),
    BISHOP(232),
    BOWMAN(300),
    HUNTER(310),
    RANGER(311),
    BOWMASTER(312),
    CROSSBOWMAN(320),
    SNIPER(321),
    MARKSMAN(322),
    THIEF(400),
    ASSASSIN(410),
    HERMIT(411),
    NIGHTLORD(412),
    BANDIT(420),
    CHIEFBANDIT(421),
    SHADOWER(422),
    BLADE_RECRUIT(430),
    BLADE_ACOLYTE(431),
    BLADE_SPECIALIST(432),
    BLADE_LORD(433),
    BLADE_MASTER(434),
    PIRATE(500),
    PIRATE_CS(501),
    JETT1(508),
    BRAWLER(510),
    MARAUDER(511),
    BUCCANEER(512),
    GUNSLINGER(520),
    OUTLAW(521),
    CORSAIR(522),
    CANNONEER(530),
    CANNON_BLASTER(531),
    CANNON_MASTER(532),
    JETT2(570),
    JETT3(571),
    JETT4(572),
    MANAGER(800),
    GM(900),
    SUPERGM(910),
    NOBLESSE(1000),
    DAWNWARRIOR1(1100),
    DAWNWARRIOR2(1110),
    DAWNWARRIOR3(1111),
    DAWNWARRIOR4(1112),
    BLAZEWIZARD1(1200),
    BLAZEWIZARD2(1210),
    BLAZEWIZARD3(1211),
    BLAZEWIZARD4(1212),
    WINDARCHER1(1300),
    WINDARCHER2(1310),
    WINDARCHER3(1311),
    WINDARCHER4(1312),
    NIGHTWALKER1(1400),
    NIGHTWALKER2(1410),
    NIGHTWALKER3(1411),
    NIGHTWALKER4(1412),
    THUNDERBREAKER1(1500),
    THUNDERBREAKER2(1510),
    THUNDERBREAKER3(1511),
    THUNDERBREAKER4(1512),
    LEGEND(2000),
    EVAN_NOOB(2001),
    ARAN1(2100),
    ARAN2(2110),
    ARAN3(2111),
    ARAN4(2112),
    EVAN1(2200),
    EVAN2(2210),
    EVAN3(2211),
    EVAN4(2212),
    EVAN5(2213),
    EVAN6(2214),
    EVAN7(2215),
    EVAN8(2216),
    EVAN9(2217),
    EVAN10(2218),
    MERCEDES_NOOB(2002),
    MERCEDES1(2300),
    MERCEDES2(2310),
    MERCEDES3(2311),
    MERCEDES4(2312),
    PHANTOM_NOOB(2003),
    PHANTOM1(2400),
    PHANTOM2(2410),
    PHANTOM3(2411),
    PHANTOM4(2412),
    luminous(2004),
    luminous1(2700),
    luminous2(2710),
    luminous3(2711),
    luminous4(2712),
    CITIZEN(3000),
    CITIZEN_DS(3001),
    DEMON_SLAYER1(3100),
    DEMON_SLAYER2(3110),
    DEMON_SLAYER3(3111),
    DEMON_SLAYER4(3112),
    BATTLE_MAGE_1(3200),
    BATTLE_MAGE_2(3210),
    BATTLE_MAGE_3(3211),
    BATTLE_MAGE_4(3212),
    WILD_HUNTER_1(3300),
    WILD_HUNTER_2(3310),
    WILD_HUNTER_3(3311),
    WILD_HUNTER_4(3312),
    MECHANIC_1(3500),
    MECHANIC_2(3510),
    MECHANIC_3(3511),
    MECHANIC_4(3512),
    xenon(3002),
    xenon1(3600),
    xenon2(3610),
    xenon3(3611),
    xenon4(3612),
    hayato(4001),
    hayato1(4100),
    hayato2(4110),
    hayato3(4111),
    hayato4(4112),
    kanna(4002),
    kanna1(4200),
    kanna2(4210),
    kanna3(4211),
    kanna4(4212),
    MIHILE(5000),
    MIHILE_1ST(5100),
    MIHILE_2ND(5110),
    MIHILE_3RD(5111),
    MIHILE_4TH(5112),
    Kaiser(6000),
    Kaiser1(6100),
    Kaiser2(6110),
    Kaiser3(6111),
    Kaiser4(6112),
    Angelic(6001),
    Angelic1(6500),
    Angelic2(6510),
    Angelic3(6511),
    Angelic4(6512),
    ADDITIONAL_SKILLS(9000);
    private final int jobid;

    private MapleJob(int id) {
        this.jobid = id;
    }

    public int getId() {
        return this.jobid;
    }

    public static String getName(MapleJob mjob) {
        return mjob.name();
    }

    public static MapleJob getById(int id) {
        for (MapleJob l : values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }

    public static int getEncodingByJob(int job) {
        int exponent = job / 100;
        return (int) Math.pow(2.0D, exponent);
    }

    public boolean isA(MapleJob basejob) {
        return (this.jobid >= basejob.getId()) && (basejob.getId() % 100 > 0 ? this.jobid / 10 == basejob.getId() / 10 : this.jobid / 100 == basejob.getId() / 100);
    }

    public boolean isSeparatedSp() {
        return (this.jobid == 508) || (this.jobid / 10 == 57) || (this.jobid == 2001) || (this.jobid == 2002) || (this.jobid == 2003) || (this.jobid >= 2200);
    }

    public int getBaseJob() {
        return this.jobid - this.jobid % 100;
    }

    public int getBeginnerJob() {
        if (this.jobid / 1000 == 1) {
            return 1000;
        }
        if ((this.jobid == 2000) || (this.jobid / 100 == 21)) {
            return 2000;
        }
        if ((this.jobid == 2001) || (this.jobid / 100 == 22)) {
            return 2001;
        }
        if ((this.jobid == 2002) || (this.jobid / 100 == 23)) {
            return 2002;
        }
        if ((this.jobid == 2003) || (this.jobid / 100 == 24)) {
            return 2003;
        }
        if ((this.jobid == 2004) || (this.jobid / 100 == 27)) {
            return 2004;
        }
        if ((this.jobid == 3000) || (this.jobid >= 3200)) {
            return 3000;
        }
        if ((this.jobid == 3001) || (this.jobid / 100 == 31)) {
            return 3001;
        }
        if ((this.jobid == 4001) || (this.jobid / 100 == 41)) {
            return 4001;
        }
        if ((this.jobid == 4002) || (this.jobid / 100 == 42)) {
            return 4002;
        }
        if ((this.jobid == 6000) || (this.jobid / 100 == 61)) {
            return 6000;
        }
        if ((this.jobid == 6001) || (this.jobid / 100 == 65)) {
            return 6001;
        }
        if (this.jobid < 1000) {
            return 0;
        }
        System.out.println("getBeginnerJob() is unknown for jobid: " + this.jobid);
        return 0;
    }

    public boolean isBeginner() {
        return this.jobid == getBeginnerJob();
    }

    public boolean isWH() {
        return this.jobid / 100 == 33;
    }

    public boolean isEvan() {
        return (this.jobid == 2001) || (this.jobid / 100 == 22);
    }

    public boolean isEvanButNotNoob() {
        return this.jobid / 100 == 22;
    }

    public boolean isDemonSlayer() {
        return (this.jobid == 3001) || (this.jobid / 100 == 31);
    }

    public boolean isDemonSlayerButNotNoob() {
        return this.jobid / 100 == 31;
    }

    public boolean isCygnus() {
        return (this.jobid >= 1000) && (this.jobid < 1600);
    }

    public boolean isMechanic() {
        return this.jobid / 100 == 35;
    }

    public boolean isMercedes() {
        return (this.jobid == 2002) || (this.jobid / 100 == 23);
    }

    public boolean isCannonShooter() {
        return this.jobid / 10 == 53;
    }

    public boolean isAran() {
        return (this.jobid == 2000) || (this.jobid / 100 == 21);
    }

    public boolean isResistance() {
        return this.jobid / 1000 == 3;
    }

    public boolean isJett() {
        return (this.jobid == 508) || (this.jobid / 10 == 57);
    }

    public boolean isPhantom() {
        return (this.jobid == 2003) || (this.jobid / 100 == 24);
    }

    public boolean isluminous() {
        return (this.jobid == 2004) || (this.jobid / 100 == 27);
    }

    public boolean ishayato() {
        return (this.jobid == 4001) || (this.jobid / 100 == 41);
    }

    public boolean iskanna() {
        return (this.jobid == 4002) || (this.jobid / 100 == 42);
    }

    public boolean iskaiser() {
        return (this.jobid == 6000) || (this.jobid / 100 == 61);
    }

    public boolean isangelic() {
        return (this.jobid == 6001) || (this.jobid / 100 == 65);
    }
}