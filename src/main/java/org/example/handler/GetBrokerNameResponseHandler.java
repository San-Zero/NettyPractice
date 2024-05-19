package org.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import org.example.message.GetBrokerNameResponse;

public class GetBrokerNameResponseHandler extends SimpleChannelInboundHandler<GetBrokerNameResponse> {
    private final Promise<String> brokerNamePromise;

    public GetBrokerNameResponseHandler(Promise<String> brokerNamePromise) {
        this.brokerNamePromise = brokerNamePromise;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GetBrokerNameResponse msg) {
        if (msg.getBrokerName() != null && !msg.getBrokerName().isEmpty()) {
            brokerNamePromise.setSuccess(msg.getBrokerName());
        } else {
            brokerNamePromise.setFailure(new Throwable("Invalid or empty broker name"));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // Can not get msg cause it is null
        //System.out.println("Broker Name: " + msg.getBrokerName());
    }

}
