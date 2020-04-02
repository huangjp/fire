package com.fire.common.cock.service;

import com.fire.common.api.IUtilService;
import com.fire.common.cock.json.format.JSONFormat;
import com.fire.common.cock.json.parse.JSONParse;
import com.fire.common.cock.shell.ShellUtil;
import com.fire.common.cock.util.Base64;
import com.fire.common.cock.util.*;
import org.apache.logging.log4j.util.Strings;
import org.osgi.service.component.annotations.Component;

import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

/**
 * @author huangjp
 * @ClassName: JSONUtil
 * @Description: (这里用一句话描述这个类的作用)
 * @date 2014年12月20日 上午11:18:23
 */
@Component
public class UtilService implements IUtilService {

    // @Resource
    // private CommonExecutors commonExecutors;

    @Override
    public String toJson(Object value) {
        return JSONFormat.format(value);
    }

    @Override
    public String toJson(Object value, String... shieldArgument) {
        return JSONFormat.format(value, shieldArgument);
    }

    @Override
    public String toJson(Object value, boolean check) {
        return JSONFormat.format(value, check);
    }

    @Override
    public String toJson(Object value, boolean check, boolean isJsonParent) {
        return JSONFormat.format(value, check, isJsonParent);
    }

    @Override
    public <T> T formToObject(InputStream in, Class<T> c) {
        return JSONParse.parseFrom(in, c);
    }

    @Override
    public <T> T formToObject(Object o, Class<T> c) {
        return JSONParse.parseFrom(o, c);
    }

    @Override
    public <T> T json2Object(String jsonString, Class<T> clazz) {
        return JSONParse.parseModel(jsonString, clazz);
    }

    @Override
    public <T> T byteToObject(byte[] bytes, Class<T> c) {
        return JSONParse.byteToObject(bytes, c);
    }

    @Override
    public Object parseInputStream(InputStream in) {
        return JSONParse.parseInputStream(in);
    }

    @Override
    public Object parseInputStreamForFrom(InputStream in) {
        return JSONParse.parseInputStreamForFrom(in);
    }

    @Override
    public Object jsonToAny(InputStream in, Class<?> clazz, String... shieldArgument) {
        return JSONParse.parseModel(in, clazz, shieldArgument);
    }

    @Override
    public Object jsonToAny(InputStream in, Class<?> clazz) {
        return JSONParse.parseModel(in, clazz);
    }

    @Override
    public Object jsonToAny(String json) {
        return JSONParse.parseModel(json);
    }

    @Override
    public Object jsonToAny(Object o, Class<?> clazz, String... shieldArgument) {
        return JSONParse.parseModel(o, clazz, shieldArgument);
    }

    @Override
    public <T> T mapToInstance(Map<String, String[]> map, Class<T> clazz) {
        return JSONParse.parseMap(map, clazz);
    }

    @Override
    public <T> List<T> jsonToList(Class<T> c, String jsonString) {
        List<T> list = JSONParse.parseList(jsonString, c);
        return list;
    }

    @Override
    public <T> T castEntity(Class<T> c, Map<String, String[]> map) {
        Map<String, Object> m = new HashMap<String, Object>(map);
        return MyUtil.castEntity(c, m);
    }

    @Override
    public void wolfInit() {
        // TODO Auto-generated method stub

    }

