package pse.modbustcpclient;

import java.nio.ByteBuffer;

import static pse.modbustcpclient.helper.Utils.*;

public abstract class RegisterConverter {
    /**
     * Converts a two 16 bit registers to 32 bit float value
     * @param registers 16 bit registers array
     * @return 32 bit float value
     */
    public static float registersToFloat(int[] registers) throws IllegalArgumentException {
        if (registers.length != 2) {
            throw new IllegalArgumentException("Invalid registers array length: " + registers.length);
        }

        byte[] lRegisterBytes = toByteArray((short)registers[0]);
        byte[] hRegisterBytes = toByteArray((short)registers[1]);
        byte[] floatBytes = {
                hRegisterBytes[0], hRegisterBytes[1],
                lRegisterBytes[0], lRegisterBytes[1]
        };

        return ByteBuffer.wrap(floatBytes).getFloat();
    }

    /**
     * Converts a two 16 bits registers to 64 bit double value
     * @param registers 16 bit registers array
     * @return 64 bit double value
     */
    public static double registersToDouble(int[] registers) throws IllegalArgumentException {
        if (registers.length != 4) {
            throw new IllegalArgumentException("Invalid registers array length: " + registers.length);
        }

        byte[] lRegisterBytes = toByteArray((short)registers[0]);
        byte[] lhRegisterBytes = toByteArray((short)registers[1]);
        byte[] hlRegisterBytes = toByteArray((short)registers[2]);
        byte[] hRegisterBytes = toByteArray((short)registers[3]);
        byte[] doubleBytes = {
            hRegisterBytes[1], hRegisterBytes[0],
            hlRegisterBytes[1], hlRegisterBytes[0],
            lhRegisterBytes[1], lhRegisterBytes[0],
            lRegisterBytes[1], lRegisterBytes[0]
        };

        return ByteBuffer.wrap(doubleBytes).getDouble();
    }

