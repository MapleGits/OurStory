package client.status;

import clientside.MapleDisease;
import constants.GameConstants;
import handling.Buffstat;
import java.io.Serializable;

public enum MonsterStatus
        implements Serializable, Buffstat {

    WATK(1, 1),
    WDEF(2, 1),
    MATK(4, 1),
    MDEF(8, 1),
    ACC(16, 1),
    AVOID(32, 1),
    SPEED(64, 1),
    STUN(128, 1),
    FREEZE(256, 1),
    POISON(512, 1),
    SEAL(1024, 1),
    SHOWDOWN(2048, 1),
    WEAPON_ATTACK_UP(4096, 1),
    WEAPON_DEFENSE_UP(8192, 1),
    MAGIC_ATTACK_UP(16384, 1),
    MAGIC_DEFENSE_UP(32768, 1),
    DOOM(65536, 1),
    SHADOW_WEB(131072, 1),
    WEAPON_IMMUNITY(262144, 1),
    MAGIC_IMMUNITY(524288, 1),
    DAMAGE_IMMUNITY(2097152, 1),
    NINJA_AMBUSH(4194304, 1),
    BURN(16777216, 1),
    DARKNESS(33554432, 1),
    HYPNOTIZE(268435456, 1),
    WEAPON_DAMAGE_REFLECT(536870912, 1),
    MAGIC_DAMAGE_REFLECT(1073741824, 1),
    NEUTRALISE(2, 2),
    IMPRINT(4, 2),
    MONSTER_BOMB(8, 2),
    MAGIC_CRASH(16, 2),
    EMPTY(134217728, 1, true),
    SUMMON(-2147483648, 1, true),
    EMPTY_1(32, 2, true),
    EMPTY_2(64, 2, true),
    EMPTY_3(128, 2, true),
    EMPTY_4(256, 2, GameConstants.GMS),
    EMPTY_5(512, 2, GameConstants.GMS),
    EMPTY_6(4096, 2, GameConstants.GMS),
    EMPTY_7(524288, 2, true);
    static final long serialVersionUID = 0L;
    private final int i;
    private final int first;
    private final boolean end;

    private MonsterStatus(int i, int first) {
        this.i = i;
        this.first = first;
        this.end = false;
    }

    private MonsterStatus(int i, int first, boolean end) {
        this.i = i;
        this.first = first;
        this.end = end;
    }

    public int getPosition() {
        return this.first;
    }

    public boolean isEmpty() {
        return this.end;
    }

    public int getValue() {
        return this.i;
    }

    public static final MapleDisease getLinkedDisease(final MonsterStatus skill) {
        switch (skill) {
            case STUN:
            case SHADOW_WEB:
                return MapleDisease.STUN;
            case POISON:
            case BURN:
                return MapleDisease.POISON;
            case SEAL:
            case MAGIC_CRASH:
                return MapleDisease.SEAL;
            case FREEZE:
                return MapleDisease.FREEZE;
            case DARKNESS:
                return MapleDisease.DARKNESS;
            case SPEED:
                return MapleDisease.SLOW;
        }
        return null;
    }
}