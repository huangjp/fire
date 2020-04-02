package com.fire.core.manager.model;

import com.fire.core.service.IService;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务管理模型
 * 
 * @author Administrator
 *
 */
public class ServiceManagerModel extends ConcurrentHashMap<Class<?>, IService> {

	private static final long serialVersionUID = 4042319218179920378L;

}
