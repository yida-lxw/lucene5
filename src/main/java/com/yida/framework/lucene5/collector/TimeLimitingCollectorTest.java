package com.yida.framework.lucene5.collector;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TimeLimitingCollector;
import org.apache.lucene.search.TimeLimitingCollector.TimeExceededException;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Counter;
/**
 * TimeLimitingCollector测试[超过限定时间，查询过程自动中断]
 * @author Lanxiaowei
 *
 */
public class TimeLimitingCollectorTest {
	public static void main(String[] args) throws Exception {
		String indexDir = "C:/lucenedir";
		Directory directory = FSDirectory.open(Paths.get(indexDir));
	    IndexReader reader = DirectoryReader.open(directory);
	    IndexSearcher searcher = new IndexSearcher(reader);
	    TermQuery termQuery = new TermQuery(new Term("title", "lucene"));
	    TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(100);
	    //必须在300毫秒内完成搜索
	    TimeLimitingCollector timeLimitingCollector = new TimeLimitingCollector(
	    	topScoreDocCollector, Counter.newCounter(true), 300);
	    
	    try {
	    	searcher.search(termQuery, null, timeLimitingCollector);
		} catch (TimeExceededException  e) {
			System.out.println("IndexSeacher.search is out of the limited time.");
			return;
		}
	    
	    TopDocs topDocs = topScoreDocCollector.topDocs();
	    ScoreDoc[] scoreDocs = topDocs.scoreDocs;
	    if(scoreDocs == null || scoreDocs.length == 0) {
	    	System.out.println("No results.");
	    	return;
	    }
		for (ScoreDoc scoreDoc : scoreDocs) {
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
