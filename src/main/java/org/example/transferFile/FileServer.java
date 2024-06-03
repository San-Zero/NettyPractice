package org.example.transferFile;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.net.ssl.SNIHostName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Netty server that receives video files uploaded via HTTP POST requests and saves them to disk.
 */
public class FileServer {
    private final int port;
    private int maxContentLength = 2_147_483_647;

    /**
     * Constructor to initialize the server with a specific port.
     *
     * @param port the port number on which the server will listen.
     */
    public FileServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        new FileServer(port).start();
    }

    /**
     * Starts the Netty server.
     *
     * @throws Exception if the server fails to start.
     */
    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // Boss group to accept connections
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // Worker group to handle connections

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(maxContentLength));
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            ch.pipeline().addLast(new FileUploadHandler()); // Custom handler to process file uploads
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync(); // Bind the server to the port
            System.out.println("Server started on port: " + port);
            f.channel().closeFuture().sync(); // Wait until the server socket is closed
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /**
     * Handler to process HTTP requests and save uploaded files to disk.
     */
    public static class FileUploadHandler extends SimpleChannelInboundHandler<HttpObject> {
        private OutputStream outputStream; // Output stream to write file data
        private String fileName; // Name of the file being uploaded
        private final AtomicInteger counter = new AtomicInteger(1); // Counter for generating unique file names

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Channel Active");
            super.channelActive(ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                System.out.println("HttpRequest received: " + request.uri());

                // Check if the request method is POST
                if (!HttpMethod.POST.equals(request.method())) {
                    sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
                    return;
                }

                // Send 100 Continue response if expected
                if (HttpUtil.is100ContinueExpected(request)) {
                    send100Continue(ctx);
                }

                String originalFileName = request.headers().get("X-File-Name");
                fileName = sanitizeFileName(originalFileName);
            }

            if (msg instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) msg;
                ByteBuffer byteBuffer = httpContent.content().nioBuffer();

                // Initialize the output stream when the first chunk of data is received
                if (outputStream == null) {
                    if (fileName == null) {
                        fileName = "unknown" + ".tmp";
                    } else {
                        fileName = "received-" + fileName;
                    }

                    // add a counter before file extension if it already exists
                    String uniqueFileName = fileName;
                    while (Files.exists(Path.of(uniqueFileName))) {
                        int index = fileName.lastIndexOf(".");
                        String baseName = fileName.substring(0, index);
                        String extension = fileName.substring(index);
                        uniqueFileName = baseName + " (" + counter.getAndIncrement() + ")" + extension;
                    }
                    fileName = uniqueFileName;
                    counter.set(1);

                    Path outputPath = Paths.get(fileName);
                    Files.createFile(outputPath);
                    outputStream = new FileOutputStream(outputPath.toFile());
                }

                // Write the received data to the file
                byte[] bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);
                outputStream.write(bytes);

                // Close the output stream when the last chunk of data is received
                if (msg instanceof LastHttpContent) {
                    outputStream.close();
                    outputStream = null;
                    fileName = null;
                    System.out.println("File received and saved.");
                    sendResponse(ctx, HttpResponseStatus.OK);
                }
            }
        }

        /**
         * Sends a 100 Continue response to the client.
         *
         * @param ctx the channel handler context.
         */
        private void send100Continue(ChannelHandlerContext ctx) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
            ctx.writeAndFlush(response);
        }

        /**
         * Sends a response to the client with the specified HTTP status.
         *
         * @param ctx    the channel handler context.
         * @param status the HTTP status to send.
         */
        private void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        /**
         * Sends an error response to the client with the specified HTTP status.
         *
         * @param ctx    the channel handler context.
         * @param status the HTTP status to send.
         */
        private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }

        /**
         * Sanitizes a filename by removing invalid characters.
         *
         * @param fileName the original filename.
         * @return the sanitized filename.
         */
        private String sanitizeFileName(String fileName) {
            if (fileName == null) {
                return null;
            }
            // Replace invalid characters with underscores
            return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        }

    }


}

