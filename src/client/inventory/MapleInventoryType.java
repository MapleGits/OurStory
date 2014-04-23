package client.inventory;

public enum MapleInventoryType {

    UNDEFINED(0),
    EQUIP(1),
    USE(2),
    SETUP(3),
    ETC(4),
    CASH(5),
    EQUIPPED(-1);
    final byte type;

    private MapleInventoryType(int type) {
        this.type = ((byte) type);
    }

    public byte getType() {
        return this.type;
    }

    public short getBitfieldEncoding() {
        return (short) (2 << this.type);
    }

    public static MapleInventoryType getByType(byte type) {
        for (MapleInventoryType l : values()) {
            if (l.getType() == type) {
                return l;
            }
        }
        return null;
    }

    public static MapleInventoryType getByWZName(String name) {
        if (name.equals("Install")) {
            return SETUP;
        }
        if (name.equals("Consume")) {
            return USE;
        }
        if (name.equals("Etc")) {
            return ETC;
        }
        if (name.equals("Eqp")) {
            return EQUIP;
        }
        if (name.equals("Cash")) {
            return CASH;
        }
        if (name.equals("Pet")) {
            return CASH;
        }
        return UNDEFINED;
    }
}