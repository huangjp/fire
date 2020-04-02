package com.fire.core.version;

/**
 * 映射兼容类型,默认MappingType.DOWN向下兼容
 * @author wind
 */
public enum MappingType {
    /**
     * 接口向上兼容,YES表示哪些版本可以访问
     */
    UP_YES,
    /**
     * 接口向上兼容,NO表示哪些版本不可以访问
     */
    UP_NO,
    /**
     * 接口向下兼容
     */
    DOWN
}
