package com.yida.framework.lucene5.termvector;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRefBuilder;

/**
 * 利用项向量自动书籍分类[项向量夹角越小相似度越高]
 * 
 * @author Lanxiaowei
 * 
 */
public class CategoryTest {
	public static void main(String[] args) throws IOException {
		String indexDir = "C:/lucenedir";
		Directory directory = FSDirectory.open(Paths.get(indexDir));
		IndexReader reader = DirectoryReader.open(directory);
		//IndexSearcher searcher = new IndexSearcher(reader);
		Map<String, Map<String, Integer>> categoryMap = new TreeMap<String, Map<String,Integer>>();
		//构建分类的项向量
		buildCategoryVectors(categoryMap, reader);
		
		getCategory("extreme agile methodology",categoryMap);
		
		getCategory("montessori education philosophy",categoryMap);
		
	}

	/**
	 * 根据项向量自动判断分类[返回项向量夹角最小的即相似度最高的]
	 * 
	 * @param subject
	 * @return
	 */
	private static String getCategory(String subject,
			Map<String, Map<String, Integer>> categoryMap) {
		//将subject按空格分割
		String[] words = subject.split(" ");

		Iterator<String> categoryIterator = categoryMap.keySet().iterator();
		double bestAngle = Double.MAX_VALUE;
		String bestCategory = null;

		while (categoryIterator.hasNext()) {
			String category = categoryIterator.next();

			double angle = computeAngle(categoryMap, words, category);
			// System.out.println(" -> angle = " + angle + " (" +
			// Math.toDegrees(angle) + ")");
			if (angle < bestAngle) {
				bestAngle = angle;
				bestCategory = category;
			}
		}
		System.out.println("The best like:" + bestCategory + "-->" + subject);
		return bestCategory;
	}

	public static void buildCategoryVectors(
			Map<String, Map<String, Integer>> categoryMap, IndexReader reader)
			throws IOException {
		int maxDoc = reader.maxDoc();
		// 遍历所有索引文档
		for (int i = 0; i < maxDoc; i++) {
			Document doc = reader.document(i);
			// 获取category域的值
			String category = doc.get("category");

			Map<String, Integer> vectorMap = categoryMap.get(category);
			if (vectorMap == null) {
				vectorMap = new TreeMap<String, Integer>();
				categoryMap.put(category, vectorMap);
			}
			
			Terms termFreqVector = reader.getTermVector(i, "subject");
			TermsEnum termsEnum = termFreqVector.iterator(null);
			addTermFreqToMap(vectorMap, termsEnum);
		}
	}

	/**
	 * 统计项向量中每个Term出现的document个数，key为Term的值，value为document总个数
	 * 
	 * @param vectorMap
	 * @param termsEnum
	 * @throws IOException
	 */
	private static void addTermFreqToMap(Map<String, Integer> vectorMap,
			TermsEnum termsEnum) throws IOException {
		CharsRefBuilder spare = new CharsRefBuilder();
		BytesRef text = null;
		while ((text = termsEnum.next()) != null) {
			spare.copyUTF8Bytes(text);
			String term = spare.toString();
			int docFreq = termsEnum.docFreq();
			System.out.println("term:" + term + "-->docFreq:" + docFreq);
			// 包含该term就累加document出现频率
			if (vectorMap.containsKey(term)) {
				Integer value = (Integer) vectorMap.get(term);
				vectorMap.put(term, new Integer(value.intValue() + docFreq));
			} else {
				vectorMap.put(term, new Integer(docFreq));
			}
		}
	}

	/**
	 * 计算两个Term项向量的夹角[夹角越小则相似度越大]
	 * 
	 * @param categoryMap
	 * @param words
	 * @param category
	 * @return
	 */
	private static double computeAngle(Map<String, Map<String, Integer>> categoryMap,
			String[] words, String category) {
		Map<String, Integer> vectorMap = categoryMap.get(category);

		int dotProduct = 0;
		int sumOfSquares = 0;
		for (String word : words) {
			int categoryWordFreq = 0;

			if (vectorMap.containsKey(word)) {
				categoryWordFreq = vectorMap.get(word).intValue();
			}

			dotProduct += categoryWordFreq;
			sumOfSquares += categoryWordFreq * categoryWordFreq;
		}

		double denominator = 0.0d;
		if (sumOfSquares == words.length) {
			denominator = sumOfSquares;
		} else {
			denominator = Math.sqrt(sumOfSquares) * Math.sqrt(words.length);
		}

		double ratio = dotProduct / denominator;

		return Math.acos(ratio);
	}
}
