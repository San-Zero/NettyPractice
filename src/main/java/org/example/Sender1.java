package org.example;

import org.example.message.LoginMessage;
import org.example.multicast.MulticastFactory;
import org.example.multicast.MulticastSender;
import org.example.multicast.message.ElectionMessage;

public class Sender1 {
    public static void main(String[] args) throws Exception {
        MulticastFactory factory = new MulticastFactory("230.0.0.0", 4321);
        MulticastSender sender = factory.getMulticastSender();
        sender.sendMulticastMessage("1. Hello World");
        sender.sendMulticastMessage("2. Hello World");
        sender.sendMulticastMessage("3. Hello World");

        sender.sendMulticastMessage(new ElectionMessage("Node1", 4, 3.4));
        sender.sendMulticastMessage(new LoginMessage("Node1", "192.168.200.200", 8080));
    }
}
