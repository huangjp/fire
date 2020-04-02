package com.fire.common.exception;

import com.fire.core.service.IService;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

public interface WolfExceptionHelper extends IService {

	public static final Logger logger = LoggerFactory.getLogger(WolfExceptionHelper.class);

	@FunctionalInterface
	interface EnumType<T extends Enum<?>> {
		T[] values();
	}

	@FunctionalInterface
	interface EnumTypeSetVal<T extends Enum<?>> {
		void setEnumValue(Enum<?> exception, String value);
	}

	@FunctionalInterface
	interface NonNull<T> {
		T nonNull();
	}

	@FunctionalInterface
	interface BeNull<T> {
		T beNull();
	}

	public String name();

	public String value();

	default <T> void requireNonNull(T t, Object... msg) {
		if (null == t) {
			StackTraceElement ste = new Throwable().getStackTrace()[1];
			throwRuntimeException(ste, msg);
		} else if (t instanceof String) {
			if (t.toString().trim().length() == 0 || "null".equals(t.toString().trim())) {
				StackTraceElement ste = new Throwable().getStackTrace()[1];
				throwRuntimeException(ste, msg);
			}
		} else if (t instanceof List) {
			if (((List<?>) t).isEmpty()) {
				StackTraceElement ste = new Throwable().getStackTrace()[1];
				throwRuntimeException(ste, msg);
			}
		} else if (t instanceof Map) {
			if (((Map<?, ?>) t).isEmpty()) {
				StackTraceElement ste = new Throwable().getStackTrace()[1];
				throwRuntimeException(ste, msg);
			}
		}
	}

	default <T> void requireBeNull(T t, Object... msg) {
		if (null != t) {
			StackTraceElement ste = new Throwable().getStackTrace()[1];
			throwRuntimeException(ste, msg);
		} else if (t instanceof String) {
			if (t.toString().trim().length() != 0 || !"null".equals(t.toString().trim())) {
				StackTraceElement ste = new Throwable().getStackTrace()[1];
				throwRuntimeException(ste, msg);
			}
		} else if (t instanceof List) {
			if (!((List<?>) t).isEmpty()) {
				StackTraceElement ste = new Throwable().getStackTrace()[1];
				throwRuntimeException(ste, msg);
			}
		} else if (t instanceof Map) {
			if (!((Map<?, ?>) t).isEmpty()) {
				StackTraceElement ste = new Throwable().getStackTrace()[1];
				throwRuntimeException(ste, msg);
			}
		}
	}

	default <T> T recordNonNull(BeNull<T> cb, NonNull<T> callback, T t, Object... msg) {
		boolean bool = false;
		if (null == t) {
			StackTraceElement ste = new Throwable().getStackTrace()[1];
			bool = recordException(ste, msg);
		} else if (t instanceof String) {
			if (t.toString().length() == 0) {
				StackTraceElement ste = new Throwable().getStackTrace()[1];
				bool = recordException(ste, msg);
			}
		} else if (t instanceof List) {
			if (((List<?>) t).isEmpty()) {
				StackTraceElement ste = new Throwable().getStackTrace()[1];
				bool = recordException(ste, msg);
			}
		} else if (t instanceof Map) {
			if (((Map<?, ?>) t).isEmpty()) {
				StackTraceElement ste = new Throwable().getStackTrace()[1];
				bool = recordException(ste, msg);
			}
		}
		if (!bool) {
			return callback.nonNull();
		}
		return cb.beNull();
	}

	default void throwRuntimeException(StackTraceElement ste, Object... msg) {
		recordException(ste, msg);
		throw new WolfException(this.name());
	}

	default void throwWeiXinException(StackTraceElement ste, Object... msg) {
		recordException(ste, msg);
		throw new WolfException();
	}

	default void throwRuntimeException(Object... msg) {
		StackTraceElement ste = new Throwable().getStackTrace()[1];
		throwRuntimeException(ste, msg);
	}

	default boolean recordException(StackTraceElement ste, Object... msg) {
		String str = this.value();
		if (Strings.isEmpty(str)) {
			str = "No configuration file configuration description information";
		}
		logger.error("File:{}({});ERROR_CODE:{};ERROR_DESC:" + str, ste.getFileName(), ste.getLineNumber(), this.name(),
				msg);
		return true;
	}

	default boolean recordException(Object... msg) {
		StackTraceElement ste = new Throwable().getStackTrace()[1];
		return recordException(ste, msg);
	}

	default String returnExceptionCode(StackTraceElement ste, Object... msg) {
		recordException(ste, msg);
		return this.name();
	}

	default String returnExceptionCode(Object... msg) {
		StackTraceElement ste = new Throwable().getStackTrace()[1];
		return returnExceptionCode(ste, msg, ste.getFileName(), ste.getMethodName(), ste.getLineNumber());
	}

	default <T extends Enum<?>> void updatedByUTF8(Dictionary<String, ?> properties, EnumType<T> enumType,
                                                   EnumTypeSetVal<T> setVal) {

		Arrays.stream(enumType.values()).forEach(t -> {
			Object obj = properties.get(t.name());
			if (obj != null) {
				try {
					String value = new String(obj.toString().getBytes("ISO-8859-1"), "UTF-8");
					setVal.setEnumValue(t, value);
				} catch (Exception e) {
					logger.error("{}", e.getCause());
				}
			}
		});
	}

	default <T extends Enum<?>> void updatedByISO88591(Dictionary<String, ?> properties, EnumType<T> enumType,
                                                       EnumTypeSetVal<T> setVal) {

		Arrays.stream(enumType.values()).forEach(t -> {
			Object obj = properties.get(t.name());
			if (obj != null) {
				setVal.setEnumValue(t, obj.toString());
			}
		});

	}
}
