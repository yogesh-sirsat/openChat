package com.example.openchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openchat.Chat.ChatListAdapter;
import com.example.openchat.Chat.ChatObject;
import com.example.openchat.User.ContactUserListActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class FirstFragment extends Fragment {

    ArrayList<ChatObject> chatList;
    private int position = 0;
    private ArrayList<String> chatMemList = new ArrayList<>();
    private String chatsName = "", chatsPhone = "";
    private RecyclerView.Adapter mChatListAdapter;
    private View fragmentView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        fragmentView = inflater.inflate(R.layout.fragment_first, container, false);


        FloatingActionButton mContactUserList;

        Log.e("firstFragment Called", "here!!");

        mContactUserList = fragmentView.findViewById(R.id.contactUserListView);

        mContactUserList.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ContactUserListActivity.class);
            startActivity(intent);
        });

        initializeRecyclerView();
        getUserChatList();


        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_first, container, false);
        return fragmentView;
    }

    private void getUserChatList() {
        DatabaseReference mUserChatDB = FirebaseDatabase.getInstance().getReference().child("user").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("chat");
        DatabaseReference chatDB = FirebaseDatabase.getInstance().getReference().child("chat");
        String authUserUid = FirebaseDatabase.getInstance().getReference().child("user").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).getKey();

        mUserChatDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.e("user chat count : ", "" + dataSnapshot.getChildrenCount());
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        //only created chat's which marked as true will appear in chat list
                        if (childSnapshot.getValue().equals(false)) {
                            Log.e(childSnapshot.getKey(), "skipped from chat list");
                            continue;
                        }
//                        boolean exists = false;
//                        for (ChatObject chatObjectIt : chatList) {
//                            if (chatObjectIt.getChatId().equals(childSnapshot.getKey())) {
//                                exists = true;
//                            }
//                        }
//                        if (exists) continue;

                        DatabaseReference chatsUserDB = chatDB.child(Objects.requireNonNull(childSnapshot.getKey()));


                        ChatObject mChat = new ChatObject(childSnapshot.getKey(), "", "");

                        chatsUserDB.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (snapshot.child("users").exists()) {
                                    Boolean isGroup = (Boolean) snapshot.child("isGroup").getValue();
                                    if (isGroup) {
                                        chatsName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                                    } else {
                                        for (DataSnapshot chatSnapshot : snapshot.child("users").getChildren()) {
                                            if (!chatSnapshot.getKey().equals(authUserUid)) {
                                                chatMemList.add(chatSnapshot.getKey());
                                                chatsName = chatSnapshot.child("name").getValue().toString();
                                                chatsPhone = chatSnapshot.child("phone").getValue().toString();


                                            }
                                        }
                                        if (chatsName.length() == 0)
                                            chatsName = chatsPhone;
                                    }

                                    mChat.setName(chatsName);
                                    mChat.setPhone(chatsPhone);
                                    chatList.add(mChat);
                                    mChatListAdapter.notifyDataSetChanged();
                                    Log.e("from fragment name: ", mChat.getName());
                                    Log.e("from fragment phone: ", mChat.getPhone());


                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }

                        });


                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeRecyclerView() {
        chatList = new ArrayList<>();
        RecyclerView mChatList = fragmentView.findViewById(R.id.chatList);
        mChatList.setNestedScrollingEnabled(false);
        mChatList.setHasFixedSize(false);
        RecyclerView.LayoutManager mChatListLayoutManager = new LinearLayoutManager(requireActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mChatList.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new ChatListAdapter(chatList);
        mChatList.setAdapter(mChatListAdapter);
    }




}

