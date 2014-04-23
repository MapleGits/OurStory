package handling.login.handler;

import clientside.MapleCharacter;
import clientside.MapleCharacterUtil;
import clientside.MapleClient;
import clientside.SkillEntry;
import clientside.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import clientside.Skill;
import constants.GameConstants;
import constants.JobConstants;
import constants.ServerConstants;
import constants.WorldConstants;
import constants.WorldConstants.WorldOption;
import handling.channel.ChannelServer;
import handling.login.LoginInformationProvider;
import handling.login.LoginInformationProvider.JobType;

import handling.login.LoginServer;
import handling.login.LoginWorker;
import handling.world.World;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.MapleItemInformationProvider;
import server.quest.MapleQuest;
import tools.data.LittleEndianAccessor;
import tools.packet.CField;
import tools.packet.CWvsContext;
import tools.packet.LoginPacket;
import tools.packet.PacketHelper;

public class CharLoginHandler {

    private static final boolean loginFailCount(MapleClient c) {
        c.loginAttempt = ((short) (c.loginAttempt + 1));
        if (c.loginAttempt > 5) {
            return true;
        }
        return false;
    }
        public static void login(final LittleEndianAccessor slea, final MapleClient c) {
        int loginok = 0;
        String login = slea.readMapleAsciiString();
        String pwd = slea.readMapleAsciiString();


        boolean isBanned = c.hasBannedIP() || c.hasBannedMac() || c.hasProxyBan();

        if (AutoRegister.getAccountExists(login) != false) {
            loginok = c.login(login, pwd, isBanned);
        } else if (AutoRegister.autoRegister != false && !isBanned) {
            AutoRegister.createAccount(login, pwd, c.getSession().getRemoteAddress().toString());
            if (AutoRegister.success != false) {
                loginok = c.login(login, pwd, isBanned);
            }
        }



        //   int loginok = c.login(login, pwd, ipBan || macBan);

        final Calendar tempbannedTill = c.getTempBanCalendar();

        if (loginok == 0 && isBanned) {
            loginok = 3;
            MapleCharacter.ban(c.getSession().getRemoteAddress().toString().split(":")[0], "Enforcing account ban, account ", false);
        }
        if (loginok != 0) {
            if (!loginFailCount(c)) {
                c.clearInformation();
                c.getSession().write(LoginPacket.getLoginFailed(loginok));
            } else {
                c.getSession().close(true);
            }
        } else if (isBanned) {
            c.getSession().write(LoginPacket.getLoginFailed(3));
        } else if (tempbannedTill.getTimeInMillis() != 0) {
            if (!loginFailCount(c)) {
                c.clearInformation();
                c.getSession().write(LoginPacket.getTempBan(PacketHelper.getTime(tempbannedTill.getTimeInMillis()), c.getBanReason()));
            } else {
                c.getSession().close(true);
            }
        } else {
            c.loginAttempt = 0;
            LoginWorker.registerClient(c);
        }
    }
        

    public static final void login(String username, MapleClient c, String pwd) {
        String login = username;
        int loginok = 0;
        boolean isBanned = c.hasBannedIP() || c.hasBannedMac() || c.hasProxyBan();
        loginok = c.login(login, pwd, isBanned);
        Calendar tempbannedTill = c.getTempBanCalendar();

        if ((loginok == 0) && (isBanned)) {
            loginok = 3;
        }
        if (loginok != 0) {
            if (!loginFailCount(c)) {
                c.clearInformation();
                c.getSession().write(LoginPacket.getLoginFailed(loginok));
            } else {
                c.getSession().close(true);
            }
        } else if (tempbannedTill.getTimeInMillis() != 0L) {
            if (!loginFailCount(c)) {
                c.clearInformation();
                c.getSession().write(LoginPacket.getTempBan(PacketHelper.getTime(tempbannedTill.getTimeInMillis()), c.getBanReason()));
            } else {
                c.getSession().close(true);
            }
        } else {
            c.loginAttempt = 0;
            LoginWorker.registerClient(c);
        }
    }

    public static void ServerListRequest(MapleClient c) {
        c.getSession().write(LoginPacket.getServerList(0, LoginServer.getLoad()));

        c.getSession().write(LoginPacket.getEndOfServerList());
    }

