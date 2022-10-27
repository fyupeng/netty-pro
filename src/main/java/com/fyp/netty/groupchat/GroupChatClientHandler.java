package com.fyp.netty.groupchat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Auther: fyp
 * @Date: 2022/2/14
 * @Description: 群聊系统客户端处理器
 * @Package: com.fyp.netty.groupchat
 * @Version: 1.0
 */
public class GroupChatClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg.trim());
    }
}
