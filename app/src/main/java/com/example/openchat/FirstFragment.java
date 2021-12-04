package com.example.openchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
    private ChatObject mChat;
    private String chatMemName = "", chatMemPhone = "";
    private RecyclerView.Adapter mChatListAdapter;

    private View fragmentView;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        FloatingActionButton mContactUserList;

        fragmentView = inflater.inflate(R.layout.fragment_first, container, false);

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
            @SuppressLint("notifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        boolean exists = false;
                        for (ChatObject chatObjectIt : chatList) {
                            if (chatObjectIt.getChatId().equals(childSnapshot.getKey())) {
                                exists = true;
                            }
                        }
                        if (exists) continue;

                        DatabaseReference chatsUserDB = chatDB.child(childSnapshot.getKey()).child("users");

                        mChat = new ChatObject(childSnapshot.getKey(), "", "");
                        chatList.add(mChat);

                        chatsUserDB.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (snapshot.exists()) {
                                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                                        if (!chatSnapshot.getKey().equals(authUserUid)) {
                                            chatMemList.add(chatSnapshot.getKey());


                                        }
                                    }
                                    getNamePhoneOfChatMem();

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

    private void getNamePhoneOfChatMem() {

        for (int ind = position; ind < chatMemList.size(); ind++) {


            DatabaseReference chatMemDb = FirebaseDatabase.getInstance().getReference().child("user").child(chatMemList.get(ind));

            int finalInd = ind;
            chatMemDb.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot chatMemSnapshot) {

                    chatMemName = chatMemSnapshot.child("name").getValue().toString();
                    chatMemPhone = chatMemSnapshot.child("phone").getValue().toString();


                    //getting name and phone of user's chat member's from database


                    chatList.get(finalInd).setName(chatMemName);
                    chatList.get(finalInd).setPhone(chatMemPhone);

                    mChatListAdapter.notifyDataSetChanged();


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            position++;

        }
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