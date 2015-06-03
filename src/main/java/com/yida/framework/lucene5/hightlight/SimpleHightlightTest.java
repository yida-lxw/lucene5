package com.yida.framework.lucene5.hightlight;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CommonTermsQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.QueryTermScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.join.BitDocIdSetCachingWrapperFilter;
import org.apache.lucene.search.join.BitDocIdSetFilter;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.search.join.ToParentBlockJoinQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

/**
 * 高亮简单测试
 * 
 * @author Lanxiaowei
 * 
 */
public class SimpleHightlightTest {
	final int QUERY = 0;
	final int QUERY_TERM = 1;
	final String FIELD_NAME = "contents";
	private static final String NUMERIC_FIELD_NAME = "nfield";
	private Directory ramDir = new RAMDirectory();
	private Analyzer analyzer = new StandardAnalyzer();
	int numHighlights = 0;
	TopDocs hits;
	int mode = QUERY;
	Fragmenter frag = new SimpleFragmenter(20);

	final FieldType FIELD_TYPE_TV;
	{
		FieldType fieldType = new FieldType(TextField.TYPE_STORED);
		fieldType.setStoreTermVectors(true);
		fieldType.setStoreTermVectorPositions(true);
		fieldType.setStoreTermVectorPayloads(true);
		fieldType.setStoreTermVectorOffsets(true);
		fieldType.freeze();
		FIELD_TYPE_TV = fieldType;
	}

	String[] texts = {
			"Hello this is a piece of text that is very long and contains too much preamble and the meat is really here which says kennedy has been shot",
			"This piece of text refers to Kennedy at the beginning then has a longer piece of text that is very long in the middle and finally ends with another reference to Kennedy",
			"JFK has been shot", "John Kennedy Kennedy has been shot",
			"This text has a typo in referring to Keneddy",
			"wordx wordy wordz wordx wordy wordx worda wordb wordy wordc",
			"y z x y z a b", "lets is a the lets is a the lets is a the lets" };

	/**
	 * 创建测试索引
	 * 
	 * @throws IOException
	 */
	public void createIndex() throws IOException {
		// Analyzer analyzer = new StandardAnalyzer();
		IndexWriter writer = new IndexWriter(ramDir, new IndexWriterConfig(
				analyzer));
		// 添加几个文本域
		for (String text : texts) {
			writer.addDocument(doc(FIELD_NAME, text));
		}

		// 添加几个数字域
		Document doc = new Document();
		doc.add(new IntField(NUMERIC_FIELD_NAME, 1, Field.Store.NO));
		doc.add(new StoredField(NUMERIC_FIELD_NAME, 1));
		writer.addDocument(doc);

		doc = new Document();
		doc.add(new IntField(NUMERIC_FIELD_NAME, 3, Field.Store.NO));
		doc.add(new StoredField(NUMERIC_FIELD_NAME, 3));
		writer.addDocument(doc);

		doc = new Document();
		doc.add(new IntField(NUMERIC_FIELD_NAME, 5, Field.Store.NO));
		doc.add(new StoredField(NUMERIC_FIELD_NAME, 5));
		writer.addDocument(doc);

		doc = new Document();
		doc.add(new IntField(NUMERIC_FIELD_NAME, 7, Field.Store.NO));
		doc.add(new StoredField(NUMERIC_FIELD_NAME, 7));
		writer.addDocument(doc);

		Document childDoc = doc(FIELD_NAME, "child document");
		Document parentDoc = doc(FIELD_NAME, "parent document");
		writer.addDocuments(Arrays.asList(childDoc, parentDoc));

		// 强制合并段文件，限制合并后段文件个数最大数量
		writer.forceMerge(1);
		writer.close();
	}

	/**
	 * 为Document添加域
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	private Document doc(String name, String value) {
		Document d = new Document();
		d.add(new Field(name, value, FIELD_TYPE_TV));
		return d;
	}

	/**
	 * 创建Token对象
	 * 
	 * @param term
	 * @param start
	 * @param offset
	 * @return
	 */
	private static Token createToken(String term, int start, int offset) {
		return new Token(term, start, offset);
	}

