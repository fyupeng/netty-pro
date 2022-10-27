package com.fyp.netty.dubborpc.nertty;

import com.fyp.netty.dubborpc.provider.HelloServiceImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Auther: fyp
 * @Date: 2022/2/22
 * @Description: Netty服务端处理器
 * @Package: com.fyp.netty.dubborpc.nertty
 * @Version: 1.0
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 获取客户端发送的消息，并调用 服务
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        System.out.println("msg"+ msg);
        // 定义 协议
        if (msg.toString().startsWith("HelloService#hello#")) {

            String result = new HelloServiceImpl().hello(msg.toString().substring(msg.toString().lastIndexOf("#") + 1));

            ctx.writeAndFlush(result);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
