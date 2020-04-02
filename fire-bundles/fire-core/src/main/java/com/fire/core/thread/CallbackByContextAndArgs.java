package com.fire.core.thread;

/**
 * 带上下文之外，还带变参的回调
 * 
 * @author Administrator
 *
 */
@FunctionalInterface
public interface CallbackByContextAndArgs {

	/**
	 * 带上下文之外，还带变参的回调
	 * 
	 * @param context
	 * @param obj
	 */
	void callback(WolfWebContext context, Object... obj);

}
