package org.example.channelpool;

import io.netty.channel.pool.SimpleChannelPool;

public class NettyFactory {
    ChannelPoolManager channelPoolManager;

    public NettyFactory() {
        channelPoolManager = new ChannelPoolManager();
    }

    public NetworkClient getNetworkClient(String ip, int port) {
        SimpleChannelPool pool = channelPoolManager.getPool(ip, port);
        return new NetworkClient(pool);
    }
}
