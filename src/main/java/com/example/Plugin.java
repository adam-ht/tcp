package com.example;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Plugin extends JavaPlugin {

    private ServerSocket serverSocket;
    FileConfiguration config = getConfig();
    
    @Override
    public void onEnable() {

        config.addDefault("port:", 50000);
        config.addDefault("shutdown_command:", "SHUTDOWN");
        config.addDefault("delay(in seconds):", 120);
        config.addDefault("allowed_ips:", "127.0.0.1");
        config.options().copyDefaults(true);
        saveConfig();

        int port = config.getInt("port:"); // Wybierz port, na którym plugin ma oczekiwać na pakiety

        try {
            serverSocket = new ServerSocket(port);
            getLogger().info("Plugin is listening on port " + port);

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
            getLogger().severe("Error while initializing TCP server: " + e.getMessage());
        }
    }

    private void handlePacket(Socket clientSocket) {
        try {
            String clientAddress = clientSocket.getInetAddress().getHostAddress();
            if(isValidIPAddress(clientAddress)){
                int result = config.getInt("delay(in seconds):") / 60;
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message = reader.readLine();

                if (message != null && message.equals(config.getString("shutdown_command:"))) {
                    getLogger().info("Received command " + config.getString("shutdown_command:") + ". Shutting down the server...");
                    if (config.getInt("delay(in seconds):") < 60) {
                        getServer().broadcastMessage("Server will be powered off in " + config.getInt("delay(in seconds):") + " seconds");
                    } else {
                        getServer().broadcastMessage("Server will be powered off in " + result + " minutes");
                    }
                    
                    try{
                    TimeUnit.SECONDS.sleep(config.getInt("delay(in seconds):"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getServer().shutdown();
                } else {
                    getLogger().warning("Received unrecognized command: " + message);
                }
            } else {
                getLogger().warning("Received packet from unknown IP address: " + clientAddress);
            }
            clientSocket.close();
        } catch (IOException e) {
            getLogger().severe("Error handling package: " + e.getMessage());
        }
    }

    private boolean isValidIPAddress(String ipAddress) {
        String[] allowedIPs = config.getString("allowed_ips:").split(",");
        
        for (String allowedIP : allowedIPs) {
            if (ipAddress.equals(allowedIP.trim()) || allowedIP.trim().equals("0.0.0.0")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            getLogger().severe("Error shutting down TCP server: " + e.getMessage());
        }
    }
}