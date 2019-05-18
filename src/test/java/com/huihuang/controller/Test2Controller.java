package com.huihuang.controller;

import com.huihuang.model.User;
import com.huihuang.service.TestService;
import core.annotation.MyAutowired;
import core.annotation.MyController;
import core.annotation.MyRequestMapping;
import core.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MyController
@MyRequestMapping("test2")
public class Test2Controller {

    @MyAutowired
    private TestService testService;

    @MyRequestMapping("test")
    public String myTest(@MyRequestParam("param")String param){
        return "Test2Controller:the param you send is :"+param;
    }

    @MyRequestMapping("getUser")
    public User getUser(@MyRequestParam("name")String name){
        return testService.getUser(name);
    }

    @MyRequestMapping("getUser2")
    public User getUser2(@MyRequestParam("user")User user){
        return testService.getUser(user.getName());
    }
}
