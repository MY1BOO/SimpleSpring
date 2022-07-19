package com.qj.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @author qiujun
 * @create 2021-12-01-21:52
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Qualifer {
    String value() default "";
}
