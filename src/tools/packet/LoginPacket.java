package tools.packet;

import clientside.MapleCharacter;
import constants.JobConstants;
import clientside.MapleClient;
import constants.JobConstants.LoginJob;
import constants.ServerConstants;
import handling.SendPacketOpcode;
import handling.login.LoginServer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import server.Randomizer;
import tools.HexTool;
import tools.data.MaplePacketLittleEndianWriter;

public class LoginPacket {

    public static final byte[] getHello(short mapleVersion, byte[] sendIv, byte[] recvIv) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

        mplew.writeShort(14);
        mplew.writeShort(mapleVersion);
        mplew.writeMapleAsciiString(ServerConstants.MAPLE_PATCH);
        mplew.write(recvIv);
        mplew.write(sendIv);
        mplew.write(8);

        return mplew.getPacket();
    }

    public static final byte[] getPing() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(2);

        mplew.writeShort(SendPacketOpcode.PING.getValue());

        return mplew.getPacket();
    }

//  public static byte[] getAuthSuccessRequest(MapleClient client) {
//    MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//
//    mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
//    mplew.writeZeroBytes(6);
//    mplew.writeInt(client.getAccID());
//    mplew.writeZeroBytes(9);
//    mplew.writeMapleAsciiString(client.getAccountName());
//    mplew.writeZeroBytes(10);
//    mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
//    mplew.writeInt(3);
//    mplew.write(HexTool.hex("01 00 01 01 01 00 00 00 00 00 01 01 00 00 00 01 00 01 00 00"));
//    mplew.write(1);
//    mplew.write(1);
//    mplew.writeLong(Randomizer.nextLong());
//    return mplew.getPacket();
//  }
    public static final byte[] getAuthSuccessRequest(MapleClient client) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.writeZeroBytes(6);
        mplew.writeInt(client.getAccID());
        mplew.writeLong(0);
        mplew.write(0);
        mplew.writeMapleAsciiString(client.getAccountName());
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeLong(0);

        // Jobs
        mplew.writeInt(21); //New Flag
        mplew.write(JobConstants.enableJobs ? 1 : 0);
        mplew.write(JobConstants.jobOrder);
        for (LoginJob j : LoginJob.values()) {
            mplew.write(j.getFlag());
        }
        mplew.write(1);
        mplew.write(4);

        mplew.writeLong(0); // Create date, whatever.

        return mplew.getPacket();
    }

    public static final byte[] getLoginFailed(int reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(reason);
        mplew.write(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static final byte[] getPermBan(byte reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(16);

        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.writeShort(2);
        mplew.writeInt(0);
        mplew.writeShort(reason);
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01 01 00"));

        return mplew.getPacket();
    }

    public static final byte[] getTempBan(long timestampTill, byte reason) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(17);

        mplew.writeShort(SendPacketOpcode.LOGIN_STATUS.getValue());
        mplew.write(2);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(reason);
        mplew.writeLong(timestampTill);

        return mplew.getPacket();
    }

    public static final byte[] getSecondAuthSuccess(MapleClient client) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOGIN_SECOND.getValue());
        mplew.write(0);
        mplew.writeInt(client.getAccID());
        mplew.writeZeroBytes(5);
        mplew.writeMapleAsciiString(client.getAccountName());
        mplew.writeLong(2L);
        mplew.writeZeroBytes(3);
        mplew.writeInt(Randomizer.nextInt());
        mplew.writeInt(Randomizer.nextInt());
        mplew.writeInt(28);
        mplew.writeInt(Randomizer.nextInt());
        mplew.writeInt(Randomizer.nextInt());
        mplew.write(1);

        return mplew.getPacket();
    }

    public static final byte[] deleteCharResponse(int cid, int state) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DELETE_CHAR_RESPONSE.getValue());
        mplew.writeInt(cid);
        mplew.write(state);

        return mplew.getPacket();
    }

    public static final byte[] secondPwError(byte mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.writeShort(SendPacketOpcode.SECONDPW_ERROR.getValue());
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] enableRecommended() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ENABLE_RECOMMENDED.getValue());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    public static byte[] sendRecommended(int world, String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SEND_RECOMMENDED.getValue());
        mplew.write(message != null ? 1 : 0);
        if (message != null) {
            mplew.writeInt(world);
            mplew.writeMapleAsciiString(message);
        }
        return mplew.getPacket();
    }

    public static byte[] ResetScreen() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.RESET_SCREEN.getValue());

        mplew.write(HexTool.getByteArrayFromHexString("02 08 00 32 30 31 32 30 38 30 38 00 08 00 32 30 31 32 30 38 31 35 00"));

        return mplew.getPacket();
    }

    public static final byte[] getServerList(int serverId, Map<Integer, Integer> channelLoad) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(serverId);
        String worldName = LoginServer.getTrueServerName();
        mplew.writeMapleAsciiString(worldName);
        mplew.write(LoginServer.getFlag());
        mplew.writeMapleAsciiString(LoginServer.getEventMessage());
        mplew.writeShort(100);
        mplew.writeShort(100);
        mplew.write(0);
        int lastChannel = 1;
        Set channels = channelLoad.keySet();
        for (int i = 30; i > 0; i--) {
            if (channels.contains(Integer.valueOf(i))) {
                lastChannel = i;
                break;
            }
        }
        mplew.write(lastChannel);

        for (int i = 1; i <= lastChannel; i++) {
            int load;
            if (channels.contains(Integer.valueOf(i))) {
                load = ((Integer) channelLoad.get(Integer.valueOf(i))).intValue();
            } else {
                load = 1200;
            }
            mplew.writeMapleAsciiString(worldName + "-" + i);
            mplew.writeInt(load);
            mplew.write(serverId);
            mplew.writeShort(i - 1);
        }
        mplew.writeShort(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static final byte[] getEndOfServerList() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERLIST.getValue());
        mplew.write(-1);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static final byte[] getLoginWelcome() {
        List flags = new LinkedList();

        return CField.spawnFlags(flags);
    }

    public static final byte[] getServerStatus(int status) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERSTATUS.getValue());
        mplew.writeShort(status);

        return mplew.getPacket();
    }

    public static final byte[] getChannelSelected() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHANNEL_SELECTED.getValue());
        mplew.writeZeroBytes(3);

        return mplew.getPacket();
    }

  /*  public static byte[] getCharList(String secondpw, List<MapleCharacter> chars, int charslots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
        mplew.write(0);
        mplew.write(chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr, (!chr.isGM()) && (chr.getLevel() >= 30), false);
        }
        //  mplew.write(HexTool.getByteArrayFromHexString("00 02 24 4E 00 00 B8 0B 00 00 00 65 75 00 00 05 3F 06 10 00 07 A6 5B 10 00 0B 04 05 14 00 37 20 E2 11 00 FF FF FF 00 00 00 00 04 05 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 03 00 00 00 00 00 00 00 FF FF FF FF AC D2 CE 01 40 02 5F 23 00 00 00 00 00"));
        mplew.write((secondpw != null) && (secondpw.length() <= 0) ? 2 : (secondpw != null) && (secondpw.length() > 0) ? 1 : 0);
        mplew.write(0);
        mplew.writeLong(charslots);
        mplew.writeInt(-1);
        mplew.write(HexTool.getByteArrayFromHexString("19 6E CE 01 30 FB 84 E8"));
        mplew.writeZeroBytes(5);
        return mplew.getPacket();
    }*/
        public static byte[] getCharList(String secondpw, List<MapleCharacter> chars, int charslots) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHARLIST.getValue());
        mplew.write(0);
        mplew.write(chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr, (!chr.isGM()) && (chr.getLevel() >= 30), false);
        }
        mplew.write((secondpw != null) && (secondpw.length() <= 0) ? 2 : (secondpw != null) && (secondpw.length() > 0) ? 1 : 0);
        mplew.write(0);
        mplew.writeLong(charslots);
        mplew.writeInt(-1);
        mplew.write(HexTool.getByteArrayFromHexString("19 6E CE 01 30 FB 84 E8"));
        mplew.writeZeroBytes(50);
        return mplew.getPacket();
    }

    public static final byte[] addNewCharEntry(MapleCharacter chr, boolean worked) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        mplew.write(worked ? 0 : 1);
        addCharEntry(mplew, chr, false, false);

        return mplew.getPacket();
    }

    public static final byte[] charNameResponse(String charname, boolean nameUsed) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHAR_NAME_RESPONSE.getValue());
        mplew.writeMapleAsciiString(charname);
        mplew.write(nameUsed ? 1 : 0);

        return mplew.getPacket();
    }

    private static void addCharEntry(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean ranking, boolean viewAll) {
        PacketHelper.addCharStats(mplew, chr);
        PacketHelper.addCharLook(mplew, chr, true);

        mplew.write(0);
        mplew.write(ranking ? 1 : 0);
        if (ranking) {
            mplew.writeInt(chr.getRank());
            mplew.writeInt(chr.getRankMove());
            mplew.writeInt(chr.getJobRank());
            mplew.writeInt(chr.getJobRankMove());
        }
    }

    public static byte[] showAllCharacter(int chars) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
        mplew.write(1);
        mplew.writeInt(chars);
        mplew.writeInt(chars + (3 - chars % 3));
        return mplew.getPacket();
    }

    public static byte[] showAllCharacterInfo(int worldid, List<MapleCharacter> chars, String pic) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ALL_CHARLIST.getValue());
        mplew.write(chars.size() == 0 ? 5 : 0);
        mplew.write(worldid);
        mplew.write(chars.size());
        for (MapleCharacter chr : chars) {
            addCharEntry(mplew, chr, true, true);
        }
        mplew.write(pic == null ? 0 : pic.equals("") ? 2 : 1);
        return mplew.getPacket();
    }

    public static byte[] enableSpecialCreation(int accid, boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPECIAL_CREATION.getValue());
        mplew.writeInt(accid);
        mplew.write(enable ? 0 : 1);
        mplew.write(0);

        return mplew.getPacket();
    }
}