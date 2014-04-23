package clientside;

import server.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;

public class PlayerRandomStream {

    private transient long seed1;
    private transient long seed2;
    private transient long seed3;

    public PlayerRandomStream() {
        int v4 = 5;
        CRand32__Seed(Randomizer.nextLong(), 803157710L, 803157710L);
    }

    public final void CRand32__Seed(long s1, long s2, long s3) {
        this.seed1 = (s1 | 0x100000);
        this.seed2 = (s2 | 0x1000);
        this.seed3 = (s3 | 0x10);
    }

    public final long CRand32__Random() {
        long v8 = (this.seed1 & 0xFFFFFFFE) << 12 ^ (this.seed1 & 0x7FFC0 ^ this.seed1 >> 13) >> 6;
        long v9 = 16L * (this.seed2 & 0xFFFFFFF8) ^ (this.seed2 >> 2 ^ this.seed2 & 0x3F800000) >> 23;
        long v10 = (this.seed3 & 0xFFFFFFF0) << 17 ^ (this.seed3 >> 3 ^ this.seed3 & 0x1FFFFF00) >> 8;
        return (v8 ^ v9 ^ v10) & 0xFFFFFFFF;
    }

    public final void connectData(MaplePacketLittleEndianWriter mplew) {
        long v5 = CRand32__Random();
        long s2 = CRand32__Random();
        long v6 = CRand32__Random();

        CRand32__Seed(v5, s2, v6);

        mplew.writeInt((int) v5);
        mplew.writeInt((int) s2);
        mplew.writeInt((int) v6);
    }
}