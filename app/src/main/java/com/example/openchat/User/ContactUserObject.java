package com.example.openchat.User;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactUserObject implements Parcelable {

    private String name, phone, uid;

    public static final Creator<ContactUserObject> CREATOR = new Creator<ContactUserObject>() {
        @Override
        public ContactUserObject createFromParcel(Parcel in) {
            return new ContactUserObject(in);
        }

        @Override
        public ContactUserObject[] newArray(int size) {
            return new ContactUserObject[size];
        }
    };
    private Integer selected;

    public ContactUserObject(String mName, String mPhone, String mUid) {
        this.name = mName;
        this.phone = mPhone;
        this.uid = mUid;
        this.selected = 0;
    }

    protected ContactUserObject(Parcel in) {
        name = in.readString();
        phone = in.readString();
        uid = in.readString();
        selected = in.readInt();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(uid);
        dest.writeInt(selected);
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

    public Integer getSelected() {
        return selected;
    }

    public void setSelected(Integer mSelected) {
        this.selected = mSelected;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setName(String mName) {
        this.name = mName;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


}
