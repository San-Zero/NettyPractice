package org.example;


import org.example.channelpool.NettyFactory;
import org.example.channelpool.NetworkServer;

public class NettyServer {
    public static void main(String[] args) throws Exception {
        NetworkServer server = new NetworkServer(8080);
    }
}
