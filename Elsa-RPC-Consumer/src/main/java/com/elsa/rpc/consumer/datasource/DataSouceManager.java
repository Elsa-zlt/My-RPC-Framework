package com.elsa.rpc.consumer.datasource;

import com.alibaba.fastjson.JSON;
import com.elsa.rpc.center.handler.ClientHandler;
import com.elsa.rpc.center.handler.ServerListener;
import com.elsa.rpc.center.pojo.ServiceInfo;
import com.elsa.rpc.consumer.msg.InbountMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName : DataSouceManager  //类名
 * @Author : elsa //作者
 */
@Service
public class DataSouceManager implements Closeable {

    @Autowired
    private ClientHandler clientHandler;

    @Autowired
    private InbountMsg<String> inbountMsg;

    private ConcurrentHashMap<String, DataSource> connectionsMap = new ConcurrentHashMap<>();

    public Connection getConnection(String serviceName) throws IOException {
        return getDataSource(serviceName).getConnection();
    }

    private DataSource getDataSource(String serviceName) throws IOException {
        DataSource dataSource = connectionsMap.computeIfAbsent(serviceName, key -> {
            DataSource source = defaultSource(serviceName);
            try {
                Class<?> aClass = Class.forName(serviceName);
                List<ServiceInfo> serviceInfos = clientHandler.lookupServices(aClass);
                for (ServiceInfo serviceInfo : serviceInfos) {
                    source.add(buildConnection(serviceInfo));
                    System.out.println(serviceName + ",服务初始化上线服务提供者:" + serviceInfo);
                }

                //添加监听
                addListener(serviceName);
                source.addListener(connection -> {
                    clientHandler.updateServiceData(aClass,connection.connectInfo(),JSON.toJSONString(connection.status()));
                });
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(serviceName + "服务不存在");
            }
            return source;
        });
        return dataSource;
    }


    @Override
    public void close() throws IOException {
        for (DataSource dataSource : connectionsMap.values()) {
            dataSource.close();
        }
    }

    private DataSource defaultSource(String serviceName) {
        return new ShortResponseDataSource(serviceName);
        // return new RoundLoadDataSource(serviceName);
    }

    private void addListener(String serviceName) throws ClassNotFoundException {
        Class<?> aClass = Class.forName(serviceName);
        clientHandler.addListener(aClass, new ServerListener() {
            @Override
            public void serverAdded(ServiceInfo service) {
                try {
                    DataSource dataSource = connectionsMap.computeIfAbsent(serviceName, key -> {
                        return defaultSource(key);
                    });
                    if(dataSource.listById(buildSign(service)).size() > 0){
                        System.out.println(serviceName + ",服务已经上线该服务提供者:" + service);
                        return;
                    }
                    Connection connection = buildConnection(service);
                    dataSource.add(connection);
                    System.out.println(serviceName + ",服务上线服务提供者:" + service);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void serverRemoved(ServiceInfo service) {
                DataSource dataSource = connectionsMap.get(serviceName);
                if (dataSource == null) {
                    return;
                }
                dataSource.remove(buildSign(service));
                System.out.println(serviceName + ",服务下线服务提供者:" + service);
            }

            @Override
            public void serverUpdated(ServiceInfo service, String data) {
                DataSource dataSource = connectionsMap.get(serviceName);
                if (dataSource == null) {
                    return;
                }
                Connection.ConnectionStatus connectionStatus = null;
                if (data != null && data.length() > 0) {
                    connectionStatus = JSON.parseObject(data, Connection.ConnectionStatus.class);
                }

                String sign = buildSign(service);
                List<Connection> connections = dataSource.listById(sign);
                for (Connection connection : connections) {
                    connection.recordStatus(connectionStatus);
                }
            }
        });
    }

    private Connection buildConnection(ServiceInfo serviceInfo) throws IOException {
        Connection connectionInfo = new NettyConnection(buildSign(serviceInfo),serviceInfo, inbountMsg);
        return connectionInfo;
    }

    private String buildSign(ServiceInfo serviceInfo){
        return serviceInfo.getIp() + ":" + serviceInfo.getPort();
    }

}
