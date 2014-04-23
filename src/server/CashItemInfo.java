package server;

public class CashItemInfo {

    private int itemId;
    private int count;
    private int price;
    private int sn;
    private int expire;
    private int gender;
    private boolean onSale;

    public CashItemInfo(int itemId, int count, int price, int sn, int expire, int gender, boolean sale) {
        this.itemId = itemId;
        this.count = count;
        this.price = price;
        this.sn = sn;
        this.expire = expire;
        this.gender = gender;
        this.onSale = sale;
    }

    public int getId() {
        return this.itemId;
    }

    public int getCount() {
        return this.count;
    }

    public int getPrice() {
        return this.price;
    }

    public int getSN() {
        return this.sn;
    }

    public int getPeriod() {
        return this.expire;
    }

    public int getGender() {
        return this.gender;
    }

    public boolean onSale() {
        return (this.onSale) || ((CashItemFactory.getInstance().getModInfo(this.sn) != null) && (CashItemFactory.getInstance().getModInfo(this.sn).showUp));
    }

    public boolean genderEquals(int g) {
        return (g == this.gender) || (this.gender == 2);
    }

    public static class CashModInfo {

        public int discountPrice;
        public int mark;
        public int priority;
        public int sn;
        public int itemid;
        public int flags;
        public int period;
        public int gender;
        public int count;
        public int meso;
        public int unk_1;
        public int unk_2;
        public int unk_3;
        public int extra_flags;
        public boolean showUp;
        public boolean packagez;
        private CashItemInfo cii;

        public CashModInfo(int sn, int discount, int mark, boolean show, int itemid, int priority, boolean packagez, int period, int gender, int count, int meso, int unk_1, int unk_2, int unk_3, int extra_flags) {
            this.sn = sn;
            this.itemid = itemid;
            this.discountPrice = discount;
            this.mark = mark;
            this.showUp = show;
            this.priority = priority;
            this.packagez = packagez;
            this.period = period;
            this.gender = gender;
            this.count = count;
            this.meso = meso;
            this.unk_1 = unk_1;
            this.unk_2 = unk_2;
            this.unk_3 = unk_3;
            this.extra_flags = extra_flags;
            this.flags = extra_flags;

            if (this.itemid > 0) {
                this.flags |= 1;
            }
            if (this.count > 0) {
                this.flags |= 2;
            }
            if (this.discountPrice > 0) {
                this.flags |= 4;
            }
            if (this.unk_1 > 0) {
                this.flags |= 8;
            }
            if (this.priority >= 0) {
                this.flags |= 16;
            }
            if (this.period > 0) {
                this.flags |= 32;
            }

            if (this.meso > 0) {
                this.flags |= 128;
            }
            if (this.unk_2 > 0) {
                this.flags |= 256;
            }
            if (this.gender >= 0) {
                this.flags |= 512;
            }
            if (this.showUp) {
                this.flags |= 1024;
            }
            if ((this.mark >= -1) || (this.mark <= 3)) {
                this.flags |= 2048;
            }
            if (this.unk_3 > 0) {
                this.flags |= 4096;
            }

            if (this.packagez) {
                this.flags |= 262144;
            }
        }

        public CashItemInfo toCItem(CashItemInfo backup) {
            if (this.cii != null) {
                return this.cii;
            }
            int item;
            if (this.itemid <= 0) {
                item = backup == null ? 0 : backup.getId();
            } else {
                item = this.itemid;
            }
            int c;
            if (this.count <= 0) {
                c = backup == null ? 0 : backup.getCount();
            } else {
                c = this.count;
            }
            int price;
            if (this.meso <= 0) {
                if (this.discountPrice <= 0) {
                    price = backup == null ? 0 : backup.getPrice();
                } else {
                    price = this.discountPrice;
                }
            } else {
                price = this.meso;
            }
            int expire;
            if (this.period <= 0) {
                expire = backup == null ? 0 : backup.getPeriod();
            } else {
                expire = this.period;
            }
            int gen;
            if (this.gender < 0) {
                gen = backup == null ? 0 : backup.getGender();
            } else {
                gen = this.gender;
            }
            boolean onSale;
            if (!this.showUp) {
                onSale = backup == null ? false : backup.onSale();
            } else {
                onSale = this.showUp;
            }

            this.cii = new CashItemInfo(item, c, price, this.sn, expire, gen, onSale);
            return this.cii;
        }
    }
}