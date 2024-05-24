package org.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.CompleteFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.example.message.GetBrokerNameResponse;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class GetBrokerNameResponseHandler extends SimpleChannelInboundHandler<GetBrokerNameResponse> {
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, GetBrokerNameResponse msg) {
        responseQueue.add(msg.getBrokerName());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public String getBrokerName() throws InterruptedException {
        return responseQueue.take();
    }
}
