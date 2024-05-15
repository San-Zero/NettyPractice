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
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import org.example.codec.MessageDecoder;
import org.example.codec.MessageEncoder;
import org.example.multicast.message.ElectionMessage;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;

public class MulticastSender {

    private final String MULTICAST_IP;
    private final int PORT;
    private Bootstrap bootstrap;

    public MulticastSender(String ip, int port) {
        MULTICAST_IP = ip;
        PORT = port;

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
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                            //ch.pipeline().addLast(new MessageDecoder());
                            //ch.pipeline().addLast(new MessageEncoder());
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }

    public void sendMulticastMessage(String message) throws InterruptedException {
        ChannelFuture future = bootstrap.bind(0).sync();
        DatagramChannel channel = (DatagramChannel) future.channel();
        DatagramPacket packet = new DatagramPacket(
                Unpooled.copiedBuffer(message, CharsetUtil.UTF_8),
                new InetSocketAddress(MULTICAST_IP, PORT));
        channel.writeAndFlush(packet).sync();
        System.out.println("Message sent: " + message);
        future.channel().close().sync();
    }

    public void sendMulticastMessage(Object message) throws Exception {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
        objStream.writeObject(message);
        objStream.flush();
        byte[] byteData = byteStream.toByteArray();

        ChannelFuture future = bootstrap.bind(0).sync();
        DatagramChannel channel = (DatagramChannel) future.channel();
        DatagramPacket packet = new DatagramPacket(
                Unpooled.copiedBuffer(byteData),
                new InetSocketAddress(MULTICAST_IP, PORT));
        channel.writeAndFlush(packet).sync();
        System.out.println("Custom object message sent.");
        future.channel().close().sync();
    }
}


