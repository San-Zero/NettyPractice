package org.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.example.chat.MyServerHandler;
import org.example.handler.GetBrokerNameMessageHandler;
import org.example.handler.LoginMessageHandler;
import org.example.message.GetBrokerNameMessage;
import org.example.message.GetBrokerNameResponse;
import org.example.message.LoginMessage;

public class HibaServer {
    public static void main(String[] args) throws Exception {
        //创建两个线程组 boosGroup、workerGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //创建服务端的启动对象，设置参数
            ServerBootstrap bootstrap = new ServerBootstrap();
            //设置两个线程组boosGroup和workerGroup
            bootstrap.group(bossGroup, workerGroup)
                    //设置服务端通道实现类型
                    .channel(NioServerSocketChannel.class)
                    //设置线程队列得到连接个数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //设置保持活动连接状态
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //使用匿名内部类的形式初始化通道对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                            ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.softCachingResolver(ClassLoader.getSystemClassLoader())));
                            ch.pipeline().addLast(new ObjectEncoder());

                            ch.pipeline().addLast(new GetBrokerNameMessageHandler());
                            ch.pipeline().addLast(new LoginMessageHandler());

                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                    System.out.println("Received message of unknown type: " + msg.getClass());
                                }
                            });


                        }
                    });
            System.out.println("HiBA Server is ready...");
            //给workerGroup的EventLoop对应的管道设置处理器
            //绑定端口号，启动服务端
            ChannelFuture channelFuture = bootstrap.bind(6666).sync();
            //对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    static class LoginHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof LoginMessage) {
                LoginMessage message = (LoginMessage) msg;
                System.out.println("Received login from: " + message.getNodeName());
            }
            ctx.fireChannelRead(msg);
        }
    }
}
