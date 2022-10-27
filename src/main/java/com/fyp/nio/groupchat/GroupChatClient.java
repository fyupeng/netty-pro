package com.fyp.nio.groupchat;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @Auther: fyp
 * @Date: 2022/2/7
 * @Description: 群聊系统客户端
 * @Package: com.fyp.nio.groupchat
 * @Version: 1.0
 */
public class GroupChatClient {

    //定义 相关 属性
    private final String HOST = "127.0.0.1";
    private final int PORT = 6667;
    private Selector selector;
    private SocketChannel socketChannel;
    private String username;

    // 构造器，完成初始化工作
    public GroupChatClient() throws IOException {

        selector = Selector.open();
        // 连接 服务器
        socketChannel = socketChannel.open(new InetSocketAddress("127.0.0.1", PORT));
        // 设置 非阻塞
        socketChannel.configureBlocking(false);
        // 将 channel 注册到 selector
        socketChannel.register(selector, SelectionKey.OP_READ);
        // 得到 username
        username = socketChannel.getLocalAddress().toString().substring(1);
        System.out.println(username + " is ok....");

    }

    // 向 服务器 发送 消息
    public void sendInfo(String info) {

        info = username + " 说：" + info;

        try {
            socketChannel.write(ByteBuffer.wrap(info.getBytes()));

        } catch (Exception e) {

        }
    }

    // 读取从 服务器 端 回复的 消息
    public void readInfo() {
        try {
            int readChannels = selector.select();
            if (readChannels > 0) {// 有可以用的 通道

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {

                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {
                        // 得到 相关的 通道
                        SocketChannel sc = (SocketChannel) key.channel();
                        // 得到一个 Buffer
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        // 读取
                        sc.read(buffer);
                        // 把缓存区的数据 转成 字符串
                        String msg = new String(buffer.array());
                        System.out.println(msg.trim());
                    }
                }
                iterator.remove();// 删除当前的selectionKey, 防止重复操作
            } else {
                //System.out.println("没有可以用的通道....");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // 启动 客户端
        GroupChatClient chatClient = new GroupChatClient();
        // 启动一个线程，每隔3秒， 读取从 服务器 发送过来的数据
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    chatClient.readInfo();

                    try {
                        Thread.currentThread().sleep(3000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        // 发送数据给 服务端
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            chatClient.sendInfo(s);
        }

    }

}
