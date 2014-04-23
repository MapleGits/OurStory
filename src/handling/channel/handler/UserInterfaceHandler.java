package handling.channel.handler;

import clientside.MapleCharacter;
import clientside.MapleClient;
import scripting.NPCScriptManager;
import tools.data.LittleEndianAccessor;

public class UserInterfaceHandler {

    public static final void CygnusSummon_NPCRequest(MapleClient c) {
        if (c.getPlayer().getJob() == 2000) {
            NPCScriptManager.getInstance().start(c, 1202000);
        } else if (c.getPlayer().getJob() == 1000) {
            NPCScriptManager.getInstance().start(c, 1101008);
        }
    }

    public static final void InGame_Poll(LittleEndianAccessor slea, MapleClient c) {
    }

    public static final void ShipObjectRequest(int mapid, MapleClient c) {
    }
}