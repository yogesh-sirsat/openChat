package com.example.openchat.User;

import androidx.annotation.NonNull;

import com.example.openchat.SplashScreenActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfile {
    public static String UserName = "", UserPhone = "", UserStatus = "";

    public UserProfile(String mUserName, String mUserPhone, String mUserStatus) {
        this.UserName = mUserName;
        this.UserPhone = mUserPhone;
        this.UserStatus = mUserStatus;
    }

    public static UserProfile getUserProfile() {
        DatabaseReference authUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(SplashScreenActivity.getAuthUserKey());
        authUserDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserName = snapshot.child("name").getValue().toString();
                    UserPhone = snapshot.child("phone").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return new UserProfile(UserName, UserPhone, "");
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
}
