package com.example.openchat.Chat;


import static com.example.openchat.SplashScreenActivity.getAuthUserKey;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openchat.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    ArrayList<MessageObject> mMessageList;

    public MessageAdapter(ArrayList<MessageObject> Message) {
        this.mMessageList = Message;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        return new MessageViewHolder(layoutView);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {


        if (mMessageList.get(position).senderId.equals(getAuthUserKey())) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            params.gravity = Gravity.END;

            holder.mMessage.setBackground(holder.mMessage.getResources().getDrawable(R.drawable.right_message_bg));
            holder.mMediaLayout.setBackground(holder.mMediaLayout.getResources().getDrawable(R.drawable.right_message_bg));
            holder.mPlusImgCnt.setBackground(holder.mPlusImgCnt.getResources().getDrawable(R.drawable.img_cnt_right_bg));

            holder.mMessageLayout.setLayoutParams(params);
            holder.mMessage.setLayoutParams(params);
            holder.mTime.setLayoutParams(params);


        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            params.gravity = Gravity.START;

            holder.mMessage.setBackground(holder.mMessage.getResources().getDrawable(R.drawable.left_message_bg));
            holder.mMediaLayout.setBackground(holder.mMediaLayout.getResources().getDrawable(R.drawable.left_message_bg));
            holder.mPlusImgCnt.setBackground(holder.mPlusImgCnt.getResources().getDrawable(R.drawable.img_cnt_left_bg));

            holder.mMessageLayout.setLayoutParams(params);
            holder.mMessage.setLayoutParams(params);
            holder.mTime.setLayoutParams(params);

        }

        if (mMessageList.get(holder.getAdapterPosition()).getMediaUrlList().isEmpty()) {
            holder.mMediaLayout.setVisibility(View.GONE);
        } else {
            holder.mMedia.setImageURI(Uri.parse(mMessageList.get(holder.getAdapterPosition()).getMediaUrlList().get(0)));
            holder.mTime.setText(mMessageList.get(position).getTimestamp());
            int plusImgCnt = (mMessageList.get(holder.getAdapterPosition()).getMediaUrlList().size()) - 1;

            if (plusImgCnt >= 1) holder.mPlusImgCnt.setText("+" + plusImgCnt);
            else holder.mPlusImgCnt.setVisibility(View.GONE);

        }

        if (mMessageList.get(holder.getAdapterPosition()).getMessage().isEmpty()) {
            holder.mMessage.setVisibility(View.GONE);
        } else {
            holder.mMessage.setMaxWidth(780);
            holder.mMessage.setText(mMessageList.get(position).getMessage());
            holder.mTime.setText(mMessageList.get(position).getTimestamp());

        }


        holder.mMediaLayout.setOnClickListener(v -> new ImageViewer.Builder(v.getContext(), mMessageList.get(holder.getAdapterPosition()).getMediaUrlList())
                .setStartPosition(0)
                .show());

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView mMessage, mTime, mPlusImgCnt;
        SimpleDraweeView mMedia;
        LinearLayout mMessageLayout;
        RelativeLayout mMediaLayout;

        MessageViewHolder(View view) {
            super(view);
            mMessage = view.findViewById(R.id.message);
            mMedia = view.findViewById(R.id.media);
            mPlusImgCnt = view.findViewById(R.id.plus_image_cnt);
            mTime = view.findViewById(R.id.time);
            mMessageLayout = view.findViewById(R.id.message_layout);
            mMediaLayout = view.findViewById(R.id.media_layout);
        }
    }

}
