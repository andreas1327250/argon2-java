package at.gadermaier.argon2;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;


public class Util {

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static long littleEndianBytesToLong(byte[] b) {
        long result = 0;
        for (int i = 7; i >= 0; i--) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    public static byte[] intToLittleEndianBytes(int a) {
        byte[] result = new byte[4];
        result[0] = (byte) (a & 0xFF);
        result[1] = (byte) ((a >> 8) & 0xFF);
        result[2] = (byte) ((a >> 16) & 0xFF);
        result[3] = (byte) ((a >> 24) & 0xFF);
        return result;
    }

    public static byte[] longToLittleEndianBytes(long a) {
        byte[] result = new byte[8];
        result[0] = (byte) (a & 0xFF);
        result[1] = (byte) ((a >> 8) & 0xFF);
        result[2] = (byte) ((a >> 16) & 0xFF);
        result[3] = (byte) ((a >> 24) & 0xFF);
        result[4] = (byte) ((a >> 32) & 0xFF);
        result[5] = (byte) ((a >> 40) & 0xFF);
        result[6] = (byte) ((a >> 48) & 0xFF);
        result[7] = (byte) ((a >> 56) & 0xFF);
        return result;
    }

    public static long intToLong(int x){
        byte[] intBytes = intToLittleEndianBytes(x);
        byte[] bytes = new byte[8];
        System.arraycopy(intBytes, 0, bytes, 0, 4);
        return littleEndianBytesToLong(bytes);
    }

    static byte[] toByteArray(char[] chars, Charset charset) {
        assert chars != null;

        CharBuffer charBuffer = CharBuffer.wrap( chars);
        ByteBuffer byteBuffer = charset.encode( charBuffer);
        byte[] bytes = Arrays.copyOfRange( byteBuffer.array(),
                                           byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    static void clearArray(byte[] arr) {
        if (arr != null)
            Arrays.fill( arr, 0, arr.length - 1, (byte)0 );
    }
}
