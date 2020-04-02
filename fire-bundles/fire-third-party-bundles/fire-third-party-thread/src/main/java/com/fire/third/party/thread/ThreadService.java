/*
 * 文件名：SmsThreadService.java
 * 时     间：上午11:21:12
 * 作     者：Administrator
 * 版     权： 2012-2022 深圳硕软所有, 公司保留所有权利.
 * 联     系：http://www.sy666.com/
 */
package com.fire.third.party.thread;

import com.fire.third.party.api.ExitUnlimitCirclePolicy;
import com.fire.third.party.api.IThreadService;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.osgi.service.component.annotations.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 类描述:这里是类描述
 *
 * @author Administrator
 * @ClassName: SmsThreadService
 * @date 2018年10月16日 上午11:21:12
 */
@Component(
        name = "com.fire.third.party.api.IThreadService",
        immediate = true
)
public class ThreadService implements IThreadService {

    private final static RejectedExecutionHandler rejected = new RejectedExecutionHandler() {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

        }
    };

    private static int size = 32;

    private final static ListeningScheduledExecutorService busiWork = MoreExecutors
            .listeningDecorator(new ScheduledThreadPoolExecutor(size,
                    newThreadFactory("busiWork-"), rejected));

    @Override
    public ListeningScheduledExecutorService getBusiwork() {
        return busiWork;
    }

    @Override
    public <T> void submitUnlimitCircleTask(Callable<T> task,
                                            ExitUnlimitCirclePolicy<T> exitCondition, long delay) {
        addtask(busiWork, task, exitCondition, delay);
    }

    @Override
    public <T> ListenableScheduledFuture<T> execute(Callable<T> task) {
        if (busiWork.isShutdown())
            return null;
        return busiWork.schedule(task, 0, TimeUnit.MILLISECONDS);
    }

    private static <T> void addtask(
            final ListeningScheduledExecutorService executor,
            final Callable<T> task,
            final ExitUnlimitCirclePolicy<T> exitCondition, final long delay) {

        if (executor.isShutdown())
            return;
        final ListenableScheduledFuture<T> future = executor.schedule(task,
                delay, TimeUnit.MILLISECONDS);
        future.addListener(new Runnable() {

            @Override
            public void run() {

                FireLoopFuture<T> nettyfuture = new FireLoopFuture<>();
                try {
                    nettyfuture.completed(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    nettyfuture.failed(e);
                } catch (Exception e) {
                    nettyfuture.failed(e);
                }

                if (exitCondition.notOver(nettyfuture))
                    addtask(executor, task, exitCondition, delay);
            }

        }, executor);
    }

    private static ThreadFactory newThreadFactory(final String name) {

        return new ThreadFactory() {

            private final AtomicInteger threadNumber = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, name + threadNumber.getAndIncrement());
                t.setDaemon(false);
                if (t.getPriority() != Thread.NORM_PRIORITY)
                    t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        };

    }
}
