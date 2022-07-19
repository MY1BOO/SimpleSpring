package com.qj.demo.action;

import com.qj.demo.service.IModifyService;
import com.qj.demo.service.IQueryService;
import com.qj.spring.framework.annotation.Autowired;
import com.qj.spring.framework.annotation.Controller;
import com.qj.spring.framework.annotation.RequestMapping;
import com.qj.spring.framework.annotation.RequestParam;
import com.qj.spring.framework.webmvc.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@Controller
@RequestMapping("/web")
public class MyAction {

	@Autowired
	IQueryService queryService;
	@Autowired
	IModifyService modifyService;

	@RequestMapping("/query.json")
	public ModelAndView query(HttpServletRequest request, HttpServletResponse response,
							  @RequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}
	
	@RequestMapping("/add*.json")
	public ModelAndView add(HttpServletRequest request,HttpServletResponse response,
			   @RequestParam("name") String name,@RequestParam("addr") String addr){

		String result = null;
		try {
			result = modifyService.add(name,addr);
		} catch (Exception e) {
			Map<String,Object> model = new HashMap<String,Object>();
			model.put("detail", "500 Exception Detail");
			model.put("stackTrace", Arrays.toString(e.getStackTrace()));
			e.printStackTrace();
			return new ModelAndView("500", model);
		}
		return out(response,result);
	}
	
	@RequestMapping("/remove.json")
	public ModelAndView remove(HttpServletRequest request,HttpServletResponse response,
		   @RequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}
	
	@RequestMapping("/edit.json")
	public ModelAndView edit(HttpServletRequest request,HttpServletResponse response,
			@RequestParam("id") Integer id,
			@RequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}
	
	
	
	private ModelAndView out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
