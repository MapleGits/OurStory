package handling.channel;

import clientside.MapleCharacter;
import constants.GameConstants;
import handling.MapleServerHandler;
import handling.cashshop.CashShopServer;
import handling.login.LoginServer;
import handling.mina.MapleCodecFactory;
import handling.world.MaplePartyCharacter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import scripting.EventScriptManager;
import server.MapleSquad;
import server.ServerProperties;
import server.Start;
import server.life.PlayerNPC;
import server.maps.AramiaFireWorks;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.shops.HiredMerchant;
import tools.ConcurrentEnumMap;
import tools.packet.CWvsContext;
//import server.events.MapleEventType;

public class ChannelServer {

    public static long serverStartTime;
    private int expRate;
    private int mesoRate;
    private int dropRate = 2;
    private int cashRate = 1;
    private int traitRate = 3;
    private int BossDropRate = 1;
    private short port = 8585;
    private static final short DEFAULT_PORT = 8585;
    private int channel;
    private int running_MerchantID = 0;
    private int flags = 0;
    private String serverMessage;
    private String ip;
    private String serverName;
    private boolean shutdown = false;
    private boolean finishedShutdown = false;
    private boolean MegaphoneMuteState = false;
    private boolean adminOnly = false;
    private PlayerStorage players;
    private IoAcceptor acceptor;
    private final MapleMapFactory mapFactory;
    private EventScriptManager eventSM;
    private AramiaFireWorks works = new AramiaFireWorks();
    private static final Map<Integer, ChannelServer> instances = new HashMap();
    private final Map<MapleSquad.MapleSquadType, MapleSquad> mapleSquads = new ConcurrentEnumMap(MapleSquad.MapleSquadType.class);
    private final Map<Integer, HiredMerchant> merchants = new HashMap();
    private final List<PlayerNPC> playerNPCs = new LinkedList();
    private final ReentrantReadWriteLock merchLock = new ReentrantReadWriteLock();
    private int eventmap = -1;
 //   private final Map<MapleEventType, MapleEvent> events = new EnumMap<MapleEventType, MapleEvent>(MapleEventType.class);
    public boolean eventOn = false;
    public int eventMap = 0;
    private boolean eventWarp;
    private String eventHost;
    private String eventName;
  //  private final Map<MapleEventType, MapleEvent> events = new EnumMap<>(MapleEventType.class);



    private ChannelServer(int channel) {
        this.channel = channel;
        this.mapFactory = new MapleMapFactory(channel);
    }

    public static Set<Integer> getAllInstance() {
        return new HashSet(instances.keySet());
    }
    


