package com.fire.system.api.model;

import com.fire.third.party.api.model.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 资源模型
 * Created from HuangJinPing on 2020/3/15 0015-下午 21:15
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public class ResourceModel<T> extends Entity {
    // 资源主键
    private String id;
    // 资源编码，用于判断资源权限
    private String resourceCode;
    // 资源描述
    private String resourceDesc;
    // 前端界面展示名称，由前端工程师在其开发的界面上自己定义，如果没有设置，则默认使用resourcsc
    private String resourceName;
    // 父资源ID
    private String parentId;
    // 父资源
    private Map<String, ResourceModel<T>> parentResource;
    // 子资源
    private Map<String, ResourceModel<T>> children;
    // 所属系统
    private SysModel sys;
    // 所属资源群
    private ResourceGroupModel<T> resourceGroup;
    // 资源所有的业务模型
    private T model;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getResourceDesc() {
        return resourceDesc;
    }

    public void setResourceDesc(String resourceDesc) {
        this.resourceDesc = resourceDesc;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Map<String, ResourceModel<T>> getParentResource() {
        return parentResource;
    }

    public void setParentResource(Map<String, ResourceModel<T>> parentResource) {
        this.parentResource = parentResource;
    }

    public synchronized Map<String, ResourceModel<T>> getChildren() {
        if (children == null) {
            children = new HashMap<>();
        }
        return children;
    }

    public void setChildren(Map<String, ResourceModel<T>> children) {
        this.children = children;
    }

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
    }

    public SysModel getSys() {
        return sys;
    }

    public void setSys(SysModel sys) {
        this.sys = sys;
    }

    public ResourceGroupModel<T> getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupModel<T> resourceGroup) {
        this.resourceGroup = resourceGroup;
    }
}
