package handling.world.guild;

import tools.packet.CWvsContext;
import tools.packet.CWvsContext.GuildPacket;

public enum MapleGuildResponse {

    ALREADY_IN_GUILD(46),
    NOT_IN_CHANNEL(48),
    NOT_IN_GUILD(51);
    private int value;

    private MapleGuildResponse(int val) {
        this.value = val;
    }

    public int getValue() {
        return this.value;
    }

    public byte[] getPacket() {
        return CWvsContext.GuildPacket.genericGuildMessage((byte) this.value);
    }
}