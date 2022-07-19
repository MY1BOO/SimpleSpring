package com.qj.spring.framework.webmvc.servlet;

import java.io.File;

/**
 * @author qiujun
 * @create 2021-12-01-16:42
 */
public class ViewResolver {
    //.jsp  .vm  .ftl   .tom
    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    private File templateRootDir;

    public ViewResolver(String tempateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(tempateRoot).getFile();
        System.out.println(templateRootPath);
        this.templateRootDir = new File(templateRootPath);
    }

    public View resolveViewName(String viewName) {
        if(null == viewName || "".equals(viewName.trim())){return null;}

        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);
        File templateFile = new File((templateRootDir.getPath() + "/" + viewName)
                .replaceAll("/+","/"));
        return new View(templateFile);
    }

}
