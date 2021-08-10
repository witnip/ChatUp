package com.witnip.chatup.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.witnip.chatup.Adapters.TopStatusAdapter;
import com.witnip.chatup.Adapters.UserAdapter;
import com.witnip.chatup.Models.Status;
import com.witnip.chatup.Models.User;
import com.witnip.chatup.Models.UserStatus;
import com.witnip.chatup.R;
import com.witnip.chatup.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase mDatabase;
    ArrayList<User> users;
    UserAdapter userAdapter;
    TopStatusAdapter topStatusAdapter;
    ArrayList<UserStatus> userStatuses;
    ArrayList<Status> statuses;

    ProgressDialog dialog;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        mDatabase = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        userStatuses = new ArrayList<>();
        statuses = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.rvStatusList.setLayoutManager(layoutManager);

        userAdapter = new UserAdapter(this,users);
        topStatusAdapter =new TopStatusAdapter(this,userStatuses);

        binding.rvStatusList.setAdapter(topStatusAdapter);
        binding.rvUsers.setAdapter(userAdapter);

        binding.rvUsers.showShimmerAdapter();
        binding.rvStatusList.showShimmerAdapter();

        mDatabase.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    if(!user.getUid().equals(FirebaseAuth.getInstance().getUid())){
                        users.add(user);
                    }

                }
                binding.rvUsers.hideShimmerAdapter();
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabase.getReference().child("users").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        mDatabase.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userStatuses.clear();
                    for(DataSnapshot storySnapshot : snapshot.getChildren()){
                        UserStatus userStatus = new UserStatus();
                        userStatus.setName(storySnapshot.child("name").getValue(String.class));
                        userStatus.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        userStatus.setLastUpdate(storySnapshot.child("lastUpdate").getValue(Long.class));

                        for(DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()){
                            Status status = statusSnapshot.getValue(Status.class);
                            statuses.add(status);
                        }
                        userStatus.setStatuses(statuses);
                        userStatuses.add(userStatus);
                    }
                    binding.rvStatusList.hideShimmerAdapter();
                    topStatusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ActivityResultLauncher<Intent> mainActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            dialog.show();
                            Intent data = result.getData();
                            FirebaseStorage mStorage = FirebaseStorage.getInstance();
                            Date date = new Date();
                            StorageReference mStorageReference = mStorage.getReference().child("status").child(date.getTime()+"");

                            assert data != null;
                            mStorageReference.putFile(data.getData()).addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    mStorageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                        UserStatus userStatus = new UserStatus();
                                        userStatus.setName(user.getName());
                                        userStatus.setProfileImage(user.getProfileImage());
                                        userStatus.setLastUpdate(date.getTime());

                                        HashMap<String,Object> obj = new HashMap<>();
                                        obj.put("name",userStatus.getName());
                                        obj.put("profileImage",userStatus.getProfileImage());
                                        obj.put("lastUpdate",userStatus.getLastUpdate());

                                        String imageUri = uri.toString();
                                        Status status = new Status(imageUri,userStatus.getLastUpdate());

                                        mDatabase.getReference()
                                                .child("stories")
                                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                                .updateChildren(obj);

                                        mDatabase.getReference()
                                                .child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .child("statuses")
                                                .push()
                                                .setValue(status);

                                        dialog.dismiss();
                                    });
                                }
                            });
                        }
                    }
                });

        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menuStatus) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    mainActivityResultLauncher.launch(intent);
                }
                return false;
            }
        });
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSearch:
                Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menuGroups:
                Toast.makeText(this, "Groups Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menuInvite:
                Toast.makeText(this, "Invite Clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menuSetting:
                Toast.makeText(this, "Setting Clicked", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}