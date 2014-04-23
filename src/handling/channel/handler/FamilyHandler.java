package handling.channel.handler;

import clientside.MapleCharacter;
import clientside.MapleCharacterUtil;
import clientside.MapleClient;
import handling.world.MaplePartyCharacter;
import handling.world.World;
import handling.world.family.MapleFamily;
import handling.world.family.MapleFamilyBuff;
import handling.world.family.MapleFamilyCharacter;
import java.util.List;
import server.maps.FieldLimitType;
import tools.data.LittleEndianAccessor;
import tools.packet.CWvsContext;

public class FamilyHandler {

    public static final void RequestFamily(LittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
        if (chr != null) {
            c.getSession().write(CWvsContext.FamilyPacket.getFamilyPedigree(chr));
        }
    }

    public static final void OpenFamily(LittleEndianAccessor slea, MapleClient c) {
        c.getSession().write(CWvsContext.FamilyPacket.getFamilyInfo(c.getPlayer()));
    }

    public static final void UseFamily(LittleEndianAccessor slea, MapleClient c) {
        int type = slea.readInt();
        if (MapleFamilyBuff.values().length <= type) {
            return;
        }
        MapleFamilyBuff entry = MapleFamilyBuff.values()[type];
        boolean success = (c.getPlayer().getFamilyId() > 0) && (c.getPlayer().canUseFamilyBuff(entry)) && (c.getPlayer().getCurrentRep() > entry.rep);
        if (!success) {
            return;
        }
        MapleCharacter victim = null;
        switch (entry) {
            case Teleport:
                victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
                if ((FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())) || (c.getPlayer().isInBlockedMap())) {
                    c.getPlayer().dropMessage(5, "Summons failed. Your current location or state does not allow a summons.");
                    success = false;
                } else if ((victim == null) || ((victim.isGM()) && (!c.getPlayer().isGM()))) {
                    c.getPlayer().dropMessage(1, "Invalid name or you are not on the same channel.");
                    success = false;
                } else if ((victim.getFamilyId() == c.getPlayer().getFamilyId()) && (!FieldLimitType.VipRock.check(victim.getMap().getFieldLimit())) && (victim.getId() != c.getPlayer().getId()) && (!victim.isInBlockedMap())) {
                    c.getPlayer().changeMap(victim.getMap(), victim.getMap().getPortal(0));
                } else {
                    c.getPlayer().dropMessage(5, "Summons failed. Your current location or state does not allow a summons.");
                    success = false;
                }
                break;
            case Summon:
                victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
                if ((FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())) || (c.getPlayer().isInBlockedMap())) {
                    c.getPlayer().dropMessage(5, "Summons failed. Your current location or state does not allow a summons.");
                } else if ((victim == null) || ((victim.isGM()) && (!c.getPlayer().isGM()))) {
                    c.getPlayer().dropMessage(1, "Invalid name or you are not on the same channel.");
                } else if (victim.getTeleportName().length() > 0) {
                    c.getPlayer().dropMessage(1, "Another character has requested to summon this character. Please try again later.");
                } else if ((victim.getFamilyId() == c.getPlayer().getFamilyId()) && (!FieldLimitType.VipRock.check(victim.getMap().getFieldLimit())) && (victim.getId() != c.getPlayer().getId()) && (!victim.isInBlockedMap())) {
                    victim.getClient().getSession().write(CWvsContext.FamilyPacket.familySummonRequest(c.getPlayer().getName(), c.getPlayer().getMap().getMapName()));
                    victim.setTeleportName(c.getPlayer().getName());
                } else {
                    c.getPlayer().dropMessage(5, "Summons failed. Your current location or state does not allow a summons.");
                }
                return;
            case Drop_12_15: // drop rate + 50% 15 min
            case EXP_12_15: // exp rate + 50% 15 min
            case Drop_12_30: // drop rate + 100% 15 min
            case EXP_12_30: // exp rate + 100% 15 min
            case Drop_15_15:
            case Drop_15_30:
                entry.applyTo(c.getPlayer());
                break;
            case Bonding:
                MapleFamily fam = World.Family.getFamily(c.getPlayer().getFamilyId());
                List<MapleFamilyCharacter> chrs = fam.getMFC(c.getPlayer().getId()).getOnlineJuniors(fam);
                if (chrs.size() < 7) {
                    success = false;
                } else {
                    for (MapleFamilyCharacter chrz : chrs) {
                        int chr = World.Find.findChannel(chrz.getId());
                        if (chr != -1) {
                            MapleCharacter chrr = World.getStorage(chr).getCharacterById(chrz.getId());
                            entry.applyTo(chrr);
                        }
                    }
                }
                break;
            case EXP_Party:
            case Drop_Party_12:
            case Drop_Party_15:
                entry.applyTo(c.getPlayer());

