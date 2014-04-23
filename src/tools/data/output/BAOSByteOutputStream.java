package tools.data.output;

import java.io.ByteArrayOutputStream;

public class BAOSByteOutputStream
        implements ByteOutputStream {

    private ByteArrayOutputStream baos;

    public BAOSByteOutputStream(ByteArrayOutputStream baos) {
        this.baos = baos;
    }

    public void writeByte(byte b) {
        this.baos.write(b);
    }
}