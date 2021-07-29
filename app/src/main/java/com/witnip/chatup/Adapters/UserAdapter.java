package com.witnip.chatup.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.witnip.chatup.ChatActivity;
import com.witnip.chatup.Models.User;
import com.witnip.chatup.R;
import com.witnip.chatup.databinding.RowConverstationBinding;

import java.util.ArrayList;

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
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_converstation,parent,false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.binding.tvUsername.setText(user.getName());
        Glide.with(mContext).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.ivProfile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoChat(user);
            }
        });
    }

    private void gotoChat(User user) {
        Intent gotoChat = new Intent(mContext, ChatActivity.class);
        gotoChat.putExtra("name",user.getName());
        gotoChat.putExtra("uid",user.getUid());
        mContext.startActivity(gotoChat);
    }

    @Override
    public int getItemCount() {
        return 0;
    }



    public class UserViewHolder extends RecyclerView.ViewHolder {
        RowConverstationBinding binding;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConverstationBinding.bind(itemView);
        }
    }
}
