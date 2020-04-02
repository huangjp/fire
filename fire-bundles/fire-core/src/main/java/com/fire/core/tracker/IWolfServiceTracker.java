package com.fire.core.tracker;//package com.wolf.core.tracker;
//
//import java.util.List;
//import java.util.Map;
//
//import org.osgi.framework.BundleContext;
//import org.osgi.util.tracker.ServiceTracker;
//
///**
// * 服务追踪接口
// * 
// * @author Administrator
// *
// */
//public interface IWolfServiceTracker {
//
//
//	/**
//	 * 同步批量追踪服务，返回TRUE表示全部追踪到，false表示没有追踪完成
//	 * 
//	 * @param trackComplete
//	 * @param context
//	 * @param serviceTrackerClasses
//	 * @return
//	 */
//	boolean serviceBatchTracker(TrackComplete trackComplete, BundleContext context,
//			List<Class<?>> serviceTrackerClasses);
//
//	/**
//	 * 返回全部追踪器
//	 * 
//	 * @return
//	 */
//	Map<Class<?>, ServiceTracker<?, ?>> getServiceTrackerMap();
//
//	/**
//	 * 返回未追踪到的服务列表
//	 * 
//	 * @return
//	 */
//	List<Class<?>> getServiceTrackers();
//
//}
