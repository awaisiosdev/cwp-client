package esde06.tol.oulu.fi;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class EventLogger {

    private static final String TAG = "CWPLogger";
    private static HashMap<String, ArrayList<Long>> eventHistory = new HashMap<>();
    private static HashMap<String, Long> eventLogs = new HashMap<>();
    public static final String serverEvent = "ServerEvent";
    public static final String lineUp = "LineUp";
    public static final String lineDown = "LineDown";


    public static void logEventStarted(String event) {
        eventLogs.remove(event);
        eventLogs.put(event, System.currentTimeMillis());
        Log.d(TAG, event);
    }

    public static void logEventEnded(String event) {
        if (!eventLogs.containsKey(event)){
            return;
        }
        long eventStarted = eventLogs.get(event);
        long duration = System.currentTimeMillis() - eventStarted;
        Log.d(TAG, event + " duration: " + duration);
        eventLogs.remove(event);
        ArrayList<Long> history = eventHistory.get(event);
        if (history == null){
            history = new ArrayList<>();
            history.add(duration);
        } else {
            history.add(duration);
        }
        eventHistory.put(event, history);
    }

    public static void getLoggingSummary(){
        Log.d(TAG, "Profiling Summary for Events");
        computeStatistics(serverEvent);
        computeStatistics(lineUp);
        computeStatistics(lineDown);
    }

    private static void computeStatistics(String event){
        ArrayList<Long> history = eventHistory.get(event);
        if (history == null){
            return;
        }
        // Log event counts, min, max & mean value.
        Log.d(TAG, event
                + " count: " + history.size()
                + " ,min: " + Collections.min(history)
                + " , max: " + Collections.max(history)
                + " , average: " + calculateMean(history)
        );
        eventHistory.remove(event);  // delete the previous event history
    }

    private static double calculateMean(ArrayList<Long> list){
        double sum = 0;
        Iterator<Long> scanner = list.iterator();
        while (scanner.hasNext()) {
            sum += scanner.next();
        }
        return Math.ceil(sum / list.size());
    }

}
