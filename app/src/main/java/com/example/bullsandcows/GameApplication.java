package com.example.bullsandcows;

import android.app.Application;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;

public class GameApplication extends Application {

    private ConnectionsClient connectionsClient;

    @Override
    public void onCreate() {
        super.onCreate();
        connectionsClient = Nearby.getConnectionsClient(this);
    }

    public ConnectionsClient getConnectionsClient() {
        return connectionsClient;
    }
}