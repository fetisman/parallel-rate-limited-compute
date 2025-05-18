package com.fetisman;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FactorialApp {
    public BlockingQueue<IndexedValue> readToComputeQueue;
    public BlockingQueue<IndexedResult> computeToWriteQueue = new PriorityBlockingQueue<>();
    public ConcurrentHashMap<Integer, BigInteger> factorialCache = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException, IOException {
        FactorialApp app = new FactorialApp();
        app.startApp();
    }

    private void startApp() throws InterruptedException, IOException {
        int NUM_COMPUTE_THREADS;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Input number of compute threads: ");
            try {
                int input = Integer.parseInt(scanner.nextLine());
                if (input > 0) {
                    NUM_COMPUTE_THREADS = input;
                    readToComputeQueue = new LinkedBlockingQueue<>(input * 100);
                    break;
                } else {
                    System.out.println("Error: number must be greater than 0.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: not a valid integer.");
            }
        }

        ExecutorService readerExecutor = Executors.newSingleThreadExecutor();
        readerExecutor.submit(new ReaderTask(readToComputeQueue, new FileReader("input.txt"), NUM_COMPUTE_THREADS));
        readerExecutor.shutdown();

        ExecutorService computePool = Executors.newFixedThreadPool(NUM_COMPUTE_THREADS);
        for (int i = 0; i < NUM_COMPUTE_THREADS; i++) {
            computePool.submit(new ComputationTask(readToComputeQueue, computeToWriteQueue, factorialCache));
        }
        computePool.shutdown();

        AtomicBoolean isDone = new AtomicBoolean(false);
        PrintWriter writer = new PrintWriter(new FileWriter("output.txt"));
        ExecutorService writeExecutor = Executors.newSingleThreadExecutor();
        writeExecutor.submit(new WriterTask(computeToWriteQueue, writer, isDone));
        writeExecutor.shutdown();

        isDone.set(true);
        boolean terminated = computePool.awaitTermination(NUM_COMPUTE_THREADS * 10L, TimeUnit.SECONDS);
        if (!terminated) {
            System.err.println("Some threads of computePool did not finish within the allotted time.");
        }
        GlobalRateLimiter.shutdown();

    }
}

record IndexedValue(int index, int value) {}

record IndexedResult(int index, int value, BigInteger result) implements Comparable<IndexedResult> {
    @Override
    public int compareTo(IndexedResult o) {
        return Integer.compare(this.index, o.index);
    }
}