    /**
     * Converts a four 16 bit registers to 64 bit double value
     * @param registers 16 bit registers array
     * @param registerOrder high or low register first
     * @return 64 bit double value
     */
    public static double registersToDouble(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException {
        if (registers.length != 4) {
            throw new IllegalArgumentException("Invalid registers array length: " + registers.length);
        }

        int[] resultRegisters = registerOrder == RegisterOrder.HighLow ?
                new int[] { registers[3], registers[2], registers[1], registers[0] } :
                new int[] { registers[0], registers[1], registers[2], registers[3] };

        return registersToDouble(resultRegisters);
    }

    /**
     * Converts a two 16 bit registers to 32 bit float value
     * @param registers 16 bit registers array
     * @param registerOrder High or low register first
     * @return 32 bit float value
     */
    public static float registersToFloat(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException {
        int[] resultRegisters = (registerOrder == RegisterOrder.HighLow) ?
                new int[] { registers[1], registers[0] } :
                new int[] { registers[0], registers[1] };

        return registersToFloat(resultRegisters);
    }

    /**
     * Converts a four 16 bit registers to 64 bit long value
     * @param registers 16 bit registers array
     * @return 64 bit long value
     */
    public static long registersToLong(int[] registers) throws IllegalArgumentException {
        if (registers.length != 4) {
            throw new IllegalArgumentException("Invalid registers array length: " + registers.length);
        }

        byte[] lRegisterBytes = toByteArray((short)registers[0]);
        byte[] lhRegisterBytes = toByteArray((short)registers[1]);
        byte[] hlRegisterBytes = toByteArray((short)registers[2]);
        byte[] hRegisterBytes = toByteArray((short)registers[3]);
        byte[] longBytes = {
                hRegisterBytes[1], hRegisterBytes[0],
                hlRegisterBytes[1], hlRegisterBytes[0],
                lhRegisterBytes[1], lhRegisterBytes[0],
                lRegisterBytes[1], lRegisterBytes[0]
        };

        return ByteBuffer.wrap(longBytes).getLong();
    }

    /**
     * Converts a four 16 bit registers to 64 bit long value
     * @param registers 16 bit registers array
     * @return 64 bit long value
     */
    public static long registersToLong(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException {
        if (registers.length != 4) {
            throw new IllegalArgumentException("Invalid registers array length: " + registers.length);
        }

        int[] resultRegisters = (registerOrder == RegisterOrder.HighLow) ?
                new int[] { registers[3], registers[2], registers[1], registers[0] } :
                new int[] { registers[0], registers[1], registers[2], registers[3] };

        return registersToLong(resultRegisters);
    }

    /**
     * Converts a two 16 bit registers to 32 bit integer value
     * @param registers 16 bit registers array
     * @return 32 bit integer value
     */
    public static int registersToInt(int[] registers) throws IllegalArgumentException {
        if (registers.length != 2) {
            throw new IllegalArgumentException("Invalid registers array length: " + registers.length);
        }

        byte[] lRegisterBytes = toByteArray((short)registers[0]);
        byte[] hRegisterBytes = toByteArray((short)registers[1]);
        byte[] doubleBytes = {
                hRegisterBytes[1], hRegisterBytes[0],
                lRegisterBytes[1], lRegisterBytes[0]
        };

        return ByteBuffer.wrap(doubleBytes).getInt();
    }

    /**
     * Converts a two 16 bit registers to 32 bit integer value
     * @param registers 16 bit registers array
     * @param registerOrder High or low register first
     * @return 32 bit integer value
     */
    public static int registersToInt(int[] registers, RegisterOrder registerOrder) throws IllegalArgumentException {
        int[] resultRegisters = (registerOrder == RegisterOrder.HighLow) ?
                new int[] { registers[1], registers[0] } :
                new int[] { registers[0], registers[1] };

        return registersToInt(resultRegisters);
    }

    /**
     * Converts a 32 bit float value to two 16 bit values to be sent as Modbus registers
     * @param value The value to be converted
     * @return 16 bit registers array
     */
    public static int[] floatToRegisters(float value) {
        var floatBytes = toByteArray(value);
        byte[] hRegisterBytes = { floatBytes[0], floatBytes[1] };
        byte[] lRegisterBytes = { floatBytes[2], floatBytes[3] };

        return new int[] {
                ByteBuffer.wrap(lRegisterBytes).getShort(),
                ByteBuffer.wrap(hRegisterBytes).getShort()
        };
    }

    /**
     * Converts a 32 bit float value to two 16 bit values to be sent as Modbus registers
     * @param value The value to be converted
     * @param registerOrder High or low register first
     * @return 16 bit registers array
     */
    public static int[] floatToRegisters(float value, RegisterOrder registerOrder) {
        int[] registers = floatToRegisters(value);

        return (registerOrder == RegisterOrder.HighLow) ?
                new int[] { registers[1], registers[0] }
                : registers;
    }

    /**
     * Converts a 32 bit integer value to two 16 bit values to be sent as Modbus registers
     * @param value The value to be converted
     * @return 16 bit registers array
     */
    public static int[] intToRegisters(int value) {
        byte[] intBytes = toByteArray(value);
        byte[] hRegisterBytes = { intBytes[0], intBytes[1] };
        byte[] lRegisterBytes = { intBytes[2], intBytes[3] };

        return new int[] {
                ByteBuffer.wrap(lRegisterBytes).getShort(),
                ByteBuffer.wrap(hRegisterBytes).getShort()
        };
    }

    /**
     * Converts a 32 bit integer value to two 16 bit values to be sent as Modbus registers
     * @param value The value to be converted
     * @param registerOrder High or low register first
     * @return 16 bit registers array
     */
    public static int[] intToRegisters(int value, RegisterOrder registerOrder) {
        int[] registers = intToRegisters(value);

        return (registerOrder == RegisterOrder.HighLow) ?
                new int[] { registers[1], registers[0] }
                : registers;
    }

    /**
     * Converts a 64 bit long value to four 16 bit values to be sent as Modbus registers
     * @param value The value to be converted
     * @return 16 bit registers array
     */
    public static int[] longToRegisters(long value) {
        byte[] longBytes = toByteArray(value);
        byte[] hhRegisterBytes = { longBytes[0], longBytes[1] };
        byte[] hlRegisterBytes = { longBytes[2], longBytes[3] };
        byte[] lhRegisterBytes = { longBytes[4], longBytes[5] };
        byte[] llRegisterBytes = { longBytes[6], longBytes[7] };

        return new int[] {
                ByteBuffer.wrap(llRegisterBytes).getShort(),
                ByteBuffer.wrap(lhRegisterBytes).getShort(),
                ByteBuffer.wrap(hlRegisterBytes).getShort(),
                ByteBuffer.wrap(hhRegisterBytes).getShort()
        };
    }

    /**
     * Converts a 64 bit value to two 16 bit values to be sent as Modbus registers
     * @param value The value to be converted
     * @param registerOrder High or low register first
     * @return 16 bit registers array
     */
    public static int[] longToRegisters(int value, RegisterOrder registerOrder) {
        int[] registers = longToRegisters(value);

        return (registerOrder == RegisterOrder.HighLow) ?
                new int[] { registers[3], registers[2], registers[1], registers[0] }
                : registers;
    }

    /**
     * Converts a 64 bit double value to four 16 bit values to be sent as Modbus registers
     * @param value The value to be converted
     * @return 16 bit registers array
     */
    public static int[] doubleToRegisters(double value) {
        byte[] doubleBytes = toByteArray(value);
        byte[] hhRegisterBytes = { doubleBytes[0], doubleBytes[1] };
        byte[] hlRegisterBytes = { doubleBytes[2], doubleBytes[3] };
        byte[] lhRegisterBytes = { doubleBytes[4], doubleBytes[5] };
        byte[] llRegisterBytes = { doubleBytes[6], doubleBytes[7] };

        return new int[] {
                ByteBuffer.wrap(llRegisterBytes).getShort(),
                ByteBuffer.wrap(lhRegisterBytes).getShort(),
                ByteBuffer.wrap(hlRegisterBytes).getShort(),
                ByteBuffer.wrap(hhRegisterBytes).getShort()
        };
    }

    /**
     * Converts a 64 bit double value to two 16 bit values to send as Modbus registers
     * @param value The value to be converted
     * @param registerOrder High or low register first
     * @return 16 bit register array
     */
    public static int[] doubleToRegisters(double value, RegisterOrder registerOrder) {
        int[] registers = doubleToRegisters(value);

        return (registerOrder == RegisterOrder.HighLow) ?
                new int[] { registers[3], registers[2], registers[1], registers[0] }
                : registers;
    }

    /**
     * Converts a 16 bit register values to string
     * @param registers The registers array received via Modbus
     * @param offset The first register containing the String to convert
     * @param stringLength The number of characters in the String (must be even)
     * @return The converted string
     */
    public static String registersToString(int[] registers, int offset, int stringLength) {
        byte[] stringBytes = new byte[stringLength];

        for (int i = 0; i < stringLength / 2; i++) {
            byte[] registerBytes = toByteArray(registers[offset + i]);
            stringBytes[i * 2] = registerBytes[0];
            stringBytes[i * 2 + 1] = registerBytes[1];
        }

        return new String(stringBytes);
    }

    /**
     * Converts a given string to 16 bit registers array
     * @param value The string
     * @return The array of 16 bit registers
     */
    public static int[] stringToRegisters(String value) {
        byte[] stringBytes = value.getBytes();
        int[] registers = new int[value.length() / 2 + value.length() % 2];

        for (int i = 0; i < registers.length; i++) {
            registers[i] = stringBytes[i * 2];
            if (i * 2 + 1 < stringBytes.length) {
                registers[i] = registers[i] | ((int)stringBytes[i * 2 + 1] << 8);
            }
        }

        return registers;
    }
}
