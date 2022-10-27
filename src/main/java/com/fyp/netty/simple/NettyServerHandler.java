package com.fyp.netty.simple;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;


/**
 * @Auther: fyp
 * @Date: 2022/2/11
 * @Description: nettty服务端处理器
 * @Package: com.fyp.netty.simple
 * @Version: 1.0
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读取数据
     * @param ctx 上下文对象，含有 管道 pipeline, 通道 channel, 地址
     * @param msg 客户端发送的 数据， 默认类型 Object
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("服务器读取线程 " + Thread.currentThread().getName());

        // 不能 调用 父类的 channelRead方法，否则 会报 以下异常：
        // An exceptionCaught() event was fired, and it reached at the tail of the pipeline.
        // It usually means the last handler in the pipeline did not handle the exception
        //super.channelRead(ctx, msg);

        System.out.println("server ctx = " + ctx);

        // 将 msg 转成一个 ByteBuffer

        ByteBuf buf = (ByteBuf) msg;
        System.out.println("客户端发送消息是： " + buf.toString(CharsetUtil.UTF_8));
        System.out.println("客户端地址：" + ctx.channel().remoteAddress());
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
