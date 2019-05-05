package esde06.tol.oulu.fi.model;

public class CWPMessage {
    public CWPModel.CWPEvent event;
    public int param;

    CWPMessage(CWPModel.CWPEvent e, int p) {
        event = e;
        param = p;
    }
}
