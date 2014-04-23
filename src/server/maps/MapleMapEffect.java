package server.maps;

import clientside.MapleClient;
import tools.packet.CField;
import tools.packet.MTSCSPacket;

public class MapleMapEffect {

    private String msg = "";
    private int itemId = 0;
    private boolean active = true;
    private boolean jukebox = false;

    public MapleMapEffect(String msg, int itemId) {
        this.msg = msg;
        this.itemId = itemId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setJukebox(boolean actie) {
        this.jukebox = actie;
    }

    public boolean isJukebox() {
        return this.jukebox;
    }

    public byte[] makeDestroyData() {
        return this.jukebox ? MTSCSPacket.playCashSong(0, "") : CField.removeMapEffect();
    }

    public byte[] makeStartData() {
        return this.jukebox ? MTSCSPacket.playCashSong(this.itemId, this.msg) : CField.startMapEffect(this.msg, this.itemId, this.active);
    }

    public void sendStartData(MapleClient c) {
        c.getSession().write(makeStartData());
    }
}