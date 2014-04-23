package scripting;

import clientside.MapleCharacter;
import clientside.MapleClient;
import server.MaplePortal;

public class PortalPlayerInteraction extends AbstractPlayerInteraction {

    private final MaplePortal portal;

    public PortalPlayerInteraction(MapleClient c, MaplePortal portal) {
        super(c, portal.getId(), c.getPlayer().getMapId());
        this.portal = portal;
    }

    public final MaplePortal getPortal() {
        return this.portal;
    }

    public final void inFreeMarket() {
        if (getMapId() != 910000000) {
            if (getPlayer().getLevel() >= 15) {
                saveLocation("FREE_MARKET");
                playPortalSE();
                warp(910000000, "st00");
            } else {
                playerMessage(5, "You must be level 15 in order to be able to enter the FreeMarket.");
            }
        }
    }

    public final void inArdentmill() {
        if (getMapId() != 910001000) {
            if (getPlayer().getLevel() >= 10) {
                saveLocation("ARDENTMILL");
                playPortalSE();
                warp(910001000, "st00");
            } else {
                playerMessage(5, "You must be level 15 in order to be able to enter the Crafting Town.");
            }
        }
    }

    public void spawnMonster(int id) {
        spawnMonster(id, 1, this.portal.getPosition());
    }

    public void spawnMonster(int id, int qty) {
        spawnMonster(id, qty, this.portal.getPosition());
    }
}