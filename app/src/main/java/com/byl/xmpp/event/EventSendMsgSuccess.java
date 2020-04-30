package com.byl.xmpp.event;

import org.jivesoftware.smack.packet.Message;

public class EventSendMsgSuccess {

    private Message message;

    public EventSendMsgSuccess() {
    }

    public EventSendMsgSuccess(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

}
