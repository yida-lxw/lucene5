package com.yida.framework.lucene5.function;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.valuesource.FieldCacheSource;

import com.yida.framework.lucene5.util.score.ScoreUtils;

/**
 * 自定义ValueSource[计算日期递减时的权重因子，日期越近权重值越高]
 * @author Lanxiaowei
 *
 */
public class DateDampingValueSouce extends FieldCacheSource {
	//当前时间
	private static long now;
	public DateDampingValueSouce(String field) {
		super(field);
		//初始化当前时间
		now = System.currentTimeMillis();
	}
	/**
	 * 这里Map里存的是IndexSeacher,context.get("searcher");获取
	 */
	@Override
	public FunctionValues getValues(Map context, LeafReaderContext leafReaderContext)
			throws IOException {
		final NumericDocValues numericDocValues = DocValues.getNumeric(leafReaderContext.reader(), field);	
		return new FunctionValues() {
			@Override
			public float floatVal(int doc) {
				return ScoreUtils.getNewsScoreFactor(now, numericDocValues,doc);
			}
			@Override
			public int intVal(int doc) {
				return (int) ScoreUtils.getNewsScoreFactor(now, numericDocValues,doc);
			}
			@Override
			public String toString(int doc) {
				return description() + '=' + intVal(doc);
			}
		};
	}
	
}
