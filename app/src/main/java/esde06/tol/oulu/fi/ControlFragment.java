package esde06.tol.oulu.fi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;
import android.widget.Toast;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import esde06.tol.oulu.fi.cwprotocol.CWPControl;
import esde06.tol.oulu.fi.cwprotocol.CWProtocolListener.CWPEvent;

public class ControlFragment extends Fragment implements View.OnTouchListener, Observer, SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = "ControlFragment";
    CWPControl control;
    ToggleButton connectionSwitch;
    SharedPreferences preferences;
    private String serverAddressKey;
    private String serverPortKey;
    private String connectionFrequencyKey;


    public ControlFragment() {
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

        connectionSwitch.setChecked(control.isConnected());

        preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        preferences.registerOnSharedPreferenceChangeListener(this);

        serverAddressKey = getString(R.string.pref_key_server_address);
        serverPortKey = getString(R.string.pref_key_server_port);
        connectionFrequencyKey = getString(R.string.pref_key_connection_frequency);
        return fragmentLayout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CWPProvider provider = (CWPProvider) getActivity();
        control = provider.getControl();
        control.addObserver(this);
        Log.d(TAG, "Started observing protocol events.");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        control.deleteObserver(this);
        control = null;
        Log.d(TAG, "Stopped observing protocol events");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v instanceof ToggleButton && event.getAction() == MotionEvent.ACTION_DOWN){
                if (!control.isConnected()){
                    String serverAddress = preferences.getString(serverAddressKey, "0.0.0.0");
                    String serverPort = preferences.getString(serverPortKey, "20000");
                    String frequency = preferences.getString(connectionFrequencyKey, "-1");

                    Log.d(TAG, "Connect to protocol server request initiated.");
                    Log.d(TAG, "Connecting to " + serverAddress + ":" + serverPort + " at frequency: " + frequency);

                    control.connect(serverAddress, Integer.parseInt(serverPort), Integer.parseInt(frequency));
                } else {
                    disconnect();
                }
        }
        return true;
    }

    private void disconnect(){
        try {
            Log.d(TAG, "Disconnect to protocol server request initiated.");
            control.disconnect();
        } catch (IOException e){

        }
    }

    @Override
    public void update(Observable o, Object arg) {
        CWPEvent event = (CWPEvent) arg;
        Log.d(TAG, "Received protocol event : " + event.name());
        if (event == CWPEvent.EConnected || event == CWPEvent.EDisconnected){
            int textId = event == CWPEvent.EConnected ? R.string.Connected : R.string.Disconnected;
            Toast.makeText(getActivity().getApplicationContext(),
                    getString(textId),
                    Toast.LENGTH_SHORT).show();
        }
        connectionSwitch.setChecked(control.isConnected());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preference changed - " + key);
        disconnect();
    }
}
