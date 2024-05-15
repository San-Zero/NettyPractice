package org.example.multicast;

public class Receiver2 {
    public static void main(String[] args) {
        MulticastReceiver receiver = new MulticastReceiver("230.0.0.0", 4321, "eno1");
    }
}