package org.example.channelpool;

import io.netty.channel.pool.SimpleChannelPool;

public class NettyFactory {
    private static SimpleChannelPool pool = null;

    public NettyFactory() {}

    public static NetworkClient getNetworkClient(String ip, int port) {
        if (pool == null) {
            pool = new ChannelPool(ip, port).getPool();
        }
        return new NetworkClient(pool);
    }
}
