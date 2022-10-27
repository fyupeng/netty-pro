package com.fyp.netty.groupchat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description: 群聊系统服务端处理器
 * @Package: com.fyp.netty.groupchat
 * @Version: 1.0
 */
public class GroupChatServerHandler extends SimpleChannelInboundHandler<String> {

    // 定义 一个 channel 组， 管理所有的 channel
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     *  handlerAdded 表示连接建立， 一旦连接， 第一个被执行
     *  将当前的 channel 加入到 channelGroup
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        Channel channel = ctx.channel();
        // 将该客户端加入 聊天的信息 ，推送给 其他 客户端
        /*
        该方法 不会 将 channelGroup 中 所有的 channel 遍历，并发送 消息
        我们 不需要 自己 遍历
         */
        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + " 加入聊天 " + sdf.format(new Date()) + "\t");
        channelGroup.add(channel);
    }

    // 断开 连接，将 xx 客户端 离开信息 推送给 其他 在线的 客户端
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        Channel channel = ctx.channel();

        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + " 离开了 \t");

        System.out.println("channelGroup size " + channelGroup.size());

    }

    // 表示 channel 处于 活动状态，提示 xx 在线
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        System.out.println(ctx.channel().remoteAddress() + "上线了~");

    }

    // 表示 channel 处于 不活动状态，提示 xx 下线
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        System.out.println(ctx.channel().remoteAddress() + "离线了~");

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        // 获取到当前的 channel
        Channel channel = ctx.channel();
        // 遍历 channelGroup，根据不同的 情况，回送 不同的 消息

        channelGroup.forEach(ch -> {
            if (channel != ch) { // 不是当前的 channel，转发消息
                ch.writeAndFlush("[客户] " + channel.remoteAddress() + " 发送了消息： " + msg + "\n");
            } else {// 回显 自己发送的消息 给自己
                ch.writeAndFlush("[自己] 发送了消息： " + msg + "\n");
            }
        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 关闭通道
        ctx.close();
    }
}
