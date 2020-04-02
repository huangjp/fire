package com.fire.system.api;

/**
 * 资源扫描服务
 * 需要实现从项目中直接扫描出所有资源，扫描的同时，需要将父子关系确定好。同时做好持久化
 * 允许系统用户通过界面对扫描出来的资源进行命名、路由、编码等操作，当然也可以不操作
 * Created from Administrator on 2020/3/22 0022-下午 17:31
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public interface IResourceScanService {

    /**
     * 扫描资源，系统会在启动时从根目录扫描当前项目中含有该注解的所有资源
     *
     * @param service 权限资源实现
     * @return 资源实现的代理模式
     */
    IResourceService scan(IResourceService service);

    /**
     * 资源实现服务重新注入时，可能权限资源发生变化，需要立即调整
     *
     * @param service 权限资源实现
     * @return 资源实现的代理模式
     */
    IResourceService modified(IResourceService service);

    /**
     * 资源服务下线时，仍然需要立即调整该权限资源
     *
     * @param service 权限资源实现
     * @return 资源实现的代理模式
     */
    IResourceService remove(IResourceService service);
}
