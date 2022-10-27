package com.fyp.nio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/4
 * @Description: 使用Channel读文件
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOFileChannel02 {
    public static void main(String[] args) throws IOException {

        //创建文件输入流
        File file = new File("d:\\file01.txt");
        FileInputStream fileInputStream = new FileInputStream(file);

        //通过fileInputStream获取对应的FileChannel -> 实际类型 FileChannelImpl
        FileChannel fileChannel = fileInputStream.getChannel();

        //创建缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());

        //将通道的数据读入到Buffer
        fileChannel.read(byteBuffer);

        //将 byteBuffer 的字节数据转成 String
        System.out.println(new String(byteBuffer.array()));
        fileInputStream.close();
    }
}
