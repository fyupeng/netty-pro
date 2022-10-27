package com.fyp.nio;


import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/4
 * @Description: 使用Channel写文件
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOFileChannel01 {
    public static void main(String[] args) throws IOException {
        String str = "hello,尚硅谷";
        //创建一个输出流 -> Channel
        FileOutputStream fileOutputStream = new FileOutputStream("d:\\file01.txt");

        //通过fileOutStream获取对应的 FileChannel
        //这个fileChannel 的真是类型是 FileChannelImpl
        FileChannel fileChannel = fileOutputStream.getChannel();

        //创建一个缓冲区 ByteBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //将str放入byteBuffer
        byteBuffer.put(str.getBytes());

        //对byteBuffer 进行flip
        byteBuffer.flip();

        //将byteBuffer写入到 fileChannel
        fileChannel.write(byteBuffer);
        fileOutputStream.close();

    }
}
