package com.huihuang;

import core.MySpringApplication;
import core.annotation.MySpringBootApplication;

@MySpringBootApplication
public class App {

    public static void main(String[] args){
        MySpringApplication.run(App.class, args);
    }
}
