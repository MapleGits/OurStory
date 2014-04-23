package tools.packet;

import clientside.InnerSkillValueHolder;
import clientside.MapleBuffStat;
import clientside.MapleCharacter;
import clientside.MapleClient;
import clientside.MapleCoolDownValueHolder;
import clientside.MapleEquipStat;
import clientside.MapleQuestStatus;
import clientside.MapleTrait;
import clientside.Skill;
import clientside.SkillEntry;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.MapleRing;
import constants.GameConstants;
import handling.Buffstat;
import handling.world.MapleCharacterLook;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SimpleTimeZone;
import server.MapleItemInformationProvider;
import server.MapleShop;
import server.MapleShopItem;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.shops.AbstractPlayerStore;
import server.shops.IMaplePlayerShop;
import tools.BitTools;
import tools.HexTool;
import tools.KoreanDateUtil;
import tools.Pair;
import tools.StringUtil;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

public class PacketHelper {

    public static final long FT_UT_OFFSET = 116444592000000000L;
    public static final long MAX_TIME = 150842304000000000L;
    public static final long ZERO_TIME = 94354848000000000L;
    public static final long PERMANENT = 150841440000000000L;

    public static final long getKoreanTimestamp(long realTimestamp) {
        return getTime(realTimestamp);
    }

    public static final long getTime(long realTimestamp) {
        if (realTimestamp == -1L) {
            return 150842304000000000L;
        }
        if (realTimestamp == -2L) {
            return 94354848000000000L;
        }
        if (realTimestamp == -3L) {
            return 150841440000000000L;
        }
        return realTimestamp * 10000L + 116444592000000000L;
    }

    public static long getFileTimestamp(long timeStampinMillis, boolean roundToMinutes) {
        if (SimpleTimeZone.getDefault().inDaylightTime(new Date())) {
            timeStampinMillis -= 3600000L;
        }
        long time;
        if (roundToMinutes) {
            time = timeStampinMillis / 1000L / 60L * 600000000L;
        } else {
            time = timeStampinMillis * 10000L;
        }
        return time + 116444592000000000L;
    }

    public static void addQuestInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        boolean idk = true;

        List<MapleQuestStatus> started = chr.getStartedQuests();
        mplew.write(1);

        mplew.writeShort(started.size());
        for (MapleQuestStatus q : started) {
            mplew.writeShort(q.getQuest().getId());
            if (q.hasMobKills()) {
                StringBuilder sb = new StringBuilder();
                for (Iterator i$ = q.getMobKills().values().iterator(); i$.hasNext();) {
                    int kills = ((Integer) i$.next()).intValue();
                    sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
                }
                mplew.writeMapleAsciiString(sb.toString());
            } else {
                mplew.writeMapleAsciiString(q.getCustomData() == null ? "" : q.getCustomData());
            }
        }

