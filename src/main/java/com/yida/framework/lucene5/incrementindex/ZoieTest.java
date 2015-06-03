package com.yida.framework.lucene5.incrementindex;

import java.util.Timer;

import org.ansj.lucene5.AnsjAnalyzer;
import org.apache.lucene.analysis.Analyzer;
/**
 * 增量索引测试
 * @author Lanxiaowei
 *
 */
public class ZoieTest {
	public static void main(String[] args) throws Exception {
		
		String userIndexPath = "C:/zoieindex";
		Analyzer analyzer = new AnsjAnalyzer();
		PersonDao personDao = new PersonDaoImpl();
		int zoieBatchSize = 10;
		int zoieBatchDelay = 1000;
		
		//先读取数据库表中已有数据创建索引
		CreateIndexTest createIndexTest = new CreateIndexTest(personDao, userIndexPath);
		createIndexTest.index();
		
		//再往数据库表中插入一条数据,模拟数据动态变化
		PersonDaoTest.addPerson();
		
		
		ZoieIndex zoindex = new ZoieIndex(userIndexPath, analyzer, personDao, 
			zoieBatchSize, zoieBatchDelay);
		Timer timer = new Timer("myTimer",false);
		timer.scheduleAtFixedRate(new ZoieIndexTimerTask(zoindex),10L,3000L);
		
		//睡眠2分钟
		Thread.sleep(2*60*1000L);
		//2分钟后定时器取消
		timer.cancel();
		System.out.println("Timer cancled.");
		
		/**把索引flush到硬盘*/
		zoindex.destroy();
		System.out.println("finished.");
	}
}
