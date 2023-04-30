package com.nitc.fyproject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class ImageActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ProgressDialog progressDialog;
    OnLoaded onLoaded;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        recyclerView = findViewById(R.id.recycler);

        String title = getIntent().getStringExtra("word").toString().trim();
        title = title.substring(0, 1).toUpperCase() + title.substring(1);
        TextView Title = findViewById(R.id.title);
        Title.setText(title);

        progressDialog = new ProgressDialog(ImageActivity.this);
        progressDialog.setMessage("Loading Image...");
        progressDialog.show();
        onLoaded = arrayList -> {
            ImageAdapter adapter = new ImageAdapter(ImageActivity.this, arrayList);
            recyclerView.setAdapter(adapter);
            progressDialog.dismiss();
        };


        new ImageGenerator(ImageActivity.this).generate(title, 600, 600, 1, onLoaded);

    }

}