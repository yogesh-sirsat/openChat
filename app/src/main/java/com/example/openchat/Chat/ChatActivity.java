package com.example.openchat.Chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openchat.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    RecyclerView mChat, mMedia;
    ArrayList<MessageObject> mMessageList;
    ArrayList<String> mediaUriList = new ArrayList<>();
    RecyclerView.Adapter mChatAdapter, mMediaAdapter;
    RecyclerView.LayoutManager mChatLayoutManager, mMediaLayoutManager;
    String chatId;
    DatabaseReference mChatDb;
    int PICK_IMAGE_INTENT = 1;


    int totalMediaUploaded = 0;


    ArrayList<String> mediaIdList = new ArrayList<>();
    EditText mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_chat);

        chatId = getIntent().getExtras().getString("chatID");

        mChatDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId);

        Button mSend = findViewById(R.id.send);
        mSend.setOnClickListener(v -> sendMessage());

        Button mAddMedia = findViewById(R.id.addMedia);
        mAddMedia.setOnClickListener(v -> openGallery());


        initializeMessage();
        initializeMedia();
        getChatMessages();
    }

    private void getChatMessages() {

        String authUser = FirebaseDatabase.getInstance().getReference().child("user").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).getKey();
        mChatDb.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                if (dataSnapshot.exists()) {
                    String text = "";
                    final String[] creatorID = {""};
                    ArrayList<String> mediaUrlList = new ArrayList<>();
                    if (dataSnapshot.child("text").getValue() != null) {
                        text = Objects.requireNonNull(dataSnapshot.child("text").getValue()).toString();
                    }
                    if (dataSnapshot.child("creator").getValue() != null) {
                        creatorID[0] = Objects.requireNonNull(dataSnapshot.child("creator").getValue()).toString();

                        if (creatorID[0].equals(authUser)) {
                            creatorID[0] = "You";
                        } else {
                            DatabaseReference chatPersonRef = FirebaseDatabase.getInstance().getReference().child("user").child(creatorID[0]);
                            chatPersonRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    creatorID[0] = snapshot.child("name").getValue().toString();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                    if (dataSnapshot.child("media").getChildrenCount() > 0) {
                        for (DataSnapshot mediaSnapshot : dataSnapshot.child("media").getChildren()) {
                            mediaUrlList.add(Objects.requireNonNull(mediaSnapshot.getValue()).toString());
                        }
                    }

                    if (text.length() != 0 && creatorID[0].length() != 0) {
                        MessageObject mMessage = new MessageObject(dataSnapshot.getKey(), creatorID[0], text, mediaUrlList);
                        mMessageList.add(mMessage);
                        mChatLayoutManager.scrollToPosition(mMessageList.size() - 1);
                        mChatAdapter.notifyDataSetChanged();
                    }

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

        mMessage = findViewById(R.id.messageToSend);
        String messageId = mChatDb.push().getKey();
        final DatabaseReference newMessageDb = mChatDb.child(messageId);

        final Map newMessageMap = new HashMap<>();
        newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());

        if (!mMessage.getText().toString().isEmpty()) {

            newMessageMap.put("text", mMessage.getText().toString());
        }

        if (!mediaUriList.isEmpty()) {
            for (String mediaUri : mediaUriList) {
                String mediaId = newMessageDb.child("media").push().getKey();
                mediaIdList.add(mediaId);
                final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat").child(chatId).child(messageId).child(mediaId);

                UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                newMessageMap.put("/media/" + mediaIdList.get(totalMediaUploaded) + "/", uri.toString());

                                totalMediaUploaded++;
                                if (totalMediaUploaded == mediaUriList.size()) {
                                    updateDatabaseWithNewMessage(newMessageDb, newMessageMap);
                                }

                            }
                        });
                    }
                });
            }
        } else {
            if (!mMessage.getText().toString().isEmpty()) {
                updateDatabaseWithNewMessage(newMessageDb, newMessageMap);
            }
        }
    }

    private void updateDatabaseWithNewMessage(DatabaseReference newMessageDb, Map newMessageMap) {

        newMessageDb.updateChildren(newMessageMap);
        mMessage.setText(null);
        mediaUriList.clear();
        mediaIdList.clear();
        totalMediaUploaded = 0;

        mMediaAdapter.notifyDataSetChanged();


    }

    private void initializeMessage() {

        mMessageList = new ArrayList<>();
        mChat = findViewById(R.id.messageList);
        mChat.setNestedScrollingEnabled(false);
        mChat.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mChat.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(mMessageList);
        mChat.setAdapter(mChatAdapter);

    }

    private void initializeMedia() {

        mediaUriList = new ArrayList<>();
        mMedia = findViewById(R.id.mediaList);
        mMedia.setNestedScrollingEnabled(false);
        mMedia.setHasFixedSize(false);
        mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mMedia.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(getApplicationContext(), mediaUriList);
        mMedia.setAdapter(mMediaAdapter);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.e("onActivityResult", "called here!!");

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_INTENT) {
                if (data.getClipData() == null) {
                    mediaUriList.add(data.getData().toString());
                } else {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }

                mMediaAdapter.notifyDataSetChanged();
            }
        }
    }
}