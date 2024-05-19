package org.example.channelpool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class ChannelPool {
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();
    private final SimpleChannelPool pool;
    private final InetSocketAddress address;

    public ChannelPool(String ip, int port) {
        address = new InetSocketAddress(ip, port);

        bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);

        pool = new SimpleChannelPool(bootstrap.remoteAddress(address), new NettyChannelPoolHandler());
    }

    public SimpleChannelPool getPool() {
        return pool;
    }
}

