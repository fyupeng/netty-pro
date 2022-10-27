package com.fyp.nio.zerocopy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @Auther: fyp
 * @Date: 2022/2/9
 * @Description: 新IO服务端
 * @Package: com.fyp.nio.zerocopy
 * @Version: 1.0
 */
public class NewIOServer {

    public static void main(String[] args) throws IOException {

        InetSocketAddress inetSocketAddress = new InetSocketAddress(7001);

        ServerSocketChannel serverSockChannel = ServerSocketChannel.open();

        ServerSocket serverSocket = serverSockChannel.socket();

        serverSocket.bind(inetSocketAddress);

        // 创建 buffer
        ByteBuffer buffer = ByteBuffer.allocate(4096);

        while (true) {
             SocketChannel socketChannel = serverSockChannel.accept();

             int readCount = 0;
             while (-1 != readCount) {

                 try {

                     readCount = socketChannel.read(buffer);


                 } catch (Exception e) {
                     //e.printStackTrace();
                     break;
                 }
                 //
                 buffer.rewind(); // 倒带 position = 0 , mark 作废

             }

        }


    }

}
