package org.example.multicast;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.example.multicast.handler.MulticastReceiverHandler;

import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

public class MulticastReceiver {

    private final String multicastIp;
    private final int multicastPort;
    private final String multicastInterface;

    public MulticastReceiver(String multicastIp, int multicastPort, String multicastInterface) {
        this.multicastIp = multicastIp;
        this.multicastPort = multicastPort;
        this.multicastInterface = multicastInterface;

        buildAndRun();
    }

    private void buildAndRun() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) throws Exception {
                            //ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                            ch.pipeline().addLast(new MulticastReceiverHandler());
                        }
                    });

            // Bind and join multicast group
            DatagramChannel channel = (DatagramChannel) bootstrap.bind(multicastPort).sync().channel();
            NetworkInterface networkInterface = NetworkInterface.getByName(multicastInterface); // Replace with your network interface name
            if (networkInterface != null) {
                channel.joinGroup(new InetSocketAddress(multicastIp, multicastPort), networkInterface).sync();
                System.out.println("Joined multicast group on interface: " + networkInterface.getDisplayName());
            } else {
                System.err.println("Network interface not found");
            }

            channel.closeFuture().await();
        } catch (SocketException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }
}


