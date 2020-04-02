package com.fire.core;

/**
 * 
 * @author Administrator
 *
 */
public abstract class AbstractBundleActivator extends AbstractServiceTracker {

	/**
	 * 初始化构造
	 */
	public AbstractBundleActivator() {
		LOG.info(this.getClass().getSimpleName() + ".java starting...");
	}

}
