package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class RelativeLifeMovement extends AbstractLifeMovement {

    private short unk;

    public RelativeLifeMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    public void setUnk(short unk) {
        this.unk = unk;
    }

    public short getUnk() {
        return this.unk;
    }

    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getType());
        lew.writePos(getPosition());
        if ((getType() == 14) || (getType() == 19) || (getType() == 20) || (getType() == 24) || (getType() == 45)) {
            lew.writeShort(this.unk);
        }
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}