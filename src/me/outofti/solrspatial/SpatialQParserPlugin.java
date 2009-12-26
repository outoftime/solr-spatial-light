package me.outofti.solrspatial;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

/**
 * A plugin that generates a query parser to perform spatial distance searches.
 *
 * The query parser builds a filter query using the LatLongDistanceFilter in the
 * lucene-spatial contrib. The body of the query should simply be a latitude and
 * longitude, separated by a comma and optional whitespace. The query can also
 * take several local params:
 *
 * latField: The latitude field to search against for the distance query.
 *           Default "lat".
 * lngField: The longitude field to search against for the distance query.
 *           Default "lng".
 * radius: The maximum distance from the centerpoint at which results can lie.
 *         Default 1.0.
 */
public class SpatialQParserPlugin extends QParserPlugin {
    /**
     * Plugin arguments.
     */
    private NamedList args;

    /**
     * Initialize the plugin.
     *
     * @param initArgs list of arguments
     *
     */
    public final void init(final NamedList initArgs) {
        args = initArgs;
    }

    /**
     * Return a spatial query parser.
     *
     * @param qstr               query string
     * @param localParams local parameters
     * @param params      query parameters
     * @param req         query request
     *
     * @return spatial query parser
     */
    public final QParser createParser(final String qstr,
                                      final SolrParams localParams,
                                      final SolrParams params,
                                      final SolrQueryRequest req) {
        return new SpatialQParser(qstr, localParams, params, req);
    }
}
