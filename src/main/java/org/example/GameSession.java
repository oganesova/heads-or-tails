package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class GameSession implements Runnable {
    private static final Logger logger = Logger.getLogger(GameSession.class.getName());
    private final Socket clientSocket;
    private int tokens = 100;
    private List<String> gameHistory = new ArrayList<>();

    public GameSession(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            String input;
            while ((input = reader.readLine()) != null) {
                if (input.equals("QUIT")) {
                    break;
                }

                handleGameRequest(Integer.parseInt(input), writer);
            }

            logGameSummary();
            clientSocket.close();
        } catch (IOException e) {
            logger.severe("Error during session: " + e.getMessage());
        }
    }

    private void handleGameRequest(int bet, BufferedWriter writer) throws IOException {
        if (bet <= tokens) {
            tokens -= bet;

            boolean result = new Random().nextBoolean();
            int win = result ? (int) (bet * 1.9) : 0;
            tokens += win;

            String outcome = result ? "Win" : "Loss";
            String historyEntry = String.format("Bet: %d, Result: %s, Balance: %d", bet, outcome, tokens);
            gameHistory.add(historyEntry);

            writer.write(historyEntry);
            writer.newLine();
            writer.flush();

            logger.info(historyEntry);
        } else {
            handleInsufficientTokens(writer);
        }
    }

    private void handleInsufficientTokens(BufferedWriter writer) throws IOException {
        String insufficientTokensMsg = "Insufficient tokens";

        writer.write(insufficientTokensMsg);
        writer.newLine();
        writer.flush();

        logger.warning(insufficientTokensMsg);
    }

    private void logGameSummary() {
        logger.info("Game session finished. Balance: " + tokens);
        logger.info("Game history:");
        gameHistory.forEach(logger::info);
    }
}

