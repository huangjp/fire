package com.fire.core.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Exception thrown when attempting to retrieve the result of a task that
 * aborted by throwing an exception. This exception can be inspected using the
 * {@link #getCause()} method.
 *
 * @see Future
 * @since 1.5
 * @author Doug Lea
 */
public class WolfExecutionException extends ExecutionException {
	private static final long serialVersionUID = 7830266012832686185L;

	/**
	 * Constructs an {@code ExecutionException} with no detail message. The
	 * cause is not initialized, and may subsequently be initialized by a call
	 * to {@link #initCause(Throwable) initCause}.
	 */
	protected WolfExecutionException() {
	}

	/**
	 * Constructs an {@code ExecutionException} with the specified detail
	 * message. The cause is not initialized, and may subsequently be
	 * initialized by a call to {@link #initCause(Throwable) initCause}.
	 *
	 * @param message
	 *            the detail message
	 */
	protected WolfExecutionException(String message) {
		super(message);
	}

	/**
	 * Constructs an {@code ExecutionException} with the specified detail
	 * message and cause.
	 *
	 * @param message
	 *            the detail message
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method)
	 */
	public WolfExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an {@code ExecutionException} with the specified cause. The
	 * detail message is set to {@code (cause == null ? null :
	 * cause.toString())} (which typically contains the class and detail message
	 * of {@code cause}).
	 *
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method)
	 */
	public WolfExecutionException(Throwable cause) {
		super(cause);
	}
}
