package com.yida.framework.lucene5.score;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;
/**
 * 为域设置权重从而影响索引文档的最终评分[为Document设置权重的API已经被废弃了]
 * @author Lanxiaowei
 *
 */
public class FieldBootTest {
	public static void main(String[] args) throws IOException {
		RAMDirectory directory = new RAMDirectory();
		Analyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(directory, config);
		Document doc1 = new Document();
		Field f1 = new TextField("title", "Java, hello world!",Store.YES);
		doc1.add(f1);
		writer.addDocument(doc1);

		Document doc2 = new Document();
		Field f2 = new TextField("title", "Java ,I like it.",Store.YES);
		//第二个文档的title域权重
		f2.setBoost(100);
		doc2.add(f2);
		writer.addDocument(doc2);
		writer.close();
		
		
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		Query query = new TermQuery(new Term("title","java"));
		TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
		ScoreDoc[] docs = topDocs.scoreDocs;
		if(null == docs || docs.length == 0) {
			System.out.println("No results for this query.");
			return;
		}
		for (ScoreDoc scoreDoc : docs) {
			int docID = scoreDoc.doc;
			float score = scoreDoc.score;
			Document document = searcher.doc(docID);
			String title = document.get("title");
			System.out.println("docId:" + docID);
			System.out.println("title:" + title);
			System.out.println("score:" + score);
			System.out.println("\n");
		}
		reader.close();
		directory.close();
	}
}
