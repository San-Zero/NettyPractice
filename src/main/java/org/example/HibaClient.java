package org.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.example.handler.GetBrokerNameMessageHandler;
import org.example.handler.GetBrokerNameResponseHandler;
import org.example.handler.LoginMessageHandler;
import org.example.message.GetBrokerNameMessage;
import org.example.message.LoginMessage;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class HibaClient {

    private final ChannelPool channelPool;

    public HibaClient(String host, int port, int maxConnections) {
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        // Add your channel handlers here (e.g., encoders, decoders, business logic)
                    }
                });

        // Create a fixed-size channel pool
        this.channelPool = new FixedChannelPool(bootstrap, new MyChannelPoolHandler(), maxConnections);
    }

    public CompletableFuture<Void> sendMessageAsync(String message) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // Acquire a channel from the pool
        channelPool.acquire().addListener((ChannelFuture channelFuture) -> {
            if (channelFuture.isSuccess()) {
                Channel channel = channelFuture.channel();
                // Send the message asynchronously over the channel
                channel.writeAndFlush(message).addListener((ChannelFuture writeFuture) -> {
                    if (writeFuture.isSuccess()) {
                        // Message sent successfully
                        future.complete(null);
                    } else {
                        // Handle write failure
                        future.completeExceptionally(writeFuture.cause());
                    }
                    // Release the channel back to the pool
                    channelPool.release(channel);
                });
            } else {
                // Handle connection failure
                future.completeExceptionally(channelFuture.cause());
            }
        });

        return future;
    }

    // Custom channel pool handler (you can customize this further)
    private static class MyChannelPoolHandler implements ChannelPool.Handler{
        @Override
        public void channelReleased(Channel ch) {
            // Called when a channel is released back to the pool
        }

        @Override
        public void channelAcquired(Channel ch) {
            // Called when a channel is acquired from the pool
        }

        @Override
        public void channelCreated(Channel ch) {
            // Called when a new channel is created (initialization)
        }
    }

    public static void main(String[] args) {
        HibaClient client = new HibaClient("localhost", 8080, 10);

        // Send a message asynchronously
        client.sendMessageAsync("Hello, world!").thenAccept((Void) -> {
            System.out.println("Message sent successfully");
        }).exceptionally((Throwable t) -> {
            System.err.println("Failed to send message: " + t.getMessage());
            return null;
        });
    }
}
