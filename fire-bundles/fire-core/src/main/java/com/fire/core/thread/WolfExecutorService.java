package com.fire.core.thread;

/**
 * 线程池服务接口
 * 
 * @author Administrator
 *
 */
public interface WolfExecutorService {

	/**
	 * 执行
	 * 
	 * @param command
	 * @param context
	 */
	public void execute(Runnable command, WolfWebContext context);

}
