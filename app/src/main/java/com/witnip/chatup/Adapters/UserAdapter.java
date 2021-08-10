package com.witnip.chatup.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.witnip.chatup.Activities.ChatActivity;
import com.witnip.chatup.Models.User;
import com.witnip.chatup.R;
import com.witnip.chatup.databinding.RowConverstationBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    Context mContext;
    ArrayList<User> users;


    public UserAdapter(Context mContext, ArrayList<User> users) {
        this.mContext = mContext;
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_converstation, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        String senderID = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderID + user.getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("lastMsgTime").exists()){
                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                            long time = snapshot.child("lastMsgTime").getValue(Long.class);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            holder.binding.tvLastTime.setText(dateFormat.format(new Date(time)));
                            holder.binding.tvLastMsg.setText(lastMsg);
                        }else{
                            holder.binding.tvLastMsg.setText("Tap to chat");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.tvUsername.setText(user.getName());
        Glide.with(mContext).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.ivProfile);

        holder.itemView.setOnClickListener(v -> gotoChat(user));
    }

    private void gotoChat(User user) {
        Intent gotoChat = new Intent(mContext, ChatActivity.class);
        gotoChat.putExtra("name", user.getName());
        gotoChat.putExtra("image", user.getProfileImage());
        gotoChat.putExtra("uid", user.getUid());
        mContext.startActivity(gotoChat);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public static class UserViewHolder extends RecyclerView.ViewHolder {
        RowConverstationBinding binding;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConverstationBinding.bind(itemView);
        }
    }
}
