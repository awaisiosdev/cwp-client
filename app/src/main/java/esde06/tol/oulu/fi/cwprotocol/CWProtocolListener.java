package esde06.tol.oulu.fi.cwprotocol;

public interface CWProtocolListener {
    enum CWPEvent {EConnected, EChangedFrequency, ELineUp, ELineDown, EServerStateChange, EDisconnected}

    void onEvent(CWPEvent event, int param);
}
