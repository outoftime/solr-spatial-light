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
            System.setProperty("solr.solr.home", "test/solr");
            System.setProperty("java.util.logging.config.file", "test/logging.properties");
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
