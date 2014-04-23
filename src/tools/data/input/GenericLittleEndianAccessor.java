package tools.data.input;

import java.awt.Point;

public class GenericLittleEndianAccessor
        implements LittleEndianAccessor {

    private final ByteInputStream bs;

    public GenericLittleEndianAccessor(ByteInputStream bs) {
        this.bs = bs;
    }

    public final int readByteAsInt() {
        return this.bs.readByte();
    }

    public final byte readByte() {
        return (byte) this.bs.readByte();
    }

    public final int readInt() {
        int byte1 = this.bs.readByte();
        int byte2 = this.bs.readByte();
        int byte3 = this.bs.readByte();
        int byte4 = this.bs.readByte();
        return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    public final short readShort() {
        int byte1 = this.bs.readByte();
        int byte2 = this.bs.readByte();
        return (short) ((byte2 << 8) + byte1);
    }

    public final char readChar() {
        return (char) readShort();
    }

    public final long readLong() {
        int byte1 = this.bs.readByte();
        int byte2 = this.bs.readByte();
        int byte3 = this.bs.readByte();
        int byte4 = this.bs.readByte();
        long byte5 = this.bs.readByte();
        long byte6 = this.bs.readByte();
        long byte7 = this.bs.readByte();
        long byte8 = this.bs.readByte();

        return (byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) + (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    public final float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public final double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public final String readAsciiString(int n) {
        char[] ret = new char[n];
        for (int x = 0; x < n; x++) {
            ret[x] = ((char) readByte());
        }
        return new String(ret);
    }

    public final long getBytesRead() {
        return this.bs.getBytesRead();
    }

    public final String readMapleAsciiString() {
        return readAsciiString(readShort());
    }

    public final Point readPos() {
        int x = readShort();
        int y = readShort();
        return new Point(x, y);
    }

    public final byte[] read(int num) {
        byte[] ret = new byte[num];
        for (int x = 0; x < num; x++) {
            ret[x] = readByte();
        }
        return ret;
    }

    public void skip(int num) {
        for (int x = 0; x < num; x++) {
            readByte();
        }
    }

    public final long available() {
        return this.bs.available();
    }

    public final String toString() {
        return this.bs.toString();
    }

    public final String toString(boolean b) {
        return this.bs.toString(b);
    }
}