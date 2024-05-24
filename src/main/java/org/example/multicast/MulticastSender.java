package org.example.multicast;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MulticastSender {

    private final InetSocketAddress address;
    private Bootstrap bootstrap;
    private ExecutorService executorService;
    private ChannelFuture future;
    private DatagramChannel channel;

    public MulticastSender(String ip, int port) {
        address = new InetSocketAddress(ip, port);
        // Create only one thread to send messages
        executorService = Executors.newSingleThreadExecutor();

        start();
    }

    private void start() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        }
                    });
            future = bootstrap.bind(0).sync();
            channel = (DatagramChannel) future.channel();
        } catch (Exception e) {
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }

    public void close() throws InterruptedException {
        // Shutdown the executor service
        executorService.shutdown();
        try {
            // Wait for all tasks to finish
            if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Close the Netty channel
        if (channel != null && channel.isOpen()) {
            channel.close().await();
        }

        // Shut down the Netty event loop group
        if (bootstrap != null && bootstrap.group() != null) {
            bootstrap.group().shutdownGracefully().sync();
        }
    }


    public java.util.concurrent.Future<Void> sendMulticastMessageAsync(Object message) {
        Runnable task = () -> {
            try {
                Thread.sleep(1000);
                sendMulticastMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        return executorService.submit(task, null);
    }

    public void sendMulticastMessage(Object message) throws Exception {
        byte[] byteData = serialize(message);
        DatagramPacket packet = new DatagramPacket(
                Unpooled.copiedBuffer(byteData), address);
        channel.writeAndFlush(packet);
        System.out.println("Custom object message sent. " + message);
    }

    private byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
        objStream.writeObject(object);
        objStream.flush();
        return byteStream.toByteArray();
    }
}


