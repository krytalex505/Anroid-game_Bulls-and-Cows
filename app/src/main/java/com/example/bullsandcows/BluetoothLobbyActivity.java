package com.example.bullsandcows;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.*;
import java.util.ArrayList;
import java.util.List;

public class BluetoothLobbyActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1001;
    private static final int REQUEST_ENABLE_BLUETOOTH = 1002;
    private static final String SERVICE_ID = "com.example.bullsandcows";

    private TextView statusText;
    private ListView devicesListView;
    private Button hostButton, discoverButton;

    private List<String> deviceNames = new ArrayList<>();
    private List<String> endpointIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private ConnectionsClient connectionsClient;
    private boolean isAdvertising = false;
    private boolean isDiscovering = false;
    private boolean isConnecting = false;
    private static WordInputActivity currentWordInputActivity;

    public static void setCurrentWordInputActivity(WordInputActivity activity) {
        currentWordInputActivity = activity;
    }

    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            byte[] bytes = payload.asBytes();
            if (bytes != null) {
                final String message = new String(bytes);
                runOnUiThread(() -> {
                    String[] parts = message.split(":");
                    if (parts.length >= 2) {
                        String word = parts[0];
                        int length = Integer.parseInt(parts[1]);

                        if (currentWordInputActivity != null) {
                            currentWordInputActivity.receiveOpponentWord(word, length);
                        }
                    }
                });
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId,
                                            @NonNull PayloadTransferUpdate update) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_lobby);


        // Очищаем статическую ссылку при запуске лобби
        setCurrentWordInputActivity(null);
        statusText = findViewById(R.id.statusText);
        devicesListView = findViewById(R.id.devicesListView);
        hostButton = findViewById(R.id.hostButton);
        discoverButton = findViewById(R.id.discoverButton);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceNames);
        devicesListView.setAdapter(adapter);

        connectionsClient = Nearby.getConnectionsClient(this);

        checkPermissions();

        hostButton.setOnClickListener(v -> {
            if (!checkBluetoothEnabled()) return;
            if (!hasAllPermissions()) {
                checkPermissions();
                return;
            }

            if (isDiscovering) {
                connectionsClient.stopDiscovery();
                isDiscovering = false;
            }
            if (!isAdvertising && !isConnecting) {
                startHosting();
            } else {
                Toast.makeText(this, "Операция уже выполняется", Toast.LENGTH_SHORT).show();
            }
        });

        discoverButton.setOnClickListener(v -> {
            if (!checkBluetoothEnabled()) return;
            if (!hasAllPermissions()) {
                checkPermissions();
                return;
            }

            if (isAdvertising) {
                connectionsClient.stopAdvertising();
                isAdvertising = false;
            }
            if (!isDiscovering && !isConnecting) {
                startDiscovery();
            } else {
                Toast.makeText(this, "Поиск уже запущен", Toast.LENGTH_SHORT).show();
            }
        });

        devicesListView.setOnItemClickListener((parent, view, position, id) -> {
            if (!isConnecting) {
                String endpointId = endpointIds.get(position);
                connectToDevice(endpointId);
            } else {
                Toast.makeText(this, "Подключение уже выполняется", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkBluetoothEnabled() {
        try {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
                return false;
            }
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, "Нет разрешения на проверку Bluetooth", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean hasAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void checkPermissions() {
        List<String> neededPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
        }

        if (!neededPermissions.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Требуются разрешения")
                    .setMessage("Для поиска устройств нужно разрешение на местоположение")
                    .setPositiveButton("OK", (dialog, which) ->
                            ActivityCompat.requestPermissions(this,
                                    neededPermissions.toArray(new String[0]),
                                    REQUEST_CODE_REQUIRED_PERMISSIONS))
                    .setNegativeButton("Отмена", null)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            Toast.makeText(this, allGranted ? "✅ Разрешения получены" : "❌ Нужны все разрешения", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            Toast.makeText(this, resultCode == RESULT_OK ? "✅ Bluetooth включен" : "❌ Нужен Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    private void startHosting() {
        isAdvertising = true;
        statusText.setText("🟢 Создание игры...");

        AdvertisingOptions options = new AdvertisingOptions.Builder()
                .setStrategy(Strategy.P2P_CLUSTER).build();

        connectionsClient.startAdvertising(
                android.os.Build.MODEL + " (Хост)",
                SERVICE_ID,
                connectionLifecycleCallback,
                options
        ).addOnSuccessListener(unused -> {
            statusText.setText("✅ Игра создана. Жду подключения...");
        }).addOnFailureListener(e -> {
            isAdvertising = false;
            statusText.setText("❌ Ошибка: " + e.getMessage());
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void startDiscovery() {
        isDiscovering = true;
        statusText.setText("🔍 Поиск игр...");
        deviceNames.clear();
        endpointIds.clear();
        adapter.notifyDataSetChanged();

        DiscoveryOptions options = new DiscoveryOptions.Builder()
                .setStrategy(Strategy.P2P_CLUSTER).build();

        connectionsClient.startDiscovery(
                SERVICE_ID,
                endpointDiscoveryCallback,
                options
        ).addOnSuccessListener(unused -> {
            statusText.setText("🔍 Поиск... Найдено: " + deviceNames.size());
        }).addOnFailureListener(e -> {
            isDiscovering = false;
            statusText.setText("❌ Ошибка: " + e.getMessage());
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void connectToDevice(String endpointId) {
        isConnecting = true;
        statusText.setText("🔄 Подключение...");

        connectionsClient.requestConnection(
                android.os.Build.MODEL + " (Игрок)",
                endpointId,
                connectionLifecycleCallback
        ).addOnSuccessListener(unused -> {
            statusText.setText("🔄 Подключение...");
        }).addOnFailureListener(e -> {
            isConnecting = false;
            statusText.setText("❌ Ошибка подключения");
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NonNull String endpointId,
                                            @NonNull DiscoveredEndpointInfo info) {
                    runOnUiThread(() -> {
                        if (!endpointIds.contains(endpointId)) {
                            endpointIds.add(endpointId);
                            deviceNames.add(info.getEndpointName());
                            adapter.notifyDataSetChanged();
                            statusText.setText("🔍 Найдено: " + deviceNames.size());
                        }
                    });
                }

                @Override
                public void onEndpointLost(@NonNull String endpointId) {
                    runOnUiThread(() -> {
                        int index = endpointIds.indexOf(endpointId);
                        if (index != -1) {
                            endpointIds.remove(index);
                            deviceNames.remove(index);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NonNull String endpointId,
                                                  @NonNull ConnectionInfo info) {
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                    runOnUiThread(() ->
                            statusText.setText("📞 Подключение от " + info.getEndpointName())
                    );
                }

                @Override
                public void onConnectionResult(@NonNull String endpointId,
                                               @NonNull ConnectionResolution result) {
                    isConnecting = false;
                    if (result.getStatus().isSuccess()) {
                        runOnUiThread(() -> {
                            Toast.makeText(BluetoothLobbyActivity.this,
                                    "✅ Подключено!", Toast.LENGTH_SHORT).show();

                            connectionsClient.stopDiscovery();
                            connectionsClient.stopAdvertising();

                            Intent intent = new Intent(BluetoothLobbyActivity.this, WordInputActivity.class);
                            intent.putExtra("ENDPOINT_ID", endpointId);
                            intent.putExtra("IS_HOST", isAdvertising);
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                            statusText.setText("❌ Ошибка подключения");
                            Toast.makeText(BluetoothLobbyActivity.this,
                                    "Не удалось подключиться", Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onDisconnected(@NonNull String endpointId) {
                    runOnUiThread(() ->
                            statusText.setText("📴 Отключено")
                    );
                }
            };

    @Override
    protected void onStop() {
        super.onStop();
        if (connectionsClient != null) {
            try {
                connectionsClient.stopAdvertising();
                connectionsClient.stopDiscovery();
            } catch (Exception e) {}
        }
    }
}