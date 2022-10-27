package com.fyp.netty.dubborpc.nertty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.sql.Connection;
import java.util.concurrent.Callable;

/**
 * @Auther: fyp
 * @Date: 2022/2/22
 * @Description: Netty客户端处理器
 * @Package: com.fyp.netty.dubborpc.nertty
 * @Version: 1.0
 */
public class NettyClientHandler extends ChannelInboundHandlerAdapter implements Callable {

    private ChannelHandlerContext context;
    private String result;
    private String param; // 客户端调用方法时，传入的参数

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        context = ctx; // 其他方法中 会使用到 ctx

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        result = (String) msg;
        notify(); // 唤醒等待的 线程

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public Object call() throws Exception {
        return null;
    }
}
