package server.maps;

import clientside.MapleCharacter;
import clientside.MapleClient;
import clientside.SkillFactory;
import constants.GameConstants;
import java.awt.Point;
import server.MapleStatEffect;
import tools.packet.CField;

public class MapleSummon extends AnimatedMapleMapObject {

    private final int ownerid;
    private final int skillLevel;
    private final int ownerLevel;
    private final int skill;
    private MapleMap map;
    private short hp;
    private boolean changedMap = false;
    private SummonMovementType movementType;
    private int lastSummonTickCount;
    private byte Summon_tickResetCount;
    private long Server_ClientSummonTickDiff;
    private long lastAttackTime;

    public MapleSummon(MapleCharacter owner, MapleStatEffect skill, Point pos, SummonMovementType movementType) {
        this(owner, skill.getSourceId(), skill.getLevel(), pos, movementType);
    }

    public MapleSummon(MapleCharacter owner, int sourceid, int level, Point pos, SummonMovementType movementType) {
        this.ownerid = owner.getId();
        this.ownerLevel = owner.getLevel();
        this.skill = sourceid;
        this.map = owner.getMap();
        this.skillLevel = level;
        this.movementType = movementType;
        setPosition(pos);

        if (!isPuppet()) {
            this.lastSummonTickCount = 0;
            this.Summon_tickResetCount = 0;
            this.Server_ClientSummonTickDiff = 0L;
            this.lastAttackTime = 0L;
        }
    }

    public final void sendSpawnData(MapleClient client) {
    }

    public final void sendDestroyData(MapleClient client) {
        client.getSession().write(CField.SummonPacket.removeSummon(this, false));
    }

    public final void updateMap(MapleMap map) {
        this.map = map;
    }

    public final MapleCharacter getOwner() {
        return this.map.getCharacterById(this.ownerid);
    }

    public final int getOwnerId() {
        return this.ownerid;
    }

    public final int getOwnerLevel() {
        return this.ownerLevel;
    }

    public final int getSkill() {
        return this.skill;
    }

    public final short getHP() {
        return this.hp;
    }

    public final void addHP(short delta) {
        this.hp = ((short) (this.hp + delta));
    }

    public final SummonMovementType getMovementType() {
        return this.movementType;
    }

    public final boolean isPuppet() {
        switch (this.skill) {
            case 3111002:
            case 3120012:
            case 3211002:
            case 3220012:
            case 13111024:
            case 4341006:
            case 33111003:
                return true;
        }
        return isAngel();
    }

    public final boolean isAngel() {
        return GameConstants.isAngel(this.skill);
    }

    public final boolean isMultiAttack() {
        if ((this.skill != 61111002) && (this.skill != 42111003) && (this.skill != 35111002) && (this.skill != 42101021) && (this.skill != 42121021) && (this.skill != 22171052) && (this.skill != 35121003) && ((isGaviota()) || (this.skill == 33101008) || (this.skill >= 35000000)) && (this.skill != 35111009) && (this.skill != 35111010) && (this.skill != 35111001)) {
            return false;
        }
        return true;
    }

    public final boolean isGaviota() {
        return this.skill == 5211002;
    }

    public final boolean isBeholder() {
        return this.skill == 1321007;
    }

    public final boolean isMultiSummon() {
        return (this.skill == 5211002) || (this.skill == 5211001) || (this.skill == 5220002) || (this.skill == 32111006) || (this.skill == 33101008);
    }

    public final boolean isSummon() {
        switch (this.skill) {
            case 1321007:
            case 2121005:
            case 2221005:
            case 36121002:
            case 36121013:
            case 36121014:
            case 2321003:
            case 3101007:
            case 3111005:
            case 3201007:
            case 3211005:
            case 4111007:
            case 4211007:
            case 13111024:
            case 5211001:
            case 5211002:
            case 5220002:
            case 5321003:
            case 5321004:
            case 5711001:
            case 11001004:
            case 12001004:
            case 12111004:
            case 13001004:
            case 14001005:
            case 15001004:
            case 23111008:
            case 23111009:
            case 23111010:
            case 32111006:
            case 33101008:
            case 33111005:
            case 35111001:
            case 35111002:
            case 35111005:
            case 35111009:
            case 35111010:
            case 35111011:
            case 35121003:
            case 35121009:
            case 35121010:
            case 35121011:
            case 42101001:
            case 42111003:
                return true;
        }
        return isAngel();
    }

    public final int getSkillLevel() {
        return this.skillLevel;
    }

    public final int getSummonType() {
        if (isAngel()) {
            return 2;
        }
        if (((this.skill != 33111003) && (this.skill != 3120012) && (this.skill != 3220012) && (isPuppet())) || (this.skill == 33101008) || (this.skill == 35111002)) {
            return 0;
        }
        switch (this.skill) {
            case 1321007:
                return 2;
            case 36121002: // TEST HYPO
            case 36121013: // TEST HYPO
            case 36121014: // TEST HYPO
            case 35111001:
            case 35111009:
            case 35111010:
            case 42111003:
                return 3;
            case 35121009:
                return 5;
            case 35121003:
                return 6;
            case 4111007:
            case 4211007:
                return 7;
            case 42101001:
                return 8;
        }
        return 1;
    }

    public final MapleMapObjectType getType() {
        return MapleMapObjectType.SUMMON;
    }

    public final void CheckSummonAttackFrequency(MapleCharacter chr, int tickcount) {
        int tickdifference = tickcount - this.lastSummonTickCount;
        long STime_TC = System.currentTimeMillis() - tickcount;
        long S_C_Difference = this.Server_ClientSummonTickDiff - STime_TC;
        this.Summon_tickResetCount = ((byte) (this.Summon_tickResetCount + 1));
        if (this.Summon_tickResetCount > 4) {
            this.Summon_tickResetCount = 0;
            this.Server_ClientSummonTickDiff = STime_TC;
        }
        this.lastSummonTickCount = tickcount;
    }

    public final void CheckPVPSummonAttackFrequency(MapleCharacter chr) {
        long tickdifference = System.currentTimeMillis() - this.lastAttackTime;
        this.lastAttackTime = System.currentTimeMillis();
    }

    public final boolean isChangedMap() {
        return this.changedMap;
    }

    public final void setChangedMap(boolean cm) {
        this.changedMap = cm;
    }
}