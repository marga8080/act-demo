package com.act.demo.config;

import java.io.Serializable;

public class ResponseResult implements Serializable {
	
    private static final long serialVersionUID = 1768211517765965398L;

    private boolean success;

    private String message;

    private Object data;

    public ResponseResult() {
    }

    public ResponseResult(boolean success) {
        this.success = success;
    }

    public ResponseResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ResponseResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
