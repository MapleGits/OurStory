package handling.cashshop;

import handling.MapleServerHandler;
import handling.channel.PlayerStorage;
import handling.mina.MapleCodecFactory;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import server.MTSStorage;
import server.ServerProperties;

public class CashShopServer {

    private static String ip;
    private static InetSocketAddress InetSocketadd;
    private static final int PORT = 8790;
    private static IoAcceptor acceptor;
    private static PlayerStorage players;
    private static PlayerStorage playersMTS;
    private static boolean finishedShutdown = false;

    public static final void run_startup_configurations() {
        ip = ServerProperties.getProperty("net.sf.odinms.world.host") + ":" + 8790;

        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));
        ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);
        players = new PlayerStorage(-10);
        playersMTS = new PlayerStorage(-20);
        try {
            acceptor.setHandler(new MapleServerHandler(-1, true, false));
            acceptor.bind(new InetSocketAddress(8790));



            System.out.println("Listening on port 8790.");
        } catch (Exception e) {
            System.err.println("Binding to port 8790 failed");
            e.printStackTrace();
            throw new RuntimeException("Binding failed.", e);
        }
    }

    public static final String getIP() {
        return ip;
    }

    public static final PlayerStorage getPlayerStorage() {
        return players;
    }

    public static final PlayerStorage getPlayerStorageMTS() {
        return playersMTS;
    }

    public static final void shutdown() {
        if (finishedShutdown) {
            return;
        }
        System.out.println("Saving all connected clients (CS)...");
        players.disconnectAll();
        playersMTS.disconnectAll();
        MTSStorage.getInstance().saveBuyNow(true);
        System.out.println("Shutting down CS...");

        finishedShutdown = true;
    }

    public static boolean isShutdown() {
        return finishedShutdown;
    }
}