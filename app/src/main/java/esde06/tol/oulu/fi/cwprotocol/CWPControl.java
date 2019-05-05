package esde06.tol.oulu.fi.cwprotocol;

import java.util.Observer;
import java.io.IOException;

public interface CWPControl {
    int DEFAULT_FREQUENCY = -1;

    void addObserver(Observer observer);

    void deleteObserver(Observer observer);

    // Connection management
    void connect(String serverAddr, int serverPort, int frequency);

    void disconnect() throws IOException;

    boolean isConnected();

    // Frequency management
    void setFrequency(int frequency);

    int frequency();

    boolean lineIsUp();
}
