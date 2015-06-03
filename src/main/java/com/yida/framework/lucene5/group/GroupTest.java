package com.yida.framework.lucene5.group;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.valuesource.BytesRefFieldSource;
import org.apache.lucene.search.CachingCollector;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.grouping.AbstractAllGroupsCollector;
import org.apache.lucene.search.grouping.AbstractFirstPassGroupingCollector;
import org.apache.lucene.search.grouping.AbstractSecondPassGroupingCollector;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.SearchGroup;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.grouping.function.FunctionAllGroupsCollector;
import org.apache.lucene.search.grouping.function.FunctionFirstPassGroupingCollector;
import org.apache.lucene.search.grouping.function.FunctionSecondPassGroupingCollector;
import org.apache.lucene.search.grouping.term.TermAllGroupsCollector;
import org.apache.lucene.search.grouping.term.TermFirstPassGroupingCollector;
import org.apache.lucene.search.grouping.term.TermSecondPassGroupingCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.mutable.MutableValue;
import org.apache.lucene.util.mutable.MutableValueStr;

import com.yida.framework.lucene5.util.Tools;
/**
 * Lucene分组测试
 * @author Lanxiaowei
 *
 */
public class GroupTest {
	/** 索引目录 */
	private static final String indexDir = "C:/group-index";
	/** 分词器 */
	private static Analyzer analyzer = new StandardAnalyzer();
	/** 分组域 */
	private static String groupField = "author";

	public static void main(String[] args) throws Exception {
		// 创建测试索引
		// createIndex();
		Directory directory = FSDirectory.open(Paths.get(indexDir));
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		Query query = new TermQuery(new Term("content", "random"));
		/**每个分组内部的排序规则*/
		Sort groupSort = Sort.RELEVANCE;
		groupBy(searcher, query, groupSort);
		//groupSearch(searcher);
	}

