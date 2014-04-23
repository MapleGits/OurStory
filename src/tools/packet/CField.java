package tools.packet;

import clientside.MapleBuffStat;
import clientside.MapleCharacter;
import clientside.MapleClient;
import clientside.MapleKeyLayout;
import clientside.MapleQuestStatus;
import clientside.MonsterFamiliar;
import clientside.Skill;
import clientside.SkillMacro;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleAndroid;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import constants.GameConstants;
import handling.SendPacketOpcode;
import handling.channel.handler.PlayerInteractionHandler;
import handling.world.World;
import handling.world.guild.MapleGuild;
import java.awt.Point;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import server.MapleDueyActions;
import server.MapleShop;
import server.MapleTrade;
import server.Randomizer;
import server.life.MapleNPC;
import server.maps.MapleDragon;
import server.maps.MapleHaku;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import server.maps.MapleMist;
import server.maps.MapleReactor;
import server.maps.MapleSummon;
import server.maps.MechDoor;
import server.maps.MapleNodes;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import tools.AttackPair;
import tools.HexTool;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

public class CField {

    public static int DEFAULT_BUFFMASK = 0;
    public static final byte[] Nexon_IP = new byte[]{(byte) 8, (byte) 31, (byte) 99, (byte) 141};

    public static byte[] getPacketFromHexString(String hex) {
        return HexTool.getByteArrayFromHexString(hex);
    }

