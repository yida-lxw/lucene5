package com.yida.framework.lucene5.analyzer.positiongap;

import java.io.IOException;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 * 位置增量测试
 * @author Lanxiaowei
 *
 */
public class GapTest {
	public static void main(String[] args) throws IOException, ParseException {
		final Directory dir = new RAMDirectory();
        final IndexWriterConfig iwConfig = new IndexWriterConfig(new SimpleAnalyzer());
        final IndexWriter writer = new IndexWriter(dir, iwConfig);

        Document doc = new Document();
        doc.add(new TextField("body", "A B C", Store.YES));
        //放 10个间隙
        doc.add(new TextField("body", new PositionIncrementTokenStream(10)));
        doc.add(new TextField("body", "D E F", Store.YES));

        System.out.println(doc);
        writer.addDocument(doc);
        writer.close();

        final IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher is = new IndexSearcher(reader);

        QueryParser queryParser = new QueryParser("body", new SimpleAnalyzer());

        for (String queryString : new String[] { "\"A B C\"", "\"A B C D\"",
                "\"A B C D\"", "\"A B C D\"~10", "\"A B C D E F\"~10",
                "\"A B C D F E\"~10", "\"A B C D F E\"~11" }) {
            Query query = queryParser.parse(queryString);
            TopDocs docs = is.search(query, 10);
            System.out.println(docs.totalHits + "\t" + queryString);
        }
        reader.close();
	}
}
