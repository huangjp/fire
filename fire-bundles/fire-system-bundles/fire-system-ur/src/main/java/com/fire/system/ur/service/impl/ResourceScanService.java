package com.fire.system.ur.service.impl;

import com.fire.common.api.IUtilService;
import com.fire.core.service.IService;
import com.fire.system.api.IResourceScanService;
import com.fire.system.api.IResourceService;
import com.fire.system.api.annotation.PowerInjection;
import com.fire.system.api.model.ResourceGroupModel;
import com.fire.system.api.model.ResourceModel;
import com.fire.third.party.api.IPersistentService;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.osgi.service.component.annotations.Reference;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 资源服务，通过查找权限注解使用情况，解析权限资源的服务
 * Created from huangjp on 2020/3/31 0031-下午 20:44
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public class ResourceScanService implements IResourceScanService, IService {

    @Reference
    private IUtilService utilService;

    @Reference
    private IPersistentService persistentService;

    private String name = ResourceScanService.class.getName();
    private String path = ResourceScanService.class.getSimpleName();

    @Override
    public IResourceService scan(IResourceService service) {

        // 类上的注解作为群组存在
        PowerInjection injection = service.getClass().getAnnotation(PowerInjection.class);
        if (injection != null) {
            // 获取持久层实例
            IPersistentService thisMap = persistentService.createAndGet(this.name, path, this.name);

            // 构建父资源
            ResourceModel parentResource = parseClassTypePowerInjection(service, injection, thisMap);

            Method[] methods = service.getClass().getDeclaredMethods();
            Stream.of(methods).forEach(method -> {
                // 方法上的注解，作为资源存在
                PowerInjection fAnn = method.getAnnotation(PowerInjection.class);
                if (fAnn != null) {
                    // 构建资源
                    parseMethodPowerInjection(service, method, fAnn, parentResource);
                }
            });

            // 持久化入库
            thisMap.insert(parentResource);

            // 构建动态代理
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(service.getClass());
            enhancer.setCallback(new MethodInterceptor() {
                @Override
                public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                    // TODO 实现权限控制相关逻辑
                    return methodProxy.invokeSuper(o, objects);
                }
            });
            // 有权限资源需求时，换成代理实例再返回
            return (IResourceService) enhancer.create();
        }

        // 没有权限资源需求，直接原装服务返回
        return service;
    }

    @Override
    public IResourceService modified(IResourceService service) {
        // 类上的注解作为群组存在
        PowerInjection injection = service.getClass().getAnnotation(PowerInjection.class);
        if (injection != null) {

            // 清空原先绑定的数据
            remove(service);

            // 重新扫描
            return scan(service);
        }

        return service;
    }

    @Override
    public IResourceService remove(IResourceService service) {
        // 类上的注解作为群组存在
        PowerInjection injection = service.getClass().getAnnotation(PowerInjection.class);
        if (injection != null) {
            String code = injection.code();
            String name = injection.name();
            if ("".equals(name)) {
                name = service.getClass().getSimpleName();
            }
            // 获取持久层实
            IPersistentService thisMap = persistentService.createAndGet(this.name, path, this.name);
            thisMap.get(this.name).clear(md5(name, code));
        }
        return service;
    }

    /**
     * 解析方法上的权限资源
     *
     * @param service
     * @param method
     * @param injection
     * @param parentResource
     */
    private void parseMethodPowerInjection(IResourceService service, Method method, PowerInjection injection, ResourceModel parentResource) {
        String fCode = injection.code();
        String fName = injection.name();
        String fDesc = injection.desc();
        if ("".equals(fName)) {
            fName = service.getClass().getSimpleName() + "_" + method.getName() + "_" + fCode;
        }
        if ("".equals(fDesc)) {
            fDesc = fName;
        }

        // 构建资源
        ResourceModel model = new ResourceModel();
        model.setId(md5(fName, fCode));
        model.setResourceCode(fCode);
        model.setResourceName(fName);
        model.setResourceDesc(fDesc);
        model.setParentId(parentResource.getId());
        model.setParentResource(Collections.singletonMap(parentResource.getId(), parentResource));
        model.setResourceGroup(parentResource.getResourceGroup());
        parentResource.getResourceGroup().getResources().add(model);
        parentResource.getChildren().put(model.getId(), model);
    }

    /**
     * 解析类上的注解资源
     *
     * @param service
     * @param injection
     * @param thisMap
     * @return
     */
    private ResourceModel parseClassTypePowerInjection(IResourceService service, PowerInjection injection, IPersistentService thisMap) {
        String code = injection.code();
        String name = injection.name();
        String desc = injection.desc();
        if ("".equals(name)) {
            name = service.getClass().getSimpleName();
        }
        if ("".equals(desc)) {
            desc = name;
        }

        // 构建父资源
        ResourceModel parentResource = new ResourceModel();
        parentResource.setId(md5(name, code));
        ResourceModel resource = thisMap.selectOne(parentResource);
        if (resource != null) {
            // 持久层有资源，表示系统重启时重新装载，但是旧数据除了权限编码之外，都不再重新变化
            BeanUtils.copyProperties(resource, parentResource);
        } else {
            // 如果持久层不存在，则为新资源，需要构建一个新的资源群
            ResourceGroupModel groupModel = new ResourceGroupModel();
            groupModel.setId(UUID.randomUUID().toString());
            groupModel.setResourceGroupName(name);
            groupModel.getResources().add(parentResource);
            parentResource.setResourceGroup(groupModel);
            parentResource.setResourceDesc(desc);
        }
        parentResource.setResourceName(name);
        parentResource.setResourceCode(code);
        return parentResource;
    }

    private String md5(String name, String code) {
        return utilService.encodeByMD5(name + "_" + code);
    }
}
