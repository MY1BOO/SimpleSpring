package com.qj.spring.framework.webmvc.servlet;

import com.qj.spring.framework.annotation.Controller;
import com.qj.spring.framework.annotation.RequestMapping;
import com.qj.spring.framework.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qiujun
 * @create 2021-12-01-16:41
 */
public class DispatcherServlet extends HttpServlet {
    private List<HandlerMapping> handlerMappings = new ArrayList<HandlerMapping>();

    private Map<HandlerMapping,HandlerAdapter> handlerAdapters = new HashMap<HandlerMapping, HandlerAdapter>();

    private ViewResolver viewResolver;

    private ApplicationContext context;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("detail", "500 Exception Detail");
            model.put("stackTrace", Arrays.toString(e.getStackTrace()));
            try {
                processDispatchResult(req,resp,new ModelAndView("500", model));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        //1、根据URL拿到对应的HandlerMapping对象
        HandlerMapping handler = getHandler(req);
        if(null == handler){
            processDispatchResult(req,resp,new ModelAndView("404"));
            return;
        }
        //2、根据HandlerMapping获得一个HandlerAdapter
        HandlerAdapter handlerAdapter = getHandlerAdapter(handler);
        //3、根据HandlerAdapter拿到一个ModelAndView
        ModelAndView mv = handlerAdapter.handle(req, resp, handler);
        //4、根据ViewResolver根据ModelAndView去拿到View
        processDispatchResult(req,resp,mv);

    }

    private HandlerMapping getHandler(HttpServletRequest req){
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        for (HandlerMapping handlerMapping : this.handlerMappings) {
            Matcher matcher = handlerMapping.getPattern().matcher(url);
            if(!matcher.matches()){continue;}
            return handlerMapping;
        }

        return null;
    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handler){
        if(this.handlerAdapters.isEmpty()){return null;}
        return this.handlerAdapters.get(handler);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, ModelAndView mv) throws Exception{
        if(null == mv){ return; }
        View view = viewResolver.resolveViewName(mv.getViewName());
        view.render(mv.getModel(),req,resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        context = new ApplicationContext(config.getInitParameter("contextConfigLocation"));
        //初始化三大组件
        initStrategies(context);
        System.out.println("GP Spring framework is init.");
    }

    private void initStrategies(ApplicationContext context){
        //初始化handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
        //初始化视图转换器
        initViewResolvers(context);
    }

    private void initHandlerMappings(ApplicationContext context){
        if(context.getBeanDefinitionCount() == 0){ return; }
        String[] beanDefinitionNames = context.getBeanDefinitionNames();
        //遍历IOC容器中的所有bean，找到controller
        for(String beanName : beanDefinitionNames){
            Object bean = context.getBean(beanName);
            Class<?> clazz = bean.getClass();
            if(!clazz.isAnnotationPresent(Controller.class)){ continue; }
            //获取controller类上的总的url
            String baseUrl = "";
            if(clazz.isAnnotationPresent(RequestMapping.class)){
                baseUrl = clazz.getAnnotation(RequestMapping.class).value();
            }
            //获取每一个不同url的方法的封装
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(RequestMapping.class)){continue;}
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                //   //demo//query
                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                handlerMappings.add(new HandlerMapping(pattern,bean,method));
                System.out.println("Mapped " + regex + "," + method);

            }
        }
    }

    private void initHandlerAdapters(ApplicationContext context) {
        for (HandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapters.put(handlerMapping,new HandlerAdapter());
        }
    }

    private void initViewResolvers(ApplicationContext context){
        //模板引擎的根路径
        String tempateRoot = context.getConfig().getProperty("templateRoot");
        viewResolver = new ViewResolver(tempateRoot);
    }

}
