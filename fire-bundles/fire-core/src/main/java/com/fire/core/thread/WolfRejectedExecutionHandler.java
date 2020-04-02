package com.fire.core.thread;

import java.util.concurrent.RejectedExecutionHandler;

/**
 * 帮助类接口
 * 
 * @author Administrator
 *
 */
public interface WolfRejectedExecutionHandler extends RejectedExecutionHandler {

	/**
	 * 拒绝执行
	 * 
	 * @param r
	 * @param executor
	 */
	void rejectedExecution(Runnable r, WolfThreadPoolExecutor executor);

}
