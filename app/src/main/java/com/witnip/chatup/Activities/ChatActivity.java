package com.witnip.chatup.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.witnip.chatup.Adapters.MessageAdapter;
import com.witnip.chatup.Models.Message;
import com.witnip.chatup.R;
import com.witnip.chatup.databinding.ActivityChatBinding;
import com.witnip.chatup.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Calendar;
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

        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("image");


        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        binding.tvName.setText(name);
        Glide.with(ChatActivity.this).load(profile).placeholder(R.drawable.avatar).into(binding.ivProfile);

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        mDatabase.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = snapshot.getValue(String.class);
                    if(!status.isEmpty()){
                        if(status.equals("Offline")){
                            binding.tvStatus.setVisibility(View.GONE);
                        }else {
                            binding.tvStatus.setText(status);
                            binding.tvStatus.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this,messages,senderRoom,receiverRoom);
        binding.rvMessage.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMessage.setAdapter(messageAdapter);

        mDatabase.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Message message = dataSnapshot.getValue(Message.class);
                            assert message != null;
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
                        .setValue(message).addOnSuccessListener(aVoid -> mDatabase.getReference().child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(aVoid1 -> {

                                }));
            }
        });

        ActivityResultLauncher<Intent> chatActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
            if(result.getResultCode() == Activity.RESULT_OK){
                Intent data = result.getData();
                assert data != null;
                if(data.getData() !=null){
                    Uri selectedImage = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = mStorage.getReference().child("chats").child(calendar.getTimeInMillis()+"");
                    dialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(task -> {
                        dialog.dismiss();
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String filePath = uri.toString();
                                String messageTxt = binding.etMessageBox.getText().toString().trim();
                                Date date = new Date();
                                Message message = new Message(messageTxt,senderUid,date.getTime());
                                message.setMessage("photo");
                                message.setImageUrl(filePath);

                                String randomKey = mDatabase.getReference().push().getKey();

                                HashMap<String,Object> lastMsgObj = new HashMap<>();
                                lastMsgObj.put("lastMsg",message.getMessage());
                                lastMsgObj.put("lastMsgTime",date.getTime());

                                mDatabase.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                                mDatabase.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

                                assert randomKey != null;
                                mDatabase.getReference().child("chats")
                                        .child(senderRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message).addOnSuccessListener(aVoid -> mDatabase.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message).addOnSuccessListener(aVoid1 -> {

                                        }));
                            });
                        }
                    });
                }
            }
        });

        binding.ivAttachment.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            chatActivityResultLauncher.launch(intent);
        });

        Handler handler = new Handler();

        binding.etMessageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mDatabase.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStopTyping,1000);
            }

            Runnable userStopTyping = new Runnable() {
                @Override
                public void run() {
                    mDatabase.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        assert currentId != null;
        mDatabase.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        assert currentId != null;
        mDatabase.getReference().child("presence").child(currentId).setValue("Offline");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

}