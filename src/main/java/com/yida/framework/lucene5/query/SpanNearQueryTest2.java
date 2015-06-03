package com.yida.framework.lucene5.query;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * SpanNearQuery测试
 * @author Lanxiaowei
 *
 */
public class SpanNearQueryTest2 {
	public static void main(String[] args) throws IOException {
		Directory dir = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwc);

        Document doc = new Document();
        doc.add(new TextField("text", "the quick brown fox jumps over the lazy dog", Field.Store.YES));
        writer.addDocument(doc);
        
        doc = new Document();
        doc.add(new TextField("text", "the quick red fox jumps over the sleepy cat", Field.Store.YES));
        writer.addDocument(doc);
        
        doc = new Document();
        doc.add(new TextField("text", "the quick brown fox jumps over the lazy rest dog", Field.Store.YES));
        writer.addDocument(doc);
        writer.close();

        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        String queryString1 = "quick";
        String queryString2 = "fox";
        String queryString3 = "dog";
        SpanQuery query1 = new SpanTermQuery(new Term("text",queryString1));
        SpanQuery query2 = new SpanTermQuery(new Term("text",queryString2));
        SpanQuery query3 = new SpanTermQuery(new Term("text",queryString3));
        //inOrder为true表示不允许有重叠且必须按文档出现顺序匹配，
        //indexOrder为false表示允许有重叠且不需要按文档出现顺序匹配，AB or BA都可以
        
        //slop即最多要移动几步，注意停用词占的位置也算
        SpanQuery spanNearQuery = new SpanNearQuery(
            new SpanQuery[] {query1,query2,query3}, 5, true, false);
        
        
        
        TopDocs results = searcher.search(spanNearQuery, null, 100);
        ScoreDoc[] scoreDocs = results.scoreDocs;
        
        for (int i = 0; i < scoreDocs.length; ++i) {
            //System.out.println(searcher.explain(query, scoreDocs[i].doc));
        	int docID = scoreDocs[i].doc;
			Document document = searcher.doc(docID);
			String path = document.get("text");
			System.out.println("text:" + path);
        }
	}
}
