package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description:
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyClientHandler extends SimpleChannelInboundHandler<Long> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Long msg) throws Exception {
        System.out.println("服务器端发回的消息：" + msg);
    }

    // 发送数据
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("MyClientHandler 发送数据");
        //ctx.writeAndFlush(Unpooled.copiedBuffer())
        ctx.writeAndFlush(123456L); // 发送的是 一个 Long
    }
}
