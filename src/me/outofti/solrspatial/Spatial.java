package me.outofti.solrspatial;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilterClause;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.spatial.tier.DistanceFilter;
import org.apache.lucene.spatial.tier.DistanceFieldComparatorSource;
import org.apache.lucene.spatial.tier.LatLongDistanceFilter;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;

/**
 * Encapsulates a spatial query component.
 */
public class Spatial extends QParser {
    /**
     * Valid query pattern.
     */
    private static final Pattern PATTERN =
        Pattern.compile("^(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)$");

    /**
     * Ratio between latitude degrees and statute miles.
     */
    private static final double DEGREES_TO_MILES = 69.047;

    /**
     * Default radius for search.
     *
     * Use half of earth's circumference, making the filter essentially a
     * no-op. We do this because the filter might still be used for sorting,
     * and it needs to be used for the distances to lazy-load.
     */
    private static final double DEFAULT_RADIUS = 24901.46;

    /**
     * Store memoized response of getDistanceFilter() method.
     */
    private DistanceFilter distanceFilter;

    /**
     * Store memoized response of getSort() method.
     */
    private Sort sort;

    /**
     * Construct the object using the superclass arguments.
     *
     * @param qstr        query string
     * @param localParams local parameters
     * @param params      global params
     * @param req         request
     */
    public Spatial(final String qstr, final SolrParams localParams,
                   final SolrParams params, final SolrQueryRequest req) {
        super(qstr, localParams, params, req);
    }

    /**
     * Return the distance filter as a Query.
     *
     * @throws ParseException if query formatting is bad
     * @return distance filter wrapped in Query
     */
    public final Query parse() throws ParseException {
        return new ConstantScoreQuery(getDistanceFilter());
    }

    /**
     * Build a distance filter out of a spatial query string.
     *
     * @throws ParseException if query formatting is bad
     * @return a distance filter
     */
    public final DistanceFilter getDistanceFilter() throws ParseException {
        if (distanceFilter == null) {
            String latLng;
            Float maybeMiles = null;
            if (localParams == null) {
                latLng = qstr;
            } else {
                latLng = localParams.get("v");
                maybeMiles = localParams.getFloat("radius");
            }

            final Matcher matcher = PATTERN.matcher(qstr);
            if (!matcher.matches()) {
                throw new ParseException(
                        "Spatial queries should be of the format LAT,LNG");
            }

            final double lat = Double.parseDouble(matcher.group(1));
            final double lng = Double.parseDouble(matcher.group(3));

            final String latField = "lat"; //TODO Parse this out of the query
            final String lngField = "lng"; //TODO Parse this out of the query

            double miles;
            Filter startingFilter;
            if (maybeMiles == null) {
                miles = DEFAULT_RADIUS;
                startingFilter =
                    new QueryWrapperFilter(new MatchAllDocsQuery());
            } else {
                miles = maybeMiles.doubleValue();
                startingFilter =
                    getBoundingBoxFilter(lat, lng, miles, latField, lngField);
            }
            distanceFilter = new LatLongDistanceFilter(
                    startingFilter, lat, lng, miles, latField, lngField);
        }
        return distanceFilter;
    }

    /**
     * Return a sort by distance based on the distance filter.
     *
     * @throws ParseException if params are malformed
     * @return sort by distance
     */
    public final Sort getSort() throws ParseException {
        if (sort == null) {
            final FieldComparatorSource dfcs =
                new DistanceFieldComparatorSource(getDistanceFilter());
            sort = new Sort(new SortField("dummy", dfcs));
        }
        return sort;
    }

    /**
     * Get a bounding box filter restricting results to a given bounding box
     * around a coordinate pair.
     *
     * @param lat      The latitude of the centerpoint of the bounding box.
     * @param lng      The longitude of the centerpoint of the bounding box.
     * @param miles    The size of the bounding box "radius" in miles.
     * @param latField The name of the field in which latitude is indexed.
     * @param lngField The name of the field in which longitude is indexed.
     *
     * @return filter restricting results to the given bounding box
     */
    private Filter getBoundingBoxFilter(final double lat, final double lng,
                                        final double miles,
                                        final String latField,
                                        final String lngField) {
        final double latRadius = Math.abs(miles / DEGREES_TO_MILES);
        final double lngRadius = Math.abs(miles / DEGREES_TO_MILES
                                          * Math.cos(lat));

        final BooleanFilter filter = new BooleanFilter();

        filter.add(new FilterClause(getBoundingFilter(latField, lat, latRadius),
                                    BooleanClause.Occur.MUST));
        filter.add(new FilterClause(getBoundingFilter(lngField, lng, lngRadius),
                                    BooleanClause.Occur.MUST));

        return filter;
    }

    /**
     * Get a filter restricting results to a upper/lower boundary around a
     * given point.
     *
     * Uses Solr introspection to build the appropriate range query for the
     * given field.
     *
     * @param fieldName name of the field on which to construct the filter.
     * @param center    centerpoint of the bounding range.
     * @param radius    distance between the centerpoint and the upper/lower
     *                  bounds.
     *
     * @return filter restricting results to the given range around the center
     */
    private Filter getBoundingFilter(final String fieldName,
                                     final double center, final double radius) {
        final SchemaField field = req.getSchema().getField(fieldName);
        final Query boundingQuery = field.getType().getRangeQuery(
                this, field, new Double(center - radius).toString(),
                new Double(center + radius).toString(), true, true);
        return new QueryWrapperFilter(boundingQuery);
    }
}
