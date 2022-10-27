package com.fyp.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @Auther: fyp
 * @Date: 2022/2/5
 * @Description: NIO服务器
 * @Package: com.fyp.nio
 * @Version: 1.0
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        //创建ServerSocketChannel -> ServerSocket
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //得到一个Selector对象
        Selector selector = Selector.open();

        //绑定一个端口6666，在服务端监听
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));

        //设置为非阻塞
        serverSocketChannel.configureBlocking(false);

        //把 serverSocketChannel 注册到 selector, 关注事件 为 OP_ACCEPT
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //循环等待客户端连接
        while (true) {
            //这里我们等待1秒，如果没有事件发生（连接事件）
            if (selector.select(1000) == 0) {//没有事件发生
                System.out.println("服务器等待了1秒，无连接");
                continue;
            }

            //如果返回的 > 0, 就获取到相关的 selectionKey集合
            //1. 如果返回的 > 0, 表示已经获取到关注的事件
            //2. selector.selectedKeys() 返回关注事件的集合
            //通过 selectionKeys 反向获取通道
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            //遍历 Set<SelectionKey>, 使用迭代器
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()) {
                //获取到selectionKey
                SelectionKey key = keyIterator.next();
                //根据key 对应的通道发生的事件做相应处理
                if (key.isAcceptable()) { //如果是OP_ACCEPT, 表示新的客户端连接
                    //给该客户端生成一个 SocketChannel
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("客户端连接成功！生成了一个socketChannel " + socketChannel.hashCode());
                    //将 SocketChannel 设置为非阻塞
                    socketChannel.configureBlocking(false);
                    //将socketChannel注册到 selector上, 关注事件为OP_READ, 同时给socketChannel
                    //关联一个Buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                    System.out.println("客户端连接后 ，注册的selectionkey 数量=" + selector.keys().size());
                }
                if (key.isReadable()) { //发生OP_READ
                    // 通过key 反向获取对应的channel
                    SocketChannel channel = (SocketChannel) key.channel();
                    //获取该channel关联的 buffer,在与客户端连接就已经创建好了
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    channel.read(buffer);
                    System.out.println("from 客户端： " + new String(buffer.array()));
                }
                //手动从集合中移动当前的selectionKey, 防止重复操作
                keyIterator.remove();
            }

        }
    }
}
