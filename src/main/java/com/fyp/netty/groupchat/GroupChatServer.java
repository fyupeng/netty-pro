package com.fyp.netty.groupchat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description: 群聊系统服务端
 * @Package: com.fyp.netty.groupchat
 * @Version: 1.0
 */
public class GroupChatServer {
    private int port; // 监听端口

    public GroupChatServer(int port) {
        this.port = port;
    }

    // 编写 run 方法，处理 客户端的 请求
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            // 获取到 pipeline
                            ChannelPipeline pipeline = ch.pipeline();
                            // 向 pipeline 加入 解码器
                            pipeline.addLast("decoder", new StringDecoder());
                            // 向 pipeline 加入 编码器
                            pipeline.addLast("encoder", new StringEncoder());
                            // 加入自己 业务处理的 handler
                            pipeline.addLast(new GroupChatServerHandler());

                        }
                    });

            System.out.println("netty 服务器启动...");
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            // 监听 关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new GroupChatServer(9000).run();
    }

}