    @Override
    public void wolfClose() {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Class<?>> getAllInterfaces(Class<?> cls) {
        return ClassUtils.getAllInterfaces(cls);
    }

    @Override
    public String getUUID() {
        return UUIDGenerator.getUUID();
    }

    @Override
    public List<Class<?>> getClasssFromPackage(String pack) {
        return ClassUtils.getClasssFromPackage(pack);
    }

    @Override
    public <T, R extends T> List<R> downGenericUpList(List<T> lowper) {
        return ListUtil.downGenericUpList(lowper);
    }

    @Override
    public <T, R extends T> List<T> upwardGenericUpList(List<R> lowper) {
        return ListUtil.upwardGenericUpList(lowper);
    }

    @Override
    public void copyProperties(Object source, Object target, boolean isSetNullValue, String... ignoreProperties) {
        BeanUtils.copyProperties(source, target, isSetNullValue,
                (null == ignoreProperties) ? (String[]) null : ignoreProperties);
    }

    @Override
    public String getCurrentDateForString(String format) {
        return DateUtil.getNowTimestampStr(format);
    }

    @Override
    public String getMoveOrderNo() {
        return SerialNumberUtil.getMoveOrderNo();
    }

    @Override
    public <T> List<T> listToTree(List<T> list, String parentField, String keyField, String childrenField) {
        return RecursionUtil.listToTree(list, parentField, keyField, childrenField);
    }

    @Override
    public String dateFormat(Date date) {
        return DateUtil.getDateTime(date);
    }

    @Override
    public String dateFormat(Date date, String format) {
        return DateUtil.getDateStr(date, format);
    }

    @Override
    public Date parseDate(String dateStr, String format) throws ParseException {
        return DateUtil.parseDate(dateStr, format);
    }

    /**
     * 返回当前时间day天之后（day>0）或day天之前（day<0）的时间
     *
     * @param date
     * @param day
     * @return
     */
    public Date getDateD(Date date, int day) {
        return DateUtil.getDateD(date, day);
    }

    /**
     * 返回当前时间minute分钟之后的日期
     *
     * @param date
     * @param minute
     * @return
     */
    public Date getDateMin(Date date, int minute) {
        return DateUtil.getDateMin(date, minute);
    }

    /**
     * 计算两个日期之间的天数差
     */
    public int daysBetween(String smdate, String bdate) throws ParseException {
        return DateUtil.daysBetween(smdate, bdate);
    }

    @Override
    public String humpcap(String str, boolean isInitsmall) {
        if (isInitsmall) {
            return MyUtil.initsmallcap(MyUtil.humpcap(str));
        }
        return MyUtil.initcap(MyUtil.humpcap(str));
    }

    @Override
    public String base64Encode(byte[] binaryData) {
        return Base64.encode(binaryData);
    }

    @Override
    public byte[] base64Decode(String encoded) {
        return Base64.decode(encoded);
    }

    @Override
    public String random(int length) {
        StringBuilder str = new StringBuilder();// 定义变长字符串
        Random random = new Random();
        // 随机生成数字，并添加到字符串
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(10);
            // 防止第一个数据是0
            if (i == 0 && num == 0) {
                return random(length);
            }
            str.append(num);
        }
        return str.toString();
    }

    // @Override
    // public WolfExecutors getWolfExecutors() {
    // return commonExecutors.getWolfExecutors();
    // }

    @Override
    public void putObject(Object obj, String key) {
        if (obj != null) {
            CockUnsafe.putObject(obj, key);
        }
    }

    @Override
    public void copyMemory(String string) {
        CockUnsafe.copyMemory(string);
    }

    @Override
    public long toAddress(Object o) {
        return CockUnsafe.toAddress(o);
    }

    @Override
    public long sizeOf(Object o) {
        return CockUnsafe.sizeOf(o);
    }

    @Override
    public String encodeBySHA(String string) {
        if (Strings.isEmpty(string))
            return string;
        return new String(PWDEncry.SHAEncode(string.getBytes()));
    }

    @Override
    public String encodeByMD5(String string) {
        if (Strings.isEmpty(string))
            return string;
        return new String(PWDEncry.MD5Encode(string.getBytes()));
    }

    @Override
    public List<File> filesByDirectory(File file, List<File> list) {
        return FileUtil.filesByDirectory(file, list);
    }

    @Override
    public Set<String> readNioFilePath(String filePath) {
        return FileOperate.readNioFilePath(filePath);
    }

    /**
     * StringTokenizer
     *
     * @param str
     * @param sign
     * @return
     */
    public String[] split(String str, String sign) {
        return StringUtils.split2(str, sign);
    }

    @Override
    public boolean execShell(String[] command) {
        return ShellUtil.execShell(command);
    }
}
