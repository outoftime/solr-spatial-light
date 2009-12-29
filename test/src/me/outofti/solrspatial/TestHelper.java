package me.outofti.solrspatial;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import static org.junit.Assert.*;

abstract class TestHelper {
    private SolrServer server;
    private int nextId = 1;

    protected SolrServer getServer() throws Exception {
        if(server == null) {
            server = EmbeddedSolrServerFactory.getInstance().getServer();
            server.deleteByQuery("*:*");
        }
        return server;
    }

    protected void addLocation(String name, double lat, double lng) throws Exception {
        final SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", new Integer(nextId++).toString());
        doc.addField("name", name);
        doc.addField("lat", new Double(lat).toString());
        doc.addField("lng", new Double(lng).toString());
        getServer().add(doc);
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

    private void assertCountMatches(String[] names, SolrDocumentList docs) throws Exception {
        assertEquals("It should return " + names.length + " results",
                     names.length, docs.size());
    }
}
