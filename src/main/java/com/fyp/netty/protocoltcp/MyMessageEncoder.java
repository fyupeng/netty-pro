package com.fyp.netty.protocoltcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.protocoltcp
 * @Version: 1.0
 */
public class MyMessageEncoder extends MessageToByteEncoder<MessageProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageProtocol msg, ByteBuf out) throws Exception {
        System.out.println("MyMessageEncoder encode 方法 被调用");
        out.writeInt(msg.getLen());
        out.writeBytes(msg.getContent());
    }

}
