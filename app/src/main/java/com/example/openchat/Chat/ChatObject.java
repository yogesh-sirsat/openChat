package com.example.openchat.Chat;

public class ChatObject {
    private String chatId, name, phone;

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

    public void setName(String mName) {
        this.name = mName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String mPhone) {
        this.phone = mPhone;
    }

    public void setChatId(String mChatId) {
        this.chatId = mChatId;
    }

}
