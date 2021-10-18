package com.example.openchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;


public class FirstFragment extends Fragment {


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        Button mLogout;
        FloatingActionButton mContactUserList;

        View fragmentView = inflater.inflate(R.layout.fragment_first, container, false);

        mLogout = fragmentView.findViewById(R.id.logOut);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(), "You Are Logged Out", Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), AuthActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();

            }
        });

        mContactUserList = fragmentView.findViewById(R.id.contactUserListView);

        mContactUserList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContactUserListActivity.class);
                startActivity(intent);
            }
        });


        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_first, container, false);
        return fragmentView;
    }


}