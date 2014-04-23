package tools.packet;

import clientside.BuddylistEntry;
import clientside.MapleBuffStat;
import clientside.MapleCharacter;
import clientside.MapleDisease;
import clientside.MapleQuestStatus;
import clientside.MapleStat;
import clientside.MapleStat.Temp;
import clientside.MapleTrait;
import clientside.MapleTrait.MapleTraitType;
import clientside.MonsterBook;
import clientside.MonsterFamiliar;
import clientside.PlayerStats;
import clientside.Skill;
import clientside.SkillEntry;
import client.inventory.Item;
import client.inventory.MapleImp;
import client.inventory.MapleImp.ImpFlag;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import constants.GameConstants;
import handling.SendPacketOpcode;
import handling.channel.MapleGuildRanking;
import handling.channel.MapleGuildRanking.GuildRankingInfo;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.World;
import handling.world.World.Alliance;
import handling.world.World.Family;
import handling.world.World.Guild;
import handling.world.World.Party;
import handling.world.exped.ExpeditionType;
import handling.world.exped.MapleExpedition;
import handling.world.exped.PartySearch;
import handling.world.exped.PartySearchType;
import handling.world.family.MapleFamily;
import handling.world.family.MapleFamilyBuff;
import handling.world.family.MapleFamilyCharacter;
import handling.world.guild.MapleBBSThread;
import handling.world.guild.MapleBBSThread.MapleBBSReply;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;
import handling.world.guild.MapleGuildCharacter;
import handling.world.guild.MapleGuildSkill;
import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.StructFamiliar;
import server.life.PlayerNPC;
import server.maps.MapleMap;
import server.quest.MapleQuest;
import server.shops.HiredMerchant;
import server.shops.MaplePlayerShopItem;
import tools.HexTool;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

public class CWvsContext {

    public static byte[] enableActions() {
        return updatePlayerStats(new EnumMap(MapleStat.class), true, null);
    }
    
    public static byte[] updatePlayerStats(Map<MapleStat, Long> stats, MapleCharacter chr) {
        return updatePlayerStats(stats, false, chr);
    }

public static byte[] updatePlayerStats(final Map<MapleStat, Long> mystats, final boolean itemReaction, final MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(itemReaction ? 1 : 0);
        long updateMask = 0L;
        for (MapleStat statupdate : mystats.keySet()) {
            updateMask |= statupdate.getValue();
        }
        mplew.writeLong(updateMask);
        for (final Entry<MapleStat, Long> statupdate : mystats.entrySet()) {
            switch (statupdate.getKey()) {
                case SKIN:
                case LEVEL:
                case FATIGUE:
                case BATTLE_RANK:
                case ICE_GAGE:
                    mplew.write((statupdate.getValue()).byteValue());
                    break;
//                case JOB:
                case STR:
                case DEX:
                case INT:
                case LUK:
                case AVAILABLEAP:
                    mplew.writeShort((statupdate.getValue()).shortValue());
                    break;
                case AVAILABLESP:
                    if (GameConstants.isSeparatedSp(chr.getJob())) {
                        mplew.write(chr.getRemainingSpSize());
                        for (int i = 0; i < chr.getRemainingSps().length; i++) {
                            if (chr.getRemainingSp(i) > 0) {
                                mplew.write(i + 1);
                                mplew.writeInt(chr.getRemainingSp(i));
                            }
                        }
                    } else {
                        mplew.writeShort(chr.getRemainingSp());
                    }
                    break;
                case TRAIT_LIMIT:
                    mplew.writeInt((statupdate.getValue()).intValue());
                    mplew.writeInt((statupdate.getValue()).intValue());
                    mplew.writeInt((statupdate.getValue()).intValue());
                    break;
                case EXP:
                case MESO:
                    mplew.writeLong((statupdate.getValue()).longValue());
                    break;
                case PET:
                    mplew.writeLong((statupdate.getValue()).intValue());
                    mplew.writeLong((statupdate.getValue()).intValue());
                    mplew.writeLong((statupdate.getValue()).intValue());
                    break;
                case BATTLE_POINTS:
                case VIRTUE:
                    mplew.writeLong((statupdate.getValue()).longValue());
                    break;
                default:
                    mplew.writeInt((statupdate.getValue()).intValue());
            }
        }

        if ((updateMask == 0L) && (!itemReaction)) {
            mplew.write(1);
        }
        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
    }

    public static byte[] temporaryStats_Aran() {
        Map stats = new EnumMap(MapleStat.Temp.class);

        stats.put(MapleStat.Temp.STR, Integer.valueOf(999));
        stats.put(MapleStat.Temp.DEX, Integer.valueOf(999));
        stats.put(MapleStat.Temp.INT, Integer.valueOf(999));
        stats.put(MapleStat.Temp.LUK, Integer.valueOf(999));
        stats.put(MapleStat.Temp.WATK, Integer.valueOf(255));
        stats.put(MapleStat.Temp.ACC, Integer.valueOf(999));
        stats.put(MapleStat.Temp.AVOID, Integer.valueOf(999));
        stats.put(MapleStat.Temp.SPEED, Integer.valueOf(140));
        stats.put(MapleStat.Temp.JUMP, Integer.valueOf(120));

        return temporaryStats(stats);
    }

    public static byte[] temporaryStats_Balrog(MapleCharacter chr) {
        Map stats = new EnumMap(MapleStat.Temp.class);

        int offset = 1 + (chr.getLevel() - 90) / 20;
        stats.put(MapleStat.Temp.STR, Integer.valueOf(chr.getStat().getTotalStr() / offset));
        stats.put(MapleStat.Temp.DEX, Integer.valueOf(chr.getStat().getTotalDex() / offset));
        stats.put(MapleStat.Temp.INT, Integer.valueOf(chr.getStat().getTotalInt() / offset));
        stats.put(MapleStat.Temp.LUK, Integer.valueOf(chr.getStat().getTotalLuk() / offset));
        stats.put(MapleStat.Temp.WATK, Integer.valueOf(chr.getStat().getTotalWatk() / offset));
        stats.put(MapleStat.Temp.MATK, Integer.valueOf(chr.getStat().getTotalMagic() / offset));

        return temporaryStats(stats);
    }

    public static byte[] temporaryStats(Map<MapleStat.Temp, Integer> mystats) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TEMP_STATS.getValue());
        int updateMask = 0;
        for (MapleStat.Temp statupdate : mystats.keySet()) {
            updateMask |= statupdate.getValue();
        }
        mplew.writeInt(updateMask);
        for (final Entry<MapleStat.Temp, Integer> statupdate : mystats.entrySet()) {
            switch (statupdate.getKey()) {
                case SPEED:
                case JUMP:
                case UNKNOWN:
                    mplew.write(((Integer) statupdate.getValue()).byteValue());
                    break;
                default:
                    mplew.writeShort(((Integer) statupdate.getValue()).shortValue());
            }

        }

        return mplew.getPacket();
    }

    public static byte[] temporaryStats_Reset() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TEMP_STATS_RESET.getValue());

        return mplew.getPacket();
    }

    public static byte[] updateSkills(Map<Skill, SkillEntry> update) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(7 + update.size() * 20);

        mplew.writeShort(SendPacketOpcode.UPDATE_SKILLS.getValue());
        mplew.write(1);
        mplew.write(0);
        mplew.writeShort(update.size());
        for (Map.Entry z : update.entrySet()) {
            mplew.writeInt(((Skill) z.getKey()).getId());
            mplew.writeInt(((SkillEntry) z.getValue()).skillevel);
            mplew.writeInt(((SkillEntry) z.getValue()).masterlevel);
            PacketHelper.addExpirationTime(mplew, ((SkillEntry) z.getValue()).expiration);
        }
        mplew.write(4);

        return mplew.getPacket();
    }

    public static byte[] giveFameErrorResponse(int op) {
        return OnFameResult(op, null, true, 0);
    }

    public static byte[] OnFameResult(int op, String charname, boolean raise, int newFame) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(op);
        if ((op == 0) || (op == 5)) {
            mplew.writeMapleAsciiString(charname == null ? "" : charname);
            mplew.write(raise ? 1 : 0);
            if (op == 0) {
                mplew.writeInt(newFame);
            }
        }

        return mplew.getPacket();
    }

    public static byte[] BombLieDetector(boolean error, int mapid, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOMB_LIE_DETECTOR.getValue());
        mplew.write(error ? 2 : 1);
        mplew.writeInt(mapid);
        mplew.writeInt(channel);

        return mplew.getPacket();
    }

    public static byte[] report(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.REPORT_RESPONSE.getValue());
        mplew.write(mode);
        if (mode == 2) {
            mplew.write(0);
            mplew.writeInt(1);
        }

        return mplew.getPacket();
    }

    public static byte[] OnSetClaimSvrAvailableTime(int from, int to) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4);

        mplew.writeShort(SendPacketOpcode.REPORT_TIME.getValue());
        mplew.write(from);
        mplew.write(to);

        return mplew.getPacket();
    }

    public static byte[] OnClaimSvrStatusChanged(boolean enable) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.writeShort(SendPacketOpcode.REPORT_STATUS.getValue());
        mplew.write(enable ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] updateMount(MapleCharacter chr, boolean levelup) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_MOUNT.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt(chr.getMount().getFatigue());
        mplew.write(levelup ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] getShowQuestCompletion(int id) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_QUEST_COMPLETION.getValue());
        mplew.writeShort(id);

        return mplew.getPacket();
    }

    public static byte[] useSkillBook(MapleCharacter chr, int skillid, int maxlevel, boolean canuse, boolean success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.USE_SKILL_BOOK.getValue());
        mplew.write(0);
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeInt(maxlevel);
        mplew.write(canuse ? 1 : 0);
        mplew.write(success ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] useAPSPReset(boolean spReset, int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(spReset ? SendPacketOpcode.SP_RESET.getValue() : SendPacketOpcode.AP_RESET.getValue());
        mplew.write(1);
        mplew.writeInt(cid);
        mplew.write(1);

        return mplew.getPacket();
    }

    public static byte[] expandCharacterSlots(int mode) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.EXPAND_CHARACTER_SLOTS.getValue());
        mplew.writeInt(mode);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] finishedGather(int type) {
        return gatherSortItem(true, type);
    }

    public static byte[] finishedSort(int type) {
        return gatherSortItem(false, type);
    }

    public static byte[] gatherSortItem(boolean gather, int type) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(gather ? SendPacketOpcode.FINISH_GATHER.getValue() : SendPacketOpcode.FINISH_SORT.getValue());
        mplew.write(1);
        mplew.write(type);

        return mplew.getPacket();
    }

    public static byte[] updateGender(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_GENDER.getValue());
        mplew.write(chr.getGender());

        return mplew.getPacket();
    }

    public static byte[] charInfo(MapleCharacter chr, boolean isSelf) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CHAR_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob());
        mplew.writeShort(chr.getSubcategory());
        mplew.write(chr.getStat().pvpRank);
        mplew.writeInt(chr.getFame());
        mplew.write(0);
        List<?> prof = chr.getProfessions();
        mplew.write(prof.size());
        for (Iterator<?> i$ = prof.iterator(); i$.hasNext();) {
            int i = ((Integer) i$.next()).intValue();
            mplew.writeShort(i);
        }
        if (chr.getGuildId() <= 0) {
            mplew.writeMapleAsciiString("-");
            mplew.writeMapleAsciiString("");
        } else {
            MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
                if (gs.getAllianceId() > 0) {
                    MapleGuildAlliance allianceName = World.Alliance.getAlliance(gs.getAllianceId());
                    if (allianceName != null) {
                        mplew.writeMapleAsciiString(allianceName.getName());
                    } else {
                        mplew.writeMapleAsciiString("");
                    }
                } else {
                    mplew.writeMapleAsciiString("");
                }
            } else {
                mplew.writeMapleAsciiString("-");
                mplew.writeMapleAsciiString("");
            }
        }

        mplew.write(isSelf ? 1 : 0);
        mplew.write(0);
        byte index = 1;
        for (MaplePet pet : chr.getPets()) {
            if (pet.getSummoned()) {
                mplew.write(index);
                mplew.writeInt(pet.getPetItemId());
                mplew.writeMapleAsciiString(pet.getName());
                mplew.write(30);
                mplew.writeShort(30000);
                mplew.write(100);
                //      mplew.write(pet.getLevel());
                //    mplew.writeShort(pet.getCloseness());
                //  mplew.write(pet.getFullness());
                mplew.writeShort(0);
                Item inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) (byte) (index == 1 ? -114 : index == 2 ? -130 : -138));
                mplew.writeInt(inv == null ? 0 : inv.getItemId());
                mplew.writeInt(-1);
                index = (byte) (index + 1);
            }
        }
        mplew.write(0);

        if ((chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -18) != null) && (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -19) != null)) {
            MapleMount mount = chr.getMount();
            mplew.write(1);
            mplew.writeInt(mount.getLevel());
            mplew.writeInt(mount.getExp());
            mplew.writeInt(mount.getFatigue());
        } else {
            mplew.write(0);
        }

        int wishlistSize = chr.getWishlistSize();
        mplew.write(wishlistSize);
        if (wishlistSize > 0) {
            int[] wishlist = chr.getWishlist();
            for (int x = 0; x < wishlistSize; x++) {
                mplew.writeInt(wishlist[x]);
            }
        }

        Item medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((byte) -46);
        mplew.writeInt(medal == null ? 0 : medal.getItemId());
        List<Pair<Integer, Long>> medalQuests = chr.getCompletedMedals();
        mplew.writeShort(medalQuests.size());
        for (Pair<Integer, Long> x : medalQuests) {
            mplew.writeShort(x.left);
            mplew.writeLong(x.right); // Gain Filetime 
        }

        for (MapleTraitType t : MapleTraitType.values()) {
            mplew.write(chr.getTrait(t).getLevel());
        }

        mplew.writeInt(0);
        mplew.writeMapleAsciiString("Creating...");
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeZeroBytes(33);
        //mplew.write(HexTool.hex("81 FC 02 00 15 00 00 00 98 0D 00 00 58 69 00 00 00 00 00 00 02 00 00 00 00 09 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00"));

