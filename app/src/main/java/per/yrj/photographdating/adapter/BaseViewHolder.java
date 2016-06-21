package per.yrj.photographdating.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by YiRenjie on 2016/6/17.
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private OnItemClickListener mListener;

    public BaseViewHolder(View itemView, OnItemClickListener listener) {
        super(itemView);
        mListener = listener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mListener != null){
            mListener.onItemClick(v, getAdapterPosition());
        }
    }

    public interface OnItemClickListener{
        void onItemClick(View v, int position);
    }

}
