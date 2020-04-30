package com.byl.xmpp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.byl.xmpp.util.Const;
import com.byl.xmpp.util.LogUtil;
import com.byl.xmpp.util.PreferencesUtils;
import com.byl.xmpp.util.ToastUtil;
import com.byl.xmpp.util.XMPPConnectionUtil;

import org.jivesoftware.smack.AbstractXMPPConnection;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.debugger.SmackDebugger;
import org.jivesoftware.smack.debugger.SmackDebuggerFactory;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.io.IOException;
import java.net.InetAddress;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText et_name = findViewById(R.id.et_name);
        final EditText et_pwd = findViewById(R.id.et_pwd);
        Button btn_login = findViewById(R.id.btn_login);
        Button btn_reg = findViewById(R.id.btn_reg);

        PreferencesUtils.putSharePre(mContext, Const.CHATING_USER, "");
        String name = PreferencesUtils.getSharePreStr(mContext, Const.ACCOUNT);
        String pwd = PreferencesUtils.getSharePreStr(mContext, Const.PWD);
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pwd)) {
            et_name.setText(name);
            et_pwd.setText(pwd);
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = et_name.getText().toString();
                final String pwd = et_pwd.getText().toString();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
                    return;
                }
                PreferencesUtils.putSharePre(mContext, Const.ACCOUNT, name);
                PreferencesUtils.putSharePre(mContext, Const.PWD, pwd);
                ToastUtil.showLongToast(mContext, "正在登录，请稍后...");
                login();
            }
        });

        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, RegActivity.class));
            }
        });
    }


    private void login() {
        XMPPConnectionUtil.login(mContext, new XMPPConnectionUtil.OnLoginListener() {
            @Override
            public void onLogin(boolean isLogin, String error) {
                if (isLogin) {
                    ToastUtil.showShortToast(mContext, "登录成功");
                    startActivity(new Intent(mContext, MainActivity.class));
                    finish();
                } else {
                    ToastUtil.showToastOnMainUi(mContext, "登录失败，请重试");
                }
            }
        });
    }


}
