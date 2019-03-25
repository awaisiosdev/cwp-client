package esde06.tol.oulu.fi;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import esde06.tol.oulu.fi.cwprotocol.CWPControl;
import esde06.tol.oulu.fi.cwprotocol.CWProtocolListener.CWPEvent;

public class ControlFragment extends Fragment implements View.OnTouchListener, Observer {

    CWPControl control;
    ToggleButton connectionSwitch;

    public ControlFragment() {
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
        View fragmentLayout = inflater.inflate(R.layout.fragment_control, container, false);
        connectionSwitch = fragmentLayout.findViewById(R.id.connectionSwitch);
        connectionSwitch.setOnTouchListener(this);
        return fragmentLayout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CWPProvider provider = (CWPProvider) getActivity();
        control = provider.getControl();
        control.addObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        control.deleteObserver(this);
        control = null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v instanceof ToggleButton){

            try {

                if (!connectionSwitch.isChecked()) {
                    control.connect("0.0.0.0", 80, -1);
                } else {
                    control.disconnect();
                }

            } catch (IOException e){

            }

        }
        return false;
    }

    @Override
    public void update(Observable o, Object arg) {
        CWPEvent event = (CWPEvent) arg;
        if (event == CWPEvent.EConnected){
            Toast.makeText(getActivity().getApplicationContext(),
                    getString(R.string.Connected),
                    Toast.LENGTH_SHORT).show();
        } else if (event == CWPEvent.EDisconnected) {
            Toast.makeText(getActivity().getApplicationContext(),
                    getString(R.string.Disconnected),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
