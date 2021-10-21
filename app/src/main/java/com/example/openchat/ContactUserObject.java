package com.example.openchat;

public class ContactUserObject {

    private String name, phone, uid;

    public ContactUserObject(String mName, String mPhone, String mUid) {
        this.name = mName;
        this.phone = mPhone;
        this.uid = mUid;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public void setName(String mName) {
        this.name = mName;
    }

}
