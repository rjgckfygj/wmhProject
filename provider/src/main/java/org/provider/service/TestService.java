package org.provider.service;

import org.annotation.RpcService;
import org.springframework.stereotype.Component;
import org.service.HelloService;

@Component
@RpcService
public class TestService implements  HelloService{
    @Override
    public Object hello(String arg) {
        return arg + "provider1";
    }
}
