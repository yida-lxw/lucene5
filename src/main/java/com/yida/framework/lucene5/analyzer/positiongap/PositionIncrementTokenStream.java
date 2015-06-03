package com.yida.framework.lucene5.analyzer.positiongap;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public class PositionIncrementTokenStream extends TokenStream {
	private boolean first = true;
    private PositionIncrementAttribute attribute;
    /**位置增量*/
    private final int positionIncrement;
    
    public PositionIncrementTokenStream(final int positionIncrement) {
        super();
        this.positionIncrement = positionIncrement;
        attribute = addAttribute(PositionIncrementAttribute.class);
    }
    
	@Override
	public boolean incrementToken() throws IOException {
		if (first) {
            first = false;
            attribute.setPositionIncrement(positionIncrement);
            return true;
        } else {
            return false;
        }
	}
	@Override
	public void reset() throws IOException {
		super.reset();
		first = true;
	}
}
