package esde06.tol.oulu.fi.cwprotocol;

import java.util.Observer;
import java.io.IOException;

public interface CWPMessaging {
    public void addObserver(Observer observer);
    public void deleteObserver(Observer observer);
    public void lineUp() throws IOException;
    public void lineDown() throws IOException;
    public boolean isConnected();
    public boolean lineIsUp();
    public boolean serverSetLineUp();
}
