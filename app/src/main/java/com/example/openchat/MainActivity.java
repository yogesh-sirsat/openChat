package com.example.openchat;


import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.openchat.Auth.AuthActivity;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
//    private static final String ONESIGNAL_APP_ID = "5e9cf109-2422-458e-99e9-e15c1418e3ee";
    private TabLayout tabLayout;
    private ViewPager2 viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        getPermissions();

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

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }


}