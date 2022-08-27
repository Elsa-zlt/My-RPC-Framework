package com.elsa.rpc.consumer.client;

public interface LoadBalance<T> {
    public T pickOne();
}
