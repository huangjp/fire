package com.fire.core.tracker;

import com.fire.core.service.IService;
import org.osgi.framework.BundleContext;

import java.util.Collection;

/**
 * 批量追踪，即当BUNDLE所有的服务都注册到后才会走的回调，
 * 
 * @author Administrator
 *
 */
public abstract class AbstractBundleServiceTracker implements IService {

	/**
	 * 服务追踪完成的回调
	 * 
	 * @param context
	 */
	public abstract void externalServicesFinished(BundleContext context);

	/**
	 * 服务未完全追踪到的回调 此处应该判断未追踪到的服务是否关键服务，并根据业务自动情况处理是否中断程序或者提示用户
	 * 
	 * 返回true时，会继续追踪服务
	 * 
	 * @param context
	 * @param list
	 */
	public boolean serviceTrackingUnfinished(BundleContext context, Collection<Class<?>> list) {
		return list != null && !list.isEmpty();
	}

}
