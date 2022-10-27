package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description:
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyByteToLongDecoder2 extends ReplayingDecoder<Void> {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("MyByteToLongDecoder2 被调用");
        // 在 ReplayingDecoder 不需要 判断 数据 是否足够 读取，内部会进行 处理判断
        out.add(in.readLong());
    }
}
