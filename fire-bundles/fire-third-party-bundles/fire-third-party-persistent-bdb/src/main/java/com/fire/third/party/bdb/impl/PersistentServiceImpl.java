package com.fire.third.party.bdb.impl;

import com.fire.third.party.api.IPersistentService;
import com.fire.third.party.api.StoredMapFactory;
import com.fire.third.party.api.model.Entity;
import com.fire.third.party.api.model.VersionObject;
import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 持久层BDB的实现
 * Created from huangjp on 2020/3/30 0030-下午 23:20
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
@Component(immediate = true, name = "com.fire.third.party.api.IPersistentService")
public class PersistentServiceImpl implements IPersistentService {

    private ConcurrentMap<String, PersistentServiceImpl> storeMaps;

    private ConcurrentMap<Serializable, VersionObject<Serializable>> storeMap;

    @Reference
    private StoredMapFactory storedMapFactory;

    @Override
    public synchronized PersistentServiceImpl createAndGet(String key, String storedPath, String name) {
        if (storeMaps == null) {
            storeMaps = new ConcurrentHashMap<>(5);
        }
        // 相当于每个实例管一个存储结构
        return storeMaps.computeIfAbsent(key, (k) -> {
            PersistentServiceImpl service = new PersistentServiceImpl();
            service.storeMap = storedMapFactory.buildMap(storedPath, name);
            return service;
        });
    }

    @Override
    public PersistentServiceImpl get(String key) {
        PersistentServiceImpl impl = storeMaps.get(key);
        return impl == null ? new PersistentServiceImpl() : impl;
    }

    @Override
    public <T extends Entity> T insert(@Nullable T t) {
        if (storeMap == null || t == null || StringUtils.isBlank(t.getId())) {
            return null;
        }
        storeMap.put(t.getId(), new VersionObject<>(t));
        return t;
    }

    @Override
    public <T extends Entity> T update(@Nullable T t) {
        return insert(t);
    }

    @Override
    public <T extends Entity> List<T> select(T t) {
        if (storeMap == null) {
            return null;
        }
        // TODO 检索表数据，要自己实现，现在先查询出全部数据
        Stream<T> entity = storeMap.values().parallelStream().map(n -> (T) n.getObj());
        return entity.collect(Collectors.toList());
    }

    @Override
    public <T extends Entity> T selectOne(T t) {
        if (storeMap == null || t == null || StringUtils.isBlank(t.getId())) {
            return null;
        }
        VersionObject<Serializable> v = storeMap.get(t.getId());
        return (T) v.getObj();
    }

    @Override
    public <T extends Entity> T remove(@Nullable T t) {
        if (storeMap == null || t == null || StringUtils.isBlank(t.getId())) {
            return null;
        }
        VersionObject<Serializable> v = storeMap.remove(t.getId());
        return (T) v.getObj();
    }

    @Override
    public boolean remove(@Nullable String s) {
        if (StringUtils.isNotBlank(s)) {
            VersionObject<Serializable> v = storeMap.remove(s);
            return v != null;
        }
        return false;
    }

    @Override
    public void close() throws Exception {
        if (storeMaps != null) {
            storeMaps.values().forEach(PersistentServiceImpl::clearStoreMap);
            storeMaps.clear();
            storeMaps = null;
        }
    }

    @Override
    public void clear(String key) {
        PersistentServiceImpl t = storeMaps.remove(key);
        if (t != null) {
            t.clearStoreMap();
            try {
                t.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void clearStoreMap() {
        if (this.storeMap != null) {
            this.storeMap.clear();
            this.storeMap = null;
        }
    }
}
