package com.witnip.chatup.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.witnip.chatup.Models.User;
import com.witnip.chatup.databinding.ActivityProfileBinding;

public class Profile extends AppCompatActivity {

    ActivityProfileBinding binding;
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    FirebaseStorage mStorage;

    Uri selectedProfile;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Profile...");
        dialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        ActivityResultLauncher<Intent> profileActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            binding.ivProfile.setImageURI(data.getData());
                            selectedProfile = data.getData();
                        }
                    }
                });

        binding.ivProfile.setOnClickListener(v -> {

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            profileActivityResultLauncher.launch(intent);
        });

        binding.btnSetupProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.etName.getText().toString().trim();
                if (name.isEmpty()) {
                    binding.tvError.setText("Please enter your name");
                    return;
                }
                dialog.show();
                if (selectedProfile != null) {
                    StorageReference reference = mStorage.getReference().child("Profiles").child(mAuth.getUid());
                    reference.putFile(selectedProfile).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                String uid = mAuth.getUid();
                                String phone = mAuth.getCurrentUser().getPhoneNumber();
                                String name1 = binding.etName.getText().toString().trim();

                                User user = new User(uid, name1, phone, imageUrl);
                                mDatabase.getReference()
                                        .child("users")
                                        .child(uid)
                                        .setValue(user).addOnSuccessListener(aVoid -> {
                                    dialog.dismiss();
                                    gotoMain();
                                });
                            });
                        }
                    });
                } else {
                    String uid = mAuth.getUid();
                    String phone = mAuth.getCurrentUser().getPhoneNumber();
                    User user = new User(uid, name, phone, "NO IMAGE");
                    mDatabase.getReference()
                            .child("users")
                            .child(uid)
                            .setValue(user).addOnSuccessListener(aVoid -> {
                        dialog.dismiss();
                        gotoMain();
                    });
                }
            }
        });
    }

    private void gotoMain() {
        Intent gotoMainActivity = new Intent(Profile.this, MainActivity.class);
        startActivity(gotoMainActivity);
        finish();
    }
}