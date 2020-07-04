package pse.modbustcpclient;

import pse.modbustcpclient.exception.FunctionCodeNotSupportedException;
import pse.modbustcpclient.exception.InvalidQuantityException;
import pse.modbustcpclient.exception.InvalidStartingAddressException;
import pse.modbustcpclient.exception.ModbusException;
import pse.modbustcpclient.listener.DataReceiveListener;
import pse.modbustcpclient.listener.DataSendListener;
import pse.modbustcpclient.protocol.RequestFrame;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.ArrayList;

import static java.net.InetAddress.getByName;
import static pse.modbustcpclient.helper.Utils.toByteArray;

public class ModbusTcpClient {
    private int port = 502;
    private String ipAddress;
    private Socket socket = new Socket();
    private int connectTimeout = 2000;
    private boolean useUdp = false;

    private InputStream inputStream;
    private DataOutputStream outputStream;

    private byte[] dataSend;
    private byte[] dataReceive;

    private List<DataSendListener> dataSendListeners = new ArrayList<>();
    private List<DataReceiveListener> dataReceiveListeners = new ArrayList<>();

    public ModbusTcpClient() {}

    public ModbusTcpClient(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public void connect() throws IOException {
        var socket = new Socket(this.ipAddress, this.port);
        socket.setSoTimeout(this.connectTimeout);

        this.socket = socket;
        this.outputStream = new DataOutputStream(this.socket.getOutputStream());
        this.inputStream = this.socket.getInputStream();
    }

    /**
     * Connects to Modbus server
     * @param ipAddress The IP address of Modbus server
     * @param port The port of Modbus server (standard is 502)
     */
    public void connect(String ipAddress, int port) throws IOException {
        this.port = port;
        this.ipAddress = ipAddress;
        this.connect();
    }

    /**
     * Closes the connection to Modbus server
     * @throws IOException the exception
     */
    public void disconnect() throws IOException {
        if (this.inputStream != null) {
            this.inputStream.close();
        }

        if (this.outputStream != null) {
            this.outputStream.close();
        }

        if (this.socket != null) {
            this.socket.close();
        }

        this.socket = null;
    }

    /**
     * Returns true if connection is established, false otherwise
     * @return connection status
     */
    public boolean isConnected() {
        return this.socket != null && this.socket.isConnected();
    }

    /**
     * Reads a discrete inputs from Modbus server
     * @param startingAddress The first address to read
     * @param quantity The number of inputs to read
     * @return Discrete inputs from Modbus server as a boolean array
     */
    public boolean[] readDiscreteInputs(int startingAddress, int quantity) throws IOException, ModbusException, InvalidStartingAddressException, InvalidQuantityException, FunctionCodeNotSupportedException {
        if (startingAddress > 65535 || startingAddress < 0) {
            throw new IllegalArgumentException("Expected starting address range: 0 .. 65535");
        }

        if (quantity > 2000 || quantity < 0) {
            throw new IllegalArgumentException("Expected quantity range: 0 .. 2000");
        }

        var requestFrame = new RequestFrame(0x02, 0x06)
                .appendData((short)startingAddress)
                .appendData((short)quantity);

        var request = requestFrame.toRequest();
        var response = this.useUdp ? this.udp(request, request.length) : this.tcp(request, request.length);
        this.validateResponse(response, (byte)0x82);

        var responseHeaderLength = 9;
        var result = new boolean[quantity];
        for (var i = 0; i < quantity; i++) {
            var value = (int)response[responseHeaderLength + i / 8];
            var mask  = (int)Math.pow(2, i % 8);
            result[i] = ((value & mask) / mask) > 0;
        }

        return result;
    }

    /**
     * Read coils from Modbus server
     * @param startingAddress The first address to read
     * @param quantity The number of inputs to read
     * @return Coils from Modbus server
     */
    public boolean[] readCoils(int startingAddress, int quantity) throws IOException, ModbusException,
            InvalidStartingAddressException, InvalidQuantityException, FunctionCodeNotSupportedException {
        if (startingAddress > 65535 || startingAddress < 0) {
            throw new IllegalArgumentException("Expected starting address range: 0 .. 65535");
        }

        if (quantity > 2000 || quantity < 0) {
            throw new IllegalArgumentException("Expected quantity range: 0 .. 2000");
        }

        var requestFrame = new RequestFrame(0x01, 0x06)
                .appendData((short)startingAddress)
                .appendData((short)quantity);

        var request = requestFrame.toRequest();
        var response = this.useUdp ? this.udp(request, request.length) : this.tcp(request, request.length);
        this.validateResponse(response, (byte)0x81);

        var responseHeaderLength = 9;
        var result = new boolean[quantity];
        for (var i = 0; i < quantity; i++) {
            var value = (int)response[responseHeaderLength + i / 8];
            var mask  = (int)Math.pow(2, i % 8);
            result[i] = ((value & mask) / mask) > 0;
        }

        return result;
    }

    /**
     * Reads holding registers from Modbus server
     * @param startingAddress The fist address to read
     * @param quantity The number of inputs to read
     * @return Holding registers from Modbus server
     */
    public int[] readHoldingRegisters(int startingAddress, int quantity) throws IOException, ModbusException, InvalidStartingAddressException, InvalidQuantityException, FunctionCodeNotSupportedException {
        return this.readHoldingRegisters(startingAddress, quantity, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Reads holding registers from Modbus server
     * @param startingAddress The fist address to read
     * @param quantity The number of inputs to read
     * @param byteOrder The byte order of response
     * @return Holding registers from Modbus server
     */
    public int[] readHoldingRegisters(int startingAddress, int quantity, ByteOrder byteOrder) throws IOException, ModbusException, InvalidStartingAddressException, InvalidQuantityException, FunctionCodeNotSupportedException {
        if (startingAddress > 65535 || startingAddress < 0) {
            throw new IllegalArgumentException("Expected starting address range: 0 .. 65535");
        }

        if (quantity > 2000 || quantity < 0) {
            throw new IllegalArgumentException("Expected quantity range: 0 .. 2000");
        }

        var requestFrame = new RequestFrame(0x03, 0x06)
                .appendData((short)startingAddress)
                .appendData((short)quantity);

        var request = requestFrame.toRequest();
        var response = this.useUdp ? this.udp(request, request.length) : this.tcp(request, request.length);
        this.validateResponse(response, (byte)0x83);

        var responseHeaderLength = 9;
        var result = new int[quantity];
        for (var i = 0; i < quantity; i++) {
            result[i] = ByteBuffer.wrap(new byte[] {
                    response[responseHeaderLength + i * 2],
                    response[responseHeaderLength + i * 2 + 1],
            }).order(byteOrder).getShort();
        }

        return result;
    }

    /**
     * Reads input registers from Modbus server
     * @param startingAddress The first address to read
     * @param quantity The number of inputs to read
     * @return Input registers from Modbus server
     */
    public int[] readInputRegisters(int startingAddress, int quantity) throws IOException, ModbusException, InvalidStartingAddressException, InvalidQuantityException, FunctionCodeNotSupportedException {
        return this.readInputRegisters(startingAddress, quantity, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Reads input registers from Modbus server
     * @param startingAddress The first address to read
     * @param quantity The number of inputs to read
     * @param byteOrder The byte order of response
     * @return Input registers from Modbus server
     */
    public int[] readInputRegisters(int startingAddress, int quantity, ByteOrder byteOrder) throws IOException, ModbusException, InvalidStartingAddressException, InvalidQuantityException, FunctionCodeNotSupportedException {
        if (startingAddress > 65535 || startingAddress < 0) {
            throw new IllegalArgumentException("Expected starting address range: 0 .. 65535");
        }

        if (quantity > 2000 || quantity < 0) {
            throw new IllegalArgumentException("Expected quantity range: 0 .. 2000");
        }

        var requestFrame = new RequestFrame(0x04, 0x06)
                .appendData(startingAddress)
                .appendData(quantity);

        var request = requestFrame.toRequest();
        var response = this.useUdp ? this.udp(request, request.length) : this.tcp(request, request.length);
        this.validateResponse(response, (byte)0x84);

        var responseHeaderLength = 9;
        var result = new int[quantity];
        for (var i = 0; i < quantity; i++) {
            result[i] = ByteBuffer.wrap(new byte[] {
                    response[responseHeaderLength + i * 2],
                    response[responseHeaderLength + i * 2 + 1]
            }).order(byteOrder).getShort();
        }

        return result;
    }

    /**
     * Writes a coil to Modbus server
     * @param startingAddress The address to write (starting from 0)
     * @param value The value to write
     */
    public void writeCoil(int startingAddress, boolean value) throws IOException, ModbusException, InvalidStartingAddressException, InvalidQuantityException, FunctionCodeNotSupportedException {
        if (startingAddress > 65535 || startingAddress < 0) {
            throw new IllegalArgumentException("Expected starting address range: 0 .. 65535");
        }

        var coilValue = value ? 0xFF00 : 0x0000;
        var requestFrame = new RequestFrame(0x05, 0x06)
                .appendData((short)startingAddress)
                .appendData((short)coilValue);

        var request = requestFrame.toRequest();
        var response = this.useUdp ? this.udp(request, request.length) : this.tcp(request, request.length);
        this.validateResponse(response, (byte)0x85);
    }

    /**
     * Writes a coils to Modbus server
     * @param startingAddress The first address to write
     * @param values The values to write to Modbus server
     */
    public void writeCoils(int startingAddress, boolean[] values) throws IOException, ModbusException, InvalidStartingAddressException, InvalidQuantityException, FunctionCodeNotSupportedException {
        if (startingAddress > 65535 || startingAddress < 0) {
            throw new IllegalArgumentException("Expected starting address range: 0 .. 65535");
        }

        var coilsCount = values.length;
        var length = 7 + coilsCount / 8 + 1;
        var bytesCount = coilsCount / 8 + 1;
        if (coilsCount % 8 == 0) {
            bytesCount = bytesCount - 1;
        }

        var requestFrame = new RequestFrame(0x0F, length)
                .appendData((short)startingAddress)
                .appendData((short)coilsCount)
                .appendData((byte)bytesCount);

        var coilValue = (byte)0;
        // This is to always have a trailing zeroes at the end
        var coilsValuesLength = bytesCount + (coilsCount % 8 == 0 ? 1 : 0);
        var coilsValues = new byte[coilsValuesLength];
        for (var i = 0; i < coilsCount; i++) {
            if ((i % 8) == 0) {
                coilValue = 0;
            }

            var value = values[i] ? 1 : 0;
            coilValue = (byte)(value << (i % 8) | (int)coilValue);
            coilsValues[i / 8] = coilValue;
        }

        requestFrame.appendData(coilsValues);

        var request = requestFrame.toRequest();
        var response = this.useUdp ? this.udp(request, request.length) : this.tcp(request, request.length);
        this.validateResponse(response, (byte)0x8F);
    }

    /**
     * Writes a holding register to Modbus server
     * @param startingAddress The address to write
     * @param value The value to write
     */
    public void writeHoldingRegister(int startingAddress, int value) throws IOException, ModbusException, InvalidStartingAddressException, InvalidQuantityException, FunctionCodeNotSupportedException {
        if (startingAddress > 65535 || startingAddress < 0) {
            throw new IllegalArgumentException("Expected starting address range: 0 .. 65535");
        }

        var requestFrame = new RequestFrame(0x06, 0x06)
                .appendData((short)startingAddress)
                .appendData((short)value);

        var request = requestFrame.toRequest();
        var response = this.useUdp ? this.udp(request, request.length) : this.tcp(request, request.length);
        this.validateResponse(response, (byte)0x86);
    }

    /**
     * Writes a holding registers to Modbus server
     * @param startingAddress The first address to write
     * @param values The values to write
     */
    public void writeHoldingRegisters(int startingAddress, int[] values) throws IOException, ModbusException, InvalidStartingAddressException, InvalidQuantityException, FunctionCodeNotSupportedException {
        if (startingAddress > 65535 || startingAddress < 0) {
            throw new IllegalArgumentException("Expected starting address range: 0 .. 65535");
        }

        var registersCount = values.length;
        var bytesCount = registersCount * 2;
        var length = 7 + registersCount * 2;
        var requestFrame = new RequestFrame(0x10, length)
                .appendData((short)startingAddress)
                .appendData((short)registersCount)
                .appendData((byte)bytesCount);

        for (int value : values) {
            var registerBytes = toByteArray((short)value);
            requestFrame.appendData((byte)registerBytes[0]);
            requestFrame.appendData((byte)registerBytes[1]);
        }

        var request = requestFrame.toRequest();
        var response = this.useUdp ? this.udp(request, request.length) : this.tcp(request, request.length);
        this.validateResponse(response, (byte)0x90);
    }

    /**
     * Reads and writes a holding registers from/to Modbus server
     * @param startingAddressRead The first address to read
     * @param quantityRead The number of values to read
     * @param startingAddressWrite The first address to write
     * @param values The values to write
     * @return Register values from Modbus server
     */
    public int[] readWriteHoldingRegisters(int startingAddressRead, int quantityRead, int startingAddressWrite,
                                           int[] values) throws IOException, ModbusException, InvalidStartingAddressException, InvalidQuantityException, FunctionCodeNotSupportedException {
        return this.readWriteHoldingRegisters(startingAddressRead, quantityRead, startingAddressWrite, values, ByteOrder.BIG_ENDIAN);
    }

    /**
     * Reads and writes a holding registers from/to Modbus server
     * @param startingAddressRead The first address to read
     * @param quantityRead The number of values to read
     * @param startingAddressWrite The first address to write
     * @param values The values to write
     * @param byteOrder The byte order of response
     * @return Register values from Modbus server
     */
    public int[] readWriteHoldingRegisters(int startingAddressRead, int quantityRead, int startingAddressWrite,
                                           int[] values, ByteOrder byteOrder) throws IOException, ModbusException, InvalidStartingAddressException, InvalidQuantityException, FunctionCodeNotSupportedException {
        if (quantityRead > 125) {
            throw new IllegalArgumentException("Quantity expected to be in range: 0 ... 125");
        }

        if (values.length > 121) {
            throw new IllegalArgumentException("Values length expected to be in range: 0 ... 121");
        }

        var quantityWrite = values.length;
        var bytesCount = quantityWrite * 2;
        var length = 7 + quantityWrite * 2;
        var requestFrame = new RequestFrame(0x17, length)
                .appendData((short)startingAddressRead)
                .appendData((short)quantityRead)
                .appendData((short)startingAddressWrite)
                .appendData((short)quantityWrite)
                .appendData((byte)bytesCount);

        for (int value : values) {
            var registerBytes = toByteArray((short)value);
            requestFrame.appendData((byte)registerBytes[0]);
            requestFrame.appendData((byte)registerBytes[1]);
        }

        var request = requestFrame.toRequest();
        var response = this.useUdp ? this.udp(request, request.length) : this.tcp(request, request.length);
        this.validateResponse(response, (byte)0x97);

        var responseHeaderLength = 9;
        var result = new int[quantityRead];
        for (var i = 0; i < quantityRead; i++) {
            result[i] = ByteBuffer.wrap(new byte[] {
                    response[responseHeaderLength + i * 2],
                    response[responseHeaderLength + i * 2 + 1]
            }).order(byteOrder).getShort();
        }

        return result;
    }

    /**
     * Returns the IP address of Modbus server
     * @return IP address
     */
    public String getIpAddress() {
        return this.ipAddress;
    }

    /**
     * Specifies the IP address of Modbus server
     * @param ipAddress the IP address
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Returns the port of Modbus server
     * @return port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Specifies the port of Modbus server
     * @param port the port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns true if UDP is enabled instead of TCP for Modbus, false otherwise
     * @return useUdp
     */
    public boolean useUdp() {
        return this.useUdp;
    }

    /**
     * Enables to leverage UDP instead of TCP for Modbus
     * @param useUdp the boolean flag
     */
    public void useUdp(boolean useUdp) {
        this.useUdp = useUdp;
    }

    public int getConnectionTimeout() {
        return this.connectTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectTimeout = connectionTimeout;
    }

    public void addReceiveDataChangedListener(DataReceiveListener listener) {
        this.dataReceiveListeners.add(listener);
    }

    public void addSendDataChangedListener(DataSendListener listener) {
        this.dataSendListeners.add(listener);
    }

    public byte[] getDataSend() {
        return this.dataSend;
    }

    public byte[] getDataReceive() {
        return this.dataReceive;
    }

    private byte[] udp(byte[] dataSend, int length) throws IOException {
        var ipAddress = getByName(this.ipAddress);
        var packetSend = new DatagramPacket(dataSend, length, ipAddress, this.port);
        var socket = new DatagramSocket();
        socket.setSoTimeout(500);
        socket.send(packetSend);

        var dataReceive = new byte[2100];
        var packetReceive = new DatagramPacket(dataReceive, dataReceive.length);
        socket.receive(packetReceive);
        socket.close();

        return packetReceive.getData();
    }

    private byte[] tcp(byte[] dataSend, int length) throws IOException {
        this.outputStream.write(dataSend, 0, length);

        if (!this.dataSendListeners.isEmpty()) {
            this.dataSend = new byte[length];
            System.arraycopy(dataSend, 0, this.dataSend, 0, length);
            this.dataSendListeners.forEach(listener -> listener.onDataSend(dataSend));
        }

        var dataReceive = new byte[2100];
        var bytesCount = this.inputStream.read(dataReceive, 0, dataReceive.length);

        if (!this.dataReceiveListeners.isEmpty()) {
            this.dataReceive = new byte[bytesCount];
            System.arraycopy(dataReceive, 0, this.dataReceive, 0, bytesCount);
            this.dataReceiveListeners.forEach(listener -> listener.onDataReceive(dataReceive));
        }

        return dataReceive;
    }

    private void validateResponse(byte[] data, byte expected) throws ModbusException, InvalidQuantityException,
            InvalidStartingAddressException, FunctionCodeNotSupportedException {
        if ((data[7] & 0xff) == expected && data[8] == 0x01) {
            throw new FunctionCodeNotSupportedException("Function code is not supported");
        }

        if ((data[7] & 0xff) == expected && data[8] == 0x02) {
            throw new InvalidStartingAddressException("Invalid starting address");
        }

        if ((data[7] & 0xff) == expected && data[8] == 0x03) {
            throw new InvalidQuantityException("Invalid quantity");
        }

        if ((data[7] & 0xff) == expected && data[8] == 0x04) {
            throw new ModbusException("Reading error");
        }
    }
}
