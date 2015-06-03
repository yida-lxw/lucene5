package com.yida.framework.lucene5.score.custom;

import java.io.IOException;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.queries.CustomScoreProvider;

public class RecencyBoostCustomScoreProvider extends CustomScoreProvider {
	//权重倍数
	private double multiplier;
	// 从1970-01-01至今的总天数
	private int day;
	// 最大过期天数
	private int maxDaysAgo;
	// 日期域的名称
	private String dayField;
	// 域缓存值
	private NumericDocValues publishDay;
	
	private SortedDocValues titleValues;

	public RecencyBoostCustomScoreProvider(LeafReaderContext context,double multiplier,int day,int maxDaysAgo,String dayField) {
		super(context);
		this.multiplier = multiplier;
		this.day = day;
		this.maxDaysAgo = maxDaysAgo;
		this.dayField = dayField;
		try {
			publishDay = context.reader().getNumericDocValues(dayField);
			titleValues = context.reader().getSortedDocValues("title2");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * subQueryScore:指的是普通Query查询的评分
	 * valSrcScore：指的是FunctionQuery查询的评分
	 */
	@Override
	public float customScore(int docId, float subQueryScore, float valSrcScore)
			throws IOException {
		String title = titleValues.get(docId).utf8ToString();
		int daysAgo = (int) (day - publishDay.get(docId));
		//System.out.println(title + ":" + daysAgo + ":" + maxDaysAgo);
		//如果在6年之内
		if (daysAgo < maxDaysAgo) {
			float boost = (float) (multiplier * (maxDaysAgo - daysAgo) / maxDaysAgo);
			return (float) (subQueryScore * (1.0 + boost));
		}
		return subQueryScore;
	}
}
