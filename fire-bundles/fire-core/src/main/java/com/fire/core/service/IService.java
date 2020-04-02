package com.fire.core.service;

/**
 * 根类，主要功能是避免Object的使用，为后期扩展其它根服务留下空间
 * 
 * @author Administrator
 *
 */
public interface IService {

	/**
	 * 代理拦截器, 重写该方法，获取代理方法WolfProxyHandler接口的能力
	 * 
	 * @return
	 */
	default WolfProxyHandler getWolfProxyHandler() {
		return null;
	}

}
