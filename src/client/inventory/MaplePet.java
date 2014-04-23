package client.inventory;

import constants.GameConstants;
import database.DatabaseConnection;
import java.awt.Point;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;

public class MaplePet
        implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    private String name;
    private int Fh = 0;
    private int stance = 0;
    private int uniqueid;
    private int petitemid;
    private int secondsLeft = 0;
    private Point pos;
    private byte fullness = 100;
    private byte level = 30;
    private byte summoned = 0;
    private short inventorypos = 0;
    private short closeness = 30000;
    private short flags = 0;
    private boolean changed = false;

    private MaplePet(int petitemid, int uniqueid) {
        this.petitemid = petitemid;
        this.uniqueid = uniqueid;
    }

    private MaplePet(int petitemid, int uniqueid, short inventorypos) {
        this.petitemid = petitemid;
        this.uniqueid = uniqueid;
        this.inventorypos = inventorypos;
    }

    public static final MaplePet loadFromDb(int itemid, int petid, short inventorypos) {
        try {
            MaplePet ret = new MaplePet(itemid, petid, inventorypos);

            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM pets WHERE petid = ?");
            ps.setInt(1, petid);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }

            ret.setName(rs.getString("name"));
            ret.setCloseness(rs.getShort("closeness"));
            ret.setLevel(rs.getByte("level"));
            ret.setFullness(rs.getByte("fullness"));
            ret.setSecondsLeft(rs.getInt("seconds"));
            ret.setFlags(rs.getShort("flags"));
            ret.changed = false;

            rs.close();
            ps.close();

            return ret;
        } catch (SQLException ex) {
            Logger.getLogger(MaplePet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public final void saveToDb() {
        if (!this.changed) {
            return;
        }
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE pets SET name = ?, level = ?, closeness = ?, fullness = ?, seconds = ?, flags = ? WHERE petid = ?");
            ps.setString(1, this.name);
            ps.setByte(2, this.level);
            ps.setShort(3, this.closeness);
            ps.setByte(4, this.fullness);
            ps.setInt(5, this.secondsLeft);
            ps.setShort(6, this.flags);
            ps.setInt(7, this.uniqueid);
            ps.executeUpdate();
            ps.close();
            this.changed = false;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static final MaplePet createPet(int itemid, int uniqueid) {
        return createPet(itemid, MapleItemInformationProvider.getInstance().getName(itemid), 1, 0, 100, uniqueid, itemid == 5000054 ? 18000 : 0, (short) ((itemid == 5000067) && (!GameConstants.GMS) ? 55 : 0));
    }

    public static final MaplePet createPet(int itemid, String name, int level, int closeness, int fullness, int uniqueid, int secondsLeft, short flag) {
        if (uniqueid <= -1) {
            uniqueid = MapleInventoryIdentifier.getInstance();
        }
        try {
            PreparedStatement pse = DatabaseConnection.getConnection().prepareStatement("INSERT INTO pets (petid, name, level, closeness, fullness, seconds, flags) VALUES (?, ?, ?, ?, ?, ?, ?)");
            pse.setInt(1, uniqueid);
            pse.setString(2, name);
            pse.setByte(3, (byte) level);
            pse.setShort(4, (short) closeness);
            pse.setByte(5, (byte) fullness);
            pse.setInt(6, secondsLeft);
            pse.setShort(7, flag);
            pse.executeUpdate();
            pse.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
        MaplePet pet = new MaplePet(itemid, uniqueid);
        pet.setName(name);
        pet.setLevel(level);
        pet.setFullness(fullness);
        pet.setCloseness(closeness);
        pet.setFlags(flag);
        pet.setSecondsLeft(secondsLeft);

        return pet;
    }

    public final String getName() {
        return this.name;
    }

    public final void setName(String name) {
        this.name = name;
        this.changed = true;
    }

    public final boolean getSummoned() {
        return this.summoned > 0;
    }

    public final byte getSummonedValue() {
        return this.summoned;
    }

    public final void setSummoned(int summoned) {
        this.summoned = ((byte) summoned);
    }

    public final short getInventoryPosition() {
        return this.inventorypos;
    }

    public final void setInventoryPosition(short inventorypos) {
        this.inventorypos = inventorypos;
    }

    public int getUniqueId() {
        return this.uniqueid;
    }

    public final short getCloseness() {
        return this.closeness;
    }

    public final void setCloseness(int closeness) {
        this.closeness = ((short) closeness);
        this.changed = true;
    }

    public final byte getLevel() {
        return this.level;
    }

    public final void setLevel(int level) {
        this.level = ((byte) level);
        this.changed = true;
    }

    public final byte getFullness() {
        return this.fullness;
    }

    public final void setFullness(int fullness) {
        this.fullness = ((byte) fullness);
        this.changed = true;
    }

    public final short getFlags() {
        return this.flags;
    }

    public final void setFlags(int fffh) {
        this.flags = ((short) fffh);
        this.changed = true;
    }

    public final int getFh() {
        return this.Fh;
    }

    public final void setFh(int Fh) {
        this.Fh = Fh;
    }

    public final Point getPos() {
        return this.pos;
    }

    public final void setPos(Point pos) {
        this.pos = pos;
    }

    public final int getStance() {
        return this.stance;
    }

    public final void setStance(int stance) {
        this.stance = stance;
    }

    public final int getPetItemId() {
        return this.petitemid;
    }

    public final boolean canConsume(int itemId) {
        MapleItemInformationProvider mii = MapleItemInformationProvider.getInstance();
        for (Iterator i$ = mii.getItemEffect(itemId).getPetsCanConsume().iterator(); i$.hasNext();) {
            int petId = ((Integer) i$.next()).intValue();
            if (petId == this.petitemid) {
                return true;
            }
        }
        return false;
    }

    public final void updatePosition(List<LifeMovementFragment> movement) {
        for (LifeMovementFragment move : movement) {
            if ((move instanceof LifeMovement)) {
                if ((move instanceof AbsoluteLifeMovement)) {
                    setPos(((LifeMovement) move).getPosition());
                }
                setStance(((LifeMovement) move).getNewstate());
            }
        }
    }

    public final int getSecondsLeft() {
        return this.secondsLeft;
    }

    public final void setSecondsLeft(int sl) {
        this.secondsLeft = sl;
        this.changed = true;
    }

    public static enum PetFlag {

        ITEM_PICKUP(1, 5190000, 5191000),
        EXPAND_PICKUP(2, 5190002, 5191002),
        AUTO_PICKUP(4, 5190003, 5191003),
        UNPICKABLE(8, 5190005, -1),
        LEFTOVER_PICKUP(16, 5190004, 5191004),
        HP_CHARGE(32, 5190001, 5191001),
        MP_CHARGE(64, 5190006, -1),
        PET_BUFF(128, -1, -1),
        PET_DRAW(256, 5190007, -1),
        PET_DIALOGUE(512, 5190008, -1);
        private final int i;
        private final int item;
        private final int remove;

        private PetFlag(int i, int item, int remove) {
            this.i = i;
            this.item = item;
            this.remove = remove;
        }

        public final int getValue() {
            return this.i;
        }

        public final boolean check(int flag) {
            return (flag & this.i) == this.i;
        }

        public static final PetFlag getByAddId(int itemId) {
            for (PetFlag flag : values()) {
                if (flag.item == itemId) {
                    return flag;
                }
            }
            return null;
        }

        public static final PetFlag getByDelId(int itemId) {
            for (PetFlag flag : values()) {
                if (flag.remove == itemId) {
                    return flag;
                }
            }
            return null;
        }
    }
}