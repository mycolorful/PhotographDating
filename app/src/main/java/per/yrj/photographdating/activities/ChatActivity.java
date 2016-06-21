package per.yrj.photographdating.activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.R;
import per.yrj.photographdating.callback.ChatCallBack;
import per.yrj.photographdating.constant.Action;
import per.yrj.photographdating.database.FriendDao;
import per.yrj.photographdating.database.MessageDao;
import per.yrj.photographdating.database.SQL;
import per.yrj.photographdating.debug.Debug;
import per.yrj.photographdating.debug.Debugable;
import per.yrj.photographdating.domain.Friend;
import per.yrj.photographdating.domain.Message;
import per.yrj.photographdating.network.NetWorkRequestManager;
import per.yrj.photographdating.receiver.PushReceiver;
import per.yrj.photographdating.utils.CommonUtils;


/**
 * Created by YiRenjie on 2016/6/17.
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener, TextWatcher, Debugable {
    private ListView listView;
    private Button btnSend;
    private EditText etContent;
    private MyAdapter mAdapter;
    private String talkTo;
    private Cursor cursor;

    private PushReceiver pushReceiver = new PushReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String from = intent.getStringExtra(PushReceiver.KEY_FROM);
            if (from.equalsIgnoreCase(talkTo)) {
                loadData();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        talkTo = getIntent().getStringExtra(getPackageName() + "chatWithAccount");

        if (talkTo == null) {
            throw new IllegalArgumentException();
        }
        // 初始化toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(talkTo);
        toolbar.setTitleTextColor(Color.WHITE);

        // 注册广播接收者
        IntentFilter filter = new IntentFilter();
        filter.addAction(PushReceiver.ACTION_TEXT);
        registerReceiver(pushReceiver, filter);

        listView = (ListView) findViewById(R.id.message_list_view);
        btnSend = (Button) findViewById(R.id.message_btn_send);
        etContent = (EditText) findViewById(R.id.message_et_content);
        btnSend.setEnabled(false);
        mAdapter = new MyAdapter(this, null, false);
        loadData();
        listView.setAdapter(mAdapter);
        listView.setSelection(cursor.getCount() - 1);

        btnSend.setOnClickListener(this);
        etContent.addTextChangedListener(this);
    }

    private void loadData() {
        MessageDao messageDao = new MessageDao(this);
        cursor = messageDao.queryMessage(MyApplication.getCurrentAccount().getAccount(), talkTo);
        mAdapter.changeCursor(cursor);
    }


    @Override
    public void onClick(View v) {
        // 本地存储
        final MessageDao messageDao = new MessageDao(this);
        final Message msg = new Message();
        msg.setOwner(MyApplication.getCurrentAccount().getAccount());
        msg.setAccount(talkTo);
        msg.setRead(true);
        msg.setContent(etContent.getText().toString());
        msg.setCreateTime(System.currentTimeMillis());
        msg.setDirection(0);
        msg.setState(1);
        msg.setType(0);
        messageDao.addMessage(msg);
        // 更新ui
        loadData();

        etContent.setText("");
        btnSend.setEnabled(false);

        // 网络发送消息
        NetWorkRequestManager manager = NetWorkRequestManager.getInstance();
        manager.sendMessage(msg, Action.Request.TEXT, new ChatCallBack() {
            @Override
            public void onSuccess() {

                msg.setState(2);
                messageDao.updateMessage(msg);
                // 更新ui
                loadData();
            }

            @Override
            public void onProgress() {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(int error, String errorString) {

                msg.setState(3);
                messageDao.updateMessage(msg);
                // 更新ui
                loadData();
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        btnSend.setEnabled(true);
    }

    @Override
    public boolean shouldDebug() {
        return true;
    }

    @Override
    public String tag() {
        return "ChatActivity";
    }

    private class MyAdapter extends CursorAdapter {

        public MyAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return View.inflate(ChatActivity.this, R.layout.item_chat, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView tvTime = (TextView) view
                    .findViewById(R.id.item_message_tv_time);
            View senderView = view.findViewById(R.id.item_message_sender);
            View receiverView = view.findViewById(R.id.item_message_receiver);

            int direction = cursor.getInt(cursor
                    .getColumnIndex(SQL.Message.COLUMN_DIRECTION));
            long createTime = cursor.getLong(cursor
                    .getColumnIndex(SQL.Message.COLUMN_CREATE_TIME));
            tvTime.setText(CommonUtils.getDateFormat(createTime) + "");

            if (direction == 0) {
                // 如果是发送..
                senderView.setVisibility(View.VISIBLE);
                receiverView.setVisibility(View.GONE);

                ImageView senderIconView = (ImageView) view
                        .findViewById(R.id.item_message_sender_icon);
                TextView senderContentView = (TextView) view
                        .findViewById(R.id.item_message_sender_tv_content);
                ProgressBar pbLoading = (ProgressBar) view
                        .findViewById(R.id.item_message_sender_pb_state);
                ImageView faildView = (ImageView) view
                        .findViewById(R.id.item_message_sender_iv_faild);

                senderContentView.setText(cursor.getString(cursor
                        .getColumnIndex(SQL.Message.COLUMN_CONTENT)));

                int state = cursor.getInt(cursor
                        .getColumnIndex(SQL.Message.COLUMN_STATE));

                // 1.正在发送 2.已经成功发送 3.发送失败
                if (state == 1) {
                    pbLoading.setVisibility(View.VISIBLE);
                    faildView.setVisibility(View.GONE);
                } else if (state == 2) {
                    pbLoading.setVisibility(View.GONE);
                    faildView.setVisibility(View.GONE);
                } else {
                    pbLoading.setVisibility(View.GONE);
                    faildView.setVisibility(View.VISIBLE);
                }
            } else {
                // 如果是接受...
                senderView.setVisibility(View.GONE);
                receiverView.setVisibility(View.VISIBLE);

                ImageView receiverIconView = (ImageView) view
                        .findViewById(R.id.item_message_receiver_icon);
                TextView receiverContentView = (TextView) view
                        .findViewById(R.id.item_message_receiver_tv_content);

                receiverContentView.setText(cursor.getString(cursor
                        .getColumnIndex(SQL.Message.COLUMN_CONTENT)));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MessageDao dao = new MessageDao(this);
        dao.clearUnread(MyApplication.getCurrentAccount().getAccount(), talkTo);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(pushReceiver);
    }
}
