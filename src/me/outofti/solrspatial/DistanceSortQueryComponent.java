package me.outofti.solrspatial;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.spatial.tier.DistanceFieldComparatorSource;
import org.apache.lucene.spatial.tier.LatLongDistanceFilter;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.search.SortSpec;

/**
 * A query component to sort the search by geographical distance from a certain
 * point.
 */
public class DistanceSortQueryComponent extends SearchComponent {
    /**
     * Pattern the distanceSort parameter should conform to.
     */
    private static final Pattern PATTERN =
        Pattern.compile("(-?\\d+(\\.\\d+)),(-?\\d+(\\.\\d+))");

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
        final String distanceSort = rb.req.getParams().get("distanceSort");
        if (distanceSort == null) { return; }
        final Matcher matcher = PATTERN.matcher(distanceSort);
        if (!matcher.matches()) {
            throw new IOException("Badly formatted lat/lng: " + distanceSort);
        }

        final double lat = Double.parseDouble(matcher.group(1));
        final double lng = Double.parseDouble(matcher.group(3));

        final String latField = "lat"; //XXX Specify this in params
        final String lngField = "lng"; //XXX Specify this in params

        //FIXME don't stomp on existing sorts
        final Filter distanceFilter = new LatLongDistanceFilter(
                new QueryWrapperFilter(new MatchAllDocsQuery()), lat, lng, 100,
                                       latField, lngField);

        final Query query = rb.getQuery();
        final Query distanceFilteredQuery =
            new FilteredQuery(query, distanceFilter);
        rb.setQuery(distanceFilteredQuery);

        final FieldComparatorSource dfcs =
            new DistanceFieldComparatorSource(distanceFilter);
        final Sort sort = new Sort(new SortField(latField, dfcs));
        SortSpec sortSpec = rb.getSortSpec();
        if (sortSpec == null) {
            //FIXME get the actual count
            sortSpec = new SortSpec(sort, DEFAULT_COUNT);
            rb.setSortSpec(sortSpec);
        } else {
            sortSpec.setSort(sort);
        }
    }

    /**
     * Execute the query.
     *
     * @param rb the response builder
     */
    public final void process(final ResponseBuilder rb) {
        // TODO
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
