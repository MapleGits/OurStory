package tools.packet;

import clientside.MapleCharacter;
import handling.SendPacketOpcode;
import java.util.List;
import server.MapleCarnivalParty;
import tools.data.MaplePacketLittleEndianWriter;

public class MonsterCarnivalPacket {

    public static byte[] startMonsterCarnival(MapleCharacter chr, int enemyavailable, int enemytotal) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_START.getValue());
        MapleCarnivalParty friendly = chr.getCarnivalParty();
        mplew.write(friendly.getTeam());
        mplew.writeInt(chr.getAvailableCP());
        mplew.writeInt(chr.getTotalCP());
        mplew.writeInt(friendly.getAvailableCP());
        mplew.writeInt(friendly.getTotalCP());
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] playerDiedMessage(String name, int lostCP, int team) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_DIED.getValue());
        mplew.write(team);
        mplew.writeMapleAsciiString(name);
        mplew.write(lostCP);

        return mplew.getPacket();
    }

    public static byte[] playerLeaveMessage(boolean leader, String name, int team) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_LEAVE.getValue());
        mplew.write(leader ? 7 : 0);
        mplew.write(team);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] CPUpdate(boolean party, int curCP, int totalCP, int team) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_OBTAINED_CP.getValue());
        mplew.writeInt(curCP);
        mplew.writeInt(totalCP);

        return mplew.getPacket();
    }

    public static byte[] showMCStats(int left, int right) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_STATS.getValue());
        mplew.writeInt(left);
        mplew.writeInt(right);

        return mplew.getPacket();
    }

    public static byte[] playerSummoned(String name, int tab, int number) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_SUMMON.getValue());
        mplew.write(tab);
        mplew.write(number);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] showMCResult(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_RESULT.getValue());
        mplew.write(mode);

        return mplew.getPacket();
    }

    public static byte[] showMCRanking(List<MapleCharacter> players) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_RANKING.getValue());
        mplew.writeShort(players.size());
        for (MapleCharacter i : players) {
            mplew.writeInt(i.getId());
            mplew.writeMapleAsciiString(i.getName());
            mplew.writeInt(10);
            mplew.write(0);
        }

        return mplew.getPacket();
    }
}