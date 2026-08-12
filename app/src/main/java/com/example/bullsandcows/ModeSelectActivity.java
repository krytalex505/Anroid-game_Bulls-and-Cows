package com.example.bullsandcows;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ModeSelectActivity extends AppCompatActivity {

    private int wordLength = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);

        wordLength = getIntent().getIntExtra("WORD_LENGTH", 5);

        Button onePlayerBtn = findViewById(R.id.button_one_player);
        Button twoPlayersBtn = findViewById(R.id.button_two_players);
        Button backBtn = findViewById(R.id.back_button);

        onePlayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModeSelectActivity.this, GameActivity.class);
                intent.putExtra("WORD_LENGTH", wordLength);
                intent.putExtra("GAME_MODE", 1);
                startActivity(intent);
            }
        });

        twoPlayersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ModeSelectActivity.this, GameActivity.class);
                intent.putExtra("WORD_LENGTH", wordLength);
                intent.putExtra("GAME_MODE", 2);
                startActivity(intent);
                Toast.makeText(ModeSelectActivity.this,
                        "Режим для двух игроков: Игрок 1 (колонка А), Игрок 2 (колонка С)",
                        Toast.LENGTH_LONG).show();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}