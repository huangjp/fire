package com.fire.core.transaction;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

/**
 * 事务回调
 * 
 * @author Administrator
 *
 * @param <T>
 */
public interface WolfTransactionCallback<T> extends TransactionCallback<T> {

	/**
	 * 事务回调
	 * 
	 * @param status
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws SQLException
	 */
	T wolfDoInTransaction(TransactionStatus status)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException;

}