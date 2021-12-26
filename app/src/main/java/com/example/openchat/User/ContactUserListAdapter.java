package com.example.openchat.User;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openchat.Chat.ChatActivity;
import com.example.openchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ContactUserListAdapter extends RecyclerView.Adapter<ContactUserListAdapter.ContactUserListViewHolder> {

    ArrayList<ContactUserObject> mContactUserList;
    HashMap<String, Integer> authUserChatDB;
    Boolean chatKeyExists = false;
    String chatKey, authUserNameByOtherUsersContact = "";
    UserProfile authUserProfile = UserProfile.getUserProfile();
    DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("user");
    DatabaseReference chatDbRef = FirebaseDatabase.getInstance().getReference().child("chat");

    public ContactUserListAdapter(ArrayList<ContactUserObject> ContactUserList) {
        this.mContactUserList = ContactUserList;
    }

    @NonNull
    @Override
    public ContactUserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_user_list_item, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        return new ContactUserListViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactUserListViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.mName.setText(mContactUserList.get(position).getName());
        holder.mPhone.setText(mContactUserList.get(position).getPhone());

        holder.mLayout.setOnClickListener(v -> {
            authUserChatDB = new HashMap<>();
            DatabaseReference authUserChat = userDbRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("chat");


            authUserChat.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        //checking is group before adding to authUserChatDb
                        if (childSnapshot.child("isGroup").exists()) {
                            continue;
                        }

                        authUserChatDB.put(childSnapshot.getKey(), 1);
                    }

                    for (Map.Entry mEle : authUserChatDB.entrySet()) {
                        String key = (String) mEle.getKey();
                        Integer value = (Integer) mEle.getValue();
                    }


                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            DatabaseReference thirdUserChat = userDbRef.child(mContactUserList.get(position).getUid()).child("chat");


            thirdUserChat.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot childSnapShot : snapshot.getChildren()) {
                        if (authUserChatDB.containsKey(childSnapShot.getKey())) {
                            chatKeyExists = true;
                            chatKey = childSnapShot.getKey();
                            chatDbRef.child(chatKey).child("users").child(mContactUserList.get(position).getUid()).child("name").setValue(holder.mName.getText());
                            chatDbRef.child(chatKey).child("users").child(mContactUserList.get(position).getUid()).child("phone").setValue(holder.mPhone.getText());
                            break;
                        }

                    }
                    if (!chatKeyExists) {
                        chatKey = chatDbRef.push().getKey();
                        assert chatKey != null;
                        chatDbRef.child(chatKey).child("isGroup").setValue(false);
                        String authUserKey = userDbRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).getKey();
                        String thirdUserKey = userDbRef.child(mContactUserList.get(position).getUid()).getKey();

                        assert chatKey != null;
                        authUserChat.child(chatKey).setValue(false);
                        thirdUserChat.child(chatKey).setValue(false);

                        chatDbRef.child(chatKey).child("users").child(authUserKey).child("name").setValue(authUserProfile.getUserName());
                        chatDbRef.child(chatKey).child("users").child(authUserKey).child("phone").setValue(authUserProfile.getUserPhone());
                        Log.e("From UserContactList : ", "name and phone");
                        Log.e(authUserProfile.getUserName(), authUserProfile.getUserPhone());

                        chatDbRef.child(chatKey).child("users").child(thirdUserKey).child("name").setValue((String) holder.mName.getText());
                        chatDbRef.child(chatKey).child("users").child(thirdUserKey).child("phone").setValue((String) holder.mPhone.getText());

                    }
                    chatKeyExists = false;
                    Intent intent = new Intent(v.getContext(), ChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("chatId", chatKey);
                    bundle.putString("chatsName", mContactUserList.get(position).getName());
                    bundle.putString("chatsPhone", mContactUserList.get(position).getPhone());
                    bundle.putString("chatsUid", mContactUserList.get(position).getUid());
                    bundle.putString("chatsAuthUserName", "");
                    intent.putExtras(bundle);
                    v.getContext().startActivity(intent);
                    return;


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        });

    }

    @Override
    public int getItemCount() {
        return mContactUserList.size();
    }

    public static class ContactUserListViewHolder extends RecyclerView.ViewHolder {
        public TextView mName, mPhone;
        public LinearLayout mLayout;

        public ContactUserListViewHolder(View view) {
            super(view);
            mName = view.findViewById(R.id.name);
            mPhone = view.findViewById(R.id.phone);
            mLayout = view.findViewById(R.id.layout);
        }
    }

}
