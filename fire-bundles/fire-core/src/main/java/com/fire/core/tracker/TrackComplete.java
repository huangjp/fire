package com.fire.core.tracker;

/**
 * 追踪完单个服务后的回调
 */
@FunctionalInterface
public interface TrackComplete {

	/**
	 * 回调
	 * 
	 * @param serviceReference
	 * @param service
	 * @param tackerClass
	 * @param type
	 */
	void back(Object service, int type);
}
