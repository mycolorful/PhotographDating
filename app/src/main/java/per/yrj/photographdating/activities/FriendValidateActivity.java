package per.yrj.photographdating.activities;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.R;
import per.yrj.photographdating.callback.ChatCallBack;
import per.yrj.photographdating.constant.Action;
import per.yrj.photographdating.database.FriendDao;
import per.yrj.photographdating.domain.Friend;
import per.yrj.photographdating.domain.Message;
import per.yrj.photographdating.network.NetWorkRequestManager;

/**
 * Created by YiRenjie on 2016/6/19.
 */
public class FriendValidateActivity extends BaseActivity implements View.OnClickListener, TextWatcher {
    private Button btSend;

    private EditText mEtContent;

    private String receiver;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_friend_validate);

        receiver = getIntent().getStringExtra("receiver");

        initView();
        initEvent();
    }

    private void initView() {
        btSend = (Button) findViewById(R.id.bt_send_invitaion);
        mEtContent = (EditText) findViewById(R.id.friend_validate_et_content);

        // 初始化toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("好友验证");
        toolbar.setTitleTextColor(Color.WHITE);
    }

    private void initEvent() {
        btSend.setOnClickListener(this);
        mEtContent.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btSend) {
            clickSend();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
    }


    private void clickSend() {
        String content = mEtContent.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "请输入邀请验证信息",Toast.LENGTH_SHORT).show();
            return;
        }

        Message msg = new Message();
        msg.setOwner(MyApplication.getCurrentAccount().getAccount());
        msg.setAccount(receiver);
        msg.setRead(true);
        msg.setContent(content);
        msg.setCreateTime(System.currentTimeMillis());
        msg.setDirection(0);
        msg.setState(1);
        msg.setType(0);

        NetWorkRequestManager.getInstance().sendMessage(msg, Action.Request.INVITATION, new ChatCallBack() {

            @Override
            public void onSuccess() {
                Toast.makeText(FriendValidateActivity.this, "邀请发送成功",Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onProgress() {

            }

            @Override
            public void onError(int error, String msg) {
                Toast.makeText(FriendValidateActivity.this, "邀请发送失败",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clickCancel() {
        finish();
    }

}
