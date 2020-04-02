package com.fire.core.injection.controller.model;

import com.fire.core.annotation.RequestMapping;
import com.fire.core.thread.WolfThread;
import com.fire.core.thread.WolfWebContext;
import com.fire.core.transaction.WolfTransactionCallback;
import com.fire.core.transaction.WolfTransactionTemplate;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * 控制类方法实体封装
 * 
 * @author Administrator
 *
 */
public class ControllerMethod {

	/**
	 * 请求路径
	 */
	private Path path;

	/**
	 * 请求方法体
	 */
	private Method method;

	/**
	 * 实例
	 */
	private Object instance;

	/**
	 * requestMapping请求接口路径
	 */
	private RequestMapping requestMapping;

	private WolfTransactionTemplate transactionTemplate;

	public void setTransactionTemplate(
			WolfTransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	public ControllerMethod(Path path, Method method, Object instance) {
		super();
		this.path = path;
		this.method = method;
		this.instance = instance;
	}

	public Object invoke(Object... args) throws Throwable {
		if (transactionTemplate != null) {
			return transactionTemplate
					.execute(new WolfTransactionCallback<Object>() {

						@Override
						public Object doInTransaction(
								TransactionStatus status) {
							try {
								// 如果使用自定义事务通常不会进入这里
								return method.invoke(instance, args);
							} catch (Throwable t) {
								status.setRollbackOnly();
								Throwable unthrowable = ExceptionUtil
										.unwrapThrowable(t);
								if (unthrowable != null) {
									if (unthrowable instanceof PersistenceException
											|| unthrowable instanceof DataAccessException) {
										throw new RuntimeException(unthrowable);
									}
								}
								return unthrowable;
							}
						}

						@Override
						public Object wolfDoInTransaction(
								TransactionStatus status)
								throws IllegalAccessException,
								IllegalArgumentException,
								InvocationTargetException, SQLException {
							// 控制层进入事务管理时，对当前线程上下文进行事务确认,
							// 控制层开启的事务必须让其下所有service代理都进行当前事务进行事务操作
							WolfWebContext context = WolfThread
									.currentWolfcontext();
							try {
								context.setSqlSessionTransactional(true);
								return method.invoke(instance, args);
							} finally {
								context.setSqlSessionTransactional(false);
							}
						}

					});
		} else {
			try {
				Object result = method.invoke(instance, args);
				return result;
			} catch (Throwable t) {
				Throwable unthrowable = ExceptionUtil.unwrapThrowable(t);
				if (unthrowable != null) {
					if (unthrowable instanceof PersistenceException
							|| unthrowable instanceof DataAccessException) {
						throw new RuntimeException(unthrowable);
					}
					throw unthrowable;
				}
				throw new RuntimeException(" null pointer dereference ");
			}
		}
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object getInstance() {
		return instance;
	}

	public void setInstance(Object instance) {
		this.instance = instance;
	}

	public RequestMapping getRequestMapping() {
		return requestMapping;
	}

	public void setRequestMapping(RequestMapping requestMapping) {
		this.requestMapping = requestMapping;
	}
}
