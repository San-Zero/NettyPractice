package org.example.multicast.message;

import java.io.Serializable;

public class ElectionMessage implements Serializable {
    private final String nodeName;
    private final int cpuCore;
    private final double cpuClock;

    public ElectionMessage(String nodeName, int cpuCore, double cpuClock) {
        this.nodeName = nodeName;
        this.cpuCore = cpuCore;
        this.cpuClock = cpuClock;
    }

    public String getNodeName() {
        return nodeName;
    }

    public int getCpuCore() {
        return cpuCore;
    }

    public double getCpuClock() {
        return cpuClock;
    }
}
