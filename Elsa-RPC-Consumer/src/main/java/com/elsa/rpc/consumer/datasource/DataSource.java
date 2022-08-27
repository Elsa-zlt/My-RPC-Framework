package com.elsa.rpc.consumer.datasource;

import java.io.Closeable;
import java.util.List;

public interface DataSource extends Closeable {

    String getServiceName();

    Connection getConnection();

    void remove(String connectionId);

    List<Connection> listById(String connectionId);

    void add(Connection connection);

    void addListener(DataSourceListener dataSourceListener);

    void removeListener(DataSourceListener dataSourceListener);

    public interface DataSourceListener {
        void connectionUpdated(Connection connection);
    }

}
