package org.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.message.LoginMessage;

public class LoginMessageHandler extends SimpleChannelInboundHandler<LoginMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginMessage msg) {
        System.out.println("Login Message: " + msg.getNodeName() + " " + msg.getServerIp() + " " + msg.getServerPort());
    }
}
