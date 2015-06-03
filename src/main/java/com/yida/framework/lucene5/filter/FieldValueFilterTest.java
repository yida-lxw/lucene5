package com.yida.framework.lucene5.filter;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocValuesRangeFilter;
import org.apache.lucene.search.FieldValueFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;

import com.yida.framework.lucene5.util.LuceneUtils;

/**
 * FieldValueFilter测试
 * @author Lanxiaowei
 *
 */
public class FieldValueFilterTest {
	public static void main(String[] args) throws IOException {
		Directory directory = LuceneUtils.openFSDirectory("C:/lucenedir");
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = LuceneUtils.getIndexSearcher(reader);
		//先把查询范围限定在包含category域的索引文档中，再根据title域去查询
		//negate表示是否取反，默认是包含指定域，取反意思就是不包含指定域
		Query query = new TermQuery(new Term("title","lucene"));
		Filter filter = new FieldValueFilter("category", false);
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
