package server;

import clientside.MapleCharacter;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import java.lang.ref.WeakReference;
import server.maps.MapleMap;

public class MapleCarnivalChallenge {

    WeakReference<MapleCharacter> challenger;
    String challengeinfo = "";

    public MapleCarnivalChallenge(MapleCharacter challenger) {
        this.challenger = new WeakReference(challenger);
        this.challengeinfo += "#b";
        for (MaplePartyCharacter pc : challenger.getParty().getMembers()) {
            MapleCharacter c = challenger.getMap().getCharacterById(pc.getId());
            if (c != null) {
                this.challengeinfo = (this.challengeinfo + c.getName() + " / Level" + c.getLevel() + " / " + getJobNameById(c.getJob()));
            }
        }
        this.challengeinfo += "#k";
    }

    public MapleCharacter getChallenger() {
        return (MapleCharacter) this.challenger.get();
    }

    public String getChallengeInfo() {
        return this.challengeinfo;
    }

    public static final String getJobNameById(int job) {
        switch (job) {
            case 0:
                return "Beginner";
            case 1000:
                return "Nobless";
            case 2000:
                return "Legend";
            case 2001:
                return "Evan";
            case 3000:
                return "Citizen";
            case 100:
                return "Warrior";
            case 110:
                return "Fighter";
            case 111:
                return "Crusader";
            case 112:
                return "Hero";
            case 120:
                return "Page";
            case 121:
                return "White Knight";
            case 122:
                return "Paladin";
            case 130:
                return "Spearman";
            case 131:
                return "Dragon Knight";
            case 132:
                return "Dark Knight";
            case 200:
                return "Magician";
            case 210:
                return "Wizard(Fire,Poison)";
            case 211:
                return "Mage(Fire,Poison)";
            case 212:
                return "Arch Mage(Fire,Poison)";
            case 220:
                return "Wizard(Ice,Lightning)";
            case 221:
                return "Mage(Ice,Lightning)";
            case 222:
                return "Arch Mage(Ice,Lightning)";
            case 230:
                return "Cleric";
            case 231:
                return "Priest";
            case 232:
                return "Bishop";
            case 300:
                return "Archer";
            case 310:
                return "Hunter";
            case 311:
                return "Ranger";
            case 312:
                return "Bowmaster";
            case 320:
                return "Crossbow man";
            case 321:
                return "Sniper";
            case 322:
                return "Crossbow Master";
            case 400:
                return "Rogue";
            case 410:
                return "Assassin";
            case 411:
                return "Hermit";
            case 412:
                return "Night Lord";
            case 420:
                return "Bandit";
            case 421:
                return "Chief Bandit";
            case 422:
                return "Shadower";
            case 430:
                return "Blade Recruit";
            case 431:
                return "Blade Acolyte";
            case 432:
                return "Blade Specialist";
            case 433:
                return "Blade Lord";
            case 434:
                return "Blade Master";
            case 500:
                return "Pirate";
            case 510:
                return "Brawler";
            case 511:
                return "Marauder";
            case 512:
                return "Buccaneer";
            case 520:
                return "Gunslinger";
            case 521:
                return "Outlaw";
            case 522:
                return "Corsair";
            case 501:
                return "Pirate (Cannoneer)";
            case 530:
                return "Cannoneer";
            case 531:
                return "Cannon Blaster";
            case 532:
                return "Cannon Master";
            case 5000:
                return "Nameless Warden";
            case 5100:
                return "Mihile 1st";
            case 5110:
                return "Mihile 2nd";
            case 5111:
                return "Mihile 3rd";
            case 5112:
                return "Mihile 4th";
            case 1100:
            case 1110:
            case 1111:
            case 1112:
                return "Soul Master";
            case 1200:
            case 1210:
            case 1211:
            case 1212:
                return "Flame Wizard";
            case 1300:
            case 1310:
            case 1311:
            case 1312:
                return "Wind Breaker";
            case 1400:
            case 1410:
            case 1411:
            case 1412:
                return "Night Walker";
            case 1500:
            case 1510:
            case 1511:
            case 1512:
                return "Striker";
            case 2100:
            case 2110:
            case 2111:
            case 2112:
                return "Aran";
            case 2200:
            case 2210:
            case 2211:
            case 2212:
            case 2213:
            case 2214:
            case 2215:
            case 2216:
            case 2217:
            case 2218:
                return "Evan";
            case 2002:
            case 2300:
            case 2310:
            case 2311:
            case 2312:
                return "Mercedes";
            case 3001:
            case 3100:
            case 3110:
            case 3111:
            case 3112:
                return "Demon Slayer";
            case 3200:
            case 3210:
            case 3211:
            case 3212:
                return "Battle Mage";
            case 3300:
            case 3310:
            case 3311:
            case 3312:
                return "Wild Hunter";
            case 3500:
            case 3510:
            case 3511:
            case 3512:
                return "Mechanic";
            case 2003:
                return "Miser";
            case 2400:
            case 2410:
            case 2411:
            case 2412:
                return "Phantom";
            case 2004:
            case 2700:
            case 2710:
            case 2711:
            case 2712:
                return "luminous";
            case 4001:
            case 4100:
            case 4110:
            case 4111:
            case 4112:
                return "hayato";
            case 4002:
            case 4200:
            case 4210:
            case 4211:
            case 4212:
                return "kanna";
            case 6000:
            case 6100:
            case 6110:
            case 6111:
            case 6112:
                return "Kaiser";
            case 6001:
            case 6500:
            case 6510:
            case 6511:
            case 6512:
                return "Angelic";
            case 3002:
            case 3600:
            case 3610:
            case 3611:
            case 3612:
                return "Xenon";
            case 3101:
            case 3120:
            case 3121:
            case 3122:
                return "Demon AvenGer";
            case 508:
            case 570:
            case 571:
            case 572:
                return "Jett";
            case 900:
                return "GM";
            case 910:
                return "SuperGM";
            case 800:
                return "Manager";
        }
        return null;
    }

