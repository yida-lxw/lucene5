package com.yida.framework.lucene5.sort;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SortingExample {
	private Directory directory;

	public SortingExample(Directory directory) {
		this.directory = directory;
	}
	
	public void displayResults(Query query, Sort sort)
			throws IOException {
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);

		//searcher.setDefaultFieldSortScoring(true, false);
		
		//Lucene5.x把是否评分的两个参数放到方法入参里来进行设置
		//searcher.search(query, filter, n, sort, doDocScores, doMaxScore);
		TopDocs results = searcher.search(query, null, 
				20, sort,true,false); 
		searcher.searchAfter(null, query, null, Integer.MAX_VALUE, sort, true, false);
		System.out.println("\nResults for: " + 
				query.toString() + " sorted by " + sort);

		System.out
				.println(StringUtils.rightPad("Title", 30)
						+ StringUtils.rightPad("pubmonth", 10)
						+ StringUtils.center("id", 4)
						+ StringUtils.center("score", 15));
		PrintStream out = new PrintStream(System.out, true, "UTF-8");

		DecimalFormat scoreFormatter = new DecimalFormat("0.######");
		for (ScoreDoc sd : results.scoreDocs) {
			int docID = sd.doc;
			float score = sd.score;
			Document doc = searcher.doc(docID);
			out.println(StringUtils.rightPad( 
					StringUtils.abbreviate(doc.get("title"), 29), 30) + 
					StringUtils.rightPad(doc.get("pubmonth"), 10) + 
					StringUtils.center("" + docID, 4) + 
					StringUtils.leftPad( 
							scoreFormatter.format(score), 12)); 
			out.println("   " + doc.get("category"));
			// out.println(searcher.explain(query, docID)); 
		}
		System.out.println("\n**************************************\n");
		reader.close();
	}

	public static void main(String[] args) throws Exception {
		String indexdir = "C:/lucenedir";
		Query allBooks = new MatchAllDocsQuery();

		QueryParser parser = new QueryParser("contents",new StandardAnalyzer()); 
		BooleanQuery query = new BooleanQuery(); 
		query.add(allBooks, BooleanClause.Occur.SHOULD); 
		query.add(parser.parse("java OR action"), BooleanClause.Occur.SHOULD); 

		Directory directory = FSDirectory.open(Paths.get(indexdir));
		SortingExample example = new SortingExample(directory); 

		example.displayResults(query, Sort.RELEVANCE);

		example.displayResults(query, Sort.INDEXORDER);

		example.displayResults(query, new Sort(new SortField("category",
				Type.STRING)));

		example.displayResults(query, new Sort(new SortField("pubmonth",
				Type.INT, true)));

		example.displayResults(query, new Sort(new SortField("category",
				Type.STRING), SortField.FIELD_SCORE, new SortField(
				"pubmonth", Type.INT, true)));

		example.displayResults(query, new Sort(new SortField[] {
				SortField.FIELD_SCORE,
				new SortField("category", Type.STRING) }));
		directory.close();
	}
}