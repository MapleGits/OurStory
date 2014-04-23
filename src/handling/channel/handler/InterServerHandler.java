package handling.channel.handler;

import client.inventory.MapleInventoryType;
import clientside.MapleCharacter;
import clientside.MapleClient;
import clientside.MapleQuestStatus;
import clientside.Skill;
import clientside.SkillEntry;
import clientside.SkillFactory;
import constants.GameConstants;
import constants.MapConstants;
import handling.cashshop.CashShopServer;
import handling.cashshop.handler.CashShopOperation;
import handling.channel.ChannelServer;
import handling.login.LoginServer;
import handling.world.CharacterIdChannelPair;
import handling.world.CharacterTransfer;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.PlayerBuffStorage;
import handling.world.World;
import handling.world.exped.MapleExpedition;
import handling.world.guild.MapleGuild;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scripting.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.maps.FieldLimitType;
import server.maps.MapleMap;
import server.maps.SavedLocationType;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.Pair;
import tools.Triple;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.BuddylistPacket;
import tools.packet.CWvsContext.ExpeditionPacket;
import tools.packet.CWvsContext.FamilyPacket;
import tools.packet.CWvsContext.GuildPacket;
import tools.packet.MTSCSPacket;
import tools.packet.JobPacket;

public class InterServerHandler {

    private static Logger log = LoggerFactory.getLogger(InterServerHandler.class);

