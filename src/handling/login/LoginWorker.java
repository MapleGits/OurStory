package handling.login;

import clientside.MapleClient;
import handling.channel.ChannelServer;
import handling.login.handler.CharLoginHandler;
import java.util.Map;
import java.util.Map.Entry;
import server.Timer;
import tools.packet.LoginPacket;

public class LoginWorker {

    private static long lastUpdate = 0L;

    public static void registerClient(final MapleClient c) {
        if (System.currentTimeMillis() - lastUpdate > 600000L) {
            lastUpdate = System.currentTimeMillis();
            Map<Integer, Integer> load = ChannelServer.getChannelLoad();
            int usersOn = 0;
            if ((load == null) || (load.size() <= 0)) {
                lastUpdate = 0L;
                c.getSession().write(LoginPacket.getLoginFailed(7));
                return;
            }
            double loadFactor = 1200.0D / (LoginServer.getUserLimit() / load.size());
            for (Entry<Integer, Integer> entry : load.entrySet()) {
                usersOn += ((Integer) entry.getValue()).intValue();
                load.put(entry.getKey(), Integer.valueOf(Math.min(1200, (int) (((Integer) entry.getValue()).intValue() * loadFactor))));
            }
            LoginServer.setLoad(load, usersOn);
            lastUpdate = System.currentTimeMillis();
        }

        if (c.finishLogin() == 0) {
           c.getSession().write(LoginPacket.getAuthSuccessRequest(c));
            CharLoginHandler.ServerListRequest(c);

            c.setIdleTask(Timer.PingTimer.getInstance().schedule(new Runnable() {
                @Override
                public void run() {
                    c.getSession().close(true);
                }
            }, 6000000L));
        } else {
            c.getSession().write(LoginPacket.getLoginFailed(7));
        }
    }
}