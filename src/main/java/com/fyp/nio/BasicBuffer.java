package com.fyp.nio;

import java.nio.IntBuffer;

/**
 * @Auther: fyp
 * @Date: 2022/1/31
 * @Description: 缓冲
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class BasicBuffer {
    public static void main(String[] args) {
        IntBuffer intBuffer = IntBuffer.allocate(5);

        //intBuffer.put(10);
        //intBuffer.put(10);
        //intBuffer.put(10);
        //intBuffer.put(10);
        //intBuffer.put(10);
        //存放数据
        for(int i = 0; i < intBuffer.capacity(); i++) {
            intBuffer.put(i * 2);
        }

        //读取数据
        intBuffer.flip();
        //intBuffer.position(2);
        //intBuffer.limit(3);

        while (intBuffer.hasRemaining()) {
            System.out.println(intBuffer.get());
        }


    }
}
