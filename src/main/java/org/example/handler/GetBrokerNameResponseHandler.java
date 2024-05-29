package org.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import org.example.message.GetBrokerNameResponse;

import java.util.concurrent.CompletableFuture;

public class GetBrokerNameResponseHandler extends SimpleChannelInboundHandler<GetBrokerNameResponse> {
    private final CompletableFuture<String> brokerNameFuture;

    public GetBrokerNameResponseHandler(CompletableFuture<String> brokerNameFuture) {
        this.brokerNameFuture = brokerNameFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GetBrokerNameResponse msg) {
        if (msg.getBrokerName() != null && !msg.getBrokerName().isEmpty()) {
            brokerNameFuture.complete(msg.getBrokerName());
        } else {
            brokerNameFuture.completeExceptionally(new Throwable("Invalid or empty broker name"));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // Can not get msg cause it is null
        //System.out.println("Broker Name: " + msg.getBrokerName());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        brokerNameFuture.completeExceptionally(cause);
    }

}
