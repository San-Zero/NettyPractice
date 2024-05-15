package org.example.multicast.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.multicast.message.ElectionMessage;

public class ElectionMessageHandler extends SimpleChannelInboundHandler<ElectionMessage> {
    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, ElectionMessage message) throws Exception {
        System.out.println("Received Election Message: " + message.getNodeName() + " " + message.getCpuCore() + " " + message.getCpuClock());
    }
}