	public static void groupBy(IndexSearcher searcher, Query query, Sort groupSort)
			throws IOException {
		/** 前N条中分组 */
		int topNGroups = 10;
		/** 分组起始偏移量 */
		int groupOffset = 0;
		/** 是否填充SearchGroup的sortValues */
		boolean fillFields = true;
		/** groupSort用于对组进行排序，docSort用于对组内记录进行排序，多数情况下两者是相同的，但也可不同 */
		Sort docSort = groupSort;
		/** 用于组内分页，起始偏移量 */
		int docOffset = 0;
		/** 每组返回多少条结果 */
		int docsPerGroup = 2;
		/** 是否需要计算总的分组数量 */
		boolean requiredTotalGroupCount = true;
		/** 是否需要缓存评分 */
		boolean cacheScores = true;

		TermFirstPassGroupingCollector c1 = new TermFirstPassGroupingCollector(
				"author", groupSort, groupOffset + topNGroups);
		//第一次查询缓存容量的大小：设置为16M
		double maxCacheRAMMB = 16.0;
		/** 将TermFirstPassGroupingCollector包装成CachingCollector，为第一次查询加缓存，避免重复评分 
		 *  CachingCollector就是用来为结果收集器添加缓存功能的
		 */
		CachingCollector cachedCollector = CachingCollector.create(c1,
				cacheScores, maxCacheRAMMB);
		// 开始第一次分组统计
		searcher.search(query, cachedCollector);

		/**第一次查询返回的结果集TopGroups中只有分组域值以及每组总的评分，至于每个分组里有几条，分别哪些索引文档，则需要进行第二次查询获取*/
		Collection<SearchGroup<BytesRef>> topGroups = c1.getTopGroups(
				groupOffset, fillFields);

		if (topGroups == null) {
			System.out.println("No groups matched ");
			return;
		}
		
		Collector secondPassCollector = null;
		
		// 是否获取每个分组内部每个索引的评分
		boolean getScores = true;
		// 是否计算最大评分
		boolean getMaxScores = true;
		// 如果需要对Lucene的score进行修正，则需要重载TermSecondPassGroupingCollector
		TermSecondPassGroupingCollector c2 = new TermSecondPassGroupingCollector(
				"author", topGroups, groupSort, docSort, docOffset
						+ docsPerGroup, getScores, getMaxScores, fillFields);

		// 如果需要计算总的分组数量，则需要把TermSecondPassGroupingCollector包装成TermAllGroupsCollector
		// TermAllGroupsCollector就是用来收集总分组数量的
		TermAllGroupsCollector allGroupsCollector = null;
		//若需要统计总的分组数量
		if (requiredTotalGroupCount) {
			allGroupsCollector = new TermAllGroupsCollector("author");
			secondPassCollector = MultiCollector.wrap(c2, allGroupsCollector);
		} else {
			secondPassCollector = c2;
		}

		/**如果第一次查询已经加了缓存，则直接从缓存中取*/
		if (cachedCollector.isCached()) {
			// 第二次查询直接从缓存中取
			cachedCollector.replay(secondPassCollector);
		} else {
			// 开始第二次分组查询
			searcher.search(query, secondPassCollector);
		}

		/** 所有组的数量 */
		int totalGroupCount = 0;
		/** 所有满足条件的记录数 */
		int totalHitCount = 0;
		/** 所有组内的满足条件的记录数(通常该值与totalHitCount是一致的) */
		int totalGroupedHitCount = -1;
		if (requiredTotalGroupCount) {
			totalGroupCount = allGroupsCollector.getGroupCount();
		}
		//打印总的分组数量
		System.out.println("groupCount: " + totalGroupCount);

		TopGroups<BytesRef> groupsResult = c2.getTopGroups(docOffset);
		//这里打印的3项信息就是第一次查询的统计结果
		totalHitCount = groupsResult.totalHitCount;
		totalGroupedHitCount = groupsResult.totalGroupedHitCount;
		System.out.println("groupsResult.totalHitCount:" + totalHitCount);
		System.out.println("groupsResult.totalGroupedHitCount:"
				+ totalGroupedHitCount);
		System.out.println("///////////////////////////////////////////////");
		int groupIdx = 0;
		
		//下面打印的是第二次查询的统计结果，如果你仅仅值需要第一次查询的统计结果信息，不需要每个分组内部的详细信息，则不需要进行第二次查询，请知晓
		// 迭代组
		for (GroupDocs<BytesRef> groupDocs : groupsResult.groups) {
			groupIdx++;
			String groupVL = groupDocs.groupValue == null ? "分组域的域值为空" : new String(groupDocs.groupValue.bytes);
			// 分组域的域值，groupIdx表示组的索引即第几组
			System.out.println("group[" + groupIdx + "].groupFieldValue:" + groupVL);
			// 当前分组内命中的总记录数
			System.out
					.println("group[" + groupIdx + "].totalHits:" + groupDocs.totalHits);
			int docIdx = 0;
			// 迭代组内的记录
			for (ScoreDoc scoreDoc : groupDocs.scoreDocs) {
				docIdx++;
				// 打印分组内部每条记录的索引文档ID及其评分
				System.out.println("group[" + groupIdx + "][" + docIdx + "]{docID:Score}:"
						+ scoreDoc.doc + "/" + scoreDoc.score);
				//根据docID可以获取到整个Document对象，通过doc.get(fieldName)可以获取某个存储域的域值
				//注意searcher.doc根据docID返回的document对象中不包含docValuesField域的域值，只包含非docValuesField域的域值，请知晓
				Document doc = searcher.doc(scoreDoc.doc);
				System.out.println("group[" + groupIdx + "][" + docIdx + "]{docID:author}:"
						+ doc.get("id") + ":" + doc.get("content"));
			}
			System.out.println("******************华丽且拉轰的分割线***********************");
		}
	}

