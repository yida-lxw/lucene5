package com.yida.framework.lucene5.pinyin;

import java.util.Map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;
/**
 * PinyinTokenFilter工厂类
 * @author Lanxiaowei
 *
 */
public class PinyinTokenFilterFactory extends TokenFilterFactory {
	private boolean firstChar;
	private boolean outChinese;
	private int minTermLenght;

	public PinyinTokenFilterFactory(Map<String, String> args) {
		super(args);
		this.firstChar = getBoolean(args, "firstChar", Constant.DEFAULT_FIRST_CHAR);
		this.outChinese = getBoolean(args, "outChinese", Constant.DEFAULT_OUT_CHINESE);
		this.minTermLenght = getInt(args, "minTermLength", Constant.DEFAULT_MIN_TERM_LRNGTH);
	}

	public TokenFilter create(TokenStream input) {
		return new PinyinTokenFilter(input, this.firstChar,
				this.minTermLenght, this.outChinese);
	}

	public boolean isFirstChar() {
		return firstChar;
	}

	public void setFirstChar(boolean firstChar) {
		this.firstChar = firstChar;
	}

	public boolean isOutChinese() {
		return outChinese;
	}

	public void setOutChinese(boolean outChinese) {
		this.outChinese = outChinese;
	}

	public int getMinTermLenght() {
		return minTermLenght;
	}

	public void setMinTermLenght(int minTermLenght) {
		this.minTermLenght = minTermLenght;
	}
}
