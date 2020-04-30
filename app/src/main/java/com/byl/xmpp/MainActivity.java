package com.byl.xmpp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.byl.xmpp.event.EventReceiveMsgSuccess;
import com.byl.xmpp.event.EventSendMsgSuccess;
import com.byl.xmpp.model.UserModel;
import com.byl.xmpp.util.Const;
import com.byl.xmpp.util.LogUtil;
import com.byl.xmpp.util.PreferencesUtils;
import com.byl.xmpp.util.ToastUtil;
import com.byl.xmpp.util.XMPPConnectionUtil;
import com.byl.xmpp.util.XmppUtil;

import androidx.annotation.NonNull;

import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jxmpp.jid.EntityBareJid;

import java.util.List;

import static com.byl.xmpp.MyApplication.MSG_NOTIFY_ID;


public class MainActivity extends BaseActivity {

    EditText et_user;
    Button btn_go;

    NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        et_user = findViewById(R.id.et_user);
        btn_go = findViewById(R.id.btn_go);

        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = et_user.getText().toString();
                if (TextUtils.isEmpty(user)) {
                    ToastUtil.showLongToast(mContext, "请输入对方账号");
                    return;
                }
                btn_go.setClickable(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final UserModel userModel = XmppUtil.getUser(user);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btn_go.setClickable(true);
                                if (userModel != null) {
                                    Intent intent = new Intent(mContext, ChatActivity.class);
                                    intent.putExtra("user", user);
                                    startActivity(intent);
                                } else {
                                    ToastUtil.showShortToast(mContext, "用户不存在");
                                }
                            }
                        });
                    }
                }).start();

            }
        });


        if (XMPPConnectionUtil.getInstance() == null) {
            handler.sendEmptyMessage(0);
        } else {
            XMPPConnectionUtil.getInstance().addConnectionListener(connectionListener);
            if (XMPPConnectionUtil.getChatManager() != null) {
                XMPPConnectionUtil.getChatManager().addIncomingListener(incomingChatMessageListener);
                XMPPConnectionUtil.getChatManager().addOutgoingListener(outgoingChatMessageListener);
            } else {
                handler.sendEmptyMessage(0);
            }

            //离线消息处理
            try {
                OfflineMessageManager offlineMessageManager = new OfflineMessageManager(XMPPConnectionUtil.getInstance());
                List<Message> messageList = offlineMessageManager.getMessages();
                for (Message message : messageList) {
                    LogUtil.e("离线消息>>" + message.getBody());
                }
                offlineMessageManager.deleteMessages();
                XMPPConnectionUtil.getInstance().sendStanza(new Presence(Presence.Type.available));
            } catch (Throwable e) {
                LogUtil.e("离线消息处理失败>>" + e.getMessage());
            }

        }

    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            super.handleMessage(msg);
            LogUtil.e("重新连接>>");
            XMPPConnectionUtil.login(mContext, new XMPPConnectionUtil.OnLoginListener() {
                @Override
                public void onLogin(boolean isLogin, String error) {
                    if (!TextUtils.isEmpty(error) && error.contains("already logged")) {
                        handler.removeMessages(0);
                        return;
                    }
                    if (isLogin) {//连接成功
                        LogUtil.e("连接成功>>");
                        XMPPConnectionUtil.getInstance().addConnectionListener(connectionListener);
                        XMPPConnectionUtil.getChatManager().addIncomingListener(incomingChatMessageListener);
                        XMPPConnectionUtil.getChatManager().addOutgoingListener(outgoingChatMessageListener);
                        handler.removeMessages(0);
                    } else {
                        LogUtil.e("连接失败>>");
                        handler.sendEmptyMessageDelayed(0, 5000);
                    }
                }
            });
        }
    };

    /**
     * 连接状态监听
     */
    ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void connected(XMPPConnection connection) {
            handler.removeMessages(0);
            LogUtil.e("connected>>");
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            handler.removeMessages(0);
            LogUtil.e("authenticated>>" + resumed);
        }

        @Override
        public void connectionClosed() {
            removeListener();
            handler.sendEmptyMessage(0);
            LogUtil.e("connectionClosed>>");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            removeListener();
            handler.sendEmptyMessage(0);
            LogUtil.e("connectionClosedOnError>>" + e.getMessage());
        }
    };

    /**
     * 接收消息监听
     */
    IncomingChatMessageListener incomingChatMessageListener = new IncomingChatMessageListener() {
        @Override
        public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
            LogUtil.e("from:" + from.asEntityBareJidString() + ">>" + message.getBody());
            String chating_user = PreferencesUtils.getSharePreStr(mContext, Const.CHATING_USER);
            //发送人是正在聊天的对象
            if (!TextUtils.isEmpty(chating_user) && (chating_user + "@" + Const.XMPP_DOMAIN).equals(from.asEntityBareJidString())) {
                EventBus.getDefault().post(new EventReceiveMsgSuccess(message));
            } else {
                showNotification(from.asEntityBareJidString(), message.getBody());
            }
        }
    };

    void showNotification(String from, String msg) {
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel("msg_channel", "msg_channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(mChannel);
            builder = new Notification.Builder(mContext, "msg_channel");
        } else {
            builder = new Notification.Builder(mContext);
        }
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle()
                .setBigContentTitle(from)
                .bigText(msg);
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
                .setTicker(from + ":" + msg)
                .setContentTitle(from)
                .setContentText(msg)
                .setStyle(bigTextStyle)
                .setAutoCancel(true);

        Notification notification = builder.build();
        notificationManager.notify(MSG_NOTIFY_ID, notification);
    }

    /**
     * 发送消息监听
     */
    OutgoingChatMessageListener outgoingChatMessageListener = new OutgoingChatMessageListener() {
        @Override
        public void newOutgoingMessage(EntityBareJid to, Message message, Chat chat) {
            LogUtil.e("to:" + to.asEntityBareJidString() + ">>" + message.getBody());
            EventBus.getDefault().post(new EventSendMsgSuccess(message));
        }
    };

    void removeListener() {
        if (XMPPConnectionUtil.getInstance() != null) {
            XMPPConnectionUtil.getInstance().removeConnectionListener(connectionListener);
        }
        if (XMPPConnectionUtil.getChatManager() != null) {
            XMPPConnectionUtil.getChatManager().removeIncomingListener(incomingChatMessageListener);
            XMPPConnectionUtil.getChatManager().removeOutgoingListener(outgoingChatMessageListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListener();
        XMPPConnectionUtil.disconnect();
    }
}
