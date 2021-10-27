package com.example.openchat.Chat;

import java.util.ArrayList;

public class MessageObject {

    String messageId, senderId, message;
    ArrayList<String> mediaUrlList;

    public MessageObject(String mMessageId, String mSenderId, String mMessage, ArrayList<String> mMediaUrlList) {
        this.messageId = mMessageId;
        this.senderId = mSenderId;
        this.message = mMessage;
        this.mediaUrlList = mMediaUrlList;
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

    public ArrayList<String> getMediaUrlList() {
        return mediaUrlList;
    }


}