    public static byte[] unlockSkill() {
        MaplePacketLittleEndianWriter wh = new MaplePacketLittleEndianWriter();
        wh.writeShort(SendPacketOpcode.UNLOCK_SKILL.getValue());
        wh.writeInt(0);
        return wh.getPacket();
    }
  public static byte[] TrifleWorm(int cid, int skillid, int ga, int oid, int gu) {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(7);
        packet.write(1);
        packet.writeInt(gu);
        packet.writeInt(oid);
        packet.writeInt(skillid);

        for (int i = 0; i < ga; i++) {
            packet.write(1);
            packet.writeInt(1 + i);
            packet.writeInt(1);
            packet.writeInt(Randomizer.rand(0x2A, 0x2F));
            packet.writeInt(7 + i);
            packet.writeInt(Randomizer.rand(5, 0xAB));
            packet.writeInt(Randomizer.rand(0, 0x37));
            packet.writeLong(0);
        }
        packet.write(0);
        return packet.getPacket();
    }
    public static final byte[] getClientAuth(int pResponse) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.CLIENT_AUTH.getValue());
        mplew.writeInt(pResponse);
        return mplew.getPacket();
    }

    public static byte[] RechargeEffect() {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        packet.write(0x2B);
        return packet.getPacket();
    }

    public static byte[] PinPointRocket(int cid, List<Integer> moblist) {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(6);
        packet.write(1);
        packet.writeInt(moblist.size());
        for (int i = 0; i < moblist.size(); i++) {
            packet.writeInt(moblist.get(i));
        }
        packet.writeInt(36001005);
        for (int i = 1; i <= moblist.size(); i++) {
            packet.write(1);
            packet.writeInt(i + 1);
            packet.writeInt(0);
            packet.writeInt(Randomizer.rand(10, 20));
            packet.writeInt(Randomizer.rand(20, 40));
            packet.writeInt(Randomizer.rand(40, 200));
            packet.writeInt(Randomizer.rand(500, 2000));
            packet.writeLong(0); //v196
        }
        packet.write(0);
        return packet.getPacket();
    }

    public static byte[] ShieldChacing(int cid, List<Integer> moblist, int skillid) {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
        packet.write(0);
        packet.writeInt(cid);
        packet.writeInt(3);
        packet.write(1);
        packet.writeInt(moblist.size());
        for (int i = 0; i < moblist.size(); i++) {
            packet.writeInt(moblist.get(i));
        }
        packet.writeInt(skillid);
        for (int i = 1; i <= moblist.size(); i++) {
            packet.write(1);
            packet.writeInt(1 + i);
            packet.writeInt(3);
            packet.writeInt(Randomizer.rand(1, 20));
            packet.writeInt(Randomizer.rand(20, 50));
            packet.writeInt(Randomizer.rand(50, 200));
            packet.writeInt(skillid == 2121055 ? 720 : 660);
            packet.writeLong(0); //v196
        }
        packet.write(0);
        return packet.getPacket();
    }

    public static byte[] cancelPollingMoon() {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();

        packet.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        packet.writeLong(0);
        packet.write(HexTool.getByteArrayFromHexString("00 00 01 00 00 00 04 80"));
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeZeroBytes(100);
        return packet.getPacket();
    }

    public static byte[] cancelLizingSun() {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();

        packet.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
        packet.writeLong(0);
        packet.write(HexTool.getByteArrayFromHexString("00 00 01 00 00 00 00 00"));
        packet.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 01 00 80"));
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0);
        packet.writeLong(0);
        packet.write(0);
        return packet.getPacket();
    }

    public static byte[] getServerIP(MapleClient c, int port, int clientId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVER_IP.getValue());
        mplew.writeShort(0);
        mplew.write(Nexon_IP);
        mplew.writeShort(port);
        mplew.writeInt(clientId);
        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeInt(1);
        mplew.writeShort(1);
        // mplew.write(HexTool.hex("00 00 00 00 00 00 00 00 00 00 01 00 00 00 01 00"));

        return mplew.getPacket();
    }

    public static byte[] SoulSeekerRegen(MapleCharacter chr, int sn) {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
        packet.write(1);
        packet.writeInt(chr.getId());
        packet.writeInt(sn);
        packet.writeInt(4);
        packet.write(1);
        packet.writeInt(sn);
        packet.writeInt(65111007); // hide skills
        packet.write(1);
        packet.writeInt(Randomizer.rand(0x06, 0x08));
        packet.writeInt(1);
        packet.writeInt(Randomizer.rand(0x28, 0x2B));
        packet.writeInt(Randomizer.rand(0x03, 0x04));
        packet.writeInt(Randomizer.rand(0xFA, 0x126));
        packet.writeInt(0);
        packet.writeLong(0); //v196
        packet.write(0);
        return packet.getPacket();
    }

    public static byte[] SoulSeeker(MapleCharacter chr, int skillid, int sn, int sc1, int sc2) {
        MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();
        packet.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
        packet.write(0);
        packet.writeInt(chr.getId());
        packet.writeInt(0x03);
        packet.write(1);
        packet.writeInt(sn);
        if (sn >= 1) {
            packet.writeInt(sc1);//SHOW_ITEM_GAIN_INCHAT
            if (sn == 2) {
                packet.writeInt(sc2);
            }
        }
        packet.writeInt(65111007); // hide skills
        for (int i = 0; i < 4; i++) {
            packet.write(1);
            packet.writeInt(i + 2);
            packet.writeInt(1);
            packet.writeInt(Randomizer.rand(0x10, 0x12));
            packet.writeInt(Randomizer.rand(0x1E, 0x20));
            packet.writeInt(Randomizer.rand(0x28, 0x29));
            packet.writeInt(630);
            packet.writeLong(0); //v196
        }
        packet.write(0);
        return packet.getPacket();
    }

    public static byte[] getChannelChange(MapleClient c, int port) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHANGE_CHANNEL.getValue());
        mplew.write(1);
        mplew.write(Nexon_IP);
        mplew.writeShort(port);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] showDoJangRank(ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DoJang_Rank.getValue());
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow());
        rs.beforeFirst();
        int stat = 0;
        while (rs.next()) {
            stat++;
            mplew.writeShort(stat);
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeLong(rs.getInt("dojo"));
        }

        return mplew.getPacket();
    }

    public static byte[] getPVPType(int type, List<Pair<Integer, String>> players1, int team, boolean enabled, int lvl) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_TYPE.getValue());
        mplew.write(type);
        mplew.write(lvl);
        mplew.write(enabled ? 1 : 0);
        mplew.write(0);
        if (type > 0) {
            mplew.write(team);
            mplew.writeInt(players1.size());
            for (Pair pl : players1) {
                mplew.writeInt(((Integer) pl.left).intValue());
                mplew.writeMapleAsciiString((String) pl.right);
                mplew.writeShort(2660);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] getPVPTransform(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_TRANSFORM.getValue());
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] getPVPDetails(List<Pair<Integer, Integer>> players) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_DETAILS.getValue());
        mplew.write(1);
        mplew.write(0);
        mplew.writeInt(players.size());
        for (Pair pl : players) {
            mplew.writeInt(((Integer) pl.left).intValue());
            mplew.write(((Integer) pl.right).intValue());
        }

        return mplew.getPacket();
    }

    public static byte[] enablePVP(boolean enabled) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_ENABLED.getValue());
        mplew.write(enabled ? 1 : 2);

        return mplew.getPacket();
    }

    public static byte[] getPVPScore(int score, boolean kill) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_SCORE.getValue());
        mplew.writeInt(score);
        mplew.write(kill ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] getPVPResult(List<Pair<Integer, MapleCharacter>> flags, int exp, int winningTeam, int playerTeam) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_RESULT.getValue());
        mplew.writeInt(flags.size());
        for (Pair f : flags) {
            mplew.writeInt(((MapleCharacter) f.right).getId());
            mplew.writeMapleAsciiString(((MapleCharacter) f.right).getName());
            mplew.writeInt(((Integer) f.left).intValue());
            mplew.writeShort(((MapleCharacter) f.right).getTeam() + 1);
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        mplew.writeZeroBytes(24);
        mplew.writeInt(exp);
        mplew.write(0);
        mplew.writeShort(100);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(winningTeam);
        mplew.write(playerTeam);

        return mplew.getPacket();
    }

    public static byte[] getPVPTeam(List<Pair<Integer, String>> players) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_TEAM.getValue());
        mplew.writeInt(players.size());
        for (Pair pl : players) {
            mplew.writeInt(((Integer) pl.left).intValue());
            mplew.writeMapleAsciiString((String) pl.right);
            mplew.writeShort(2660);
        }

        return mplew.getPacket();
    }

    public static byte[] getPVPScoreboard(List<Pair<Integer, MapleCharacter>> flags, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_SCOREBOARD.getValue());
        mplew.writeShort(flags.size());
        for (Pair f : flags) {
            mplew.writeInt(((MapleCharacter) f.right).getId());
            mplew.writeMapleAsciiString(((MapleCharacter) f.right).getName());
            mplew.writeInt(((Integer) f.left).intValue());
            mplew.write(type == 0 ? 0 : ((MapleCharacter) f.right).getTeam() + 1);
        }

        return mplew.getPacket();
    }

    public static byte[] getPVPPoints(int p1, int p2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_POINTS.getValue());
        mplew.writeInt(p1);
        mplew.writeInt(p2);

        return mplew.getPacket();
    }

    public static byte[] getPVPKilled(String lastWords) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_KILLED.getValue());
        mplew.writeMapleAsciiString(lastWords);

        return mplew.getPacket();
    }

    public static byte[] getPVPMode(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_MODE.getValue());
        mplew.write(mode);

        return mplew.getPacket();
    }

    public static byte[] getPVPIceHPBar(int hp, int maxHp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_ICEKNIGHT.getValue());
        mplew.writeInt(hp);
        mplew.writeInt(maxHp);

        return mplew.getPacket();
    }

    public static byte[] getCaptureFlags(MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CAPTURE_FLAGS.getValue());
        mplew.writeRect(map.getArea(0));
        mplew.writeInt(((Point) ((Pair) map.getGuardians().get(0)).left).x);
        mplew.writeInt(((Point) ((Pair) map.getGuardians().get(0)).left).y);
        mplew.writeRect(map.getArea(1));
        mplew.writeInt(((Point) ((Pair) map.getGuardians().get(1)).left).x);
        mplew.writeInt(((Point) ((Pair) map.getGuardians().get(1)).left).y);

        return mplew.getPacket();
    }

    public static byte[] getCapturePosition(MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        Point p1 = map.getPointOfItem(2910000);
        Point p2 = map.getPointOfItem(2910001);
        mplew.writeShort(SendPacketOpcode.CAPTURE_POSITION.getValue());
        mplew.write(p1 == null ? 0 : 1);
        if (p1 != null) {
            mplew.writeInt(p1.x);
            mplew.writeInt(p1.y);
        }
        mplew.write(p2 == null ? 0 : 1);
        if (p2 != null) {
            mplew.writeInt(p2.x);
            mplew.writeInt(p2.y);
        }

        return mplew.getPacket();
    }

    public static byte[] resetCapture() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CAPTURE_RESET.getValue());

        return mplew.getPacket();
    }

    public static byte[] getMacros(SkillMacro[] macros) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SKILL_MACRO.getValue());
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        mplew.write(count);
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                mplew.writeMapleAsciiString(macro.getName());
                mplew.write(macro.getShout());
                mplew.writeInt(macro.getSkill1());
                mplew.writeInt(macro.getSkill2());
                mplew.writeInt(macro.getSkill3());
            }
        }

        return mplew.getPacket();
    }

    public static byte[] innerResetMessage() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.INNER_ABILITY_RESET_MSG.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("26 00 49 6E 6E 65 72 20 50 6F 74 65 6E 74 69 61 6C 20 68 61 73 20 62 65 65 6E 20 72 65 63 6F 6E 66 69 67 75 72 65 64 2E 01"));

        return mplew.getPacket();
    }

    public static byte[] updateInnerPotential() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ENABLE_POTENTIAL_EFF.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01 00 94 1D 2C 04 03 00 00 00 01"));

        return mplew.getPacket();
    }

    public static byte[] getCharInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());
        mplew.writeShort(2);
        mplew.writeLong(1L);
        mplew.writeLong(2L);
        mplew.writeLong(chr.getClient().getChannel() - 1);
        mplew.write(0);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.write(1);
        mplew.writeShort(0);
        chr.CRand().connectData(mplew);
        PacketHelper.addCharacterInfo(mplew, chr);
        mplew.writeInt(0); // acc id
        mplew.writeInt(0); // char id
        mplew.write(HexTool.hex("04 00 00 00 00 00 00 00 75 96 8F 00 00 00 00 00 76 96 8F 00 00 00 00 00 77 96 8F 00 00 00 00 00 78 96 8F 00 00 00 00 00 00 00 00 00"));
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mplew.writeInt(100);
        mplew.writeShort(0);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.WARP_TO_MAP.getValue());

        mplew.writeShort(2);
        mplew.writeLong(1L);
        mplew.writeLong(2L);
        mplew.writeLong(chr.getClient().getChannel() - 1);
        mplew.write(0);
        mplew.write(2);
        mplew.writeLong(0);
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeInt(chr.getStat().getHp());
        mplew.write(0);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mplew.writeInt(100);
        mplew.writeZeroBytes(300);

        return mplew.getPacket();
    }

    public static byte[] spawnFlags(List<Pair<String, Integer>> flags) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOGIN_WELCOME.getValue());
        mplew.write(flags == null ? 0 : flags.size());
        if (flags != null) {
            for (Pair f : flags) {
                mplew.writeMapleAsciiString((String) f.left);
                mplew.write(((Integer) f.right).intValue());
            }
        }

        return mplew.getPacket();
    }

    public static byte[] serverBlocked(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVER_BLOCKED.getValue());
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] pvpBlocked(int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] showEquipEffect() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());

        return mplew.getPacket();
    }

    public static byte[] showEquipEffect(int team) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
        mplew.writeShort(team);

        return mplew.getPacket();
    }

    public static byte[] multiChat(String name, String chattext, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MULTICHAT.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(chattext);

        return mplew.getPacket();
    }

    public static byte[] getFindReplyWithCS(String target, boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(2);
        mplew.writeInt(-1);

        return mplew.getPacket();
    }

    public static byte[] getFindReplyWithMTS(String target, boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(0);
        mplew.writeInt(-1);

        return mplew.getPacket();
    }

    public static byte[] getWhisper(String sender, int channel, String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(18);
        mplew.writeMapleAsciiString(sender);
        mplew.writeShort(channel - 1);
        mplew.writeMapleAsciiString(text);

        return mplew.getPacket();
    }
    
        public static byte[] showPotentialReset(int chr, boolean success, int itemid) {
         MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

         mplew.writeShort(SendPacketOpcode.SHOW_POTENTIAL_RESET.getValue());
         mplew.writeInt(chr);
         mplew.write(success ? 1 : 0);
         mplew.writeInt(itemid);

         return mplew.getPacket();
    }

    public static byte[] getWhisperReply(String target, byte reply) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(10);
        mplew.writeMapleAsciiString(target);
        mplew.write(reply);

        return mplew.getPacket();
    }

    public static byte[] getFindReplyWithMap(String target, int mapid, boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(1);
        mplew.writeInt(mapid);
        mplew.writeZeroBytes(8);

        return mplew.getPacket();
    }

    public static byte[] getFindReply(String target, int channel, boolean buddy) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(3);
        mplew.writeInt(channel - 1);

        return mplew.getPacket();
    }

    public static final byte[] MapEff(String path) {
        return environmentChange(path, 4);
    }

    public static final byte[] MapNameDisplay(int mapid) {
        return environmentChange("maplemap/enter/" + mapid, 4);
    }

    public static final byte[] Aran_Start() {
        return environmentChange("Aran/balloon", 4);
    }

    public static byte[] musicChange(String song) {
        return environmentChange(song, 7);
    }

    public static byte[] showEffect(String effect) {
        return environmentChange(effect, 4);
    }

    public static byte[] playSound(String sound) {
        return environmentChange(sound, 5);
    }

    public static byte[] environmentChange(String env, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
     //   mplew.write(0);
        mplew.write(mode);
        mplew.writeMapleAsciiString(env);

        return mplew.getPacket();
    }

    public static byte[] trembleEffect(int type, int delay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOSS_ENV.getValue());
    //    mplew.writeShort(1);
   //     mplew.writeShort(1);
   //     mplew.writeShort(0);
        mplew.write(1);
        mplew.write(type);
        mplew.writeInt(delay);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] environmentMove(String env, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_ENV.getValue());
        mplew.writeMapleAsciiString(env);
        mplew.writeInt(mode);

        return mplew.getPacket();
    }

    public static byte[] getUpdateEnvironment(MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_ENV.getValue());
        mplew.writeInt(map.getEnvironment().size());
        for (Map.Entry mp : map.getEnvironment().entrySet()) {
            mplew.writeMapleAsciiString((String) mp.getKey());
            mplew.writeInt(((Integer) mp.getValue()).intValue());
        }

        return mplew.getPacket();
    }

    public static byte[] startMapEffect(String msg, int itemid, boolean active) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MAP_EFFECT.getValue());
        mplew.write(active ? 0 : 1);

        mplew.writeInt(itemid);
        if (active) {
            mplew.writeMapleAsciiString(msg);
        }
        return mplew.getPacket();
    }

    public static byte[] removeMapEffect() {
        return startMapEffect(null, 0, false);
    }

    public static byte[] GameMaster_Func(int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GM_EFFECT.getValue());
        mplew.write(value);
        mplew.writeZeroBytes(17);

        return mplew.getPacket();
    }

    public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OX_QUIZ.getValue());
        mplew.write(askQuestion ? 1 : 0);
        mplew.write(questionSet);
        mplew.writeShort(questionId);

        return mplew.getPacket();
    }

    public static byte[] showEventInstructions() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GMEVENT_INSTRUCTIONS.getValue());
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] getPVPClock(int type, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(3);
        mplew.write(type);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] getClock(int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(2);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] getClockTime(int hour, int min, int sec) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CLOCK.getValue());
        mplew.write(1);
        mplew.write(hour);
        mplew.write(min);
        mplew.write(sec);

        return mplew.getPacket();
    }

    public static byte[] boatPacket(int effect, int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOAT_MOVE.getValue());
        mplew.write(effect);
        mplew.write(mode);

        return mplew.getPacket();
    }

    public static byte[] setBoatState(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOAT_STATE.getValue());
        mplew.write(effect);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] stopClock() {
        return getPacketFromHexString(Integer.toHexString(SendPacketOpcode.STOP_CLOCK.getValue()) + " 00");
    }

    public static byte[] showAriantScoreBoard() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ARIANT_SCOREBOARD.getValue());

        return mplew.getPacket();
    }

    public static byte[] sendPyramidUpdate(int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PYRAMID_UPDATE.getValue());
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] sendPyramidResult(byte rank, int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PYRAMID_RESULT.getValue());
        mplew.write(rank);
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] quickSlot(String skil) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.QUICK_SLOT.getValue());
        mplew.write(skil == null ? 0 : 1);
        if (skil != null) {
            String[] slots = skil.split(",");
            for (int i = 0; i < 8; i++) {
                mplew.writeInt(Integer.parseInt(slots[i]));
            }
        }

        return mplew.getPacket();
    }

    public static byte[] getInventoryStatus() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
        mplew.writeShort(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] getMovingPlatforms(MapleMap map) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_PLATFORM.getValue());
        mplew.writeInt(map.getPlatforms().size());
        for (MapleNodes.MaplePlatform mp : map.getPlatforms()) {
            mplew.writeMapleAsciiString(mp.name);
            mplew.writeInt(mp.start);
            mplew.writeInt(mp.SN.size());
            for (int x = 0; x < mp.SN.size(); x++) {
                mplew.writeInt(((Integer) mp.SN.get(x)).intValue());
            }
            mplew.writeInt(mp.speed);
            mplew.writeInt(mp.x1);
            mplew.writeInt(mp.x2);
            mplew.writeInt(mp.y1);
            mplew.writeInt(mp.y2);
            mplew.writeInt(mp.x1);
            mplew.writeInt(mp.y1);
            mplew.writeShort(mp.r);
        }

        return mplew.getPacket();
    }

    public static byte[] sendPyramidKills(int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PYRAMID_KILL_COUNT.getValue());
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] sendPVPMaps() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_INFO.getValue());
        mplew.write(3);
        for (int i = 0; i < 20; i++) {
            mplew.writeInt(10);
        }
        mplew.writeZeroBytes(124);
        mplew.writeShort(150);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] gainForce(int oid, int count, int color) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
        mplew.write(1); // 0 = remote user?
        mplew.writeInt(oid);
        byte newcheck = 0;
        mplew.writeInt(newcheck); //unk
        if (newcheck > 0) {
            mplew.writeInt(0); //unk
            mplew.writeInt(0); //unk
        }
        mplew.write(0);
        mplew.writeInt(4); // size, for each below
        mplew.writeInt(count); //count
        mplew.writeInt(color); //color, 1-10 for demon, 1-2 for phantom
        mplew.writeInt(0); //unk
        mplew.writeInt(0); //unk
        return mplew.getPacket();
    }

    public static byte[] achievementRatio(int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ACHIEVEMENT_RATIO.getValue());
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] getPublicNPCInfo() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PUBLIC_NPC.getValue());
        mplew.write(HexTool.hex("09 00 00 00 00 66 7B 89 00 02 00 00 00 0A 00 00 00 43 00 55 73 65 20 74 68 65 20 23 63 44 69 6D 65 6E 73 69 6F 6E 61 6C 20 4D 69 72 72 6F 72 23 20 74 6F 20 6D 6F 76 65 20 74 6F 20 61 20 76 61 72 69 65 74 79 20 6F 66 20 70 61 72 74 79 20 71 75 65 73 74 73 2E 00 40 E0 FD 3B 37 4F 01 00 80 05 BB 46 E6 17 02 00 00 00 00 9B 69 8A 00 01 00 00 00 14 00 00 00 C3 00 54 61 6B 65 73 20 79 6F 75 20 74 6F 20 23 63 3C 4D 6F 6E 73 74 65 72 20 50 61 72 6B 3E 23 2C 20 61 20 70 61 72 74 79 2D 70 6C 61 79 20 7A 6F 6E 65 20 77 68 65 72 65 20 79 6F 75 20 63 61 6E 20 74 65 61 6D 20 75 70 20 74 6F 20 64 65 66 65 61 74 5C 6E 70 6F 77 65 72 66 75 6C 20 6D 6F 6E 73 74 65 72 73 2E 5C 6E 23 63 4E 6F 72 6D 61 6C 20 4D 6F 6E 73 74 65 72 20 50 61 72 6B 3A 20 4D 75 73 74 20 62 65 20 4C 76 2E 20 36 30 20 2D 20 39 35 20 6F 72 20 4C 76 2E 20 31 33 35 2B 20 74 6F 20 70 61 72 74 69 63 69 70 61 74 65 5C 6E 45 78 74 72 65 6D 65 3A 20 4C 76 2E 20 34 30 20 2D 20 31 30 30 00 40 E0 FD 3B 37 4F 01 00 80 05 BB 46 E6 17 02 00 00 00 00 96 54 89 00 05 00 00 00 00 00 00 00 49 00 4D 6F 76 65 20 74 6F 20 74 68 65 20 63 6C 6F 73 65 73 74 20 23 63 49 6E 74 65 72 63 6F 6E 74 69 6E 65 6E 74 61 6C 20 53 74 61 74 69 6F 6E 23 20 74 6F 20 79 6F 75 72 20 63 75 72 72 65 6E 74 20 6C 6F 63 61 74 69 6F 6E 2E 00 40 E0 FD 3B 37 4F 01 00 80 05 BB 46 E6 17 02 00 00 00 00 97 54 89 00 03 00 00 00 00 00 00 00 47 00 4D 6F 76 65 20 74 6F 20 74 68 65 20 23 63 46 72 65 65 20 4D 61 72 6B 65 74 23 2C 20 77 68 65 72 65 20 79 6F 75 20 63 61 6E 20 74 72 61 64 65 20 69 74 65 6D 73 20 77 69 74 68 20 6F 74 68 65 72 20 75 73 65 72 73 2E 00 40 E0 FD 3B 37 4F 01 00 80 05 BB 46 E6 17 02 00 00 00 00 98 54 89 00 04 00 00 00 1E 00 00 00 5E 00 4D 6F 76 65 20 74 6F 20 23 63 41 72 64 65 6E 74 6D 69 6C 6C 23 2C 20 74 68 65 20 74 6F 77 6E 20 6F 66 20 50 72 6F 66 65 73 73 69 6F 6E 73 2E 5C 6E 23 63 4F 6E 6C 79 20 4C 76 2E 20 33 30 20 6F 72 20 61 62 6F 76 65 20 63 61 6E 20 6D 6F 76 65 20 74 6F 20 41 72 64 65 6E 74 6D 69 6C 6C 00 40 E0 FD 3B 37 4F 01 00 80 05 BB 46 E6 17 02 00 00 00 00 99 54 89 00 06 00 00 00 00 00 00 00 30 00 54 61 6B 65 20 74 68 65 20 23 63 54 61 78 69 23 20 74 6F 20 6D 6F 76 65 20 74 6F 20 6D 61 6A 6F 72 20 61 72 65 61 73 20 71 75 69 63 6B 6C 79 2E 00 40 E0 FD 3B 37 4F 01 00 80 05 BB 46 E6 17 02 00 00 00 00 79 7B 89 00 0A 00 00 00 1E 00 00 00 1D 00 52 65 63 65 69 76 65 20 50 61 72 74 2D 54 69 6D 65 20 4A 6F 62 20 72 65 77 61 72 64 2E 00 40 E0 FD 3B 37 4F 01 00 80 05 BB 46 E6 17 02 00 00 00 00 BB 54 89 00 0D 00 00 00 01 00 00 00 30 00 59 6F 75 20 63 61 6E 20 67 65 74 20 61 20 73 74 79 6C 69 73 68 20 68 61 69 72 63 75 74 20 66 72 6F 6D 20 42 69 67 20 48 65 61 64 77 61 72 64 2E 00 40 E0 FD 3B 37 4F 01 00 80 05 BB 46 E6 17 02 00 00 00 00 64 66 8C 00 0E 00 00 00 01 00 00 00 2E 00 59 6F 75 20 63 61 6E 20 67 65 74 20 70 6C 61 73 74 69 63 20 73 75 72 67 65 72 79 20 66 72 6F 6D 20 4E 75 72 73 65 20 50 72 65 74 74 79 2E 00 40 E0 FD 3B 37 4F 01 00 80 05 BB 46 E6 17 02"));
