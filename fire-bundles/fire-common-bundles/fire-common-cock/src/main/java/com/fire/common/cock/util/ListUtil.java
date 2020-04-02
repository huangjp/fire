package com.fire.common.cock.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** 
 * @ClassName: ListUtil 
 * @Description: (这里用一句话描述这个类的作用) 
 * @author huangjp
 * @date 2014年12月20日 下午12:13:18 
 */
public class ListUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(ListUtil.class);
	
	/**
	 * 为集合向上泛型
	 * @param lowper 原集合
	 * @param c 泛型为哪个类型的集合
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T, R extends T> List<T> upwardGenericUpList(List<R> lowper) {
		List<T> list = new ArrayList<T>();
		for(Object object : lowper) {
			list.add((T) object);
		}
		return list;
	}
	
	/**
	 * 为集合向下泛型
	 * @param lowper 原集合
	 * @param c 泛型为哪个类型的集合
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T, R extends T> List<R> downGenericUpList(List<T> lowper) {
		List<R> list = new ArrayList<R>();
		for(Object object : lowper) {
			list.add((R) object);
		}
		return list;
	}
	
	/**
	 * 从接口的一个子类向另一个子类过度
	 * @param lowper
	 * @param c
	 * @return
	 */
	public static <T, R extends T> List<R> sonToOtherSonWithList(List<T> lowper, Class<R> c) {
		List<R> list = new ArrayList<R>();
		for(Object object : lowper) {
			try {
				R r = c.newInstance();
				BeanUtils.copyProperties(object, r, true);
				list.add(r);
			} catch (InstantiationException e) {
				LOG.error("{}", e);
			} catch (IllegalAccessException e) {
				LOG.error("{}", e);
			}
		}
		return list;
	}
	
	/**
	 * 常规转换LIST
	 * @param t
	 * @return
	 */
	public static <T> List<T> ObjectToList(T t) {
		List<T> list = new ArrayList<T>();
		list.add(t);
		return list;
	}
}
