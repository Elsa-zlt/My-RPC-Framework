package com.elsa.rpc.consumer.msg;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * @ClassName : OutBoundMsg  //类名
 * @Author : elsa //作者
 */
public interface OutBoundMsg<P, R> {
    Future<R> sendMsg(P msg) throws IOException;
}
