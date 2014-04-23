package handling.world.family;

import handling.world.World;
import handling.world.World.Family;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FamilyLoad {

    public static final int NumSavingThreads = 8;
    private static final TimingThread[] Threads = new TimingThread[8];
    private static final AtomicInteger Distribute = new AtomicInteger(0);

    public static void QueueFamilyForLoad(int hm) {
        int Current = Distribute.getAndIncrement() % 8;
        Threads[Current].getRunnable().Queue(Integer.valueOf(hm));
    }

    public static void Execute(Object ToNotify) {
        for (int i = 0; i < Threads.length; i++) {
            Threads[i].getRunnable().SetToNotify(ToNotify);
        }
        for (int i = 0; i < Threads.length; i++) {
            Threads[i].start();
        }
    }

    static {
        for (int i = 0; i < Threads.length; i++) {
            Threads[i] = new TimingThread(new FamilyLoadRunnable());
        }
    }

    private static class TimingThread extends Thread {

        private final FamilyLoad.FamilyLoadRunnable ext;

        public TimingThread(FamilyLoad.FamilyLoadRunnable r) {
            super();
            this.ext = r;
        }

        public FamilyLoad.FamilyLoadRunnable getRunnable() {
            return this.ext;
        }
    }

    private static class FamilyLoadRunnable
            implements Runnable {

        private Object ToNotify;
        private ArrayBlockingQueue<Integer> Queue = new ArrayBlockingQueue(1000);

        public void run() {
            try {
                while (!this.Queue.isEmpty()) {
                    World.Family.addLoadedFamily(new MapleFamily(((Integer) this.Queue.take()).intValue()));
                }
                synchronized (this.ToNotify) {
                    this.ToNotify.notify();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(FamilyLoad.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private void Queue(Integer hm) {
            this.Queue.add(hm);
        }

        private void SetToNotify(Object o) {
            if (this.ToNotify == null) {
                this.ToNotify = o;
            }
        }
    }
}