package com.elsa.rpc.consumer.datasource;

import com.elsa.rpc.center.pojo.ServiceInfo;
import com.elsa.rpc.consumer.msg.OutBoundMsg;

import java.io.Closeable;

/**
 * @ClassName : Connection  //类名
 * @Author : elsa //作者
 */
public interface Connection extends Closeable {

    boolean isActive();

    String id();

    ServiceInfo connectInfo();

    OutBoundMsg<String, Void> getOutBoundMsg();

    void recordStatus(ConnectionStatus t);

    ConnectionStatus status();

    public static class ConnectionStatus {
        private long recordTime;

        private int responseMillisSeconds;

        public long getRecordTime() {
            return recordTime;
        }

        public void setRecordTime(long recordTime) {
            this.recordTime = recordTime;
        }

        public int getResponseMillisSeconds() {
            return responseMillisSeconds;
        }

        public void setResponseMillisSeconds(int responseMillisSeconds) {
            this.responseMillisSeconds = responseMillisSeconds;
        }
    }
}
