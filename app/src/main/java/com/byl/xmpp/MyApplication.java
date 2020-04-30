package com.byl.xmpp;

import android.app.Application;

import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

public class MyApplication extends Application {

    public static int MSG_NOTIFY_ID = 0x000001;
    public static XMPPTCPConnection xmpptcpConnection;
    public static ChatManager chatManager;

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
