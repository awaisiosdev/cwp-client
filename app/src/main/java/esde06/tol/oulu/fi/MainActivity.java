package esde06.tol.oulu.fi;

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

import esde06.tol.oulu.fi.cwprotocol.CWPMessaging;
import esde06.tol.oulu.fi.model.CWPModel;
import esde06.tol.oulu.fi.CWPProvider;

public class MainActivity extends AppCompatActivity implements CWPProvider {

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
        return super.onOptionsItemSelected(item);
    }

    public CWPMessaging getMessaging() {
        return cwpModel;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0){
                return new TappingFragment();
            } else if (position == 1){
                return new ControlFragment();
            }

            return null;
        }

        @Override
        public String getPageTitle(int position) {

            if (position == 0){
                return "Tapping";
            } else if (position == 1){
                return "Control";
            }

            return null;
        }
        @Override
        public int getCount() {
            // Show 1 total pages.
            return 2;
        }
    }
}
