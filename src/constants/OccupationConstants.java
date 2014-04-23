package constants;

import server.Randomizer;
import tools.Pair;

public class OccupationConstants {

    private static final int[] occuEXP = {0, 2000, 4000, 6500, 9500, 13000, 17000, 21000, 25500, 30000};

    public static int getOccExpForLevel(int level) {
        if ((level < 0) || (level >= occuEXP.length)) {
            return 2147483647;
        }
        return occuEXP[level];
    }

    public static double getExpMultiplier(short occuId) {
        switch (occuId / 100) {
            case 2:
                return 1.0D + (occuId % 10 == 0 ? 10 : occuId % 10) / 10.0D;
            case 3:
                return occuId == 310 ? 0.5D : 0.3D;
            case 4:
                return 0.6D;
            case 5:
                return 0.8D;
        }
        return 1.0D;
    }

    public static double getMesoMultiplier(short occuId) {
        if (occuId / 100 == 5) {
            return 0.8D;
        }
        return 1.0D;
    }

    public static double getDropMultiplier(short occuId) {
        if (occuId / 100 == 5) {
            return 0.8D;
        }
        return 1.0D;
    }

    public static double getCashMultiplier(short occuId) {
        if (occuId / 100 == 5) {
            return 2.0D;
        }
        return 1.0D;
    }

    public static int getGamerChance(short id) {
        if (id / 100 == 2) {
            return id % 10 * 4;
        }
        return 0;
    }

    public static Pair<Integer, Integer> getHunterChance(short id) {
        int chance = id % 2 == 0 ? 100 : 30;
        int level = id % 10;
        int drops = 2;
        if ((level == 3) || (level == 4)) {
            drops = 3;
        } else if ((level == 5) || (level == 6)) {
            drops = 4;
        } else if ((level == 7) || (level == 8)) {
            drops = 5;
        } else if ((level == 9) || (level == 0)) {
            drops = 6;
        }
        return new Pair(Integer.valueOf(chance), Integer.valueOf(drops));
    }

    public static byte getNinjaClones(short occ) {
        byte size = 1;
        if (occ >= 103) {
            if (Randomizer.nextInt(100) < (occ <= 108 ? 90 : occ <= 106 ? 80 : occ <= 104 ? 70 : 100)) {
                size = (byte) (size + 1);
            }
        }
        if (occ >= 105) {
            if (Randomizer.nextInt(100) < (occ <= 108 ? 80 : occ <= 106 ? 70 : 90)) {
                size = (byte) (size + 1);
            }
        }
        if (occ >= 107) {
            if (Randomizer.nextInt(100) < (occ <= 108 ? 60 : 70)) {
                size = (byte) (size + 1);
            }
        }
        if ((occ >= 109) && (Randomizer.nextInt(100) < 60)) {
            size = (byte) (size + 1);
        }
        return size;
    }

    public static double getCloneDMG(short occ) {
        if (occ / 100 != 1) {
            return 100.0D;
        }
        switch (occ % 10) {
            case 1:
                return 4.0D;
            case 2:
            case 3:
                return 2.8D;
            case 4:
            case 5:
                return 2.5D;
            case 6:
            case 7:
                return 2.0D;
            case 0:
            case 8:
            case 9:
                return 1.8D;
        }
        return 100.0D;
    }

    public static double getVortexRange(short id) {
        if (id / 100 != 4) {
            return 0.005D;
        }
        double range = 0.01D;
        byte level = (byte) (id % 10);
        if (level == 9) {
            range += 0.08500000000000001D;
        } else {
            range += (level == 0 ? 9 : level) * 0.01D;
        }
        return range;
    }

    public static String toString(int id) {
        switch (id / 100) {
            case 1:
                return "Ninja";
            case 2:
                return "Gamer";
            case 3:
                return "Hunter";
            case 4:
                return "Vortex";
            case 5:
                return "NX Whore";
        }
        return "Undefined";
    }
}