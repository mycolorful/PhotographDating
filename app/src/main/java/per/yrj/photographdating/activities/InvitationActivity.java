package per.yrj.photographdating.activities;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.R;
import per.yrj.photographdating.database.BackTaskDao;
import per.yrj.photographdating.database.FriendDao;
import per.yrj.photographdating.database.InvitationDao;
import per.yrj.photographdating.database.SQL;
import per.yrj.photographdating.domain.Account;
import per.yrj.photographdating.domain.BackTask;
import per.yrj.photographdating.domain.Friend;
import per.yrj.photographdating.domain.Invitation;
import per.yrj.photographdating.domain.NetTask;
import per.yrj.photographdating.service.BackgroundService;
import per.yrj.photographdating.utils.BackTaskFactory;
import per.yrj.photographdating.utils.CommonUtils;
import per.yrj.photographdating.utils.DirUtil;
import per.yrj.photographdating.utils.SerializableUtil;

/**
 * Created by YiRenjie on 2016/6/20.
 */
public class InvitationActivity extends BaseActivity{
    private ListView listView;
    private FriendNewAdapter adapter;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_friend_new);
        initView();
        loadData();
    }

    private void initView() {
        // 初始化toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("好友请求");
        toolbar.setTitleTextColor(Color.WHITE);

        listView = (ListView) findViewById(R.id.friend_new_list_view);

        adapter = new FriendNewAdapter(this, null);
        listView.setAdapter(adapter);
    }

    private void loadData() {
        Account account = MyApplication.getCurrentAccount();

        InvitationDao dao = new InvitationDao(this);
        Cursor cursor = dao.queryCursor(account.getAccount());
        adapter.changeCursor(cursor);
    }

    View.OnClickListener acceptListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object o = v.getTag();
            if (o == null) {
                return;
            }

            // 更新数据库
            InvitationDao dao = new InvitationDao(getApplicationContext());
            Invitation invitation = (Invitation) o;
            invitation.setAgree(true);
            dao.updateInvitation(invitation);

            // 添加到好友列表
            FriendDao friendDao = new FriendDao(getApplicationContext());
            Friend friend = friendDao.queryFriendByAccount(
                    invitation.getOwner(), invitation.getAccount());
            if (friend == null) {
                friend = new Friend();
                friend.setAccount(invitation.getAccount());
                friend.setAlpha(CommonUtils.getFirstAlpha(invitation.getName()));
                friend.setIcon(invitation.getIcon());
                friend.setName(invitation.getName());
                friend.setOwner(invitation.getOwner());
                friend.setSort(0);
                friendDao.addFriend(friend);
            }

            // ui更新
            adapter.changeCursor(dao.queryCursor(invitation.getOwner()));

            // 添加接受朋友邀请的任务
            addAcceptFriendTask(invitation);
        }
    };

    private void addAcceptFriendTask(Invitation invitation) {

        // 存储到后台任务中
        String taskDir = DirUtil.getTaskDir(this);
        String file = CommonUtils.string2MD5(invitation.getAccount() + "_"
                + SystemClock.currentThreadTimeMillis());
        String path = new File(taskDir, file).getAbsolutePath();

        BackTask task = new BackTask();
        task.setOwner(invitation.getOwner());
        task.setPath(path);
        task.setState(0);
        new BackTaskDao(getApplicationContext()).addTask(task);

        NetTask netTask = BackTaskFactory.newFriendAcceptTask(
                invitation.getAccount(), invitation.getOwner());
        try {
            // 写入到缓存
            SerializableUtil.write(netTask, path);

            // 开启后台服务
            startService(new Intent(getApplicationContext(),
                    BackgroundService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class FriendNewAdapter extends CursorAdapter {

        public FriendNewAdapter(Context context, Cursor c) {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return View.inflate(context, R.layout.item_new_friend, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView ivIcon = (ImageView) view
                    .findViewById(R.id.item_new_friend_icon);
            TextView tvName = (TextView) view
                    .findViewById(R.id.item_new_friend_name);
            TextView tvAccept = (TextView) view
                    .findViewById(R.id.item_new_friend_tv_accept);
            Button btnAccept = (Button) view
                    .findViewById(R.id.item_new_friend_btn_accept);

            String account = cursor.getString(cursor
                    .getColumnIndex(SQL.Invitation.COLUMN_INVITATOR_ACCOUNT));
            String name = cursor.getString(cursor
                    .getColumnIndex(SQL.Invitation.COLUMN_INVITATOR_NAME));
            String icon = cursor.getString(cursor
                    .getColumnIndex(SQL.Invitation.COLUMN_INVITATOR_ICON));
            boolean agree = cursor.getInt(cursor
                    .getColumnIndex(SQL.Invitation.COLUMN_AGREE)) == 1;
            String content = cursor.getString(cursor
                    .getColumnIndex(SQL.Invitation.COLUMN_CONTENT));
            String owner = cursor.getString(cursor
                    .getColumnIndex(SQL.Invitation.COLUMN_OWNER));
            long id = cursor.getLong(cursor
                    .getColumnIndex(SQL.Invitation.COLUMN_ID));

            Invitation invitation = new Invitation();
            invitation.setAccount(account);
            invitation.setAgree(agree);
            invitation.setContent(content);
            invitation.setIcon(icon);
            invitation.setName(name);
            invitation.setOwner(owner);
            invitation.setId(id);

            if (!agree) {
                btnAccept.setVisibility(View.VISIBLE);
                tvAccept.setVisibility(View.GONE);
            } else {
                btnAccept.setVisibility(View.GONE);
                tvAccept.setVisibility(View.VISIBLE);
            }

            tvName.setText(account);

            btnAccept.setOnClickListener(acceptListener);
            btnAccept.setTag(invitation);
        }
    }
}
