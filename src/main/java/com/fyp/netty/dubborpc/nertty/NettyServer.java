package com.fyp.netty.dubborpc.nertty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @Auther: fyp
 * @Date: 2022/2/22
 * @Description: Netty服务端
 * @Package: com.fyp.netty.dubborpc.nertty
 * @Version: 1.0
 */
public class NettyServer {

    public static void startServer(String hostName, int port) {
        startServer0(hostName, port);
    }

    private static void startServer0(String hostName, int port) {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new NettyServerHandler());

                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(hostName, port).sync();

            System.out.println("服务提供方开始提供服务~");

            channelFuture.channel().closeFuture().sync();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
          bossGroup.shutdownGracefully();
          workerGroup.shutdownGracefully();
        }


    }

}
