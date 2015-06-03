package com.yida.framework.lucene5.filter;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.TermFilter;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;

import com.yida.framework.lucene5.util.LuceneUtils;

/**
 * CachingWrapperFilter测试[作用就是把其他Filter包装成一个带缓存功能的Filter,
 * 其实就是放入内存中，同一个Filter第二次再执行就会直接从缓存中返回，注意key是reader对象，
 * 所以必须是同一个reader实例才能利用缓存，否则第二次还是会重新执行过滤操作不从缓存中取]
 * @author Lanxiaowei
 *
 */
public class CachingWrapperFilterTest {
	public static void main(String[] args) throws IOException {
		Directory directory = LuceneUtils.openFSDirectory("C:/lucenedir");
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = LuceneUtils.getIndexSearcher(reader);
		//为Filter添加缓存功能，添加缓存其实就是充分利用内存去减轻CPU负荷，提高查询速度
		Query query = new TermQuery(new Term("title","ant"));
		Filter filter = new CachingWrapperFilter(new TermFilter(new Term("subject","junit")));
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
