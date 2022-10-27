package com.fyp.netty.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @Auther: fyp
 * @Date: 2022/2/13
 * @Description: ByteBuf测试
 * @Package: com.fyp.netty.buf
 * @Version: 1.0
 */
public class NettyByteBuf01 {
    public static void main(String[] args) {
        /*
        创建一个ByteBuf
            说明:
            1. 创建对象，该对象 包含一个 数组 arr，是一个 byte[10]
            2. 在 netty 的 buffer 中，不需要 使用 flip 进行 反转
                底层 维护了 readIndex 和 writeIndex
            3. 通过 readerIndex 和 writerIndex 和 capacity 将 buffer 分为 三个 区域
            0 - readerIndex 已经 读取的 区域
            readrIndex - writeIndex 可读的 区域
            writerIndex - capacity 可写的 区域
            左闭右开
         */

        ByteBuf buffer = Unpooled.buffer(10);

        for (int i =0; i < 10; i++) {
            buffer.writeByte(i);
        }

        System.out.println("capacity: " + buffer.capacity());

        //for (int i = 0; i < buffer.capacity(); i++) {
        //    System.out.println(buffer.getByte(i));
        //}

        for (int i = 0; i < buffer.capacity(); i++) {
            System.out.println(buffer.readByte());
        }

    }
}
