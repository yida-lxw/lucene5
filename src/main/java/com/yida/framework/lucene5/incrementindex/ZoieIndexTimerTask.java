package com.yida.framework.lucene5.incrementindex;

import java.util.TimerTask;

import proj.zoie.api.ZoieException;

/**
 * Zoie定时增量索引任务
 * @author LANXIAOWEI
 *
 */
public class ZoieIndexTimerTask extends TimerTask {
	private ZoieIndex zoieIndex;
	
	@Override
	public void run() {
		try {
			zoieIndex.init();
			zoieIndex.updateIndexData();
		} catch (ZoieException e) {
			e.printStackTrace();
		}
	}

	public ZoieIndexTimerTask(ZoieIndex zoieIndex) {
		super();
		this.zoieIndex = zoieIndex;
	}

	public ZoieIndex getZoieIndex() {
		return zoieIndex;
	}

	public void setZoieIndex(ZoieIndex zoieIndex) {
		this.zoieIndex = zoieIndex;
	}
}
