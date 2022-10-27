package com.fyp.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;


/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: nettty服务端处理器
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<StudentPOJO.Student> {

    /**
     * 读取数据
     * @param ctx 上下文对象，含有 管道 pipeline, 通道 channel, 地址
     * @param msg 客户端发送的 数据， 默认类型 Object
     * @throws Exception
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, StudentPOJO.Student msg) throws Exception {

        System.out.println("客户端发送的数据 id " + msg.getId() + " name " + msg.getName());
    }

    // 处理读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);

        // 将数据 写入到 缓存 并 刷新
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端: 狗", CharsetUtil.UTF_8));
    }

    // 处理异常， 要关闭 通道


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);

        ctx.close();
    }

}
