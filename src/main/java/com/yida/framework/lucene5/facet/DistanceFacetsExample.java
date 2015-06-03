package com.yida.framework.lucene5.facet;

import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.range.DoubleRange;
import org.apache.lucene.facet.range.DoubleRangeFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queries.BooleanFilter;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * 地理距离统计
 * 
 * @author Lanxiaowei
 * 
 */
public class DistanceFacetsExample {
	/** 1千米以内 */
	final DoubleRange ONE_KM = new DoubleRange("< 1 km", 0.0, true, 1.0, false);
	/** 2千米以内 */
	final DoubleRange TWO_KM = new DoubleRange("< 2 km", 0.0, true, 2.0, false);
	/** 5千米以内 */
	final DoubleRange FIVE_KM = new DoubleRange("< 5 km", 0.0, true, 5.0, false);
	/** 10千米以内 */
	final DoubleRange TEN_KM = new DoubleRange("< 10 km", 0.0, true, 10.0,
			false);

	/**索引目录*/
	private final Directory indexDir = new RAMDirectory();
	/**索引搜索器*/
	private IndexSearcher searcher;
	/**Facet配置对象*/
	private final FacetsConfig config = new FacetsConfig();

	/** 原点的纬度 */
	public final static double ORIGIN_LATITUDE = 40.7143528;

	/** 原点的经度 */
	public final static double ORIGIN_LONGITUDE = -74.0059731;

	/**
	 * Radius of the Earth in KM(地球的半径，单位：KM,注意这仅仅是一个近似值)
	 * 
	 * NOTE: this is approximate, because the earth is a bit wider at the
	 * equator than the poles. See http://en.wikipedia.org/wiki/Earth_radius
	 */
	public final static double EARTH_RADIUS_KM = 6371.01;

	public DistanceFacetsExample() {
	}

	/**
	 * 创建测试索引
	 */
	public void index() throws IOException {
		IndexWriter writer = new IndexWriter(indexDir, new IndexWriterConfig(
				new WhitespaceAnalyzer()));

		// 根据经纬度创建索引(注意这里并没有使用FacetField)
		Document doc = new Document();
		doc.add(new DoubleDocValuesField("latitude", 40.759011));
		doc.add(new DoubleDocValuesField("longitude", -73.9844722));
		writer.addDocument(doc);

		doc = new Document();
		doc.add(new DoubleDocValuesField("latitude", 40.718266));
		doc.add(new DoubleDocValuesField("longitude", -74.007819));
		writer.addDocument(doc);

		doc = new Document();
		doc.add(new DoubleDocValuesField("latitude", 40.7051157));
		doc.add(new DoubleDocValuesField("longitude", -74.0088305));
		writer.addDocument(doc);
		
		/*doc.add(new DoubleField("latitude", 40.759011, Field.Store.YES));
	    doc.add(new DoubleField("longitude", -73.9844722, Field.Store.YES));
	    writer.addDocument(doc);
	    
	    doc = new Document();
	    doc.add(new DoubleField("latitude", 40.718266, Field.Store.YES));
	    doc.add(new DoubleField("longitude", -74.007819, Field.Store.YES));
	    writer.addDocument(doc);
	    
	    doc = new Document();
	    doc.add(new DoubleField("latitude", 40.7051157, Field.Store.YES));
	    doc.add(new DoubleField("longitude", -74.0088305, Field.Store.YES));
		writer.addDocument(doc);
		*/

		searcher = new IndexSearcher(DirectoryReader.open(writer, true));
		writer.commit();
		writer.close();
	}

	private ValueSource getDistanceValueSource() {
		Expression distance;
		try {
			//haversin公式是用来计算地球上任意两点之间的距离，具体请Google 【haversin公式】去详细了解
			distance = JavascriptCompiler.compile("haversin(" + ORIGIN_LATITUDE
					+ "," + ORIGIN_LONGITUDE + ",latitude,longitude)");
		} catch (ParseException pe) {
			throw new RuntimeException(pe);
		}
		SimpleBindings bindings = new SimpleBindings();
		//先根据距离远近升序排序，默认是升序，则距离小的排前面即离的近的排前面
		bindings.add(new SortField("latitude", SortField.Type.DOUBLE));
		//再根据经度升序排序
		bindings.add(new SortField("longitude", SortField.Type.DOUBLE));

		return distance.getValueSource(bindings);
	}