    public static final String getJobBasicNameById(int job) {
        switch (job) {
            case 0:
            case 1000:
            case 2000:
            case 2001:
            case 2002:
            case 2003:
            case 2004:
            case 3000:
            case 3001:
            case 4001:
            case 4002:
            case 6000:
            case 6001:
                return "Beginner";
            case 100:
            case 110:
            case 111:
            case 112:
            case 120:
            case 121:
            case 122:
            case 130:
            case 131:
            case 132:
            case 1100:
            case 1110:
            case 1111:
            case 1112:
            case 2100:
            case 2110:
            case 2111:
            case 2112:
            case 3100:
            case 3110:
            case 3111:
            case 3112:
            case 4100:
            case 4110:
            case 4111:
            case 4112:
            case 6100:
            case 6110:
            case 6111:
            case 6112:
                return "Warrior";
            case 200:
            case 210:
            case 211:
            case 212:
            case 220:
            case 221:
            case 222:
            case 230:
            case 231:
            case 232:
            case 1200:
            case 1210:
            case 1211:
            case 1212:
            case 2200:
            case 2210:
            case 2211:
            case 2212:
            case 2213:
            case 2214:
            case 2215:
            case 2216:
            case 2217:
            case 2218:
            case 2700:
            case 2710:
            case 2711:
            case 2712:
            case 3200:
            case 3210:
            case 3211:
            case 3212:
            case 4200:
            case 4210:
            case 4211:
            case 4212:
                return "Magician";
            case 300:
            case 310:
            case 311:
            case 312:
            case 320:
            case 321:
            case 322:
            case 1300:
            case 1310:
            case 1311:
            case 1312:
            case 2300:
            case 2310:
            case 2311:
            case 2312:
            case 3300:
            case 3310:
            case 3311:
            case 3312:
            case 6500:
            case 6510:
            case 6511:
            case 6512:
                return "Bowman";
            case 400:
            case 410:
            case 411:
            case 412:
            case 420:
            case 421:
            case 422:
            case 430:
            case 431:
            case 432:
            case 433:
            case 434:
            case 1400:
            case 1410:
            case 1411:
            case 1412:
            case 2400:
            case 2410:
            case 2411:
            case 2412:
                return "Thief";
            case 500:
            case 501:
            case 508:
            case 510:
            case 511:
            case 512:
            case 520:
            case 521:
            case 522:
            case 530:
            case 531:
            case 532:
            case 570:
            case 571:
            case 572:
            case 1500:
            case 1510:
            case 1511:
            case 1512:
            case 3500:
            case 3510:
            case 3511:
            case 3512:
                return "Pirate";
        }
        return "";
    }
}