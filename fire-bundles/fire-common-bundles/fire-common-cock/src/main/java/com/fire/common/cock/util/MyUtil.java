package com.fire.common.cock.util;


import com.fire.common.exception.CommonException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

public class MyUtil {

	public static <T> Map<String, Object> castMap(T entity)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, SecurityException, NoSuchMethodException {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		List<Field> field = new ArrayList<Field>();
		getFields(field, entity.getClass());
		for (Field f : field) {
			String fieldName = f.getName();
			Method m = null;
			try {
				m = ReflectUtil.getMethod(entity.getClass(), "get"
						+ initcap(fieldName), new Class[0]);
			} catch (Exception localException) {
			}
			if (m != null) {
				Object o = m.invoke(entity, new Object[0]);
				if ((o != null) && (m.getReturnType() != Boolean.TYPE)) {
					map.put(fieldName, o);
				}
			}
		}
		return map;
	}

	public static <T> void getFields(List<Field> field, Class<T> c) {
		field.addAll(Arrays.asList(c.getDeclaredFields()));
		if (c.getSuperclass() != null) {
			getFields(field, c.getSuperclass());
		}
	}

	public static <T> T castEntity(Class<T> c, Map<String, Object> map) {
		try {
			T t = c.newInstance();
			Field[] field = c.getDeclaredFields();
			for (Field f : field) {
				String fieldname = f.getName();
				for (Object key : map.keySet()) {
					if (fieldname.equals(key)) {
						f.setAccessible(true);
						f.set(t, map.get(key));
						break;
					}
				}
			}
			return t;
		} catch (InstantiationException | IllegalAccessException e) {
			CommonException.MAP_CONVER_ENTITY_ERROR.throwRuntimeException(e
					.getMessage());
		}
		return null;

	}

	public static <T> List<T> castList(Class<T> c, T[] ts) {
		if (ts == null)
			return new ArrayList<T>();
		List<T> list = new ArrayList<T>();
		for (T t : ts) {
			list.add(t);
		}
		return list;
	}

