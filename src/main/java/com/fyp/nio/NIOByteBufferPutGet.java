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
public class NIOByteBufferPutGet {
    public static void main(String[] args) {
        //创建一个Buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(64);

        //类型化操作放入数据
        byteBuffer.putInt(100);
        byteBuffer.putLong(9);
        byteBuffer.putChar('尚');
        byteBuffer.putShort((short) 4);

        //取出
        byteBuffer.flip();

        System.out.println();

        //按通道顺序获取,因为获取会移动position
        //而获取的数据是从position出发的，根据获取的get类型来定量position移动
        System.out.println(byteBuffer.getInt());
        System.out.println(byteBuffer.getLong());
        System.out.println(byteBuffer.getChar());
        System.out.println(byteBuffer.getShort());
    }
}
