package com.fyp.bio;

import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Auther: fyp
 * @Date: 2022/1/31
 * @Description: BIO服务
 * @Package: com.fyp.bio
 * @Version: 1.0
 */
public class BIOServer {
    public static void main(String[] args) throws IOException {
        /**
         * 思路：
         * 1. 创建一个线程池
         * 2. 如果有客户链接，就创建一个线程，与之通讯（单独写一个方法）
         */
        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();

        //创建ServerSocket
        ServerSocket serverSocket = new ServerSocket(6666);

        System.out.println("服务器启动了");

        while (true) {
            System.out.println("等待连接");
            //监听，等待客户端链接
            final Socket socket = serverSocket.accept();
            System.out.println("连接到一个客户端");

            //创建一个线程，与之通讯（单独写一个方法）
            newCachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    //可以客户端通讯
                    handler(socket);
                }
            });


        }
    }

    //编写一个handle方法，和客户端通讯
    public static void handler(Socket socket) {
        try {
            System.out.println("线程信息 id = " + Thread.currentThread().getId() + " 名字 = " + Thread.currentThread().getName());
            byte[] bytes = new byte[10];
            //通过Socket获取输入流
            InputStream inputStream = socket.getInputStream();

            while (true) {
                System.out.println("read....");
                //没有读到数据会阻塞
                int read = inputStream.read(bytes);
                if(read != -1) {
                    System.out.println("线程信息 id = " + Thread.currentThread().getId() + " 名字 = " + Thread.currentThread().getName() + " -- " + new String(bytes, 0, read));
                } else {
                    System.out.println("break");
                    break;
                }
            }
            System.out.println("跳出read循环");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("关闭和client的连接");
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
