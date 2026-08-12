package com.example.bullsandcows;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button singlePlayerButton = findViewById(R.id.singlePlayerButton);
        Button twoPlayerButton = findViewById(R.id.twoPlayerButton);
        Button statsButton = findViewById(R.id.statsButton);

        singlePlayerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WordLengthActivity.class);
            intent.putExtra("GAME_MODE", 1);
            startActivity(intent);
        });

        twoPlayerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConnectionTypeActivity.class);
            startActivity(intent);
        });

        statsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });
    }
}