package com.fire.system.api;

import com.fire.system.api.model.UserModel;

import java.util.List;

/**
 * 用户服务
 * 主要提供API，实现逻辑需要遵守如下约定
 * <p>
 * 此服务并不是基于角色认证的权限模型系统，而是直接根据用户（群）--资源（群）关系来绑定系统
 * 也就是用户-资源，用户-资源群，用户群-资源，用户群-资源群，四种逻辑来绑定权限关系。
 * 在加上系统所属进行系统间的资源、用户的隔离访问，一个用户可以属于多个系统，但是一个资源只能属于一个系统
 * Created from Administrator on 2020/3/22 0022-下午 16:57
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public interface IUserService {

    <T> List<UserModel<T>> selectAll(UserModel<T> model);

    <T> UserModel<T> saveUser(UserModel<T> model);

    <T> int saveUsers(List<UserModel<T>> models);

    <T> UserModel<T> selectOne(UserModel<T> model);

    <T> UserModel<T> removeOne(UserModel<T> model);

}
