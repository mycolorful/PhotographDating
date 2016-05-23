package per.yrj.photographdating;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import java.util.ArrayList;
import java.util.List;

import per.yrj.photographdating.fragments.MainFragment;
import per.yrj.photographdating.fragments.MatchFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    private List<Fragment> mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mFragments = new ArrayList<>();
        mFragments.add(new MainFragment());
        mFragments.add(new MatchFragment());
        //add a MainFragment to FrameLayout
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fl_content, new MainFragment(), "main_fragment");
        transaction.commit();

        //-----------------init NavigationView--------------------------
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

     @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //-------------------- NavigationItemSelectedListener --------------------

    private int lastIndex;
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_camera) {
            showFragment(0);
        } else if (id == R.id.nav_gallery) {
            showFragment(1);
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 实现fragment切换时，MainFragment不会被remove，只会被hide。
     * 其他fragment则会remove。
     * @param index
     */
    private void showFragment(int index) {
        if (index == lastIndex)
            return;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (lastIndex == 0){
            transaction.hide(mFragments.get(lastIndex));
            transaction.add(R.id.fl_content, mFragments.get(index));
        }else {
            transaction.remove(mFragments.get(lastIndex));
            if (index == 0){
                transaction.show(mFragments.get(index));
            }else {
                transaction.add(R.id.fl_content,mFragments.get(index));
            }
        }
        transaction.commit();
        lastIndex = index;
    }

}
