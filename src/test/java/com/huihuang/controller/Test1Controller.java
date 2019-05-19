package com.huihuang.controller;

import com.huihuang.service.TestService;
import core.annotation.MyAutowired;
import core.annotation.MyController;
import core.annotation.MyRequestMapping;
import core.annotation.MyRequestParam;


@MyRequestMapping("test")
@MyController
public class Test1Controller {
//    @MyAutowired
    private TestService testService;

    @MyRequestMapping("mytest")
    public void myTest(@MyRequestParam("param") String param){
        testService.printParam(param);
    }
}
