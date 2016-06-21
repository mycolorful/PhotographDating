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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import per.yrj.photographdating.MyApplication;
import per.yrj.photographdating.R;
import per.yrj.photographdating.activities.FriendDetailActivity;
import per.yrj.photographdating.activities.InvitationActivity;
import per.yrj.photographdating.adapter.BaseViewHolder;
import per.yrj.photographdating.adapter.DividerItemDecoration;
import per.yrj.photographdating.database.FriendDao;
import per.yrj.photographdating.database.InvitationDao;
import per.yrj.photographdating.database.SQL;
import per.yrj.photographdating.debug.Debug;
import per.yrj.photographdating.debug.Debugable;
import per.yrj.photographdating.domain.Friend;
import per.yrj.photographdating.receiver.PushReceiver;

/**
 * Created by YiRenjie on 2016/5/21.
 */
public class PeopleFragment extends BaseFragment implements BaseViewHolder.OnItemClickListener, Debugable, View.OnClickListener {
    private RecyclerView rvPeople;
    private List<Friend> mData;
    private MyAdapter mAdapter;
    private View newFriendDot;

    private PushReceiver pushReceiver = new PushReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String account = intent.getStringExtra(PushReceiver.KEY_TO);
            if (MyApplication.getCurrentAccount().getAccount().equalsIgnoreCase(account)) {
                loadData();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fra_people, container, false);
        rvPeople = (RecyclerView) v.findViewById(R.id.rv_people);
        rvPeople.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvPeople.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        RelativeLayout newFriend = (RelativeLayout) v.findViewById(R.id.contact_item_new_friend);
        newFriend.setOnClickListener(this);
        newFriendDot = newFriend.findViewById(R.id.contact_item_new_friend_dot);

        //注册广播接收者
        IntentFilter filter = new IntentFilter();
        filter.addAction(PushReceiver.ACTION_REINVATION);
        filter.addAction(PushReceiver.ACTION_INVATION);
        filter.addAction(PushReceiver.ACTION_ICON_CHANGE);
        filter.addAction(PushReceiver.ACTION_NAME_CHANGE);
        getActivity().registerReceiver(pushReceiver, filter);

        mAdapter = new MyAdapter();
        loadData();
        rvPeople.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        mData = new ArrayList<>();
        FriendDao friendDao = new FriendDao(getActivity());
        Cursor cursor = friendDao.queryFriends(MyApplication.getCurrentAccount().getAccount());
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor
                    .getColumnIndex(SQL.Friend.COLUMN_NAME));

            long id = cursor.getLong(cursor
                    .getColumnIndex(SQL.Friend.COLUMN_ID));
            String account = cursor.getString(cursor
                    .getColumnIndex(SQL.Friend.COLUMN_ACCOUNT));
            Debug.i(this, account);// log
            String alpha = cursor.getString(cursor
                    .getColumnIndex(SQL.Friend.COLUMN_ALPHA));
            String area = cursor.getString(cursor
                    .getColumnIndex(SQL.Friend.COLUMN_AREA));
            String icon = cursor.getString(cursor
                    .getColumnIndex(SQL.Friend.COLUMN_ICON));
            String nickName = cursor.getString(cursor
                    .getColumnIndex(SQL.Friend.COLUMN_NICKNAME));
            String owner = cursor.getString(cursor
                    .getColumnIndex(SQL.Friend.COLUMN_OWNER));
            int sex = cursor.getInt(cursor
                    .getColumnIndex(SQL.Friend.COLUMN_SEX));
            String sign = cursor.getString(cursor
                    .getColumnIndex(SQL.Friend.COLUMN_SIGN));
            int sort = cursor.getInt(cursor
                    .getColumnIndex(SQL.Friend.COLUMN_SORT));
            Friend friend = new Friend();
            friend.setAccount(account);
            friend.setAlpha(alpha);
            friend.setArea(area);
            friend.setIcon(icon);
            friend.setId(id);
            friend.setName(name);
            friend.setNickName(nickName);
            friend.setOwner(owner);
            friend.setSex(sex);
            friend.setSign(sign);
            friend.setSort(sort);
            mData.add(friend);
        }
        InvitationDao dao = new InvitationDao(getActivity());
        boolean hasUnagree = dao.hasUnagree(MyApplication.getCurrentAccount().getAccount());
        Debug.i(this, hasUnagree+"");
        if (hasUnagree){
            newFriendDot.setVisibility(View.VISIBLE);
        }else {
            newFriendDot.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View v, int position) {

        Intent intent = new Intent(getActivity(),
                FriendDetailActivity.class);
        intent.putExtra(FriendDetailActivity.KEY_ENTER,
                FriendDetailActivity.ENTER_CONTACT);
        intent.putExtra(FriendDetailActivity.KEY_DATA, mData.get(position));
        startActivity(intent);
    }

    @Override
    public boolean shouldDebug() {
        return true;
    }

    @Override
    public String tag() {
        return "PeopleFragment";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.contact_item_new_friend:
                startActivity(new Intent(getActivity(), InvitationActivity.class));
                break;
        }
    }


    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private BaseViewHolder.OnItemClickListener mListener;

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(View.inflate(getContext(), R.layout.item_contact, null));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Friend friend = mData.get(position);
            holder.tvName.setText(friend.getAccount());
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void setOnItemClickListener(BaseViewHolder.OnItemClickListener listener) {
            mListener = listener;
        }

        class MyViewHolder extends BaseViewHolder {
            TextView tvName;
            ImageView ivIcon;

            public MyViewHolder(View itemView) {
                super(itemView, mListener);
                tvName = (TextView) itemView
                        .findViewById(R.id.item_contact_name);
                ivIcon = (ImageView) itemView.findViewById(R.id.item_contact_icon);
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(pushReceiver);
    }
}
