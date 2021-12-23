package com.example.openchat;


import static com.example.openchat.SplashScreenActivity.getAuthUserKey;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.openchat.Auth.AuthActivity;
import com.example.openchat.Call.IncomingCallActivity;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    //    private static final String ONESIGNAL_APP_ID = "5e9cf109-2422-458e-99e9-e15c1418e3ee";
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private String incomingCallId;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("mainActivity Called", "here!!");
//        // Enable verbose OneSignal logging to debug issues if needed.
//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
//
//        // OneSignal Initialization
//        OneSignal.initWithContext(this);
//        OneSignal.setAppId(ONESIGNAL_APP_ID);





        Fresco.initialize(this);
        setContentView(R.layout.activity_main);


        FragmentManager fragmentManager = getSupportFragmentManager();
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(fragmentManager, getLifecycle());
        viewPager = findViewById(R.id.view_pager);
        viewPagerAdapter.addFragment(new FirstFragment(), "CHAT");
        viewPagerAdapter.addFragment(new SecondFragment(), "STATUS");
        viewPagerAdapter.addFragment(new ThirdFragment(), "CALLS");
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        new TabLayoutMediator(
                tabLayout,
                viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(viewPagerAdapter.getPageTitle(position));
//                        tab.setIcon()
                    }
                }).attach();

        //incomingCall listener
        FirebaseDatabase.getInstance().getReference().child("user").child(getAuthUserKey()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e("call on liveCallDbRef : ", "called here");
                if (snapshot.child("liveCall").exists()) {

                    if (snapshot.child("liveCall").child("guest").exists()) {
                        Log.e("liveCallDbRef guest exits : ", "called here");
                        Log.e(" before condition check :", "" + Objects.requireNonNull(snapshot.child("liveCall").getValue()).toString());


                        if (Objects.requireNonNull(snapshot.child("liveCall").child("guest").getValue()).toString().equals("self")) {
                            Log.e(" THE CALL DATA CHANGED :", "" + Objects.requireNonNull(snapshot.getValue()).toString());
                            incomingCallId = Objects.requireNonNull(snapshot.child("liveCall").child("callId").getValue()).toString();
                            getCallerName();
                        }
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void getCallerName() {
        DatabaseReference incomingCallDbRef = FirebaseDatabase.getInstance().getReference().child("call").child(incomingCallId);

        incomingCallDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Intent incomingCall = new Intent(getApplicationContext(), IncomingCallActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("callId", incomingCallId);
                    bundle.putString("callerName", snapshot.child("host").child("name").getValue().toString());
                    bundle.putString("callerUid", snapshot.child("host").child("uid").getValue().toString());
                    bundle.putString("callType", snapshot.child("callType").getValue().toString());
                    incomingCall.putExtras(bundle);
                    startActivity(incomingCall);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Log Out");
        menu.add("Settings");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals("Log Out")) {

            Toast.makeText(getApplicationContext(), "You Are Logged Out", Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else if (item.getTitle().equals("Settings")) {
            Toast.makeText(this, "Settings Is Under Development", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


}