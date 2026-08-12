package com.example.bullsandcows;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class WordLengthActivity extends AppCompatActivity {

    private int gameMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_length);

        gameMode = getIntent().getIntExtra("GAME_MODE", 1);

        Button button3 = findViewById(R.id.length3Button);
        Button button4 = findViewById(R.id.length4Button);
        Button button5 = findViewById(R.id.length5Button);
        Button button6 = findViewById(R.id.length6Button);
        Button buttonRandom = findViewById(R.id.randomButton);
        Button backButton = findViewById(R.id.backButton);

        button3.setOnClickListener(v -> startGame(3, false));
        button4.setOnClickListener(v -> startGame(4, false));
        button5.setOnClickListener(v -> startGame(5, false));
        button6.setOnClickListener(v -> startGame(6, false));
        buttonRandom.setOnClickListener(v -> startGame(0, true));
        backButton.setOnClickListener(v -> finish());
    }

    private void startGame(int length, boolean isRandom) {
        Intent intent = new Intent(WordLengthActivity.this, GameActivity.class);
        intent.putExtra("GAME_MODE", gameMode);
        intent.putExtra("WORD_LENGTH", length);
        intent.putExtra("IS_RANDOM", isRandom);
        startActivity(intent);
        finish();
    }
}