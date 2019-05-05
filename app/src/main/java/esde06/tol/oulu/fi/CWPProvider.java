package esde06.tol.oulu.fi;

import esde06.tol.oulu.fi.cwprotocol.CWPControl;
import esde06.tol.oulu.fi.cwprotocol.CWPMessaging;
import esde06.tol.oulu.fi.model.CWPAudio;

public interface CWPProvider {
    CWPMessaging getMessaging();

    CWPControl getControl();

    CWPAudio getAudio();
}
