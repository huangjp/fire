package com.fire.system.api.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 资源分组模型
 * 资源群绑定业务系统，并且包含所有子资源
 * Created from HuangJinPing on 2020/3/15 0015-下午 21:15
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public class ResourceGroupModel<T> implements Serializable {
    // 资源组主键
    private String id;
    // 所属系统
    private SysModel sys;
    // 资源组名称
    private String resourceGroupName;
    // 其包含的所有资源列表
    public List<ResourceModel<T>> resources = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SysModel getSys() {
        return sys;
    }

    public void setSys(SysModel sys) {
        this.sys = sys;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    public List<ResourceModel<T>> getResources() {
        return resources;
    }

    public void setResources(List<ResourceModel<T>> resources) {
        this.resources = resources;
    }
}
