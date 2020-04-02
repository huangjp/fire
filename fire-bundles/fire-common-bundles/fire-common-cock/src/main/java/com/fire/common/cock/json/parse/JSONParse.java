package com.fire.common.cock.json.parse;

import com.fire.common.cock.util.ClassUtils;
import com.fire.common.cock.util.DateUtil;
import com.fire.common.cock.util.MyUtil;
import com.fire.common.exception.CommonException;
import com.fire.core.annotation.JsonFormat;
import com.fire.core.date.WolfDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.*;

@SuppressWarnings("unchecked")
public class JSONParse {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static JSONParse jsonParse = new JSONParse();

    // 缓存全部类型，key为类全名
    private static HashMap<String, JavaBeanInfo> deserializers = new HashMap<>(16);

    @Deprecated
    private int currentIndex;

    private JSONParse() {
        super();
        currentIndex = 0;
    }

    public static JSONParse getInstance() {
        if (jsonParse == null) {
            jsonParse = new JSONParse();
        }
        return jsonParse;
    }

    public static Object parseAny(String json, Class<?> c) {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readJson(json);
        Object t = jsonParse.instance(c, o);
        return t;
    }

    public static Object parseAny(InputStream in, Class<?> c) {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readInputStream(in);
        Object t = jsonParse.instance(c, o);
        return t;
    }

    public static <T> T byteToObject(byte[] bytes, Class<T> c) {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readFromByByte(bytes);
        T t = (T) jsonParse.instance(c, o);
        return t;
    }

    public static <T> T parseFrom(InputStream in, Class<T> c) {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readFromByByte(jsonParse.inputToByte(in));
        T t = (T) jsonParse.instance(c, o);
        return t;
    }

    public static <T> T parseFrom(Object o, Class<T> c) {
        JSONParse jsonParse = getInstance();
        T t = (T) jsonParse.instance(c, o);
        return t;
    }

    public static Object parseInputStreamForFrom(InputStream in) {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readFromByByte(jsonParse.inputToByte(in));
        return o;
    }

    public static <T> T parseModel(InputStream in, Class<T> c,
                                   String... shieldArgument) {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readInputStream(in);
        T t = (T) jsonParse.instance(c, o, shieldArgument);
        return t;
    }

    public static Object parseInputStream(InputStream in) {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readInputStream(in);
        return o;
    }

    public static <T> T parseModel(Object o, Class<T> c,
                                   String... shieldArgument) {
        JSONParse jsonParse = getInstance();
        T t = (T) jsonParse.instance(c, o, shieldArgument);
        return t;
    }

    public static <T> List<T> parseList(Object o, Class<T> c) {
        JSONParse jsonParse = getInstance();
        List<T> list = (List<T>) jsonParse.instance(c, o);
        return list;
    }

    public static <T> List<T> parseList(InputStream in, Class<T> c) {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readInputStream(in);
        List<T> list = (List<T>) jsonParse.instance(c, o);
        return list;
    }

    public static <T> T parseModel(String json, Class<T> c) {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readJson(json);
        T t = (T) jsonParse.instance(c, o);
        return t;
    }

    public static <T> T parseModelNew(String json, Class<T> c) {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readJson(c, json);
        return (T) o;
    }

    public static Object parseModel(String json) {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readJson(json);
        return o;
    }

    public static <T> List<T> parseList(String json, Class<T> c) {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readJson(json);
        List<T> list = (List<T>) jsonParse.instance(c, o);
        return list;
    }

    public static <T> T parseMap(Map<String, String[]> map, Class<T> c) {
        JSONParse jsonParse = getInstance();
        T t = (T) jsonParse.instance(c, map);
        return t;
    }

    public static String checkJson(String json)
            throws UnsupportedEncodingException {
        JSONParse jsonParse = getInstance();
        Object o = jsonParse.readJsonByByte(json.getBytes("UTF-8"), 0);
        StringBuilder sb = new StringBuilder();
        if (jsonParse.isWrapClass(o.getClass())) {
            sb.append('"');
            sb.append(new String(o.toString().getBytes("UTF-8"), "UTF-8"));
            sb.append('"');
        } else if (o instanceof List) {
            List<Object> list = (List<Object>) o;
            jsonParse.checkJsonList(list, sb);
        } else {
            jsonParse.checkJsonMap(o, sb);
        }
        return sb.toString();
    }

    private void checkJsonList(List<Object> list, StringBuilder sb)
            throws UnsupportedEncodingException {
        sb.append('[');
        for (Object o : list) {
            if (isWrapClass(o.getClass())) {
                sb.append('"');
                sb.append(new String(o.toString().getBytes("UTF-8"), "UTF-8"));
                sb.append('"');
                sb.append(',');
            } else if (o instanceof List) {
                checkJsonList((List<Object>) o, sb);
                sb.append(',');
            } else {
                checkJsonMap(o, sb);
                sb.append(',');
            }
        }
        if (sb.toString().charAt(sb.length() - 1) == ',') {
            sb.setCharAt(sb.length() - 1, ']');
        } else {
            sb.append(']');
        }
    }

