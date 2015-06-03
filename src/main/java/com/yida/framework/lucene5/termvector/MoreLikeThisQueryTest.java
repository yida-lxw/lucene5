package com.yida.framework.lucene5.termvector;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queries.mlt.MoreLikeThisQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * MoreLikeThisQuery测试
 * @author Lanxiaowei
 *
 */
public class MoreLikeThisQueryTest {
	public static void main(String[] args) throws IOException {
		String indexDir = "C:/lucenedir";
		Directory directory = FSDirectory.open(Paths.get(indexDir));
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		String[] moreLikeFields = new String[] {"title","author"};
		MoreLikeThisQuery query = new MoreLikeThisQuery("lucene in action", 
			moreLikeFields, new StandardAnalyzer(), "author");
		query.setMinDocFreq(1);
		query.setMinTermFrequency(1);
		//System.out.println(query.toString());
		TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		//文档id为1的书
		//System.out.println(reader.document(docNum).get("title") + "-->");
		for (ScoreDoc sdoc : scoreDocs) {
			Document doc = reader.document(sdoc.doc);
			
			//找到与文档id为1的书相似的书
			System.out.println("    more like this:  " + doc.get("title"));
		}
	}
}
