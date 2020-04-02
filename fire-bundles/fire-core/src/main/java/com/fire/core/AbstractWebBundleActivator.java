package com.fire.core;

/**
 * 
 * @author Administrator
 *
 */
public abstract class AbstractWebBundleActivator extends AbstractWebServiceTracker{

	public AbstractWebBundleActivator() {
		super();
		LOG.info(this.getClass().getSimpleName() + ".java starting...");
	}

}
