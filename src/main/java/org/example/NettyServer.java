package org.example;


import org.example.channelpool.NettyFactory;
import org.example.channelpool.NetworkServer;

public class NettyServer {
    public static void main(String[] args) throws Exception {
        NettyFactory nettyFactory = new NettyFactory();
        NetworkServer server = nettyFactory.getNetworkServer(8080);
    }
}
