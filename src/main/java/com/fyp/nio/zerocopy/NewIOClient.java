package com.fyp.nio.zerocopy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/9
 * @Description: 新IO客户端
 * @Package: com.fyp.nio.zerocopy
 * @Version: 1.0
 */
public class NewIOClient {

    public static void main(String[] args) throws IOException {

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 7001));
        String filename = "aiXcoder-3.3.1-2020.zip";

        // 得到 一个 文件 channel
        FileChannel fileChannel = new FileInputStream(filename).getChannel();

        // 准备 发送
        long startTime = System.currentTimeMillis();

        // 在 linux 的 下一个 transferTo 方法就可以 完成 传输
        // 在 windows 下一次 调用 transferTo 只能发送 8M
        // 需要 分段 传输文件，而且 主要 传输时 的位置 transferTo 底层用到零拷贝
        long transferCount = fileChannel.transferTo(0, fileChannel.size(), socketChannel);

        System.out.println("发送总字节数： " + transferCount + ", 耗时： " + (System.currentTimeMillis() - startTime));

        fileChannel.close();
    }

}
