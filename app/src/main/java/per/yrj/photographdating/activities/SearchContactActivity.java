package per.yrj.photographdating.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.R;
import per.yrj.photographdating.callback.CallBackObject;
import per.yrj.photographdating.database.FriendDao;
import per.yrj.photographdating.domain.Account;
import per.yrj.photographdating.domain.Friend;
import per.yrj.photographdating.future.HttpFuture;
import per.yrj.photographdating.network.NetWorkRequestManager;
import per.yrj.photographdating.utils.CommonUtils;

/**
 * Created by YiRenjie on 2016/6/19.
 */
public class SearchContactActivity extends BaseActivity implements SearchView.OnQueryTextListener {
    private View vClickItem;
    private TextView tvSearchContent;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_contact);

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

        vClickItem = findViewById(R.id.search_item);
        tvSearchContent = (TextView) findViewById(R.id.search_tv_content);
        progressBar = (ProgressBar) findViewById(R.id.login_progress);
        vClickItem.setVisibility(View.GONE);
    }

    private void initEvent() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_contact, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        Account currentAccount = MyApplication.getCurrentAccount();
        String currentUser = currentAccount.getAccount();
        if (currentUser.equals(query)) {
            Toast.makeText(this,"不要找自己啦", Toast.LENGTH_SHORT).show();
            return true;
        }

        // 已有的朋友
        FriendDao dao = new FriendDao(this);
        Friend friend = dao.queryFriendByAccount(currentUser, query);
        if (friend != null) {
            Intent intent = new Intent(this, FriendDetailActivity.class);
            intent.putExtra(FriendDetailActivity.KEY_ENTER,
                    FriendDetailActivity.ENTER_CONTACT);
            intent.putExtra(FriendDetailActivity.KEY_DATA, friend);
            startActivity(intent);

            return true;
        }

        CommonUtils.hideSoftKeyboard(SearchContactActivity.this);
        progressBar.setVisibility(View.VISIBLE);

        HttpFuture future = NetWorkRequestManager.getInstance().searchContact(query,
                new CallBackObject<Friend>() {

                    @Override
                    public void onSuccess(Friend t, String action) {
                        progressBar.setVisibility(View.GONE);
                        if (t != null) {

                            Intent intent = new Intent(
                                    SearchContactActivity.this,
                                    FriendDetailActivity.class);
                            intent.putExtra(FriendDetailActivity.KEY_ENTER,
                                    FriendDetailActivity.ENTER_SEARCH);
                            intent.putExtra(FriendDetailActivity.KEY_DATA, t);
                            startActivity(intent);

                            finish();
                        }
                    }

                    @Override
                    public void onFailure(int error, String msg) {
                        progressBar.setVisibility(View.GONE);
                        Log.i("", error + " : " + msg);

                        if (error == 200) {
                            Toast.makeText(SearchContactActivity.this, "你搜索的用户不存在"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            vClickItem.setVisibility(View.GONE);
            return false;
        }
        tvSearchContent.setText(newText);
        vClickItem.setVisibility(View.VISIBLE);
        return true;
    }

}
