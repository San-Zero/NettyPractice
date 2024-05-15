package org.example.multicast.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class MulticastReceiverHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        String message = packet.content().toString(io.netty.util.CharsetUtil.UTF_8);
        System.out.println("Received message: " + message);
    }
}
