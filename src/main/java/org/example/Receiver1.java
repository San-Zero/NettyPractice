package org.example;

import org.example.multicast.MulticastFactory;
import org.example.multicast.MulticastReceiver;

public class Receiver1 {
    public static void main(String[] args) {
        MulticastFactory factory = new MulticastFactory();
        MulticastReceiver receiver = factory.getMulticastReceiver("230.0.0.0", 4321, "enp6s0");
    }
}
