package com.act.demo.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author Administrator
 *
 */
@Controller
public class IndexController {

	static final Logger LOGGER = Logger.getLogger(IndexController.class);

	@RequestMapping({"/", "index"})
    public String index() {
    	return "index";
    }

}
