package org.example;

import org.example.channelpool.NetworkLibrary;
import org.example.channelpool.NetworkServer;

public class NettyServer {
    public static void main(String[] args) throws Exception {
        //NetworkServer server = new NetworkServer(8080);
        NetworkLibrary networkLibrary = new NetworkLibrary();
        networkLibrary.startNetworkServer(8080);

        networkLibrary.receiveString().thenAccept((message) -> {
            System.out.println("Received message: " + message);
        }).exceptionally((e) -> {
            System.out.println("Error: " + e.getMessage());
            return null;
        });
    }
}
