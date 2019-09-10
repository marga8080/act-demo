package com.act.demo.controller;

import com.act.demo.config.ResponseResult;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

public class BaseController {

	protected final Log logger = LogFactory.get();
	
	public ResponseResult result(boolean success, String message, Object data) {
		ResponseResult result = new ResponseResult();
		result.setSuccess(success);
		result.setMessage(message);
		result.setData(data);
		return result;
	}
	
	protected ResponseResult success() {
		return success(null);
	}
	
	protected ResponseResult success(Object data) {
		return success("成功", data);
	}
	
	protected ResponseResult success(String message, Object data) {
		return result(true, message, data);
	}
	
	protected ResponseResult error(String message, Object data) {
		return result(false, message, data);
	}
	
	protected ResponseResult error(String message) {
		return error(message);
	}
}
