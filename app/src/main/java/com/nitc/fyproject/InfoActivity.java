package com.nitc.fyproject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.airbnb.lottie.LottieAnimationView;
import com.nitc.fyproject.adapters.ImageAdapter;
import com.nitc.fyproject.classes.LoadingDialog;
import com.nitc.fyproject.classes.Models3D;
import com.nitc.fyproject.classes.OnLoaded;
import com.nitc.fyproject.classes.Thesaurus;
import com.nitc.fyproject.classes.WordInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class InfoActivity extends AppCompatActivity {
    private String[] words;
    private int currentWordIndex = 0;
    private TextToSpeech textToSpeech;
    Thesaurus.GetSynonymsTask synonymsTask;
    private LinearLayout synonymsLayout;
    private TextView synonymsTextView, def, pos, sentenceTextView;
    private ImageView previousButton, nextButton, imageView, editText, LoadImage, LoadModel, play_pause;
    String sentence = "";
    private boolean speaking;
    Thread read;
    LinearLayout defLayout, SynsLayout, posLayout;
    RecyclerView recyclerView;
    ProgressDialog progressDialog;
    OnLoaded onLoaded;
    HashMap<String, String> hash_map;
    HashMap<String, WordInfo> dict;
    private LoadingDialog loadingDialog;
    RelativeLayout info_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        getSupportActionBar().hide();
        findView();
        buttons_visibility(false);


        loadingDialog = new LoadingDialog(this, "info_loading");
        loadingDialog.show();
        info_layout.setVisibility(View.INVISIBLE);
        Handler handler = new Handler();
        Runnable loadingrunnable = new Runnable() {
            @Override
            public void run() {
                loadingDialog.cancel();
                info_layout.setVisibility(View.VISIBLE);
            }
        };
        handler.postDelayed(loadingrunnable, 5000);


        sentence = cleanSentence(getIntent().getStringExtra("recognisedText"));
        hash_map = new Models3D().getModels();

        words = sentence.split(" ");
        sentenceTextView.setText(sentence);
        speaking = false;
        dict = getInfo();
        editText.setOnClickListener(v -> {
            Intent intent = new Intent(InfoActivity.this, TypeActivity.class);
            intent.putExtra("text", sentence);
            startActivity(intent);
        });
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            } else {
                textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            textToSpeech.setLanguage(Locale.ENGLISH);
                        } else {
                            Toast.makeText(InfoActivity.this, status, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        onLoaded = arrayList -> {
            ImageAdapter adapter = new ImageAdapter(InfoActivity.this, arrayList);
            recyclerView.setAdapter(adapter);
            progressDialog.dismiss();
        };
        Runnable runnable = () -> {
            while (speaking && currentWordIndex < words.length) {
                final String word = words[currentWordIndex].trim();

                runOnUiThread(() -> {
                    highlightCurrentWord(sentence);
                    synonymsLayout.setVisibility(View.VISIBLE);
                    updateUI();
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
        };
        read = new Thread(runnable);
        play_pause.setOnClickListener(view -> {
            if (!speaking) {
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
                currentWordIndex--;
                speaking = false;
                read.interrupt();
                Picasso.get().load(R.drawable.play).into(play_pause);
            }
        });
        previousButton.setOnClickListener(v -> {
            if (currentWordIndex > 0) {
                currentWordIndex--;
                highlightCurrentWord(sentence);
                textToSpeech.speak(words[currentWordIndex], TextToSpeech.QUEUE_FLUSH, null, null);
                updateUI();
                read.interrupt();
                speaking = false;
            }

        });
        nextButton.setOnClickListener(v -> {

            if (currentWordIndex >= words.length - 1) {
                currentWordIndex = 0;
            } else {
                currentWordIndex++;
            }
            highlightCurrentWord(sentence);
            textToSpeech.speak(words[currentWordIndex], TextToSpeech.QUEUE_FLUSH, null, null);
            updateUI();
            read.interrupt();
            speaking = false;

        });
    }

    private String cleanSentence(String sentence) {
        char[] symbols = {'!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~', '\n'};
        for (char symbol : symbols) {
            sentence = sentence.replace(String.valueOf(symbol), " ");
        }
        return sentence;
    }

    private void findView() {
        sentenceTextView = findViewById(R.id.sentence_textview);
        synonymsTextView = findViewById(R.id.synonyms_textview);
        imageView = findViewById(R.id.imageview);
        previousButton = findViewById(R.id.previous_button);
        nextButton = findViewById(R.id.next_button);
        synonymsLayout = findViewById(R.id.synonyms_layout);
        def = findViewById(R.id.def);
        pos = findViewById(R.id.fos);
        LoadImage = findViewById(R.id.load_image);
        Picasso.get().load(R.drawable.picture).into(LoadImage);
        progressDialog = new ProgressDialog(InfoActivity.this);
        progressDialog.setMessage("Loading Image...");
        LoadModel = findViewById(R.id.load_model);
        editText = findViewById(R.id.edit_query);
        defLayout = findViewById(R.id.defLayout);
        synonymsLayout = findViewById(R.id.synonyms_layout);
        posLayout = findViewById(R.id.posLayout);

        play_pause = findViewById(R.id.play_pause);
        Picasso.get().load(R.drawable.play).into(play_pause);

        info_layout = findViewById(R.id.info_layout);

    }

    private void highlightCurrentWord(String sentence) {
        String currentWord = words[currentWordIndex];
        int wordStartIndex = 0;
        int spaces = 0;
        for (int i = 0; i < sentence.length(); i++) {
            if (sentence.charAt(i) == ' ') {
                spaces++;
            }

            if (spaces == currentWordIndex) {
                if (spaces != 0) {
                    wordStartIndex = i + 1;
                }
                break;
            }

        }
        int wordEndIndex = wordStartIndex + currentWord.length();

        SpannableStringBuilder builder = new SpannableStringBuilder(sentence);
        builder.setSpan(new BackgroundColorSpan(Color.BLUE), wordStartIndex, wordEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sentenceTextView.setText(builder);
    }

    @SuppressLint("SetTextI18n")
    private HashMap<String, WordInfo> getInfo() {

        HashMap<String, WordInfo> wordInfos = new HashMap<String, WordInfo>();
        for (String word : words) {
            try {
                WordInfo wordInfo = new WordInfo();
                synonymsTask = new Thesaurus.GetSynonymsTask(List -> {
                    if (List.get(0).toString().substring(1, List.get(0).toString().length() - 1).equals("")) {
                        wordInfo.setSynonyms("");
                    } else {
                        wordInfo.setSynonyms(List.get(0).toString().substring(1, List.get(0).toString().length() - 1));
                    }
                    if (List.get(2).get(0) == null) {
                        wordInfo.setPartOfSpeech("");
                    } else {
                        wordInfo.setPartOfSpeech(List.get(2).get(0));
                    }
                    if (!(List.get(2).get(0).contains("conjuction") || List.get(2).get(0).contains("preposition") || List.get(2).get(0).contains("article"))) {
                        try {
                            if (List.get(1).get(0) == null) {
                                wordInfo.setDefinition("");
                            } else {
                                wordInfo.setDefinition(List.get(1).get(0));
                            }
                        } catch (Exception e) {
                            wordInfo.setDefinition("");
                            System.out.println(e.toString());
                        }
                    }
                });
                synonymsTask.execute(word);
                wordInfos.put(word, wordInfo);
            } catch (Exception e) {
                System.out.println(e.toString());
                wordInfos.put(word, new WordInfo("", "", ""));
            }
        }
        return wordInfos;
    }

    private void loadModel(String word) {
        String title = word;
        title = title.substring(0, 1).toUpperCase() + title.substring(1);

        Intent sceneViewerIntent = new Intent(Intent.ACTION_VIEW);

        if (hash_map.containsKey(title.toLowerCase(Locale.ROOT))) {
            Uri intentUri =
                    Uri.parse("https://arvr.google.com/scene-viewer/1.0").buildUpon()
                            .appendQueryParameter("file", hash_map.get(title.toLowerCase(Locale.ROOT)))
                            .appendQueryParameter("mode", "3d_preferred")
                            .appendQueryParameter("title", title)
                            .appendQueryParameter("link", hash_map.get(title.toLowerCase(Locale.ROOT)))
                            .appendQueryParameter("enable_vertical_placement", "true")
                            .build();
            sceneViewerIntent.setData(intentUri);
            sceneViewerIntent.setPackage("com.google.ar.core");
            startActivity(sceneViewerIntent);
        } else {
            Toast.makeText(this, "Model is not available yet...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        textToSpeech.shutdown();
    }

    void buttons_visibility(boolean flag) {
        if (flag) {
            LoadModel.setVisibility(View.VISIBLE);
            LoadImage.setVisibility(View.VISIBLE);
            LoadImage.setOnClickListener(v -> {
                Intent intent = new Intent(InfoActivity.this, ImageActivity.class);
                intent.putExtra("word", words[currentWordIndex]);
                startActivity(intent);
            });
            LoadModel.setOnClickListener(v -> {
                loadModel(words[currentWordIndex]);
            });
        } else {
            LoadModel.setVisibility(View.INVISIBLE);
            LoadImage.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    void updateUI() {

        String word = words[currentWordIndex];
        WordInfo wordInfo = dict.get(word);
        ArrayList<String> pronouns = new ArrayList<>(Arrays.asList("he", "him", "her", "she"));

        String Pos = wordInfo.getPartOfSpeech();
        String Syns = wordInfo.getSynonyms();
        String Def = wordInfo.getDefinition();

        if (Pos == null || Pos.equals("")) {
            posLayout.setVisibility(View.GONE);
        } else {
            posLayout.setVisibility(View.VISIBLE);
            pos.setText(Pos.substring(0, 1).toUpperCase() + Pos.substring(1));
            buttons_visibility(Pos.contains("noun"));
//            buttons_visibility(Pos.contains("pronoun") && pronouns.contains(word.toLowerCase(Locale.ROOT)));
        }
        if (Syns == null || Syns.equals("")) {
            synonymsLayout.setVisibility(View.GONE);
        } else {
            synonymsLayout.setVisibility(View.VISIBLE);
            synonymsTextView.setText(Syns.substring(0, 1).toUpperCase() + Syns.substring(1));
        }
        if (Def == null || Def.equals("")) {
            defLayout.setVisibility(View.GONE);
        } else {
            defLayout.setVisibility(View.VISIBLE);
            def.setText(Def.substring(0, 1).toUpperCase() + Def.substring(1));
        }

    }
}