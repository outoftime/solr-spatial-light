package me.outofti.solrspatial;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilterClause;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.spatial.tier.LatLongDistanceFilter;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.util.NumberUtils;

public class SpatialQParser extends QParser {
	private static final Pattern PATTERN = Pattern.compile("^(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)$");
	private static final double DEGREES_TO_MILES = 69.047;

	public SpatialQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
		super(qstr, localParams, params, req);
	}

	public Query parse() throws ParseException {
		final Matcher matcher = PATTERN.matcher(qstr);
		if(!matcher.matches()) {
			throw new ParseException("Spatial queries should be of the format LAT,LNG");
		}

		final double lat = Double.parseDouble(matcher.group(1));
		final double lng = Double.parseDouble(matcher.group(3));

		final double miles = (double)localParams.getFloat("radius", 1.0F);
		final String latField = localParams.get("latField", "lat");
		final String lngField = localParams.get("lngField", "lng");

		final Filter startingFilter = getBoundingBoxFilter(lat, lng, miles, latField, lngField);
		return new ConstantScoreQuery(new LatLongDistanceFilter(startingFilter, lat, lng, miles, latField, lngField));
	}

	private Filter getBoundingBoxFilter(final double lat, final double lng, final double miles, final String latField, final String lngField) {
		final double latRadius = Math.abs(miles / 2.0 / DEGREES_TO_MILES);
		final double lngRadius = Math.abs(miles / 2.0 / DEGREES_TO_MILES * Math.cos(lat));

		final BooleanFilter filter = new BooleanFilter();
		
		filter.add(new FilterClause(getBoundingFilter(latField, lat, latRadius), BooleanClause.Occur.MUST));
		filter.add(new FilterClause(getBoundingFilter(lngField, lng, lngRadius), BooleanClause.Occur.MUST));

		return filter;
	}

	private Filter getBoundingFilter(final String fieldName, final double center, final double radius) {
		// return new TermRangeFilter(fieldName, new Double(center - radius).toString(), new Double(center + radius).toString(), true, true);
		return NumericRangeFilter.newDoubleRange(fieldName, 8, new Double(center - radius), new Double(center + radius), true, true);
	}
}
