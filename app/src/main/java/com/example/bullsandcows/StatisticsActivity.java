package com.example.bullsandcows;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private TextView totalGamesText, winsText, winRateText, avgAttemptsText;
    private LinearLayout historyContainer;
    private Button clearButton, backButton;
    private GameLogger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        logger = new GameLogger(this);

        totalGamesText = findViewById(R.id.total_games_text);
        winsText = findViewById(R.id.wins_text);
        winRateText = findViewById(R.id.win_rate_text);
        avgAttemptsText = findViewById(R.id.avg_attempts_text);
        historyContainer = findViewById(R.id.history_container);
        clearButton = findViewById(R.id.clear_button);
        backButton = findViewById(R.id.back_button);

        clearButton.setOnClickListener(v -> clearStats());
        backButton.setOnClickListener(v -> finish());

        updateStats();
        displayHistory();
    }

    private void updateStats() {
        int totalGames = logger.getTotalGames();
        int wins = logger.getWins();
        int totalAttempts = logger.getTotalAttempts();

        double winRate = totalGames > 0 ? (wins * 100.0 / totalGames) : 0;
        double avgAttempts = wins > 0 ? (double) totalAttempts / wins : 0;

        DecimalFormat df = new DecimalFormat("#.#");

        totalGamesText.setText("Всего игр: " + totalGames);
        winsText.setText("Побед: " + wins);
        winRateText.setText("Процент побед: " + df.format(winRate) + "%");
        avgAttemptsText.setText("Среднее попыток: " + df.format(avgAttempts));
    }

    private void displayHistory() {
        historyContainer.removeAllViews();

        List<GameLogger.GameRecord> records = logger.getRecords();

        if (records.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("История игр пуста");
            emptyText.setPadding(16, 16, 16, 16);
            emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            emptyText.setTextSize(16);
            emptyText.setTextColor(0xFF666666);
            historyContainer.addView(emptyText);
            return;
        }

        for (GameLogger.GameRecord record : records) {
            View recordView = getLayoutInflater().inflate(R.layout.item_history, null);

            TextView wordText = recordView.findViewById(R.id.word_text);
            TextView resultText = recordView.findViewById(R.id.result_text);
            TextView timeText = recordView.findViewById(R.id.time_text);
            TextView dateText = recordView.findViewById(R.id.date_text);

            wordText.setText(record.word);
            resultText.setText((record.won ? "✅" : "❌") + " " + record.attempts + " поп.");
            timeText.setText(record.time);
            dateText.setText(record.date);

            historyContainer.addView(recordView);
        }
    }

    private void clearStats() {
        logger.clearRecords();
        updateStats();
        displayHistory();
    }
}