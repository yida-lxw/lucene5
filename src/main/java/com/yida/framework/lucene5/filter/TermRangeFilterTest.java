package com.yida.framework.lucene5.filter;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.BooleanFilter;
import org.apache.lucene.queries.TermFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.store.Directory;

import com.yida.framework.lucene5.util.LuceneUtils;
/**
 * TermRangeFilter
 * @author Lanxiaowei
 *
 */
public class TermRangeFilterTest {
	public static void main(String[] args) throws IOException {
		Directory directory = LuceneUtils.openFSDirectory("C:/lucenedir");
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = LuceneUtils.getIndexSearcher(reader);

		Query query = new TermQuery(new Term("title","junit"));
		Filter filter1 = new TermFilter(new Term("subject","junit"));
		//title域中a-j范围内的，包含a,j两个边界
		Filter filter2 = TermRangeFilter.newStringRange("title", "a", "j", true, true);
		
		BooleanFilter booleanFilter = new BooleanFilter();
		booleanFilter.add(filter1, Occur.MUST);
		booleanFilter.add(filter2, Occur.MUST);
		List<Document> list = LuceneUtils.query(indexSearcher, query,booleanFilter);
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
