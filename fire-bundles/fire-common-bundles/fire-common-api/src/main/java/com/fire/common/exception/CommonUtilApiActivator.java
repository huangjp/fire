package com.fire.common.exception;

import com.fire.core.AbstractServiceTracker;
import com.fire.core.service.IService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class CommonUtilApiActivator extends AbstractServiceTracker {

	@Override
	public void start(BundleContext context) throws Exception {
//		this.autoIniect(context);

		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.put(Constants.SERVICE_PID, CommonException.class
				.getPackage().getName());
		ServiceRegistration<ManagedService> cfgService = context
				.registerService(ManagedService.class, CommonException.INIT,
						properties);
		this.serviceRegs.put(ManagedService.class, cfgService);

//		this.init();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		this.autoClear();
	}

	@Override
	protected List<Class<? extends IService>> regServiceClasses() {
		List<Class<? extends IService>> list = new ArrayList<Class<? extends IService>>();
		return list;
	}

	@Override
	protected List<Class<?>> serviceTrackerClasses() {
		List<Class<?>> list = new ArrayList<Class<?>>();
		return list;
	}

}
