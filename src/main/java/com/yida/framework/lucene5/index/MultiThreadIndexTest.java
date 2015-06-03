package com.yida.framework.lucene5.index;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多线程创建索引
 * @author Lanxiaowei
 *
 */
public class MultiThreadIndexTest {
	/**
	 * 创建了5个线程同时创建索引
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		int threadCount = 5;
		ExecutorService pool = Executors.newFixedThreadPool(threadCount);
		CountDownLatch countDownLatch1 = new CountDownLatch(1);
		CountDownLatch countDownLatch2 = new CountDownLatch(threadCount);
		for(int i = 0; i < threadCount; i++) {
			Runnable runnable = new IndexCreator("C:/doc" + (i+1), "C:/lucenedir" + (i+1),threadCount,
					countDownLatch1,countDownLatch2);
			//子线程交给线程池管理
			pool.execute(runnable);
		}
		
		countDownLatch1.countDown();
		System.out.println("开始创建索引");
		//等待所有线程都完成
		countDownLatch2.await();
		//线程全部完成工作
		System.out.println("所有线程都创建索引完毕");
		//释放线程池资源
		pool.shutdown();
	}
}