    private void checkJsonMap(Object o, StringBuilder sb)
            throws UnsupportedEncodingException {
        sb.append('{');
        Map<String, Object> map = (Map<String, Object>) o;
        for (String key : map.keySet()) {
            sb.append('"');
            sb.append(key);
            sb.append('"');
            sb.append(':');
            Object value = map.get(key);
            if (null != value) {
                Class<?> fieldType = value.getClass();
                if (isWrapClass(fieldType)) {
                    if (fieldType.equals(int.class)
                            || fieldType.equals(Integer.class)) {
                        sb.append(new String(value.toString().getBytes("UTF-8"),
                                "UTF-8"));
                    } else if (fieldType.equals(short.class)
                            || fieldType.equals(Short.class)) {
                        sb.append(new String(value.toString().getBytes("UTF-8"),
                                "UTF-8"));
                    } else if (fieldType.equals(double.class)
                            || fieldType.equals(Double.class)) {
                        sb.append(new String(value.toString().getBytes("UTF-8"),
                                "UTF-8"));
                    } else if (fieldType.equals(boolean.class)
                            || fieldType.equals(Boolean.class)) {
                        sb.append(new String(value.toString().getBytes("UTF-8"),
                                "UTF-8"));
                    } else if (fieldType.equals(float.class)
                            || fieldType.equals(Float.class)) {
                        sb.append(new String(value.toString().getBytes("UTF-8"),
                                "UTF-8"));
                    } else if (fieldType.equals(long.class)
                            || fieldType.equals(Long.class)) {
                        sb.append(new String(value.toString().getBytes("UTF-8"),
                                "UTF-8"));
                    } else {
                        sb.append('"');
                        sb.append(new String(value.toString().getBytes("UTF-8"),
                                "UTF-8"));
                        sb.append('"');
                    }
                } else if (value instanceof List) {
                    checkJsonList((List<Object>) value, sb);
                } else {
                    checkJsonMap(value, sb);
                }
            } else {
                sb.append((String) null);
            }
            sb.append(',');
        }
        if (sb.toString().charAt(sb.length() - 1) == ',') {
            sb.setCharAt(sb.length() - 1, '}');
        } else {
            sb.append('}');
        }
    }

    public Object instance(Class<?> c, Object map, String... shieldArgument) {
        try {
            Object t = c.newInstance();

            if (map instanceof List) {
                List<Object> list = (List<Object>) map;
                List<Object> newList = new ArrayList<Object>();
                if (!isWrapClass(c)) {
                    for (Object o : list) {
                        newList.add(instance(c, o));
                    }
                }
                return newList;
            } else if (map instanceof Map) {
                mapToObject(map, c, t, shieldArgument);
                mapToSuperObject(c.getSuperclass(), map, t, shieldArgument);
            } else if (Date.class.equals(c)) {
                t = getDateForJson(null, map);
            }

            return t;
        } catch (InstantiationException e) {
            CommonException.JSON_PARSE_ERROR
                    .throwRuntimeException(e.getMessage(), map);
        } catch (IllegalAccessException e) {
            CommonException.JSON_PARSE_ERROR
                    .throwRuntimeException(e.getMessage(), map);
        } catch (IllegalArgumentException e) {
            CommonException.JSON_PARSE_ERROR
                    .throwRuntimeException(e.getMessage(), map);
        } catch (SecurityException e) {
            CommonException.JSON_PARSE_ERROR
                    .throwRuntimeException(e.getMessage(), map);
        } catch (UnsupportedEncodingException e) {
            CommonException.JSON_PARSE_ERROR
                    .throwRuntimeException(e.getMessage(), map);
        }
        return null;
    }

    private void mapToSuperObject(Class<?> c, Object json, Object t,
                                  String... shieldArgument) throws IllegalArgumentException,
            IllegalAccessException, UnsupportedEncodingException {
        if (c != null && json != null && t != null) {
            mapToObject(json, c, t, shieldArgument);
            mapToSuperObject(c.getSuperclass(), json, t, shieldArgument);
        }
    }

    private void mapToObject(Object json, Class<?> c, Object t,
                             String... shieldArgument) throws IllegalArgumentException,
            IllegalAccessException, UnsupportedEncodingException {
        String[] shieldArguments = shieldArgument;
        boolean isParm = false;
        if (null != shieldArgument && shieldArgument.length > 0) {
            isParm = true;
        }

        Map<String, Object> map = (Map<String, Object>) json;
        Field[] fields = c.getDeclaredFields();
        for (Field f : fields) {
            String name = f.getName();
            if (isParm && Arrays.asList(shieldArguments).contains(name)) {
                continue;
            }
            Object o = map.get(name);
            if (null != o) {
                f.setAccessible(true);
                Class<?> fieldType = f.getType();
                if (!isWrapClass(fieldType)) {
                    if (fieldType.equals(List.class)) {
                        ParameterizedType ptParameterizedType = (ParameterizedType) f
                                .getGenericType();
                        Type[] type = ptParameterizedType
                                .getActualTypeArguments();
                        Class<?> cc = (Class<?>) type[0];
                        if (!isWrapClass(cc)) {
                            f.set(t, instance(cc, o));
                        } else {
                            f.set(t, o);
                        }
                    } else if (Date.class.equals(fieldType)) {
                        Date date = getDateForJson(f, o);
                        f.set(t, date);
                    } else {
                        f.set(t, instance(fieldType, o));
                    }
                } else {
                    f.set(t, getBaseTypeProperties(fieldType, o));
                }
            }
        }
    }