        mplew.writeShort(0);
        mplew.write(1);
        List<MapleQuestStatus> completed = chr.getCompletedQuests();
        mplew.writeShort(completed.size());
        for (MapleQuestStatus q : completed) {
            mplew.writeShort(q.getQuest().getId());
            mplew.writeInt((int) getTime(q.getCompletionTime()));
        }
    }

       public static void addSkillInfo(final MaplePacketLittleEndianWriter mplew, final MapleCharacter chr) { // 0x100
        final Map<Skill, SkillEntry> skills = chr.getSkills();
        boolean useOld = skills.size() < 500;
        mplew.write(useOld ? 1 : 0); // To handle the old skill system or something? 
        if (useOld) {
            mplew.writeShort(skills.size());
            for (final Entry<Skill, SkillEntry> skill : skills.entrySet()) {
                mplew.writeInt(skill.getKey().getId());
                mplew.writeInt(skill.getValue().skillevel);
                addExpirationTime(mplew, skill.getValue().expiration);

                if (skill.getKey().isFourthJob()) {
                    mplew.writeInt(skill.getValue().masterlevel);
                }
            }
        } else {
            final Map<Integer, Integer> skillsWithoutMax = new LinkedHashMap<>();
            final Map<Integer, Long> skillsWithExpiration = new LinkedHashMap<>();
            final Map<Integer, Byte> skillsWithMax = new LinkedHashMap<>();

            // Fill in these maps
            for (final Entry<Skill, SkillEntry> skill : skills.entrySet()) {
                skillsWithoutMax.put(skill.getKey().getId(), skill.getValue().skillevel);
                if (skill.getValue().expiration > 0) {
                    skillsWithExpiration.put(skill.getKey().getId(), skill.getValue().expiration);
                }
                if (skill.getKey().isFourthJob()) {
                    skillsWithMax.put(skill.getKey().getId(), skill.getValue().masterlevel);
                }
            }

            int amount = skillsWithoutMax.size();
            mplew.writeShort(amount);
            for (final Entry<Integer, Integer> x : skillsWithoutMax.entrySet()) {
                mplew.writeInt(x.getKey());
                mplew.writeInt(x.getValue()); // 80000000, 80000001, 80001040 show cid if linked.
            }
            mplew.writeShort(0); // For each, int

            amount = skillsWithExpiration.size();
            mplew.writeShort(amount);
            for (final Entry<Integer, Long> x : skillsWithExpiration.entrySet()) {
                mplew.writeInt(x.getKey());
                mplew.writeLong(x.getValue()); // Probably expiring skills here
            }
            mplew.writeShort(0); // For each, int

            amount = skillsWithMax.size();
            mplew.writeShort(amount);
            for (final Entry<Integer, Byte> x : skillsWithMax.entrySet()) {
                mplew.writeInt(x.getKey());
                mplew.writeInt(x.getValue());
            }
            mplew.writeShort(0); // For each, int (Master level = 0? O.O)
        }
    }

    public static void addCoolDownInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final List<MapleCoolDownValueHolder> cd = chr.getCooldowns();

        mplew.writeShort(cd.size());
        for (MapleCoolDownValueHolder cooling : cd) {
            mplew.writeInt(cooling.skillId);
            mplew.writeInt((int) (cooling.length + cooling.startTime - System.currentTimeMillis()) / 1000);
        }
        if (cd.isEmpty()) {
            mplew.writeShort(0);
        }
    }

    public static void addRocksInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        int[] mapz = chr.getRegRocks();
        for (int i = 0; i < 5; i++) {
            mplew.writeInt(mapz[i]);
        }

        int[] map = chr.getRocks();
        for (int i = 0; i < 10; i++) {
            mplew.writeInt(map[i]);
        }

        int[] maps = chr.getHyperRocks();
        for (int i = 0; i < 13; i++) {
            mplew.writeInt(maps[i]);
        }
        for (int i = 0; i < 13; i++) {
            mplew.writeInt(maps[i]);
        }
    }

    public static final void addRingInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(0);

        final Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> aRing = chr.getRings(true);
        final List<MapleRing> cRing = (List) aRing.getLeft();
        mplew.writeShort(cRing.size());
        for (MapleRing ring : cRing) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(ring.getPartnerName(), 13);
            mplew.writeLong(ring.getRingId());
            mplew.writeLong(ring.getPartnerRingId());
        }
        final List<MapleRing> fRing = (List) aRing.getMid();
        mplew.writeShort(fRing.size());
        for (MapleRing ring : fRing) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(ring.getPartnerName(), 13);
            mplew.writeLong(ring.getRingId());
            mplew.writeLong(ring.getPartnerRingId());
            mplew.writeInt(ring.getItemId());
        }
        final List<MapleRing> mRing = (List) aRing.getRight();
        mplew.writeShort(mRing.size());
        int marriageId = 30000;
        for (MapleRing ring : mRing) {
            mplew.writeInt(marriageId);
            mplew.writeInt(chr.getId());
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeShort(3);
            mplew.writeInt(ring.getItemId());
            mplew.writeInt(ring.getItemId());
            mplew.writeAsciiString(chr.getName(), 13);
            mplew.writeAsciiString(ring.getPartnerName(), 13);
        }
    }

    public static void addInventoryInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeLong(chr.getMeso());
        mplew.writeInt(0);
        mplew.writeLong(0);
        mplew.writeInt(chr.getId());
        mplew.writeZeroBytes(31);
        mplew.write(chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.USE).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.SETUP).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.ETC).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.CASH).getSlotLimit());
        mplew.writeLong(getTime(-2L));
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        final List<Item> equipped = iv.newList();
        Collections.sort(equipped);
        for (Item item : equipped) {
            if ((item.getPosition() < 0) && (item.getPosition() > -100)) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }

        mplew.writeShort(0);
        for (Item item : equipped) {
            if ((item.getPosition() <= -100) && (item.getPosition() > -1000)) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }

        }
        mplew.writeShort(0);
        iv = chr.getInventory(MapleInventoryType.EQUIP);
        for (Item item : iv.list()) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
        }

        mplew.writeShort(0);
        for (Item item : equipped) {
            if ((item.getPosition() <= -1000) && (item.getPosition() > -1100)) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.writeShort(0);
        for (Item item : equipped) {
            if ((item.getPosition() <= -1100) && (item.getPosition() > -1200)) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.writeShort(0);
        for (Item item : equipped) {
            if (item.getPosition() <= -1200) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }

        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        for (Item item : equipped) {
            if ((item.getPosition() <= -5000) && (item.getPosition() >= -5002)) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);

        iv = chr.getInventory(MapleInventoryType.USE);
        for (Item item : iv.list()) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.SETUP);
        for (Item item : iv.list()) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.ETC);
        for (Item item : iv.list()) {
            if (item.getPosition() < 100) {
                addItemPosition(mplew, item, false, false);
                addItemInfo(mplew, item, chr);
            }
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.CASH);
        for (Item item : iv.list()) {
            addItemPosition(mplew, item, false, false);
            addItemInfo(mplew, item, chr);
        }
        mplew.write(0);
     for (int i = 0; i < chr.getExtendedSlots().size(); i++) {
            mplew.writeInt(i);
            mplew.writeInt(chr.getExtendedSlot(i));
            for (Item item : chr.getInventory(MapleInventoryType.ETC).list()) {
                if ((item.getPosition() > i * 100 + 100) && (item.getPosition() < i * 100 + 200)) {
                    addItemPosition(mplew, item, false, true);
                    addItemInfo(mplew, item, chr);
                }
            }
            mplew.writeInt(-1);
        }
        mplew.writeZeroBytes(17);
    }

    public static void addCharStats(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getId());
        mplew.writeAsciiString(chr.getName(), 13);
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor());
        mplew.writeInt(chr.getFace());
        mplew.writeInt(chr.getHair());
        mplew.writeZeroBytes(24);

        mplew.write(chr.getLevel());
        mplew.writeShort(chr.getJob());
        chr.getStat().connectData(mplew);
        mplew.writeShort(0);
        if (GameConstants.isSeparatedSp(chr.getJob())) {

            mplew.write(0); // SP SUCKS DICK

        } else {
            mplew.writeShort(0);
        }

        mplew.writeLong(chr.getExp());
        mplew.writeInt(chr.getFame());
        mplew.writeInt(0);
        mplew.writeInt(chr.getGachExp());
        mplew.writeInt(chr.getMapId());
        mplew.write(chr.getInitialSpawnpoint());

        mplew.writeShort(chr.getSubcategory());
        if (GameConstants.isDemon(chr.getJob()) || GameConstants.demonAvenger(chr.getJob()) || GameConstants.xenon(chr.getJob())) {
            mplew.writeLong(0);
        } else {
            mplew.writeInt(0);
        }
        mplew.write(chr.getFatigue());
        mplew.writeInt(GameConstants.getCurrentDate());
        for (MapleTrait.MapleTraitType t : MapleTrait.MapleTraitType.values()) {
            mplew.writeInt(chr.getTrait(t).getTotalExp());
        }
        for (MapleTrait.MapleTraitType t : MapleTrait.MapleTraitType.values()) {
            mplew.writeShort(0);
        }
        mplew.writeLong(getTime(-2L));
        mplew.write(0);
        mplew.writeInt(chr.getStat().pvpExp);
        mplew.write(chr.getStat().pvpRank);
        mplew.writeInt(chr.getBattlePoints());
        mplew.write(5);
        mplew.write(6);
        mplew.writeZeroBytes(5);

        mplew.writeLong(0);
        mplew.writeShort(0);
        mplew.writeZeroBytes(3);
        chr.getCharacterCard().connectData(mplew);
        mplew.writeLong(0);

    }

    public static void addCharLook(MaplePacketLittleEndianWriter mplew, MapleCharacterLook chr, boolean mega) {
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor());
        mplew.writeInt(chr.getFace());
        mplew.writeInt(chr.getJob());
        mplew.write(mega ? 0 : 1);
        mplew.writeInt(chr.getHair());

        final Map<Byte, Integer> myEquip = new LinkedHashMap<>();
        final Map<Byte, Integer> maskedEquip = new LinkedHashMap<>();
        final Map<Byte, Integer> equip = chr.getEquips();
        for (final Entry<Byte, Integer> item : equip.entrySet()) {
            if (item.getKey() < -127) { //not visible
                continue;
            }
            byte pos = (byte) (item.getKey() * -1);

            if (pos < 100 && myEquip.get(pos) == null) {
                myEquip.put(pos, item.getValue());
            } else if (pos > 100 && pos != 111) {
                pos = (byte) (pos - 100);
                if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, myEquip.get(pos));
                }
                myEquip.put(pos, item.getValue());
            } else if (myEquip.get(pos) != null) {
                maskedEquip.put(pos, item.getValue());
            }
        }
        for (final Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF); // end of visible itens
        mplew.write(0xFF);
        // masked itens
        for (final Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            mplew.write(entry.getKey());
            mplew.writeInt(entry.getValue());
        }
        mplew.write(0xFF); // ending markers

        Integer cWeapon = equip.get((byte) -111);
        mplew.writeInt(cWeapon != null ? cWeapon : 0);
        cWeapon = equip.get((byte) -11);
        mplew.writeInt(cWeapon != null ? cWeapon : 0);
        cWeapon = equip.get((byte) -10);
        mplew.writeInt(cWeapon != null ? cWeapon : 0);
        mplew.write(0);
        mplew.writeZeroBytes(12); // pets
        if (GameConstants.isDemon(chr.getJob()) || GameConstants.demonAvenger(chr.getJob()) || GameConstants.xenon(chr.getJob())) {
            mplew.writeInt(0);
        }
    }

    public static void addExpirationTime(MaplePacketLittleEndianWriter mplew, long time) {
        mplew.writeLong(getTime(time));
    }

    public static void addItemPosition(MaplePacketLittleEndianWriter mplew, Item item, boolean trade, boolean bagSlot) {
        if (item == null) {
            mplew.write(0);
            return;
        }
        short pos = item.getPosition();
        if (pos <= -1) {
            pos = (short) (pos * -1);
            if ((pos > 100) && (pos < 1000)) {
                pos = (short) (pos - 100);
            }
        }
        if (bagSlot) {
            mplew.writeInt(pos % 100 - 1);
        } else if ((!trade) && (item.getType() == 1)) {
            mplew.writeShort(pos);
        } else {
            mplew.write(pos);
        }
    }

    public static void addItemInfo(MaplePacketLittleEndianWriter mplew, Item item) {
        addItemInfo(mplew, item, null);
    }

    public static void addItemInfo(MaplePacketLittleEndianWriter mplew, Item item, MapleCharacter chr) {
        mplew.write(item.getPet() != null ? 3 : item.getType());
        mplew.writeInt(item.getItemId());
        boolean hasUniqueId = (item.getUniqueId() > 0) && (!GameConstants.isMarriageRing(item.getItemId())) && (item.getItemId() / 10000 != 166);

        mplew.write(hasUniqueId ? 1 : 0);
        if (hasUniqueId) {
            mplew.writeLong(item.getUniqueId());
        }
        if (item.getPet() != null) {
            addPetItemInfo(mplew, item, item.getPet(), true);
        } else {
            addExpirationTime(mplew, item.getExpiration());
            mplew.writeInt(chr == null ? -1 : chr.getExtendedSlots().indexOf(Integer.valueOf(item.getItemId())));
            if (item.getType() == 1) {
                GameConstants.setitemstat((Equip) item);
                Equip equip = (Equip) item;
                if (equip.getstat().size() > 0) {
                    int head = 0;
                    for (Map.Entry stat : equip.getstat().entrySet()) {
                        head += ((MapleEquipStat) stat.getKey()).getValue();
                    }
                    mplew.writeInt(head);
                    if (equip.getstat().get(MapleEquipStat.SLOTS) != null) {
                        mplew.write(equip.getUpgradeSlots());
                    }
                    if (equip.getstat().get(MapleEquipStat.Level) != null) {
                        mplew.write(equip.getLevel());
                    }
                    if (equip.getstat().get(MapleEquipStat.STR) != null) {
                        mplew.writeShort(equip.getStr());
                    }
                    if (equip.getstat().get(MapleEquipStat.DEX) != null) {
                        mplew.writeShort(equip.getDex());
                    }
                    if (equip.getstat().get(MapleEquipStat.INT) != null) {
                        mplew.writeShort(equip.getInt());
                    }
                    if (equip.getstat().get(MapleEquipStat.LUK) != null) {
                        mplew.writeShort(equip.getLuk());
                    }
                    if (equip.getstat().get(MapleEquipStat.maxHp) != null) {
                        mplew.writeShort(equip.getHp());
                    }
                    if (equip.getstat().get(MapleEquipStat.maxMp) != null) {
                        mplew.writeShort(equip.getMp());
                    }
                    if (equip.getstat().get(MapleEquipStat.WATK) != null) {
                        mplew.writeShort(equip.getWatk());
                    }
                    if (equip.getstat().get(MapleEquipStat.MATK) != null) {
                        mplew.writeShort(equip.getMatk());
                    }
                    if (equip.getstat().get(MapleEquipStat.WDEF) != null) {
                        mplew.writeShort(equip.getWdef());
                    }
                    if (equip.getstat().get(MapleEquipStat.MDEF) != null) {
                        mplew.writeShort(equip.getMdef());
                    }
                    if (equip.getstat().get(MapleEquipStat.ACC) != null) {
                        mplew.writeShort(equip.getAcc());
                    }
                    if (equip.getstat().get(MapleEquipStat.AVOID) != null) {
                        mplew.writeShort(equip.getAvoid());
                    }
                    if (equip.getstat().get(MapleEquipStat.Hands) != null) {
                        mplew.writeShort(equip.getHands());
                    }
                    if (equip.getstat().get(MapleEquipStat.SPEED) != null) {
                        mplew.writeShort(equip.getSpeed());
                    }
                    if (equip.getstat().get(MapleEquipStat.JUMP) != null) {
                        mplew.writeShort(equip.getJump());
                    }
                    if (equip.getstat().get(MapleEquipStat.flag) != null) {
                        mplew.writeShort(equip.getFlag());
                    }
                    if (equip.getstat().get(MapleEquipStat.Hammer) != null) {
                        mplew.writeInt(equip.getViciousHammer());
                    }
                } else {
                    mplew.writeInt(0);
                }
                mplew.writeInt(4);
                mplew.write(-1);
                mplew.writeMapleAsciiString(equip.getOwner());

                mplew.write(equip.getState());
                mplew.write(equip.getEnhance());
                mplew.writeShort(equip.getPotential1());
                mplew.writeShort(equip.getPotential2());
                mplew.writeShort(equip.getPotential3());
                mplew.writeShort(equip.getPotential4());
                mplew.writeShort(equip.getPotential5());
                mplew.writeShort(0);
                mplew.writeShort(0);
                mplew.writeShort(equip.getSocketState());
                mplew.writeShort(equip.getSocket1() % 10000);
                mplew.writeShort(equip.getSocket2() % 10000);
                mplew.writeShort(equip.getSocket3() % 10000);
                if (!hasUniqueId) {
                    mplew.writeLong(equip.getInventoryId() <= 0L ? -1L : equip.getInventoryId());
                }
                mplew.writeLong(getTime(-2L));
                mplew.writeInt(-1); //?
                mplew.writeLong(0);
                mplew.writeLong(getTime(-2));
                mplew.writeZeroBytes(16);
            } else {
                mplew.writeShort(item.getQuantity());
                mplew.writeMapleAsciiString(item.getOwner());
                mplew.writeShort(item.getFlag());
                if ((GameConstants.isThrowingStar(item.getItemId())) || (GameConstants.isBullet(item.getItemId())) || (item.getItemId() / 10000 == 287)) {
                    mplew.writeLong(item.getInventoryId() <= 0L ? -1L : item.getInventoryId());
                }
            }
        }
    }

    public static void serializeMovementList(MaplePacketLittleEndianWriter lew, List<LifeMovementFragment> moves) {
        lew.write(moves.size());
        for (LifeMovementFragment move : moves) {
            move.serialize(lew);
        }
    }

    public static void addAnnounceBox(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        if ((chr.getPlayerShop() != null) && (chr.getPlayerShop().isOwner(chr)) && (chr.getPlayerShop().getShopType() != 1) && (chr.getPlayerShop().isAvailable())) {
            addInteraction(mplew, chr.getPlayerShop());
        } else {
            mplew.write(0);
        }
    }

    public static void addInteraction(MaplePacketLittleEndianWriter mplew, IMaplePlayerShop shop) {
        mplew.write(shop.getGameType());
        mplew.writeInt(((AbstractPlayerStore) shop).getObjectId());
        mplew.writeMapleAsciiString(shop.getDescription());
        if (shop.getShopType() != 1) {
            mplew.write(shop.getPassword().length() > 0 ? 1 : 0);
        }
        mplew.write(shop.getItemId() % 10);
        mplew.write(shop.getSize());
        mplew.write(shop.getMaxSize());
        if (shop.getShopType() != 1) {
            mplew.write(shop.isOpen() ? 0 : 1);
        }
    }

    public static void addCharacterInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.write(HexTool.hex("FF FF FF FF FF FF BF FF"));
        mplew.writeZeroBytes(20);
        addCharStats(mplew, chr);
        mplew.write(chr.getBuddylist().getCapacity());
        if (chr.getBlessOfFairyOrigin() != null) {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getBlessOfFairyOrigin());
        } else {
            mplew.write(0);
        }
        if (chr.getBlessOfEmpressOrigin() != null) {
            mplew.write(1);
            mplew.writeMapleAsciiString(chr.getBlessOfEmpressOrigin());
        } else {
            mplew.write(0);
        }
        MapleQuestStatus ultExplorer = chr.getQuestNoAdd(MapleQuest.getInstance(111111));
        if ((ultExplorer != null) && (ultExplorer.getCustomData() != null)) {
            mplew.write(1);
            mplew.writeMapleAsciiString(ultExplorer.getCustomData());
        } else {
            mplew.write(0);
        }
        addInventoryInfo(mplew, chr);
        addSkillInfo(mplew, chr);
        addCoolDownInfo(mplew, chr);
        addQuestInfo(mplew, chr);
        addRingInfo(mplew, chr);
        addRocksInfo(mplew, chr);
        addMonsterBookInfo(mplew, chr);
        mplew.writeShort(0);
        mplew.writeShort(0);
        chr.QuestInfoPacket(mplew); 
        if ((chr.getJob() >= 3300) && (chr.getJob() <= 3312)) {
            addJaguarInfo(mplew, chr);
        }
        mplew.writeInt(0);
        addStealSkills(mplew, chr);
        mplew.writeZeroBytes(5);
        addInnerStats(mplew, chr);
        mplew.writeInt(chr.getHonourLevel());    
        mplew.writeInt(chr.getHonourExp()); 
        mplew.writeLong(0);
        mplew.writeZeroBytes(17);
        mplew.writeLong(getTime(-2L));
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString("Creating..."); 
        mplew.writeInt(0); 
        mplew.writeInt(0); 
        mplew.writeZeroBytes(39);
        mplew.writeLong(getTime(-2L));
        mplew.writeZeroBytes(75);
        mplew.writeLong(getTime(System.currentTimeMillis()));
        mplew.writeInt(0);
        mplew.write(1);
        mplew.writeShort(0);
    }

    public static final int getSkillBook(int i) {
        switch (i) {
            case 1:
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 2;
        }
        return 0;
    }

    public static final void addInnerStats(final MaplePacketLittleEndianWriter w, final MapleCharacter player) {
        final List<InnerSkillValueHolder> skills = player.getInnerSkills();
        w.writeShort(skills.size());


        for (int i = 0; i < skills.size(); ++i) {
            w.write(i + 1);
            w.writeInt(skills.get(i).getSkillId()); //��ų id
            w.write(skills.get(i).getSkillLevel()); //�ɼ� (x��, �ִ밪 = maxLevel)
            w.write(skills.get(i).getRank());
        }

    }

    public static final void addCoreAura(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeZeroBytes(72);
        mplew.writeLong(getTime(System.currentTimeMillis() + 86400000L));
        mplew.writeInt(0);
        mplew.write(GameConstants.isJett(chr.getJob()) ? 1 : 0);
    }

    public static void addStolenSkills(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, int jobNum, boolean writeJob) {
        if (writeJob) {
            mplew.writeInt(jobNum);
        }
        int count = 0;
        if (chr.getStolenSkills() != null) {
            for (Pair sk : chr.getStolenSkills()) {
                if (GameConstants.getJobNumber(((Integer) sk.left).intValue() / 10000) == jobNum) {
                    mplew.writeInt(((Integer) sk.left).intValue());
                    count++;
                    if (count >= GameConstants.getNumSteal(jobNum)) {
                        break;
                    }
                }
            }
        }
        while (count < GameConstants.getNumSteal(jobNum)) {
            mplew.writeInt(0);
            count++;
        }
    }

    public static void addChosenSkills(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        for (int i = 1; i <= 4; i++) {
            boolean found = false;
            if (chr.getStolenSkills() != null) {
                for (Pair sk : chr.getStolenSkills()) {
                    if ((GameConstants.getJobNumber(((Integer) sk.left).intValue() / 10000) == i) && (((Boolean) sk.right).booleanValue())) {
                        mplew.writeInt(((Integer) sk.left).intValue());
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                mplew.writeInt(0);
            }
        }
    }

    public static final void addStealSkills(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        for (int i = 1; i <= 4; i++) {
            addStolenSkills(mplew, chr, i, false);
        }
        addChosenSkills(mplew, chr);
    }

    public static final void addMonsterBookInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(0);

        if (chr.getMonsterBook().getSetScore() > 0) {
            chr.getMonsterBook().writeFinished(mplew);
        } else {
            chr.getMonsterBook().writeUnfinished(mplew);
        }

        mplew.writeInt(chr.getMonsterBook().getSet());
    }

    public static final void addPetItemInfo(MaplePacketLittleEndianWriter mplew, Item item, MaplePet pet, boolean active) {
        if (item == null) {
            mplew.writeLong(PacketHelper.getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
        } else {
            addExpirationTime(mplew, item.getExpiration() <= System.currentTimeMillis() ? -1L : item.getExpiration());
        }
        mplew.writeInt(-1);
        mplew.writeAsciiString(pet.getName(), 13);
        mplew.write(pet.getLevel());
        mplew.writeShort(30000);
        mplew.write(100);
        // mplew.writeShort(pet.getCloseness());
        //  mplew.write(pet.getFullness());
        if (item == null) {
            mplew.writeLong(PacketHelper.getKoreanTimestamp((long) (System.currentTimeMillis() * 1.5)));
        } else {
            addExpirationTime(mplew, item.getExpiration() <= System.currentTimeMillis() ? -1L : item.getExpiration());
        }
        mplew.writeShort(0);
        mplew.writeShort(pet.getFlags());
        mplew.writeInt((pet.getPetItemId() == 5000054) && (pet.getSecondsLeft() > 0) ? pet.getSecondsLeft() : 0);
        mplew.writeShort(0);
        mplew.write((byte) (pet.getSummoned() ? pet.getSummonedValue() : active ? 0 : 0));
        for (int i = 0; i < 4; i++) {
            mplew.write(0);
        }
        mplew.writeInt(-1);
        mplew.writeShort(100);
    }

    public static final void addShopInfo(MaplePacketLittleEndianWriter mplew, MapleShop shop, MapleClient c) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        mplew.write(0);

        mplew.writeShort(shop.getItems().size());
        for (MapleShopItem item : shop.getItems()) {
            addShopItemInfo(mplew, item, shop, ii, null);
        }
    }

    public static final void addShopItemInfo(MaplePacketLittleEndianWriter mplew, MapleShopItem item, MapleShop shop, MapleItemInformationProvider ii, Item i) {
        mplew.writeInt(item.getItemId());
        mplew.writeInt(item.getPrice());
        mplew.write(0);
        mplew.writeInt(item.getReqItem());
        mplew.writeInt(item.getReqItemQ());
        mplew.writeLong(1440 * item.getPeriod());
        mplew.writeInt(0);
        mplew.writeLong(getTime(-2L));
        mplew.writeLong(getTime(-1L));
        mplew.writeInt(item.getRank());
        mplew.write(item.getState() > 0 ? 1 : 0);
        mplew.writeInt(item.getPeriod() > 0 ? 1 : 0);
        if ((!GameConstants.isThrowingStar(item.getItemId())) && (!GameConstants.isBullet(item.getItemId()))) {
            mplew.writeShort(1);
            mplew.writeShort(item.getBuyable());
        } else {
            mplew.writeZeroBytes(6);
            mplew.writeShort(BitTools.doubleToShortBits(ii.getPrice(item.getItemId())));
            mplew.writeShort(ii.getSlotMax(item.getItemId()));
        }

        mplew.write(i == null ? 0 : 1);
        if (i != null) {
            addItemInfo(mplew, i);
        }
        if (shop.getRanks().size() > 0) {
            mplew.write(item.getRank() >= 0 ? 1 : 0);
            if (item.getRank() >= 0) {
                mplew.write(item.getRank());
            }
        }
        mplew.writeZeroBytes(16);
        for (int x = 0; x < 4; x++) {
            mplew.writeLong(9410165 + x);
        }
    }

    public static final void addJaguarInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.write(0x28);
        mplew.writeZeroBytes(20);
    }

    public static <E extends Buffstat> void writeSingleMask_ENERGY(MaplePacketLittleEndianWriter mplew, E statup) {
        for (int i = 10; i >= 1; i--) {
            mplew.writeInt(i == statup.getPosition() ? statup.getValue() : 0);
        }
    }

    public static <E extends Buffstat> void writeSingleMask(MaplePacketLittleEndianWriter mplew, E statup) {
        for (int i = 12; i >= 1; i--) {
            mplew.writeInt(i == statup.getPosition() ? statup.getValue() : 0);
        }
    }

    public static final <T extends Buffstat> void writeLongSingleMask(MaplePacketLittleEndianWriter packet, T statups) {
        long firstmask = 0;
        long secondmask = 0;
        long thirdmask = 0;
        long forthmask = 0;
        long zeromask = 0;
        if (statups.getPosition() == 1) {
            firstmask |= statups.getValue();
        } else if (statups.getPosition() == 2) {
            secondmask |= statups.getValue();
        } else if (statups.getPosition() == 3) {
            thirdmask |= statups.getValue();
        } else if (statups.getPosition() == 4) {
            forthmask |= statups.getValue();
        } else if (statups.getPosition() == 0) {
            zeromask |= statups.getValue();
        }
        packet.writeLong(0); //v192
        packet.writeLong(zeromask);
        packet.writeLong(firstmask);
        packet.writeLong(secondmask);
        packet.writeLong(thirdmask);
        packet.writeLong(forthmask);
    }

    public static <E extends Buffstat> void writeMask(MaplePacketLittleEndianWriter mplew, Collection<E> statups) {
        int[] mask = new int[10];
        if (!statups.contains(MapleBuffStat.MONSTER_RIDING)) {
            mask = new int[12];
        }
        for (Buffstat statup : statups) {
            mask[(statup.getPosition() - 1)] |= statup.getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }
    }

    public static <E extends Buffstat> void writeBuffMask(MaplePacketLittleEndianWriter mplew, Collection<Pair<E, Integer>> statups) {
        int[] mask = new int[10];
        if (!statups.contains(MapleBuffStat.MONSTER_RIDING)) {
            mask = new int[12];
        }
        for (Pair statup : statups) {
            mask[(((Buffstat) statup.left).getPosition() - 1)] |= ((Buffstat) statup.left).getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }
    }

    public static <E extends Buffstat> void writeBuffMask(MaplePacketLittleEndianWriter mplew, Map<E, Integer> statups) {
        int[] mask = new int[10];
        if (!statups.containsKey(MapleBuffStat.MONSTER_RIDING)) {
            mask = new int[12];
        }
        for (Buffstat statup : statups.keySet()) {
            mask[(statup.getPosition() - 1)] |= statup.getValue();
        }
        for (int i = mask.length; i >= 1; i--) {
            mplew.writeInt(mask[(i - 1)]);
        }
    }
}