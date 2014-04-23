package handling.login;

import constants.GameConstants;
import handling.MapleServerHandler;
import handling.mina.MapleCodecFactory;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import server.ServerProperties;
import tools.Pair;
import tools.Triple;

public class LoginServer {

    public static final int PORT = 8484;
    private static InetSocketAddress InetSocketadd;
    private static IoAcceptor acceptor;
    private static Map<Integer, Integer> load = new HashMap();
    private static String serverName;
    private static String eventMessage;
    private static byte flag;
    private static int maxCharacters;
    private static int userLimit;
    private static int usersOn = 0;
    private static boolean finishedShutdown = true;
    private static boolean adminOnly = false;
    private static HashMap<Integer, Pair<String, String>> loginAuth = new HashMap<>();
    private static HashSet<String> loginIPAuth = new HashSet<>();

    public static void putLoginAuth(int chrid, String ip, String tempIP) {
        loginAuth.put(chrid, new Pair<>(ip, tempIP));
        loginIPAuth.add(ip);
    }

    public static Pair<String, String> getLoginAuth(int chrid) {
        return loginAuth.remove(chrid);
    }

    public static boolean containsIPAuth(String ip) {
        return loginIPAuth.contains(ip);
    }

    public static void removeIPAuth(String ip) {
        loginIPAuth.remove(ip);
    }

    public static void addIPAuth(String ip) {
        loginIPAuth.add(ip);
    }

    public static final void addChannel(int channel) {
        load.put(Integer.valueOf(channel), Integer.valueOf(0));
    }

    public static final void removeChannel(int channel) {
        load.remove(Integer.valueOf(channel));
    }

    public static final void run_startup_configurations() {
        userLimit = Integer.parseInt(ServerProperties.getProperty("net.sf.odinms.login.userlimit"));
        serverName = ServerProperties.getProperty("net.sf.odinms.login.serverName");
        eventMessage = ServerProperties.getProperty("net.sf.odinms.login.eventMessage");
        flag = Byte.parseByte(ServerProperties.getProperty("net.sf.odinms.login.flag"));
        maxCharacters = Integer.parseInt(ServerProperties.getProperty("net.sf.odinms.login.maxCharacters"));
        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));

        acceptor.setHandler(new MapleServerHandler(-1, false, false));
        //acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
        ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);


        try {
            acceptor.bind(new InetSocketAddress(8484));
            System.out.println("Listening on port 8484.");
        } catch (IOException e) {
            System.err.println("Binding to port 8484 failed" + e);
        }
    }

    public static final void shutdown() {
        if (finishedShutdown) {
            return;
        }
        System.out.println("Shutting down login...");
        acceptor.setCloseOnDeactivation(true);
        for (IoSession ss : acceptor.getManagedSessions().values()) {
            ss.close(true);
        }
        acceptor.unbind();
        acceptor.dispose();

        finishedShutdown = true;
    }

    public static final String getServerName() {
        return serverName;
    }

    public static final String getTrueServerName() {
        return serverName.substring(0, serverName.length() - (GameConstants.GMS ? 2 : 3));
    }

    public static final String getEventMessage() {
        return eventMessage;
    }

    public static final byte getFlag() {
        return flag;
    }

    public static final int getMaxCharacters() {
        return 15;
    }

    public static final Map<Integer, Integer> getLoad() {
        return load;
    }

    public static void setLoad(Map<Integer, Integer> load_, int usersOn_) {
        load = load_;
        usersOn = usersOn_;
    }

    public static final void setEventMessage(String newMessage) {
        eventMessage = newMessage;
    }

    public static final void setFlag(byte newflag) {
        flag = newflag;
    }

    public static final int getUserLimit() {
        return userLimit;
    }

    public static final int getUsersOn() {
        return usersOn;
    }

    public static final void setUserLimit(int newLimit) {
        userLimit = newLimit;
    }

    public static final int getNumberOfSessions() {
        return acceptor.getManagedSessions().size();
    }

    public static final boolean isAdminOnly() {
        return adminOnly;
    }

    public static final boolean isShutdown() {
        return finishedShutdown;
    }

    public static final void setOn() {
        finishedShutdown = false;
    }
}