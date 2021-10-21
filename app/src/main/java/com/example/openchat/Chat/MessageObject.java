package com.example.openchat.Chat;

public class MessageObject {

    String messageId, senderId, message;

    public MessageObject(String mMessageId, String mSenderId, String mMessage) {
        this.messageId = mMessageId;
        this.senderId = mSenderId;
        this.message = mMessage;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }


}
