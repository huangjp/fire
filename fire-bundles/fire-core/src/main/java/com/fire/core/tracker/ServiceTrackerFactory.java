package com.fire.core.tracker;

import com.fire.core.manager.model.ServiceReferenceManagerModel;
import com.fire.core.manager.model.ServiceTrackerManagerModel;
import com.fire.core.service.IService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ServiceTrackerFactory {

	// 单例
	private static ServiceTrackerFactory serviceTrackerFactory;

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 每个bundle未完成追踪服务的列表
	 */
	private Map<Long, Set<Class<?>>> trackers;

	/**
	 * 存放从外部bundle引用服务的引用实例，用于获取已经存在的外部服务
	 */
	private ServiceReferenceManagerModel serviceRefMap;

	/**
	 * 全部追踪器容器 有时候外部服务并不一定在本bundle启动时就能拿到， 需要放一个追踪器追踪他们，此容器存放的追踪器需要在bundle stop时销毁掉
	 */
	private ServiceTrackerManagerModel serviceTrackerMap;

	/**
	 * 全部服务追踪回调
	 */
	private Map<Class<?>, Map<Long, TrackComplete>> cTrackCompletes;

	private ServiceTrackerFactory() {
	}

	/**
	 * 关闭，清理。本地OSGI工程在update时无须清理本容器，本工厂方法是全局的
	 */
	public static void close(long id, TrackComplete back) {
		if (serviceTrackerFactory != null) {
			// 如果服务追踪无异常，正常情况，这里本身就是空的，也不需要清理
			// serviceTrackerFactory.trackers.remove(id);
			// 启动时该回调会重新注入，因也不需要清理
			// serviceTrackerFactory.cTrackCompletes.values().forEach(m -> m.remove(back));
			// 追踪外部服务无须清理,也不需要关闭
			// for (Class<?> c : serviceTrackerFactory.serviceTrackerMap.keySet()) {
			// ServiceTracker<?, ?> serTracker =
			// serviceTrackerFactory.serviceTrackerMap.get(c);
			// serTracker.close();
			// }
			// serviceTrackerFactory.serviceTrackerMap.clear();
			// 外部引用无须清理
			// serviceTrackerFactory.serviceRefMap.clear();
		}
	}

	/**
	 * 获取调用时还未追踪到的服务明细
	 * 
	 * @param context
	 * @return
	 */
	public static Set<Class<?>> getServiceTrackers(BundleContext context) {
		return geServiceTrackerFactory().trackers.get(context.getBundle().getBundleId());
	}

	/**
	 * 获取外部引用的服务
	 * 
	 * @param context
	 * @param c
	 * @return
	 */
	public static <T> T getReferenceService(BundleContext context, Class<T> c) {
		return geServiceTrackerFactory().referenceService(context, c);
	}

	/**
	 * 开启不阻塞追踪，如果返回true表示已经全部追踪完成
	 * 
	 * @param callback
	 * @param context
	 * @return
	 */
	public static boolean startTracker(TrackComplete callback, BundleContext context, Class<?>... services) {
		ServiceTrackerFactory serviceTrackerFactory = geServiceTrackerFactory();
		// 先获取本地值
		Set<Class<?>> sets = null;
		// 本地不存在数据，则获取外面传入值做为初始化数据；本地存在，但不为empty时，则拷贝一份数据
		if (services != null && services.length > 0) {
			sets = new CopyOnWriteArraySet<>(Arrays.asList(services));
			serviceTrackerFactory.trackers.put(context.getBundle().getBundleId(),sets);
		} else {
			sets = getServiceTrackers(context);
		}
		// 存在值，才需要追踪；不存在值，则表示没有要追踪的服务，直接返回true表示追踪完成
		if (sets != null && sets.size() > 0) {
			return serviceTrackerFactory.serviceBatchTracker(callback, context, sets);
		} else {
			return true;
		}
	}

	// 实例化兼初始化
	private static ServiceTrackerFactory geServiceTrackerFactory() {
		if (serviceTrackerFactory == null) {
			serviceTrackerFactory = new ServiceTrackerFactory();
			serviceTrackerFactory.trackers = new ConcurrentHashMap<>();
			serviceTrackerFactory.serviceTrackerMap = new ServiceTrackerManagerModel();
			serviceTrackerFactory.cTrackCompletes = new ConcurrentHashMap<>();
			serviceTrackerFactory.serviceRefMap = new ServiceReferenceManagerModel();
		}
		return serviceTrackerFactory;
	}

	@SuppressWarnings("unchecked")
	private final <T> T referenceService(BundleContext context, Class<T> c) {
		ServiceReference<?> ref = this.serviceRefMap.get(c);
		if (null == ref) {
			// 将从其它bundle获取到的服务引用添加到指定容器
			ref = context.getServiceReference(c);
			if (null != ref) {
				Class<?>[] interfaces = c.getInterfaces();
				for (Class<?> face : interfaces) {
					if (!IService.class.equals(face)) {
						serviceRefMap.put(face, (ServiceReference<?>) ref);
					}
				}
				serviceRefMap.put(c, (ServiceReference<?>) ref);
			} else {
				return null;
			}
		}
		return (T) context.getService(ref);
	}

	private void notifyBundleCallback(ServiceReference<Object> reference, Object service, Class<?> tackerClass,
			int type) {
		Map<Long, TrackComplete> list = cTrackCompletes.get(tackerClass);
		list.values().forEach(t -> t.back(service, type));
	}

	private boolean serviceBatchTracker(TrackComplete callback, BundleContext context,
			Collection<Class<?>> serviceTrackerClasses) {

		serviceTrackerClasses.forEach(c -> {
			Map<Long, TrackComplete> list = cTrackCompletes.get(c);
			if (list == null) {
				list = new ConcurrentHashMap<>();
				cTrackCompletes.put(c, list);
			}
			list.put(context.getBundle().getBundleId(), callback);
			ServiceTracker<?, ?> tracker = serviceTrackerMap.get(c);
			if (tracker == null) {
				WfServiceTracker<Object, Object> st = new WfServiceTracker<Object, Object>(context, c, null) {

					@Override
					public Object addingService(ServiceReference<Object> reference) {

						logger.info("BUNDLE:{},Track the service:{}", context.getBundle(),
								this.tackerClass.getSimpleName());
						
						Object service = super.addingService(reference);
						serviceRefMap.put(tackerClass, reference);
						// 依赖注入
						notifyBundleCallback(reference, service, this.tackerClass, 1);
						// 删除追踪类型，避免重复追踪
						trackers.values().stream().forEach(v -> v.remove(this.tackerClass));
						return service;
					}

					@Override
					public void removedService(ServiceReference<Object> reference, Object service) {
						super.removedService(reference, service);
						// 依赖反注入，避免无效实例影响业务逻辑
						notifyBundleCallback(reference, service, this.tackerClass, 0);
						serviceRefMap.remove(tackerClass);
						logger.info("BUNDLE:{},Remove the SERVICE:{}", context.getBundle(), service);
					}

				};
				st.open();
				serviceTrackerMap.put(c, st);
			} else {
				// 追踪器已经开启，则尝试从追踪器获取服务
				Object service = tracker.getService();
				if (service == null) {
					// 尝试从引用处获取服务
					service = referenceService(context, c);
				}
				if (service == null) {
					logger.info("BUNDLE:{},Tracking... :{}", context.getBundle(), c.getSimpleName());
				} else {
					// 说明追踪到相关服务，则对当前工程执行依赖注入
					callback.back(service, 1);
					// 同时删除追踪类型，避免重复追踪
					trackers.get(context.getBundle().getBundleId()).remove(c);

					logger.info("BUNDLE:{},Track the service:{}", context.getBundle(), c.getSimpleName());
				}
			}
		});

		Set<Class<?>> classes = getServiceTrackers(context);

		return classes == null || classes.isEmpty();
	}

	/**
	 * 
	 * @author Administrator
	 *
	 * @param <S>
	 * @param <T>
	 */
	private class WfServiceTracker<S, T> extends ServiceTracker<S, T> {

		protected Class<?> tackerClass;

		public WfServiceTracker(BundleContext context, Class<?> tackerClass,
				ServiceTrackerCustomizer<S, T> customizer) {
			super(context, tackerClass.getName(), customizer);
			this.tackerClass = tackerClass;
		}

	}
}
