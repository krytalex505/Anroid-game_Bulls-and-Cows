package com.example.bullsandcows;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ConnectionTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_type);

        Button localButton = findViewById(R.id.localButton);
        Button bluetoothButton = findViewById(R.id.bluetoothButton);
        Button backButton = findViewById(R.id.backButton);

        localButton.setOnClickListener(v -> {
            Intent intent = new Intent(ConnectionTypeActivity.this, WordLengthActivity.class);
            intent.putExtra("GAME_MODE", 2);
            startActivity(intent);
        });

        bluetoothButton.setOnClickListener(v -> {
            Intent intent = new Intent(ConnectionTypeActivity.this, BluetoothLobbyActivity.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> finish());
    }
}