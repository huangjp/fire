package com.fire.common.cock.util;

import com.fire.common.cock.pojo.JsonRootBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;

/**
 * 内存操作类，用户内存信息安全
 * 
 * @author Administrator
 *
 */
@SuppressWarnings("restriction")
public class CockUnsafe {

	protected static final Logger LOG = LoggerFactory
			.getLogger(CockUnsafe.class);

	private static Unsafe getUnsafe() {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			Unsafe unsafe = (Unsafe) f.get(null);
			return unsafe;
		} catch (NoSuchFieldException e) {
			LOG.error("getUnsafe() {}", e);
		} catch (SecurityException e) {
			LOG.error("getUnsafe() {}", e);
		} catch (IllegalArgumentException e) {
			LOG.error("getUnsafe() {}", e);
		} catch (IllegalAccessException e) {
			LOG.error("getUnsafe() {}", e);
		}
		return Unsafe.getUnsafe();
	}

	public static void main(String[] args) {
		JsonRootBean user = new JsonRootBean();
		user.setUsername("admin");

		String password = user.getUsername();
		System.out.println(user.getUsername());
		System.out.println(password);

		Unsafe unsafe = getUnsafe();

		String str = new String(password.replaceAll(".", "?"));

		unsafe.copyMemory(str, 0L, null, toAddress(password), sizeOf(password));

		System.out.println(password);
		System.out.println(user.getUsername());

	}

	public static void copyMemory(String string) {
		Unsafe unsafe = getUnsafe();

		String str = new String(string.replaceAll(".", "?"));

		unsafe.copyMemory(str, 0L, null, toAddress(string), sizeOf(string));
	}

	private static long normalize(int value) {
		if (value >= 0)
			return value;
		return (~0L >>> 32) & value;
	}

	public static void putObject(Object obj, String key) {
		try {
			Field name = obj.getClass().getDeclaredField(key);
			Unsafe unsafe = getUnsafe();
			unsafe.putObject(obj, unsafe.objectFieldOffset(name), "???");
		} catch (NoSuchFieldException e) {
			LOG.error("getUnsafe() {}", e);
		} catch (SecurityException e) {
			LOG.error("getUnsafe() {}", e);
		}
	}

	public static long sizeOf(Object obj) {
		Unsafe unsafe = getUnsafe();

		HashSet<Field> hashSet = new HashSet<Field>();
		Class<?> c = obj.getClass();
		if (c != Object.class) {
			for (Field f : c.getDeclaredFields()) {
				if ((f.getModifiers() & Modifier.STATIC) == 0) {
					hashSet.add(f);
				}
			}
		}

		long maxsize = 0;
		for (Field f : hashSet) {
			long offset = unsafe.objectFieldOffset(f);
			if (offset > maxsize) {
				maxsize = offset;
			}
		}
		return ((maxsize / 8) + 1) * 8;
	}

	public static long toAddress(Object obj) {
		Unsafe unsafe = getUnsafe();
		Object[] objects = new Object[] { obj };
		long address = unsafe.arrayBaseOffset(Object[].class);
		return normalize(unsafe.getInt(objects, address));
	}
}
