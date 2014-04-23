package server.maps;

import clientside.MapleCharacter;
import clientside.MapleClient;
import java.awt.Point;
import tools.packet.CField;

public class MechDoor extends MapleMapObject {

    private int owner;
    private int partyid;
    private int id;

    public MechDoor(MapleCharacter owner, Point pos, int id) {
        this.owner = owner.getId();
        this.partyid = (owner.getParty() == null ? 0 : owner.getParty().getId());
        setPosition(pos);
        this.id = id;
    }

    public void sendSpawnData(MapleClient client) {
        client.getSession().write(CField.spawnMechDoor(this, false));
    }

    public void sendDestroyData(MapleClient client) {
        client.getSession().write(CField.removeMechDoor(this, false));
    }

    public int getOwnerId() {
        return this.owner;
    }

    public int getPartyId() {
        return this.partyid;
    }

    public int getId() {
        return this.id;
    }

    public MapleMapObjectType getType() {
        return MapleMapObjectType.DOOR;
    }
}