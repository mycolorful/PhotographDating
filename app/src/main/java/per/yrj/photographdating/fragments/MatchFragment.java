package per.yrj.photographdating.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import per.yrj.photographdating.R;

/**
 * Created by YiRenjie on 2016/5/21.
 */
public class MatchFragment extends BaseFragment {
    private AppCompatActivity mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = (AppCompatActivity) getContext();
        View v = inflater.inflate(R.layout.fra_match, null, false);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        mContext.setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) mContext.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                mContext, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        return v;
    }
}
