package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description: 服务器初始化类
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 入站的 handler 进行解码 MyByteToLongDecoder
        pipeline.addLast(new MyByteToLongDecoder());
        // 出站的 handler 进行编码 MyLongToByteEncoder
        pipeline.addLast(new MyLongToByteEncoder());
        // 自定义的 handler 处理业务逻辑
        pipeline.addLast(new MyServerHandler());


    }
}
