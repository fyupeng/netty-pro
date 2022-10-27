package com.fyp.nio;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/5
 * @Description: 直接修改内存
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
/*
说明：
1. MappedByteBuffer 可让文件直接在内存修改，操作系统不需要拷贝一次
 */
public class MappedByteBufferTest {
    public static void main(String[] args) throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile("1.txt", "rw");

        FileChannel channel = randomAccessFile.getChannel();

        /**
         * 参数1: FileChannel.MapMode.READ_WRITE 使用的读写模式
         * 参数2: 0: 可以直接修改的初始位置
         * 参数3: 5: 是映射到内存的大小，即将 1.txt 的多少个字节映射到内存
         * 可以直接修改的范围就是 0-5
         */
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);

        mappedByteBuffer.put(0, (byte) 'H');
        mappedByteBuffer.put(4, (byte) '9');
        //会抛出 IndexOutOfBoundsException 异常
        //mappedByteBuffer.put(5, (byte) 'Y');

        randomAccessFile.close();


    }
}
