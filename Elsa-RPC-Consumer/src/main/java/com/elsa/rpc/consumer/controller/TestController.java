package com.elsa.rpc.consumer.controller;

import com.elsa.rpc.api.IEmailService;
import com.elsa.rpc.api.IUserService;
import com.elsa.rpc.consumer.proxy.RpcClientProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName : TestController  //类名
 * @Author : elsa //作者
 */
@RestController
public class TestController {

    @Autowired
    private RpcClientProxy rpcClientProxy;

    @RequestMapping(value = "test/rpc")
    public Object getUser(int id){
        IUserService userService = (IUserService) rpcClientProxy.createProxy(IUserService.class);
        return userService.getById(id);
    }

    @RequestMapping(value = "test/email")
    public Object getEmail(int id){
        IEmailService userService = (IEmailService) rpcClientProxy.createProxy(IEmailService.class);
        return userService.getById(id);
    }

}
