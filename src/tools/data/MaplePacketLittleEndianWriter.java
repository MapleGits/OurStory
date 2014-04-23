package tools.data;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import tools.HexTool;

public class MaplePacketLittleEndianWriter {

    private final ByteArrayOutputStream baos;
    private static final Charset ASCII = Charset.forName("US-ASCII");
    private final boolean debuglog = false;

    public MaplePacketLittleEndianWriter() {
        this(32);
    }

    public MaplePacketLittleEndianWriter(int size) {
        this.baos = new ByteArrayOutputStream(size);
    }

    public final byte[] getPacket() {
        return this.baos.toByteArray();
    }

    public final String toString() {
        return HexTool.toString(this.baos.toByteArray());
    }

    public final void writeZeroBytes(int i) {
        if (debuglog) {
            System.out.println("DEBUG[writeZeroBytes]: " + i);
        }
        for (int x = 0; x < i; x++) {
            this.baos.write(0);
        }
    }

    public final void write(byte[] b) {
        for (int x = 0; x < b.length; x++) {
            this.baos.write(b[x]);
        }
        if (debuglog) {
            System.out.println("DEBUG[write]: " + "/" + HexTool.toString(b));
        }
    }

    public final void write(byte b) {
        if (debuglog) {
            System.out.println("DEBUG[write]: " + b + "/" + HexTool.toString(b));
        }
        this.baos.write(b);
    }

    public final void write(int b) {
        this.baos.write((byte) b);
        if (debuglog) {
            System.out.println("DEBUG[write]: " + b + "/" + HexTool.toString(b));
        }
    }

    public final void writeShort(int i) {
        this.baos.write((byte) (i & 0xFF));
        this.baos.write((byte) (i >>> 8 & 0xFF));
        if (debuglog) {
            System.out.println("DEBUG[writeShort]: " + i + "/" + HexTool.toString((byte) (i & 0xFF)) + " " + HexTool.toString((byte) (i >>> 8 & 0xFF)));
        }
    }

    public final void writeInt(int i) {
        this.baos.write((byte) (i & 0xFF));
        this.baos.write((byte) (i >>> 8 & 0xFF));
        this.baos.write((byte) (i >>> 16 & 0xFF));
        this.baos.write((byte) (i >>> 24 & 0xFF));
        if (debuglog) {
            System.out.println("DEBUG[writeInt]: " + i + "/" + HexTool.toString((byte) (i & 0xFF)) + " " + HexTool.toString((byte) (i >>> 8 & 0xFF)) + " " + HexTool.toString((byte) (i >>> 16 & 0xFF)) + " " + HexTool.toString((byte) (i >>> 24 & 0xFF)));
        }
    }

    public final void writeAsciiString(String s) {
        write(s.getBytes(ASCII));
        if (debuglog) {
            System.out.println("DEBUG[writeAsciiString]: " + s + "/" + HexTool.toString(s.getBytes(ASCII)));
        }
    }

    public final void writeAsciiString(String s, int max) {
        if (s.length() > max) {
            s = s.substring(0, max);
        }
        write(s.getBytes(ASCII));
        if (debuglog) {
            System.out.println("DEBUG[writeAsciiString2]: " + s + "/" + s.getBytes(ASCII));
        }
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

    public final void writeRect(Rectangle s) {
        writeInt(s.x);
        writeInt(s.y);
        writeInt(s.x + s.width);
        writeInt(s.y + s.height);
    }

    public final void writeLong(long l) {
        this.baos.write((byte) (int) (l & 0xFF));
        this.baos.write((byte) (int) (l >>> 8 & 0xFF));
        this.baos.write((byte) (int) (l >>> 16 & 0xFF));
        this.baos.write((byte) (int) (l >>> 24 & 0xFF));
        this.baos.write((byte) (int) (l >>> 32 & 0xFF));
        this.baos.write((byte) (int) (l >>> 40 & 0xFF));
        this.baos.write((byte) (int) (l >>> 48 & 0xFF));
        this.baos.write((byte) (int) (l >>> 56 & 0xFF));
    }

    public final void writeReversedLong(long l) {
        this.baos.write((byte) (int) (l >>> 32 & 0xFF));
        this.baos.write((byte) (int) (l >>> 40 & 0xFF));
        this.baos.write((byte) (int) (l >>> 48 & 0xFF));
        this.baos.write((byte) (int) (l >>> 56 & 0xFF));
        this.baos.write((byte) (int) (l & 0xFF));
        this.baos.write((byte) (int) (l >>> 8 & 0xFF));
        this.baos.write((byte) (int) (l >>> 16 & 0xFF));
        this.baos.write((byte) (int) (l >>> 24 & 0xFF));
    }
}