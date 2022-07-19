package com.qj.spring.framework.webmvc.servlet;

import com.qj.spring.framework.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * @author qiujun
 * @create 2021-12-01-16:41
 */
public class HandlerAdapter {
    //
    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handler) throws InvocationTargetException, IllegalAccessException {
        //参数匹配
        Method method=handler.getMethod();
        //获得method的形参列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //实参
        Object[] paramsValues = new Object[parameterTypes.length];
        //拿到前端url的参数映射
        Map<String,String[]> paramsMap=req.getParameterMap();
        for(int i=0;i<parameterTypes.length;i++){
            if(parameterTypes[i] == HttpServletRequest.class){
                paramsValues[i]=req;
                continue;
            }else if(parameterTypes[i] == HttpServletResponse.class) {
                paramsValues[i] = resp;
                continue;
            }else if(parameterTypes[i]==String.class){
                //获得该参数的注解列表
                Annotation[][] params = method.getParameterAnnotations();
                for (Annotation[] param : params) {
                    for (Annotation annotation : param) {
                        //若注解为RequestParam赋值
                        if(annotation instanceof RequestParam){
                            String paramsName = ((RequestParam) annotation).value();
                            paramsValues[i]= Arrays.toString(paramsMap.get(paramsName)).replaceAll("\\[|\\]","");
                        }
                    }
                }
            }
        }
        Object result = method.invoke(handler.getController(), paramsValues);

        if(result == null || result instanceof Void){return null;}
        boolean isModelAndView=method.getReturnType()==ModelAndView.class;
        if(isModelAndView){
            return (ModelAndView) result;
        }

        return null;
    }

}
