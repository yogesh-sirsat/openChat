package com.example.openchat.Group;

import static com.example.openchat.SplashScreenActivity.getAuthUserKey;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.openchat.MainActivity;
import com.example.openchat.R;
import com.example.openchat.User.ContactUserObject;
import com.example.openchat.User.UserProfile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.Objects;

public class CreateNewGroupActivity extends AppCompatActivity {

    TextInputEditText groupNameInput;
    String groupName = "", newGroupChatId;
    private ArrayList<ContactUserObject> groupMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);
        groupMembers = new ArrayList<>();

        groupMembers = getIntent().getExtras().getParcelableArrayList("Selected Users");

        groupNameInput = (TextInputEditText) findViewById(R.id.group_name_text);

        FloatingActionButton proceedCreateGroup = (FloatingActionButton) findViewById(R.id.proceed_create_group);
        proceedCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Objects.requireNonNull(groupNameInput.getText()).toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Assign A Group Name", Toast.LENGTH_LONG).show();
                } else {
                    groupName = Objects.requireNonNull(groupNameInput.getText()).toString();
                    CreateNewGroup();
                }
            }
        });
    }

    private void CreateNewGroup() {
        String authUserKey = getAuthUserKey();
        DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("user");
        DatabaseReference chatDbRef = FirebaseDatabase.getInstance().getReference().child("chat");
        newGroupChatId = chatDbRef.push().getKey();
        chatDbRef.child(newGroupChatId).child("isGroup").setValue(true);
        assert newGroupChatId != null;
        DatabaseReference groupChatDbRef = chatDbRef.child(newGroupChatId);

        groupChatDbRef.child("name").setValue(groupName);
        groupChatDbRef.child("admin").setValue(authUserKey);
        groupChatDbRef.child("createdAt").setValue(ServerValue.TIMESTAMP);

        for (ContactUserObject member : groupMembers) {
            groupChatDbRef.child("users").child(member.getUid()).child("name").setValue(member.getName());
            groupChatDbRef.child("users").child(member.getUid()).child("phone").setValue(member.getPhone());
            userDbRef.child(member.getUid()).child("chat").child(newGroupChatId).setValue(true);

        }
        groupChatDbRef.child("users").child(authUserKey).child("name").setValue(UserProfile.getUserName());
        groupChatDbRef.child("users").child(authUserKey).child("phone").setValue(UserProfile.getUserPhone());
        userDbRef.child(authUserKey).child("chat").child(newGroupChatId).setValue(true);

        openNewGroup();


    }

    private void openNewGroup() {
        Intent enterInGroup = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("chatId", newGroupChatId);
        bundle.putString("chatsName", groupName);
        enterInGroup.putExtras(bundle);
        startActivity(enterInGroup);
        finish();
    }
}