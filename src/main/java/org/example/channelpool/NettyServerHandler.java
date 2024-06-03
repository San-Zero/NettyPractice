package org.example.channelpool;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.message.GetBrokerNameMessage;
import org.example.message.GetBrokerNameResponse;
import org.example.message.LoginMessage;
import org.example.message.LoginResponse;

import java.util.concurrent.atomic.AtomicInteger;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    static AtomicInteger count = new AtomicInteger(1);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //System.out.println("Channel Active");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (msg) {
            case String string -> handleString(ctx, string);
            case GetBrokerNameMessage ignored -> handleGetBrokerNameMessage(ctx);
            case LoginMessage loginMessage -> handleLoginMessage(ctx, loginMessage);
            case null, default -> System.out.println("Unknown message type: " + msg);
        }

    }

    private static void handleLoginMessage(ChannelHandlerContext ctx, LoginMessage loginMessage) {
        System.out.println("Received LoginMessage: "
                + loginMessage.getNodeName() + " "
                + loginMessage.getServerIp() + " "
                + loginMessage.getServerPort());
        ctx.writeAndFlush(new LoginResponse(true));
    }

    private static void handleGetBrokerNameMessage(ChannelHandlerContext ctx) {
        System.out.println("Received GetBrokerNameMessage");
        System.out.println("Return Broker Name to HiBA Client");
        ctx.writeAndFlush(new GetBrokerNameResponse("Broker1"));
    }

    private static void handleString(ChannelHandlerContext ctx, String string) {
        System.out.println(count.getAndIncrement() + ":" + string);
        ctx.writeAndFlush("Welcome to Netty.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String clientID = ctx.channel().remoteAddress().toString();
        if (cause instanceof java.io.IOException) {
            System.out.println("Client :"+ clientID +" disconnected");
        } else {
            System.out.println("Error: " + cause);
        }
        cause.printStackTrace();
        ctx.close();
    }
}

