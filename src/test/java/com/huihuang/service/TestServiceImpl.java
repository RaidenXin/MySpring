package com.huihuang.service;

import core.annotation.MyService;

import java.util.logging.Logger;

@MyService
public class TestServiceImpl implements TestService {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    @Override
    public void printParam(String param) {
        logger.info("接收到的参数为： "+param);
    }
}
