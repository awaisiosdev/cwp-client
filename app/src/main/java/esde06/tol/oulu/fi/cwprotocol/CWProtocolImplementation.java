package esde06.tol.oulu.fi.cwprotocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.Observer;
import java.io.OutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.net.Socket;
import android.util.Log;
import android.os.Handler;
import android.os.ConditionVariable;


public class CWProtocolImplementation implements CWPControl, CWPMessaging, Runnable {

    private static final String TAG = "ProtocolImplementation";

    public enum CWPState {Disconnected, Connected, LineUp, LineDown};
    private volatile CWPState currentState = CWPState.Disconnected;
    private CWPState nextState = currentState;
    private Boolean lineUpByUser = false;
    private Boolean lineUpByServer = false;

    private CWPConnectionReader reader = null;
    private CWPConnectionWriter writer = null;
    private static final int BUFFER_LENGTH = 64;
    private OutputStream nos = null; //Network Output Stream
    private ByteBuffer outBuffer = null;
    private String serverAddr = null;
    private int serverPort = -1;
    private int messageValue = 0;

    private long connectedStamp = 0;
    private long lastLineUpStamp = 0;

    private ConditionVariable writerHandle = new ConditionVariable();
    private int data32bit = 0;
    private short data16bit = 0;

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
        lineUpByUser = true;
        if (lineUpByServer){
           return;
        }
        lastLineUpStamp = System.currentTimeMillis();
        data32bit = (int) (lastLineUpStamp - connectedStamp);
        writerHandle.open();
        currentState = CWPState.LineUp;
        Log.d(TAG, "Line Up message : " + data32bit);
        Log.d(TAG, "Sending line Up state change event.");
        listener.onEvent(CWProtocolListener.CWPEvent.ELineUp, 0);
    }

    public void lineDown() throws IOException {
        Log.d(TAG, "Line Down signal generated by user.");
        lineUpByUser = false;
        if (lineUpByServer){
            return;
        }
        data16bit = (short) (System.currentTimeMillis() - lastLineUpStamp);
        writerHandle.open();
        currentState = CWPState.LineDown;
        Log.d(TAG, "Line Down message : " + data16bit);
        Log.d(TAG, "Sending line Down state change event.");
        listener.onEvent(CWProtocolListener.CWPEvent.ELineDown, 0);
    }

    public void connect(String serverAddr, int serverPort, int frequency) {
        Log.d(TAG, "Connect to CWP Server.");
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.currentFrequency = frequency;
        reader = new CWPConnectionReader(this);
        reader.startReading();
        Log.d(TAG, "Started Reading incoming messages.");

        writer = new CWPConnectionWriter();
        writer.startSending();
    }

    public void disconnect() throws IOException {
        Log.d(TAG, "Disconnect CWP Server.");
        if (reader != null) {
            reader.stopReading();
            try {
                reader.join();
            } catch (InterruptedException e){
            }
            reader = null;
        }
        if (writer != null){
            writer.stopSending();
            writer = null;
        }

    }

    public void sendFrequency(){
        data32bit = this.currentFrequency;
        writerHandle.open();
        Log.d(TAG, "Frequency change message : " + data32bit);
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

    public void setFrequency(int frequency) {
        Log.d(TAG, "Set frequency to " + frequency);
        currentFrequency = frequency;
        sendFrequency();
    }

    public int frequency() {
        return currentFrequency;
    }

    @Override
    public void run() {

        if (currentState == CWPState.Connected && nextState == CWPState.LineDown){
            if (messageValue != currentFrequency) {
                Log.d(TAG, "Sending frequency change to the server");
                sendFrequency();
                return;
            } else {
                Log.d(TAG, "Frequency is now changed to " + currentFrequency);
                Log.d(TAG, "Sending Frequency change event.");
                listener.onEvent(CWProtocolListener.CWPEvent.EChangedFrequency, 0);
            }
        }

        switch(nextState){
            case Connected:
                connectedStamp = System.currentTimeMillis();
                Log.d(TAG, "Sending Connected state change event.");
                listener.onEvent(CWProtocolListener.CWPEvent.EConnected, 0);
                break;
            case Disconnected:
                Log.d(TAG, "Sending Disconnected state change event.");
                listener.onEvent(CWProtocolListener.CWPEvent.EDisconnected, 0);
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
        currentState = nextState;
    }

    private class CWPConnectionReader extends Thread {

        private static final String TAG = "CWPReader";

        private volatile boolean running = false;
        private Runnable myProcessor;
        private Socket cwpSocket = null;
        private InputStream nis = null; //Network Input Stream
        private int bytesToRead = 4;
        private int bytesRead = 0;
        private int reservedValue = -2147483648;

        CWPConnectionReader(Runnable processor) {
            myProcessor = processor;
        }

        void startReading() {
            Log.d(TAG, "Reading Started");
            running = true;
            start();
        }

        void stopReading() throws IOException {
            Log.d(TAG, "Reading Stopped");
            running = false;
            if (cwpSocket != null){
                cwpSocket.close();
                cwpSocket = null;
            }
            if (nis != null){
                nis.close();
                nis = null;
            }
            if (nos != null){
                nos.close();
                nos = null;
            }
            changeProtocolState(CWPState.Disconnected, 0);
        }

        private void doInitialize() throws IOException {
            InetSocketAddress address = new InetSocketAddress(serverAddr, serverPort);
            cwpSocket = new Socket();
            cwpSocket.connect(address);
            nis = cwpSocket.getInputStream();
            nos = cwpSocket.getOutputStream();
            changeProtocolState(CWPState.Connected, 0);
        }

        private int readLoop(byte [] bytes, int bytesToRead) throws IOException {
            int readNow = nis.read(bytes, bytesRead, bytesToRead - bytesRead);
            if (readNow == -1) {
                throw new IOException("Read -1 from server");
            }
            return readNow;
        }
        // Get the buffer value by setting position to 0, and clear it after reading value.
        private int readValueAndClear(ByteBuffer buffer) {
            Log.d(TAG, "Bytes read cycle completed.");
            buffer.position(0);
            int value = buffer.getInt();
            Log.d(TAG, "Received Value: " + value);
            buffer.clear();
            return value;
        }

        // Start new read cycle
        private void startNewReadCycle(int bytesToRead){
            this.bytesToRead = bytesToRead;
            this.bytesRead = 0;
            Log.d(TAG, "Starting new read cycle for bytes: " + bytesToRead);
        }

        @Override
        public void run() {
            try {
                doInitialize();
                ByteBuffer buffer = ByteBuffer.allocate(100);
                while (running) {
                    bytesRead = bytesRead + readLoop(buffer.array(), this.bytesToRead);
                    Log.d(TAG, "Bytes Read: " + bytesRead + " , Bytes To Read: " + this.bytesToRead);
                    if(bytesRead != this.bytesToRead){
                        continue;
                    }

                    if (this.bytesRead == 2){
                        int value = readValueAndClear(buffer);
                        Log.d(TAG, "Received Line Down Signal");
                        changeProtocolState(CWPState.LineDown, value);
                        startNewReadCycle(4);
                        continue;
                    }

                    if (this.bytesRead == 4){
                        int value = readValueAndClear(buffer);
                        if (value > 0){
                            Log.d(TAG, "Received Line Up Signal");
                            changeProtocolState(CWPState.LineUp, value);
                            startNewReadCycle(2);
                        } else if (value < 0){
                            if (value != reservedValue) {
                                Log.d(TAG, "Received Frequency Confirmation signal");
                                changeProtocolState(CWPState.LineDown, value);
                            } else {
                                Log.d(TAG, "Ignoring reserved value :" + value);
                            }
                            startNewReadCycle(4);
                        }
                    }
                }
            }  catch (IOException e) {
                changeProtocolState(CWPState.Disconnected, 0);
            }
        }

        private void changeProtocolState(CWPState state, int param) {
            Log.d(TAG, "Change protocol state to " + state);
            nextState = state;
            messageValue = param;
            receiveHandler.post(myProcessor);
        }
    }

    private class CWPConnectionWriter extends Thread {
        private static final String TAG = "CWPWriter";
        private volatile boolean running = false;

        public void startSending() {
            Log.d(TAG, "Sending started");
            running = true;
            start();
        }

        public void stopSending() {
            Log.d(TAG, "Sending stopped");
            running = false;
            writerHandle.open();
        }

        private void sendMessage(int msg) throws IOException {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.order(ByteOrder.BIG_ENDIAN);
            buffer.putInt(msg);
            buffer.position(0);
            byte[] data = buffer.array();
            nos.write(data);
            nos.flush();
        }

        private void sendMessage(short msg) throws IOException {
           ByteBuffer buffer = ByteBuffer.allocate(2);
           buffer.order(ByteOrder.BIG_ENDIAN);
           buffer.putShort(msg);
           buffer.position(0);
           byte[] data = buffer.array();
           nos.write(data);
           nos.flush();
        }

        @Override
        public void run(){
            try {
                while (running) {
                    writerHandle.block();     // block thread execution when there is no data to send.
                    if (data32bit > 0 || data32bit < 0){
                        sendMessage(data32bit);
                        data32bit = 0;
                    } else if (data16bit > 0){
                        sendMessage(data16bit);
                        data16bit = 0;
                    }
                    writerHandle.close(); // close so thread does not keep running until protocol implementation opens it again.
                }

            } catch (IOException e) {

            }
        }
    }

}
