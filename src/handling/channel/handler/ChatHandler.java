package handling.channel.handler;

import clientside.MapleCharacter;
import clientside.MapleCharacterUtil;
import clientside.MapleClient;
import client.messages.CommandProcessor;
import constants.GameConstants;
import constants.ServerConstants;
import handling.channel.ChannelServer;
import handling.world.MapleMessenger;
import handling.world.MapleMessengerCharacter;
import handling.world.World;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class ChatHandler {

    public static final void GeneralChat(String text, byte unk, MapleClient c, MapleCharacter chr) {
        if ((text.length() > 0) && (chr != null) && (chr.getMap() != null)) {
            if (!CommandProcessor.processCommand(c, text, ServerConstants.CommandType.NORMAL)) {
                if ((!chr.isIntern()) && (text.length() >= 80)) {
                    return;
                }
                if (c.getPlayer().getMap().getId() == GameConstants.JAIL) {
                    c.getPlayer().dropMessage(5, "You're in jail, herp derp.");
                    c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                    return;
                }
                if (c.getPlayer().isMuted() || (c.getPlayer().getMap().getMuted() && !c.getPlayer().isGM())) {
                    c.getPlayer().dropMessage(5, c.getPlayer().isMuted() ? "You are Muted, therefore you are unable to talk. " : "The map is Muted, therefore you are unable to talk.");
                    c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
                    return;
                }

                if (chr.isHidden()) {
                    if ((chr.isIntern()) && (!chr.isSuperGM()) && (unk == 0)) {
                        chr.getMap().broadcastGMMessage(chr, CField.getChatText(chr.getId(), text, false, 1), true);
                        if (unk == 0) {
                            chr.getMap().broadcastGMMessage(chr, CWvsContext.serverNotice(2, chr.getName() + " : " + text), true);
                        }
                    } else {
                        chr.getMap().broadcastGMMessage(chr, CField.getChatText(chr.getId(), text, c.getPlayer().isSuperGM(), unk), true);
                    }
                } else {
                    if ((chr.isIntern()) && (!chr.isSuperGM()) && (unk == 0)) {
                        chr.getMap().broadcastMessage(CField.getChatText(chr.getId(), text, false, 1), c.getPlayer().getTruePosition());
                        if (unk == 0) {
                            chr.getMap().broadcastMessage(CWvsContext.serverNotice(2, chr.getName() + " : " + text), c.getPlayer().getTruePosition());
                        }
                    } else {
                        chr.getMap().broadcastMessage(CField.getChatText(chr.getId(), text, c.getPlayer().isSuperGM(), unk), c.getPlayer().getTruePosition());
                    }
                }


            }
        }
    }

    public static final void Others(LittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if (c.getPlayer().getMap().getId() == GameConstants.JAIL) {
            c.getPlayer().dropMessage(5, "You're in jail, herp derp.");
            c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
            return;
        }
        if (c.getPlayer().isMuted() || (c.getPlayer().getMap().getMuted() && !c.getPlayer().isGM())) {
            c.getPlayer().dropMessage(5, c.getPlayer().isMuted() ? "You are Muted, therefore you are unable to talk. " : "The map is Muted, therefore you are unable to talk.");
            c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
            return;
        }
        int type = slea.readByte();
        byte numRecipients = slea.readByte();
        if (numRecipients <= 0) {
            return;
        }
        int[] recipients = new int[numRecipients];

        for (byte i = 0; i < numRecipients; i = (byte) (i + 1)) {
            recipients[i] = slea.readInt();
        }
        String chattext = slea.readMapleAsciiString();
        if ((chr == null) || (!chr.getCanTalk())) {
            c.getSession().write(CWvsContext.serverNotice(6, "You have been muted and are therefore unable to talk."));
            return;
        }

        if (c.isMonitored()) {
            String chattype = "Unknown";
            switch (type) {
                case 0:
                    chattype = "Buddy";
                    break;
                case 1:
                    chattype = "Party";
                    break;
                case 2:
                    chattype = "Guild";
                    break;
                case 3:
                    chattype = "Alliance";
                    break;
                case 4:
                    chattype = "Expedition";
            }

            World.Broadcast.broadcastGMMessage(CWvsContext.serverNotice(6, "[GM Message] " + MapleCharacterUtil.makeMapleReadable(chr.getName()) + " said (" + chattype + "): " + chattext));
        }

        if (chattext.length() > 0) {
            if (!CommandProcessor.processCommand(c, chattext, ServerConstants.CommandType.NORMAL));
        } else {
            return;
        }


        switch (type) {
            case 0:
                World.Buddy.buddyChat(recipients, chr.getId(), chr.getName(), chattext);
                break;
            case 1:
                if (chr.getParty() != null) {
                    World.Party.partyChat(chr.getParty().getId(), chattext, chr.getName());
                }
                break;
            case 2:
                if (chr.getGuildId() > 0) {
                    World.Guild.guildChat(chr.getGuildId(), chr.getName(), chr.getId(), chattext);
                }
                break;
            case 3:
                if (chr.getGuildId() > 0) {
                    World.Alliance.allianceChat(chr.getGuildId(), chr.getName(), chr.getId(), chattext);
                }
                break;
            case 4:
                if ((chr.getParty() != null) && (chr.getParty().getExpeditionId() > 0)) {
                    World.Party.expedChat(chr.getParty().getExpeditionId(), chattext, chr.getName());
                }
                break;
        }
    }

    public static final void Messenger(LittleEndianAccessor slea, MapleClient c) {
        MapleMessenger messenger = c.getPlayer().getMessenger();
        if (c.getPlayer().getMap().getId() == GameConstants.JAIL) {
            c.getPlayer().dropMessage(5, "You're in jail, herp derp.");
            c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
            return;
        }
        if (c.getPlayer().isMuted() || (c.getPlayer().getMap().getMuted() && !c.getPlayer().isGM())) {
            c.getPlayer().dropMessage(5, c.getPlayer().isMuted() ? "You are Muted, therefore you are unable to talk. " : "The map is Muted, therefore you are unable to talk.");
            c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
            return;
        }
        int action = slea.readByte();
        switch (action) {
            case 0:
                if (messenger == null) {
                    int type = slea.readShort();
                    int messengerid = slea.readInt();
                    if (messengerid == 0) {
                        c.getPlayer().setMessenger(World.Messenger.createMessenger(new MapleMessengerCharacter(c.getPlayer())));
                    } else {
                        messenger = World.Messenger.getMessenger(messengerid);
                        if (messenger != null) {
                            int position = messenger.getLowestPosition();
                            if ((position > -1) && (position < 7)) {
                                c.getPlayer().setMessenger(messenger);
                                World.Messenger.joinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getPlayer()), c.getPlayer().getName(), c.getChannel());
                            }
                        }
                    }
                }
                break;
            case 1:
                break;
            case 2:
                if (messenger != null) {
                    MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getPlayer());
                    World.Messenger.leaveMessenger(messenger.getId(), messengerplayer);
                    c.getPlayer().setMessenger(null);
                }
                break;
            case 3:
                if (messenger != null) {

                    int position = messenger.getLowestPosition();
                    if ((position <= -1) || (position >= 7)) {
                        return;
                    }
                    String input = slea.readMapleAsciiString();
                    MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(input);
                    if (target != null) {

                        if (target.getMessenger() == null) {

                            c.getSession().write(CField.messengerNote(input, 4, 1));
                            target.getClient().getSession().write(CField.messengerInvite(c.getPlayer().getName(), messenger.getId()));

                        } else {
                            c.getSession().write(CField.messengerChat(c.getPlayer().getName(), " : " + target.getName() + " is already using Maple Messenger."));
                        }

                    } else if (World.isConnected(input)) {
                        World.Messenger.messengerInvite(c.getPlayer().getName(), messenger.getId(), input, c.getChannel());
                    } else {
                        c.getSession().write(CField.messengerNote(input, 4, 0));
                    }
                }
                break;
            case 5:
                final String targeted = slea.readMapleAsciiString();
                final MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(targeted);
                if (target != null) { // This channel
                    if (target.getMessenger() != null) {
                        target.getClient().getSession().write(CField.messengerNote(c.getPlayer().getName(), 5, 0));
                    }
                } else { // Other channel
                    if (!c.getPlayer().isIntern()) {
                        World.Messenger.declineChat(targeted, c.getPlayer().getName());
                    }
                }
            case 6:
                if (messenger != null) {
                    String charname = slea.readMapleAsciiString();
                    String text = slea.readMapleAsciiString();
                    World.Messenger.messengerChat(messenger.getId(), charname, text, c.getPlayer().getName());
                }
                break;
            case 4:
                break;
            case 14:
                String charname = slea.readMapleAsciiString();
                String text = slea.readMapleAsciiString();
                if (text.contains(" << ")) {
                    String[] parts = text.split(" << ");
                    String part1 = parts[0]; // 004
                    String part2 = parts[1]; // 034556
                    World.Messenger.messengerWhusper(messenger.getId(), charname, part2, c.getPlayer().getName(), part1);
                }

                break;
        }
    }

    public static final void Whisper_Find(LittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getMap().getId() == GameConstants.JAIL) {
            c.getPlayer().dropMessage(5, "You're in jail, herp derp.");
            c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
            return;
        }
        if (c.getPlayer().isMuted() || (c.getPlayer().getMap().getMuted() && !c.getPlayer().isGM())) {
            c.getPlayer().dropMessage(5, c.getPlayer().isMuted() ? "You are Muted, therefore you are unable to talk. " : "The map is Muted, therefore you are unable to talk.");
            c.getPlayer().getClient().getSession().write(CWvsContext.enableActions());
            return;
        }
        byte mode = slea.readByte();
        slea.readInt();
        switch (mode) {
            case 5:
            case 68:
                final String recipient = slea.readMapleAsciiString();
                MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
                if (player != null) {
                    if ((!player.isIntern()) || ((c.getPlayer().isIntern()) && (player.isIntern()))) {
                        c.getSession().write(CField.getFindReplyWithMap(player.getName(), player.getMap().getId(), mode == 68));
                    } else {
                        c.getSession().write(CField.getWhisperReply(recipient, (byte) 0));
                    }
                } else {
                    int ch = World.Find.findChannel(recipient);
                    if (ch > 0) {
                        player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipient);
                        if (player == null) {
                            break;
                        }
                        if (player != null) {
                            if ((!player.isIntern()) || ((c.getPlayer().isIntern()) && (player.isIntern()))) {
                                c.getSession().write(CField.getFindReply(recipient, (byte) ch, mode == 68));
                            } else {
                                c.getSession().write(CField.getWhisperReply(recipient, (byte) 0));
                            }
                            return;
                        }
                    }
                    if (ch == -10) {
                        c.getSession().write(CField.getFindReplyWithCS(recipient, mode == 68));
                    } else if (ch == -20) {
                        c.getPlayer().dropMessage(5, "'" + recipient + "' is at the MTS.");
                    } else {
                        c.getSession().write(CField.getWhisperReply(recipient, (byte) 0));
                    }
                }
                break;
            case 6:
                if ((c.getPlayer() == null) || (c.getPlayer().getMap() == null)) {
                    return;
                }
                if (!c.getPlayer().getCanTalk()) {
                    c.getSession().write(CWvsContext.serverNotice(6, "You have been muted and are therefore unable to talk."));
                    return;
                }

                final String recipientx = slea.readMapleAsciiString();
                final String text = slea.readMapleAsciiString();
                int ch = World.Find.findChannel(recipientx);
                if (ch > 0) {
                    final MapleCharacter playerx = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipientx);
                    if (playerx != null) {
                        playerx.getClient().getSession().write(CField.getWhisper(c.getPlayer().getName(), c.getChannel(), text));

                        c.getSession().write(CField.getWhisperReply(recipientx, (byte) 1));

                    }
                } else {
                    c.getSession().write(CField.getWhisperReply(recipientx, (byte) 0));
                }
                break;
        }
    }
}