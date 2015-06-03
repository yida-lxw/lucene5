package com.yida.framework.lucene5.filter;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.TermFilter;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;

import com.yida.framework.lucene5.util.LuceneUtils;

/**
 * TermsFilter测试
 * @author Lanxiaowei
 *
 */
public class TermsFilterTest {
	public static void main(String[] args) throws IOException {
		Directory directory = LuceneUtils.openFSDirectory("C:/lucenedir");
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = LuceneUtils.getIndexSearcher(reader);
		//先把搜索范围限制在subject域中包含lucene关键字或pubmonth域中包含201005的索引文档，
		//然后再在剩下的索引文档中查询title域中包含lucene关键字的索引文档
		Query query = new TermQuery(new Term("title","lucene"));
		Filter filter = new TermsFilter(new Term[] {
			new Term("subject","lucene"),
			new Term("pubmonth","201005")
		});
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
			System.out.println("isbn:" + isbn);
			String pubmonth = doc.get("pubmonth");
			System.out.println("category:" + category);
			System.out.println("title:" + title);
			System.out.println("author:" + author);
			System.out.println("pubmonth:" + pubmonth);
			System.out.println("*****************************************************\n\n");
		}
		
	}
}
