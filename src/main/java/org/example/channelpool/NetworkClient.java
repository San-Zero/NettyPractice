package org.example.channelpool;

import io.netty.channel.Channel;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.example.handler.GetBrokerNameResponseHandler;
import org.example.handler.LoginResponseHandler;
import org.example.message.GetBrokerNameMessage;
import org.example.message.LoginMessage;

import java.util.concurrent.CompletableFuture;


public class NetworkClient {
    private final SimpleChannelPool pool;

    public NetworkClient(SimpleChannelPool pool) {
        this.pool = pool;
    }

    public CompletableFuture<Void> sendString(String message) {
        return sendMessage(message);
    }

    public CompletableFuture<Boolean> loginToBroker(LoginMessage message) {
        CompletableFuture<Boolean> loginFuture = new CompletableFuture<>();
        LoginResponseHandler handler = new LoginResponseHandler(loginFuture);
        return sendMessageAndGetResponse(message, loginFuture, handler);
    }

    public CompletableFuture<String> getBrokerName() {
        CompletableFuture<String> brokerNameFuture = new CompletableFuture<>();
        GetBrokerNameResponseHandler handler = new GetBrokerNameResponseHandler(brokerNameFuture);
        return sendMessageAndGetResponse(new GetBrokerNameMessage(), brokerNameFuture, handler);
    }

    private <T, H> CompletableFuture<T> sendMessageAndGetResponse(Object message, CompletableFuture<T> future, SimpleChannelInboundHandler<H> handler) {
        //CompletableFuture<T> future = new CompletableFuture<>();
        Future<Channel> channelFuture = pool.acquire();

        channelFuture.addListener((FutureListener<Channel>) f1 -> {
            if (!f1.isSuccess()) {
                future.completeExceptionally(f1.cause());
                return;
            }

            Channel ch = f1.getNow();

            ch.pipeline().addAfter("MessageEncoder", "responseHandler", handler);

            ch.writeAndFlush(message).addListener((FutureListener<Void>) f2 -> {
                if (!f2.isSuccess()) {
                    future.completeExceptionally(f2.cause());
                }
            });

            future.whenComplete((result, ex) -> {
                ch.pipeline().remove(handler);
                pool.release(ch);
            });
        });

        return future;
    }

    public CompletableFuture<Void> sendMessage(Object message) {
        CompletableFuture<Void> objectFuture = new CompletableFuture<>();
        Future<Channel> channelFuture = pool.acquire();

        channelFuture.addListener((FutureListener<Channel>) f1 -> {
            if (!f1.isSuccess()) {
                objectFuture.completeExceptionally(f1.cause());
                return;
            }

            Channel ch = f1.getNow();

            ch.writeAndFlush(message).addListener((FutureListener<Void>) f2 -> {
                if (!f2.isSuccess()) {
                    objectFuture.completeExceptionally(f2.cause());
                } else {
                    objectFuture.complete(null);
                }
            });

            objectFuture.whenComplete((result, ex) -> {
                pool.release(ch);
            });
        });

        return objectFuture;
    }

    public CompletableFuture<String> receiveMessage() {
        CompletableFuture<String> future = new CompletableFuture<>();

        Future<Channel> channelFuture = pool.acquire();

        channelFuture.addListener((FutureListener<Channel>) f1 -> {
            if (!f1.isSuccess()) {
                future.completeExceptionally(f1.cause());
                return;
            }

            Channel ch = f1.getNow();

            //ch.pipeline().addAfter("MessageEncoder", "responseHandler", new GetBrokerNameResponseHandler(future));

            ch.pipeline().addLast(new GetBrokerNameResponseHandler(future));

            future.whenComplete((result, ex) -> {
                //ch.pipeline().remove("responseHandler");
                pool.release(ch);
            });
        });

        return future;
    }
}
