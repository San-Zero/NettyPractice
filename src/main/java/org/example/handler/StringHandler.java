package org.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.channelpool.ChannelPoolManager;

import java.util.concurrent.CompletableFuture;

public class StringHandler extends ChannelInboundHandlerAdapter {
    private final CompletableFuture<String> future;

    public StringHandler(CompletableFuture<String> future) {
        this.future = future;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof String) {
            future.complete((String) msg);
            ctx.pipeline().remove(this);
        } else {
            future.completeExceptionally(new Throwable("Invalid message type"));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        future.completeExceptionally(cause);
        ctx.close();
    }
}
