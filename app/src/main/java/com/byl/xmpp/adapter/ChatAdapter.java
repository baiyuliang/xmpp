package com.byl.xmpp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.byl.xmpp.R;
import com.byl.xmpp.model.MsgModel;

import java.util.List;


/**
 * from为收到的消息，to为自己的消息
 *
 * @author baiyuliang
 */
@SuppressLint("NewApi")
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    List<MsgModel> list;
    Context mContext;

    public ChatAdapter(Context mContext, List<MsgModel> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_chat, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MsgModel msgModel = list.get(position);
        if (msgModel.isMine()) {
            holder.tv_right.setVisibility(View.VISIBLE);
            holder.tv_left.setVisibility(View.GONE);
            holder.tv_right.setText(msgModel.getContent());
        } else {
            holder.tv_right.setVisibility(View.GONE);
            holder.tv_left.setVisibility(View.VISIBLE);
            holder.tv_left.setText(msgModel.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_left, tv_right;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_left = itemView.findViewById(R.id.tv_left);
            tv_right = itemView.findViewById(R.id.tv_right);
        }
    }

}
