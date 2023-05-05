package com.nitc.fyproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.nitc.fyproject.kotlin.KotlinImageActivity;

public class MainActivity extends AppCompatActivity {

    Button scan, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        scan = findViewById(R.id.scan);
        type = findViewById(R.id.type);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ScanActivity.class));
            }
        });

        type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, KotlinImageActivity.class);
//                intent.putExtra("language", "In kotlin");
//                startActivity(intent);
                startActivity(new Intent(MainActivity.this, TypeActivity.class));
            }
        });
    }
}