package com.elsa.rpc.provider.service;

import com.elsa.rpc.api.IEmailService;
import com.elsa.rpc.pojo.Email;
import com.elsa.rpc.provider.ServerBootstrapApplication;
import com.elsa.rpc.provider.anno.RpcService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName : IEmailServiceImpl  //类名
 * @Author : elsa //作者
 */
@RpcService
@Service
public class IEmailServiceImpl implements IEmailService {
    Map<Object, Email> mailMap = new HashMap();

    @Override
    public Email getById(int id) {
        if (mailMap.size() == 0) {
            Email email1 = new Email();
            email1.setId(1);
            email1.setEmail("张三email"+ ServerBootstrapApplication.port);
            Email email2 = new Email();
            email2.setId(1);
            email2.setEmail("李四email"+ ServerBootstrapApplication.port);

            mailMap.put(email1.getId(), email1);
            mailMap.put(email2.getId(), email2);
        }
        return mailMap.get(id);
    }
}

