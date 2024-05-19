package org.example.channelpool;

import io.netty.channel.pool.SimpleChannelPool;

public class NettyFactory {
    private static SimpleChannelPool pool;

    public NettyFactory() {
    }

    public NettyFactory(String ip, int port) {
        pool = new ChannelPool(ip, port).getPool();
    }

    public static NetworkClient getNetworkClient() {
        return new NetworkClient(pool);
    }


    public static NetworkServer getNetworkServer(int port) throws InterruptedException {
        return new NetworkServer(port);
    }
}
