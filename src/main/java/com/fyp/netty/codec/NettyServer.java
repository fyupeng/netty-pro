package com.fyp.netty.codec;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;

/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: netty服务端
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyServer {
    public static void main(String[] args) throws InterruptedException {

        // 创建 BossGroup 和 WorkGroup
        /*
            说明：
            1. 创建两个线程组 bossGroup 和 workGroup
            2. bossGroup 只是 处理 连接请求， 真正的 和客户端 业务处理， 会交给 workGroup 来完成
            3. 两个都是 无限循环
            4. bossGroup 和 workerGroup 含有的 子线程 （NioEventLoop）的个数 默认实际 cpu核数 * 2
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            // 创建 服务器端的 启动对象， 配置参数
            ServerBootstrap bootstrap = new ServerBootstrap();

            // 使用 链式编程 来 进行设置
            bootstrap.group(bossGroup, workerGroup) // 设置两个 线程组
                    .channel(NioServerSocketChannel.class) // 使用 NioSocketChannel 作为 服务器的 通道实现
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置 线程队列 得到 连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置 保持活动 连接状态
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 创建一个 通道 测试对象

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 在 pipeline 中加入 ProtobufDecoder
                            // 指定对 哪种对象 进行解码
                            pipeline.addLast("decoder", new ProtobufDecoder(StudentPOJO.Student.getDefaultInstance()));
                            pipeline.addLast(new NettyServerHandler());
                        }
                    }); // 给我们的 workerGroup 的 EventLoop 对应的管道 设置处理器

            System.out.println("... 服务器 is ready...");

            // 绑定一个 端口， 并且同步，生成 一个 ChannelFuture 对象
            // 启动 服务器并绑定端口
            ChannelFuture cf = bootstrap.bind(6667).sync();

            // 对 关闭通道 进行监听
            cf.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