    private Date getDateForJson(Field f, Object o) throws UnsupportedEncodingException {
        Date date = null;
        try {
            date = new Date(Long.valueOf(o.toString()));
        } catch (NumberFormatException e) {
            this.logger.warn(
                    "Recommend the use of long transmission date data, avoid the performance overhead。 ERROR：{}",
                    e.getMessage());
            if (f == null) {
                date = DateUtil.parseDate(o.toString(),
                        WolfDateFormat.YYYYMMDDHHMMSS);
                if (date == null) {
                    date = DateUtil.parseDate(
                            URLDecoder.decode(o.toString(),
                                    "utf-8"),
                            WolfDateFormat.YYYYMMDDHHMMSS);
                }
            } else {
                JsonFormat jf = f.getAnnotation(JsonFormat.class);
                if (jf == null) {
                    date = DateUtil.parseDate(o.toString(),
                            WolfDateFormat.YYYYMMDDHHMMSS);
                    if (date == null) {
                        date = DateUtil.parseDate(
                                URLDecoder.decode(o.toString(),
                                        "utf-8"),
                                WolfDateFormat.YYYYMMDDHHMMSS);
                    }
                } else {
                    date = DateUtil.parseDate(o.toString(),
                            jf.pattern());
                    if (date == null) {
                        date = DateUtil.parseDate(URLDecoder
                                        .decode(o.toString(), "utf-8"),
                                jf.pattern());
                    }
                }
            }
            if (date == null) {
                this.logger.error(
                        "DATE JSON : {}, parse NumberFormatException : {}",
                        o.toString(), e);
            }
        }
        return date;
    }

    private <T> T getBaseTypeProperties(Class<T> c, Object o) {
        if (c.equals(String.class)) {
            if (o instanceof String[]) {
                String[] arr = (String[]) o;
                return (T) arr[0];
            }
            return (T) o;
        } else {
            if (c.equals(int.class) || c.equals(Integer.class)) {
                return (T) Integer.valueOf(o.toString());
            } else if (c.equals(short.class) || c.equals(Short.class)) {
                return (T) Short.valueOf(o.toString());
            } else if (c.equals(double.class) || c.equals(Double.class)) {
                return (T) Double.valueOf(o.toString());
            } else if (c.equals(boolean.class) || c.equals(Boolean.class)) {
                return (T) Boolean.valueOf(o.toString());
            } else if (c.equals(float.class) || c.equals(Float.class)) {
                return (T) Float.valueOf(o.toString());
            } else if (c.equals(long.class) || c.equals(Long.class)) {
                return (T) Long.valueOf(o.toString());
            } else if (c.equals(char.class) || c.equals(Character.class)) {
                return (T) o.toString().toCharArray();
            } else if (c.equals(byte.class) || c.equals(Byte.class)) {
                return (T) Byte.valueOf(o.toString());
            } else {
                CommonException.JSON_PARSE_UNKNOWN_TYPE_ERROR
                        .recordException(o);
                return (T) o;
            }
        }
    }

    private boolean isWrapClass(Class<?> c) {
        return ClassUtils.isWrapClass(c);
    }

