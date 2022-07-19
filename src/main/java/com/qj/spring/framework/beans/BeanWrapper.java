package com.qj.spring.framework.beans;

/**
 * @author qiujun
 * @create 2021-12-01-16:32
 */
public class BeanWrapper {
    private Object wrapperInstance;
    private Class<?> wrapperClass;

    public BeanWrapper(Object wrapperInstance) {
        this.wrapperClass = wrapperInstance.getClass();
        this.wrapperInstance = wrapperInstance;
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public Class<?> getWrapperClass() {
        return wrapperClass;
    }
}
