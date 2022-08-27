package com.elsa.rpc.consumer.proxy;

import com.alibaba.fastjson.JSON;
import com.elsa.rpc.common.RpcRequest;
import com.elsa.rpc.common.RpcResponse;
import com.elsa.rpc.consumer.msg.OutBoundMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * @ClassName : RpcClientProxy  //类名
 * @Author : elsa //作者
 */
@Service
public class RpcClientProxy {
    @Autowired
    private OutBoundMsg<RpcRequest,RpcResponse> outBoundMsg;

    public Object createProxy(Class serviceClass) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{serviceClass}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //1.封装request请求对象
                        RpcRequest rpcRequest = new RpcRequest();
                        rpcRequest.setRequestId(UUID.randomUUID().toString());
                        rpcRequest.setClassName(method.getDeclaringClass().getName());
                        rpcRequest.setMethodName(method.getName());
                        rpcRequest.setParameterTypes(method.getParameterTypes());
                        rpcRequest.setParameters(args);
                        try {
                            //3.发送消息
                            Future<RpcResponse> rpcResponseFuture = outBoundMsg.sendMsg(rpcRequest);
                            RpcResponse rpcResponse = rpcResponseFuture.get();
                            if (rpcResponse.getError() != null) {
                                throw new RuntimeException(rpcResponse.getError());
                            }
                            //4.返回结果
                            Object result = rpcResponse.getResult();
                            return JSON.parseObject(result.toString(), method.getReturnType());
                        } catch (Exception e) {
                            throw e;
                        }
                    }
                });
    }
}