    public static final void ServerStatusRequest(MapleClient c) {
        int numPlayer = LoginServer.getUsersOn();
        int userLimit = LoginServer.getUserLimit();
        if (numPlayer >= userLimit) {
            c.getSession().write(LoginPacket.getServerStatus(2));
        } else if (numPlayer * 2 >= userLimit) {
            c.getSession().write(LoginPacket.getServerStatus(1));
        } else {
            c.getSession().write(LoginPacket.getServerStatus(0));
        }
    }

    public static void CharlistRequest(final LittleEndianAccessor slea, final MapleClient c) {
        if (!c.isLoggedIn()) {
            c.getSession().close();
            return;
        }
        slea.readByte(); //2?
        final int server = slea.readByte();
        final int channel = slea.readByte() + 1;
        if (!World.isChannelAvailable(channel, server) || !WorldOption.isExists(server)) {
            c.getSession().write(LoginPacket.getLoginFailed(10)); //cannot process so many
            return;
        }

        if (!WorldOption.getById(server).isAvailable() && !(c.isGm() && server == WorldConstants.gmserver)) {
    //        c.getSession().write(CWvsContext.broadcastMsg(1, "We are sorry, but " + WorldConstants.getNameById(server) + " is currently not available. \r\nPlease try another world."));
            c.getSession().write(LoginPacket.getLoginFailed(1)); //Shows no message, but it is used to unstuck
            return;
        }

        //System.out.println("Client " + c.getSession().getRemoteAddress().toString().split(":")[0] + " is connecting to server " + server + " channel " + channel + "");
        final List<MapleCharacter> chars = c.loadCharacters(server);
        if (chars != null && ChannelServer.getInstance(channel) != null) {
            c.setWorld(server);
            c.setChannel(channel);
            //this shit aint needed. c.getSession().write(LoginPacket.getSecondAuthSuccess(c));
            c.getSession().write(LoginPacket.getCharList(c.getSecondPassword(), chars, c.getCharacterSlots()));
        } else {
            c.getSession().close();
        }
    }

    public static final void updateCCards(LittleEndianAccessor slea, MapleClient c) {
        if ((slea.available() != 36) || (!c.isLoggedIn())) {
            c.getSession().close(true);
            return;
        }
        Map<Integer, Integer> cids = new LinkedHashMap();
        for (int i = 1; i <= 9; i++) {
            int charId = slea.readInt();
            if (((!c.login_Auth(charId)) && (charId != 0)) || (ChannelServer.getInstance(c.getChannel()) == null) || (c.getWorld() != 0)) {
                c.getSession().close(true);
                return;
            }
            cids.put(Integer.valueOf(i), Integer.valueOf(charId));
        }
        c.updateCharacterCards(cids);
    }

    public static final void CheckCharName(String name, MapleClient c) {
        c.getSession().write(LoginPacket.charNameResponse(name, (!MapleCharacterUtil.canCreateChar(name, c.isGm())) || ((LoginInformationProvider.getInstance().isForbiddenName(name)) && (!c.isGm()))));
    }
    

