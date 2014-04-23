package constants;

import clientside.MapleCharacter;
import clientside.MapleClient;
import handling.channel.ChannelServer;

public class TutorialConstants {

    public static final int[] tutorialDrops = {4031220, 4000353, 4000136};
    public static final int dropPosX = 219;
    public static final int dropPosY = -85;
    public static final int tutorialDropsMap = 30000;
    public static final int beginnerNPC = 9000054;
    public static final String beginnerNPCName = "Ranch Owner";
    public static final byte[] position = {-101, -102, -105, -107, -108, -49};
    public static final int[] starterPack = {1082149, 1372005, 1302007, 1332063, 1432000, 1442000, 1452002, 1462047, 1472000, 1302001, 1382000, 1492000, 1482000};
    public static final int[] equipStats = {10, 5, 69, 10, 0};

    public static boolean isBeginnerEquip(int itemid) {
        switch (itemid) {
            case 1000005:
            case 1001005:
            case 1010002:
            case 1011000:
            case 1050071:
            case 1051061:
            case 1072353:
            case 1072354:
            case 1080000:
            case 1080001:
            case 1142073:
                return true;
        }
        return false;
    }

    public static String getStageMSG(MapleCharacter chr, int id) {
        switch (id) {
            case 10000:
                return "Welcome to stage 1";
            case 20000:
                return "Welcome to stage 2";
            case 30000:
                return "Welcome to stage 3";
            case 30001:
                return "Time to choose an occupation";
            case 40000:
                return "Welcome to stage 4";
            case 50000:
                return "Welcome to stage 5";
            case 913030000:
                return "Oh my god!, what happened to this place?";
        }
        return "Welcome back #e" + chr.getName() + "#n!";
    }

    public static String getTutorialTalk(MapleCharacter chr, int id) {
        switch (id) {
            case 10000:
                return "Hello, I am the #eRanch Owner's #rattractive daughter#k#n. here to introduce you to " + chr.getClient().getChannelServer().getServerName() + "! ";
            case 20000:
                return "This is a #equiz on " + chr.getClient().getChannelServer().getServerName() + "#n, so simply use #bthe bulletin board as a reference#k and #e#rpass my quiz#k#n!";
            case 30000:
                return "Hey look, #ea chef#n...\r\n \r\n#e#rWho's hungry#k#n?";
            case 30001:
                return "#eHey, it's the boss#n...\r\n \r\n#e#rI tried to get a job from him for ages#n#k...";
            case 40000:
                return "This is the easiest stage, please collect the items which are dropped by the monster!";
            case 50000:
                return "Pff, welcome to the last stage.\r\nTalk to #eRanch Owner#n for the requirements!";
            case 913030000:
                return "Oh my god!, what happened to this place?\r\nLook there is one person alive, it's #eRanch Owner#n.\r\nI guess he needs your help.";
        }
        return "Welcome back #e" + chr.getName() + "#n!";
    }

    public static int getQuest(MapleCharacter chr, int id) {
        switch (id / 10000) {
            case 1:
                return 50000;
            case 2:
                return 50001;
            case 3:
                return 50002;
            case 4:
                return 50003;
            case 5:
                return 50004;
        }
        return 99999;
    }

    public static final String getPortalBlockedMsg() {
        return "You haven't finished this stage yet.";
    }

    public static final String getDropBlockedMsg() {
        return "You cannot drop your tutorial equips.";
    }

    public static final String getTradeBlockedMsg() {
        return "You cannot trade your tutorial equips.";
    }

    public static final String getEquipBlockedMsg() {
        return "You cannot unequip your tutorial equips.";
    }
}