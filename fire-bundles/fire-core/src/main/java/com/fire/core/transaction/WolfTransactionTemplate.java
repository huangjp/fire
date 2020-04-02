package com.fire.core.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.*;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * Template class that simplifies programmatic transaction demarcation and
 * transaction exception handling.
 *
 * <p>
 * The central method is {@link #execute}, supporting transactional code that
 * implements the {@link TransactionCallback} interface. This template handles
 * the transaction lifecycle and possible exceptions such that neither the
 * TransactionCallback implementation nor the calling code needs to explicitly
 * handle transactions.
 *
 * <p>
 * Typical usage: Allows for writing low-level data access objects that use
 * resources such as JDBC DataSources but are not transaction-aware themselves.
 * Instead, they can implicitly participate in transactions handled by
 * higher-level application services utilizing this class, making calls to the
 * low-level services via an inner-class callback object.
 *
 * <p>
 * Can be used within a service implementation via direct instantiation with a
 * transaction manager reference, or get prepared in an application context and
 * passed to services as bean reference. Note: The transaction manager should
 * always be configured as bean in the application context: in the first case
 * given to the service directly, in the second case given to the prepared
 * template.
 *
 * <p>
 * Supports setting the propagation behavior and the isolation level by name,
 * for convenient configuration in context definitions.
 *
 * @author Juergen Hoeller
 * @since 17.03.2003
 * @see #execute
 * @see #setTransactionManager
 * @see org.springframework.transaction.PlatformTransactionManager
 */
public class WolfTransactionTemplate extends DefaultTransactionDefinition
		implements TransactionOperations, InitializingBean {

	private static final long serialVersionUID = 3475544185590346223L;

	/** Logger available to subclasses */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private PlatformTransactionManager transactionManager;

	/**
	 * Construct a new TransactionTemplate for bean usage.
	 * <p>
	 * Note: The PlatformTransactionManager needs to be set before any
	 * <code>execute</code> calls.
	 * 
	 * @see #setTransactionManager
	 */
	public WolfTransactionTemplate() {
	}

	/**
	 * Construct a new TransactionTemplate using the given transaction manager.
	 * 
	 * @param transactionManager
	 *            the transaction management strategy to be used
	 */
	// public WolfTransactionTemplate(PlatformTransactionManager
	// transactionManager) {
	// this.transactionManager = transactionManager;
	// }

	public WolfTransactionTemplate(
			PlatformTransactionManager transactionManager) {
		super();
		this.transactionManager = transactionManager;
	}

	/**
	 * Construct a new TransactionTemplate using the given transaction manager,
	 * taking its default settings from the given transaction definition.
	 * 
	 * @param transactionManager
	 *            the transaction management strategy to be used
	 * @param transactionDefinition
	 *            the transaction definition to copy the default settings from.
	 *            Local properties can still be set to change values.
	 */
	public WolfTransactionTemplate(
			PlatformTransactionManager transactionManager,
			TransactionDefinition transactionDefinition) {
		super(transactionDefinition);
		this.transactionManager = transactionManager;
	}

	/**
	 * Set the transaction management strategy to be used.
	 */
	public void setTransactionManager(
			PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Return the transaction management strategy to be used.
	 */
	public PlatformTransactionManager getTransactionManager() {
		return this.transactionManager;
	}

	@Override
	public void afterPropertiesSet() {
		if (this.transactionManager == null) {
			throw new IllegalArgumentException(
					"Property 'transactionManager' is required");
		}
	}

	@Override
	public <T> T execute(TransactionCallback<T> action)
			throws TransactionException {
		if (this.transactionManager instanceof CallbackPreferringPlatformTransactionManager) {
			return ((CallbackPreferringPlatformTransactionManager) this.transactionManager)
					.execute(this, action);
		} else {
			TransactionStatus status = this.transactionManager
					.getTransaction(this);
			T result;
			try {
				WolfTransactionCallback<T> wtc = (WolfTransactionCallback<T>) action;
				result = wtc.wolfDoInTransaction(status);
			} catch (RuntimeException ex) {
				// Transactional code threw application exception -> rollback
				rollbackOnException(status, ex);
				throw ex;
			} catch (Error err) {
				// Transactional code threw error -> rollback
				rollbackOnException(status, err);
				throw err;
			} catch (Exception ex) {
				// Transactional code threw unexpected exception -> rollback
				rollbackOnException(status, ex);
				throw new UndeclaredThrowableException(ex,
						"TransactionCallback threw undeclared checked exception");
			} 
			this.transactionManager.commit(status);
			return result;
		}
	}

	/**
	 * Perform a rollback, handling rollback exceptions properly.
	 * 
	 * @param status
	 *            object representing the transaction
	 * @param ex
	 *            the thrown application exception or error
	 * @throws TransactionException
	 *             in case of a rollback error
	 */
	private void rollbackOnException(TransactionStatus status, Throwable ex)
			throws TransactionException {
		logger.debug(
				"Initiating transaction rollback on application exception", ex);
		try {
			this.transactionManager.rollback(status);
		} catch (TransactionSystemException ex2) {
			logger.error(
					"Application exception overridden by rollback exception",
					ex);
			ex2.initApplicationException(ex);
			throw ex2;
		} catch (RuntimeException ex2) {
			logger.error(
					"Application exception overridden by rollback exception",
					ex);
			throw ex2;
		} catch (Error err) {
			logger.error("Application exception overridden by rollback error",
					ex);
			throw err;
		}
	}

}
