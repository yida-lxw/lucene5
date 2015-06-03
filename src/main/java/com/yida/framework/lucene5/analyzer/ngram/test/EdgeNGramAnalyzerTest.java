package com.yida.framework.lucene5.analyzer.ngram.test;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;

import com.yida.framework.lucene5.analyzer.ngram.EdgeNGramAnalyzer;
import com.yida.framework.lucene5.util.AnalyzerUtils;
/**
 * EdgeNGramAnalyzer自定义分词器测试
 * @author Lanxiaowei
 *
 */
public class EdgeNGramAnalyzerTest {
	public static void main(String[] args) throws IOException {
		String text = "丽丽，我爱你 ";
		Analyzer analyzer = new EdgeNGramAnalyzer(text.length());
		AnalyzerUtils.displayTokens(analyzer, text);
	}
}
