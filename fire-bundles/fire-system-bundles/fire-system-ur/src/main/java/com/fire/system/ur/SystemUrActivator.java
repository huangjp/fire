package com.fire.system.ur;

import com.fire.core.tracker.service.FireServiceTracker;
import com.fire.system.api.IResourceScanService;
import com.fire.system.api.IResourceService;
import com.fire.system.ur.service.impl.ResourceScanService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * 基于用户、资源实现的权限系统的入口
 * Created from huangjp on 2020/3/31 0031-下午 20:03
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public class SystemUrActivator implements BundleActivator {

    private FireServiceTracker<IResourceService, IResourceService> tracker;
    private ServiceRegistration<IResourceScanService> imServiceReg;

    @Override
    public void start(BundleContext context) throws Exception {

        // 注册资源扫描服务
        IResourceScanService service = new ResourceScanService();
        imServiceReg = context.registerService(IResourceScanService.class,
                service, null);

        tracker = new FireServiceTracker<>(context, IResourceService.class,
                new ServiceTrackerCustomizer<IResourceService, IResourceService>() {
                    @Override
                    public IResourceService addingService(ServiceReference reference) {
                        // 每发现一次资源注册，就执行一次扫描
                        IResourceService resourceService = (IResourceService) context.getService(reference);
                        return service.scan(resourceService);
                    }

                    @Override
                    public void modifiedService(ServiceReference reference, IResourceService instance) {
                        // 每发现一次资源注册，就执行一次扫描
                        IResourceService resourceService = (IResourceService) instance;
                        service.modified(resourceService);
                    }

                    @Override
                    public void removedService(ServiceReference reference, IResourceService instance) {
                        // 每次删除服务，对应的资源也要删除
                        IResourceService resourceService = (IResourceService) instance;
                        service.remove(resourceService);
                    }
                });

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (tracker != null) {
            tracker.close();
        }
        if (imServiceReg != null) {
            imServiceReg.unregister();
        }
    }

}
