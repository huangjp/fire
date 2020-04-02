package com.fire.core.service;

import java.lang.reflect.Method;

/**
 * 面向切面编程
 * 
 * @author Administrator
 *
 */
public interface WolfProxyHandler {

	/**
	 * 代理方法执行前
	 * 
	 * @param method
	 */
	void before(Method method);

	/**
	 * 代理方法执行后
	 * 
	 * @param method
	 */
	void after(Method method);
}
