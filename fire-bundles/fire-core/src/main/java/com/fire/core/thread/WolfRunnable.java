package com.fire.core.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * https://my.oschina.net/hosee/blog/615927
 * 
 * @author Administrator
 *
 */
public class WolfRunnable extends AbstractQueuedSynchronizer implements
		Runnable {

	private static final long serialVersionUID = 5644235388830776604L;

	private CompletableFuture<Integer> re;

	private CallbackByContext callbackByContext;

	private Callback callback;

	private CallbackNoContext callbackNoContext;

	private CallbackByArgs callbackByArgs;

	private CallbackByContextAndArgs callbackByContextAndArgs;

	private Object[] args;

	protected WolfWebContext context;

	public WolfRunnable() {
		super();
	}

	/**
	 * 使用指定上下文，传入异步回调
	 * 
	 * @param re
	 * @param context
	 */
	public WolfRunnable(CompletableFuture<Integer> re, WolfWebContext context) {
		this(context);
		this.re = re;
	}

	/**
	 * 使用指定上下文，传入带上下文参数的回调
	 * 
	 * @param callback
	 * @param context
	 */
	public WolfRunnable(CallbackByContext callback, WolfWebContext context) {
		this(context);
		this.callbackByContext = callback;
	}

	/**
	 * 继承当前线程的上下文，传入一个无参回调
	 * 
	 * @param callback
	 */
	public WolfRunnable(Callback callback) {
		// 使用当前线程上下文
		this(WolfThread.currentWolfcontext());
		this.callback = callback;
	}

	/**
	 * 不使用上下文，传入一个有参回调
	 * 
	 * @param callback
	 */
	public WolfRunnable(CallbackNoContext callback) {
		this.callbackNoContext = callback;
	}

	/**
	 * 使用指定上下文，会入一个无参回调
	 * 
	 * @param callback
	 * @param context
	 */
	public WolfRunnable(Callback callback, WolfWebContext context) {
		this(context);
		this.callback = callback;
	}

	/**
	 * 继承当前线程上下文，使用带变参的回调
	 * 
	 * @param callbackByArgs
	 * @param objects
	 */
	public WolfRunnable(CallbackByArgs callbackByArgs, Object... objects) {
		// 使用当前线程上下文
		this(WolfThread.currentWolfcontext());
		this.callbackByArgs = callbackByArgs;
		this.args = objects;
	}

	/**
	 * 使用指定上下文，传入带变参和上下文参数的回调
	 * 
	 * @param callbackByContextAndArgs
	 * @param context
	 * @param objects
	 */
	public WolfRunnable(CallbackByContextAndArgs callbackByContextAndArgs,
			WolfWebContext context, Object... objects) {
		this(context);
		this.callbackByContextAndArgs = callbackByContextAndArgs;
		this.args = objects;
	}

	protected WolfRunnable(WolfWebContext context) {
		super();
		if (context == null) {
			context = new WolfWebContext();
		}
		this.context = context;
	}

	public WolfWebContext getContext() {
		return context;
	}

	@Override
	public void run() {
		if (callbackByContext != null) {
			callbackByContext.callback(this.context);
		} else if (callback != null) {
			callback.callback();
		} else if (callbackNoContext != null) {
			callbackNoContext.callback(false);
		} else if (callbackByArgs != null) {
			callbackByArgs.callback(args);
		} else if (callbackByContextAndArgs != null) {
			callbackByContextAndArgs.callback(context, args);
		} else if (re != null) {
			re.thenRun(this);
		}
	}

}