	/**
	 * Given a latitude and longitude (in degrees) and the maximum great circle
	 * (surface of the earth) distance, returns a simple Filter bounding box to
	 * "fast match" candidates.
	 */
	public static Filter getBoundingBoxFilter(double originLat,
			double originLng, double maxDistanceKM) {

		// Basic bounding box geo math from
		// http://JanMatuschek.de/LatitudeLongitudeBoundingCoordinates,
		// licensed under creative commons 3.0:
		// http://creativecommons.org/licenses/by/3.0

		// TODO: maybe switch to recursive prefix tree instead
		// (in lucene/spatial)? It should be more efficient
		// since it's a 2D trie...

		// 角度转换为弧度
		double originLatRadians = Math.toRadians(originLat);
		double originLngRadians = Math.toRadians(originLng);

		double angle = maxDistanceKM / EARTH_RADIUS_KM;

		double minLat = originLatRadians - angle;
		double maxLat = originLatRadians + angle;

		double minLng;
		double maxLng;
		if (minLat > Math.toRadians(-90) && maxLat < Math.toRadians(90)) {
			double delta = Math.asin(Math.sin(angle)
					/ Math.cos(originLatRadians));
			minLng = originLngRadians - delta;
			if (minLng < Math.toRadians(-180)) {
				minLng += 2 * Math.PI;
			}
			maxLng = originLngRadians + delta;
			if (maxLng > Math.toRadians(180)) {
				maxLng -= 2 * Math.PI;
			}
		} else {
			// The query includes a pole!
			minLat = Math.max(minLat, Math.toRadians(-90));
			maxLat = Math.min(maxLat, Math.toRadians(90));
			minLng = Math.toRadians(-180);
			maxLng = Math.toRadians(180);
		}

		BooleanFilter f = new BooleanFilter();

		f.add(NumericRangeFilter.newDoubleRange("latitude",
				Math.toDegrees(minLat), Math.toDegrees(maxLat), true, true),
				BooleanClause.Occur.MUST);

		if (minLng > maxLng) {
			// The bounding box crosses the international date line(国际日期变更线)
			BooleanFilter lonF = new BooleanFilter();
			lonF.add(
					NumericRangeFilter.newDoubleRange("longitude",
							Math.toDegrees(minLng), null, true, true),
					BooleanClause.Occur.SHOULD);
			lonF.add(
					NumericRangeFilter.newDoubleRange("longitude", null,
							Math.toDegrees(maxLng), true, true),
					BooleanClause.Occur.SHOULD);
			f.add(lonF, BooleanClause.Occur.MUST);
		} else {
			f.add(NumericRangeFilter.newDoubleRange("longitude",
					Math.toDegrees(minLng), Math.toDegrees(maxLng), true, true),
					BooleanClause.Occur.MUST);
		}

		return f;
	}

	
	public FacetResult search() throws IOException {

		FacetsCollector fc = new FacetsCollector();

		searcher.search(new MatchAllDocsQuery(), fc);
		Facets facets = new DoubleRangeFacetCounts("field",
				getDistanceValueSource(), fc, getBoundingBoxFilter(
						ORIGIN_LATITUDE, ORIGIN_LONGITUDE, 10.0), ONE_KM,
				TWO_KM, FIVE_KM, TEN_KM);

		return facets.getTopChildren(10, "field");
	}

	/**
	 * 使用DrillDownQuery查询
	 * @param range
	 * @return
	 * @throws IOException
	 */
	public TopDocs drillDown(DoubleRange range) throws IOException {
		DrillDownQuery q = new DrillDownQuery(config);
		final ValueSource vs = getDistanceValueSource();
		q.add("field", range.getFilter(
				getBoundingBoxFilter(ORIGIN_LATITUDE, ORIGIN_LONGITUDE,
						range.max), vs));
		DrillSideways ds = new DrillSideways(searcher, config,
				(TaxonomyReader)null) {
			@Override
			protected Facets buildFacetsResult(FacetsCollector drillDowns,
					FacetsCollector[] drillSideways, String[] drillSidewaysDims)
					throws IOException {
				assert drillSideways.length == 1;
				return new DoubleRangeFacetCounts("field", vs,
						drillSideways[0], ONE_KM, TWO_KM, FIVE_KM, TEN_KM);
			}
		};
		return ds.search(q, 10).hits;
	}

	public void close() throws IOException {
		searcher.getIndexReader().close();
		indexDir.close();
	}

	public static void main(String[] args) throws Exception {
		DistanceFacetsExample example = new DistanceFacetsExample();
		example.index();

		System.out.println("Distance facet counting example:");
		System.out.println("-----------------------");
		System.out.println(example.search());

		System.out.println("\n");
		System.out.println("Distance facet drill-down example (field/< 2 km):");
		System.out.println("---------------------------------------------");
		TopDocs hits = example.drillDown(example.TWO_KM);
		System.out.println(hits.totalHits + " totalHits");

		example.close();
	}
}
