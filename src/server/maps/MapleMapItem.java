package server.maps;

import clientside.MapleCharacter;
import clientside.MapleClient;
import client.inventory.Item;
import java.awt.Point;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import tools.packet.CField;

public class MapleMapItem extends MapleMapObject {

    protected Item item;
    protected MapleMapObject dropper;
    protected int character_ownerid;
    protected int meso = 0;
    protected int questid = -1;
    protected byte type;
    protected boolean pickedUp = false;
    protected boolean playerDrop;
    protected boolean randDrop = false;
    protected long nextExpiry = 0L;
    protected long nextFFA = 0L;
    private ReentrantLock lock = new ReentrantLock();

    public MapleMapItem(Item item, Point position, MapleMapObject dropper, MapleCharacter owner, byte type, boolean playerDrop) {
        setPosition(position);
        this.item = item;
        this.dropper = dropper;
        this.character_ownerid = owner.getId();
        this.type = type;
        this.playerDrop = playerDrop;
    }

    public MapleMapItem(Item item, Point position, MapleMapObject dropper, MapleCharacter owner, byte type, boolean playerDrop, int questid) {
        setPosition(position);
        this.item = item;
        this.dropper = dropper;
        this.character_ownerid = owner.getId();
        this.type = type;
        this.playerDrop = playerDrop;
        this.questid = questid;
    }

    public MapleMapItem(int meso, Point position, MapleMapObject dropper, MapleCharacter owner, byte type, boolean playerDrop) {
        setPosition(position);
        this.item = null;
        this.dropper = dropper;
        this.character_ownerid = owner.getId();
        this.meso = meso;
        this.type = type;
        this.playerDrop = playerDrop;
    }

    public MapleMapItem(Point position, Item item) {
        setPosition(position);
        this.item = item;
        this.character_ownerid = 0;
        this.type = 2;
        this.playerDrop = false;
        this.randDrop = true;
    }

    public final Item getItem() {
        return this.item;
    }

    public void setItem(Item z) {
        this.item = z;
    }

    public final int getQuest() {
        return this.questid;
    }

    public final int getItemId() {
        if (getMeso() > 0) {
            return this.meso;
        }
        return this.item.getItemId();
    }

    public final MapleMapObject getDropper() {
        return this.dropper;
    }

    public final int getOwner() {
        return this.character_ownerid;
    }

    public final int getMeso() {
        return this.meso;
    }

    public final boolean isPlayerDrop() {
        return this.playerDrop;
    }

    public final boolean isPickedUp() {
        return this.pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    public byte getDropType() {
        return this.type;
    }

    public void setDropType(byte z) {
        this.type = z;
    }

    public final boolean isRandDrop() {
        return this.randDrop;
    }

    public final MapleMapObjectType getType() {
        return MapleMapObjectType.ITEM;
    }

    public void sendSpawnData(MapleClient client) {
        if ((this.questid <= 0) || (client.getPlayer().getQuestStatus(this.questid) == 1)) {
            client.getSession().write(CField.dropItemFromMapObject(this, null, getTruePosition(), (byte) 2));
        }
    }

    public void sendDestroyData(MapleClient client) {
        client.getSession().write(CField.removeItemFromMap(getObjectId(), 1, 0));
    }

    public Lock getLock() {
        return this.lock;
    }

    public void registerExpire(long time) {
        this.nextExpiry = (System.currentTimeMillis() + time);
    }

    public void registerFFA(long time) {
        this.nextFFA = (System.currentTimeMillis() + time);
    }

    public boolean shouldExpire(long now) {
        return (!this.pickedUp) && (this.nextExpiry > 0L) && (this.nextExpiry < now);
    }

    public boolean shouldFFA(long now) {
        return (!this.pickedUp) && (this.type < 2) && (this.nextFFA > 0L) && (this.nextFFA < now);
    }

    public boolean hasFFA() {
        return this.nextFFA > 0L;
    }

    public void expire(MapleMap map) {
        this.pickedUp = true;
        map.broadcastMessage(CField.removeItemFromMap(getObjectId(), 0, 0));
        map.removeMapObject(this);
        if (this.randDrop) {
            map.spawnRandDrop();
        }
    }
}