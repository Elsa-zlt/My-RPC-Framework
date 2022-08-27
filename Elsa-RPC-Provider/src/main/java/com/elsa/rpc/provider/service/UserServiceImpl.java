package com.elsa.rpc.provider.service;

import com.elsa.rpc.api.IUserService;
import com.elsa.rpc.pojo.User;
import com.elsa.rpc.provider.ServerBootstrapApplication;
import com.elsa.rpc.provider.anno.RpcService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName : UserServiceImpl  //类名
 * @Author : elsa //作者
 */

@RpcService
@Service
public class UserServiceImpl implements IUserService {
    Map<Object, User> userMap = new HashMap();

    @Override
    public User getById(int id) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (userMap.size() == 0) {
            User user1 = new User();
            user1.setId(1);
            user1.setName("张三"+ ServerBootstrapApplication.port);
            User user2 = new User();
            user2.setId(2);
            user2.setName("李四"+ ServerBootstrapApplication.port);
            userMap.put(user1.getId(), user1);
            userMap.put(user2.getId(), user2);
        }
        return userMap.get(id);
    }
}
