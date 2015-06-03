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
import org.apache.lucene.search.AutomatonQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.automaton.Automata;
import org.apache.lucene.util.automaton.Automaton;

/**
 * AutomatonQuery测试
 * @author Lanxiaowei
 *
 */
public class AutomatonQueryTest {
	public static void main(String[] args) throws IOException {
		Directory dir = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwc);

        Document doc = new Document();
        doc.add(new TextField("text", "dog", Field.Store.YES));
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new TextField("text", "dig", Field.Store.YES));
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new TextField("text", "drg", Field.Store.YES));
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new TextField("text", "hive", Field.Store.YES));
        writer.addDocument(doc);
        writer.close();

        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        
        Automaton automaton = Automata.makeAnyString();
        System.out.println(automaton.toDot());
        MultiTermQuery query = new AutomatonQuery(new Term("text"), automaton);
        query.setRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
        

        TopDocs results = searcher.search(query, null, 100);
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
