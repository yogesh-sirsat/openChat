package com.example.openchat.Chat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    RecyclerView mChat;
    ArrayList<MessageObject> mMessageList;
    RecyclerView.Adapter mChatAdapter;
    RecyclerView.LayoutManager mChatLayoutManager;
    String chatId;
    DatabaseReference mChatDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getExtras().getString("chatID");

        mChatDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId);

        Button mSend = findViewById(R.id.send);
        mSend.setOnClickListener(v -> sendMessage());

        initializeRecyclerView();
        getChatMessages();
    }

    private void getChatMessages() {
        mChatDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                if (dataSnapshot.exists()) {
                    String text = "", creatorID = "";
                    if (dataSnapshot.child("text").getValue() != null) {
                        text = Objects.requireNonNull(dataSnapshot.child("text").getValue()).toString();
                    }
                    if (dataSnapshot.child("creator").getValue() != null) {
                        creatorID = Objects.requireNonNull(dataSnapshot.child("creator").getValue()).toString();
                    }

                    MessageObject mMessage = new MessageObject(dataSnapshot.getKey(), creatorID, text);
                    mMessageList.add(mMessage);
                    mChatLayoutManager.scrollToPosition(mMessageList.size() - 1);
                    mChatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage() {
        EditText mMessage = findViewById(R.id.messageToSend);

        if (!mMessage.getText().toString().isEmpty()) {
            DatabaseReference newMessageDb = mChatDb.push();

            Map newMessageMap = new HashMap<>();
            newMessageMap.put("text", mMessage.getText().toString());
            newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());

            newMessageDb.updateChildren(newMessageMap);
        }
        mMessage.setText(null);
    }

    private void initializeRecyclerView() {
        mMessageList = new ArrayList<>();
        mChat = findViewById(R.id.messageList);
        mChat.setNestedScrollingEnabled(false);
        mChat.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mChat.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(mMessageList);
        mChat.setAdapter(mChatAdapter);
    }
}