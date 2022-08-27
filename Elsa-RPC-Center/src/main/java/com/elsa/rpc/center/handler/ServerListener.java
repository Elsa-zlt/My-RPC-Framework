package com.elsa.rpc.center.handler;

import com.elsa.rpc.center.pojo.ServiceInfo;

public interface ServerListener {

    void serverAdded(ServiceInfo service);

    void serverRemoved(ServiceInfo service);

    void serverUpdated(ServiceInfo service, String data);

}
