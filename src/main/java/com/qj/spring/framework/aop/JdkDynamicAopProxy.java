package com.qj.spring.framework.aop;

import com.qj.spring.framework.aop.aspect.Advice;
import com.qj.spring.framework.aop.support.AdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author qiujun
 * @create 2021-12-01-16:44
 */
public class JdkDynamicAopProxy implements InvocationHandler {
    private AdvisedSupport config;
    public JdkDynamicAopProxy(AdvisedSupport config) {
        this.config = config;
    }

    public Object getProxy() {
        //第一个参数，生成的新类用什么方式加载
        //第二个参数，生成的新类要实现哪个接口
        //第三个参数，通过反射触发调用invoke
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),
                this.config.getTargetClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String,Advice> advices = this.config.getAdvices(method,this.config.getTargetClass());
        Object returnValue;

        //织入前置通知
        invokeAdivce(advices.get("before"));

        try {
            returnValue = method.invoke(this.config.getTarget(), args);
        }catch (Exception e){
            invokeAdivce(advices.get("afterThrowing"));
            e.printStackTrace();
            throw e;
        }

        invokeAdivce(advices.get("after"));
        return returnValue;
    }

    private void invokeAdivce(Advice advice) {
        try {
            advice.getAdviceMethod().invoke(advice.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
