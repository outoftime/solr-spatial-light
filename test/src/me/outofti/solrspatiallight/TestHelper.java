package me.outofti.solrspatiallight;

import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import static org.junit.Assert.*;

abstract class TestHelper {
    private SolrServer server;
    private int nextId = 1;

    protected SolrServer getServer() throws Exception {
        if(server == null) {
            server = EmbeddedSolrServerFactory.getInstance().getServer();
            server.deleteByQuery("id:[* TO *]");
        }
        return server;
    }

    protected void addLocation(String name, String latField, String lngField,
                               double rating, double lat, double lng) throws Exception {
        final SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", new Integer(nextId++).toString());
        doc.addField("name", name);
        doc.addField("rating", rating);
        doc.addField("name_t", name);
        doc.addField(latField, new Double(lat).toString());
        doc.addField(lngField, new Double(lng).toString());
        getServer().add(doc);
    }

    protected void addLocation(String name, double rating, double lat, double lng) throws Exception {
        addLocation(name, "lat", "lng", rating, lat, lng);
    }

    protected void assertResults(SolrQuery query, String... names) throws Exception {
        final SolrDocumentList docs = getServer().query(query).getResults();
        assertCountMatches(names, docs);
        for(final String name : names) {
            boolean success = false;
            for(int i = 0; i < docs.size(); i++) {
                if (docs.get(i).getFieldValue("name").toString().equals(name)) {
                    success = true;
                    break;
                }
            }
            if (!success) { fail("Expected " + name + " in results"); }
        }
    }

    protected void assertResultsInOrder(SolrQuery query, String... names) throws Exception {
        final SolrDocumentList docs = getServer().query(query).getResults();
        assertCountMatches(names, docs);
        for(int i = 0; i < names.length; i++) {
            final String name = names[i];
            assertEquals("It should return \"" + name + "\" as result " + i,
                         name, docs.get(i).getFieldValue("name").toString());
        }
    }

    protected void assertResultDistancesInOrder(SolrQuery query) throws Exception {
        final QueryResponse response = getServer().query(query);
        final SolrDocumentList docs = response.getResults();
        final Map distances =
            (Map) response.getResponse().get("distances");
        Double lastDistance = null;
        assertEquals("Expected " + docs.size() + " distances", docs.size(), distances.size());
        for(int i = 0; i < docs.size(); i++) {
            final Double distance = new Double(distances.get(docs.get(i).getFieldValue("id")).toString());
            assertNotNull(distance);
            if (lastDistance != null) {
                assertTrue(lastDistance < distance);
            }
            lastDistance = distance;
        }
    }

    private void assertCountMatches(String[] names, SolrDocumentList docs) throws Exception {
        assertEquals("It should return " + names.length + " results",
                     names.length, docs.size());
    }
}
