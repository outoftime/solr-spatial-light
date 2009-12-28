package me.outofti.solrspatial;

import org.junit.Test;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;

public class SpatialQParserTest extends TestHelper {
    @Test public void simpleSpatialSearch() throws Exception {
        addLocation("New York", 40.7142691, -74.0059729); //      0.000 miles
        addLocation("Brooklyn", 40.6501037, -73.9495823); //      5.328 miles
        addLocation("Staten Island", 40.5834379, -74.1495875); // 11.765 miles, but inside the bounding box
        addLocation("Yonkers", 40.9312099, -73.8987469);  //      16.000 miles
        getServer().commit();
        final SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.addFilterQuery("{!spatial radius=10}40.7142691, -74.0059729");
        assertResultsInOrder(query, "New York", "Brooklyn");
    }
}
