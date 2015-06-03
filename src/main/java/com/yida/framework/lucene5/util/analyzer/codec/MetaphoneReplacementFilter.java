package com.yida.framework.lucene5.util.analyzer.codec;

import java.io.IOException;

import org.apache.commons.codec.language.Metaphone;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class MetaphoneReplacementFilter extends TokenFilter {
	public static final String METAPHONE = "metaphone";

	private Metaphone metaphoner = new Metaphone();
	private CharTermAttribute termAttr;
	private TypeAttribute typeAttr;

	protected MetaphoneReplacementFilter(TokenStream input) {
		super(input);
		termAttr = addAttribute(CharTermAttribute.class);
		typeAttr = addAttribute(TypeAttribute.class);
	}

	public boolean incrementToken() throws IOException {
		if (!input.incrementToken()) {
			return false;
		}
		String encoded = metaphoner.encode(termAttr.toString());
		//System.out.println("termAttr:" + termAttr.toString() + ",encoded:" + encoded);
		//termAttr.setTermBuffer(encoded);
		//termAttr.append(encoded);
		termAttr.copyBuffer(encoded.toCharArray(), 0, encoded.length());
		typeAttr.setType(METAPHONE);
		return true;
	}
}
