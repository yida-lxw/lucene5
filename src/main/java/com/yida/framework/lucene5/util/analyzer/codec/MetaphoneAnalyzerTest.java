package com.yida.framework.lucene5.util.analyzer.codec;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;

import com.yida.framework.lucene5.util.AnalyzerUtils;

public class MetaphoneAnalyzerTest extends TestCase {
	
	public void testMetaphoneReplacementAnalyzer() throws IOException {
		String text = "cool cat";
	    Analyzer analyzer = new MetaphoneReplacementAnalyzer();
		AnalyzerUtils.displayTokens(analyzer, text);
	}
	
	public void testKoolKat() throws Exception {
	    RAMDirectory directory = new RAMDirectory();
	    Analyzer analyzer = new MetaphoneReplacementAnalyzer();
	    IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
	    indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
	    IndexWriter writer = new IndexWriter(directory, indexWriterConfig);
	    Document doc = new Document();
	    doc.add(new TextField("contents","cool cat",Field.Store.YES));
	    writer.addDocument(doc);
	    writer.commit();
	    writer.close();

	    IndexReader indexReader = DirectoryReader.open(directory);
	    IndexSearcher searcher = new IndexSearcher(indexReader);

	    Query query = new QueryParser("contents", analyzer).parse("kool kat");

	    TopDocs hits = searcher.search(query, Integer.MAX_VALUE);
	    assertEquals(1, hits.totalHits);
	    int docID = hits.scoreDocs[0].doc;
	    doc = searcher.doc(docID);
	    assertEquals("cool cat", doc.get("contents"));

	    indexReader.close();
	  }

	  /*
	    #A Index document
	    #B Parse query text
	    #C Verify match
	    #D Retrieve original value
	  */

	  public static void main(String[] args) throws IOException {
	    MetaphoneReplacementAnalyzer analyzer =
	                                 new MetaphoneReplacementAnalyzer();
	    AnalyzerUtils.displayTokens(analyzer,
	                   "The quick brown fox jumped over the lazy dog");

	    System.out.println("");
	    AnalyzerUtils.displayTokens(analyzer,
	                   "Tha quik brown phox jumpd ovvar tha lazi dag");
	  }
}
