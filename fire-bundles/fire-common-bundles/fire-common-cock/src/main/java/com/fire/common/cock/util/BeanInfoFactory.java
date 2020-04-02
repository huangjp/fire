package com.fire.common.cock.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;

public interface BeanInfoFactory {
	BeanInfo getBeanInfo(Class<?> beanClass) throws IntrospectionException;
}
