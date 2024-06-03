package org.example.transferFile;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;
import java.io.IOException;

public class FileClient {
    private final String host;
    private final int port;
    private final EventLoopGroup group = new NioEventLoopGroup();
    private ChannelFuture f;

    public FileClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
        String filePath = args.length > 2 ? args[2] : "D:/tools/iso/ubuntu-20.04.3-desktop-amd64.iso";

        String filePath2 = "D:/Videos/2024-05-22_15-45-40.mkv";

        String[] filePaths = {filePath, filePath2};

        for (String path : filePaths) {
            new Thread(() -> {
                try {
                    new FileClient(host, port).start(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public void start(String filePath) throws Exception {
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            //ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                            ch.pipeline().addLast(new HttpClientCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(512 * 1024 * 1024));
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpObject>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
                                    if (msg instanceof HttpResponse) {
                                        HttpResponse response = (HttpResponse) msg;
                                        System.out.println("Response received: " + response.status());
                                    }
                                }
                            });
                        }
                    });

            // Start the client
            f = b.connect(host, port).sync();

            sendFile(filePath);

            // Wait until the connection is closed
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void sendFile(String filePath) throws IOException {
        System.out.println("Sending file: " + filePath);
        // Send the file
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/upload");
            request.headers().set(HttpHeaderNames.HOST, host);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
            request.headers().set("X-File-Name", file.getName());

            f.channel().write(request);
            f.channel().writeAndFlush(new ChunkedFile(file));
            System.out.println("File sent: " + filePath);
        } else {
            System.out.println("File not found: " + filePath);
        }
    }
}

