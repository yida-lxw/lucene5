package com.yida.framework.lucene5.score.custom;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.yida.framework.lucene5.util.Constans;
/**
 * CustomScoreQuery测试
 * @author Lanxiaowei
 *
 */
public class CustomScoreQueryTest {
	
	public static void main(String[] args) throws IOException, ParseException {
		String indexDir = "C:/lucenedir";
		Directory directory = FSDirectory.open(Paths.get(indexDir));
	    IndexReader reader = DirectoryReader.open(directory);
	    IndexSearcher searcher = new IndexSearcher(reader);
	    
	    int day = (int) (new Date().getTime() / Constans.DAY_MILLIS);
	    QueryParser parser = new QueryParser("contents",new StandardAnalyzer());
	    Query query = parser.parse("java in action");       
	    Query customScoreQuery = new RecencyBoostCustomScoreQuery(query,2.0,day, 6*365,"pubmonthAsDay");
	    Sort sort = new Sort(new SortField[] {SortField.FIELD_SCORE,
	        new SortField("title2", SortField.Type.STRING)});
	    TopDocs hits = searcher.search(customScoreQuery, null, Integer.MAX_VALUE, sort,true,false);

	    for (int i = 0; i < hits.scoreDocs.length; i++) {
	    	//两种方式取Document都行，其实searcher.doc内部本质还是调用reader.document
	      //Document doc = reader.document(hits.scoreDocs[i].doc);
	    	Document doc = searcher.doc(hits.scoreDocs[i].doc);
	      System.out.println((1+i) + ": " +
	                         doc.get("title") +
	                         ": pubmonth=" +
	                         doc.get("pubmonth") +
	                         " score=" + hits.scoreDocs[i].score);
	    }
	    reader.close();
	    directory.close();
	}
}
