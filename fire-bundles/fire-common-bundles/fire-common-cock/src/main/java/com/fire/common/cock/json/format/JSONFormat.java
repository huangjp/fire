package com.fire.common.cock.json.format;

import com.fire.common.cock.json.parse.JSONParse;
import com.fire.common.cock.util.ClassUtils;
import com.fire.common.cock.util.DateUtil;
import com.fire.common.exception.CommonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class JSONFormat {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Map<Class<?>, Object> map;

	private JSONFormat() {
		super();
		this.map = new HashMap<Class<?>, Object>();
	}

	public static String format(Object object, String... shieldArgument) {
		JSONFormat json = new JSONFormat();
		String str = json.formatJson(object, false, true, shieldArgument);
		json.map.clear();
		json = null;
		return str;
	}

	public static String format(Object object, boolean check) {
		JSONFormat json = new JSONFormat();
		String str = json.formatJson(object, check, true);
		json.map.clear();
		json = null;
		return str;
	}

	public static String format(Object object, boolean check, boolean isJsonParent) {
		JSONFormat json = new JSONFormat();
		String str = json.formatJson(object, check, isJsonParent);
		json.map.clear();
		json = null;
		return str;
	}

	private String formatJson(Object object, boolean check, boolean isJsonParent, String... shieldArgument) {
		if (null == object) {
			return null;
		}

		if (String.class.equals(object.getClass())) {
			return object.toString();
		}

		try {
			StringBuilder sb = new StringBuilder();
			if (object instanceof Throwable) {
				return object.toString();
			}

			if (object instanceof List) {
				formatList(object, sb, isJsonParent, shieldArgument);
			} else if (object != null && object instanceof Arrays) {
				formatList(object, sb, isJsonParent, shieldArgument);
			} else if (isWrapClass(object.getClass())) {
				sb.append('"');
				sb.append(new String(object.toString().getBytes("UTF-8"), "UTF-8"));
				sb.append('"');
				sb.append(',');
			} else if (object instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) object;
				mapToString(map, sb, isJsonParent, shieldArgument);
			} else {
				formatObject(object, sb, isJsonParent, shieldArgument);
			}

			String s = sb.toString();

			if (!check) {
				return s;
			}

			/*
			 * TODO 2016-12-17，此处为解决同一实例类出现两个相同字段（比如父、子都有相同字段时），
			 * 各浏览器采用容器解析json时，都是后面出现的key的value覆盖前面的，这会导致后Null覆盖前面有值的数据
			 * 实际这个模型应该很少出现才对，但是java支持这种model，为确保json格式化能正常被各浏览器支持，
			 * 此处暂时采用此办法过度使用一下，后期当想办法，从格式化的过程中进行判断处理，此处检查目前几乎增加一倍格式化时间
			 */

			String checkJSON = JSONParse.checkJson(sb.toString());
			if (null == checkJSON || "".equals(checkJSON) || checkJSON.length() < s.length() / 2) {
				return s;
			}
			logger.debug(s);
			return checkJSON;
		} catch (UnsupportedEncodingException e) {
			CommonException.JSON_FORMAT_ERROR.throwRuntimeException(e.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void formatList(Object object, StringBuilder sb, boolean isJsonParent, String... shieldArgument)
			throws UnsupportedEncodingException {
		if (null == object) {
			sb.append("[]");
			return;
		}
		Object[] list = null;
		if (object.getClass().isArray()) {
			list = (Object[]) object;
		} else {
			list = ((List<Object>) object).toArray();
		}
		if (null == list || list.length == 0) {
			sb.append("[]");
			return;
		}
		sb.append('[');
		for (Object o : list) {
			if (null != o) {
				if (isWrapClass(o.getClass())) {
					sb.append('"');
					sb.append(new String(o.toString().getBytes("UTF-8"), "UTF-8"));
					sb.append('"');
				} else if (o instanceof List) {
					formatList(o, sb, isJsonParent, shieldArgument);
				} else if (o != null && o instanceof Arrays) {
					formatList(o, sb, isJsonParent, shieldArgument);
				} else if(o instanceof Map) {
					Map<?, ?> m = (Map<?, ?>) o;
					mapToString(m, sb, isJsonParent, shieldArgument);
				} else {
					formatObject(o, sb, isJsonParent, shieldArgument);
				}
				sb.append(',');
			}
		}
		if (sb.toString().charAt(sb.length() - 1) == ',') {
			sb.setCharAt(sb.length() - 1, ']');
		} else {
			sb.append(']');
		}
	}

	private void formatObject(Object object, StringBuilder sb, boolean isJsonParent, String... shieldArgument)
			throws UnsupportedEncodingException {
		if (null == object) {
			sb.append("{}");
			return;
		}

		Class<?> c = object.getClass();

		if (sb.length() != 0
				&& (sb.toString().charAt(sb.length() - 1) == '}' || sb.toString().charAt(sb.length() - 1) == ']')) {
			if (sb.toString().charAt(sb.length() - 1) == ':') {
				sb.append("null");
			}
			sb.append(',');
		}
		sb.append('{');
		format(c, object, sb, isJsonParent, shieldArgument);

		char ch = sb.toString().charAt(sb.length() - 1);
		if (ch != '{') {
			if (ch == ',') {
				sb.setCharAt(sb.length() - 1, '}');
			} else {
				if (sb.toString().charAt(sb.length() - 1) == ':') {
					sb.append("null");
				}
				sb.append('}');
			}
		} else {
			// sb.delete(sb.length() - 2, sb.length());
			// TODO 2016-12-26 修改为直接添加一个括回来
			sb.append('}');
		}
		map.clear();
	}

	private void format(Class<?> c, Object object, StringBuilder sb, boolean isJsonParent, String... shieldArgument)
			throws UnsupportedEncodingException {
		if (null != sb && null != object && null != c) {
			// TODO 2017-3-23 修改判断逻辑为使用KEY和与此KEY对应的VALUE实例相同时才return
			if (map.containsKey(c) && map.get(c).equals(object)) {
				return;
			} else {
				map.put(c, object);
			}
			objectToString(c, object, sb, isJsonParent, shieldArgument);
			if(isJsonParent) {
				Class<?> parentC = c.getSuperclass();
				if (null != parentC && !Object.class.equals(parentC) && !(object instanceof Map)) {
					format(parentC, object, sb, isJsonParent, shieldArgument);
				}
			}
		}
	}

	private void mapToString(Map<?, ?> object, StringBuilder sb, boolean isJsonParent, String... shieldArguments)
			throws UnsupportedEncodingException {
		sb.append('{');
		for (Object o : object.keySet()) {
			if (o instanceof String) {
				sb.append('"');
				sb.append(o);
				sb.append('"');
				sb.append(':');
				Object instance = object.get(o);
				if (instance == null) {
					sb.append((String) null);
				} else {
					Class<?> fieldType = instance.getClass();
					if (isWrapClass(fieldType)) {
						if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
							sb.append(instance.toString());
						} else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
							sb.append(instance.toString());
						} else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
							sb.append(instance.toString());
						} else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
							sb.append(instance.toString());
						} else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
							sb.append(instance.toString());
						} else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
							sb.append(instance.toString());
						} else {
							sb.append('"');
							sb.append(instance.toString());
							sb.append('"');
						}
					} else if (List.class.equals(fieldType) || (instance != null && instance instanceof List)) {
						formatList(instance, sb, isJsonParent);
					} else if (fieldType.isArray() || (instance != null && instance instanceof Arrays)) {
						formatList(instance, sb, isJsonParent);
					} else if (Date.class.equals(fieldType) || (instance != null && instance instanceof Date)) {
						sb.append('"');
						String dateStr = DateUtil.getDateTime((Date) instance);
						sb.append(dateStr);
						sb.append('"');
					} else if (Map.class.equals(fieldType) || instance instanceof Map) {
						Map<?, ?> map = (Map<?, ?>) instance;
						mapToString(map, sb, isJsonParent, shieldArguments);
					} else {
						formatObject(instance, sb, isJsonParent);
					}
				}
				sb.append(',');
			} else {
				this.logger.info("暂不支持MapKey为：{}的JSON化", o.getClass());
				// TODO 暂不支持
			}
		}
		if (sb.toString().charAt(sb.length() - 1) == ',') {
			sb.setCharAt(sb.length() - 1, '}');
		} else {
			sb.append('}');
		}
	}

	private void objectToString(Class<?> c, Object object, StringBuilder sb, boolean isJsonParent,
			String... shieldArgument) throws UnsupportedEncodingException {
		if (isWrapClass(c)) {
			sb.append('"');
			sb.append(object.toString());
			sb.append('"');
		} else {
			String[] shieldArguments = shieldArgument;
			boolean isParm = false;
			if (null != shieldArgument && shieldArgument.length > 0) {
				isParm = true;
			}
			for (Field f : c.getDeclaredFields()) {
				//静态属性不JSON化
				if(Modifier.isStatic(f.getModifiers())) {
					continue;
				}
				
				String name = f.getName();
				
				//过滤不需要JSON化的字段
				if (isParm && Arrays.asList(shieldArguments).contains(name)) {
					continue;
				}
				
				f.setAccessible(true);
				sb.append('"');
				sb.append(name);
				sb.append('"');
				sb.append(':');
				Class<?> fieldType = f.getType();
				try {
					Object instance = f.get(object);
					// TODO 待确认 2017-3-18修改逻辑：
					// 除了通过字段类型判断外，还加上实例本来所属的类型，
					// 这样做是为了避免字段类型为Object时判断不到List类型的问题
					if (isWrapClass(fieldType) || (instance != null && isWrapClass(instance.getClass()))) {
						if (null != instance) {
							if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
								sb.append(instance.toString());
							} else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
								sb.append(instance.toString());
							} else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
								sb.append(instance.toString());
							} else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
								sb.append(instance.toString());
							} else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
								sb.append(instance.toString());
							} else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
								sb.append(instance.toString());
							} else {
								sb.append('"');
								String val = instance.toString();
								sb.append(JSONParse.dealJsonStr(val));
								sb.append('"');
							}
						} else {
							sb.append((String) null);
						}
					} else if (List.class.equals(fieldType) || (instance != null && instance instanceof List)) {
						formatList(instance, sb, isJsonParent);
					} else if (fieldType.isArray() || (instance != null && instance instanceof Arrays)) {
						if (instance != null && instance.getClass().equals(byte[].class)) {
							String json = new String((byte[]) instance);
							sb.append(json);
						} else {
							formatList(instance, sb, isJsonParent);
						}
					} else if (Date.class.equals(fieldType) || (instance != null && instance instanceof Date)) {
						if (null == instance) {
							sb.append("null");
						} else {
							sb.append('"');
							String dateStr = DateUtil.getDateTime((Date) instance);
							sb.append(dateStr);
							sb.append('"');
						}
					} else if (Map.class.equals(fieldType) || instance instanceof Map) {
						Map<?, ?> map = (Map<?, ?>) instance;
						mapToString(map, sb, isJsonParent, shieldArguments);
					} else {
						formatObject(instance, sb, isJsonParent);
					}

				} catch (IllegalArgumentException e) {
					this.logger.error("{}", e.getCause());
				} catch (IllegalAccessException e) {
					this.logger.error("{}", e.getCause());
				}
				if (sb.toString().charAt(sb.length() - 1) == ':') {
					sb.append("null");
				}
				sb.append(',');
			}

		}
	}

	private boolean isWrapClass(Class<?> c) {
		return ClassUtils.isWrapClass(c);
	}
}
