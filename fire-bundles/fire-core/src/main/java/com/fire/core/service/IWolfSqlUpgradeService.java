package com.fire.core.service;

import com.fire.core.thread.Callback;

/**
 * @author wind 数据库升级脚本管理服务
 */
public interface IWolfSqlUpgradeService extends IService {

	/**
	 * 执行数据库升级操作
	 * 
	 * @param backPath
	 *            备份目录，可以不指定，则由系统自动备份到/conf/back/目录下 重新实现请注意后面需要带上"/"
	 * 
	 * @param sqlFilePath
	 *            sql文件所在目录，可以不指定，则由系统在/conf/upgrade/{升级版本}/目录下自动查找
	 *            查找规则是：在指定目录下查找【{数据库名称}.sql】后缀文件进行升级操作 重新实现请注意后面需要带上"/"
	 */
	void upgrade(Callback callback, String backPath, String sqlFilePath);

}
