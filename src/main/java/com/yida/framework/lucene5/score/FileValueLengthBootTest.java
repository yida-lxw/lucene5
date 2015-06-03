package com.yida.framework.lucene5.score;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
 * 测试域值长度对评分的影响
 * @author Lanxioawei
 *
 */
public class FileValueLengthBootTest {
	public static void main(String[] args) throws IOException {
		RAMDirectory directory = new RAMDirectory();
		Analyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(directory, config);
		Document doc1 = new Document();
		//Field f1 = new Field("title", "Java, hello world!", Field.Store.YES, Field.Index.ANALYZED_NO_NORMS);
		Field f1 = new Field("title", "Java, hello world!", Field.Store.YES, Field.Index.ANALYZED);
		doc1.add(f1);
		writer.addDocument(doc1);

		Document doc2 = new Document();
		//Field.Index.ANALYZED_NO_NORMS表示禁用Norms
		//Field f2 = new Field("title", "Hello hello hello hello hello Java Java.", Field.Store.YES, Field.Index.ANALYZED_NO_NORMS);
		Field f2 = new Field("title", "Hello hello hello hello hello Java Java.", Field.Store.YES, Field.Index.ANALYZED);
		doc2.add(f2);
		writer.addDocument(doc2);
		writer.close();
		
		//因为第二个索引文档的title域值比第一个的Term个数要多，所以第二个索引文档评分比第一个低
		//但如果禁用Norms,不考虑索引域值的长度因素，因为第二个文档匹配到了两个Term,所以评分较高
		
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
