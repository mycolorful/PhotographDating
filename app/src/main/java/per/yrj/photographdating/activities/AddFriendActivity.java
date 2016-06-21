package per.yrj.photographdating.activities;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import per.yrj.photographdating.R;

/**
 *
 * Created by YiRenjie on 2016/6/19.
 */
public class AddFriendActivity extends BaseActivity implements View.OnClickListener {
    private EditText mSearchView;
    private View mScanView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_add);

        // 初始化toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("添加朋友");
        toolbar.setTitleTextColor(Color.WHITE);

        initView();
        initEvent();
    }

    private void initView() {
        mSearchView = (EditText) findViewById(R.id.friend_add_et_search);
        mScanView = findViewById(R.id.friend_add_scan);
    }

    private void initEvent() {
        mScanView.setOnClickListener(this);
        mSearchView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mScanView) {
            clickScan();
        } else if (v == mSearchView) {
            clickSearch();
        }
    }

    private void clickScan() {
//        startActivity(new Intent(this, QRActivity.class));
    }

    private void clickSearch() {
        startActivity(new Intent(this, SearchContactActivity.class));
    }
}
