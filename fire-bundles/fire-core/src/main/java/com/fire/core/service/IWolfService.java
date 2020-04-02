package com.fire.core.service;

/**
 * 定义初始化方法
 * 
 * @author Administrator
 *
 */
public interface IWolfService extends IService {

	/**
	 * karaf 在构造实例bundle时，在构造方法中会执行此初始化动作，通常用于特殊参数的相关检查的动作
	 * 初始化方法中不能使用需要依赖注入的实例，因为bundle实例化时，外部服务都不一定已经注册进容器
	 */
	void wolfInit();

	/**
	 * 在取消服务注册时会触发此接口，对服务内部使用的容器进行清理动作
	 */
	void wolfClose();

}
