package com.fire.system.api.model;

import com.fire.third.party.api.model.Entity;

import java.util.Map;

/**
 * 用户模型
 * Created from HuangJinPing on 2020/3/15 0015-下午 21:15
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public class UserModel<T> extends Entity {

    // 主键
    private String id;

    // 登录名称
    private String loginName;

    // 密码
    private String password;

    // 用户业务系统的模型
    private T model;

    /**
     * 所属系统，一个用户在多个系统中存在
     * 结构说明：
     * {
     *     sysModelId(系统ID):SysModel(系统模型)
     * }
     */
    private Map<String, SysModel> sysModels;

    /**
     * 所属用户组, 一个用户可以属于多个用户组
     * 结构说明：
     * {
     *     userGroupModelId(用户群组ID):UserGroupModel(用户群组模型)
     * }
     */
    private Map<String, UserGroupModel> groups;

    /**
     * 拥有的资源，这些资源包含从用户处绑定的，也包含从用户组绑定的
     * 结构说明：
     * {
     *     sysModelId(系统ID):{
     *         ResourceModelId（资源ID）:ResourceModel（资源模型）
     *     }
     * }
     */
    private Map<String, Map<String, ResourceModel>> resources;

    public UserModel() {
    }

    public UserModel(T model) {
        this.model = model;
    }

    public UserModel(String id, T model) {
        this.id = id;
        this.model = model;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
    }

    public Map<String, SysModel> getSysModels() {
        return sysModels;
    }

    public void setSysModels(Map<String, SysModel> sysModels) {
        this.sysModels = sysModels;
    }

    public Map<String, UserGroupModel> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, UserGroupModel> groups) {
        this.groups = groups;
    }

    public Map<String, Map<String, ResourceModel>> getResources() {
        return resources;
    }

    public void setResources(Map<String, Map<String, ResourceModel>> resources) {
        this.resources = resources;
    }
}
