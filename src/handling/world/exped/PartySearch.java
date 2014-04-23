package handling.world.exped;

import handling.world.World;
import handling.world.World.Party;
import java.util.concurrent.ScheduledFuture;
import server.Timer;
import server.Timer.EtcTimer;

public class PartySearch {

    private String name;
    private int partyId;
    private PartySearchType pst;
    private ScheduledFuture<?> removal;

    public PartySearch(String name, int partyId, PartySearchType pst) {
        this.name = name;
        this.partyId = partyId;
        this.pst = pst;
        scheduleRemoval();
    }

    public PartySearchType getType() {
        return this.pst;
    }

    public int getId() {
        return this.partyId;
    }

    public String getName() {
        return this.name;
    }

    public void scheduleRemoval() {
        cancelRemoval();
        this.removal = Timer.EtcTimer.getInstance().schedule(new Runnable() {
            public void run() {
                World.Party.removeSearch(PartySearch.this, "The Party Listing was removed because it has expired.");
            }
        }, this.pst.timeLimit * 60000);
    }

    public void cancelRemoval() {
        if (this.removal != null) {
            this.removal.cancel(false);
            this.removal = null;
        }
    }
}