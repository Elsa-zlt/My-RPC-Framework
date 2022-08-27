package com.elsa.rpc.consumer.datasource;

import com.elsa.rpc.consumer.client.LoadBalance;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * @ClassName : LoadDataSouce  //类名
 * @Author : elsa //作者
 */
abstract class LoadDataSouce implements DataSource,LoadBalance<Connection>{

    private final String serviceName;

    private List<Connection> list;

    private Collection<DataSourceListener> listeners = new ConcurrentLinkedQueue();

    public LoadDataSouce(String serviceName) {
        this.serviceName = serviceName;
        this.list = newList();
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public Connection getConnection() {
        if (connections().size() == 0) {
            throw new RuntimeException("没有可用的连接");
        }
        return popConnection(pickOne());
    }

    @Override
    public void remove(String connectionId) {
        list.removeIf(new Predicate(){
            @Override
            public boolean test(Object o) {
                Connection old = (Connection) o;
                if(connectionId.equals(old.id())){
                    try {
                        old.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public List<Connection> listById(String sign) {
        List<Connection> result = new ArrayList<>();
        for (Connection connection : list) {
            if(sign.equals(connection.id())){
                result.add(popConnection(connection));
            }
        }
        return result;
    }

    @Override
    public void add(Connection connection) {
        for (Connection old : list) {
            if(old.id().equals(connection.id())){
                return;
            }
        }
        list.add(connection);
    }

    @Override
    public void addListener(DataSourceListener dataSourceListener) {
        listeners.add(dataSourceListener);
    }

    @Override
    public void removeListener(DataSourceListener dataSourceListener) {
        listeners.remove(dataSourceListener);
    }

    @Override
    public void close() throws IOException {
        for (Connection connection : list) {
            connection.close();
        }
    }



    protected List<Connection> connections(){
        return list;
    }

    protected Collection<DataSourceListener> listeners(){
        return listeners;
    }

    /**
     * 要求返回的list是 thread safe
     * @return
     */
    protected List<Connection> newList() {
        return new CopyOnWriteArrayList<>();
    }

    protected Connection popConnection(Connection connection){
        return new PooledConnection(this,connection).getProxyConnection();
    }


    //connection close从datasource删除
    class PooledConnection implements InvocationHandler {
        private final String CLOSE = "close";
        private final Class<?>[] IFACES = new Class<?>[] { Connection.class };

        private final LoadDataSouce loadDataSouce;
        private final Connection realConnection;
        private final Connection proxyConnection;

        public PooledConnection(LoadDataSouce loadDataSouce, Connection realConnection) {
            this.loadDataSouce = loadDataSouce;
            this.realConnection = realConnection;
            this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);;
        }

        public LoadDataSouce getLoadDataSouce() {
            return loadDataSouce;
        }

        public Connection getRealConnection() {
            return realConnection;
        }

        public Connection getProxyConnection() {
            return proxyConnection;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            if (CLOSE.hashCode() == methodName.hashCode() && CLOSE.equals(methodName)) {
                loadDataSouce.connections().remove(realConnection);
            }
            return method.invoke(realConnection, args);
        }
    }
}
