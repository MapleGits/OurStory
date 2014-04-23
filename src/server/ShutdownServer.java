package server;

import database.DatabaseConnection;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.World;
import handling.world.World.Alliance;
import handling.world.World.Broadcast;
import handling.world.World.Family;
import handling.world.World.Guild;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import tools.packet.CWvsContext;

public class ShutdownServer
        implements ShutdownServerMBean {

    public static ShutdownServer instance;
    public int mode = 0;

    public static void registerMBean() {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            instance = new ShutdownServer();
            mBeanServer.registerMBean(instance, new ObjectName("server:type=ShutdownServer"));
        } catch (Exception e) {
            System.out.println("Error registering Shutdown MBean");
            e.printStackTrace();
        }
    }

    public static ShutdownServer getInstance() {
        return instance;
    }

    public void shutdown() {
        run();
    }

    public void run() {
        if (this.mode == 0) {
            int ret = 0;
            World.Broadcast.broadcastMessage(CWvsContext.serverNotice(0, "The world is going to shutdown soon. Please log off safely."));
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.setShutdown();
                cs.setServerMessage("The world is going to shutdown soon. Please log off safely.");
                ret += cs.closeAllMerchant();
            }

            World.Guild.save();
            World.Alliance.save();
            World.Family.save();
            System.out.println("Shutdown 1 has completed. Hired merchants saved: " + ret);
            this.mode += 1;
        } else if (this.mode == 1) {
            this.mode += 1;
            System.out.println("Shutdown 2 commencing...");
            try {
                World.Broadcast.broadcastMessage(CWvsContext.serverNotice(0, "The world is going to shutdown now. Please log off safely."));
                Integer[] chs = (Integer[]) ChannelServer.getAllInstance().toArray(new Integer[0]);

                Integer[] arr$ = chs;
                int len$ = arr$.length;
                for (int i$ = 0; i$ < len$; i$++) {
                    int i = arr$[i$].intValue();
                    try {
                        ChannelServer cs = ChannelServer.getInstance(i);
                        synchronized (this) {
                            cs.shutdown();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                LoginServer.shutdown();
                CashShopServer.shutdown();
                DatabaseConnection.closeAll();
            } catch (SQLException e) {
                System.err.println("THROW" + e);
            }
            Timer.WorldTimer.getInstance().stop();
            Timer.MapTimer.getInstance().stop();
            Timer.BuffTimer.getInstance().stop();
            Timer.CloneTimer.getInstance().stop();
            Timer.EventTimer.getInstance().stop();
            Timer.EtcTimer.getInstance().stop();
            Timer.PingTimer.getInstance().stop();
            System.out.println("Shutdown 2 has finished.");
            try {
                Thread.sleep(5000L);
            } catch (Exception e) {
            }
            System.exit(0);
        }
    }
}