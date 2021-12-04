package com.example.openchat.User;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
            DatabaseReference authUserChat = FirebaseDatabase.getInstance().getReference().child("user").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("chat");


            authUserChat.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.e("Count ", "" + snapshot.getChildrenCount());
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        authUserChatDB.put(childSnapshot.getKey(), 1);
                    }

                    for (Map.Entry mEle : authUserChatDB.entrySet()) {
                        String key = (String) mEle.getKey();
                        Integer value = (Integer) mEle.getValue();
                        Log.d("DB Key And Values : ", key + " -> " + value);
                    }


                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            DatabaseReference thirdUserChat = FirebaseDatabase.getInstance().getReference().child("user").child(mContactUserList.get(position).getUid()).child("chat");

            thirdUserChat.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot childSnapShot : snapshot.getChildren()) {
                        if (authUserChatDB.containsKey(childSnapShot.getKey())) {
                            Toast.makeText(v.getContext(), "Chat Is Ready!!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                    String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
                    String authUserKey = FirebaseDatabase.getInstance().getReference().child("user").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).getKey();
                    String thirdUserKey = FirebaseDatabase.getInstance().getReference().child("user").child(mContactUserList.get(position).getUid()).getKey();

                    assert key != null;
                    FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("users").child(authUserKey).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("users").child(thirdUserKey).setValue(true);

                    authUserChat.child(key).setValue(true);
                    thirdUserChat.child(key).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("user").child(mContactUserList.get(position).getUid()).child("name").setValue((String) holder.mName.getText());

                    Toast.makeText(v.getContext(), "Chat Created!!", Toast.LENGTH_SHORT).show();
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
