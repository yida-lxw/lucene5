package com.yida.framework.lucene5.sort.custom;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.BinaryDocValuesField;
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
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

/**
 * 自定义排序测试
 * @author Lanxiaowei
 *
 */
public class CustomSortTest {
	public static void main(String[] args) throws Exception {
		RAMDirectory directory = new RAMDirectory();  
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        addPoint(indexWriter, "El charro", "restaurant", 1, 2);  
        addPoint(indexWriter, "Cafe Poca Cosa", "restaurant", 5, 9);  
        addPoint(indexWriter, "Los Betos", "restaurant", 9, 6);  
        addPoint(indexWriter, "Nico's Toco Shop", "restaurant", 3, 8);  
        indexWriter.close();  
          
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);  
        Query query = new TermQuery(new Term("type","restaurant"));  
        Sort sort = new Sort(new SortField("location",new DistanceComparatorSource(10, 10)));  
        TopFieldDocs topDocs = searcher.search(query, null, Integer.MAX_VALUE,sort,true,false);  
        ScoreDoc[] docs = topDocs.scoreDocs;
        for(ScoreDoc doc : docs){
            Document document = searcher.doc(doc.doc);  
            System.out.println(document.get("name") + ":" + doc.score);
        }
	}
	
	private static void addPoint(IndexWriter writer,String name,String type,int x,int y) throws Exception{  
        Document document = new Document();  
        String xy = x + "," + y;
        document.add(new Field("name",name,Field.Store.YES,Field.Index.NOT_ANALYZED));  
        document.add(new Field("type",type,Field.Store.YES,Field.Index.NOT_ANALYZED));  
        document.add(new Field("location",xy,Field.Store.YES,Field.Index.NOT_ANALYZED));  
        document.add(new BinaryDocValuesField("location", new BytesRef(xy.getBytes())));  
        writer.addDocument(document);  
    }  
}