    public final void run_startup_configurations() {
        setChannel(this.channel);
        try {
            this.expRate = Integer.parseInt(ServerProperties.getProperty("net.sf.odinms.world.exp"));
            this.mesoRate = Integer.parseInt(ServerProperties.getProperty("net.sf.odinms.world.meso"));
            this.serverMessage = ServerProperties.getProperty("net.sf.odinms.world.serverMessage");
            this.serverName = ServerProperties.getProperty("net.sf.odinms.login.serverName");
            this.flags = Integer.parseInt(ServerProperties.getProperty("net.sf.odinms.world.flags", "0"));
            this.eventSM = new EventScriptManager(this, ServerProperties.getProperty("net.sf.odinms.channel.events").split(","));
            this.port = Short.parseShort(ServerProperties.getProperty("net.sf.odinms.channel.net.port" + this.channel, String.valueOf(8585 + this.channel)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.ip = (Start.nexonip + ":" + this.port);

        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec", (IoFilter) new ProtocolCodecFilter(new MapleCodecFactory()));
        players = new PlayerStorage(channel);
        try {
            acceptor.setHandler(new MapleServerHandler(channel, false, false));
            acceptor.bind(new InetSocketAddress(port));
            ((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(true);
            System.out.println("Channel " + this.channel + ": Listening on port " + this.port + "");
            this.eventSM.init();
        } catch (IOException e) {
            System.out.println("Binding to port " + this.port + " failed (ch: " + getChannel() + ")" + e);
        }

    }

    public final void shutdown() {
        if (this.finishedShutdown) {
            return;
        }
        broadcastPacket(CWvsContext.serverNotice(0, "This channel will now shut down."));

        this.shutdown = true;

        System.out.println("Channel " + this.channel + ", Saving characters...");

        getPlayerStorage().disconnectAll();
        acceptor.setCloseOnDeactivation(true);
        for (IoSession ss : acceptor.getManagedSessions().values()) {
            ss.close(true);
        }
        acceptor.unbind();
        acceptor.dispose();
        System.out.println("Channel " + this.channel + ", Unbinding...");

        instances.remove(Integer.valueOf(this.channel));
        setFinishShutdown();
    }

    public final MapleCharacter getPlayer() {
        return getPlayer();
    }

    public final List<MapleCharacter> getPartyMembers(MapleCharacter party) {
        List chars = new LinkedList();
        for (MaplePartyCharacter partychar : getPlayer().getParty().getMembers()) {
            if (partychar.getChannel() == getChannel()) {
                MapleCharacter chr = getPlayerStorage().getCharacterByName(partychar.getName());
                if (chr != null) {
                    chars.add(chr);
                }
            }
        }
        return chars;
    }

    public final boolean hasFinishedShutdown() {
        return this.finishedShutdown;
    }

    public final MapleMapFactory getMapFactory() {
        return this.mapFactory;
    }

    public static final ChannelServer newInstance(int channel) {
        return new ChannelServer(channel);
    }

    public static final ChannelServer getInstance(int channel) {
        return (ChannelServer) instances.get(Integer.valueOf(channel));
    }

    public final void addPlayer(MapleCharacter chr) {
        getPlayerStorage().registerPlayer(chr);
    }

    public PlayerStorage getPlayerStorage() {
        if (players == null) { //wth
            players = new PlayerStorage(channel); //wthhhh
        }
        return players;
    }

    public final void removePlayer(MapleCharacter chr) {
        getPlayerStorage().deregisterPlayer(chr);
    }

    public final void removePlayer(int idz, String namez) {
        getPlayerStorage().deregisterPlayer(idz, namez);
    }
    

    public final String getServerMessage() {
        return this.serverMessage;
    }

    public final void setServerMessage(String newMessage) {
        this.serverMessage = newMessage;
        broadcastPacket(CWvsContext.serverMessage(this.serverMessage));
    }

    public final void broadcastPacket(byte[] data) {
        getPlayerStorage().broadcastPacket(data);
    }

    public final void broadcastSmegaPacket(byte[] data) {
        getPlayerStorage().broadcastSmegaPacket(data);
    }

    public final void broadcastGMPacket(byte[] data) {
        getPlayerStorage().broadcastGMPacket(data);
    }

    public final int getExpRate() {
        return this.expRate;
    }

    public final void setExpRate(int expRate) {
        this.expRate = expRate;
    }

    public final int getCashRate() {
        return this.cashRate;
    }

    public final int getChannel() {
        return this.channel;
    }

    public final void setChannel(int channel) {
        instances.put(Integer.valueOf(channel), this);
        LoginServer.addChannel(channel);
    }

    public static final ArrayList<ChannelServer> getAllInstances() {
        return new ArrayList(instances.values());
    }

    public final String getIP() {
        return this.ip;
    }

    public String getIP(int channel) {
        try {
            return getIP(channel);
        } catch (Exception e) {
            System.out.println("Lost connection to world server " + e);
        }
        throw new RuntimeException("Lost connection to world server");
    }

    public final boolean isShutdown() {
        return this.shutdown;
    }

    public final int getLoadedMaps() {
        return this.mapFactory.getLoadedMaps();
    }

    public final EventScriptManager getEventSM() {
        return this.eventSM;
    }

    public final void reloadEvents() {
        this.eventSM.cancel();
        this.eventSM = new EventScriptManager(this, ServerProperties.getProperty("net.sf.odinms.channel.events").split(","));
        this.eventSM.init();
    }
    
    

    public final int getMesoRate() {
        return this.mesoRate;
    }

    public final void setMesoRate(int mesoRate) {
        this.mesoRate = mesoRate;
    }

    public final int getDropRate() {
        return this.dropRate;
    }

    public final int getBossDropRate() {
        return this.BossDropRate;
    }

    public static final void startChannel_Main() {
        serverStartTime = System.currentTimeMillis();

        for (int i = 0; i < Integer.parseInt(ServerProperties.getProperty("net.sf.odinms.channel.count", "0")); i++) {
            newInstance(i + 1).run_startup_configurations();
        }

    }

    public Map<MapleSquad.MapleSquadType, MapleSquad> getAllSquads() {
        return Collections.unmodifiableMap(this.mapleSquads);
    }

    public final MapleSquad getMapleSquad(String type) {
        return getMapleSquad(MapleSquad.MapleSquadType.valueOf(type.toLowerCase()));
    }

    public final MapleSquad getMapleSquad(MapleSquad.MapleSquadType type) {
        return (MapleSquad) this.mapleSquads.get(type);
    }

    public final boolean addMapleSquad(MapleSquad squad, String type) {
        MapleSquad.MapleSquadType types = MapleSquad.MapleSquadType.valueOf(type.toLowerCase());
        if ((types != null) && (!this.mapleSquads.containsKey(types))) {
            this.mapleSquads.put(types, squad);
            squad.scheduleRemoval();
            return true;
        }
        return false;
    }

    public final boolean removeMapleSquad(MapleSquad.MapleSquadType types) {
        if ((types != null) && (this.mapleSquads.containsKey(types))) {
            this.mapleSquads.remove(types);
            return true;
        }
        return false;
    }

    public final int closeAllMerchant() {
        int ret = 0;
        this.merchLock.writeLock().lock();
        try {
            Iterator merchants_ = this.merchants.entrySet().iterator();
            while (merchants_.hasNext()) {
                HiredMerchant hm = (HiredMerchant) ((Map.Entry) merchants_.next()).getValue();
                hm.closeShop(true, false);

                hm.getMap().removeMapObject(hm);
                merchants_.remove();
                ret++;
            }
        } finally {
            this.merchLock.writeLock().unlock();
        }

        for (int i = 910000001; i <= 910000022; i++) {
            for (MapleMapObject mmo : this.mapFactory.getMap(i).getAllHiredMerchantsThreadsafe()) {
                ((HiredMerchant) mmo).closeShop(true, false);

                ret++;
            }
        }
        return ret;
    }

    public final int addMerchant(HiredMerchant hMerchant) {
        this.merchLock.writeLock().lock();
        try {
            this.running_MerchantID += 1;
            this.merchants.put(Integer.valueOf(this.running_MerchantID), hMerchant);
            return this.running_MerchantID;
        } finally {
            this.merchLock.writeLock().unlock();
        }
    }

    public final void removeMerchant(HiredMerchant hMerchant) {
        this.merchLock.writeLock().lock();
        try {
            this.merchants.remove(Integer.valueOf(hMerchant.getStoreId()));
        } finally {
            this.merchLock.writeLock().unlock();
        }
    }

    public final boolean containsMerchant(int accid, int cid) {
        boolean contains = false;

        this.merchLock.readLock().lock();
        try {
            Iterator itr = this.merchants.values().iterator();

            while (itr.hasNext()) {
                HiredMerchant hm = (HiredMerchant) itr.next();
                if ((hm.getOwnerAccId() == accid) || (hm.getOwnerId() == cid)) {
                    contains = true;
                    break;
                }
            }
        } finally {
            this.merchLock.readLock().unlock();
        }
        return contains;
    }

    public final List<HiredMerchant> searchMerchant(int itemSearch) {
        List list = new LinkedList();
        this.merchLock.readLock().lock();
        try {
            Iterator itr = this.merchants.values().iterator();

            while (itr.hasNext()) {
                HiredMerchant hm = (HiredMerchant) itr.next();
                if (hm.searchItem(itemSearch).size() > 0) {
                    list.add(hm);
                }
            }
        } finally {
            this.merchLock.readLock().unlock();
        }
        return list;
    }

    public final void toggleMegaphoneMuteState() {
        this.MegaphoneMuteState = (!this.MegaphoneMuteState);
    }

    public final boolean getMegaphoneMuteState() {
        return this.MegaphoneMuteState;
    }



    public final Collection<PlayerNPC> getAllPlayerNPC() {
        return this.playerNPCs;
    }

    public final void addPlayerNPC(PlayerNPC npc) {
        if (this.playerNPCs.contains(npc)) {
            return;
        }
        this.playerNPCs.add(npc);
        getMapFactory().getMap(npc.getMapId()).addMapObject(npc);
    }

    public final void removePlayerNPC(PlayerNPC npc) {
        if (this.playerNPCs.contains(npc)) {
            this.playerNPCs.remove(npc);
            getMapFactory().getMap(npc.getMapId()).removeMapObject(npc);
        }
    }

    public final String getServerName() {
        return this.serverName;
    }

    public final void setServerName(String sn) {
        this.serverName = sn;
    }

    public final String getTrueServerName() {
        return this.serverName.substring(0, this.serverName.length() - (GameConstants.GMS ? 2 : 3));
    }

    public final int getPort() {
        return this.port;
    }

    public static final Set<Integer> getChannelServer() {
        return new HashSet(instances.keySet());
    }

    public final void setShutdown() {
        this.shutdown = true;
        System.out.println("Channel " + this.channel + " has set to shutdown and is closing Hired Merchants...");
    }

    public final void setFinishShutdown() {
        this.finishedShutdown = true;
        System.out.println("Channel " + this.channel + " has finished shutdown.");
    }

    public final boolean isAdminOnly() {
        return this.adminOnly;
    }

    public static final int getChannelCount() {
        return instances.size();
    }

    public final int getTempFlag() {
        return this.flags;
    }

    public static Map<Integer, Integer> getChannelLoad() {
        Map ret = new HashMap();
        for (ChannelServer cs : instances.values()) {
            ret.put(Integer.valueOf(cs.getChannel()), Integer.valueOf(cs.getConnectedClients()));
        }
        return ret;
    }

    public int getConnectedClients() {
        return getPlayerStorage().getConnectedClients();
    }

    public void broadcastMessage(byte[] message) {
        broadcastPacket(message);
    }

    public void broadcastSmega(byte[] message) {
        broadcastSmegaPacket(message);
    }

    public void broadcastGMMessage(byte[] message) {
        broadcastGMPacket(message);
    }

    public AramiaFireWorks getFireWorks() {
        return this.works;
    }

    public int getTraitRate() {
        return this.traitRate;
    }
}