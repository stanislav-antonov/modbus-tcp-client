package pse.modbustcpclient.listener;

public interface DataReceiveListener {
    void onDataReceive(byte[] dataReceive);
}