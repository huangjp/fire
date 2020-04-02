package com.fire.system.api.model;

import com.fire.third.party.api.model.Entity;

import java.util.Map;

/**
 * 系统模型
 * Created from HuangJinPing on 2020/3/15 0015-下午 21:15
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public class SysModel extends Entity {
    // 系统主键
    private String id;
    // 系统编码
    private String sysCode;
    // 系统拥有的用户
    private Map<String, UserModel> users;
    // 系统拥有的群组
    private Map<String, UserGroupModel> userGroups;
    // 系统拥有的资源
    private Map<String, ResourceModel> resources;
    // 系统拥有的资源群
    private Map<String, ResourceGroupModel> resourceGroups;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSysCode() {
        return sysCode;
    }

    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public Map<String, UserModel> getUsers() {
        return users;
    }

    public void setUsers(Map<String, UserModel> users) {
        this.users = users;
    }

    public Map<String, UserGroupModel> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(Map<String, UserGroupModel> userGroups) {
        this.userGroups = userGroups;
    }

    public Map<String, ResourceModel> getResources() {
        return resources;
    }

    public void setResources(Map<String, ResourceModel> resources) {
        this.resources = resources;
    }

    public Map<String, ResourceGroupModel> getResourceGroups() {
        return resourceGroups;
    }

    public void setResourceGroups(Map<String, ResourceGroupModel> resourceGroups) {
        this.resourceGroups = resourceGroups;
    }
}
