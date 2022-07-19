package com.qj.spring.framework.webmvc.servlet;

import java.util.Map;

/**
 * @author qiujun
 * @create 2021-12-01-16:42
 */
public class ModelAndView {
    private String viewName;
    private Map<String,?> model;

    public ModelAndView(String viewName, Map<String, Object> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }


    public String getViewName() {
        return viewName;
    }

    public Map<String,?> getModel() {
        return model;
    }
}
