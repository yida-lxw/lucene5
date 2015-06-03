package com.yida.framework.lucene5.score.payload;

import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.util.BytesRef;

import com.yida.framework.lucene5.util.Tools;

public class PayloadSimilarity extends DefaultSimilarity {
	@Override
	public float scorePayload(int doc, int start, int end, BytesRef payload) {
		int isbold = Tools.bytes2int(payload.bytes);
		if (isbold == BoldFilter.IS_BOLD) {
			return 100f;
		}
		return 1f;
	}
}
