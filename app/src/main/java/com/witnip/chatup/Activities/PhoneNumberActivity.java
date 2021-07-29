package com.witnip.chatup.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.witnip.chatup.R;
import com.witnip.chatup.databinding.ActivityPhoneNumberBinding;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            gotoMain();
        }

        binding.etPhoneNumber.requestFocus();

        binding.btnContinue.setOnClickListener(v -> {
            gotoOTPActivity();
        });
    }

    private void gotoOTPActivity() {
        Intent gotoOPTActivity = new Intent(PhoneNumberActivity.this,OTPActivity.class);
        gotoOPTActivity.putExtra("phoneNumber",binding.etPhoneNumber.getText().toString().trim());
        startActivity(gotoOPTActivity);
    }

    private void gotoMain() {
        Intent gotoMainActivity = new Intent(PhoneNumberActivity.this, MainActivity.class);
        startActivity(gotoMainActivity);
        finish();
    }
}