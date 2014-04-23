package clientside;

public enum MapleStat {

    SKIN(1L),
    FACE(2L),
    HAIR(4L),
    LEVEL(16L),
    JOB(32L),
    STR(64L),
    DEX(128L),
    INT(256L),
    LUK(512L),
    HP(1024L),
    MAXHP(2048L),
    MP(4096L),
    MAXMP(8192L),
    AVAILABLEAP(0x4000),
    AVAILABLESP(0x8000),
    EXP(65536L),
    FAME(131072L),
    MESO(262144L),
    PET(1572872L),
    GACHAPONEXP(2097152L),
    FATIGUE(4194304L),
    CHARISMA(8388608L),
    INSIGHT(16777216L),
    WILL(33554432L),
    CRAFT(67108864L),
    SENSE(134217728L),
    CHARM(268435456L),
    TRAIT_LIMIT(536870912L),
    BATTLE_EXP(1073741824L),
    BATTLE_RANK(2147483648L),
    BATTLE_POINTS(4294967296L),
    ICE_GAGE(8589934592L),
    VIRTUE(17179869184L);
    private final long i;

    private MapleStat(long i) {
        this.i = i;
    }

    public long getValue() {
        return this.i;
    }

    public static final MapleStat getByValue(long value) {
        for (MapleStat stat : values()) {
            if (stat.i == value) {
                return stat;
            }
        }
        return null;
    }

    public static enum Temp {

        STR(1),
        DEX(2),
        INT(4),
        LUK(8),
        WATK(16),
        WDEF(32),
        MATK(64),
        MDEF(128),
        ACC(256),
        AVOID(512),
        SPEED(1024),
        JUMP(2048),
        UNKNOWN(4096);
        private final int i;

        private Temp(int i) {
            this.i = i;
        }

        public int getValue() {
            return this.i;
        }
    }
}