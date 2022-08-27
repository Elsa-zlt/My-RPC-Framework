package com.elsa.rpc.center;

import com.elsa.rpc.center.handler.ClientHandler;
import com.elsa.rpc.center.handler.ServerHandler;
import com.elsa.rpc.center.handler.ZookeeperServerHandler;
import com.elsa.rpc.center.pojo.ZKConfig;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName : RpcCenterConfiguration  //类名
 * @Author : elsa //作者
 */
@Configuration
@ConditionalOnProperty(value = "rpc.center.active", havingValue = "true", matchIfMissing = false)
public class RpcCenterConfiguration {

    @Bean()
    @ConfigurationProperties(prefix = "rpc.center")
    public ZKConfig newDataSource() {
        return new ZKConfig();
    }

    @Bean
    public CuratorFramework newCuratorFramework(ZKConfig zkConfig){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zkConfig.getConnectStr())
                .sessionTimeoutMs(zkConfig.getSessionTimeoutMs())
                .connectionTimeoutMs(zkConfig.getConnectionTimeoutMs())
                .retryPolicy(retryPolicy)
                //namespace和connectString中指定nameSpace.只能二选一
                // .namespace("curator")
                .namespace(zkConfig.getNamespace())
                .build();
        client.start();
        return client;
    }

    @Bean
    public ServerHandler newServerHandler(CuratorFramework client){
        return new ZookeeperServerHandler(client);
    }

    @Bean
    public ClientHandler newClientHandler(CuratorFramework client){
        return new ZookeeperServerHandler(client);
    }

}
