package me.outofti.solrspatial;

import org.junit.Test;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrDocumentList;

import static org.junit.Assert.*;

public class DistanceSortQueryComponentTest extends TestHelper {
    @Test public void simpleDistanceSorting() throws Exception {
        addLocation("Brooklyn", 40.6501037, -73.9495823); //      5.328 miles
        addLocation("New York", 40.7142691, -74.0059729); //      0.000 miles
        addLocation("Yonkers", 40.9312099, -73.8987469);  //      16.000 miles
        addLocation("Staten Island", 40.5834379, -74.1495875); // 11.765 miles, but inside the bounding box
        getServer().commit();
        final SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.add("distanceSort", "40.714269,-74.0059729");
        assertResultsInOrder(query, "New York", "Brooklyn", "Staten Island",
                             "Yonkers");
    }
}
