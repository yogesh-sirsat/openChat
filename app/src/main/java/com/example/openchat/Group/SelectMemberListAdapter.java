package com.example.openchat.Group;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openchat.R;
import com.example.openchat.User.ContactUserObject;

import java.util.ArrayList;

public class SelectMemberListAdapter extends RecyclerView.Adapter<SelectMemberListAdapter.SelectMemberListViewHolder> {

    ArrayList<ContactUserObject> contactUserList;

    public SelectMemberListAdapter(ArrayList<ContactUserObject> mContactUserList) {
        this.contactUserList = mContactUserList;
    }

    @NonNull
    @Override
    public SelectMemberListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_member_item, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);
        return new SelectMemberListViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectMemberListViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.name.setText(contactUserList.get(position).getName());
        holder.phone.setText(contactUserList.get(position).getPhone());

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (contactUserList.get(position).getSelected() == 0) {
                    holder.linearLayout.setForeground(holder.linearLayout.getResources().getDrawable(R.drawable.select_item_foreground));
                    contactUserList.get(position).setSelected(1);
                    SelectGroupMembersActivity.selectedUser.add(contactUserList.get(position));
                } else {
                    holder.linearLayout.setForeground(null);
                    contactUserList.get(position).setSelected(0);
                    SelectGroupMembersActivity.selectedUser.remove(contactUserList.get(position));
                }


            }
        });
    }

    @Override
    public int getItemCount() {
        return contactUserList.size();
    }

    public static class SelectMemberListViewHolder extends RecyclerView.ViewHolder {
        public TextView name, phone;
        public LinearLayout linearLayout;

        public SelectMemberListViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name);
            this.phone = itemView.findViewById(R.id.phone);
            this.linearLayout = itemView.findViewById(R.id.select_member_layout);
        }
    }
}
