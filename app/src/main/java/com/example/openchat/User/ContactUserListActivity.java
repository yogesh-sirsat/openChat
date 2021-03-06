package com.example.openchat.User;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openchat.Group.SelectGroupMembersActivity;
import com.example.openchat.R;
import com.example.openchat.Utils.CountryToPhonePrefix;
import com.google.firebase.auth.FirebaseAuth;
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
    private RecyclerView.Adapter mContactUserListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_user_list);

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();

        // Customize the back button
//        actionBar.setHomeAsUpIndicator(R.drawable.mybutton);

        // showing the back button in action bar
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        userContactList = new ArrayList<>();
        usersList = new ArrayList<>();


        initializeRecyclerView();
        getContactList();
    }

    private void getContactList() {

        String ISOPrefix = getCountryIso();

        @SuppressLint("Recycle") Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            @SuppressLint("Range") String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            @SuppressLint("Range") String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            if (!String.valueOf(phone.charAt(0)).equals("+")) {
                phone = ISOPrefix + phone;
            }

            ContactUserObject mContact = new ContactUserObject(name, phone, "");
            userContactList.add(mContact);
            getContactUserDetails(mContact);
        }
    }

    private void getContactUserDetails(ContactUserObject mContact) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance("https://openchat-ys-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("user");
        String authUserUid = FirebaseDatabase.getInstance().getReference().child("user").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).getKey();
        Query query = mUserDB.orderByChild("phone").equalTo(mContact.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = "", name = "";
                    Log.e("authUserkey : ", authUserUid);
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        Log.e("chatMemKey : ", childSnapshot.getKey());
                        assert authUserUid != null;
                        if (authUserUid.equals(childSnapshot.getKey())) continue;
                        if (childSnapshot.child("phone").getValue() != null) {
                            phone = Objects.requireNonNull(childSnapshot.child("phone").getValue()).toString();
                        }
                        if (childSnapshot.child("name").getValue() != null) {
                            name = Objects.requireNonNull(childSnapshot.child("name").getValue()).toString();
                        }

                        ContactUserObject mUser = new ContactUserObject(name, phone, childSnapshot.getKey());

                        if (name.equals(phone)) {
                            for (ContactUserObject mContactIterator : userContactList) {

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getCountryIso() {
        String iso = "IN";

        getApplicationContext();
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null) {
            if (telephonyManager.getNetworkCountryIso().equals("")) {
                iso = telephonyManager.getNetworkCountryIso();
            }
        }

        return CountryToPhonePrefix.getPhone(iso);
    }

    private void initializeRecyclerView() {
        RecyclerView mContactUserList = findViewById(R.id.contactUserList);
        mContactUserList.setNestedScrollingEnabled(false);
        mContactUserList.setHasFixedSize(false);
        RecyclerView.LayoutManager mContactUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mContactUserList.setLayoutManager(mContactUserListLayoutManager);
        mContactUserListAdapter = new ContactUserListAdapter(usersList);

        mContactUserList.setAdapter(mContactUserListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, "New Group");
        menu.add(1, 2, 2, "New Contact");
        return super.onCreateOptionsMenu(menu);
    }

    // this event will enable the back
    // function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case 1:
                Intent newGroup = new Intent(this, SelectGroupMembersActivity.class);
                newGroup.putExtra("contactUserList", usersList);
                startActivity(newGroup);
                return true;
            case 2:
                // Creates a new Intent to insert a contact
                Intent newContact = new Intent(ContactsContract.Intents.Insert.ACTION);
                // Sets the MIME type to match the Contacts Provider
                newContact.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                startActivity(newContact);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


}