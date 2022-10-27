package com.fyp.nio.zerocopy;



import java.io.*;
import java.net.Socket;

/**
 * @Auther: fyp
 * @Date: 2022/2/9
 * @Description: 传统IO客户端
 * @Package: com.fyp.nio.zerocopy
 * @Version: 1.0
 */
public class OldIOClient {

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("localhost", 7001);

        String fileName = "aiXcoder-3.3.1-2020.zip";
        InputStream inputStream = new FileInputStream(fileName);

        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        byte[] buffer = new byte[4096];
        long readCount;
        long total = 0;

        long startTime = System.currentTimeMillis();

        while ((readCount = inputStream.read(buffer)) > 0) {
            total += readCount;
            dataOutputStream.write(buffer, 0 , (int) readCount);
        }

        System.out.println("发送总字节数： " + total + ", 耗时： " + (System.currentTimeMillis() - startTime));


        dataOutputStream.close();
        socket.close();
        inputStream.close();
    }

}
