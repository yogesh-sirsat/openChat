package com.example.openchat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ContactUserListAdapter extends RecyclerView.Adapter<ContactUserListAdapter.ContactUserListViewHolder> {

    ArrayList<UserObject> mContactUserList;

    public ContactUserListAdapter(ArrayList<UserObject> ContactUserList) {
        this.mContactUserList = ContactUserList;
    }

    @NonNull
    @Override
    public ContactUserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_user_list_item, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        ContactUserListViewHolder contactUserListViewHolder = new ContactUserListViewHolder(layoutView);
        return contactUserListViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactUserListViewHolder holder, int position) {
        holder.mName.setText(mContactUserList.get(position).getName());
        holder.mPhone.setText(mContactUserList.get(position).getPhone());

    }

    @Override
    public int getItemCount() {
        return mContactUserList.size();
    }

    public class ContactUserListViewHolder extends RecyclerView.ViewHolder {
        TextView mName, mPhone;

        public ContactUserListViewHolder(View view) {
            super(view);
            mName = view.findViewById(R.id.name);
            mPhone = view.findViewById(R.id.phone);
        }
    }

}
