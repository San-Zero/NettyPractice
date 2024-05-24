package org.example;

import io.netty.util.concurrent.Future;
import org.example.channelpool.NettyFactory;
import org.example.channelpool.NetworkClient;
import org.example.message.LoginMessage;

import java.util.concurrent.ExecutionException;

public class NettyClient {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NettyFactory factory = new NettyFactory("127.0.0.1", 8080);
        NetworkClient client = factory.getNetworkClient();

        client.sendString("Hello World");

        java.util.concurrent.Future<String> future = client.getBrokerNameAsync();

        client.loginToBroker(new LoginMessage("Node1", "192.168.200.200", 6666));

        System.out.println("Broker Name: " + future.get());
    }
}
