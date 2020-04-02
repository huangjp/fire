package com.fire.system.api.model;

import java.io.Serializable;
import java.util.List;

/**
 * 用户组模型
 * Created from HuangJinPing on 2020/3/15 0015-下午 21:15
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public class UserGroupModel<T> implements Serializable {
    // 用户组主键
    private String id;
    // 用户组名称
    private String userGroupName;
    // 用户列表
    private List<UserModel<T>> users;
    // 所属系统
    private SysModel sys;
    // 用户组父亲
    private UserGroupModel<T> parentGroup;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserGroupName() {
        return userGroupName;
    }

    public void setUserGroupName(String userGroupName) {
        this.userGroupName = userGroupName;
    }

    public List<UserModel<T>> getUsers() {
        return users;
    }

    public void setUsers(List<UserModel<T>> users) {
        this.users = users;
    }

    public SysModel getSys() {
        return sys;
    }

    public void setSys(SysModel sys) {
        this.sys = sys;
    }

    public UserGroupModel<T> getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(UserGroupModel<T> parentGroup) {
        this.parentGroup = parentGroup;
    }
}
