package clientside;

import server.MapleInventoryManipulator;
import server.Randomizer;
import tools.packet.CField;

public class RockPaperScissors {

    private int round = 0;
    private boolean ableAnswer = true;
    private boolean win = false;

    public RockPaperScissors(MapleClient c, byte mode) {
        c.getSession().write(CField.getRPSMode((byte) (9 + mode), -1, -1, -1));
        if (mode == 0) {
            c.getPlayer().gainMeso(-1000L, true, true);
        }
    }

    public final boolean answer(MapleClient c, int answer) {
        if ((this.ableAnswer) && (!this.win) && (answer >= 0) && (answer <= 2)) {
            int response = Randomizer.nextInt(3);
            if (response == answer) {
                c.getSession().write(CField.getRPSMode((byte) 11, -1, (byte) response, (byte) this.round));
            } else if (((answer == 0) && (response == 2)) || ((answer == 1) && (response == 0)) || ((answer == 2) && (response == 1))) {
                c.getSession().write(CField.getRPSMode((byte) 11, -1, (byte) response, (byte) (this.round + 1)));
                this.ableAnswer = false;
                this.win = true;
            } else {
                c.getSession().write(CField.getRPSMode((byte) 11, -1, (byte) response, -1));
                this.ableAnswer = false;
            }
            return true;
        }
        reward(c);
        return false;
    }

    public final boolean timeOut(MapleClient c) {
        if ((this.ableAnswer) && (!this.win)) {
            this.ableAnswer = false;
            c.getSession().write(CField.getRPSMode((byte) 10, -1, -1, -1));
            return true;
        }
        reward(c);
        return false;
    }

    public final boolean nextRound(MapleClient c) {
        if (this.win) {
            this.round += 1;
            if (this.round < 10) {
                this.win = false;
                this.ableAnswer = true;
                c.getSession().write(CField.getRPSMode((byte) 12, -1, -1, -1));
                return true;
            }
        }
        reward(c);
        return false;
    }

    public final void reward(MapleClient c) {
        if (this.win) {
            MapleInventoryManipulator.addById(c, 4031332 + this.round, (short) 1, "", null, 0L, "Obtained from rock, paper, scissors");
        } else if (this.round == 0) {
            c.getPlayer().gainMeso(500L, true, true);
        }
        c.getPlayer().setRPS(null);
    }

    public final void dispose(MapleClient c) {
        reward(c);
        c.getSession().write(CField.getRPSMode((byte) 13, -1, -1, -1));
    }
}