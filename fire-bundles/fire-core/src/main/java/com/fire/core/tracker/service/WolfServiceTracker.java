package com.fire.core.tracker.service;//package com.wolf.core.tracker.service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.ServiceReference;
//import org.osgi.util.tracker.ServiceTracker;
//import org.osgi.util.tracker.ServiceTrackerCustomizer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.wolf.core.manager.model.ServiceTrackerManagerModel;
//import com.wolf.core.tracker.IWolfServiceTracker;
//import com.wolf.core.tracker.TrackComplete;
//
///**
// * 扩展OSGI服务追踪器，增加批量追踪统一回调的方式
// * 
// * @author Administrator
// *
// */
//public class WolfServiceTracker implements IWolfServiceTracker {
//
//	protected final Logger logger = LoggerFactory.getLogger(getClass());
//
//	/**
//	 * 追踪器容器
//	 */
//	private ServiceTrackerManagerModel serviceTrackerMap;
//
//	/**
//	 * 未追踪到的服务
//	 */
//	private List<Class<?>> serviceTrackers;
//
//	public WolfServiceTracker() {
//		super();
//	}
//
//	/**
//	 * 
//	 * @author Administrator
//	 *
//	 * @param <S>
//	 * @param <T>
//	 */
//	private class WfServiceTracker<S, T> extends ServiceTracker<S, T> {
//
//		protected Class<?> tackerClass;
//
//		public WfServiceTracker(BundleContext context, Class<?> clazz, ServiceTrackerCustomizer<S, T> customizer) {
//			super(context, clazz.getName(), customizer);
//			tackerClass = clazz;
//		}
//
//	}
//
//	@Override
//	public boolean serviceBatchTracker(TrackComplete trackComplete, BundleContext context,
//			List<Class<?>> serviceTrackerClasses) {
//
//		serviceTrackers = new ArrayList<>(serviceTrackerClasses);
//
//		serviceTrackerMap = new ServiceTrackerManagerModel(serviceTrackerClasses.size());
//
//		CountDownLatch countDownLatch = new CountDownLatch(serviceTrackerClasses.size());
//
//		serviceTrackerClasses.stream().forEach(c -> {
//
//			logger.info("BUNDLE:{},Tracking service began:{}", context.getBundle(), c.getSimpleName());
//			WfServiceTracker<?, ?> st = new WfServiceTracker<Object, Object>(context, c, null) {
//
//				@Override
//				public Object addingService(ServiceReference<Object> reference) {
//
//					logger.info("BUNDLE:{},Track the service:{}", context.getBundle(), this.tackerClass.getSimpleName());
//
//					serviceTrackers.remove(this.tackerClass);
//
//					Object service = super.addingService(reference);
//
//					trackComplete.back(reference, service, this.tackerClass, 1);
//
//					countDownLatch.countDown();
//					return service;
//				}
//
//				@Override
//				public void removedService(ServiceReference<Object> reference, Object service) {
//					super.removedService(reference, service);
//
//					trackComplete.back(reference, service, this.tackerClass, 0);
//
//					logger.info("BUNDLE:{},Remove the SERVICE:{}", context.getBundle(), service);
//				}
//
//			};
//			serviceTrackerMap.put(c, st);
//			st.open();
//		});
//
//		try {
//
//			int sed = Integer.max(serviceTrackerClasses.size(), 5);
//			boolean bool = countDownLatch.await(sed, TimeUnit.SECONDS);
//			// 5秒钟以上还追踪不到服务时，停止等待，打印出超时未追踪到的服务
//			if (!bool) {
//				logger.warn("BUNDLE:{},Not to track services : {}", context.getBundle(), countDownLatch.getCount());
//				serviceTrackers.forEach(c -> {
//					logger.warn("BUNDLE:{},No service to track:{}", context.getBundle(), c.getSimpleName());
//				});
//			}
//
//			return countDownLatch.getCount() == 0;
//		} catch (InterruptedException e) {
//			logger.error("{}", e);
//			return false;
//		}
//	}
//
//	@Override
//	public Map<Class<?>, ServiceTracker<?, ?>> getServiceTrackerMap() {
//		return serviceTrackerMap;
//	}
//
//	@Override
//	public List<Class<?>> getServiceTrackers() {
//		return serviceTrackers;
//	}
//
//	public void colse() {
//		serviceTrackerMap.clear();
//		serviceTrackers.clear();
//	}
//
//}
