package server.maps;

import clientside.MapleCharacter;
import clientside.MapleClient;
import java.awt.Point;
import java.util.List;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import tools.packet.CField;

public class MapleHaku extends AnimatedMapleMapObject {

    private int owner;
    private int jobid;
    private int fh;
    private boolean stats;
    private Point pos = new Point(0, 0);

    public MapleHaku(MapleCharacter owner) {
        this.owner = owner.getId();
        this.jobid = owner.getJob();
        this.fh = owner.getFH();
        this.stats = false;

        if ((this.jobid < 4200) || (this.jobid > 4212)) {
            throw new RuntimeException("Trying to create a dragon for a non-Evan");
        }
        setPosition(owner.getTruePosition());
        setStance(this.fh);
    }

    public void sendSpawnData(MapleClient client) {
        client.getSession().write(CField.spawnHaku(this));

    }

    public void sendDestroyData(MapleClient client) {
        client.getSession().write(CField.removeDragon(this.owner));
    }

    public int getOwner() {
        return this.owner;
    }

    public int getJobId() {
        return this.jobid;
    }

    public void sendstats() {
        this.stats = (!this.stats);
    }

    public boolean getstats() {
        return this.stats;
    }

    public MapleMapObjectType getType() {
        return MapleMapObjectType.SUMMON;
    }

    public final Point getPos() {
        return this.pos;
    }

    public final void setPos(Point pos) {
        this.pos = pos;
    }

    public final void updatePosition(List<LifeMovementFragment> movement) {
        for (LifeMovementFragment move : movement) {
            if ((move instanceof LifeMovement)) {
                if ((move instanceof AbsoluteLifeMovement)) {
                    setPos(((LifeMovement) move).getPosition());
                }
                setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}