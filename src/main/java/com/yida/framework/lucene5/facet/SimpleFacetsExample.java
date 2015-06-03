package com.yida.framework.lucene5.facet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * Facet简单示例
 * 
 * @author Lanxiaowei
 * 
 */
public class SimpleFacetsExample {
	private final Directory indexDir = new RAMDirectory();
	private final Directory taxoDir = new RAMDirectory();
	private final FacetsConfig config = new FacetsConfig();

	public SimpleFacetsExample() {
		this.config.setHierarchical("Author", true);
		this.config.setHierarchical("Publish Date", true);
		this.config.setMultiValued("Author", true);
		this.config.setRequireDimCount("Author", true);
	}

	/**
	 * 创建测试索引
	 * 
	 * @throws IOException
	 */
	private void index() throws IOException {
		IndexWriter indexWriter = new IndexWriter(this.indexDir,
				new IndexWriterConfig(new WhitespaceAnalyzer())
						.setOpenMode(IndexWriterConfig.OpenMode.CREATE));

		DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(
				this.taxoDir);

		Document doc = new Document();
		doc.add(new FacetField("Author", new String[] { "Bob","Jack", "Tom" }));
		doc.add(new FacetField("Publish Date", new String[] { "2010", "10",
				"15" }));
		indexWriter.addDocument(this.config.build(taxoWriter, doc));

		doc = new Document();
		doc.add(new FacetField("Author", new String[] { "Lisa" }));
		doc.add(new FacetField("Publish Date", new String[] { "2010", "10",
				"20" }));
		indexWriter.addDocument(this.config.build(taxoWriter, doc));

		doc = new Document();
		doc.add(new FacetField("Author", new String[] { "Lisa" }));
		doc.add(new FacetField("Publish Date",
				new String[] { "2012", "1", "1" }));
		indexWriter.addDocument(this.config.build(taxoWriter, doc));

		doc = new Document();
		doc.add(new FacetField("Author", new String[] { "Susan" }));
		doc.add(new FacetField("Publish Date",
				new String[] { "2012", "1", "7" }));
		indexWriter.addDocument(this.config.build(taxoWriter, doc));

		doc = new Document();
		doc.add(new FacetField("Author", new String[] { "Frank" }));
		doc.add(new FacetField("Publish Date",
				new String[] { "1999", "5", "5" }));
		indexWriter.addDocument(this.config.build(taxoWriter, doc));

		indexWriter.close();
		taxoWriter.close();
	}

	private List<FacetResult> facetsWithSearch() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(this.taxoDir);

		FacetsCollector fc = new FacetsCollector();

		FacetsCollector.search(searcher, new MatchAllDocsQuery(), 10, fc);

		List<FacetResult> results = new ArrayList<FacetResult>();

		//不指定域名称，则默认会统计所有FacetField域的总数
		Facets facets = new FastTaxonomyFacetCounts(taxoReader, this.config, fc);
		results.add(facets.getTopChildren(10, "Author", new String[0]));
		results.add(facets.getTopChildren(10, "Publish Date", new String[0]));

		indexReader.close();
		taxoReader.close();

		return results;
	}

	private List<FacetResult> facetsOnly() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(this.taxoDir);

		FacetsCollector fc = new FacetsCollector();

		searcher.search(new MatchAllDocsQuery(), null, fc);

		List<FacetResult> results = new ArrayList<FacetResult>();

		Facets facets = new FastTaxonomyFacetCounts(taxoReader, this.config, fc);

		results.add(facets.getTopChildren(10, "Author"));
		results.add(facets.getTopChildren(10, "Publish Date"));

		indexReader.close();
		taxoReader.close();

		return results;
	}

	private FacetResult drillDown() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(this.taxoDir);

		
		DrillDownQuery q = new DrillDownQuery(this.config);
		q.add("Publish Date", new String[] { "2010" });
		
		FacetsCollector fc = new FacetsCollector();
		FacetsCollector.search(searcher, q, 10, fc);

		Facets facets = new FastTaxonomyFacetCounts(taxoReader, this.config, fc);
		FacetResult result = facets.getTopChildren(10, "Author", new String[0]);

		indexReader.close();
		taxoReader.close();

		return result;
	}

	private List<FacetResult> drillSideways() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(this.taxoDir);

		DrillDownQuery q = new DrillDownQuery(this.config);

		q.add("Publish Date", new String[] { "2010" });

		DrillSideways ds = new DrillSideways(searcher, this.config, taxoReader);
		DrillSideways.DrillSidewaysResult result = ds.search(q, 10);

		List<FacetResult> facets = result.facets.getAllDims(10);

		indexReader.close();
		taxoReader.close();

		return facets;
	}

	public List<FacetResult> runFacetOnly() throws IOException {
		index();
		return facetsOnly();
	}

	public List<FacetResult> runSearch() throws IOException {
		index();
		return facetsWithSearch();
	}

	public FacetResult runDrillDown() throws IOException {
		index();
		return drillDown();
	}

	public List<FacetResult> runDrillSideways() throws IOException {
		index();
		return drillSideways();
	}

	public static void main(String[] args) throws Exception {
		// one
		System.out.println("Facet counting example:");
		System.out.println("-----------------------");
		SimpleFacetsExample example = new SimpleFacetsExample();
		List<FacetResult> results1 = example.runFacetOnly();
		System.out.println("Author: " + results1.get(0));
		System.out.println("Publish Date: " + results1.get(1));
		
		
		// two
		System.out.println("Facet counting example (combined facets and search):");
		System.out.println("-----------------------");
		List<FacetResult> results = example.runSearch();
		System.out.println("Author: " + results.get(0));
		System.out.println("Publish Date: " + results.get(1));
		
		
		// three
		System.out.println("Facet drill-down example (Publish Date/2010):");
		System.out.println("---------------------------------------------");
		System.out.println("Author: " + example.runDrillDown());

		// four
		System.out.println("Facet drill-sideways example (Publish Date/2010):");
		System.out.println("---------------------------------------------");
		for (FacetResult result : example.runDrillSideways()) {
			System.out.println(result);
		}
	}
}
