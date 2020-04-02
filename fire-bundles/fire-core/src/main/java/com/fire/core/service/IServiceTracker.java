package com.fire.core.service;

import org.osgi.framework.BundleContext;

/**
 * 该接口的实现将动态获取他所依赖的外部服务被删除或者添加时的动作能力
 * 
 * @author Administrator
 *
 */
public interface IServiceTracker extends IService {

	/**
	 * 外部服务注册进容器时
	 * 
	 * @param context
	 * @param service
	 */
	void addingService(BundleContext context, Object service);

	/**
	 * 外部服务被删除时
	 * 
	 * @param context
	 * @param service
	 */
	void removedService(BundleContext context, Object service);
}
