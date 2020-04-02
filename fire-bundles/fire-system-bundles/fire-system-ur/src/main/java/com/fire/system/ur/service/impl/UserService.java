package com.fire.system.ur.service.impl;

import com.fire.system.api.IUserService;
import com.fire.system.api.model.UserModel;
import com.fire.third.party.api.IPersistentService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.UUID;

/**
 * 用户相关接口服务
 * Created from huangjp on 2020/3/31 0031-下午 20:06
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
@Component
public class UserService implements IUserService {

    @Reference
    private IPersistentService persistentService;

    private String name = UserModel.class.getName();
    private String path = UserModel.class.getSimpleName();

    public <T> List<UserModel<T>> selectAll(UserModel<T> model) {
        List<UserModel<T>> users = persistentService.get(name).select(new UserModel<T>());
        return users;
    }

    public <T> UserModel<T> saveUser(UserModel<T> model) {
        if (model != null && (model.getId() == null || !"".equals(model.getId()))) {
            model.setId(UUID.randomUUID().toString());
        }
        if (model != null) {
            return persistentService.createAndGet(name, path, path).insert(model);
        }
        return model;
    }

    public <T> UserModel<T> selectOne(UserModel<T> model) {
        return persistentService.get(name).selectOne(model);
    }

    public <T> UserModel<T> removeOne(UserModel<T> model) {
        return persistentService.get(name).remove(model);
    }
}