    public static void CreateChar(LittleEndianAccessor rh, MapleClient c) {
        String name = rh.readMapleAsciiString();
        MapleCharacter newchar = MapleCharacter.getDefault(c);
        rh.skip(4);
        int JobType = rh.readInt(); // 1 = Adventurer, 0 = Cygnus, 2 = Aran
        short subCategory = rh.readShort();
        newchar.setGender(rh.readByte());
        newchar.setSkinColor(rh.readByte());
        rh.skip(1);
        newchar.setFace(rh.readInt());
        newchar.setHair(rh.readInt());
        if (JobType == LoginInformationProvider.JobType2.resistance.getValue() || JobType == LoginInformationProvider.JobType2.xenon.getValue()) {
            rh.readInt();
        }
        int top = rh.readInt();
        int bottom = 0;
        if (JobType != LoginInformationProvider.JobType2.resistance.getValue() && JobType != LoginInformationProvider.JobType2.mercedes.getValue() && JobType != LoginInformationProvider.JobType2.demon.getValue() && JobType != LoginInformationProvider.JobType2.zen.getValue() && JobType != LoginInformationProvider.JobType2.luminous.getValue() && JobType != LoginInformationProvider.JobType2.kaiser.getValue() && JobType != LoginInformationProvider.JobType2.xenon.getValue() && JobType != LoginInformationProvider.JobType2.adventurer.getValue() && JobType != LoginInformationProvider.JobType2.angelicbuster.getValue() && JobType != LoginInformationProvider.JobType2.dualblade.getValue() && JobType != LoginInformationProvider.JobType2.phantom.getValue() && JobType != LoginInformationProvider.JobType2.demonavenger.getValue()) {
            bottom = rh.readInt();
        }
        int cape = 0;
        if (JobType == LoginInformationProvider.JobType2.phantom.getValue() || JobType == LoginInformationProvider.JobType2.zen.getValue() || JobType == LoginInformationProvider.JobType2.demonavenger.getValue()) {
            cape = rh.readInt();
        }
        int shoes = rh.readInt();
        int weapon = rh.readInt();
        int weapon1 = 0;
        if (JobType == LoginInformationProvider.JobType2.demonavenger.getValue()) {
            weapon1 = rh.readInt();
        }
        int shield = 0;
        if (JobType == LoginInformationProvider.JobType2.demon.getValue()) {
            shield = rh.readInt();
        }

        newchar.setSubcategory(subCategory);
        newchar.setName(name);

        int mapcode = 10000;


        newchar.setMap(mapcode);
        MapleInventory equip = newchar.getInventory(MapleInventoryType.EQUIPPED);
        Item eq_top = MapleItemInformationProvider.getInstance().getEquipById(1042180);
        eq_top.setPosition((byte) -5);
        equip.addFromDB(eq_top);
        if (newchar.getGender() == 0) {
            Item eq_bottom = MapleItemInformationProvider.getInstance().getEquipById(1060138);
            eq_bottom.setPosition((byte) -6);
            equip.addFromDB(eq_bottom);
        } else if (newchar.getGender() == 1) {
            Item eq_bottom = MapleItemInformationProvider.getInstance().getEquipById(1061161);
            eq_bottom.setPosition((byte) -6);
            equip.addFromDB(eq_bottom);
        }

        Item eq_shoes = MapleItemInformationProvider.getInstance().getEquipById(1072678);
        eq_shoes.setPosition((byte) -7);
        equip.addFromDB(eq_shoes);
        Item eq_weapon = MapleItemInformationProvider.getInstance().getEquipById(1302000);
        eq_weapon.setPosition((byte) -11);
        equip.addFromDB(eq_weapon);
        Item pHat = MapleItemInformationProvider.getInstance().getEquipById(1003104);
        pHat.setPosition((byte) -101);
        equip.addFromDB(pHat);


        if (MapleCharacterUtil.canCreateChar(name, false) && !LoginInformationProvider.getInstance().isForbiddenName(name)) {
            MapleCharacter.saveNewCharToDB(newchar, (short) 0);
            c.getSession().write(LoginPacket.addNewCharEntry(newchar, true));
            c.createdChar(newchar.getId());
        } else {
            c.getSession().write(LoginPacket.addNewCharEntry(newchar, false));
        }
        newchar = null;
    }

