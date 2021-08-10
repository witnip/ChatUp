package com.witnip.chatup.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.witnip.chatup.Models.Message;
import com.witnip.chatup.R;
import com.witnip.chatup.databinding.DeleteDialogBinding;
import com.witnip.chatup.databinding.ReceiveChatBinding;
import com.witnip.chatup.databinding.SentChatBinding;

import java.util.ArrayList;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter{

    Context mContext;
    ArrayList<Message> messages;

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    String senderRoom,receiverRoom;

    public MessageAdapter(Context mContext, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.mContext = mContext;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if(Objects.equals(FirebaseAuth.getInstance().getUid(), message.getSenderID())){
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

        int[] reactions = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(mContext)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(mContext, config, (pos) -> {
            if(holder.getClass() == SentViewHolder.class){
                SentViewHolder sentViewHolder = (SentViewHolder) holder;
                sentViewHolder.binding.ivFeeling.setImageResource(reactions[pos]);
                sentViewHolder.binding.ivFeeling.setVisibility(View.VISIBLE);
            }else{
                ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;
                receiverViewHolder.binding.ivFeeling.setImageResource(reactions[pos]);
                receiverViewHolder.binding.ivFeeling.setVisibility(View.VISIBLE);
            }

            message.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageID()).setValue(message);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageID()).setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });



        if(holder.getClass() == SentViewHolder.class){
            SentViewHolder sentViewHolder = (SentViewHolder) holder;

            if(message.getMessage().equals("photo")){
                sentViewHolder.binding.image.setVisibility(View.VISIBLE);
                sentViewHolder.binding.tvSentMessage.setVisibility(View.GONE);
                Glide.with(mContext).load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(sentViewHolder.binding.image);
            }

            sentViewHolder.binding.tvSentMessage.setText(message.getMessage());

            if(message.getFeeling()>=0){
                sentViewHolder.binding.ivFeeling.setImageResource(reactions[message.getFeeling()]);
                sentViewHolder.binding.ivFeeling.setVisibility(View.VISIBLE);
            }else {
                sentViewHolder.binding.ivFeeling.setVisibility(View.GONE);
            }

            sentViewHolder.binding.tvSentMessage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });

            sentViewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });

            sentViewHolder.binding.delete.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.delete_dialog, null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();

                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message is removed.");
                            message.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageID()).setValue(message);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(message.getMessageID()).setValue(message);
                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageID()).setValue(null);
                            dialog.dismiss();
                        }
                    });

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return true;
                }
            });
        }else{
            ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;

            if(message.getMessage().equals("photo")){
                receiverViewHolder.binding.image.setVisibility(View.VISIBLE);
                receiverViewHolder.binding.tvReceiveMessage.setVisibility(View.GONE);
                Glide.with(mContext)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(receiverViewHolder.binding.image);
            }

            receiverViewHolder.binding.tvReceiveMessage.setText(message.getMessage());

            if(message.getFeeling()>=0){
                receiverViewHolder.binding.ivFeeling.setImageResource(reactions[message.getFeeling()]);
                receiverViewHolder.binding.ivFeeling.setVisibility(View.VISIBLE);
            }else {
                receiverViewHolder.binding.ivFeeling.setVisibility(View.GONE);
            }

            receiverViewHolder.binding.tvReceiveMessage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });

            receiverViewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v,event);
                    return false;
                }
            });

            receiverViewHolder.binding.delete.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    View view = LayoutInflater.from(mContext).inflate(R.layout.delete_dialog, null);
                    DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setTitle("Delete Message")
                            .setView(binding.getRoot())
                            .create();

                    binding.everyone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            message.setMessage("This message is removed.");
                            message.setFeeling(-1);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageID()).setValue(message);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(message.getMessageID()).setValue(message);
                            dialog.dismiss();
                        }
                    });

                    binding.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("chats")
                                    .child(senderRoom)
                                    .child("messages")
                                    .child(message.getMessageID()).setValue(null);
                            dialog.dismiss();
                        }
                    });

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder{

        SentChatBinding binding;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SentChatBinding.bind(itemView);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder{

        ReceiveChatBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ReceiveChatBinding.bind(itemView);

        }
    }
}
