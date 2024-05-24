package org.example.channelpool;

import io.netty.channel.Channel;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import org.example.handler.GetBrokerNameResponseHandler;
import org.example.message.GetBrokerNameMessage;
import org.example.message.LoginMessage;

import java.util.concurrent.*;

public class NetworkClient {
    private final SimpleChannelPool pool;
    private final ExecutorService executorService;

    public NetworkClient(SimpleChannelPool pool) {
        this.pool = pool;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void sendString(String message) {
        sendMessage(message);
    }

    public void loginToBroker(LoginMessage message) {
        sendMessage(message);
    }

    //public String getBrokerName() {
    //    try {
    //        return getBrokerNameAsync().get(10, TimeUnit.SECONDS);
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //        return null;
    //    }
    //}

    public java.util.concurrent.Future<String> getBrokerNameAsync() {
        Callable<String> task = () -> {
            Future<Channel> future = pool.acquire();
            Channel ch = null;

            try {
                // Wait for the channel to be available
                ch = future.get();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            if (ch == null) {
                throw new NullPointerException("Channel is null");
            }

            GetBrokerNameResponseHandler handler = new GetBrokerNameResponseHandler();

            try {
                ch.pipeline().addAfter("MessageEncoder", "GetBrokerNameResponseHandler", handler);
                ch.writeAndFlush(new GetBrokerNameMessage());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return handler.getBrokerName();
        };

        return executorService.submit(task);
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

}
