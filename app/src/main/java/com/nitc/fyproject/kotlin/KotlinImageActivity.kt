package com.nitc.fyproject.kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.nitc.fyproject.R

class KotlinImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_image)
        val textView = findViewById<TextView>(R.id.text)
        textView.setText(intent.getStringExtra("language"))
    }
}