package com.yida.framework.lucene5.analyzer.synonym;

import java.io.IOException;
/**
 * 同义词提取引擎
 * @author Lanxiaowei
 *
 */
public interface SynonymEngine {
	String[] getSynonyms(String s) throws IOException;
}