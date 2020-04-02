package com.fire.third.party.api;

import com.fire.third.party.api.model.Entity;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 持久层统一API
 * Created from huangjp on 2020/3/30 0030-下午 21:01
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
public interface IPersistentService extends AutoCloseable {

    /**
     * 创建表，并获取该表的持久化实例
     * @param key 表ID
     * @param storedPath 表地址
     * @param name 名称
     * @return
     */
    IPersistentService createAndGet(String key, String storedPath, String name);

    /**
     * 获取指定表的持久化实例
     * @param key 通过表ID获取
     * @return
     */
    IPersistentService get(String key);

    /**
     * 清空指定表全部内容
     * @param key
     */
    void clear(String key);

    <T extends Entity> T insert(@Nullable T entity);

    <T extends Entity> T update(@Nullable T entity);

    <T extends Entity> List<T> select(T entity);

    <T extends Entity> T selectOne(T entity);

    <T extends Entity> T remove(@Nullable T entity);

    boolean remove(@Nullable String id);

}
