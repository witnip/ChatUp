package com.witnip.chatup.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.witnip.chatup.Adapters.MessageAdapter;
import com.witnip.chatup.Models.Message;
import com.witnip.chatup.databinding.ActivityChatBinding;
import com.witnip.chatup.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    ArrayList<Message> messages;
    MessageAdapter messageAdapter;

    String senderRoom,receiverRoom;

    FirebaseDatabase mDatabase;
    FirebaseStorage mStorage;

    ProgressDialog dialog;
    String senderUid;
    String receiverUid;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = getIntent().getStringExtra("name");
        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this,messages,senderRoom,receiverRoom);
        binding.rvMessage.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMessage.setAdapter(messageAdapter);



        mDatabase = FirebaseDatabase.getInstance();

        mDatabase.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Message message = dataSnapshot.getValue(Message.class);
                            message.setMessageID(dataSnapshot.getKey());
                            messages.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });





        binding.btnSend.setOnClickListener(v -> {
            String messageTxt = binding.etMessageBox.getText().toString().trim();
            binding.etMessageBox.setText("");
            if(!TextUtils.isEmpty(messageTxt)){
                Date date = new Date();
                Message message = new Message(messageTxt,senderUid,date.getTime());
                String randomKey = mDatabase.getReference().push().getKey();

                HashMap<String,Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg",message.getMessage());
                lastMsgObj.put("lastMsgTime",date.getTime());

                mDatabase.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                mDatabase.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                mDatabase.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDatabase.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });

                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}