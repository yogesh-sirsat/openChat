package com.example.openchat.Group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openchat.R;
import com.example.openchat.User.ContactUserObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class SelectGroupMembersActivity extends AppCompatActivity {

    public static ArrayList<ContactUserObject> selectedUser;
    ArrayList<ContactUserObject> contactUserList;
    FloatingActionButton proceedNewGroup;
    private RecyclerView.Adapter userListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_members);

        selectedUser = new ArrayList<>();
        contactUserList = new ArrayList<>();
        contactUserList = getIntent().getExtras().getParcelableArrayList("contactUserList");

        proceedNewGroup = findViewById(R.id.proceed_new_group);
        proceedNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUser.size() < 2) {
                    Toast.makeText(getApplicationContext(), "Please Select At Least 2 Users", Toast.LENGTH_LONG).show();
                } else {
                    Intent createNewGroup = new Intent(SelectGroupMembersActivity.this, CreateNewGroupActivity.class);
                    createNewGroup.putExtra("Selected Users", selectedUser);
                    startActivity(createNewGroup);

                }
            }
        });

        initializeRecyclerView();


    }

    private void initializeRecyclerView() {
        RecyclerView userListRecyclerView = findViewById(R.id.userListRecyclerView);
        userListRecyclerView.setNestedScrollingEnabled(false);
        userListRecyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager userListLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        userListRecyclerView.setLayoutManager(userListLayoutManager);
        userListAdapter = new SelectMemberListAdapter(contactUserList);
        userListRecyclerView.setAdapter(userListAdapter);
    }
}