package org.example.multicast;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class MulticastFactory {

    public MulticastFactory() {
    }

    public MulticastSender getMulticastSender(String multicastIP, int multicastPort) {
        return new MulticastSender(multicastIP, multicastPort);
    }

    public MulticastReceiver getMulticastReceiver(String multicastIP, int multicastPort, String multicastInterface) {
        return new MulticastReceiver(multicastIP, multicastPort, multicastInterface);
    }
}
