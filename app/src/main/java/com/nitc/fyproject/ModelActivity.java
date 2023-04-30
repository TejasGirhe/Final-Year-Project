package com.nitc.fyproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;

public class ModelActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);

        String title = getIntent().getStringExtra("word").trim().toLowerCase(Locale.ROOT);




    }

}