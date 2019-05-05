package esde06.tol.oulu.fi;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.MotionEvent;
import android.widget.TextView;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import esde06.tol.oulu.fi.cwprotocol.CWPMessaging;
import esde06.tol.oulu.fi.cwprotocol.CWProtocolListener.CWPEvent;
import esde06.tol.oulu.fi.model.CWPMessage;

public class TappingFragment extends Fragment implements View.OnTouchListener, Observer {

    private static final String TAG = "TappingFragment";
    private ImageView lineStatusImage;
    private CWPMessaging messaging;

    public TappingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_tapping, container, false);
        lineStatusImage = fragmentView.findViewById(R.id.lineStatusIcon);
        lineStatusImage.setOnTouchListener(this);
        return fragmentView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CWPProvider provider = (CWPProvider) getActivity();
        if (provider != null) {
            messaging = provider.getMessaging();
            messaging.addObserver(this);
        }
        Log.d(TAG, "Started observing protocol events.");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        messaging.deleteObserver(this);
        messaging = null;
        Log.d(TAG, "Stopped observing protocol events.");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (!messaging.isConnected()) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            try {
                Log.d(TAG, "Line Up signal send by user.");
                messaging.lineUp();
                changeLineState(true, R.id.userLineState);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            try {
                Log.d(TAG, "Line Down signal send by user.");
                messaging.lineDown();
                changeLineState(false, R.id.userLineState);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private void changeLineState(boolean isLineUp, Integer id) {
        if (this.getView() != null) {
            TextView lineState = this.getView().findViewById(id);
            if (isLineUp) {
                lineState.setText("●");
            } else {
                lineState.setText("○");
            }
        }
    }

    private void changeLineStatus(CWPEvent event) {
        switch (event) {
            case ELineDown:
                lineStatusImage.setImageResource(R.mipmap.down);
                break;
            case EChangedFrequency:
                break;
            case ELineUp:
                lineStatusImage.setImageResource(R.mipmap.up);
                break;
            case EConnected:
                lineStatusImage.setImageResource(R.mipmap.down);
                break;
            case EServerStateChange:
                break;
            case EDisconnected:
                changeLineState(false, R.id.serverLineState);
                changeLineState(false, R.id.userLineState);
                lineStatusImage.setImageResource(R.mipmap.offline);
                break;
        }
        if (event == CWPEvent.ELineDown || event == CWPEvent.ELineUp || event == CWPEvent.EServerStateChange) {
            changeLineState(messaging.serverSetLineUp(), R.id.serverLineState);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        CWPMessage msg = (CWPMessage) arg;
        changeLineStatus(msg.event);
        Log.d(TAG, "Received protocol event: " + msg.event.name());
    }
}
