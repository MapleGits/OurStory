package handling.world;

import clientside.MapleBuffStat;
import java.io.Serializable;
import java.util.Map;
import server.MapleStatEffect;

public class PlayerBuffValueHolder
        implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    public long startTime;
    public int localDuration;
    public int cid;
    public MapleStatEffect effect;
    public Map<MapleBuffStat, Integer> statup;

    public PlayerBuffValueHolder(long startTime, MapleStatEffect effect, Map<MapleBuffStat, Integer> statup, int localDuration, int cid) {
        this.startTime = startTime;
        this.effect = effect;
        this.statup = statup;
        this.localDuration = localDuration;
        this.cid = cid;
    }
}