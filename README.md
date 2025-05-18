A multithreaded console application that reads numbers from an input file, calculates their factorials using a thread pool, 
and writes the results to an output file in the original order. 
The application also enforces a global computation rate limit of 100 factorials per second across all threads.

  **Features:**

- **Reader thread:** Reads numbers from input.txt and passes them to the computation pool.

- **Computation pool:** A configurable number of threads compute factorials with caching and rate limiting.

- **Writer thread:** Collects results and writes them to output.txt in the order of the original input.

- **Rate limiting:** Enforces a global limit of 100 computations per second using Semaphore, ScheduledExecutorService.

- **Thread-safe communication:** Uses BlockingQueue, PriorityBlockingQueue, and atomic counters for coordination.

