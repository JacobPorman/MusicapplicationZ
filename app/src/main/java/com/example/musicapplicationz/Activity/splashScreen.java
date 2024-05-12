package com.example.musicapplicationz.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.musicapplicationz.R;
import com.google.firebase.auth.FirebaseAuth;

public class splashScreen extends AppCompatActivity {
    private Handler handler;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        firebaseAuth = FirebaseAuth.getInstance();
        handler =new Handler();
        handler.postDelayed(() -> {
            Intent intent;
            if(firebaseAuth.getCurrentUser() != null){
                intent = new Intent(getApplicationContext(), com.example.musicapplicationz.Activity.MainActivity.class);
            }else {
                intent = new Intent(getApplicationContext(), com.example.musicapplicationz.Activity.loginActivity.class);
            }
            startActivity(intent);
            finish();
        },3000);
    }
}
