package com.yida.framework.lucene5.pinyin;

import java.io.IOException;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
/**
 * 拼音过滤器[负责将汉字转换为拼音]
 * @author Lanxiaowei
 *
 */
public class PinyinTokenFilter extends TokenFilter {
	private final CharTermAttribute termAtt;
	/**汉语拼音输出转换器[基于Pinyin4j]*/
	private HanyuPinyinOutputFormat outputFormat;
	/**对于多音字会有多个拼音,firstChar即表示只取第一个,否则会取多个拼音*/
	private boolean firstChar;
	/**Term最小长度[小于这个最小长度的不进行拼音转换]*/
	private int minTermLength;
	private char[] curTermBuffer;
	private int curTermLength;
	private boolean outChinese;

	public PinyinTokenFilter(TokenStream input) {
		this(input, Constant.DEFAULT_FIRST_CHAR, Constant.DEFAULT_MIN_TERM_LRNGTH);
	}

	public PinyinTokenFilter(TokenStream input, boolean firstChar) {
		this(input, firstChar, Constant.DEFAULT_MIN_TERM_LRNGTH);
	}

	public PinyinTokenFilter(TokenStream input, boolean firstChar,
			int minTermLenght) {
		this(input, firstChar, minTermLenght, Constant.DEFAULT_NGRAM_CHINESE);
	}

	public PinyinTokenFilter(TokenStream input, boolean firstChar,
			int minTermLenght, boolean outChinese) {
		super(input);

		this.termAtt = ((CharTermAttribute) addAttribute(CharTermAttribute.class));
		this.outputFormat = new HanyuPinyinOutputFormat();
		this.firstChar = false;
		this.minTermLength = Constant.DEFAULT_MIN_TERM_LRNGTH;

		this.outChinese = Constant.DEFAULT_OUT_CHINESE;

		this.firstChar = firstChar;
		this.minTermLength = minTermLenght;
		if (this.minTermLength < 1) {
			this.minTermLength = 1;
		}
		this.outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		this.outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
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
				this.curTermBuffer = ((char[]) this.termAtt.buffer().clone());
				this.curTermLength = this.termAtt.length();
			}

			if (this.outChinese) {
				this.outChinese = false;
				this.termAtt.copyBuffer(this.curTermBuffer, 0,
						this.curTermLength);
				return true;
			}
			this.outChinese = true;
			String chinese = this.termAtt.toString();

			if (containsChinese(chinese)) {
				this.outChinese = true;
				if (chinese.length() >= this.minTermLength) {
					try {
						String chineseTerm = getPinyinString(chinese);
						this.termAtt.copyBuffer(chineseTerm.toCharArray(), 0,
								chineseTerm.length());
					} catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
						badHanyuPinyinOutputFormatCombination.printStackTrace();
					}
					this.curTermBuffer = null;
					return true;
				}

			}

			this.curTermBuffer = null;
		}
	}

	public void reset() throws IOException {
		super.reset();
	}

	private String getPinyinString(String chinese)
			throws BadHanyuPinyinOutputFormatCombination {
		String chineseTerm = null;
		if (this.firstChar) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < chinese.length(); i++) {
				String[] array = PinyinHelper.toHanyuPinyinStringArray(
						chinese.charAt(i), this.outputFormat);
				if ((array != null) && (array.length != 0)) {
					String s = array[0];
					char c = s.charAt(0);

					sb.append(c);
				}
			}
			chineseTerm = sb.toString();
		} else {
			chineseTerm = PinyinHelper.toHanyuPinyinString(chinese,
					this.outputFormat, "");
		}
		return chineseTerm;
	}
}
