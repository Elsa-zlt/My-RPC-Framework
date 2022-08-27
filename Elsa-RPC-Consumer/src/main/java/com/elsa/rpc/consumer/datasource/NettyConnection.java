package com.elsa.rpc.consumer.datasource;

import com.elsa.rpc.center.pojo.ServiceInfo;
import com.elsa.rpc.consumer.handler.RpcClientHandler;
import com.elsa.rpc.consumer.msg.InbountMsg;
import com.elsa.rpc.consumer.msg.OutBoundMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

/**
 * @ClassName : NettyConnection  //类名
 * @Author : elsa //作者
 */
public class NettyConnection  implements Connection {
    private ServiceInfo serviceInfo;

    private String id;

    private InbountMsg<String> inbountMsg;

    private volatile EventLoopGroup group;

    private volatile Channel channel;

    private volatile ChannelPipeline pipeline;

    private volatile ConnectionStatus connectionStatus;


    private OutBoundMsg<String,Void> outBoundMsg = new OutBoundMsg<String, Void>() {
        @Override
        public Future<Void> sendMsg(String msg) {
            return pipeline.writeAndFlush(msg + "$");
        }
    };


    public NettyConnection(String id,ServiceInfo serviceInfo,InbountMsg<String> inbountMsg) throws IOException {
        this.id = id;
        this.serviceInfo = serviceInfo;
        this.inbountMsg = inbountMsg;
        this.init();
    }

    @Override
    public OutBoundMsg<String, Void> getOutBoundMsg() {
        return outBoundMsg;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public ServiceInfo connectInfo() {
        return serviceInfo;
    }

    @Override
    public void recordStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    @Override
    public ConnectionStatus status() {
        return connectionStatus;
    }




    @Override
    public boolean isActive(){
        if(channel == null){
            return false;
        }
        return this.channel.isActive();
    }



    @Override
    public void close() throws IOException {
        try{
            if (this.channel != null) {
                this.channel.close();
            }
            if (this.group != null) {
                this.group.shutdownGracefully();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 初始化方法-连接Netty服务端
     */
    private Connection init() throws IOException {
        try {
            //1.创建线程组
            NioEventLoopGroup group = new NioEventLoopGroup();
            //2.创建启动助手
            Bootstrap bootstrap = new Bootstrap();
            //3.设置参数
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            ByteBuf byteBuf = Unpooled.copiedBuffer("$".getBytes(StandardCharsets.UTF_8));
                            pipeline.addLast(new DelimiterBasedFrameDecoder(2048, byteBuf));

                            //String类型编解码器
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());


                            //添加客户端处理类
                            pipeline.addLast(new RpcClientHandler(inbountMsg));
                        }
                    });
            //4.连接Netty服务端
            Channel channel = bootstrap.connect(this.serviceInfo.getIp(), this.serviceInfo.getPort()).sync().channel();
            ChannelPipeline pipeline = channel.pipeline();

            this.pipeline = pipeline;
            this.group = group;
            this.channel = channel;
        } catch (Exception exception) {
            exception.printStackTrace();
            close();
        }
        return this;
    }
}

