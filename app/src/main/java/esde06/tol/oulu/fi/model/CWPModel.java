package esde06.tol.oulu.fi.model;

import java.io.IOException;
import java.util.Observable;

import esde06.tol.oulu.fi.cwprotocol.CWPControl;
import esde06.tol.oulu.fi.cwprotocol.CWPMessaging;

public class CWPModel extends Observable implements CWPMessaging, CWPControl {
    public enum CWPState { Disconnected, Connected, LineUp, LineDown };
    private volatile CWPState currentState = CWPState.Disconnected;
    int frequency = CWPControl.DEFAULT_FREQUENCY;

    // CWPMessaging Interface Implementation

    public void lineUp () throws IOException {
        currentState = CWPState.LineUp;
        setChanged();
        notifyObservers(currentState);
    }

    public void lineDown () throws IOException {
        currentState = CWPState.LineDown;
        setChanged();
        notifyObservers(currentState);
    }

    public boolean lineIsUp () {
        return currentState == CWPState.LineUp;
    }

    public boolean serverSetLineUp(){
        return false;
    }

    // CWPControl Interface Implementation

    public void connect(String serverAddr, int serverPort, int frequency) throws IOException {
        currentState = CWPState.Connected;
        setChanged();
        notifyObservers(currentState);
    }

    public void disconnect() throws IOException {
        currentState = CWPState.Disconnected;
        setChanged();
        notifyObservers(currentState);
    }

    public boolean isConnected () {
         return currentState == CWPState.Connected;
    }

    public void setFrequency(int frequency) throws IOException {

    }

    @Override
    public int frequency() {
        return 0;
    }


}
