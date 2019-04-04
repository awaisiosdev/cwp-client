package esde06.tol.oulu.fi.cwprotocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.net.Socket;
import java.util.Date;
import android.util.Log;
import android.os.Handler;


public class CWProtocolImplementation implements CWPControl, CWPMessaging, Runnable {

    private static final String TAG = "ProtocolImplementation";

    public enum CWPState {Disconnected, Connected, LineUp, LineDown};
    private volatile CWPState currentState = CWPState.Disconnected;
    private CWPState nextState = currentState;
    private Boolean lineUpByUser = false;
    private Boolean lineUpByServer = false;

    private CWPConnectionReader reader;
    private static final int BUFFER_LENGTH = 64;
    private OutputStream nos = null; //Network Output Stream
    private ByteBuffer outBuffer = null;
    private String serverAddr = null;
    private int serverPort = -1;
    private int messageValue;


    private int currentFrequency = CWPControl.DEFAULT_FREQUENCY;

    private Handler receiveHandler = new Handler();
    private CWProtocolListener listener;


    public CWProtocolImplementation(CWProtocolListener listener){
          this.listener = listener;
    }

    public void addObserver(Observer observer) {
    }

    public void deleteObserver(Observer observer) {
    }

    public void lineUp() throws IOException {
        Log.d(TAG, "Line Up signal generated by user.");
        currentState = CWPState.LineUp;
        lineUpByUser = true;
        if (lineUpByServer){
           return;
        }
        Log.d(TAG, "Sending line Up state change event.");
        listener.onEvent(CWProtocolListener.CWPEvent.ELineUp, 0);
    }

    public void lineDown() throws IOException {
        Log.d(TAG, "Line Down signal generated by user.");
        currentState = CWPState.LineDown;
        lineUpByUser = false;
        if (lineUpByServer){
            return;
        }
        Log.d(TAG, "Sending line Down state change event.");
        listener.onEvent(CWProtocolListener.CWPEvent.ELineDown, 0);
    }

    public void connect(String serverAddr, int serverPort, int frequency) throws IOException {
        Log.d(TAG, "Connect to CWP Server.");
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.currentFrequency = frequency;
        reader = new CWPConnectionReader(this);
        reader.startReading();
        Log.d(TAG, "Started Reading incoming messages.");
        currentState = CWPState.Connected;
    }

    public void disconnect() throws IOException {
        Log.d(TAG, "Disconnect CWP Server.");
        if (reader != null) {
            try {
                reader.stopReading();
                reader.join();
            } catch (InterruptedException e) {

            } catch (IOException e) {

            }
            reader = null;
        }
        currentState = CWPState.Disconnected;
    }

    public CWPState getCurrentState() {
        return currentState;
    }

    public boolean isConnected() {
        return currentState != CWPState.Disconnected;
    }

    public boolean lineIsUp() {
        return currentState == CWPState.LineUp;
    }

    public boolean serverSetLineUp() {
        return lineUpByServer;
    }

    public void setFrequency(int frequency) throws IOException {
        Log.d(TAG, "Set frequency to " + frequency);
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
                listener.onEvent(CWProtocolListener.CWPEvent.EConnected, 0);
                Log.d(TAG, "Sending Connected state change event.");
                break;
            case Disconnected:
                listener.onEvent(CWProtocolListener.CWPEvent.EDisconnected, 0);
                Log.d(TAG, "Sending Disconnected state change event.");
                break;
            case LineDown:
                lineUpByServer = false;
                if (lineUpByUser) {
                    listener.onEvent(CWProtocolListener.CWPEvent.EServerStateChange, 0);
                    Log.d(TAG, "Sending server state change event");
                } else {
                    listener.onEvent(CWProtocolListener.CWPEvent.ELineDown, 0);
                    Log.d(TAG, "Sending Line Down state change event.");
                }
                break;
            case LineUp:
                lineUpByServer = true;
                if (lineUpByUser) {
                    listener.onEvent(CWProtocolListener.CWPEvent.EServerStateChange, 0);
                    Log.d(TAG, "Sending server state change event");
                } else {
                    listener.onEvent(CWProtocolListener.CWPEvent.ELineUp, 0);
                    Log.d(TAG, "Sending Line Up state change event.");
                }
                break;

        }
    }

    private class CWPConnectionReader extends Thread {

        private static final String TAG = "CWPReader";

        private volatile boolean running = false;
        private Runnable myProcessor;
        private Socket cwpSocket = null;
        private InputStream nis = null; //Network Input Stream
        private int bytesToRead = 4;
        private int bytesRead = 0;

        CWPConnectionReader(Runnable processor) {
            myProcessor = processor;
        }

        void startReading() {
            Log.d(TAG, "Reading Started");
            running = true;
            start();
        }

        void stopReading() throws InterruptedException, IOException {
            Log.d(TAG, "Reading Stopped");
            running = false;
            cwpSocket.close();
            nis.close();
            nos.close();
            cwpSocket = null;
            nis = null;
            nos = null;
            changeProtocolState(CWPState.Disconnected, 0);
        }

        private void doInitialize() throws InterruptedException, IOException {
            InetSocketAddress address = new InetSocketAddress(serverAddr, serverPort);
            cwpSocket = new Socket();
            cwpSocket.connect(address);
            nis = cwpSocket.getInputStream();
            nos = cwpSocket.getOutputStream();
            changeProtocolState(CWPState.Connected, 0);
        }

        private int readLoop(byte [] bytes, int bytesToRead) throws IOException {
            return 0;
        }


        @Override
        public void run() {
            try {
                doInitialize();
                while(running) {
                    
                }
            } catch (InterruptedException e) {

            } catch (IOException e) {

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
