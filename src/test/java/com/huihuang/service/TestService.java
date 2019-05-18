package com.huihuang.service;

import com.huihuang.model.User;

public interface TestService {
    void printParam(String param);
    User getUser(String name);
}
