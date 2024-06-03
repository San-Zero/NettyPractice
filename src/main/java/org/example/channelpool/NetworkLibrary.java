package org.example.channelpool;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.FutureListener;
import org.example.codec.MessageDecoder;
import org.example.codec.MessageEncoder;
import org.example.handler.GetBrokerNameResponseHandler;
import org.example.handler.StringHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class NetworkLibrary {
    private EventLoopGroup serverBossGroup;
    private EventLoopGroup serverWorkerGroup;
    private ServerBootstrap serverBootstrap;

    private EventLoopGroup clientGroup;
    private Bootstrap clientBootstrap;

    private SimpleChannelPool pool;

    public NetworkLibrary() {
    }

    public void startNetworkServer(int port) throws InterruptedException {
        serverBossGroup = new NioEventLoopGroup();
        serverWorkerGroup = new NioEventLoopGroup();
        try {
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(serverBossGroup, serverWorkerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    //.addLast(new LoggingHandler(LogLevel.INFO))
                                    .addLast(new MessageDecoder())
                                    .addLast(new MessageEncoder())
                                    .addLast(new DefaultEventExecutorGroup(8),
                                            new NettyServerHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            serverBossGroup.shutdownGracefully();
            serverWorkerGroup.shutdownGracefully();
        }
    }

    public void startNetworkClient(String ip, int port) {
        clientGroup = new NioEventLoopGroup();
        clientBootstrap = new Bootstrap();
        clientBootstrap.group(clientGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                //.addLast(new LoggingHandler(LogLevel.INFO))
                                .addLast("MessageDecoder", new MessageDecoder())
                                .addLast("MessageEncoder", new MessageEncoder());
                    }
                });
        pool = new SimpleChannelPool(clientBootstrap.remoteAddress(ip, port), new NettyChannelPoolHandler());
    }

    public CompletableFuture<Void> sendString(String message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Future<Channel> channelFuture = pool.acquire();

        pool.acquire().addListener((FutureListener<Channel>) f1 -> {
            if (!f1.isSuccess()) {
                future.completeExceptionally(f1.cause());
                return;
            }

            Channel ch = f1.getNow();

            ch.writeAndFlush(message).addListener((FutureListener<Void>) f2 -> {
                if (!f2.isSuccess()) {
                    future.completeExceptionally(f2.cause());
                } else {
                    future.complete(null);
                }
            });

            future.whenComplete((result, ex) -> {
                pool.release(ch);
            });
        });

        return future;
    }

    public CompletableFuture<String> receiveString() {
        CompletableFuture<String> future = new CompletableFuture<>();
        Future<Channel> channelFuture = pool.acquire();

        pool.acquire().addListener((FutureListener<Channel>) f1 -> {
            if (!f1.isSuccess()) {
                future.completeExceptionally(f1.cause());
                return;
            }

            Channel ch = f1.getNow();

            ch.pipeline().addLast(new StringHandler(future));

            future.whenComplete((result, ex) -> {
                pool.release(ch);
            });
        });
        return future;
    }
}
