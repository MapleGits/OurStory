package server.movement;

import java.awt.Point;
import tools.data.MaplePacketLittleEndianWriter;

public class ChangeEquipSpecialAwesome
        implements LifeMovementFragment {

    private int type;
    private int wui;

    public ChangeEquipSpecialAwesome(int type, int wui) {
        this.type = type;
        this.wui = wui;
    }

    public void serialize(MaplePacketLittleEndianWriter lew) {
        lew.write(this.type);
        lew.write(this.wui);
    }

    public Point getPosition() {
        return new Point(0, 0);
    }
}