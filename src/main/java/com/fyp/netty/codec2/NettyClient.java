package com.fyp.netty.codec2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;


/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: netty客户端
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyClient {
    public static void main(String[] args) throws InterruptedException {

        // 客户端 需要 一个 事件循环组
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {

            // 创建 客户端 启动对象
            // 注意客户端 使用的不是 ServerBootStrap, 而是BootStrap
            Bootstrap bootstrap = new Bootstrap();

            // 设置相关 参数
            bootstrap.group(group) // 设置 线程组
                    .channel(NioSocketChannel.class) // 设置 客户端 通道的 实现类（反射)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 在 pipeline 中 加入 ProtobufEncoder
                            ch.pipeline().addLast("encoder", new ProtobufEncoder());
                            ch.pipeline().addLast(new NettyClientHandler()); // 加入自己的 处理器
                        }
                    });

            System.out.println("客户端 ok...");

            // 启动客户端 去连接 服务器端
            // 关于 ChannelFuture 要分析， 涉及到 netty 的异步模型
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6667).sync();

            // 对关闭 通道 进行监听
            channelFuture.channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }
    }
}
