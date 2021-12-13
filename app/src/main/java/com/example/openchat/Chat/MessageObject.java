package com.example.openchat.Chat;

import java.util.ArrayList;

public class MessageObject {

    String messageId, senderId, message, timestamp;
    ArrayList<String> mediaUrlList;

    public MessageObject(String mMessageId, String mSenderId, String mMessage, ArrayList<String> mMediaUrlList, String mTimeStamp) {
        this.messageId = mMessageId;
        this.senderId = mSenderId;
        this.message = mMessage;
        this.mediaUrlList = mMediaUrlList;
        this.timestamp = mTimeStamp;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ArrayList<String> getMediaUrlList() {
        return mediaUrlList;
    }


}
