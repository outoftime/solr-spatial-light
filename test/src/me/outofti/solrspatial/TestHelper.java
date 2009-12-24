package me.outofti.solrspatial;

import org.apache.solr.client.solrj.SolrServer;

abstract class TestHelper {
	private SolrServer server;

	protected SolrServer getServer() throws Exception {
		if(server == null) {
			server = EmbeddedSolrServerFactory.getInstance().getServer();
			server.deleteByQuery("*:*");
		}
		return server;
	}
}
