package com.example.openchat;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ContactUserListActivity extends AppCompatActivity {

    ArrayList<ContactUserObject> userContactList, usersList;
    private RecyclerView mContactUserList;
    private RecyclerView.Adapter mContactUserListAdapter;
    private RecyclerView.LayoutManager mContactUserListLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_user_list);

        userContactList = new ArrayList<>();
        usersList = new ArrayList<>();


        initializeRecyclerView();
        getContactList();
    }

    private void getContactList() {

        String ISOPrefix = getCountryIso();

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if (!String.valueOf(phone.charAt(0)).equals("+")) {
                phone = ISOPrefix + phone;
            }

            ContactUserObject mContact = new ContactUserObject(name, phone);
            userContactList.add(mContact);
            getContactUserDetails(mContact);
        }
    }

    private void getContactUserDetails(ContactUserObject mContact) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance("https://openchat-ys-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("user");
        Query query = mUserDB.orderByChild("phone").equalTo(mContact.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = "", name = "";
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (childSnapshot.child("phone").getValue() != null) {
                            phone = Objects.requireNonNull(childSnapshot.child("phone").getValue()).toString();
                        }
                        if (childSnapshot.child("name").getValue() != null) {
                            name = Objects.requireNonNull(childSnapshot.child("name").getValue()).toString();
                        }
                    }

                    ContactUserObject mUser = new ContactUserObject(name, phone);

                    if (name.equals(phone)) {
                        for (ContactUserObject mContactIterator : userContactList) {
                            Log.d("ATTENTION!!! : ", "DEBUGGING WINDOW BELOW");
                            Log.d("mUser NAME : ", name);
                            Log.d("mUser PHONE : ", phone);
                            Log.d("NAME : ", mContactIterator.getName());
                            Log.d("PHONE : ", mContactIterator.getPhone());
                            if (mContactIterator.getPhone().equals(mUser.getPhone())) {
                                mUser.setName(mContactIterator.getName());
                                break;
                            }
                        }
                    }

                    usersList.add(mUser);

                    mContactUserListAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getCountryIso() {
        String iso = "IN";

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null) {
            if (telephonyManager.getNetworkCountryIso().equals("")) {
                iso = telephonyManager.getNetworkCountryIso();
            }
        }

        return CountryToPhonePrefix.getPhone(iso);
    }

    private void initializeRecyclerView() {
        mContactUserList = findViewById(R.id.contactUserList);
        mContactUserList.setNestedScrollingEnabled(false);
        mContactUserList.setHasFixedSize(false);
        mContactUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mContactUserList.setLayoutManager(mContactUserListLayoutManager);
        mContactUserListAdapter = new ContactUserListAdapter(usersList);
        mContactUserList.setAdapter(mContactUserListAdapter);
    }
}