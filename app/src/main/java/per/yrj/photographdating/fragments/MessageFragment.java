package per.yrj.photographdating.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.R;
import per.yrj.photographdating.activities.ChatActivity;
import per.yrj.photographdating.adapter.BaseViewHolder;
import per.yrj.photographdating.adapter.DividerItemDecoration;
import per.yrj.photographdating.database.MessageDao;
import per.yrj.photographdating.database.SQL;
import per.yrj.photographdating.domain.Account;
import per.yrj.photographdating.domain.Conversation;
import per.yrj.photographdating.receiver.PushReceiver;

/**
 * Created by YiRenjie on 2016/5/21.
 */
public class MessageFragment extends BaseFragment implements BaseViewHolder.OnItemClickListener {
    private RecyclerView rvMessage;
    private MyAdapter mAdapter;
    private List<Conversation> mData;
    private MyApplication mApplication;

    /**
     * 用于接收新会话
     */
    private PushReceiver pushReceiver = new PushReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String to = intent.getStringExtra(PushReceiver.KEY_TO);

            Account account = mApplication.getCurrentAccount();
            if (account.getAccount().equalsIgnoreCase(to)) {
                loadData();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fra_message, container, false);
        rvMessage = (RecyclerView) v.findViewById(R.id.rv_message);
        rvMessage.setLayoutManager(new LinearLayoutManager(getContext()));
        mApplication = (MyApplication) getActivity().getApplication();
        //注册广播接收者
        IntentFilter filter = new IntentFilter();
        filter.addAction(PushReceiver.ACTION_TEXT);
        filter.addAction(PushReceiver.ACTION_ICON_CHANGE);
        filter.addAction(PushReceiver.ACTION_NAME_CHANGE);
        getActivity().registerReceiver(pushReceiver, filter);

        mAdapter = new MyAdapter();
        loadData();
        rvMessage.setAdapter(mAdapter);
        rvMessage.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mAdapter.setOnItemClickListener(this);

        return v;
    }

    /**
     * 当有新的会话时刷新界面
     */
    private void loadData() {
        mData = new ArrayList<>();
        MessageDao dao = new MessageDao(getActivity());
        Cursor cursor = dao.queryConversation(mApplication.getCurrentAccount().getAccount());
        while (cursor.moveToNext()) {
            Conversation conversation = new Conversation();
            conversation.setAccount(cursor.getString(cursor.getColumnIndex(SQL.Conversation.COLUMN_ACCOUNT)));
            conversation.setName(cursor.getString(cursor.getColumnIndex(SQL.Conversation.COLUMN_NAME)));
            conversation.setContent(cursor.getString(cursor.getColumnIndex(SQL.Conversation.COLUMN_CONTENT)));
            conversation.setUnread(cursor.getInt(cursor.getColumnIndex(SQL.Conversation.COLUMN_UNREAD)));
            mData.add(conversation);
        }
        cursor.close();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View v, int position) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        Conversation talkToAccount = mData.get(position);
        intent.putExtra(getActivity().getPackageName()+"chatWithAccount", talkToAccount.getAccount());
        startActivity(intent);
        talkToAccount.setUnread(0);

    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private BaseViewHolder.OnItemClickListener mListener;

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(View.inflate(getContext(), R.layout.item_conversation, null));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Conversation conversation = mData.get(position);
            holder.tvName.setText(conversation.getAccount());
            holder.tvContent.setText(conversation.getContent());
            int unread = conversation.getUnread();
            if (unread <= 0) {
                holder.tvUnread.setVisibility(View.GONE);
                holder.tvUnread.setText("");
            } else {
                if (unread >= 99) {
                    holder.tvUnread.setText("99");
                } else {
                    holder.tvUnread.setText("" + unread);
                }
                holder.tvUnread.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void setOnItemClickListener(BaseViewHolder.OnItemClickListener listener) {
            mListener = listener;
        }

        class MyViewHolder extends BaseViewHolder {
            TextView tvUnread;
            TextView tvName;
            TextView tvContent;
            ImageView ivIcon;

            public MyViewHolder(View itemView) {
                super(itemView, mListener);
                tvUnread = (TextView) itemView
                        .findViewById(R.id.item_converation_tv_unread);
                tvName = (TextView) itemView
                        .findViewById(R.id.item_converation_name);
                tvContent = (TextView) itemView
                        .findViewById(R.id.item_converation_content);
                ivIcon = (ImageView) itemView.findViewById(R.id.item_conversation_icon);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(pushReceiver);
    }
}
