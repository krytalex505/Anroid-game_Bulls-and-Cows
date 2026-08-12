package com.example.bullsandcows;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView infoText = findViewById(R.id.info_text);
        Button backButton = findViewById(R.id.back_button);

        String info = "БЫКИ И КОРОВЫ\n\n" +
                "Правила игры:\n\n" +
                "1. Компьютер загадывает слово из 3-6 букв\n" +
                "2. Вы вводите свои варианты (буквы не повторяются)\n" +
                "3. После каждого хода показывается результат:\n" +
                "   Бык - буква на своём месте\n" +
                "   Корова - буква есть, но не на своём месте\n" +
                "   Ноль - буквы нет в слове\n\n" +
                "4. У вас есть 25 попыток\n" +
                "5. Серым подсвечиваются только буквы, которых нет в слове\n" +
                "6. После победы можно сохранить результат и найти слово в Google\n\n" +
                "Версия: 1.0";

        infoText.setText(info);

        backButton.setOnClickListener(v -> finish());
    }
}