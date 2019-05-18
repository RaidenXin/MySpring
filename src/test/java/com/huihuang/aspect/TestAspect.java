package com.huihuang.aspect;

import core.aspect.Aspect;
import core.annotation.*;
import core.handler.ProceedingJoinPoint;

import java.util.logging.Logger;

@MyComponent
@MyAspect
public class TestAspect implements Aspect {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    @MyPointcut(values = {"com.huihuang"})
    public void aspect(){
    }

    @MyBefore(MethodNames = { "myTest"})
    public void before(){
        logger.info("before 开始干活了！");
    }

    @MyAfter(MethodNames = { "myTest"})
    public void after(){
        logger.info("after 打完收工了！");
    }

    @MyAround(MethodNames = {"printParam"})
    public Object  aroundPringLog(ProceedingJoinPoint proceedingJoinPoint){
        logger.info("around 这是环绕通知！");
        Object[] args = proceedingJoinPoint.getArgs();//得到方法执行所需的参数
        proceedingJoinPoint.proceed(args);
        logger.info("around 这是环绕通知！");
        return null;
    }
}
