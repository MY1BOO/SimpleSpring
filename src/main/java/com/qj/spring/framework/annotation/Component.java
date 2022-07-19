package com.qj.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @author qiujun
 * @create 2021-12-01-16:26
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    String value() default "";
}