	public static void groupSearch(IndexSearcher indexSearcher)
			throws IOException {

		Sort groupSort = Sort.RELEVANCE;

		/** 第一次查询只有Top N条记录进行分组统计 */
		final AbstractFirstPassGroupingCollector<?> c1 = createRandomFirstPassCollector(
				groupField, groupSort, 10);
		indexSearcher.search(new TermQuery(new Term("content", "random")), c1);

		/*
		 * final AbstractSecondPassGroupingCollector<?> c2 =
		 * createSecondPassCollector( c1, groupField, groupSort, null, 0, 5,
		 * true, true, true); indexSearcher.search(new TermQuery(new
		 * Term("content", "random")), c2);
		 */

		/** 第一个参数表示截取偏移量offset，截取[offset, offset+topN]范围内的组 */
		Collection<?> groups = c1.getTopGroups(0, true);
		System.out.println("group.size:" + groups.size());
		for (Object object : groups) {
			SearchGroup searchGroup = (SearchGroup) object;

			if (searchGroup.groupValue != null) {
				if (searchGroup.groupValue.getClass().isAssignableFrom(
						BytesRef.class)) {
					String groupVL = new String(
							(((BytesRef) searchGroup.groupValue)).bytes);
					if (groupVL.equals("")) {
						System.out.println("该分组不包含分组域");
					} else {
						System.out.println(groupVL);
					}
				} else if (searchGroup.groupValue.getClass().isAssignableFrom(
						MutableValueStr.class)) {
					if (searchGroup.groupValue.toString().endsWith("(null)")) {
						System.out.println("该分组不包含分组域");
					} else {
						System.out
								.println(new String(
										(((MutableValueStr) searchGroup.groupValue)).value
												.bytes()));
					}
				}
			} else {
				System.out.println("该分组不包含分组域");
			}
			for (int i = 0; i < searchGroup.sortValues.length; i++) {
				System.out.println("searchGroup.sortValues:"
						+ searchGroup.sortValues[i]);
			}
		}

		/*
		 * System.out.println("groups.maxScore：" + groups.maxScore);
		 * System.out.println("groups.totalHitCount：" + groups.totalHitCount);
		 * System.out.println("groups.totalGroupedHitCount：" +
		 * groups.totalGroupedHitCount); System.out.println("groups.length：" +
		 * groups.groups.length); System.out.println("");
		 * 
		 * GroupDocs<?> group = groups.groups[0]; compareGroupValue("author3",
		 * group); System.out.println(group.scoreDocs.length);
		 */

	}

	/**
	 * 创建测试用的索引文档
	 * 
	 * @throws IOException
	 */
	public static void createIndex() throws IOException {
		Directory dir = FSDirectory.open(Paths.get(indexDir));
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter writer = new IndexWriter(dir, indexWriterConfig);
		addDocuments(groupField, writer);
	}

	/**
	 * 添加索引文档
	 * 
	 * @param groupField
	 * @param writer
	 * @throws IOException
	 */
	public static void addDocuments(String groupField, IndexWriter writer)
			throws IOException {
		// 0
		Document doc = new Document();
		addGroupField(doc, groupField, "author1");
		doc.add(new TextField("content", "random text", Field.Store.YES));
		doc.add(new Field("id", "1", Store.YES, Index.NOT_ANALYZED));
		writer.addDocument(doc);

		// 1
		doc = new Document();
		addGroupField(doc, groupField, "author1");
		doc.add(new TextField("content", "some more random text",
				Field.Store.YES));
		doc.add(new Field("id", "2", Store.YES, Index.NOT_ANALYZED));
		writer.addDocument(doc);

		// 2
		doc = new Document();
		addGroupField(doc, groupField, "author1");
		doc.add(new TextField("content", "some more random textual data",
				Field.Store.YES));
		doc.add(new Field("id", "3", Store.YES, Index.NOT_ANALYZED));
		writer.addDocument(doc);

		// 3
		doc = new Document();
		addGroupField(doc, groupField, "author2");
		doc.add(new TextField("content", "some random text", Field.Store.YES));
		doc.add(new Field("id", "4", Store.YES, Index.NOT_ANALYZED));
		writer.addDocument(doc);

		// 4
		doc = new Document();
		addGroupField(doc, groupField, "author3");
		doc.add(new TextField("content", "some more random text",
				Field.Store.YES));
		doc.add(new Field("id", "5", Store.YES, Index.NOT_ANALYZED));
		writer.addDocument(doc);

		// 5
		doc = new Document();
		addGroupField(doc, groupField, "author3");
		doc.add(new TextField("content", "random", Field.Store.YES));
		doc.add(new Field("id", "6", Store.YES, Index.NOT_ANALYZED));
		writer.addDocument(doc);

		// 6 -- no author field
		doc = new Document();
		doc.add(new TextField("content",
				"random word stuck in alot of other text", Field.Store.YES));
		doc.add(new Field("id", "6", Store.YES, Index.NOT_ANALYZED));
		writer.addDocument(doc);
		writer.commit();
		writer.close();
	}

