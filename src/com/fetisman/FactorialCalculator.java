package com.fetisman;

import java.math.BigInteger;

public class FactorialCalculator {
    public static BigInteger calculateFactorial(int n) {
        if (n < 0) throw new IllegalArgumentException("Negative numbers not allowed");

        GlobalRateLimiter.acquire();

        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }
}

