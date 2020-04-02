package com.fire.common.exception;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import java.util.Dictionary;

public enum CommonException implements ManagedService, WolfExceptionHelper  {
	
	/**
	 * 初始化
	 */
	INIT,
	
	/**
	 * 参数格式错误
	 */
	PARAMETER_FORMAT_ERROR,

	/**
	 * JSON 格式化异常
	 */
	JSON_FORMAT_ERROR,
	
	/**
	 * JSON 解析异常
	 */
	JSON_PARSE_ERROR,
	
	/**
	 * JSON 解析时出现未知类型
	 */
	JSON_PARSE_UNKNOWN_TYPE_ERROR,
	
	/**
	 * JSON 解析InputStream时出现 IO 异常
	 */
	JSON_PARSE_INPUTSTREAM_ERROR,
	
	/**
	 * MAP转实体异常
	 */
	MAP_CONVER_ENTITY_ERROR,
	
	/**
	 * 日期型字符串格式错误
	 */
	DATE_FORMAT_ERROR,
	
	/**
	 * 日期解析错误
	 */
	DATE_PARSE_ERROR;
	
	private String value;

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {

		this.updatedByUTF8(properties, CommonException::values,
				(exception, value) -> ((CommonException) exception).value = value);
	}

	@Override
	public String value() {
		return value;
	}
	
}