    private final byte[] inputToByte(InputStream inStream) {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        try {
            while ((rc = inStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
        } catch (IOException e) {
            CommonException.JSON_PARSE_INPUTSTREAM_ERROR
                    .throwRuntimeException(e.getMessage());
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

    private final Object readFromByByte(byte[] buf) {
        Map<String, Object> map = new HashMap<String, Object>();
        String key = null;
        String value = null;
        Integer head = 0;
        for (int i = 0; i < buf.length; i++) {
            switch (buf[i]) {
                case '=':
                    key = new String(Arrays.copyOfRange(buf, head, i));
                    key = MyUtil.initsmallcap(MyUtil.humpcap(key));
                    head = i + 1;
                    break;
                case '&':
                    value = new String(Arrays.copyOfRange(buf, head, i));
                    head = i + 1;
                    map.put(key, value);
                    break;
                default:
                    break;
            }
        }
        value = new String(Arrays.copyOfRange(buf, head, buf.length));
        map.put(key, value);
        return map;
    }

    @Deprecated
    private final Object readJsonByByte(byte[] buf, int findType)
            throws UnsupportedEncodingException {
        List<Object> list = new ArrayList<Object>();
        Map<String, Object> map = new HashMap<String, Object>();

        Integer head = null;
        String key = null;
        for (; currentIndex < buf.length; currentIndex++) {
            byte c = buf[currentIndex];
            switch (c) {
                case '\\':// 没有参考价值
                    break;
                /**
                 * 遇到单引号，虽然是有key,value的字符串才为被引号包括，
                 * 但是由于仅通过引号在有数组存在的情况无法确定是key还是value，因而此处不用
                 */
                case '\'':
                    break;
                /**
                 * 双引号，同上
                 */
                case '"':
                    break;
                case '\f':// 没有参考价值
                    break;
                case '\t':// 没有参考价值
                    break;
                case ' ':// 没有参考价值
                    break;
                case '\r':// 没有参考价值
                    break;
                case '\n':// 没有参考价值
                    break;
                /**
                 * 遇到“[”，左中括号后的内容需要使用递归 一、左中括号前面可能有：引号、冒号、或者什么都没有、左中括号、逗号
                 * 1、前面什么没有和前面是引号一样，表示整条json串的开始 2、前面是冒号，表示这个括号内数据是当前key的value
                 * 3、前面是左中括号，表示这个括号内数据是上层数组的值 4、前面是逗号，表示这个括号内数据是当前list容器的值
                 * 二、左中括号后面可能有：左大括号、引号、左中括号 1、后面引号说明数组内容是字符串型的值 2、后面大括号是数组内容是对象类型的值
                 * 3、后面中括号说明数组内仍然是数组，此处可当作与字符串型一样处理即可
                 * 三、由于递归里面可能无法判断上层要找什么值而不能确定容器实例，因而在递归前需要先确定要找什么数据，递归时传入findType参数
                 */
                case '[':
                    // 如果左中括号后面紧跟大括号,说明这个数组容器封装的是对象
                    int type = 0;
                    if (buf[currentIndex + 1] == '{') {
                        type = 1;
                    } else {// 否则封装的就是基础类型或者字符串
                        type = 2;
                    }

                    int listReturnType = 0;
                    if (currentIndex == 0 || buf[currentIndex - 1] == '"'
                            || buf[currentIndex - 1] == '\'') { // 说明是第一个中括号
                        listReturnType = 0;
                    } else if (buf[currentIndex - 1] == ':') {// 说明这个中括号内容是当前key的value
                        listReturnType = 1;
                    } else if (buf[currentIndex - 1] == '['
                            || buf[currentIndex - 1] == ',') {// 说明这个中括号内容是当前数组的值
                        listReturnType = 2;
                    }

                    currentIndex++;
                    Object listO = readJsonByByte(buf, type);// 递归去原集合找到后面的整条数组

                    if (listReturnType == 0) {
                        return listO;
                    } else if (listReturnType == 1) {
                        if (null == map.get(key))
                            map.put(key, listO);
                        key = null;
                    } else if (listReturnType == 2) {
                        list.add(listO);
                    }
                    break;
                /**
                 * 遇到“{”，左大括号后的内容与数组一样需要递归查找 一、左大括号前面可能有：引号、什么都没有、左中括号、逗号、冒号
                 * 1、前面引号和前面什么没有一样，说明是整个json串的开始 2、前面左中括号说明当前map是上层数组的值
                 * 3、前面逗号说明当前map是当前数组的值 4、前面是冒号说明是当前key的value 二、左大括号后面可能有：引号
                 * 1、引号无参考价值
                 */
                case '{':
                    int objectReturnType = 0;
                    if (currentIndex == 0 || buf[currentIndex - 1] == '"'
                            || buf[currentIndex - 1] == '\'') {// 说明是第一个大括号
                        objectReturnType = 0;
                    } else if (buf[currentIndex - 1] == ':') {// 说明这个大括号内容是对象的value
                        objectReturnType = 1;
                    } else if (buf[currentIndex - 1] == '['
                            || buf[currentIndex - 1] == ',') {// 说明这个大括号内容是当前数组的值
                        objectReturnType = 2;
                    }

                    currentIndex++;
                    Object objectO = readJsonByByte(buf, 1);// 递归去原集合找到整个对象

                    if (objectReturnType == 0) {
                        return objectO;
                    } else if (objectReturnType == 1) {
                        if (null == map.get(key))
                            map.put(key, objectO);
                        key = null;
                    } else if (objectReturnType == 2) {
                        list.add(objectO);
                    }
                    break;
                /**
                 * 遇到“：”，前面的一定是key,后面的不管是什么，都一定是value,但value不确定是什么类型，则留给后面的去判断
                 * 为避免key出错，约定：key用完立即置null，则此处要先判断为null才设置，否则则可能是value中包含有:符号
                 */
                case ':':
                    if (null != key) {
                        break;
                    }
                    int cut = currentIndex - 1;
                    if (buf[currentIndex - 2] == '\\'
                            || buf[currentIndex - 2] == '/') {
                        cut = currentIndex - 2;
                    }
                    if (buf[currentIndex - 1] == ' ') {
                        cut = currentIndex - 2;
                        if (buf[currentIndex - 3] == '\\'
                                || buf[currentIndex - 3] == '/') {
                            cut = currentIndex - 3;
                        }
                    }
                    String str = new String(Arrays.copyOfRange(buf, head, cut),
                            "UTF-8");
                    key = MyUtil.initsmallcap(MyUtil.humpcap(str));
                    head = null;
                    break;
                /**
                 * 遇到“，”，前面可能出现：右大括号、右中括号、引号、或者是null、或者数字，后面的管不着了，且前面出现的一定是值
                 * 1、当前面是右中括号或者右大括号时，一定是上层递归下来的，在上层的括号处已经处理过了
                 * 2、当前面是引号时，那么就一定是当前key或者数组的value
                 * 3、当前面是null时，问题就有点复杂了，需要判断前四个字符来确定是null
                 * 4、当前面是数字时，无特定办法判断，目前只能是排除如上情况都不是时则表示为数字
                 */
                case ',':

                    if (!(buf[currentIndex + 1] == '"'
                            || buf[currentIndex + 1] == ']'
                            || buf[currentIndex + 1] == '}'
                            || buf[currentIndex + 1] == '\'')) {
                        break;
                    }

                    if ((buf[currentIndex + 1] == ' ')
                            && !(buf[currentIndex + 2] == '"'
                            || buf[currentIndex + 2] == ']'
                            || buf[currentIndex + 2] == '}'
                            || buf[currentIndex + 2] == '\'')) {
                        break;
                    }

                    boolean isNull = false;
                    if (buf[currentIndex - 1] == 'l'
                            || buf[currentIndex - 1] == 'L') {
                        if (buf[currentIndex - 2] == 'l'
                                || buf[currentIndex - 2] == 'L') {
                            if (buf[currentIndex - 3] == 'u'
                                    || buf[currentIndex - 3] == 'U') {
                                if (buf[currentIndex - 4] == 'n'
                                        || buf[currentIndex - 4] == 'N') {
                                    isNull = true;
                                }
                            }
                        }
                    }
                    if (buf[currentIndex - 1] == '"'
                            || buf[currentIndex - 1] == '\'' || isNull) {
                        String value = null;
                        if (null != head && !isNull) {// 为空时说明有空字符串
                            int cutValue = currentIndex - 1;
                            if (buf[currentIndex - 2] == '\\'
                                    || buf[currentIndex - 2] == '/') {
                                cutValue = currentIndex - 2;
                            }
                            value = new String(
                                    Arrays.copyOfRange(buf, head, cutValue),
                                    "UTF-8");
                        }
                        head = null;
                        if (findType == 1) {
                            if (null == map.get(key))
                                map.put(key, value);
                            key = null;
                        } else if (findType == 2) {
                            list.add(value);
                        }
                    } else if (buf[currentIndex - 1] != '"'
                            && buf[currentIndex - 1] != '\'' && !isNull) {
                        if (null != head) {
                            String value = new String(
                                    Arrays.copyOfRange(buf, head, currentIndex),
                                    "UTF-8");
                            if (null == map.get(key))
                                map.put(key, value);
                            key = null;
                            head = null;
                        }
                    }
                    break;
                /**
                 * 遇到“}” 1、右大括号前面如果有引号出现，说明有最后一组key-value的value，需要先放入当前map
                 * 2、如果没有引号而是右中括号或右大括号，不做处理，只管返回即可
                 * 3、这个括号表示一个对象寻找结束，由于对象的查找一定是上层递归过来的，此处需要将当前map返回上层
                 */
                case '}':
                    boolean isNull1 = false;
                    if (buf[currentIndex - 1] == 'l'
                            || buf[currentIndex - 1] == 'L') {
                        if (buf[currentIndex - 2] == 'l'
                                || buf[currentIndex - 2] == 'L') {
                            if (buf[currentIndex - 3] == 'u'
                                    || buf[currentIndex - 3] == 'U') {
                                if (buf[currentIndex - 4] == 'n'
                                        || buf[currentIndex - 4] == 'N') {
                                    isNull1 = true;
                                }
                            }
                        }
                    }
                    if (buf[currentIndex - 1] == '"'
                            || buf[currentIndex - 1] == '\'' || isNull1) {
                        String value = null;
                        if (null != head && !isNull1) {// 为空时说明有空字符串
                            int cutValue = currentIndex - 1;
                            if (buf[currentIndex - 2] == '\\'
                                    || buf[currentIndex - 2] == '/') {
                                cutValue = currentIndex - 2;
                            }
                            value = new String(
                                    Arrays.copyOfRange(buf, head, cutValue),
                                    "UTF-8");
                        }
                        if (null == map.get(key))
                            map.put(key, value);
                        key = null;
                        head = null;
                    } else if (buf[currentIndex - 1] != '"'
                            && buf[currentIndex - 1] != '\'' && !isNull1) {
                        if (null != head) {
                            String value = new String(
                                    Arrays.copyOfRange(buf, head, currentIndex),
                                    "UTF-8");
                            if (null == map.get(key))
                                map.put(key, value);
                            key = null;
                            head = null;
                        }
                    }
                    return map;
                /**
                 * 遇到“]”,右中括号前面可能的符号：引号，右大括号，右中括号，其后面出现的任何字符都没有参考价值
                 * 1、右中括号前面如果是右大/右中括号，则由其自行处理，此处不管
                 * 2、右中括号前面如果是引号，则表示是该数组最后一个值，需要先放入当前list 3、右中括号一定为上层递归而来，因而必须返回
                 */
                case ']':
                    boolean isNull2 = false;
                    if (buf[currentIndex - 1] == 'l'
                            || buf[currentIndex - 1] == 'L') {
                        if (buf[currentIndex - 2] == 'l'
                                || buf[currentIndex - 2] == 'L') {
                            if (buf[currentIndex - 3] == 'u'
                                    || buf[currentIndex - 3] == 'U') {
                                if (buf[currentIndex - 4] == 'n'
                                        || buf[currentIndex - 4] == 'N') {
                                    isNull2 = true;
                                }
                            }
                        }
                    }
                    if (buf[currentIndex - 1] == '"'
                            || buf[currentIndex - 1] == '\'' || isNull2) {
                        if (null != head && !isNull2) {// 为空时说明有空字符串
                            int cutKey = currentIndex - 1;
                            if (buf[currentIndex - 2] == '\\'
                                    || buf[currentIndex - 2] == '/') {
                                cutKey = currentIndex - 2;
                            }
                            key = new String(Arrays.copyOfRange(buf, head, cutKey),
                                    "UTF-8");
                        }
                        head = null;
                        list.add(key);
                        key = null;
                    }
                    return list;
                /**
                 * 这种情况表示一定是有用的字符的数据，不管是key还是value都使用这个head索引值作字符检索的开始位置
                 * 约定：上面所有环节使用完head必须置null，因而此处判断为null时则记录下集合的索引位置
                 */
                default:
                    if (null == head) {
                        head = currentIndex;
                    }
                    break;
            }
        }

        return null;
    }

    /**
     * @param objectJson 1-找对象数组，2-找字符串数组
     * @return
     */
    public final Object readJson(String objectJson) {

        if (objectJson == null) {
            return null;
        }

        try {
            // TODO 从页面传值调用此方法时似乎有中文乱码异常，再次使用时请检查"".getBytes()方法以什么编码获取
//			return parseBytes(objectJson.getBytes("UTF-8"));
            return parseBytes(objectJson.toCharArray());
        } catch (UnsupportedEncodingException e) {
            CommonException.JSON_PARSE_ERROR
                    .throwRuntimeException(e.getMessage(), objectJson);
        }
        return null;
    }

    public final Object readJson(Class<?> c, String objectJson) {

        if (objectJson == null) {
            return null;
        }

        try {
            return parseBytes(c, objectJson.toCharArray());
        } catch (UnsupportedEncodingException | IllegalAccessException | NoSuchFieldException | InstantiationException e) {
            CommonException.JSON_PARSE_ERROR
                    .throwRuntimeException(e.getMessage(), objectJson);
        }
        return null;
    }

    private final Object readInputStream(InputStream in) {

        if (null == in) {
            return null;
        }

        try {
            byte[] bufs = inputToByte(in);
            return parseBytes(null);
        } catch (UnsupportedEncodingException e) {
            CommonException.JSON_PARSE_ERROR
                    .throwRuntimeException(e.getMessage());
        }
        return null;
    }

    /**
     * 处理json特殊字符
     *
     * @param jsonStr
     * @return
     */
    public static String dealJsonStr(String jsonStr) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; jsonStr != null && i < jsonStr.length(); i++) {
            char c = jsonStr.charAt(i);
            switch (c) {
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private Object parseBytes(Class<?> clz, char[] buf) throws UnsupportedEncodingException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        ByteForEach json = new ByteForEach(buf);
        json.index++;
        if (buf[0] == '{') {
            return parseBytesToObject(clz, json);
        } else if (buf[0] == '[') {
            return parseBytesToArrays(clz, json);
        } else {
            // 这是字符串形式，直接返回即可
            return parseBytesToString(json, false);
        }
    }

    private Object parseBytes(char[] buf) throws UnsupportedEncodingException {
        ByteForEach json = new ByteForEach(buf);
        json.index++;
        if (buf[0] == '{') {
            return parseBytesToObject(json);
        } else if (buf[0] == '[') {
            return parseBytesToArrays(json);
        } else {
            // 这是字符串形式，直接返回即可
            return parseBytesToString(json, false);
        }
    }

    private Object parseBytesToObject(Class<?> clz, ByteForEach json) throws UnsupportedEncodingException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        char[] buf = json.buf;
        Object object = clz.newInstance();
        JavaBeanInfo javaBeanInfo = deserializers.get(clz.getName());
        if (javaBeanInfo == null) {
            Field[] fields = clz.getDeclaredFields();
            javaBeanInfo = new JavaBeanInfo(fields);
            deserializers.put(clz.getName(), javaBeanInfo);
        }
        String key = null;
        // 最后一值一定是‘}’，所以不用循环查最后一个
        for (boolean next = true; next && json.index < buf.length - 1; json.index++) {
            char c = buf[json.index];
            switch (c) {
                // 只有第一个引号会进入，因此这里面只能是key或者是value
                case '"':
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    if (key == null) {
                        key = parseBytesToString(json, true);
                    } else {
                        String value = parseBytesToStringValue(json, false);
                        javaBeanInfo.setField(object, key, value);
                        key = null;
                    }
                    break;
                case '{'://递归去找对象
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    json.index++;
                    if (key != null) {
                        Class<?> classz = javaBeanInfo.getFieldType(key);
                        Object value = parseBytesToObject(classz, json);
                        javaBeanInfo.setField(object, key, value);
                        key = null;
                    } else {
                        Map<String, Object> value = parseBytesToObject(json);
                        System.out.println(value);
//                        object.putAll(value);
                    }
                    break;
                case '}'://对象找完了，即退出
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    next = false;
                    break;
                case '[':// 递归找数组
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    json.index++;
                    if (key != null) {
                        Class<?> classz = javaBeanInfo.getFieldType(key);
                        List<Object> arrays = parseBytesToArrays(classz, json);
                        javaBeanInfo.setField(object, key, arrays);
                        key = null;
                    }
                    break;
                case ':'://针对布尔型、数值类型的数据处理值
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    if (buf[json.index + 1] != '{' && buf[json.index + 1] != '"' && buf[json.index + 1] != '[') {
                        String v = parseBytesToString(json, false);
                        if (key != null) {
                            javaBeanInfo.setField(object, key, v);
                            key = null;
                            if (buf[json.index] == '}' || buf[json.index] == ']') {
                                json.index--;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        json.index--;
        return object;
    }

    private Map<String, Object> parseBytesToObject(ByteForEach json) throws UnsupportedEncodingException {
        char[] buf = json.buf;
        Map<String, Object> object = new HashMap<>(5);
        String key = null;
        // 最后一值一定是‘}’，所以不用循环查最后一个
        for (boolean next = true; next && json.index < buf.length - 1; json.index++) {
            char c = buf[json.index];
            switch (c) {
                // 只有第一个引号会进入，因此这里面只能是key或者是value
                case '"':
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    if (key == null) {
                        key = parseBytesToString(json, true);
                    } else {
                        String value = parseBytesToStringValue(json, false);
                        object.put(key, value);
                        key = null;
                    }
                    break;
                case '{'://递归去找对象
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    json.index++;
                    Map<String, Object> value = parseBytesToObject(json);
                    if (key != null) {
                        object.put(key, value);
                        key = null;
                    } else {
                        object.putAll(value);
                    }
                    break;
                case '}'://对象找完了，即退出
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    next = false;
                    break;
                case '[':// 递归找数组
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    json.index++;
                    List<Object> arrays = parseBytesToArrays(json);
                    if (key != null) {
                        object.put(key, arrays);
                        key = null;
                    }
                    break;
                case ':'://针对布尔型、数值类型的数据处理值
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    if (buf[json.index + 1] != '{' && buf[json.index + 1] != '"' && buf[json.index + 1] != '[') {
                        String v = parseBytesToString(json, false);
                        if (key != null) {
                            object.put(key, v);
                            key = null;
                            if (buf[json.index] == '}' || buf[json.index] == ']') {
                                json.index--;
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        json.index--;
        return object;
    }

    private List<Object> parseBytesToArrays(Class<?> clz, ByteForEach json) throws UnsupportedEncodingException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        List<Object> list = new ArrayList<>(5);
        char[] buf = json.buf;
        // 找数组进来的，最后一个一定是个“]”，所以不用循环判断了
        for (boolean next = true; next && json.index < buf.length - 1; json.index++) {
            char c = buf[json.index];
            switch (c) {
                case '"':// 说明接下来的是字符串数组
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    String v = parseBytesToString(json, false);
                    list.add(v);
                    break;
                case '{':// 说明数组包含对象，则递归去找对象
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    json.index++;
                    Object value = parseBytesToObject(clz, json);
                    list.add(value);
                    break;
                case '[':// 说明数组包含数组，则递归继续找
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    json.index++;
                    List<Object> arrays = parseBytesToArrays(clz, json);
                    list.addAll(arrays);
                    break;
                case ']':// 数据找完的标识
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    next = false;
                    break;
                case ' '://防止混合进布尔、数值类型的数据
                    break;
                case '}'://防止混合进布尔、数值类型的数据
                    break;
                case ','://防止混合进布尔、数值类型的数据
                    break;
                default:// 只有布尔、数值型的数组来找数据才会进到这里
                    json.index--;
                    String va = parseBytesToString(json, false);
                    list.add(va);
                    if (buf[json.index] == ']') {
                        json.index--;
                    }
                    break;
            }
        }
        json.index--;
        return list;
    }

    // 专门找数组
    private List<Object> parseBytesToArrays(ByteForEach json) throws UnsupportedEncodingException {
        List<Object> list = new ArrayList<>(16);
        char[] buf = json.buf;
        // 找数组进来的，最后一个一定是个“]”，所以不用循环判断了
        for (boolean next = true; next && json.index < buf.length - 1; json.index++) {
            char c = buf[json.index];
            switch (c) {
                case '"':// 说明接下来的是字符串数组
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    String v = parseBytesToString(json, false);
                    list.add(v);
                    break;
                case '{':// 说明数组包含对象，则递归去找对象
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    json.index++;
                    Map<String, Object> value = parseBytesToObject(json);
                    list.add(value);
                    break;
                case '[':// 说明数组包含数组，则递归继续找
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    json.index++;
                    List<Object> arrays = parseBytesToArrays(json);
                    list.addAll(arrays);
                    break;
                case ']':// 数据找完的标识
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    next = false;
                    break;
                case ' '://防止混合进布尔、数值类型的数据
                    break;
                case '}'://防止混合进布尔、数值类型的数据
                    break;
                case ','://防止混合进布尔、数值类型的数据
                    break;
                default:// 只有布尔、数值型的数组来找数据才会进到这里
                    json.index--;
                    String va = parseBytesToString(json, false);
                    list.add(va);
                    if (buf[json.index] == ']') {
                        json.index--;
                    }
                    break;
            }
        }
        json.index--;
        return list;
    }

    private String parseBytesToStringValue(ByteForEach json, boolean isKey) throws UnsupportedEncodingException {
        char[] buf = json.buf;
        json.index++;
        for (; buf[json.index] == ' '; json.index++)
            ;
        int head = json.index;
        String object = null;
        for (boolean next = true; next && json.index < buf.length; json.index++) {
            char c = buf[json.index];
            switch (c) {// 专门解析出字符串
                case '\\'://出现转义符号后，无论下一个是什么都直接跳过
                    json.index++;
                    break;
                case '"':
                    object = newString(json, head, isKey);
                    next = false;
                    break;
                default:
                    break;
            }
        }
        json.index--;
        return object;
    }

    private String parseBytesToString(ByteForEach json, boolean isKey) throws UnsupportedEncodingException {
        char[] buf = json.buf;
        json.index++;
        for (; buf[json.index] == ' '; json.index++)
            ;
        int head = json.index;
        String object = null;
        for (boolean next = true; next && json.index < buf.length; json.index++) {
            char c = buf[json.index];
            switch (c) {// 专门解析出字符串
                case '\\'://出现转义符号后，无论下一个是什么都直接跳过
                    json.index++;
                    break;
                case '"':
                    object = newString(json, head, isKey);
                    next = false;
                    break;
                case ','://数组前端找布尔、数值类型的数据
                    object = newString(json, head, isKey);
                    next = false;
                    break;
                case '}':// 与上相同，但同时该方法也支持取最后一个布尔、数值型数据。
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    // 如果后面出现引号，说明有子json串包含在内
                    if (buf[json.index + 1] != '"') {
                        object = newString(json, head, isKey);
                        next = false;
                    }
                    break;
                case ']'://与上相同
                    for (; buf[json.index + 1] == ' '; json.index++)
                        ;
                    // 如果后面出现引号，说明有子json串包含在内
                    if (buf[json.index + 1] != '"') {
                        object = newString(json, head, isKey);
                        next = false;
                    }
                    break;
                default:
                    break;
            }
        }
        json.index--;
        return object;
    }

    private String newString(ByteForEach json, int head, boolean isKey) throws UnsupportedEncodingException {
        char[] buf = json.buf;
        String str = new String(buf, head, json.index - head);
//		String str = new String(Arrays.copyOfRange(buf, head, json.index),
//				"UTF-8");
        if (isKey && str.contains("_")) {
            String v = MyUtil.initsmallcap(MyUtil.humpcap(str));
            return "null".equals(v) ? null : v.trim();
        } else {
            return "null".equals(str) ? null : str.trim();
        }
    }

    private class ByteForEach {
        int index;
        char[] buf;

        public ByteForEach(char[] buf) {
            this.buf = buf;
        }
    }

    private class JavaBeanInfo {
        Field[] fields;
        HashMap<String, JavaBeanFieldInfo> fieldDeserializers = new HashMap<>(16);

        public JavaBeanInfo(Field[] fields) {
            this.fields = fields;
        }

        /**
         * 获取指定字段类型
         *
         * @param name
         * @return
         */
        private Class<?> getFieldType(String name) {
            JavaBeanFieldInfo info = fieldDeserializers.get(name);
            if (info == null) {
                for (int i = 0; i < fields.length; i++) {
                    Field f = fields[i];
                    if (f.getName().equals(name)) {
                        info = new JavaBeanFieldInfo(f);
                        fieldDeserializers.put(name, info);
                        break;
                    }
                }
            }

            return info.fieldType;
        }

        private void setField(Object t, String name, Object value) throws IllegalAccessException, UnsupportedEncodingException {
            JavaBeanFieldInfo info = fieldDeserializers.get(name);
            Field f = null;
            if (info == null) {
                for (int i = 0; i < fields.length; i++) {
                    Field f1 = fields[i];
                    if (f1.getName().equals(name)) {
                        info = new JavaBeanFieldInfo(f1);
                        f = f1;
                        fieldDeserializers.put(name, info);
                        break;
                    }
                }
            } else {
                f = info.field;
            }
            if (f != null) {
                if (info.isWrapClass) {
                    if (info.isDate) {
                        Date date = getDateForJson(f, value);
                        f.set(t, date);
                    } else {
                        f.set(t, value);
                    }
                } else {
                    f.set(t, getBaseTypeProperties(info.fieldType, value));
                }
            }
        }
    }

    private class JavaBeanFieldInfo {
        Field field;
        Class<?> fieldType;
        boolean isWrapClass;
        boolean isDate;

        public JavaBeanFieldInfo(Field field) {
            field.setAccessible(true);
            this.field = field;
            Class<?> fieldType = field.getType();
            this.isWrapClass = !isWrapClass(fieldType);
            this.isDate = this.isWrapClass && Date.class.equals(fieldType);
            if (isWrapClass) {
                if (fieldType.equals(List.class)) {
                    ParameterizedType ptParameterizedType = (ParameterizedType) field
                            .getGenericType();
                    Type[] type = ptParameterizedType
                            .getActualTypeArguments();
                    Class<?> cc = (Class<?>) type[0];
                    fieldType = cc;
                }
            }
            this.fieldType = fieldType;
        }
    }
}
