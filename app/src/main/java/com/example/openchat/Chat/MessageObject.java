package com.example.openchat.Chat;

import java.util.ArrayList;

public class MessageObject {

    String messageId, message, creatorUid, creatorName, creatorPhone, timestamp, creatorNameByAuthUserContact;
    Boolean isGroupMessage, isCreatorInContact;
    ArrayList<String> mediaUrlList;

    public MessageObject(String mMessageId, String mMessage, String mCreatorUid, String mCreatorName, String mCreatorPhone,
                         ArrayList<String> mMediaUrlList, String mTimeStamp, Boolean mIsGroupMessage, Boolean mIsCreatorInContact,
                         String mCreatorNameByAuthUserContact) {

        this.messageId = mMessageId;
        this.message = mMessage;
        this.creatorUid = mCreatorUid;
        this.creatorName = mCreatorName;
        this.creatorPhone = mCreatorPhone;
        this.mediaUrlList = mMediaUrlList;
        this.timestamp = mTimeStamp;
        this.isGroupMessage = mIsGroupMessage;
        this.isCreatorInContact = mIsCreatorInContact;
        this.creatorNameByAuthUserContact = mCreatorNameByAuthUserContact;
    }

    public String getMessage() {
        return message;
    }


    public String getMessageId() {
        return messageId;
    }

    public String getCreatorUid() {
        return creatorUid;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getCreatorPhone() {
        return creatorPhone;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ArrayList<String> getMediaUrlList() {
        return mediaUrlList;
    }

    public Boolean getIsGroupMessage() {
        return isGroupMessage;
    }

    public Boolean getIsCreatorInContact() {
        return isCreatorInContact;
    }

    public String getCreatorNameByAuthUserContact() {
        return creatorNameByAuthUserContact;
    }
}