//        mplew.write(GameConstants.publicNpcIds.length); /// OK
//           // 
//       for (int i = 0; i < GameConstants.publicNpcIds.length; i++) {
//             mplew.writeInt(0);
//            mplew.writeInt(GameConstants.publicNpcIds[i]);
//            mplew.writeInt(i);
//            mplew.writeInt(i);
//            mplew.writeMapleAsciiString(GameConstants.publicNpcs[i]);   
//            mplew.write(HexTool.hex("00 40 E0 FD 3B 37 4F 01 00 80 05 BB 46 E6 17 02"));
//       }

        return mplew.getPacket();
    }

    public static byte[] spawnPlayerMapobject(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_PLAYER.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeMapleAsciiString(chr.getName());
        //  mplew.write(m);
        MapleQuestStatus ultExplorer = chr.getQuestNoAdd(MapleQuest.getInstance(111111));
        if ((ultExplorer != null) && (ultExplorer.getCustomData() != null)) {
            mplew.writeMapleAsciiString(ultExplorer.getCustomData());
        } else {
            mplew.writeMapleAsciiString("");
        }
        if (chr.getGuildId() <= 0) {
            mplew.writeZeroBytes(8);
        } else {
            MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
                mplew.writeShort(gs.getLogoBG());
                mplew.write(gs.getLogoBGColor());
                mplew.writeShort(gs.getLogo());
                mplew.write(gs.getLogoColor());
            } else {
                mplew.writeZeroBytes(8);
            }
        }
        mplew.write(1);

        mplew.writeShort(0);
        mplew.write(0);
        mplew.write(-2);
//        packet.writeInt(0); // 1.2.192 new
        mplew.write(0); // 1.2.192 new
        mplew.write(2);
        mplew.writeLong(0);
        mplew.writeInt(0); // 1.2.192 new
        mplew.writeShort(0);
        mplew.write(HexTool.getByteArrayFromHexString("00 80 02 00 00 00 00 00"));
        mplew.writeLong(0);
//        packet.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 00"));
        mplew.writeLong(0);
        mplew.writeInt(0);

////mplew.write(HexTool.hex("F2 03 10 B9 0B 01 01 00 00 00 FE 00 02 00 00 00 00 00 00 00 00 00 00 00 0A 00 00 00 80 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
//        final List<Pair<Integer, Integer>> buffvalue = new ArrayList<>();
//        final List<Pair<Integer, Integer>> buffvaluenew = new ArrayList<>();
//        int[] mask = new int[12];
//        mask[0] |= -33554432;
//        mask[4] |= 163840;
//        mask[7] |= 4096;
//        if ((chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null) || (chr.isHidden())) {
//            mask[MapleBuffStat.DARKSIGHT.getPosition(true)] |= MapleBuffStat.DARKSIGHT.getValue();
//        }
//        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
//            mask[MapleBuffStat.SOULARROW.getPosition(true)] |= MapleBuffStat.SOULARROW.getValue();
//        }
//
//        if ((chr.getBuffedValue(MapleBuffStat.COMBO) != null)) {
//            mask[MapleBuffStat.COMBO.getPosition(true)] |= MapleBuffStat.COMBO.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue()), Integer.valueOf(1)));
//        }
//        if (chr.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) {
//            mask[MapleBuffStat.WK_CHARGE.getPosition(true)] |= MapleBuffStat.WK_CHARGE.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.WK_CHARGE).intValue()), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffSource(MapleBuffStat.WK_CHARGE)), Integer.valueOf(3)));
//        }
//        if ((chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null)) {
//            mask[MapleBuffStat.SHADOWPARTNER.getPosition(true)] |= MapleBuffStat.SHADOWPARTNER.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER).intValue()), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffSource(MapleBuffStat.SHADOWPARTNER)), Integer.valueOf(3)));
//        }
//
//        if ((chr.getBuffedValue(MapleBuffStat.MORPH) != null)) {
//            mask[MapleBuffStat.MORPH.getPosition(true)] |= MapleBuffStat.MORPH.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getStatForBuff(MapleBuffStat.MORPH).getMorph(chr)), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffSource(MapleBuffStat.MORPH)), Integer.valueOf(3)));
//        }
//        if (chr.getBuffedValue(MapleBuffStat.BERSERK_FURY) != null) {
//            mask[MapleBuffStat.BERSERK_FURY.getPosition(true)] |= MapleBuffStat.BERSERK_FURY.getValue();
//        }
//        if (chr.getBuffedValue(MapleBuffStat.DIVINE_BODY) != null) {
//            mask[MapleBuffStat.DIVINE_BODY.getPosition(true)] |= MapleBuffStat.DIVINE_BODY.getValue();
//        }
//
//        if (chr.getBuffedValue(MapleBuffStat.WIND_WALK) != null) {
//            mask[MapleBuffStat.WIND_WALK.getPosition(true)] |= MapleBuffStat.WIND_WALK.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.WIND_WALK).intValue()), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.WIND_WALK)), Integer.valueOf(3)));
//        }
//        if (chr.getBuffedValue(MapleBuffStat.PYRAMID_PQ) != null) {
//            mask[MapleBuffStat.PYRAMID_PQ.getPosition(true)] |= MapleBuffStat.PYRAMID_PQ.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.PYRAMID_PQ).intValue()), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.PYRAMID_PQ)), Integer.valueOf(3)));
//        }
//
//        if (chr.getBuffedValue(MapleBuffStat.OWL_SPIRIT) != null) {
//            mask[MapleBuffStat.OWL_SPIRIT.getPosition(true)] |= MapleBuffStat.OWL_SPIRIT.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.OWL_SPIRIT).intValue()), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.OWL_SPIRIT)), Integer.valueOf(3)));
//        }
//        if (chr.getBuffedValue(MapleBuffStat.FINAL_CUT) != null) {
//            mask[MapleBuffStat.FINAL_CUT.getPosition(true)] |= MapleBuffStat.FINAL_CUT.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.FINAL_CUT).intValue()), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.FINAL_CUT)), Integer.valueOf(3)));
//        }
//
//        if (chr.getBuffedValue(MapleBuffStat.TORNADO) != null) {
//            mask[MapleBuffStat.TORNADO.getPosition(true)] |= MapleBuffStat.TORNADO.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.TORNADO).intValue()), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.TORNADO)), Integer.valueOf(3)));
//        }
//        if (chr.getBuffedValue(MapleBuffStat.INFILTRATE) != null) {
//            mask[MapleBuffStat.INFILTRATE.getPosition(true)] |= MapleBuffStat.INFILTRATE.getValue();
//        }
//        if (chr.getBuffedValue(MapleBuffStat.MECH_CHANGE) != null) {
//            mask[MapleBuffStat.MECH_CHANGE.getPosition(true)] |= MapleBuffStat.MECH_CHANGE.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MECH_CHANGE).intValue()), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.MECH_CHANGE)), Integer.valueOf(3)));
//        }
//        if (chr.getBuffedValue(MapleBuffStat.DARK_AURA) != null) {
//            mask[MapleBuffStat.DARK_AURA.getPosition(true)] |= MapleBuffStat.DARK_AURA.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.DARK_AURA).intValue()), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.DARK_AURA)), Integer.valueOf(3)));
//        }
//        if (chr.getBuffedValue(MapleBuffStat.BLUE_AURA) != null) {
//            mask[MapleBuffStat.BLUE_AURA.getPosition(true)] |= MapleBuffStat.BLUE_AURA.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.BLUE_AURA).intValue()), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.BLUE_AURA)), Integer.valueOf(3)));
//        }
//        if (chr.getBuffedValue(MapleBuffStat.YELLOW_AURA) != null) {
//            mask[MapleBuffStat.YELLOW_AURA.getPosition(true)] |= MapleBuffStat.YELLOW_AURA.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.YELLOW_AURA).intValue()), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.YELLOW_AURA)), Integer.valueOf(3)));
//        }
//        if ((chr.getBuffedValue(MapleBuffStat.WATER_SHIELD) != null)) {
//            mask[MapleBuffStat.WATER_SHIELD.getPosition(true)] |= MapleBuffStat.WATER_SHIELD.getValue();
//            buffvaluenew.add(new Pair(Integer.valueOf(chr.getTotalSkillLevel(chr.getTrueBuffSource(MapleBuffStat.WATER_SHIELD))), Integer.valueOf(2)));
//            buffvaluenew.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.WATER_SHIELD)), Integer.valueOf(4)));
//            buffvaluenew.add(new Pair(Integer.valueOf(9), Integer.valueOf(0)));
//        }
//        for (int i = 0; i < mask.length; i++) {
//            mplew.writeInt(mask[i]);
//        }
//      
//        for (Pair<Integer, Integer> i : buffvalue) {
//            if (i.right == 3) {
//                mplew.writeInt(i.left.intValue());
//            } else if (i.right == 2) {
//                mplew.writeShort(i.left.shortValue());
//            } else if (i.right == 1) {
//                mplew.write(i.left.byteValue());
//            }
//        }
//        mplew.writeInt(0);
        mplew.writeInt(-1);
        //mplew.write(HexTool.hex("00 03 00 00 00 01 00 18 CA 31 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"));
