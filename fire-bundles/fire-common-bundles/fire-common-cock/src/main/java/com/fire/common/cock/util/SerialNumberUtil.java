package com.fire.common.cock.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName: SerialNumberUtil
 * @Description: (这里用一句话描述这个类的作用)
 * @author huangjp
 * @date 2015年1月19日 下午3:55:55
 */
public class SerialNumberUtil {
	private static String count = "000";
	private static String dateValue = new SimpleDateFormat("yyyyMMdd").format(new Date());

	/**
	 * 产生流水号
	 * 例如：
	 * if(maxNo == null || "".equals(maxNo)) {
	 *		no = SerialNumberUtil.getMoveOrderNo();
	 *	} else {
	 *		maxNo = maxNo.substring(8);//截取后四位
	 *		no = SerialNumberUtil.getMoveOrderNo(maxNo);
	 *	}
	 * @param myCount 传入则从myCount开始计数生成单号，不传入则从count开始计数
	 * @return 返回标准单号
	 */
	public synchronized static String getMoveOrderNo(String... myCount) {
		long No = 0;
		if (myCount != null && myCount.length != 0)
			count = myCount[0];
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		String nowdate = sdf.format(new Date());
		No = Long.parseLong(nowdate);
		if (!(String.valueOf(No)).equals(dateValue)) {
			count = "000";
			dateValue = String.valueOf(No);
		}
		SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
		String num = String.valueOf(date.format(new Date()));
		num += getNo(count);
		return num;
	}

	/**
	 * 返回当天的订单数+1
	 */
	public static String getNo(String s) {
		String rs = s;
		int i = Integer.parseInt(rs);
		i += 1;
		rs = "" + i;
		for (int j = rs.length(); j < 3; j++) {
			rs = "0" + rs;
		}
		count = rs;
		return rs;
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 1000; i++) {
			System.out.println(getMoveOrderNo());
		}
	}
	
	
	//时间生成器
//	private String toDateString(Calendar calendar) {
//		int year = calendar.get(Calendar.YEAR);
//		int month = calendar.get(Calendar.MONTH)+1;
//		int day = calendar.get(Calendar.DAY_OF_MONTH);
//		int msec = calendar.get(Calendar.MILLISECOND);
//		return "" + year + month + day + msec;
//	}
	
	
	
/*	  public static void main(String[] args) {
	    	System.out.println("ggg");
			Calendar cal = new GregorianCalendar();
				cal.set(Calendar.MILLISECOND, cal.get(Calendar.MILLISECOND) + 1);
				System.out.println(toDateString(cal));
			
		}*/



}