    public static final void CreateUltimate(LittleEndianAccessor slea, MapleClient c) {
        if ((!c.isLoggedIn()) || (c.getPlayer() == null) || (c.getPlayer().getLevel() < 120) || (c.getPlayer().getMapId() != 130000000) || (c.getPlayer().getQuestStatus(20734) != 0) || (c.getPlayer().getQuestStatus(20616) != 2) || (!GameConstants.isKOC(c.getPlayer().getJob())) || (!c.canMakeCharacter(c.getPlayer().getWorld()))) {
            c.getPlayer().dropMessage(1, "You have no character slots.");
            c.getSession().write(CField.createUltimate(0));
            return;
        }
        System.out.println(slea.toString());
        String name = slea.readMapleAsciiString();
        int job = slea.readInt();

        int face = slea.readInt();
        int hair = slea.readInt();

        int hat = slea.readInt();
        int top = slea.readInt();
        int glove = slea.readInt();
        int shoes = slea.readInt();
        int weapon = slea.readInt();

        byte gender = c.getPlayer().getGender();
        LoginInformationProvider.JobType jobType = LoginInformationProvider.JobType.Adventurer;

        jobType = LoginInformationProvider.JobType.UltimateAdventurer;

        MapleCharacter newchar = MapleCharacter.getDefault(c);
        newchar.setJob(job);
        newchar.setWorld(c.getPlayer().getWorld());
        newchar.setFace(face);
        newchar.setHair(hair);
        newchar.setGender(gender);
        newchar.setName(name);
        newchar.setSkinColor((byte) 3);
        newchar.setLevel((short) 50);
        newchar.getStat().str = 4;
        newchar.getStat().dex = 4;
        newchar.getStat().int_ = 4;
        newchar.getStat().luk = 4;
        newchar.setRemainingAp(254);
        newchar.setRemainingSp(job / 100 == 2 ? 128 : 122);
        newchar.getStat().maxhp += 150;
        newchar.getStat().maxmp += 125;
        switch (job) {
            case 110:
            case 120:
            case 130:
                newchar.getStat().maxhp += 600;
                newchar.getStat().maxhp += 2000;
                newchar.getStat().maxmp += 200;
                break;
            case 210:
            case 220:
            case 230:
                newchar.getStat().maxmp += 600;
                newchar.getStat().maxhp += 500;
                newchar.getStat().maxmp += 2000;
                break;
            case 310:
            case 320:
            case 410:
            case 420:
            case 520:
                newchar.getStat().maxhp += 500;
                newchar.getStat().maxmp += 250;
                newchar.getStat().maxhp += 900;
                newchar.getStat().maxmp += 600;
                break;
            case 510:
                newchar.getStat().maxhp += 500;
                newchar.getStat().maxmp += 250;
                newchar.getStat().maxhp += 450;
                newchar.getStat().maxmp += 300;
                newchar.getStat().maxhp += 800;
                newchar.getStat().maxmp += 400;
                break;
            default:
                return;
        }
        for (int i = 2490; i < 2507; i++) {
            newchar.setQuestAdd(MapleQuest.getInstance(i), (byte) 2, null);
        }
        newchar.setQuestAdd(MapleQuest.getInstance(29947), (byte) 2, null);
        newchar.setQuestAdd(MapleQuest.getInstance(111111), (byte) 0, c.getPlayer().getName());

        Map ss = new HashMap();
        ss.put(SkillFactory.getSkill(1074 + job / 100), new SkillEntry(5, (byte) 5, -1L));
        ss.put(SkillFactory.getSkill(80), new SkillEntry(1, (byte) 1, -1L));
        newchar.changeSkillLevel_Skip(ss, false);
        MapleItemInformationProvider li = MapleItemInformationProvider.getInstance();

        int[] items = {1142257, hat, top, shoes, glove, weapon, hat + 1, top + 1, shoes + 1, glove + 1, weapon + 1};
        for (byte i = 0; i < items.length; i = (byte) (i + 1)) {
            Item item = li.getEquipById(items[i]);
            item.setPosition((short) (byte) (i + 1));
            newchar.getInventory(MapleInventoryType.EQUIP).addFromDB(item);
        }
        newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000004, (short) 0, (short) 100, (short) 0));
        newchar.getInventory(MapleInventoryType.USE).addItem(new Item(2000004, (short) 0, (short) 100, (short) 0));
        c.getPlayer().fakeRelog();
        if ((MapleCharacterUtil.canCreateChar(name, c.isGm())) && ((!LoginInformationProvider.getInstance().isForbiddenName(name)) || (c.isGm()))) {
            MapleCharacter.saveNewCharToDB(newchar, (short) 0);
            MapleQuest.getInstance(20734).forceComplete(c.getPlayer(), 1101000);
            c.getSession().write(CField.createUltimate(1));
        } else {
            c.getSession().write(CField.createUltimate(0));
        }
    }

    public static final void DeleteChar(LittleEndianAccessor slea, MapleClient c) {
        String Secondpw_Client = GameConstants.GMS ? slea.readMapleAsciiString() : null;
        if (Secondpw_Client == null) {
            if (slea.readByte() > 0) {
                Secondpw_Client = slea.readMapleAsciiString();
            }
            slea.readMapleAsciiString();
        }

        int Character_ID = slea.readInt();

        if ((!c.login_Auth(Character_ID)) || (!c.isLoggedIn()) || (loginFailCount(c))) {
            c.getSession().close(true);
            return;
        }
        byte state = 0;

        if (c.getSecondPassword() != null) {
            if (Secondpw_Client == null) {
                c.getSession().close(true);
                return;
            }
            if (!c.CheckSecondPassword(Secondpw_Client)) {
                state = 20;
            }

        }

        if (state == 0) {
            state = (byte) c.deleteCharacter(Character_ID);
        }
        c.getSession().write(LoginPacket.deleteCharResponse(Character_ID, state));
    }

    public static final void Character_WithoutSecondPassword(LittleEndianAccessor slea, MapleClient c, boolean haspic, boolean view) {
        slea.readByte();
        slea.readByte();
        int charId = slea.readInt();
        if (view) {
            c.setChannel(1);
            c.setWorld(slea.readInt());
        }
        String currentpw = c.getSecondPassword();
        if ((!c.isLoggedIn()) || (loginFailCount(c)) || ((currentpw != null) && ((!currentpw.equals("")) || (haspic))) || (!c.login_Auth(charId)) || (ChannelServer.getInstance(c.getChannel()) == null) || (c.getWorld() != 0)) {
            c.getSession().close(true);
            return;
        }
        slea.readMapleAsciiString();
        c.updateMacs(slea.readMapleAsciiString());
        if (slea.available() != 0L) {
            String setpassword = slea.readMapleAsciiString();

            if ((setpassword.length() >= 6) && (setpassword.length() <= 16)) {
                c.setSecondPassword(setpassword);
                c.updateSecondPassword();
            } else {
                c.getSession().write(LoginPacket.secondPwError((byte) 20));
                return;
            }
        } else if ((GameConstants.GMS) && (haspic)) {
            return;
        }
        if (c.getIdleTask() != null) {
            c.getIdleTask().cancel(true);
        }
        String s = c.getSessionIPAddress();
        LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP());
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
        c.getSession().write(CField.getServerIP(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), charId));
    }

    public static final void Character_WithSecondPassword(LittleEndianAccessor slea, MapleClient c, boolean view) {
        String password = slea.readMapleAsciiString();
        int charId = slea.readInt();
        if (view) {
            c.setChannel(1);
            c.setWorld(slea.readInt());
        }
        if ((!c.isLoggedIn()) || (loginFailCount(c)) || (c.getSecondPassword() == null) || (!c.login_Auth(charId)) || (ChannelServer.getInstance(c.getChannel()) == null) || (c.getWorld() != 0)) {
            c.getSession().close(true);
            return;
        }
        if (GameConstants.GMS) {
            c.updateMacs(slea.readMapleAsciiString());
        }
        if ((c.CheckSecondPassword(password)) && (password.length() >= 6) && (password.length() <= 16)) {
            if (c.getIdleTask() != null) {
                c.getIdleTask().cancel(true);
            }
            String s = c.getSessionIPAddress();
            LoginServer.putLoginAuth(charId, s.substring(s.indexOf('/') + 1, s.length()), c.getTempIP());
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION, s);
            c.getSession().write(CField.getServerIP(c, Integer.parseInt(ChannelServer.getInstance(c.getChannel()).getIP().split(":")[1]), charId));
        } else {
            c.getSession().write(LoginPacket.secondPwError((byte) 0x14));
        }
    }

    public static void ViewChar(LittleEndianAccessor slea, MapleClient c) {
        Map<Byte, ArrayList<MapleCharacter>> worlds = new HashMap<Byte, ArrayList<MapleCharacter>>();
        List<MapleCharacter> chars = c.loadCharacters(0);
        c.getSession().write(LoginPacket.showAllCharacter(chars.size()));
        for (MapleCharacter chr : chars) {
            if (chr != null) {
                ArrayList<MapleCharacter> chrr;
                if (!worlds.containsKey(Byte.valueOf(chr.getWorld()))) {
                    chrr = new ArrayList<MapleCharacter>();
                    worlds.put(Byte.valueOf(chr.getWorld()), chrr);
                } else {
                    chrr = (ArrayList) worlds.get(Byte.valueOf(chr.getWorld()));
                }
                chrr.add(chr);
            }
        }
        for (Entry<Byte, ArrayList<MapleCharacter>> w : worlds.entrySet()) {
            c.getSession().write(LoginPacket.showAllCharacterInfo(((Byte) w.getKey()).byteValue(), (List) w.getValue(), c.getSecondPassword()));
        }
    }
}