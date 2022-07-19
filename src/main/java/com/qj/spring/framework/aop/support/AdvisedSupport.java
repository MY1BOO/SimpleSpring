package com.qj.spring.framework.aop.support;

import com.qj.spring.framework.aop.aspect.Advice;
import com.qj.spring.framework.aop.config.AopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qiujun
 * @create 2021-12-01-16:44
 */
public class AdvisedSupport {
    private AopConfig config;
    private Class targetClass;
    private Object target;
    private Pattern pointCutClassPattern;
    private Map<Method,Map<String, Advice>> methodCache;

    public AdvisedSupport(AopConfig config) {
        this.config = config;
        //对配置文件中的特殊字符进行转义
        String pointCut = config.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");

        // pointCut=public .* com.gupaoedu.vip.demo.service..*Service..*(.*)
        String pointCutForClassRegex = pointCut.substring(0,pointCut.lastIndexOf("\\(") - 4);
        //提取class的全名
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    public void weaving() {

        try {
            //开始映射目标类方法和通知的关系
            methodCache = new HashMap<Method, Map<String, Advice>>();

            //开始匹配目标类的方法
            Pattern pointCutPattern = Pattern.compile(config.getPointCut());

            //先把要织入的切面的方法缓存起来
            Class aspectClass = Class.forName(this.config.getAspectClass());
            Map<String,Method> aspectMethods = new HashMap<String, Method>();
            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(),method);
            }

            //扫描目标类的所有的方法
            for (Method method : this.targetClass.getMethods()) {
                //包括了修饰符、返回值、方法名、形参列表
                String methodString = method.toString();
                //把异常去掉
                if(methodString.contains("throws")){
                    methodString = methodString.substring(0,methodString.lastIndexOf("throws")).trim();
                }

                Matcher matcher = pointCutPattern.matcher(methodString);
                if(matcher.matches()){
                    Map<String,Advice> advices = new HashMap<String, Advice>();

                    //前置通知
                    if(!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))){
                        advices.put("before",
                                new Advice(aspectClass.newInstance(),aspectMethods.get(config.getAspectBefore())));
                    }

                    //后置通知
                    if(!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))){
                        advices.put("after",
                                new Advice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfter())));
                    }

                    //异常通知
                    if(!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))){
                        advices.put("afterThrowing",
                                new Advice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfterThrow())));
                    }

                    methodCache.put(method,advices);
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Map<String,Advice> getAdvices(Method method, Class targetClass) throws Exception {
        Map<String,Advice> cached = methodCache.get(method);
        //有可能是代理以后的方法
        if(null == cached){
            Method m = targetClass.getMethod(method.getName(),method.getParameterTypes());
            cached = methodCache.get(m);
            this.methodCache.put(m,cached);
        }
        return cached;
    }

}
