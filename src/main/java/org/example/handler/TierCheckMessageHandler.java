package org.example.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.example.message.TierCheckMessage;

public class TierCheckMessageHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TierCheckMessage) {
            //获取客户端发送过来的消息
            TierCheckMessage tierCheckMessage = (TierCheckMessage) msg;
            System.out.println("Received TierCheckMessage");
            System.out.println("The tier is: " + tierCheckMessage.getTier());
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //发生异常，关闭通道
        ctx.close();
    }
}
