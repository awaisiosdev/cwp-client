package esde06.tol.oulu.fi.model;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

import java.util.Observer;
import java.util.Observable;
import esde06.tol.oulu.fi.cwprotocol.CWProtocolListener.CWPEvent;

public class Signaller implements Observer {

    private final static String TAG = "Signaller";
    private ToneGenerator generator;
    private Boolean isMuted = false;
    private int alertVolume = ToneGenerator.MAX_VOLUME;

    public Signaller () {
        generator = new ToneGenerator(AudioManager.STREAM_DTMF, alertVolume);
    }

    private void start(){
        if (isMuted){
            return;
        }
        generator.startTone(ToneGenerator.TONE_DTMF_5);
    }

    private void stop() {
        generator.stopTone();
    }

    @Override
    public void update(Observable o, Object arg) {
       CWPMessage msg = (CWPMessage) arg;
        Log.d(TAG, "Received protocol event : " + msg.event.name());
        if (msg.event == CWPEvent.ELineUp){
            start();
        } else if(msg.event == CWPEvent.ELineDown || msg.event == CWPEvent.EDisconnected) {
            stop();
        }
    }
}

