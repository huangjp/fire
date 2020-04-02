package com.fire.third.party.api;

import com.fire.third.party.api.model.VersionObject;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * Created from huangjp on 2020/3/30 0030-下午 21:01
 *
 * @version 1.0
 * @email 262404150@qq.com
 * @param <K>
 * @param <T>
 */
public interface StoredMapFactory<K,T extends VersionObject<Serializable>> {
	
	/**
	 * @param storedpath
	 * 数据文件保存的路径
	 * @param name 
	 * Map的名字
	 */
	ConcurrentMap<K,T> buildMap(String storedpath, String name);
	
}
