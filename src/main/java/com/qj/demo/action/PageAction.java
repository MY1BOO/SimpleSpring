package com.qj.demo.action;

import com.qj.demo.service.IQueryService;
import com.qj.spring.framework.annotation.Autowired;
import com.qj.spring.framework.annotation.Controller;
import com.qj.spring.framework.annotation.RequestMapping;
import com.qj.spring.framework.annotation.RequestParam;
import com.qj.spring.framework.webmvc.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@Controller
@RequestMapping("/")
public class PageAction {

    @Autowired
    IQueryService queryService;

    @RequestMapping("/first.html")
    public ModelAndView query(@RequestParam("teacher") String teacher){
        String result = queryService.query(teacher);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new ModelAndView("first.html",model);
    }

}
