package org.example.channelpool;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.message.GetBrokerNameMessage;
import org.example.message.GetBrokerNameResponse;
import org.example.message.LoginMessage;

import java.util.concurrent.atomic.AtomicInteger;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    static AtomicInteger count = new AtomicInteger(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handleLoginMessage("channelActived");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (msg) {
            case String body -> handleString(ctx, body);
            case GetBrokerNameMessage getBrokerNameMessage -> handleGetBrokerNameMessage(ctx);
            case LoginMessage body ->
                    handleLoginMessage("Login Message: " + body.getNodeName() + " " + body.getServerIp() + " " + body.getServerPort());
            case null, default -> handleLoginMessage("Received message of unknown type: " + msg.getClass());
        }

    }

    private static void handleLoginMessage(String body) {
        System.out.println(body);
    }

    private static void handleGetBrokerNameMessage(ChannelHandlerContext ctx) {
        System.out.println("Received GetBrokerNameMessage");
        System.out.println("Return Broker Name to HiBA Client");
        ctx.writeAndFlush(new GetBrokerNameResponse("Broker1"));
    }

    private static void handleString(ChannelHandlerContext ctx, String body) {
        System.out.println(count.getAndIncrement() + ":" + body);
        ctx.writeAndFlush("Welcome to Netty.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