//        if (buffvaluenew.isEmpty()) {
//            mplew.writeZeroBytes(10);
//        } else {
//            mplew.write(0);
//            for (Pair i : buffvaluenew) {
//                if (((Integer) i.right).intValue() == 4) {
//                    mplew.writeInt(((Integer) i.left).intValue());
//                } else if (((Integer) i.right).intValue() == 2) {
//                    mplew.writeShort(((Integer) i.left).shortValue());
//                } else if (((Integer) i.right).intValue() == 1) {
//                    mplew.write(((Integer) i.left).byteValue());
//                } else if (((Integer) i.right).intValue() == 0) {
//                    mplew.writeZeroBytes(((Integer) i.left).intValue());
//                }
//            }
//        }
//                mplew.writeLong(0);
//        mplew.writeLong(0);
//        mplew.writeLong(0);
//        mplew.writeInt(0);

        mplew.writeZeroBytes(30);

        int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeZeroBytes(10);
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeZeroBytes(10);
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0);
        int buffSrc = chr.getBuffSource(MapleBuffStat.MONSTER_RIDING);
        if (buffSrc > 0) {
            Item c_mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -118);
            Item mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18);
            if ((GameConstants.getMountItem(buffSrc, chr) == 0) && (c_mount != null) && (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -119) != null)) {
                mplew.writeInt(c_mount.getItemId());
            } else if ((GameConstants.getMountItem(buffSrc, chr) == 0) && (mount != null) && (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -19) != null)) {
                mplew.writeInt(mount.getItemId());
            } else {
                mplew.writeInt(GameConstants.getMountItem(buffSrc, chr));
            }
            mplew.writeInt(buffSrc);
        } else {
            mplew.writeLong(0L);
        }
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeZeroBytes(15);
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeZeroBytes(16);
        mplew.write(1);
        mplew.writeInt(CHAR_MAGIC_SPAWN);

        mplew.writeShort(0);
        mplew.writeInt(chr.getJob());
        PacketHelper.addCharLook(mplew, chr, true);
        mplew.writeInt(0);
        mplew.writeInt(0);

        mplew.writeInt(Math.min(250, chr.getInventory(MapleInventoryType.CASH).countById(5110000))); //max is like 100. but w/e
        mplew.writeInt(0);
        mplew.writeInt(0);

        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);

        final MapleQuestStatus stat = chr.getQuestNoAdd(MapleQuest.getInstance(GameConstants.ITEM_TITLE));
        mplew.writeInt(stat != null && stat.getCustomData() != null ? Integer.valueOf(stat.getCustomData()) : 0);
        mplew.writeInt(0);
        mplew.writeInt(chr.getItemEffect());
        mplew.writeInt(GameConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP ? chr.getChair() : 0);
        mplew.writeInt(0);

        mplew.writePos(chr.getTruePosition());
        mplew.write(chr.getStance());
        mplew.writeShort(chr.getFH());
        mplew.write(0); //new v140
        mplew.write(0);
        //  mplew.write(0);
        mplew.write(0);
        mplew.writeShort(1);

        mplew.writeInt(chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt(chr.getMount().getFatigue());

        PacketHelper.addAnnounceBox(mplew, chr);
        mplew.write((chr.getChalkboard() != null) && (chr.getChalkboard().length() > 0) ? 1 : 0);
        if ((chr.getChalkboard() != null) && (chr.getChalkboard().length() > 0)) {
            mplew.writeMapleAsciiString(chr.getChalkboard());
        }
        Triple rings = chr.getRings(false);
        addRingInfo(mplew, (List) rings.getLeft());
        addRingInfo(mplew, (List) rings.getMid());
        addMRingInfo(mplew, (List) rings.getRight(), chr);

        mplew.write(chr.getStat().Berserk ? 1 : 0);
        mplew.writeInt(0);
        if ((chr.getJob() >= 6000) && (chr.getJob() <= 6512)) {
            mplew.writeZeroBytes(9);
        }
        mplew.writeMapleAsciiString(chr.getClient().getAccountName());
        mplew.write(HexTool.hex("B2 12 00 00 03 00 00 00 3C 00 00 00 30 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 00"));

//        mplew.writeInt(0);
//        mplew.writeInt(0);
//        mplew.writeInt(0);
//        mplew.writeInt(0);
//        mplew.writeInt(0);
//        mplew.writeInt(0);
//        mplew.writeShort(0);
//        mplew.write(0);

        mplew.writeInt(1);
        mplew.write(-1);
        mplew.writeInt(-1);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeZeroBytes(100);
        return mplew.getPacket();
    }

    public static byte[] removePlayerFromMap(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        mplew.writeInt(cid);

        return mplew.getPacket();
    }

    public static byte[] getChatText(int cidfrom, String text, boolean whiteBG, int show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHATTEXT.getValue());
        mplew.writeInt(cidfrom);
        mplew.write(whiteBG ? 1 : 0);
        mplew.writeMapleAsciiString(text);
        mplew.write(show);

        return mplew.getPacket();
    }

    public static byte[] getScrollEffect(int chr, int useid, int equipid, Equip.ScrollResult scrollSuccess, boolean legendarySpirit, boolean whiteScroll) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
        mplew.writeInt(chr);
        mplew.write(scrollSuccess == Equip.ScrollResult.CURSE ? 2 : scrollSuccess == Equip.ScrollResult.SUCCESS ? 1 : 0);
        mplew.write(legendarySpirit ? 1 : 0);
        mplew.writeInt(useid);
        mplew.writeInt(equipid);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] showMagnifyingEffect(int chr, short pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_MAGNIFYING_EFFECT.getValue());
        mplew.writeInt(chr);
        mplew.writeShort(pos);

        return mplew.getPacket();
    }

    public static byte[] showPotentialReset(boolean fireworks, int chr, boolean success, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(fireworks ? SendPacketOpcode.SHOW_FIREWORKS_EFFECT.getValue() : SendPacketOpcode.SHOW_POTENTIAL_RESET.getValue());
        mplew.writeInt(chr);
        mplew.write(success ? 1 : 0);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] showNebuliteEffect(int chr, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_NEBULITE_EFFECT.getValue());
        mplew.writeInt(chr);
        mplew.write(success ? 1 : 0);
        mplew.writeMapleAsciiString(success ? "Successfully mounted Nebulite." : "Failed to mount Nebulite.");

        return mplew.getPacket();
    }

    public static byte[] useNebuliteFusion(int cid, int itemId, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_FUSION_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(success ? 1 : 0);
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] pvpAttack(int cid, int playerLevel, int skill, int skillLevel, int speed, int mastery, int projectile, int attackCount, int chargeTime, int stance, int direction, int range, int linkSkill, int linkSkillLevel, boolean movementSkill, boolean pushTarget, boolean pullTarget, List<AttackPair> attack) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.write(playerLevel);
        mplew.writeInt(skill);
        mplew.write(skillLevel);
        mplew.writeInt(linkSkill != skill ? linkSkill : 0);
        mplew.write(linkSkillLevel != skillLevel ? linkSkillLevel : 0);
        mplew.write(direction);
        mplew.write(movementSkill ? 1 : 0);
        mplew.write(pushTarget ? 1 : 0);
        mplew.write(pullTarget ? 1 : 0);
        mplew.write(0);
        mplew.writeShort(stance);
        mplew.write(speed);
        mplew.write(mastery);
        mplew.writeInt(projectile);
        mplew.writeInt(chargeTime);
        mplew.writeInt(range);
        mplew.write(attack.size());
        mplew.write(0);
        mplew.writeInt(0);
        mplew.write(attackCount);
        mplew.write(0);
        for (AttackPair p : attack) {
            mplew.writeInt(p.objectid);
            mplew.writeInt(0);
            mplew.writePos(p.point);
            mplew.write(0);
            mplew.writeInt(0);
            for (Pair atk : p.attack) {
                mplew.writeInt(((Integer) atk.left).intValue());
                mplew.writeInt(0);
                mplew.write(((Boolean) atk.right).booleanValue() ? 1 : 0);
                mplew.writeShort(0);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] getPVPMist(int cid, int mistSkill, int mistLevel, int damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_MIST.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(mistSkill);
        mplew.write(mistLevel);
        mplew.writeInt(damage);
        mplew.write(8);
        mplew.writeInt(1000);

        return mplew.getPacket();
    }

    public static byte[] pvpCool(int cid, List<Integer> attack) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_COOL.getValue());
        mplew.writeInt(cid);
        mplew.write(attack.size());
        for (Iterator i$ = attack.iterator(); i$.hasNext();) {
            int b = ((Integer) i$.next()).intValue();
            mplew.writeInt(b);
        }

        return mplew.getPacket();
    }

    public static byte[] teslaTriangle(int cid, int sum1, int sum2, int sum3) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TESLA_TRIANGLE.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(sum1);
        mplew.writeInt(sum2);
        mplew.writeInt(sum3);

        return mplew.getPacket();
    }

    public static byte[] followEffect(int initiator, int replier, Point toMap) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FOLLOW_EFFECT.getValue());
        mplew.writeInt(initiator);
        mplew.writeInt(replier);
        mplew.writeLong(0);
        if (replier == 0) {
            mplew.write(toMap == null ? 0 : 1);
            if (toMap != null) {
                mplew.writeInt(toMap.x);
                mplew.writeInt(toMap.y);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] showPQReward(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_PQ_REWARD.getValue());
        mplew.writeInt(cid);
        for (int i = 0; i < 6; i++) {
            mplew.write(0);
        }

        return mplew.getPacket();
    }

    public static byte[] craftMake(int cid, int something, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CRAFT_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(something);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] craftFinished(int cid, int craftID, int ranking, int itemId, int quantity, int exp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CRAFT_COMPLETE.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(craftID);
        mplew.writeInt(ranking);
        mplew.writeInt(itemId);
        mplew.writeInt(quantity);
        mplew.writeInt(exp);

        return mplew.getPacket();
    }

    public static byte[] harvestResult(int cid, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.HARVESTED.getValue());
        mplew.writeInt(cid);
        mplew.write(success ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] playerDamaged(int cid, int dmg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_DAMAGED.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(dmg);

        return mplew.getPacket();
    }

    public static byte[] showPyramidEffect(int chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.NETT_PYRAMID.getValue());
        mplew.writeInt(chr);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] pamsSongEffect(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PAMS_SONG.getValue());
        mplew.writeInt(cid);

        return mplew.getPacket();
    }

    public static byte[] spawnHaku_change0(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.Haku_CHANGE_0.getValue());
        mplew.writeInt(cid);

        return mplew.getPacket();
    }

    public static byte[] spawnHaku_change1(MapleHaku d) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.Haku_CHANGE_1.getValue());
        mplew.writeInt(d.getOwner());
        mplew.writePos(d.getPosition());
        mplew.writeShort(d.getStance());
        mplew.writeZeroBytes(6);

        return mplew.getPacket();
    }

    public static byte[] spawnHaku_bianshen(int cid, int oid, boolean change) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.Haku_CHANGE.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.write(change ? 2 : 1);

        return mplew.getPacket();
    }

    public static byte[] spawnHaku(MapleHaku d) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_HAKU.getValue());
        mplew.writeInt(d.getOwner());
        mplew.writeInt(d.getObjectId());
        mplew.writeInt(40020109);
        mplew.write(1);
        mplew.writePos(d.getPosition());
        mplew.write(0);
        mplew.writeShort(d.getStance());

        return mplew.getPacket();
    }

    public static byte[] moveHaku(int cid, int oid, Point pos, List<LifeMovementFragment> res) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.Haku_MOVE.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.writeInt(0);
        mplew.writePos(pos);
        mplew.writeInt(0);
        PacketHelper.serializeMovementList(mplew, res);
        return mplew.getPacket();
    }

    public static byte[] spawnDragon(MapleDragon d) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DRAGON_SPAWN.getValue());
        mplew.writeInt(d.getOwner());
        mplew.writeInt(d.getPosition().x);
        mplew.writeInt(d.getPosition().y);
        mplew.write(d.getStance());
        mplew.writeShort(0);
        mplew.writeShort(d.getJobId());

        return mplew.getPacket();
    }

    public static byte[] removeDragon(int chrid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DRAGON_REMOVE.getValue());
        mplew.writeInt(chrid);

        return mplew.getPacket();
    }

    public static byte[] moveDragon(MapleDragon d, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DRAGON_MOVE.getValue());
        mplew.writeInt(d.getOwner());
        mplew.writeInt(0);
        mplew.writePos(startPos);
        mplew.writeInt(0);
        PacketHelper.serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    public static byte[] spawnAndroid(MapleCharacter cid, MapleAndroid android) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ANDROID_SPAWN.getValue());
        mplew.writeInt(cid.getId());
        mplew.write(android.getItemId() == 1662006 ? 5 : android.getItemId() - 1661999);
        mplew.writePos(android.getPos());
        mplew.write(android.getStance());
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(android.getHair() - 30000);
        mplew.writeShort(android.getFace() - 20000);
        mplew.writeMapleAsciiString(android.getName());
        for (short i = -1200; i > -1207; i = (short) (i - 1)) {
            Item item = cid.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
            mplew.writeInt(item != null ? item.getItemId() : 0);
        }

        return mplew.getPacket();
    }

    public static byte[] moveAndroid(int cid, Point pos, List<LifeMovementFragment> res) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.ANDROID_MOVE.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(0);
        mplew.writePos(pos);
        mplew.writeInt(2147483647);
        PacketHelper.serializeMovementList(mplew, res);
        return mplew.getPacket();
    }

    public static byte[] showAndroidEmotion(int cid, int animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ANDROID_EMOTION.getValue());
        mplew.writeInt(cid);
        mplew.write(0);
        mplew.write(animation);

        return mplew.getPacket();
    }

    public static byte[] updateAndroidLook(boolean itemOnly, MapleCharacter cid, MapleAndroid android) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ANDROID_UPDATE.getValue());
        mplew.writeInt(cid.getId());
        mplew.write(itemOnly ? 1 : 0);
        if (itemOnly) {
            for (short i = -1200; i > -1207; i = (short) (i - 1)) {
                Item item = cid.getInventory(MapleInventoryType.EQUIPPED).getItem(i);
                mplew.writeInt(item != null ? item.getItemId() : 0);
            }
        } else {
            mplew.writeShort(0);
            mplew.writeShort(android.getHair() - 30000);
            mplew.writeShort(android.getFace() - 20000);
            mplew.writeMapleAsciiString(android.getName());
        }

        return mplew.getPacket();
    }

    public static byte[] deactivateAndroid(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ANDROID_DEACTIVATED.getValue());
        mplew.writeInt(cid);

        return mplew.getPacket();
    }

    public static byte[] removeFamiliar(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_FAMILIAR.getValue());
        mplew.writeInt(cid);
        mplew.writeShort(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] spawnFamiliar(MonsterFamiliar mf, boolean spawn, boolean respawn) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(respawn ? SendPacketOpcode.RESPAWN_FAMILIAR.getValue() : SendPacketOpcode.SPAWN_FAMILIAR.getValue());
        mplew.writeInt(mf.getCharacterId());
        mplew.write(spawn ? 1 : 0);
        mplew.write(respawn ? 1 : 0);
        mplew.write(0);
        if (spawn) {
            mplew.writeInt(mf.getFamiliar());
            mplew.writeInt(mf.getFatigue());
            mplew.writeInt(mf.getVitality() * 300);
            mplew.writeMapleAsciiString(mf.getName());
            mplew.writePos(mf.getTruePosition());
            mplew.write(mf.getStance());
            mplew.writeShort(mf.getFh());
        }

        return mplew.getPacket();
    }

    public static byte[] addStolenSkill(int jobNum, int index, int skill, int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_STOLEN_SKILLS.getValue());
        mplew.write(1);
        mplew.write(0);
        mplew.writeInt(jobNum);
        mplew.writeInt(index);
        mplew.writeInt(skill);
        mplew.writeInt(level);
        mplew.writeInt(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] removeStolenSkill(int jobNum, int index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_STOLEN_SKILLS.getValue());
        mplew.write(1);
        mplew.write(3);
        mplew.writeInt(jobNum);
        mplew.writeInt(index);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] replaceStolenSkill(int base, int skill) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REPLACE_SKILLS.getValue());
        mplew.write(1);
        mplew.write(skill > 0 ? 1 : 0);
        mplew.writeInt(base);
        mplew.writeInt(skill);

        return mplew.getPacket();
    }

    public static byte[] gainCardStack(int oid, int runningId, int color, int skillid, int damage, int times) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
        mplew.write(0);
        mplew.writeInt(oid);
        mplew.writeInt(1);
        mplew.writeInt(damage);
        mplew.writeInt(skillid);
        for (int i = 0; i < times; i++) {
            mplew.write(1);
            mplew.writeInt(damage == 0 ? runningId + i : runningId);
            mplew.writeInt(color);
            mplew.writeInt(Randomizer.rand(15, 29));
            mplew.writeInt(Randomizer.rand(7, 11));
            mplew.writeInt(Randomizer.rand(0, 9));
        }
        mplew.write(0);
        mplew.writeZeroBytes(50);
        return mplew.getPacket();
    }

    public static byte[] getCarteAnimation(int cid, int oid, int job, int total, int numDisplay) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
        mplew.write(0);
        mplew.writeInt(cid);
        mplew.writeInt(1);

        mplew.writeInt(oid);
        mplew.writeInt(job == 2412 ? 24120002 : 24100003);
        mplew.write(1);
        for (int i = 1; i <= numDisplay; i++) {
            mplew.writeInt(total - (numDisplay - i));
            mplew.writeInt(job == 2412 ? 2 : 0);

            mplew.writeInt(15 + Randomizer.nextInt(15));
            mplew.writeInt(7 + Randomizer.nextInt(5));
            mplew.writeInt(Randomizer.nextInt(4));

            mplew.write(i == numDisplay ? 0 : 1);
        }

        return mplew.getPacket();
    }

    public static byte[] moveFamiliar(int cid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_FAMILIAR.getValue());
        mplew.writeInt(cid);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writePos(startPos);
        mplew.writeInt(0);
        PacketHelper.serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    public static byte[] touchFamiliar(int cid, byte unk, int objectid, int type, int delay, int damage) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TOUCH_FAMILIAR.getValue());
        mplew.writeInt(cid);
        mplew.write(0);
        mplew.write(unk);
        mplew.writeInt(objectid);
        mplew.writeInt(type);
        mplew.writeInt(delay);
        mplew.writeInt(damage);

        return mplew.getPacket();
    }

    public static byte[] familiarAttack(int cid, byte unk, List<Triple<Integer, Integer, List<Integer>>> attackPair) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ATTACK_FAMILIAR.getValue());
        mplew.writeInt(cid);
        mplew.write(0);
        mplew.write(unk);
        mplew.write(attackPair.size());
        for (Triple<Integer, Integer, List<Integer>> s : attackPair) {
            mplew.writeInt(s.left);
            mplew.write(s.mid);
            mplew.write(s.right.size());
            for (int damage : s.right) {
                mplew.writeInt(damage);
            }
        }
        Iterator i$;
        return mplew.getPacket();
    }

    public static byte[] renameFamiliar(MonsterFamiliar mf) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.RENAME_FAMILIAR.getValue());
        mplew.writeInt(mf.getCharacterId());
        mplew.write(0);
        mplew.writeInt(mf.getFamiliar());
        mplew.writeMapleAsciiString(mf.getName());

        return mplew.getPacket();
    }

    public static byte[] updateFamiliar(MonsterFamiliar mf) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_FAMILIAR.getValue());
        mplew.writeInt(mf.getCharacterId());
        mplew.writeInt(mf.getFamiliar());
        mplew.writeInt(mf.getFatigue());
        mplew.writeLong(PacketHelper.getTime(mf.getVitality() >= 3 ? System.currentTimeMillis() : -2L));

        return mplew.getPacket();
    }

    public static byte[] movePlayer(int cid, List<LifeMovementFragment> moves, Point startPos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MOVE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(0);
        mplew.writePos(startPos);
        mplew.writeInt(0);
        PacketHelper.serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    public static byte[] closeRangeAttack(int cid, int tbyte, int skill, int level, int display, byte speed, List<AttackPair> damage, boolean energy, int lvl, byte mastery, byte unk, int charge) {
        return addAttackInfo(energy ? 4 : 0, cid, tbyte, skill, level, display, speed, damage, lvl, mastery, unk, 0, null, 0);
    }

    public static byte[] rangedAttack(int cid, byte tbyte, int skill, int level, int display, byte speed, int itemid, List<AttackPair> damage, Point pos, int lvl, byte mastery, byte unk) {
        return addAttackInfo(1, cid, tbyte, skill, level, display, speed, damage, lvl, mastery, unk, itemid, pos, 0);
    }

    public static byte[] strafeAttack(int cid, byte tbyte, int skill, int level, int display, byte speed, int itemid, List<AttackPair> damage, Point pos, int lvl, byte mastery, byte unk, int ultLevel) {
        return addAttackInfo(2, cid, tbyte, skill, level, display, speed, damage, lvl, mastery, unk, itemid, pos, ultLevel);
    }

    public static byte[] magicAttack(int cid, int tbyte, int skill, int level, int display, byte speed, List<AttackPair> damage, int charge, int lvl, byte unk) {
        return addAttackInfo(3, cid, tbyte, skill, level, display, speed, damage, lvl, (byte) 0, unk, charge, null, 0);
    }

    public static byte[] addAttackInfo(int type, int cid, int tbyte, int skill, int level, int display, byte speed, List<AttackPair> damage, int lvl, byte mastery, byte unk, int charge, Point pos, int ultLevel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        if (type == 0) {
            mplew.writeShort(SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue());
        } else if ((type == 1) || (type == 2)) {
            mplew.writeShort(SendPacketOpcode.RANGED_ATTACK.getValue());
        } else if (type == 3) {
            mplew.writeShort(SendPacketOpcode.MAGIC_ATTACK.getValue());
        } else {
            mplew.writeShort(SendPacketOpcode.ENERGY_ATTACK.getValue());
        }

        mplew.writeInt(cid);
        mplew.write(tbyte);
        mplew.write(lvl);
        if ((skill > 0) || (type == 3)) {
            mplew.write(level);
            mplew.writeInt(skill);
        } else {
            mplew.write(0);
        }

        if (type == 2) {
            mplew.write(ultLevel);
            if (ultLevel > 0) {
                mplew.writeInt(3220010);
            }
        }
        if (type == 1 || type == 0) {
            mplew.write(0);
        }

        mplew.write(unk);
        mplew.writeShort(display);
        mplew.write(speed);
        mplew.write(mastery);
        mplew.writeInt(charge);
        for (final AttackPair oned : damage) {
            if (oned.attack != null) {
                mplew.writeInt(oned.objectid);

                mplew.writeShort(7);
                mplew.write(0);
                mplew.write(0);
                if (skill == 4211006) {
                    mplew.write(oned.attack.size());
                    for (Pair<Integer, Boolean> eachd : oned.attack) {
                        mplew.writeInt(eachd.left); //m.e. is never crit
                    }
                } else {
                    for (final Pair<Integer, Boolean> eachd : oned.attack) {
                        mplew.write(eachd.right ? 1 : 0); // highest bit set = crit
                        mplew.writeInt(eachd.left); //m.e. is never crit
                    }
                }
            }
        }


        if ((type == 1) || (type == 2)) {
            mplew.writePos(pos);
        } else if ((type == 3) && (charge > 0)) {
            mplew.writeInt(charge);
        }
        mplew.writeZeroBytes(100);

        return mplew.getPacket();
    }

    public static byte[] skillEffect(MapleCharacter from, int skillId, byte level, short display, byte unk) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        mplew.write(level);
        mplew.writeShort(display);
        mplew.write(unk);
        if (skillId == 13111020) {
            mplew.writePos(from.getPosition()); // Position
        }

        return mplew.getPacket();
    }

    public static byte[] skillCancel(MapleCharacter from, int skillId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);

        return mplew.getPacket();
    }

    public static byte[] damagePlayer2(int cid, int type, int damage, int monsteridfrom, int skillid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.write(type);
        mplew.writeInt(damage);

        mplew.write(0);
        if (type >= -1) {
            mplew.writeInt(monsteridfrom);
            mplew.write(0);
            mplew.writeInt(skillid);
            mplew.writeInt(0);
            mplew.write(0);

            mplew.write(0);
        }
        mplew.writeInt(damage);


        return mplew.getPacket();
    }

    public static byte[] damagePlayer(int cid, int type, int damage, int monsteridfrom, byte direction, int skillid, int pDMG, boolean pPhysical, int pID, byte pType, Point pPos, byte offset, int offset_d, int fake) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.write(type);
        mplew.writeInt(damage);
        mplew.write(0);
        if (type >= -1) {
            mplew.writeInt(monsteridfrom);
            mplew.write(direction);
            mplew.writeInt(skillid);
            mplew.writeInt(pDMG);
            mplew.write(0);
            if (pDMG > 0) {
                mplew.write(pPhysical ? 1 : 0);
                mplew.writeInt(pID);
                mplew.write(pType);
                mplew.writePos(pPos);
            }
            mplew.write(offset);
            if (offset == 1) {
                mplew.writeInt(offset_d);
            }
        }
        mplew.writeInt(damage);
        if ((damage <= 0) || (fake > 0)) {
            mplew.writeInt(fake);
        }

        return mplew.getPacket();
    }

    public static byte[] facialExpression(MapleCharacter from, int expression) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FACIAL_EXPRESSION.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(expression);
        mplew.writeInt(-1);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] itemEffect(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] showAnelicbuster(int characterid, int tempid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ANGELIC_CHANGE.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(tempid);

        return mplew.getPacket();
    }

    public static byte[] showTitle(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_TITLE.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);

        return mplew.getPacket();
    }

    public static byte[] showChair(int characterid, int itemid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_CHAIR.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] updateCharLook(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        PacketHelper.addCharLook(mplew, chr, false);
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        addRingInfo(mplew, rings.getLeft());
        addRingInfo(mplew, rings.getMid());
        addMRingInfo(mplew, rings.getRight(), chr);
        mplew.writeInt(0); // -> charid to follow (4)
        return mplew.getPacket();
    }

    public static byte[] updatePartyMemberHP(int cid, int curhp, int maxhp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(curhp);
        mplew.writeInt(maxhp);

        return mplew.getPacket();
    }

    public static byte[] loadGuildName(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOAD_GUILD_NAME.getValue());
        mplew.writeInt(chr.getId());
        if (chr.getGuildId() <= 0) {
            mplew.writeShort(0);
        } else {
            MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
            } else {
                mplew.writeShort(0);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] loadGuildIcon(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOAD_GUILD_ICON.getValue());
        mplew.writeInt(chr.getId());
        if (chr.getGuildId() <= 0) {
            mplew.writeZeroBytes(6);
        } else {
            MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mplew.writeShort(gs.getLogoBG());
                mplew.write(gs.getLogoBGColor());
                mplew.writeShort(gs.getLogo());
                mplew.write(gs.getLogoColor());
            } else {
                mplew.writeZeroBytes(6);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] changeTeam(int cid, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LOAD_TEAM.getValue());
        mplew.writeInt(cid);
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] showHarvesting(int cid, int tool) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_HARVEST.getValue());
        mplew.writeInt(cid);
        if (tool > 0) {
            mplew.write(1);
            mplew.write(0);
            mplew.writeShort(0);
            mplew.writeInt(tool);
            mplew.writeZeroBytes(30);
        } else {
            mplew.write(0);
            mplew.writeZeroBytes(33);
        }
        return mplew.getPacket();
    }

    public static byte[] getPVPHPBar(int cid, int hp, int maxHp) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_HP.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(hp);
        mplew.writeInt(maxHp);

        return mplew.getPacket();
    }

    public static byte[] cancelChair(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CANCEL_CHAIR.getValue());
        if (id == -1) {
            mplew.write(0);
        } else {
            mplew.write(1);
            mplew.writeShort(id);
        }

        return mplew.getPacket();
    }

    public static byte[] instantMapWarp(byte portal) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CURRENT_MAP_WARP.getValue());
        mplew.write(0);
        mplew.write(portal);

        return mplew.getPacket();
    }

    public static byte[] updateQuestInfo(MapleCharacter c, int quest, int npc, byte progress) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(progress);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] updateQuestFinish(int quest, int npc, int nextquest) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(10);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(nextquest);

        return mplew.getPacket();
    }

    public static byte[] sendHint(String hint, int width, int height) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_HINT.getValue());
        mplew.writeMapleAsciiString(hint);
        mplew.writeShort(width < 1 ? Math.max(hint.length() * 10, 40) : width);
        mplew.writeShort(Math.max(height, 5));
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] testCombo(int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ARAN_COMBO.getValue());
        mplew.writeInt(value);

        return mplew.getPacket();
    }

    public static byte[] rechargeCombo(int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ARAN_COMBO_RECHARGE.getValue());
        mplew.writeInt(value);

        return mplew.getPacket();
    }

    public static byte[] getFollowMessage(String msg) {
        return getGameMessage(msg, (short) 11);
    }

    public static byte[] getGameMessage(String msg, short colour) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GAME_MESSAGE.getValue());
        mplew.writeShort(colour);
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] getBuffZoneEffect(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BUFF_ZONE_EFFECT.getValue());
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] getTimeBombAttack() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TIME_BOMB_ATTACK.getValue());
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(10);
        mplew.writeInt(6);

        return mplew.getPacket();
    }

    public static byte[] moveFollow(Point otherStart, Point myStart, Point otherEnd, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FOLLOW_MOVE.getValue());
        mplew.writeInt(0);
        mplew.writePos(otherStart);
        mplew.writePos(myStart);
        PacketHelper.serializeMovementList(mplew, moves);
        mplew.write(17);
        for (int i = 0; i < 8; i++) {
            mplew.write(0);
        }
        mplew.write(0);
        mplew.writePos(otherEnd);
        mplew.writePos(otherStart);
        mplew.writeZeroBytes(100);
        return mplew.getPacket();
    }

    public static byte[] getFollowMsg(int opcode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FOLLOW_MSG.getValue());
        mplew.writeLong(opcode);

        return mplew.getPacket();
    }

    public static byte[] registerFamiliar(MonsterFamiliar mf) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REGISTER_FAMILIAR.getValue());
        mplew.writeLong(mf.getId());
        mf.writeRegisterPacket(mplew, false);
        mplew.writeShort(mf.getVitality() >= 3 ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] createUltimate(int amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CREATE_ULTIMATE.getValue());
        mplew.writeInt(amount);

        return mplew.getPacket();
    }

    public static byte[] harvestMessage(int oid, int msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.HARVEST_MESSAGE.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(msg);

        return mplew.getPacket();
    }

    public static byte[] openBag(int index, int itemId, boolean firstTime) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OPEN_BAG.getValue());
        mplew.writeInt(index);
        mplew.writeInt(itemId);
        mplew.writeShort(firstTime ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] dragonBlink(int portalId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DRAGON_BLINK.getValue());
        mplew.write(portalId);

        return mplew.getPacket();
    }

    public static byte[] getPVPIceGage(int score) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PVP_ICEGAGE.getValue());
        mplew.writeInt(score);

        return mplew.getPacket();
    }

    public static byte[] skillCooldown(int sid, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.COOLDOWN.getValue());
        mplew.writeInt(sid);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        mplew.write(mod);
        mplew.writeInt(drop.getObjectId());
        mplew.write(drop.getMeso() > 0 ? 1 : 0);
        mplew.writeInt(drop.getItemId());
        mplew.writeInt(drop.getOwner());
        mplew.write(drop.getDropType());
        mplew.writePos(dropto);
        mplew.writeInt(0);
        if (mod != 2) {
            mplew.writePos(dropfrom);
            mplew.writeShort(0);
        }
        mplew.write(0);
        if (drop.getMeso() == 0) {
            PacketHelper.addExpirationTime(mplew, drop.getItem().getExpiration());
        }
        mplew.writeShort(drop.isPlayerDrop() ? 0 : 1);
        mplew.writeZeroBytes(3);

        return mplew.getPacket();
    }

    public static byte[] explodeDrop(int oid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(4);
        mplew.writeInt(oid);
        mplew.writeShort(655);

        return mplew.getPacket();
    }

    public static byte[] removeItemFromMap(int oid, int animation, int cid) {
        return removeItemFromMap(oid, animation, cid, 0);
    }

    public static byte[] removeItemFromMap(int oid, int animation, int cid, int slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(animation);
        mplew.writeInt(oid);
        if (animation >= 2) {
            mplew.writeInt(cid);
            if (animation == 5) {
                mplew.writeInt(slot);
            }
        }
        return mplew.getPacket();
    }

    public static byte[] spawnMist(MapleMist mist) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_MIST.getValue());
        mplew.writeInt(mist.getObjectId());

        mplew.write(mist.isMobMist() ? 0 : mist.isPoisonMist());
        mplew.writeInt(mist.getOwnerId());
        if (mist.getMobSkill() == null) {
            mplew.writeInt(mist.getSourceSkill().getId());
        } else {
            mplew.writeInt(mist.getMobSkill().getSkillId());
        }
        mplew.write(mist.getSkillLevel());
        mplew.writeShort(mist.getSkillDelay());
        mplew.writeRect(mist.getBox());
        mplew.writeLong(0L);
        mplew.writePos(mist.getPosition());
        mplew.writeLong(0L);

        return mplew.getPacket();
    }

    public static byte[] removeMist(int oid, boolean eruption) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_MIST.getValue());
        mplew.writeInt(oid);
        mplew.write(eruption ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] spawnDoor(int oid, Point pos, boolean animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_DOOR.getValue());
        mplew.write(animation ? 0 : 1);
        mplew.writeInt(oid);
        mplew.writePos(pos);

        return mplew.getPacket();
    }

    public static byte[] removeDoor(int oid, boolean animation) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.REMOVE_DOOR.getValue());
        mplew.write(animation ? 0 : 1);
        mplew.writeInt(oid);

        return mplew.getPacket();
    }

    public static byte[] spawnMechDoor(MechDoor md, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MECH_DOOR_SPAWN.getValue());
        mplew.write(animated ? 0 : 1);
        mplew.writeInt(md.getOwnerId());
        mplew.writePos(md.getTruePosition());
        mplew.write(md.getId());
        mplew.writeInt(md.getPartyId());
        return mplew.getPacket();
    }

    public static byte[] removeMechDoor(MechDoor md, boolean animated) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MECH_DOOR_REMOVE.getValue());
        mplew.write(animated ? 0 : 1);
        mplew.writeInt(md.getOwnerId());
        mplew.write(md.getId());

        return mplew.getPacket();
    }

    public static byte[] triggerReactor(MapleReactor reactor, int stance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REACTOR_HIT.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getTruePosition());
        mplew.writeInt(stance);
        return mplew.getPacket();
    }

    public static byte[] spawnReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REACTOR_SPAWN.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.writeInt(reactor.getReactorId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getTruePosition());
        mplew.write(reactor.getFacingDirection());
        mplew.writeMapleAsciiString(reactor.getName());

        return mplew.getPacket();
    }

    public static byte[] destroyReactor(MapleReactor reactor) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REACTOR_DESTROY.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());

        return mplew.getPacket();
    }

    public static byte[] makeExtractor(int cid, String cname, Point pos, int timeLeft, int itemId, int fee) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_EXTRACTOR.getValue());
        mplew.writeInt(cid);
        mplew.writeMapleAsciiString(cname);
        mplew.writeInt(pos.x);
        mplew.writeInt(pos.y);
        mplew.writeShort(timeLeft);
        mplew.writeInt(itemId);
        mplew.writeInt(fee);

        return mplew.getPacket();
    }

    public static byte[] removeExtractor(int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REMOVE_EXTRACTOR.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(1);

        return mplew.getPacket();
    }


    public static byte[] updateAriantScore(List<MapleCharacter> players) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ARIANT_SCORE_UPDATE.getValue());
        mplew.write(players.size());
        for (MapleCharacter i : players) {
            mplew.writeMapleAsciiString(i.getName());
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static byte[] sheepRanchInfo(byte wolf, byte sheep) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHEEP_RANCH_INFO.getValue());
        mplew.write(wolf);
        mplew.write(sheep);

        return mplew.getPacket();
    }

    public static byte[] sheepRanchClothes(int cid, byte clothes) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHEEP_RANCH_CLOTHES.getValue());
        mplew.writeInt(cid);
        mplew.write(clothes);

        return mplew.getPacket();
    }

    public static byte[] updateWitchTowerKeys(int keys) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WITCH_TOWER.getValue());
        mplew.write(keys);

        return mplew.getPacket();
    }

    public static byte[] showChaosZakumShrine(boolean spawned, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHAOS_ZAKUM_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] showChaosHorntailShrine(boolean spawned, int time) {
        return showHorntailShrine(spawned, time);
    }

    public static byte[] showHorntailShrine(boolean spawned, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.HORNTAIL_SHRINE.getValue());
        mplew.write(spawned ? 1 : 0);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] getRPSMode(byte mode, int mesos, int selection, int answer) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.RPS_GAME.getValue());
        mplew.write(mode);
        switch (mode) {
            case 6:
                if (mesos != -1) {
                    mplew.writeInt(mesos);
                }
                break;
            case 8:
                mplew.writeInt(9000019);
                break;
            case 11:
                mplew.write(selection);
                mplew.write(answer);
        }

        return mplew.getPacket();
    }

    public static byte[] messengerInvite(String from, int messengerid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(3);
        mplew.writeMapleAsciiString(from);
        mplew.write(0);
        mplew.writeInt(messengerid);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0);
        mplew.write(position);
        PacketHelper.addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.writeShort(channel);
        mplew.write(1);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] removeMessengerPlayer(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(2);
        mplew.write(position);

        return mplew.getPacket();
    }

    public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(7);
        mplew.write(position);
        PacketHelper.addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.writeShort(channel);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] joinMessenger(int position) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(1);
        mplew.write(position);

        return mplew.getPacket();
    }

    public static byte[] messengerChat(String charname, String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(6);
        mplew.writeMapleAsciiString(charname);
        mplew.writeMapleAsciiString(text);

        return mplew.getPacket();
    }

    public static byte[] messengerNote(String text, int mode, int mode2) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MESSENGER.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(text);
        mplew.write(mode2);

        return mplew.getPacket();
    }

    public static byte[] removeItemFromDuey(boolean remove, int Package) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DUEY.getValue());
        mplew.write(24);
        mplew.writeInt(Package);
        mplew.write(remove ? 3 : 4);

        return mplew.getPacket();
    }

    public static byte[] sendDuey(byte operation, List<MapleDueyActions> packages) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.DUEY.getValue());
        mplew.write(operation);

        switch (operation) {
            case 9:
                mplew.write(1);

                break;
            case 10:
                mplew.write(0);
                mplew.write(packages.size());

                for (MapleDueyActions dp : packages) {
                    mplew.writeInt(dp.getPackageId());
                    mplew.writeAsciiString(dp.getSender(), 13);
                    mplew.writeInt(dp.getMesos());
                    mplew.writeLong(PacketHelper.getTime(dp.getSentTime()));
                    mplew.writeZeroBytes(205);

                    if (dp.getItem() != null) {
                        mplew.write(1);
                        PacketHelper.addItemInfo(mplew, dp.getItem());
                    } else {
                        mplew.write(0);
                    }
                }

                mplew.write(0);
        }

        return mplew.getPacket();
    }

    public static byte[] getKeymap(MapleKeyLayout layout) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.KEYMAP.getValue());

        layout.writeData(mplew);

        return mplew.getPacket();
    }

    public static byte[] petAutoHP(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PET_AUTO_HP.getValue());
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static byte[] petAutoMP(int itemId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PET_AUTO_MP.getValue());
        mplew.writeInt(itemId);

        return mplew.getPacket();
    }

    public static void addRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings) {
        mplew.write(rings.size());
        for (MapleRing ring : rings) {
            mplew.writeInt(1);
            mplew.writeLong(ring.getRingId());
            mplew.writeLong(ring.getPartnerRingId());
            mplew.writeInt(ring.getItemId());
        }
    }

    public static void addMRingInfo(MaplePacketLittleEndianWriter mplew, List<MapleRing> rings, MapleCharacter chr) {
        mplew.write(rings.size());
        for (MapleRing ring : rings) {
            mplew.writeInt(1);
            mplew.writeInt(chr.getId());
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeInt(ring.getItemId());
        }
    }

    public static byte[] getBuffBar(long millis) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BUFF_BAR.getValue());
        mplew.writeLong(millis);

        return mplew.getPacket();
    }

    public static byte[] getBoosterFamiliar(int cid, int familiar, int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOOSTER_FAMILIAR.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(familiar);
        mplew.writeLong(id);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] updateCardStack(int total) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PHANTOM_CARD.getValue());
        mplew.write(total);

        return mplew.getPacket();
    }

    public static byte[] updateluminouscombo_black(int darktotal, int lighttotal, int darktype, int lighttype) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LUMINOUS_COMBO.getValue());
        mplew.writeInt(darktotal);
        mplew.writeInt(lighttotal);
        mplew.writeInt(darktype);
        mplew.writeInt(lighttype);
        mplew.writeInt(1210382225);

        return mplew.getPacket();
    }

    public static byte[] viewSkills(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TARGET_SKILL.getValue());
        List skillz = new ArrayList();
        for (Skill sk : chr.getSkills().keySet()) {
            if ((sk.canBeLearnedBy(chr.getJob())) && (GameConstants.canSteal(sk)) && (!skillz.contains(Integer.valueOf(sk.getId())))) {
                skillz.add(Integer.valueOf(sk.getId()));
            }
        }
        mplew.write(1);
        mplew.writeInt(chr.getId());
        mplew.writeInt(skillz.isEmpty() ? 2 : 4);
        mplew.writeInt(chr.getJob());
        mplew.writeInt(skillz.size());
        for (Iterator i$ = skillz.iterator(); i$.hasNext();) {
            int i = ((Integer) i$.next()).intValue();
            mplew.writeInt(i);
        }

        return mplew.getPacket();
    }

    static {
        DEFAULT_BUFFMASK |= MapleBuffStat.ENERGY_CHARGE.getValue();
        DEFAULT_BUFFMASK |= MapleBuffStat.DASH_SPEED.getValue();
        DEFAULT_BUFFMASK |= MapleBuffStat.DASH_JUMP.getValue();
        DEFAULT_BUFFMASK |= MapleBuffStat.MONSTER_RIDING.getValue();
        DEFAULT_BUFFMASK |= MapleBuffStat.SPEED_INFUSION.getValue();
        DEFAULT_BUFFMASK |= MapleBuffStat.HOMING_BEACON.getValue();
        DEFAULT_BUFFMASK |= MapleBuffStat.DEFAULT_BUFFSTAT.getValue();
    }

    public static class EffectPacket {

        public static byte[] showForeignEffect(int effect) {
            return showForeignEffect(-1, effect);
        }

        public static byte[] showForeignEffect(int cid, int effect) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            if (cid == -1) {
                mplew.writeShort(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
            } else {
                mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
            }
            mplew.write(effect);

            return mplew.getPacket();
        }

        public static byte[] showItemLevelupEffect() {
            return showForeignEffect(18);
        }

        public static byte[] showForeignItemLevelupEffect(int cid) {
            return showForeignEffect(cid, 18);
        }

        public static byte[] showOwnDiceEffect(int skillid, int effectid, int effectid2, int level) {
            return showDiceEffect(-1, skillid, effectid, effectid2, level);
        }

        public static byte[] showDiceEffect(int cid, int skillid, int effectid, int effectid2, int level) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            if (cid == -1) {
                mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            } else {
                mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
            }
            mplew.write(3);
            mplew.writeInt(effectid);
            mplew.writeInt(effectid2);
            mplew.writeInt(skillid);
            mplew.write(level);
            mplew.write(0);
            mplew.writeZeroBytes(100);
            return mplew.getPacket();
        }

        public static byte[] useCharm(byte charmsleft, byte daysleft, boolean safetyCharm) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(8);
            mplew.write(safetyCharm ? 1 : 0);
            mplew.write(charmsleft);
            mplew.write(daysleft);
            if (!safetyCharm) {
                mplew.writeInt(0);
            }

            return mplew.getPacket();
        }

        public static byte[] Mulung_DojoUp2() {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(10);

            return mplew.getPacket();
        }

        public static byte[] showOwnHpHealed(int amount) {
            return showHpHealed(-1, amount);
        }

        public static byte[] showHpHealed(int cid, int amount) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            if (cid == -1) {
                mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            } else {
                mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
            }
            mplew.write(30);
            mplew.writeInt(amount);

            return mplew.getPacket();
        }

        public static byte[] showRewardItemAnimation(int itemId, String effect) {
            return showRewardItemAnimation(itemId, effect, -1);
        }

        public static byte[] showRewardItemAnimation(int itemId, String effect, int from_playerid) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            if (from_playerid == -1) {
                mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            } else {
                mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(from_playerid);
            }
            mplew.write(17);
            mplew.writeInt(itemId);
            mplew.write((effect != null) && (effect.length() > 0) ? 1 : 0);
            if ((effect != null) && (effect.length() > 0)) {
                mplew.writeMapleAsciiString(effect);
            }

            return mplew.getPacket();
        }

        public static byte[] showCashItemEffect(int itemId) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(23);
            mplew.writeInt(itemId);

            return mplew.getPacket();
        }

        public static byte[] ItemMaker_Success() {
            return ItemMaker_Success_3rdParty(-1);
        }

        public static byte[] ItemMaker_Success_3rdParty(int from_playerid) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            if (from_playerid == -1) {
                mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            } else {
                mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(from_playerid);
            }
            mplew.write(19);
            mplew.writeInt(0);

            return mplew.getPacket();
        }

        public static byte[] useWheel(byte charmsleft) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(24);
            mplew.write(charmsleft);

            return mplew.getPacket();
        }

        public static byte[] showOwnBuffEffect(int skillid, int effectid, int playerLevel, int skillLevel) {
            return showBuffeffect(-1, skillid, effectid, playerLevel, skillLevel, (byte) 3);
        }

        public static byte[] showOwnBuffEffect(int skillid, int effectid, int playerLevel, int skillLevel, byte direction) {
            return showBuffeffect(-1, skillid, effectid, playerLevel, skillLevel, direction);
        }

        public static byte[] showBuffeffect(int cid, int skillid, int effectid, int playerLevel, int skillLevel) {
            return showBuffeffect(cid, skillid, effectid, playerLevel, skillLevel, (byte) 3);
        }

        public static byte[] showBuffeffect(int cid, int skillid, int effectid, int playerLevel, int skillLevel, byte direction) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            if (cid == -1) {
                mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            } else {
                mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
            }
            mplew.write(effectid);
            mplew.writeInt(skillid);
            mplew.write(playerLevel - 1);
            if ((effectid == 2) && (skillid == 31111003)) {
                mplew.writeInt(0);
            }
            mplew.write(skillLevel);
            if ((direction != 3) || (skillid == 1320006) || (skillid == 30001062) || (skillid == 30001061)) {
                mplew.write(direction);
            }

            if (skillid == 30001062) {
                mplew.writeInt(0);
            }
            mplew.writeZeroBytes(10);

            return mplew.getPacket();
        }

        public static byte[] ShowWZEffect(String data) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(21);
            mplew.writeMapleAsciiString(data);

            return mplew.getPacket();
        }

        public static byte[] showOwnCraftingEffect(String effect, int time, int mode) {
            return showCraftingEffect(-1, effect, time, mode);
        }

        public static byte[] showCraftingEffect(int cid, String effect, int time, int mode) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            if (cid == -1) {
                mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            } else {
                mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(cid);
            }
            mplew.write(33);
            mplew.writeMapleAsciiString(effect);
            mplew.write(0);
            mplew.writeInt(time);
            mplew.writeInt(mode);
            if (mode == 2) {
                mplew.writeInt(0);
            }

            return mplew.getPacket();
        }

        public static byte[] AranTutInstructionalBalloon(String data) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(25);
            mplew.writeMapleAsciiString(data);
            mplew.writeInt(1);

            return mplew.getPacket();
        }

        public static byte[] showOwnPetLevelUp(byte index) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(6);
            mplew.write(0);
            mplew.write(index);

            return mplew.getPacket();
        }

        public static byte[] showOwnChampionEffect() {
            return showChampionEffect(-1);
        }

        public static byte[] showChampionEffect(int from_playerid) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            if (from_playerid == -1) {
                mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            } else {
                mplew.writeShort(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
                mplew.writeInt(from_playerid);
            }
            mplew.write(34);
            mplew.writeInt(30000);

            return mplew.getPacket();
        }
    }

    public static class UIPacket {

        public static byte[] getDirectionStatus(boolean enable) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.DIRECTION_STATUS.getValue());
            mplew.write(enable ? 1 : 0);

            return mplew.getPacket();
        }

        public static byte[] openUI(int type) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

            mplew.writeShort(SendPacketOpcode.OPEN_UI.getValue());
            mplew.write(type);

            return mplew.getPacket();
        }

        public static byte[] getDirectionInfoTest(byte type, int value) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
            mplew.write(type);
            mplew.writeInt(value);
            return mplew.getPacket();
        }

        public static byte[] sendRepairWindow(int npc) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);

            mplew.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
            mplew.writeInt(33);
            mplew.writeInt(npc);

            return mplew.getPacket();
        }

        public static byte[] startAzwan(int npc) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);
            mplew.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
            mplew.writeInt(70);
            mplew.writeInt(npc);
            return mplew.getPacket();
        }

        public static byte[] sendRedLeaf(int npc) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);

            mplew.writeShort(SendPacketOpcode.OPEN_UI_OPTION.getValue());
            mplew.writeInt(66);
            mplew.writeInt(npc);

            return mplew.getPacket();
        }

        public static byte[] IntroLock(boolean enable) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_LOCK.getValue());
            mplew.write(enable ? 1 : 0);
            mplew.writeInt(0);

            return mplew.getPacket();
        }

        public static byte[] IntroEnableUI(int wtf) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_ENABLE_UI.getValue());
            mplew.write(wtf > 0 ? 1 : 0);
            if (wtf > 0) {
                mplew.writeShort(wtf);
            }

            return mplew.getPacket();
        }

        public static byte[] IntroDisableUI(boolean enable) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.CYGNUS_INTRO_DISABLE_UI.getValue());
            mplew.write(enable ? 1 : 0);

            return mplew.getPacket();
        }

        public static byte[] summonHelper(boolean summon) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SUMMON_HINT.getValue());
            mplew.write(summon ? 1 : 0);

            return mplew.getPacket();
        }

        public static byte[] summonMessage(int type) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
            mplew.write(1);
            mplew.writeInt(type);
            mplew.writeInt(7000);

            return mplew.getPacket();
        }

        public static byte[] summonMessage(String message) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SUMMON_HINT_MSG.getValue());
            mplew.write(0);
            mplew.writeMapleAsciiString(message);
            mplew.writeInt(200);
            mplew.writeShort(0);
            mplew.writeInt(10000);

            return mplew.getPacket();
        }

        public static byte[] getDirectionInfo(int type, int value) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
            mplew.write(type);
            mplew.writeLong(value);

            return mplew.getPacket();
        }

        public static byte[] getDirectionInfo(String data, int value, int x, int y, int pro) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.DIRECTION_INFO.getValue());
            mplew.write(2);
            mplew.writeMapleAsciiString(data);
            mplew.writeInt(value);
            mplew.writeInt(x);
            mplew.writeInt(y);
            mplew.writeShort(pro);
            mplew.writeInt(0);

            return mplew.getPacket();
        }

        public static byte[] reissueMedal(int itemId, int type) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.REISSUE_MEDAL.getValue());
            mplew.write(type);
            mplew.writeInt(itemId);

            return mplew.getPacket();
        }

        public static final byte[] playMovie(String data, boolean show) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PLAY_MOVIE.getValue());
            mplew.writeMapleAsciiString(data);
            mplew.write(show ? 1 : 0);

            return mplew.getPacket();
        }
    }

    public static class SummonPacket {

        public static byte[] spawnSummon(MapleSummon summon, boolean animated) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SPAWN_SUMMON.getValue());

