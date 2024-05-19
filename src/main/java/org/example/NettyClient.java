package org.example;

import org.example.channelpool.NettyFactory;
import org.example.channelpool.NetworkClient;
import org.example.message.LoginMessage;

public class NettyClient {
    public static void main(String[] args) {
        NettyFactory factory = new NettyFactory("127.0.0.1", 8080);
        NetworkClient client = factory.getNetworkClient();

        client.sendString("Hello World");

        String brokerName = client.getBrokerName();
        if (brokerName != null) {
            System.out.println("Broker Name: " + brokerName);
        } else {
            System.out.println("Failed to get broker name");
        }

        client.loginToBroker(new LoginMessage("Node1", "192.168.200.200", 6666));
    }
}
