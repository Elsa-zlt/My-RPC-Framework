package com.elsa.rpc.provider;

import com.elsa.rpc.provider.server.RpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @ClassName : ServerBootstrapApplication  //类名
 * @Author : elsa //作者
 */
@SpringBootApplication
public class ServerBootstrapApplication implements CommandLineRunner {
    public static final int port = ThreadLocalRandom.current().nextInt(8000,9000);

    @Autowired
    RpcServer rpcServer;

    public static void main(String[] args) {
        SpringApplication.run(ServerBootstrapApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                rpcServer.startServer("127.0.0.1",port);
            }
        }).start();
    }
}
