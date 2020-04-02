package com.fire.core.thread;

import java.util.concurrent.ThreadFactory;

/**
 * 线程池工厂类
 * 
 * @author Administrator
 *
 */
public interface WolfThreadFactory extends ThreadFactory {

	/**
	 * 创建线程
	 * 
	 * @param r
	 * @param context
	 * @return
	 */
	Thread newThread(WolfRunnable r, WolfWebContext context);

}
