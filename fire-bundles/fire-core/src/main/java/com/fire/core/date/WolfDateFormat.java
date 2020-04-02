package com.fire.core.date;

import org.apache.logging.log4j.util.Strings;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 扩展日期格式化工具
 * 
 * @author Administrator
 *
 */
public class WolfDateFormat extends SimpleDateFormat {

	private static final long serialVersionUID = -5506939760777629624L;

	/** 标准时间格式：HH:mm:ss */
	public final static String NORM_TIME_PATTERN = "HH:mm:ss";

	/** 标准日期时间格式，精确到毫秒：yyyy-MM-dd HH:mm:ss.SSS */
	public final static String YYYYMMDDHHMMSSSSS = "yyyy-MM-dd HH:mm:ss.SSS";

	/** HTTP头中日期时间格式：EEE, dd MMM yyyy HH:mm:ss z */
	public final static String HTTP_DATETIME_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

	/** HTTP头中日期时间格式：EEE MMM dd HH:mm:ss zzz yyyy */
	public final static String JDK_DATETIME_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";

	public static final String YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";

	public static final String YYYYMMDDHHMM = "yyyy-MM-dd HH:mm";

	public static final String YYYYMMDDHH = "yyyy-MM-dd HH";

	public static final String YYYYMMDD = "yyyy-MM-dd";

	public static final String YYYYMM = "yyyy-MM";

	public static final String DD = "dd";
	
	public static final int LENGTH19 = 19;
	public static final int LENGTH6 = 6;
	public static final int LENGTH9 = 9;
	public static final int LENGTH16 = 16;
	public static final int LENGTH3 = 3;
	public static final int LENGTH13 = 13;
	public static final int LENGTH10 = 10;

	public WolfDateFormat() {
		super();
	}

	public static String now(String format) {
		WolfDateFormat wdf = new WolfDateFormat(format);
		return wdf.format(new Date());
	}

	public WolfDateFormat(String pattern, DateFormatSymbols formatSymbols) {
		super(pattern, formatSymbols);
	}

	public WolfDateFormat(String pattern, Locale locale) {
		super(pattern, locale);
	}

	public WolfDateFormat(String pattern) {
		super(pattern);
	}

	@Override
	public Date parse(String source) {
		if (Strings.isEmpty(source)) {
			return null;
		}

		String pattern = toPattern();
		int i = pattern.length() - source.length();
		if (pattern.length() >= LENGTH19) {
			if (i == LENGTH3) {
				source = source + ":00";
			} else if (i == LENGTH6) {
				source = source + ":00:00";
			} else if (i == LENGTH9) {
				source = source + " 00:00:00";
			} else {
				// 暂不支持
			}
		} else if (pattern.length() == LENGTH16) {
			if (i == LENGTH3) {
				source = source + ":00";
			} else if (i == LENGTH6) {
				source = source + " 00:00";
			} else {
				// 暂不支持
			}
		} else if (pattern.length() == LENGTH13) {
			if (i == LENGTH3) {
				source = source + " 00";
			} else {
				// 暂不支持
			}
		} else if (pattern.length() == LENGTH10) {
			// 暂不支持
		}

		try {
			return super.parse(source);
		} catch (ParseException e) {
			try {
				source = URLDecoder.decode(source, "utf-8");
				return super.parse(source);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

}
