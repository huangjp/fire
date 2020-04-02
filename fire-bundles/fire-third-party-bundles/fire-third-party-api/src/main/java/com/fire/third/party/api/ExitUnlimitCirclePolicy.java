package com.fire.third.party.api;

import java.util.concurrent.Future;

/**
 * 循环策略
 * Created from huangjp on 2020/3/30 0030-下午 21:01
 *
 * @version 1.0
 * @email 262404150@qq.com
 * @param <T>
 */
public interface ExitUnlimitCirclePolicy<T> {
	boolean notOver(Future<T> future);
}
