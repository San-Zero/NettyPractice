package org.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.example.handler.GetBrokerNameResponseHandler;
import org.example.message.GetBrokerNameMessage;
import org.example.message.GetBrokerNameResponse;
import org.example.message.LoginMessage;

public class HibaClient {
    public static void main(String[] args) throws Exception {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        try {
            //创建bootstrap对象，配置参数
            Bootstrap bootstrap = new Bootstrap();
            //设置线程组
            bootstrap.group(eventExecutors)
                    //设置客户端的通道实现类型
                    .channel(NioSocketChannel.class)
                    //使用匿名内部类初始化通道
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                            ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.softCachingResolver(ClassLoader.getSystemClassLoader())));
                            ch.pipeline().addLast(new ObjectEncoder());

                            ch.pipeline().addLast(new GetBrokerNameResponseHandler());
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    ctx.channel().writeAndFlush(new GetBrokerNameMessage());
                                    ctx.channel().writeAndFlush(new LoginMessage("Node1", "192.168.200.200", 6666));
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                    System.out.println("Received message of unknown type: " + msg.getClass());
                                }
                            });


                        }
                    });

            System.out.println("Connect to HiBA Server");
            //连接服务端
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6666).sync();
            //对通道关闭进行监听
            channelFuture.channel().closeFuture().sync();
        } finally {
            //关闭线程组
            eventExecutors.shutdownGracefully();
        }
    }

}
