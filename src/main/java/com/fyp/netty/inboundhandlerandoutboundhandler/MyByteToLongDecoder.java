package com.fyp.netty.inboundhandlerandoutboundhandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2022/2/16
 * @Description:
 * @Package: com.fyp.netty.inboundhandlerandoutboundhandler
 * @Version: 1.0
 */
public class MyByteToLongDecoder extends ByteToMessageDecoder {

    /**
     * decode 会根据接受的数据，调用多次，直到  确定没有 新的元素 添加到 list，
     *        或者是 ByteBuf  没有更多的 可读字节为止
     *        如果list out 不为空， 就会将 list 的内容 传递给 下一个 ChannelInBoundHandler
     *        处理，该处理器的 方法也会被 调用多次
     * @param ctx 上下文对象
     * @param in 入站的 ByteBuf
     * @param out list 集合，将解码后的 数据 传给下一个 handler
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        System.out.println("MyByteToLongDecoder decoder 被调用");

        // 因为 long 8个字节
         if (in.readableBytes() >= 8) {
             out.add(in.readLong());
         }

    }

}
