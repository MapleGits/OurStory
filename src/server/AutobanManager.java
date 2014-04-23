package server;

import clientside.MapleClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

public class AutobanManager
        implements Runnable {

    private Map<Integer, Integer> points = new HashMap();
    private Map<Integer, List<String>> reasons = new HashMap();
    private Set<ExpirationEntry> expirations = new TreeSet();
    private static final int AUTOBAN_POINTS = 5000;
    private static AutobanManager instance = new AutobanManager();
    private final ReentrantLock lock = new ReentrantLock(true);

    public static final AutobanManager getInstance() {
        return instance;
    }

    public final void autoban(MapleClient c, String reason) {
    }

    public final void addPoints(MapleClient c, int points, long expiration, String reason) {
    }

    public final void run() {
        long now = System.currentTimeMillis();
        for (ExpirationEntry e : this.expirations) {
            if (e.time <= now) {
                this.points.put(Integer.valueOf(e.acc), Integer.valueOf(((Integer) this.points.get(Integer.valueOf(e.acc))).intValue() - e.points));
            } else {
                return;
            }
        }
    }

    private static class ExpirationEntry
            implements Comparable<ExpirationEntry> {

        public long time;
        public int acc;
        public int points;

        public ExpirationEntry(long time, int acc, int points) {
            this.time = time;
            this.acc = acc;
            this.points = points;
        }

        public int compareTo(ExpirationEntry o) {
            return (int) (this.time - o.time);
        }

        public boolean equals(Object oth) {
            if (!(oth instanceof ExpirationEntry)) {
                return false;
            }
            ExpirationEntry ee = (ExpirationEntry) oth;
            return (this.time == ee.time) && (this.points == ee.points) && (this.acc == ee.acc);
        }
    }
}