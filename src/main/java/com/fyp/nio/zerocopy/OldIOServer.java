package com.fyp.nio.zerocopy;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Auther: fyp
 * @Date: 2022/2/9
 * @Description: 传统IO服务端
 * @Package: com.fyp.nio.zerocopy
 * @Version: 1.0
 */
public class OldIOServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(7001);

        while (true) {
            Socket socket = serverSocket.accept();
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            try {

                byte[] byteArray = new byte[4096];

                while (true) {
                    int readCount = dataInputStream.read(byteArray, 0, byteArray.length);

                    if (-1 == readCount) {
                        break;
                    }

                    System.out.println("读取字节数： " + readCount);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
