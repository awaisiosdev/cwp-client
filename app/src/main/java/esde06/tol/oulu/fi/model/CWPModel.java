package esde06.tol.oulu.fi.model;

import java.io.IOException;
import java.util.Observable;
import esde06.tol.oulu.fi.cwprotocol.CWPMessaging;

public class CWPModel extends Observable implements CWPMessaging {
    public enum CWPState { Disconnected, Connected, LineUp, LineDown };
    private volatile CWPState currentState = CWPState.Connected;


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

    public boolean isConnected () {
         return currentState == CWPState.Connected;
    }

    public boolean serverSetLineUp(){
        return false;
    }
}
