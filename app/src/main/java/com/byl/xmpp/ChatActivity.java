package com.byl.xmpp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.byl.xmpp.adapter.ChatAdapter;
import com.byl.xmpp.event.EventReceiveMsgSuccess;
import com.byl.xmpp.event.EventSendMsgSuccess;
import com.byl.xmpp.model.MsgModel;
import com.byl.xmpp.util.Const;
import com.byl.xmpp.util.LogUtil;
import com.byl.xmpp.util.PreferencesUtils;
import com.byl.xmpp.util.ToastUtil;
import com.byl.xmpp.util.XMPPConnectionUtil;
import com.byl.xmpp.util.XmppUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.minidns.record.A;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity {

    TextView tv_title;
    EditText et_content;
    Button btn_send;

    RecyclerView mRecyclerView;
    ChatAdapter chatAdapter;
    List<MsgModel> msgModelList;
    LinearLayoutManager linearLayoutManager;
    String to_user = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        EventBus.getDefault().register(this);
        to_user = getIntent().getStringExtra("user");
        PreferencesUtils.putSharePre(mContext, Const.CHATING_USER, to_user);

        tv_title = findViewById(R.id.tv_title);
        mRecyclerView = findViewById(R.id.mRecyclerView);
        et_content = findViewById(R.id.et_content);
        btn_send = findViewById(R.id.btn_send);

        tv_title.setText(to_user);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XmppUtil.sendMsg(mContext, to_user, et_content.getText().toString());
            }
        });

        msgModelList = new ArrayList<>();
        chatAdapter = new ChatAdapter(mContext, msgModelList);
        linearLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(chatAdapter);


    }


    /**
     * 发送消息成功
     *
     * @param eventSendMsgSuccess
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventSendMsgSuccess eventSendMsgSuccess) {
        if (eventSendMsgSuccess.getMessage() != null) {
            Message message = eventSendMsgSuccess.getMessage();
            MsgModel msgModel = new MsgModel();
            msgModel.setMine(true);
            msgModel.setContent(message.getBody());
            msgModelList.add(msgModel);
            chatAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(msgModelList.size() - 1);
            et_content.setText("");
        }
    }

    /**
     * 接收消息成功
     *
     * @param eventReceiveMsgSuccess
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(final EventReceiveMsgSuccess eventReceiveMsgSuccess) {
        if (eventReceiveMsgSuccess.getMessage() != null) {
            Message message = eventReceiveMsgSuccess.getMessage();
            MsgModel msgModel = new MsgModel();
            msgModel.setMine(false);
            msgModel.setContent(message.getBody());
            msgModelList.add(msgModel);
            chatAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(msgModelList.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        PreferencesUtils.putSharePre(mContext, Const.CHATING_USER, "");
    }


}