	public static String getMapKeys(Map<String, Object> map) {
		if (map == null)
			return "";
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String key : map.keySet()) {
			i++;
			if (i == 1)
				sb.append(key);
			else {
				sb.append("," + key);
			}
		}
		return "".equals(sb.toString()) ? "*" : sb.toString();
	}

	public static String getMapKeys(List<Map<String, Object>> list) {
		if ((list == null) || (list.isEmpty()))
			return "";
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String key : list.get(0).keySet()) {
			i++;
			if (i == 1)
				sb.append(key);
			else {
				sb.append("," + key);
			}
		}
		return "".equals(sb.toString()) ? "*" : sb.toString();
	}

	public static String initcap(String str) {
		if (StringUtils.isEmpty(str))
			return str;
		char[] ch = str.toCharArray();
		if ((ch[0] >= 'a') && (ch[0] <= 'z')) {
			ch[0] = (char) (ch[0] - ' ');
		}
		return new String(ch);
	}

	public static String humpcap(String str) {
		if (StringUtils.isEmpty(str))
			return str;
		if (!str.contains(" ") && !str.contains("_") && !str.contains("-")) {
			return str;
		}
		String[] strings = str.split("_");
		if (str.contains(" ")) {
			strings = str.split(" ");
		}
		if (str.contains("-")) {
			strings = str.split("-");
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			sb.append(initcap(strings[i].toLowerCase()));
		}
		return sb.toString();
	}

	public static String initsmallcap(String str) {
		if (StringUtils.isEmpty(str))
			return str;
		char[] ch = str.toCharArray();
		if ((ch[0] >= 'A') && (ch[0] <= 'Z')) {
			ch[0] = (char) (ch[0] + ' ');
		}
		return new String(ch);
	}

	public static void main(String[] args) {
		String str = "access_Token";
		System.out.println(initsmallcap(humpcap(str)));
	}

	public static Object getString(Object o) {
		Object object = o;
		String typename = object.getClass().getSimpleName();
		if (("Date".equals(typename)) || ("Timestamp".equals(typename))) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			object = "'" + sdf.format(o) + "'";
			return object;
		}
		int i = object.toString().trim().length();
		if ((object.toString().substring(0, 1).contains("'"))
				&& (object.toString().substring(i - 1, i).contains("'")))
			return object.toString().trim();
		if ((object.toString().contains("+"))
				|| (object.toString().contains("-")))
			return object.toString().trim();
		if ("".equals(object.toString().trim()))
			return "''";
		if ("String".equals(typename))
			object = "'" + object.toString().trim() + "'";
		return object;
	}

	public static String toLowerCaseInitial(String srcString, boolean flag) {
		StringBuilder sb = new StringBuilder();
		if (flag)
			sb.append(Character.toLowerCase(srcString.charAt(0)));
		else {
			sb.append(Character.toUpperCase(srcString.charAt(0)));
		}
		sb.append(srcString.substring(1));
		return sb.toString();
	}

	public static String getLastName(String clazzName) {
		String[] ls = clazzName.split("\\.");
		return ls[(ls.length - 1)];
	}

	public static String formatPath(String path) {
		String reg0 = "\\\\＋";
		String reg = "\\\\＋|/＋";
		String temp = path.trim().replaceAll(reg0, "/");
		temp = temp.replaceAll(reg, "/");
		if (temp.endsWith("/")) {
			temp = temp.substring(0, temp.length() - 1);
		}
		if (System.getProperty("file.separator").equals("\\")) {
			temp = temp.replace('/', '\\');
		}
		return temp;
	}

	public static String formatPath4Ftp(String path) {
		String reg0 = "\\\\＋";
		String reg = "\\\\＋|/＋";
		String temp = path.trim().replaceAll(reg0, "/");
		temp = temp.replaceAll(reg, "/");
		if (temp.endsWith("/")) {
			temp = temp.substring(0, temp.length() - 1);
		}
		return temp;
	}

	public static String getParentPath(String path) {
		return new File(path).getParent();
	}

	public static String getRelativeRootPath(String fullPath, String rootPath) {
		String relativeRootPath = null;
		String _fullPath = formatPath(fullPath);
		String _rootPath = formatPath(rootPath);

		if (_fullPath.startsWith(_rootPath))
			relativeRootPath = fullPath.substring(_rootPath.length());
		else {
			throw new RuntimeException("要处理的两个字符串没有包含关系，处理失败！");
		}
		if (relativeRootPath == null) {
			return null;
		}
		return formatPath(relativeRootPath);
	}

	public static String getSystemLineSeparator() {
		return System.getProperty("line.separator");
	}

	public static List<String> series2List(String series) {
		return series2List(series, "\\|");
	}

	private static List<String> series2List(String series, String regex) {
		List<String> result = new ArrayList<String>();
		if ((series != null) && (regex != null)) {
			for (String s : series.split(regex)) {
				if ((s.trim() != null) && (!s.trim().equals("")))
					result.add(s.trim());
			}
		}
		return result;
	}

	public static String list2series(List<String> strList) {
		StringBuffer series = new StringBuffer();
		for (String s : strList) {
			series.append(s).append("|");
		}
		return series.toString();
	}

	public static String firstToLowerCase(String resStr) {
		if (resStr == null)
			return null;
		if ("".equals(resStr.trim())) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		Character c = Character.valueOf(resStr.charAt(0));
		if (Character.isLetter(c.charValue())) {
			if (Character.isUpperCase(c.charValue()))
				c = Character.valueOf(Character.toLowerCase(c.charValue()));
			sb.append(resStr);
			sb.setCharAt(0, c.charValue());
			return sb.toString();
		}

		return resStr;
	}

	public static String firstToUpperCase(String resStr) {
		if (resStr == null)
			return null;
		if ("".equals(resStr.trim())) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		Character c = Character.valueOf(resStr.charAt(0));
		if (Character.isLetter(c.charValue())) {
			if (Character.isLowerCase(c.charValue()))
				c = Character.valueOf(Character.toUpperCase(c.charValue()));
			sb.append(resStr);
			sb.setCharAt(0, c.charValue());
			return sb.toString();
		}

		return resStr;
	}

	public static Map<String, Object> getMap(String key, Object value) {
		if (!StringUtils.isEmpty(key) && null != value) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(key, value);
			return map;
		}
		return null;
	}

	public static String getSqlStringFromMap(Map<String, Object> map) {
		if (map == null) {
			return "";
		}
		String str = "";
		Set<String> keySet = map.keySet();
		Iterator<String> keyIt = keySet.iterator();
		while (keyIt.hasNext()) {
			String key = (String) keyIt.next();
			str = str + key;
			str = str + "=";
			Object value = map.get(key).toString();

			str = str + value;
			if (keyIt.hasNext()) {
				str = str + " and ";
			}
		}
		return str;
	}

	public static String getFuzzySqlStringFromMap(Map<String, Object> map) {
		if (map == null) {
			return "";
		}
		String str = "";
		Set<String> keySet = map.keySet();
		Iterator<String> keyIt = keySet.iterator();
		while (keyIt.hasNext()) {
			String key = (String) keyIt.next();
			str = str + key;
			str = str + " like '%";
			Object value = map.get(key).toString();

			str = str + value;
			str = str + "%' ";
			if (keyIt.hasNext()) {
				str = str + " and ";
			}
		}
		return str;
	}

	public static Class<?> getClass(String className) {
		try {
			Class<?> c = Class.forName(className);
			return c;
		} catch (ClassNotFoundException e) {
		}
		throw new RuntimeException("Not find the class");
	}

	public static Class<?> getClass(String path, String key) {
		Properties prop = new Properties();
		try {
			prop.load(MyUtil.class.getClassLoader().getResourceAsStream(path));
			String className = prop.getProperty(key);
			Class<?> c = Class.forName(className);
			return c;
		} catch (IOException e1) {
			throw new RuntimeException(
					"File loading fails, could not find the path to the file specified");
		} catch (ClassNotFoundException e) {
		}
		throw new RuntimeException("Not find the class");
	}

	public static String getValue(String path, String key) {
		Properties prop = new Properties();
		try {
			prop.load(MyUtil.class.getClassLoader().getResourceAsStream(path));
			String className = prop.getProperty(key);
			return className;
		} catch (IOException e1) {
		}
		throw new RuntimeException(
				"File loading fails, could not find the path to the file specified");
	}
}