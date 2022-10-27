package com.fyp.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.tcp
 * @Version: 1.0
 */
public class MyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private int count;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        // 使用 客户端 发送 十条 数据，从而模拟 tcp 的 粘包和拆包
        for (int i = 0; i <10; i++) {
            ByteBuf buffer = Unpooled.copiedBuffer("hello,server" + i, Charset.forName("utf-8"));
            ctx.writeAndFlush(buffer);
        }

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

        byte[] buffer = new byte[msg.readableBytes()];
        msg.readBytes(buffer);

        String message = new String(buffer, Charset.forName("utf-8"));
        System.out.println("客户端接收到消息=" + message);
        System.out.println("客户端接受消息数据= " + (++this.count));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
