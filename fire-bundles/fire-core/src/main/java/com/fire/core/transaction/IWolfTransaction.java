package com.fire.core.transaction;

/**
 * WOLF的事务
 * 
 * @author Administrator
 *
 */
public interface IWolfTransaction {

	/**
	 * 需要连接数据库操作的工程，将可以获取到一个事务实例
	 * 
	 * @return
	 */
	WolfTransactionTemplate getWolfTransactionTemplate();

}
