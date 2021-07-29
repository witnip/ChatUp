package com.witnip.chatup.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.witnip.chatup.R;
import com.witnip.chatup.databinding.ActivityPhoneNumberBinding;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

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
}