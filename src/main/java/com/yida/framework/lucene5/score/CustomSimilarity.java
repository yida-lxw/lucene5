package com.yida.framework.lucene5.score;

import org.apache.lucene.search.similarities.DefaultSimilarity;

public class CustomSimilarity extends DefaultSimilarity {
	@Override
	public float idf(long docFreq, long numDocs) {
		//docFreq表示某个Term在哪几个文档中出现过，numDocs表示总的文档数
		System.out.println("docFreq：" + docFreq);
		System.out.println("numDocs：" + numDocs);
		return super.idf(docFreq, numDocs);
	}
}
