    package com.example.bullsandcows;

    import android.content.Context;
    import android.content.SharedPreferences;
    import com.google.gson.Gson;
    import com.google.gson.reflect.TypeToken;
    import java.lang.reflect.Type;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;
    import java.util.Locale;

    public class GameLogger {

        private Context context;
        private SharedPreferences prefs;
        private Gson gson;

        public GameLogger(Context context) {
            this.context = context;
            this.prefs = context.getSharedPreferences("GameStats", Context.MODE_PRIVATE);
            this.gson = new Gson();
        }

        public void saveGame(String word, int attempts, int wordLength, boolean won, String time) {
            List<GameRecord> records = getRecords();

            GameRecord record = new GameRecord();
            record.word = word;
            record.attempts = attempts;
            record.won = won;
            record.time = time;
            record.date = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date());

            records.add(0, record);

            if (records.size() > 50) {
                records = records.subList(0, 50);
            }

            String json = gson.toJson(records);
            prefs.edit().putString("game_records", json).apply();

            updateStats(won, attempts);
        }

        private void updateStats(boolean won, int attempts) {
            int totalGames = prefs.getInt("total_games", 0) + 1;
            int wins = prefs.getInt("wins", 0) + (won ? 1 : 0);
            int totalAttempts = prefs.getInt("total_attempts", 0) + attempts;

            prefs.edit()
                    .putInt("total_games", totalGames)
                    .putInt("wins", wins)
                    .putInt("total_attempts", totalAttempts)
                    .apply();
        }

        public List<GameRecord> getRecords() {
            String json = prefs.getString("game_records", "");
            if (json.isEmpty()) {
                return new ArrayList<>();
            }

            Type type = new TypeToken<List<GameRecord>>(){}.getType();
            return gson.fromJson(json, type);
        }

        public void clearRecords() {
            prefs.edit()
                    .remove("game_records")
                    .putInt("total_games", 0)
                    .putInt("wins", 0)
                    .putInt("total_attempts", 0)
                    .apply();
        }

        public int getTotalGames() {
            return prefs.getInt("total_games", 0);
        }

        public int getWins() {
            return prefs.getInt("wins", 0);
        }

        public int getTotalAttempts() {
            return prefs.getInt("total_attempts", 0);
        }

        public static class GameRecord {
            public String word;
            public int attempts;
            public boolean won;
            public String time;
            public String date;
        }
    }