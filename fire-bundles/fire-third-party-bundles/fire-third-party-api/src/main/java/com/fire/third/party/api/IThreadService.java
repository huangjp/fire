package com.fire.third.party.api;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;

import java.util.concurrent.Callable;

/**
 * Created from huangjp on 2020/3/30 0030-下午 21:54
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public interface IThreadService {
    ListeningScheduledExecutorService getBusiwork();

    <T> void submitUnlimitCircleTask(Callable<T> task,
                                     ExitUnlimitCirclePolicy<T> exitCondition, long delay);

    <T> ListenableScheduledFuture<T> execute(Callable<T> task);
}
