package com.fire.core.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * 自定义线程池工厂类
 * 
 * @author Administrator
 *
 */
public class WolfExecutors extends WolfThreadPoolExecutor {

	/**
	 * 使用自定义线程工厂的线程池,这通常是常用的工作线程。其核心线程在空闲时不会销毁</br>
	 * 比如在并用处理request请求时可以用这个，在推送、短信、微信等常用服务可以使用
	 * 
	 * @param corePoolSize
	 * @return
	 */
	public static final WolfExecutors newWolfThreadPool(int corePoolSize) {
		WolfExecutors we = new WolfExecutors(corePoolSize, corePoolSize, 0L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
		// we.allowCoreThreadTimeOut(true);
		return we;
	}

	/**
	 * 使用自定义线程工厂的线程池，这通常是临时工作的线程。其核心线程在空闲时会自动清理。</br>
	 * 比如在工程启动时使用，启动完成后则不再使用，然后在空闲时会自动清理线程池的操作
	 * 
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @return
	 */
	public static final WolfExecutors newWolfCachedThreadPool(int corePoolSize, int maximumPoolSize) {
		WolfExecutors we = new WolfExecutors(corePoolSize, maximumPoolSize, 60L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>());
		we.allowCoreThreadTimeOut(true);
		return we;
	}

	private WolfExecutors(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	private WolfExecutors(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, WolfRejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	private WolfExecutors(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, WolfThreadFactory threadFactory, WolfRejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
	}

	private WolfExecutors(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, WolfThreadFactory threadFactory) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
	}

	@Override
	protected void terminated() {
		super.terminated();
	}

	public static WolfThreadFactory defaultThreadFactory() {
		return new WolfDefaultThreadFactory();
	}

	static class WolfDefaultThreadFactory implements WolfThreadFactory {
		private final LongAdder poolNumber = new LongAdder();
		private final ThreadGroup group;
		private final LongAdder threadNumber = new LongAdder();
		private final String namePrefix;

		WolfDefaultThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			poolNumber.increment();
			namePrefix = "WOLF-T-" + poolNumber.longValue();
		}

		@Override
		public Thread newThread(WolfRunnable r, WolfWebContext context) {
			threadNumber.increment();
			Thread t = new WolfThread(group, r, namePrefix + threadNumber.longValue(), context);
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}

		@Override
		public Thread newThread(Runnable r) {
			threadNumber.increment();
			Thread t = new Thread(group, r, namePrefix + threadNumber.longValue());
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}
	}

}
