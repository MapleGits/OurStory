package scripting;

import clientside.MapleCharacter;
import clientside.MapleCharacterUtil;
import clientside.MapleClient;
import clientside.MapleStat;
import clientside.Skill;
import clientside.SkillEntry;
import clientside.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.MultiMirror;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.channel.PlayerStorage;
import handling.channel.handler.HiredMerchantHandler;
import handling.channel.handler.PlayersHandler;
import handling.login.LoginInformationProvider;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.World;
import handling.world.exped.ExpeditionType;
import handling.world.guild.MapleGuild;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.script.Invocable;
import server.MapleCarnivalChallenge;
import server.MapleCarnivalParty;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleShopFactory;
import server.MapleSlideMenu;
import server.MapleSlideMenu.SlideMenu0;
import server.MapleSlideMenu.SlideMenu1;
import server.MapleSlideMenu.SlideMenu2;
import server.MapleSlideMenu.SlideMenu3;
import server.MapleSlideMenu.SlideMenu4;
import server.MapleSlideMenu.SlideMenu5;
import server.MapleSquad;
import server.MapleStatEffect;
import server.Randomizer;
import server.RankingWorker;
import server.SpeedRunner;
import server.StructItemOption;
import server.Timer;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import server.life.MonsterDropEntry;
import server.maps.Event_DojoAgent;
import server.maps.Event_PyramidSubway;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.StringUtil;
import tools.Triple;
import tools.packet.CField;
import tools.packet.CWvsContext;

public class NPCConversationManager extends AbstractPlayerInteraction {

    private String getText;
    private byte type;
    private byte lastMsg = -1;
    private int questid;
    private int channel;
    public boolean pendingDisposal = false;
    private Invocable iv;
    private PlayerStorage players;

    public NPCConversationManager(MapleClient c, int npc, int questid, byte type, Invocable iv) {
        super(c, npc, questid);
        this.type = type;
        this.iv = iv;
    }

    public Invocable getIv() {
        return this.iv;
    }

    public int getNpc() {
        return this.id;
    }

    public void sendSlideMenu(final int type, final String sel) {
        if (lastMsg > -1) {
            return;
        }
        int lasticon = 0;
        //if (type == 0 && sel.contains("#")) {
        //    String splitted[] = sel.split("#");
        //    lasticon = Integer.parseInt(splitted[splitted.length - 2]);
        //    if (lasticon < 0) {
        //        lasticon = 0;
        //    }
        //}
        c.getSession().write(CField.NPCPacket.getSlideMenu(id, type, lasticon, sel));
        lastMsg = 0x12;//was12
    }

    public String getDimensionalMirror(MapleCharacter character) {
        return MapleSlideMenu.SlideMenu0.getSelectionInfo(character, id);
    }
    
    public String getSlideMenuSelection(int type) {
        switch (type) {
            case 0:
                return SlideMenu0.getSelectionInfo(getPlayer(), id);
            case 1:
                return SlideMenu1.getSelectionInfo(getPlayer(), id);
            case 2:
                return SlideMenu2.getSelectionInfo(getPlayer(), id);
            case 3:
                return SlideMenu3.getSelectionInfo(getPlayer(), id);
            case 4:
                return SlideMenu4.getSelectionInfo(getPlayer(), id);
            case 5:
                return SlideMenu5.getSelectionInfo(getPlayer(), id);
            default:
                return SlideMenu0.getSelectionInfo(getPlayer(), id);
        }
    }

    public int getSlideMenuDataInteger(int type) {
        switch (type) {
            case 0:
                return SlideMenu0.getDataInteger(type);
            case 1:
                return SlideMenu1.getDataInteger(type);
            case 2:
                return SlideMenu2.getDataInteger(type);
            case 3:
                return SlideMenu3.getDataInteger(type);
            case 4:
                return SlideMenu4.getDataInteger(type);
            case 5:
                return SlideMenu5.getDataInteger(type);
            default:
                return SlideMenu0.getDataInteger(type);
        }
    }

    
    public int getQuest() {
        return this.id2;
    }

    public byte getType() {
        return this.type;
    }

    public void safeDispose() {
        this.pendingDisposal = true;
    }

    public void dispose() {
        NPCScriptManager.getInstance().dispose(this.c);
    }

    public void askMapSelection(String sel) {
        if (this.lastMsg > -1) {
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getMapSelection(this.id, sel));
        this.lastMsg = ((byte) (GameConstants.GMS ? 17 : 16));
    }

    public void sendNext(String text) {
        sendNext(text, this.id);
    }

