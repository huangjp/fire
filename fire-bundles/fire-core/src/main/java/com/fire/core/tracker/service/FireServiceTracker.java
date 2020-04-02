package com.fire.core.tracker.service;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * 追踪
 * Created from huangjp on 2020/3/31 0031-下午 21:38
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public class FireServiceTracker<S, T> extends ServiceTracker<S, T> {

    protected Class<?> tackerClass;

    public FireServiceTracker(BundleContext context, Class<?> tackerClass,
                              ServiceTrackerCustomizer<S, T> customizer) {
        super(context, tackerClass.getName(), customizer);
        this.tackerClass = tackerClass;
    }

}
