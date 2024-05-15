package org.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.message.GetBrokerNameResponse;

public class GetBrokerNameResponseHandler extends SimpleChannelInboundHandler<GetBrokerNameResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GetBrokerNameResponse msg) {
        System.out.println("Broker Name: " + msg.getBrokerName());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // Can not get msg cause it is null
        //System.out.println("Broker Name: " + msg.getBrokerName());
    }

}
