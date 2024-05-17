package org.example.multicast;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;

public class MulticastSender {

    private final InetSocketAddress address;
    private Bootstrap bootstrap;

    public MulticastSender(String ip, int port) {
        address = new InetSocketAddress(ip, port);

        build();
    }

    private void build() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            //ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }

    public void sendMulticastMessage(Object message) throws Exception {
        byte[] byteData = serialize(message);

        ChannelFuture future = bootstrap.bind(0).sync();
        DatagramChannel channel = (DatagramChannel) future.channel();
        DatagramPacket packet = new DatagramPacket(
                Unpooled.copiedBuffer(byteData), address);
        channel.writeAndFlush(packet).sync();
        System.out.println("Custom object message sent.");
        future.channel().close().sync();
    }

    private byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
        objStream.writeObject(object);
        objStream.flush();
        return byteStream.toByteArray();
    }
}


