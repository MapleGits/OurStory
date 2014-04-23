package client.inventory;

import clientside.MapleEquipStat;
import constants.GameConstants;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.StructItemOption;

public class Equip extends Item
        implements Serializable {

    public static final int ARMOR_RATIO = 350000;
    public static final int WEAPON_RATIO = 700000;
    private byte upgradeSlots = 0;
    private byte level = 0;
    private byte vicioushammer = 0;
    private byte enhance = 0;
    private short str = 0;
    private short dex = 0;
    private short _int = 0;
    private short luk = 0;
    private short hp = 0;
    private short mp = 0;
    private byte bossDamage = 0, ignorePDR = 0, totalDamage = 0, allStat = 0;
    private short watk = 0;
    private short matk = 0;
    private short wdef = 0;
    private short mdef = 0;
    private short acc = 0;
    private short avoid = 0;
    private short hands = 0;
    private short speed = 0;
    private short jump = 0;
    private short charmExp = 0;
    private short pvpDamage = 0;
    private int itemEXP = 0;
    private int durability = -1;
    private int incSkill = -1;
    private int potential1 = 0;
    private int potential2 = 0;
    private int potential3 = 0;
    private int potential4 = 0;
    private int potential5 = 0;
    private int potential6 = 0;
    private int potential7 = 0;
    private int potential8 = 0;
 //   private int fusionAnvil = 0;
    private int socket1 = -1;
    private int socket2 = -1;
    private int socket3 = -1;
    private MapleRing ring = null;
    private MapleAndroid android = null;
    private Map<MapleEquipStat, Integer> stats = new LinkedHashMap();

    public Equip(int id, short position, byte flag) {
        super(id, position, (short) 1, (short) flag);
    }

    public Equip(int id, short position, int uniqueid, short flag) {
        super(id, position, (short) 1, flag, uniqueid);
    }

    public Map<MapleEquipStat, Integer> getstat() {
        return this.stats;
    }

    public void setstat(MapleEquipStat statss, int str) {
        this.stats.put(statss, Integer.valueOf(str));
    }

    public Item copy() {
        Equip ret = new Equip(getItemId(), getPosition(), getUniqueId(), getFlag());
        ret.str = this.str;
        ret.dex = this.dex;
        ret._int = this._int;
        ret.luk = this.luk;
        ret.hp = this.hp;
        ret.mp = this.mp;
        ret.matk = this.matk;
        ret.mdef = this.mdef;
        ret.watk = this.watk;
        ret.wdef = this.wdef;
        ret.acc = this.acc;
        ret.avoid = this.avoid;
        ret.hands = this.hands;
        ret.speed = this.speed;
        ret.jump = this.jump;
        ret.enhance = this.enhance;
        ret.upgradeSlots = this.upgradeSlots;
        ret.level = this.level;
        ret.itemEXP = this.itemEXP;
        ret.durability = this.durability;
        ret.vicioushammer = this.vicioushammer;
        ret.potential1 = this.potential1;
        ret.potential2 = this.potential2;
        ret.potential3 = this.potential3;
        ret.potential4 = this.potential4;
        ret.potential5 = this.potential5;
     //   ret.fusionAnvil = fusionAnvil;
        ret.bossDamage = bossDamage;
        ret.ignorePDR = ignorePDR;
        ret.totalDamage = totalDamage;
        ret.allStat = allStat;
        ret.socket1 = this.socket1;
        ret.socket2 = this.socket2;
        ret.socket3 = this.socket3;
        ret.charmExp = this.charmExp;
        ret.pvpDamage = this.pvpDamage;
        ret.incSkill = this.incSkill;
        ret.setGiftFrom(getGiftFrom());
        ret.setOwner(getOwner());
        ret.setQuantity(getQuantity());
        ret.setExpiration(getExpiration());
        return ret;
    }

    public byte getType() {
        return 1;
    }

    public byte getUpgradeSlots() {
        return this.upgradeSlots;
    }

    public short getStr() {
        return this.str;
    }

    public short getDex() {
        return this.dex;
    }
    
  //      public int getFusionAnvil() {
  //      return fusionAnvil;
  //  }

 //   public void setFusionAnvil(final int en) {
 //       fusionAnvil = en;
 //   }

    public short getInt() {
        return this._int;
    }

    public short getLuk() {
        return this.luk;
    }

    public short getHp() {
        return this.hp;
    }

    public short getMp() {
        return this.mp;
    }

    public short getWatk() {
        return this.watk;
    }

    public short getMatk() {
        return this.matk;
    }

    public short getWdef() {
        return this.wdef;
    }

    public short getMdef() {
        return this.mdef;
    }

    public short getAcc() {
        return this.acc;
    }

    public short getAvoid() {
        return this.avoid;
    }

    public short getHands() {
        return this.hands;
    }

    public short getSpeed() {
        return this.speed;
    }
    
        public byte getBossDamage() {
        return bossDamage;
    }

    public void setBossDamage(byte bossDamage) {
        this.bossDamage = bossDamage;
    }

    public byte getIgnorePDR() {
        return ignorePDR;
    }

    public void setIgnorePDR(byte ignorePDR) {
        this.ignorePDR = ignorePDR;
    }

    public byte getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(byte totalDamage) {
        this.totalDamage = totalDamage;
    }

    public byte getAllStat() {
        return allStat;
    }

    public void setAllStat(byte allStat) {
        this.allStat = allStat;
    }

    public short getJump() {
        return this.jump;
    }

    public void setStr(short str) {
        if (str < 0) {
            str = 0;
        }
        this.str = str;
    }

    public void setDex(short dex) {
        if (dex < 0) {
            dex = 0;
        }
        this.dex = dex;
    }

    public void setInt(short _int) {
        if (_int < 0) {
            _int = 0;
        }
        this._int = _int;
    }

    public void setLuk(short luk) {
        if (luk < 0) {
            luk = 0;
        }
        this.luk = luk;
    }

    public void setHp(short hp) {
        if (hp < 0) {
            hp = 0;
        }
        this.hp = hp;
    }

    public void setMp(short mp) {
        if (mp < 0) {
            mp = 0;
        }
        this.mp = mp;
    }

    public void setWatk(short watk) {
        if (watk < 0) {
            watk = 0;
        }
        this.watk = watk;
    }

    public void setMatk(short matk) {
        if (matk < 0) {
            matk = 0;
        }
        this.matk = matk;
    }

    public void setWdef(short wdef) {
        if (wdef < 0) {
            wdef = 0;
        }
        this.wdef = wdef;
    }

    public void setMdef(short mdef) {
        if (mdef < 0) {
            mdef = 0;
        }
        this.mdef = mdef;
    }

    public void setAcc(short acc) {
        if (acc < 0) {
            acc = 0;
        }
        this.acc = acc;
    }

    public void setAvoid(short avoid) {
        if (avoid < 0) {
            avoid = 0;
        }
        this.avoid = avoid;
    }

    public void setHands(short hands) {
        if (hands < 0) {
            hands = 0;
        }
        this.hands = hands;
    }

    public void setSpeed(short speed) {
        if (speed < 0) {
            speed = 0;
        }
        this.speed = speed;
    }

    public void setJump(short jump) {
        if (jump < 0) {
            jump = 0;
        }
        this.jump = jump;
    }

    public void setUpgradeSlots(byte upgradeSlots) {
        this.upgradeSlots = upgradeSlots;
    }

    public byte getLevel() {
        return this.level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    public byte getViciousHammer() {
        return this.vicioushammer;
    }

    public void setViciousHammer(byte ham) {
        this.vicioushammer = ham;
    }

    public int getItemEXP() {
        return this.itemEXP;
    }

    public void setItemEXP(int itemEXP) {
        if (itemEXP < 0) {
            itemEXP = 0;
        }
        this.itemEXP = itemEXP;
    }

    public int getEquipExp() {
        if (this.itemEXP <= 0) {
            return 0;
        }

        if (GameConstants.isWeapon(getItemId())) {
            return this.itemEXP / 700000;
        }
        return this.itemEXP / 350000;
    }

    public int getEquipExpForLevel() {
        if (getEquipExp() <= 0) {
            return 0;
        }
        int expz = getEquipExp();
        for (int i = getBaseLevel(); (i <= GameConstants.getMaxLevel(getItemId()))
                && (expz >= GameConstants.getExpForLevel(i, getItemId())); i++) {
            expz -= GameConstants.getExpForLevel(i, getItemId());
        }

        return expz;
    }

    public int getExpPercentage() {
        if ((getEquipLevel() < getBaseLevel()) || (getEquipLevel() > GameConstants.getMaxLevel(getItemId())) || (GameConstants.getExpForLevel(getEquipLevel(), getItemId()) <= 0)) {
            return 0;
        }
        return getEquipExpForLevel() * 100 / GameConstants.getExpForLevel(getEquipLevel(), getItemId());
    }

    public int getEquipLevel() {
        if (GameConstants.getMaxLevel(getItemId()) <= 0) {
            return 0;
        }
        if (getEquipExp() <= 0) {
            return getBaseLevel();
        }
        int levelz = getBaseLevel();
        int expz = getEquipExp();
        for (int i = levelz; (GameConstants.getStatFromWeapon(getItemId()) == null ? i <= GameConstants.getMaxLevel(getItemId()) : i < GameConstants.getMaxLevel(getItemId()))
                && (expz >= GameConstants.getExpForLevel(i, getItemId())); i++) {
            levelz++;
            expz -= GameConstants.getExpForLevel(i, getItemId());
        }

        return levelz;
    }

    public int getBaseLevel() {
        return GameConstants.getStatFromWeapon(getItemId()) == null ? 1 : 0;
    }

    public void setQuantity(short quantity) {
        if ((quantity < 0) || (quantity > 1)) {
            throw new RuntimeException("Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")");
        }
        super.setQuantity(quantity);
    }

    public int getDurability() {
        return this.durability;
    }

    public void setDurability(int dur) {
        this.durability = dur;
    }

    public byte getEnhance() {
        return this.enhance;
    }

    public void setEnhance(byte en) {
        this.enhance = en;
    }

    public int getPotential1() {
        return this.potential1;
    }

    public void setPotential1(int en) {
        this.potential1 = en;
    }

    public int getPotential2() {
        return this.potential2;
    }

    public void setPotential2(int en) {
        this.potential2 = en;
    }

    public int getPotential3() {
        return this.potential3;
    }

    public void setPotential3(int en) {
        this.potential3 = en;
    }

    public int getPotential4() {
        return this.potential4;
    }

    public void setPotential4(int en) {
        this.potential4 = en;
    }

    public int getPotential5() {
        return this.potential5;
    }

    public void setPotential5(int en) {
        this.potential5 = en;
    }

    public int getPotential6() {
        return this.potential6;
    }

    public void setPotential6(int en) {
        this.potential6 = en;
    }

    public int getPotential7() {
        return this.potential7;
    }

    public void setPotential7(int en) {
        this.potential7 = en;
    }

    public int getPotential8() {
        return this.potential8;
    }

    public void setPotential8(int en) {
        this.potential8 = en;
    }

    public byte getState() {
        int pots = this.potential1 + this.potential2 + this.potential3;
        if (this.potential1 < 0) {
            return 1;
        }
        if ((this.potential1 >= 40000) || (this.potential2 >= 40000) || (this.potential3 >= 40000)) {
            return 20;
        }
        if ((this.potential1 >= 30000) || (this.potential2 >= 30000) || (this.potential3 >= 30000)) {
            return 19;
        }
        if ((this.potential1 >= 20000) || (this.potential2 >= 20000) || (this.potential3 >= 20000)) {
            return 18;
        }
        if (pots >= 1) {
            return 17;
        }
        if (pots < 0) {
            return 1;
        }
        return 0;
    }

    public void resetPotential_Fuse(boolean half, int potentialState) {
        potentialState = -potentialState;
        if (Randomizer.nextInt(100) < 4) {
            potentialState -= (Randomizer.nextInt(100) < 4 ? 2 : 1);
        }
        setPotential1(potentialState);
        setPotential2(Randomizer.nextInt(half ? 5 : 10) == 0 ? potentialState : 0);
        setPotential3(0);
        setPotential4(0);
        setPotential5(0);
    }

    public void resetPotential() { //equip first one, scroll hidden on it
        //no legendary, 0.16% chance unique, 4% chance epic, else rare
        final int rank = Randomizer.nextInt(100) < 4 ? (Randomizer.nextInt(100) < 4 ? -19 : -18) : -17;
        setPotential1(rank);
        setPotential2((Randomizer.nextInt(10) == 0 ? rank : 0)); //1/10 chance of 3 line
        setPotential3(0); //just set it theoretically
   //     setPotential4(0); //just set it theoretically
   //     setPotential5(0); //just set it theoretically
    }

   
    public void renewPotential(int type) { // 0 = normal miracle cube, 1 = premium, 2 = epic pot scroll, 3 = super, 5 = enlightening
        Random rand = new Random();
        //final int rank = type == 2 ? -18 : type == 5 ? (Randomizer.nextInt(100) < 3 && getState() != 20 ? -20 : Randomizer.nextInt(100) < 10 && getState() != 20 ? -(getState() + 1) : -(getState())) : (Randomizer.nextInt(100) < 4 && getState() != (type == 3 ? 20 : 19) ? -(getState() + 1) : -(getState())); // 4 % chance to up 1 tier
        final int rank = type == 2 ? -18 : (Randomizer.nextInt(100) < 4 && getState() != (type == 3 ? 20 : 19) ? -(getState() + 1) : -(getState())); // 4 % chance to up 1 tier
        setPotential1(getState() == 20 && rand.nextInt(100) <= 1 ? 60002 : rank);
        if (getPotential3() > 0) {
            setPotential2(getState() == 20 && rand.nextInt(100) <= 1 ? 60002 :rank); // put back old 3rd line
        } else {
            switch (type) {
                case 1: // premium-> suppose to be 25%
                    setPotential2(Randomizer.nextInt(10) == 0 ? rank : 0); //1/10 chance of 3 line
                    break;
                case 2: // epic pot
                    setPotential2(Randomizer.nextInt(10) >= 1 ? rank : 0); //2/10 chance of 3 line
                    break;
                case 3: // super
                    setPotential2(getState() == 20 && rand.nextInt(100) <= 1 ? 60002 : Randomizer.nextInt(10) <= 2 ? rank : 0); //3/10 chance of 3 line
                    break;
                case 4: // revolutionary
                    setPotential2(getState() == 20 && rand.nextInt(100) <= 1 ? 60002 : Randomizer.nextInt(10) <= 3 ? rank : 0); //4/10 chance of 3 line
                    break;
                case 5: // enlightening
                    setPotential2(getState() == 20 && rand.nextInt(100) <= 1 ? 60002 : Randomizer.nextInt(10) <= 2 ? rank : 0); //3/10 chance of 3 line
                    break;
                default:
                    setPotential2(0);
                    break;
            }
        }
         if (type == 3) { // super
            setPotential3(getState() == 20 && rand.nextInt(100) <= 1 ? 60002 : Randomizer.nextInt(100) <= 2 ? rank : 0); // 3/100 to get 4 lines
        } else { // premium cannot get 3 lines.
            setPotential3(0); //just set it theoretically
        }

      //  setPotential5(0); //just set it theoretically
    }


    public int getIncSkill() {
        return this.incSkill;
    }

    public void setIncSkill(int inc) {
        this.incSkill = inc;
    }

    public short getCharmEXP() {
        return this.charmExp;
    }

    public short getPVPDamage() {
        return this.pvpDamage;
    }

    public void setCharmEXP(short s) {
        this.charmExp = s;
    }

    public void setPVPDamage(short p) {
        this.pvpDamage = p;
    }

    public MapleRing getRing() {
        if ((!GameConstants.isEffectRing(getItemId())) || (getUniqueId() <= 0)) {
            return null;
        }
        if (this.ring == null) {
            this.ring = MapleRing.loadFromDb(getUniqueId(), getPosition() < 0);
        }
        return this.ring;
    }

    public void setRing(MapleRing ring) {
        this.ring = ring;
    }

    public MapleAndroid getAndroid() {
        if ((getItemId() / 10000 != 166) || (getUniqueId() <= 0)) {
            return null;
        }
        if (this.android == null) {
            this.android = MapleAndroid.loadFromDb(getItemId(), getUniqueId());
        }
        return this.android;
    }

    public void setAndroid(MapleAndroid ring) {
        this.android = ring;
    }

    public short getSocketState() {
        int flag = 0;
        if ((this.socket1 != -1) || (this.socket2 != -1) || (this.socket3 != -1)) {
            flag |= SocketFlag.DEFAULT.getValue();
        }
        if (this.socket1 != -1) {
            flag |= SocketFlag.SOCKET_BOX_1.getValue();
        }
        if (this.socket1 > 0) {
            flag |= SocketFlag.USED_SOCKET_1.getValue();
        }
        if (this.socket2 != -1) {
            flag |= SocketFlag.SOCKET_BOX_2.getValue();
        }
        if (this.socket2 > 0) {
            flag |= SocketFlag.USED_SOCKET_2.getValue();
        }
        if (this.socket3 != -1) {
            flag |= SocketFlag.SOCKET_BOX_3.getValue();
        }
        if (this.socket3 > 0) {
            flag |= SocketFlag.USED_SOCKET_3.getValue();
        }
        return (short) flag;
    }

    public int getSocket1() {
        return this.socket1;
    }

    public void setSocket1(int socket1) {
        this.socket1 = socket1;
    }

    public int getSocket2() {
        return this.socket2;
    }

    public void setSocket2(int socket2) {
        this.socket2 = socket2;
    }

    public int getSocket3() {
        return this.socket3;
    }

    public void setSocket3(int socket3) {
        this.socket3 = socket3;
    }

    public static enum ScrollResult {

        SUCCESS, FAIL, CURSE;
    }
    
}