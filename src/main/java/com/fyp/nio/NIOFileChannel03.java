package com.fyp.nio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/4
 * @Description: 使用Channel读写文件
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOFileChannel03 {
    public static void main(String[] args) throws IOException {
        FileInputStream fileInputStream = new FileInputStream("1.txt");
        FileChannel fileChannel01 = fileInputStream.getChannel();

        FileOutputStream fileOutputStream = new FileOutputStream("2.txt");
        FileChannel fileChannel02 = fileOutputStream.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
        
        while (true) { //循环读数

            /*
                public final Buffer clear() {
                    this.position = 0;
                    this.limit = this.capacity;
                    this.mark = -1;
                    return this;
                }
             */
            byteBuffer.clear();

            int read = fileChannel01.read(byteBuffer);
            System.out.println("read= " + read);
            if (read == -1) { //表示读完
                break;
            }
            //将buffer 中的数据 写入到 fileChannel02 --- 2.txt
            byteBuffer.flip();
            fileChannel02.write(byteBuffer);


        }

        fileInputStream.close();


    }
}
