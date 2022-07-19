package com.qj.spring.framework.beans.support;

import com.qj.spring.framework.annotation.Component;
import com.qj.spring.framework.annotation.Controller;
import com.qj.spring.framework.annotation.Service;
import com.qj.spring.framework.beans.config.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author qiujun
 * @create 2021-12-01-16:33
 */
public class BeanDefinitionReader {

    private Properties contextConfig = new Properties();

    //需要注册到IoC容器的class
    private List<String> registryBeanClasses = new ArrayList<String>();

    public BeanDefinitionReader(String[] configLocations) {
        //1、读取配置文件
        doLoadConfig(configLocations[0]);

        //2、扫描相关的类
            doScanner(contextConfig.getProperty("scanPackage"));
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.replaceAll("classpath:",""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doScanner(String scanPackage) {
        try {
            URL url = BeanDefinitionReader.class.getResource("/" + scanPackage.replaceAll("\\.","/") );
            String decode = URLDecoder.decode(url.getPath(), "utf-8");
            URL realURL = new URL("file:/" + decode);
            File classPath = new File(realURL.getFile());
            for(File file : classPath.listFiles()){
                if(file.isDirectory()){
                    doScanner(scanPackage+"."+file.getName());
                } else {
                    //classPath下除了有.class文件，还有 .xml  .properties .yml
                    //取反，减少代码嵌套
                    //代码嵌套超过三层，就要拉平
                    if(!file.getName().endsWith(".class")){ continue; }

                    String className = scanPackage + "." + file.getName().replace(".class", "");
    //            可以在实例化阶段通过调用Class.forName(className)拿到Class对象
                    //从而可以通过反射去创建实例
                    registryBeanClasses.add(className);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public List<BeanDefinition> loadBeanDefinitions() {
        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        for(String className : registryBeanClasses){
            try {
                Class<?> beanClass = Class.forName(className);
                //判断自身是不是一个接口
                if(beanClass.isInterface()){ continue; }
                if(beanClass.isAnnotationPresent(Component.class)||beanClass.isAnnotationPresent(Controller.class)){
                    beanDefinitions.add(new BeanDefinition(toLowerFirstCase(beanClass.getSimpleName()),beanClass.getName()));
                } else if(beanClass.isAnnotationPresent(Service.class)){
                    Class<?>[] interfaces = beanClass.getInterfaces();
                    beanDefinitions.add(new BeanDefinition(interfaces[0].getSimpleName(),beanClass.getName()));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return beanDefinitions;
    }

    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        //因为大写字符的ASCII码和小写字母的ASCII正好相差32
        //而且大写字母ASCII码要小于小写字母的ASCII码
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig() {
        return this.contextConfig;
    }
}
