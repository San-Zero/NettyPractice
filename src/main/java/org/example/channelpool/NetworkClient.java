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

import java.util.concurrent.*;

public class NetworkClient implements AutoCloseable {
    private final SimpleChannelPool pool;
    private final ExecutorService executorService;

    public NetworkClient(SimpleChannelPool pool) {
        this.pool = pool;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public CompletableFuture<Void> sendString(String message) {
        return sendMessage(message);
    }

    public CompletableFuture<Boolean> loginToBroker(LoginMessage message) {
        CompletableFuture<Boolean> loginFuture = new CompletableFuture<>();
        LoginResponseHandler handler = new LoginResponseHandler(loginFuture);
        return sendMessageAndGetResponse(message, loginFuture, handler);
    }

    public CompletableFuture<String> getBrokerName() throws InterruptedException {
        CompletableFuture<String> brokerNameFuture = new CompletableFuture<>();
        GetBrokerNameResponseHandler handler = new GetBrokerNameResponseHandler(brokerNameFuture);
        return sendMessageAndGetResponse(new GetBrokerNameMessage(), brokerNameFuture, handler);
    }

    public CompletableFuture<String> getBrokerNameAsync() {
        CompletableFuture<String> brokerNameFuture = new CompletableFuture<>();

        // Acquire a channel from the pool
        Future<Channel> channelFuture = pool.acquire();

        // Add a listener to handle the channel acquisition result
        channelFuture.addListener((FutureListener<Channel>) f1 -> {
            if (!f1.isSuccess()) {
                brokerNameFuture.completeExceptionally(f1.cause());
            } else {
                Channel ch = f1.getNow();

                // Create a handler for the response
                GetBrokerNameResponseHandler handler = new GetBrokerNameResponseHandler(brokerNameFuture);

                try {
                    // Add the handler to the pipeline
                    ch.pipeline().addAfter("MessageEncoder", "getBrokerNameResponseHandler", handler);

                    // Send the message
                    ch.writeAndFlush(new GetBrokerNameMessage()).addListener((FutureListener<Void>) f2 -> {
                        if (!f2.isSuccess()) {
                            brokerNameFuture.completeExceptionally(f2.cause());
                        }
                    });
                } catch (Exception e) {
                    brokerNameFuture.completeExceptionally(e);
                }

                // Release the channel back to the pool when the future is completed
                brokerNameFuture.whenComplete((result, throwable) -> {
                    ch.pipeline().remove(handler);
                    pool.release(ch);
                });
            }
        });

        return brokerNameFuture;
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

            future.whenComplete((result, throwable) -> {
                ch.pipeline().remove(handler);
                pool.release(ch);
            });
        });

        return future;
    }

    private CompletableFuture<Void> sendMessage(Object message) {
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
                }else{
                    Thread.sleep(2000);
                    objectFuture.complete(null);
                }
            });

            pool.release(ch);
        });

        return objectFuture;
    }

    @Override
    public void close() throws Exception {
    }
}
