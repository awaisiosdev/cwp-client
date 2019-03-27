package esde06.tol.oulu.fi.cwprotocol;

import java.io.IOException;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import android.util.Log;
import android.os.Handler;


public class CWProtocolImplementation implements CWPControl, CWPMessaging, Runnable {

    public enum CWPState {Disconnected, Connected, LineUp, LineDown};
    private volatile CWPState currentState = CWPState.Disconnected;
    private CWPState nextState = currentState;
    private int currentFrequency = CWPControl.DEFAULT_FREQUENCY;
    private CWPConnectionReader reader;
    private Handler receiveHandler = new Handler();
    private int messageValue;
    private static final String TAG = "CWProtcolImplemenation";
    private CWProtocolListener listener;

    public CWProtocolImplementation(CWProtocolListener listener){
          this.listener = listener;
    }

    public void addObserver(Observer observer) {
    }

    public void deleteObserver(Observer observer) {
    }

    public void lineUp() throws IOException {
        currentState = CWPState.LineUp;
        listener.onEvent(CWProtocolListener.CWPEvent.ELineUp, 0);
    }

    public void lineDown() throws IOException {
        currentState = CWPState.LineDown;
        listener.onEvent(CWProtocolListener.CWPEvent.ELineDown, 0);
    }

    public void connect(String serverAddr, int serverPort, int frequency) throws IOException {
        currentState = CWPState.Connected;
        reader = new CWPConnectionReader(this);
        reader.startReading();
    }

    public void disconnect() throws IOException {
        currentState = CWPState.Disconnected;
        if (reader != null) {
            try {
                reader.stopReading();
                reader.join();
            } catch (InterruptedException e) {

            }
            reader = null;
        }
    }

    public CWPState getCurrentState() {
        return currentState;
    }

    public boolean isConnected() {
        return currentState == CWPState.Connected;
    }

    public boolean lineIsUp() {
        return currentState == CWPState.LineUp;
    }

    public boolean serverSetLineUp() {
        return false;
    }

    public void setFrequency(int frequency) throws IOException {
        this.currentFrequency = frequency;
    }

    public int frequency() {
        return currentFrequency;
    }

    @Override
    public void run() {
        currentState = nextState;
        switch(nextState){
            case Connected:
                Log.d(TAG, "State changed to Connected");
                listener.onEvent(CWProtocolListener.CWPEvent.EConnected, 0);
                break;
            case Disconnected:
                Log.d(TAG, "State changed to Disconnected");
                listener.onEvent(CWProtocolListener.CWPEvent.EDisconnected, 0);
                break;
            case LineDown:
                Log.d(TAG, "State changed to Line Down");
                listener.onEvent(CWProtocolListener.CWPEvent.ELineDown, 0);
                break;
            case LineUp:
                Log.d(TAG, "State changed to Line UP");
                listener.onEvent(CWProtocolListener.CWPEvent.ELineUp, 0);
                break;

        }
    }

    private class CWPConnectionReader extends Thread {
        private volatile boolean running = false;
        private Runnable myProcessor;
        private static final String TAG = "CWPReader";

        // Used before networking for timing cw signals
        private Timer readerTimer;
        private TimerTask readerTask;


        CWPConnectionReader(Runnable processor) {
            myProcessor = processor;
        }

        void startReading() {
            running = true;
            start();
        }

        void stopReading() throws InterruptedException {
            readerTimer.cancel();
            running = false;
            readerTimer = null;
            readerTask = null;
        }

        private void doInitialize() throws InterruptedException {
           readerTimer = new Timer();
           readerTask = new TimerTask() {
               @Override
               public void run() {
                   Log.d(TAG, "Timer event Fire");
                   System.out.println("Task performed on " + new Date());
                   try {
                       if (currentState == CWPState.LineUp) {
                           changeProtocolState(CWPState.LineDown, 0);
                       } else if (currentState == CWPState.LineDown) {
                           changeProtocolState(CWPState.LineUp, 0);
                       }
                   } catch (InterruptedException e) {

                   }
               }
           };
           readerTimer.scheduleAtFixedRate(readerTask, 2000, 3000);
        }


        @Override
        public void run() {
            try {
                changeProtocolState(CWPState.Connected, 0);
                doInitialize();
                changeProtocolState(CWPState.LineDown, 0);
                while(running) {
                    
                }
            } catch (InterruptedException e) {

            }


        }

        private void changeProtocolState(CWPState state, int param) throws InterruptedException {
            Log.d(TAG, "Change protocol state to " + state);
            nextState = state;
            messageValue = param;
            receiveHandler.post(myProcessor);
        }
    }

}
