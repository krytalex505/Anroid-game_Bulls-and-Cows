package com.example.bullsandcows;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class LengthSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_length_select);

        Button button3 = findViewById(R.id.button_3);
        Button button4 = findViewById(R.id.button_4);
        Button button5 = findViewById(R.id.button_5);
        Button button6 = findViewById(R.id.button_6);
        Button buttonRandom = findViewById(R.id.button_random);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int length = 5;
                if (v.getId() == R.id.button_3) length = 3;
                else if (v.getId() == R.id.button_4) length = 4;
                else if (v.getId() == R.id.button_5) length = 5;
                else if (v.getId() == R.id.button_6) length = 6;
                else if (v.getId() == R.id.button_random) {
                    Random random = new Random();
                    length = random.nextInt(4) + 3;
                }

                Intent intent = new Intent(LengthSelectActivity.this, ModeSelectActivity.class);
                intent.putExtra("WORD_LENGTH", length);
                startActivity(intent);
            }
        };

        button3.setOnClickListener(listener);
        button4.setOnClickListener(listener);
        button5.setOnClickListener(listener);
        button6.setOnClickListener(listener);
        buttonRandom.setOnClickListener(listener);
    }
}