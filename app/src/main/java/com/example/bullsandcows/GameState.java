package com.example.bullsandcows;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import java.util.List;

public class GameState {

    private SharedPreferences prefs;
    private Gson gson;

    public GameState() {
        this.gson = new Gson();
    }

    public void saveState(Context context, String targetWord, int currentRow,
                          List<String> bullsList, List<String> cowsList,
                          List<String> zerosList, String[][] guesses) {
        // Логика сохранения состояния игры
        prefs = context.getSharedPreferences("GameState", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Сохраняем простые данные
        editor.putString("targetWord", targetWord);
        editor.putInt("currentRow", currentRow);

        // Сохраняем списки через Gson
        editor.putString("bullsList", gson.toJson(bullsList));
        editor.putString("cowsList", gson.toJson(cowsList));
        editor.putString("zerosList", gson.toJson(zerosList));

        // Сохраняем массив guesses (нужно преобразовать в JSON)
        editor.putString("guesses", gson.toJson(guesses));

        editor.apply();
    }

    public void loadState(Context context) {
        prefs = context.getSharedPreferences("GameState", Context.MODE_PRIVATE);
        // Здесь логика загрузки состояния
    }
}