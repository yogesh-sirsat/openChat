package com.example.openchat.Chat;

import static com.example.openchat.User.UserContactList.countryIso;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openchat.Auth.AuthActivity;
import com.example.openchat.Call.AudioCallActivity;
import com.example.openchat.Call.VideoCallActivity;
import com.example.openchat.MainActivity;
import com.example.openchat.R;
import com.example.openchat.User.UserProfile;
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

    private static final int PERMISSION_REQUEST_CODE = 1;
    public static boolean videoCallReq = false;

    ArrayList<String> mediaIdList = new ArrayList<>();
    ArrayList<String> mediaUriList = new ArrayList<>();
    ArrayList<MessageObject> mMessageList;
    HashMap<String, String> authUserContacts = new HashMap<>();

    UserProfile authUserProfile;
    DatabaseReference mChatDb, userDbRef;
    EditText mMessage;
    Menu chatMenu;
    String[] permissions = {"android.permission.CAMERA", "android.permission.RECORD_AUDIO"};
    String chatId = "", chatsName = "", authUserUid = "", otherUserUid = "", otherUserName = "", authUserNameByOtherUsersContact = "", otherUserPhone = "";

    int totalMediaUploaded = 0;
    int PICK_IMAGE_INTENT = 1;
    boolean perCamera, perRecordAudio, isOtherUserAvailable = false, isGroup = false;

    private RecyclerView.Adapter mChatAdapter, mMediaAdapter;
    private RecyclerView.LayoutManager mChatLayoutManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_chat);

        Log.e("chat activity : ", "called here");

        userDbRef = FirebaseDatabase.getInstance().getReference().child("user");
        authUserUid = FirebaseAuth.getInstance().getUid();


        // calling the action bar
        ActionBar actionBar = getSupportActionBar();


        chatId = getIntent().getExtras().getString("chatId");
        chatsName = getIntent().getExtras().getString("chatsName");
        otherUserPhone = getIntent().getExtras().getString("chatsPhone");
        otherUserName = chatsName;
        otherUserUid = getIntent().getExtras().getString("chatsUid");
        authUserNameByOtherUsersContact = getIntent().getExtras().getString("chatsAuthUserName");

        if (otherUserUid.isEmpty()) {
            isGroup = true;
            getUserContacts();
            //                    chatMenu.findItem(R.id.audio_call).setVisible(false);
//                    chatMenu.findItem(R.id.video_call).setVisible(false);
        } else {
            isGroup = false;
        }

        assert actionBar != null;
        actionBar.setTitle(chatsName);


        otherUserName = chatsName;

        mChatDb = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId);


        mChatDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                if (!snapshot.child("isGroup").exists()) {
                    if (snapshot.child("users").exists()) {
                        authUserNameByOtherUsersContact = Objects.requireNonNull(snapshot.child("users").child(authUserUid).child("name").getValue()).toString();
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
        actionBar.setDisplayHomeAsUpEnabled(true);

        //setting other end user name as title
        actionBar.setTitle(otherUserName);

        Button mSend = findViewById(R.id.send);
        mSend.setOnClickListener(v -> sendMessage());

        Button mAddMedia = findViewById(R.id.addMedia);
        mAddMedia.setOnClickListener(v -> openGallery());


        initializeMessage();
        initializeMedia();
        getChatMessages();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("Range")
    private void getUserContacts() {
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

            authUserContacts.put(phone, name);


        }

    }


    private void getChatMessages() {
        Log.e("getChatMessages:", "called here");

        mChatDb.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                Log.e("addChildEventListener:", "called here");

                if (dataSnapshot.exists()) {
                    String text = "", creatorUid = "", creatorName = "", creatorPhone = "", timestamp = "", creatorNameByAuthUserContact = "";
                    ArrayList<String> mediaUrlList = new ArrayList<>();
                    if (dataSnapshot.child("text").getValue() != null) {
                        text = Objects.requireNonNull(dataSnapshot.child("text").getValue()).toString();
                    }
                    if (dataSnapshot.child("creatorUid").getValue() != null) {
                        creatorUid = Objects.requireNonNull(dataSnapshot.child("creatorUid").getValue()).toString();

                    }
                    if (dataSnapshot.child("creatorName").getValue() != null) {
                        creatorName = Objects.requireNonNull(dataSnapshot.child("creatorName").getValue()).toString();

                    }
                    if (dataSnapshot.child("creatorPhone").getValue() != null) {
                        creatorPhone = Objects.requireNonNull(dataSnapshot.child("creatorPhone").getValue()).toString();

                    }

                    if (dataSnapshot.child("media").getChildrenCount() > 0) {
                        for (DataSnapshot mediaSnapshot : dataSnapshot.child("media").getChildren()) {
                            mediaUrlList.add(Objects.requireNonNull(mediaSnapshot.getValue().toString()));
                        }
                    }

                    if (creatorUid.length() != 0) {
                        Date date = new Date(dataSnapshot.child("timestamp").getValue(Long.class));
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd-MM-yy", Locale.getDefault());
                        timestamp = simpleDateFormat.format(date);

                        Boolean isCreatorInContact = false;
                        if (authUserContacts.containsKey(creatorPhone)) {
                            isCreatorInContact = true;
                            creatorNameByAuthUserContact = authUserContacts.get(creatorPhone);

                        }

                        MessageObject mMessage = new MessageObject(dataSnapshot.getKey(), text, creatorUid, creatorName, creatorPhone,
                                mediaUrlList, timestamp, isGroup, isCreatorInContact, creatorNameByAuthUserContact);

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
        newMessageMap.put("creatorUid", UserProfile.getUserId());
        newMessageMap.put("creatorName", UserProfile.getUserName());
        newMessageMap.put("creatorPhone", UserProfile.getUserPhone());

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
        if (!isGroup) {
            userDbRef.child(authUserUid).child("chat").child(chatId).setValue(true);
            userDbRef.child(otherUserUid).child("chat").child(chatId).setValue(true);
        }


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


    private void requestAudioCall() {
        Log.e("requestAudioCall : ", "called here");
        if (isOtherUserAvailable) {
            Intent videoCallIntent = new Intent(ChatActivity.this, AudioCallActivity.class);
            startActivity(videoCallIntent);

        } else {
            Toast.makeText(this, otherUserName + " Is Busy", Toast.LENGTH_SHORT).show();
        }
    }


    private void requestVideoCall() {
        Log.e("requestVideoCall : ", "called here");

        if (isOtherUserAvailable) {
            Intent videoCallIntent = new Intent(ChatActivity.this, VideoCallActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("otherUserName", otherUserName);
            bundle.putString("otherUserUid", otherUserUid);
            bundle.putString("authUserNameByOtherUsersContact", authUserNameByOtherUsersContact);
            bundle.putString("authUserUid", authUserUid);
            videoCallIntent.putExtras(bundle);
            videoCallReq = true;
            startActivity(videoCallIntent);
        } else {
            Toast.makeText(this, otherUserName + " Is Busy", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCallStatus() {

        DatabaseReference otherUserLiveCallStatus = FirebaseDatabase.getInstance().getReference().child("user").child(otherUserUid).child("liveCall");
        otherUserLiveCallStatus.addValueEventListener(new ValueEventListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e("other user live call status : ", "function called here");
                if (snapshot.exists()) {
                    isOtherUserAvailable = false;
                    Log.e("other user live call status : ", "1");

                } else {
                    isOtherUserAvailable = true;
                    Log.e("other user live call status : ", "2");

                }
                requestVideoCall();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    private boolean permissionsGranted() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccess = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean recordAudioAccess = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccess && recordAudioAccess) {
                        perCamera = true;
                        perRecordAudio = true;
                    } else if (recordAudioAccess) {
                        perRecordAudio = true;
                        perCamera = false;
                    } else if (cameraAccess) {
                        perCamera = true;
                        perRecordAudio = false;
                    } else {
                        perRecordAudio = false;
                        perCamera = false;
                    }

                }
                return;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;

            case R.id.audio_call:
                requestAudioCall();
                return true;

            case R.id.video_call:
                if (!permissionsGranted()) {
                    askPermissions();
                    if (perCamera && perRecordAudio) {
                        checkCallStatus();
                    } else {
                        Toast.makeText(this, "Allow For Access To Video Call", Toast.LENGTH_LONG).show();
                    }
                } else {
                    checkCallStatus();

                }


                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}