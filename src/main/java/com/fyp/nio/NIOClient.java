package com.fyp.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/6
 * @Description: 客户端
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOClient {
    public static void main(String[] args) throws IOException {
        //得到一个网络通道
        SocketChannel socketChannel = SocketChannel.open();
        //设置非阻塞
        socketChannel.configureBlocking(false);
        //提供服务端的 ip 和 端口
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);
        /*
            连接服务器
            1. 为非阻塞模式时，即不会等到方法执行完毕再返回，会立即返回，如果返回前已经连接成功，则返回true
            返回false 时，说明未连接成功，所以需要再通过while循环地finishConnect()完成最终的连接
            2. 为阻塞模式时，直到连接建立或抛出异常
            不会返回false，连接不上就抛异常，不需要借助finishConnect()
         */
        if (!socketChannel.connect(inetSocketAddress)) {
            while (!socketChannel.finishConnect()) {
                System.out.println("因为连接需要时间，客户端不会阻塞，可以做其他工作");
            }
        }
        //如果连接成功，就发送数据
        String str = "hello, 尚硅谷";
        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());
        //发送数据，将buffer 写入 channel
        socketChannel.write(buffer);
        System.in.read();


    }
}
