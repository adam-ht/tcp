package com.example;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Plugin extends JavaPlugin {

    private ServerSocket serverSocket;
    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {

        config.addDefault("port:", 50000);
        config.addDefault("shutdown_command:", "SHUTDOWN");
        config.options().copyDefaults(true);
        saveConfig();

        int port = config.getInt("port:"); // Wybierz port, na którym plugin ma oczekiwać na pakiety

        try {
            serverSocket = new ServerSocket(port);
            getLogger().info("Plugin oczekuje na pakiety na porcie " + port);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            Socket clientSocket = serverSocket.accept();
                            handlePacket(clientSocket);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            getLogger().severe("Błąd podczas inicjalizacji serwera TCP: " + e.getMessage());
        }
    }

    private void handlePacket(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message = reader.readLine();

            if (message != null && message.equals(config.getString("shutdown_command:"))) {
                getLogger().info("Otrzymano polecenie SHUTDOWN. Wyłączanie serwera...");
                getServer().shutdown();
            } else {
                getLogger().warning("Otrzymano nieznany pakiet: " + message);
            }

            clientSocket.close();
        } catch (IOException e) {
            getLogger().severe("Błąd podczas obsługi pakietu: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            getLogger().severe("Błąd podczas zamykania serwera TCP: " + e.getMessage());
        }
    }
}