package com.fire.core.manager.model;

import org.osgi.framework.ServiceReference;

import java.util.concurrent.ConcurrentHashMap;

/**
 * OSGI 引用管理模型
 * 
 * @author Administrator
 *
 */
public class ServiceReferenceManagerModel extends ConcurrentHashMap<Class<?>, ServiceReference<?>> {

	private static final long serialVersionUID = 7446687842643389705L;

}
