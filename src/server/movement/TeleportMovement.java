package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class TeleportMovement extends AbstractLifeMovement {

    private short fh;

    public TeleportMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    public void setFh(short fh) {
        this.fh = fh;
    }

    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getType());
        lew.writePos(getPosition());
        lew.writeShort(this.fh);
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}