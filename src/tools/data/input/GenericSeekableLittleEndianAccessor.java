package tools.data.input;

import java.io.IOException;
import java.io.PrintStream;

public class GenericSeekableLittleEndianAccessor extends GenericLittleEndianAccessor
        implements SeekableLittleEndianAccessor {

    private final SeekableInputStreamBytestream bs;

    public GenericSeekableLittleEndianAccessor(SeekableInputStreamBytestream bs) {
        super(bs);
        this.bs = bs;
    }

    public final void seek(long offset) {
        try {
            this.bs.seek(offset);
        } catch (IOException e) {
            System.err.println("Seek failed" + e);
        }
    }

    public final long getPosition() {
        try {
            return this.bs.getPosition();
        } catch (IOException e) {
            System.err.println("getPosition failed" + e);
        }
        return -1L;
    }

    public final void skip(int num) {
        seek(getPosition() + num);
    }
}