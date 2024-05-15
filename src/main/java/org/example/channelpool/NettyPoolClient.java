package org.example.channelpool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.example.message.GetBrokerNameMessage;
import org.example.message.LoginMessage;

import java.net.InetSocketAddress;

public class NettyPoolClient {
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap();
    private final InetSocketAddress address;
    private final int maxConnections;

    public NettyPoolClient(String ip, int port, int maxConnections) {
        this.address = new InetSocketAddress(ip, port);
        this.maxConnections = maxConnections;
    }

    ChannelPoolMap<InetSocketAddress, SimpleChannelPool> poolMap;

    public void build() throws Exception {
        // 建立bootstrap
        bootstrap.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);

        // 建立channel pool，根據最大連線數量產生對應數量的channel，
        poolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
            @Override
            protected SimpleChannelPool newPool(InetSocketAddress key) {
                return new FixedChannelPool(bootstrap.remoteAddress(key), new NettyChannelPoolHandler(), maxConnections);
            }
        };
    }

    public Future<Channel> sendString(String message) {
        final SimpleChannelPool pool = poolMap.get(address);
        Future<Channel> future = pool.acquire();
        future.addListener((FutureListener<Channel>) f1 -> {
            if (f1.isSuccess()) {
                Channel ch = f1.getNow();
                ch.writeAndFlush(message);
                // Release back to pool
                pool.release(ch);
            }
        });

        return future;
    }

    public void loginToBroker(LoginMessage message){
        sendMessage(message);
    }

    public void getBrokerName(){
        sendMessage(new GetBrokerNameMessage());
    }

    public void sendMessage(Object message){
        final SimpleChannelPool pool = poolMap.get(address);
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


    public static void main(String[] args) throws Exception {
        NettyPoolClient client = new NettyPoolClient("127.0.0.1", 8080, 5);
        client.build();
        final String ECHO_REQ = "Hello Netty.";
        for (int i = 0; i < 2; i++) {
            client.sendString(ECHO_REQ);
        }

        client.getBrokerName();
        client.loginToBroker(new LoginMessage("Node01", "192.168.200.200", 6666));
    }
}
