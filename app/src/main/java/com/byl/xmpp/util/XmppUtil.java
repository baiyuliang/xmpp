package com.byl.xmpp.util;


import android.content.Context;
import android.text.TextUtils;

import com.byl.xmpp.model.UserModel;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class XmppUtil {

    /**
     *
     * @return
     */
    public static List<UserModel> getFirends() {
        List<UserModel> userModelList = new ArrayList<>();
        if (XMPPConnectionUtil.getInstance() != null) {
            try {
                Roster roster = Roster.getInstanceFor(XMPPConnectionUtil.getInstance());
                Set<RosterEntry> rosterEntrySet = roster.getEntries();
                LogUtil.e("查询好友列表失败>>" + rosterEntrySet.size());
                for (RosterEntry entry : rosterEntrySet) {
                    UserModel userModel = new UserModel();
                    userModel.setUsername(entry.getJid().toString());
                    userModel.setName(entry.getName());
                    userModelList.add(userModel);
                    LogUtil.e(userModel.getUsername());
                }
            } catch (Throwable e) {
                e.printStackTrace();
                LogUtil.e("查询好友列表失败>>" + e.getMessage());
            }

        }
        return userModelList;
    }


    public static UserModel getUser(String username) {
        return searchUser(XMPPConnectionUtil.getInstance(), username);
    }

    public static UserModel searchUser(XMPPTCPConnection xmpptcpConnection, String username) {
        UserModel userModel = null;
        if (xmpptcpConnection != null) {
            try {
                UserSearchManager userSearchManager = new UserSearchManager(xmpptcpConnection);
                DomainBareJid domainBareJid = JidCreate.domainBareFrom("search." + xmpptcpConnection.getXMPPServiceDomain());
                Form searchForm = userSearchManager.getSearchForm(domainBareJid);
                Form answerForm = searchForm.createAnswerForm();
                answerForm.setAnswer("Username", true);
                answerForm.setAnswer("search", username);
                ReportedData data = userSearchManager.getSearchResults(answerForm, domainBareJid);
                List<ReportedData.Row> rowList = data.getRows();
                for (ReportedData.Row row : rowList) {
                    userModel = new UserModel();
                    userModel.setUsername(row.getValues("Username").get(0).toString());
                    userModel.setName(row.getValues("Name").get(0).toString());
                    userModel.setEmail(row.getValues("Email").get(0).toString());
                    LogUtil.e(userModel.getUsername() + "-" + username);
                    if (userModel.getUsername().equals(username)) {
                        return userModel;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
                LogUtil.e("查询用户失败>>" + e.getMessage());
            }

        }
        return null;
    }

    /**
     * 发送消息
     *
     * @param context
     * @param to
     * @param msg
     */
    public static void sendMsg(Context context, String to, String msg) {
        if (TextUtils.isEmpty(msg)) {
            ToastUtil.showLongToast(context, "请输入发送内容");
            return;
        }
        if (XMPPConnectionUtil.getInstance() != null && XMPPConnectionUtil.getInstance().isConnected()) {//连接正常
            if (XMPPConnectionUtil.getInstance().isAuthenticated()) {//登录信息正常
                if (XMPPConnectionUtil.getChatManager() != null) {
                    try {
                        EntityBareJid jid = JidCreate.entityBareFrom(to + "@" + Const.XMPP_DOMAIN);
                        Chat chat = XMPPConnectionUtil.getChatManager().chatWith(jid);
                        if (chat != null) {
                            Message message = new Message();
                            message.setBody(msg);
                            chat.send(message);
                        } else {
                            ToastUtil.showShortToast(context, "发送失败，请重试");
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        ToastUtil.showShortToast(context, "发送失败：" + e.getMessage());
                        LogUtil.e("sendMsg>>" + e.getMessage());
                    }
                } else {
                    ToastUtil.showShortToast(context, "聊天登录信息失效，请重新登录");
                }
            } else {
                ToastUtil.showShortToast(context, "聊天登录信息失效，请重新登录");
            }
        } else {
            ToastUtil.showShortToast(context, "聊天服务器连接失败，请重试");
        }
    }

}
