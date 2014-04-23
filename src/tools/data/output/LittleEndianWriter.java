package tools.data.output;

import java.awt.Point;

public abstract interface LittleEndianWriter {

    public abstract void writeZeroBytes(int paramInt);

    public abstract void write(byte[] paramArrayOfByte);

    public abstract void write(byte paramByte);

    public abstract void write(int paramInt);

    public abstract void writeInt(int paramInt);

    public abstract void writeShort(short paramShort);

    public abstract void writeShort(int paramInt);

    public abstract void writeLong(long paramLong);

    public abstract void writeAsciiString(String paramString);

    public abstract void writeAsciiString(String paramString, int paramInt);

    public abstract void writePos(Point paramPoint);

    public abstract void writeMapleAsciiString(String paramString);
}