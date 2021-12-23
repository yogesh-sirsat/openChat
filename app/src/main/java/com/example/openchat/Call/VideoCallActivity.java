package com.example.openchat.Call;

import static com.example.openchat.Chat.ChatActivity.videoCallReq;
import static com.example.openchat.SplashScreenActivity.getAuthUserKey;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openchat.Chat.ChatActivity;
import com.example.openchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class VideoCallActivity extends AppCompatActivity {
    public static boolean isPeerConnected = false;
    boolean isAudio = true;
    boolean isVideo = true;
    WebView webView;
    ImageView toggleAudioBtn, toggleVideoBtn, disConnectBtn, addVideoBtn;
    String videoCallId, hostUserName, hostUid, guestUserName, guestUid, peerConnectionId, callId;

    //variables for guest
    String callerUid = "", uniqueId = "", authUserId = getAuthUserKey();

    DatabaseReference callDbRef = FirebaseDatabase.getInstance().getReference().child("call");
    DatabaseReference authUserDbRef = FirebaseDatabase.getInstance().getReference().child("user").child(getAuthUserKey());
    DatabaseReference otherUserDbRef;

    public static void onPeerConnected() {
        isPeerConnected = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        Log.e("video call Activity: ", "called here " + videoCallReq);
        webView = findViewById(R.id.video_webView);


        uniqueId = getUniqueId();

        //intent from guest
        if (IncomingCallActivity.videoCallAccept) {
            callId = getIntent().getExtras().getString("callId");
            callerUid = getIntent().getExtras().getString("callerUid");
            callDbRef.child(callId).child("peerConnectionId").setValue(uniqueId);
            callDbRef.child(callId).child("isLive").setValue(true);
            callDbRef.child(callId).child("timePeriod").child("startTime").setValue(ServerValue.TIMESTAMP);
        }

        setUpWebView();

        //intent from host
        if (ChatActivity.videoCallReq) {
            hostUserName = getIntent().getExtras().getString("authUserNameByOtherUsersContact");
            hostUid = getIntent().getExtras().getString("authUserUid");
            guestUserName = getIntent().getExtras().getString("otherUserName");
            guestUid = getIntent().getExtras().getString("otherUserUid");
            otherUserDbRef = FirebaseDatabase.getInstance().getReference().child("user").child(guestUid);

        }


        addVideoBtn = findViewById(R.id.addVideoBtn);
        addVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCallRequest();
            }
        });

        toggleAudioBtn = findViewById(R.id.toggleAudioBtn);
        toggleAudioBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                isAudio = !isAudio;
                callJavascriptFunction("javascript:toggleAudio(\"" + isAudio + "\")");
                if (isAudio) {
                    toggleAudioBtn.setImageResource(R.drawable.ic_baseline_mic_24);
                } else {
                    toggleAudioBtn.setImageResource(R.drawable.ic_baseline_mic_off_24);
                }
            }
        });

        toggleVideoBtn = findViewById(R.id.toggleVideoBtn);
        toggleVideoBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                isVideo = !isVideo;
                callJavascriptFunction("javascript:toggleVideo(\"" + isVideo + "\")");
                if (isVideo) {
                    toggleVideoBtn.setImageResource(R.drawable.ic_baseline_videocam_24);
                } else {
                    toggleVideoBtn.setImageResource(R.drawable.ic_baseline_videocam_off_24);
                }

            }
        });


    }

    private void sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, "check your internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        videoCallId = callDbRef.push().getKey();
        callId = videoCallId;
        Log.e("videoCallId : ", videoCallId);
        Log.e("hostUid : ", hostUid);
        Log.e("hostName : ", hostUserName);

        Log.e("guestId : ", guestUid);
        Log.e("guestName : ", guestUserName);

        FirebaseDatabase.getInstance().getReference().child("user").child(hostUid).child("call").child(videoCallId).setValue(true);
        FirebaseDatabase.getInstance().getReference().child("user").child(guestUid).child("call").child(videoCallId).setValue(true);

        DatabaseReference newCallDb = callDbRef.child(videoCallId);

        final Map newCallMap = new HashMap<>();
        newCallMap.put("timeStamp", ServerValue.TIMESTAMP);
        newCallMap.put("callType", "video");
        newCallDb.setValue(newCallMap);

        newCallDb.child("host").child("name").setValue(hostUserName);
        newCallDb.child("host").child("uid").setValue(hostUid);

        newCallDb.child("guest").child("name").setValue(guestUserName);
        newCallDb.child("guest").child("uid").setValue(guestUid);

        otherUserDbRef.child("liveCall").child("callId").setValue(videoCallId);
        otherUserDbRef.child("liveCall").child("host").setValue(hostUid);
        otherUserDbRef.child("liveCall").child("guest").setValue("self");

        authUserDbRef.child("liveCall").child("callId").setValue(videoCallId);
        authUserDbRef.child("liveCall").child("host").setValue("self");
        authUserDbRef.child("liveCall").child("guest").setValue(guestUid);


        listenForAnswer();


    }

    private void listenForAnswer() {
        callDbRef.child("videoCallId").addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("status").exists()) {
                    if (snapshot.child("status").getValue().equals("picked")) {
                        peerConnectionId = Objects.requireNonNull(snapshot.child("peerConnectionId").getValue()).toString();
                        callJavascriptFunction("javascript:startCall(\"" + peerConnectionId + "\")");
                    } else {
                        Toast.makeText(getApplicationContext(), guestUserName + " Is Not Responding", Toast.LENGTH_LONG).show();
                        videoCallReq = false;
                        IncomingCallActivity.videoCallAccept = false;
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void setUpWebView() {
        webView.setWebChromeClient(new WebChromeClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.addJavascriptInterface(new JavascriptInterface(this), "VideoCall");

        loadVideoCall();

    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        webView.loadUrl(filePath);

        webView.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onPageFinished(WebView view, String url) {
                initializePeer();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initializePeer() {


        callJavascriptFunction("javascript:init(\"" + uniqueId + "\")");


    }

    @NonNull
    private String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void callJavascriptFunction(String functionString) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(functionString, null);
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        if (isPeerConnected) {
            callDbRef.child(callId).child("isLive").setValue(false);
            callDbRef.child(callId).child("timePeriod").child("endTime").setValue(ServerValue.TIMESTAMP);
        }

        FirebaseDatabase.getInstance().getReference().child("user").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("liveCall").setValue(null);

        if (videoCallReq) {
            FirebaseDatabase.getInstance().getReference().child("user").child(guestUid).child("liveCall").setValue(null);
        }
        if (IncomingCallActivity.videoCallAccept) {
            FirebaseDatabase.getInstance().getReference().child("user").child(callerUid).child("liveCall").setValue(null);
        }
        videoCallReq = false;
        IncomingCallActivity.videoCallAccept = false;
        webView.loadUrl("about:blank");
        super.onDestroy();
    }
}