	/**
	 * 判断域值是否与分组域值相等
	 * 
	 * @param expected
	 * @param group
	 */
	private static void compareGroupValue(String expected, GroupDocs<?> group) {
		if (expected == null) {
			if (group.groupValue == null) {
				return;
			} else if (group.groupValue.getClass().isAssignableFrom(
					MutableValueStr.class)) {
				return;
			} else if (((BytesRef) group.groupValue).length == 0) {
				return;
			}
		}

		if (group.groupValue.getClass().isAssignableFrom(BytesRef.class)) {
			System.out.println("expected == groupValue?"
					+ new BytesRef(expected) == group.groupValue);
		} else if (group.groupValue.getClass().isAssignableFrom(
				MutableValueStr.class)) {
			MutableValueStr v = new MutableValueStr();
			v.value.copyChars(expected);
			System.out
					.println("expected == groupValue?" + v == group.groupValue);
		} else {
		}
	}

	/**
	 * 创建FirstPassCollector首次检索
	 * 
	 * @param groupField
	 * @param groupSort
	 * @param topDocs
	 * @param firstPassGroupingCollector
	 * @return
	 * @throws IOException
	 */
	private AbstractFirstPassGroupingCollector<?> createFirstPassCollector(
			String groupField, Sort groupSort, int topDocs,
			AbstractFirstPassGroupingCollector<?> firstPassGroupingCollector)
			throws IOException {
		if (TermFirstPassGroupingCollector.class
				.isAssignableFrom(firstPassGroupingCollector.getClass())) {
			ValueSource vs = new BytesRefFieldSource(groupField);
			return new FunctionFirstPassGroupingCollector(vs, new HashMap(),
					groupSort, topDocs);
		}
		return new TermFirstPassGroupingCollector(groupField, groupSort,
				topDocs);
	}

	private static AbstractFirstPassGroupingCollector<?> createRandomFirstPassCollector(
			String groupField, Sort groupSort, int topDocs) throws IOException {
		AbstractFirstPassGroupingCollector<?> selected;
		// boolean flag = new Random().nextBoolean();
		if (false) {
			ValueSource vs = new BytesRefFieldSource(groupField);
			// FunctionFirstPassGroupingCollector区别是对于分组域的值采用MutableValueStr进行存储，
			// MutableValueStr内部维护的是一个BytesRefBuilder，BytesRefBuilder内部有一个grow函数，会自动
			// 扩充内部byte[]容量，而BytesRef是定长的buffer
			selected = new FunctionFirstPassGroupingCollector(vs,
					new HashMap(), groupSort, topDocs);
		} else {
			// TermFirstPassGroupingCollector适用于你的分组域是一个非DocValuesField
			selected = new TermFirstPassGroupingCollector(groupField,
					groupSort, topDocs);
		}
		return selected;
	}

