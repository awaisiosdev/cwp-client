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
import esde06.tol.oulu.fi.model.CWPMessage;
import esde06.tol.oulu.fi.model.CWPAudio;

public class ControlFragment extends Fragment implements View.OnTouchListener, TextView.OnEditorActionListener, Observer, SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = "ControlFragment";
    CWPControl control;
    CWPAudio audioHandle;
    ToggleButton connectionSwitch;
    EditText frequencyValue;
    Button changeFrequency;
    SharedPreferences preferences;

    private String serverAddressKey;
    private String serverPortKey;
    private String connectionFrequencyKey;
    private String beepMuteKey;
    private String beepVolumeKey;
    private String autoReconnectKey;
    private String shouldConnectAuto;

    public ControlFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentLayout = inflater.inflate(R.layout.fragment_control, container, false);


        setupSharedPreferenes();

        connectionSwitch = fragmentLayout.findViewById(R.id.connectionSwitch);
        connectionSwitch.setOnTouchListener(this);
        frequencyValue = fragmentLayout.findViewById(R.id.frequencyValue);
        frequencyValue.setOnEditorActionListener(this);
        changeFrequency = fragmentLayout.findViewById(R.id.changeFrequency);
        changeFrequency.setOnTouchListener(this);

        frequencyValue.setText(preferences.getString(connectionFrequencyKey, "-1"));
        return fragmentLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        connectionSwitch.setChecked(control.isConnected());
        setupAudioFeedback();
        if (preferences.getBoolean(autoReconnectKey, true) && preferences.getBoolean(shouldConnectAuto, false)){
            connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        audioHandle.turnOffAudioFeedback();
        if (control.isConnected()){
            disconnect();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CWPProvider provider = (CWPProvider) getActivity();
        audioHandle = provider.getAudio();
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
        String message = "Connecting to " + serverAddress + ":" + serverPort + " at frequency: " + frequency;
        Log.d(TAG, message);
        showToast(message);

        control.connect(serverAddress, Integer.parseInt(serverPort), Integer.parseInt(frequency));
        setShouldConnectAuto(false);
    }

    private void disconnect(){
        try {
            Log.d(TAG, "Disconnect to protocol server request initiated.");
            control.disconnect();
        } catch (IOException e){

        }
    }

    private void changeFrequency(){

        hideKeyboard();
        if (!control.isConnected() || control.lineIsUp()){
            showToast("Frequency change not allowed.");
            return;
        }

        // Get the new frequency value
        int newFrequency = Integer.valueOf(frequencyValue.getText().toString());
        if (control.frequency() == newFrequency){
            showToast("New frequency is the same.");
            return;
        }

        control.setFrequency(newFrequency);
        // store the new frequency value in the preferences.
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(connectionFrequencyKey,Integer.toString(newFrequency));
        edit.apply();

    }

    private void setupAudioFeedback(){
        Boolean isAudioMuted = preferences.getBoolean(beepMuteKey, true);
        if (isAudioMuted){
            audioHandle.turnOffAudioFeedback();
            return;
        }
        int alertVolume = preferences.getInt(beepVolumeKey, 50);
        audioHandle.turnOnAudioFeedback(alertVolume);
    }

    private void hideKeyboard(){
        frequencyValue.clearFocus();
        InputMethodManager imm = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(frequencyValue.getWindowToken(), 0);
    }

    private void setupSharedPreferenes(){
        // Initialize the shared preference keys
        serverAddressKey = getString(R.string.pref_key_server_address);
        serverPortKey = getString(R.string.pref_key_server_port);
        connectionFrequencyKey = getString(R.string.pref_key_connection_frequency);
        beepMuteKey = getString(R.string.pref_key_signal_beep_mute);
        beepVolumeKey = getString(R.string.pref_key_signal_beep_volume);
        autoReconnectKey = getString(R.string.pref_key_auto_reconnect);
        shouldConnectAuto = getString(R.string.pref_key_should_auto_connect);

        preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void setShouldConnectAuto(Boolean flag){
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(shouldConnectAuto,flag);
        edit.apply();
    }

    @Override
    public void update(Observable o, Object arg) {
        CWPMessage msg = (CWPMessage) arg;
        Log.d(TAG, "Received protocol event : " + msg.event.name());
        if (msg.event == CWPEvent.EConnected || msg.event == CWPEvent.EDisconnected){
            int textId = msg.event == CWPEvent.EConnected ? R.string.Connected : R.string.Disconnected;
            showToast(getString(textId));
        }
        connectionSwitch.setChecked(control.isConnected());
        if (msg.event == CWPEvent.EChangedFrequency) {
            showToast("Frequency Set: " + msg.param);
        }
        if (msg.event == CWPEvent.EConnected){
            setShouldConnectAuto(true);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preference changed - " + key);
        if (key.equals(beepMuteKey) || key.equals(beepVolumeKey)){
            setupAudioFeedback();
        }
    }

    private void showToast(String message){
        Toast.makeText(getActivity().getApplicationContext(),
                message,
                Toast.LENGTH_SHORT).show();
    }
}
