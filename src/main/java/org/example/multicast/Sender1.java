package org.example.multicast;

import org.example.multicast.message.ElectionMessage;

public class Sender1 {
    public static void main(String[] args) throws Exception {
        MulticastSender sender = new MulticastSender("230.0.0.0", 4321);
        sender.sendMulticastMessage("1. Hello World");
        sender.sendMulticastMessage("2. Hello World");
        sender.sendMulticastMessage("3. Hello World");

        sender.sendMulticastMessage(new ElectionMessage("Node1", 4, 3.4));
    }
}