	private static <T> AbstractSecondPassGroupingCollector<T> createSecondPassCollector(
			AbstractFirstPassGroupingCollector firstPassGroupingCollector,
			String groupField, Sort groupSort, Sort sortWithinGroup,
			int groupOffset, int maxDocsPerGroup, boolean getScores,
			boolean getMaxScores, boolean fillSortFields) throws IOException {

		if (TermFirstPassGroupingCollector.class
				.isAssignableFrom(firstPassGroupingCollector.getClass())) {
			Collection<SearchGroup<BytesRef>> searchGroups = firstPassGroupingCollector
					.getTopGroups(groupOffset, fillSortFields);
			return (AbstractSecondPassGroupingCollector) new TermSecondPassGroupingCollector(
					groupField, searchGroups, groupSort, sortWithinGroup,
					maxDocsPerGroup, getScores, getMaxScores, fillSortFields);
		} else {
			ValueSource vs = new BytesRefFieldSource(groupField);
			Collection<SearchGroup<MutableValue>> searchGroups = firstPassGroupingCollector
					.getTopGroups(groupOffset, fillSortFields);
			return (AbstractSecondPassGroupingCollector) new FunctionSecondPassGroupingCollector(
					searchGroups, groupSort, sortWithinGroup, maxDocsPerGroup,
					getScores, getMaxScores, fillSortFields, vs, new HashMap());
		}
	}

	// Basically converts searchGroups from MutableValue to BytesRef if grouping
	// by ValueSource
	@SuppressWarnings("unchecked")
	private AbstractSecondPassGroupingCollector<?> createSecondPassCollector(
			AbstractFirstPassGroupingCollector<?> firstPassGroupingCollector,
			String groupField, Collection<SearchGroup<BytesRef>> searchGroups,
			Sort groupSort, Sort sortWithinGroup, int maxDocsPerGroup,
			boolean getScores, boolean getMaxScores, boolean fillSortFields)
			throws IOException {
		if (firstPassGroupingCollector.getClass().isAssignableFrom(
				TermFirstPassGroupingCollector.class)) {
			return new TermSecondPassGroupingCollector(groupField,
					searchGroups, groupSort, sortWithinGroup, maxDocsPerGroup,
					getScores, getMaxScores, fillSortFields);
		} else {
			ValueSource vs = new BytesRefFieldSource(groupField);
			List<SearchGroup<MutableValue>> mvalSearchGroups = new ArrayList<SearchGroup<MutableValue>>(
					searchGroups.size());
			for (SearchGroup<BytesRef> mergedTopGroup : searchGroups) {
				SearchGroup<MutableValue> sg = new SearchGroup();
				MutableValueStr groupValue = new MutableValueStr();
				if (mergedTopGroup.groupValue != null) {
					groupValue.value.copyBytes(mergedTopGroup.groupValue);
				} else {
					groupValue.exists = false;
				}
				sg.groupValue = groupValue;
				sg.sortValues = mergedTopGroup.sortValues;
				mvalSearchGroups.add(sg);
			}

			return new FunctionSecondPassGroupingCollector(mvalSearchGroups,
					groupSort, sortWithinGroup, maxDocsPerGroup, getScores,
					getMaxScores, fillSortFields, vs, new HashMap());
		}
	}

	private AbstractAllGroupsCollector<?> createAllGroupsCollector(
			AbstractFirstPassGroupingCollector<?> firstPassGroupingCollector,
			String groupField) {
		if (firstPassGroupingCollector.getClass().isAssignableFrom(
				TermFirstPassGroupingCollector.class)) {
			return new TermAllGroupsCollector(groupField);
		} else {
			ValueSource vs = new BytesRefFieldSource(groupField);
			return new FunctionAllGroupsCollector(vs, new HashMap());
		}
	}

	/**
	 * 添加分组域
	 * 
	 * @param doc
	 *            索引文档
	 * @param groupField
	 *            需要分组的域名称
	 * @param value
	 *            域值
	 */
	private static void addGroupField(Document doc, String groupField,
			String value) {
		doc.add(new SortedDocValuesField(groupField, new BytesRef(value)));
	}
}
