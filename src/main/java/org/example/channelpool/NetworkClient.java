package org.example.channelpool;

import io.netty.channel.Channel;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.example.message.GetBrokerNameMessage;
import org.example.message.LoginMessage;

public class NetworkClient {
    private final SimpleChannelPool pool;

    public NetworkClient(SimpleChannelPool pool) {
        this.pool = pool;
    }

    private void sendMessage(Object message) {
        Future<Channel> future = pool.acquire();
        future.addListener((FutureListener<Channel>) f1 -> {
            if (f1.isSuccess()) {
                Channel ch = f1.getNow();
                ch.writeAndFlush(message);
                // Release back to pool
                pool.release(ch);
            }
        });
    }

    public void loginToBroker(LoginMessage message) {
        sendMessage(message);
    }

    public String getBrokerName() {
        sendMessage(new GetBrokerNameMessage());
        return "BrokerName";
    }

    public void sendString(String message) {
        sendMessage(message);
    }
}
