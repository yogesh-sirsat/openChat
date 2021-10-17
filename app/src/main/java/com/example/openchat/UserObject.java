package com.example.openchat;

public class UserObject {

    private String name, phone;

    public UserObject(String mName, String mPhone) {
        this.name = mName;
        this.phone = mPhone;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }
}
