package com.fyp.netty.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * @Auther: fyp
 * @Date: 2022/2/12
 * @Description: 测试http服务端的初始化器
 * @Package: com.fyp.netty.http
 * @Version: 1.0
 */
public class TestServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        // 向 管道 加入 处理器

        // 得到 管道
        ChannelPipeline pipeline = ch.pipeline();

        // 加入 一个 netty 提供的  httpServerCodec => [coder - decoder]
        /*
            HttpServerCodec 说明
            1. HttpServerCodec 是 netty 提供的 处理 http 的编解码器
            2. 增加一个自定义的 handler
         */
        pipeline.addLast("MyHttpServerCodec", new HttpServerCodec());
        pipeline.addLast("MyTestHttpServerHandler", new TestHttpServerHandler());

        System.out.println("ok ~");

    }
}
