package com.yida.framework.lucene5.function;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
/**
 * FunctionQuery测试
 * @author Lanxiaowei
 *
 */
public class FunctionQueryTest {
	private static final DateFormat formate = new SimpleDateFormat("yyyy-MM-dd");
	public static void main(String[] args) throws Exception {
		String indexDir = "C:/lucenedir-functionquery";
		Directory directory = FSDirectory.open(Paths.get(indexDir));
	    
	    //System.out.println(0.001953125f * 100000000 * 0.001953125f / 100000000);
	    //创建测试索引[注意：只用创建一次，第二次运行前请注释掉这行代码]
	    //createIndex(directory);
		
		
	    IndexReader reader = DirectoryReader.open(directory);
	    IndexSearcher searcher = new IndexSearcher(reader);
	    //创建一个普通的TermQuery
	    TermQuery termQuery = new TermQuery(new Term("title", "solr"));
	    //根据可以计算日期衰减因子的自定义ValueSource来创建FunctionQuery
	    FunctionQuery functionQuery = new FunctionQuery(new DateDampingValueSouce("publishDate")); 
	    //自定义评分查询[CustomScoreQuery将普通Query和FunctionQuery组合在一起，至于两者的Query评分按什么算法计算得到最后得分，由用户自己去重写来干预评分]
	    //默认实现是把普通查询评分和FunctionQuery高级查询评分相乘求积得到最终得分，你可以自己重写默认的实现
	    CustomScoreQuery customScoreQuery = new CustomScoreQuery(termQuery, functionQuery);
	    //创建排序器[按评分降序排序]
	    Sort sort = new Sort(new SortField[] {SortField.FIELD_SCORE});
	    TopDocs topDocs = searcher.search(customScoreQuery, null, Integer.MAX_VALUE, sort,true,false);
	    ScoreDoc[] docs = topDocs.scoreDocs;
	    
		for (ScoreDoc scoreDoc : docs) {
			int docID = scoreDoc.doc;
			Document document = searcher.doc(docID);
			String title = document.get("title");
			String publishDateString = document.get("publishDate");
			System.out.println(publishDateString);
			long publishMills = Long.valueOf(publishDateString);
			Date date = new Date(publishMills);
			publishDateString = formate.format(date);
			float score = scoreDoc.score;
			System.out.println(docID + "  " + title + "                    " + 
			    publishDateString + "            " + score);
		}
	    
	    reader.close();
	    directory.close();
	}
	
	/**
	 * 创建Document对象
	 * @param title              书名
	 * @param publishDateString  书籍出版日期
	 * @return
	 * @throws ParseException
	 */
	public static Document createDocument(String title,String publishDateString) throws ParseException {
		Date publishDate = formate.parse(publishDateString);
		Document doc = new Document();
		doc.add(new TextField("title",title,Field.Store.YES));
		doc.add(new LongField("publishDate", publishDate.getTime(),Store.YES));
		doc.add(new NumericDocValuesField("publishDate", publishDate.getTime()));
		return doc;
	}
	
	//创建测试索引
	public static void createIndex(Directory directory) throws ParseException, IOException {
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(directory, indexWriterConfig);
	    
		//创建测试索引
		Document doc1 = createDocument("Lucene in action 2th edition", "2010-05-05");
		Document doc2 = createDocument("Lucene Progamming", "2008-07-11");
		Document doc3 = createDocument("Lucene User Guide", "2014-11-24");
		Document doc4 = createDocument("Lucene5 Cookbook", "2015-01-09");
		Document doc5 = createDocument("Apache Lucene API 5.0.0", "2015-02-25");
		Document doc6 = createDocument("Apache Solr 4 Cookbook", "2013-10-22");
		Document doc7 = createDocument("Administrating Solr", "2015-01-20");
		Document doc8 = createDocument("Apache Solr Essentials", "2013-08-16");
		Document doc9 = createDocument("Apache Solr High Performance", "2014-06-28");
		Document doc10 = createDocument("Apache Solr API 5.0.0", "2015-03-02");
		
		writer.addDocument(doc1);
		writer.addDocument(doc2);
		writer.addDocument(doc3);
		writer.addDocument(doc4);
		writer.addDocument(doc5);
		writer.addDocument(doc6);
		writer.addDocument(doc7);
		writer.addDocument(doc8);
		writer.addDocument(doc9);
		writer.addDocument(doc10);
		writer.close();
	}
}
