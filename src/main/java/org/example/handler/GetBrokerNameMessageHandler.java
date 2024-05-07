package org.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.message.GetBrokerNameMessage;
import org.example.message.GetBrokerNameResponse;

public class GetBrokerNameMessageHandler extends SimpleChannelInboundHandler<GetBrokerNameMessage> {
    @Override
    public void channelRead0(ChannelHandlerContext ctx, GetBrokerNameMessage msg) {
        System.out.println("Received GetBrokerNameMessage");
        System.out.println("Return Broker Name to HiBA Client");
        ctx.channel().writeAndFlush(new GetBrokerNameResponse("Broker1"));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //System.out.println("Return Broker Name to HiBA Client");
        //ctx.channel().writeAndFlush(new GetBrokerNameResponse("Broker1"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
