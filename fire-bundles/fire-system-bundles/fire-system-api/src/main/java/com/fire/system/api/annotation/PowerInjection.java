package com.fire.system.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限注入注解，标记此注解的类会成为权限目录，标记此注解的方法会成功权限接口，会被权限模块监听方法的调用
 * Created from huangjp on 2020/3/31 0031-下午 20:38
 *
 * @version 1.0
 * @email 262404150@qq.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PowerInjection {

    /**
     * 权限码，必填属性
     *
     * @return
     */
    String code();

    /**
     * 权限名称
     *
     * @return
     */
    String name() default "";

    /**
     * 权限描述
     *
     * @return
     */
    String desc() default "";
}
