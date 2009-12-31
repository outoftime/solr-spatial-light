package me.outofti.solrspatiallight;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;

class EmbeddedSolrServerFactory {
    private static EmbeddedSolrServerFactory instance;

    private EmbeddedSolrServer server;

    public static EmbeddedSolrServerFactory getInstance() {
        if(instance == null) {
            System.setProperty("solr.solr.home", "/home/mat/src/solr-spatial/test/solr"); //TODO Figure out how to make relative
            System.setProperty("java.util.logging.config.file", "home/mat/src/solr-spatial/test/logging.properties");
            instance = new EmbeddedSolrServerFactory();
        }
        return instance;
    }

    private EmbeddedSolrServerFactory() { }

    public EmbeddedSolrServer getServer() throws IOException, ParserConfigurationException, SAXException {
        if(this.server == null) {
            final CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            final CoreContainer coreContainer = initializer.initialize();
            this.server = new EmbeddedSolrServer(coreContainer, "");
        }
        return this.server;
    }
}