    public void sendNext(String text, int id) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(id, (byte) 0, text, "00 01", (byte) 0));
        this.lastMsg = 0;
    }

    public void sendPlayerToNpc(String text) {
        sendNextS(text, (byte) 3, this.id);
    }

    public void sendNextNoESC(String text) {
        sendNextS(text, (byte) 1, this.id);
    }

    public void sendNextNoESC(String text, int id) {
        sendNextS(text, (byte) 1, id);
    }

    public void sendNextS(String text, byte type) {
        sendNextS(text, type, this.id);
    }

    public void sendNextS(String text, byte type, int idd) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimpleS(text, type);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(this.id, (byte) 0, text, "00 01", type, idd));
        this.lastMsg = 0;
    }

    public void sendPrev(String text) {
        sendPrev(text, this.id);
    }

    public void sendPrev(String text, int id) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(id, (byte) 0, text, "01 00", (byte) 0));
        this.lastMsg = 0;
    }

    public void sendPrevS(String text, byte type) {
        sendPrevS(text, type, this.id);
    }

    public void sendPrevS(String text, byte type, int idd) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimpleS(text, type);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(this.id, (byte) 0, text, "01 00", type, idd));
        this.lastMsg = 0;
    }

    public void sendNextPrev(String text) {
        sendNextPrev(text, this.id);
    }

    public void sendNextPrev(String text, int id) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(id, (byte) 0, text, "01 01", (byte) 0));
        this.lastMsg = 0;
    }

    public void PlayerToNpc(String text) {
        sendNextPrevS(text, (byte) 3);
    }

    public void sendNextPrevS(String text) {
        sendNextPrevS(text, (byte) 3);
    }

    public void sendNextPrevS(String text, byte type) {
        sendNextPrevS(text, type, this.id);
    }

    public void sendNextPrevS(String text, byte type, int idd) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimpleS(text, type);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(this.id, (byte) 0, text, "01 01", type, idd));
        this.lastMsg = 0;
    }

    public void sendOk(String text) {
        sendOk(text, this.id);
    }

    public void sendOk(String text, int id) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(id, (byte) 0, text, "00 00", (byte) 0));
        this.lastMsg = 0;
    }

    public void sendOkS(String text, byte type) {
        sendOkS(text, type, this.id);
    }

    public void sendOkS(String text, byte type, int idd) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimpleS(text, type);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(this.id, (byte) 0, text, "00 00", type, idd));
        this.lastMsg = 0;
    }

    public void sendYesNo(String text) {
        sendYesNo(text, this.id);
    }

    public void sendYesNo(String text, int id) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(id, (byte) 2, text, "", (byte) 0));
        this.lastMsg = 2;
    }

    public void sendYesNoS(String text, byte type) {
        sendYesNoS(text, type, this.id);
    }

    public void sendYesNoS(String text, byte type, int idd) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimpleS(text, type);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(this.id, (byte) 2, text, "", type, idd));
        this.lastMsg = 2;
    }

    public void sendAcceptDecline(String text) {
        askAcceptDecline(text);
    }

    public void sendAcceptDeclineNoESC(String text) {
        askAcceptDeclineNoESC(text);
    }

    public void askAcceptDecline(String text) {
        askAcceptDecline(text, this.id);
    }

    public void askAcceptDecline(String text, int id) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.lastMsg = ((byte) (GameConstants.GMS ? 16 : 14));
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(id, this.lastMsg, text, "", (byte) 0));
    }

    public void askAcceptDecline1(String text, byte last) {
        askAcceptDecline1(text, this.id, last);
    }

    public void askAcceptDecline1(String text, int id, byte last) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.lastMsg = ((byte) (GameConstants.GMS ? 15 : 14));
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(id, last, text, "", (byte) 0));
    }

    public void askAcceptDeclineNoESC(String text) {
        askAcceptDeclineNoESC(text, this.id);
    }

    public void askAcceptDeclineNoESC(String text, int id) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.lastMsg = ((byte) (GameConstants.GMS ? 15 : 14));
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(id, this.lastMsg, text, "", (byte) 1));
    }

    public void askAvatar(String text, int[] args) {
        if (this.lastMsg > -1) {
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalkStyle(this.id, text, args));
        this.lastMsg = 9;
    }

    public void sendSimple(String text) {
        sendSimple(text, this.id);
    }

    public void sendSimple(String text, int id) {
        if (this.lastMsg > -1) {
            return;
        }
        if (!text.contains("#L")) {
            sendNext(text);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(id, (byte) 5, text, "", (byte) 0));
        this.lastMsg = 5;
    }

    public void sendSimpleS(String text, byte type) {
        sendSimpleS(text, type, this.id);
    }

    public void sendSimpleS(String text, byte type, int idd) {
        if (this.lastMsg > -1) {
            return;
        }
        if (!text.contains("#L")) {
            sendNextS(text, type);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalk(this.id, (byte) 5, text, "", type, idd));
        this.lastMsg = 5;
    }

    public void sendStyle(String text, int[] styles) {
        if (this.lastMsg > -1) {
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalkStyle(this.id, text, styles));
        this.lastMsg = 9;
    }

    public void sendGetNumber(String text, int def, int min, int max) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalkNum(this.id, text, def, min, max));
        this.lastMsg = 4;
    }

    public void sendGetText(String text) {
        sendGetText(text, this.id);
    }

    public void sendGetText(String text, int id) {
        if (this.lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) {
            sendSimple(text);
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getNPCTalkText(id, text));
        this.lastMsg = 3;
    }

    public void setGetText(String text) {
        this.getText = text;
    }

    public String getText() {
        return this.getText;
    }

    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.HAIR, hair);
        getPlayer().equipChanged();
    }

    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.FACE, face);
        getPlayer().equipChanged();
    }

    public void setSkin(int color) {
        getPlayer().setSkinColor((byte) color);
        getPlayer().updateSingleStat(MapleStat.SKIN, color);
        getPlayer().equipChanged();
    }

    public int setRandomAvatar(int ticket, int[] args_all) {
        if (!haveItem(ticket)) {
            return -1;
        }
        gainItem(ticket, (short) -1);

        int args = args_all[Randomizer.nextInt(args_all.length)];
        if (args < 100) {
            this.c.getPlayer().setSkinColor((byte) args);
            this.c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
        } else if (args < 30000) {
            this.c.getPlayer().setFace(args);
            this.c.getPlayer().updateSingleStat(MapleStat.FACE, args);
        } else {
            this.c.getPlayer().setHair(args);
            this.c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
        }
        this.c.getPlayer().equipChanged();

        return 1;
    }

    public int setAvatar(int ticket, int args) {
        if (!haveItem(ticket)) {
            return -1;
        }
        gainItem(ticket, (short) -1);

        if (args < 100) {
            this.c.getPlayer().setSkinColor((byte) args);
            this.c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
        } else if (args < 30000) {
            this.c.getPlayer().setFace(args);
            this.c.getPlayer().updateSingleStat(MapleStat.FACE, args);
        } else {
            this.c.getPlayer().setHair(args);
            this.c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
        }
        this.c.getPlayer().equipChanged();

        return 1;
    }

    public void sendStorage() {
        this.c.getPlayer().setConversation(4);
        this.c.getPlayer().getStorage().sendStorage(this.c, this.id);
    }

    public void openShop(int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(this.c);
    }

    public void openShopNPC(int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(this.c, this.id);
    }

    public int gainGachaponItem(int id, int quantity) {
        return gainGachaponItem(id, quantity, this.c.getPlayer().getMap().getStreetName());
    }

    public int gainGachaponItem(int id, int quantity, String msg) {
        return gainGachaponItem(id, quantity, this.c.getPlayer().getMap().getStreetName(), (byte) 0);
    }

    public int gainGachaponItem(int id, int quantity, String msg, byte rareness) {
        try {
            if (!MapleItemInformationProvider.getInstance().itemExists(id)) {
                return -1;
            }
            Item item = MapleInventoryManipulator.addbyId_Gachapon(this.c, id, (short) quantity);

            if (item == null) {
                return -1;
            }
            if (rareness == 0) {
                rareness = GameConstants.gachaponRareItem(item.getItemId());
            }
            if (rareness > 0) {
                World.Broadcast.broadcastMessage(CWvsContext.getGachaponMega(this.c.getPlayer().getName(), " : got a(n)", item, rareness, msg));
            }
            this.c.getSession().write(CWvsContext.InfoPacket.getShowItemGain(item.getItemId(), (short) quantity, true));
            return item.getItemId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int useNebuliteGachapon() {
        try {
            if ((this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < 1) || (this.c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() < 1) || (this.c.getPlayer().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < 1) || (this.c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() < 1) || (this.c.getPlayer().getInventory(MapleInventoryType.CASH).getNumFreeSlot() < 1)) {
                return -1;
            }
            int grade = 0;
            int chance = Randomizer.nextInt(100);
            if (chance < 1) {
                grade = 3;
            } else if (chance < 5) {
                grade = 2;
            } else if (chance < 35) {
                grade = 1;
            } else {
                grade = Randomizer.nextInt(100) < 25 ? 5 : 0;
            }
            int newId = 0;
            if (grade == 5) {
                newId = 4420000;
            } else {
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                List pots = new LinkedList(ii.getAllSocketInfo(grade).values());
                while (newId == 0) {
                    StructItemOption pot = (StructItemOption) pots.get(Randomizer.nextInt(pots.size()));
                    if (pot != null) {
                        newId = pot.opID;
                    }
                }
            }
            Item item = MapleInventoryManipulator.addbyId_Gachapon(this.c, newId, (short) 1);
            if (item == null) {
                return -1;
            }
            if ((grade >= 2) && (grade != 5)) {
                World.Broadcast.broadcastMessage(CWvsContext.getGachaponMega(this.c.getPlayer().getName(), " : got a(n)", item, (byte) 0, "Maple World"));
            }
            this.c.getSession().write(CWvsContext.InfoPacket.getShowItemGain(newId, (short) 1, true));
            gainItem(2430748, (short) 1);
            gainItemSilent(5220094, (short) -1);
            return item.getItemId();
        } catch (Exception e) {
            System.out.println(new StringBuilder().append("[Error] Failed to use Nebulite Gachapon. ").append(e).toString());
        }
        return -1;
    }

    public void changeJob(short job) {
        this.c.getPlayer().changeJob(job);
    }

    public void startQuest(int idd) {
        MapleQuest.getInstance(idd).start(getPlayer(), this.id);
    }

    public void completeQuest(int idd) {
        MapleQuest.getInstance(idd).complete(getPlayer(), this.id);
    }

    public void forfeitQuest(int idd) {
        MapleQuest.getInstance(idd).forfeit(getPlayer());
    }

    public void forceStartQuest() {
        MapleQuest.getInstance(this.id2).forceStart(getPlayer(), getNpc(), null);
    }

    public void forceStartQuest(int idd) {
        MapleQuest.getInstance(idd).forceStart(getPlayer(), getNpc(), null);
    }

    public void forceStartQuest(String customData) {
        MapleQuest.getInstance(this.id2).forceStart(getPlayer(), getNpc(), customData);
    }

    public void forceCompleteQuest() {
        MapleQuest.getInstance(this.id2).forceComplete(getPlayer(), getNpc());
    }

    public void forceCompleteQuest(int idd) {
        MapleQuest.getInstance(idd).forceComplete(getPlayer(), getNpc());
    }

    public String getQuestCustomData() {
        return this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(this.id2)).getCustomData();
    }

    public void setQuestCustomData(String customData) {
        getPlayer().getQuestNAdd(MapleQuest.getInstance(this.id2)).setCustomData(customData);
    }

    public String getQuestCustomData(int qid) {
        return this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(qid)).getCustomData();
    }

    public void setQuestCustomData(int qid, String customData) {
        getPlayer().getQuestNAdd(MapleQuest.getInstance(qid)).setCustomData(customData);
    }

    public long getMeso() {
        return getPlayer().getMeso();
    }

    public void gainAp(int amount) {
        this.c.getPlayer().gainAp((short) amount);
    }

    public void expandInventory(byte type, int amt) {
        this.c.getPlayer().expandInventory(type, amt);
    }

    public void unequipEverything() {
        MapleInventory equipped = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
        MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
        List ids = new LinkedList();
        for (Item item : equipped.newList()) {
            ids.add(Short.valueOf(item.getPosition()));
        }
        for (Iterator i$ = ids.iterator(); i$.hasNext();) {
            short id = ((Short) i$.next()).shortValue();
            MapleInventoryManipulator.unequip(getC(), id, equip.getNextFreeSlot());
        }
    }

    public final void clearSkills() {
        final Map<Skill, SkillEntry> skills = new HashMap<>(getPlayer().getSkills());
        final Map<Skill, SkillEntry> newList = new HashMap<>();
        for (Entry<Skill, SkillEntry> skill : skills.entrySet()) {
            newList.put(skill.getKey(), new SkillEntry((byte) 0, (byte) 0, -1));
        }
        getPlayer().changeSkillsLevel(newList);
        newList.clear();
        skills.clear();
    }

    public boolean hasSkill(int skillid) {
        Skill theSkill = SkillFactory.getSkill(skillid);
        if (theSkill != null) {
            return this.c.getPlayer().getSkillLevel(theSkill) > 0;
        }
        return false;
    }

    public void showEffect(boolean broadcast, String effect) {
        if (broadcast) {
            this.c.getPlayer().getMap().broadcastMessage(CField.showEffect(effect));
        } else {
            this.c.getSession().write(CField.showEffect(effect));
        }
    }

    public void playSound(boolean broadcast, String sound) {
        if (broadcast) {
            this.c.getPlayer().getMap().broadcastMessage(CField.playSound(sound));
        } else {
            this.c.getSession().write(CField.playSound(sound));
        }
    }

    public void environmentChange(boolean broadcast, String env) {
        if (broadcast) {
            this.c.getPlayer().getMap().broadcastMessage(CField.environmentChange(env, 2));
        } else {
            this.c.getSession().write(CField.environmentChange(env, 2));
        }
    }

    public void updateBuddyCapacity(int capacity) {
        this.c.getPlayer().setBuddyCapacity((byte) capacity);
    }

    public int getBuddyCapacity() {
        return this.c.getPlayer().getBuddyCapacity();
    }

    public int partyMembersInMap() {
        int inMap = 0;
        if (getPlayer().getParty() == null) {
            return inMap;
        }
        for (MapleCharacter char2 : getPlayer().getMap().getCharactersThreadsafe()) {
            if ((char2.getParty() != null) && (char2.getParty().getId() == getPlayer().getParty().getId())) {
                inMap++;
            }
        }
        return inMap;
    }

    public List<MapleCharacter> getPartyMembers() {
        if (getPlayer().getParty() == null) {
            return null;
        }
        List<MapleCharacter> chars = new LinkedList<MapleCharacter>(); // creates an empty array full of shit..
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            for (ChannelServer channel : ChannelServer.getAllInstances()) {
                MapleCharacter ch = channel.getPlayerStorage().getCharacterById(chr.getId());
                if (ch != null) { // double check <3
                    chars.add(ch);
                }
            }
        }
        return chars;
    }

    public void warpPartyWithExp(int mapId, int exp) {
        if (getPlayer().getParty() == null) {
            warp(mapId, 0);
            gainExp(exp);
            return;
        }
        MapleMap target = getMap(mapId);
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = this.c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
            if (((curChar.getEventInstance() == null) && (getPlayer().getEventInstance() == null)) || (curChar.getEventInstance() == getPlayer().getEventInstance())) {
                curChar.changeMap(target, target.getPortal(0));
                curChar.gainExp(exp, true, false, true);
            }
        }
    }

    public void warpPartyWithExpMeso(int mapId, int exp, int meso) {
        if (getPlayer().getParty() == null) {
            warp(mapId, 0);
            gainExp(exp);
            gainMeso(meso);
            return;
        }
        MapleMap target = getMap(mapId);
        for (MaplePartyCharacter chr : getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = this.c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName());
            if (((curChar.getEventInstance() == null) && (getPlayer().getEventInstance() == null)) || (curChar.getEventInstance() == getPlayer().getEventInstance())) {
                curChar.changeMap(target, target.getPortal(0));
                curChar.gainExp(exp, true, false, true);
                curChar.gainMeso(meso, true);
            }
        }
    }

    public MapleSquad getSquad(String type) {
        return this.c.getChannelServer().getMapleSquad(type);
    }

    public int getSquadAvailability(String type) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        }
        return squad.getStatus();
    }

    public boolean registerSquad(String type, int minutes, String startText) {
        if (this.c.getChannelServer().getMapleSquad(type) == null) {
            MapleSquad squad = new MapleSquad(this.c.getChannel(), type, this.c.getPlayer(), minutes * 60 * 1000, startText);
            boolean ret = this.c.getChannelServer().addMapleSquad(squad, type);
            if (ret) {
                MapleMap map = this.c.getPlayer().getMap();

                map.broadcastMessage(CField.getClock(minutes * 60));
                map.broadcastMessage(CWvsContext.serverNotice(6, new StringBuilder().append(this.c.getPlayer().getName()).append(startText).toString()));
            } else {
                squad.clear();
            }
            return ret;
        }
        return false;
    }

    public boolean getSquadList(String type, byte type_) {
        try {
            MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
            if (squad == null) {
                return false;
            }
            if ((type_ == 0) || (type_ == 3)) {
                sendNext(squad.getSquadMemberString(type_));
            } else if (type_ == 1) {
                sendSimple(squad.getSquadMemberString(type_));
            } else if (type_ == 2) {
                if (squad.getBannedMemberSize() > 0) {
                    sendSimple(squad.getSquadMemberString(type_));
                } else {
                    sendNext(squad.getSquadMemberString(type_));
                }
            }
            return true;
        } catch (NullPointerException ex) {
            FileoutputUtil.outputFileError("Log_Script_Except.txt", ex);
        }
        return false;
    }

    public byte isSquadLeader(String type) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        }
        if ((squad.getLeader() != null) && (squad.getLeader().getId() == this.c.getPlayer().getId())) {
            return 1;
        }
        return 0;
    }

    public boolean reAdd(String eim, String squad) {
        EventInstanceManager eimz = getDisconnected(eim);
        MapleSquad squadz = getSquad(squad);
        if ((eimz != null) && (squadz != null)) {
            squadz.reAddMember(getPlayer());
            eimz.registerPlayer(getPlayer());
            return true;
        }
        return false;
    }

    public void banMember(String type, int pos) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.banMember(pos);
        }
    }

    public void acceptMember(String type, int pos) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.acceptMember(pos);
        }
    }

    public int addMember(String type, boolean join) {
        try {
            MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
            if (squad != null) {
                return squad.addMember(this.c.getPlayer(), join);
            }
            return -1;
        } catch (NullPointerException ex) {
            FileoutputUtil.outputFileError("Log_Script_Except.txt", ex);
        }
        return -1;
    }

    public byte isSquadMember(String type) {
        MapleSquad squad = this.c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        }
        if (squad.getMembers().contains(this.c.getPlayer())) {
            return 1;
        }
        if (squad.isBanned(this.c.getPlayer())) {
            return 2;
        }
        return 0;
    }

    public void resetReactors() {
        getPlayer().getMap().resetReactors();
    }

    public void genericGuildMessage(int code) {
        this.c.getSession().write(CWvsContext.GuildPacket.genericGuildMessage((byte) code));
    }

    public void disbandGuild() {
        int gid = this.c.getPlayer().getGuildId();
        if ((gid <= 0) || (this.c.getPlayer().getGuildRank() != 1)) {
            return;
        }
        World.Guild.disbandGuild(gid);
    }

    public void increaseGuildCapacity(boolean trueMax) {
        if ((this.c.getPlayer().getMeso() < 500000L) && (!trueMax)) {
            this.c.getSession().write(CWvsContext.serverNotice(1, "You do not have enough mesos."));
            return;
        }
        int gid = this.c.getPlayer().getGuildId();
        if (gid <= 0) {
            return;
        }
        if (World.Guild.increaseGuildCapacity(gid, trueMax)) {
            if (!trueMax) {
                this.c.getPlayer().gainMeso(-500000L, true, true);
            } else {
                gainGP(-25000);
            }
            sendNext("Your guild capacity has been raised...");
        } else if (!trueMax) {
            sendNext("Please check if your guild capacity is full. (Limit: 100)");
        } else {
            sendNext("Please check if your guild capacity is full, if you have the GP needed or if subtracting GP would decrease a guild level. (Limit: 200)");
        }
    }

    public void displayGuildRanks() {
        this.c.getSession().write(CWvsContext.GuildPacket.showGuildRanks(this.id, MapleGuildRanking.getInstance().getRank()));
    }

    public boolean removePlayerFromInstance() {
        if (this.c.getPlayer().getEventInstance() != null) {
            this.c.getPlayer().getEventInstance().removePlayer(this.c.getPlayer());
            return true;
        }
        return false;
    }

    public boolean isPlayerInstance() {
        if (this.c.getPlayer().getEventInstance() != null) {
            return true;
        }
        return false;
    }

    public void makeTaintedEquip(byte slot) {
        Equip sel = (Equip) this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) slot);
        sel.setStr((short) 69);
        sel.setDex((short) 69);
        sel.setInt((short) 69);
        sel.setLuk((short) 69);
        sel.setHp((short) 69);
        sel.setMp((short) 69);
        sel.setWatk((short) 69);
        sel.setMatk((short) 69);
        sel.setWdef((short) 69);
        sel.setMdef((short) 69);
        sel.setAcc((short) 69);
        sel.setAvoid((short) 69);
        sel.setHands((short) 69);
        sel.setSpeed((short) 69);
        sel.setJump((short) 69);
        sel.setUpgradeSlots((byte) 69);
        sel.setViciousHammer((byte) 69);
        sel.setEnhance((byte) 69);
        this.c.getPlayer().equipChanged();
        this.c.getPlayer().fakeRelog();
    }

    public void changeStat(byte slot, int type, int amount) {
        Equip sel = (Equip) this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) slot);
        switch (type) {
            case 0:
                sel.setStr((short) amount);
                break;
            case 1:
                sel.setDex((short) amount);
                break;
            case 2:
                sel.setInt((short) amount);
                break;
            case 3:
                sel.setLuk((short) amount);
                break;
            case 4:
                sel.setHp((short) amount);
                break;
            case 5:
                sel.setMp((short) amount);
                break;
            case 6:
                sel.setWatk((short) amount);
                break;
            case 7:
                sel.setMatk((short) amount);
                break;
            case 8:
                sel.setWdef((short) amount);
                break;
            case 9:
                sel.setMdef((short) amount);
                break;
            case 10:
                sel.setAcc((short) amount);
                break;
            case 11:
                sel.setAvoid((short) amount);
                break;
            case 12:
                sel.setHands((short) amount);
                break;
            case 13:
                sel.setSpeed((short) amount);
                break;
            case 14:
                sel.setJump((short) amount);
                break;
            case 15:
                sel.setUpgradeSlots((byte) amount);
                break;
            case 16:
                sel.setViciousHammer((byte) amount);
                break;
            case 17:
                sel.setLevel((byte) amount);
                break;
            case 18:
                sel.setEnhance((byte) amount);
                break;
            case 19:
                sel.setPotential1(amount);
                break;
            case 20:
                sel.setPotential2(amount);
                break;
            case 21:
                sel.setPotential3(amount);
                break;
            case 22:
                sel.setPotential4(amount);
                break;
            case 23:
                sel.setPotential5(amount);
                break;
            case 24:
                sel.setOwner(getText());
                break;
        }

        this.c.getPlayer().equipChanged();
        this.c.getPlayer().fakeRelog();
    }

    public void openDuey() {
        this.c.getPlayer().setConversation(2);
        this.c.getSession().write(CField.sendDuey((byte) 9, null));
    }

    public void openMerchantItemStore() {
        this.c.getPlayer().setConversation(3);
        HiredMerchantHandler.displayMerch(this.c);

        this.c.getSession().write(CWvsContext.enableActions());
    }

    public void sendPVPWindow() {
        this.c.getSession().write(CField.UIPacket.openUI(50));
        this.c.getSession().write(CField.sendPVPMaps());
    }

    public void sendAzwanWindow() {
        this.c.getSession().write(CField.UIPacket.openUI(70));
    }

    public void sendRepairWindow() {
        this.c.getSession().write(CField.UIPacket.sendRepairWindow(this.id));
    }

    public void sendRedLeaf() {
        this.c.getSession().write(CField.UIPacket.sendRedLeaf(this.id));
    }

    public void sendProfessionWindow() {
        this.c.getSession().write(CField.UIPacket.openUI(42));
    }

    public final int getDojoPoints() {
        return dojo_getPts();
    }

    public final int getDojoRecord() {
        return this.c.getPlayer().getIntNoRecord(150101);
    }

    public void setDojoRecord(boolean reset) {
        if (reset) {
            this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(150101)).setCustomData("0");
            this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(150100)).setCustomData("0");
        } else {
            this.c.getPlayer().getQuestNAdd(MapleQuest.getInstance(150101)).setCustomData(String.valueOf(this.c.getPlayer().getIntRecord(150101) + 1));
        }
    }

    public boolean start_DojoAgent(boolean dojo, boolean party) {
        if (dojo) {
            return Event_DojoAgent.warpStartDojo(this.c.getPlayer(), party);
        }
        return Event_DojoAgent.warpStartAgent(this.c.getPlayer(), party);
    }

    public boolean start_PyramidSubway(int pyramid) {
        if (pyramid >= 0) {
            return Event_PyramidSubway.warpStartPyramid(this.c.getPlayer(), pyramid);
        }
        return Event_PyramidSubway.warpStartSubway(this.c.getPlayer());
    }

    public boolean bonus_PyramidSubway(int pyramid) {
        if (pyramid >= 0) {
            return Event_PyramidSubway.warpBonusPyramid(this.c.getPlayer(), pyramid);
        }
        return Event_PyramidSubway.warpBonusSubway(this.c.getPlayer());
    }

    public final short getKegs() {
        return this.c.getChannelServer().getFireWorks().getKegsPercentage();
    }

    public void giveKegs(int kegs) {
        this.c.getChannelServer().getFireWorks().giveKegs(this.c.getPlayer(), kegs);
    }

    public final short getSunshines() {
        return this.c.getChannelServer().getFireWorks().getSunsPercentage();
    }

    public void addSunshines(int kegs) {
        this.c.getChannelServer().getFireWorks().giveSuns(this.c.getPlayer(), kegs);
    }

    public final short getDecorations() {
        return this.c.getChannelServer().getFireWorks().getDecsPercentage();
    }

    public void addDecorations(int kegs) {
        try {
            this.c.getChannelServer().getFireWorks().giveDecs(this.c.getPlayer(), kegs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final MapleCarnivalParty getCarnivalParty() {
        return this.c.getPlayer().getCarnivalParty();
    }

    public final MapleCarnivalChallenge getNextCarnivalRequest() {
        return this.c.getPlayer().getNextCarnivalRequest();
    }

    public final MapleCarnivalChallenge getCarnivalChallenge(MapleCharacter chr) {
        return new MapleCarnivalChallenge(chr);
    }

    public final int getChannel() {
        return this.channel;
    }

    public void warpMihile() {
        warpMap(104000000, 0);
        this.c.removeClickedNPC();
    }

    public void maxStats() {
        Map statup = new EnumMap(MapleStat.class);
        this.c.getPlayer().getStat().str = 32767;
        this.c.getPlayer().getStat().dex = 32767;
        this.c.getPlayer().getStat().int_ = 32767;
        this.c.getPlayer().getStat().luk = 32767;

        int overrDemon = GameConstants.isDemon(this.c.getPlayer().getJob()) ? GameConstants.getMPByJob(this.c.getPlayer().getJob()) : 99999;
        this.c.getPlayer().getStat().maxhp = 500000;
        this.c.getPlayer().getStat().maxmp = overrDemon;
        this.c.getPlayer().getStat().setHp(500000, this.c.getPlayer());
        this.c.getPlayer().getStat().setMp(overrDemon, this.c.getPlayer());

        statup.put(MapleStat.STR, Integer.valueOf(32767));
        statup.put(MapleStat.DEX, Integer.valueOf(32767));
        statup.put(MapleStat.LUK, Integer.valueOf(32767));
        statup.put(MapleStat.INT, Integer.valueOf(32767));
        statup.put(MapleStat.HP, Integer.valueOf(500000));
        statup.put(MapleStat.MAXHP, Integer.valueOf(500000));
        statup.put(MapleStat.MP, Integer.valueOf(overrDemon));
        statup.put(MapleStat.MAXMP, Integer.valueOf(overrDemon));
        this.c.getPlayer().getStat().recalcLocalStats(this.c.getPlayer());
    }

    public int setAndroid(int args) {
        if (args < 30000) {
            this.c.getPlayer().getAndroid().setFace(args);
            this.c.getPlayer().getAndroid().saveToDb();
        } else {
            this.c.getPlayer().getAndroid().setHair(args);
            this.c.getPlayer().getAndroid().saveToDb();
        }
        this.c.getPlayer().equipChanged();

        return 1;
    }

    public int getAndroidStat(String type) {
        if (type.equals("HAIR")) {
            return this.c.getPlayer().getAndroid().getHair();
        }
        if (type.equals("FACE")) {
            return this.c.getPlayer().getAndroid().getFace();
        }
        if (type.equals("GENDER")) {
            int itemid = this.c.getPlayer().getAndroid().getItemId();
            if ((itemid == 1662000) || (itemid == 1662002)) {
                return 0;
            }
            return 1;
        }

        return -1;
    }

    public void reloadChar() {
        getPlayer().getClient().getSession().write(CField.getCharInfo(getPlayer()));
        getPlayer().getMap().removePlayer(getPlayer());
        getPlayer().getMap().addPlayer(getPlayer());
    }
    
            public void ReloadMap() {

      //  @Override
   //     public int execute(MapleClient c, String[] splitted) {
            MapleMap oldmap = c.getPlayer().getMap();
            MapleMap newmap = c.getChannelServer().getMapFactory().getMap(c.getPlayer().getMapId(), true, true, true);
            for (MapleCharacter chr : c.getPlayer().getMap().getCharactersThreadsafe()) {
                chr.changeMap(newmap);
            }
            oldmap = null;
            c.getPlayer().getMap().respawn(true);
        }


    public void askAndroid(String text, int[] args) {
        if (this.lastMsg > -1) {
            return;
        }
        this.c.getSession().write(CField.NPCPacket.getAndroidTalkStyle(this.id, text, args));
        this.lastMsg = 10;
    }

    public MapleCharacter getChar() {
        return getPlayer();
    }

    public static int editEquipById(MapleCharacter chr, int max, int itemid, String stat, int newval) {
        return editEquipById(chr, max, itemid, stat, (short) newval);
    }

    public static int editEquipById(MapleCharacter chr, int max, int itemid, String stat, short newval) {
        if (!MapleItemInformationProvider.getInstance().isEquip(itemid)) {
            return -1;
        }

        List<Item> equips = chr.getInventory(MapleInventoryType.EQUIP).listById(itemid);
        List<Item> equipped = chr.getInventory(MapleInventoryType.EQUIPPED).listById(itemid);

        if ((equips.size() == 0) && (equipped.size() == 0)) {
            return 0;
        }

        int edited = 0;

        for (Item itm : equips) {
            Equip e = (Equip) itm;
            if (edited >= max) {
                break;
            }
            edited++;
            if (stat.equals("str")) {
                e.setStr(newval);
            } else if (stat.equals("dex")) {
                e.setDex(newval);
            } else if (stat.equals("int")) {
                e.setInt(newval);
            } else if (stat.equals("luk")) {
                e.setLuk(newval);
            } else if (stat.equals("watk")) {
                e.setWatk(newval);
            } else if (stat.equals("matk")) {
                e.setMatk(newval);
            } else {
                return -2;
            }
        }
        for (Item itm : equipped) {
            Equip e = (Equip) itm;
            if (edited >= max) {
                break;
            }
            edited++;
            if (stat.equals("str")) {
                e.setStr(newval);
            } else if (stat.equals("dex")) {
                e.setDex(newval);
            } else if (stat.equals("int")) {
                e.setInt(newval);
            } else if (stat.equals("luk")) {
                e.setLuk(newval);
            } else if (stat.equals("watk")) {
                e.setWatk(newval);
            } else if (stat.equals("matk")) {
                e.setMatk(newval);
            } else {
                return -2;
            }

        }

        return edited;
    }

    public int getReborns() {
        return getPlayer().getReborns();
    }

    public Triple<String, Map<Integer, String>, Long> getSpeedRun(String typ) {
        ExpeditionType type = ExpeditionType.valueOf(typ);
        if (SpeedRunner.getSpeedRunData(type) != null) {
            return SpeedRunner.getSpeedRunData(type);
        }
        return new Triple("", new HashMap(), Long.valueOf(0L));
    }

    public boolean getSR(Triple<String, Map<Integer, String>, Long> ma, int sel) {
        if ((((Map) ma.mid).get(Integer.valueOf(sel)) == null) || (((String) ((Map) ma.mid).get(Integer.valueOf(sel))).length() <= 0)) {
            dispose();
            return false;
        }
        sendOk((String) ((Map) ma.mid).get(Integer.valueOf(sel)));
        return true;
    }

    public Equip getEquip(int itemid) {
        return (Equip) MapleItemInformationProvider.getInstance().getEquipById(itemid);
    }

    public void setExpiration(Object statsSel, long expire) {
        if ((statsSel instanceof Equip)) {
            ((Equip) statsSel).setExpiration(System.currentTimeMillis() + expire * 24L * 60L * 60L * 1000L);
        }
    }

    public void setLock(Object statsSel) {
        if ((statsSel instanceof Equip)) {
            Equip eq = (Equip) statsSel;
            if (eq.getExpiration() == -1L) {
                eq.setFlag((short) (byte) (eq.getFlag() | ItemFlag.LOCK.getValue()));
            } else {
                eq.setFlag((short) (byte) (eq.getFlag() | ItemFlag.UNTRADEABLE.getValue()));
            }
        }
    }

    public boolean addFromDrop(Object statsSel) {
        if ((statsSel instanceof Item)) {
            Item it = (Item) statsSel;
            return (MapleInventoryManipulator.checkSpace(getClient(), it.getItemId(), it.getQuantity(), it.getOwner())) && (MapleInventoryManipulator.addFromDrop(getClient(), it, false));
        }
        return false;
    }

    public int getVPoints() {
        return getPlayer().getVPoints();
    }

    public void setVPoints(int fuck) {
        getPlayer().setVPoints(getPlayer().getVPoints() + fuck);
    }

    public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type) {
        return replaceItem(slot, invType, statsSel, offset, type, false);
    }

    public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type, boolean takeSlot) {
        MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
        if (inv == null) {
            return false;
        }
        Item item = getPlayer().getInventory(inv).getItem((short) (byte) slot);
        if ((item == null) || ((statsSel instanceof Item))) {
            item = (Item) statsSel;
        }
        if (offset > 0) {
            if (inv != MapleInventoryType.EQUIP) {
                return false;
            }
            Equip eq = (Equip) item;
            if (takeSlot) {
                if (eq.getUpgradeSlots() < 1) {
                    return false;
                }
                eq.setUpgradeSlots((byte) (eq.getUpgradeSlots() - 1));

                if (eq.getExpiration() == -1L) {
                    eq.setFlag((short) (byte) (eq.getFlag() | ItemFlag.LOCK.getValue()));
                } else {
                    eq.setFlag((short) (byte) (eq.getFlag() | ItemFlag.UNTRADEABLE.getValue()));
                }
            }
            if (type.equalsIgnoreCase("Slots")) {
                eq.setUpgradeSlots((byte) (eq.getUpgradeSlots() + offset));
                eq.setViciousHammer((byte) (eq.getViciousHammer() + offset));
            } else if (type.equalsIgnoreCase("Level")) {
                eq.setLevel((byte) (eq.getLevel() + offset));
            } else if (type.equalsIgnoreCase("Hammer")) {
                eq.setViciousHammer((byte) (eq.getViciousHammer() + offset));
            } else if (type.equalsIgnoreCase("STR")) {
                eq.setStr((short) (eq.getStr() + offset));
            } else if (type.equalsIgnoreCase("DEX")) {
                eq.setDex((short) (eq.getDex() + offset));
            } else if (type.equalsIgnoreCase("INT")) {
                eq.setInt((short) (eq.getInt() + offset));
            } else if (type.equalsIgnoreCase("LUK")) {
                eq.setLuk((short) (eq.getLuk() + offset));
            } else if (type.equalsIgnoreCase("HP")) {
                eq.setHp((short) (eq.getHp() + offset));
            } else if (type.equalsIgnoreCase("MP")) {
                eq.setMp((short) (eq.getMp() + offset));
            } else if (type.equalsIgnoreCase("WATK")) {
                eq.setWatk((short) (eq.getWatk() + offset));
            } else if (type.equalsIgnoreCase("MATK")) {
                eq.setMatk((short) (eq.getMatk() + offset));
            } else if (type.equalsIgnoreCase("WDEF")) {
                eq.setWdef((short) (eq.getWdef() + offset));
            } else if (type.equalsIgnoreCase("MDEF")) {
                eq.setMdef((short) (eq.getMdef() + offset));
            } else if (type.equalsIgnoreCase("ACC")) {
                eq.setAcc((short) (eq.getAcc() + offset));
            } else if (type.equalsIgnoreCase("Avoid")) {
                eq.setAvoid((short) (eq.getAvoid() + offset));
            } else if (type.equalsIgnoreCase("Hands")) {
                eq.setHands((short) (eq.getHands() + offset));
            } else if (type.equalsIgnoreCase("Speed")) {
                eq.setSpeed((short) (eq.getSpeed() + offset));
            } else if (type.equalsIgnoreCase("Jump")) {
                eq.setJump((short) (eq.getJump() + offset));
            } else if (type.equalsIgnoreCase("ItemEXP")) {
                eq.setItemEXP(eq.getItemEXP() + offset);
            } else if (type.equalsIgnoreCase("Expiration")) {
                eq.setExpiration(eq.getExpiration() + offset);
            } else if (type.equalsIgnoreCase("Flag")) {
                eq.setFlag((short) (byte) (eq.getFlag() + offset));
            }
            item = eq.copy();
        }
        MapleInventoryManipulator.removeFromSlot(getClient(), inv, (short) slot, item.getQuantity(), false);
        return MapleInventoryManipulator.addFromDrop(getClient(), item, false);
    }

    public boolean replaceItem(int slot, int invType, Object statsSel, int upgradeSlots) {
        return replaceItem(slot, invType, statsSel, upgradeSlots, "Slots");
    }

    public boolean isCash(int itemId) {
        return MapleItemInformationProvider.getInstance().isCash(itemId);
    }

    public int getTotalStat(int itemId) {
        return MapleItemInformationProvider.getInstance().getTotalStat((Equip) MapleItemInformationProvider.getInstance().getEquipById(itemId));
    }

    public int getReqLevel(int itemId) {
        return MapleItemInformationProvider.getInstance().getReqLevel(itemId);
    }

    public MapleStatEffect getEffect(int buff) {
        return MapleItemInformationProvider.getInstance().getItemEffect(buff);
    }

    public void buffGuild(int buff, int duration, String msg) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleStatEffect mse;
        if ((ii.getItemEffect(buff) != null) && (getPlayer().getGuildId() > 0)) {
            mse = ii.getItemEffect(buff);
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr.getGuildId() == getPlayer().getGuildId()) {
                        mse.applyTo(chr, chr, true, null, duration);
                        chr.dropMessage(5, new StringBuilder().append("Your guild has gotten a ").append(msg).append(" buff.").toString());
                    }
                }
            }
        }
    }

    public boolean createAlliance(String alliancename) {
        MapleParty pt = this.c.getPlayer().getParty();
        MapleCharacter otherChar = this.c.getChannelServer().getPlayerStorage().getCharacterById(pt.getMemberByIndex(1).getId());
        if ((otherChar == null) || (otherChar.getId() == this.c.getPlayer().getId())) {
            return false;
        }
        try {
            return World.Alliance.createAlliance(alliancename, this.c.getPlayer().getId(), otherChar.getId(), this.c.getPlayer().getGuildId(), otherChar.getGuildId());
        } catch (Exception re) {
            re.printStackTrace();
        }
        return false;
    }

    public boolean addCapacityToAlliance() {
        try {
            MapleGuild gs = World.Guild.getGuild(this.c.getPlayer().getGuildId());
            if ((gs != null) && (this.c.getPlayer().getGuildRank() == 1) && (this.c.getPlayer().getAllianceRank() == 1)
                    && (World.Alliance.getAllianceLeader(gs.getAllianceId()) == this.c.getPlayer().getId()) && (World.Alliance.changeAllianceCapacity(gs.getAllianceId()))) {
                gainMeso(-10000000);
                return true;
            }
        } catch (Exception re) {
            re.printStackTrace();
        }
        return false;
    }

    public boolean disbandAlliance() {
        try {
            MapleGuild gs = World.Guild.getGuild(this.c.getPlayer().getGuildId());
            if ((gs != null) && (this.c.getPlayer().getGuildRank() == 1) && (this.c.getPlayer().getAllianceRank() == 1)
                    && (World.Alliance.getAllianceLeader(gs.getAllianceId()) == this.c.getPlayer().getId()) && (World.Alliance.disbandAlliance(gs.getAllianceId()))) {
                return true;
            }
        } catch (Exception re) {
            re.printStackTrace();
        }
        return false;
    }

    public byte getLastMsg() {
        return this.lastMsg;
    }

    public final void setLastMsg(byte last) {
        this.lastMsg = last;
    }

    public final void maxAllSkills() {
        getPlayer().maxAllSkills();
    }

    public final void maxSkillsByJob() {
        HashMap sa = new HashMap();
        for (Skill skil : SkillFactory.getAllSkills()) {
            if ((GameConstants.isApplicableSkill(skil.getId())) && (skil.canBeLearnedBy(getPlayer().getJob()))) {
                sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
            }
        }
        getPlayer().changeSkillsLevel(sa);
    }

    public final void resetStats(int str, int dex, int z, int luk) {
        this.c.getPlayer().resetStats(str, dex, z, luk);
    }

    public void killAllMonsters(int mapid) {
        MapleMap map = this.c.getChannelServer().getMapFactory().getMap(mapid);
        map.killAllMonsters(false);
    }

    public void clearDrops() {
        MapleMap map = c.getPlayer().getMap();
        double range = Double.POSITIVE_INFINITY;
        java.util.List<MapleMapObject> items = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.ITEM));
        for (MapleMapObject itemmo : items) {
            map.removeMapObject(itemmo);
            map.broadcastMessage(CField.removeItemFromMap(itemmo.getObjectId(), 0, c.getPlayer().getId()));
        }
    }


    

    public final boolean dropItem(int slot, int invType, int quantity) {
        MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
        if (inv == null) {
            return false;
        }
        return MapleInventoryManipulator.drop(this.c, inv, (short) slot, (short) quantity, true);
    }

    public String getPotStuff(int itemid, int pot) {
        return GameConstants.resolvePotentialID(itemid, pot);
    }

    public final List<Integer> getAllPotentialInfo() {
        List list = new ArrayList(MapleItemInformationProvider.getInstance().getAllPotentialInfo().keySet());
        Collections.sort(list);
        return list;
    }

    public final List<Integer> getAllPotentialInfoSearch(String content) {
        List<Integer> list = new ArrayList<>();
        for (Entry<Integer, List<StructItemOption>> i : MapleItemInformationProvider.getInstance().getAllPotentialInfo().entrySet()) {
            for (StructItemOption ii : i.getValue()) {
                if (ii.toString().contains(content)) {
                    list.add(i.getKey());
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    public void MakeGMItem(byte slot, MapleCharacter player) {
        MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
        Equip eu = (Equip) equip.getItem((short) slot);
        int item = equip.getItem((short) slot).getItemId();
        short hand = eu.getHands();
        byte level = eu.getLevel();
        Equip nItem = new Equip(item, (short) slot, (byte) 0);
        nItem.setStr((short) 32767);
        nItem.setDex((short) 32767);
        nItem.setInt((short) 32767);
        nItem.setLuk((short) 32767);
        nItem.setUpgradeSlots((byte) 0);
        nItem.setHands(hand);
        nItem.setLevel(level);
        player.getInventory(MapleInventoryType.EQUIP).removeItem((short) slot);
        player.getInventory(MapleInventoryType.EQUIP).addFromDB(nItem);
    }

    public final void sendRPS() {
        this.c.getSession().write(CField.getRPSMode((byte) 8, -1, -1, -1));
    }

    public final void setQuestRecord(Object ch, int questid, String data) {
        ((MapleCharacter) ch).getQuestNAdd(MapleQuest.getInstance(questid)).setCustomData(data);
    }

    public final void broadcastPacket(byte[] data) {
        getPlayerStorage().broadcastPacket(data);
    }

    public final PlayerStorage getPlayerStorage() {
        if (this.players == null) {
            this.players = new PlayerStorage(this.channel);
        }
        return this.players;
    }

    public final void doWeddingEffect(Object ch) {
        final MapleCharacter chr = (MapleCharacter) ch;
        final MapleCharacter player = getPlayer();
        getMap().broadcastMessage(CWvsContext.yellowChat(new StringBuilder().append(player.getName()).append(", do you take ").append(chr.getName()).append(" as your wife and promise to stay beside her through all downtimes, crashes, and lags?").toString()));
        Timer.CloneTimer.getInstance().schedule(new Runnable() {
            public void run() {
                if ((chr == null) || (player == null)) {
                    NPCConversationManager.this.warpMap(680000500, 0);
                } else {
                    chr.getMap().broadcastMessage(CWvsContext.yellowChat(chr.getName() + ", do you take " + player.getName() + " as your husband and promise to stay beside him through all downtimes, crashes, and lags?"));
                }
            }
        }, 10000L);

        Timer.CloneTimer.getInstance().schedule(new Runnable() {
            public void run() {
                if ((chr == null) || (player == null)) {
                    if (player != null) {
                        NPCConversationManager.this.setQuestRecord(player, 160001, "3");
                        NPCConversationManager.this.setQuestRecord(player, 160002, "0");
                    } else if (chr != null) {
                        NPCConversationManager.this.setQuestRecord(chr, 160001, "3");
                        NPCConversationManager.this.setQuestRecord(chr, 160002, "0");
                    }
                    NPCConversationManager.this.warpMap(680000500, 0);
                } else {
                    NPCConversationManager.this.setQuestRecord(player, 160001, "2");
                    NPCConversationManager.this.setQuestRecord(chr, 160001, "2");
                    NPCConversationManager.this.sendNPCText(player.getName() + " and " + chr.getName() + ", I wish you two all the best on your " + chr.getClient().getChannelServer().getServerName() + " journey together!", 9201002);
                    chr.getMap().startExtendedMapEffect("You may now kiss the bride, " + player.getName() + "!", 5120006);
                    if (chr.getGuildId() > 0) {
                        World.Guild.guildPacket(chr.getGuildId(), CWvsContext.sendMarriage(false, chr.getName()));
                    }
                    if (chr.getFamilyId() > 0) {
                        World.Family.familyPacket(chr.getFamilyId(), CWvsContext.sendMarriage(true, chr.getName()), chr.getId());
                    }
                    if (player.getGuildId() > 0) {
                        World.Guild.guildPacket(player.getGuildId(), CWvsContext.sendMarriage(false, player.getName()));
                    }
                    if (player.getFamilyId() > 0) {
                        World.Family.familyPacket(player.getFamilyId(), CWvsContext.sendMarriage(true, chr.getName()), player.getId());
                    }
                }
            }
        }, 20000L);
    }

    public void putKey(int key, int type, int action) {
        getPlayer().changeKeybinding(key, (byte) type, action);
        getClient().getSession().write(CField.getKeymap(getPlayer().getKeyLayout()));
    }

    public void logDonator(String log, int previous_points) {
        StringBuilder logg = new StringBuilder();
        logg.append(MapleCharacterUtil.makeMapleReadable(getPlayer().getName()));
        logg.append(" [CID: ").append(getPlayer().getId()).append("] ");
        logg.append(" [Account: ").append(MapleCharacterUtil.makeMapleReadable(getClient().getAccountName())).append("] ");
        logg.append(log);
        logg.append(new StringBuilder().append(" [Previous: ").append(previous_points).append("] [Now: ").append(getPlayer().getPoints()).append("]").toString());
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO donorlog VALUES(DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, MapleCharacterUtil.makeMapleReadable(getClient().getAccountName()));
            ps.setInt(2, getClient().getAccID());
            ps.setString(3, MapleCharacterUtil.makeMapleReadable(getPlayer().getName()));
            ps.setInt(4, getPlayer().getId());
            ps.setString(5, log);
            ps.setString(6, FileoutputUtil.CurrentReadable_Time());
            ps.setInt(7, previous_points);
            ps.setInt(8, getPlayer().getPoints());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        FileoutputUtil.log("Log_Donator.rtf", logg.toString());
    }

    public void doRing(String name, int itemid) {
        PlayersHandler.DoRing(getClient(), name, itemid);
    }

    public int getNaturalStats(int itemid, String it) {
        Map eqStats = MapleItemInformationProvider.getInstance().getEquipStats(itemid);
        if ((eqStats != null) && (eqStats.containsKey(it))) {
            return ((Integer) eqStats.get(it)).intValue();
        }
        return 0;
    }

    public boolean isEligibleName(String t) {
        return (MapleCharacterUtil.canCreateChar(t, getPlayer().isGM())) && ((!LoginInformationProvider.getInstance().isForbiddenName(t)) || (getPlayer().isGM()));
    }

    public String checkDrop(int mobId) {
        List ranks = MapleMonsterInformationProvider.getInstance().retrieveDrop(mobId);
        if ((ranks != null) && (ranks.size() > 0)) {
            int num = 0;
            int itemId = 0;
            int ch = 0;

            StringBuilder name = new StringBuilder();
            for (int i = 0; i < ranks.size(); i++) {
                MonsterDropEntry de = (MonsterDropEntry) ranks.get(i);
                if ((de.chance > 0) && ((de.questid <= 0) || ((de.questid > 0) && (MapleQuest.getInstance(de.questid).getName().length() > 0)))) {
                    itemId = de.itemId;
                    if (num == 0) {
                        name.append(new StringBuilder().append("Drops for #o").append(mobId).append("#\r\n").toString());
                        name.append("--------------------------------------\r\n");
                    }
                    String namez = new StringBuilder().append("#z").append(itemId).append("#").toString();
                    if (itemId == 0) {
                        itemId = 4031041;
                        namez = new StringBuilder().append(de.Minimum * getClient().getChannelServer().getMesoRate()).append(" to ").append(de.Maximum * getClient().getChannelServer().getMesoRate()).append(" meso").toString();
                    }
                    ch = de.chance * getClient().getChannelServer().getDropRate();
                    name.append(new StringBuilder().append(num + 1).append(") #v").append(itemId).append("#").append(namez).append(" - ").append(Integer.valueOf(ch >= 999999 ? 1000000 : ch).doubleValue() / 10000.0D).append("% chance. ").append((de.questid > 0) && (MapleQuest.getInstance(de.questid).getName().length() > 0) ? new StringBuilder().append("Requires quest ").append(MapleQuest.getInstance(de.questid).getName()).append(" to be started.").toString() : "").append("\r\n").toString());
                    num++;
                }
            }
            if (name.length() > 0) {
                return name.toString();
            }
        }

        return "No drops was returned.";
    }

    public String getLeftPadded(String in, char padchar, int length) {
        return StringUtil.getLeftPaddedStr(in, padchar, length);
    }

    public void handleDivorce() {
        if (getPlayer().getMarriageId() <= 0) {
            sendNext("Please make sure you have a marriage.");
            return;
        }
        int chz = World.Find.findChannel(getPlayer().getMarriageId());
        if (chz == -1) {
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE queststatus SET customData = ? WHERE characterid = ? AND (quest = ? OR quest = ?)");
                ps.setString(1, "0");
                ps.setInt(2, getPlayer().getMarriageId());
                ps.setInt(3, 160001);
                ps.setInt(4, 160002);
                ps.executeUpdate();
                ps.close();

                ps = con.prepareStatement("UPDATE characters SET marriageid = ? WHERE id = ?");
                ps.setInt(1, 0);
                ps.setInt(2, getPlayer().getMarriageId());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                outputFileError(e);
                return;
            }
            setQuestRecord(getPlayer(), 160001, "0");
            setQuestRecord(getPlayer(), 160002, "0");
            getPlayer().setMarriageId(0);
            sendNext("You have been successfully divorced...");
            return;
        }
        if (chz < -1) {
            sendNext("Please make sure your partner is logged on.");
            return;
        }
        MapleCharacter cPlayer = ChannelServer.getInstance(chz).getPlayerStorage().getCharacterById(getPlayer().getMarriageId());
        if (cPlayer != null) {
            cPlayer.dropMessage(1, "Your partner has divorced you.");
            cPlayer.setMarriageId(0);
            setQuestRecord(cPlayer, 160001, "0");
            setQuestRecord(getPlayer(), 160001, "0");
            setQuestRecord(cPlayer, 160002, "0");
            setQuestRecord(getPlayer(), 160002, "0");
            getPlayer().setMarriageId(0);
            sendNext("You have been successfully divorced...");
        } else {
            sendNext("An error occurred...");
        }
    }

    public String getReadableMillis(long startMillis, long endMillis) {
        return StringUtil.getReadableMillis(startMillis, endMillis);
    }

    public void sendUltimateExplorer() {
        getClient().getSession().write(CWvsContext.ultimateExplorer());
    }

    public void sendPendant(boolean b) {
        this.c.getSession().write(CWvsContext.pendantSlot(b));
    }

    public Triple<Integer, Integer, Integer> getCompensation() {
        Triple ret = null;
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM compensationlog_confirmed WHERE chrname LIKE ?");
            ps.setString(1, getPlayer().getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret = new Triple(Integer.valueOf(rs.getInt("value")), Integer.valueOf(rs.getInt("taken")), Integer.valueOf(rs.getInt("donor")));
            }
            rs.close();
            ps.close();
            return ret;
        } catch (SQLException e) {
            FileoutputUtil.outputFileError("Log_Script_Except.txt", e);
        }
        return ret;
    }

    public boolean deleteCompensation(int taken) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE compensationlog_confirmed SET taken = ? WHERE chrname LIKE ?");
            ps.setInt(1, taken);
            ps.setString(2, getPlayer().getName());
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            FileoutputUtil.outputFileError("Log_Script_Except.txt", e);
        }
        return false;
    }

    public void gainAPS(int gain) {
        getPlayer().gainAPS(gain);
    }

    public void changeJobById(short job) {
        this.c.getPlayer().changeJob(job);
    }

    public int getJobId() {
        return getPlayer().getJob();
    }

    public int getLevel() {
        return getPlayer().getLevel();
    }

    public int getEquipId(byte slot) {
        MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
        Equip eu = (Equip) equip.getItem((short) slot);
        return equip.getItem((short) slot).getItemId();
    }

    public int getUseId(byte slot) {
        MapleInventory use = getPlayer().getInventory(MapleInventoryType.USE);
        return use.getItem((short) slot).getItemId();
    }

    public int getSetupId(byte slot) {
        MapleInventory setup = getPlayer().getInventory(MapleInventoryType.SETUP);
        return setup.getItem((short) slot).getItemId();
    }

    public int getCashId(byte slot) {
        MapleInventory cash = getPlayer().getInventory(MapleInventoryType.CASH);
        return cash.getItem((short) slot).getItemId();
    }

    public int getETCId(byte slot) {
        MapleInventory etc = getPlayer().getInventory(MapleInventoryType.ETC);
        return etc.getItem((short) slot).getItemId();
    }

    public String EquipList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory equip = c.getPlayer().getInventory(MapleInventoryType.EQUIP);
        List<String> stra = new LinkedList();
        for (Item item : equip.list()) {
            stra.add(new StringBuilder().append("#L").append(item.getPosition()).append("##v").append(item.getItemId()).append("##l").toString());
        }
        for (String strb : stra) {
            str.append(strb);
        }
        return str.toString();
    }

    public String UseList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory use = c.getPlayer().getInventory(MapleInventoryType.USE);
        List<String> stra = new LinkedList();
        for (Item item : use.list()) {
            stra.add(new StringBuilder().append("#L").append(item.getPosition()).append("##v").append(item.getItemId()).append("##l").toString());
        }
        for (String strb : stra) {
            str.append(strb);
        }
        return str.toString();
    }

    public String CashList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory cash = c.getPlayer().getInventory(MapleInventoryType.CASH);
        List<String> stra = new LinkedList();
        for (Item item : cash.list()) {
            stra.add(new StringBuilder().append("#L").append(item.getPosition()).append("##v").append(item.getItemId()).append("##l").toString());
        }
        for (String strb : stra) {
            str.append(strb);
        }
        return str.toString();
    }

    public String ETCList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory etc = c.getPlayer().getInventory(MapleInventoryType.ETC);
        List<String> stra = new LinkedList();
        for (Item item : etc.list()) {
            stra.add(new StringBuilder().append("#L").append(item.getPosition()).append("##v").append(item.getItemId()).append("##l").toString());
        }
        for (String strb : stra) {
            str.append(strb);
        }
        return str.toString();
    }

    public String SetupList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory setup = c.getPlayer().getInventory(MapleInventoryType.SETUP);
        List<String> stra = new LinkedList();
        for (Item item : setup.list()) {
            stra.add(new StringBuilder().append("#L").append(item.getPosition()).append("##v").append(item.getItemId()).append("##l").toString());
        }
        for (String strb : stra) {
            str.append(strb);
        }
        return str.toString();
    }

    public String getJobName(int id) {
        return MapleCarnivalChallenge.getJobNameById(id);
    }

    public boolean isValidJob(int id) {
        return MapleCarnivalChallenge.getJobNameById(id) != null;
    }

    public String getJobNameById(int id) {
        return MapleCarnivalChallenge.getJobNameById(id);
    }

    public void test1(int a, int b, int c, int d) {
        getClient().getSession().write(CField.updateluminouscombo_black(a, b, c, d));
    }
}