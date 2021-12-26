package com.example.openchat.Chat;

public class ChatObject {
    private String chatId, name, phone, chatsUid, chatsAuthUserName;

    public ChatObject(String mChadId, String mName, String mPhone) {
        this.chatId = mChadId;
        this.name = mName;
        this.phone = mPhone;
    }

    public String getChatId() {
        return chatId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getChatsUid() {
        return chatsUid;
    }

    public void setChatsUid(String chatsUid) {
        this.chatsUid = chatsUid;
    }

    public String getChatsAuthUserName() {
        return chatsAuthUserName;
    }

    public void setChatsAuthUserName(String chatsAuthUserName) {
        this.chatsAuthUserName = chatsAuthUserName;
    }

    public void setName(String mName) {
        this.name = mName;
    }

    public void setPhone(String mPhone) {
        this.phone = mPhone;
    }

    public void setChatId(String mChatId) {
        this.chatId = mChatId;
    }

}
