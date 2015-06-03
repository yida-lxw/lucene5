package com.yida.framework.lucene5.analyzer.synonym;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;

import com.yida.framework.lucene5.util.AnalyzerUtils;

public class SynonymAnalyzerTest {
	public static void main(String[] args) throws IOException {
		String text = "The quick brown fox jumps over the lazy dog";
	    Analyzer analyzer = new SynonymAnalyzer(new BaseSynonymEngine());
		AnalyzerUtils.displayTokens(analyzer, text);
	}
}
