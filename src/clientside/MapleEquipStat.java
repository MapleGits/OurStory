package clientside;

public enum MapleEquipStat {

    SLOTS(1),
    Level(2),
    STR(4),
    DEX(8),
    INT(16),
    LUK(32),
    maxHp(64),
    maxMp(128),
    WATK(256),
    MATK(512),
    WDEF(1024),
    MDEF(2048),
    ACC(4096),
    AVOID(8192),
    Hands(16384),
    SPEED(32768),
    JUMP(65536),
    flag(131072),
    Hammer(4194304);
    private final int i;

    private MapleEquipStat(int i) {
        this.i = i;
    }

    public int getValue() {
        return this.i;
    }
}