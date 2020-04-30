package com.byl.xmpp.event;

import org.jivesoftware.smack.packet.Message;

public class EventReceiveMsgSuccess {
    private Message message;

    public EventReceiveMsgSuccess(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
