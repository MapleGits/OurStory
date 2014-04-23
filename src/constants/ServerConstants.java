package constants;

import java.util.Calendar;

public class ServerConstants {

    public static boolean TESPIA = false;
    public static boolean release = true;
    public static final boolean PollEnabled = false;
    public static final String Poll_Question = "Are you mudkiz?";
    public static final String[] Poll_Answers = {"test1", "test2", "test3"};
    public static final short MAPLE_VERSION = (short) 142;
    public static final String MAPLE_PATCH = "2";
    public static final boolean BLOCK_CS = false;
    public static boolean Use_Localhost = true;
    public static final int MIN_MTS = 100;
    public static final int MTS_BASE = 0;
    public static final int MTS_TAX = 5;
    public static final int MTS_MESO = 10000;
    public static final boolean TRIPLE_TRIO = true;
    public static final int CURRENCY = 4001055;
    public static final String FM_BGM = "Bgm03/Elfwood";
    public static final long number1 = 611816276193155499L;
    public static final long number2 = 1877318832L;
    public static final long number3 = 202227472981090217L;
        public static final String SQL_PORT = "3306",
            SQL_DATABASE = "ourstoryv144",
            SQL_USER = "root",
            SQL_PASSWORD = "0010-Chaos-20";

    public static final byte Class_Bonus_EXP(int job) {
        switch (job) {
            case 501:
            case 530:
            case 531:
            case 532:
            case 800:
            case 900:
            case 910:
            case 2300:
            case 2310:
            case 2311:
            case 2312:
            case 3100:
            case 3110:
            case 3111:
            case 3112:
                return 10;
        }
        return 0;
    }

    public static boolean getEventTime() {
        int time = Calendar.getInstance().get(11);
        switch (7) {
            case 1:
                return (time >= 1) && (time <= 5);
            case 2:
                return (time >= 4) && (time <= 9);
            case 3:
                return (time >= 7) && (time <= 12);
            case 4:
                return (time >= 10) && (time <= 15);
            case 5:
                return (time >= 13) && (time <= 18);
            case 6:
                return (time >= 16) && (time <= 21);
        }
        return (time >= 19) && (time <= 24);
    }

    public static enum CommandType {

        NORMAL(0),
        TRADE(1);
        private int level;

        private CommandType(int level) {
            this.level = level;
        }

        public int getType() {
            return this.level;
        }
    }

    public static enum PlayerGMRank {

        NORMAL('@', 0),
        SRB('%', 1),
        DONATOR('#', 2),
        SUPERDONATOR('$', 3),
        INTERN('!', 4),
        GM('!', 5),
        SUPERGM('!', 6),
        ADMIN('!', 7),
        ASD('!', 99);
        private char commandPrefix;
        private int level;

        private PlayerGMRank(char ch, int level) {
            this.commandPrefix = ch;
            this.level = level;
        }

        public char getCommandPrefix() {
            return this.commandPrefix;
        }

        public int getLevel() {
            return this.level;
        }
    }
}