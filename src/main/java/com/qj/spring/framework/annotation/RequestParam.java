package com.qj.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @author qiujun
 * @create 2021-12-01-16:19
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
	
	String value() default "";
	
	boolean required() default true;

}
