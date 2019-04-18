package esde06.tol.oulu.fi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ToggleButton;
import android.widget.Toast;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import esde06.tol.oulu.fi.cwprotocol.CWPControl;
import esde06.tol.oulu.fi.cwprotocol.CWProtocolListener.CWPEvent;

public class ControlFragment extends Fragment implements View.OnTouchListener, TextView.OnEditorActionListener, Observer, SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = "ControlFragment";
    CWPControl control;
    ToggleButton connectionSwitch;
    EditText frequencyValue;
    Button changeFrequency;
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

        serverAddressKey = getString(R.string.pref_key_server_address);
        serverPortKey = getString(R.string.pref_key_server_port);
        connectionFrequencyKey = getString(R.string.pref_key_connection_frequency);

        connectionSwitch = fragmentLayout.findViewById(R.id.connectionSwitch);
        connectionSwitch.setOnTouchListener(this);
        frequencyValue = fragmentLayout.findViewById(R.id.frequencyValue);
        frequencyValue.setOnEditorActionListener(this);
        changeFrequency = fragmentLayout.findViewById(R.id.changeFrequency);
        changeFrequency.setOnTouchListener(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        preferences.registerOnSharedPreferenceChangeListener(this);

        connectionSwitch.setChecked(control.isConnected());
        frequencyValue.setText(preferences.getString(connectionFrequencyKey, "-1"));

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
                    connect();
                } else {
                    disconnect();
                }
        } else if (v instanceof Button && event.getAction() == MotionEvent.ACTION_DOWN){
            changeFrequency();
            // Hide the soft keyboard
            InputMethodManager imm = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(frequencyValue.getWindowToken(), 0);
        }
        return true;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            changeFrequency();
        }
        return false;
    }

    private void connect(){
        String serverAddress = preferences.getString(serverAddressKey, "0.0.0.0");
        String serverPort = preferences.getString(serverPortKey, "20000");
        String frequency = preferences.getString(connectionFrequencyKey, "-1");

        Log.d(TAG, "Connect to protocol server request initiated.");
        Log.d(TAG, "Connecting to " + serverAddress + ":" + serverPort + " at frequency: " + frequency);

        control.connect(serverAddress, Integer.parseInt(serverPort), Integer.parseInt(frequency));
    }

    private void disconnect(){
        try {
            Log.d(TAG, "Disconnect to protocol server request initiated.");
            control.disconnect();
        } catch (IOException e){

        }
    }

    private void changeFrequency(){

        // Get the new frequency value
        int newFrequency = Integer.valueOf(frequencyValue.getText().toString());
        control.setFrequency(newFrequency);

        // store the new frequency value in the preferences.
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(connectionFrequencyKey,Integer.toString(newFrequency));
        edit.apply();
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
        if (event == CWPEvent.EChangedFrequency) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Frequency Changed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preference changed - " + key);
        if (key != connectionFrequencyKey){
            if (control.isConnected()){
                disconnect();
            }
        }
    }
}
