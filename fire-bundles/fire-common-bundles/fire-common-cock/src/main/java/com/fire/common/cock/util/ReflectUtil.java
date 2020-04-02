package com.fire.common.cock.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/** 
 * @ClassName: ReflectUtil 
 * @Description: (这里用一句话描述这个类的作用) 
 * @author huangjp
 * @date 2014年12月19日 上午10:14:40 
 */
public class ReflectUtil {

	/**
	 * 反射获取本类对应KEY名称的值
	 * @param key
	 * @param t
	 * @return
	 */
	public static <T> Object getObjectByReflect(String key, T t, Class<?>...types) {
		try {
			Method m = getMethod(t.getClass(), "get" + firstToUpperCase(key), types);
			return m.invoke(t);
		} catch (SecurityException e) {
			throw new RuntimeException("SecurityException", e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("IllegalArgumentException", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("IllegalAccessException", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("InvocationTargetException", e);
		} catch(NullPointerException e){
			e.printStackTrace();
			throw new NullPointerException("NullPointerException");
		}
	}
	
	/**
	 * 通过反射为本类对象设置value
	 * @param key
	 * @param value
	 * @param t
	 */
	public static <T> void setObjectByReflect(String key, Object value, T t, Class<?>...types) {
		Method m = getMethod(t.getClass(), "set" + firstToUpperCase(key), types);
		try {
			m.invoke(t, value);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("IllegalArgumentException", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("IllegalAccessException", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("InvocationTargetException", e);
		}
	}
	
	private static String firstToUpperCase(String string) {
		return MyUtil.firstToUpperCase(string);
	}
	
	/**
     * 反射获得方法，若本类不存在该方法则递归调用父类查找，若方法始终不存在返回空
     * @author lzxz
     * @param clazz           类对象
     * @param methodName      方法名
     * @param parameterTypes  方法参数列表
     * @return 此方法获得Method对象总是可用的
     */
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		
        Method method = null;
        try {
            if(clazz == null) return null;
            
            method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            
            return method;
        } catch (NoSuchMethodException e) {
            return getMethod(clazz.getSuperclass(), methodName, parameterTypes);
        }
    }
	
	/**
	 * 通过ID从集合中获取对应pojo
	 * @param list
	 * @param key
	 * @param id
	 * @param types
	 * @return
	 */
	public static  <T> T getObjectById(List<T> list, String key, Object id, Class<?>...types) {
		if(id == null || list == null) return null;
		for (T t : list) {
			Method m = getMethod(t.getClass(), "get" + firstToUpperCase(key), types);
			try {
				if(id != null && id.toString().equals(m.invoke(t))) return t;
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("IllegalArgumentException", e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("IllegalAccessException", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("InvocationTargetException", e);
			}
		}
		return null;
	}
}
