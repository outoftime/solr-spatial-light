package me.outofti.solrspatiallight;

import org.apache.solr.client.solrj.SolrQuery;

public class SolrSpatialBenchmark extends TestHelper {
    private final int documents;
    private final double distanceRatio;
    private final double filterRatio;
    private final boolean sort;

    private SolrQuery query;

    public SolrSpatialBenchmark(final int numDocuments,
                                final double distanceHitRatio,
                                final double filterHitRatio,
                                final boolean doSort) {
        documents = numDocuments;
        distanceRatio = distanceHitRatio;
        filterRatio = filterHitRatio;
        sort = doSort;
    }

    public final void prepare() throws Exception {
        getServer().deleteByQuery("*:*");
        final int sqrt = (int) Math.floor(Math.sqrt(documents));
        for(int i = 0; i < documents; i++) {
            final int filterPosition = i / sqrt;
            final int distancePosition = i % sqrt;
            addLocation("Location " + i, (float) filterPosition / sqrt * 5.0,
                        (float) distancePosition / sqrt * 100.0 / 69.047, 0.0);
        }
        getServer().commit();
        query = new SolrQuery();
        query.setQuery("*:*");
        query.add("spatial", "{!radius=" + (distanceRatio * 100.0)
                             + " sort=" + sort + "}0.0,0.0");
        query.addFilterQuery("rating:[0.0 TO " + (filterRatio * 5.0) + "]");
    }

    public final void run() throws Exception {
        getServer().query(query);
    }

    public final void outputDescriptionForTime(double timeInMillis) {
        System.out.format(
                "%10d Documents; %.2f Filter Hit Rate; %.2f Distance Hit Rate: %5.2fms%n",
                documents, filterRatio, distanceRatio, timeInMillis);
    }
}
