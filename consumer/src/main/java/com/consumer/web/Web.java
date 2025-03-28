package com.consumer.web;

import org.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.service.HelloService;
@RestController
@RequestMapping
public class Web {
    @RpcReference
    HelloService helloService;

    @GetMapping
    public Object hello(String arg){
        return helloService.hello(arg);
    }
}
