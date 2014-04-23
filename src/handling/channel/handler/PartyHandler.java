package handling.channel.handler;

import clientside.MapleCharacter;
import clientside.MapleClient;
import constants.GameConstants;
import handling.SendPacketOpcode;
import handling.channel.ChannelServer;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.World;
import handling.world.exped.ExpeditionType;
import handling.world.exped.MapleExpedition;
import handling.world.exped.PartySearch;
import handling.world.exped.PartySearchType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import server.maps.Event_DojoAgent;
import server.maps.FieldLimitType;
import server.quest.MapleQuest;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

public class PartyHandler {

    public static final void DenyPartyRequest(LittleEndianAccessor slea, MapleClient c) {
        int action = slea.readByte();
        if ((action == 50)) {
            MapleCharacter chr = c.getPlayer().getMap().getCharacterById(slea.readInt());
            if ((chr != null) && (chr.getParty() == null) && (c.getPlayer().getParty() != null) && (c.getPlayer().getParty().getLeader().getId() == c.getPlayer().getId()) && (c.getPlayer().getParty().getMembers().size() < 6) && (c.getPlayer().getParty().getExpeditionId() <= 0) && (chr.getQuestNoAdd(MapleQuest.getInstance(122901)) == null) && (c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(122900)) == null)) {
                chr.setParty(c.getPlayer().getParty());
                World.Party.updateParty(c.getPlayer().getParty().getId(), PartyOperation.JOIN, new MaplePartyCharacter(chr));
                chr.receivePartyMemberHP();
                chr.updatePartyMemberHP();
            }
            return;
        }
        int partyid = slea.readInt();
        if ((c.getPlayer().getParty() == null) && (c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(122901)) == null)) {
            MapleParty party = World.Party.getParty(partyid);
            if (party != null) {
                if (party.getExpeditionId() > 0) {
                    c.getPlayer().dropMessage(5, "You may not do party operations while in a raid.");
                    return;
                }
                if (action == 35) {//was 31 // 35== inv
                    if (party.getMembers().size() < 6) {
                        c.getPlayer().setParty(party);
                        World.Party.updateParty(partyid, PartyOperation.JOIN, new MaplePartyCharacter(c.getPlayer()));
                        c.getPlayer().receivePartyMemberHP();
                        c.getPlayer().updatePartyMemberHP();
                    } else {
                        c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(22, null));
                    }
                } else if (action != 30) {//30 == party join
                    MapleCharacter cfrom = c.getChannelServer().getPlayerStorage().getCharacterById(party.getLeader().getId());
                    if (cfrom != null) {
                        cfrom.getClient().getSession().write(CWvsContext.PartyPacket.partyStatusMessage(23, c.getPlayer().getName()));
                    }
                }
            } else {
                c.getPlayer().dropMessage(5, "The party you are trying to join does not exist");
            }
        } else {
            c.getPlayer().dropMessage(5, "You can't join the party as you are already in one");
        }
    }
    

    public static final void PartyOperation(LittleEndianAccessor slea, MapleClient c) {
        int operation = slea.readByte();
        MapleParty party = c.getPlayer().getParty();
        MaplePartyCharacter partyplayer = new MaplePartyCharacter(c.getPlayer());

        switch (operation) {
            case 1:
                if (party == null) {
                    party = World.Party.createParty(partyplayer);
                    c.getPlayer().setParty(party);
                    c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                } else {
                    if (party.getExpeditionId() > 0) {
                        c.getPlayer().dropMessage(5, "You may not do party operations while in a raid.");
                        return;
                    }
                    if ((partyplayer.equals(party.getLeader())) && (party.getMembers().size() == 1)) {
                        c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                    } else {
                        c.getPlayer().dropMessage(5, "You can't create a party as you are already in one");
                    }
                }
                break;
            case 2://dispand and leave?
                if (party == null) {
                    break;
                }
                if (party.getExpeditionId() > 0) {
                    c.getPlayer().dropMessage(5, "You may not do party operations while in a raid.");
                    return;
                }
                if (partyplayer.equals(party.getLeader())) {
                    if (GameConstants.isDojo(c.getPlayer().getMapId())) {
                        Event_DojoAgent.failed(c.getPlayer());
                    }
                    if (c.getPlayer().getPyramidSubway() != null) {
                        c.getPlayer().getPyramidSubway().fail(c.getPlayer());
                    }
                    World.Party.updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
                    if (c.getPlayer().getEventInstance() != null) {
                        c.getPlayer().getEventInstance().disbandParty();
                    }
                } else {
                    if (GameConstants.isDojo(c.getPlayer().getMapId())) {
                        Event_DojoAgent.failed(c.getPlayer());
                    }
                    if (c.getPlayer().getPyramidSubway() != null) {
                        c.getPlayer().getPyramidSubway().fail(c.getPlayer());
                    }
                    World.Party.updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
                    if (c.getPlayer().getEventInstance() != null) {
                        c.getPlayer().getEventInstance().leftParty(c.getPlayer());
                    }
                }
                c.getPlayer().setParty(null);
                break;
            case 3:
                int partyid = slea.readInt();
                if (party == null) {
                    party = World.Party.getParty(partyid);
                    if (party != null) {
                        if (party.getExpeditionId() > 0) {
                            c.getPlayer().dropMessage(5, "You may not do party operations while in a raid.");
                            return;
                        }
                        if ((party.getMembers().size() < 6) && (c.getPlayer().getQuestNoAdd(MapleQuest.getInstance(122901)) == null)) {
                            c.getPlayer().setParty(party);
                            World.Party.updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
                            c.getPlayer().receivePartyMemberHP();
                            c.getPlayer().updatePartyMemberHP();
                        } else {
                            c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(22, null));
                        }
                    } else {
                        c.getPlayer().dropMessage(5, "The party you are trying to join does not exist");
                    }
                } else {
                    c.getPlayer().dropMessage(5, "You can't join the party as you are already in one");
                }
                break;
            case 4:
                if (party == null) {
                    party = World.Party.createParty(partyplayer);
                    c.getPlayer().setParty(party);
                    c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                }

                String theName = slea.readMapleAsciiString();
                int theCh = World.Find.findChannel(theName);
                if (theCh > 0) {
                    MapleCharacter invited = ChannelServer.getInstance(theCh).getPlayerStorage().getCharacterByName(theName);
                    if ((invited != null) && (invited.getParty() == null) && (invited.getQuestNoAdd(MapleQuest.getInstance(122901)) == null)) {
                        if (party.getExpeditionId() > 0) {
                            c.getPlayer().dropMessage(5, "You may not do party operations while in a raid.");
                            return;
                        }
                        if (party.getMembers().size() < 6) {
                            c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(30, invited.getName()));
                            invited.getClient().getSession().write(CWvsContext.PartyPacket.partyInvite(c.getPlayer()));
                        } else {
                            c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(22, null));
                        }
                    } else {
                        c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(21, null));
                    }
                } else {
                    c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(17, null));
                }
                break;
            case 6://was5
                if ((party == null) || (partyplayer == null) || (!partyplayer.equals(party.getLeader()))) {
                    break;
                }
                if (party.getExpeditionId() > 0) {
                    c.getPlayer().dropMessage(5, "You may not do party operations while in a raid.");
                    return;
                }
                MaplePartyCharacter expelled = party.getMemberById(slea.readInt());
                if (expelled != null) {
                    if ((GameConstants.isDojo(c.getPlayer().getMapId())) && (expelled.isOnline())) {
                        Event_DojoAgent.failed(c.getPlayer());
                    }
                    if ((c.getPlayer().getPyramidSubway() != null) && (expelled.isOnline())) {
                        c.getPlayer().getPyramidSubway().fail(c.getPlayer());
                    }
                    World.Party.updateParty(party.getId(), PartyOperation.EXPEL, expelled);
                    if (c.getPlayer().getEventInstance() != null) {
                        if (expelled.isOnline()) {
                            c.getPlayer().getEventInstance().disbandParty();
                        }
                    }
                }
                break;
            case 7://was 6
                if (party == null) {
                    break;
                }
                if (party.getExpeditionId() > 0) {
                    c.getPlayer().dropMessage(5, "You may not do party operations while in a raid.");
                    return;
                }
                MaplePartyCharacter newleader = party.getMemberById(slea.readInt());
                if ((newleader != null) && (partyplayer.equals(party.getLeader()))) {
                    World.Party.updateParty(party.getId(), PartyOperation.CHANGE_LEADER, newleader);
                }
                break;
            case 66://was 7
                if (party != null) {
                    if ((c.getPlayer().getEventInstance() != null) || (c.getPlayer().getPyramidSubway() != null) || (party.getExpeditionId() > 0) || (GameConstants.isDojo(c.getPlayer().getMapId()))) {
                        c.getPlayer().dropMessage(5, "You may not do party operations while in a raid.");
                        return;
                    }
                    if (partyplayer.equals(party.getLeader())) {
                        World.Party.updateParty(party.getId(), PartyOperation.DISBAND, partyplayer);
                    } else {
                        World.Party.updateParty(party.getId(), PartyOperation.LEAVE, partyplayer);
                    }
                    c.getPlayer().setParty(null);
                }
                int partyid_ = slea.readInt();
                party = World.Party.getParty(partyid_);
                if ((party == null) || (party.getMembers().size() >= 6)) {
                    break;
                }
                if (party.getExpeditionId() > 0) {
                    c.getPlayer().dropMessage(5, "You may not do party operations while in a raid.");
                    return;
                }
                MapleCharacter cfrom = c.getPlayer().getMap().getCharacterById(party.getLeader().getId());
                if ((cfrom != null) && (cfrom.getQuestNoAdd(MapleQuest.getInstance(122900)) == null)) {
                    c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(50, c.getPlayer().getName()));
                    cfrom.getClient().getSession().write(CWvsContext.PartyPacket.partyRequestInvite(c.getPlayer()));
                } else {
                    c.getPlayer().dropMessage(5, "Player was not found or player is not accepting party requests.");
                }
                break;
            case 8:
                if (slea.readByte() > 0) {
                    c.getPlayer().getQuestRemove(MapleQuest.getInstance(122900));
                } else {
                    c.getPlayer().getQuestNAdd(MapleQuest.getInstance(122900));
                }
                break;
            default:
                System.out.println("Unhandled Party function." + operation);
        }
    }

    public static final void AllowPartyInvite(LittleEndianAccessor slea, MapleClient c) {
        if (slea.readByte() > 0) {
            c.getPlayer().getQuestRemove(MapleQuest.getInstance(122901));
        } else {
            c.getPlayer().getQuestNAdd(MapleQuest.getInstance(122901));
        }
    }

    public static final void MemberSearch(LittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer().isInBlockedMap()) || (FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit()))) {
            c.getPlayer().dropMessage(5, "You may not do party search here.");
            return;
        }
        c.getSession().write(CWvsContext.PartyPacket.showMemberSearch(c.getPlayer().getMap().getCharactersThreadsafe()));
    }

    public static final void PartySearch(LittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer().isInBlockedMap()) || (FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit()))) {
            c.getPlayer().dropMessage(5, "You may not do party search here.");
            return;
        }
        List parties = new ArrayList();
        for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
            if (chr != null) {
                if ((chr.getParty() != null) && (chr.getParty().getId() != c.getPlayer().getParty().getId()) && (!parties.contains(chr.getParty()))) {
                    parties.add(chr.getParty());
                }
            }
        }

        c.getSession().write(CWvsContext.PartyPacket.showPartySearch(parties));
    }

    public static final void PartyListing(LittleEndianAccessor slea, MapleClient c) {
        int mode = slea.readByte();
        PartySearchType pst;
        switch (mode) {
            case -105:
            case -97:
            case 81:
            case 159:
                pst = PartySearchType.getById(slea.readInt());
                if ((pst == null) || (c.getPlayer().getLevel() > pst.maxLevel) || (c.getPlayer().getLevel() < pst.minLevel)) {
                    return;
                }
                if ((c.getPlayer().getParty() == null) && (World.Party.searchParty(pst).size() < 10)) {
                    MapleParty party = World.Party.createParty(new MaplePartyCharacter(c.getPlayer()), pst.id);
                    c.getPlayer().setParty(party);
                    c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                    PartySearch ps = new PartySearch(slea.readMapleAsciiString(), pst.exped ? party.getExpeditionId() : party.getId(), pst);
                    World.Party.addSearch(ps);
                    if (pst.exped) {
                        c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(World.Party.getExped(party.getExpeditionId()), true, false));
                    }
                    c.getSession().write(CWvsContext.PartyPacket.partyListingAdded(ps));
                } else {
                    c.getPlayer().dropMessage(1, "Unable to create. Please leave the party.");
                }
                break;
            case -103:
            case -95:
            case 83:
            case 161:
                pst = PartySearchType.getById(slea.readInt());
                if ((pst == null) || (c.getPlayer().getLevel() > pst.maxLevel) || (c.getPlayer().getLevel() < pst.minLevel)) {
                    return;
                }
                c.getSession().write(CWvsContext.PartyPacket.getPartyListing(pst));
                break;
            case -102:
            case -94:
            case 84:
            case 162:
                break;
            case -101:
            case -93:
            case 85:
            case 163:
                MapleParty party = c.getPlayer().getParty();
                MaplePartyCharacter partyplayer = new MaplePartyCharacter(c.getPlayer());
                if (party == null) {
                    int theId = slea.readInt();
                    party = World.Party.getParty(theId);
                    if (party != null) {
                        PartySearch ps = World.Party.getSearchByParty(party.getId());
                        if ((ps != null) && (c.getPlayer().getLevel() <= ps.getType().maxLevel) && (c.getPlayer().getLevel() >= ps.getType().minLevel) && (party.getMembers().size() < 6)) {
                            c.getPlayer().setParty(party);
                            World.Party.updateParty(party.getId(), PartyOperation.JOIN, partyplayer);
                            c.getPlayer().receivePartyMemberHP();
                            c.getPlayer().updatePartyMemberHP();
                        } else {
                            c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(21, null));
                        }
                    } else {
                        MapleExpedition exped = World.Party.getExped(theId);
                        if (exped != null) {
                            PartySearch ps = World.Party.getSearchByExped(exped.getId());
                            if ((ps != null) && (c.getPlayer().getLevel() <= ps.getType().maxLevel) && (c.getPlayer().getLevel() >= ps.getType().minLevel) && (exped.getAllMembers() < exped.getType().maxMembers)) {
                                int partyId = exped.getFreeParty();
                                if (partyId < 0) {
                                    c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(21, null));
                                } else if (partyId == 0) {
                                    party = World.Party.createPartyAndAdd(partyplayer, exped.getId());
                                    c.getPlayer().setParty(party);
                                    c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                                    c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(exped, true, false));
                                    World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionJoined(c.getPlayer().getName()), null);
                                    World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionUpdate(exped.getIndex(party.getId()), party), null);
                                } else {
                                    c.getPlayer().setParty(World.Party.getParty(partyId));
                                    World.Party.updateParty(partyId, PartyOperation.JOIN, partyplayer);
                                    c.getPlayer().receivePartyMemberHP();
                                    c.getPlayer().updatePartyMemberHP();
                                    c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(exped, true, false));
                                    World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionJoined(c.getPlayer().getName()), null);
                                }
                            } else {
                                c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(0, c.getPlayer().getName()));
                            }
                        }
                    }
                }
                break;
            default:
                if (c.getPlayer().isGM()) {
                    System.out.println("Unknown PartyListing : " + mode + "\n" + slea);
                }
                break;
        }
    }

    /*     */ public static final void Expedition(LittleEndianAccessor slea, MapleClient c) /*     */ {
        /* 414 */ if ((c.getPlayer() == null) || (c.getPlayer().getMap() == null)) {
            /* 415 */ return;
            /*     */        }
        /* 417 */ int mode = slea.readByte();
        /*     */ String name;
        /*     */ MapleParty part;
        /*     */ MapleExpedition exped;
        /*     */ int cid;
        /*     */ Iterator i$;

        /* 420 */ switch (mode) /*     */ {
            /*     */ case 64:
            /*     */ case 134:
                /* 424 */ ExpeditionType et = ExpeditionType.getById(slea.readInt());
                /* 425 */ if ((et != null) && (c.getPlayer().getParty() == null) && (c.getPlayer().getLevel() <= et.maxLevel) && (c.getPlayer().getLevel() >= et.minLevel)) {
                    /* 426 */ MapleParty party = World.Party.createParty(new MaplePartyCharacter(c.getPlayer()), et.exped);
                    /* 427 */ c.getPlayer().setParty(party);
                    /* 428 */ c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                    /* 429 */ c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(World.Party.getExped(party.getExpeditionId()), true, false));
                    /*     */                } else {
                    /* 431 */ c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(0, ""));
                    /*     */                }
                /* 433 */ break;
            /*     */ case 65:
            /*     */ case 135:
                /* 437 */ name = slea.readMapleAsciiString();
                /* 438 */ int theCh = World.Find.findChannel(name);
                /* 439 */ if (theCh > 0) {
                    /* 440 */ MapleCharacter invited = ChannelServer.getInstance(theCh).getPlayerStorage().getCharacterByName(name);
                    /* 441 */ MapleParty party = c.getPlayer().getParty();
                    /* 442 */ if ((invited != null) && (invited.getParty() == null) && (party != null) && (party.getExpeditionId() > 0)) {
                        /* 443 */ MapleExpedition me = World.Party.getExped(party.getExpeditionId());
                        /* 444 */ if ((me != null) && (me.getAllMembers() < me.getType().maxMembers) && (invited.getLevel() <= me.getType().maxLevel) && (invited.getLevel() >= me.getType().minLevel)) {
                            /* 445 */          //   c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(7, invited.getName()));
/* 446 */            // invited.getClient().getSession().write(CWvsContext.ExpeditionPacket.expeditionInvite(c.getPlayer(), me.getType().exped));
/*     */                        } else {
                            /* 448 */             //c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(3, invited.getName()));  
/*     */                        }
                        /*     */                    } else {
                        /* 451 */ c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(2, name));
                        /*     */                    }
                    /*     */                } else {
                    /* 454 */ c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(0, name));
                    /*     */                }
                /* 456 */ break;
            /*     */ case 66:
            /*     */ case 136:
                /* 460 */ name = slea.readMapleAsciiString();
                /* 461 */ int action = slea.readInt();
                /* 462 */ int theChh = World.Find.findChannel(name);
                /* 463 */ if (theChh <= 0) {
                    break;
                }
                /* 464 */ MapleCharacter cfrom = ChannelServer.getInstance(theChh).getPlayerStorage().getCharacterByName(name);
                /* 465 */ if ((cfrom != null) && (cfrom.getParty() != null) && (cfrom.getParty().getExpeditionId() > 0)) {
                    /* 466 */ MapleParty party = cfrom.getParty();
                    /* 467 */ exped = World.Party.getExped(party.getExpeditionId());
                    /* 468 */ if ((exped != null) && (action == 8)) {
                        /* 469 */ if ((c.getPlayer().getLevel() <= exped.getType().maxLevel) && (c.getPlayer().getLevel() >= exped.getType().minLevel) && (exped.getAllMembers() < exped.getType().maxMembers)) {
                            /* 470 */ int partyId = exped.getFreeParty();
                            /* 471 */ if (partyId < 0) {
                                /* 472 */ c.getSession().write(CWvsContext.PartyPacket.partyStatusMessage(21, null));
                                /* 473 */                            } else if (partyId == 0) {
                                /* 474 */ party = World.Party.createPartyAndAdd(new MaplePartyCharacter(c.getPlayer()), exped.getId());
                                /* 475 */ c.getPlayer().setParty(party);
                                /* 476 */ c.getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                                /* 477 */ c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(exped, true, false));
                                /* 478 */ World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionJoined(c.getPlayer().getName()), null);
                                /* 479 */ World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionUpdate(exped.getIndex(party.getId()), party), null);
                                /*     */                            } else {
                                /* 481 */ c.getPlayer().setParty(World.Party.getParty(partyId));
                                /* 482 */ World.Party.updateParty(partyId, PartyOperation.JOIN, new MaplePartyCharacter(c.getPlayer()));
                                /* 483 */ c.getPlayer().receivePartyMemberHP();
                                /* 484 */ c.getPlayer().updatePartyMemberHP();
                                /* 485 */ c.getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(exped, false, false));
                                /* 486 */ World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionJoined(c.getPlayer().getName()), null);
                                /*     */                            }
                            /*     */                        } else {
                            /* 489 */ c.getSession().write(CWvsContext.ExpeditionPacket.expeditionError(3, cfrom.getName()));
                            /*     */                        }
                        /* 491 */                    } else if (action == 9) {
                        /* 492 */ cfrom.getClient().getSession().write(CWvsContext.PartyPacket.partyStatusMessage(23, c.getPlayer().getName()));
                        /*     */                    }
                    /*     */                }
                /* 495 */ break;
            /*     */ case 67:
            /*     */ case 137:
                /* 500 */ part = c.getPlayer().getParty();
                /* 501 */ if ((part == null) || (part.getExpeditionId() <= 0)) {
                    break;
                }
                /* 502 */ exped = World.Party.getExped(part.getExpeditionId());
                /* 503 */ if (exped != null) {
                    /* 504 */ if (GameConstants.isDojo(c.getPlayer().getMapId())) {
                        /* 505 */ Event_DojoAgent.failed(c.getPlayer());
                        /*     */                    }
                    /* 507 */ if (exped.getLeader() == c.getPlayer().getId()) {
                        /* 508 */ World.Party.disbandExped(exped.getId());
                        /* 509 */ if (c.getPlayer().getEventInstance() != null) /* 510 */ {
                            c.getPlayer().getEventInstance().disbandParty();
                        }
                        /*     */                    } /* 512 */ else if (part.getLeader().getId() == c.getPlayer().getId()) {
                        /* 513 */ World.Party.updateParty(part.getId(), PartyOperation.DISBAND, new MaplePartyCharacter(c.getPlayer()));
                        /* 514 */ if (c.getPlayer().getEventInstance() != null) {
                            /* 515 */ c.getPlayer().getEventInstance().disbandParty();
                            /*     */                        }
                        /* 517 */ World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionLeft(c.getPlayer().getName()), null);
                        /*     */                    } else {
                        /* 519 */ World.Party.updateParty(part.getId(), PartyOperation.LEAVE, new MaplePartyCharacter(c.getPlayer()));
                        /* 520 */ if (c.getPlayer().getEventInstance() != null) {
                            /* 521 */ c.getPlayer().getEventInstance().leftParty(c.getPlayer());
                            /*     */                        }
                        /* 523 */ World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionLeft(c.getPlayer().getName()), null);
                        /*     */                    }
                    /* 525 */ if (c.getPlayer().getPyramidSubway() != null) {
                        /* 526 */ c.getPlayer().getPyramidSubway().fail(c.getPlayer());
                        /*     */                    }
                    /* 528 */ c.getPlayer().setParty(null);
                    /*     */                }
                /* 530 */ break;
            /*     */ case 68:
            /*     */ case 138:
                /* 535 */ part = c.getPlayer().getParty();
                /* 536 */ if ((part == null) || (part.getExpeditionId() <= 0)) {
                    break;
                }
                /* 537 */ exped = World.Party.getExped(part.getExpeditionId());
                /* 538 */ if ((exped != null) && (exped.getLeader() == c.getPlayer().getId())) {
                    /* 539 */ cid = slea.readInt();
                    /* 540 */ for (i$ = exped.getParties().iterator(); i$.hasNext();) {
                        int i = ((Integer) i$.next()).intValue();
                        /* 541 */ MapleParty par = World.Party.getParty(i);
                        /* 542 */ if (par != null) {
                            /* 543 */ MaplePartyCharacter expelled = par.getMemberById(cid);
                            /* 544 */ if (expelled != null) {
                                /* 545 */ if ((expelled.isOnline()) && (GameConstants.isDojo(c.getPlayer().getMapId()))) {
                                    /* 546 */ Event_DojoAgent.failed(c.getPlayer());
                                    /*     */                                }
                                /* 548 */ World.Party.updateParty(i, PartyOperation.EXPEL, expelled);
                                /* 549 */ if ((c.getPlayer().getEventInstance() != null)
                                        && /* 550 */ (expelled.isOnline())) {
                                    /* 551 */ c.getPlayer().getEventInstance().disbandParty();
                                    /*     */                                }
                                /*     */
                                /* 554 */ if ((c.getPlayer().getPyramidSubway() != null) && (expelled.isOnline())) {
                                    /* 555 */ c.getPlayer().getPyramidSubway().fail(c.getPlayer());
                                    /*     */                                }
                                /* 557 */ World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionLeft(expelled.getName()), null);
                                /* 558 */ break;
                                /*     */                            }
                            /*     */                        }
                        /*     */                    }
                    /*     */                }
                /* 563 */ break;
            /*     */ case 69:
            /*     */ case 139:
                /* 568 */ part = c.getPlayer().getParty();
                /* 569 */ if ((part == null) || (part.getExpeditionId() <= 0)) {
                    break;
                }
                /* 570 */ exped = World.Party.getExped(part.getExpeditionId());
                /* 571 */ if ((exped != null) && (exped.getLeader() == c.getPlayer().getId())) {
                    /* 572 */ MaplePartyCharacter newleader = part.getMemberById(slea.readInt());
                    /* 573 */ if (newleader != null) {
                        /* 574 */ World.Party.updateParty(part.getId(), PartyOperation.CHANGE_LEADER, newleader);
                        /* 575 */ exped.setLeader(newleader.getId());
                        /* 576 */ World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionLeaderChanged(0), null);
                        /*     */                    }
                    /*     */                }
                /* 579 */ break;
            /*     */ case 70:
            /*     */ case 140:
                /* 584 */ part = c.getPlayer().getParty();
                /* 585 */ if ((part == null) || (part.getExpeditionId() <= 0)) {
                    break;
                }
                /* 586 */ exped = World.Party.getExped(part.getExpeditionId());
                /* 587 */ if ((exped != null) && (exped.getLeader() == c.getPlayer().getId())) {
                    /* 588 */ cid = slea.readInt();
                    /* 589 */ for (i$ = exped.getParties().iterator(); i$.hasNext();) {
                        int i = ((Integer) i$.next()).intValue();
                        /* 590 */ MapleParty par = World.Party.getParty(i);
                        /* 591 */ if (par != null) {
                            /* 592 */ MaplePartyCharacter newleader = par.getMemberById(cid);
                            /* 593 */ if ((newleader != null) && (par.getId() != part.getId())) {
                                /* 594 */ World.Party.updateParty(par.getId(), PartyOperation.CHANGE_LEADER, newleader);
                                /*     */                            }
                            /*     */                        }
                        /*     */                    }
                    /*     */                }
                /* 599 */ break;
            /*     */ case 71:
            /*     */ case 141:
                /* 604 */ part = c.getPlayer().getParty();
                /* 605 */ if ((part == null) || (part.getExpeditionId() <= 0)) {
                    break;
                }
                /* 606 */ exped = World.Party.getExped(part.getExpeditionId());
                /* 607 */ if ((exped != null) && (exped.getLeader() == c.getPlayer().getId())) {
                    /* 608 */ int partyIndexTo = slea.readInt();
                    /* 609 */ if ((partyIndexTo < exped.getType().maxParty) && (partyIndexTo <= exped.getParties().size())) {
                        /* 610 */ cid = slea.readInt();
                        /* 611 */ for (i$ = exped.getParties().iterator(); i$.hasNext();) {
                            int i = ((Integer) i$.next()).intValue();
                            /* 612 */ MapleParty par = World.Party.getParty(i);
                            /* 613 */ if (par != null) {
                                /* 614 */ MaplePartyCharacter expelled = par.getMemberById(cid);
                                /* 615 */ if ((expelled != null) && (expelled.isOnline())) {
                                    /* 616 */ MapleCharacter chr = World.getStorage(expelled.getChannel()).getCharacterById(expelled.getId());
                                    /* 617 */ if (chr == null) {
                                        /*     */ break;
                                        /*     */                                    }
                                    /* 620 */ if (partyIndexTo < exped.getParties().size()) {
                                        /* 621 */ MapleParty party = World.Party.getParty(((Integer) exped.getParties().get(partyIndexTo)).intValue());
                                        /* 622 */ if ((party == null) || (party.getMembers().size() >= 6)) {
                                            /* 623 */ c.getPlayer().dropMessage(5, "Invalid party.");
                                            /* 624 */ break;
                                            /*     */                                        }
                                        /*     */                                    }
                                    /* 627 */ if (GameConstants.isDojo(c.getPlayer().getMapId())) {
                                        /* 628 */ Event_DojoAgent.failed(c.getPlayer());
                                        /*     */                                    }
                                    /* 630 */ World.Party.updateParty(i, PartyOperation.EXPEL, expelled);
                                    /* 631 */ if (partyIndexTo < exped.getParties().size()) {
                                        /* 632 */ MapleParty party = World.Party.getParty(((Integer) exped.getParties().get(partyIndexTo)).intValue());
                                        /* 633 */ if ((party != null) && (party.getMembers().size() < 6)) {
                                            /* 634 */ World.Party.updateParty(party.getId(), PartyOperation.JOIN, expelled);
                                            /* 635 */ chr.receivePartyMemberHP();
                                            /* 636 */ chr.updatePartyMemberHP();
                                            /* 637 */ chr.getClient().getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(exped, true, false));
                                            /*     */                                        }
                                        /*     */                                    } else {
                                        /* 640 */ MapleParty party = World.Party.createPartyAndAdd(expelled, exped.getId());
                                        /* 641 */ chr.setParty(party);
                                        /* 642 */ chr.getClient().getSession().write(CWvsContext.PartyPacket.partyCreated(party.getId()));
                                        /* 643 */ chr.getClient().getSession().write(CWvsContext.ExpeditionPacket.expeditionStatus(exped, true, false));
                                        /* 644 */ World.Party.expedPacket(exped.getId(), CWvsContext.ExpeditionPacket.expeditionUpdate(exped.getIndex(party.getId()), party), null);
                                        /*     */                                    }
                                    /* 646 */ if ((c.getPlayer().getEventInstance() != null)
                                            && /* 647 */ (expelled.isOnline())) {
                                        /* 648 */ c.getPlayer().getEventInstance().disbandParty();
                                        /*     */                                    }
                                    /*     */
                                    /* 651 */ if (c.getPlayer().getPyramidSubway() == null) {
                                        break;
                                    }
                                    /* 652 */ c.getPlayer().getPyramidSubway().fail(c.getPlayer());
                                    break;
                                    /*     */                                }
                                /*     */                            }
                            /*     */                        }
                        /*     */                    }
                    /*     */
                    /*     */                }
                /*     */
                /* 660 */ break;
            /*     */ default:
                /* 663 */ if (!c.getPlayer().isGM()) {
                    break;
                }
                /* 664 */ System.out.println("Unknown Expedition : " + mode + "\n" + slea);
            /*     */        }
        /*     */    }
}