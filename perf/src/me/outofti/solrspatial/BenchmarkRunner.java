package me.outofti.solrspatial;

public class BenchmarkRunner {
    private static final int RUNS = 100;

    public static void main(String[] argv) throws Exception {
        for (int numDocuments = 100; numDocuments <= 10000; numDocuments *= 10) {
            for (double filterRatio = 0.1; filterRatio <= 1.0; filterRatio += 0.1) {
                for (double distanceRatio = 0.1; distanceRatio <= 1.0; distanceRatio += 0.1) {
                    SolrSpatialBenchmark benchmark =
                        new SolrSpatialBenchmark(numDocuments, filterRatio,
                                                 distanceRatio, true);
                    long totalTime = 0L;
                    benchmark.prepare();
                    for (int run = 0; run < RUNS; run++) {
                        final long startTime = System.currentTimeMillis();
                        benchmark.run();
                        totalTime += System.currentTimeMillis() - startTime;
                    }
                    benchmark.outputDescriptionForTime((double) totalTime / (double) RUNS);
                }
            }
        }
        System.exit(0);
    }
}
