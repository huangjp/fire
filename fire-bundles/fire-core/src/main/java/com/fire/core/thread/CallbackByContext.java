package com.fire.core.thread;

/**
 * 带上下文件回调
 * 
 * @author Administrator
 *
 */
@FunctionalInterface
public interface CallbackByContext {

	/**
	 * 上下文参数
	 * 
	 * @param context
	 */
	void callback(WolfWebContext context);

}
