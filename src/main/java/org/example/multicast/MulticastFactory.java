package org.example.multicast;

public class MulticastFactory {
    private final String ip;
    private final int port;

    public MulticastFactory(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public MulticastSender getMulticastSender() {
        return new MulticastSender(ip, port);
    }
}
