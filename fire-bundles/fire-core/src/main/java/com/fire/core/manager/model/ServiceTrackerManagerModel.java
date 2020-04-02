package com.fire.core.manager.model;

import org.osgi.util.tracker.ServiceTracker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OSGI服务追踪管理模型
 * 
 * @author Administrator
 *
 */
public class ServiceTrackerManagerModel extends ConcurrentHashMap<Class<?>, ServiceTracker<?, ?>> {

	private static final long serialVersionUID = -9133010166646663913L;

	public ServiceTrackerManagerModel() {
		super();
	}

	public ServiceTrackerManagerModel(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public ServiceTrackerManagerModel(int initialCapacity) {
		super(initialCapacity);
	}

	public ServiceTrackerManagerModel(Map<? extends Class<?>, ? extends ServiceTracker<?, ?>> m) {
		super(m);
	}

}
