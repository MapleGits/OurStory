package client.inventory;

public enum MapleWeaponType {

    NOT_A_WEAPON(1.43F, 20),
    BOW(1.2F, 15),
    CLAW(1.75F, 15),
    CANE(1.3F, 15),
    DAGGER(1.3F, 20),
    CROSSBOW(1.35F, 15),
    AXE1H(1.2F, 20),
    SWORD1H(1.2F, 20),
    BLUNT1H(1.2F, 20),
    AXE2H(1.32F, 20),
    SWORD2H(1.32F, 20),
    BLUNT2H(1.32F, 20),
    POLE_ARM(1.49F, 20),
    SPEAR(1.49F, 20),
    STAFF(1.0F, 25),
    FAN(1.5f, 20),
    SHINING_ROD(1.0F, 25),
    WAND(1.0F, 25),
    KNUCKLE(1.7F, 20),
    GUN(1.5F, 15),
    KATANA(1.5f, 20),
    CANNON(1.35F, 15),
    DUAL_BOW(1.35F, 15),
    MAGIC_ARROW(2.0F, 15),
    CARTE(2.0F, 15),
    KATARA(1.3F, 20),
    SOULSHOOTER(1.22f, 20),
    DESPERADO(1.23f, 20), ENERGYSWORD(1.23f, 20);
    private final float damageMultiplier;
    private final int baseMastery;

    private MapleWeaponType(float maxDamageMultiplier, int baseMastery) {
        this.damageMultiplier = maxDamageMultiplier;
        this.baseMastery = baseMastery;
    }

    public final float getMaxDamageMultiplier() {
        return this.damageMultiplier;
    }

    public final int getBaseMastery() {
        return this.baseMastery;
    }
}