package com.fire.common.cock.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
	private static Connection con;
	private static String url;
	private static String user;
	private static String password;

	static {
		Properties pr = new Properties();
		try {
			pr.load(DBUtil.class.getClassLoader().getResourceAsStream(
					"cock/toService"));
			Class.forName(pr.getProperty("DriverClass"));
			url = pr.getProperty("url");
			user = pr.getProperty("user");
			password = pr.getProperty("password");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static PreparedStatement getPS(String sql) {
		try {
			con = DriverManager.getConnection(url, user, password);
			return con.prepareStatement(sql);
		} catch (SQLException e) {
		}
		return null;
	}

	public static void closeCon() {
		if (con != null)
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
}
