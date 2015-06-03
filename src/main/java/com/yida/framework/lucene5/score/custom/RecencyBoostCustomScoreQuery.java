package com.yida.framework.lucene5.score.custom;

import java.io.IOException;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;

public class RecencyBoostCustomScoreQuery extends CustomScoreQuery {
	// 倍数
	private double multiplier;
	// 从1970-01-01至今的总天数
	private int day;
	// 最大过期天数
	private int maxDaysAgo;
	// 日期域的名称
	private String dayField;
	public RecencyBoostCustomScoreQuery(Query subQuery,double multiplier,int day,int maxDaysAgo,String dayField) {
		super(subQuery);
		this.multiplier = multiplier;
		this.day = day;
		this.maxDaysAgo = maxDaysAgo;
		this.dayField = dayField;
	}

	@Override
	protected CustomScoreProvider getCustomScoreProvider(
			LeafReaderContext context) throws IOException {
		return new RecencyBoostCustomScoreProvider(context,multiplier,day,maxDaysAgo,dayField);
	}
}