//            26 F8 50 00 // ownerid
//            23 03 00 00  // objectid
//            5D 78 2F 00  // skill 
//            61 // ownerlv
//            03  //skill lv
//            1E 0B 4E 01 // pos 
//            04 // movement type
//            78 // summontype
//            00  // animated?
//            03 // ?? 
////            01 01 01 00
            //   mplew.write(HexTool.hex("26 F8 50 00 2E 03 00 00 5D 78 2F 00 61 03 1E 0B 4E 01 04 78 00 03 01 01 01 00"));
            mplew.writeInt(summon.getOwnerId());
            mplew.writeInt(summon.getObjectId());
            mplew.writeInt(summon.getSkill());
            mplew.write(summon.getOwnerLevel());
            mplew.write(summon.getSkillLevel());
            mplew.writePos(summon.getPosition());
//            mplew.write(HexTool.hex("78 00 03 01 01 01 00"));
            mplew.write((summon.getSkill() == 32111006) || (summon.getSkill() == 33101005) || (summon.getSkill() == 42100010) ? 5 : 4);
            if ((summon.getSkill() == 35121003) && (summon.getOwner().getMap() != null)) {
                mplew.writeShort(summon.getOwner().getMap().getFootholds().findBelow(summon.getPosition()).getId());
            } else {
                mplew.writeShort(0);
            }
            mplew.write(summon.getMovementType().getValue());
            mplew.write(summon.getSummonType());
            mplew.write(animated ? 1 : 0);
            mplew.write(1);
            MapleCharacter chr = summon.getOwner();
            mplew.write((summon.getSkill() == 4341006) && (chr != null) ? 1 : 0);
            if ((summon.getSkill() == 4341006) && (chr != null)) {
                PacketHelper.addCharLook(mplew, chr, true);
            }
            if (summon.getSkill() == 35111002) {
                mplew.write(0);
            }
            if (summon.getSkill() == 42111003) {
                mplew.writeZeroBytes(8);
            }

            return mplew.getPacket();
        }

        public static byte[] removeSummon(int ownerId, int objId) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
            mplew.writeInt(ownerId);
            mplew.writeInt(objId);
            mplew.write(10);

            return mplew.getPacket();
        }

        public static byte[] removeSummon(MapleSummon summon, boolean animated) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.REMOVE_SUMMON.getValue());
            mplew.writeInt(summon.getOwnerId());
            mplew.writeInt(summon.getObjectId());
            if (animated) {
                switch (summon.getSkill()) {
                    case 35121003:
                        mplew.write(10);
                        break;
                    case 33101008:
                    case 35111001:
                    case 35111002:
                    case 35111005:
                    case 35111009:
                    case 35111010:
                    case 35111011:
                    case 35121009:
                    case 35121010:
                    case 35121011:
                        mplew.write(5);
                        break;
                    default:
                        mplew.write(4);
                        break;
                }
            } else {
                mplew.write(1);
            }

            return mplew.getPacket();
        }

        public static byte[] moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.MOVE_SUMMON.getValue());
            mplew.writeInt(cid);
            mplew.writeInt(oid);
            mplew.writeInt(0);
            mplew.writePos(startPos);
            mplew.writeInt(0);
            PacketHelper.serializeMovementList(mplew, moves);

            return mplew.getPacket();
        }

        public static byte[] summonAttack(int cid, int summonSkillId, byte animation, List<Pair<Integer, Integer>> allDamage, int level, boolean darkFlare) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SUMMON_ATTACK.getValue());
            mplew.writeInt(cid);
            mplew.writeInt(summonSkillId);
            mplew.write(level - 1);
            mplew.write(animation);
            mplew.write(allDamage.size());
            for (Pair attackEntry : allDamage) {
                mplew.writeInt(((Integer) attackEntry.left).intValue());
                mplew.write(7);
                mplew.writeInt(((Integer) attackEntry.right).intValue());
            }
            mplew.write(darkFlare ? 1 : 0);

            return mplew.getPacket();
        }

        public static byte[] pvpSummonAttack(int cid, int playerLevel, int oid, int animation, Point pos, List<AttackPair> attack) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PVP_SUMMON.getValue());
            mplew.writeInt(cid);
            mplew.writeInt(oid);
            mplew.write(playerLevel);
            mplew.write(animation);
            mplew.writePos(pos);
            mplew.writeInt(0);
            mplew.write(attack.size());
            for (AttackPair p : attack) {
                mplew.writeInt(p.objectid);
                mplew.writePos(p.point);
                mplew.write(p.attack.size());
                mplew.write(0);
                for (Pair atk : p.attack) {
                    mplew.writeInt(((Integer) atk.left).intValue());
                }
            }

            return mplew.getPacket();
        }

        public static byte[] summonSkill(int cid, int summonSkillId, int newStance) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SUMMON_SKILL.getValue());
            mplew.writeInt(cid);
            mplew.writeInt(summonSkillId);
            mplew.write(newStance);

            return mplew.getPacket();
        }

        public static byte[] damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.DAMAGE_SUMMON.getValue());
            mplew.writeInt(cid);
            mplew.writeInt(summonSkillId);
            mplew.write(unkByte);
            mplew.writeInt(damage);
            mplew.writeInt(monsterIdFrom);
            mplew.write(0);

            return mplew.getPacket();
        }
    }

    public static class NPCPacket {

        public static byte[] spawnNPC(MapleNPC life, boolean show) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SPAWN_NPC.getValue());
            mplew.writeInt(life.getObjectId());
            mplew.writeInt(life.getId());
            mplew.writeShort(life.getPosition().x);
            mplew.writeShort(life.getCy());
            mplew.write(life.getF() == 1 ? 0 : 1);
            mplew.writeShort(life.getFh());
            mplew.writeShort(life.getRx0());
            mplew.writeShort(life.getRx1());
            mplew.write(show ? 1 : 0);

            return mplew.getPacket();
        }
        
                public static byte[] getSlideMenu(int npcid, int type, int lasticon, String sel) {
            //Types: 0 - map selection 1 - neo city map selection 2 - korean map selection 3 - tele rock map selection 4 - dojo buff selection
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
            mplew.write(4); //slide menu
            mplew.writeInt(npcid);
            mplew.write(0);
            mplew.writeShort(0x12);//0x12
            mplew.writeInt(type); //menu type
            mplew.writeInt(type == 0 ? lasticon : 0); //last icon on menu
            mplew.writeMapleAsciiString(sel);

            return mplew.getPacket();
        }

        public static byte[] removeNPC(int objectid) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.REMOVE_NPC.getValue());
            mplew.writeInt(objectid);

            return mplew.getPacket();
        }

        public static byte[] removeNPCController(int objectid) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
            mplew.write(0);
            mplew.writeInt(objectid);

            return mplew.getPacket();
        }

        public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
            mplew.write(1);
            mplew.writeInt(life.getObjectId());
            mplew.writeInt(life.getId());
            mplew.writeShort(life.getPosition().x);
            mplew.writeShort(life.getCy());
            mplew.write(life.getF() == 1 ? 0 : 1);
            mplew.writeShort(life.getFh());
            mplew.writeShort(life.getRx0());
            mplew.writeShort(life.getRx1());
            mplew.write(MiniMap ? 1 : 0);

            return mplew.getPacket();
        }

        public static byte[] setNPCScriptable(List<Pair<Integer, String>> npcs) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.NPC_SCRIPTABLE.getValue());
            mplew.write(npcs.size());
            for (Pair s : npcs) {
                mplew.writeInt(((Integer) s.left).intValue());
                mplew.writeMapleAsciiString((String) s.right);
                mplew.writeInt(0);
                mplew.writeInt(2147483647);
            }
            return mplew.getPacket();
        }

        public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type) {
            return getNPCTalk(npc, msgType, talk, endBytes, type, npc);
        }

        public static byte[] testpacket() {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.write(HexTool.getByteArrayFromHexString(""));
            return mplew.getPacket();
        }

        public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type, int diffNPC) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
            mplew.write(4);
            mplew.writeInt(npc);
            mplew.write(0);
            mplew.write(msgType);
            mplew.write(type);
            if ((type & 0x4) != 0) {
                mplew.writeInt(diffNPC);
            }
            mplew.writeMapleAsciiString(talk);
            mplew.write(HexTool.getByteArrayFromHexString(endBytes));

            return mplew.getPacket();
        }

        public static byte[] getMapSelection(int npcid, String sel) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
            mplew.write(4);
            mplew.writeInt(npcid);
            mplew.writeShort(GameConstants.GMS ? 17 : 16);
            mplew.writeInt(npcid == 2083006 ? 1 : 0);
            mplew.writeInt(npcid == 9010022 ? 1 : 0);
            mplew.writeMapleAsciiString(sel);

            return mplew.getPacket();
        }

        public static byte[] getAndroidTalkStyle(int npc, String talk, int[] args) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
            mplew.write(4);
            mplew.writeInt(npc);
            mplew.writeShort(10);
            mplew.writeMapleAsciiString(talk);
            mplew.write(args.length);

            for (int i = 0; i < args.length; i++) {
                mplew.writeInt(args[i]);
            }
            return mplew.getPacket();
        }

        public static byte[] getNPCTalkStyle(int npc, String talk, int[] args) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
            mplew.write(4);
            mplew.writeInt(npc);
            mplew.write(0);
            mplew.writeShort(9);
            mplew.writeMapleAsciiString(talk);
            mplew.write(args.length);

            for (int i = 0; i < args.length; i++) {
                mplew.writeInt(args[i]);
            }
            return mplew.getPacket();
        }

        public static byte[] getNPCTalkNum(int npc, String talk, int def, int min, int max) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
            mplew.write(4);
            mplew.writeInt(npc);
            mplew.write(0);
            mplew.writeShort(4);
            mplew.writeMapleAsciiString(talk);
            mplew.writeInt(def);
            mplew.writeInt(min);
            mplew.writeInt(max);
            mplew.writeInt(0);

            return mplew.getPacket();
        }

        public static byte[] getNPCTalkText(int npc, String talk) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
            mplew.write(4);
            mplew.writeInt(npc);
            mplew.write(0);
            mplew.writeShort(3);
            mplew.writeMapleAsciiString(talk);
            mplew.writeInt(0);
            mplew.writeInt(0);

            return mplew.getPacket();
        }

        public static byte[] getSelfTalkText(String text) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
            mplew.write(3);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.write(17);
            mplew.writeMapleAsciiString(text);
            return mplew.getPacket();
        }

        public static byte[] getNPCTutoEffect(String effect) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
            mplew.write(3);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.write(1);
            mplew.writeShort(257);
            mplew.writeMapleAsciiString(effect);
            return mplew.getPacket();
        }

        public static byte[] getEvanTutorial(String data) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());

            mplew.write(8);
            mplew.writeInt(0);
            mplew.write(1);
            mplew.write(1);
            mplew.write(1);
            mplew.writeMapleAsciiString(data);

            return mplew.getPacket();
        }

        public static byte[] getNPCShop(int sid, MapleShop shop, MapleClient c) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.OPEN_NPC_SHOP.getValue());
            mplew.writeInt(0);
            mplew.writeInt(sid);
            PacketHelper.addShopInfo(mplew, shop, c);

            return mplew.getPacket();
        }

        public static byte[] confirmShopTransaction(byte code, MapleShop shop, MapleClient c, int indexBought) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
            mplew.write(code);
            if (code == 5) {
                mplew.writeInt(0);
                mplew.writeInt(shop.getNpcId());
                PacketHelper.addShopInfo(mplew, shop, c);
            } else {
                mplew.write(0);
                mplew.write(indexBought >= 0 ? 1 : 0);
                if (indexBought >= 0) {
                    mplew.writeInt(indexBought);
                }
            }

            return mplew.getPacket();
        }

        public static byte[] getStorage(int npcId, byte slots, Collection<Item> items, long meso) {
            MaplePacketLittleEndianWriter packet = new MaplePacketLittleEndianWriter();

            packet.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
            packet.write(0x16);
            packet.writeInt(npcId);
            packet.write(slots);
            packet.writeShort(0x7E);
            packet.writeShort(0);
            packet.writeInt(0);
            packet.writeLong(meso);
            packet.writeShort(0);
            packet.write((byte) items.size());
            for (Item item : items) {
                PacketHelper.addItemInfo(packet, item);
            }
            packet.writeZeroBytes(4);

            return packet.getPacket();
        }

        public static byte[] getStorageFull() {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
            mplew.write(17);

            return mplew.getPacket();
        }

        public static byte[] mesoStorage(byte slots, long meso) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
            mplew.write(19);
            mplew.write(slots);
            mplew.writeShort(2);
            mplew.writeShort(0);
            mplew.writeInt(0);
            mplew.writeLong(meso);

            return mplew.getPacket();
        }

        public static byte[] arrangeStorage(byte slots, Collection<Item> items, boolean changed) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
            mplew.write(15);
            mplew.write(slots);
            mplew.write(124);
            mplew.writeZeroBytes(10);
            mplew.write(items.size());
            for (Item item : items) {
                PacketHelper.addItemInfo(mplew, item);
            }
            mplew.write(0);
            return mplew.getPacket();
        }

        public static byte[] storeStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
            mplew.write(13);
            mplew.write(slots);
            mplew.writeShort(type.getBitfieldEncoding());
            mplew.writeShort(0);
            mplew.writeInt(0);
            mplew.write(items.size());
            for (Item item : items) {
                PacketHelper.addItemInfo(mplew, item);
            }
            return mplew.getPacket();
        }

        public static byte[] takeOutStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.OPEN_STORAGE.getValue());
            mplew.write(9);
            mplew.write(slots);
            mplew.writeShort(type.getBitfieldEncoding());
            mplew.writeShort(0);
            mplew.writeInt(0);
            mplew.write(items.size());
            for (Item item : items) {
                PacketHelper.addItemInfo(mplew, item);
            }
            return mplew.getPacket();
        }
    }

    public static class InteractionPacket {

        public static byte[] getTradeInvite(MapleCharacter c) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
            mplew.write(PlayerInteractionHandler.Interaction.INVITE_TRADE.action);
            mplew.write(4);
            mplew.writeMapleAsciiString(c.getName());
            mplew.writeInt(c.getLevel());
            mplew.writeInt(c.getJob());
            return mplew.getPacket();
        }

        public static byte[] getTradeMesoSet(byte number, int meso) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
            mplew.write(4);
            mplew.write(number);
            mplew.writeLong(meso);

            return mplew.getPacket();
        }

        public static byte[] getTradeItemAdd(byte number, Item item) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
            mplew.write(0);
            mplew.write(number);
            mplew.write(item.getPosition());
            PacketHelper.addItemInfo(mplew, item);

            return mplew.getPacket();
        }

        public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
            mplew.write(0x14);
            mplew.write(4);
            mplew.write(2);
            mplew.write(number);

            if (number == 1) {
                mplew.write(0);
                PacketHelper.addCharLook(mplew, trade.getPartner().getChr(), false);
                mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
                mplew.writeShort(trade.getPartner().getChr().getJob());
            }
            mplew.write(number);
            PacketHelper.addCharLook(mplew, c.getPlayer(), false);
            mplew.writeMapleAsciiString(c.getPlayer().getName());
            mplew.writeShort(c.getPlayer().getJob());
            mplew.write(255);

            return mplew.getPacket();
        }

        public static byte[] getTradeConfirmation() {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
            mplew.write(PlayerInteractionHandler.Interaction.CONFIRM_TRADE.action);

            return mplew.getPacket();
        }

        public static byte[] TradeMessage(byte UserSlot, byte message) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
            mplew.write(PlayerInteractionHandler.Interaction.EXIT.action);
            mplew.write(UserSlot);
            mplew.write(message);

            return mplew.getPacket();
        }

        public static byte[] getTradeCancel(byte UserSlot, int unsuccessful) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PLAYER_INTERACTION.getValue());
            mplew.write(PlayerInteractionHandler.Interaction.EXIT.action);
            mplew.write(UserSlot);
            mplew.write(2);

            return mplew.getPacket();
        }
    }
}