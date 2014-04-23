package client.inventory;

import java.io.Serializable;

public class Item
        implements Comparable<Item>, Serializable {

    private final int id;
    private short position;
    private short quantity;
    private short flag;
    private long expiration = -1L;
    private long inventoryitemid = 0L;
    private MaplePet pet = null;
    private int uniqueid;
    private String owner = "";
    private String GameMaster_log = "";
    private String giftFrom = "";

    public Item(int id, short position, short quantity, short flag, int uniqueid) {
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.flag = flag;
        this.uniqueid = uniqueid;
    }

    public Item(int id, short position, short quantity, short flag) {
        this.id = id;
        this.position = position;
        this.quantity = quantity;
        this.flag = flag;
        this.uniqueid = -1;
    }

    public Item(int id, byte position, short quantity) {
        this.id = id;
        this.position = ((short) position);
        this.quantity = quantity;
        this.uniqueid = -1;
    }

    public Item copy() {
        Item ret = new Item(this.id, this.position, this.quantity, this.flag, this.uniqueid);
        ret.pet = this.pet;
        ret.owner = this.owner;
        ret.GameMaster_log = this.GameMaster_log;
        ret.expiration = this.expiration;
        ret.giftFrom = this.giftFrom;
        return ret;
    }

    public Item copyWithQuantity(short qq) {
        Item ret = new Item(this.id, this.position, qq, this.flag, this.uniqueid);
        ret.pet = this.pet;
        ret.owner = this.owner;
        ret.GameMaster_log = this.GameMaster_log;
        ret.expiration = this.expiration;
        ret.giftFrom = this.giftFrom;
        return ret;
    }

    public final void setPosition(short position) {
        this.position = position;

        if (this.pet != null) {
            this.pet.setInventoryPosition(position);
        }
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }

    public final int getItemId() {
        return this.id;
    }

    public final short getPosition() {
        return this.position;
    }

    public final short getFlag() {
        return this.flag;
    }

    public final short getQuantity() {
        return this.quantity;
    }

    public byte getType() {
        return 2;
    }

    public final String getOwner() {
        return this.owner;
    }

    public final void setOwner(String owner) {
        this.owner = owner;
    }

    public final void setFlag(short flag) {
        this.flag = flag;
    }

    public final long getExpiration() {
        return this.expiration;
    }

    public final void setExpiration(long expire) {
        this.expiration = expire;
    }

    public final String getGMLog() {
        return this.GameMaster_log;
    }

    public void setGMLog(String GameMaster_log) {
        this.GameMaster_log = GameMaster_log;
    }

    public final int getUniqueId() {
        return this.uniqueid;
    }

    public void setUniqueId(int ui) {
        this.uniqueid = ui;
    }

    public final long getInventoryId() {
        return this.inventoryitemid;
    }

    public void setInventoryId(long ui) {
        this.inventoryitemid = ui;
    }

    public final MaplePet getPet() {
        return this.pet;
    }

    public final void setPet(MaplePet pet) {
        this.pet = pet;
        if (pet != null) {
            this.uniqueid = pet.getUniqueId();
        }
    }

    public void setGiftFrom(String gf) {
        this.giftFrom = gf;
    }

    public String getGiftFrom() {
        return this.giftFrom;
    }

    public int compareTo(Item other) {
        if (Math.abs(this.position) < Math.abs(other.getPosition())) {
            return -1;
        }
        if (Math.abs(this.position) == Math.abs(other.getPosition())) {
            return 0;
        }
        return 1;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Item)) {
            return false;
        }
        Item ite = (Item) obj;
        return (this.uniqueid == ite.getUniqueId()) && (this.id == ite.getItemId()) && (this.quantity == ite.getQuantity()) && (Math.abs(this.position) == Math.abs(ite.getPosition()));
    }

    public String toString() {
        return "Item: " + this.id + " quantity: " + this.quantity;
    }
}