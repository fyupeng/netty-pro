package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description:
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyLongToByteEncoder extends MessageToByteEncoder<Long> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Long msg, ByteBuf out) throws Exception {
        System.out.println("MyLongToByteEncoder encode 被调用");
        System.out.println("msg " + msg);
        out.writeLong(msg);
    }


}