	public Highlighter getHighlighter(Query query, String fieldName,
			Formatter formatter) {
		return getHighlighter(query, fieldName, formatter, true);
	}

	/**
	 * 创建高亮器
	 * 
	 * @param query
	 * @param fieldName
	 * @param formatter
	 * @param expanMultiTerm
	 * @return
	 */
	public Highlighter getHighlighter(Query query, String fieldName,
			Formatter formatter, boolean expanMultiTerm) {
		Scorer scorer;
		if (mode == QUERY) {
			scorer = new QueryScorer(query, fieldName);
			// 是否展开多Term查询
			if (!expanMultiTerm) {
				((QueryScorer) scorer).setExpandMultiTermQuery(false);
			}
		} else if (mode == QUERY_TERM) {
			scorer = new QueryTermScorer(query);
		} else {
			throw new RuntimeException("Unknown highlight mode");
		}
		return new Highlighter(formatter, scorer);
	}

	/**
	 * 获取高亮后的文本(如果高亮失败，则返回原样文本)
	 * 
	 * @param query
	 * @param fieldName
	 * @param text
	 * @return
	 * @throws IOException
	 * @throws InvalidTokenOffsetsException
	 */
	private String highlightField(Query query, String fieldName, String text)
			throws IOException, InvalidTokenOffsetsException {
		// 将用户输入的搜索关键字通过分词器转化为TokenStream
		TokenStream tokenStream = analyzer.tokenStream(fieldName, text);
		// SimpleHTMLFormatter默认是使用<B></B>
		SimpleHTMLFormatter formatter = new SimpleHTMLFormatter();
		// 第3个参数表示默认域
		QueryScorer scorer = new QueryScorer(query, fieldName, FIELD_NAME);
		Highlighter highlighter = new Highlighter(formatter, scorer);
		highlighter.setTextFragmenter(new SimpleFragmenter(Integer.MAX_VALUE));

		// maxNumFragments:最大的高亮个数，separator：多个高亮段之间的分隔符，默认是...
		String rv = highlighter.getBestFragments(tokenStream, text, 1, "...");
		return rv.length() == 0 ? text : rv;
	}

	public Query doSearching(Query unReWrittenQuery) throws Exception {
		IndexReader reader = DirectoryReader.open(ramDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		// 对于MultiTermQuery, TermRangeQuery, PrefixQuery，你如果使用QueryTermScorer而非QueryScorer，
		//那么你必须对MultiTermQuery, TermRangeQuery, PrefixQuery进行rewrite
		Query query = unReWrittenQuery.rewrite(reader);
		hits = searcher.search(query, null, 1000);
		return query;
	}

	public void testHighlightingWithDefaultField() throws Exception {

		String s1 = "I call our world world Flatland, not because we call it so";

		PhraseQuery q = new PhraseQuery();
		// 表示两个Term之间最大3个间距
		q.setSlop(3);
		q.add(new Term(FIELD_NAME, "world"));
		q.add(new Term(FIELD_NAME, "flatland"));

		String observed = highlightField(q, FIELD_NAME, s1);
		System.out.println(observed);

		q = new PhraseQuery();
		q.setSlop(3);
		q.add(new Term("text", "world"));
		q.add(new Term("text", "flatland"));
		// 高亮域域查询时Query域不一致，所以无法高亮，这个务必注意
		observed = highlightField(q, FIELD_NAME, s1);
		System.out.println(observed);
	}

	/**
	 * CommonTermsQuery中使用高亮
	 * 
	 * @throws Exception
	 */
	public void testHighlightingCommonTermsQuery() throws Exception {
		createIndex();
		// 第一个参数：频率高的Term必须出现，第二个参数：频率低的Term可有可无，第三个参数表示Term出现的最大频率
		CommonTermsQuery query = new CommonTermsQuery(Occur.MUST, Occur.SHOULD,
				3);
		query.add(new Term(FIELD_NAME, "this"));
		query.add(new Term(FIELD_NAME, "long"));
		query.add(new Term(FIELD_NAME, "very"));
		IndexReader reader = DirectoryReader.open(ramDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		TopDocs hits = searcher.search(query, 10);
		System.out.println("hits.totalHits:" + hits.totalHits);
		QueryScorer scorer = new QueryScorer(query, FIELD_NAME);
		Highlighter highlighter = new Highlighter(scorer);

		Document doc = searcher.doc(hits.scoreDocs[0].doc);
		String storedField = doc.get(FIELD_NAME);

		TokenStream stream = TokenSources.getAnyTokenStream(
				searcher.getIndexReader(), hits.scoreDocs[0].doc, FIELD_NAME,
				doc, analyzer);
		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
		highlighter.setTextFragmenter(fragmenter);
		String fragment = highlighter.getBestFragment(stream, storedField);
		System.out.println("fragment:" + fragment);

		doc = searcher.doc(hits.scoreDocs[1].doc);
		storedField = doc.get(FIELD_NAME);

		stream = TokenSources.getAnyTokenStream(searcher.getIndexReader(),
				hits.scoreDocs[1].doc, FIELD_NAME, doc, analyzer);
		highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));
		fragment = highlighter.getBestFragment(stream, storedField);
		// 打印第二个匹配结果高亮后的结果，默认是加<B></B>
		System.out.println("fragment:" + fragment);
		reader.close();
		ramDir.close();
	}

