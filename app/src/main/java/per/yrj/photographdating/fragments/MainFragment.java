package per.yrj.photographdating.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import per.yrj.photographdating.R;

/**
 * Created by YiRenjie on 2016/5/22.
 */
public class MainFragment extends Fragment {
    private AppCompatActivity mContext;
    private List<Fragment> mFragments;
    private FloatingActionButton fab;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = (AppCompatActivity) getContext();
        View v = inflater.inflate(R.layout.app_bar_main, null, false);
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        mContext.setSupportActionBar(toolbar);

        //fragments
        mFragments = new ArrayList<>();
        mFragments.add(new MessageFragment());
        mFragments.add(new PeopleFragment());
        mFragments.add(new DiscoveryFragment());
        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager_main);
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(mContext.getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) v.findViewById(R.id.tab_main);
        tabLayout.setupWithViewPager(viewPager);

        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) mContext.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                mContext, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        return v;
    }

    class MyViewPagerAdapter extends FragmentStatePagerAdapter {

        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.main_fras_title)[position];
        }
    }

}
