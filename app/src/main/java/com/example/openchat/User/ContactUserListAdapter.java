package com.example.openchat.User;

import android.annotation.SuppressLint;
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
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class ContactUserListAdapter extends RecyclerView.Adapter<ContactUserListAdapter.ContactUserListViewHolder> {

    ArrayList<ContactUserObject> mContactUserList;

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
            Toast.makeText(v.getContext(), "Clicked!!", Toast.LENGTH_SHORT).show();
            String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

            FirebaseDatabase.getInstance().getReference().child("user").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("chat").child(key).setValue(true);
            FirebaseDatabase.getInstance().getReference().child("user").child(mContactUserList.get(position).getUid()).child("chat").child(key).setValue(true);

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
