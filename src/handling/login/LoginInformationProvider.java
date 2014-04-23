package handling.login;

import constants.GameConstants;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Triple;

public class LoginInformationProvider {

    private static final LoginInformationProvider instance = new LoginInformationProvider();
    protected final List<String> ForbiddenName = new ArrayList();
    protected final Map<Triple<Integer, Integer, Integer>, List<Integer>> makeCharInfo = new HashMap();

    public static LoginInformationProvider getInstance() {
        return instance;
    }

    protected LoginInformationProvider() {
        final String WZpath = System.getProperty("net.sf.odinms.wzpath");
        final MapleDataProvider prov = MapleDataProviderFactory.getDataProvider(new File(WZpath + "/Etc.wz"));
        MapleData nameData = prov.getData("ForbiddenName.img");
        for (final MapleData data : nameData.getChildren()) {
            ForbiddenName.add(MapleDataTool.getString(data));
        }
        nameData = prov.getData("Curse.img");
        for (final MapleData data : nameData.getChildren()) {
            ForbiddenName.add(MapleDataTool.getString(data).split(",")[0]);
        }
        final MapleData infoData = prov.getData("MakeCharInfo.img");
        final MapleData data = infoData.getChildByPath("Info");
        for (MapleData dat : data) {
            int val = -1;
            if (dat.getName().endsWith("Female")) { // comes first..
                val = 1;
            } else if (dat.getName().endsWith("Male")) {
                val = 0;
            }
            final int job = JobType.getByJob(dat.getName()).type;
            for (MapleData da : dat) {
                final Triple<Integer, Integer, Integer> key = new Triple<Integer, Integer, Integer>(val, Integer.parseInt(da.getName()), job);
                List<Integer> our = makeCharInfo.get(key);
                if (our == null) {
                    our = new ArrayList<Integer>();
                    makeCharInfo.put(key, our);
                }
                for (MapleData d : da) {
                    our.add(MapleDataTool.getInt(d, -1));
                }
            }
        }
        if (GameConstants.GMS) { //TODO LEGEND
            for (MapleData dat : infoData) {
                try {
                    final int type = JobType.getById(Integer.parseInt(dat.getName())).type;
                    for (MapleData d : dat) {
                        int val;
                        if (d.getName().endsWith("female")) {
                            val = 1;
                        } else if (d.getName().endsWith("male")) {
                            val = 0;
                        } else {
                            continue;
                        }
                        for (MapleData da : d) {
                            final Triple<Integer, Integer, Integer> key = new Triple<Integer, Integer, Integer>(val, Integer.parseInt(da.getName()), type);
                            List<Integer> our = makeCharInfo.get(key);
                            if (our == null) {
                                our = new ArrayList<Integer>();
                                makeCharInfo.put(key, our);
                            }
                            for (MapleData dd : da) {
                                our.add(MapleDataTool.getInt(dd, -1));
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        final MapleData uA = infoData.getChildByPath("UltimateAdventurer");
        for (MapleData dat : uA) {
            final Triple<Integer, Integer, Integer> key = new Triple<Integer, Integer, Integer>(-1, Integer.parseInt(dat.getName()), JobType.UltimateAdventurer.type);
            List<Integer> our = makeCharInfo.get(key);
            if (our == null) {
                our = new ArrayList<Integer>();
                makeCharInfo.put(key, our);
            }
            for (MapleData d : dat) {
                our.add(MapleDataTool.getInt(d, -1));
            }
        }
    }


    public static boolean isExtendedSpJob(int jobId) {
        return GameConstants.isSeparatedSp(jobId);
    }

    public final boolean isForbiddenName(String in) {
        for (String name : this.ForbiddenName) {
            if (in.toLowerCase().contains(name.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public final boolean isEligibleItem(int gender, int val, int job, int item) {
        if (item < 0) {
            return false;
        }
        Triple key = new Triple(Integer.valueOf(gender), Integer.valueOf(val), Integer.valueOf(job));
        List our = (List) this.makeCharInfo.get(key);
        if (our == null) {
            return false;
        }
        return our.contains(Integer.valueOf(item));
    }

    public static enum JobType {

        UltimateAdventurer(-1, "Ultimate", 0, 130000000),
        Resistance(0, "Resistance", 3000, 931000000),
        Adventurer(1, "", 0, 10000),
        Cygnus(2, "Premium", 1000, 913040000),
        Aran(3, "Orient", 2000, 914000000),
        Evan(4, "Evan", 2001, 900090000),
        Mercedes(5, "", 2002, 910150000),
        Demon(6, "", 3001, 931050310),
        Phantom(7, "", 2003, 10000),
        DualBlade(8, "", 0, 103050900),
        Mihile(9, "", 5000, 913070000),
        Jett(10, "", 508, 10000),
        Luminous(11, "", 2004, 10000),
        Kaiser(12, "", 6000, 10000),
        CANNONEER(13, "CANNONEER", 530, 10000),
        Xenon(14, "", 0, 10000),
        AngelicBurster(15, "", 6001, 10000);
        public int type;
        public int id;
        public int map;
        public String job;

        private JobType(int type, String job, int id, int map) {
            this.type = type;
            this.job = job;
            this.id = id;
            this.map = map;
        }

        public static JobType getByJob(String g) {
            for (JobType e : values()) {
                if ((e.job.length() > 0) && (g.startsWith(e.job))) {
                    return e;
                }
            }
            return Adventurer;
        }

        public static JobType getByType(int g) {
            for (JobType e : values()) {
                if (e.type == g) {
                    return e;
                }
            }
            return Adventurer;
        }

        public static JobType getById(int g) {
            for (JobType e : values()) {
                if ((e.id == g) || ((g == 508) && (e.type == 8))) {
                    return e;
                }
            }
            return Adventurer;
        }
    }

    public enum JobType2 {

        resistance((byte) 0),
        adventurer((byte) 1),
        cygnus((byte) 2),
        aran((byte) 3),
        evan((byte) 4),
        mercedes((byte) 5),
        demon((byte) 6),
        phantom((byte) 7),
        dualblade((byte) 8),
        mihile((byte) 9),
        zen((byte) 10),
        luminous((byte) 11),
        kaiser((byte) 12),
        angelicbuster((byte) 13),
        xenon((byte) 14),
        demonavenger((byte) 15);
        private byte jobCode = -1;

        private JobType2(byte type) {
            this.jobCode = type;
        }

        public byte getValue() {
            return jobCode;
        }
    }
}