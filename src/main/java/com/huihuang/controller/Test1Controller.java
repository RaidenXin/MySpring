package com.huihuang.controller;

import com.huihuang.service.TestService;
import core.annotation.MyAutowired;
import core.annotation.MyController;
import core.annotation.MyRequestMapping;
import core.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MyRequestMapping("test")
@MyController
public class Test1Controller {
    @MyAutowired
    private TestService testService;

    @MyRequestMapping("test")
    public void myTest(HttpServletRequest request, HttpServletResponse response,
                       @MyRequestParam("param") String param){
        try {
            response.getWriter().write( "Test1Controller:the param you send is :"+param);
            testService.printParam(param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
