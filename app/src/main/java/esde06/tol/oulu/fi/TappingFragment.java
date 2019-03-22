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

public class TappingFragment extends Fragment implements View.OnTouchListener {

    private ImageView lineStatusImage;

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
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
             changeLineStatusIcon(true);
             changeUserLineState(true);
             return true;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP ) {
            changeLineStatusIcon(false);
            changeUserLineState(false);
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
}