	/**
	 * 测试下高亮最大显示个数和高亮段显示字符长度控制
	 * 
	 * @throws Exception
	 */
	public void testSimpleTermQueryHighlighter() throws Exception {
		// 创建索引
		createIndex();
		IndexReader reader = DirectoryReader.open(ramDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		Query query = doSearching(new TermQuery(new Term(FIELD_NAME, "kennedy")));
		// 这里不能简单的使用TermQuery,MultiTermQuery，需要query.rewriter下，需要引起你们的注意
		// Query query = new TermQuery(new Term(FIELD_NAME, "kennedy"));

		// 设置最大显示的高亮段个数，即显示<B></B>的个数
		int maxNumFragmentsRequired = 1;

		QueryScorer scorer = new QueryScorer(query, FIELD_NAME);
		Highlighter highlighter = new Highlighter(scorer);

		for (int i = 0; i < hits.totalHits; i++) {
			String text = searcher.doc(hits.scoreDocs[i].doc).get(FIELD_NAME);
			TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, text);
			// SimpleFragmenter构造函数里的这个参数表示显示的高亮段字符的总长度<B></B>标签也是计算在内的
			// 自己调整这个数字，数数显示的高亮段字符的长度去感受下，你就懂了
			highlighter.setTextFragmenter(new SimpleFragmenter(17));

			String result = highlighter.getBestFragments(tokenStream, text,
					maxNumFragmentsRequired, "...");
			System.out.println("\t" + result);
		}
	}

