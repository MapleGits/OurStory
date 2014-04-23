package server;

public class MapleShopItem {

    private short buyable;
    private int itemId;
    private int price;
    private int reqItem;
    private int reqItemQ;
    private byte rank;
    private int period;
    private int state;

    public MapleShopItem(int itemId, int price, short buyable) {
        this.buyable = buyable;
        this.itemId = itemId;
        this.price = price;
        this.reqItem = 0;
        this.reqItemQ = 0;
        this.rank = 0;
        this.period = 0;
        this.state = 0;
    }

    public MapleShopItem(short buyable, int itemId, int price, int reqItem, int reqItemQ, int period, int state, byte rank) {
        this.buyable = buyable;
        this.itemId = itemId;
        this.price = price;
        this.reqItem = reqItem;
        this.reqItemQ = reqItemQ;
        this.rank = rank;
        this.period = period;
        this.state = state;
    }

    public short getBuyable() {
        return this.buyable;
    }

    public int getItemId() {
        return this.itemId;
    }

    public int getPrice() {
        return this.price;
    }

    public int getReqItem() {
        return this.reqItem;
    }

    public int getReqItemQ() {
        return this.reqItemQ;
    }

    public byte getRank() {
        return this.rank;
    }

    public int getPeriod() {
        return this.period;
    }

    public int getState() {
        return this.state;
    }
}