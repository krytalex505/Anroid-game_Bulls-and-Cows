package com.example.bullsandcows;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch soundSwitch, vibrationSwitch, darkThemeSwitch;
    private Button clearStatsButton, backButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);

        soundSwitch = findViewById(R.id.sound_switch);
        vibrationSwitch = findViewById(R.id.vibration_switch);
        darkThemeSwitch = findViewById(R.id.dark_theme_switch);
        clearStatsButton = findViewById(R.id.clear_stats_button);
        backButton = findViewById(R.id.back_button);

        // Загружаем сохранённые настройки
        soundSwitch.setChecked(prefs.getBoolean("sound", true));
        vibrationSwitch.setChecked(prefs.getBoolean("vibration", true));
        darkThemeSwitch.setChecked(prefs.getBoolean("dark_theme", false));

        // Сохраняем настройки при изменении
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sound", isChecked).apply();
            Toast.makeText(this, "Звук " + (isChecked ? "включён" : "выключен"), Toast.LENGTH_SHORT).show();
        });

        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("vibration", isChecked).apply();
            Toast.makeText(this, "Вибрация " + (isChecked ? "включена" : "выключена"), Toast.LENGTH_SHORT).show();
        });

        darkThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_theme", isChecked).apply();
            Toast.makeText(this, "Тёмная тема будет применена после перезапуска", Toast.LENGTH_LONG).show();
        });

        clearStatsButton.setOnClickListener(v -> {
            // Очищаем статистику
            getSharedPreferences("GameStats", MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(this, "Статистика очищена", Toast.LENGTH_SHORT).show();
        });

        backButton.setOnClickListener(v -> finish());
    }
}