package com.fire.third.party.api.model;

import java.io.Serializable;

/**
 * 版本管理类型
 * Created from huangjp on 2020/3/30 0030-下午 21:10
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public class VersionObject<T extends Serializable> implements Serializable {
    private static final long serialVersionUID = 6721353360960222853L;
    private T obj;
    private long version;

    public VersionObject(T obj) {
        this.obj = obj;
        this.version = System.currentTimeMillis();
    }

    public T getObj() {
        return obj;
    }

    public long getVersion() {
        return version;
    }

}
