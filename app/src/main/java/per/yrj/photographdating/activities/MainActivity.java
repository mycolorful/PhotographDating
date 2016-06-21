package per.yrj.photographdating.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.R;
import per.yrj.photographdating.database.FriendDao;
import per.yrj.photographdating.database.MessageDao;
import per.yrj.photographdating.debug.Debug;
import per.yrj.photographdating.domain.Account;
import per.yrj.photographdating.domain.Conversation;
import per.yrj.photographdating.domain.Friend;
import per.yrj.photographdating.domain.Message;
import per.yrj.photographdating.fragments.MainFragment;
import per.yrj.photographdating.fragments.MatchFragment;
import per.yrj.photographdating.service.ChatCoreService;
import per.yrj.photographdating.utils.CommonUtils;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    private List<Fragment> mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        boolean isFirst = getSharedPreferences("config", Context.MODE_PRIVATE).getBoolean("isFirst", true);
        if (isFirst){
            sendWelcome();
        }
        // 开启核心服务
        if (!CommonUtils.isServiceRunning(ChatCoreService.class)){
            startService(new Intent(this, ChatCoreService.class));
        }

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
        TextView tvName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.tv_user_name);
        String name = MyApplication.getCurrentAccount().getName();
        if (name == null){
            tvName.setText("未设置昵称");
        }else {
            tvName.setText(name);
        }
    }

    /**
     * 用户首次登陆时发送欢迎语
     */
    private void sendWelcome() {
        {
            FriendDao friendDao = new FriendDao(this);
            Account account = MyApplication.getCurrentAccount();
            Friend friend = friendDao.queryFriendByAccount(account.getAccount(), "PhotographDating");
            if (friend == null) {
                // 初始化通讯录
                friend = new Friend();
                friend.setOwner(account.getAccount());
                friend.setAccount("PhotographDating");
                friend.setAlpha("P");
                friend.setArea("");
                friend.setIcon("");
                friend.setName("约拍团队");
                friend.setNickName("");
                friend.setSort(1000);

                friendDao.addFriend(friend);
            }
//            Debug.i(this, account);// log

            MessageDao messageDao = new MessageDao(this);
            Message message = new Message();
            message.setAccount("PhotographDating");
            message.setContent("欢迎使用约拍，在这里找到你的专属摄影师");
            message.setCreateTime(System.currentTimeMillis());
            message.setDirection(1);
            message.setOwner(account.getAccount());
            message.setRead(false);
            messageDao.addMessage(message);
            getSharedPreferences("config", Context.MODE_PRIVATE).edit().putBoolean("isFirst",false).commit();
        }
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
        if (id == R.id.action_add_friend) {
            startActivity(new Intent(this, SearchContactActivity.class));
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

        System.out.println("=====");
        if (id == R.id.nav_camera) {
            showFragment(0);
            System.out.println("0");
        } else if (id == R.id.nav_match) {
            System.out.println("1");
            showFragment(1);
        } else if (id == R.id.nav_manage) {
            System.out.println("3");
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
            transaction.add(R.id.fl_content, mFragments.get(index));
            transaction.hide(mFragments.get(lastIndex));
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
