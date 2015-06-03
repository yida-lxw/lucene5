package com.yida.framework.lucene5.core;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.ansj.lucene5.AnsjAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.yida.framework.lucene5.util.Page;

/**
 * Lucene搜索第一个示例
 * @author Lanxiaowei
 *
 */
public class SearchFirstTest {
	public static void main(String[] args) throws ParseException, IOException {
		//参数定义
		String directoryPath = "D:/lucenedir";
		String fieldName = "contents";
		String queryString = "么么哒";
		int currentPage = 1;
		int pageSize = 10;
		
		Page<Document> page = pageQuery(fieldName, queryString, directoryPath, currentPage, pageSize);
		if(page == null || page.getItems() == null || page.getItems().size() == 0) {
			System.out.println("No results found.");
			return;
		}
		for(Document doc : page.getItems()) {
			String path = doc.get("path");
			String content = doc.get("contents");
			System.out.println("path:" + path);
			System.out.println("contents:" + content);
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
	
	/**
	 * Lucene分页查询
	 * @param directoryPath
	 * @param query
	 * @param page
	 * @throws IOException
	 */
	public static void pageQuery(String directoryPath,Query query,Page<Document> page) throws IOException {
		IndexSearcher searcher = createIndexSearcher(directoryPath);
		int totalRecord = searchTotalRecord(searcher,query);
		//设置总记录数
		page.setTotalRecord(totalRecord);
		TopDocs topDocs = searcher.searchAfter(page.getAfterDoc(),query, page.getPageSize());
		List<Document> docList = new ArrayList<Document>();
		ScoreDoc[] docs = topDocs.scoreDocs;
		int index = 0;
		for (ScoreDoc scoreDoc : docs) {
			int docID = scoreDoc.doc;
			Document document = searcher.doc(docID);
			if(index == docs.length - 1) {
				page.setAfterDoc(scoreDoc);
				page.setAfterDocId(docID);
			}
			docList.add(document);
			index++;
		}
		page.setItems(docList);
		searcher.getIndexReader().close();
	}
	
	/**
	 * 索引分页查询
	 * @param fieldName
	 * @param queryString
	 * @param currentPage
	 * @param pageSize
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static Page<Document> pageQuery(String fieldName,String queryString,String directoryPath,int currentPage,int pageSize) throws ParseException, IOException {
		//QueryParser parser = new QueryParser(fieldName, new StandardAnalyzer());
		//QueryParser parser = new QueryParser(fieldName, new IKAnalyzer());
		QueryParser parser = new QueryParser(fieldName, new AnsjAnalyzer());
		Query query = parser.parse(queryString);
		Page<Document> page = new Page<Document>(currentPage,pageSize);
		pageQuery(directoryPath, query, page);
		return page;
	}
	
	/**
	 * @Title: searchTotalRecord
	 * @Description: 获取符合条件的总记录数
	 * @param query
	 * @return
	 * @throws IOException
	 */
	public static int searchTotalRecord(IndexSearcher searcher,Query query) throws IOException {
		TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
		if(topDocs == null || topDocs.scoreDocs == null || topDocs.scoreDocs.length == 0) {
			return 0;
		}
		ScoreDoc[] docs = topDocs.scoreDocs;
		return docs.length;
	}
}