//        mplew.writeZeroBytes(17);
//        mplew.writeZeroBytes(29);
//        mplew.write(1);
//        mplew.writeZeroBytes(11);

        List<Integer> chairs = new ArrayList<>();
        for (Item i : chr.getInventory(MapleInventoryType.SETUP).newList()) {
            if (i.getItemId() / 10000 == 301 && !chairs.contains(i.getItemId())) {
                chairs.add(i.getItemId());
            }
        }
        mplew.writeInt(chairs.size());
        for (int i : chairs) {
            mplew.writeInt(i);
        }

        return mplew.getPacket();
    }

    public static byte[] getMonsterBookInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOOK_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getLevel());
        chr.getMonsterBook().writeCharInfoPacket(mplew);

        return mplew.getPacket();
    }

    public static byte[] spawnPortal(int townId, int targetId, int skillId, Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SPAWN_PORTAL.getValue());
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        if ((townId != 999999999) && (targetId != 999999999)) {
            mplew.writeInt(skillId);
            mplew.writePos(pos);
        }

        return mplew.getPacket();
    }

    public static byte[] mechPortal(Point pos) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MECH_PORTAL.getValue());
        mplew.writePos(pos);

        return mplew.getPacket();
    }

    public static byte[] echoMegaphone(String name, String message) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ECHO_MESSAGE.getValue());
        mplew.write(0);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(message);

        return mplew.getPacket();
    }

    public static byte[] showQuestMsg(String msg) {
        return serverNotice(5, msg);
    }

    public static byte[] Mulung_Pts(int recv, int total) {
        return showQuestMsg(new StringBuilder().append("You have received ").append(recv).append(" training points, for the accumulated total of ").append(total).append(" training points.").toString());
    }

    public static byte[] serverMessage(String message) {
        return serverMessage(4, 0, message, false);
    }

    public static byte[] serverNotice(int type, String message) {
        return serverMessage(type, 0, message, false);
    }

    public static byte[] serverNotice(int type, int channel, String message) {
        return serverMessage(type, channel, message, false);
    }

    public static byte[] serverNotice(int type, int channel, String message, boolean smegaEar) {
        return serverMessage(type, channel, message, smegaEar);
    }

    private static byte[] serverMessage(int type, int channel, String message, boolean megaEar) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        if (type == 4) {
            mplew.write(1);
        }
        if ((type != 23) && (type != 24)) {
            mplew.writeMapleAsciiString(message);
        }
        switch (type) {
            case 3:
            case 22:
            case 25:
            case 26:
                mplew.write(channel - 1);
                mplew.write(megaEar ? 1 : 0);
                break;
            case 9:
                mplew.write(channel - 1);
                break;
            case 12:
                mplew.writeInt(channel);
                break;
            case 6:
            case 11:
            case 20:
                mplew.writeInt((channel >= 1000000) && (channel < 6000000) ? channel : 0);

                break;
            case 24:
                mplew.writeShort(0);
            case 4:
            case 5:
            case 7:
            case 8:
            case 10:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 21:
            case 23:
        }
        return mplew.getPacket();
    }

    public static byte[] getGachaponMega(String name, String message, Item item, byte rareness, String gacha) {
        return getGachaponMega(name, message, item, rareness, false, gacha);
    }

    public static byte[] getGachaponMega(String name, String message, Item item, byte rareness, boolean dragon, String gacha) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(13);
        mplew.writeMapleAsciiString(new StringBuilder().append(name).append(message).toString());
        if (!dragon) {
            mplew.writeInt(0);
            mplew.writeInt(item.getItemId());
        }
        mplew.writeMapleAsciiString(gacha);
        PacketHelper.addItemInfo(mplew, item);

        return mplew.getPacket();
    }

    public static byte[] getAniMsg(int questID, int time) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(23);
        mplew.writeShort(questID);
        mplew.writeInt(time);

        return mplew.getPacket();
    }

    public static byte[] tripleSmega(List<String> message, boolean ear, int channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(10);
        if (message.get(0) != null) {
            mplew.writeMapleAsciiString((String) message.get(0));
        }
        mplew.write(message.size());
        for (int i = 1; i < message.size(); i++) {
            if (message.get(i) != null) {
                mplew.writeMapleAsciiString((String) message.get(i));
            }
        }
        mplew.write(channel - 1);
        mplew.write(ear ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] itemMegaphone(String msg, boolean whisper, int channel, Item item) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(8);
        mplew.writeMapleAsciiString(msg);
        mplew.write(channel - 1);
        mplew.write(whisper ? 1 : 0);
        PacketHelper.addItemPosition(mplew, item, true, false);
        if (item != null) {
            PacketHelper.addItemInfo(mplew, item);
        }

        return mplew.getPacket();
    }

    public static byte[] getPeanutResult(int itemId, short quantity, int itemId2, short quantity2, int ourItem) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PIGMI_REWARD.getValue());
        mplew.writeInt(itemId);
        mplew.writeShort(quantity);
        mplew.writeInt(ourItem);
        mplew.writeInt(itemId2);
        mplew.writeInt(quantity2);

        return mplew.getPacket();
    }

    public static byte[] getOwlOpen() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
        mplew.write(9);
        mplew.write(GameConstants.owlItems.length);
        for (int i : GameConstants.owlItems) {
            mplew.writeInt(i);
        }

        return mplew.getPacket();
    }

    public static byte[] getOwlSearched(int itemSearch, List<HiredMerchant> hms) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.OWL_OF_MINERVA.getValue());
        mplew.write(8);
        mplew.writeInt(0);
        mplew.writeInt(itemSearch);
        int size = 0;

        for (HiredMerchant hm : hms) {
            size += hm.searchItem(itemSearch).size();
        }

        mplew.writeInt(size);
        for (HiredMerchant hm : hms) {
            /* 3342 */ for (Iterator i = hms.iterator(); i.hasNext();) {
                hm = (HiredMerchant) i.next();
                final List<MaplePlayerShopItem> items = hm.searchItem(itemSearch);
                for (MaplePlayerShopItem item : items) {
                    mplew.writeMapleAsciiString(hm.getOwnerName());
                    mplew.writeInt(hm.getMap().getId());
                    mplew.writeMapleAsciiString(hm.getDescription());
                    mplew.writeInt(item.item.getQuantity());
                    mplew.writeInt(item.bundles);
                    mplew.writeInt(item.price);
                    switch (2) {
                        case 0:
                            mplew.writeInt(hm.getOwnerId());
                            break;
                        case 1:
                            mplew.writeInt(hm.getStoreId());
                            break;
                        default:
                            mplew.writeInt(hm.getObjectId());
                    }

                    mplew.write(hm.getFreeSlot() == -1 ? 1 : 0);
                    mplew.write(GameConstants.getInventoryType(itemSearch).getType());
                    if (GameConstants.getInventoryType(itemSearch) == MapleInventoryType.EQUIP) {
                        PacketHelper.addItemInfo(mplew, item.item);
                    }
                }
            }
        }
        HiredMerchant hm;
        Iterator i;
        return mplew.getPacket();
    }

    public static byte[] getOwlMessage(int msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.writeShort(SendPacketOpcode.OWL_RESULT.getValue());
        mplew.write(msg);

        return mplew.getPacket();
    }

    public static byte[] sendEngagementRequest(String name, int cid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ENGAGE_REQUEST.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(name);
        mplew.writeInt(cid);

        return mplew.getPacket();
    }

    public static byte[] sendEngagement(byte msg, int item, MapleCharacter male, MapleCharacter female) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ENGAGE_RESULT.getValue());
        mplew.write(msg);
        if ((msg == 11) || (msg == 12)) {
            mplew.writeInt(0);
            mplew.writeInt(male.getId());
            mplew.writeInt(female.getId());
            mplew.writeShort(1);
            mplew.writeInt(item);
            mplew.writeInt(item);
            mplew.writeAsciiString(male.getName(), 13);
            mplew.writeAsciiString(female.getName(), 13);
        } else if (msg == 15) {
            mplew.writeAsciiString("Male", 13);
            mplew.writeAsciiString("Female", 13);
            mplew.writeShort(0);
        }

        return mplew.getPacket();
    }

    public static byte[] sendWeddingGive() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WEDDING_GIFT.getValue());
        mplew.write(9);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] sendWeddingReceive() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WEDDING_GIFT.getValue());
        mplew.write(10);
        mplew.writeLong(-1L);
        mplew.writeInt(0);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] giveWeddingItem() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WEDDING_GIFT.getValue());
        mplew.write(11);
        mplew.write(0);
        mplew.writeLong(0L);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] receiveWeddingItem() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.WEDDING_GIFT.getValue());
        mplew.write(15);
        mplew.writeLong(0L);
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] sendCashPetFood(boolean success, byte index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3 + (success ? 1 : 0));

        mplew.writeShort(SendPacketOpcode.USE_CASH_PET_FOOD.getValue());
        mplew.write(success ? 0 : 1);
        if (success) {
            mplew.write(index);
        }

        return mplew.getPacket();
    }

    public static byte[] yellowChat(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.YELLOW_CHAT.getValue());
        mplew.write(-1);
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] shopDiscount(int percent) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOP_DISCOUNT.getValue());
        mplew.write(percent);

        return mplew.getPacket();
    }

    public static byte[] catchMob(int mobid, int itemid, byte success) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CATCH_MOB.getValue());
        mplew.write(success);
        mplew.writeInt(itemid);
        mplew.writeInt(mobid);

        return mplew.getPacket();
    }

    public static byte[] spawnPlayerNPC(PlayerNPC npc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PLAYER_NPC.getValue());
        mplew.write(1);
        mplew.writeInt(npc.getId());
        mplew.writeMapleAsciiString(npc.getName());
        PacketHelper.addCharLook(mplew, npc, true);

        return mplew.getPacket();
    }

    public static byte[] disabledNPC(List<Integer> ids) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3 + ids.size() * 4);

        mplew.writeShort(SendPacketOpcode.DISABLE_NPC.getValue());
        mplew.write(ids.size());
        for (Integer i : ids) {
            mplew.writeInt(i.intValue());
        }

        return mplew.getPacket();
    }

    public static byte[] getCard(int itemid, int level) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GET_CARD.getValue());
        mplew.write(itemid > 0 ? 1 : 0);
        if (itemid > 0) {
            mplew.writeInt(itemid);
            mplew.writeInt(level);
        }
        return mplew.getPacket();
    }

    public static byte[] changeCardSet(int set) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CARD_SET.getValue());
        mplew.writeInt(set);

        return mplew.getPacket();
    }

    public static byte[] upgradeBook(Item book, MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.BOOK_STATS.getValue());
        mplew.writeInt(book.getPosition());
        PacketHelper.addItemInfo(mplew, book, chr);

        return mplew.getPacket();
    }

    public static byte[] getCardDrops(int cardid, List<Integer> drops) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CARD_DROPS.getValue());
        mplew.writeInt(cardid);
        mplew.writeShort(drops == null ? 0 : drops.size());
        if (drops != null) {
            for (Integer de : drops) {
                mplew.writeInt(de.intValue());
            }
        }

        return mplew.getPacket();
    }

    public static byte[] getFamiliarInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FAMILIAR_INFO.getValue());
        mplew.writeInt(chr.getFamiliars().size());
        for (MonsterFamiliar mf : chr.getFamiliars().values()) {
            mf.writeRegisterPacket(mplew, true);
        }
        List<Pair<Integer, Long>> size = new ArrayList<>();
        for (Item i : chr.getInventory(MapleInventoryType.USE).list()) {
            if (i.getItemId() / 10000 == 287) {
                StructFamiliar f = MapleItemInformationProvider.getInstance().getFamiliarByItem(i.getItemId());
                if (f != null) {
                    size.add(new Pair(Integer.valueOf(f.familiar), Long.valueOf(i.getInventoryId())));
                }
            }
        }
        mplew.writeInt(size.size());
        for (Pair s : size) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(((Integer) s.left).intValue());
            mplew.writeLong(((Long) s.right).longValue());
            mplew.write(0);
        }
        size.clear();

        return mplew.getPacket();
    }

    public static byte[] MulungEnergy(int energy) {
        return sendPyramidEnergy("energy", String.valueOf(energy));
    }

    public static byte[] sendPyramidEnergy(String type, String amount) {
        return sendString(1, type, amount);
    }

    public static byte[] sendGhostPoint(String type, String amount) {
        return sendString(2, type, amount);
    }

    public static byte[] sendGhostStatus(String type, String amount) {
        return sendString(3, type, amount);
    }

    public static byte[] sendString(int type, String object, String amount) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        switch (type) {
            case 1:
                mplew.writeShort(SendPacketOpcode.SESSION_VALUE.getValue());
                break;
            case 2:
                mplew.writeShort(SendPacketOpcode.PARTY_VALUE.getValue());
                break;
            case 3:
                mplew.writeShort(SendPacketOpcode.MAP_VALUE.getValue());
        }

        mplew.writeMapleAsciiString(object);
        mplew.writeMapleAsciiString(amount);

        return mplew.getPacket();
    }

    public static byte[] fairyPendantMessage(int termStart, int incExpR) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(14);

        mplew.writeShort(SendPacketOpcode.EXP_BONUS.getValue());
        mplew.writeInt(17);
        mplew.writeInt(0);

        mplew.writeInt(incExpR);

        return mplew.getPacket();
    }

    public static byte[] potionDiscountMessage(int type, int potionDiscR) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(10);

        mplew.writeShort(SendPacketOpcode.POTION_BONUS.getValue());
        mplew.writeInt(type);
        mplew.writeInt(potionDiscR);

        return mplew.getPacket();
    }

    public static byte[] sendLevelup(boolean family, int level, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.LEVEL_UPDATE.getValue());
        mplew.write(family ? 1 : 2);
        mplew.writeInt(level);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] sendMarriage(boolean family, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MARRIAGE_UPDATE.getValue());
        mplew.write(family ? 1 : 0);
        mplew.writeMapleAsciiString(name);

        return mplew.getPacket();
    }

    public static byte[] sendJobup(boolean family, int jobid, String name) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.JOB_UPDATE.getValue());
        mplew.write(family ? 1 : 0);
        mplew.writeInt(jobid);
        mplew.writeMapleAsciiString(new StringBuilder().append(!family ? "> " : "").append(name).toString());

        return mplew.getPacket();
    }

    

    public static byte[] getAvatarMega(MapleCharacter chr, int channel, int itemId, List<String> text, boolean ear) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.AVATAR_MEGA.getValue());
        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(chr.getName());
        for (String i : text) {
            mplew.writeMapleAsciiString(i);
        }
        mplew.writeInt(channel - 1);
        mplew.write(ear ? 1 : 0);
        PacketHelper.addCharLook(mplew, chr, true);

        return mplew.getPacket();
    }

    public static byte[] GMPoliceMessage(boolean dc) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);

        mplew.writeShort(SendPacketOpcode.GM_POLICE.getValue());
        mplew.write(dc ? 10 : 0);

        return mplew.getPacket();
    }
    
        public static byte[] giveDemonWatk(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.write(HexTool.hex("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 03 00 00 00 00 00 65 EB 92 EB 00 00 00 00 00"));
        mplew.writeInt(chr.getStat().getHp());
        mplew.writeZeroBytes(9);
        return mplew.getPacket();
    }

    public static byte[] pendantSlot(boolean p) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PENDANT_SLOT.getValue());
        mplew.write(p ? 1 : 0);
        return mplew.getPacket();
    }

    public static byte[] followRequest(int chrid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FOLLOW_REQUEST.getValue());
        mplew.writeInt(chrid);

        return mplew.getPacket();
    }

    public static byte[] getTopMsg(String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.TOP_MSG.getValue());
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] showMidMsg(String s, int l) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MID_MSG.getValue());
        mplew.write(l);
        mplew.writeMapleAsciiString(s);
        mplew.write(s.length() > 0 ? 0 : 1);

        return mplew.getPacket();
    }

    public static byte[] getMidMsg(String msg, boolean keep, int index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MID_MSG.getValue());
        mplew.write(index);
        mplew.writeMapleAsciiString(msg);
        mplew.write(keep ? 0 : 1);

        return mplew.getPacket();
    }

    public static byte[] clearMidMsg() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.CLEAR_MID_MSG.getValue());

        return mplew.getPacket();
    }

    public static byte[] updateJaguar(MapleCharacter from) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_JAGUAR.getValue());
        PacketHelper.addJaguarInfo(mplew, from);

        return mplew.getPacket();
    }

    public static byte[] loadInformation() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.YOUR_INFORMATION.getValue());
        mplew.write(2);
        mplew.write(0);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(0);

        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] saveInformation(boolean fail) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.YOUR_INFORMATION.getValue());
        mplew.write(4);
        mplew.write(fail ? 0 : 1);

        return mplew.getPacket();
    }

    public static byte[] myInfoResult() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FIND_FRIEND.getValue());
        mplew.write(6);
        mplew.writeInt(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] findFriendResult(List<MapleCharacter> friends) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FIND_FRIEND.getValue());
        mplew.write(8);
        mplew.writeShort(friends.size());
        for (MapleCharacter mc : friends) {
            mplew.writeInt(mc.getId());
            mplew.writeMapleAsciiString(mc.getName());
            mplew.write(mc.getLevel());
            mplew.writeShort(mc.getJob());
            mplew.writeInt(0);
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }

    public static byte[] friendFinderError() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FIND_FRIEND.getValue());
        mplew.write(9);
        mplew.write(12);

        return mplew.getPacket();
    }

    public static byte[] friendCharacterInfo(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.FIND_FRIEND.getValue());
        mplew.write(11);
        mplew.writeInt(chr.getId());
        PacketHelper.addCharLook(mplew, chr, true);

        return mplew.getPacket();
    }

    public static byte[] showBackgroundEffect(String eff, int value) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.VISITOR.getValue());
        mplew.writeMapleAsciiString(eff);
        mplew.write(value);

        return mplew.getPacket();
    }

    public static byte[] sendPinkBeanChoco() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PINKBEAN_CHOCO.getValue());
        mplew.writeInt(0);
        mplew.write(1);
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(0);

        return mplew.getPacket();
    }

    public static byte[] changeChannelMsg(int channel, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(8 + msg.length());

        mplew.writeShort(SendPacketOpcode.AUTO_CC_MSG.getValue());
        mplew.writeInt(channel);
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] pamSongUI() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PAM_SONG.getValue());

        return mplew.getPacket();
    }

    public static byte[] ultimateExplorer() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ULTIMATE_EXPLORER.getValue());

        return mplew.getPacket();
    }

    public static byte[] professionInfo(String skil, int level1, int level2, int chance) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PROFESSION_INFO.getValue());
        mplew.writeMapleAsciiString(skil);
        mplew.writeInt(level1);
        mplew.writeInt(level2);
        mplew.write(1);
        mplew.writeInt((skil.startsWith("9200")) || (skil.startsWith("9201")) ? 100 : chance);

        return mplew.getPacket();
    }

    public static byte[] updateAzwanFame(int level, int fame, boolean levelup) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.AZWAN_FAME.getValue());
        mplew.writeInt(level);
        mplew.writeInt(fame);
        mplew.write(levelup ? 1 : 0);

        return mplew.getPacket();
    }

    public static byte[] updateImpTime() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.UPDATE_IMP_TIME.getValue());
        mplew.writeInt(0);
        mplew.writeLong(0L);

        return mplew.getPacket();
    }

    public static byte[] updateImp(MapleImp imp, int mask, int index, boolean login) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.ITEM_POT.getValue());
        mplew.write(login ? 0 : 1);
        mplew.writeInt(index + 1);
        mplew.writeInt(mask);
        if ((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) {
            Pair i = MapleItemInformationProvider.getInstance().getPot(imp.getItemId());
            if (i == null) {
                return enableActions();
            }
            mplew.writeInt(((Integer) i.left).intValue());
            mplew.write(imp.getLevel());
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.STATE.getValue()) != 0)) {
            mplew.write(imp.getState());
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.FULLNESS.getValue()) != 0)) {
            mplew.writeInt(imp.getFullness());
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.CLOSENESS.getValue()) != 0)) {
            mplew.writeInt(imp.getCloseness());
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.CLOSENESS_LEFT.getValue()) != 0)) {
            mplew.writeInt(1);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.MINUTES_LEFT.getValue()) != 0)) {
            mplew.writeInt(0);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.LEVEL.getValue()) != 0)) {
            mplew.write(1);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.FULLNESS_2.getValue()) != 0)) {
            mplew.writeInt(imp.getFullness());
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.UPDATE_TIME.getValue()) != 0)) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.CREATE_TIME.getValue()) != 0)) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.AWAKE_TIME.getValue()) != 0)) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.SLEEP_TIME.getValue()) != 0)) {
            mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.MAX_CLOSENESS.getValue()) != 0)) {
            mplew.writeInt(100);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.MAX_DELAY.getValue()) != 0)) {
            mplew.writeInt(1000);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.MAX_FULLNESS.getValue()) != 0)) {
            mplew.writeInt(1000);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.MAX_ALIVE.getValue()) != 0)) {
            mplew.writeInt(1);
        }
        if (((mask & MapleImp.ImpFlag.SUMMONED.getValue()) != 0) || ((mask & MapleImp.ImpFlag.MAX_MINUTES.getValue()) != 0)) {
            mplew.writeInt(10);
        }
        mplew.write(0);

        return mplew.getPacket();
    }

    public static byte[] getMulungRanking() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MULUNG_DOJO_RANKING.getValue());
        mplew.writeInt(1);

        mplew.writeShort(1);
        mplew.writeMapleAsciiString("hi");
        mplew.writeLong(2L);

        return mplew.getPacket();
    }

    public static byte[] getMulungMessage(boolean dc, String msg) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MULUNG_MESSAGE.getValue());
        mplew.write(dc ? 1 : 0);
        mplew.writeMapleAsciiString(msg);

        return mplew.getPacket();
    }

    public static byte[] showCardDeck(int cardAmount) {
        MaplePacketLittleEndianWriter writer = new MaplePacketLittleEndianWriter();

        writer.write(cardAmount);
        return writer.getPacket();
    }

    public static byte[] giveKilling(int x) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        PacketHelper.writeSingleMask(mplew, MapleBuffStat.KILL_COUNT);
        mplew.writeZeroBytes(5);
        mplew.writeInt(x);
        mplew.write(0);
        mplew.writeZeroBytes(100);
        return mplew.getPacket();
    }

    public static class InventoryPacket {

        public static byte[] addInventorySlot(MapleInventoryType type, Item item) {
            return addInventorySlot(type, item, false);
        }

        public static byte[] addInventorySlot(MapleInventoryType type, Item item, boolean fromDrop) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(fromDrop ? 1 : 0);
            mplew.write(1);
            mplew.write(1);

            mplew.write(GameConstants.isInBag(item.getPosition(), type.getType()) ? 9 : 0);
            mplew.write(type.getType());
            mplew.writeShort(item.getPosition());
            PacketHelper.addItemInfo(mplew, item);
            mplew.write(0);

            return mplew.getPacket();
        }

        public static byte[] updateInventorySlot(MapleInventoryType type, Item item, boolean fromDrop) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(fromDrop ? 1 : 0);
            mplew.write(1);
            mplew.write(0);

            mplew.write(GameConstants.isInBag(item.getPosition(), type.getType()) ? 6 : 1);
            mplew.write(type.getType());
            mplew.writeShort(item.getPosition());
            mplew.writeShort(item.getQuantity());
            mplew.write(0);

            return mplew.getPacket();
        }

        public static byte[] moveInventoryItem(MapleInventoryType type, short src, short dst, boolean bag, boolean bothBag) {
            return moveInventoryItem(type, src, dst, (byte) -1, bag, bothBag);
        }

        public static byte[] moveInventoryItem(MapleInventoryType type, short src, short dst, short equipIndicator, boolean bag, boolean bothBag) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(1);
            mplew.write(1);
            mplew.write(0);

            mplew.write(bag ? 5 : bothBag ? 8 : 2);
            mplew.write(type.getType());
            mplew.writeShort(src);
            mplew.writeShort(dst);
            if (bag) {
                mplew.writeShort(0);
            }
            if (equipIndicator != -1) {
                mplew.write(equipIndicator);
            }

            return mplew.getPacket();
        }

        public static byte[] moveAndMergeInventoryItem(MapleInventoryType type, short src, short dst, short total, boolean bag, boolean switchSrcDst, boolean bothBag) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(1);
            mplew.write(2);
            mplew.write(0);

            mplew.write((bag) && ((switchSrcDst) || (bothBag)) ? 7 : 3);
            mplew.write(type.getType());
            mplew.writeShort(src);

            mplew.write((bag) && ((!switchSrcDst) || (bothBag)) ? 6 : 1);
            mplew.write(type.getType());
            mplew.writeShort(dst);
            mplew.writeShort(total);

            return mplew.getPacket();
        }

        public static byte[] moveAndMergeWithRestInventoryItem(MapleInventoryType type, short src, short dst, short srcQ, short dstQ, boolean bag, boolean switchSrcDst, boolean bothBag) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(1);
            mplew.write(2);
            mplew.write(0);

            mplew.write((bag) && ((switchSrcDst) || (bothBag)) ? 6 : 1);
            mplew.write(type.getType());
            mplew.writeShort(src);
            mplew.writeShort(srcQ);

            mplew.write((bag) && ((!switchSrcDst) || (bothBag)) ? 6 : 1);
            mplew.write(type.getType());
            mplew.writeShort(dst);
            mplew.writeShort(dstQ);

            return mplew.getPacket();
        }

        public static byte[] clearInventoryItem(MapleInventoryType type, short slot, boolean fromDrop) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(fromDrop ? 1 : 0);
            mplew.write(1);
            mplew.write(0);

            mplew.write((slot > 100) && (type == MapleInventoryType.ETC) ? 7 : 3);
            mplew.write(type.getType());
            mplew.writeShort(slot);

            return mplew.getPacket();
        }

        public static byte[] updateSpecialItemUse(Item item, byte invType, MapleCharacter chr) {
            return updateSpecialItemUse(item, invType, item.getPosition(), false, chr);
        }

        public static byte[] updateSpecialItemUse(Item item, byte invType, short pos, boolean theShort, MapleCharacter chr) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(0);
            mplew.write(2);
            mplew.write(0);

            mplew.write(GameConstants.isInBag(pos, invType) ? 7 : 3);
            mplew.write(invType);
            mplew.writeShort(pos);

            mplew.write(0);
            mplew.write(invType);
            if ((item.getType() == 1) || (theShort)) {
                mplew.writeShort(pos);
            } else {
                mplew.write(pos);
            }
            PacketHelper.addItemInfo(mplew, item, chr);
            if (pos < 0) {
                mplew.write(2);
            }

            return mplew.getPacket();
        }

        public static byte[] updateSpecialItemUse_(Item item, byte invType, MapleCharacter chr) {
            return updateSpecialItemUse_(item, invType, item.getPosition(), chr);
        }

        public static byte[] updateSpecialItemUse_(Item item, byte invType, short pos, MapleCharacter chr) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(0);
            mplew.write(1);
            mplew.write(0);

            mplew.write(0);
            mplew.write(invType);
            if (item.getType() == 1) {
                mplew.writeShort(pos);
            } else {
                mplew.write(pos);
            }
            PacketHelper.addItemInfo(mplew, item, chr);
            if (pos < 0) {
                mplew.write(1);
            }

            return mplew.getPacket();
        }

        public static byte[] scrolledItem(int sucessd) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SCroll_MSG.getValue());
            mplew.writeInt(sucessd);
            return mplew.getPacket();
        }

        public static byte[] scrolledItem(Item scroll, Item item, boolean destroyed, boolean potential) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(1);
            mplew.write(destroyed ? 2 : 3);
            mplew.write(0);

            mplew.write(scroll.getQuantity() > 0 ? 1 : 3);
            mplew.write(GameConstants.getInventoryType(scroll.getItemId()).getType());
            mplew.writeShort(scroll.getPosition());
            if (scroll.getQuantity() > 0) {
                mplew.writeShort(scroll.getQuantity());
            }

            mplew.write(3);
            mplew.write(MapleInventoryType.EQUIP.getType());
            mplew.writeShort(item.getPosition());
            if (!destroyed) {
                mplew.write(0);
                mplew.write(MapleInventoryType.EQUIP.getType());
                mplew.writeShort(item.getPosition());
                PacketHelper.addItemInfo(mplew, item);
            }
            if (!potential) {
                mplew.write(7);
            }
            mplew.write(1);

            return mplew.getPacket();
        }

        public static byte[] moveAndUpgradeItem(MapleInventoryType type, Item item, short oldpos, short newpos, MapleCharacter chr) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(1);
            mplew.write(3);
            mplew.write(0);

            mplew.write(GameConstants.isInBag(newpos, type.getType()) ? 7 : 3);
            mplew.write(type.getType());
            mplew.writeShort(oldpos);

            mplew.write(0);
            mplew.write(1);
            mplew.writeShort(oldpos);
            PacketHelper.addItemInfo(mplew, item, chr);

            mplew.write(2);
            mplew.write(type.getType());
            mplew.writeShort(oldpos);
            mplew.writeShort(newpos);
            mplew.write(0);

            return mplew.getPacket();
        }

        public static byte[] dropInventoryItem(MapleInventoryType type, short src) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(1);
            mplew.write(1);
            mplew.write(0);

            mplew.write(3);
            mplew.write(type.getType());
            mplew.writeShort(src);
            if (src < 0) {
                mplew.write(1);
            }

            return mplew.getPacket();
        }

        public static byte[] dropInventoryItemUpdate(MapleInventoryType type, Item item) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(1);
            mplew.write(1);
            mplew.write(0);

            mplew.write(1);
            mplew.write(type.getType());
            mplew.writeShort(item.getPosition());
            mplew.writeShort(item.getQuantity());

            return mplew.getPacket();
        }

        public static byte[] getInventoryFull() {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(1);
            mplew.write(0);
            mplew.write(0);

            return mplew.getPacket();
        }

        public static byte[] getInventoryStatus() {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_OPERATION.getValue());
            mplew.write(0);
            mplew.write(0);
            mplew.write(0);

            return mplew.getPacket();
        }

        public static byte[] getSlotUpdate(byte invType, byte newSlots) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.INVENTORY_GROW.getValue());
            mplew.write(invType);
            mplew.write(newSlots);

            return mplew.getPacket();
        }

        public static byte[] getShowInventoryFull() {
            return CWvsContext.InfoPacket.getShowInventoryStatus(255);
        }

        public static byte[] showItemUnavailable() {
            return CWvsContext.InfoPacket.getShowInventoryStatus(254);
        }
    }

    public static class BuffPacket {

        public static byte[] giveDice(int buffid, int skillid, int duration, Map<MapleBuffStat, Integer> statups) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
            PacketHelper.writeBuffMask(mplew, statups);

            mplew.writeShort(Math.max(buffid / 100, Math.max(buffid / 10, buffid % 10)));
            mplew.writeInt(skillid);
            mplew.writeInt(duration);
            mplew.writeZeroBytes(5);
            mplew.writeInt(GameConstants.getDiceStat(buffid, 3));
            mplew.writeInt(GameConstants.getDiceStat(buffid, 3));
            mplew.writeInt(GameConstants.getDiceStat(buffid, 4));
            mplew.writeZeroBytes(20);
            mplew.writeInt(GameConstants.getDiceStat(buffid, 2));
            mplew.writeZeroBytes(12);
            mplew.writeInt(GameConstants.getDiceStat(buffid, 5));
            mplew.writeZeroBytes(16);
            mplew.writeInt(GameConstants.getDiceStat(buffid, 6));
            mplew.writeZeroBytes(16);
            mplew.writeInt(1000);
            mplew.writeZeroBytes(100);
            return mplew.getPacket();
        }

        public static byte[] giveHoming(int skillid, int mobid, int x) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
            PacketHelper.writeSingleMask(mplew, MapleBuffStat.HOMING_BEACON);
            mplew.writeShort(0);
            mplew.write(0);
            mplew.writeInt(1);
            mplew.writeLong(skillid);
            mplew.write(0);
            mplew.writeLong(mobid);
            mplew.writeShort(0);
            mplew.writeShort(0);
            mplew.write(0);
            mplew.write(0);
            return mplew.getPacket();
        }

        public static byte[] giveMount(int buffid, int skillid, Map<MapleBuffStat, Integer> statups) {

            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            // if (cid == -1) {
            mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
            // } else {
            //mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
            //  mplew.writeInt(cid);
            // }
            //   mplew.write(HexTool.hex("00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A 00 00 00 0A 00 00 00"));
            PacketHelper.writeBuffMask(mplew, statups);
//            mplew.writeZeroBytes(cid == -1 ? 13 : 27);
            mplew.writeInt(0);
            mplew.writeInt(10);
            mplew.writeInt(10);
            mplew.writeInt(skillid);
            mplew.write(HexTool.hex("00 C2 EB 0B"));
            mplew.writeInt(10);
            mplew.writeInt(skillid);
            mplew.write(HexTool.hex("00 C2 EB 0B"));
            mplew.writeZeroBytes(5);
            mplew.writeInt(buffid);
            mplew.writeInt(skillid);
            mplew.writeZeroBytes(9);
            mplew.write(1);
            mplew.write(6);
            mplew.writeInt(0);
            mplew.writeZeroBytes(100);
            return mplew.getPacket();
        }

        public static byte[] showMonsterRiding(int cid, Map<MapleBuffStat, Integer> statups, int buffid, int skillId) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
            mplew.writeInt(cid);

            PacketHelper.writeBuffMask(mplew, statups);
            mplew.writeZeroBytes(27);
            mplew.writeInt(buffid);
            mplew.writeInt(skillId);

            mplew.writeZeroBytes(9);
            mplew.write(4);
            return mplew.getPacket();


        }

        public static byte[] givePirate(Map<MapleBuffStat, Integer> statups, int duration, int skillid) {
            return giveForeignPirate(statups, duration, -1, skillid);
        }

        public static byte[] giveForeignPirate(Map<MapleBuffStat, Integer> statups, int duration, int cid, int skillid) {
            boolean infusion = (skillid == 5121009) || (skillid == 15111005);
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
            mplew.writeInt(cid);
            PacketHelper.writeBuffMask(mplew, statups);
            mplew.writeShort(0);
            mplew.write(0);
            for (Integer stat : statups.values()) {
                mplew.writeInt(stat.intValue());
                mplew.writeLong(skillid);
                mplew.writeZeroBytes(infusion ? 6 : 1);
                mplew.writeShort(duration);
            }
            mplew.writeShort(0);
            mplew.writeShort(0);
            mplew.write(1);
            mplew.write(1);
            mplew.writeZeroBytes(20);
            return mplew.getPacket();
        }

