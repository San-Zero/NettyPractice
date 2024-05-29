package org.example;

import org.example.channelpool.NettyFactory;
import org.example.channelpool.NetworkClient;
import org.example.message.LoginMessage;

import java.util.concurrent.CompletableFuture;

public class NettyClient {
    public static void main(String[] args) {
        try {
            NettyFactory factory = new NettyFactory();
            NetworkClient client = factory.getNetworkClient("127.0.0.1", 8080);

            CompletableFuture<Void> sendStringFuture = client.sendString("Hello World");

            CompletableFuture<String> brokerNameFuture = client.getBrokerName();

            CompletableFuture<Boolean> loginToBrokerFuture = client.loginToBroker(
                    new LoginMessage("Node1", "192.168.200.200", 6666));

            sendStringFuture.thenAccept((f) -> {
                System.out.println("Message sent successfully");
            }).exceptionally((e) -> {
                System.out.println("Error: " + e.getMessage());
                return null;
            });

            brokerNameFuture.thenAccept((brokerName) -> {
                System.out.println("Broker Name: " + brokerName);
            }).exceptionally((e) -> {
                System.out.println("Error: " + e.getMessage());
                return null;
            });

            loginToBrokerFuture.thenAccept((v) -> {
                if (v) {
                    System.out.println("Login to broker successful");
                } else {
                    System.out.println("Login to broker failed");
                }
            }).exceptionally((e) -> {
                System.out.println("Error: " + e.getMessage());
                return null;
            });

            Thread.sleep(5000);
            //System.out.println("Broker Name: " + future.get());
        } catch (
                Exception e) {
            // TODO: replace print error message with logger
            e.printStackTrace();
        }
    }
}
