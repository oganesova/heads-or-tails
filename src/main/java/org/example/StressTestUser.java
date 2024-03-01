package org.example;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StressTestUser implements Runnable {
    private static final Logger logger = Logger.getLogger(StressTestUser.class.getName());
    private final int userId;
    private final int intervalBetweenRequests;
    private final int numRequests;
    private final CountDownLatch latch;
    private final AtomicInteger successfulRequests;
    private final AtomicInteger failedRequests;
    private final AtomicLong totalRequestTime;

    public StressTestUser(int userId, int intervalBetweenRequests, int numRequests, CountDownLatch latch) {
        this.userId = userId;
        this.intervalBetweenRequests = intervalBetweenRequests;
        this.numRequests = numRequests;
        this.latch = latch;
        this.successfulRequests = new AtomicInteger(0);
        this.failedRequests = new AtomicInteger(0);
        this.totalRequestTime = new AtomicLong(0);
    }

    public int getUserId() {
        return userId;
    }

    public AtomicInteger getSuccessfulRequests() {
        return successfulRequests;
    }

    public AtomicInteger getFailedRequests() {
        return failedRequests;
    }

    public double getAverageRequestTime() {
        return numRequests > 0 ? (double) totalRequestTime.get() / numRequests : 0;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < numRequests; i++) {
                long startTime = System.currentTimeMillis();

                try (Socket socket = new Socket("localhost", 12345);
                     OutputStream outputStream = socket.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    int bet = (int) (Math.random() * 20) + 1;
                    writer.write(String.valueOf(bet));
                    writer.newLine();
                    writer.flush();

                    String response = reader.readLine();
                    if (response != null) {
                        successfulRequests.incrementAndGet();
                    } else {
                        failedRequests.incrementAndGet();
                    }

                    long endTime = System.currentTimeMillis();
                    totalRequestTime.addAndGet(endTime - startTime);

                    Thread.sleep(intervalBetweenRequests);
                } catch (IOException | InterruptedException e) {
                    failedRequests.incrementAndGet();
                }
            }
        } finally {
            latch.countDown();
        }
    }
}