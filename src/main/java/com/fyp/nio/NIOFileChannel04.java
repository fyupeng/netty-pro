package com.fyp.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/4
 * @Description: 拷贝文件-transferFrom方法
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOFileChannel04 {
    public static void main(String[] args) throws IOException {
        //创建输入流和输出流
        FileInputStream fileInputStream = new FileInputStream("d:\\wallhaven.png");
        FileOutputStream fileOutputStream = new FileOutputStream("d:\\wallhaven01.png");

        //获取各个流对应的Channel
        FileChannel sourceCh = fileInputStream.getChannel();
        FileChannel destCh = fileOutputStream.getChannel();

        //使用transferFrom完成拷贝
        destCh.transferFrom(sourceCh, 0, sourceCh.size());

        //关闭相关通道和流
        sourceCh.close();
        destCh.close();
        fileInputStream.close();
        fileOutputStream.close();
    }
}
