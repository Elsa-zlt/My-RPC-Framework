package com.elsa.rpc.center.handler;

import com.elsa.rpc.center.pojo.ServiceInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @ClassName : ZookeeperServerHandler  //类名
 * @Author : elsa //作者
 */
public class ZookeeperServerHandler implements ClientHandler , ServerHandler{

    private static final String RPC_ROOT_PATH = "/elsa";
    private static final String ZK_PATH_SPLIT = "/";
    private static final String charSet = "utf-8";
    private CuratorFramework client;
    private Collection<ServerListenerFilter> listeners = new ConcurrentLinkedQueue();
    private AtomicBoolean watcherAdded = new AtomicBoolean(false);

    public ZookeeperServerHandler(CuratorFramework client) {
        this.client = client;
    }

    @Override
    public List<ServiceInfo> lookupServices(Class serverClass) {
        try {
            List<String> strings = listChildren(buildServerPath(serverClass));
            return strings.stream().map(s -> trans(s)).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public void updateServiceData(Class serverClass, ServiceInfo serviceInfo, String data) {
        try {
            updateData(buildServerNodePath(serverClass, serviceInfo.getIp(), serviceInfo.getPort()), data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addListener(Class serverClass, ServerListener serverListener) {
        ServerListenerFilter serverListenerFilter = new ServerListenerFilter(serverClass, serverListener);
        listeners.add(serverListenerFilter);
        if(!watcherAdded.get() && watcherAdded.compareAndSet(false, true)) {
            registerWatcher(buildServerPath(serverClass));
        }
    }

    @Override
    public boolean removeListener(Class serverClass, ServerListener serverListener) {
        return listeners.removeIf(oldServerListenerFilter -> {
            if (oldServerListenerFilter.getServerListener().equals(serverListener) &&
                    serverClass == oldServerListenerFilter.getServerClass()) {
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    public boolean registerServer(Class serverClass, String ip, int port) {
        try {
            createNode(buildServerNodePath(serverClass, ip, port), null, CreateMode.EPHEMERAL);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void deleteNode(String path) throws Exception {
        client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
    }

    private boolean existsNode(String path) throws Exception {
        Stat stat = client.checkExists().forPath(path);
        return stat != null;
    }

    private String getData(String path) throws Exception {
        Stat stat = new Stat();
        String data = new String(client.getData().storingStatIn(stat).forPath(path),charSet);
        return data;
    }

    private void registerWatcher(String path) {
        CuratorCacheListener listener = CuratorCacheListener
                .builder()
                .forPathChildrenCache(path, client, (c, e) -> {
                    //数据节点子节点(包括子节点数据)的变化
                    Iterator<ServerListenerFilter> iterator = listeners.iterator();
                    PathChildrenCacheEvent.Type type = e.getType();


                    while (iterator.hasNext()) {
                        ServerListenerFilter next = iterator.next();
                        switch (type) {
                            case CHILD_ADDED:
                                next.serverAdded(e.getData().getPath());
                                break;
                            case CHILD_REMOVED:
                                next.serverRemoved(e.getData().getPath());
                                break;
                            case CHILD_UPDATED:
                                byte[] dataByte = e.getData().getData();
                                String data = dataByte == null ? null : new String(dataByte, charSet);
                                next.serverUpdated(e.getData().getPath(),data);
                                break;
                            default:
                                break;
                        }
                    }
                })
                .build();

        CuratorCache cache = CuratorCache.build(client, path);
        cache.listenable()
                .addListener(listener, Executors.newSingleThreadExecutor());
        cache.start();
    }


    private void updateData(String path, String data) throws  Exception {
        Stat stat = client.setData().forPath(path, data.getBytes(charSet));
    }

    private static ServiceInfo trans(String server) {
        ServiceInfo serviceInfo = new ServiceInfo();
        String[] split = server.split(":");
        serviceInfo.setIp(split[0]);
        serviceInfo.setPort(Integer.parseInt(split[1]));
        return serviceInfo;
    }

    private List<String> listChildren(String path) throws Exception {
        List<String> list = client.getChildren().forPath(path);
        return list;
    }

    private static String buildServerPath(Class serverClass) {
        return RPC_ROOT_PATH + ZK_PATH_SPLIT + serverClass.getName();
    }

    private static String buildServerNodePath(Class serverClass, String ip, int port) {
        return buildServerPath(serverClass) + ZK_PATH_SPLIT + ip + ":" + port;
    }

    private void createNode(String path, String data, CreateMode createMode) throws  Exception {
        if (data == null) {
            client.create().creatingParentContainersIfNeeded()
                    .withMode(createMode).forPath(path);
        } else {
            client.create().creatingParentContainersIfNeeded()
                    .withMode(createMode).forPath(path, data.getBytes(charSet));
        }
    }


    private static class ServerListenerFilter implements ServerListener {
        private String prefixMatchPath;
        private ServerListener serverListener;
        private Class serverClass;

        public ServerListenerFilter(Class serverClass, ServerListener serverListener) {
            this.serverClass = serverClass;
            this.prefixMatchPath = buildServerPath(serverClass) + ZK_PATH_SPLIT;
            this.serverListener = serverListener;
        }

        @Override
        public void serverAdded(ServiceInfo service) {
            serverListener.serverAdded(service);
        }

        @Override
        public void serverRemoved(ServiceInfo service) {
            serverListener.serverRemoved(service);
        }

        @Override
        public void serverUpdated(ServiceInfo service, String data) {
            serverListener.serverUpdated(service,data);
        }

        public void serverAdded(String server) {
            String serverInfo = server.replaceFirst(prefixMatchPath, "");
            if (serverInfo.length() != server.length()) {
                serverAdded(trans(serverInfo));
            }
        }

        public void serverRemoved(String server) {
            String serverInfo = server.replaceFirst(prefixMatchPath, "");
            if (serverInfo.length() != server.length()) {
                serverRemoved(trans(serverInfo));
            }
        }

        public void serverUpdated(String server, String data) {
            String serverInfo = server.replaceFirst(prefixMatchPath, "");
            if (serverInfo.length() != server.length()) {
                serverUpdated(trans(serverInfo),data);
            }
        }

        public ServerListener getServerListener() {
            return serverListener;
        }

        public Class getServerClass() {
            return serverClass;
        }
    }
}
