package com.fire.system.web;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * test
 * Created from huangjp on 2020/4/2 0002-下午 23:19
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("test web");
    }

    @Override
    public void stop(BundleContext context) throws Exception {

    }
}
