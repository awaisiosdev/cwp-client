package esde06.tol.oulu.fi.cwprotocol;

public interface CWProtocolListener {
    public enum CWPEvent {EConnected, EChangedFrequency, ELineUp, ELineDown, EServerStateChange, EDisconnected};
    public void onEvent(CWPEvent event, int param);
}
