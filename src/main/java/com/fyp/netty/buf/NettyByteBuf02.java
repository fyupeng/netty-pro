package com.fyp.netty.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * @Auther: fyp
 * @Date: 2022/2/13
 * @Description: ByteBuf测试
 * @Package: com.fyp.netty.buf
 * @Version: 1.0
 */
public class NettyByteBuf02 {

    public static void main(String[] args) {

        // 创建 ByteBuf
        ByteBuf buf = Unpooled.copiedBuffer("hello,world!", CharsetUtil.UTF_8);

        if(buf.hasArray()) { //true

            byte[] content = buf.array();

            // 将 content 转成 字符串
            System.out.println(new String(content, CharsetUtil.UTF_8));

            System.out.println("ByteBuf= " + buf);

            System.out.println(buf.arrayOffset());
            System.out.println(buf.readerIndex());
            System.out.println(buf.writerIndex());
            System.out.println(buf.capacity());

            int len = buf.readableBytes(); // 可读的 字节数
            System.out.println("len= " + len);

            // 使用 for 取出 各个字符
            for (int i = 0; i < len; i++) {
                System.out.println((char) buf.getUnsignedByte(i));
            }

            System.out.println(buf.getCharSequence(0, 4, CharsetUtil.UTF_8));
            System.out.println(buf.getCharSequence(4, 6, CharsetUtil.UTF_8));
        }

    }

}
