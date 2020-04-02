package com.fire.core.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Test {
	public static void main(String[] args) {
		// 用来测试的List
		List<String> data = new ArrayList<>();
		CountDownLatch countDownLatch = new CountDownLatch(100);
		WolfExecutors executorService = WolfExecutors.newWolfCachedThreadPool(1, 100);
		for (int i = 0; i < 100; i++) {
			executorService.execute(new Runnable() {
				
				@Override
				public void run() {
					data.add("123");
					countDownLatch.countDown();
				}
			});
		}
		
//		// 用来让主线程等待100个子线程执行完毕
//		CountDownLatch countDownLatch = new CountDownLatch(100);
//		// 启动100个子线程
//		for (int i = 0; i < 100; i++) {
//			SampleTask task = new SampleTask(data, countDownLatch);
//			Thread thread = new Thread(task);
//			thread.start();
//		}
		try {
			// 主线程等待所有子线程执行完成，再向下执行
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println(data.size());
//		// List的size
//		System.out.println(data.size());
	}
}

class SampleTask implements Runnable {
	CountDownLatch countDownLatch;
	List<String> data;

	public SampleTask(List<String> data, CountDownLatch countDownLatch) {
		this.data = data;
		this.countDownLatch = countDownLatch;
	}

	@Override
		public void run() {
		   // 每个线程向List中添加100个元素  
		   for(int i = 0; i < 100; i++)  
		   {  
		       data.add("1");
		   }  
		   // 完成一个子线程  
		   countDownLatch.countDown();
		}
}
