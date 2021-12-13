package com.example.openchat.Chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    String chatId, authUserUid = "", thirdUserUid = "", thirdUserName = "";
    ArrayList<String> mediaIdList = new ArrayList<>();
    ArrayList<String> mediaUriList = new ArrayList<>();
    ArrayList<MessageObject> mMessageList;
    DatabaseReference mChatDb;
    EditText mMessage;
    int totalMediaUploaded = 0;
    int PICK_IMAGE_INTENT = 1;
    private RecyclerView.Adapter mChatAdapter, mMediaAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_chat);

        authUserUid = FirebaseDatabase.getInstance().getReference().child("user").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).getKey();


        // calling the action bar
        ActionBar actionBar = getSupportActionBar();


        chatId = getIntent().getExtras().getString("chatID");

        mChatDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId);

        mChatDb.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        if (!Objects.equals(childSnapshot.getKey(), authUserUid)) {

                            thirdUserUid = childSnapshot.getKey();
                            thirdUserName = Objects.requireNonNull(childSnapshot.child("name").getValue()).toString();
                            //setting third user name as title
                            assert actionBar != null;
                            actionBar.setTitle(thirdUserName);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        // Customize the back button
//        actionBar.setHomeAsUpIndicator(R.drawable.);

        // showing the back button in action bar
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        //setting third user name as title
        actionBar.setTitle(thirdUserName);

        Button mSend = findViewById(R.id.send);
        mSend.setOnClickListener(v -> sendMessage());

        Button mAddMedia = findViewById(R.id.addMedia);
        mAddMedia.setOnClickListener(v -> openGallery());


        initializeMessage();
        initializeMedia();
        getChatMessages();
    }

    private void getChatMessages() {
        Log.e("getChatMessages:", "called here");

        mChatDb.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Log.e("addChildEventListener:", "called here");

                if (dataSnapshot.exists()) {
                    String text = "", creatorID = "", timestamp = "";
                    ArrayList<String> mediaUrlList = new ArrayList<>();
                    if (dataSnapshot.child("text").getValue() != null) {
                        text = Objects.requireNonNull(dataSnapshot.child("text").getValue()).toString();
                    }
                    if (dataSnapshot.child("creator").getValue() != null) {
                        creatorID = Objects.requireNonNull(dataSnapshot.child("creator").getValue()).toString();

                    }
                    if (dataSnapshot.child("media").getChildrenCount() > 0) {
                        for (DataSnapshot mediaSnapshot : dataSnapshot.child("media").getChildren()) {
                            mediaUrlList.add(Objects.requireNonNull(mediaSnapshot.getValue().toString()));
                        }
                    }

                    if (creatorID.length() != 0) {
                        Date date = new Date(dataSnapshot.child("timestamp").getValue(Long.class));
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("H:m d-M-yy", Locale.getDefault());
                        timestamp = simpleDateFormat.format(date);
                        MessageObject mMessage = new MessageObject(dataSnapshot.getKey(), creatorID, text, mediaUrlList, timestamp);
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
        assert messageId != null;
        final DatabaseReference newMessageDb = mChatDb.child(messageId);

        final Map newMessageMap = new HashMap<>();
        newMessageMap.put("timestamp", ServerValue.TIMESTAMP);
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
                uploadTask.addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    newMessageMap.put("/media/" + mediaIdList.get(totalMediaUploaded) + "/", uri.toString());

                    totalMediaUploaded++;
                    if (totalMediaUploaded == mediaUriList.size()) {
                        updateDatabaseWithNewMessage(newMessageDb, newMessageMap);
                    }

                }));
            }
        } else {
            if (!mMessage.getText().toString().isEmpty()) {
                updateDatabaseWithNewMessage(newMessageDb, newMessageMap);
            }
        }

        //setting chatId as true as user's started their chat
        FirebaseDatabase.getInstance().getReference().child("user").child(authUserUid).child("chat").child(chatId).setValue(true);
        FirebaseDatabase.getInstance().getReference().child("user").child(thirdUserUid).child("chat").child(chatId).setValue(true);


    }

    @SuppressLint("NotifyDataSetChanged")
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
        RecyclerView mChat = findViewById(R.id.messageList);
        mChat.setNestedScrollingEnabled(false);
        mChat.setHasFixedSize(false);
        mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mChat.setLayoutManager(mChatLayoutManager);
        mChatAdapter = new MessageAdapter(mMessageList);
        mChat.setAdapter(mChatAdapter);
        mChat.getRecycledViewPool().setMaxRecycledViews(0, 0);


    }

    private void initializeMedia() {

        mediaUriList = new ArrayList<>();
        RecyclerView mMedia = findViewById(R.id.mediaList);
        mMedia.setNestedScrollingEnabled(false);
        mMedia.setHasFixedSize(false);
        RecyclerView.LayoutManager mMediaLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mMedia.setLayoutManager(mMediaLayoutManager);
        mMediaAdapter = new MediaAdapter(getApplicationContext(), mediaUriList);
        mMedia.setAdapter(mMediaAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}