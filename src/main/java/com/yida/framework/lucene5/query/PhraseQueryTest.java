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
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public class PhraseQueryTest {
	public static void main(String[] args) throws IOException {
		Directory dir = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwc);

        Document doc = new Document();
        doc.add(new TextField("text", "quick brown fox", Field.Store.YES));
        writer.addDocument(doc);
        
        doc = new Document();
        doc.add(new TextField("text", "jumps over lazy broun dog", Field.Store.YES));
        writer.addDocument(doc);
        
        doc = new Document();
        doc.add(new TextField("text", "jumps over extremely very lazy the broxn dog", Field.Store.YES));
        writer.addDocument(doc);
        
        
        writer.close();

        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        //PhraseQuery必须按它在文档中出现的顺序匹配，slop即为最多需要移动的步数
        String term1 = "dog";
        String term2 = "jumps";
        PhraseQuery phraseQuery = new PhraseQuery();
        phraseQuery.add(new Term("text",term1),0);
        phraseQuery.add(new Term("text",term2),2);
        phraseQuery.setSlop(8);
        
        TopDocs results = searcher.search(phraseQuery, null, 100);
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
