package me.outofti.solrspatial;

import org.junit.Test;
import org.junit.Assert;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import static org.junit.Assert.*;

public class SpatialQParserTest extends TestHelper {
	private int nextId = 1;

	@Test public void simpleSpatialSearch() throws Exception {
		final SolrServer server = getServer();
		addLocation("New York", 40.7142691, -74.0059729); //  0.000 miles
		addLocation("Brooklyn", 40.6501037, -73.9495823); //  5.328 miles
		addLocation("Yonkers", 40.9312099, -73.8987469);  // 16.000 miles
		server.commit();
		final SolrQuery query = new SolrQuery();
		query.setQuery("*:*");
		query.addFilterQuery("{!spatial radius=10}40.7142691, -74.0059729");
		final SolrDocumentList docs = server.query(query).getResults();
		assertEquals("It should return 2 results", 2, docs.size());
		assertEquals("It should return New York", "New York", docs.get(0).getFieldValue("name"));
		assertEquals("It should return Brooklyn", "Brooklyn", docs.get(1).getFieldValue("name"));
	}

	private void addLocation(String name, double lat, double lng) throws Exception {
		final SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", new Integer(nextId++).toString());
		doc.addField("name", name);
		doc.addField("lat", new Double(lat).toString());
		doc.addField("lng", new Double(lng).toString());
		getServer().add(doc);
	}
}
