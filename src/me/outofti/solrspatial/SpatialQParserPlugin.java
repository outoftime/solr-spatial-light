package me.outofti.solrspatial;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

public class SpatialQParserPlugin extends QParserPlugin { 
	private NamedList args;

	public void init(NamedList args) {
		this.args = args;
	}

	public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		return new SpatialQParser(qstr, localParams, params, req);
	}
}
