package com.example.bullsandcows;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    // UI элементы
    private ScrollView gameScrollView;
    private GridLayout singlePlayerGrid;
    private LinearLayout twoPlayersContainer;
    private GridLayout player1Grid;
    private GridLayout player2Grid;
    private TextView player1Title, player2Title;
    private LinearLayout player1Panel, player2Panel;

    // Колонки счета
    private LinearLayout singleScoreContainer;
    private LinearLayout player1ScoreContainer;
    private LinearLayout player2ScoreContainer;
    private TextView[][] scoreCells;
    private TextView[][] player1ScoreCells;
    private TextView[][] player2ScoreCells;

    private TextView attemptsText;
    private TextView timerText;
    private TextView infoButton, statsButton;
    private Button btnMove, btnZero, btnClear, btnPin, btnDelete;
    private Button btnSaveResult;

    // Аналитические поля
    private LinearLayout bullsLayout, cowsLayout, zerosLayout;

    // Карта для хранения кнопок клавиатуры
    private final Map<String, Button> keyboardButtons = new HashMap<>();

    // Игровые данные
    private int gameMode = 1;
    private int wordLength = 3;
    private final int maxAttempts = 25;
    private String targetWord;
    private boolean gameActive = true;
    private boolean gameWon = false;
    private boolean isRandomMode = false;

    // Для одного игрока
    private TextView[][] singlePlayerCells;
    private String[][] singlePlayerGuesses;
    private int currentRow = 0;
    private int currentCol = 0;

    // Для двух игроков
    private TextView[][] player1Cells;
    private String[][] player1Guesses;
    private TextView[][] player2Cells;
    private String[][] player2Guesses;
    private int player1Row = 0;
    private int player1Col = 0;
    private int player2Row = 0;
    private int player2Col = 0;
    private boolean isPlayer1Turn = true;
    private String player1Word = "";
    private String player2Word = "";
    private boolean player1Ready = false;
    private boolean player2Ready = false;

    // Для Bluetooth
    private String myWord = "";
    private String opponentWord = "";
    private String endpointId = "";
    private boolean isHost = false;
    private ConnectionsClient connectionsClient;

    // Для нулевых букв
    private final List<String> zeroLetters = new ArrayList<>();

    // Таймер
    private long startTime = 0L;
    private final Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private String formattedTime = "00:00";

    // Для сохранения данных
    private SharedPreferences sharedPreferences;
    private static final String SAVED_GAME_PREFS = "saved_game";

    // Класс для хранения результата быков и коров
    private static class BullsCowsResult {
        int bulls;
        int cows;
        BullsCowsResult(int bulls, int cows) {
            this.bulls = bulls;
            this.cows = cows;
        }
    }

    // Фильтр для русских букв
    private static class RussianLetterFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   android.text.Spanned dest, int dstart, int dend) {
            if (source.length() == 0) return null;
            String input = source.toString();
            if (input.matches("[а-яА-ЯёЁ]")) {
                return input.toLowerCase(Locale.getDefault());
            }
            return "";
        }
    }

    // Словари для разной длины слов
    private final String[] words3 = {
            "дом","лес","кот","пёс","сон","рот","луг","жук","мак","рак",
            "лук","нос","рог","бак","век","год","дуб","зуб","код","лоб",
            "мёд","нож","пол","суп","ток","ухо","фон","ход","час","шаг",
            "щит","мир","сыр","дым","тип","вид","жар","пар",
            "дар","бор","тир","гул","зал","кит","рай","май"
    };

    private final String[] words4 = {
            "дома","леса","море","гора","река","ночь","день",
            "рука","нога","лицо","тело","душа","вода","небо",
            "снег","луна","поле","мост","утро",
            "игра","стол","стул","лист","цвет",
            "хлеб","свет","тень","путь"
    };


    // ИСПРАВЛЕНО: только слова из 5 букв
    private final String[] words5 = {
            "птица","кошка",
            "сосна","липа","вишня","груша",
            "лимон","манго","арбуз","дыня","тыква",
            "перец","хлеба","масло","сахар","солью",
            "чайка",
            "песок","ветер","дождь","снега"
    };


    // ИСПРАВЛЕНО: только слова из 6 букв
    private final String[] words6 = {

            "вокзал",
            "здание",

            "ракета","печаль",
            "летчик", "артист", "танцор",

            "приказ","курица","свекла",
            "ластик", "краски", "альбом", "картон" , "прибор", "фильтр","монстр","мастер"


    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        gameMode = getIntent().getIntExtra("GAME_MODE", 1);
        int requestedLength = getIntent().getIntExtra("WORD_LENGTH", 3);
        isRandomMode = getIntent().getBooleanExtra("IS_RANDOM", false);

        sharedPreferences = getSharedPreferences(SAVED_GAME_PREFS, MODE_PRIVATE);

        // Определяем длину слова
        if (isRandomMode) {
            String p = "mode_" + gameMode + "_random_";
            boolean hasSavedGame = sharedPreferences.getBoolean(p + "has_saved_game", false);

            if (hasSavedGame) {
                wordLength = sharedPreferences.getInt(p + "word_length", 3);
            } else {
                Random rand = new Random();
                wordLength = rand.nextInt(4) + 3;
            }
        } else {
            wordLength = requestedLength;
        }

        initViews();

        if (gameMode == 3) {
            // Bluetooth режим
            myWord = getIntent().getStringExtra("MY_WORD");
            opponentWord = getIntent().getStringExtra("OPPONENT_WORD");
            endpointId = getIntent().getStringExtra("ENDPOINT_ID");
            isHost = getIntent().getBooleanExtra("IS_HOST", false);
            GameApplication app = (GameApplication) getApplication();
            connectionsClient = app.getConnectionsClient();
            if (opponentWord != null) {
                wordLength = opponentWord.length();
                targetWord = opponentWord;
            }
            recreateGameGrid();
            String message = String.format(Locale.getDefault(),
                    "Угадайте слово соперника! (%d букв)", wordLength);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        setupGameMode();
        initAnalyticsAreas();
        setupKeyboard();

        // Скрываем кнопку ПИН в Bluetooth режиме
        if (btnPin != null) {
            btnPin.setVisibility(gameMode == 3 ? View.GONE : View.VISIBLE);
        }

        // Генерация целевого слова для режимов 1 и 2
        if (gameMode == 1 || gameMode == 2) {
            String p = getSavePrefix();
            boolean hasSavedGame = sharedPreferences.getBoolean(p + "has_saved_game", false);

            if (!hasSavedGame) {
                selectTargetWord();
            }

            String modeText = isRandomMode ? " (рандомный режим)" : "";
            String message = String.format(Locale.getDefault(),
                    "Загадано слово из %d букв%s", wordLength, modeText);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

        setupButtonListeners();
        startTimer();
        updateAttemptsText();

        if (gameMode != 3) {
            checkSavedGame();
        }
    }

    private String getSavePrefix() {
        if (isRandomMode) {
            return "mode_" + gameMode + "_random_";
        } else {
            return "mode_" + gameMode + "_length_" + wordLength + "_";
        }
    }

    private void recreateGameGrid() {
        if (singlePlayerGrid != null) {
            singlePlayerGrid.removeAllViews();
            createSinglePlayerGrid();
        }
    }

    private void initViews() {
        gameScrollView = findViewById(R.id.game_scroll_view);
        singlePlayerGrid = findViewById(R.id.single_player_grid);
        twoPlayersContainer = findViewById(R.id.two_players_container);
        player1Grid = findViewById(R.id.player1_grid);
        player2Grid = findViewById(R.id.player2_grid);
        player1Title = findViewById(R.id.player1_title);
        player2Title = findViewById(R.id.player2_title);
        player1Panel = findViewById(R.id.player1_panel);
        player2Panel = findViewById(R.id.player2_panel);

        singleScoreContainer = findViewById(R.id.single_score_container);
        player1ScoreContainer = findViewById(R.id.player1_score_container);
        player2ScoreContainer = findViewById(R.id.player2_score_container);

        attemptsText = findViewById(R.id.attempts_text);
        timerText = findViewById(R.id.timer_text);
        infoButton = findViewById(R.id.info_button);
        statsButton = findViewById(R.id.stats_button);

        btnMove = findViewById(R.id.btn_move);
        btnZero = findViewById(R.id.btn_zero);
        btnClear = findViewById(R.id.btn_clear);
        btnPin = findViewById(R.id.btn_pin);
        btnDelete = findViewById(R.id.btn_delete);
        btnSaveResult = findViewById(R.id.btn_save_result);

        btnSaveResult.setVisibility(View.GONE);

        bullsLayout = findViewById(R.id.bulls_input_layout);
        cowsLayout = findViewById(R.id.cows_input_layout);
        zerosLayout = findViewById(R.id.zeros_input_layout);
    }

    private void setupGameMode() {
        if (gameMode == 1) {
            twoPlayersContainer.setVisibility(View.GONE);
            singlePlayerGrid.setVisibility(View.VISIBLE);
            gameScrollView.setVisibility(View.VISIBLE);
            createSinglePlayerGrid();
            createScoreColumn(singleScoreContainer, true);
            String text = String.format(Locale.getDefault(),
                    "Один игрок - Попытка 1/%d", maxAttempts);
            attemptsText.setText(text);
        } else if (gameMode == 2) {
            twoPlayersContainer.setVisibility(View.VISIBLE);
            singlePlayerGrid.setVisibility(View.GONE);
            gameScrollView.setVisibility(View.GONE);
            createTwoPlayerGrids();
            createScoreColumn(player1ScoreContainer, false);
            createScoreColumn(player2ScoreContainer, false);
            attemptsText.setText("Два игрока - Ход Игрока 1");
            highlightCurrentPlayer();
        } else if (gameMode == 3) {
            twoPlayersContainer.setVisibility(View.GONE);
            singlePlayerGrid.setVisibility(View.VISIBLE);
            gameScrollView.setVisibility(View.VISIBLE);
            createSinglePlayerGrid();
            createScoreColumn(singleScoreContainer, true);
            String text = String.format(Locale.getDefault(),
                    "Bluetooth - Попытка 1/%d", maxAttempts);
            attemptsText.setText(text);
        }
    }

    private int getCellHeight() {
        if (wordLength == 3) return 70;
        if (wordLength == 4) return 65;
        if (wordLength == 5) return 60;
        return 55;
    }

    private void createScoreColumn(LinearLayout container, boolean isSingle) {
        container.removeAllViews();
        int cellHeight = getCellHeight();

        if (isSingle) {
            scoreCells = new TextView[maxAttempts][1];
            for (int row = 0; row < maxAttempts; row++) {
                TextView cell = createScoreCell(cellHeight);
                container.addView(cell);
                scoreCells[row][0] = cell;
            }
        } else {
            if (container == player1ScoreContainer) {
                player1ScoreCells = new TextView[maxAttempts][1];
                for (int row = 0; row < maxAttempts; row++) {
                    TextView cell = createScoreCell(cellHeight);
                    container.addView(cell);
                    player1ScoreCells[row][0] = cell;
                }
            } else {
                player2ScoreCells = new TextView[maxAttempts][1];
                for (int row = 0; row < maxAttempts; row++) {
                    TextView cell = createScoreCell(cellHeight);
                    container.addView(cell);
                    player2ScoreCells[row][0] = cell;
                }
            }
        }
    }

    private TextView createScoreCell(int height) {
        TextView cell = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, height);
        params.setMargins(1, 1, 1, 1);
        cell.setLayoutParams(params);
        cell.setGravity(android.view.Gravity.CENTER);
        cell.setBackgroundResource(R.drawable.cell_background);
        cell.setTextSize(12);
        cell.setTextColor(0xFF000000);
        return cell;
    }

    private void createSinglePlayerGrid() {
        int cellHeight = getCellHeight();
        singlePlayerGrid.setColumnCount(wordLength);
        singlePlayerGrid.setRowCount(maxAttempts);
        singlePlayerGrid.removeAllViews();

        singlePlayerCells = new TextView[maxAttempts][wordLength];
        singlePlayerGuesses = new String[maxAttempts][wordLength];

        for (int row = 0; row < maxAttempts; row++) {
            for (int col = 0; col < wordLength; col++) {
                TextView cell = new TextView(this);
                cell.setTextSize(12);
                cell.setGravity(android.view.Gravity.CENTER);
                cell.setBackgroundResource(R.drawable.cell_background);
                cell.setTextColor(0xFF000000);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = cellHeight;
                params.setMargins(1, 1, 1, 1);
                params.columnSpec = GridLayout.spec(col, 1f);
                params.rowSpec = GridLayout.spec(row);
                cell.setLayoutParams(params);

                singlePlayerGrid.addView(cell);
                singlePlayerCells[row][col] = cell;
            }
        }
    }

    private void createTwoPlayerGrids() {
        int cellHeight = getCellHeight();

        player1Grid.setColumnCount(wordLength);
        player1Grid.setRowCount(maxAttempts);
        player2Grid.setColumnCount(wordLength);
        player2Grid.setRowCount(maxAttempts);

        player1Grid.removeAllViews();
        player2Grid.removeAllViews();

        player1Cells = new TextView[maxAttempts][wordLength];
        player1Guesses = new String[maxAttempts][wordLength];
        player2Cells = new TextView[maxAttempts][wordLength];
        player2Guesses = new String[maxAttempts][wordLength];

        for (int row = 0; row < maxAttempts; row++) {
            for (int col = 0; col < wordLength; col++) {
                TextView cell1 = new TextView(this);
                cell1.setTextSize(12);
                cell1.setGravity(android.view.Gravity.CENTER);
                cell1.setBackgroundResource(R.drawable.cell_background);
                cell1.setTextColor(0xFF000000);

                GridLayout.LayoutParams params1 = new GridLayout.LayoutParams();
                params1.width = 0;
                params1.height = cellHeight;
                params1.setMargins(1, 1, 1, 1);
                params1.columnSpec = GridLayout.spec(col, 1f);
                params1.rowSpec = GridLayout.spec(row);
                cell1.setLayoutParams(params1);
                player1Grid.addView(cell1);
                player1Cells[row][col] = cell1;

                TextView cell2 = new TextView(this);
                cell2.setTextSize(12);
                cell2.setGravity(android.view.Gravity.CENTER);
                cell2.setBackgroundResource(R.drawable.cell_background);
                cell2.setTextColor(0xFF000000);

                GridLayout.LayoutParams params2 = new GridLayout.LayoutParams();
                params2.width = 0;
                params2.height = cellHeight;
                params2.setMargins(1, 1, 1, 1);
                params2.columnSpec = GridLayout.spec(col, 1f);
                params2.rowSpec = GridLayout.spec(row);
                cell2.setLayoutParams(params2);
                player2Grid.addView(cell2);
                player2Cells[row][col] = cell2;
            }
        }
    }

    private void highlightCurrentPlayer() {
        if (gameMode == 2) {
            if (isPlayer1Turn) {
                player1Panel.setBackgroundColor(0x20FF5722);
                player2Panel.setBackgroundColor(0x00000000);
                player1Title.setTextColor(0xFFFF5722);
                player2Title.setTextColor(0xFF000000);
            } else {
                player1Panel.setBackgroundColor(0x00000000);
                player2Panel.setBackgroundColor(0x204CAF50);
                player1Title.setTextColor(0xFF000000);
                player2Title.setTextColor(0xFF4CAF50);
            }
        }
    }

    private void initAnalyticsAreas() {
        bullsLayout.removeAllViews();
        cowsLayout.removeAllViews();
        zerosLayout.removeAllViews();

        for (int i = 0; i < 25; i++) {
            createAnalyticsEditText(bullsLayout);
            createAnalyticsEditText(cowsLayout);
            createAnalyticsEditText(zerosLayout);
        }
    }

    private void createAnalyticsEditText(LinearLayout layout) {
        EditText editText = new EditText(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(70, 70);
        params.setMargins(2, 2, 2, 2);
        editText.setLayoutParams(params);
        editText.setBackgroundResource(R.drawable.cell_background);
        editText.setGravity(android.view.Gravity.CENTER);
        editText.setTextSize(14);
        editText.setPadding(2, 2, 2, 2);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setClickable(true);
        editText.setCursorVisible(true);
        editText.setEnabled(true);
        editText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(1),
                new RussianLetterFilter()
        });
        layout.addView(editText);
    }

    private void setupKeyboard() {
        addKeyboardButton(R.id.btn_y, "й");
        addKeyboardButton(R.id.btn_c, "ц");
        addKeyboardButton(R.id.btn_u, "у");
        addKeyboardButton(R.id.btn_k, "к");
        addKeyboardButton(R.id.btn_e, "е");
        addKeyboardButton(R.id.btn_n, "н");
        addKeyboardButton(R.id.btn_g, "г");
        addKeyboardButton(R.id.btn_sh, "ш");
        addKeyboardButton(R.id.btn_shch, "щ");
        addKeyboardButton(R.id.btn_z, "з");
        addKeyboardButton(R.id.btn_h, "х");
        addKeyboardButton(R.id.btn_soft, "ъ");
        addKeyboardButton(R.id.btn_f, "ф");
        addKeyboardButton(R.id.btn_yi, "ы");
        addKeyboardButton(R.id.btn_v, "в");
        addKeyboardButton(R.id.btn_a, "а");
        addKeyboardButton(R.id.btn_p, "п");
        addKeyboardButton(R.id.btn_r, "р");
        addKeyboardButton(R.id.btn_o, "о");
        addKeyboardButton(R.id.btn_l, "л");
        addKeyboardButton(R.id.btn_d, "д");
        addKeyboardButton(R.id.btn_zh, "ж");
        addKeyboardButton(R.id.btn_e_rev, "э");
        addKeyboardButton(R.id.btn_ya, "я");
        addKeyboardButton(R.id.btn_ch, "ч");
        addKeyboardButton(R.id.btn_s, "с");
        addKeyboardButton(R.id.btn_m, "м");
        addKeyboardButton(R.id.btn_i, "и");
        addKeyboardButton(R.id.btn_t, "т");
        addKeyboardButton(R.id.btn_soft2, "ь");
        addKeyboardButton(R.id.btn_b, "б");
        addKeyboardButton(R.id.btn_yu, "ю");
    }

    private void addKeyboardButton(int id, String letter) {
        Button btn = findViewById(id);
        if (btn != null) {
            keyboardButtons.put(letter, btn);
            btn.setText(letter);
            btn.setOnClickListener(v -> {
                if (!gameActive) {
                    Toast.makeText(this, "Игра окончена. Нажмите СБРОС", Toast.LENGTH_SHORT).show();
                    return;
                }
                handleKeyPress(letter);
            });
        }
    }

    private void handleKeyPress(String letter) {
        if (gameMode == 1) {
            handleSinglePlayerKeyPress(letter);
        } else if (gameMode == 2) {
            handleTwoPlayerKeyPress(letter);
        } else if (gameMode == 3) {
            handleSinglePlayerKeyPress(letter);
        }
    }

    private void handleSinglePlayerKeyPress(String letter) {
        if (currentRow < maxAttempts && currentCol < wordLength) {
            for (int i = 0; i < currentCol; i++) {
                if (singlePlayerGuesses[currentRow][i] != null &&
                        singlePlayerGuesses[currentRow][i].equals(letter)) {
                    Toast.makeText(this, "Буквы не должны повторяться!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            singlePlayerCells[currentRow][currentCol].setText(letter);
            singlePlayerGuesses[currentRow][currentCol] = letter;
            currentCol++;
            if (currentCol == wordLength) {
                Toast.makeText(this, "Слово готово. Нажмите ХОД", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleTwoPlayerKeyPress(String letter) {
        if (!gameActive) {
            Toast.makeText(this, "Игра окончена. Нажмите СБРОС", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPlayer1Turn) {
            if (player1Row < maxAttempts && player1Col < wordLength) {
                for (int i = 0; i < player1Col; i++) {
                    if (player1Guesses[player1Row][i] != null &&
                            player1Guesses[player1Row][i].equals(letter)) {
                        Toast.makeText(this, "Буквы не должны повторяться!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                player1Cells[player1Row][player1Col].setText(letter);
                player1Guesses[player1Row][player1Col] = letter;
                player1Col++;
                if (player1Col == wordLength) {
                    player1Ready = true;
                    player1Word = getWordFromGuesses(player1Guesses[player1Row]);
                    isPlayer1Turn = false;
                    player1Col = 0;
                    attemptsText.setText("Два игрока - Ход Игрока 2");
                    highlightCurrentPlayer();
                    Toast.makeText(this, "✅ Игрок 1 ввёл слово", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (player2Row < maxAttempts && player2Col < wordLength) {
                for (int i = 0; i < player2Col; i++) {
                    if (player2Guesses[player2Row][i] != null &&
                            player2Guesses[player2Row][i].equals(letter)) {
                        Toast.makeText(this, "Буквы не должны повторяться!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                player2Cells[player2Row][player2Col].setText(letter);
                player2Guesses[player2Row][player2Col] = letter;
                player2Col++;
                if (player2Col == wordLength) {
                    player2Ready = true;
                    player2Word = getWordFromGuesses(player2Guesses[player2Row]);
                    player2Col = 0;
                    Toast.makeText(this, "✅ Игрок 2 ввёл слово. Нажмите ХОД", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getWordFromGuesses(String[] guesses) {
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < wordLength; i++) {
            word.append(guesses[i]);
        }
        return word.toString();
    }

    private void selectTargetWord() {
        Random random = new Random();
        int index;
        if (wordLength == 3) {
            index = random.nextInt(words3.length);
            targetWord = words3[index];
        } else if (wordLength == 4) {
            index = random.nextInt(words4.length);
            targetWord = words4[index];
        } else if (wordLength == 5) {
            index = random.nextInt(words5.length);
            targetWord = words5[index];
        } else if (wordLength == 6) {
            index = random.nextInt(words6.length);
            targetWord = words6[index];
        } else {
            wordLength = 3;
            index = random.nextInt(words3.length);
            targetWord = words3[index];
        }
    }

    private void startTimer() {
        startTime = SystemClock.uptimeMillis();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = SystemClock.uptimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                timerText.setText("Время: " + formattedTime);
                timerHandler.postDelayed(this, 500);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void setupButtonListeners() {
        infoButton.setOnClickListener(v -> showInfo());
        statsButton.setOnClickListener(v -> showStats());
        btnMove.setOnClickListener(v -> checkWord());
        btnZero.setOnClickListener(v -> zeroFunction());
        btnClear.setOnClickListener(v -> resetGame());
        btnPin.setOnClickListener(v -> pinFunction());
        btnDelete.setOnClickListener(v -> deleteLastLetter());
        btnSaveResult.setOnClickListener(v -> saveResult());
    }

    private void deleteLastLetter() {
        if (!gameActive) return;
        if (gameMode == 1 || gameMode == 3) {
            if (currentCol > 0) {
                currentCol--;
                singlePlayerCells[currentRow][currentCol].setText("");
                singlePlayerGuesses[currentRow][currentCol] = null;
            }
        } else if (gameMode == 2) {
            if (isPlayer1Turn) {
                if (player1Col > 0) {
                    player1Col--;
                    player1Cells[player1Row][player1Col].setText("");
                    player1Guesses[player1Row][player1Col] = null;
                    player1Ready = false;
                }
            } else {
                if (player2Col > 0) {
                    player2Col--;
                    player2Cells[player2Row][player2Col].setText("");
                    player2Guesses[player2Row][player2Col] = null;
                    player2Ready = false;
                }
            }
        }
    }

    private void checkWord() {
        if (!gameActive) return;
        if (gameMode == 1) {
            checkSinglePlayerWord();
        } else if (gameMode == 2) {
            checkTwoPlayerWords();
        } else if (gameMode == 3) {
            checkBluetoothWord();
        }
    }

    private void resetKeyboardColors() {
        for (Button btn : keyboardButtons.values()) {
            btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.keyboard_default));
            btn.setTextColor(0xFF000000);
        }
    }

    private void highlightZeroLettersForWord(String word) {
        for (int i = 0; i < word.length(); i++) {
            String letter = String.valueOf(word.charAt(i));
            Button btn = keyboardButtons.get(letter);
            if (btn != null) {
                btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.zero_gray));
                btn.setTextColor(0xFFFFFFFF);
            }
        }
    }

    private BullsCowsResult calculateBullsAndCows(String guess, String target) {
        if (guess == null || target == null) return new BullsCowsResult(0, 0);
        int bulls = 0, cows = 0;
        boolean[] guessUsed = new boolean[guess.length()];
        boolean[] targetUsed = new boolean[target.length()];
        for (int i = 0; i < Math.min(guess.length(), target.length()); i++) {
            if (guess.charAt(i) == target.charAt(i)) {
                bulls++;
                guessUsed[i] = true;
                targetUsed[i] = true;
            }
        }
        for (int i = 0; i < guess.length(); i++) {
            if (guessUsed[i]) continue;
            for (int j = 0; j < target.length(); j++) {
                if (!targetUsed[j] && guess.charAt(i) == target.charAt(j)) {
                    cows++;
                    targetUsed[j] = true;
                    break;
                }
            }
        }
        return new BullsCowsResult(bulls, cows);
    }

    private void checkSinglePlayerWord() {
        for (int col = 0; col < wordLength; col++) {
            if (singlePlayerGuesses[currentRow][col] == null) {
                Toast.makeText(this, "Введите все буквы!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        StringBuilder guessBuilder = new StringBuilder();
        for (int col = 0; col < wordLength; col++) {
            guessBuilder.append(singlePlayerGuesses[currentRow][col]);
        }
        String guess = guessBuilder.toString();
        if (guess.length() != wordLength) return;
        BullsCowsResult result = calculateBullsAndCows(guess, targetWord);
        Toast.makeText(this, "Быки: " + result.bulls + ", Коровы: " + result.cows, Toast.LENGTH_LONG).show();
        if (scoreCells != null && currentRow < maxAttempts) {
            scoreCells[currentRow][0].setText(result.bulls + "б " + result.cows + "к");
        }

        // Добавляем нулевые буквы в список
        if (result.bulls == 0 && result.cows == 0) {
            for (int i = 0; i < guess.length(); i++) {
                String letter = String.valueOf(guess.charAt(i));
                if (!zeroLetters.contains(letter)) {
                    zeroLetters.add(letter);
                }
            }
            highlightZeroLettersForWord(guess);
        }

        if (result.bulls == wordLength) {
            gameWon = true;
            stopTimer();
            showVictoryDialog();
            return;
        }
        currentRow++;
        currentCol = 0;
        if (currentRow < maxAttempts) {
            updateAttemptsText();
        } else {
            stopTimer();
            gameWon = false;
            showDefeatDialog();
            gameActive = false;
            btnMove.setEnabled(false);
        }
    }

    private void checkTwoPlayerWords() {
        if (!player1Ready || !player2Ready) {
            Toast.makeText(this, "Оба игрока должны ввести слова!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (player1Word == null || player2Word == null ||
                player1Word.length() != wordLength || player2Word.length() != wordLength) {
            Toast.makeText(this, "Ошибка: неверная длина слов", Toast.LENGTH_SHORT).show();
            return;
        }
        BullsCowsResult result1 = calculateBullsAndCows(player1Word, targetWord);
        BullsCowsResult result2 = calculateBullsAndCows(player2Word, targetWord);
        if (player1ScoreCells != null && player1Row < maxAttempts) {
            player1ScoreCells[player1Row][0].setText(result1.bulls + "б " + result1.cows + "к");
        }
        if (player2ScoreCells != null && player2Row < maxAttempts) {
            player2ScoreCells[player2Row][0].setText(result2.bulls + "б " + result2.cows + "к");
        }
        resetKeyboardColors();

        // Добавляем нулевые буквы в список
        if (result1.bulls == 0 && result1.cows == 0) {
            for (int i = 0; i < player1Word.length(); i++) {
                String letter = String.valueOf(player1Word.charAt(i));
                if (!zeroLetters.contains(letter)) {
                    zeroLetters.add(letter);
                }
            }
            highlightZeroLettersForWord(player1Word);
        }
        if (result2.bulls == 0 && result2.cows == 0) {
            for (int i = 0; i < player2Word.length(); i++) {
                String letter = String.valueOf(player2Word.charAt(i));
                if (!zeroLetters.contains(letter)) {
                    zeroLetters.add(letter);
                }
            }
            highlightZeroLettersForWord(player2Word);
        }

        Toast.makeText(this, "Игрок 1: " + result1.bulls + "б " + result1.cows + "к | Игрок 2: " + result2.bulls + "б " + result2.cows + "к", Toast.LENGTH_LONG).show();
        if (result1.bulls == wordLength) {
            gameWon = true;
            stopTimer();
            showTwoPlayerVictoryDialog("Игрок 1");
            return;
        }
        if (result2.bulls == wordLength) {
            gameWon = true;
            stopTimer();
            showTwoPlayerVictoryDialog("Игрок 2");
            return;
        }
        player1Row++;
        player2Row++;
        player1Ready = false;
        player2Ready = false;
        isPlayer1Turn = true;
        if (player1Row < maxAttempts) {
            attemptsText.setText("Два игрока - Ход Игрока 1");
            highlightCurrentPlayer();
            player1Word = "";
            player2Word = "";
            player1Col = 0;
            player2Col = 0;
            updateAttemptsText();
        } else {
            gameActive = false;
            btnMove.setEnabled(false);
            gameWon = false;
            showTwoPlayerFinalResults(result1.bulls, result1.cows, result2.bulls, result2.cows);
        }
    }

    private void showTwoPlayerVictoryDialog(String winner) {
        new AlertDialog.Builder(this)
                .setTitle("🎉 ПОБЕДА!")
                .setMessage(winner + " угадал слово!\n\nЗагаданное слово: " + targetWord + "\nВремя: " + formattedTime)
                .setPositiveButton("Новая игра", (d, w) -> resetGame())
                .setNegativeButton("Выйти", (d, w) -> finish())
                .show();
    }

    private void showTwoPlayerFinalResults(int b1, int c1, int b2, int c2) {
        new AlertDialog.Builder(this)
                .setTitle("📊 РЕЗУЛЬТАТЫ")
                .setMessage("Игрок 1: " + b1 + "б " + c1 + "к\nИгрок 2: " + b2 + "б " + c2 + "к\n\nЗагаданное слово: " + targetWord + "\nВремя: " + formattedTime)
                .setPositiveButton("Новая игра", (d, w) -> resetGame())
                .setNegativeButton("Выйти", (d, w) -> finish())
                .show();
    }

    private void checkBluetoothWord() {
        if (opponentWord == null || opponentWord.isEmpty()) {
            Toast.makeText(this, "Ошибка: слово соперника не получено", Toast.LENGTH_LONG).show();
            return;
        }
        for (int col = 0; col < wordLength; col++) {
            if (singlePlayerGuesses[currentRow][col] == null) {
                Toast.makeText(this, "Введите все буквы!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        StringBuilder guess = new StringBuilder();
        for (int col = 0; col < wordLength; col++) guess.append(singlePlayerGuesses[currentRow][col]);
        String guessWord = guess.toString();
        BullsCowsResult result = calculateBullsAndCows(guessWord, opponentWord);
        if (scoreCells != null && currentRow < maxAttempts) {
            scoreCells[currentRow][0].setText(result.bulls + "б " + result.cows + "к");
        }

        // Добавляем нулевые буквы в список
        if (result.bulls == 0 && result.cows == 0) {
            for (int i = 0; i < guessWord.length(); i++) {
                String letter = String.valueOf(guessWord.charAt(i));
                if (!zeroLetters.contains(letter)) {
                    zeroLetters.add(letter);
                }
            }
            highlightZeroLettersForWord(guessWord);
        }

        Toast.makeText(this, "Быки: " + result.bulls + ", Коровы: " + result.cows, Toast.LENGTH_LONG).show();
        if (result.bulls == wordLength) {
            gameWon = true;
            stopTimer();
            showBluetoothResults();
            return;
        }
        currentRow++;
        currentCol = 0;
        if (currentRow < maxAttempts) {
            updateAttemptsText();
        } else {
            stopTimer();
            gameWon = false;
            showBluetoothResults();
            gameActive = false;
            btnMove.setEnabled(false);
        }
    }

    private void showBluetoothResults() {
        String msg = gameWon ? "Вы угадали слово!\n\nСлово: " + opponentWord + "\nВремя: " + formattedTime + "\nПопыток: " + currentRow
                : "Вы не угадали слово!\n\nСлово: " + opponentWord + "\nВремя: " + formattedTime;
        new AlertDialog.Builder(this)
                .setTitle(gameWon ? "🎉 ПОБЕДА!" : "😢 ИГРА ОКОНЧЕНА")
                .setMessage(msg)
                .setPositiveButton("Сохранить", (d, w) -> {
                    new GameLogger(this).saveGame(opponentWord, gameWon ? currentRow : maxAttempts, wordLength, gameWon, formattedTime);
                    Toast.makeText(this, "✅ Результат сохранён!", Toast.LENGTH_SHORT).show();
                    d.dismiss();
                    showBluetoothEndDialog();
                })
                .setNeutralButton("Новая игра", (d, w) -> {
                    Intent intent = new Intent(GameActivity.this, BluetoothLobbyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Выйти", (d, w) -> finish())
                .show();
    }

    private void showBluetoothEndDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Что дальше?")
                .setMessage("Хотите начать новую игру или выйти в меню?")
                .setPositiveButton("Новая игра", (d, w) -> {
                    Intent intent = new Intent(GameActivity.this, BluetoothLobbyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Выйти", (d, w) -> finish())
                .show();
    }

    private void showVictoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🎉 ПОБЕДА!")
                .setMessage("Вы угадали слово!\n\nВремя: " + formattedTime + "\nПопыток: " + (currentRow + 1))
                .setPositiveButton("ГУГЛ", (d, w) -> {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(targetWord + " значение слова"))));
                })
                .setNegativeButton("Сохранить", (d, w) -> {
                    saveResult();
                    btnSaveResult.setVisibility(View.VISIBLE);
                    btnMove.setEnabled(false);
                    Toast.makeText(this, "✅ Результат сохранён!", Toast.LENGTH_SHORT).show();
                    d.dismiss();
                    showPostSaveDialog();
                })
                .setNeutralButton("Новая игра", (d, w) -> resetGame())
                .show();
    }

    private void showPostSaveDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Что дальше?")
                .setMessage("Хотите начать новую игру или выйти в меню?")
                .setPositiveButton("Новая игра", (d, w) -> resetGame())
                .setNegativeButton("Выйти", (d, w) -> finish())
                .show();
    }

    private void showDefeatDialog() {
        new AlertDialog.Builder(this)
                .setTitle("😢 ИГРА ОКОНЧЕНА")
                .setMessage("Вы не угадали слово!\n\nВремя: " + formattedTime)
                .setPositiveButton("Сохранить", (d, w) -> {
                    gameWon = false;
                    saveResult();
                    btnSaveResult.setVisibility(View.VISIBLE);
                    btnMove.setEnabled(false);
                    Toast.makeText(this, "✅ Результат сохранён!", Toast.LENGTH_SHORT).show();
                    d.dismiss();
                    showPostSaveDialog();
                })
                .setNeutralButton("Новая игра", (d, w) -> resetGame())
                .setNegativeButton("Выйти", (d, w) -> finish())
                .show();
    }

    private void updateAttemptsText() {
        if (gameMode == 1 || gameMode == 3) {
            attemptsText.setText(String.format("Попытка %d/%d", currentRow + 1, maxAttempts));
        } else if (gameMode == 2) {
            attemptsText.setText(String.format("Два игрока - Ход %s (%d/%d)", isPlayer1Turn ? "Игрока 1" : "Игрока 2", player1Row + 1, maxAttempts));
        }
    }

    private void zeroFunction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ввод нулевых букв");
        final EditText input = new EditText(this);
        input.setHint("Введите нулевые буквы (например: абвг)");
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(wordLength)});
        builder.setView(input);
        builder.setPositiveButton("Добавить", (d, w) -> {
            String letters = input.getText().toString().toLowerCase(Locale.getDefault());
            for (int i = 0; i < letters.length(); i++) {
                String letter = String.valueOf(letters.charAt(i));
                if (!zeroLetters.contains(letter)) zeroLetters.add(letter);
            }
            for (String letter : zeroLetters) {
                Button btn = keyboardButtons.get(letter);
                if (btn != null) {
                    btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.zero_gray));
                    btn.setTextColor(0xFFFFFFFF);
                }
            }
            Toast.makeText(this, "Нулевые буквы добавлены: " + letters, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Сбросить всё", (d, w) -> {
            resetKeyboardColors();
            zeroLetters.clear();
            Toast.makeText(this, "Подсветка нулевых букв сброшена", Toast.LENGTH_SHORT).show();
        });
        builder.setNeutralButton("Отмена", null);
        builder.show();
    }

    private void saveResult() {
        if (gameMode == 1 || gameMode == 3) {
            new GameLogger(this).saveGame(targetWord, gameWon ? currentRow : maxAttempts, wordLength, gameWon, formattedTime);
            saveZeroLettersToPrefs();
            Toast.makeText(this, "✅ Результат сохранён в статистику! Время: " + formattedTime, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "В режиме двух игроков результаты не сохраняются", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveZeroLettersToPrefs() {
        StringBuilder sb = new StringBuilder();
        for (String l : zeroLetters) sb.append(l);
        String p = getSavePrefix();
        sharedPreferences.edit().putString(p + "zero_letters", sb.toString()).apply();
    }

    private void pinFunction() {
        if (!gameActive) {
            Toast.makeText(this, "Игра не активна", Toast.LENGTH_SHORT).show();
            return;
        }
        saveGameToPreferences();
        Toast.makeText(this, "✅ Игра сохранена!", Toast.LENGTH_LONG).show();
    }

    private void saveGameToPreferences() {
        SharedPreferences.Editor e = sharedPreferences.edit();
        String p = getSavePrefix();

        e.putBoolean(p + "has_saved_game", true);
        e.putInt(p + "game_mode", gameMode);
        e.putInt(p + "word_length", wordLength);
        e.putBoolean(p + "is_random", isRandomMode);
        e.putString(p + "target_word", targetWord);
        e.putLong(p + "timer_time", SystemClock.uptimeMillis() - startTime);
        e.putInt(p + "current_row", currentRow);
        e.putInt(p + "current_col", currentCol);

        StringBuilder zeroLettersStr = new StringBuilder();
        for (String letter : zeroLetters) {
            zeroLettersStr.append(letter);
        }
        e.putString(p + "zero_letters", zeroLettersStr.toString());

        if (gameMode == 1 || gameMode == 3) {
            for (int r = 0; r < maxAttempts; r++) {
                for (int c = 0; c < wordLength; c++) {
                    if (singlePlayerGuesses[r][c] != null) {
                        e.putString(p + "cell_" + r + "_" + c, singlePlayerGuesses[r][c]);
                    }
                }
            }
            if (scoreCells != null) {
                for (int r = 0; r < maxAttempts; r++) {
                    if (scoreCells[r][0].getText().length() > 0) {
                        e.putString(p + "score_" + r, scoreCells[r][0].getText().toString());
                    }
                }
            }
        } else if (gameMode == 2) {
            e.putInt(p + "player1_row", player1Row);
            e.putInt(p + "player1_col", player1Col);
            e.putInt(p + "player2_row", player2Row);
            e.putInt(p + "player2_col", player2Col);
            e.putBoolean(p + "is_player1_turn", isPlayer1Turn);
            for (int r = 0; r < maxAttempts; r++) {
                for (int c = 0; c < wordLength; c++) {
                    if (player1Guesses[r][c] != null) {
                        e.putString(p + "p1_cell_" + r + "_" + c, player1Guesses[r][c]);
                    }
                    if (player2Guesses[r][c] != null) {
                        e.putString(p + "p2_cell_" + r + "_" + c, player2Guesses[r][c]);
                    }
                }
            }
            if (player1ScoreCells != null) {
                for (int r = 0; r < maxAttempts; r++) {
                    if (player1ScoreCells[r][0].getText().length() > 0) {
                        e.putString(p + "p1_score_" + r, player1ScoreCells[r][0].getText().toString());
                    }
                    if (player2ScoreCells[r][0].getText().length() > 0) {
                        e.putString(p + "p2_score_" + r, player2ScoreCells[r][0].getText().toString());
                    }
                }
            }
        }
        e.apply();
    }

    private void clearCurrentSavedGame() {
        SharedPreferences.Editor e = sharedPreferences.edit();
        String p = getSavePrefix();

        e.remove(p + "has_saved_game");
        e.remove(p + "game_mode");
        e.remove(p + "word_length");
        e.remove(p + "is_random");
        e.remove(p + "target_word");
        e.remove(p + "timer_time");
        e.remove(p + "current_row");
        e.remove(p + "current_col");
        e.remove(p + "zero_letters");

        if (gameMode == 1 || gameMode == 3) {
            for (int r = 0; r < maxAttempts; r++) {
                for (int c = 0; c < wordLength; c++) e.remove(p + "cell_" + r + "_" + c);
                e.remove(p + "score_" + r);
            }
        } else if (gameMode == 2) {
            e.remove(p + "player1_row");
            e.remove(p + "player1_col");
            e.remove(p + "player2_row");
            e.remove(p + "player2_col");
            e.remove(p + "is_player1_turn");
            for (int r = 0; r < maxAttempts; r++) {
                for (int c = 0; c < wordLength; c++) {
                    e.remove(p + "p1_cell_" + r + "_" + c);
                    e.remove(p + "p2_cell_" + r + "_" + c);
                }
                e.remove(p + "p1_score_" + r);
                e.remove(p + "p2_score_" + r);
            }
        }
        e.apply();
    }

    private void checkSavedGame() {
        String p = getSavePrefix();
        boolean hasSavedGame = sharedPreferences.getBoolean(p + "has_saved_game", false);

        if (hasSavedGame) {
            String modeText = isRandomMode ? "рандомный режим" : "режим с " + wordLength + " буквами";
            new AlertDialog.Builder(this)
                    .setTitle("💾 Восстановить игру")
                    .setMessage("У вас есть сохраненная игра (" + modeText + "). Хотите продолжить?")
                    .setPositiveButton("Да", (d, w) -> loadSavedGame())
                    .setNegativeButton("Нет", (d, w) -> {
                        clearCurrentSavedGame();
                        if (gameMode == 1) selectTargetWord();
                        else if (gameMode == 2) selectTargetWord();
                    })
                    .show();
        }
    }

    private void loadSavedGame() {
        String p = getSavePrefix();

        if (!sharedPreferences.getBoolean(p + "has_saved_game", false)) {
            return;
        }

        targetWord = sharedPreferences.getString(p + "target_word", targetWord);
        startTime = SystemClock.uptimeMillis() - sharedPreferences.getLong(p + "timer_time", 0);
        currentRow = sharedPreferences.getInt(p + "current_row", 0);
        currentCol = sharedPreferences.getInt(p + "current_col", 0);

        // Восстанавливаем нулевые буквы
        String zeroLettersStr = sharedPreferences.getString(p + "zero_letters", "");
        zeroLetters.clear();
        for (int i = 0; i < zeroLettersStr.length(); i++) {
            zeroLetters.add(String.valueOf(zeroLettersStr.charAt(i)));
        }

        // Восстанавливаем подсветку клавиш
        resetKeyboardColors();
        for (String letter : zeroLetters) {
            Button btn = keyboardButtons.get(letter);
            if (btn != null) {
                btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.zero_gray));
                btn.setTextColor(0xFFFFFFFF);
            }
        }

        if (gameMode == 1 || gameMode == 3) {
            for (int r = 0; r < maxAttempts; r++) {
                for (int c = 0; c < wordLength; c++) {
                    String letter = sharedPreferences.getString(p + "cell_" + r + "_" + c, null);
                    if (letter != null) {
                        singlePlayerCells[r][c].setText(letter);
                        singlePlayerGuesses[r][c] = letter;
                    }
                }
            }
            if (scoreCells != null) {
                for (int r = 0; r < maxAttempts; r++) {
                    String score = sharedPreferences.getString(p + "score_" + r, null);
                    if (score != null) {
                        scoreCells[r][0].setText(score);
                    }
                }
            }
            updateAttemptsText();
            if (currentRow >= maxAttempts) {
                gameActive = false;
                btnMove.setEnabled(false);
            }
        } else if (gameMode == 2) {
            player1Row = sharedPreferences.getInt(p + "player1_row", 0);
            player1Col = sharedPreferences.getInt(p + "player1_col", 0);
            player2Row = sharedPreferences.getInt(p + "player2_row", 0);
            player2Col = sharedPreferences.getInt(p + "player2_col", 0);
            isPlayer1Turn = sharedPreferences.getBoolean(p + "is_player1_turn", true);

            for (int r = 0; r < maxAttempts; r++) {
                for (int c = 0; c < wordLength; c++) {
                    String p1 = sharedPreferences.getString(p + "p1_cell_" + r + "_" + c, null);
                    if (p1 != null) {
                        player1Cells[r][c].setText(p1);
                        player1Guesses[r][c] = p1;
                    }
                    String p2 = sharedPreferences.getString(p + "p2_cell_" + r + "_" + c, null);
                    if (p2 != null) {
                        player2Cells[r][c].setText(p2);
                        player2Guesses[r][c] = p2;
                    }
                }
            }
            if (player1ScoreCells != null) {
                for (int r = 0; r < maxAttempts; r++) {
                    String s1 = sharedPreferences.getString(p + "p1_score_" + r, null);
                    if (s1 != null) player1ScoreCells[r][0].setText(s1);
                    String s2 = sharedPreferences.getString(p + "p2_score_" + r, null);
                    if (s2 != null) player2ScoreCells[r][0].setText(s2);
                }
            }
            if (player1Row < maxAttempts && player1Col == wordLength) {
                player1Ready = true;
                player1Word = getWordFromGuesses(player1Guesses[player1Row]);
            }
            if (player2Row < maxAttempts && player2Col == wordLength) {
                player2Ready = true;
                player2Word = getWordFromGuesses(player2Guesses[player2Row]);
            }
            attemptsText.setText(isPlayer1Turn ? "Два игрока - Ход Игрока 1" : "Два игрока - Ход Игрока 2");
            highlightCurrentPlayer();
            if (player1Row >= maxAttempts || player2Row >= maxAttempts) {
                gameActive = false;
                btnMove.setEnabled(false);
            }
        }

        Toast.makeText(this, "Игра восстановлена!", Toast.LENGTH_SHORT).show();
    }

    private void showInfo() { startActivity(new Intent(this, AboutActivity.class)); }
    private void showStats() { startActivity(new Intent(this, StatisticsActivity.class)); }

    private void resetGame() {
        currentRow = currentCol = player1Row = player1Col = player2Row = player2Col = 0;
        isPlayer1Turn = true;
        player1Ready = player2Ready = false;
        gameActive = true;
        gameWon = false;
        stopTimer();
        startTimer();
        if (gameMode == 1 || gameMode == 3) {
            for (int r = 0; r < maxAttempts; r++)
                for (int c = 0; c < wordLength; c++) {
                    singlePlayerCells[r][c].setText("");
                    singlePlayerCells[r][c].setBackgroundResource(R.drawable.cell_background);
                    singlePlayerGuesses[r][c] = null;
                }
            if (scoreCells != null) for (int r = 0; r < maxAttempts; r++) scoreCells[r][0].setText("");
        } else if (gameMode == 2) {
            selectTargetWord();
            for (int r = 0; r < maxAttempts; r++)
                for (int c = 0; c < wordLength; c++) {
                    player1Cells[r][c].setText("");
                    player1Cells[r][c].setBackgroundResource(R.drawable.cell_background);
                    player1Guesses[r][c] = null;
                    player2Cells[r][c].setText("");
                    player2Cells[r][c].setBackgroundResource(R.drawable.cell_background);
                    player2Guesses[r][c] = null;
                }
            if (player1ScoreCells != null)
                for (int r = 0; r < maxAttempts; r++) {
                    player1ScoreCells[r][0].setText("");
                    player2ScoreCells[r][0].setText("");
                }
            attemptsText.setText("Два игрока - Ход Игрока 1");
            highlightCurrentPlayer();
            updateAttemptsText();
        }
        bullsLayout.removeAllViews();
        cowsLayout.removeAllViews();
        zerosLayout.removeAllViews();
        initAnalyticsAreas();
        resetKeyboardColors();
        zeroLetters.clear();
        btnSaveResult.setVisibility(View.GONE);
        btnMove.setEnabled(true);
        clearCurrentSavedGame();
        if (gameMode == 1) {
            updateAttemptsText();
            selectTargetWord();
            Toast.makeText(this, "Новая игра!", Toast.LENGTH_SHORT).show();
        } else if (gameMode == 2) {
            Toast.makeText(this, "Новая игра для двух игроков!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onPause() { super.onPause(); }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (gameMode == 3 && connectionsClient != null) {
            try { if (endpointId != null) connectionsClient.disconnectFromEndpoint(endpointId); } catch (Exception e) {}
        }
    }
}