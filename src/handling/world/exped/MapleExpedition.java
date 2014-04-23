package handling.world.exped;

import handling.world.MapleParty;
import handling.world.World;
import handling.world.World.Party;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MapleExpedition {

    private List<Integer> parties;
    private ExpeditionType et;
    private int leaderId;
    private int id;

    public MapleExpedition(ExpeditionType ett, int leaderId, int id) {
        this.et = ett;
        this.id = id;
        this.leaderId = leaderId;
        this.parties = new ArrayList(ett.maxParty);
    }

    public ExpeditionType getType() {
        return this.et;
    }

    public int getLeader() {
        return this.leaderId;
    }

    public List<Integer> getParties() {
        return this.parties;
    }

    public int getId() {
        return this.id;
    }

    public int getAllMembers() {
        int ret = 0;
        for (int i = 0; i < this.parties.size(); i++) {
            MapleParty pp = World.Party.getParty(((Integer) this.parties.get(i)).intValue());
            if (pp == null) {
                this.parties.remove(i);
            } else {
                ret += pp.getMembers().size();
            }
        }
        return ret;
    }

    public int getFreeParty() {
        for (int i = 0; i < this.parties.size(); i++) {
            MapleParty pp = World.Party.getParty(((Integer) this.parties.get(i)).intValue());
            if (pp == null) {
                this.parties.remove(i);
            } else if (pp.getMembers().size() < 6) {
                return pp.getId();
            }
        }
        if (this.parties.size() < this.et.maxParty) {
            return 0;
        }
        return -1;
    }

    public int getIndex(int partyId) {
        for (int i = 0; i < this.parties.size(); i++) {
            if (((Integer) this.parties.get(i)).intValue() == partyId) {
                return i;
            }
        }
        return -1;
    }

    public void setLeader(int newLead) {
        this.leaderId = newLead;
    }
}