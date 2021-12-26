package com.example.openchat.User;

import static android.content.Context.TELEPHONY_SERVICE;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.openchat.Auth.AuthActivity;
import com.example.openchat.Utils.CountryToPhonePrefix;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserContactList {

    public static ArrayList<ContactUserObject> mUserContactList = new ArrayList<>();
    public static ArrayList<ContactUserObject> mUsersList = new ArrayList<>();
    public static ArrayList<ContactUserObject> contactUserList;
    public static HashMap<String, Integer> mAuthUserChatDB;
    public static Boolean mChatKeyExists = false;
    public static String mChatKey;
    public static UserProfile userProfile = UserProfile.getUserProfile();

    static DatabaseReference mUserDbRef;
    static String authUserUid;

//    public static Context context = MainActivity.getAppContext();


    @SuppressLint("Range")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void contactList() {
        String ISOPrefix = countryIso(), name = "", phone = "";
        @SuppressLint("Recycle") Cursor phones = AuthActivity.getAppContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if (!String.valueOf(phone.charAt(0)).equals("+")) {
                phone = ISOPrefix + phone;
            }

            ContactUserObject mContact = new ContactUserObject(name, phone, "");
            mUserContactList.add(mContact);

            genContactUserDetails(mContact);
            Log.e("code worked :", "until here");

        }
    }

    public static void genContactUserDetails(ContactUserObject mContact) {
        Log.e("contactUserDetails : ", "called here");
        mUserDbRef = FirebaseDatabase.getInstance("https://openchat-ys-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("user");
        authUserUid = mUserDbRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).getKey();
        Query query = mUserDbRef.orderByChild("phone").equalTo(mContact.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = "", name = "";
                    Log.e("authUserkey : ", authUserUid);
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        Log.e("chatMemKey : ", childSnapshot.getKey());
                        if (authUserUid.equals(childSnapshot.getKey())) continue;
                        if (childSnapshot.child("phone").getValue() != null) {
                            phone = Objects.requireNonNull(childSnapshot.child("phone").getValue()).toString();
                        }
                        if (childSnapshot.child("name").getValue() != null) {
                            name = Objects.requireNonNull(childSnapshot.child("name").getValue()).toString();
                        }

                        ContactUserObject mUser = new ContactUserObject(name, phone, childSnapshot.getKey());

                        if (name.equals(phone)) {
                            for (ContactUserObject mContactIterator : mUserContactList) {

                                if (mContactIterator.getPhone().equals(mUser.getPhone())) {
                                    mUser.setName(mContactIterator.getName());
                                    break;
                                }
                            }
                        }

//                        mUsersList.add(mUser);
                        createChat(mUser);

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        createChat();


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String countryIso() {
        String iso = "IN";


        TelephonyManager telephonyManager = (TelephonyManager) AuthActivity.getAppContext().getSystemService(TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null) {
            if (telephonyManager.getNetworkCountryIso().equals("")) {
                iso = telephonyManager.getNetworkCountryIso();
            }
        }

        return CountryToPhonePrefix.getPhone(iso);
    }

    public static void createChat(ContactUserObject contact) {
        Log.e("createChat : ", "called here");


        mAuthUserChatDB = new HashMap<>();
        DatabaseReference authUserChat = mUserDbRef.child(authUserUid).child("chat");
        DatabaseReference chatDbRef = FirebaseDatabase.getInstance().getReference().child("chat");


        authUserChat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e("Count ", "" + snapshot.getChildrenCount());
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    //checking is group before adding to authUserChatDb
                    if (childSnapshot.child("isGroup").exists()) {
                        continue;
                    }

                    mAuthUserChatDB.put(childSnapshot.getKey(), 1);

                }

                for (Map.Entry mEle : mAuthUserChatDB.entrySet()) {
                    String key = (String) mEle.getKey();
                    Integer value = (Integer) mEle.getValue();
                    Log.e("DB Key And Values : ", key + " -> " + value);
                }


            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        DatabaseReference thirdUserChat = mUserDbRef.child(contact.getUid()).child("chat");

        thirdUserChat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e("second addvaluelistener", "calling here");
                for (DataSnapshot childSnapShot : snapshot.getChildren()) {
                    if (mAuthUserChatDB.containsKey(childSnapShot.getKey())) {
                        mChatKeyExists = true;
                        mChatKey = childSnapShot.getKey();
                        chatDbRef.child(mChatKey).child("users").child(contact.getUid()).child("name").setValue((String) contact.getName());
                        chatDbRef.child(mChatKey).child("users").child(contact.getUid()).child("phone").setValue((String) contact.getPhone());
                        Log.e("createChat name : ", contact.getName());

                        break;
                    }

                }
                if (!mChatKeyExists) {
                    mChatKey = chatDbRef.push().getKey();
                    assert mChatKey != null;
                    chatDbRef.child(mChatKey).child("isGroup").setValue(false);
                    String authUserKey = mUserDbRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).getKey();
                    String thirdUserKey = mUserDbRef.child(contact.getUid()).getKey();

                    assert mChatKey != null;
                    chatDbRef.child(mChatKey).child("users").child(authUserKey).setValue(true);

                    authUserChat.child(mChatKey).setValue(false);
                    thirdUserChat.child(mChatKey).setValue(false);

                    chatDbRef.child(mChatKey).child("users").child(authUserKey).child("name").setValue(userProfile.getUserName());
                    chatDbRef.child(mChatKey).child("users").child(authUserKey).child("phone").setValue(userProfile.getUserPhone());
                    Log.e("From UserContactList : ", "name and phone");
                    Log.e(userProfile.getUserName(), userProfile.getUserPhone());

                    chatDbRef.child(mChatKey).child("users").child(thirdUserKey).child("name").setValue((String) contact.getName());
                    chatDbRef.child(mChatKey).child("users").child(thirdUserKey).child("phone").setValue((String) contact.getPhone());
                    Log.e("createChat name : ", contact.getName());


                }
                mChatKeyExists = false;
                return;


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
