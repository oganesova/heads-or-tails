package org.example;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StressTestClient {
    private final String serverAddress = "localhost";
    private final int serverPort = 12345;
    private static final Logger logger = Logger.getLogger(StressTestClient.class.getName());
    private List<StressTestUser> users = new ArrayList<>();

    static {
        logger.addHandler(new ConsoleHandler());
        logger.setLevel(Level.INFO);
    }

    public static void main(String[] args) {
        StressTestClient stressTestClient = new StressTestClient();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the number of users: ");
        int numUsers = scanner.nextInt();

        System.out.print("Enter the interval between requests (in milliseconds): ");
        int intervalBetweenRequests = scanner.nextInt();

        System.out.print("Enter the number of requests per user: ");
        int numRequestsPerUser = scanner.nextInt();

        scanner.close();

        stressTestClient.runStressTest(numUsers, intervalBetweenRequests, numRequestsPerUser);
    }

    public void runStressTest(int numUsers, int intervalBetweenRequests, int numRequests) {
        CountDownLatch latch = new CountDownLatch(numUsers);

        for (int i = 0; i < numUsers; i++) {
            StressTestUser user = new StressTestUser(i + 1, intervalBetweenRequests, numRequests, latch);
            new Thread(user).start();
            users.add(user);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error waiting for users to finish", e);
        }
        displayResults(users);
    }

    private void displayResults(List<StressTestUser> users) {
        System.out.println("Results of the stress test:");
        System.out.println("User | Successful Requests | Failed Requests | Average Request Time");

        for (StressTestUser user : users) {
            System.out.printf("%4d | %19d | %15d | %20.2f%n",
                    user.getUserId(), user.getSuccessfulRequests().get(), user.getFailedRequests().get(), user.getAverageRequestTime());
        }
    }
}
