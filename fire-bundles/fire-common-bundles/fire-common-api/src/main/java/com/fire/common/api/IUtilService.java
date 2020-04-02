package com.fire.common.api;

import com.fire.core.service.IWolfService;

import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IUtilService extends IWolfService {

	/**
	 * 将对象转换为json字符串，默认不检查，以节约时间， 如果是复杂类型，JSON化需要检查时，请使用toJson(Object value, boolean
	 * check)
	 * 
	 * @param value
	 * @return
	 */
	public abstract String toJson(Object value);

	/**
	 * 将对象转换为json字符串,并且过滤shieldArgument相关字段
	 * 
	 * @param value
	 * @param shieldArgument
	 * @return
	 */
	public abstract String toJson(Object value, String... shieldArgument);

	/**
	 * 将对象转换为json字符串
	 * 
	 * @param value
	 * @param check
	 *            是否是复杂类型，有继承关系的实例对象是为复杂json转换
	 * @return
	 */
	String toJson(Object value, boolean check);

	/**
	 * 将对象转换为json字符串，可以设置是否check， 是否是将其父类也json化
	 * 
	 * @param value
	 * @param check
	 * @param isJsonParent
	 * @return
	 */
	String toJson(Object value, boolean check, boolean isJsonParent);

	/**
	 * 序列化表单转换为实例化对象
	 * 
	 * @param c
	 * @return
	 */
	public abstract <T> T formToObject(InputStream in, Class<T> c);

	public abstract <T> T formToObject(Object o, Class<T> c);

	/**
	 * 将json字符串转换为对象
	 * 
	 * @param jsonString
	 * @param clazz
	 * @return
	 */
	public abstract <T> T json2Object(String jsonString, Class<T> clazz);

	/**
	 * byte数组转指定类型
	 * 
	 * @param bytes
	 * @param c
	 * @return
	 */
	public abstract <T> T byteToObject(byte[] bytes, Class<T> c);

	/**
	 * 将map转换为实例
	 * 
	 * @param map
	 * @param clazz
	 * @return
	 */
	public abstract <T> T mapToInstance(Map<String, String[]> map, Class<T> clazz);

	/**
	 * 将json串转换成任何实例
	 * 
	 * @param in
	 * @param clazz
	 * @return
	 */
	public abstract Object jsonToAny(InputStream in, Class<?> clazz);

	public abstract Object jsonToAny(String json);

	public abstract Object jsonToAny(Object o, Class<?> clazz, String... shieldArgument);

	public abstract Object jsonToAny(InputStream in, Class<?> clazz, String... shieldArgument);

	public abstract Object parseInputStream(InputStream in);

	public abstract Object parseInputStreamForFrom(InputStream in);

	/**
	 * 将json字符串转换为List
	 * 
	 * @param jsonString
	 *            json字符串
	 * @param c
	 *            例如：String[].class
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> jsonToList(Class<T> c, String jsonString);

	/**
	 * map转换为实体对象
	 * 
	 * @param c
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public abstract <T> T castEntity(Class<T> c, Map<String, String[]> map);

	/**
	 * 获取所有接口
	 * 
	 * @param cls
	 * @return
	 */
	public abstract List<Class<?>> getAllInterfaces(Class<?> cls);

	/**
	 * 获取包下所有class<?>
	 * 
	 * @param pack
	 * @return
	 */
	public abstract List<Class<?>> getClasssFromPackage(String pack);

	/**
	 * 生成一个uuid
	 * 
	 * @return
	 */
	public abstract String getUUID();

	/**
	 * 为集合向下泛型
	 * 
	 * @param lowper
	 * @return
	 */
	public <T, R extends T> List<R> downGenericUpList(List<T> lowper);

	/**
	 * 为集合向上泛型
	 * 
	 * @param lowper
	 * @return
	 */
	public <T, R extends T> List<T> upwardGenericUpList(List<R> lowper);

	/**
	 * 实例类型的变更
	 * 
	 * @param source
	 * @param target
	 */
	public void copyProperties(Object source, Object target, boolean isSetNullValue, String... ignoreProperties);

	/**
	 * 获取当前日期的字符串格式
	 * 
	 * @param format
	 *            例如“yyyy-MM-dd HH:mm:ss”
	 * @return
	 */
	public String getCurrentDateForString(String format);

	/**
	 * 序列号生成
	 * 
	 * @return
	 */
	public String getMoveOrderNo();

	/**
	 * 列表转成树结构
	 * 
	 * @param list
	 * @param parentField
	 * @param keyField
	 * @param childrenField
	 * @return
	 */
	public <T> List<T> listToTree(List<T> list, String parentField, String keyField, String childrenField);

	/**
	 * 返回yyyy-MM-dd HH:mm:ss格式的时间字符串
	 * 
	 * @return
	 */
	public String dateFormat(Date date);

	/**
	 * 返回指定格式的日期字条串
	 * 
	 * @param date
	 * @param format
	 *            yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public String dateFormat(Date date, String format);

	public Date parseDate(String dateStr, String format) throws ParseException;

	/**
	 * 返回当前时间day天之后（day>0）或day天之前（day<0）的时间
	 * 
	 * @param date
	 * @param day
	 * @return
	 */
	public Date getDateD(Date date, int day);

	/**
	 * 返回当前时间minute分钟之后或之前的日期
	 * 
	 * @param date
	 * @param minute
	 * @return
	 */
	public Date getDateMin(Date date, int minute);

	/**
	 * 计算两个日期之间的天数差
	 */
	public int daysBetween(String smdate, String bdate) throws ParseException;

	/**
	 * 将带下划线字符串转成驼峰串
	 * 
	 * @param str
	 * @param isInitsmall
	 *            首字母是否小写
	 * @return
	 */
	public String humpcap(String str, boolean isInitsmall);

	/**
	 * 生成指定长度的随机数
	 * 
	 * @param length
	 * @return
	 */
	public String random(int length);

	public String base64Encode(byte[] binaryData);

	public byte[] base64Decode(String encoded);

	/**
	 * 获取线程池
	 * 
	 * @return
	 */
	// public WolfExecutors getWolfExecutors();

	/**
	 * 谨慎使用，直接修改内存数据，在信息需要达到内存安全级别时使用
	 * 
	 * @param obj
	 * @param key
	 */
	public void putObject(Object obj, String key);

	/**
	 * 操作字条串内存数据，信息安全达到内存级别时使用
	 * 
	 * @param string
	 */
	public void copyMemory(String string);

	/**
	 * 获取实例的内存地址
	 * 
	 * @param o
	 * @return
	 */
	public long toAddress(Object o);

	/**
	 * 获取实例实例大小
	 * 
	 * @param o
	 * @return
	 */
	public long sizeOf(Object o);

	/**
	 * SHA 加密算法
	 * 
	 * @param string
	 * @return
	 */
	public String encodeBySHA(String string);

	/**
	 * MD5 加密算法
	 * @param string
	 * @return
	 */
	public String encodeByMD5(String string);

	/**
	 * 递归指定文件夹下所有文件
	 * 
	 * @param file
	 * @param list
	 * @return
	 */
	public List<File> filesByDirectory(File file, List<File> list);

	/**
	 * 递归指定文件夹下的文件，并将其转换成set
	 * 
	 * @param filePath
	 * @return
	 */
	public Set<String> readNioFilePath(String filePath);

	/**
	 * StringTokenizer 字符串分隔
	 * 
	 * @param str
	 * @param sign
	 * @return
	 */
	public String[] split(String str, String sign);

	/**
	 * 执行shell脚本
	 * 
	 * @param command
	 */
	public boolean execShell(String[] command);

}