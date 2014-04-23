package server.life;

import clientside.MapleClient;
import server.MapleShopFactory;
import server.maps.MapleMapObjectType;
import tools.packet.CField;

public class MapleNPC extends AbstractLoadedMapleLife {

    private String name = "MISSINGNO";
    private boolean custom = false;

    public MapleNPC(int id, String name) {
        super(id);
        this.name = name;
    }

    public final boolean hasShop() {
        return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
    }

    public final void sendShop(MapleClient c) {
        MapleShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if (getId() >= 9901000) {
            return;
        }
        client.getSession().write(CField.NPCPacket.spawnNPC(this, true));
        client.getSession().write(CField.NPCPacket.spawnNPCRequestController(this, true));
    }

    @Override
    public final void sendDestroyData(MapleClient client) {
        client.getSession().write(CField.NPCPacket.removeNPCController(getObjectId()));
        client.getSession().write(CField.NPCPacket.removeNPC(getObjectId()));
    }

    @Override
    public final MapleMapObjectType getType() {
        return MapleMapObjectType.NPC;
    }

    public final String getName() {
        return this.name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public final boolean isCustom() {
        return this.custom;
    }

    public final void setCustom(boolean custom) {
        this.custom = custom;
    }
}