	public void testSimplePhraseQueryHightlighting() throws Exception {
		// 创建索引
		createIndex();
		IndexReader reader = DirectoryReader.open(ramDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		PhraseQuery phraseQuery = new PhraseQuery();
		phraseQuery.add(new Term(FIELD_NAME, "very"));
		phraseQuery.add(new Term(FIELD_NAME, "long"));
		phraseQuery.add(new Term(FIELD_NAME, "contains"), 3);

		// 如果不对Query进行rewrite，你将会得到一个NullPointerException
		Query query = doSearching(phraseQuery);

		// 这两个参数很诡异 
		//当你设置最多显示2个高亮段，但如果SimpleFragmenter构造参数设置的最大段字符长度能够显示超过2个高亮段，则会无视maxNumFragmentsRequired设置
		//相反如果你最大能显示的段字符长度设置的很小不足以显示1个高亮段，而最多能显示的高亮段个数大于1，这是最大能显示的段字符长度设置无效，以最多能显示的高亮段个数为准。
		int maxNumFragmentsRequired = 3;

		QueryScorer scorer = new QueryScorer(query, FIELD_NAME);
		Highlighter highlighter = new Highlighter(scorer);

		for (int i = 0; i < hits.totalHits; i++) {
			final Document doc = searcher.doc(hits.scoreDocs[i].doc);
			String text = doc.get(FIELD_NAME);
			TokenStream tokenStream = TokenSources.getAnyTokenStream(reader,
					hits.scoreDocs[i].doc, FIELD_NAME, doc, analyzer);

			highlighter.setTextFragmenter(new SimpleFragmenter(2));

			String result = highlighter.getBestFragments(tokenStream, text,
					maxNumFragmentsRequired, "...");
			System.out.println("\t" + result);
		}

		// 测试2
		phraseQuery = new PhraseQuery();
		phraseQuery.add(new Term(FIELD_NAME, "piece"), 1);
		phraseQuery.add(new Term(FIELD_NAME, "text"), 3);
		phraseQuery.add(new Term(FIELD_NAME, "refers"), 4);
		phraseQuery.add(new Term(FIELD_NAME, "kennedy"), 6);

		query = doSearching(phraseQuery);
		maxNumFragmentsRequired = 2;

		scorer = new QueryScorer(query, FIELD_NAME);
		highlighter = new Highlighter(scorer);

		for (int i = 0; i < hits.totalHits; i++) {
			final Document doc = searcher.doc(hits.scoreDocs[i].doc);
			String text = doc.get(FIELD_NAME);
			TokenStream tokenStream = TokenSources.getAnyTokenStream(reader,
					hits.scoreDocs[i].doc, FIELD_NAME, doc, analyzer);

			highlighter.setTextFragmenter(new SimpleFragmenter(40));

			String result = highlighter.getBestFragments(tokenStream, text,
					maxNumFragmentsRequired, "...");
			System.out.println("\t" + result);
		}
	}

	/**
	 * 在正则查询中使用高亮器
	 * 
	 * @throws Exception
	 */
	public void testRegexQueryHightlighting() throws Exception {
		// 创建索引
		createIndex();
		IndexReader reader = DirectoryReader.open(ramDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		Query query = new RegexpQuery(new Term(FIELD_NAME, "ken.*"));
		searcher = new IndexSearcher(reader);
		hits = searcher.search(query, 100);
		int maxNumFragmentsRequired = 2;

		QueryScorer scorer = new QueryScorer(query, FIELD_NAME);
		Highlighter highlighter = new Highlighter(scorer);

		for (int i = 0; i < hits.totalHits; i++) {
			final Document doc = searcher.doc(hits.scoreDocs[i].doc);
			String text = doc.get(FIELD_NAME);
			TokenStream tokenStream = TokenSources.getAnyTokenStream(reader,
					hits.scoreDocs[i].doc, FIELD_NAME, doc, analyzer);

			highlighter.setTextFragmenter(new SimpleFragmenter(40));

			String result = highlighter.getBestFragments(tokenStream, text,
					maxNumFragmentsRequired, "...");
			System.out.println("\t" + result);
		}

	}
	
	/**
	 * 在通配符查询中使用高亮器
	 * 
	 * @throws Exception
	 */
	public void testWildcardQueryHightlighting() throws Exception {
		// 创建索引
		createIndex();
		IndexReader reader = DirectoryReader.open(ramDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		Query query = new WildcardQuery(new Term(FIELD_NAME, "k?nnedy"));
		searcher = new IndexSearcher(reader);
		hits = searcher.search(query, 100);
		int maxNumFragmentsRequired = 2;

		QueryScorer scorer = new QueryScorer(query, FIELD_NAME);
		Highlighter highlighter = new Highlighter(scorer);

		for (int i = 0; i < hits.totalHits; i++) {
			final Document doc = searcher.doc(hits.scoreDocs[i].doc);
			String text = doc.get(FIELD_NAME);
			TokenStream tokenStream = TokenSources.getAnyTokenStream(reader,
					hits.scoreDocs[i].doc, FIELD_NAME, doc, analyzer);

			highlighter.setTextFragmenter(new SimpleFragmenter(40));

			String result = highlighter.getBestFragments(tokenStream, text,
					maxNumFragmentsRequired, "...");
			System.out.println("\t" + result);
		}

	}
	
	/**
	 * 在TermRangeQuery中使用高亮器
	 * 
	 * @throws Exception
	 */
	public void testTermRangeQueryHightlighting() throws Exception {
		// 创建索引
		createIndex();
		IndexReader reader = DirectoryReader.open(ramDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		TermRangeQuery rangeQuery = new TermRangeQuery(
	            FIELD_NAME,
	            new BytesRef("kannedy"),
	            new BytesRef("kznnedy"),
	            true, true);
		rangeQuery.setRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
		searcher = new IndexSearcher(reader);
		hits = searcher.search(rangeQuery, 100);
		int maxNumFragmentsRequired = 2;

		QueryScorer scorer = new QueryScorer(rangeQuery, FIELD_NAME);
		Highlighter highlighter = new Highlighter(scorer);

		for (int i = 0; i < hits.totalHits; i++) {
			final Document doc = searcher.doc(hits.scoreDocs[i].doc);
			String text = doc.get(FIELD_NAME);
			TokenStream tokenStream = TokenSources.getAnyTokenStream(reader,
					hits.scoreDocs[i].doc, FIELD_NAME, doc, analyzer);

			highlighter.setTextFragmenter(new SimpleFragmenter(40));

			String result = highlighter.getBestFragments(tokenStream, text,
					maxNumFragmentsRequired, "...");
			System.out.println("\t" + result);
		}

	}
	
	/**
	 * 在SpanNear查询中使用高亮器
	 * 
	 * @throws Exception
	 */
	public void testSpanNearQueryHightlighting() throws Exception {
		// 创建索引
		createIndex();
		IndexReader reader = DirectoryReader.open(ramDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		Query query = new SpanNearQuery(new SpanQuery[] {
		        new SpanTermQuery(new Term(FIELD_NAME, "beginning")),
		        new SpanTermQuery(new Term(FIELD_NAME, "kennedy")) }, 3, false);
		/*Query query = doSearching(new SpanNearQuery(new SpanQuery[] {
		        new SpanTermQuery(new Term(FIELD_NAME, "beginning")),
		        new SpanTermQuery(new Term(FIELD_NAME, "kennedy")) }, 3, false));*/
		searcher = new IndexSearcher(reader);
		hits = searcher.search(query, 100);
		int maxNumFragmentsRequired = 2;

		QueryScorer scorer = new QueryScorer(query, FIELD_NAME);
		Highlighter highlighter = new Highlighter(scorer);

		for (int i = 0; i < hits.totalHits; i++) {
			final Document doc = searcher.doc(hits.scoreDocs[i].doc);
			String text = doc.get(FIELD_NAME);
			TokenStream tokenStream = TokenSources.getAnyTokenStream(reader,
					hits.scoreDocs[i].doc, FIELD_NAME, doc, analyzer);

			highlighter.setTextFragmenter(new SimpleFragmenter(40));

			String result = highlighter.getBestFragments(tokenStream, text,
					maxNumFragmentsRequired, "...");
			System.out.println("\t" + result);
		}

	}
	
	/**
	 * 在FuzzyQuery查询中使用高亮器
	 * 
	 * @throws Exception
	 */
	public void testFuzzyQueryHightlighting() throws Exception {
		// 创建索引
		createIndex();
		IndexReader reader = DirectoryReader.open(ramDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		FuzzyQuery query = new FuzzyQuery(new Term(FIELD_NAME, "kinnedy"), 2);
		searcher = new IndexSearcher(reader);
		hits = searcher.search(query, 100);
		int maxNumFragmentsRequired = 2;

		QueryScorer scorer = new QueryScorer(query, FIELD_NAME);
		Highlighter highlighter = new Highlighter(scorer);

		for (int i = 0; i < hits.totalHits; i++) {
			final Document doc = searcher.doc(hits.scoreDocs[i].doc);
			String text = doc.get(FIELD_NAME);
			TokenStream tokenStream = TokenSources.getAnyTokenStream(reader,
					hits.scoreDocs[i].doc, FIELD_NAME, doc, analyzer);

			highlighter.setTextFragmenter(new SimpleFragmenter(40));

			String result = highlighter.getBestFragments(tokenStream, text,
					maxNumFragmentsRequired, "...");
			System.out.println("\t" + result);
		}
	}

	/**
	 * 在joinQuery中使用高亮器
	 * @throws Exception
	 */
	public void testToParentBlockJoinQuery() throws Exception {
		// 创建索引
		createIndex();
		IndexReader reader = DirectoryReader.open(ramDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		//你过滤出域值包含parent的索引文档作为parent
		BitDocIdSetFilter parentFilter = new BitDocIdSetCachingWrapperFilter(
				new QueryWrapperFilter(new TermQuery(new Term(FIELD_NAME,
						"parent"))));
		//然后通过ToParentBlockJoinQuery在parent中找child索引文档且child索引文档必须符合[域值包含child字符]
		//我们在创建索引时是通过addDocuments添加的parent和child的，即addDocuments，这里接收一个documents数组，
		//父子关系判定规则是，数组中最后一个索引为parent,前面剩下的索引文档都作为parent的child,记住child必须在parent前面
		//这也是addDocuments和addDocument的区别
		Query query = new ToParentBlockJoinQuery(new TermQuery(new Term(
				FIELD_NAME, "child")), parentFilter, ScoreMode.Total);
		hits = searcher.search(query, 100);
		int maxNumFragmentsRequired = 3;

		QueryScorer scorer = new QueryScorer(query, FIELD_NAME);
		Highlighter highlighter = new Highlighter(scorer);

		for (int i = 0; i < hits.totalHits; i++) {
			String text = "child document";
			TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, text);

			highlighter.setTextFragmenter(new SimpleFragmenter(50));
			String fragment = highlighter.getBestFragments(tokenStream, text,
					maxNumFragmentsRequired, "...");
			System.out.println("\t" + fragment);
		}
	}
	
	/**
	 * 测试高亮时对特殊字符进行编码，如< > & "等等
	 * 在构造高亮器时传入SimpleHTMLEncoder即可
	 * 通过SimpleHTMLFormatter可以自定义高亮时的开始和结束标签，如：new SimpleHTMLFormatter("<font color=\"red\">","</font>")
	 * 默认是<B> </B>
	 * @throws Exception
	 */
	public void testEncoding() throws Exception {
	    String rawDocContent = "\"Smith & sons' prices < 3 and >4\" claims article";
	    Query query = new RegexpQuery(new Term(FIELD_NAME,"price.*"));
	    QueryScorer scorer = new QueryScorer(query, FIELD_NAME, FIELD_NAME);
	    Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<font color=\"red\">","</font>"),new SimpleHTMLEncoder(),scorer);
	    highlighter.setTextFragmenter(new SimpleFragmenter(2000));
	    TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, rawDocContent);

	    String encodedSnippet = highlighter.getBestFragments(tokenStream, rawDocContent, 1, "");
	    System.out.println(encodedSnippet);
	}

	public static void main(String[] args) throws Exception {
		SimpleHightlightTest simpleHightlightTest = new SimpleHightlightTest();
		// simpleHightlightTest.testHighlightingCommonTermsQuery();

		// simpleHightlightTest.testHighlightingWithDefaultField();

		// simpleHightlightTest.testSimpleTermQueryHighlighter();

		simpleHightlightTest.testSimplePhraseQueryHightlighting();

		//simpleHightlightTest.testRegexQueryHightlighting();
		
		//simpleHightlightTest.testWildcardQueryHightlighting();
		
		//simpleHightlightTest.testToParentBlockJoinQuery();
		
		//simpleHightlightTest.testSpanNearQueryHightlighting();
		
		//simpleHightlightTest.testFuzzyQueryHightlighting();
		
		//simpleHightlightTest.testTermRangeQueryHightlighting();
		
		//simpleHightlightTest.testEncoding();
	}
}
