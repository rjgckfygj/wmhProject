package com.provider2.service;

import org.service.HelloService;

public class TestService implements HelloService {
    @Override
    public Object hello(String arg) {
        return arg + "provider2";
    }
}
