package com.elsa.rpc.consumer.handler;

import com.elsa.rpc.consumer.msg.InbountMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @ClassName : RpcClientHandler  //类名
 * @Author : elsa //作者
 */
public class RpcClientHandler  extends SimpleChannelInboundHandler<String> {
    InbountMsg<String> inbountMsg;

    public RpcClientHandler(InbountMsg<String> inbountMsg) {
        this.inbountMsg = inbountMsg;
    }


    /**
     * 通道读取就绪事件
     *
     * @param channelHandlerContext
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        inbountMsg.receive(msg);
    }

}
