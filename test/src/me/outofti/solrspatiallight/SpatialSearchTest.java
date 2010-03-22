package me.outofti.solrspatiallight;

import org.junit.Test;

import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrDocumentList;


import static org.junit.Assert.*;

public class SpatialSearchTest extends TestHelper {
    private static final String STANDARD_LAT_FIELD = "lat";
    private static final String STANDARD_LNG_FIELD = "lng";
    private static final String PARAM_NAME = "spatial";


    private static boolean firstRun = true;
    public static void firstRunComplete() {
        firstRun = false;
    }
    public static boolean isFirstRun() {
        return firstRun;
    }

    @Test public void simpleSpatialSearch() throws Exception {
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.add(PARAM_NAME, "{!radius=10}40.7142691, -74.0059729");
        assertResults(query, "New York", "Brooklyn");
    }

    @Test public void customFieldNames() throws Exception {
        addStandardFixtures("latitude", "longitude");
        final SolrQuery query = new SolrQuery();
        query.add("spatial",
                  "{!radius=10}latitude:40.7142691, longitude:-74.0059729");
        assertResults(query, "New York", "Brooklyn");
    }

    @Test public void withOtherFilters() throws Exception {
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.add("spatial", "{!radius=15}40.7142691, -74.0059729");
        query.addFilterQuery("rating:4.0");
        assertResults(query, "New York", "Staten Island");
    }

    @Test public void simpleDistanceSorting() throws Exception {
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.add("spatial", "{!sort=true}40.7142691, -74.0059729");
        assertResultsInOrder(query, "New York", "Brooklyn", "Staten Island",
                             "Yonkers");
    }

    @Test public void sortingWithOtherFilters() throws Exception {
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.add("spatial", "{!sort=true}40.7142691, -74.0059729");
        getServer().query(query);
        query.addFilterQuery("rating:4.0");
        assertResultsInOrder(query, "New York", "Staten Island", "Yonkers");
    }

    @Test public void specifiedLimit() throws Exception {
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.add("spatial", "{!sort=true}40.7142691, -74.0059729");
        query.setRows(2);
        assertResultsInOrder(query, "New York", "Brooklyn");
    }

    @Test public void compoundSort() throws Exception {
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.add("spatial", "{!sort=true}40.7142691, -74.0059729");
        query.addSortField("rating", SolrQuery.ORDER.desc);
        assertResultsInOrder(query, "Brooklyn", "New York", "Staten Island",
                             "Yonkers");
    }

    @Test public void dismaxWithSpatialSorting() throws Exception {
        addLocation("New Haven", 5.0, 41.3081527, -72.9281577);
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.add("defType", "dismax");
        query.add("q", "new");
        query.add("qf", "name_t other_t");
        query.add("spatial", "{!sort=true}40.7142691, -74.0059729");
        assertResults(query, "New York", "New Haven");
    }

    @Test public void sortsByDistanceWithDismax() throws Exception {
        addLocation("New Haven", 5.0, 41.3081527, -72.9281577);
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.add("defType", "dismax");
        query.add("q", "new haven");
        query.add("qf", "name_t other_t");
        query.add("mm", "1");
        query.add("spatial", "{!sort=true}40.7142691, -74.0059729");
        assertResultsInOrder(query, "New York", "New Haven");
    }

    @Test public void addsDistanceToResponse() throws Exception {
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.add(PARAM_NAME, "{!radius=10 sort=true}40.7142691, -74.0059729");
        assertResultDistancesInOrder(query);
    }

    /* Regression test for problem with empty distances the second time an
     * identical search is performed. */
    @Test public void addsDistanceToResponseInSecondSearch() throws Exception {
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.add(PARAM_NAME, "{!radius=10 sort=true}40.7142691, -74.0059729");
        getServer().query(query);
        firstRunComplete();
        getServer().query(query);
        assertResultDistancesInOrder(query);
    }

    @Test public void addsDistanceToResponseWithoutRadius() throws Exception {
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.add(PARAM_NAME, "{!sort=true}40.7142691, -74.0059729");
        assertResultDistancesInOrder(query);
    }
    
    @Test public void onlyAddsDistancesFromResults() throws Exception {
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.add(PARAM_NAME, "{!sort=true}40.7142691, -74.0059729");
        query.setRows(2);
        assertResultDistancesInOrder(query);
    }

    @Test public void searchWithoutSpatial() throws Exception {
        addStandardFixtures();
        final SolrQuery query = new SolrQuery();
        query.addFilterQuery("rating:4.0");
        assertResults(query, "New York", "Staten Island", "Yonkers");
    }

    private void addStandardFixtures() throws Exception {
        addStandardFixtures(STANDARD_LAT_FIELD, STANDARD_LNG_FIELD);
    }

    private void addStandardFixtures(String latField, String lngField) throws Exception {
        // 11.765 miles, but inside the bounding box
        addLocation("Staten Island", latField, lngField, 4.0, 40.5834379, -74.1495875);
        // 0.000 miles
        addLocation("New York", latField, lngField, 4.0, 40.7142691, -74.0059729);
        // 16.000 miles
        addLocation("Yonkers", latField, lngField, 4.0, 40.9312099, -73.8987469);
        // 5.328 miles
        addLocation("Brooklyn", latField, lngField, 5.0, 40.6501037, -73.9495823);
        getServer().commit();
    }
}
