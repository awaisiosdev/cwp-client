package esde06.tol.oulu.fi;
import android.util.Log;

public class EventLogger {

    private static final String TAG = "CWPLogger";
    private static long eventStarted = 0;

    public static void logEventStarted(String event) {
        eventStarted = System.currentTimeMillis();
        Log.d(TAG, event);
    }

    public static void logEventEnded(String event) {
        if (eventStarted > 0) {
            long duration = System.currentTimeMillis() - eventStarted;
            Log.d(TAG, event + " duration: " +  duration);
        }
    }

}
