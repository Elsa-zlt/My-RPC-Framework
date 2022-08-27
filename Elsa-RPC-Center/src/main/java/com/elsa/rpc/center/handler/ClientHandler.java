package com.elsa.rpc.center.handler;

import com.elsa.rpc.center.pojo.ServiceInfo;

import java.util.List;

public interface ClientHandler {

    List<ServiceInfo> lookupServices(Class serverClass);

    void updateServiceData(Class serverClass, ServiceInfo serviceInfo, String data);

    void addListener(Class serverClass, ServerListener serverListener);

    boolean removeListener(Class serverClass, ServerListener serverListener);

}
