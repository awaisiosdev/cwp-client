package esde06.tol.oulu.fi.model;

import android.util.Log;

import java.io.IOException;
import java.util.Observable;

import esde06.tol.oulu.fi.cwprotocol.CWPControl;
import esde06.tol.oulu.fi.cwprotocol.CWPMessaging;
import esde06.tol.oulu.fi.cwprotocol.CWProtocolImplementation;
import esde06.tol.oulu.fi.cwprotocol.CWProtocolListener;

public class CWPModel extends Observable implements CWPMessaging, CWPControl, CWProtocolListener  {

    private final static String TAG = "CWPModel";
    private Signaller audioFeedback;
    CWProtocolImplementation protocol = new CWProtocolImplementation(this);
    // CWPMessaging Interface Implementation

    public CWPModel(){
        audioFeedback = new Signaller();
        this.addObserver(audioFeedback);
    }

    public void lineUp () throws IOException {
        Log.d(TAG, "Pass line Up request");
        protocol.lineUp();
    }

    public void lineDown () throws IOException {
        Log.d(TAG, "Pass line Down request");
        protocol.lineDown();
    }

    public boolean lineIsUp () {
        return protocol.lineIsUp();
    }

    public boolean serverSetLineUp(){
        return protocol.serverSetLineUp();
    }

    // CWPControl Interface Implementation

    public void connect(String serverAddr, int serverPort, int frequency) {
        Log.d(TAG, "Pass connect to server request.");
        protocol.connect(serverAddr, serverPort, frequency);
    }

    public void disconnect() throws IOException {
        Log.d(TAG, "Pass disconnect to server request.");
        protocol.disconnect();
    }

    public boolean isConnected () {
         return protocol.isConnected();
    }

    public void setFrequency(int frequency) {
        Log.d(TAG, "Set Frequency to : " + frequency);
        protocol.setFrequency(frequency);
    }

    @Override
    public int frequency() {
        return protocol.frequency();
    }

    public void onEvent(CWPEvent event, int param) {
        Log.d(TAG, "Event received from protocol implementation. Notifying observers.");
        setChanged();
        notifyObservers(new CWPMessage(event, param));
    }



}
