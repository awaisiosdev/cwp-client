package esde06.tol.oulu.fi.model;

import android.util.Log;
import java.io.IOException;
import java.util.Observable;

import esde06.tol.oulu.fi.cwprotocol.CWPControl;
import esde06.tol.oulu.fi.cwprotocol.CWPMessaging;
import esde06.tol.oulu.fi.cwprotocol.CWProtocolImplementation;
import esde06.tol.oulu.fi.cwprotocol.CWProtocolListener;

public class CWPModel extends Observable implements CWPMessaging, CWPControl, CWProtocolListener, CWPAudio {

    private final static String TAG = "CWPModel";
    private Signaller audioFeedback;
    private CWProtocolImplementation protocol = new CWProtocolImplementation(this);

    // CWPMessaging Interface Implementation
    public void lineUp() {
        Log.d(TAG, "Pass line Up request");
        protocol.lineUp();
    }

    public void lineDown() {
        Log.d(TAG, "Pass line Down request");
        protocol.lineDown();
    }

    public boolean lineIsUp() {
        return protocol.lineIsUp();
    }

    public boolean serverSetLineUp() {
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

    public boolean isConnected() {
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

    public void turnOnAudioFeedback(int alertVolume) {
        Log.d(TAG, "Audio Feedback turned On! - Volume : " + alertVolume);
        audioFeedback = new Signaller(alertVolume);
        this.addObserver(audioFeedback);
    }

    public void turnOffAudioFeedback() {
        Log.d(TAG, "Audio Feedback turned off!");
        if (audioFeedback != null) {
            audioFeedback.forceStop();
            this.deleteObserver(audioFeedback);
            audioFeedback = null;
        }
    }


}
