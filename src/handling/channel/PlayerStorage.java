package handling.channel;

import clientside.MapleCharacter;
import clientside.MapleCharacterUtil;
import handling.world.CharacterTransfer;
import handling.world.World;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import server.Timer;

public class PlayerStorage {

    private final ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();
    private final Lock rL = this.mutex.readLock();
    private final Lock wL = this.mutex.writeLock();
    private final ReentrantReadWriteLock mutex2 = new ReentrantReadWriteLock();
    private final Lock rL2 = this.mutex2.readLock();
    private final Lock wL2 = this.mutex2.writeLock();
    private final Map<String, MapleCharacter> nameToChar = new HashMap();
    private final Map<Integer, MapleCharacter> idToChar = new HashMap();
    private final Map<Integer, CharacterTransfer> PendingCharacter = new HashMap<>();
    private int channel;

    public PlayerStorage(int channel) {
        this.channel = channel;

        Timer.PingTimer.getInstance().register(new PersistingTask(), 60000L);
    }

    public final ArrayList<MapleCharacter> getAllCharacters() {
        this.rL.lock();
        try {
            return new ArrayList(this.idToChar.values());
        } finally {
            this.rL.unlock();
        }
    }

    public final void registerPlayer(MapleCharacter chr) {
        this.wL.lock();
        try {
            this.nameToChar.put(chr.getName().toLowerCase(), chr);
            this.idToChar.put(Integer.valueOf(chr.getId()), chr);
        } finally {
            this.wL.unlock();
        }
        World.Find.register(chr.getId(), chr.getName(), this.channel);
    }

    public final void registerPendingPlayer(final CharacterTransfer chr, final int playerid) {
        wL2.lock();
        try {
            PendingCharacter.put(playerid, chr);//new Pair(System.currentTimeMillis(), chr));
        } finally {
            wL2.unlock();
        }
    }

    public final void deregisterPlayer(final MapleCharacter chr) {
        wL.lock();
        try {
            nameToChar.remove(chr.getName().toLowerCase());
            idToChar.remove(chr.getId());
        } finally {
            wL.unlock();
        }
        World.Find.forceDeregister(chr.getId(), chr.getName());
    }

    public final void deregisterPlayer(final int idz, final String namez) {
        wL.lock();
        try {
            nameToChar.remove(namez.toLowerCase());
            idToChar.remove(idz);
        } finally {
            wL.unlock();
        }
        World.Find.forceDeregister(idz, namez);
    }

    public final int pendingCharacterSize() {
        return PendingCharacter.size();
    }

    public final void deregisterPendingPlayer(final int charid) {
        wL2.lock();
        try {
            PendingCharacter.remove(charid);
        } finally {
            wL2.unlock();
        }
    }

    public final CharacterTransfer getPendingCharacter(final int charid) {
        wL2.lock();
        try {
            return PendingCharacter.remove(charid);
        } finally {
            wL2.unlock();
        }
    }

    public final MapleCharacter getCharacterByName(final String name) {
        rL.lock();
        try {
            return nameToChar.get(name.toLowerCase());
        } finally {
            rL.unlock();
        }
    }

    public final MapleCharacter getCharacterById(int id) {
        this.rL.lock();
        try {
            return (MapleCharacter) this.idToChar.get(Integer.valueOf(id));
        } finally {
            this.rL.unlock();
        }
    }

    public final int getConnectedClients() {
        return this.idToChar.size();
    }

    public final void disconnectAll() {
        disconnectAll(false);
    }

    public final void disconnectAll(boolean checkGM) {
        this.wL.lock();
        try {
            Iterator itr = this.nameToChar.values().iterator();

            while (itr.hasNext()) {
                MapleCharacter chr = (MapleCharacter) itr.next();

                if ((!chr.isGM()) || (!checkGM)) {
                    chr.getClient().disconnect(false, false, true);
                    chr.getClient().getSession().close(true);
                    World.Find.forceDeregister(chr.getId(), chr.getName());
                    itr.remove();
                }
            }
        } finally {
            this.wL.unlock();
        }
    }

    public final String getOnlinePlayers(boolean byGM) {
        StringBuilder sb = new StringBuilder();

        if (byGM) {
            this.rL.lock();
            try {
                Iterator itr = this.nameToChar.values().iterator();
                while (itr.hasNext()) {
                    sb.append(MapleCharacterUtil.makeMapleReadable(((MapleCharacter) itr.next()).getName()));
                    sb.append(", ");
                }
            } finally {
                this.rL.unlock();
            }
        } else {
            this.rL.lock();
            try {
                Iterator itr = this.nameToChar.values().iterator();

                while (itr.hasNext()) {
                    MapleCharacter chr = (MapleCharacter) itr.next();

                    if (!chr.isGM()) {
                        sb.append(MapleCharacterUtil.makeMapleReadable(chr.getName()));
                        sb.append(", ");
                    }
                }
            } finally {
                this.rL.unlock();
            }
        }
        return sb.toString();
    }

    public final void broadcastPacket(byte[] data) {
        this.rL.lock();
        try {
            Iterator itr = this.nameToChar.values().iterator();
            while (itr.hasNext()) {
                ((MapleCharacter) itr.next()).getClient().getSession().write(data);
            }
        } finally {
            this.rL.unlock();
        }
    }

    public final void broadcastSmegaPacket(byte[] data) {
        this.rL.lock();
        try {
            Iterator itr = this.nameToChar.values().iterator();

            while (itr.hasNext()) {
                MapleCharacter chr = (MapleCharacter) itr.next();

                if ((chr.getClient().isLoggedIn()) && (chr.getSmega())) {
                    chr.getClient().getSession().write(data);
                }
            }
        } finally {
            this.rL.unlock();
        }
    }

    public final void broadcastGMPacket(byte[] data) {
        this.rL.lock();
        try {
            Iterator itr = this.nameToChar.values().iterator();

            while (itr.hasNext()) {
                MapleCharacter chr = (MapleCharacter) itr.next();

                if ((chr.getClient().isLoggedIn()) && (chr.isIntern())) {
                    chr.getClient().getSession().write(data);
                }
            }
        } finally {
            this.rL.unlock();
        }
    }

    public final void NameDereg(String name) {
        wL.lock();
        try {
            nameToChar.remove(name.toLowerCase());
        } finally {
            wL.unlock();
        }

    }

    public class PersistingTask implements Runnable {

        @Override
        public void run() {
            wL2.lock();
            try {
                final long currenttime = System.currentTimeMillis();
                final Iterator<Map.Entry<Integer, CharacterTransfer>> itr = PendingCharacter.entrySet().iterator();

                while (itr.hasNext()) {
                    if (currenttime - itr.next().getValue().TranferTime > 40000) { // 40 sec
                        itr.remove();
                    }
                }
            } finally {
                wL2.unlock();
            }
        }
    }
}