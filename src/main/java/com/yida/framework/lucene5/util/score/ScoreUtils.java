package com.yida.framework.lucene5.util.score;

import org.apache.lucene.index.NumericDocValues;

import com.yida.framework.lucene5.util.Constans;

/**
 * 计算衰减因子[按天为单位]
 * @author Lanxiaowei
 *
 */
public class ScoreUtils {
	/**存储衰减因子-按天为单位*/
	private static float[] daysDampingFactor = new float[120];
	/**降级阀值*/
	private static float demoteboost = 0.9f;
	static {
		daysDampingFactor[0] = 1;
		//第一周时权重降级处理
		for (int i = 1; i < 7; i++) {
			daysDampingFactor[i] = daysDampingFactor[i - 1] * demoteboost;
		}
		//第二周
		for (int i = 7; i < 31; i++) {			
			daysDampingFactor[i] = daysDampingFactor[i / 7 * 7 - 1]
					* demoteboost;
		}
		//第三周以后
		for (int i = 31; i < daysDampingFactor.length; i++) {
			daysDampingFactor[i] = daysDampingFactor[i / 31 * 31 - 1]
					* demoteboost;
		}
	}
	
	//根据相差天数获取当前的权重衰减因子
	private static float dayDamping(int delta) {
		float factor = delta < daysDampingFactor.length ? daysDampingFactor[delta]
				: daysDampingFactor[daysDampingFactor.length - 1];
		System.out.println("delta:" + delta + "-->" + "factor:" + factor);
		return factor;
	}
	
	public static float getNewsScoreFactor(long now, NumericDocValues numericDocValues, int docId) {
		long time = numericDocValues.get(docId);
		float factor = 1;
		int day = (int) (time / Constans.DAY_MILLIS);
		int nowDay = (int) (now / Constans.DAY_MILLIS);
		System.out.println(day + ":" + nowDay + ":" + (nowDay - day));
		// 如果提供的日期比当前日期小，则计算相差天数，传入dayDamping计算日期衰减因子
		if (day < nowDay) {
			factor = dayDamping(nowDay - day);
		} else if (day > nowDay) {
			//如果提供的日期比当前日期还大即提供的是未来的日期
			factor = Float.MIN_VALUE;
		} else if (now - time <= Constans.HALF_HOUR_MILLIS && now >= time) {
			//如果两者是同一天且提供的日期是过去半小时之内的，则权重因子乘以2
			factor = 2;
		}
		return factor;
	}
	
	public static float getNewsScoreFactor(long now, long time) {
		float factor = 1;
		int day = (int) (time / Constans.DAY_MILLIS);
		int nowDay = (int) (now / Constans.DAY_MILLIS);
		// 如果提供的日期比当前日期小，则计算相差天数，传入dayDamping计算日期衰减因子
		if (day < nowDay) {
			factor = dayDamping(nowDay - day);
		} else if (day > nowDay) {
			//如果提供的日期比当前日期还大即提供的是未来的日期
			factor = Float.MIN_VALUE;
		} else if (now - time <= Constans.HALF_HOUR_MILLIS && now >= time) {
			//如果两者是同一天且提供的日期是过去半小时之内的，则权重因子乘以2
			factor = 2;
		}
		return factor;
	}
	public static float getNewsScoreFactor(long time) {
		long now = System.currentTimeMillis();
		return getNewsScoreFactor(now, time);
	}
}
