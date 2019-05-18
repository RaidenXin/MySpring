package com.huihuang.test;

import com.alibaba.fastjson.JSON;
import com.huihuang.model.User;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestClient {

    public static void main(String[] args){
        Map<String, Object> map = new HashMap<>();
        User user = new User();
        user.setName("zhangsan");
        user.setId("1");
        user.setAge(11);
        map.put("zhangsan", user);
        String value = JSON.toJSONString(map);
        System.out.println(value);
        List<String> list = Arrays.asList(new String[]{"1","2"});
        System.out.println(JSON.toJSONString(list));
    }

}
