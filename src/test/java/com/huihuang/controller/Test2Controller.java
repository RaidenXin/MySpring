package com.huihuang.controller;

import core.annotation.MyController;
import core.annotation.MyRequestMapping;
import core.annotation.MyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MyController
@MyRequestMapping("test2")
public class Test2Controller {

    @MyRequestMapping("test")
    public void myTest(HttpServletRequest request, HttpServletResponse response, @MyRequestParam("param") String param){
        try {
            response.getWriter().write( "Test2Controller:the param you send is :"+param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
