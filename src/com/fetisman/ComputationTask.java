package com.fetisman;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ComputationTask implements Runnable {
    private final BlockingQueue<IndexedValue> readToComputeQueue;
    private final BlockingQueue<IndexedResult> computeToWriteQueue;
    private final ConcurrentHashMap<Integer, BigInteger> factorialCache;

    public ComputationTask(BlockingQueue<IndexedValue> readToComputeQueue, BlockingQueue<IndexedResult> computeToWriteQueue, ConcurrentHashMap<Integer, BigInteger> factorialCache) {
        this.readToComputeQueue = readToComputeQueue;
        this.computeToWriteQueue = computeToWriteQueue;
        this.factorialCache = factorialCache;
    }

    @Override
    public void run() {
        try {
            while (true) {
                IndexedValue val = readToComputeQueue.poll(1, TimeUnit.SECONDS);
                if (val == null) continue;
                if (val.value() == Integer.MIN_VALUE) break;

                BigInteger fact = factorialCache.computeIfAbsent(val.value(), FactorialCalculator::calculateFactorial);
                computeToWriteQueue.add(new IndexedResult(val.index(), val.value(), fact));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
