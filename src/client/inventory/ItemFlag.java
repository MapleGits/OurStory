package client.inventory;

public enum ItemFlag {

    LOCK(1),
    SPIKES(2),
    COLD(4),
    UNTRADEABLE(8),
    KARMA_EQ(16),
    KARMA_USE(2),
    CHARM_EQUIPPED(32),
    ANDROID_ACTIVATED(64),
    CRAFTED(128),
    CRAFTED_USE(16),
    SHIELD_WARD(256),
    LUCKS_KEY(512),
    KARMA_ACC_USE(1024),
    KARMA_ACC(4096),
    SLOTS_PROTECT(8192),
    SCROLL_PROTECT(16384);
    private final int i;

    private ItemFlag(int i) {
        this.i = i;
    }

    public final int getValue() {
        return this.i;
    }

    public final boolean check(int flag) {
        return (flag & this.i) == this.i;
    }
}