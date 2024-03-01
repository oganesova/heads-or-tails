package org.example;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final int SERVER_PORT = 12345;
    private final List<GameSession> gameSessions = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        Main server = new Main();
        server.start();
    }

    public void start() {
        setupLogging();

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New client connected");

                    GameSession gameSession = new GameSession(clientSocket);
                    gameSessions.add(gameSession);

                    Thread sessionThread = new Thread(gameSession);
                    sessionThread.start();
                } catch (IOException e) {
                    logger.warning("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.severe("Error starting the server: " + e.getMessage());
        }
    }

    private void setupLogging() {
        try {
            FileHandler fileHandler = new FileHandler("%h/server.log");
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.warning("Error setting up logging");
        }
    }
}
