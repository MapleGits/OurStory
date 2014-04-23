package tools.data.output;

import java.awt.Point;
import java.nio.charset.Charset;

public class GenericLittleEndianWriter
        implements LittleEndianWriter {

    private static final Charset ASCII = Charset.forName("US-ASCII");
    private ByteOutputStream bos;

    protected GenericLittleEndianWriter() {
    }

    protected void setByteOutputStream(ByteOutputStream bos) {
        this.bos = bos;
    }

    public GenericLittleEndianWriter(ByteOutputStream bos) {
        this.bos = bos;
    }

    public final void writeZeroBytes(int i) {
        for (int x = 0; x < i; x++) {
            this.bos.writeByte((byte) 0);
        }
    }

    public final void write(byte[] b) {
        for (int x = 0; x < b.length; x++) {
            this.bos.writeByte(b[x]);
        }
    }

    public final void write(byte b) {
        this.bos.writeByte(b);
    }

    public final void write(int b) {
        this.bos.writeByte((byte) b);
    }

    public final void writeShort(short i) {
        this.bos.writeByte((byte) (i & 0xFF));
        this.bos.writeByte((byte) (i >>> 8 & 0xFF));
    }

    public final void writeShort(int i) {
        this.bos.writeByte((byte) (i & 0xFF));
        this.bos.writeByte((byte) (i >>> 8 & 0xFF));
    }

    public final void writeInt(int i) {
        this.bos.writeByte((byte) (i & 0xFF));
        this.bos.writeByte((byte) (i >>> 8 & 0xFF));
        this.bos.writeByte((byte) (i >>> 16 & 0xFF));
        this.bos.writeByte((byte) (i >>> 24 & 0xFF));
    }

    public final void writeAsciiString(String s) {
        write(s.getBytes(ASCII));
    }

    public final void writeAsciiString(String s, int max) {
        if (s.length() > max) {
            s = s.substring(0, max);
        }
        write(s.getBytes(ASCII));
        for (int i = s.length(); i < max; i++) {
            write(0);
        }
    }

    public final void writeMapleAsciiString(String s) {
        writeShort((short) s.length());
        writeAsciiString(s);
    }

    public final void writePos(Point s) {
        writeShort(s.x);
        writeShort(s.y);
    }

    public final void writeLong(long l) {
        this.bos.writeByte((byte) (int) (l & 0xFF));
        this.bos.writeByte((byte) (int) (l >>> 8 & 0xFF));
        this.bos.writeByte((byte) (int) (l >>> 16 & 0xFF));
        this.bos.writeByte((byte) (int) (l >>> 24 & 0xFF));
        this.bos.writeByte((byte) (int) (l >>> 32 & 0xFF));
        this.bos.writeByte((byte) (int) (l >>> 40 & 0xFF));
        this.bos.writeByte((byte) (int) (l >>> 48 & 0xFF));
        this.bos.writeByte((byte) (int) (l >>> 56 & 0xFF));
    }
}