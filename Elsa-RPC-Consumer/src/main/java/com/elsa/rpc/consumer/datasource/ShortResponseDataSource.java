package com.elsa.rpc.consumer.datasource;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName : ShortResponseDataSource  //类名
 * @Author : elsa //作者
 */
public class ShortResponseDataSource extends LoadDataSouce {

    private final Comparator<Connection> compareTo = new ShortResponseComparator();

    private final static ScheduledThreadPoolExecutor SCHEDULED_THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(2);
    private final static Vector<ShortResponseDataSource> VECTOR = new Vector<>();

    static {
        SCHEDULED_THREAD_POOL_EXECUTOR.scheduleWithFixedDelay(()->{
            try {
                for (ShortResponseDataSource shortResponseDataSource : VECTOR) {
                    List<Connection> connections = shortResponseDataSource.connections();
                    Collection<DataSourceListener> listeners = shortResponseDataSource.listeners();
                    if(listeners == null || listeners.size() == 0){
                        return;
                    }
                    for (Connection connection : connections) {
                        Connection.ConnectionStatus status = connection.status();
                        if(status != null){
                            for (DataSourceListener listener : listeners) {
                                listener.connectionUpdated(connection);
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        },0,5, TimeUnit.SECONDS);
    }

    public ShortResponseDataSource(String serviceName) {
        super(serviceName);
        VECTOR.add(this);
    }

    @Override
    public Connection pickOne() {
        List<Connection> connections = connections();
        Connection connection = connections.get(0);
        for (int i = 1; i < connections.size(); i++) {
            if(compareTo.compare(connection,connections.get(i)) > 0){
                connection = connections.get(i);
            }
        }
        return connection;
    }



    private static class ShortResponseComparator implements Comparator<Connection>  {

        @Override
        public int compare(Connection o1, Connection o2) {
            Connection.ConnectionStatus status1 = o1.status();
            Connection.ConnectionStatus status2 = o2.status();
            if(status1 != null && (status1.getRecordTime() + 50000) < System.currentTimeMillis()){
                status1 = null;
            }
            if(status2 != null && (status2.getRecordTime() + 50000) < System.currentTimeMillis()){
                status2 = null;
            }

            if(status1 == null && status2 == null){
                return 0;
            }
            if(status1 == null){
                return -1;
            }
            if(status2 == null){
                return 1;
            }

            if(status1.getResponseMillisSeconds() == status2.getResponseMillisSeconds()){
                return ThreadLocalRandom.current().nextInt(-1,2);
            }
            return status1.getResponseMillisSeconds() - status2.getResponseMillisSeconds();
        }
    }
}

