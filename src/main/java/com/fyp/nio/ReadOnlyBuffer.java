package com.fyp.nio;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * @Auther: fyp
 * @Date: 2022/2/4
 * @Description:
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class ReadOnlyBuffer {
    public static void main(String[] args) {
        //创建一个Buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(64);

        for (int i = 0; i < 64; i++) {
            byteBuffer.put((byte) i);
        }

        //读取
        byteBuffer.flip();

        //得到一个只读Buffer
        ByteBuffer readOnlyBuffer = byteBuffer.asReadOnlyBuffer();
        System.out.println(readOnlyBuffer.getClass());

        //读取
        while (readOnlyBuffer.hasRemaining()) {
            System.out.println(readOnlyBuffer.get());
        }

        //会抛出 ReadOnlyBufferException 异常
        readOnlyBuffer.put((byte) 100);
    }
}
