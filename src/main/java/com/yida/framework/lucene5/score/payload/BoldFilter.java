package com.yida.framework.lucene5.score.payload;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;

import com.yida.framework.lucene5.util.Tools;

public class BoldFilter extends TokenFilter {
	public static int IS_NOT_BOLD = 0;
	public static int IS_BOLD = 1;
	private CharTermAttribute termAtt;
	private PayloadAttribute payloadAtt;

	protected BoldFilter(TokenStream input) {
		super(input);
		termAtt = addAttribute(CharTermAttribute.class);
		payloadAtt = addAttribute(PayloadAttribute.class);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			final char[] buffer = termAtt.buffer();
			final int length = termAtt.length();
			String tokenstring = new String(buffer, 0, length).toLowerCase();
			//System.out.println("token:" + tokenstring);
			if (tokenstring.startsWith("<b>") && tokenstring.endsWith("</b>")) {
				tokenstring = tokenstring.replace("<b>", "");
				tokenstring = tokenstring.replace("</b>", "");
				termAtt.copyBuffer(tokenstring.toCharArray(), 0, tokenstring.length());
				//在分词阶段，设置payload信息
				payloadAtt.setPayload(new BytesRef(Tools.int2bytes(IS_BOLD)));
			} else {
				payloadAtt.setPayload(new BytesRef(Tools.int2bytes(IS_NOT_BOLD)));
			}
			return true;
		} else
			return false;
	}
}
