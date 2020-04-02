package com.fire.core.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义线程，扩展自动定的线程上下文支持
 * 
 * @author Administrator
 *
 */
public class WolfThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(WolfThread.class);

	private WolfWebContext context;

	private WolfThread(ThreadGroup group, WolfRunnable target, String name, long stackSize) {
		super(group, target, name, stackSize);
	}

	public WolfThread(WolfRunnable target, String name) {
		super(target, name);
		this.context = currentWolfcontext();
	}

	public WolfThread(ThreadGroup group, WolfRunnable target, String name, WolfWebContext context) {
		this(group, target, name, 0);
		this.context = context;
	}

	public static final WolfWebContext currentWolfcontext() {
		Thread currentThread = Thread.currentThread();
		if (currentThread instanceof WolfThread) {
			WolfThread thread = (WolfThread) currentThread;
			if (thread.context == null) {
				logger.warn("The current thread is not set context!!!");
			}
			return thread.context;
		}
		logger.warn("Please use WolfExecutors provide thread pool!!!");
		return null;
	}

	void setContext(WolfWebContext context) {
		this.context = context;
	}

}
