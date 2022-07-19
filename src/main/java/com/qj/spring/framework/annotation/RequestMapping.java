package com.qj.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @author qiujun
 * @create 2021-12-01-16:19
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
	String value() default "";
}
