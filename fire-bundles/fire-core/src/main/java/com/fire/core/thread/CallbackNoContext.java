package com.fire.core.thread;

/**
 * 布尔参回调
 * 
 * @author Administrator
 *
 */
@FunctionalInterface
public interface CallbackNoContext {

	/**
	 * 布尔参回调
	 * 
	 * @param bool
	 */
	void callback(boolean bool);

}
