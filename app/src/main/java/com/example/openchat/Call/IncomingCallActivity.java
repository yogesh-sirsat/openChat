package com.example.openchat.Call;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.openchat.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class IncomingCallActivity extends AppCompatActivity {
    public static boolean videoCallAccept = false;
    boolean callResponse = false;
    private TextView callerNameView, callTypeView;
    private ImageView callTypeImgView;
    private FloatingActionButton callAccept, callDecline;
    private String callerName = "", incomingCallKey = "", callType = "Video", callerUid = "";
    private DatabaseReference callDbRef = FirebaseDatabase.getInstance().getReference().child("call");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_incoming_call);

        Log.e("incomingCall activity :", "called here");
        incomingCallKey = getIntent().getExtras().getString("callId");
        callerName = getIntent().getExtras().getString("callerName");
        callerUid = getIntent().getExtras().getString("callerUid");
        callType = getIntent().getExtras().getString("callType");

        callerNameView = findViewById(R.id.caller_name);

        callerNameView.setText(callerName);

        callTypeImgView = findViewById(R.id.call_type);


        if (callType.equals("Audio")) {
            callTypeImgView.setVisibility(View.GONE);
        }

        callAccept = findViewById(R.id.accept_call);
        callAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callResponse = true;
                callDbRef.child(incomingCallKey).child("status").setValue("picked");
                if (callType.equals("video")) {
                    videoCallAccept = true;
                    Intent videoCallIntent = new Intent(IncomingCallActivity.this, VideoCallActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("callId", incomingCallKey);
                    bundle.putString("callerUid", callerUid);
                    videoCallIntent.putExtras(bundle);
                    startActivity(videoCallIntent);


                } else {
                    Intent audioCallIntent = new Intent(IncomingCallActivity.this, AudioCallActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("callerUid", callerUid);
                    bundle.putString("callId", incomingCallKey);
                    audioCallIntent.putExtras(bundle);
                    startActivity(audioCallIntent);
                }
                finish();

            }
        });

        callDecline = findViewById(R.id.decline_call);
        callDecline.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                callResponse = true;
                callDbRef.child(incomingCallKey).child("status").setValue("notPicked");
                finish();
            }
        });

//        if (!callResponse){
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    //This method will be executed once the timer is over
//                    // Start your app main activity
//                    callDbRef.child(incomingCallKey).child("status").setValue("missed");
//                    finish();
//                }
//            }, 15000);
//
//        }

    }


    @Override
    public void onBackPressed() {
        callDbRef.child(incomingCallKey).child("status").setValue("missed");
        finish();
        super.onBackPressed();
    }
}