package com.fire.common.cock.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PWDEncry {
	/** MD5加密算法 **/
	public static byte[] MD5Encode(byte[] obj) {

		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");

			md5.update(obj);
			return md5.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * SHA 加密算法
	 * **/
	public static byte[] SHAEncode(byte[] obj) {

		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("SHA");

			md5.update(obj);
			return md5.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static void main(String[] args) {
		String str = new String(SHAEncode("admin".getBytes()));
		String str1 = new String(SHAEncode("admin".getBytes()));
		System.out.println(str.equals(str1));
	}
}
