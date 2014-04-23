package tools.data.input;

import java.io.IOException;

public abstract interface SeekableInputStreamBytestream extends ByteInputStream {

    public abstract void seek(long paramLong)
            throws IOException;

    public abstract long getPosition()
            throws IOException;

    public abstract String toString(boolean paramBoolean);
}