    public static void EnterCS(MapleClient c, MapleCharacter chr, boolean mts) {
        // NPCScriptManager.getInstance().start(c, 9000155);
        //   chr.getClient().getSession().write(CWvsContext.enableActions());
        if ((chr.hasBlockedInventory()) || (chr.getMap() == null) || (chr.getEventInstance() != null) || (c.getChannelServer() == null) || (MapConstants.isStorylineMap(chr.getMapId()))) {
            c.getSession().write(CField.serverBlocked(2));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }

        if ((mts) && (chr.getLevel() < 50)) {
            chr.dropMessage(1, "You may not enter the Maple Trading System until level 50.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (World.getPendingCharacterSize() >= 10) {
            chr.dropMessage(1, "The server is busy at the moment. Please try again in a minute or less.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }

        ChannelServer ch = ChannelServer.getInstance(c.getChannel());

        chr.changeRemoval();

        if (chr.getMessenger() != null) {
            MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(chr);
            World.Messenger.leaveMessenger(chr.getMessenger().getId(), messengerplayer);
        }
        PlayerBuffStorage.addBuffsToStorage(chr.getId(), chr.getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(chr.getId(), chr.getCooldowns());
        PlayerBuffStorage.addDiseaseToStorage(chr.getId(), chr.getAllDiseases());
        World.ChannelChange_Data(new CharacterTransfer(chr), chr.getId(), mts ? -20 : -10);
        ch.removePlayer(chr);
        c.updateLoginState(MapleClient.CHANGE_CHANNEL, c.getSessionIPAddress());
        chr.saveToDB(false, false);
        chr.getMap().removePlayer(chr);
        c.getSession().write(CField.getChannelChange(c, Integer.parseInt(CashShopServer.getIP().split(":")[1])));
        c.setPlayer(null);
        c.setReceiving(false);
    }

    public static void EnterMTS(MapleClient c, MapleCharacter chr) {
        if ((chr.hasBlockedInventory()) || (chr.getMap() == null) || (chr.getEventInstance() != null) || (c.getChannelServer() == null)) {
            chr.dropMessage(1, "Please try again later.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (chr.getLevel() < 15) {
            chr.dropMessage(1, "You may not enter the Free Market until level 15.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if ((chr.getMapId() >= 910000000) && (chr.getMapId() <= 910000022)) {
            chr.dropMessage(1, "You are already in the Free Market.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        chr.dropMessage(5, "You will be transported to the Free Market Entrance.");
        chr.saveLocation(SavedLocationType.fromString("FREE_MARKET"));
        MapleMap warpz = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(910000000);
        if (warpz != null) {
            chr.changeMap(warpz, warpz.getPortal("st00"));
        } else {
            chr.dropMessage(5, "Please try again later.");
        }
        c.getSession().write(CWvsContext.enableActions());
    }

    public static void Loggedin(int playerid, MapleClient c) {
        final ChannelServer channelServer = c.getChannelServer();
        MapleCharacter player;
        final CharacterTransfer transfer = channelServer.getPlayerStorage().getPendingCharacter(playerid);

        if (transfer == null) { // Player isn't in storage, probably isn't CC
            player = MapleCharacter.loadCharFromDB(playerid, c, true);
            Pair<String, String> ip = LoginServer.getLoginAuth(playerid);
            String s = c.getSessionIPAddress();
            if (ip == null || !s.substring(s.indexOf('/') + 1, s.length()).equals(ip.left)) {
                if (ip != null) {
                    LoginServer.putLoginAuth(playerid, ip.left, ip.right);
                }
                System.out.println(c.getAccountName() + " BUG?2");
                c.getSession().close(true);
                return;
            }
            c.setTempIP(ip.right);
        } else {
            player = MapleCharacter.ReconstructChr(transfer, c, true);
        }
        c.setPlayer(player);
        c.setAccID(player.getAccountID());


        if (!c.CheckIPAddress()) { // Remote hack
            System.out.println(c.getAccountName() + " BUG?3");
            c.getSession().close(true);
            return;
        }

        final int state = c.getLoginState();
        boolean allowLogin = false;

        if (state == MapleClient.LOGIN_SERVER_TRANSITION || state == MapleClient.CHANGE_CHANNEL || state == MapleClient.LOGIN_NOTLOGGEDIN) {
            allowLogin = !World.isCharacterListConnected(c.loadCharacterNames(c.getWorld()));
        }

      
        if (!allowLogin) {
            c.setPlayer(null);
            c.getSession().close(true);
            return;
        }
        c.updateLoginState(MapleClient.LOGIN_LOGGEDIN, c.getSessionIPAddress());



        channelServer.addPlayer(player);

        c.getSession().write(CField.getCharInfo(player));
        c.getSession().write(MTSCSPacket.enableCSUse());
        player.getMap().addPlayer(player);

        try {
            // Start of buddylist
            final int buddyIds[] = player.getBuddylist().getBuddyIds();
            World.Buddy.loggedOn(player.getName(), player.getId(), c.getChannel(), buddyIds);
            if (player.getParty() != null) {
                final MapleParty party = player.getParty();
                World.Party.updateParty(party.getId(), PartyOperation.LOG_ONOFF, new MaplePartyCharacter(player));

                if (party != null && party.getExpeditionId() > 0) {
                    final MapleExpedition me = World.Party.getExped(party.getExpeditionId());
                    if (me != null) {
                        c.getSession().write(ExpeditionPacket.expeditionStatus(me, false, true));
                    }
                }
            }
        if (GameConstants.demonAvenger(player.getJob())) {
            c.getSession().write(JobPacket.AvengerPacket.giveAvengerHpBuff(player.getStat().getHp()));
        }
            final CharacterIdChannelPair[] onlineBuddies = World.Find.multiBuddyFind(player.getId(), buddyIds);
            for (CharacterIdChannelPair onlineBuddy : onlineBuddies) {
                player.getBuddylist().get(onlineBuddy.getCharacterId()).setChannel(onlineBuddy.getChannel());
            }
            c.getSession().write(BuddylistPacket.updateBuddylist(player.getBuddylist().getBuddies()));


            final MapleMessenger messenger = player.getMessenger();
            if (messenger != null) {
                World.Messenger.silentJoinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getPlayer()));
                World.Messenger.updateMessenger(messenger.getId(), c.getPlayer().getName(), c.getChannel());
            }

            // Start of Guild and alliance
            if (player.getGuildId() > 0) {
                World.Guild.setGuildMemberOnline(player.getMGC(), true, c.getChannel());
                c.getSession().write(GuildPacket.showGuildInfo(player));
                final MapleGuild gs = World.Guild.getGuild(player.getGuildId());
                if (gs != null) {
                    final List<byte[]> packetList = World.Alliance.getAllianceInfo(gs.getAllianceId(), true);
                    if (packetList != null) {
                        for (byte[] pack : packetList) {
                            if (pack != null) {
                                c.getSession().write(pack);
                            }
                        }
                    }
                } else { //guild not found, change guild id
                    player.setGuildId(0);
                    player.setGuildRank((byte) 5);
                    player.setAllianceRank((byte) 5);
                    player.saveGuildStatus();
                }
            }

            if (player.getFamilyId() > 0) {
                World.Family.setFamilyMemberOnline(player.getMFC(), true, c.getChannel());
            }
            c.getSession().write(FamilyPacket.getFamilyData());
            c.getSession().write(FamilyPacket.getFamilyInfo(player));
        } catch (Exception e) {
            FileoutputUtil.outputFileError(FileoutputUtil.Login_Error, e);
        }
        player.getClient().getSession().write(CWvsContext.serverMessage(channelServer.getServerMessage()));


        player.showNote();
        player.sendImp();
        player.updatePartyMemberHP();
        player.startFairySchedule(false);
        c.getSession().write(CField.getKeymap(player.getKeyLayout()));
        player.updatePetAuto();
        player.expirationTask(true, transfer == null);
        if (player.getJob() == 132) {
            player.checkBerserk();
        }
        if (GameConstants.demonAvenger(player.getJob())) {
            c.getSession().write(JobPacket.AvengerPacket.giveAvengerHpBuff(player.getStat().getHp()));
        }
        player.spawnClones();
        player.spawnSavedPets();
        if (player.getStat().equippedSummon > 0 && SkillFactory.getSkill(player.getStat().equippedSummon) != null) {

            SkillFactory.getSkill(player.getStat().equippedSummon).getEffect(1).applyTo(player);

        }
       
  /*      if ((player.getJob() >= 2711) && (player.getJob() <= 2712)) {
            player.checkLunarTide();
        }*/

        MapleQuestStatus stat = player.getQuestNoAdd(MapleQuest.getInstance(122700));
        Map<Skill, SkillEntry> skill = c.getPlayer().getSkills();
        c.getPlayer().getClient().getSession().write(CWvsContext.updateSkills(skill));
        player.sendMacros();
        stat = player.getQuestNoAdd(MapleQuest.getInstance(123000));
        c.getSession().write(CField.quickSlot((stat != null) && (stat.getCustomData() != null) ? stat.getCustomData() : null));
        c.getSession().write(CWvsContext.getFamiliarInfo(player));
        c.getSession().write(CField.getInventoryStatus());
          if (player.getJob() == 3122) {
            c.getSession().write(CWvsContext.giveDemonWatk(player));
        }
       NPCScriptManager.getInstance().start(c, 9250152);
    }

    public static void ChangeChannel(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr, boolean room) {
        if ((chr == null) || (chr.hasBlockedInventory()) || (chr.getEventInstance() != null) || (chr.getMap() == null) || (chr.isInBlockedMap()) || (FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit()))) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (World.getPendingCharacterSize() >= 10) {
            chr.dropMessage(1, "The server is busy at the moment. Please try again in a less than a minute.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        int chc = slea.readByte() + 1;
        int mapid = 0;
        if (room) {
            mapid = slea.readInt();
        }
        slea.readInt();
        //chr.updateTick(slea.readInt());
        if (!World.isChannelAvailable(chc)) {
            chr.dropMessage(1, "The channel is full at the moment.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if ((room) && ((mapid < 910000001) || (mapid > 910000022))) {
            chr.dropMessage(1, "The channel is full at the moment.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (room) {
            if (chr.getMapId() == mapid) {
                if (c.getChannel() == chc) {
                    chr.dropMessage(1, new StringBuilder().append("You are already in ").append(chr.getMap().getMapName()).toString());
                    c.getSession().write(CWvsContext.enableActions());
                } else {
                    chr.changeChannel(chc);
                }
            } else {
                if (c.getChannel() != chc) {
                    chr.changeChannel(chc);
                }
                MapleMap warpz = ChannelServer.getInstance(c.getChannel()).getMapFactory().getMap(mapid);
                if (warpz != null) {
                    chr.changeMap(warpz, warpz.getPortal("out00"));
                } else {
                    chr.dropMessage(1, "The channel is full at the moment.");
                    c.getSession().write(CWvsContext.enableActions());
                }
            }
        } else {
            chr.changeChannel(chc);
        }
    }

}