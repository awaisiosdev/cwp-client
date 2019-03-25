package esde06.tol.oulu.fi.model;

import java.io.IOException;
import java.util.Observable;

import esde06.tol.oulu.fi.cwprotocol.CWPControl;
import esde06.tol.oulu.fi.cwprotocol.CWPMessaging;
import esde06.tol.oulu.fi.cwprotocol.CWProtocolImplementation;
import esde06.tol.oulu.fi.cwprotocol.CWProtocolListener;

public class CWPModel extends Observable implements CWPMessaging, CWPControl, CWProtocolListener  {
    CWProtocolImplementation protocol = new CWProtocolImplementation(this);

    // CWPMessaging Interface Implementation

    public void lineUp () throws IOException {
        protocol.lineUp();
    }

    public void lineDown () throws IOException {
        protocol.lineDown();
    }

    public boolean lineIsUp () {
        return protocol.lineIsUp();
    }

    public boolean serverSetLineUp(){
        return protocol.serverSetLineUp();
    }

    // CWPControl Interface Implementation

    public void connect(String serverAddr, int serverPort, int frequency) throws IOException {
        protocol.connect(serverAddr, serverPort, frequency);
    }

    public void disconnect() throws IOException {
        protocol.disconnect();
    }

    public boolean isConnected () {
         return protocol.isConnected();
    }

    public void setFrequency(int frequency) throws IOException {
        protocol.setFrequency(frequency);
    }

    @Override
    public int frequency() {
        return protocol.frequency();
    }

    public void onEvent(CWPEvent event, int param) {
        setChanged();
        notifyObservers(event);
    }



}
