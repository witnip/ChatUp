package com.witnip.chatup.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.witnip.chatup.Models.Message;
import com.witnip.chatup.Models.User;
import com.witnip.chatup.R;
import com.witnip.chatup.databinding.ReceiveChatBinding;
import com.witnip.chatup.databinding.SentChatBinding;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter{

    Context mContext;
    ArrayList<Message> messages;

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    public MessageAdapter(Context mContext, ArrayList<Message> messages) {
        this.mContext = mContext;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderID())){
            return ITEM_SENT;
        }else{
            return ITEM_RECEIVE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SENT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.sent_chat,parent,false);
            return new SentViewHolder(view);
        }else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.receive_chat,parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if(holder.getClass() == SentViewHolder.class){

        }else{

        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class SentViewHolder extends RecyclerView.ViewHolder{

        SentChatBinding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SentChatBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{

        ReceiveChatBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ReceiveChatBinding.bind(itemView);

        }
    }
}
