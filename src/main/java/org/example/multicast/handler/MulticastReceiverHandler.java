package org.example.multicast.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.example.message.LoginMessage;
import org.example.multicast.message.ElectionMessage;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import io.netty.buffer.ByteBuf;


public class MulticastReceiverHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        ByteBuf content = packet.content();
        Object receivedObject = decode(content);

        // Handle the object based on its type
        switch (receivedObject) {
            case ElectionMessage message -> handleElectionMessage(message);
            case LoginMessage message -> handleLoginMessage(message);
            case String message -> System.out.println("Received a string message: " + message);
            case null, default ->
                    System.out.println("Received an unrecognized object type.");
        }
    }

    private Object decode(ByteBuf content) throws Exception {
        byte[] data = new byte[content.readableBytes()];
        content.readBytes(data);
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        }
    }

    private void handleElectionMessage(ElectionMessage message) {
        System.out.print("Received ElectionMessage from node: " + message.getNodeName());
        System.out.print(" CPU Cores: " + message.getCpuCore());
        System.out.println(" CPU Clock Speed: " + message.getCpuClock() + " GHz");
    }

    private void handleLoginMessage(LoginMessage message) {
        System.out.println("Login Message: " + message.getNodeName() + " " + message.getServerIp() + " " + message.getServerPort());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

