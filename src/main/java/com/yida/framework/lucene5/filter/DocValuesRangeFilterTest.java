package com.yida.framework.lucene5.filter;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocValuesRangeFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;

import com.yida.framework.lucene5.util.LuceneUtils;

/**
 * DocValuesRangeFilter测试
 * @author Lanxiaowei
 *
 */
public class DocValuesRangeFilterTest {
	public static void main(String[] args) throws IOException {
		Directory directory = LuceneUtils.openFSDirectory("C:/lucenedir");
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = LuceneUtils.getIndexSearcher(reader);
		//先把搜索范围限定在 pubmonth in[199901 to 201005]的索引文档
		//再在限定范围内搜索title域包含lucene关键字的索引文档
		Query query = new TermQuery(new Term("title","lucene"));
		Filter filter = DocValuesRangeFilter
			.newIntRange("pubmonth", 199901, 201005, true, true);
		List<Document> list = LuceneUtils.query(indexSearcher, query,filter);
		if(null == list || list.size() <= 0) {
			System.out.println("No results.");
			return;
		}
		for(Document doc : list) {
			String isbn = doc.get("isbn");
			String category = doc.get("category");
			String title = doc.get("title");
			String author = doc.get("author");
			String pubmonth = doc.get("pubmonth");
			System.out.println("isbn:" + isbn);
			System.out.println("category:" + category);
			System.out.println("title:" + title);
			System.out.println("author:" + author);
			System.out.println("pubmonth:" + pubmonth);
			System.out.println("*****************************************************\n\n");
		}
	}
}