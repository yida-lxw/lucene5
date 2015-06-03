package com.yida.framework.lucene5.query;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

/**
 * FuzzyQuery测试
 * @author Lanxiaowei
 *
 */
public class FuzzyQueryTest {
	public static void main(String[] args) throws ParseException, IOException {
		//参数定义
		String directoryPath = "D:/lucenedir";
		String fieldName = "contents";
		String queryString = "xiapngguo"; 
		
		Query query = new FuzzyQuery(new Term(fieldName,queryString),2,3,50,false);
		List<Document> list = query(directoryPath,query);
		if(list == null || list.size() == 0) {
			System.out.println("No results found.");
			return;
		}
		for(Document doc : list) {
			String path = doc.get("path");
			String content = doc.get("contents");
			System.out.println("path:" + path);
			//System.out.println("contents:" + content);
		}
	}
	/**
	 * 创建索引阅读器
	 * @param directoryPath  索引目录
	 * @return
	 * @throws IOException   可能会抛出IO异常
	 */
	public static IndexReader createIndexReader(String directoryPath) throws IOException {
		return DirectoryReader.open(FSDirectory.open(Paths.get(directoryPath, new String[0])));
	}
	
	/**
	 * 创建索引查询器
	 * @param directoryPath   索引目录
	 * @return
	 * @throws IOException
	 */
	public static IndexSearcher createIndexSearcher(String directoryPath) throws IOException {
		return new IndexSearcher(createIndexReader(directoryPath));
	}
	
	/**
	 * 创建索引查询器
	 * @param reader
	 * @return
	 */
	public static IndexSearcher createIndexSearcher(IndexReader reader) {
		return new IndexSearcher(reader);
	}
	
	public static List<Document> query(String directoryPath,Query query) throws IOException {
		IndexSearcher searcher = createIndexSearcher(directoryPath);
		TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
		List<Document> docList = new ArrayList<Document>();
		ScoreDoc[] docs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : docs) {
			int docID = scoreDoc.doc;
			Document document = searcher.doc(docID);
			docList.add(document);
		}
		searcher.getIndexReader().close();
		return docList;
	}
}
