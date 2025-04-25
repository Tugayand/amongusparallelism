package bench;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import data.Image;

import solutions.ParallelFinderSubtotals;
import solutions.ParallelFinderGlobalHashMap;
import solutions.SequentialFinder;

import java.util.AbstractMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class MeasureRuntimes {

    /**
     * Enum representing different strategies for benchmarking.
     */
    public enum Strategy {
        SEQUENTIAL,          // Sequential execution
        PARALLEL_GLOBAL,     // Parallel execution using a global HashMap
        PARALLEL_LOCAL,      // Parallel execution using local HashMaps
        THRESHOLD_TEST;      // Parallel execution with varying thresholds
    }

    static final List<Callable<Object>> strategies = List.of(
            sequential(),
            parallelGlobal(),
            parallelLocal(),
            thresholdTest()
    );

    public static List<Integer> useResults = new ArrayList<>(10000);

    public static void main(String[] args) {
        try {
            thresholdTest().call();
            int numCores = Runtime.getRuntime().availableProcessors();
            int[] coreCounts = new int[5];
            for (int i = 0; i < 5; i++) {
                coreCounts[i] = Math.max(1, (int) Math.round((double) numCores * i / 4));
            }
            int threshold = 500;
            int repetitions = 15;
            String imagePath = "images/place_23k_23k.png";
            testVaryingCores(coreCounts, threshold, repetitions, imagePath).call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Benchmarks a given task multiple times and writes the runtimes to a CSV file.
     *
     * @param task The task to benchmark (as a Callable).
     * @param repetitions The number of repetitions.
     * @param strategy The strategy being benchmarked.
     * @throws Exception If the task throws an exception.
     */
    public static long benchmark(Callable<Integer> task, int repetitions, String implementation, String threshold) throws Exception {
        List<Long> runtimes = new ArrayList<>(repetitions);

        for (int i = 0; i < repetitions; i++) {
            System.gc();
            long startTime = System.nanoTime();
            Integer result = task.call();
            long endTime = System.nanoTime();
            runtimes.add(endTime - startTime);
            useResults.add(result);
        }

        File file = new File("runtimes_" + implementation + ".csv");
        writeToCSV(file, implementation, threshold, runtimes);

        // Return the average runtime
        return runtimes.stream().mapToLong(Long::longValue).sum() / repetitions;
    }
    
    /**
     * Writes a list of runtimes to a CSV file.
     *
     * @param runtimes The list of runtimes in nanoseconds.
     * @param fileName The name of the CSV file.
     * @throws IOException If an I/O error occurs.
     */
    private static void writeToCSV(File file, String implementation, String threshold, List<Long> runtimes) {
        try (PrintWriter csvWriter = new PrintWriter(new FileOutputStream(file, true))) {
            StringBuilder line = new StringBuilder(implementation + "," + threshold);
            for (Long runtime : runtimes) {
                line.append(",").append(runtime);
            }
            csvWriter.println(line);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void saveResultsToFile(String fileName, int threshold, long Tseq, long T1, long TP, long overhead, double applicationSpeedup, double computationalSpeedup) {
        File file = new File(fileName);
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(file, true))) {
            writer.printf("%d,%d,%d,%d,%d,%.2f,%.2f%n", threshold, Tseq, T1, TP, overhead, applicationSpeedup, computationalSpeedup);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // note that i make use of callable object, inspired by www.naukri.com/code360/library/callable-interface-in-java#:~:text=Use%20Callable%20in%20Java%20when,better%20control%20and%20error%20handling.
    // essentially makes it possible for exception handling in multi-threaded environment
    static Callable<Object> thresholdTest() {
        return new Callable<Object>() {
            @Override
            public Object call() {
                int n_repetitions = 15;
                int[] thresholds = {10, 500, 2000, 5000, 10000};
                int numCores = Runtime.getRuntime().availableProcessors(); // P = number of cores

                String imagePath = "images/place_23k_23k.png";
                Image img = new Image(imagePath);

                try {
                    for (int threshold : thresholds) {
                        // Measure Tseq
                        SequentialFinder sequentialFinder = new SequentialFinder();
                        long Tseq = benchmark(new Callable<Integer>() {
                            @Override
                            public Integer call() throws Exception {
                                AbstractMap<Integer, Integer> result = sequentialFinder.countAmongiByColour(img);
                                return result.size();
                            }
                        }, n_repetitions, "Sequential", "Threshold: " + threshold);
                        // Measure T1
                        ParallelFinderSubtotals singleThreadFinder = new ParallelFinderSubtotals(1, threshold, Integer.MAX_VALUE);
                        long T1 = benchmark(new Callable<Integer>() {
                            @Override
                            public Integer call() throws Exception {
                                AbstractMap<Integer, Integer> result = singleThreadFinder.countAmongiByColour(img);
                                return result.size();
                            }
                        }, n_repetitions, "Single Thread", "Threshold: " + threshold);

                        // Measure TP
                        ParallelFinderSubtotals multiThreadFinder = new ParallelFinderSubtotals(numCores, threshold, Integer.MAX_VALUE);
                        long TP = benchmark(new Callable<Integer>() {
                            @Override
                            public Integer call() throws Exception {
                                AbstractMap<Integer, Integer> result = multiThreadFinder.countAmongiByColour(img);
                                return result.size();
                            }
                        }, n_repetitions, "Multi Thread", "Threshold: " + threshold);

                        // Calculate metrics
                        long overhead = T1 / Tseq;
                        double applicationSpeedup = (double) Tseq / TP;
                        double computationalSpeedup = (double) T1 / TP;

                        File file = new File("results.csv");
                        saveResultsToFile("results.csv", threshold, Tseq, T1, TP, overhead, applicationSpeedup, computationalSpeedup);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
    }

    static Callable<Object> testVaryingCores(int[] coreCounts, int threshold, int repetitions, String imagePath) {
        return new Callable<Object>() {
            @Override
            public Object call() {
                Image img = new Image(imagePath);

                try {
                    // Measure Tseq (Sequential execution time)
                    SequentialFinder sequentialFinder = new SequentialFinder();
                    long Tseq = benchmark(new Callable<Integer>() {
                        @Override
                        public Integer call() throws Exception {
                            AbstractMap<Integer, Integer> result = sequentialFinder.countAmongiByColour(img);
                            return result.size();
                        }
                    }, repetitions, "Cores Test Sequential", "Threshold: " + threshold);

                    // Measure T1 (Single-threaded execution time)
                    ParallelFinderGlobalHashMap globalSingleThread = new ParallelFinderGlobalHashMap(1, threshold);
                    long T1Global = benchmark(new Callable<Integer>() {
                        @Override
                        public Integer call() throws Exception {
                            AbstractMap<Integer, Integer> result = globalSingleThread.countAmongiByColour(img);
                            return result.size();
                        }
                    }, repetitions, "Global Single Thread", "Threshold: " + threshold);

                    ParallelFinderSubtotals localSingleThread = new ParallelFinderSubtotals(1, threshold, Integer.MAX_VALUE);
                    long T1Local = benchmark(new Callable<Integer>() {
                        @Override
                        public Integer call() throws Exception {
                            AbstractMap<Integer, Integer> result = localSingleThread.countAmongiByColour(img);
                            return result.size();
                        }
                    }, repetitions, "Local Single Thread", "Threshold: " + threshold);

                    // Loop through core counts and measure TP
                    for (int cores : coreCounts) {
                        if (cores <= 1) continue; // Skip single-core as Tseq and T1 already cover it

                        // Measure TP for Global HashMap
                        ParallelFinderGlobalHashMap globalMultiThread = new ParallelFinderGlobalHashMap(cores, threshold);
                        long TPGlobal = benchmark(new Callable<Integer>() {
                            @Override
                            public Integer call() throws Exception {
                                AbstractMap<Integer, Integer> result = globalMultiThread.countAmongiByColour(img);
                                return result.size();
                            }
                        }, repetitions, "Global Multi Thread", "Cores: " + cores);

                        // Measure TP for Local HashMap
                        ParallelFinderSubtotals localMultiThread = new ParallelFinderSubtotals(cores, threshold, Integer.MAX_VALUE);
                        long TPLocal = benchmark(new Callable<Integer>() {
                            @Override
                            public Integer call() throws Exception {
                                AbstractMap<Integer, Integer> result = localMultiThread.countAmongiByColour(img);
                                return result.size();
                            }
                        }, repetitions, "Local Multi Thread", "Cores: " + cores);

                        // Calculate metrics for Global HashMap
                        long overheadGlobal = T1Global / Tseq;
                        double applicationSpeedupGlobal = (double) Tseq / TPGlobal;
                        double computationalSpeedupGlobal = (double) T1Global / TPGlobal;

                        // Calculate metrics for Local HashMap
                        long overheadLocal = T1Local / Tseq;
                        double applicationSpeedupLocal = (double) Tseq / TPLocal;
                        double computationalSpeedupLocal = (double) T1Local / TPLocal;

                        File file = new File("results_global.csv");
                        File fileLocal = new File("results_local.csv");
                        // Save results
                        saveResultsToFile("results_global.csv", cores, Tseq, T1Global, TPGlobal, overheadGlobal, applicationSpeedupGlobal, computationalSpeedupGlobal);
                        saveResultsToFile("results_local.csv", cores, Tseq, T1Local, TPLocal, overheadLocal, applicationSpeedupLocal, computationalSpeedupLocal);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
    }

    /**
     * Tests the performance of the implementation using local HashMaps for various thresholds.
     *
     * @param thresholds The array of threshold values to test.
     * @param numCores The number of cores to use for parallel execution.
     */
    private static void testLocalHashMaps(int[] thresholds, int numCores, int repetitions) throws Exception {
        String imagePath = "images/place_23k_23k.png";
        Image img = new Image(imagePath);
        for (int threshold : thresholds) {
            ParallelFinderSubtotals finder = new ParallelFinderSubtotals(numCores, threshold, threshold);
            benchmark(() -> {
                AbstractMap<Integer, Integer> result = finder.countAmongiByColour(img);
                return result.size();
            }, repetitions, "Local HashMap", "Threshold: " + threshold);
        }
    }

    /**
     * Tests the performance of the implementation using a global HashMap for various thresholds.
     *
     * @param thresholds The array of threshold values to test.
     * @param numCores The number of cores to use for parallel execution.
     */
    private static void testGlobalHashMaps(int[] thresholds, int numCores, int repetitions) throws Exception {
        String imagePath = "images/place_23k_23k.png";
        Image img = new Image(imagePath);
        for (int threshold : thresholds) {
            ParallelFinderGlobalHashMap finder = new ParallelFinderGlobalHashMap(numCores, threshold);
            benchmark(() -> {
                AbstractMap<Integer, Integer> result = finder.countAmongiByColour(img);
                return result.size();
            }, repetitions, "Global HashMap", threshold + "");
        }
    }

    /**
     * Defines the sequential strategy.
     */
    static Callable<Object> sequential() {
        return () -> {
            System.out.println("Executing sequential strategy...");
            // Add your sequential implementation logic here
            return null;
        };
    }

    /**
     * Defines the parallel strategy using a global HashMap.
     */
    static Callable<Object> parallelGlobal() {
        return () -> {
            System.out.println("Executing parallel strategy with global HashMap...");
            // Add your parallel global HashMap implementation logic here
            return null;
        };
    }

    /**
     * Defines the parallel strategy using local HashMaps.
     */
    static Callable<Object> parallelLocal() {
        return () -> {
            System.out.println("Executing parallel strategy with local HashMaps...");
            // Add your parallel local HashMap implementation logic here
            return null;
        };
    }
}