package com.huihuang.service;

import com.huihuang.model.User;
import core.annotation.MyService;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@MyService
public class TestServiceImpl implements TestService {

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private static final Map<String, User> userMap = new HashMap<>();
    static {
        User user = new User();
        user.setName("zhangsan");
        user.setId("1");
        user.setAge(11);
        User user1 = new User();
        user1.setName("lisi");
        user1.setId("2");
        user1.setAge(22);
        User user2 = new User();
        user2.setName("wangwu");
        user2.setId("3");
        user2.setAge(33);
        userMap.put(user.getName(), user);
        userMap.put(user1.getName(), user1);
        userMap.put(user2.getName(), user2);
    }

    @Override
    public void printParam(String param) {
        logger.info("接收到的参数为： "+param);
    }

    @Override
    public User getUser(String name) {
        return userMap.get(name);
    }
}
