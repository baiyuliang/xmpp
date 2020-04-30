package com.byl.xmpp.util;

import android.app.Activity;
import android.text.TextUtils;

import com.byl.xmpp.MyApplication;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;

import java.net.InetAddress;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import static com.byl.xmpp.MyApplication.chatManager;
import static com.byl.xmpp.MyApplication.xmpptcpConnection;

public class XMPPConnectionUtil {

    public static XMPPTCPConnection getInstance() {
        if (xmpptcpConnection != null && xmpptcpConnection.isConnected() && xmpptcpConnection.isAuthenticated()) {
            return xmpptcpConnection;
        }
        return null;
    }

    public static void disconnect() {
        if (chatManager != null) {
            chatManager = null;
        }
        if (xmpptcpConnection != null && xmpptcpConnection.isConnected()) {
            xmpptcpConnection.disconnect();
            xmpptcpConnection = null;
        }
    }

    public static void register(final Activity context, final String username, final String pwd, final OnRegisterListener onRegisterListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress addr = InetAddress.getByName(Const.XMPP_HOST);
                    HostnameVerifier verifier = new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return false;
                        }
                    };
                    DomainBareJid serviceName = JidCreate.domainBareFrom(Const.XMPP_DOMAIN);//Domain 服务器名称
                    XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                            .setHost(Const.XMPP_HOST)
                            .setPort(5222)
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                            .setXmppDomain(serviceName)
                            .setHostnameVerifier(verifier)
                            .setHostAddress(addr)
                            .setConnectTimeout(30000)
                            .setSendPresence(false)//离线状态登录
                            .build();
                    XMPPTCPConnection xmpptcpConnection = new XMPPTCPConnection(config);
                    //连接
                    xmpptcpConnection.connect();
                    //注册
                    if (xmpptcpConnection.isConnected()) {
                        LogUtil.e("注册>>" + username);
                        AccountManager accountManager = AccountManager.getInstance(xmpptcpConnection);
                        accountManager.sensitiveOperationOverInsecureConnection(true);
                        accountManager.createAccount(Localpart.from(username), pwd);
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onRegisterListener.onRegister(true, "注册成功");
                            }
                        });
                    } else {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onRegisterListener.onRegister(false, "注册失败，请检查您的网络");
                            }
                        });
                    }
                } catch (final Throwable e) {
                    LogUtil.e("注册失败>>" + e.getMessage());
                    String error = e.getMessage();
                    if (!TextUtils.isEmpty(error)) {
                        if (error.contains("conflict")) {
                            error = "该账号已注册";
                        }
                    } else {
                        error = "注册失败";
                    }
                    final String finalError = error;
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onRegisterListener.onRegister(false, finalError);
                        }
                    });
                }
            }
        }).start();

    }

    public static void login(final Activity context, final OnLoginListener onLoginListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    disconnect();
                    InetAddress addr = InetAddress.getByName(Const.XMPP_HOST);
                    HostnameVerifier verifier = new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return false;
                        }
                    };
                    DomainBareJid serviceName = JidCreate.domainBareFrom(Const.XMPP_DOMAIN);//Domain 服务器名称
                    XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                            .setHost(Const.XMPP_HOST)
                            .setPort(5222)
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                            .setXmppDomain(serviceName)
                            .setHostnameVerifier(verifier)
                            .setHostAddress(addr)
                            .setConnectTimeout(30000)
                            .build();
                    xmpptcpConnection = new XMPPTCPConnection(config);
                    //开启重联机制
//                    ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(xmpptcpConnection);
//                    reconnectionManager.setFixedDelay(5);
//                    reconnectionManager.enableAutomaticReconnection();
                    //连接
                    xmpptcpConnection.connect();
                    //登录
                    xmpptcpConnection.login(PreferencesUtils.getSharePreStr(context, Const.ACCOUNT), PreferencesUtils.getSharePreStr(context, Const.PWD));
                    if (xmpptcpConnection.isAuthenticated()) {//登录成功
                        LogUtil.e("登录成功>>");
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onLoginListener.onLogin(true, "");
                            }
                        });
                    } else {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onLoginListener.onLogin(false, "登录失败，请重试");
                            }
                        });
                    }
                } catch (final Throwable e) {
                    LogUtil.e(e.getMessage());
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onLoginListener.onLogin(false, "登录失败：" + e.getMessage());
                        }
                    });
                }
            }
        }).start();

    }

    public static ChatManager getChatManager() {
        if (xmpptcpConnection == null || !xmpptcpConnection.isConnected() || !xmpptcpConnection.isAuthenticated()) {
            return null;
        }
        if (chatManager == null) {
            chatManager = ChatManager.getInstanceFor(xmpptcpConnection);
        }
        return chatManager;
    }

    public interface OnRegisterListener {
        void onRegister(boolean isRegister, String error);
    }

    public interface OnLoginListener {
        void onLogin(boolean isLogin, String error);
    }


}
