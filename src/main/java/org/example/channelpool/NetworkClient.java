package org.example.channelpool;

import io.netty.channel.Channel;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import org.example.handler.GetBrokerNameResponseHandler;
import org.example.message.GetBrokerNameMessage;
import org.example.message.LoginMessage;

import java.util.concurrent.CompletableFuture;

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

    public CompletableFuture<String> getBrokerName() {
        // 創建一個CompletableFuture，用於最終返回brokerName結果
        CompletableFuture<String> brokerNameFuture = new CompletableFuture<>();

        // 從poolMap中獲取與特定地址相關的channel pool

        // 從pool中獲取一個channel的Future
        Future<Channel> future = pool.acquire();

        // 為獲取channel的Future添加監聽器
        future.addListener((FutureListener<Channel>) f1 -> {
            if (f1.isSuccess()) { // 如果成功獲得channel
                // 獲取現有channel
                Channel ch = f1.getNow();

                // 在channel的EventLoop中創建一個新的Promise，用於異步處理brokerName
                Promise<String> brokerNamePromise = ch.eventLoop().newPromise();

                try {
                    // 在NettyClientHandler之前添加GetBrokerNameResponseHandler
                    ch.pipeline().addAfter("MessageEncoder", "getBrokerNameResponseHandler", new GetBrokerNameResponseHandler(brokerNamePromise));
                    // 向服務器發送GetBrokerNameMessage消息
                    ch.writeAndFlush(new GetBrokerNameMessage());
                } catch (Exception e) { // 捕捉添加Handler或發送消息時的異常
                    // 如果發生異常，設置Promise的失敗狀態
                    brokerNamePromise.setFailure(e);
                }

                // 為brokerName的Promise添加監聽器，處理返回結果
                brokerNamePromise.addListener((FutureListener<String>) f2 -> {
                    if (!f2.isSuccess()) {
                        // 如果獲取brokerName失敗，輸出失敗信息並將Future設為異常
                        brokerNameFuture.completeExceptionally(f2.cause());
                    } else { // 如果成功接收到brokerName
                        // 從Promise獲取brokerName
                        String brokerName = f2.getNow();
                        if (brokerName != null && !brokerName.isEmpty()) {
                            // 如果brokerName有效，輸出並完成Future
                            brokerNameFuture.complete(brokerName);
                        } else {
                            // 如果brokerName無效或空，輸出並將Future設為null
                            brokerNameFuture.complete(null);
                        }
                    }

                    // 移除GetBrokerNameResponseHandler
                    ch.pipeline().remove("getBrokerNameResponseHandler");
                    // 釋放channel回pool
                    pool.release(ch);
                });
            } else {
                // 如果無法成功獲取channel，將Future設為異常
                brokerNameFuture.completeExceptionally(f1.cause());
            }
        });

        // 返回最終的CompletableFuture
        return brokerNameFuture;
    }


    public void sendString(String message) {
        sendMessage(message);
    }
}
