package org.example.message;

import java.io.Serializable;

public class LoginMessage implements Serializable {
    private String nodeName;
    private String serverIp;
    private int serverPort;

    public LoginMessage(String nodeName, String serverIp, int serverPort) {
        this.nodeName = nodeName;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }
}
