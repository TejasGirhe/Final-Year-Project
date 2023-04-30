package com.nitc.fyproject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class InfoActivity extends AppCompatActivity {
    private String[] words;
    private int currentWordIndex = 0;
    private TextToSpeech textToSpeech;
    Thesaurus.GetSynonymsTask synonymsTask;
    private TextView sentenceTextView;
    ImageView editText;
    private LinearLayout synonymsLayout;
    private TextView synonymsTextView;
    private ImageView imageView;
    private ImageView previousButton, nextButton;
    String sentence = "";
    private boolean speaking;
    TextView def, fos;
    Thread read;

    ImageView LoadImage, LoadModel;
    RecyclerView recyclerView;
    ProgressDialog progressDialog;
    OnLoaded onLoaded;
    HashMap<String, String> hash_map;
    private boolean mIsPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        sentenceTextView = findViewById(R.id.sentence_textview);
        synonymsTextView = findViewById(R.id.synonyms_textview);
        imageView = findViewById(R.id.imageview);
        previousButton = findViewById(R.id.previous_button);
        nextButton = findViewById(R.id.next_button);
        synonymsLayout = findViewById(R.id.synonyms_layout);
        def = findViewById(R.id.def);
        fos = findViewById(R.id.fos);
        LoadImage = findViewById(R.id.load_image);
        Picasso.get().load(R.drawable.picture).into(LoadImage);
        progressDialog = new ProgressDialog(InfoActivity.this);
        progressDialog.setMessage("Loading Image...");
        LoadModel = findViewById(R.id.load_model);
        editText = findViewById(R.id.edit_query);

        ImageView play_pause = findViewById(R.id.play_pause);
        Picasso.get().load(R.drawable.play).into(play_pause);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        buttons_visibility(false);

        sentence = getIntent().getStringExtra("recognisedText");
        char[] symbols = {'!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~', '\n'};
        for (char symbol : symbols) {
            sentence = sentence.replace(String.valueOf(symbol), " ");
        }

//3d models

        hash_map = new HashMap<String, String>();
        hash_map.put("Flower",  "https://raw.githubusercontent.com/TejasGirhe/3D-Model-Libraray/main/flower/scene.gltf");
        hash_map.put("Avocado", "https://raw.githubusercontent.com/TejasGirhe/3D-Models/main/avocado/scene.gltf");


        words = sentence.split(" ");
        sentenceTextView.setText(sentence);
        speaking = false;

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InfoActivity.this, TypeActivity.class);
                intent.putExtra("text", sentence);
                startActivity(intent);
            }
        });

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }else{
                    textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                textToSpeech.setLanguage(Locale.ENGLISH);
                            }
                        }
                    });
                }
            }
        });

        onLoaded = new OnLoaded() {
            @Override
            public void loaded(ArrayList<String> arrayList) {
                ImageAdapter adapter = new ImageAdapter(InfoActivity.this, arrayList);
                recyclerView.setAdapter(adapter);
                progressDialog.dismiss();
            }
        };

        Runnable runnable = new Runnable() {
            public void run() {
                while (speaking && currentWordIndex < words.length) {
                    final String word = words[currentWordIndex].trim();

                    runOnUiThread(() -> {
                        highlightCurrentWord(sentence);
                        synonymsLayout.setVisibility(View.VISIBLE);
                        getSynonyms(word);
                        updateWordUI(words[currentWordIndex]);
                    });


                    textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
                    while (textToSpeech.isSpeaking()) {
                        // Wait for the first word to finish speaking before moving on to the next one
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.out.println(e.toString());
                    }

                    currentWordIndex++;
                    if (currentWordIndex >= words.length || currentWordIndex < 0) {
                        currentWordIndex = 0;
                        runOnUiThread(() -> Picasso.get().load(R.drawable.play).into(play_pause));
                        speaking = false;
                    }
                }
            }
        };
        read = new Thread(runnable);

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!speaking) {
                    mIsPlaying = true;
                    Picasso.get().load(R.drawable.pause).into(play_pause);

                    speaking = true;
                    if (currentWordIndex >= words.length) {
                        currentWordIndex = 0;
                    }
                    if (!read.isAlive()) {
                        read = new Thread(runnable);
                        read.start();
                    } else {
                        synchronized (read) {
                            read.notify();
                        }
                    }
                } else {
                    mIsPlaying = false;
                    currentWordIndex--;
                    speaking = false;
                    read.interrupt();
                    Picasso.get().load(R.drawable.play).into(play_pause);
                }
            }
        });


        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentWordIndex > 0) {
                    currentWordIndex--;
                    highlightCurrentWord(sentence);
                    textToSpeech.speak(words[currentWordIndex], TextToSpeech.QUEUE_FLUSH, null, null);
                    updateWordUI(words[currentWordIndex]);
                    read.interrupt();
                    speaking = false;
                }

            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentWordIndex >= words.length - 1) {
                    currentWordIndex = 0;
                } else {
                    currentWordIndex++;
                }
                highlightCurrentWord(sentence);
                textToSpeech.speak(words[currentWordIndex], TextToSpeech.QUEUE_FLUSH, null, null);
                updateWordUI(words[currentWordIndex]);
                read.interrupt();
                speaking = false;

            }
        });
    }

    private void highlightCurrentWord(String sentence) {
        String[] words = sentence.split(" ");
        String currentWord = words[currentWordIndex];

        int wordStartIndex = 0;

        int spaces = 0;
        for (int i = 0; i < sentence.length(); i++) {
            if (sentence.charAt(i) == ' ') {
                spaces++;
            }

            if (spaces == currentWordIndex) {
                if (spaces == 0) {
                    wordStartIndex = 0;
                    break;
                } else {
                    wordStartIndex = i + 1;
                    break;
                }
            }

        }
        int wordEndIndex = wordStartIndex + currentWord.length();

        SpannableStringBuilder builder = new SpannableStringBuilder(sentence);
        builder.setSpan(new BackgroundColorSpan(Color.BLUE), wordStartIndex, wordEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sentenceTextView.setText(builder);
    }

    private void updateWordUI(String word) {
        getSynonyms(word);
    }

    @SuppressLint("SetTextI18n")
    private void getSynonyms(String word){
        try{
            synonymsTask = new Thesaurus.GetSynonymsTask(List -> {
                synonymsTextView.setText(List.get(0).toString().substring(1, List.get(0).toString().length() - 1));
                fos.setText(List.get(2).get(0));
                if(!(List.get(2).get(0).contains("conjuction") || List.get(2).get(0).contains("preposition") || List.get(2).get(0).contains("article")))
                {
                    try{
                        def.setText(List.get(1).get(0));
                    }catch (Exception e){
                        System.out.println(e);
                    }
                }

                String POS = fos.getText().toString();
                if(POS.contains("noun") || POS.contains("pronoun"))  // || List.get(2).get(0).contains("verb")
                {
                    if(POS.contains("pronoun"))
                    {
                        if(word.toLowerCase(Locale.ROOT).contains("he") || word.toLowerCase(Locale.ROOT).contains("him") || word.toLowerCase(Locale.ROOT).contains("her") || word.toLowerCase(Locale.ROOT).contains("she"))
                        {
                            buttons_visibility(true);
                            LoadImage.setOnClickListener(v -> {
                                Intent intent = new Intent(InfoActivity.this, ImageActivity.class);
                                intent.putExtra("word", word);
                                startActivity(intent);
                            });
                            LoadModel.setOnClickListener(v -> {
                                loadModel(word);
                            });

                        }
                        else
                        {
                            buttons_visibility(false);
                        }
                    }
                    else{
                        buttons_visibility(true);
                        LoadImage.setOnClickListener(v -> {
                            Intent intent = new Intent(InfoActivity.this, ImageActivity.class);
                            intent.putExtra("word", word);
                            startActivity(intent);
                        });
                        LoadModel.setOnClickListener(v -> {
                            loadModel(word);
                        });
                    }
                }
                else{
                    buttons_visibility(false);
                }
            });
            synonymsTask.execute(word);
        }catch (Exception e){
            System.out.println(e);
        }
    }

    private void loadModel(String word) {
        String title = word;
        title = title.substring(0, 1).toUpperCase() + title.substring(1);

        Intent sceneViewerIntent = new Intent(Intent.ACTION_VIEW);

        if(hash_map.containsKey(title)){
            Uri intentUri =
                    Uri.parse("https://arvr.google.com/scene-viewer/1.0").buildUpon()
                            .appendQueryParameter("file", hash_map.get(title))
                            .appendQueryParameter("mode", "3d_preferred")
                            .appendQueryParameter("title", title)
                            .appendQueryParameter("link", hash_map.get(title))
                            .appendQueryParameter("enable_vertical_placement", "true")
                            .build();
            sceneViewerIntent.setData(intentUri);
            sceneViewerIntent.setPackage("com.google.ar.core");
            startActivity(sceneViewerIntent);
        }else{
            Toast.makeText(this, "Model is not available yet...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        textToSpeech.shutdown();
    }

    void buttons_visibility(boolean flag){
        if(flag){
            LoadModel.setVisibility(View.VISIBLE);
            LoadImage.setVisibility(View.VISIBLE);
        }else{
            LoadModel.setVisibility(View.INVISIBLE);
            LoadImage.setVisibility(View.INVISIBLE);
        }
    }
}