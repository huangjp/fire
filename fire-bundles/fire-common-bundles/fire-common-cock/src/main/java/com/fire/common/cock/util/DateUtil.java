package com.fire.common.cock.util;


import com.fire.common.exception.CommonException;
import com.fire.core.date.WolfDateFormat;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	/**
	 * 将日期字符串转换为日期类型
	 * 
	 * @param dateStr
	 *            日期字符串
	 * @param format
	 *            日期字符串格式
	 * @return
	 * @throws ParseException
	 */
	public static Date parseDate(String dateStr, String format) {
		WolfDateFormat wdf = new WolfDateFormat(format);
		Date date = wdf.parse(dateStr);
		return date;
	}

	/**
	 * 返回yyyy-MM-dd格式的时间字符串
	 * 
	 * @param date
	 * @return
	 */
	public static String getDate(Date date) {
		WolfDateFormat YMDFormatter = new WolfDateFormat(
				WolfDateFormat.YYYYMMDD);
		return YMDFormatter.format(date);
	}

	/**
	 * 返回yyyy-MM-dd HH:mm:ss格式的时间字符串
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateTime(Date date) {
		WolfDateFormat YMDHMSFormatter = new WolfDateFormat(
				WolfDateFormat.YYYYMMDDHHMMSS);
		return YMDHMSFormatter.format(date);
	}

	/**
	 * 将日期按照指定格式转换为字符串
	 * 
	 * @param date
	 * @param formatStr
	 * @return
	 */
	public static String getDateStr(Date date, String formatStr) {
		WolfDateFormat WolfDateFormat = new WolfDateFormat(formatStr);
		return WolfDateFormat.format(date);
	}
	
	/**
	 * 返回当前时间minute分钟之后的日期
	 * @param date
	 * @param minute
	 * @return
	 */
	public static Date getDateMin(Date date, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, minute);
		return calendar.getTime();
	}

	/**
	 * 返回当前时间day天之后（day>0）或day天之前（day<0）的时间
	 * 
	 * @param date
	 * @param day
	 * @return
	 */
	public static Date getDateD(Date date, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, day);
		return calendar.getTime();
	}

	/**
	 * 返回当前时间month个月之后（month>0）或month个月之前（month<0）的时间
	 * 
	 * @param date
	 * @param month
	 * @return
	 */
	public static Date getDateM(Date date, int month) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, month);
		return calendar.getTime();
	}

	/**
	 * 返回当前时间year年之后（year>0）或year年之前（year<0）的时间
	 * 
	 * @param date
	 * @param year
	 * @return
	 */
	public static Date getDateY(Date date, int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, year);
		return calendar.getTime();
	}

	/**
	 * 返回参数指定日期所属于的年份
	 * 
	 * @param date
	 *            日期
	 * @return
	 */
	public static int getYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * 返回参数指定日期所属于的月份
	 * 
	 * @param date
	 *            日期
	 * @return
	 */
	public static int getMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * 返回参数指定日期是哪一天
	 * 
	 * @param date
	 * @return
	 */
	public static int getDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 返回参数指定时间的小时
	 * 
	 * @param date
	 * @return
	 */
	public static int getHour(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * 返回参数指定时间的分钟
	 * 
	 * @param date
	 * @return
	 */
	public static int getMinute(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.MINUTE);
	}

	/**
	 * 返回参数指定时间的秒
	 * 
	 * @param date
	 * @return
	 */
	public static int getSecond(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(Calendar.SECOND);
	}
	
	/**
	 * 计算两个日期之间的天数差
	 * @param smdate
	 * @param bdate
	 * @return
	 * @throws ParseException
	 */
	public static int daysBetween(String smdate,String bdate) throws ParseException{  
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");  
        Calendar cal = Calendar.getInstance();    
        cal.setTime(sdf.parse(smdate));    
        long time1 = cal.getTimeInMillis();                 
        cal.setTime(sdf.parse(bdate));    
        long time2 = cal.getTimeInMillis();         
        long between_days=(time2-time1)/(1000*3600*24);     
        return Integer.parseInt(String.valueOf(between_days));     
    }  

	/**
	 * 返回参数指定时间的长整形表示
	 * 
	 * @param date
	 * @return
	 */
	public static long getMillis(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getTimeInMillis();
	}

	public static void main(String[] args) {
		WolfDateFormat cdf = new WolfDateFormat(WolfDateFormat.YYYYMMDD);
//		String str = "2017-05-25 06:29:44 +0000";
//		Date date = cdf.parse(str);
//		System.out.println(date);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		
		System.out.println(cdf.parse(cdf.format(cal.getTime())));
		
		System.out.println(getDateMin(new Date(), 30));;
	}

	/**
	 * 获取当前时间的 字符串 如 2013-01-02 21:21:21
	 * 
	 * @return
	 */
	public static final String getNowTimestampStr() {
		WolfDateFormat YMDHMSFormatter = new WolfDateFormat(
				WolfDateFormat.YYYYMMDDHHMMSS);
		return YMDHMSFormatter
				.format(new Timestamp(System.currentTimeMillis()));
	}

	public static final String getNowTimestampStr(String format) {
		WolfDateFormat YMDHMSFormatter = new WolfDateFormat(format);
		return YMDHMSFormatter
				.format(new Timestamp(System.currentTimeMillis()));
	}

	/**
	 * 
	 * @标题: getSqlTimestampDate
	 * @描述: 把date转化Timestamp
	 *
	 * @参数信息
	 * @param nowDate
	 * @return
	 *
	 * @返回类型 Timestamp
	 * @开发者 moysh
	 * @可能抛出异常
	 */
	public static Timestamp getSqlTimestampDate(Date nowDate) {
		if (nowDate == null) {
			nowDate = getNowDate(null);
		}
		Timestamp ts = new Timestamp(nowDate.getTime());
		return ts;
	}

	/**
	 * 
	 * @标题: getDateByType
	 * @描述: 取得当前格式日期字符串
	 *
	 * @参数信息
	 * @param dateType
	 * @return
	 *
	 * @返回类型 String
	 * @开发者 moysh
	 * @可能抛出异常
	 */
	public static String getDateByType(String dateType) {
		String today = "";
		Date day = new Date();
		SimpleDateFormat bartDateFormat = new SimpleDateFormat(dateType);
		today = bartDateFormat.format(day);
		return today;
	}

	/**
	 * 比较两个日期大小
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static String compareDate(Long d1, Long d2) {
		String result = "";
		if (d2 > d1) {
			result = "G";
		} else if (d2 < d1) {
			result = "L";
		} else {
			result = "E";
		}
		return result;
	}

	// 给定一个日期型字符串，返回加减n天后的日期型字符串
	public static String nDaysAfterOneDateString(String basicDate, int n) {
		WolfDateFormat df = new WolfDateFormat(WolfDateFormat.YYYYMMDDHHMMSS);
		Date tmpDate = null;
		try {
			tmpDate = df.parse(basicDate);
			long nDay = (tmpDate.getTime() / (24 * 60 * 60 * 1000) + 1 + n)
					* (24 * 60 * 60 * 1000);
			tmpDate.setTime(nDay);

			return df.format(tmpDate);
		} catch (Exception e) {
			CommonException.DATE_FORMAT_ERROR.recordException(e.getMessage());
		}
		return "";
	}

	/**
	 * 日期字符串格式 变 日期
	 * 
	 * @param s
	 *            String
	 * @return Date
	 */
	public static Date getDate(String s, String datetype) {
		WolfDateFormat sdf = new WolfDateFormat(datetype);
		return sdf.parse(s);
	}

	/**
	 * 获取现在时间
	 * 
	 * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
	 */
	public static Date getNowDate(String dateType) {
		Date currentTime = new Date();
		if (dateType == null || "".equals(dateType)) {
			dateType = WolfDateFormat.YYYYMMDDHHMMSS;
		}
		WolfDateFormat formatter = new WolfDateFormat(dateType);
		String dateString = formatter.format(currentTime);

		return formatter.parse(dateString);
	}

	/**
	 * 比较两个时间的先后 date1早于date2返回true；否则返回false
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static boolean compateTime(Date date1, Date date2) {
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime(date1);
		calendar2.setTime(date2);
		return calendar1.before(calendar2);

	}

	/**
	 * hjp 2014-9-17 返回当前日期 减指定日期date的天数，用于mysql中 SUBDATE(curdate(),INTERVAL ?
	 * DAY)的?
	 * 
	 * @param date
	 * @return
	 */
	public static int getPassDay(Date date) {
		if (date == null)
			return -1;// -1在mysql数据中表示当天
		WolfDateFormat YMDFormatter = new WolfDateFormat(
				WolfDateFormat.YYYYMMDD);
		YMDFormatter.set2DigitYearStart(new Date());
		int nowDay = YMDFormatter.getCalendar().get(Calendar.DAY_OF_MONTH);
		YMDFormatter.set2DigitYearStart(date);
		int thisDay = YMDFormatter.getCalendar().get(Calendar.DAY_OF_MONTH);
		return nowDay - thisDay;
	}

	/**
	 * hjp 2014-9-17 返回当前日期 减指定日期date的天数，用于mysql中 SUBDATE(curdate(),INTERVAL ?
	 * DAY)的?
	 * 
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public static int getPassDay(String date) throws ParseException {
		return getPassDay(parseDate(date, WolfDateFormat.YYYYMMDD));
	}
}