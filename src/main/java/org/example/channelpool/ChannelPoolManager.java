package org.example.channelpool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ChannelPoolManager {
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();
    private final Map<InetSocketAddress, SimpleChannelPool> poolMap;

    public ChannelPoolManager() {
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);

        poolMap = new HashMap<>();
    }

    public SimpleChannelPool getPool(String ip, int port) {
        InetSocketAddress address = new InetSocketAddress(ip, port);
        return poolMap.computeIfAbsent(
                address,
                addr -> new SimpleChannelPool(bootstrap.remoteAddress(address), new NettyChannelPoolHandler())
        );
    }

    public void close() {
        group.shutdownGracefully();
    }
}

