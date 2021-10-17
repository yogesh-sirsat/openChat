package com.example.openchat;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactUserListActivity extends AppCompatActivity {

    ArrayList<UserObject> UserContactsList;
    private RecyclerView mContactUserList;
    private RecyclerView.Adapter mContactUserListAdapter;
    private RecyclerView.LayoutManager mContactUserListLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_user_list);

        UserContactsList = new ArrayList<>();

        initializeRecyclerView();
        getContactList();
    }

    private void getContactList() {
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            UserObject mContact = new UserObject(name, phone);
            UserContactsList.add(mContact);
            mContactUserListAdapter.notifyDataSetChanged();
        }
    }

    private void initializeRecyclerView() {
        mContactUserList = findViewById(R.id.contactUserList);
        mContactUserList.setNestedScrollingEnabled(false);
        mContactUserList.setHasFixedSize(false);
        mContactUserListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mContactUserList.setLayoutManager(mContactUserListLayoutManager);
        mContactUserListAdapter = new ContactUserListAdapter(UserContactsList);
        mContactUserList.setAdapter(mContactUserListAdapter);
    }
}