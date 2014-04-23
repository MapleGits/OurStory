package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class AbsoluteLifeMovement extends AbstractLifeMovement {

    private Point pixelsPerSecond;
    private Point offset;
    private short unk;
    private short fh;

    public AbsoluteLifeMovement(int type, Point position, int duration, int newstate) {
        super(type, position, duration, newstate);
    }

    public void setPixelsPerSecond(Point wobble) {
        this.pixelsPerSecond = wobble;
    }

    public void setOffset(Point wobble) {
        this.offset = wobble;
    }

    public void setFh(short fh) {
        this.fh = fh;
    }

    public void setUnk(short unk) {
        this.unk = unk;
    }

    public short getUnk() {
        return this.unk;
    }

    public void defaulted() {
        this.unk = 0;
        this.fh = 0;
        this.pixelsPerSecond = new Point(0, 0);
        this.offset = new Point(0, 0);
    }

    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(getType());
        lew.writePos(getPosition());
        lew.writePos(this.pixelsPerSecond);
        lew.writeShort(this.unk);
        if (getType() == 15) {
            lew.writeShort(this.fh);
        }
        if (getType() != 44) {
            lew.writePos(this.offset);
        }
        lew.write(getNewstate());
        lew.writeShort(getDuration());
    }
}