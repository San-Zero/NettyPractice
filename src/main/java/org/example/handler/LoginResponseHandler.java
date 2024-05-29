package org.example.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.message.LoginResponse;

import java.util.concurrent.CompletableFuture;

public class LoginResponseHandler extends SimpleChannelInboundHandler<LoginResponse> {
    private final CompletableFuture<Boolean> loginFuture;

    public LoginResponseHandler(CompletableFuture<Boolean> loginFuture) {
        this.loginFuture = loginFuture;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LoginResponse loginResponse) throws Exception {
        loginFuture.complete(loginResponse.isSuccess());
    }
}