                if (c.getPlayer().getParty() != null) {
                    for (MaplePartyCharacter mpc : c.getPlayer().getParty().getMembers()) {
                        if (mpc.getId() != c.getPlayer().getId()) {
                            MapleCharacter chr = c.getPlayer().getMap().getCharacterById(mpc.getId());
                            if (chr != null) {
                                entry.applyTo(chr);
                            }
                        }
                    }
                }
                break;
        }

        if (success) {
            c.getPlayer().setCurrentRep(c.getPlayer().getCurrentRep() - entry.rep);
            c.getSession().write(CWvsContext.FamilyPacket.changeRep(-entry.rep, c.getPlayer().getName()));
            c.getPlayer().useFamilyBuff(entry);
        } else {
            c.getPlayer().dropMessage(5, "An error occured.");
        }
    }

    public static final void FamilyOperation(LittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer() == null) {
            return;
        }
        MapleCharacter addChr = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
        if (addChr == null) {
            c.getPlayer().dropMessage(1, "The name you requested is incorrect or he/she is currently not logged in.");
        } else if ((addChr.getFamilyId() == c.getPlayer().getFamilyId()) && (addChr.getFamilyId() > 0)) {
            c.getPlayer().dropMessage(1, "You belong to the same family.");
        } else if (addChr.getMapId() != c.getPlayer().getMapId()) {
            c.getPlayer().dropMessage(1, "The one you wish to add as a junior must be in the same map.");
        } else if (addChr.getSeniorId() != 0) {
            c.getPlayer().dropMessage(1, "The character is already a junior of another character.");
        } else if (addChr.getLevel() >= c.getPlayer().getLevel()) {
            c.getPlayer().dropMessage(1, "The junior you wish to add must be at a lower rank.");
        } else if (addChr.getLevel() < c.getPlayer().getLevel() - 20) {
            c.getPlayer().dropMessage(1, "The gap between you and your junior must be within 20 levels.");
        } else if (addChr.getLevel() < 10) {
            c.getPlayer().dropMessage(1, "The junior you wish to add must be over Level 10.");
        } else if ((c.getPlayer().getJunior1() > 0) && (c.getPlayer().getJunior2() > 0)) {
            c.getPlayer().dropMessage(1, "You have 2 juniors already.");
        } else {
            addChr.getClient().getSession().write(CWvsContext.FamilyPacket.sendFamilyInvite(c.getPlayer().getId(), c.getPlayer().getLevel(), c.getPlayer().getJob(), c.getPlayer().getName()));
        }
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void FamilyPrecept(LittleEndianAccessor slea, MapleClient c) {
        MapleFamily fam = World.Family.getFamily(c.getPlayer().getFamilyId());
        if ((fam == null) || (fam.getLeaderId() != c.getPlayer().getId())) {
            return;
        }
        fam.setNotice(slea.readMapleAsciiString());
    }

    public static final void FamilySummon(LittleEndianAccessor slea, MapleClient c) {
        MapleFamilyBuff cost = MapleFamilyBuff.Summon;
        MapleCharacter tt = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
        if ((c.getPlayer().getFamilyId() > 0) && (tt != null) && (tt.getFamilyId() == c.getPlayer().getFamilyId()) && (!FieldLimitType.VipRock.check(tt.getMap().getFieldLimit())) && (!FieldLimitType.VipRock.check(c.getPlayer().getMap().getFieldLimit())) && (tt.canUseFamilyBuff(cost)) && (c.getPlayer().getTeleportName().equals(tt.getName())) && (tt.getCurrentRep() > cost.rep) && (!c.getPlayer().isInBlockedMap()) && (!tt.isInBlockedMap())) {
            boolean accepted = slea.readByte() > 0;
            if (accepted) {
                c.getPlayer().changeMap(tt.getMap(), tt.getMap().getPortal(0));
                tt.setCurrentRep(tt.getCurrentRep() - cost.rep);
                tt.getClient().getSession().write(CWvsContext.FamilyPacket.changeRep(-cost.rep, tt.getName()));
                tt.useFamilyBuff(cost);
            } else {
                tt.dropMessage(5, "Summons failed. Your current location or state does not allow a summons.");
            }
        } else {
            c.getPlayer().dropMessage(5, "Summons failed. Your current location or state does not allow a summons.");
        }
        c.getPlayer().setTeleportName("");
    }

    public static final void DeleteJunior(LittleEndianAccessor slea, MapleClient c) {
        int juniorid = slea.readInt();
        if ((c.getPlayer().getFamilyId() <= 0) || (juniorid <= 0) || ((c.getPlayer().getJunior1() != juniorid) && (c.getPlayer().getJunior2() != juniorid))) {
            return;
        }

        MapleFamily fam = World.Family.getFamily(c.getPlayer().getFamilyId());
        MapleFamilyCharacter other = fam.getMFC(juniorid);
        if (other == null) {
            return;
        }
        MapleFamilyCharacter oth = c.getPlayer().getMFC();
        boolean junior2 = oth.getJunior2() == juniorid;
        if (junior2) {
            oth.setJunior2(0);
        } else {
            oth.setJunior1(0);
        }
        c.getPlayer().saveFamilyStatus();
        other.setSeniorId(0);

        MapleFamily.setOfflineFamilyStatus(other.getFamilyId(), other.getSeniorId(), other.getJunior1(), other.getJunior2(), other.getCurrentRep(), other.getTotalRep(), other.getId());

        MapleCharacterUtil.sendNote(other.getName(), c.getPlayer().getName(), c.getPlayer().getName() + " has requested to sever ties with you, so the family relationship has ended.", 0);
        if (!fam.splitFamily(juniorid, other)) {
            if (!junior2) {
                fam.resetDescendants();
            }
            fam.resetPedigree();
        }
        c.getPlayer().dropMessage(1, "Broke up with (" + other.getName() + ").\r\nFamily relationship has ended.");
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void DeleteSenior(LittleEndianAccessor slea, MapleClient c) {
        if ((c.getPlayer().getFamilyId() <= 0) || (c.getPlayer().getSeniorId() <= 0)) {
            return;
        }

        MapleFamily fam = World.Family.getFamily(c.getPlayer().getFamilyId());
        MapleFamilyCharacter mgc = fam.getMFC(c.getPlayer().getSeniorId());
        MapleFamilyCharacter mgc_ = c.getPlayer().getMFC();
        mgc_.setSeniorId(0);
        boolean junior2 = mgc.getJunior2() == c.getPlayer().getId();
        if (junior2) {
            mgc.setJunior2(0);
        } else {
            mgc.setJunior1(0);
        }

        MapleFamily.setOfflineFamilyStatus(mgc.getFamilyId(), mgc.getSeniorId(), mgc.getJunior1(), mgc.getJunior2(), mgc.getCurrentRep(), mgc.getTotalRep(), mgc.getId());

        c.getPlayer().saveFamilyStatus();
        MapleCharacterUtil.sendNote(mgc.getName(), c.getPlayer().getName(), c.getPlayer().getName() + " has requested to sever ties with you, so the family relationship has ended.", 0);
        if (!fam.splitFamily(c.getPlayer().getId(), mgc_)) {
            if (!junior2) {
                fam.resetDescendants();
            }
            fam.resetPedigree();
        }
        c.getPlayer().dropMessage(1, "Broke up with (" + mgc.getName() + ").\r\nFamily relationship has ended.");
        c.getSession().write(CWvsContext.enableActions());
    }

    public static final void AcceptFamily(LittleEndianAccessor slea, MapleClient c) {
        MapleCharacter inviter = c.getPlayer().getMap().getCharacterById(slea.readInt());
        if ((inviter != null) && (c.getPlayer().getSeniorId() == 0) && (inviter.getLevel() - 20 <= c.getPlayer().getLevel()) && (inviter.getLevel() >= 10) && (inviter.getName().equals(slea.readMapleAsciiString())) && (inviter.getNoJuniors() < 2) && (c.getPlayer().getLevel() >= 10)) {
            boolean accepted = slea.readByte() > 0;
            inviter.getClient().getSession().write(CWvsContext.FamilyPacket.sendFamilyJoinResponse(accepted, c.getPlayer().getName()));
            if (accepted) {
                c.getSession().write(CWvsContext.FamilyPacket.getSeniorMessage(inviter.getName()));
                int old = c.getPlayer().getMFC() == null ? 0 : c.getPlayer().getMFC().getFamilyId();
                int oldj1 = c.getPlayer().getMFC() == null ? 0 : c.getPlayer().getMFC().getJunior1();
                int oldj2 = c.getPlayer().getMFC() == null ? 0 : c.getPlayer().getMFC().getJunior2();
                if ((inviter.getFamilyId() > 0) && (World.Family.getFamily(inviter.getFamilyId()) != null)) {
                    MapleFamily fam = World.Family.getFamily(inviter.getFamilyId());

                    c.getPlayer().setFamily(old <= 0 ? inviter.getFamilyId() : old, inviter.getId(), oldj1 <= 0 ? 0 : oldj1, oldj2 <= 0 ? 0 : oldj2);
                    MapleFamilyCharacter mf = inviter.getMFC();
                    if (mf.getJunior1() > 0) {
                        mf.setJunior2(c.getPlayer().getId());
                    } else {
                        mf.setJunior1(c.getPlayer().getId());
                    }
                    inviter.saveFamilyStatus();
                    if ((old > 0) && (World.Family.getFamily(old) != null)) {
                        MapleFamily.mergeFamily(fam, World.Family.getFamily(old));
                    } else {
                        c.getPlayer().setFamily(inviter.getFamilyId(), inviter.getId(), oldj1 <= 0 ? 0 : oldj1, oldj2 <= 0 ? 0 : oldj2);
                        fam.setOnline(c.getPlayer().getId(), true, c.getChannel());
                        c.getPlayer().saveFamilyStatus();
                    }
                    if (fam != null) {
                        if ((inviter.getNoJuniors() == 1) || (old > 0)) {
                            fam.resetDescendants();
                        }
                        fam.resetPedigree();
                    }
                } else {
                    int id = MapleFamily.createFamily(inviter.getId());
                    if (id > 0) {
                        MapleFamily.setOfflineFamilyStatus(id, 0, c.getPlayer().getId(), 0, inviter.getCurrentRep(), inviter.getTotalRep(), inviter.getId());
                        MapleFamily.setOfflineFamilyStatus(id, inviter.getId(), oldj1 <= 0 ? 0 : oldj1, oldj2 <= 0 ? 0 : oldj2, c.getPlayer().getCurrentRep(), c.getPlayer().getTotalRep(), c.getPlayer().getId());
                        inviter.setFamily(id, 0, c.getPlayer().getId(), 0);
                        c.getPlayer().setFamily(id, inviter.getId(), oldj1 <= 0 ? 0 : oldj1, oldj2 <= 0 ? 0 : oldj2);
                        MapleFamily fam = World.Family.getFamily(id);
                        fam.setOnline(inviter.getId(), true, inviter.getClient().getChannel());
                        if ((old > 0) && (World.Family.getFamily(old) != null)) {
                            MapleFamily.mergeFamily(fam, World.Family.getFamily(old));
                        } else {
                            fam.setOnline(c.getPlayer().getId(), true, c.getChannel());
                        }
                        fam.resetDescendants();
                        fam.resetPedigree();
                    }
                }

                c.getSession().write(CWvsContext.FamilyPacket.getFamilyInfo(c.getPlayer()));
            }
        }
    }
}