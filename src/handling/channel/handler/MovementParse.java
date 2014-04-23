package handling.channel.handler;

import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import server.maps.AnimatedMapleMapObject;
import server.movement.AbsoluteLifeMovement;
import server.movement.ChangeEquipSpecialAwesome;
import server.movement.GroundMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import server.movement.RelativeLifeMovement;
import server.movement.TeleportMovement;
import tools.FileoutputUtil;
import tools.data.LittleEndianAccessor;

public class MovementParse {

    public static List<LifeMovementFragment> parseMovement(LittleEndianAccessor lea, int kind) {
        List res = new ArrayList();
        byte numCommands = lea.readByte();

        for (byte i = 0; i < numCommands; i = (byte) (i + 1)) {
            byte command = lea.readByte();
            switch (command) {
                case 0:
                case 7:
                case 8:
                case 15:
                case 16:
                case 17:
                case 53: {
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short xwobble = lea.readShort();
                    short ywobble = lea.readShort();
                    short unk = lea.readShort();
                    short fh = 0;
                    short xoffset = 0;
                    short yoffset = 0;
                    if (command == 15) {
                        fh = lea.readShort();
                    }
                    if (command != 44) {
                        xoffset = lea.readShort();
                        yoffset = lea.readShort();
                    }
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();

                    AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    alm.setUnk(unk);
                    alm.setFh(fh);
                    alm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    alm.setOffset(new Point(xoffset, yoffset));

                    res.add(alm);
                    break;
                }
                case 1:
                case 2:
                case 14:
                case 19:
                case 20:
                case 21:
                case 24:
                case 45: {
                    short xmod = lea.readShort();
                    short ymod = lea.readShort();
                    short unk = 0;
                    if ((command == 14) || (command == 19) || (command == 20) || (command == 24) || (command == 45)) {
                        unk = lea.readShort();
                    }
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();

                    RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(xmod, ymod), duration, newstate);

                    rlm.setUnk(unk);
                    res.add(rlm);
                    break;
                }
                case 18:
                case 22:
                case 23:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 46: {
                    byte newstate = lea.readByte();
                    short unk = lea.readShort();

                    GroundMovement am = new GroundMovement(command, new Point(0, 0), unk, newstate);

                    res.add(am);
                    break;
                }
                case 3:
                case 4:
                case 5:
                case 6:
                case 9:
                case 10:
                case 11:
                case 13:
                case 47: {
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short fh = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();

                    TeleportMovement tm = new TeleportMovement(command, new Point(xpos, ypos), duration, newstate);
                    tm.setFh(fh);

                    res.add(tm);
                    break;
                }
                case 12:
                    res.add(new ChangeEquipSpecialAwesome(command, lea.readByte()));
                    break;
                case 48:
                case 49:
                case 50:
                case 51:
                case 52:
                default:
                    System.out.println("Kind movement: " + kind + ", Remaining : " + (numCommands - res.size()) + " New type of movement ID : " + command + ", packet : " + lea.toString(true));
                    FileoutputUtil.log("Log_Movement.txt", "Kind movement: " + kind + ", Remaining : " + (numCommands - res.size()) + " New type of movement ID : " + command + ", packet : " + lea.toString(true));
                    return null;
            }
        }
        if (numCommands != res.size()) {
            return null;
        }
        return res;
    }

    public static void updatePosition(List<LifeMovementFragment> movement, AnimatedMapleMapObject target, int yoffset) {
        if (movement == null) {
            return;
        }
        for (LifeMovementFragment move : movement) {
            if ((move instanceof LifeMovement)) {
                if ((move instanceof AbsoluteLifeMovement)) {
                    Point position = ((LifeMovement) move).getPosition();
                    position.y += yoffset;
                    target.setPosition(position);
                }
                target.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}