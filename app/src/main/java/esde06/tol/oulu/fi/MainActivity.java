package esde06.tol.oulu.fi;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import esde06.tol.oulu.fi.cwprotocol.CWPControl;
import esde06.tol.oulu.fi.cwprotocol.CWPMessaging;
import esde06.tol.oulu.fi.model.CWPModel;

public class MainActivity extends AppCompatActivity implements CWPProvider {

    private final static String TAG = "MainActivity";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private CWPModel cwpModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        cwpModel = new CWPModel();
        PreferenceManager.setDefaultValues(this, R.xml.pref_cwpserver, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_signal_alert, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (cwpModel != null){
            Log.d(TAG, "Audio Feedback turned On!");
            cwpModel.turnOnAudioFeedback();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cwpModel != null){
            Log.d(TAG, "Audio Feedback turned Off!");
            cwpModel.turnOffAudioFeedback();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public CWPMessaging getMessaging() {
        return cwpModel;
    }
    public CWPControl getControl() {
        return cwpModel;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0){
                Log.d(TAG, "Tapping fragment created.");
                return new TappingFragment();
            } else if (position == 1){
                Log.d(TAG, "Control Fragment created.");
                return new ControlFragment();
            }

            return null;
        }

        @Override
        public String getPageTitle(int position) {

            if (position == 0){
                Log.d(TAG, "Tapping fragment title set.");
                return "Tapping";
            } else if (position == 1){
                Log.d(TAG, "Control fragment title set.");
                return "Control";
            }

            return null;
        }
        @Override
        public int getCount() {
            Log.d(TAG, "2 fragments");
            return 2;
        }
    }
}
