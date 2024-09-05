package com.example.posscanner.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.posscanner.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splashscreen);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreen.this, HomeScreen.class);
            startActivity(intent);
            finish();
        },2000);
    }
}