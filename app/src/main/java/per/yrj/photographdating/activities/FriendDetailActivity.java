package per.yrj.photographdating.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import per.yrj.photographdating.R;
import per.yrj.photographdating.domain.Friend;

/**
 * Created by YiRenjie on 2016/6/19.
 */
public class FriendDetailActivity extends BaseActivity implements View.OnClickListener {
    public static final String KEY_ENTER = "enter";
    public static final String KEY_DATA = "data";

    public static final int ENTER_SEARCH = 1;
    public static final int ENTER_CONTACT = 2;

    private ImageView mIvIconView;

    private TextView mTvNameView;
    private TextView mTvAccountView;
    private TextView mTvNickNameView;

    private TextView mTvSignView;

    private Button mBtnAdd;
    private Button mBtnSend;

    private int enterFlag;
    private Friend mFriend;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_friend_detail);

        enterFlag = getIntent().getIntExtra(KEY_ENTER, -1);

        if (enterFlag == -1) {
            throw new RuntimeException("没有定义入口");
        }

        mFriend = (Friend) getIntent().getSerializableExtra(KEY_DATA);

        initView();
        initEvent();
    }

    private void initView() {

        // 初始化toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(mFriend.getAccount());
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mIvIconView = (ImageView) findViewById(R.id.friend_detail_iv_icon);
        mTvNameView = (TextView) findViewById(R.id.friend_detail_tv_name);
        mTvAccountView = (TextView) findViewById(R.id.friend_detail_tv_account);
        mTvNickNameView = (TextView) findViewById(R.id.friend_detail_tv_nickname);

        mTvSignView = (TextView) findViewById(R.id.friend_detail_tv_sign);

        mBtnAdd = (Button) findViewById(R.id.friend_detail_btn_add);
        mBtnSend = (Button) findViewById(R.id.friend_detail_btn_send);

        if (enterFlag == ENTER_SEARCH) {
            mTvAccountView.setVisibility(View.GONE);
            mTvNickNameView.setVisibility(View.GONE);

            mBtnAdd.setVisibility(View.VISIBLE);
            mBtnSend.setVisibility(View.GONE);

            mTvNameView.setText(mFriend.getName());
        } else if (enterFlag == ENTER_CONTACT) {
            mTvAccountView.setVisibility(View.VISIBLE);
            mTvNickNameView.setVisibility(View.VISIBLE);

            mBtnAdd.setVisibility(View.GONE);
            mBtnSend.setVisibility(View.VISIBLE);

            mTvNameView.setText(mFriend.getName());
            mTvAccountView.setText("黑信号:" + mFriend.getAccount());
            mTvNickNameView.setText("昵称:" + mFriend.getNickName());
        }
    }

    private void initEvent() {
        mBtnAdd.setOnClickListener(this);
        mBtnSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnAdd) {
            clickAdd();
        } else if (v == mBtnSend) {
            clickSend();
        }
    }

    private void clickBack() {
        finish();
    }

    private void clickSend() {
        // 跳转到发消息页面
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(getPackageName()+"chatWithAccount", mFriend.getAccount());
        startActivity(intent);
    }

    private void clickAdd() {
        // 发送邀请
        Intent intent = new Intent(this, FriendValidateActivity.class);
        intent.putExtra("receiver", mFriend.getAccount());
        startActivity(intent);
    }
}
