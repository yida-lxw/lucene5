package com.yida.framework.lucene5.facet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.FloatAssociationFacetField;
import org.apache.lucene.facet.taxonomy.IntAssociationFacetField;
import org.apache.lucene.facet.taxonomy.TaxonomyFacetSumFloatAssociations;
import org.apache.lucene.facet.taxonomy.TaxonomyFacetSumIntAssociations;
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

public class AssociationsFacetsExample {
	private final Directory indexDir = new RAMDirectory();
	  private final Directory taxoDir = new RAMDirectory();
	  private final FacetsConfig config;

	  public AssociationsFacetsExample()
	  {
	    this.config = new FacetsConfig();
	    this.config.setMultiValued("tags", true);
	    this.config.setIndexFieldName("tags", "$tags");
	    this.config.setMultiValued("genre", true);
	    this.config.setIndexFieldName("genre", "$genre");
	  }

	  private void index() throws IOException
	  {
	    IndexWriterConfig iwc = new IndexWriterConfig(new WhitespaceAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE);
	    IndexWriter indexWriter = new IndexWriter(this.indexDir, iwc);

	    DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(this.taxoDir);

	    Document doc = new Document();

	    //3 --> lucene[不再是统计lucene这个域值的出现总次数，而是统计IntAssociationFacetField的第一个构造参数assoc总和]
	    doc.add(new IntAssociationFacetField(3, "tags", new String[] { "lucene" }));

	    doc.add(new FloatAssociationFacetField(0.87F, "genre", new String[] { "computing" }));
	    indexWriter.addDocument(this.config.build(taxoWriter, doc));

	    doc = new Document();

	    //1 --> lucene
	    doc.add(new IntAssociationFacetField(1, "tags", new String[] { "lucene" }));

	    doc.add(new IntAssociationFacetField(2, "tags", new String[] { "solr" }));

	    doc.add(new FloatAssociationFacetField(0.75F, "genre", new String[] { "computing" }));

	    doc.add(new FloatAssociationFacetField(0.34F, "genre", new String[] { "software" }));
	    indexWriter.addDocument(this.config.build(taxoWriter, doc));

	    indexWriter.close();
	    taxoWriter.close();
	  }

	  private List<FacetResult> sumAssociations() throws IOException
	  {
	    DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
	    IndexSearcher searcher = new IndexSearcher(indexReader);
	    TaxonomyReader taxoReader = new DirectoryTaxonomyReader(this.taxoDir);

	    FacetsCollector fc = new FacetsCollector();

	    FacetsCollector.search(searcher, new MatchAllDocsQuery(), 10, fc);

	    //定义了两个Facet
	    Facets tags = new TaxonomyFacetSumIntAssociations("$tags", taxoReader, this.config, fc);
	    Facets genre = new TaxonomyFacetSumFloatAssociations("$genre", taxoReader, this.config, fc);

	    List<FacetResult> results = new ArrayList<FacetResult>();
	    results.add(tags.getTopChildren(10, "tags", new String[0]));
	    results.add(genre.getTopChildren(10, "genre", new String[0]));

	    indexReader.close();
	    taxoReader.close();

	    return results;
	  }

	  private FacetResult drillDown() throws IOException
	  {
	    DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
	    IndexSearcher searcher = new IndexSearcher(indexReader);
	    TaxonomyReader taxoReader = new DirectoryTaxonomyReader(this.taxoDir);

	    DrillDownQuery q = new DrillDownQuery(this.config);

	    q.add("tags", new String[] { "solr" });
	    FacetsCollector fc = new FacetsCollector();
	    FacetsCollector.search(searcher, q, 10, fc);

	    Facets facets = new TaxonomyFacetSumFloatAssociations("$genre", taxoReader, this.config, fc);
	    FacetResult result = facets.getTopChildren(10, "$genre", new String[0]);

	    indexReader.close();
	    taxoReader.close();

	    return result;
	  }

	  public List<FacetResult> runSumAssociations() throws IOException
	  {
	    index();
	    return sumAssociations();
	  }

	  public FacetResult runDrillDown() throws IOException
	  {
	    index();
	    return drillDown();
	  }

	  public static void main(String[] args) throws Exception
	  {
	    System.out.println("Sum associations example:");
	    System.out.println("-------------------------");
	    List<FacetResult> results = new AssociationsFacetsExample().runSumAssociations();
	    System.out.println("tags: " + results.get(0));
	    System.out.println("genre: " + results.get(1));
	  }
}
