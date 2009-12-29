package me.outofti.solrspatial;

import java.io.IOException;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.FilteredQuery;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SortSpec;

/**
 * A query component that handles spatial queries and adds the appropriate
 * filter and/or sort.
 */
public class SpatialQueryComponent extends SearchComponent {
    /**
     * Spatial query parameter name.
     */
    private static final String PARAM = "spatial";

    /**
     * Temporary.
     */
    private static final int DEFAULT_COUNT = 30;

    /**
     * Prepare the response.
     *
     * @param rb the response builder
     *
     * @throws IOException if param is badly formed
     */
    public final void prepare(final ResponseBuilder rb) throws IOException {
        final String queryWithLocalParams = rb.req.getParams().get(PARAM);
        if (queryWithLocalParams == null) { return; }

        try {
            final SolrParams localParams =
                extractLocalParams(queryWithLocalParams, rb);
            final String qstr = localParams.get("v");

            final Spatial spatial = new Spatial(qstr, localParams,
                                                rb.req.getParams(), rb.req);
            attachDistanceFilter(rb, spatial);

            if (localParams.getBool("sort", false)) {
                attachSort(rb, spatial);
            }
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /**
     * Execute the query.
     *
     * @param rb the response builder
     */
    public final void process(final ResponseBuilder rb) { }

    /**
     * Attach the distance filter to the query.
     *
     * @param rb      response builder
     * @param spatial spatial query parser
     *
     * @throws ParseException if query is malformed
     */
    private void attachDistanceFilter(final ResponseBuilder rb,
                                            final Spatial spatial)
        throws ParseException {
        rb.setQuery(new FilteredQuery(rb.getQuery(),
                                      spatial.getDistanceFilter()));
    }

    /**
     * Attach a distance sort to the search.
     *
     * @param rb      response builder
     * @param spatial spatial query parser
     *
     * @throws ParseException if query is malformed
     */
    private void attachSort(final ResponseBuilder rb,
                            final Spatial spatial)
        throws ParseException {
        SortSpec sortSpec = rb.getSortSpec();

        if (sortSpec == null) {
            //FIXME get the actual count
            sortSpec = new SortSpec(spatial.getSort(), DEFAULT_COUNT);
            rb.setSortSpec(sortSpec);
        } else {
            //FIXME don't clobber existing sort
            sortSpec.setSort(spatial.getSort());
        }
    }

    /**
     * Extract local parameters, if any, from the given string.
     *
     * @param qstr raw query string
     * @param rb   response builder
     *
     * @throws ParseException if query is malformed
     * @return local params, containing at least a "v" parameter for the query
     *         itself
     */
    private SolrParams extractLocalParams(final String qstr,
                                          final ResponseBuilder rb)
        throws ParseException {
        final SolrParams localParams =
            QueryParsing.getLocalParams(qstr, rb.req.getParams());

        if (localParams != null) {
            return localParams;
        }
        final ModifiableSolrParams modifiableParams =
            new ModifiableSolrParams();
        modifiableParams.set("v", qstr);
        return modifiableParams;
    }

    /////////////////////////
    //    SolrInfoMBean    //
    /////////////////////////

    /**
     * Name of the class.
     *
     * @return description
     */
    public final String getDescription() {
        return "distance";
    }

    /**
     * Source.
     *
     * @return source
     */
    public final String getSource() {
        return ""; // TODO
    }

    /**
     * Source ID.
     *
     * @return source ID
     */
    public final String getSourceId() {
        return ""; // TODO
    }

    /**
     * Revision.
     *
     * @return revision number
     */
    public final String getVersion() {
        return ""; // TODO
    }
}
