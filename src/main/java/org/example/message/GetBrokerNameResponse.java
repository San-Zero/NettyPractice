package org.example.message;

import java.io.Serializable;

public class GetBrokerNameResponse implements Serializable {
    private final String brokerName;

    public GetBrokerNameResponse(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getBrokerName() {
        return brokerName;
    }
}
