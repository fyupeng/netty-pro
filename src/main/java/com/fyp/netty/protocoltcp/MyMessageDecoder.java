package com.fyp.netty.protocoltcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @Auther: fyp
 * @Date: 2022/2/17
 * @Description:
 * @Package: com.fyp.netty.protocoltcp
 * @Version: 1.0
 */
public class MyMessageDecoder extends ReplayingDecoder<Void> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        System.out.println("MyMessageDecoder decode 被调用");
        // 需要 将 二进制 字节码 -> MessageProtocol 数据包（对象）
        /**
         * 获取 是 在 之前的 编码器写入的 int 和 byte 类型数据
         *      out.writeInt(msg.getLen());
     *          out.writeBytes(msg.getContent());
         */
        int length = in.readInt();


        byte[] content = new byte[length];
        in.readBytes(content);

        MessageProtocol messageProtocol = new MessageProtocol();
        messageProtocol.setLen(length);
        messageProtocol.setContent(content);

        out.add(messageProtocol);
    }
}
