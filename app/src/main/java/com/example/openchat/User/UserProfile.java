package com.example.openchat.User;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class UserProfile {
    public static String UserName = "", UserPhone = "", UserStatus = "", UserId = "";

    public UserProfile(String mUserName, String mUserPhone, String mUserStatus, String mUserId) {
        UserName = mUserName;
        UserPhone = mUserPhone;
        UserStatus = mUserStatus;
        UserId = mUserId;
    }

    public static UserProfile getUserProfile() {
        UserId = FirebaseAuth.getInstance().getUid();
        DatabaseReference authUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(UserId);
        authUserDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    UserPhone = Objects.requireNonNull(snapshot.child("phone").getValue()).toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return new UserProfile(UserName, UserPhone, "", UserId);
    }

    public static String getUserName() {
        return UserName;
    }

    public static String getUserPhone() {
        return UserPhone;
    }

    public static String getUserStatus() {
        return UserStatus;
    }

    public static String getUserId() {
        return UserId;
    }
}
