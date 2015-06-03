package com.yida.framework.lucene5.facet;

import java.io.IOException;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.range.LongRange;
import org.apache.lucene.facet.range.LongRangeFacetCounts;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class RangeFacetsExample {
	private final Directory indexDir = new RAMDirectory();
	private IndexSearcher searcher;
	/**当前时间的毫秒数*/
	private final long nowSec = System.currentTimeMillis();

	/**1小时之前的毫秒数*/
	final LongRange PAST_HOUR = new LongRange("Past hour", this.nowSec - 3600L,
			true, this.nowSec, true);
	/**6小时之前的毫秒数*/
	final LongRange PAST_SIX_HOURS = new LongRange("Past six hours",
			this.nowSec - 21600L, true, this.nowSec, true);
	/**24小时之前的毫秒数*/
	final LongRange PAST_DAY = new LongRange("Past day", this.nowSec - 86400L,
			true, this.nowSec, true);

	/**
	 * 创建测试索引
	 * @throws IOException
	 */
	public void index() throws IOException {
		IndexWriter indexWriter = new IndexWriter(this.indexDir,
				new IndexWriterConfig(new WhitespaceAnalyzer())
						.setOpenMode(IndexWriterConfig.OpenMode.CREATE));

		/**
		 * 每次按[1000*i]这个斜率递减创建一个索引
		 */
		for (int i = 0; i < 100; i++) {
			Document doc = new Document();
			long then = this.nowSec - i * 1000;

			doc.add(new NumericDocValuesField("timestamp", then));

			doc.add(new LongField("timestamp", then, Field.Store.YES));
			indexWriter.addDocument(doc);
		}
		
		this.searcher = new IndexSearcher(DirectoryReader.open(indexWriter,
				true));
		indexWriter.close();
	}

	/**
	 * 获取FacetConfig配置对象
	 * @return
	 */
	private FacetsConfig getConfig() {
		return new FacetsConfig();
	}

	public FacetResult search() throws IOException {
		/**创建Facet结果收集器*/
		FacetsCollector fc = new FacetsCollector();
		TopDocs topDocs = FacetsCollector.search(this.searcher, new MatchAllDocsQuery(), 20, fc);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for(ScoreDoc scoreDoc : scoreDocs) {
			int docId = scoreDoc.doc;
			Document doc = searcher.doc(docId);
			System.out.println(scoreDoc.doc + "\t" + doc.get("timestamp"));
		}
		//定义3个Facet: 统计 
		//[过去一小时之前-->当前时间]  [过去6小时之前-->当前时间]  [过去24小时之前-->当前时间]
		Facets facets = new LongRangeFacetCounts("timestamp", fc,
				new LongRange[] { this.PAST_HOUR, this.PAST_SIX_HOURS,
						this.PAST_DAY });
		
		return facets.getTopChildren(10, "timestamp", new String[0]);
	}

	/**
	 * 使用DrillDownQuery进行Facet统计
	 * @param range
	 * @return
	 * @throws IOException
	 */
	public TopDocs drillDown(LongRange range) throws IOException {
		DrillDownQuery q = new DrillDownQuery(getConfig());

		q.add("timestamp", NumericRangeQuery.newLongRange("timestamp",
				Long.valueOf(range.min), Long.valueOf(range.max),
				range.minInclusive, range.maxInclusive));

		return this.searcher.search(q, 10);
	}

	public void close() throws IOException {
		this.searcher.getIndexReader().close();
		this.indexDir.close();
	}

	public static void main(String[] args) throws Exception {
		RangeFacetsExample example = new RangeFacetsExample();
		example.index();

		System.out.println("Facet counting example:");
		System.out.println("-----------------------");
		System.out.println(example.search());

		System.out.println("\n");
		
		//只统计6个小时之前的Facet
		System.out
				.println("Facet drill-down example (timestamp/Past six hours):");
		System.out.println("---------------------------------------------");
		TopDocs hits = example.drillDown(example.PAST_SIX_HOURS);
		System.out.println(hits.totalHits + " totalHits");

		example.close();
	}
}
