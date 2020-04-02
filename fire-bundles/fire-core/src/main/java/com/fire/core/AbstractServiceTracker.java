package com.fire.core;

import com.fire.core.manager.model.ServiceManagerModel;
import com.fire.core.manager.model.ServiceRegistrationManagerModel;
import com.fire.core.service.*;
import com.fire.core.thread.WolfExecutors;
import com.fire.core.thread.WolfThread;
import com.fire.core.thread.WolfWebContext;
import com.fire.core.tracker.AbstractBundleServiceTracker;
import com.fire.core.tracker.ServiceTrackerFactory;
import com.fire.core.transaction.IWolfTransaction;
import com.fire.core.transaction.WolfTransactionCallback;
import com.fire.core.transaction.WolfTransactionTemplate;
import org.apache.logging.log4j.util.Strings;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.lang.reflect.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static org.apache.ibatis.reflection.ExceptionUtil.unwrapThrowable;

/**
 * 提供依赖注入服务，本地服务实例化，和存放这些实例的容器，bundle结束时需要调用autoClear进行清理 提供service代理和事务控制
 * 
 * @author Administrator
 *
 */
@SuppressWarnings("unchecked")
public abstract class AbstractServiceTracker implements BundleActivator {

	/**
	 * 被注入的依赖实例
	 * 
	 * @author Administrator
	 *
	 * @param <T>
	 */
	@FunctionalInterface
	protected interface Cback<T extends IService> {
		Object getInstanceByField(Field field);
	}

