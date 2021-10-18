package com.example.openchat;

public class ContactUserObject {

    private String name, phone;

    public ContactUserObject(String mName, String mPhone) {
        this.name = mName;
        this.phone = mPhone;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String mName) {
        this.name = mName;
    }

}
