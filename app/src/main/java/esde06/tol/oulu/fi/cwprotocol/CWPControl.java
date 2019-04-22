package esde06.tol.oulu.fi.cwprotocol;

import java.util.Observer;
import java.io.IOException;

public interface CWPControl {
    public static final int DEFAULT_FREQUENCY = -1;

    public void addObserver(Observer observer);
    public void deleteObserver(Observer observer);
    // Connection management
    public void connect(String serverAddr, int serverPort, int frequency);
    public void disconnect()  throws IOException;
    public boolean isConnected();
    // Frequency management
    public void setFrequency(int frequency);
    public int frequency();
    public boolean lineIsUp();
}
