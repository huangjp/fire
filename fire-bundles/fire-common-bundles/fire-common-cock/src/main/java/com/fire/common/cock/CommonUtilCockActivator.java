package com.fire.common.cock;

import com.fire.common.api.IUtilService;
import com.fire.common.cock.service.UtilService;
import com.fire.common.cock.util.CachedIntrospectionResults;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class CommonUtilCockActivator implements BundleActivator {

	private ServiceRegistration<IUtilService> JSONUtilService;

	public void start(BundleContext context) throws Exception {
		JSONUtilService = context.registerService(IUtilService.class, new UtilService(), null);
	}

	public void stop(BundleContext context) throws Exception {
		if (JSONUtilService != null)
			JSONUtilService.unregister();

		// TODO hjp 2016-12-14加入清理缓存，因为bundle化后容器不被清理,待测试
		CachedIntrospectionResults.clearClassLoader(this.getClass().getClassLoader());
	}

}
