package com.byl.xmpp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.byl.xmpp.util.Const;
import com.byl.xmpp.util.PreferencesUtils;
import com.byl.xmpp.util.ToastUtil;
import com.byl.xmpp.util.XMPPConnectionUtil;


public class RegActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        final EditText et_name = findViewById(R.id.et_name);
        final EditText et_pwd = findViewById(R.id.et_pwd);
        Button btn_reg = findViewById(R.id.btn_reg);

        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = et_name.getText().toString();
                final String pwd = et_pwd.getText().toString();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
                    return;
                }
                ToastUtil.showLongToast(mContext, "正在注册，请稍后...");
                XMPPConnectionUtil.register(mContext, name, pwd, new XMPPConnectionUtil.OnRegisterListener() {
                    @Override
                    public void onRegister(boolean isRegister, String error) {
                        ToastUtil.showLongToast(mContext, error);
                        if (isRegister) {
                            PreferencesUtils.putSharePre(mContext, Const.ACCOUNT, name);
                            PreferencesUtils.putSharePre(mContext, Const.PWD, pwd);
                            finish();
                        }
                    }
                });
            }
        });
    }


}