///*      */     public static byte[] giveArcane(Map<Integer, Integer> statups, int duration) {
///*  501 */       MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
///*      */ 
///*  503 */       mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
///*  504 */       PacketHelper.writeSingleMask(mplew, MapleBuffStat.ARCANE_AIM);
///*      */ 
///*  506 */       mplew.writeShort(0);
///*  507 */       mplew.write(0);
///*  508 */       mplew.writeInt(statups.size());
///*  509 */       for (Map.Entry stat : statups.entrySet()) {
///*  510 */         mplew.writeInt(((Integer)stat.getKey()).intValue());
///*  511 */         mplew.writeLong(((Integer)stat.getValue()).intValue());
///*  512 */         mplew.writeInt(duration);
///*      */       }
///*  514 */       mplew.writeShort(0);
///*  515 */       mplew.writeShort(0);
///*  516 */       mplew.write(1);
///*  517 */       mplew.write(1);
//mplew.writeZeroBytes(60);
///*  518 */       return mplew.getPacket();
///*      */     }
        public static byte[] giveEnergyChargeTest(int bar, int bufflength) {
            return giveEnergyChargeTest(-1, bar, bufflength);
        }

        public static byte[] giveEnergyChargeTest(int cid, int bar, int bufflength) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            if (cid == -1) {
                mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
            } else {
                mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
                mplew.writeInt(cid);
            }
            PacketHelper.writeSingleMask(mplew, MapleBuffStat.ENERGY_CHARGE);
            if (cid == -1) {
                mplew.writeZeroBytes(13);
                mplew.writeInt(Math.min(bar, 10000));
                mplew.writeZeroBytes(9);
                mplew.writeInt(bar >= 10000 ? bufflength : 0);
                mplew.writeShort(0);
                mplew.write(6);
            } else {
                mplew.writeZeroBytes(27);
                mplew.writeInt(Math.min(bar, 10000));
                mplew.writeZeroBytes(9);
                mplew.writeInt(bar >= 10000 ? bufflength : 0);
            }

            return mplew.getPacket();
        }

        public static byte[] giveAriaBuff(int bufflevel, int buffid, int bufflength) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
            mplew.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 80 00 00 00 00 00 00 00 00 00 00 40 00 00 00 00 00 00 00 00 00 00 00 00 00"));
            for (int i = 0; i < 2; i++) {
                mplew.writeShort(bufflevel);
                mplew.writeInt(buffid);
                mplew.writeInt(bufflength);
            }
            mplew.writeZeroBytes(3);
            mplew.writeShort(0);
            mplew.write(0);
            System.out.println("ARIRA_BUFF PACKET: " + mplew.toString());
            return mplew.getPacket();
        }

        public static byte[] giveBuff(int buffid, int bufflength, Map<MapleBuffStat, Integer> statups, MapleStatEffect effect) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
            PacketHelper.writeBuffMask(mplew, statups);
            boolean stacked = false;
            boolean isAura = false;
            boolean iscombo = false;
            for (Map.Entry stat : statups.entrySet()) {
                if ((stat.getKey() == MapleBuffStat.YELLOW_AURA) || (stat.getKey() == MapleBuffStat.BLUE_AURA) || (stat.getKey() == MapleBuffStat.DARK_AURA)) {
                    isAura = true;
                }
                if (stat.getKey() == MapleBuffStat.COMBO) {
                    iscombo = true;
                }

                if (((MapleBuffStat) stat.getKey()).canStack()) {
                    if (!stacked) {
                        mplew.writeZeroBytes(5);
                        stacked = true;
                    }

                    mplew.writeInt(1);
                    mplew.writeInt(buffid);
                    if (buffid == 61111004) {
                        mplew.writeInt(30);
                        mplew.writeInt(1060433976);
                    } else {
                        mplew.writeLong(((Integer) stat.getValue()).longValue());
                    }
                } else {
                    if ((stat.getKey() == MapleBuffStat.SPIRIT_SURGE) || (stat.getKey() == MapleBuffStat.KAISER_COMBO) || (stat.getKey() == MapleBuffStat.Damage_Absorbed) || (stat.getKey() == MapleBuffStat.Crit_Damage) || (stat.getKey() == MapleBuffStat.SHADOWPARTNER)) {
                        mplew.writeInt(((Integer) stat.getValue()).intValue());
                    } else if (statups.containsKey(MapleBuffStat.Tempest_Blades)) {
                        mplew.writeShort(10);
                    } else {
                        mplew.writeShort(((Integer) stat.getValue()).intValue());
                    }
                    mplew.writeInt(buffid);
                }
                mplew.writeInt(bufflength);
                if (statups.containsKey(MapleBuffStat.Tempest_Blades)) {
                    mplew.writeZeroBytes(5);
                    mplew.writeInt(buffid == 61101002 ? 1 : 2);
                    mplew.writeInt(buffid == 61101002 ? 3 : 5);
                    mplew.writeInt(((Integer) stat.getValue()).intValue());
                    mplew.writeInt(buffid == 61101002 ? 3 : 5);
                }
            }
            if (buffid == 24121004) {
                mplew.writeZeroBytes(3);
                mplew.writeShort(0);
                mplew.write(0);
            }
            if (!isAura) {
                mplew.writeShort(0);
                if ((effect != null) && (effect.isDivineShield())) {
                    mplew.writeInt(effect.getEnhancedWatk());
                } else if ((effect != null) && (effect.getCharColor() > 0)) {
                    mplew.writeInt(effect.getCharColor());
                } else if ((effect != null) && (effect.isInflation())) {
                    mplew.writeInt(effect.getInflation());
                }
            }

            mplew.writeShort(0);
            if (buffid == 32110000 || buffid == 32111012 || buffid == 2221054 || buffid == 36121003 || buffid == 11101022 || buffid == 11111022 || buffid == 2311012 || buffid == 100001263) {
                mplew.write(1);
            }
       //        if (buffid == 31011001) {// Overload Release?? idk
        //          mplew.writeInt(effect.getDuration());
           //       JobPacket.AvengerPacket.cancelExceed();
        //        }
               if (buffid == 31211004) {// Recovery
                  mplew.writeInt(effect.getDuration());
                  JobPacket.AvengerPacket.cancelExceed();
                }
            if (buffid == 27111004) {
                mplew.write(0);
                mplew.writeShort(1000);
                mplew.writeShort(0);
            }
            if (buffid == 27110007) {
                mplew.write(0);
                mplew.writeShort(25);
            }
            if (buffid == 27101202) {
                mplew.writeZeroBytes(10);
            }
            if (iscombo) {
                mplew.writeShort(258);
                mplew.writeShort(600);
            } else {
                mplew.write(0);
                mplew.write((effect != null) && (effect.isShadow()) ? 1 : 2);
            }
            if (isAura) {
                mplew.writeInt(0);
            }
            /*  627 */ if (isMovementAffectingStat(statups)) {
                /*  628 */ mplew.write(4);
                /*      */            }
            mplew.writeZeroBytes(200);
            return mplew.getPacket();
        }

        public static byte[] giveBuff2(int buffid, int bufflength, Map<MapleBuffStat, Integer> statups, MapleStatEffect effect) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
            PacketHelper.writeBuffMask(mplew, statups);
            boolean stacked = false;

            for (Map.Entry stat : statups.entrySet()) {


                if (((MapleBuffStat) stat.getKey()).canStack()) {
                    if (!stacked) {
                        mplew.writeZeroBytes(5);
                        stacked = true;
                    }
                    mplew.writeInt(1);
                    mplew.writeInt(buffid);

                    mplew.writeLong(((Integer) stat.getValue()).longValue());

                }
                mplew.writeInt(bufflength);

            }


            mplew.writeShort(0);


            mplew.writeShort(0);


            mplew.write(0);
            mplew.write((effect != null) && (effect.isShadow()) ? 1 : 2);



            mplew.writeZeroBytes(200);
            return mplew.getPacket();
        }

        /*      */ private static boolean isMovementAffectingStat(Map<MapleBuffStat, Integer> statups) /*      */ {
            /*  644 */ return (statups.containsKey(MapleBuffStat.JUMP)) || (statups.containsKey(MapleBuffStat.SPEED)) || (statups.containsKey(MapleBuffStat.MORPH)) || (statups.containsKey(MapleBuffStat.GHOST_MORPH)) || (statups.containsKey(MapleBuffStat.MAPLE_WARRIOR)) || (statups.containsKey(MapleBuffStat.MONSTER_RIDING)) || (statups.containsKey(MapleBuffStat.DASH_SPEED)) || (statups.containsKey(MapleBuffStat.DASH_JUMP)) || (statups.containsKey(MapleBuffStat.SOARING)) || (statups.containsKey(MapleBuffStat.YELLOW_AURA)) || (statups.containsKey(MapleBuffStat.SNATCH)) || (statups.containsKey(MapleBuffStat.ANGEL_SPEED)) || (statups.containsKey(MapleBuffStat.ANGEL_JUMP)) || (statups.containsKey(MapleBuffStat.ENERGY_CHARGE)) || (statups.containsKey(MapleBuffStat.MECH_CHANGE));
            /*      */        }
        /*      */

        public static byte[] giveDebuff(MapleDisease statups, int x, int skillid, int level, int duration) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
            PacketHelper.writeSingleMask(mplew, statups);
            mplew.writeShort(x);
            mplew.writeShort(skillid);
            mplew.writeShort(level);
            mplew.writeInt(duration);
            mplew.writeShort(0);
            mplew.writeShort(0);
            mplew.write(1);
            mplew.write(0);
            mplew.write(1);
            mplew.writeZeroBytes(100);
            return mplew.getPacket();
        }

        public static byte[] cancelRiding() {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());
            mplew.write(HexTool.hex("00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A 00 00 00 04 01"));
            return mplew.getPacket();
        }

        public static byte[] cancelBuff(List<MapleBuffStat> statups) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());

            PacketHelper.writeMask(mplew, statups);
            for (MapleBuffStat z : statups) {
                if (z.canStack()) {
                    mplew.writeInt(0);
                }

            }

            mplew.writeZeroBytes(2);
            mplew.writeLong(0L);
            mplew.writeLong(0L);
            mplew.writeLong(0L);
            mplew.write(0);

            return mplew.getPacket();
        }

        public static byte[] cancelDebuff(MapleDisease mask) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());

            PacketHelper.writeSingleMask(mplew, mask);
            mplew.write(3);
            mplew.write(1);
            mplew.writeLong(0L);
            mplew.write(0);
            return mplew.getPacket();
        }

        public static byte[] cancelHoming() {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.CANCEL_BUFF.getValue());

            PacketHelper.writeSingleMask(mplew, MapleBuffStat.HOMING_BEACON);
            mplew.write(0);

            return mplew.getPacket();
        }

        public static byte[] giveForeignBuff(int cid, Map<MapleBuffStat, Integer> statups, MapleStatEffect effect) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
            mplew.writeInt(cid);

            PacketHelper.writeBuffMask(mplew, statups);
            for (Entry<MapleBuffStat, Integer> statup : statups.entrySet()) {
                if (statup.getKey() == MapleBuffStat.SHADOWPARTNER || statup.getKey() == MapleBuffStat.MECH_CHANGE || statup.getKey() == MapleBuffStat.DARK_AURA || statup.getKey() == MapleBuffStat.YELLOW_AURA || statup.getKey() == MapleBuffStat.BLUE_AURA || statup.getKey() == MapleBuffStat.GIANT_POTION || statup.getKey() == MapleBuffStat.SPIRIT_LINK || statup.getKey() == MapleBuffStat.PYRAMID_PQ || statup.getKey() == MapleBuffStat.WK_CHARGE || statup.getKey() == MapleBuffStat.DAMAGE_R || statup.getKey() == MapleBuffStat.MORPH || statup.getKey() == MapleBuffStat.WATER_SHIELD || statup.getKey() == MapleBuffStat.DARK_METAMORPHOSIS) {
                    mplew.writeShort(statup.getValue().shortValue());
                    mplew.writeInt(effect.isSkill() ? effect.getSourceId() : -effect.getSourceId());
                } else if (statup.getKey() == MapleBuffStat.FAMILIAR_SHADOW) {
                    mplew.writeInt(statup.getValue().intValue());
                    mplew.writeInt(effect.getCharColor());
                } else {
                    mplew.writeShort(statup.getValue().shortValue());
                }
            }
            mplew.writeShort(1);
            mplew.write(0);
            mplew.writeInt(2);
            mplew.writeZeroBytes(13);
            mplew.writeShort(600);

            mplew.writeZeroBytes(20);
            /*
             mplew.writeShort(0);
             mplew.writeShort(0);
             mplew.write(1);
             mplew.write(1);
             mplew.write(0);*///v140
            return mplew.getPacket();
        }

        public static byte[] giveForeignDebuff(int cid, MapleDisease statups, int skillid, int level, int x) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
            mplew.writeInt(cid);

            PacketHelper.writeSingleMask(mplew, statups);
            if (skillid == 125) {
                mplew.writeShort(0);
                mplew.write(0);
            }
            mplew.writeShort(x);
            mplew.writeShort(skillid);
            mplew.writeShort(level);
            mplew.writeShort(0);
            mplew.writeShort(0);
            mplew.write(1);
            mplew.write(1);
            mplew.write(0);
            mplew.writeZeroBytes(20);
            return mplew.getPacket();
        }

        public static byte[] cancelForeignBuff(int cid, List<MapleBuffStat> statups) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
            mplew.writeInt(cid);
            PacketHelper.writeMask(mplew, statups);
            mplew.write(3);
            mplew.write(1);
            mplew.write(0);
            mplew.writeZeroBytes(20);

            return mplew.getPacket();
        }

        public static byte[] cancelForeignDebuff(int cid, MapleDisease mask) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
            mplew.writeInt(cid);

            PacketHelper.writeSingleMask(mplew, mask);
            mplew.write(3);
            mplew.write(1);
            mplew.write(0);
            mplew.writeZeroBytes(20);
            return mplew.getPacket();
        }

        public static byte[] giveCard(int cid, int oid, int skillid) {
            MaplePacketLittleEndianWriter writer = new MaplePacketLittleEndianWriter();
            writer.writeShort(SendPacketOpcode.GAIN_FORCE.getValue());
            writer.write(0);
            writer.writeInt(cid);
            writer.writeInt(1);
            writer.writeInt(oid);
            writer.writeInt(skillid);
            writer.write(1);
            writer.writeInt(2);
            writer.writeInt(1);
            writer.writeInt(21);
            writer.writeInt(8);
            writer.writeInt(8);
            writer.write(0);
            return writer.getPacket();
        }
    }

    public static class InfoPacket {

        public static byte[] showMesoGain(int gain, boolean inChat) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            if (!inChat) {
                mplew.write(0);
                mplew.write(1);
                mplew.write(0);
                mplew.writeInt(gain);
                mplew.writeShort(0);
            } else {
                mplew.write(6);
                mplew.writeInt(gain);
                mplew.writeInt(-1);
            }

            return mplew.getPacket();
        }

        public static byte[] getShowInventoryStatus(int mode) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(0);
            mplew.write(mode);
            mplew.writeInt(0);
            mplew.writeInt(0);

            return mplew.getPacket();
        }

        public static byte[] getShowItemGain(int itemId, short quantity) {
            return getShowItemGain(itemId, quantity, false);
        }

        public static byte[] getShowItemGain(int itemId, short quantity, boolean inChat) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            if (inChat) {
                mplew.writeShort(SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
                mplew.write(5);
                mplew.write(1);
                mplew.writeInt(itemId);
                mplew.writeInt(quantity);
            } else {
                mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
                mplew.writeShort(0);
                mplew.writeInt(itemId);
                mplew.writeInt(quantity);
            }

            return mplew.getPacket();
        }

        public static byte[] updateQuest(MapleQuestStatus quest) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(1);
            mplew.writeShort(quest.getQuest().getId());
            mplew.write(quest.getStatus());
            switch (quest.getStatus()) {
                case 0:
                    mplew.write(0);
                    break;
                case 1:
                    mplew.writeMapleAsciiString(quest.getCustomData() != null ? quest.getCustomData() : "");
                    break;
                case 2:
                    mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
            }

            return mplew.getPacket();
        }

        public static byte[] updateQuestMobKills(MapleQuestStatus status) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(1);
            mplew.writeShort(status.getQuest().getId());
            mplew.write(1);
            StringBuilder sb = new StringBuilder();
            for (Iterator i$ = status.getMobKills().values().iterator(); i$.hasNext();) {
                int kills = ((Integer) i$.next()).intValue();
                sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
            }
            mplew.writeMapleAsciiString(sb.toString());
            mplew.writeLong(0L);

            return mplew.getPacket();
        }

        public static byte[] itemExpired(int itemid) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(2);
            mplew.writeInt(itemid);

            return mplew.getPacket();
        }

        public static byte[] GainEXP_Monster(int gain, boolean white, int partyinc, int Class_Bonus_EXP, int Equipment_Bonus_EXP, int Premium_Bonus_EXP) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(3);
            mplew.write(white ? 1 : 0);
            mplew.writeInt(gain);
            mplew.write(0);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.write(0);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.writeInt(partyinc);

            mplew.writeInt(Equipment_Bonus_EXP);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);

            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(Premium_Bonus_EXP);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            return mplew.getPacket();
        }

        public static byte[] GainEXP_Others(int gain, boolean inChat, boolean white) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(3);
            mplew.write(white ? 1 : 0);
            mplew.writeInt(gain);
            mplew.write(inChat ? 1 : 0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            if (inChat) {
                mplew.writeLong(0L);
            } else {
                mplew.writeShort(0);
                mplew.write(0);
            }
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.writeInt(0);
            mplew.write(0);
            return mplew.getPacket();
        }

        public static byte[] getSPMsg(byte sp, short job) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(4);
            mplew.writeShort(job);
            mplew.write(sp);

            return mplew.getPacket();
        }

        public static byte[] getShowFameGain(int gain) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(5);
            mplew.writeInt(gain);

            return mplew.getPacket();
        }

        public static byte[] getGPMsg(int itemid) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(7);
            mplew.writeInt(itemid);

            return mplew.getPacket();
        }

        public static byte[] getGPContribution(int itemid) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(8);
            mplew.writeInt(itemid);

            return mplew.getPacket();
        }

        public static byte[] getStatusMsg(int itemid) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(9);
            mplew.writeInt(itemid);

            return mplew.getPacket();
        }

        public static byte[] updateInfoQuest(int quest, String data) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(12);
            mplew.writeShort(quest);
            mplew.writeMapleAsciiString(data);

            return mplew.getPacket();
        }

        public static byte[] showItemReplaceMessage(List<String> message) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(14);
            mplew.write(message.size());
            for (String x : message) {
                mplew.writeMapleAsciiString(x);
            }

            return mplew.getPacket();
        }

        public static byte[] showTraitGain(MapleTrait.MapleTraitType trait, int amount) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(16);
            mplew.writeLong(trait.getStat().getValue());
            mplew.writeInt(amount);

            return mplew.getPacket();
        }

        public static byte[] showTraitMaxed(MapleTrait.MapleTraitType trait) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(17);
            mplew.writeLong(trait.getStat().getValue());

            return mplew.getPacket();
        }

        public static byte[] getBPMsg(int amount) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(21);
            mplew.writeInt(amount);
            mplew.writeInt(0);

            return mplew.getPacket();
        }

        public static byte[] showExpireMessage(byte type, List<Integer> item) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(4 + item.size() * 4);

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(type);
            mplew.write(item.size());
            for (Integer it : item) {
                mplew.writeInt(it.intValue());
            }

            return mplew.getPacket();
        }

        public static byte[] showStatusMessage(int mode, String info, String data) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(mode);
            if (mode == 22) {
                mplew.writeMapleAsciiString(info);
                mplew.writeMapleAsciiString(data);
            }

            return mplew.getPacket();
        }

        public static byte[] showReturnStone(int act) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.write(23);
            mplew.write(act);

            return mplew.getPacket();
        }
    }

    public static class GuildPacket {

        public static byte[] guildInvite(int gid, String charName, int levelFrom, int jobFrom) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(5);
            mplew.writeInt(gid);
            mplew.writeMapleAsciiString(charName);
            mplew.writeInt(levelFrom);
            mplew.writeInt(jobFrom);
            mplew.writeInt(0);
            return mplew.getPacket();
        }

        public static byte[] showGuildInfo(MapleCharacter c) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(32);
            if ((c == null) || (c.getMGC() == null)) {
                mplew.write(0);
                return mplew.getPacket();
            }
            MapleGuild g = World.Guild.getGuild(c.getGuildId());
            if (g == null) {
                mplew.write(0);
                return mplew.getPacket();
            }
            mplew.write(1);
            getGuildInfo(mplew, g);

            return mplew.getPacket();
        }

        public static void getGuildInfo(MaplePacketLittleEndianWriter mplew, MapleGuild guild) {
            mplew.writeInt(guild.getId());
            mplew.writeMapleAsciiString(guild.getName());
            for (int i = 1; i <= 5; i++) {
                mplew.writeMapleAsciiString(guild.getRankTitle(i));
            }
            guild.addMemberData(mplew);
            mplew.writeInt(guild.getCapacity());
            mplew.writeShort(guild.getLogoBG());
            mplew.write(guild.getLogoBGColor());
            mplew.writeShort(guild.getLogo());
            mplew.write(guild.getLogoColor());
            mplew.writeMapleAsciiString(guild.getNotice());
            mplew.writeInt(guild.getGP());
            mplew.writeInt(guild.getGP());
            mplew.writeInt(guild.getAllianceId() > 0 ? guild.getAllianceId() : 0);
            mplew.write(guild.getLevel());
            mplew.writeShort(0);
            mplew.writeShort(guild.getSkills().size());
            for (MapleGuildSkill i : guild.getSkills()) {
                mplew.writeInt(i.skillID);
                mplew.writeShort(i.level);
                mplew.writeLong(PacketHelper.getTime(i.timestamp));
                mplew.writeMapleAsciiString(i.purchaser);
                mplew.writeMapleAsciiString(i.activator);
            }
        }

        public static byte[] newGuildInfo(MapleCharacter c) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(38);
            if ((c == null) || (c.getMGC() == null)) {
                return genericGuildMessage((byte) 37);
            }
            MapleGuild g = World.Guild.getGuild(c.getGuildId());
            if (g == null) {
                return genericGuildMessage((byte) 37);
            }
            getGuildInfo(mplew, g);

            return mplew.getPacket();
        }

        public static byte[] newGuildMember(MapleGuildCharacter mgc) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(45);
            mplew.writeInt(mgc.getGuildId());
            mplew.writeInt(mgc.getId());
            mplew.writeAsciiString(mgc.getName(), 13);
            mplew.writeInt(mgc.getJobId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt(mgc.getGuildRank());
            mplew.writeInt(mgc.isOnline() ? 1 : 0);
            mplew.writeInt(mgc.getAllianceRank());
            mplew.writeInt(mgc.getGuildContribution());

            return mplew.getPacket();
        }

        public static byte[] memberLeft(MapleGuildCharacter mgc, boolean bExpelled) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(bExpelled ? 53 : 50);
            mplew.writeInt(mgc.getGuildId());
            mplew.writeInt(mgc.getId());
            mplew.writeMapleAsciiString(mgc.getName());

            return mplew.getPacket();
        }

        public static byte[] guildDisband(int gid) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(56);
            mplew.writeInt(gid);
            mplew.write(1);

            return mplew.getPacket();
        }

        public static byte[] guildCapacityChange(int gid, int capacity) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(64);
            mplew.writeInt(gid);
            mplew.write(capacity);

            return mplew.getPacket();
        }

        public static byte[] guildContribution(int gid, int cid, int c) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(72);
            mplew.writeInt(gid);
            mplew.writeInt(cid);
            mplew.writeInt(c);

            return mplew.getPacket();
        }

        public static byte[] changeRank(MapleGuildCharacter mgc) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(70);
            mplew.writeInt(mgc.getGuildId());
            mplew.writeInt(mgc.getId());
            mplew.write(mgc.getGuildRank());

            return mplew.getPacket();
        }

        public static byte[] rankTitleChange(int gid, String[] ranks) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(68);
            mplew.writeInt(gid);
            for (String r : ranks) {
                mplew.writeMapleAsciiString(r);
            }

            return mplew.getPacket();
        }

        public static byte[] guildEmblemChange(int gid, short bg, byte bgcolor, short logo, byte logocolor) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(73);
            mplew.writeInt(gid);
            mplew.writeShort(bg);
            mplew.write(bgcolor);
            mplew.writeShort(logo);
            mplew.write(logocolor);

            return mplew.getPacket();
        }

        public static byte[] updateGP(int gid, int GP, int glevel) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(79);
            mplew.writeInt(gid);
            mplew.writeInt(GP);
            mplew.writeInt(glevel);

            return mplew.getPacket();
        }

        public static byte[] guildNotice(int gid, String notice) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(75);
            mplew.writeInt(gid);
            mplew.writeMapleAsciiString(notice);

            return mplew.getPacket();
        }

        public static byte[] guildMemberLevelJobUpdate(MapleGuildCharacter mgc) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(66);
            mplew.writeInt(mgc.getGuildId());
            mplew.writeInt(mgc.getId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt(mgc.getJobId());

            return mplew.getPacket();
        }

        public static byte[] guildMemberOnline(int gid, int cid, boolean bOnline) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(67);
            mplew.writeInt(gid);
            mplew.writeInt(cid);
            mplew.write(bOnline ? 1 : 0);

            return mplew.getPacket();
        }

        public static byte[] showGuildRanks(int npcid, List<MapleGuildRanking.GuildRankingInfo> all) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(80);
            mplew.writeInt(npcid);
            mplew.writeInt(all.size());
            for (MapleGuildRanking.GuildRankingInfo info : all) {
                mplew.writeShort(0);
                mplew.writeMapleAsciiString(info.getName());
                mplew.writeInt(info.getGP());
                mplew.writeInt(info.getLogo());
                mplew.writeInt(info.getLogoColor());
                mplew.writeInt(info.getLogoBg());
                mplew.writeInt(info.getLogoBgColor());
            }

            return mplew.getPacket();
        }

        public static byte[] guildSkillPurchased(int gid, int sid, int level, long expiration, String purchase, String activate) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(85);
            mplew.writeInt(gid);
            mplew.writeInt(sid);
            mplew.writeShort(level);
            mplew.writeLong(PacketHelper.getTime(expiration));
            mplew.writeMapleAsciiString(purchase);
            mplew.writeMapleAsciiString(activate);

            return mplew.getPacket();
        }

        public static byte[] guildLeaderChanged(int gid, int oldLeader, int newLeader, int allianceId) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(89);
            mplew.writeInt(gid);
            mplew.writeInt(oldLeader);
            mplew.writeInt(newLeader);
            mplew.write(1);
            mplew.writeInt(allianceId);

            return mplew.getPacket();
        }

        public static byte[] denyGuildInvitation(String charname) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(61);
            mplew.writeMapleAsciiString(charname);

            return mplew.getPacket();
        }

        public static byte[] genericGuildMessage(byte code) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(code);
            if (code == 87) {
                mplew.writeInt(0);
            }
            if ((code == 3) || (code == 59) || (code == 60) || (code == 61) || (code == 84) || (code == 87)) {
                mplew.writeMapleAsciiString("");
            }

            return mplew.getPacket();
        }

        public static byte[] BBSThreadList(List<MapleBBSThread> bbs, int start) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
            mplew.write(6);
            if (bbs == null) {
                mplew.write(0);
                mplew.writeLong(0L);
                return mplew.getPacket();
            }
            int threadCount = bbs.size();
            MapleBBSThread notice = null;
            for (MapleBBSThread b : bbs) {
                if (b.isNotice()) {
                    notice = b;
                    break;
                }
            }
            mplew.write(notice == null ? 0 : 1);
            if (notice != null) {
                addThread(mplew, notice);
            }
            if (threadCount < start) {
                start = 0;
            }
            mplew.writeInt(threadCount);
            int pages = Math.min(10, threadCount - start);
            mplew.writeInt(pages);
            for (int i = 0; i < pages; i++) {
                addThread(mplew, (MapleBBSThread) bbs.get(start + i));
            }

            return mplew.getPacket();
        }

        private static void addThread(MaplePacketLittleEndianWriter mplew, MapleBBSThread rs) {
            mplew.writeInt(rs.localthreadID);
            mplew.writeInt(rs.ownerID);
            mplew.writeMapleAsciiString(rs.name);
            mplew.writeLong(PacketHelper.getKoreanTimestamp(rs.timestamp));
            mplew.writeInt(rs.icon);
            mplew.writeInt(rs.getReplyCount());
        }

        public static byte[] showThread(MapleBBSThread thread) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.BBS_OPERATION.getValue());
            mplew.write(7);
            mplew.writeInt(thread.localthreadID);
            mplew.writeInt(thread.ownerID);
            mplew.writeLong(PacketHelper.getKoreanTimestamp(thread.timestamp));
            mplew.writeMapleAsciiString(thread.name);
            mplew.writeMapleAsciiString(thread.text);
            mplew.writeInt(thread.icon);
            mplew.writeInt(thread.getReplyCount());
            for (MapleBBSThread.MapleBBSReply reply : thread.replies.values()) {
                mplew.writeInt(reply.replyid);
                mplew.writeInt(reply.ownerID);
                mplew.writeLong(PacketHelper.getKoreanTimestamp(reply.timestamp));
                mplew.writeMapleAsciiString(reply.content);
            }

            return mplew.getPacket();
        }
    }

    public static class PartyPacket {

        public static byte[] partyCreated(int partyid) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
            mplew.write(14);
            mplew.writeInt(partyid);
            mplew.writeInt(999999999);
            mplew.writeInt(999999999);
            mplew.writeInt(0);
            mplew.writeShort(0);
            mplew.writeShort(0);
            mplew.write(0);
            mplew.write(1);

            return mplew.getPacket();
        }

        public static byte[] partyInvite(MapleCharacter from) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
            mplew.write(4);
            mplew.writeInt(from.getParty() == null ? 0 : from.getParty().getId());
            mplew.writeMapleAsciiString(from.getName());
            mplew.writeInt(from.getLevel());
            mplew.writeInt(from.getJob());
            mplew.write(0);
            mplew.writeInt(0);
            return mplew.getPacket();
        }

        public static byte[] partyRequestInvite(MapleCharacter from) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
            mplew.write(7);
            mplew.writeInt(from.getId());
            mplew.writeMapleAsciiString(from.getName());
            mplew.writeInt(from.getLevel());
            mplew.writeInt(from.getJob());

            return mplew.getPacket();
        }

        public static byte[] partyStatusMessage(int message, String charname) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
            mplew.write(message);
            if ((message == 30) || (message == 52)) {
                mplew.writeMapleAsciiString(charname);
            } else if (message == 45) {
                mplew.write(0);
            }

            return mplew.getPacket();
        }

        public static void addPartyStatus(int forchannel, MapleParty party, MaplePacketLittleEndianWriter lew, boolean leaving) {
            addPartyStatus(forchannel, party, lew, leaving, false);
        }

        public static void addPartyStatus(int forchannel, MapleParty party, MaplePacketLittleEndianWriter lew, boolean leaving, boolean exped) {
            List<MaplePartyCharacter> partymembers;
            if (party == null) {
                partymembers = new ArrayList();
            } else {
                partymembers = new ArrayList(party.getMembers());
            }
            while (partymembers.size() < 6) {
                partymembers.add(new MaplePartyCharacter());
            }
            for (MaplePartyCharacter partychar : partymembers) {
                lew.writeInt(partychar.getId());
            }
            for (MaplePartyCharacter partychar : partymembers) {
                lew.writeAsciiString(partychar.getName(), 13);
            }
            for (MaplePartyCharacter partychar : partymembers) {
                lew.writeInt(partychar.getJobId());
            }
//            for (MaplePartyCharacter partychar : partymembers) {
////¾ÆÁ÷ ¹ºÁö´Â ¸ð¸£°ÚÀ½
//if (partychar.isOnline()) {
//lew.writeInt(1);
//} else {
//lew.writeInt(0);
//}
//}
            for (MaplePartyCharacter partychar : partymembers) {
                lew.writeInt(0);
            }
            for (MaplePartyCharacter partychar : partymembers) {
                lew.writeInt(partychar.getLevel());
            }
            for (MaplePartyCharacter partychar : partymembers) {
                lew.writeInt(partychar.isOnline() ? partychar.getChannel() - 1 : -2);
            }
            for (MaplePartyCharacter partychar : partymembers) {
                lew.writeInt(0);
            }

            lew.writeInt(party == null ? 0 : party.getLeader().getId());
            if (exped) {
                return;
            }
            for (MaplePartyCharacter partychar : partymembers) {
                lew.writeInt(partychar.getChannel() == forchannel ? partychar.getMapid() : 999999999);
            }
            for (MaplePartyCharacter partychar : partymembers) {
                if ((partychar.getChannel() == forchannel) && (!leaving)) {
                    lew.writeInt(partychar.getDoorTown());
                    lew.writeInt(partychar.getDoorTarget());
                    lew.writeInt(partychar.getDoorSkill());
                    lew.writeInt(partychar.getDoorPosition().x);
                    lew.writeInt(partychar.getDoorPosition().y);
                } else {
                    lew.writeInt(leaving ? 999999999 : 0);
                    lew.writeLong(leaving ? 999999999L : 0L);
                    lew.writeLong(leaving ? -1L : 0L);
                }
            }
            lew.write(1);
        }

        public static byte[] updateParty(int forChannel, MapleParty party, PartyOperation op, MaplePartyCharacter target) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
            switch (op) {
                case DISBAND:
                case EXPEL:
                case LEAVE:
                    mplew.write(18);
                    mplew.writeInt(party.getId());
                    mplew.writeInt(target.getId());
                    mplew.write(op == PartyOperation.DISBAND ? 0 : 1);
                    if (op == PartyOperation.DISBAND) {
                        break;
                    }
                    mplew.write(op == PartyOperation.EXPEL ? 1 : 0);
                    mplew.writeMapleAsciiString(target.getName());
                    addPartyStatus(forChannel, party, mplew, op == PartyOperation.LEAVE);
                    break;
                case JOIN:
                    mplew.write(21);
                    mplew.writeInt(party.getId());
                    mplew.writeMapleAsciiString(target.getName());
                    addPartyStatus(forChannel, party, mplew, false);
                    break;
                case SILENT_UPDATE:
                case LOG_ONOFF:
                    mplew.write(13);
                    mplew.writeInt(party.getId());
                    addPartyStatus(forChannel, party, mplew, op == PartyOperation.LOG_ONOFF);
                    break;
                case CHANGE_LEADER:
                case CHANGE_LEADER_DC:
                    mplew.write(45);
                    mplew.writeInt(target.getId());
                    mplew.write(op == PartyOperation.CHANGE_LEADER_DC ? 1 : 0);
            }
            return mplew.getPacket();
        }

        public static byte[] partyPortal(int townId, int targetId, int skillId, Point position, boolean animation) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
            mplew.write(61);
            mplew.write(animation ? 0 : 1);
            mplew.writeInt(townId);
            mplew.writeInt(targetId);
            mplew.writeInt(skillId);
            mplew.writePos(position);

            return mplew.getPacket();
        }

        public static byte[] getPartyListing(PartySearchType pst) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
            mplew.write(GameConstants.GMS ? 147 : 77);
            mplew.writeInt(pst.id);
            final List<PartySearch> parties = World.Party.searchParty(pst);
            mplew.writeInt(parties.size());
            for (PartySearch party : parties) {
                mplew.writeInt(0);
                mplew.writeInt(2);
                if (pst.exped) {
                    MapleExpedition me = World.Party.getExped(party.getId());
                    mplew.writeInt(me.getType().maxMembers);
                    mplew.writeInt(party.getId());
                    mplew.writeAsciiString(party.getName(), 48);
                    for (int i = 0; i < 5; i++) {
                        if (i < me.getParties().size()) {
                            MapleParty part = World.Party.getParty(((Integer) me.getParties().get(i)).intValue());
                            if (part != null) {
                                addPartyStatus(-1, part, mplew, false, true);
                            } else {
                                mplew.writeZeroBytes(202);
                            }
                        } else {
                            mplew.writeZeroBytes(202);
                        }
                    }
                } else {
                    mplew.writeInt(0);
                    mplew.writeInt(party.getId());
                    mplew.writeAsciiString(party.getName(), 48);
                    addPartyStatus(-1, World.Party.getParty(party.getId()), mplew, false, true);
                }

                mplew.writeShort(0);
            }

            return mplew.getPacket();
        }

        public static byte[] partyListingAdded(PartySearch ps) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.PARTY_OPERATION.getValue());
            mplew.write(93);
            mplew.writeInt(ps.getType().id);
            mplew.writeInt(0);
            mplew.writeInt(1);
            if (ps.getType().exped) {
                MapleExpedition me = World.Party.getExped(ps.getId());
                mplew.writeInt(me.getType().maxMembers);
                mplew.writeInt(ps.getId());
                mplew.writeAsciiString(ps.getName(), 48);
                for (int i = 0; i < 5; i++) {
                    if (i < me.getParties().size()) {
                        MapleParty party = World.Party.getParty(((Integer) me.getParties().get(i)).intValue());
                        if (party != null) {
                            addPartyStatus(-1, party, mplew, false, true);
                        } else {
                            mplew.writeZeroBytes(202);
                        }
                    } else {
                        mplew.writeZeroBytes(202);
                    }
                }
            } else {
                mplew.writeInt(0);
                mplew.writeInt(ps.getId());
                mplew.writeAsciiString(ps.getName(), 48);
                addPartyStatus(-1, World.Party.getParty(ps.getId()), mplew, false, true);
            }
            mplew.writeShort(0);

            return mplew.getPacket();
        }

        public static byte[] showMemberSearch(List<MapleCharacter> chr) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.MEMBER_SEARCH.getValue());
            mplew.write(chr.size());
            for (MapleCharacter c : chr) {
                mplew.writeInt(c.getId());
                mplew.writeMapleAsciiString(c.getName());
                mplew.writeShort(c.getJob());
                mplew.write(c.getLevel());
            }
            return mplew.getPacket();
        }

        public static byte[] showPartySearch(List<MapleParty> chr) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.PARTY_SEARCH.getValue());
            mplew.write(chr.size());
            for (MapleParty c : chr) {
                mplew.writeInt(c.getId());
                mplew.writeMapleAsciiString(c.getLeader().getName());
                mplew.write(c.getLeader().getLevel());
                mplew.write(c.getLeader().isOnline() ? 1 : 0);
                mplew.write(c.getMembers().size());
                for (MaplePartyCharacter ch : c.getMembers()) {
                    mplew.writeInt(ch.getId());
                    mplew.writeMapleAsciiString(ch.getName());
                    mplew.writeShort(ch.getJobId());
                    mplew.write(ch.getLevel());
                    mplew.write(ch.isOnline() ? 1 : 0);
                }
            }
            return mplew.getPacket();
        }
    }

    public static class ExpeditionPacket {

        public static byte[] expeditionStatus(MapleExpedition me, boolean created, boolean silent) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
            mplew.write(silent ? 72 : created ? 74 : 76);
            mplew.writeInt(me.getType().exped);
            mplew.writeInt(0);
            for (int i = 0; i < 6; i++) {
                if (i < me.getParties().size()) {
                    MapleParty party = World.Party.getParty(((Integer) me.getParties().get(i)).intValue());

                    CWvsContext.PartyPacket.addPartyStatus(-1, party, mplew, false, true);
                } else {
                    CWvsContext.PartyPacket.addPartyStatus(-1, null, mplew, false, true);
                }

            }

            return mplew.getPacket();
        }

        public static byte[] expeditionError(int errcode, String name) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
            mplew.write(88);
            mplew.writeInt(errcode);
            mplew.writeMapleAsciiString(name);

            return mplew.getPacket();
        }

        public static byte[] expeditionMessage(int code) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
            mplew.write(code);

            return mplew.getPacket();
        }

        public static byte[] expeditionJoined(String name) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
            mplew.write(75);
            mplew.writeMapleAsciiString(name);

            return mplew.getPacket();
        }

        public static byte[] expeditionLeft(String name) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
            mplew.write(79);
            mplew.writeMapleAsciiString(name);

            return mplew.getPacket();
        }

        public static byte[] expeditionLeaderChanged(int newLeader) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
            mplew.write(84);
            mplew.writeInt(newLeader);

            return mplew.getPacket();
        }

        public static byte[] expeditionUpdate(int partyIndex, MapleParty party) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
            mplew.write(85);
            mplew.writeInt(0);
            mplew.writeInt(partyIndex);

            CWvsContext.PartyPacket.addPartyStatus(-1, party, mplew, false, true);

            return mplew.getPacket();
        }

        public static byte[] expeditionInvite(MapleCharacter from, int exped) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.EXPEDITION_OPERATION.getValue());
            mplew.write(87);
            mplew.writeInt(from.getLevel());
            mplew.writeInt(from.getJob());
            mplew.writeMapleAsciiString(from.getName());
            mplew.writeInt(exped);
            mplew.writeInt(0);
            return mplew.getPacket();
        }
    }

    public static class BuddylistPacket {

        public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist) {
            return updateBuddylist(buddylist, 7);
        }

        public static byte[] updateBuddylist(Collection<BuddylistEntry> buddylist, int deleted) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
            mplew.write(deleted);
            mplew.write(buddylist.size());
            for (BuddylistEntry buddy : buddylist) {
                mplew.writeInt(buddy.getCharacterId());
                mplew.writeAsciiString(buddy.getName(), 13);
                mplew.write(buddy.isVisible() ? 0 : 1);
                mplew.writeInt(buddy.getChannel() == -1 ? -1 : buddy.getChannel() - 1);
                mplew.writeAsciiString(buddy.getGroup(), 17);
            }
            for (int x = 0; x < buddylist.size(); x++) {
                mplew.writeInt(0);
            }

            return mplew.getPacket();
        }

        public static byte[] updateBuddyChannel(int characterid, int channel) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
            mplew.write(20);
            mplew.writeInt(characterid);
            mplew.write(0);
            mplew.writeInt(channel);

            return mplew.getPacket();
        }

        public static byte[] requestBuddylistAdd(int cidFrom, String nameFrom, int levelFrom, int jobFrom) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
            mplew.write(9);
            mplew.writeInt(cidFrom);
            mplew.writeMapleAsciiString(nameFrom);
            mplew.writeInt(levelFrom);
            mplew.writeInt(jobFrom);
            mplew.writeInt(0);
            mplew.writeInt(cidFrom);
            mplew.writeAsciiString(nameFrom, 13);
            mplew.write(1);
            mplew.writeInt(0);
            mplew.writeAsciiString("Group Unknown", 16);
            mplew.writeShort(1);

            return mplew.getPacket();
        }

        public static byte[] updateBuddyCapacity(int capacity) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
            mplew.write(21);
            mplew.write(capacity);

            return mplew.getPacket();
        }

        public static byte[] buddylistMessage(byte message) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.BUDDYLIST.getValue());
            mplew.write(message);

            return mplew.getPacket();
        }
    }

    public static class FamilyPacket {

        public static byte[] getFamilyData() {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.FAMILY.getValue());
            MapleFamilyBuff[] entries = MapleFamilyBuff.values();
            mplew.writeInt(entries.length);

            for (MapleFamilyBuff entry : entries) {
                mplew.write(entry.type);
                mplew.writeInt(entry.rep);
                mplew.writeInt(1);
                mplew.writeMapleAsciiString(entry.name);
                mplew.writeMapleAsciiString(entry.desc);
            }
            return mplew.getPacket();
        }

        public static byte[] getFamilyInfo(MapleCharacter chr) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.OPEN_FAMILY.getValue());
            mplew.writeInt(chr.getCurrentRep());
            mplew.writeInt(chr.getTotalRep());
            mplew.writeInt(chr.getTotalRep());
            mplew.writeShort(chr.getNoJuniors());
            mplew.writeShort(2);
            mplew.writeShort(chr.getNoJuniors());
            MapleFamily family = World.Family.getFamily(chr.getFamilyId());
            if (family != null) {
                mplew.writeInt(family.getLeaderId());
                mplew.writeMapleAsciiString(family.getLeaderName());
                mplew.writeMapleAsciiString(family.getNotice());
            } else {
                mplew.writeLong(0L);
            }
            List b = chr.usedBuffs();
            mplew.writeInt(b.size());
            for (Iterator i$ = b.iterator(); i$.hasNext();) {
                int ii = ((Integer) i$.next()).intValue();
                mplew.writeInt(ii);
                mplew.writeInt(1);
            }

            return mplew.getPacket();
        }

        public static void addFamilyCharInfo(MapleFamilyCharacter ldr, MaplePacketLittleEndianWriter mplew) {
            mplew.writeInt(ldr.getId());
            mplew.writeInt(ldr.getSeniorId());
            mplew.writeShort(ldr.getJobId());
            mplew.writeShort(0);
            mplew.write(ldr.getLevel());
            mplew.write(ldr.isOnline() ? 1 : 0);
            mplew.writeInt(ldr.getCurrentRep());
            mplew.writeInt(ldr.getTotalRep());
            mplew.writeInt(ldr.getTotalRep());
            mplew.writeInt(ldr.getTotalRep());
            mplew.writeInt(Math.max(ldr.getChannel(), 0));
            mplew.writeInt(0);
            mplew.writeMapleAsciiString(ldr.getName());
        }

        public static byte[] getFamilyPedigree(MapleCharacter chr) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.SEND_PEDIGREE.getValue());
            mplew.writeInt(chr.getId());
            MapleFamily family = World.Family.getFamily(chr.getFamilyId());

            int descendants = 2;
            int gens = 0;
            int generations = 0;
            if (family == null) {
                mplew.writeInt(2);
                addFamilyCharInfo(new MapleFamilyCharacter(chr, 0, 0, 0, 0), mplew);
            } else {
                mplew.writeInt(family.getMFC(chr.getId()).getPedigree().size() + 1);
                addFamilyCharInfo(family.getMFC(family.getLeaderId()), mplew);

                if (chr.getSeniorId() > 0) {
                    MapleFamilyCharacter senior = family.getMFC(chr.getSeniorId());
                    if (senior != null) {
                        if (senior.getSeniorId() > 0) {
                            addFamilyCharInfo(family.getMFC(senior.getSeniorId()), mplew);
                        }
                        addFamilyCharInfo(senior, mplew);
                    }
                }
            }
            addFamilyCharInfo(chr.getMFC() == null ? new MapleFamilyCharacter(chr, 0, 0, 0, 0) : chr.getMFC(), mplew);
            if (family != null) {
                if (chr.getSeniorId() > 0) {
                    MapleFamilyCharacter senior = family.getMFC(chr.getSeniorId());
                    if (senior != null) {
                        if ((senior.getJunior1() > 0) && (senior.getJunior1() != chr.getId())) {
                            addFamilyCharInfo(family.getMFC(senior.getJunior1()), mplew);
                        } else if ((senior.getJunior2() > 0) && (senior.getJunior2() != chr.getId())) {
                            addFamilyCharInfo(family.getMFC(senior.getJunior2()), mplew);
                        }

                    }

                }

                if (chr.getJunior1() > 0) {
                    MapleFamilyCharacter junior = family.getMFC(chr.getJunior1());
                    if (junior != null) {
                        addFamilyCharInfo(junior, mplew);
                    }
                }
                if (chr.getJunior2() > 0) {
                    MapleFamilyCharacter junior = family.getMFC(chr.getJunior2());
                    if (junior != null) {
                        addFamilyCharInfo(junior, mplew);
                    }
                }
                if (chr.getJunior1() > 0) {
                    MapleFamilyCharacter junior = family.getMFC(chr.getJunior1());
                    if (junior != null) {
                        if ((junior.getJunior1() > 0) && (family.getMFC(junior.getJunior1()) != null)) {
                            gens++;
                            addFamilyCharInfo(family.getMFC(junior.getJunior1()), mplew);
                        }
                        if ((junior.getJunior2() > 0) && (family.getMFC(junior.getJunior2()) != null)) {
                            gens++;
                            addFamilyCharInfo(family.getMFC(junior.getJunior2()), mplew);
                        }
                    }
                }
                if (chr.getJunior2() > 0) {
                    MapleFamilyCharacter junior = family.getMFC(chr.getJunior2());
                    if (junior != null) {
                        if ((junior.getJunior1() > 0) && (family.getMFC(junior.getJunior1()) != null)) {
                            gens++;
                            addFamilyCharInfo(family.getMFC(junior.getJunior1()), mplew);
                        }
                        if ((junior.getJunior2() > 0) && (family.getMFC(junior.getJunior2()) != null)) {
                            gens++;
                            addFamilyCharInfo(family.getMFC(junior.getJunior2()), mplew);
                        }
                    }
                }
                generations = family.getMemberSize();
            }
            mplew.writeLong(gens);
            mplew.writeInt(0);
            mplew.writeInt(-1);
            mplew.writeInt(generations);

            if (family != null) {
                if (chr.getJunior1() > 0) {
                    MapleFamilyCharacter junior = family.getMFC(chr.getJunior1());
                    if (junior != null) {
                        if ((junior.getJunior1() > 0) && (family.getMFC(junior.getJunior1()) != null)) {
                            mplew.writeInt(junior.getJunior1());
                            mplew.writeInt(family.getMFC(junior.getJunior1()).getDescendants());
                        } else {
                            mplew.writeInt(0);
                        }
                        if ((junior.getJunior2() > 0) && (family.getMFC(junior.getJunior2()) != null)) {
                            mplew.writeInt(junior.getJunior2());
                            mplew.writeInt(family.getMFC(junior.getJunior2()).getDescendants());
                        } else {
                            mplew.writeInt(0);
                        }
                    }
                }
                if (chr.getJunior2() > 0) {
                    MapleFamilyCharacter junior = family.getMFC(chr.getJunior2());
                    if (junior != null) {
                        if ((junior.getJunior1() > 0) && (family.getMFC(junior.getJunior1()) != null)) {
                            mplew.writeInt(junior.getJunior1());
                            mplew.writeInt(family.getMFC(junior.getJunior1()).getDescendants());
                        } else {
                            mplew.writeInt(0);
                        }
                        if ((junior.getJunior2() > 0) && (family.getMFC(junior.getJunior2()) != null)) {
                            mplew.writeInt(junior.getJunior2());
                            mplew.writeInt(family.getMFC(junior.getJunior2()).getDescendants());
                        } else {
                            mplew.writeInt(0);
                        }
                    }
                }
            }

            List b = chr.usedBuffs();
            mplew.writeInt(b.size());
            for (Iterator i$ = b.iterator(); i$.hasNext();) {
                int ii = ((Integer) i$.next()).intValue();
                mplew.writeInt(ii);
                mplew.writeInt(1);
            }
            mplew.writeShort(2);

            return mplew.getPacket();
        }

        public static byte[] getFamilyMsg(byte type, int meso) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.FAMILY_MESSAGE.getValue());
            mplew.writeInt(type);
            mplew.writeInt(meso);

            return mplew.getPacket();
        }

        public static byte[] sendFamilyInvite(int cid, int otherLevel, int otherJob, String inviter) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.FAMILY_INVITE.getValue());
            mplew.writeInt(cid);
            mplew.writeInt(otherLevel);
            mplew.writeInt(otherJob);
            mplew.writeInt(0);
            mplew.writeMapleAsciiString(inviter);
            return mplew.getPacket();
        }

        public static byte[] sendFamilyJoinResponse(boolean accepted, String added) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.FAMILY_JUNIOR.getValue());
            mplew.write(accepted ? 1 : 0);
            mplew.writeMapleAsciiString(added);

            return mplew.getPacket();
        }

        public static byte[] getSeniorMessage(String name) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.SENIOR_MESSAGE.getValue());
            mplew.writeMapleAsciiString(name);

            return mplew.getPacket();
        }

        public static byte[] changeRep(int r, String name) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.REP_INCREASE.getValue());
            mplew.writeInt(r);
            mplew.writeMapleAsciiString(name);

            return mplew.getPacket();
        }

        public static byte[] familyLoggedIn(boolean online, String name) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.FAMILY_LOGGEDIN.getValue());
            mplew.write(online ? 1 : 0);
            mplew.writeMapleAsciiString(name);

            return mplew.getPacket();
        }

        public static byte[] familyBuff(int type, int buffnr, int amount, int time) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(SendPacketOpcode.FAMILY_BUFF.getValue());
            mplew.write(type);
            if ((type >= 2) && (type <= 4)) {
                mplew.writeInt(buffnr);

                mplew.writeInt(type == 3 ? 0 : amount);
                mplew.writeInt(type == 2 ? 0 : amount);
                mplew.write(0);
                mplew.writeInt(time);
            }
            return mplew.getPacket();
        }

        public static byte[] cancelFamilyBuff() {
            return familyBuff(0, 0, 0, 0);
        }

        public static byte[] familySummonRequest(String name, String mapname) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.FAMILY_USE_REQUEST.getValue());
            mplew.writeMapleAsciiString(name);
            mplew.writeMapleAsciiString(mapname);

            return mplew.getPacket();
        }
    }

    public static class AlliancePacket {

        public static byte[] getAllianceInfo(MapleGuildAlliance alliance) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(12);
            mplew.write(alliance == null ? 0 : 1);
            if (alliance != null) {
                addAllianceInfo(mplew, alliance);
            }

            return mplew.getPacket();
        }

        private static void addAllianceInfo(MaplePacketLittleEndianWriter mplew, MapleGuildAlliance alliance) {
            mplew.writeInt(alliance.getId());
            mplew.writeMapleAsciiString(alliance.getName());
            for (int i = 1; i <= 5; i++) {
                mplew.writeMapleAsciiString(alliance.getRank(i));
            }
            mplew.write(alliance.getNoGuilds());
            for (int i = 0; i < alliance.getNoGuilds(); i++) {
                mplew.writeInt(alliance.getGuildId(i));
            }
            mplew.writeInt(alliance.getCapacity());
            mplew.writeMapleAsciiString(alliance.getNotice());
        }

        public static byte[] getGuildAlliance(MapleGuildAlliance alliance) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(13);
            if (alliance == null) {
                mplew.writeInt(0);
                return mplew.getPacket();
            }
            int noGuilds = alliance.getNoGuilds();
            MapleGuild[] g = new MapleGuild[noGuilds];
            for (int i = 0; i < alliance.getNoGuilds(); i++) {
                g[i] = World.Guild.getGuild(alliance.getGuildId(i));
                if (g[i] == null) {
                    return CWvsContext.enableActions();
                }
            }
            mplew.writeInt(noGuilds);
            for (MapleGuild gg : g) {
                CWvsContext.GuildPacket.getGuildInfo(mplew, gg);
            }
            return mplew.getPacket();
        }

        public static byte[] allianceMemberOnline(int alliance, int gid, int id, boolean online) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(14);
            mplew.writeInt(alliance);
            mplew.writeInt(gid);
            mplew.writeInt(id);
            mplew.write(online ? 1 : 0);

            return mplew.getPacket();
        }

        public static byte[] removeGuildFromAlliance(MapleGuildAlliance alliance, MapleGuild expelledGuild, boolean expelled) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(16);
            addAllianceInfo(mplew, alliance);
            CWvsContext.GuildPacket.getGuildInfo(mplew, expelledGuild);
            mplew.write(expelled ? 1 : 0);

            return mplew.getPacket();
        }

        public static byte[] addGuildToAlliance(MapleGuildAlliance alliance, MapleGuild newGuild) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(18);
            addAllianceInfo(mplew, alliance);
            mplew.writeInt(newGuild.getId());
            CWvsContext.GuildPacket.getGuildInfo(mplew, newGuild);
            mplew.write(0);

            return mplew.getPacket();
        }

        public static byte[] sendAllianceInvite(String allianceName, MapleCharacter inviter) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(3);
            mplew.writeInt(inviter.getGuildId());
            mplew.writeMapleAsciiString(inviter.getName());
            mplew.writeMapleAsciiString(allianceName);

            return mplew.getPacket();
        }

        public static byte[] getAllianceUpdate(MapleGuildAlliance alliance) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(23);
            addAllianceInfo(mplew, alliance);

            return mplew.getPacket();
        }

        public static byte[] createGuildAlliance(MapleGuildAlliance alliance) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(15);
            addAllianceInfo(mplew, alliance);
            int noGuilds = alliance.getNoGuilds();
            MapleGuild[] g = new MapleGuild[noGuilds];
            for (int i = 0; i < alliance.getNoGuilds(); i++) {
                g[i] = World.Guild.getGuild(alliance.getGuildId(i));
                if (g[i] == null) {
                    return CWvsContext.enableActions();
                }
            }
            for (MapleGuild gg : g) {
                CWvsContext.GuildPacket.getGuildInfo(mplew, gg);
            }
            return mplew.getPacket();
        }

        public static byte[] updateAlliance(MapleGuildCharacter mgc, int allianceid) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(24);
            mplew.writeInt(allianceid);
            mplew.writeInt(mgc.getGuildId());
            mplew.writeInt(mgc.getId());
            mplew.writeInt(mgc.getLevel());
            mplew.writeInt(mgc.getJobId());

            return mplew.getPacket();
        }

        public static byte[] updateAllianceLeader(int allianceid, int newLeader, int oldLeader) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(25);
            mplew.writeInt(allianceid);
            mplew.writeInt(oldLeader);
            mplew.writeInt(newLeader);

            return mplew.getPacket();
        }

        public static byte[] allianceRankChange(int aid, String[] ranks) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
            mplew.write(26);
            mplew.writeInt(aid);
            for (String r : ranks) {
                mplew.writeMapleAsciiString(r);
            }

            return mplew.getPacket();
        }

        public static byte[] updateAllianceRank(MapleGuildCharacter mgc) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(27);
            mplew.writeInt(mgc.getId());
            mplew.write(mgc.getAllianceRank());

            return mplew.getPacket();
        }

        public static byte[] changeAllianceNotice(int allianceid, String notice) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(28);
            mplew.writeInt(allianceid);
            mplew.writeMapleAsciiString(notice);

            return mplew.getPacket();
        }

        public static byte[] disbandAlliance(int alliance) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(29);
            mplew.writeInt(alliance);

            return mplew.getPacket();
        }

        public static byte[] changeAlliance(MapleGuildAlliance alliance, boolean in) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(1);
            mplew.write(in ? 1 : 0);
            mplew.writeInt(in ? alliance.getId() : 0);
            int noGuilds = alliance.getNoGuilds();
            MapleGuild[] g = new MapleGuild[noGuilds];
            for (int i = 0; i < noGuilds; i++) {
                g[i] = World.Guild.getGuild(alliance.getGuildId(i));
                if (g[i] == null) {
                    return CWvsContext.enableActions();
                }
            }
            mplew.write(noGuilds);
            for (int i = 0; i < noGuilds; i++) {
                mplew.writeInt(g[i].getId());

                Collection<MapleGuildCharacter> members = g[i].getMembers();
                mplew.writeInt(members.size());
                for (MapleGuildCharacter mgc : members) {
                    mplew.writeInt(mgc.getId());
                    mplew.write((byte) (in ? mgc.getAllianceRank() : 0));
                }
            }

            return mplew.getPacket();
        }

        public static byte[] changeAllianceLeader(int allianceid, int newLeader, int oldLeader) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(2);
            mplew.writeInt(allianceid);
            mplew.writeInt(oldLeader);
            mplew.writeInt(newLeader);

            return mplew.getPacket();
        }

        public static byte[] changeGuildInAlliance(MapleGuildAlliance alliance, MapleGuild guild, boolean add) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(4);
            mplew.writeInt(add ? alliance.getId() : 0);
            mplew.writeInt(guild.getId());
            Collection<MapleGuildCharacter> members = guild.getMembers();
            mplew.writeInt(members.size());
            for (MapleGuildCharacter mgc : members) {
                mplew.writeInt(mgc.getId());
                mplew.write((byte) (add ? mgc.getAllianceRank() : 0));
            }

            return mplew.getPacket();
        }

        public static byte[] changeAllianceRank(int allianceid, MapleGuildCharacter player) {
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            mplew.writeShort(SendPacketOpcode.ALLIANCE_OPERATION.getValue());
            mplew.write(5);
            mplew.writeInt(allianceid);
            mplew.writeInt(player.getId());
            mplew.writeInt(player.getAllianceRank());

            return mplew.getPacket();
        }
    }
}