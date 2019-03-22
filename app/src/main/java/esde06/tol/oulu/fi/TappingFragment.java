package esde06.tol.oulu.fi;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import esde06.tol.oulu.fi.model.CWPModel;
import esde06.tol.oulu.fi.model.CWPModel.CWPState;
import esde06.tol.oulu.fi.CWPProvider;

public class TappingFragment extends Fragment implements View.OnTouchListener, Observer {

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
        lineStatusImage = (ImageView) fragmentView.findViewById(R.id.lineStatusIcon);

        fragmentView.setOnTouchListener(this);
        return fragmentView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CWPProvider provider = (CWPProvider) getActivity();
        messaging = provider.getMessaging();
        messaging.addObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        messaging.deleteObserver(this);
        messaging = null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
             try {
                 messaging.lineUp();
             } catch (IOException e) {

             }
             return true;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP ) {
            try {
                messaging.lineDown();
            } catch (IOException e) {

            }
            return true;
        }
        return false;
    }

    private void changeUserLineState(boolean isLineUp){
        TextView userLineState = this.getView().findViewById(R.id.userLineState);
        if (isLineUp) {
            userLineState.setText("●");
        } else {
            userLineState.setText("○");
        }
    }

    private void changeLineStatusIcon(boolean isUp) {
        if (isUp){
            lineStatusImage.setImageResource(R.mipmap.up);
        } else {
            lineStatusImage.setImageResource(R.mipmap.down);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        CWPState state = (CWPState) arg;
        changeLineStatusIcon(state == CWPState.LineUp);
        changeUserLineState(state == CWPState.LineUp);
    }
}
