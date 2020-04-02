package com.fire.core.manager.model;

import org.osgi.framework.ServiceRegistration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务注册管理模型
 * 
 * @author Administrator
 *
 */
public class ServiceRegistrationManagerModel extends ConcurrentHashMap<Class<?>, ServiceRegistration<?>> {

	private static final long serialVersionUID = -704569155584094596L;

}
