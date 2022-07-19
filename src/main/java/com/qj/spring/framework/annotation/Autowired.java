package com.qj.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @author qiujun
 * @create 2021-12-01-16:19
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    String value() default "";
}
