package com.fetisman;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class ReaderTask implements Runnable {
    private final FileReader file;
    private final int numComputeThreads;
    private final BlockingQueue<IndexedValue> readToComputeQueue;

    public ReaderTask(BlockingQueue<IndexedValue> readToComputeQueue, FileReader file, int numComputeThreads) {
        this.readToComputeQueue = readToComputeQueue;
        this.file = file;
        this.numComputeThreads = numComputeThreads;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(file)) {
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    int num = Integer.parseInt(line.trim());
                    readToComputeQueue.put(new IndexedValue(index++, num)); // is blocked if queue is full
                } catch (NumberFormatException e) {
                    System.err.println("Error parse line: " + line);
                }
            }

            for (int i = 0; i < numComputeThreads; i++) {
                readToComputeQueue.put(new IndexedValue(index++, Integer.MIN_VALUE)); // the signal to entire of threads in the pool about read data end
            }
        } catch (InterruptedException e) {
            System.err.println("ReaderTask interrupted: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("ReaderTask I/O error: " + e.getMessage());
        }
    }
}
