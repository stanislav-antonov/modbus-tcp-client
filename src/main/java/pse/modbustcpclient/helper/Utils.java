package pse.modbustcpclient.helper;

import java.nio.ByteBuffer;

public abstract class Utils {
    public static byte[] toByteArray(short value) {
        return ByteBuffer.allocate(2).putShort(value).array();
    }

    public static byte[] toByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static byte[] toByteArray(float value) {
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static byte[] toByteArray(double value) {
        return ByteBuffer.allocate(8).putDouble(value).array();
    }

    public static byte[] toByteArray(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }
}
