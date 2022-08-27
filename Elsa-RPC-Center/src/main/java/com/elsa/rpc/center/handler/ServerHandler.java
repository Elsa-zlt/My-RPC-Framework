package com.elsa.rpc.center.handler;

public interface ServerHandler {

    boolean registerServer(Class serverClass, String ip, int port);

}
