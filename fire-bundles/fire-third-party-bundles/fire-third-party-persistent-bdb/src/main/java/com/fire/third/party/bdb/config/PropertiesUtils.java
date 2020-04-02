package com.fire.third.party.bdb.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {
	private static final Properties global = loadProperties("global.properties");
	private static final Properties je = loadProperties("je.properties");
    public static String globalBDBBaseHome = PropertiesUtils.getproperties("BDBBaseHome",System.getProperty("java.io.tmpdir"));
	public static String getdefaultTransportCharset()
	{
		return global.getProperty("defaultTransportCharset");
	}
	
	public static String getproperties(String key,String defaultValue)
	{
		String ret = global.getProperty(key);
		return  StringUtils.isBlank(ret) ? defaultValue :ret;
	}
	
	public static Properties getJeProperties(){
		Properties properties = new Properties();
		properties.putAll(je);
		return properties;
	}

	private static Properties loadProperties(String resources) {

		// 先从当前运行目录获取配置文件，如果不存在则从类加载器中获取
		File file = new File(System.getProperty("user.dir")  + File.separator + resources);
		InputStream inputstream = null;
		if(file.exists()) {
			try
			{
				inputstream = FileUtils.openInputStream(file);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		} else {
			inputstream = PropertiesUtils.class.getClassLoader().getResourceAsStream(resources);
		}

		Properties properties = new Properties();

		try {

			// 加载配置文件

			properties.load(inputstream);

			System.out.println(properties);
			return properties;

		} catch (IOException e) {

			throw new RuntimeException(e);

		} finally {

			try {

				inputstream.close();

			} catch (IOException e) {

				throw new RuntimeException(e);

			}

		}
	}

}
