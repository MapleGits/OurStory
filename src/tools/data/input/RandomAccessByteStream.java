package tools.data.input;

import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

public class RandomAccessByteStream
        implements SeekableInputStreamBytestream {

    private final RandomAccessFile raf;
    private long read = 0L;

    public RandomAccessByteStream(RandomAccessFile raf) {
        this.raf = raf;
    }

    public final int readByte() {
        try {
            int temp = this.raf.read();
            if (temp == -1) {
                throw new RuntimeException("EOF");
            }
            this.read += 1L;
            return temp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final void seek(long offset)
            throws IOException {
        this.raf.seek(offset);
    }

    public final long getPosition()
            throws IOException {
        return this.raf.getFilePointer();
    }

    public final long getBytesRead() {
        return this.read;
    }

    public final long available() {
        try {
            return this.raf.length() - this.raf.getFilePointer();
        } catch (IOException e) {
            System.err.println("ERROR" + e);
        }
        return 0L;
    }

    public final String toString(boolean b) {
        return toString();
    }
}