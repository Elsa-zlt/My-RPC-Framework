package com.elsa.rpc.consumer.msg;

/**
 * @ClassName : InbountMsg  //类名
 * @Author : elsa //作者
 */
public interface InbountMsg<T> {
    void receive(T msg);
}
