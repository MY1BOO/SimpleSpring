package com.qj.spring.framework.context;

import com.qj.spring.framework.annotation.*;
import com.qj.spring.framework.aop.JdkDynamicAopProxy;
import com.qj.spring.framework.aop.config.AopConfig;
import com.qj.spring.framework.aop.support.AdvisedSupport;
import com.qj.spring.framework.beans.BeanWrapper;
import com.qj.spring.framework.beans.config.BeanDefinition;
import com.qj.spring.framework.beans.support.BeanDefinitionReader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author qiujun
 * @create 2021-12-01-16:33
 */
public class ApplicationContext {
    private String[]  configLocations;
    private BeanDefinitionReader reader;

    private final Map<String,BeanDefinition> beanDefinitionMap = new HashMap<String,BeanDefinition>();
    private Map<String,BeanWrapper> factoryBeanInstanceCache = new HashMap<String, BeanWrapper>();
    private AdvisedSupport advisedSupport;

    public ApplicationContext(String... configLocations) {
        this.configLocations = configLocations;

        try {
            //1、读取配置文件
            reader = new BeanDefinitionReader(configLocations);

            //2、解析配置文件，将aop的配置信息读取到advisedSupport中
            advisedSupport=instantionAopConfig();

            //3、解析配置文件，将配置信息变成BeanDefinition对象
            List<BeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

            //4、把BeanDefinition对应的实例注册到beanDefinitionMap key=beanName,value=beanDefinition对象
            doRegisterBeanDefinition(beanDefinitions);

            //5、创建实例化对象
            instantiateBean();

            //6、执行依赖注入
            populateBean();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void doRegisterBeanDefinition(List<BeanDefinition> beanDefinitions) throws Exception {
        for(BeanDefinition beanDefinition : beanDefinitions){
            if(this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exists!");
            }
            this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }
    }

    private void instantiateBean() {
        for(Map.Entry<String,BeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()){
            String beanName = beanDefinitionEntry.getKey();
            //通过反射实例化
            Object instance = doCreateBean(beanDefinitionEntry.getValue());
            if(instance!=null){
                //将创建出来的实例包装为BeanWrapper对象，放入IoC容器中
                BeanWrapper beanWrapper = new BeanWrapper(instance);
                factoryBeanInstanceCache.put(beanName,beanWrapper);
            }else {
                factoryBeanInstanceCache.put(beanName,null);
            }
        }
    }

    //创建实例化对象
    private Object doCreateBean(BeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try {
            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();

            //1、读取配置，将通知和目标类建立关系
            advisedSupport = instantionAopConfig();
            advisedSupport.setTargetClass(clazz);
            advisedSupport.setTarget(instance);
            //判断，要不要生成代理类
            if(advisedSupport.pointCutMatch()){
                advisedSupport.weaving();
                instance = new JdkDynamicAopProxy(advisedSupport).getProxy();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return instance;
    }

    private AdvisedSupport instantionAopConfig() {
        AopConfig config = new AopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));
        return new AdvisedSupport(config);
    }

    private void populateBean(){
        for(Map.Entry<String,BeanWrapper> beanWrapperEntry: factoryBeanInstanceCache.entrySet()){
            BeanWrapper beanWrapperEntryValue = beanWrapperEntry.getValue();
            doPopulateBean(beanWrapperEntryValue);
        }
    }

    //完成依赖注入
    private void doPopulateBean(BeanWrapper beanWrapper) {
        Class<?> clazz = beanWrapper.getWrapperClass();
        Object object = beanWrapper.getWrapperInstance();
        Field[] declaredFields = clazz.getDeclaredFields();
        for(Field declaredField : declaredFields){
            try {
                if(!declaredField.isAnnotationPresent(Autowired.class)){continue;}
                declaredField.setAccessible(true);
                //ByType
                if(declaredField.getType().isInterface()){
                    declaredField.set(object,factoryBeanInstanceCache.get(declaredField.getType().getSimpleName()).getWrapperInstance());
                }else{
                    String beanNameA=declaredField.getType().getSimpleName().substring(0,1).toLowerCase()+declaredField.getType().getSimpleName().substring(1);
                    declaredField.set(object,factoryBeanInstanceCache.get(beanNameA).getWrapperInstance());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    public Object getBean(Class beanClass){
        return getBean(beanClass.getName());
    }

    public Object getBean(String beanName){
        return this.factoryBeanInstanceCache.get(beanName).getWrapperInstance();
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }
}
