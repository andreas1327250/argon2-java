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

	public static long readLong(byte[] buffer, int offset) {
		long result = 0;
        for (int i = offset + 7; i >= offset; i--) {
            result <<= 8;
            result |= (buffer[i] & 0xFF);
        }
        return result;
	}

	public static int writeInt(byte[] result, int offset, int value) {
		result[offset++] = byte0(value);
        result[offset++] = byte1(value);
        result[offset++] = byte2(value);
        result[offset++] = byte3(value);
        return offset;
	}

	public static byte byte0(int a) {
		return (byte) (a & 0xFF);
	}

	public static byte byte1(int a) {
		return (byte) ((a >> 8) & 0xFF);
	}

	public static byte byte2(int a) {
		return (byte) ((a >> 16) & 0xFF);
	}

	public static byte byte3(int a) {
		return (byte) ((a >> 24) & 0xFF);
	}

    public static byte[] longToLittleEndianBytes(long value) {
        byte[] result = new byte[8];
        writeLong(result, 0, value);
        return result;
    }

	public static int writeLong(byte[] result, int offset, long value) {
		result[offset++] = byte0(value);
        result[offset++] = byte1(value);
        result[offset++] = byte2(value);
        result[offset++] = byte3(value);
        result[offset++] = byte4(value);
        result[offset++] = byte5(value);
        result[offset++] = byte6(value);
        result[offset++] = byte7(value);
        return offset;
	}

	private static byte byte0(long value) {
		return (byte) (value & 0xFF);
	}

	private static byte byte1(long value) {
		return (byte) ((value >> 8) & 0xFF);
	}

	private static byte byte2(long value) {
		return (byte) ((value >> 16) & 0xFF);
	}

	private static byte byte3(long value) {
		return (byte) ((value >> 24) & 0xFF);
	}

	private static byte byte4(long value) {
		return (byte) ((value >> 32) & 0xFF);
	}

	private static byte byte5(long value) {
		return (byte) ((value >> 40) & 0xFF);
	}

	private static byte byte6(long value) {
		return (byte) ((value >> 48) & 0xFF);
	}

	private static byte byte7(long value) {
		return (byte) ((value >> 56) & 0xFF);
	}

    public static long intToLong(int x){
    	return x & 0xFFFFFFFFL;
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
