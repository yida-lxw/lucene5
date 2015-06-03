package com.yida.framework.lucene5.spatial;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.distance.DistanceUtils;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Shape;

/**
 * Lucene地理位置查询测试
 * 
 * @author Lanxiaowei
 * 
 */
public class LuceneSpatialTest {
	/** Spatial上下文 */
	private SpatialContext ctx;
	/** 提供索引和查询模型的策略接口 */
	private SpatialStrategy strategy;
	/** 索引目录 */
	private Directory directory;

	/**
	 * Spatial初始化
	 */
	protected void init() {
		// SpatialContext也可以通过SpatialContextFactory工厂类来构建
		this.ctx = SpatialContext.GEO;
		//网格最大11层
		int maxLevels = 11;
		// SpatialPrefixTree也可以通过SpatialPrefixTreeFactory工厂类构建
		SpatialPrefixTree grid = new GeohashPrefixTree(ctx, maxLevels);

		this.strategy = new RecursivePrefixTreeStrategy(grid, "myGeoField");
		// 初始化索引目录
		this.directory = new RAMDirectory();
	}

	private void indexPoints() throws Exception {
		IndexWriterConfig iwConfig = new IndexWriterConfig(new IKAnalyzer());
		IndexWriter indexWriter = new IndexWriter(directory, iwConfig);
		//这里的x,y即经纬度，x为Longitude(经度),y为Latitude(纬度) 
		indexWriter.addDocument(newSampleDocument(2,
				ctx.makePoint(-80.93, 33.77)));
		/** WKT表示法：POINT(Longitude,Latitude)*/
		indexWriter.addDocument(newSampleDocument(4,
				ctx.readShapeFromWkt("POINT(60.9289094 -50.7693246)")));

		indexWriter.addDocument(newSampleDocument(20, ctx.makePoint(0.1, 0.1),
				ctx.makePoint(0, 0)));

		indexWriter.close();
	}

	/**
	 * 创建Document索引对象
	 * 
	 * @param id
	 * @param shapes
	 * @return
	 */
	private Document newSampleDocument(int id, Shape... shapes) {
		Document doc = new Document();
		doc.add(new StoredField("id", id));
		doc.add(new NumericDocValuesField("id", id));
		for (Shape shape : shapes) {
			for (Field f : strategy.createIndexableFields(shape)) {
				doc.add(f);
			}
			Point pt = (Point) shape;
			doc.add(new StoredField(strategy.getFieldName(), pt.getX() + " "
					+ pt.getY()));
		}
		return doc;
	}

	/**
	 * 地理位置搜索
	 * @throws Exception
	 */
	private void search() throws Exception {
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		// 按照id升序排序
		Sort idSort = new Sort(new SortField("id", SortField.Type.INT));
		//搜索方圆200千米范围以内,这里-80.0, 33.0分别是当前位置的经纬度，以当前位置为圆心，200米为半径画圆
		//注意后面的EARTH_MEAN_RADIUS_KM表示200的单位是千米，看到KM了么。
		SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,
				ctx.makeCircle(-80.0, 33.0, DistanceUtils.dist2Degrees(200,
						DistanceUtils.EARTH_MEAN_RADIUS_KM)));
		//根据SpatialArgs参数创建过滤器
		Filter filter = strategy.makeFilter(args);
		//开始搜索
		TopDocs docs = indexSearcher.search(new MatchAllDocsQuery(), filter,
				10, idSort);
		
		Document doc1 = indexSearcher.doc(docs.scoreDocs[0].doc);
		String doc1Str = doc1.getField(strategy.getFieldName()).stringValue();
		int spaceIdx = doc1Str.indexOf(' ');
		double x = Double.parseDouble(doc1Str.substring(0, spaceIdx));
		double y = Double.parseDouble(doc1Str.substring(spaceIdx + 1));
		double doc1DistDEG = ctx
				.calcDistance(args.getShape().getCenter(), x, y);
		System.out.println("(Longitude,latitude):" + "(" + x + "," + y + ")");
		System.out.println("doc1DistDEG:" + doc1DistDEG * DistanceUtils.DEG_TO_KM);
		System.out.println(DistanceUtils.degrees2Dist(doc1DistDEG,DistanceUtils.EARTH_MEAN_RADIUS_KM));
		
		//定义一个坐标点(x,y)即(经度,纬度)即当前用户所在地点
		Point pt = ctx.makePoint(60, -50);
		//计算当前用户所在坐标点与索引坐标点中心之间的距离即当前用户地点与每个待匹配地点之间的距离，DEG_TO_KM表示以KM为单位
		ValueSource valueSource = strategy.makeDistanceValueSource(pt,
				DistanceUtils.DEG_TO_KM);
		//根据命中点与当前位置坐标点的距离远近降序排，距离数字大的排在前面，false表示降序，true表示升序
		Sort distSort = new Sort(valueSource.getSortField(false))
				.rewrite(indexSearcher);
		TopDocs topdocs = indexSearcher.search(new MatchAllDocsQuery(), 10,
				distSort);
		ScoreDoc[] scoreDocs = topdocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			int docId = scoreDoc.doc;
			Document document = indexSearcher.doc(docId);
			int gotid = document.getField("id").numericValue().intValue();
			String geoField = document.getField(strategy.getFieldName()).stringValue();
			int xy = geoField.indexOf(' ');
			double xPoint = Double.parseDouble(geoField.substring(0, xy));
			double yPoint = Double.parseDouble(geoField.substring(xy + 1));
			double distDEG = ctx
					.calcDistance(args.getShape().getCenter(), xPoint, yPoint);
			double juli = DistanceUtils.degrees2Dist(distDEG,DistanceUtils.EARTH_MEAN_RADIUS_KM);
			System.out.println("docId:" + docId + ",id:" + gotid + ",distance:" + juli + "KM");
		}
		/*args = new SpatialArgs(SpatialOperation.Intersects, ctx.makeCircle(
				-80.0, 33.0, 1));
		SpatialArgs args2 = new SpatialArgsParser().parse(
				"Intersects(BUFFER(POINT(-80 33),1))", ctx);
		System.out.println("args2:" + args2.toString());*/
		indexReader.close();
	}
	
	public static void main(String[] args) throws Exception {
		LuceneSpatialTest luceneSpatialTest = new LuceneSpatialTest();
		luceneSpatialTest.init();
		luceneSpatialTest.indexPoints();
		luceneSpatialTest.search();
	}
}
