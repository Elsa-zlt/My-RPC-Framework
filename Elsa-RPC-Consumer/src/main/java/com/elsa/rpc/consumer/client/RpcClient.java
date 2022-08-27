package com.elsa.rpc.consumer.client;

import com.alibaba.fastjson.JSON;
import com.elsa.rpc.common.RpcRequest;
import com.elsa.rpc.common.RpcResponse;
import com.elsa.rpc.consumer.datasource.Connection;
import com.elsa.rpc.consumer.datasource.DataSouceManager;
import com.elsa.rpc.consumer.msg.InbountMsg;
import com.elsa.rpc.consumer.msg.OutBoundMsg;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * @ClassName : RpcClient  //类名
 * 客户端
 * 1.连接Netty服务端
 * 2.提供给调用者主动关闭资源的方法
 * 3.提供消息发送的方法
 * @Author : elsa //作者
 */
@Service
public class RpcClient implements DisposableBean, OutBoundMsg<RpcRequest, RpcResponse>, InbountMsg<String> {
    @Autowired
    private DataSouceManager dataSouceManager;

    private ConcurrentHashMap<String,RecordFuture> map = new ConcurrentHashMap<>();

    @Override
    public void receive(String msg) {
        try{
            //交给io线程做
            System.out.println("=====" + Thread.currentThread() + "===" + msg);
            RpcResponse rpcResponse = JSON.parseObject(msg, RpcResponse.class);
            String requestId = rpcResponse.getRequestId();
            RecordFuture remove = map.remove(requestId);
            if(remove == null){
                throw new RuntimeException("历史消息," + msg);
            }
            remove.complete(rpcResponse);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public Future<RpcResponse> sendMsg(RpcRequest msg) throws IOException {
        Connection connection  = dataSouceManager.getConnection(msg.getClassName());
        connection.getOutBoundMsg().sendMsg(JSON.toJSONString(msg));
        RecordFuture future = new RecordFuture(connection);
        map.put(msg.getRequestId(),future);
        return future;
    }

    @Override
    public void destroy() throws IOException {
        if (dataSouceManager != null) {
            dataSouceManager.close();
        }
    }

    private static class RecordFuture extends CompletableFuture<RpcResponse> {
        private long startTime;

        private long finishedTime;

        private Connection connection;

        public RecordFuture(Connection connection) {
            this.connection = connection;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public boolean complete(RpcResponse value) {
            boolean complete = super.complete(value);
            finished();
            return complete;
        }

        private void finished(){
            finishedTime = System.currentTimeMillis();

            Connection.ConnectionStatus connectionStatus = new Connection.ConnectionStatus();
            connectionStatus.setRecordTime(finishedTime);
            connectionStatus.setResponseMillisSeconds((int)(finishedTime - startTime));
            connection.recordStatus(connectionStatus);
        }

    }
}
