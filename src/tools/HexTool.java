package tools;

import java.io.ByteArrayOutputStream;
import org.apache.mina.core.buffer.IoBuffer;

public class HexTool {

    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static final String toString(byte byteValue) {
        int tmp = byteValue << 8;
        char[] retstr = {HEX[(tmp >> 12 & 0xF)], HEX[(tmp >> 8 & 0xF)]};
        return String.valueOf(retstr);
    }

    public static final String toString(IoBuffer buf) {
        buf.flip();
        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);
        String ret = toString(arr);
        buf.flip();
        buf.put(arr);
        return ret;
    }

    public static final String toString(int intValue) {
        return Integer.toHexString(intValue);
    }

    public static final String toString(byte[] bytes) {
        StringBuilder hexed = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            hexed.append(toString(bytes[i]));
            hexed.append(' ');
        }
        return hexed.substring(0, hexed.length() - 1);
    }

    public static final String toStringFromAscii(byte[] bytes) {
        char[] ret = new char[bytes.length];
        for (int x = 0; x < bytes.length; x++) {
            if ((bytes[x] < 32) && (bytes[x] >= 0)) {
                ret[x] = '.';
            } else {
                int chr = (short) bytes[x] & 0xFF;
                ret[x] = ((char) chr);
            }
        }
        return String.valueOf(ret);
    }

    public static final String toPaddedStringFromAscii(byte[] bytes) {
        String str = toStringFromAscii(bytes);
        StringBuilder ret = new StringBuilder(str.length() * 3);
        for (int i = 0; i < str.length(); i++) {
            ret.append(str.charAt(i));
            ret.append("  ");
        }
        return ret.toString();
    }

    public static byte[] getByteArrayFromHexString(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nexti = 0;
        int nextb = 0;
        boolean highoc = true;
        outer:
        while (true) {
            int number = -1;
            while (number == -1) {
                if (nexti == hex.length()) {
                    break outer;
                }
                char chr = hex.charAt(nexti);
                if ((chr >= '0') && (chr <= '9')) {
                    number = chr - '0';
                } else if ((chr >= 'a') && (chr <= 'f')) {
                    number = chr - 'a' + 10;
                } else if ((chr >= 'A') && (chr <= 'F')) {
                    number = chr - 'A' + 10;
                } else {
                    number = -1;
                }
                nexti++;
            }
            if (highoc) {
                nextb = number << 4;
                highoc = false;
            } else {
                nextb |= number;
                highoc = true;
                baos.write(nextb);
            }
        }
        label161:
        return baos.toByteArray();
    }

    public static byte[] hex(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nexti = 0;
        int nextb = 0;
        boolean highoc = true;
        outer:
        while (true) {
            int number = -1;
            while (number == -1) {
                if (nexti == hex.length()) {
                    break outer;
                }
                char chr = hex.charAt(nexti);
                if ((chr >= '0') && (chr <= '9')) {
                    number = chr - '0';
                } else if ((chr >= 'a') && (chr <= 'f')) {
                    number = chr - 'a' + 10;
                } else if ((chr >= 'A') && (chr <= 'F')) {
                    number = chr - 'A' + 10;
                } else {
                    number = -1;
                }
                nexti++;
            }
            if (highoc) {
                nextb = number << 4;
                highoc = false;
            } else {
                nextb |= number;
                highoc = true;
                baos.write(nextb);
            }
        }
        label161:
        return baos.toByteArray();
    }

    public static final String getOpcodeToString(int op) {
        return new StringBuilder().append("0x").append(StringUtil.getLeftPaddedStr(Integer.toHexString(op).toUpperCase(), '0', 4)).toString();
    }
}