	/**
	 * 需要注入其它服务的本类实例
	 * 
	 * @author Administrator
	 *
	 * @param <T>
	 */
	@FunctionalInterface
	protected interface FuncBack<T> {
		Object getInstanceByClass(Class<?> c);
	}

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractServiceTracker.class);

	/**
	 * 判断当前机器为正式环境
	 */
	private static Boolean IS_OFFICIAL;

	/**
	 * 判断当前机器为 win
	 */
	private static Boolean IS_WINDOWS;

	static {
		IS_WINDOWS = System.getProperties().getProperty("os.name").startsWith("Windows");
		try {
			InetAddress addr = InetAddress.getLocalHost();
			// 获得本机IP
			String ip = addr.getHostAddress().toString();
			IS_OFFICIAL = "172.18.98.97".equals(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	/**
	 * jenkins 打包时，用来截取属于jenkins配置前缀
	 */
	private static final String WIN = "win.";

	/**
	 * 正式环境中 用于截取配置文件属性前缀
	 */
	private static final String LINUX = "linux.";

	/**
	 * 开发环境本地配置前缀，通常在win系统开发会使用
	 */
	private static final String LOCAL = "local.";

	/**
	 * docker运行环境使用的配置
	 */
	private static final String DOCKER = "docker_env_";

	/**
	 * 线程池，再追踪外部服务，注入服务时使用 Integer.MAX_VALUE
	 * 有耗近系统资源的风险，但考虑此时仅在项目启动或者更新时才使用，且线程不可能无限大。因此可以使用
	 */
	private static final ExecutorService EXECUTOR_SERVICE = WolfExecutors.newWolfCachedThreadPool(0, Integer.MAX_VALUE);

	/**
	 * TODO 后期实现为通过扫描获取service注解的方式，自动获取服务类，则无须由子类实现了
	 * 
	 * @return
	 */
	protected abstract List<Class<? extends IService>> regServiceClasses();

	/**
	 * 本bundle需要追踪的外部服务列表 TODO 后期可优化为通过注解区分内部、外部，即可自动扫描，无须子类实现
	 * 
	 * @return
	 */
	protected abstract List<Class<?>> serviceTrackerClasses();

	/**
	 * 存放从注册本地服务的注册实例，当bundle stop时需要对该窗口所有注册实例进行取消注册的动作
	 */
	protected final ServiceRegistrationManagerModel serviceRegs = new ServiceRegistrationManagerModel();

	private Long bundleId;

	/**
	 * 只有在自动注入完成后才会有值 ，存放当前bundle上下文
	 */
	protected BundleContext context;

	/**
	 * 存放所有本地服务，即实现regServiceClasses()方法的服务，
	 * 本地服务需要继承com.wolf.core.base.IService接口实现其初始化方法
	 */
	private final ServiceManagerModel wolfServiceMap = new ServiceManagerModel();

	/**
	 * 本地服务的代理实例存放
	 */
	private final ServiceManagerModel wolfServiceProxyMap = new ServiceManagerModel();

	/**
	 * 存放配置文件中的数据，可在实现managedService的updated方法中通过调用setProperties()设置该对象
	 */
	private Dictionary<String, ?> properties;

	/**
	 * 当前需要追踪的服务class
	 */
	private List<Class<?>> serviceTrackerClasses;

	/**
	 * 当前需要代理的服务class
	 */
	private List<Class<? extends IService>> regServiceClasses;

	private TrackerRunnable trackerRunnable;

	public BundleContext getContext() {
		return context;
	}

	/**
	 * 为regServiceClasses()指定类型自动注入带有Resource注解的服务并且实例化该类放入wolfServiceMap容器中
	 * 注：regServiceClasses()指定类型，均需要实现com.wolf.core.base.IWolfService接口
	 * 
	 * @param context
	 * @throws Exception
	 */
	protected boolean autoIniect(BundleContext context) {

		this.context = context;
		this.bundleId = context.getBundle().getBundleId();
		// TODO 初始化各服务实例对象，反射机制注入相关实例，后期可以反射属性方法，目前仅支持属性注解Resource的反射
		List<Class<? extends IService>> clazzs = getRegServiceClasses();
		// 实例化
		autoInstanceWolfService(clazzs);
		// 先注入本地依赖
		reflectionIniect(clazzs, this::findInstanceByFieldType);
		return true;
	}

	/**
	 * 当需要使用外部服务时，在您bundle和start方法请使用如下方法
	 * 该方法会先追踪服务,然后再进行自动注入，如果您不需要使用外部服务，可以直接走autoIniect这个方法
	 * 
	 * @param context
	 * @throws Exception
	 */
	protected final void serviceBatchTracker(BundleContext context, AbstractBundleServiceTracker serviceTracker)
			throws Exception {

		this.autoIniect(context);

		List<Class<?>> serviceTrackerClasses = getServiceTrackerClasses();

		if (null == serviceTrackerClasses || serviceTrackerClasses.isEmpty()) {
			serviceTracker.externalServicesFinished(context);
			init();
			return;
		}

		trackerRunnable = new TrackerRunnable(context, serviceTracker);

		serviceTrackerClasses
				.forEach(c -> LOG.info("BUNDLE:{},Tracking service began:{}", context.getBundle(), c.getSimpleName()));

		EXECUTOR_SERVICE.execute(trackerRunnable);
	}

	/**
	 * 执行当前BUNDLE的所有服务中实现初始化接口的方法
	 */
	public final void init() {

		if (context != null) {
			LOG.info("BUNDLE:{}, To initialize...", context.getBundle());
			// 按顺序执行init方法
			getRegServiceClasses().forEach(c -> {
				IService service = this.wolfServiceMap.get(c);
				if (service instanceof IWolfService) {
					((IWolfService) service).wolfInit();
				}
			});
			LOG.info("BUNDLE:{}, Initializes the end. Start to finish.", context.getBundle());
		} else {
			LOG.warn("Initialization too early!!!");
		}

	}

	/**
	 * 仅对实现Iservice接口的本地服务进行注入，注入的依赖对象包括其它本要服务和来自callback回调取得的实例
	 * 
	 * @param clazzs
	 * @param callback
	 */
	protected void reflectionIniect(List<Class<? extends IService>> clazzs, Cback<?> callback) {
		iniect(new ArrayList<Class<?>>(clazzs), (t -> {
			return getWolfService(t);
		}), callback);
	}

	/**
	 * 可以注入任何类型,前提是callback 参数能返回需要注入的实例，当然实例若为Null仍然会被注入
	 * 这通常是用于注册本地服务和mybatis的mapper或者配置文件中的属性
	 * 
	 * @param clazzs
	 * @param fcb
	 * @param callback
	 */
	protected <T> void iniect(List<Class<?>> clazzs, FuncBack<T> fcb, Cback<?> callback) {
		iniect(clazzs, fcb, ((service, fields) -> {
			// 通过回调获取的服务，通常为本地服务或者本地配置文件属性或者mapper映射
			Arrays.asList(fields).stream().filter(field -> {
				Resource resource = field.getAnnotation(Resource.class);
				return null != resource;
			}).forEach(field -> {
				Object instance = callback.getInstanceByField(field);
				if (null != instance) {
					injection(field, service, instance);
				}
			});
		}));
	}

	/**
	 * 针对 外部追踪服务的实例更新, 仅能更新本地实现iservice接口的服务中的外部依赖
	 * 
	 * @param clazzs
	 * @param serObject
	 * @param i
	 *            0-删除 1-新增
	 */
	protected void iniect(List<Class<? extends IService>> clazzs, Object serObject, int i) {
		iniect(new ArrayList<Class<?>>(clazzs), (t -> {
			return getWolfService(t);
		}), ((service, fields) -> {
			if (service == null) {
				return;
			}
			// 追踪外部服务时
			Arrays.asList(fields).stream().filter(field -> {
				Resource resource = field.getAnnotation(Resource.class);
				if (null != resource) {
					List<Class<?>> ll = getAllInterfaces(serObject.getClass());
					return serObject.getClass().equals(field.getType()) || (ll != null && ll.contains(field.getType()));
				}
				return false;
			}).forEach(field -> {
				Object instance = null;

				// 为0，表示服务已经被删除，需要置null，防止旧服务影响,同时清理容器数据
				if (i == 0) {
					wolfServiceMap.remove(field.getType());
					wolfServiceProxyMap.remove(field.getType());
					List<Class<?>> ll = getAllInterfaces(serObject.getClass());
					ll.stream().forEach(t -> {
						wolfServiceMap.remove(t);
						wolfServiceProxyMap.remove(t);
					});
				} else if (i == 1) {
					// 为1时，服务被监听到注入
					instance = serObject;
					LOG.debug("BUNDLE:{},iniect instance:{}, field:{}", context.getBundle(), service, field.getName());
				}

				injection(field, service, instance);

				serviceTrackerCallback(service, serObject, i);
			});
		}));
	}

	/**
	 * 依赖查询方法，会从配置文件、本地服务中查找
	 * 
	 * @param field
	 * @return
	 */
	protected Object findInstanceByFieldType(Field field) {
		Class<?> fieldType = field.getType();
		Object instance = null;
		if (AbstractServiceTracker.class.equals(fieldType)) {
			instance = AbstractServiceTracker.this;
		} else if (String.class.equals(fieldType) && null != properties) {
			// String 类型的注入肯定都是要从配置文件中获取
			instance = properties.get(field.getName());
		} else {
			// 配置字典中都找不到实例就在本地实例中找
			instance = getWolfService(fieldType);
		}
		return instance;
	}

	/**
	 * 获取追踪实例，用于接受追踪到的服务注入操作
	 * 
	 * @return
	 */
	public TrackerRunnable getTrackerRunnable() {
		return trackerRunnable;
	}

	/**
	 * 获取本地服务，如果当然bundle依赖数据库，则会获取到代理实例
	 * 
	 * @param c
	 * @return
	 */
	public final <T> T getWolfService(Class<T> c) {
		T target = (T) wolfServiceMap.get(c);

		if (target == null) {
			return null;
		}

		if (this instanceof IWolfTransaction && !target.getClass().equals(c)) {
			IWolfTransaction iwt = (IWolfTransaction) this;
			T proxy = (T) wolfServiceProxyMap.get(c);
			if (proxy == null) {
				WolfInvocationHandler myHandler = new WolfInvocationHandler(target, iwt);

				Class<?>[] interfaces = target.getClass().getInterfaces();

				proxy = (T) Proxy.newProxyInstance(target.getClass().getClassLoader(), interfaces, myHandler);

				wolfServiceProxyMap.put(c, (IService) proxy);
			}
			return proxy;
		}

		return target;
	}

	/**
	 * 有些实例是手动生成的，则需要手动添加进,手动添加的方法会立即执行依赖注入，并加入到依赖注入容器， 也就是在有服务更新时依然可以获取持续注入的能力
	 * 
	 * @param c
	 * @param instance
	 */
	public final <T extends IService> void putWolfService(Class<T> c, T instance) {
		this.wolfServiceMap.put(c, instance);
		for (Class<?> face : c.getInterfaces()) {
			wolfServiceMap.put(face, instance);
		}

		// 立即执行依赖注入
		List<Class<? extends IService>> list = new ArrayList<>();
		list.add(c);
		reflectionIniect(list, this::findInstanceByFieldType);

		// 添加进注入容器，获取持续更新服务的能力,和自动初始化关闭等功能
		List<Class<? extends IService>> services = getRegServiceClasses();
		if (services != null && !services.contains(c)) {
			services.add(c);
		} else if (services == null) {
			this.regServiceClasses = list;
		}
	}

	/**
	 * 获取外部引用的服务
	 * 
	 * @param c
	 * @return
	 */
	public final <T> T getReferenceService(Class<T> c) {
		return ServiceTrackerFactory.getReferenceService(context, c);
	}

	/**
	 * 获取已经注册了的本地注册实例
	 * 
	 * @param c
	 * @return
	 */
	public final ServiceRegistration<?> getServiceRegistration(Class<?> c) {
		return this.serviceRegs.get(c);
	}

	/**
	 * 获取当前配置文件的属性
	 * 
	 * @param key
	 * @return
	 */
	public final String getProperties(Object key) {
		return null == properties ? null : (String) properties.get(key);
	}

	/**
	 * 当配置文件更新时需要重置各实例中的依赖
	 * 
	 * @param properties
	 */
	public final Hashtable<String, String> setProperties(Dictionary<String, ?> properties) {

		LOG.info("The current environment:{}", IS_WINDOWS ? "WIN" : (IS_OFFICIAL ? "OFFICIAL" : "LINUX"));
		Hashtable<String, String> dictionarys = new Hashtable<String, String>();
		for (Enumeration<String> keys = properties.keys(); keys.hasMoreElements();) {
			String key = keys.nextElement();
			Object o = properties.get(key);
			String value = o.toString();

			// 没有前缀的配置都是通用配置
			if (IS_WINDOWS) {
				// 取本地配置，用于开发人员，取本地配置时开发人员请检查是否有win配置，有的话会被win配置替换掉
				dictionarys.put(key.replace(LOCAL, ""), value.trim());
				// 如果有win配置则是测试人员，则以测试人员配置为准
				dictionarys.put(key.replace(WIN, ""), value.trim());
			} else {
				if (IS_OFFICIAL) {
					// 正式环境的配置
					dictionarys.put(key.replace(LINUX, ""), value.trim());
				} else {
					// docker.env 配置优先
					if (key.startsWith(DOCKER)) {
						key = key.replace(DOCKER, "");
						value = value.trim();
						String[] str = value.split("\\+");
						if (str.length == 1) {
							value = System.getenv(DOCKER + value + "_" + key);
						} else if (str.length == 2) {
							value = System.getenv(str[1]);
							String env = System.getenv(DOCKER + str[0] + "_" + key);
							if ("serverUrl".equals(key)) {
								value = value.trim();
							} else {
								value = value.trim() + env.trim();
							}
						}
						if (Strings.isNotEmpty(key) && Strings.isNotEmpty(value)) {
							dictionarys.put(key, value);
						}
					} else {
						key = key.replace(WIN, "");
						if (dictionarys.get(key) == null) {
							dictionarys.put(key, value.trim());
						}
					}
				}
			}
		}

		this.properties = dictionarys;

		reflectionIniect(getRegServiceClasses(), (field -> {
			Class<?> fieldType = field.getType();
			Object instance = null;
			if (String.class.equals(fieldType) && null != this.properties) {
				// String 类型的注入肯定都是要从配置文件中获取
				instance = this.properties.get(field.getName());
			}
			return instance;
		}));

		return dictionarys;
	}

	/**
	 * 获取全部追踪的服务类型
	 * 
	 * @return
	 */
	public List<Class<?>> getServiceTrackerClasses() {
		if (this.serviceTrackerClasses == null) {
			this.serviceTrackerClasses = serviceTrackerClasses();
		}
		return this.serviceTrackerClasses;
	}

	/**
	 * serviceRegs容器中所有服务取消注册的统一接口，财时清理所有容器
	 * 
	 * @throws IOException
	 */
	public void autoClear() {

		// 清理服务容器
		for (Class<?> c : this.serviceRegs.keySet()) {
			serviceRegs.get(c).unregister();
		}
		this.serviceRegs.clear();

		this.wolfColse();
		this.wolfServiceMap.clear();
		this.wolfServiceProxyMap.clear();

		// 清理配置文件字典容器
		this.properties = null;
		this.context = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleId == null) ? 0 : bundleId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractServiceTracker other = (AbstractServiceTracker) obj;
		if (bundleId == null) {
			if (other.bundleId != null) {
				return false;
			}
		} else if (!bundleId.equals(other.bundleId)) {
			return false;
		}
		return true;
	}

	protected List<Class<?>> getAllInterfaces(Class<?> cls) {
		if (cls == null) {
			return null;
		}

		List<Class<?>> interfacesFound = new ArrayList<Class<?>>();
		getAllInterfaces(cls, interfacesFound);

		return interfacesFound;
	}

	protected void wolfColse() {
		// 按启动初始化顺序倒序清理服务
		List<Class<? extends IService>> list = getRegServiceClasses();
		for (int i = list.size() - 1; i >= 0; i--) {
			IService service = wolfServiceMap.get(list.get(i));
			if (service instanceof IWolfService) {
				((IWolfService) service).wolfClose();
			}
		}
	}

	protected List<Class<? extends IService>> getRegServiceClasses() {
		if (this.regServiceClasses == null) {
			this.regServiceClasses = regServiceClasses();
		}
		return this.regServiceClasses;
	}

	/**
	 * 实例化注入进容器的对象
	 * 
	 * @param clazzs
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private <T extends IService> void autoInstanceWolfService(List<Class<? extends IService>> clazzs) {
		for (Class<? extends IService> c : clazzs) {
			// 先到本地获取服务
			Object service = getWolfService(c);
			// 取不到就新建
			if (null == service) {
				try {
					service = c.newInstance();

					// 将本地服务添加到指定容器
					for (Class<?> face : c.getInterfaces()) {
						wolfServiceMap.put(face, (IService) service);
					}
					wolfServiceMap.put(c, (IService) service);
				} catch (InstantiationException | IllegalAccessException e) {
					LOG.error(
							c.getSimpleName()
									+ "Instantiate the exception, may be no void structure or is an abstract class：{}",
							e);
				}
			}
		}
	}

	private void getAllInterfaces(Class<?> cls, List<Class<?>> interfacesFound) {
		while (cls != null) {
			Class<?>[] interfaces = cls.getInterfaces();

			for (int i = 0; i < interfaces.length; i++) {
				if (!interfacesFound.contains(interfaces[i])) {
					interfacesFound.add(interfaces[i]);
					getAllInterfaces(interfaces[i], interfacesFound);
				}
			}

			cls = cls.getSuperclass();
		}
	}

	private <T> void iniect(Class<?> c, FuncBack<T> fcb, Callback callback) {
		Object service = fcb.getInstanceByClass(c);
		if (service instanceof Proxy) {
			service = ((WolfInvocationHandler) Proxy.getInvocationHandler(service)).getTarget();
		}
		callback.exe(service, c.getDeclaredFields());
	}

	private <T> void iniect(List<Class<?>> clazzs, FuncBack<T> fcb, Callback callback) {
		clazzs.stream().forEach(c -> {
			iniect(c, fcb, callback);
		});
	}

	private void injection(Field field, Object instance, Object fieldService) {
		if (instance == null || field == null) {
			return;
		}
		try {
			field.setAccessible(true);
			field.set(instance, fieldService);
			LOG.debug("BUNDLE:{},iniect instance:{}, field:{}", context.getBundle(), instance, field.getName());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			LOG.error("Dependency injection exception： {}", e);
		}
	}

	/**
	 * 是否有继承追踪服务的接口，有则进行回调 最后的Int参数：0-删除服务时 1-表示注入服务时
	 * 
	 * @param service
	 * @param serObject
	 * @param i
	 */
	private void serviceTrackerCallback(Object service, Object serObject, int i) {
		List<Class<?>> sClasses = getAllInterfaces(service.getClass());
		if (sClasses != null && !sClasses.isEmpty() && sClasses.contains(IServiceTracker.class)) {
			final IServiceTracker st = (IServiceTracker) service;
			EXECUTOR_SERVICE.execute(new Runnable() {

				@Override
				public void run() {
					if (i == 0) {
						st.removedService(context, serObject);
					} else if (i == 1) {
						st.addingService(context, serObject);
					}
				}
			});
		}
	}

	/**
	 * 私有回调
	 * 
	 * @author Administrator
	 *
	 */
	private interface Callback {
		/**
		 * 执行
		 * 
		 * @param service
		 * @param field
		 */
		void exe(Object service, Field[] field);
	}

	protected static class WolfClassLoader extends URLClassLoader {

		private static WolfClassLoader CLASS_LOADER;

		public WolfClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
			super(urls, parent, factory);
		}

		public WolfClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
		}

		public WolfClassLoader(URL[] urls) {
			super(urls);
		}

		public static WolfClassLoader getClassLoader(String url) {
			if (CLASS_LOADER == null) {
				URL[] urls = new URL[1];
				try {
					urls[0] = new URL("file:/" + url + "/");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				CLASS_LOADER = new WolfClassLoader(urls, Thread.currentThread().getContextClassLoader());
			}
			return CLASS_LOADER;
		}

	}

	private class TrackerRunnable implements Runnable {

		private Long bundleId;

		private BundleContext context;

		private AbstractBundleServiceTracker serviceTracker;

		private TrackerRunnable(BundleContext context, AbstractBundleServiceTracker serviceTracker) {
			super();
			this.context = context;
			this.bundleId = context.getBundle().getBundleId();
			this.serviceTracker = serviceTracker;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((bundleId == null) ? 0 : bundleId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (this == obj) {
				return true;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			TrackerRunnable other = (TrackerRunnable) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (bundleId == null) {
				if (other.bundleId != null) {
					return false;
				}
			} else if (!bundleId.equals(other.bundleId)) {
				return false;
			}
			return true;
		}

		@Override
		public void run() {
			LOG.debug("BUNDLE:{}, start tacking ...", context.getBundle());
			// 第一次追踪服务
			boolean flag = exec(getServiceTrackerClasses().toArray(new Class[0]));
			int i = 1;
			// 先去询问业务模块是否继续追踪
			while (!flag && serviceTracker.serviceTrackingUnfinished(context,
					ServiceTrackerFactory.getServiceTrackers(context))) {
				LOG.info("BUNDLE:{}, Tracking service, How many times ：{}", context.getBundle(), i++);

				// 业务系统确定继续追踪，则等待1000毫秒再询问一次
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 追踪
				flag = exec();

			}

			// 完成必须服务的追踪后，则允许业务正常启动
			serviceTracker.externalServicesFinished(context);

			// 如果bundle依赖有配置文件，那么追踪配置文件更新情况
			AbstractServiceTracker activator = AbstractServiceTracker.this;
			if (activator instanceof ManagedService) {
				while (activator.properties == null) {
					LOG.warn("BUNDLE:{}, config file not found!!", context.getBundle());
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			// 检查依赖注入，存在则等待并主动获取依赖进行注入
			for (Class<?> c : wolfServiceMap.keySet()) {
				IService service = wolfServiceMap.get(c);
				Arrays.stream(c.getDeclaredFields()).filter(field -> {
					Resource resource = field.getAnnotation(Resource.class);
					return resource != null;
				}).forEach(field -> {
					try {
						field.setAccessible(true);
						while (field.get(service) == null) {
							// 先从外部服务拿
							Object o = ServiceTrackerFactory.getReferenceService(context, field.getType());
							if (o == null) {
								// 没有就从配置文件中拿
								o = activator.properties == null ? null : activator.properties.get(field.getName());
								if (o == null) {
									LOG.warn("iniect error , field : 【{}】， service instance : {}", field.getName(),
											service);
									Thread.sleep(500);
									continue;
								}
							}
							injection(field, service, o);
						}
					} catch (IllegalArgumentException | IllegalAccessException | InterruptedException e) {
						e.printStackTrace();
					}
				});
			}

			// 执行升级后再初始化。或者没有升级需求时，直接初始化
			if (getServiceTrackerClasses().contains(IWolfSqlUpgradeService.class)) {
				IWolfSqlUpgradeService service = ServiceTrackerFactory.getReferenceService(context,
						IWolfSqlUpgradeService.class);
				if (service != null) {
					service.upgrade(activator::init, null, null);
				} else {
					init();
				}
			} else {
				init();
			}

			LOG.info("BUNDLE:{}, Implement the callback.", context.getBundle());

		}

		private boolean exec(Class<?>... classes) {
			boolean isTrackComplate = ServiceTrackerFactory.startTracker(this::receiveService, context, classes);
			return isTrackComplate;
		}

		private void receiveService(Object service, int type) {
			iniect(getRegServiceClasses(), service, type);
		}

		private AbstractServiceTracker getOuterType() {
			return AbstractServiceTracker.this;
		}
	}

	/**
	 * 方法代理，访问代理实例时，执行代理实例的任何方法都被此代理截获
	 * 
	 * @author Administrator
	 *
	 */
	private class WolfInvocationHandler implements InvocationHandler {

		/** 目标实例 */
		private Object target;

		/** 事务管理器 */
		private IWolfTransaction wolfTransaction;

		public WolfInvocationHandler(Object target, IWolfTransaction wolfTransaction) {
			super();
			this.target = target;
			this.wolfTransaction = wolfTransaction;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

			Transactional transactional = method.getAnnotation(Transactional.class);
			WolfWebContext context = WolfThread.currentWolfcontext();
			/*
			 * 1、注解使用Transactional的 2、方法名称以insert|update开头的 3、当前线程已经是在事务中的 以上三种情况都会进入事务控制范围
			 */
			if (branchConfirm(method, transactional, context)) {
				// 获取事务管理
				WolfTransactionTemplate transactionTemplate = wolfTransaction.getWolfTransactionTemplate();
				if (transactionTemplate != null) {
					return transactionTemplate.execute(new WolfTransactionCallback<Object>() {

						@Override
						public Object doInTransaction(TransactionStatus status) {
							// 通常使用自定义事务，理论上不会进入该方法
							try {
								return invoke(method, args);
							} catch (Throwable t) {
								Throwable unwrapped = unwrapThrowable(t);
								return unwrapped;
							}
						}

						@Override
						public Object wolfDoInTransaction(TransactionStatus status) throws IllegalAccessException,
								IllegalArgumentException, InvocationTargetException, SQLException {
							WolfWebContext context = WolfThread.currentWolfcontext();
							if (context != null && !context.isSqlSessionTransactional()) {
								try {
									context.setSqlSessionTransactional(true);
									return invoke(method, args);
								} finally {
									context.setSqlSessionTransactional(false);
								}
							} else {
								return invoke(method, args);
							}
						}
					});
				}
			}
			try {

				return invoke(method, args);
			} catch (Throwable t) {
				Throwable unwrapped = unwrapThrowable(t);
				throw unwrapped;
			}
		}

		/**
		 * 获取目标实例
		 * 
		 * @return
		 */
		public Object getTarget() {
			return target;
		}

		private boolean branchConfirm(Method method, Transactional transactional, WolfWebContext context) {
			return method.getName().startsWith("insert") || method.getName().startsWith("update")
					|| transactional != null || (context != null && context.isSqlSessionTransactional());
		}

		private Object invoke(Method method, Object[] args)
				throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			WolfProxyHandler wph = null;
			if (getAllInterfaces(target.getClass()).contains(IService.class)) {
				wph = ((IService) target).getWolfProxyHandler();

				if (wph != null) {
					// 方法执行前
					wph.before(method);
				}
			}
			Object o = method.invoke(target, args);

			if (wph != null) {
				// 执行后回调
				wph.after(method);
			}
			return o;
		}

	}

}
