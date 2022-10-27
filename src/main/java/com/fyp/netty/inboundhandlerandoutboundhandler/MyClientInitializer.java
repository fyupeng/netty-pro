package com.fyp.netty.inboundhandlerandoutboundhandler;

        import io.netty.channel.ChannelInitializer;
        import io.netty.channel.ChannelPipeline;
        import io.netty.channel.socket.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description: 客户端初始化类
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyClientInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();
        // 加入 一个 入站的 handler 对数据 进行 解码
        //pipeline.addLast(new MyByteToLongDecoder());
        pipeline.addLast(new MyByteToLongDecoder2());
        // 加入 一个 出站的 handler 对数据 进行 编码
        pipeline.addLast(new MyLongToByteEncoder());
        // 加入 一个自定义的 handler 处理业务
        pipeline.addLast(new MyClientHandler());

    }
}
