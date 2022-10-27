package com.fyp.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.tcp
 * @Version: 1.0
 */
public class MyServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private int count;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        byte[] buffer = new byte[msg.readableBytes()];
        msg.readBytes(buffer);

        // 将 buffer 转成 字符串
        String message = new String(buffer, Charset.forName("UTF-8"));

        System.out.println("服务器接收到数据 " + message);
        System.out.println("服务器接收到数据量= " + (++this.count));

        // 服务端 回送数据 给客户端 ，回送一个 随机 id
        ByteBuf responseByteBuf = Unpooled.copiedBuffer(UUID.randomUUID().toString() + "\n", Charset.forName("utf-8"));
        ctx.writeAndFlush(responseByteBuf);
    }
}
