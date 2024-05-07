package org.example.message;


import java.io.Serializable;

import java.util.Map;
import java.util.HashMap;

public abstract class Message implements Serializable{
    private int SequenceId;
    private static final Map<Integer, Class<? extends Message>> messageClasses = new HashMap<>();


    protected Message() {
    }

    protected void setSequenceID(int SequenceID) {
        this.SequenceId = SequenceID;
    }

    public static Class<? extends Message> getMessageClass(int messageType) {
        return messageClasses.get(messageType);
    }

    public int getSequenceId() {
        return SequenceId;
    }
}

