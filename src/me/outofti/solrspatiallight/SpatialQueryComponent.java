package me.outofti.solrspatiallight;

import java.io.IOException;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SortSpec;

/**
 * Solr support for spatial search.
 *
 * This query component extends Solr to support the lucene-spatial contrib,
 * which allows filtering and/or sorting based on geographical distance from a
 * given latitude and longitude.
 *
 * All spatial information is passed in the "spatial" parameter, which takes
 * the usual local param format. The following local params are accepted:
 *
 * <table>
 *   <tr>
 *     <th>radius</th>
 *     <td>
 *       Radius in miles to which to filter the search results, as a float. By
 *       default, results are not filtered geographically.
 *     </td>
 *   </tr>
 *   <tr>
 *     <th>sort</th>
 *     <td>
 *       true or false: whether to sort the results by distance from the
 *       centerpoint. If other sorts are specified via the "sort" parameter,
 *       they will take precedence over the geographical sort.
 *     </td>
 *   </tr>
 * </table>
 *
 * As the above indicates, if no local parameters are passed, the spatial
 * component has no effect (except on performance).
 *
 * The query itself specifies the geographical centerpoint and, optionally,
 * the names of the latitude and longitude fields to be searched, in the
 * format <strong>[latField:]lat,[lngField:]lng</strong> . If the field names
 * are not specified, they default to <strong>lat</strong> and
 * <strong>lng</strong>.
 *
 * The latitude and longitude fields <strong>must be of the type
 * TrieDoubleField</strong>.
 *
 * <h4>Examples</h4>
 *
 * <table>
 *   <tr>
 *     <th>{!radius=10.0}40.65,-73.95</th>
 *     <td>
 *       Filter results to documents whose <strong>lat</strong> and
 *       <strong>lng</strong> fields contain a point within 10 miles of
 *       &lt;40.65,-73.95&gt;.
 *     </td>
 *   </tr>
 *   <tr>
 *     <th>{!sort=true}latitude:40.65,longitude:-73.95</th>
 *     <td>
 *       Sort results in ascending order of proximity to &lt;40.65,-73.95&gt;
 *       as indexed in their <strong>latitude</strong> and
 *       <strong>longitude</strong> fields.
 *     </td>
 *   </tr>
 * </table>
 *
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
     * If no search is specified, then distance sort becomes the sole sort. If
     * a search is specified, then distance sort is attached as the *last* sort.
     *
     * @param rb      response builder
     * @param spatial spatial query parser
     *
     * @throws ParseException if query is malformed
     */
    private void attachSort(final ResponseBuilder rb,
                            final Spatial spatial)
        throws ParseException {
        final SortSpec sortSpec = rb.getSortSpec();
        final Sort sort = sortSpec.getSort();
        final SortField sortField = spatial.getSortField();

        if (sort == null) {
            sortSpec.setSort(new Sort(sortField));
        } else {
            final SortField[] existingSortFields = sort.getSort();
            final SortField[] sortFields =
                new SortField[existingSortFields.length + 1];
            int i;
            for (i = 0; i < existingSortFields.length; i++) {
                sortFields[i] = existingSortFields[i];
            }
            sortFields[i] = sortField;
            sortSpec.setSort(new Sort(sortFields));
            System.out.println("");
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
