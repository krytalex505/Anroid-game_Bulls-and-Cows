package com.example.bullsandcows;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import java.util.Locale;
import java.util.Random;

public class WordInputActivity extends AppCompatActivity {

    private TextView playerTypeText, wordLengthInfo, waitingText;
    private LinearLayout wordInputContainer;
    private Button readyButton;

    private int myWordLength;
    private EditText[] letterInputs;
    private String myWord = "";
    private String opponentWord = null;
    private int opponentWordLength = 0;

    private boolean isHost;
    private String endpointId;
    private ConnectionsClient connectionsClient;
    private boolean wordSent = false;
    private boolean wordReceived = false;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_input);

        playerTypeText = findViewById(R.id.playerTypeText);
        wordInputContainer = findViewById(R.id.wordInputContainer);
        wordLengthInfo = findViewById(R.id.wordLengthInfo);
        waitingText = findViewById(R.id.waitingText);
        readyButton = findViewById(R.id.readyButton);

        isHost = getIntent().getBooleanExtra("IS_HOST", false);
        endpointId = getIntent().getStringExtra("ENDPOINT_ID");

        GameApplication app = (GameApplication) getApplication();
        connectionsClient = app.getConnectionsClient();

        // КАЖДЫЙ ИГРОК ЗАГАДЫВАЕТ СВОЁ СЛОВО СО СВОЕЙ ДЛИНОЙ
        Random rand = new Random();
        myWordLength = rand.nextInt(4) + 3;
        wordLengthInfo.setText("Длина вашего слова: " + myWordLength + " букв");
        createLetterInputs(myWordLength);
        readyButton.setEnabled(false);

        if (isHost) {
            playerTypeText.setText("ХОСТ - загадайте слово:");
        } else {
            playerTypeText.setText("ИГРОК - загадайте слово:");
        }

        waitingText.setVisibility(View.GONE);
        BluetoothLobbyActivity.setCurrentWordInputActivity(this);
    }

    private void createLetterInputs(int length) {
        if (wordInputContainer != null) {
            wordInputContainer.removeAllViews();
        }

        letterInputs = new EditText[length];

        int cellSize;
        if (length == 3) cellSize = 120;
        else if (length == 4) cellSize = 100;
        else if (length == 5) cellSize = 85;
        else cellSize = 75;

        for (int i = 0; i < length; i++) {
            EditText editText = new EditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cellSize, cellSize);
            params.setMargins(5, 5, 5, 5);
            editText.setLayoutParams(params);
            editText.setBackgroundResource(R.drawable.cell_background);
            editText.setGravity(android.view.Gravity.CENTER);
            editText.setTextSize(18);
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

            editText.setFilters(new InputFilter[]{
                    new InputFilter.LengthFilter(1),
                    (source, start, end, dest, dstart, dend) -> {
                        if (source.length() == 0) return null;
                        String input = source.toString();
                        if (input.matches("[а-яА-ЯёЁ]")) {
                            return input.toLowerCase(Locale.getDefault());
                        }
                        return "";
                    }
            });

            final int index = i;

            editText.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (editText.getText().length() > 0) {
                        editText.setText("");
                        if (index > 0) letterInputs[index - 1].requestFocus();
                    } else if (index > 0) {
                        letterInputs[index - 1].setText("");
                        letterInputs[index - 1].requestFocus();
                    }
                    return true;
                }
                return false;
            });

            editText.addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        String newLetter = s.toString();
                        for (int j = 0; j < index; j++) {
                            if (letterInputs[j].getText().toString().equals(newLetter)) {
                                Toast.makeText(WordInputActivity.this,
                                        "❌ Буквы не должны повторяться!", Toast.LENGTH_SHORT).show();
                                editText.setText("");
                                return;
                            }
                        }
                    }

                    boolean allFilled = true;
                    for (int j = 0; j < length; j++) {
                        if (letterInputs[j].getText().toString().isEmpty()) {
                            allFilled = false;
                            break;
                        }
                    }
                    readyButton.setEnabled(allFilled);

                    if (s.length() == 1 && index < length - 1) {
                        letterInputs[index + 1].requestFocus();
                    }
                }

                public void afterTextChanged(android.text.Editable s) {}
            });

            wordInputContainer.addView(editText);
            letterInputs[i] = editText;
        }
        if (letterInputs.length > 0) {
            letterInputs[0].requestFocus();
        }
    }

    public void onReadyClick(View view) {
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < myWordLength; i++) {
            if (letterInputs[i] == null || letterInputs[i].getText().toString().isEmpty()) {
                Toast.makeText(this, "❌ Заполните все буквы!", Toast.LENGTH_SHORT).show();
                return;
            }
            word.append(letterInputs[i].getText().toString());
        }
        myWord = word.toString();

        for (int i = 0; i < myWordLength; i++) {
            for (int j = i + 1; j < myWordLength; j++) {
                if (myWord.charAt(i) == myWord.charAt(j)) {
                    Toast.makeText(this, "❌ Повторяющиеся буквы!", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        String message = myWord + ":" + myWordLength;
        Payload payload = Payload.fromBytes(message.getBytes());
        connectionsClient.sendPayload(endpointId, payload);

        wordSent = true;

        for (int i = 0; i < myWordLength; i++) {
            if (letterInputs[i] != null) {
                letterInputs[i].setEnabled(false);
            }
        }
        readyButton.setEnabled(false);
        waitingText.setVisibility(View.VISIBLE);
        waitingText.setText("⏳ Ждем слово соперника...");

        if (wordReceived) {
            startGame();
        } else {
            handler.postDelayed(() -> {
                if (!wordReceived && !isFinishing()) {
                    waitingText.setText("❌ Соперник не ответил");
                    Toast.makeText(this, "Ошибка: соперник не отвечает", Toast.LENGTH_LONG).show();
                    readyButton.setEnabled(true);
                    for (int i = 0; i < myWordLength; i++) {
                        if (letterInputs[i] != null) {
                            letterInputs[i].setEnabled(true);
                        }
                    }
                }
            }, 30000);
        }
    }

    public void receiveOpponentWord(String word, int length) {
        this.opponentWord = word;
        this.opponentWordLength = length;
        this.wordReceived = true;

        runOnUiThread(() -> {
            // НЕ МЕНЯЕМ ДЛИНУ СВОЕГО СЛОВА!
            // Просто запоминаем слово соперника
            if (wordSent) {
                startGame();
            }
        });
    }

    private void startGame() {
        Intent intent = new Intent(WordInputActivity.this, GameActivity.class);
        intent.putExtra("GAME_MODE", 3);
        intent.putExtra("MY_WORD", myWord);
        intent.putExtra("OPPONENT_WORD", opponentWord);
        intent.putExtra("ENDPOINT_ID", endpointId);
        intent.putExtra("IS_HOST", isHost);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        BluetoothLobbyActivity.setCurrentWordInputActivity(null);
        if (connectionsClient != null) {
            try {
                connectionsClient.stopAdvertising();
                connectionsClient.stopDiscovery();
                if (endpointId != null) {
                    connectionsClient.disconnectFromEndpoint(endpointId);
                }
            } catch (Exception e) {}
        }
    }
}