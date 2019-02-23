package com.huihuang.aspect;

import aspect.Aspect;
import com.sun.org.apache.xpath.internal.operations.String;
import core.annotation.*;

import java.util.logging.Logger;

@MyComponent
@MyAspect
public class TestAspect implements Aspect {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    @MyPointcut(values = {"com.huihuang"})
    public void aspect(){
    }

    @MyBefore(MethodNames = {"printParam", "myTest"})
    public void before(){
        logger.info("before 开始干活了！");
    }

    @MyAfter(MethodNames = {"printParam", "myTest"})
    public void after(){
        logger.info("after 打完收工了！");
    }
}
