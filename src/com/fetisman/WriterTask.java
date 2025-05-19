package com.fetisman;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WriterTask implements Runnable {
    private final AtomicInteger expectedIndex = new AtomicInteger(0);
    private final BlockingQueue<IndexedResult> computeToWriteQueue;
    private final PrintWriter writer;
    private final AtomicBoolean isDone;

    public WriterTask(BlockingQueue<IndexedResult> computeToWriteQueue, PrintWriter writer, AtomicBoolean isDone) {
        this.computeToWriteQueue = computeToWriteQueue;
        this.writer = writer;
        this.isDone = isDone;
    }

    @Override
    public void run() {
        while (!isDone.get() || !computeToWriteQueue.isEmpty()) {
            IndexedResult peeked;
            if ((peeked = computeToWriteQueue.peek()) != null && peeked.index() == expectedIndex.get()) {
                IndexedResult result = computeToWriteQueue.poll();
                try {
                    writer.println(result.value() + " = " + result.result());
                } catch (Exception e) {
                    System.err.println("Error while writing to file: " + e.getMessage());
                    e.printStackTrace();
                }
                expectedIndex.incrementAndGet();
            } else {
                try {
                    Thread.sleep(10); // prevents busy waiting
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

}
