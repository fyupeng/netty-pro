package com.fyp.nio.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @Auther: fyp
 * @Date: 2022/2/6
 * @Description: 群聊系统服务端
 * @Package: com.fyp.nio.groupchat
 * @Version: 1.0
 */
public class GroupChatServer {

    private Selector selector;
    private ServerSocketChannel listenChannel;
    private static final int PORT = 6667;

    public GroupChatServer() {

        try {
            selector = Selector.open();
            listenChannel = ServerSocketChannel.open();
            listenChannel.socket().bind(new InetSocketAddress(PORT));
            listenChannel.configureBlocking(false);
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void listen() {
        try {
            while (true) {
                // count 获取的是 在阻塞过程中 同时发生的 事件 数，直到有事件 发生，才会执行，否则一直阻塞
                /*
                    select 方法在 没有 客户端发起连接时， 会一直阻塞，至少有一个客户端连接，其他 客户端再 发起连接 不再阻塞，会立即返回
                 */
                int count = selector.select();
                System.out.println(count);
                if(count > 0) {// 有事件 处理
                    // 遍历得到 SelectionKey 集合
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        //取出SelectionKey
                        SelectionKey key = iterator.next();
                        //监听到accept
                        if (key.isAcceptable()) {
                            SocketChannel sc = listenChannel.accept();
                            // 监听到 客户端 的 SocketChannel 总是默认为阻塞方式，需要重新设置
                            sc.configureBlocking(false);
                            //将该 sc 注册到 selector
                            sc.register(selector, SelectionKey.OP_READ);

                            System.out.println(sc.getRemoteAddress() + " 上线 ");
                        }

                        if (key.isReadable()) { // 通道发送 read 事件， 即通道是可读状态
                            //处理读
                            readData(key);
                        }
                        /*
                            每次  监听到 客户端后， selector会将 连接上的 客户端 选中， 并添加到 selectionKeys 中
                            要注册到 selector 上，使用该方法，selector 将 不再选中
                            如果没有移除，selector 不能选中 其他的 客户端连接
                            iterator.remove() 移除后，将释放 selector 中的 selectionKeys
                         */
                        iterator.remove();
                    }
                } else {
                    //System.out.println("等待....");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 发送异常处理
        }
    }

    public void readData(SelectionKey key) {
        // 取到关联的 channel
        SocketChannel channel = null;
        try {
            // 得到 channel
             channel = (SocketChannel) key.channel();

             // 创建buffer
             ByteBuffer buffer = ByteBuffer.allocate(1024);

             int count = channel.read(buffer);

             // 根据 count 的值 做 处理
            if (count > 0) {
                // 把 缓冲区 的 数据 转成 字符串
                String msg = new String(buffer.array());
                // 输出该消息
                System.out.println("from 客户端： " + msg);
                //想其他客户端转发消息，专门写一个方法来处理
                sendInfoToOtherClients(msg, channel);
            }

        } catch (IOException e) {
            //e.printStackTrace();
            try {
                System.out.println(channel.getRemoteAddress() + "离线了");
                // 取消 注册
                key.cancel();
                // 关闭通道
                channel.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // 转发消息给其他客户端（通道）
    private void sendInfoToOtherClients(String msg, SocketChannel self) throws IOException {
        System.out.println("服务器转发消息中....");
        // 遍历所有 注册到 selector 上 的 SockChannel, 并排除 self
        for (SelectionKey key : selector.keys()) {
            // 通过 key 取出 对应的 SocketChannel
            Channel targetChannel = key.channel();

            // 排除自己
            if (targetChannel instanceof SocketChannel && targetChannel != self) {
                // 转型
                SocketChannel dest = (SocketChannel) targetChannel;
                // 将 msg 存储到 buffer
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                // 将 buffer 的数据 写入 通道
                dest.write(buffer);
            }

        }
    }


    public static void main(String[] args) {
        // 创建 服务器 对象
        GroupChatServer groupChatServer = new GroupChatServer();
        groupChatServer.listen();
    }
}
