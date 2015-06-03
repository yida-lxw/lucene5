package com.yida.framework.lucene5.pinyin;

import java.util.Map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;
/**
 * PinyinNGramTokenFilter工厂类
 * @author Lanxiaowei
 *
 */
public class PinyinNGramTokenFilterFactory extends TokenFilterFactory {
	public static final boolean DEFAULT_NGRAM_CHINESE = false;
	private int minGram;
	private int maxGram;
	private boolean nGramChinese;

	public PinyinNGramTokenFilterFactory(Map<String, String> args) {
		super(args);

		this.minGram = getInt(args, "minGram", Constant.DEFAULT_MIN_GRAM);
		this.maxGram = getInt(args, "maxGram", Constant.DEFAULT_MAX_GRAM);
		this.nGramChinese = getBoolean(args, "nGramChinese", DEFAULT_NGRAM_CHINESE);
	}

	public TokenFilter create(TokenStream input) {
		return new PinyinNGramTokenFilter(input, this.minGram, this.maxGram,
				this.nGramChinese);
	}
}