package esde06.tol.oulu.fi.cwprotocol;

import java.util.Observer;
import java.io.IOException;

public interface CWPMessaging {
    void addObserver(Observer observer);

    void deleteObserver(Observer observer);

    void lineUp() throws IOException;

    void lineDown() throws IOException;

    boolean isConnected();

    boolean lineIsUp();

    boolean serverSetLineUp();
}
