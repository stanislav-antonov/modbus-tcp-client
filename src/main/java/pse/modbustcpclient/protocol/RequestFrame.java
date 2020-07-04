package pse.modbustcpclient.protocol;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;

import static pse.modbustcpclient.helper.Utils.toByteArray;

public class RequestFrame {
    private byte[] length;
    private byte functionCode;
    private final byte unitIdentifier = 0x01;
    private final byte[] protocolIdentifier = toByteArray((short)0x00);
    private final byte[] transactionIdentifier = toByteArray((short)0x01);

    private List<Byte> data = new ArrayList<>();

    public RequestFrame(int functionCode, int length) {
        this.functionCode = (byte)functionCode;
        this.length = toByteArray((short)length);
    }

    public RequestFrame appendData(int value) {
        var bytes = toByteArray((short)value);
        this.data.add(bytes[0]);
        this.data.add(bytes[1]);
        return this;
    }

    public RequestFrame appendData(byte value) {
        this.data.add(value);
        return this;
    }

    public RequestFrame appendData(byte[] value) {
        var values = new ArrayList<Byte>();
        IntStream.range(0, value.length).forEach(i -> values.add(value[i]));
        this.data.addAll(values);
        return this;
    }

    public byte[] toRequest() {
        var header = new ArrayList<Byte>();
        header.add(this.transactionIdentifier[0]);
        header.add(this.transactionIdentifier[1]);
        header.add(this.protocolIdentifier[0]);
        header.add(this.protocolIdentifier[1]);
        header.add(this.length[0]);
        header.add(this.length[1]);
        header.add(this.unitIdentifier);
        header.add(this.functionCode);

        header.addAll(data);

        var result = new byte[header.size()];
        IntStream.range(0, header.size()).forEach(i -> result[i] = header.get(i));

        return result;
    }
}