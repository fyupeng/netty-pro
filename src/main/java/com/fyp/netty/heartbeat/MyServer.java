package com.fyp.netty.heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description:
 * @Package: com.fyp.netty.heartbeat
 * @Version: 1.0
 */
public class MyServer {

    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

                ServerBootstrap serverBootstrap = new ServerBootstrap();

                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.INFO)) // 在 bossGroup 中 增加一个 日志 处理器
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                // 加入一个 netty 提供的 IdleStateHandler
                                /*
                                说明
                                 1. IdleStateHandler 是 netty 提供的 处理 空闲状态的 处理器
                                 2.readerIdleTime 表示多长时间 没有读，就会发送一个 心跳检测包 检测是否连接
                                 3. writerIdleTime 表示多长时间 没有写，就会发送一个 心跳检测包 检测是否连接
                                 4. allIdleTime 表示多长时间 没有读写，就会发送一个 心跳检测包 检测是否连接
                                 5. 文档说明
                                 Triggers an {@link IdleStateEvent} when a {@link Channel} has not performed
                                 read, write, or both operation for a while.
                                 6. 当 IdleStateEvent 触发后， 就会 传递给管道 的下一个 handler 去处理
                                 通过 调用（触发）下一个 handler 的 userEventTriggered, 在该方法中去处理
                                 IdleStateEvent（读空闲，写空闲，读写空闲）
                                 */
                                pipeline.addLast(new StringDecoder());
                                pipeline.addLast(new StringEncoder());
                                pipeline.addLast(new IdleStateHandler(3, 5, 7 , TimeUnit.SECONDS));
                                // 加入一个 对 空闲检测 进行处理的 handler
                                pipeline.addLast(new MyServerHandler());
                            }
                        });

            ChannelFuture channelFuture = serverBootstrap.bind(9000).sync();
            channelFuture.channel().closeFuture().sync();


        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

}
