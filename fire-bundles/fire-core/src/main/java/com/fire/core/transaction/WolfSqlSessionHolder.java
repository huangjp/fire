package com.fire.core.transaction;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.transaction.support.ResourceHolderSupport;

import static org.springframework.util.Assert.notNull;

/**
 * 数据库会话帮助类
 * 
 * @author Administrator
 *
 */
public final class WolfSqlSessionHolder extends ResourceHolderSupport {

	private final SqlSession sqlSession;

	private final ExecutorType executorType;

	private final PersistenceExceptionTranslator exceptionTranslator;

	/**
	 * Creates a new holder instance.
	 *
	 * @param sqlSession
	 *            the {@code SqlSession} has to be hold.
	 * @param executorType
	 *            the {@code ExecutorType} has to be hold.
	 */
	public WolfSqlSessionHolder(SqlSession sqlSession, ExecutorType executorType,
			PersistenceExceptionTranslator exceptionTranslator) {

		notNull(sqlSession, "SqlSession must not be null");
		notNull(executorType, "ExecutorType must not be null");

		this.sqlSession = sqlSession;
		this.executorType = executorType;
		this.exceptionTranslator = exceptionTranslator;
	}

	public SqlSession getSqlSession() {
		return sqlSession;
	}

	public ExecutorType getExecutorType() {
		return executorType;
	}

	public PersistenceExceptionTranslator getPersistenceExceptionTranslator() {
		return exceptionTranslator;
	}

}