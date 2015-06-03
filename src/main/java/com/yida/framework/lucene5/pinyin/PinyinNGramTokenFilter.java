package com.yida.framework.lucene5.pinyin;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
/**
 * 对转换后的拼音进行NGram处理的TokenFilter
 * @author Lanxiaowei
 *
 */
public class PinyinNGramTokenFilter extends TokenFilter {
	public static final boolean DEFAULT_NGRAM_CHINESE = false;
	private final int minGram;
	private final int maxGram;
	/**是否需要对中文进行NGram[默认为false]*/
	private final boolean nGramChinese;
	private final CharTermAttribute termAtt;
	private final OffsetAttribute offsetAtt;
	private char[] curTermBuffer;
	private int curTermLength;
	private int curGramSize;
	private int tokStart;

	public PinyinNGramTokenFilter(TokenStream input) {
		this(input, Constant.DEFAULT_MIN_GRAM, Constant.DEFAULT_MAX_GRAM, DEFAULT_NGRAM_CHINESE);
	}
	
	public PinyinNGramTokenFilter(TokenStream input, int maxGram) {
		this(input, Constant.DEFAULT_MIN_GRAM, maxGram, DEFAULT_NGRAM_CHINESE);
	}
	
	public PinyinNGramTokenFilter(TokenStream input, int minGram, int maxGram) {
		this(input, minGram, maxGram, DEFAULT_NGRAM_CHINESE);
	}

	public PinyinNGramTokenFilter(TokenStream input, int minGram, int maxGram,
			boolean nGramChinese) {
		super(input);

		this.termAtt = ((CharTermAttribute) addAttribute(CharTermAttribute.class));
		this.offsetAtt = ((OffsetAttribute) addAttribute(OffsetAttribute.class));

		if (minGram < 1) {
			throw new IllegalArgumentException(
					"minGram must be greater than zero");
		}
		if (minGram > maxGram) {
			throw new IllegalArgumentException(
					"minGram must not be greater than maxGram");
		}
		this.minGram = minGram;
		this.maxGram = maxGram;
		this.nGramChinese = nGramChinese;
	}

	public static boolean containsChinese(String s) {
		if ((s == null) || ("".equals(s.trim())))
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (isChinese(s.charAt(i)))
				return true;
		}
		return false;
	}

	public static boolean isChinese(char a) {
		int v = a;
		return (v >= 19968) && (v <= 171941);
	}

	public final boolean incrementToken() throws IOException {
		while (true) {
			if (this.curTermBuffer == null) {
				if (!this.input.incrementToken()) {
					return false;
				}
				if ((!this.nGramChinese)
						&& (containsChinese(this.termAtt.toString()))) {
					return true;
				}
				this.curTermBuffer = ((char[]) this.termAtt.buffer().clone());

				this.curTermLength = this.termAtt.length();
				this.curGramSize = this.minGram;
				this.tokStart = this.offsetAtt.startOffset();
			}

			if (this.curGramSize <= this.maxGram) {
				if (this.curGramSize >= this.curTermLength) {
					clearAttributes();
					this.offsetAtt.setOffset(this.tokStart + 0, this.tokStart
							+ this.curTermLength);
					this.termAtt.copyBuffer(this.curTermBuffer, 0,
							this.curTermLength);
					this.curTermBuffer = null;
					return true;
				}
				int start = 0;
				int end = start + this.curGramSize;
				clearAttributes();
				this.offsetAtt.setOffset(this.tokStart + start, this.tokStart
						+ end);
				this.termAtt.copyBuffer(this.curTermBuffer, start,
						this.curGramSize);
				this.curGramSize += 1;
				return true;
			}

			this.curTermBuffer = null;
		}
	}

	public void reset() throws IOException {
		super.reset();
		this.curTermBuffer = null;
	}
}
