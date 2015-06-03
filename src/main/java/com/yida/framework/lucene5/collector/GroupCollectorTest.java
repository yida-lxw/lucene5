package com.yida.framework.lucene5.collector;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
/**
 * 自定义Collector测试
 * @author Lanxiaowei
 *
 */
public class GroupCollectorTest {
	public static void main(String[] args) throws IOException {
		String indexDir = "C:/lucenedir";
		Directory directory = FSDirectory.open(Paths.get(indexDir));
	    IndexReader reader = DirectoryReader.open(directory);
	    IndexSearcher searcher = new IndexSearcher(reader);
	    TermQuery termQuery = new TermQuery(new Term("title", "lucene"));
	    GroupCollector collector = new GroupCollector("title2");
	    searcher.search(termQuery, null, collector);
	    List<ScoreDoc> docs = collector.getScoreDocs();
		for (ScoreDoc scoreDoc : docs) {
			int docID = scoreDoc.doc;
			Document document = searcher.doc(docID);
			String title = document.get("title");
			float score = scoreDoc.score;
			System.out.println(docID + ":" + title + "  " + score);
		}
	    
	    reader.close();
	    directory.close();
	}
}
