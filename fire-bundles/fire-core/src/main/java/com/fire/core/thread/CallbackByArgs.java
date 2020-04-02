package com.fire.core.thread;

/**
 * 带参回调
 * @author Administrator
 *
 */
@FunctionalInterface
public interface CallbackByArgs {

	/**
	 * 带变量回调
	 * @param obj
	 */
	void callback(Object... obj);

}
