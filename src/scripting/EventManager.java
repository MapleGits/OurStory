package scripting;

import clientside.MapleCharacter;
import constants.ServerConstants;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.script.Invocable;
import javax.script.ScriptException;
import server.MapleSquad;
import server.Randomizer;
import server.Timer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.OverrideMonsterStats;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import server.maps.MapleReactorFactory;
import tools.FileoutputUtil;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class EventManager {

    private static int[] eventChannel = new int[2];
    private Invocable iv;
    private int channel;
    private Map<String, EventInstanceManager> instances = new WeakHashMap();
    private Properties props = new Properties();
    private String name;
    private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
    private final Lock rL = this.mutex.readLock();
    private final Lock wL = this.mutex.writeLock();
    private long timeStarted = 0L;
    private long eventTime = 0L;
    private List<MapleCharacter> chars = new LinkedList();
    private EventManager em;
    private ScheduledFuture<?> eventTimer;
    private boolean disposed = false;

    public EventManager(ChannelServer cserv, Invocable iv, String name) {
        this.iv = iv;
        this.channel = cserv.getChannel();
        this.name = name;
    }

    public void cancel() {
        try {
            this.iv.invokeFunction("cancelSchedule", new Object[]{(Object) null});
        } catch (Exception ex) {
            System.out.println(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : cancelSchedule:\n").append(ex).toString());
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : cancelSchedule:\n").append(ex).toString());
        }
    }

    public ScheduledFuture<?> schedule(final String methodName, long delay) {
        return Timer.EventTimer.getInstance().schedule(new Runnable() {
            public void run() {
                try {
                    EventManager.this.iv.invokeFunction(methodName, new Object[]{(Object) null});
                } catch (Exception ex) {
                    System.out.println("Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + ex);
                    FileoutputUtil.log("Log_Script_Except.txt", "Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + ex);
                }
            }
        }, delay);
    }

    public ScheduledFuture<?> schedule(final String methodName, long delay, final EventInstanceManager eim) {
        return Timer.EventTimer.getInstance().schedule(new Runnable() {
            public void run() {
                try {
                    EventManager.this.iv.invokeFunction(methodName, new Object[]{eim});
                } catch (Exception ex) {
                    System.out.println("Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + ex);
                    FileoutputUtil.log("Log_Script_Except.txt", "Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + ex);
                }
            }
        }, delay);
    }

    public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, long timestamp) {
        return Timer.EventTimer.getInstance().scheduleAtTimestamp(new Runnable() {
            public void run() {
                try {
                    EventManager.this.iv.invokeFunction(methodName, new Object[]{(Object) null});
                } catch (ScriptException ex) {
                    System.out.println("Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + ex);
                } catch (NoSuchMethodException ex) {
                    System.out.println("Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + ex);
                }
            }
        }, timestamp);
    }

    public int getChannel() {
        return this.channel;
    }

    public ChannelServer getChannelServer() {
        return ChannelServer.getInstance(this.channel);
    }

    public EventInstanceManager getInstance(String name) {
        return (EventInstanceManager) this.instances.get(name);
    }

    public Collection<EventInstanceManager> getInstances() {
        return Collections.unmodifiableCollection(this.instances.values());
    }

    public EventInstanceManager newInstance(String name) {
        EventInstanceManager ret = new EventInstanceManager(this, name, this.channel);
        this.instances.put(name, ret);
        return ret;
    }

    public EventInstanceManager readyInstance() {
        try {
            return (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{(Object) null});
        } catch (ScriptException ex) {
            if (!ServerConstants.release) {
                ex.printStackTrace();
            }
        } catch (NoSuchMethodException ex) {
            if (!ServerConstants.release) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public void disposeInstance(String name) {
        this.instances.remove(name);
        if ((getProperty("state") != null) && (this.instances.size() == 0)) {
            setProperty("state", "0");
        }
        if ((getProperty("leader") != null) && (this.instances.size() == 0) && (getProperty("leader").equals("false"))) {
            setProperty("leader", "true");
        }
        if (this.name.equals("CWKPQ")) {
            MapleSquad squad = ChannelServer.getInstance(this.channel).getMapleSquad("CWKPQ");
            if (squad != null) {
                squad.clear();
                squad.copy();
            }
        }
    }

    public Invocable getIv() {
        return this.iv;
    }

    public void setProperty(String key, String value) {
        this.props.setProperty(key, value);
    }

    public void setPropertyAswan(String key, String value) {
        this.props.setProperty(key, value);
    }

    public String getProperty(String key) {
        return this.props.getProperty(key);
    }

    public final Properties getProperties() {
        return this.props;
    }

    public String getName() {
        return this.name;
    }

    public void startInstance() {
        try {
            this.iv.invokeFunction("setup", new Object[]{(Object) null});
        } catch (Exception ex) {
            ex.printStackTrace();
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\n").append(ex).toString());
        }
    }

    public void startInstance_Solo(String mapid, MapleCharacter chr) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{mapid});
            eim.registerPlayer(chr);
        } catch (Exception ex) {
            ex.printStackTrace();
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\n").append(ex).toString());
        }
    }

    public void startInstance(String mapid, MapleCharacter chr) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{mapid});
            eim.registerCarnivalParty(chr, chr.getMap(), (byte) 0);
        } catch (Exception ex) {
            ex.printStackTrace();
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\n").append(ex).toString());
        }
    }

    public void startInstance_Party(String mapid, MapleCharacter chr) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{mapid});
            eim.registerParty(chr.getParty(), chr.getMap());
        } catch (Exception ex) {
            ex.printStackTrace();
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup:\n").append(ex).toString());
        }
    }

    public void startInstance(MapleCharacter character, String leader) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{(Object) null});
            eim.registerPlayer(character);
            eim.setProperty("leader", leader);
            eim.setProperty("guildid", String.valueOf(character.getGuildId()));
            setProperty("guildid", String.valueOf(character.getGuildId()));
        } catch (Exception ex) {
            System.out.println(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-Guild:\n").append(ex).toString());
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-Guild:\n").append(ex).toString());
        }
    }

    public void startInstance_CharID(MapleCharacter character) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{Integer.valueOf(character.getId())});
            eim.registerPlayer(character);
        } catch (Exception ex) {
            System.out.println(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-CharID:\n").append(ex).toString());
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-CharID:\n").append(ex).toString());
        }
    }

    public void startInstance_CharMapID(MapleCharacter character) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{Integer.valueOf(character.getId()), Integer.valueOf(character.getMapId())});
            eim.registerPlayer(character);
        } catch (Exception ex) {
            System.out.println(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-CharID:\n").append(ex).toString());
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-CharID:\n").append(ex).toString());
        }
    }

    public void startInstance(MapleCharacter character) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{(Object) null});
            eim.registerPlayer(character);
        } catch (Exception ex) {
            System.out.println(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-character:\n").append(ex).toString());
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-character:\n").append(ex).toString());
        }
    }

    public void startInstance(MapleParty party, MapleMap map) {
        startInstance(party, map, 255);
    }

    public void startInstance(MapleParty party, MapleMap map, int maxLevel) {
        try {
            int averageLevel = 0;
            int size = 0;
            for (MaplePartyCharacter mpc : party.getMembers()) {
                if ((mpc.isOnline()) && (mpc.getMapid() == map.getId()) && (mpc.getChannel() == map.getChannel())) {
                    averageLevel += mpc.getLevel();
                    size++;
                }
            }
            if (size <= 0) {
                return;
            }
            averageLevel /= size;
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{Integer.valueOf(Math.min(maxLevel, averageLevel)), Integer.valueOf(party.getId())});
            eim.registerParty(party, map);
        } catch (ScriptException ex) {
            System.out.println(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-partyid:\n").append(ex).toString());
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-partyid:\n").append(ex).toString());
        } catch (Exception ex) {
            startInstance_NoID(party, map, ex);
        }
    }

    public void startInstance_NoID(MapleParty party, MapleMap map) {
        startInstance_NoID(party, map, null);
    }

    public void startInstance_NoID(MapleParty party, MapleMap map, Exception old) {
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{(Object) null});
            eim.registerParty(party, map);
        } catch (Exception ex) {
            System.out.println(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-party:\n").append(ex).toString());
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-party:\n").append(ex).append("\n").append(old == null ? "no old exception" : old).toString());
        }
    }

    public void startInstance(EventInstanceManager eim, String leader) {
        try {
            this.iv.invokeFunction("setup", new Object[]{eim});
            eim.setProperty("leader", leader);
        } catch (Exception ex) {
            System.out.println(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-leader:\n").append(ex).toString());
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-leader:\n").append(ex).toString());
        }
    }

    public void startInstance(MapleSquad squad, MapleMap map) {
        startInstance(squad, map, -1);
    }

    public void startInstance(MapleSquad squad, MapleMap map, int questID) {
        if (squad.getStatus() == 0) {
            return;
        }
        if (!squad.getLeader().isGM()) {
            if (squad.getMembers().size() < squad.getType().i) {
                squad.getLeader().dropMessage(5, new StringBuilder().append("The squad has less than ").append(squad.getType().i).append(" people participating.").toString());
                return;
            }
            if ((this.name.equals("CWKPQ")) && (squad.getJobs().size() < 5)) {
                squad.getLeader().dropMessage(5, "The squad requires members from every type of job.");
                return;
            }
        }
        try {
            EventInstanceManager eim = (EventInstanceManager) this.iv.invokeFunction("setup", new Object[]{squad.getLeaderName()});
            eim.registerSquad(squad, map, questID);
        } catch (Exception ex) {
            System.out.println(new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-squad:\n").append(ex).toString());
            FileoutputUtil.log("Log_Script_Except.txt", new StringBuilder().append("Event name : ").append(this.name).append(", method Name : setup-squad:\n").append(ex).toString());
        }
    }

    public void warpAllPlayer(int from, int to) {
        MapleMap tomap = getMapFactory().getMap(to);
        MapleMap frommap = getMapFactory().getMap(from);
        List<MapleCharacter> list = frommap.getCharactersThreadsafe();
        if ((tomap != null) && (frommap != null) && (list != null) && (frommap.getCharactersSize() > 0)) {
            for (MapleMapObject mmo : list) {
                ((MapleCharacter) mmo).changeMap(tomap, tomap.getPortal(0));
            }
        }
    }

    public MapleMapFactory getMapFactory() {
        return getChannelServer().getMapFactory();
    }

    public OverrideMonsterStats newMonsterStats() {
        return new OverrideMonsterStats();
    }

    public List<MapleCharacter> newCharList() {
        return new ArrayList();
    }

    public MapleMonster getMonster(int id) {
        return MapleLifeFactory.getMonster(id);
    }

    public MapleReactor getReactor(int id) {
        return new MapleReactor(MapleReactorFactory.getReactor(id), id);
    }

    public void broadcastShip(int mapid, int effect, int mode) {
        getMapFactory().getMap(mapid).broadcastMessage(CField.boatPacket(effect, mode));
    }

    public void broadcastYellowMsg(String msg) {
        getChannelServer().broadcastPacket(CWvsContext.yellowChat(msg));
    }

    public void broadcastServerMsg(int type, String msg, boolean weather) {
        if (!weather) {
            getChannelServer().broadcastPacket(CWvsContext.serverNotice(type, msg));
        } else {
            for (MapleMap load : getMapFactory().getAllMaps()) {
                if (load.getCharactersSize() > 0) {
                    load.startMapEffect(msg, type);
                }
            }
        }
    }

   
    public void timeOut(long delay, final EventManager eim) {
        if ((this.disposed) || (eim == null)) {
            return;
        }
        this.eventTimer = Timer.EventTimer.getInstance().schedule(new Runnable() {
            public void run() {
                if ((EventManager.this.disposed) || (eim == null) || (EventManager.this.em == null)) {
                    return;
                }
                try {
                    EventManager.this.em.getIv().invokeFunction("scheduledTimeout", new Object[]{eim});
                } catch (Exception ex) {
                    FileoutputUtil.log("Log_Script_Except.txt", "Event name" + EventManager.this.em.getName() + ", Instance name : " + EventManager.this.name + ", method Name : scheduledTimeout:\n" + ex);
                    System.out.println("Event name" + EventManager.this.em.getName() + ", Instance name : " + EventManager.this.name + ", method Name : scheduledTimeout:\n" + ex);
                }
            }
        }, delay);
    }

    public void startEventTimer(long time) {
        try {
            if (this.disposed) {
                return;
            }
            this.timeStarted = System.currentTimeMillis();
            this.eventTime = time;
            if (this.eventTimer != null) {
                this.eventTimer.cancel(false);
            }
            this.eventTimer = null;
            int timesend = (int) time / 1000;

            for (MapleCharacter chr : getPlayers()) {
                chr.getClient().getSession().write(CField.getClock(timesend));
            }
            timeOut(time, this);
        } catch (Exception ex) {
            FileoutputUtil.outputFileError("Log_Script_Except.txt", ex);
            System.out.println(new StringBuilder().append("Event name").append(this.em.getName()).append(", Instance name : ").append(this.name).append(", method Name : restartEventTimer:\n").toString());
            ex.printStackTrace();
        }
    }

    public List<MapleCharacter> getPlayers() {
        if (this.disposed) {
            return Collections.emptyList();
        }
        this.rL.lock();
        try {
            return new LinkedList(this.chars);
        } finally {
            this.rL.unlock();
        }
    }

    public void setWorldEvent() {
        for (int i = 0; i < eventChannel.length; i++) {
            eventChannel[i] = (Randomizer.nextInt(ChannelServer.getAllInstances().size() - 4) + 2 + i);
        }
    }
}