package com.yida.framework.lucene5.query;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.FieldMaskingSpanQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/**
 * FieldMaskingSpanQuery测试
 * @author Lanxiaowei
 *
 */
public class FieldMaskingSpanQueryTest {
	public static void main(String[] args) throws IOException {
		Directory dir = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(dir, iwc);

        Document doc = new Document();

        doc.add(new Field("teacherid", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("studentfirstname", "james", Field.Store.YES, Field.Index.NOT_ANALYZED));
        
        doc.add(new Field("studentsurname", "jones", Field.Store.YES, Field.Index.NOT_ANALYZED));

        writer.addDocument(doc);
        
        
        //teacher2
        doc = new Document();

        doc.add(new Field("teacherid", "2", Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("studentfirstname", "james", Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("studentsurname", "smith", Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("studentfirstname", "sally", Field.Store.YES, Field.Index.NOT_ANALYZED));

        doc.add(new Field("studentsurname", "jones", Field.Store.YES, Field.Index.NOT_ANALYZED));

        writer.addDocument(doc);
        
        writer.close();

        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        SpanQuery q1  = new SpanTermQuery(new Term("studentfirstname", "james"));
        SpanQuery q2  = new SpanTermQuery(new Term("studentsurname", "jones"));
        
        SpanQuery q2m = new FieldMaskingSpanQuery(q2, "studentfirstname");

        Query query = new SpanNearQuery(new SpanQuery[]{q1, q2m}, -1, false);
        TopDocs results = searcher.search(query, null, 100);
        ScoreDoc[] scoreDocs = results.scoreDocs;
        
        for (int i = 0; i < scoreDocs.length; ++i) {
            //System.out.println(searcher.explain(query, scoreDocs[i].doc));
        	int docID = scoreDocs[i].doc;
			Document document = searcher.doc(docID);
			String teacherid = document.get("teacherid");
			System.out.println("teacherid:" + teacherid);
        }
	}
}
